package com.data.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.data.dto.AmountRequest;
import com.data.dto.OpenAccountRequest;
import com.data.dto.TransferRequest;
import com.data.model.Account;
import com.data.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AccountController {

	@Autowired
	private AccountService service;

	@GetMapping("/accounts")
	public ResponseEntity<List<Account>> myAccounts(Authentication auth) {
		return ResponseEntity.ok(service.getMyAccounts(auth.getName()));
	}

	@PostMapping("/accounts")
	public ResponseEntity<Account> openAccount(@Valid @RequestBody OpenAccountRequest request, Authentication auth) {
		return new ResponseEntity<Account>(service.openAccount(auth.getName(), request), HttpStatus.CREATED);
	}

	@GetMapping("/account/me")
	public ResponseEntity<Account> myAccount(@RequestHeader(value = "X-Account-Number", required = false) String acc,
			Authentication auth) {
		return ResponseEntity.ok(service.resolveAccount(auth.getName(), acc));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/account/deposit")
	public ResponseEntity<Account> deposit(@Valid @RequestBody AmountRequest request,
			@RequestHeader(value = "X-Account-Number", required = false) String acc, Authentication auth) {
		return ResponseEntity.ok(service.deposit(auth.getName(), acc, request.getAmount()));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/account/withdraw")
	public ResponseEntity<Account> withdraw(@Valid @RequestBody AmountRequest request,
			@RequestHeader(value = "X-Account-Number", required = false) String acc, Authentication auth) {
		return ResponseEntity.ok(service.withdraw(auth.getName(), acc, request.getAmount()));
	}

	@PostMapping("/account/transfer")
	public ResponseEntity<Account> transfer(@Valid @RequestBody TransferRequest request,
			@RequestHeader(value = "X-Account-Number", required = false) String acc, Authentication auth) {
		return ResponseEntity.ok(service.transfer(auth.getName(), acc, request));
	}
}