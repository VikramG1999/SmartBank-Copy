package com.data.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.data.dto.FdRequest;
import com.data.exception.BadRequestException;
import com.data.exception.ResourceNotFoundException;
import com.data.model.Account;
import com.data.model.FixedDeposit;
import com.data.repository.AccountRepository;
import com.data.repository.FixedDepositRepository;

@Service
public class FixedDepositServiceImpl implements FixedDepositService {

	// One fixed rate for every tenure.
	private static final BigDecimal ANNUAL_RATE = new BigDecimal("6.50");
	private static final BigDecimal MIN_AMOUNT = new BigDecimal("1000");
	private static final List<Integer> TENURES = List.of(3, 6, 12, 24, 36);
	private static final String FORMULA = "Maturity amount = Principal + (Principal x Annual rate x Months) / 1200 (simple interest)";

	@Autowired
	private FixedDepositRepository repository;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private AccountService accountService;

	@Override
	public Map<String, Object> getOptions() {
		Map<String, Object> options = new LinkedHashMap<>();
		options.put("annualRatePercent", ANNUAL_RATE);
		options.put("tenuresInMonths", TENURES);
		options.put("minimumAmount", MIN_AMOUNT);
		options.put("formula", FORMULA);
		return options;
	}

	@Override
	public Map<String, Object> quote(BigDecimal amount, int months) {
		validate(amount, months);
		BigDecimal maturity = maturityAmount(amount, months);
		Map<String, Object> quote = new LinkedHashMap<>();
		quote.put("principal", amount);
		quote.put("months", months);
		quote.put("annualRatePercent", ANNUAL_RATE);
		quote.put("interest", maturity.subtract(amount));
		quote.put("maturityAmount", maturity);
		quote.put("maturityDate", LocalDate.now().plusMonths(months));
		return quote;
	}

	@Override
	@Transactional
	public FixedDeposit open(String email, String accountNumber, FdRequest request) {
		int months = request.getMonths();
		validate(request.getAmount(), months);

		Account account = accountService.resolveAccount(email, accountNumber);
		accountService.debit(account, request.getAmount(), "FD_OPEN", null);

		FixedDeposit fd = new FixedDeposit();
		fd.setReferenceNo("FD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		fd.setOwnerEmail(email);
		fd.setAccountNumber(account.getAccountNumber());
		fd.setPrincipal(request.getAmount());
		fd.setAnnualRate(ANNUAL_RATE);
		fd.setMonths(months);
		fd.setMaturityAmount(maturityAmount(request.getAmount(), months));
		fd.setStartDate(LocalDate.now());
		fd.setMaturityDate(LocalDate.now().plusMonths(months));
		return repository.save(fd);
	}

	@Override
	public List<FixedDeposit> getMyDeposits(String email) {
		return repository.findByOwnerEmailOrderByIdDesc(email);
	}

	@Override
	@Transactional
	public FixedDeposit close(String email, Long id) {
		FixedDeposit fd = repository.findByIdAndOwnerEmail(id, email)
				.orElseThrow(() -> new ResourceNotFoundException("Fixed deposit not found"));
		if (!"ACTIVE".equals(fd.getStatus())) {
			throw new BadRequestException("Fixed deposit is already closed");
		}
		if (LocalDate.now().isBefore(fd.getMaturityDate())) {
			throw new BadRequestException("Fixed deposit has not matured yet. Maturity date: " + fd.getMaturityDate());
		}
		Account account = accountRepository.findByAccountNumber(fd.getAccountNumber())
				.orElseThrow(() -> new ResourceNotFoundException("Account not found"));
		accountService.credit(account, fd.getMaturityAmount(), "FD_MATURITY", null);

		fd.setStatus("MATURED");
		fd.setClosedAt(LocalDateTime.now());
		return repository.save(fd);
	}

	@Override
	public List<FixedDeposit> getAllDeposits(String status) {
		if (status == null || status.isBlank()) {
			return repository.findAllByOrderByIdDesc();
		}
		return repository.findByStatus(status.trim().toUpperCase());
	}

	// ---------- helper methods ----------

	private void validate(BigDecimal amount, int months) {
		if (amount.compareTo(MIN_AMOUNT) < 0) {
			throw new BadRequestException("Minimum deposit amount is " + MIN_AMOUNT);
		}
		if (!TENURES.contains(months)) {
			throw new BadRequestException("Tenure must be one of " + TENURES + " months");
		}
	}

	private BigDecimal maturityAmount(BigDecimal principal, int months) {
		BigDecimal interest = principal.multiply(ANNUAL_RATE).multiply(BigDecimal.valueOf(months))
				.divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
		return principal.add(interest);
	}
}