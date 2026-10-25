package com.data.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.data.dto.BeneficiaryRequest;
import com.data.model.Beneficiary;
import com.data.service.BeneficiaryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

	@Autowired
	private BeneficiaryService service;

	@PostMapping
	public ResponseEntity<Beneficiary> add(@Valid @RequestBody BeneficiaryRequest request, Authentication auth) {
		return new ResponseEntity<Beneficiary>(service.addBeneficiary(auth.getName(), request), HttpStatus.CREATED);
	}

	@GetMapping
	public ResponseEntity<List<Beneficiary>> getAll(Authentication auth) {
		return ResponseEntity.ok(service.getMyBeneficiaries(auth.getName()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Map<String, String>> delete(@PathVariable Long id, Authentication auth) {
		service.deleteBeneficiary(auth.getName(), id);
		return ResponseEntity.ok(Map.of("message", "Beneficiary removed"));
	}
}