package com.data.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.data.model.Account;
import com.data.model.FixedDeposit;
import com.data.model.User;
import com.data.service.AccountService;
import com.data.service.FixedDepositService;
import com.data.service.TransactionService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	private AccountService accountService;
	@Autowired
	private TransactionService transactionService;
	@Autowired
	private FixedDepositService fixedDepositService;

	@GetMapping("/stats")
	public ResponseEntity<Map<String, Object>> stats() {
		return ResponseEntity.ok(accountService.getStats());
	}

	@GetMapping("/users")
	public ResponseEntity<List<User>> users() {
		return ResponseEntity.ok(accountService.getAllUsers());
	}

	@GetMapping("/accounts")
	public ResponseEntity<List<Account>> accounts() {
		return ResponseEntity.ok(accountService.getAllAccounts());
	}

	@PutMapping("/freeze/{accountNumber}")
	public ResponseEntity<Account> freeze(@PathVariable String accountNumber) {
		return ResponseEntity.ok(accountService.freezeAccount(accountNumber));
	}

	@PutMapping("/unfreeze/{accountNumber}")
	public ResponseEntity<Account> unfreeze(@PathVariable String accountNumber) {
		return ResponseEntity.ok(accountService.unfreezeAccount(accountNumber));
	}

	@GetMapping("/transactions")
	public ResponseEntity<Map<String, Object>> transactions(
			@RequestParam(defaultValue = "") String account,
			@RequestParam(defaultValue = "") String type,
			@RequestParam(defaultValue = "") String from,
			@RequestParam(defaultValue = "") String to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(transactionService.getAllTransactions(account, type, from, to,
				Math.max(page, 0), Math.min(Math.max(size, 1), 50)));
	}

	@GetMapping("/fd")
	public ResponseEntity<List<FixedDeposit>> fixedDeposits(@RequestParam(defaultValue = "") String status) {
		return ResponseEntity.ok(fixedDepositService.getAllDeposits(status));
	}
}