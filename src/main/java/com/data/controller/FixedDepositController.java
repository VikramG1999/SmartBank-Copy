package com.data.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.data.dto.FdRequest;
import com.data.model.FixedDeposit;
import com.data.service.FixedDepositService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fd")
public class FixedDepositController {

	@Autowired
	private FixedDepositService service;

	@GetMapping("/options")
	public ResponseEntity<Map<String, Object>> options() {
		return ResponseEntity.ok(service.getOptions());
	}

	@GetMapping("/quote")
	public ResponseEntity<Map<String, Object>> quote(@RequestParam BigDecimal amount, @RequestParam Integer months) {
		return ResponseEntity.ok(service.quote(amount, months));
	}

	@PostMapping
	public ResponseEntity<FixedDeposit> open(@Valid @RequestBody FdRequest request,
			@RequestHeader(value = "X-Account-Number", required = false) String acc, Authentication auth) {
		return new ResponseEntity<FixedDeposit>(service.open(auth.getName(), acc, request), HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<FixedDeposit>> getMine(Authentication auth) {
		return ResponseEntity.ok(service.getMyDeposits(auth.getName()));
	}

	@PostMapping("/{id}/close")
	public ResponseEntity<FixedDeposit> close(@PathVariable Long id, Authentication auth) {
		return ResponseEntity.ok(service.close(auth.getName(), id));
	}
}