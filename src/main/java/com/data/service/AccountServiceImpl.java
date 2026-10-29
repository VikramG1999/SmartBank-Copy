package com.data.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.data.dto.OpenAccountRequest;
import com.data.dto.TransferRequest;
import com.data.exception.BadRequestException;
import com.data.exception.ResourceNotFoundException;
import com.data.model.Account;
import com.data.model.Transaction;
import com.data.model.User;
import com.data.repository.AccountRepository;
import com.data.repository.FixedDepositRepository;
import com.data.repository.TransactionRepository;
import com.data.repository.UserRepository;

@Service
public class AccountServiceImpl implements AccountService {

	private static final BigDecimal DAILY_LIMIT = new BigDecimal("50000");
	private static final int MAX_ACCOUNTS = 3;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private FixedDepositRepository fixedDepositRepository;

	@Override
	public Account createAccount(User user, String type) {
		Account account = new Account();
		account.setUser(user);
		account.setAccountType(type == null || type.isBlank() ? "SAVINGS" : type);
		String number;
		do {
			number = "SB" + ThreadLocalRandom.current().nextLong(1000000000L, 10000000000L);
		} while (accountRepository.existsByAccountNumber(number));
		account.setAccountNumber(number);
		return accountRepository.save(account);
	}

	@Override
	public List<Account> getMyAccounts(String email) {
		return accountRepository.findByUserEmailOrderByIdAsc(email);
	}

	@Override
	@Transactional
	public Account openAccount(String email, OpenAccountRequest request) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		if (getMyAccounts(email).size() >= MAX_ACCOUNTS) {
			throw new BadRequestException("You can have at most " + MAX_ACCOUNTS + " accounts");
		}
		return createAccount(user, request.getAccountType());
	}

	@Override
	public Account resolveAccount(String email, String accountNumber) {
		List<Account> list = getMyAccounts(email);
		if (list.isEmpty()) {
			throw new ResourceNotFoundException("Account not found");
		}
		if (accountNumber == null || accountNumber.isBlank()) {
			return list.get(0);
		}
		return list.stream().filter(a -> a.getAccountNumber().equals(accountNumber.trim())).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));
	}

	@Override
	@Transactional
	public Account deposit(String email, String accountNumber, BigDecimal amount) {
		return credit(resolveAccount(email, accountNumber), amount, "DEPOSIT", "Simulated");
	}

	@Override
	@Transactional
	public Account withdraw(String email, String accountNumber, BigDecimal amount) {
		return debit(resolveAccount(email, accountNumber), amount, "WITHDRAW", "Simulated");
	}

	@Override
	@Transactional
	public Account transfer(String email, String accountNumber, TransferRequest request) {
		Account sender = resolveAccount(email, accountNumber);
		checkActive(sender, "Your account is frozen");

		Account receiver = accountRepository.findByAccountNumber(request.getToAccount().trim().toUpperCase())
				.orElseThrow(() -> new ResourceNotFoundException("Receiver account not found"));
		checkActive(receiver, "Receiver account is frozen");

		if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
			throw new BadRequestException("You cannot transfer to the same account");
		}
		BigDecimal amount = request.getAmount();
		if (sender.getBalance().compareTo(amount) < 0) {
			throw new BadRequestException("Insufficient balance");
		}
		BigDecimal sentToday = transactionRepository.sumTransfersSince(
				sender.getAccountNumber(), LocalDate.now().atStartOfDay());
		if (sentToday.add(amount).compareTo(DAILY_LIMIT) > 0) {
			throw new BadRequestException("Daily transfer limit of " + DAILY_LIMIT + " exceeded");
		}

		sender.setBalance(sender.getBalance().subtract(amount));
		receiver.setBalance(receiver.getBalance().add(amount));
		accountRepository.save(receiver);
		Account saved = accountRepository.save(sender);
		saveTransaction(saved.getAccountNumber(), receiver.getAccountNumber(), amount, "TRANSFER", null,
				saved.getBalance(), receiver.getBalance());
		return saved;
	}

	@Override
	@Transactional
	public Account credit(Account account, BigDecimal amount, String type, String category) {
		checkActive(account, "Your account is frozen");
		account.setBalance(account.getBalance().add(amount));
		Account saved = accountRepository.save(account);
		saveTransaction(null, saved.getAccountNumber(), amount, type, category, null, saved.getBalance());
		return saved;
	}

	@Override
	@Transactional
	public Account debit(Account account, BigDecimal amount, String type, String category) {
		checkActive(account, "Your account is frozen");
		if (account.getBalance().compareTo(amount) < 0) {
			throw new BadRequestException("Insufficient balance");
		}
		account.setBalance(account.getBalance().subtract(amount));
		Account saved = accountRepository.save(account);
		saveTransaction(saved.getAccountNumber(), null, amount, type, category, saved.getBalance(), null);
		return saved;
	}

	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public List<Account> getAllAccounts() {
		return accountRepository.findAll();
	}

	@Override
	public Account freezeAccount(String accountNumber) {
		return changeStatus(accountNumber, "FROZEN");
	}

	@Override
	public Account unfreezeAccount(String accountNumber) {
		return changeStatus(accountNumber, "ACTIVE");
	}

	@Override
	public Map<String, Object> getStats() {
		Map<String, Object> stats = new LinkedHashMap<>();
		stats.put("totalCustomers", userRepository.countByRole("CUSTOMER"));
		stats.put("totalAccounts", accountRepository.count());
		stats.put("frozenAccounts", accountRepository.countByStatus("FROZEN"));
		stats.put("totalTransactions", transactionRepository.count());
		stats.put("totalBalance", accountRepository.totalBalance());
		stats.put("activeFixedDeposits", fixedDepositRepository.countByStatus("ACTIVE"));
		stats.put("maturedFixedDeposits", fixedDepositRepository.countByStatus("MATURED"));
		return stats;
	}
	
	@Override
	@Transactional
	public Account adminDeposit(String accountNumber, BigDecimal amount) {
		Account account = accountRepository.findByAccountNumber(accountNumber.trim().toUpperCase())
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));
		return credit(account, amount, "DEPOSIT", "Simulated");
	}

	@Override
	@Transactional
	public Account adminWithdraw(String accountNumber, BigDecimal amount) {
		Account account = accountRepository.findByAccountNumber(accountNumber.trim().toUpperCase())
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));
		return debit(account, amount, "WITHDRAW", "Simulated");
	}

	// ---------- helper methods ----------

	private void checkActive(Account account, String message) {
		if ("FROZEN".equals(account.getStatus())) {
			throw new BadRequestException(message);
		}
	}

	private Account changeStatus(String accountNumber, String status) {
		Account account = accountRepository.findByAccountNumber(accountNumber.trim().toUpperCase())
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));
		account.setStatus(status);
		return accountRepository.save(account);
	}

	private void saveTransaction(String from, String to, BigDecimal amount, String type, String category,
			BigDecimal fromBalanceAfter, BigDecimal toBalanceAfter) {
		Transaction t = new Transaction();
		t.setReferenceNo("TXN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		t.setFromAccount(from);
		t.setToAccount(to);
		t.setAmount(amount);
		t.setFromBalanceAfter(fromBalanceAfter);
		t.setToBalanceAfter(toBalanceAfter);
		t.setType(type);
		t.setCategory(category);
		t.setStatus("SUCCESS");
		transactionRepository.save(t);
	}
}