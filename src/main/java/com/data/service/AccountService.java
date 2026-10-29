package com.data.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.data.dto.OpenAccountRequest;
import com.data.dto.TransferRequest;
import com.data.model.Account;
import com.data.model.User;

public interface AccountService {

	Account createAccount(User user, String type);

	List<Account> getMyAccounts(String email);

	Account openAccount(String email, OpenAccountRequest request);

	Account resolveAccount(String email, String accountNumber);

	Account deposit(String email, String accountNumber, BigDecimal amount);

	Account withdraw(String email, String accountNumber, BigDecimal amount);

	Account transfer(String email, String accountNumber, TransferRequest request);

	Account credit(Account account, BigDecimal amount, String type, String category);

	Account debit(Account account, BigDecimal amount, String type, String category);

	List<User> getAllUsers();

	List<Account> getAllAccounts();

	Account freezeAccount(String accountNumber);

	Account unfreezeAccount(String accountNumber);

	Map<String, Object> getStats();

	Account adminDeposit(String accountNumber, BigDecimal amount);

	Account adminWithdraw(String accountNumber, BigDecimal amount);
}