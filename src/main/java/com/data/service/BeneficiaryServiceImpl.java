package com.data.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.data.dto.BeneficiaryRequest;
import com.data.exception.BadRequestException;
import com.data.exception.ResourceNotFoundException;
import com.data.model.Account;
import com.data.model.Beneficiary;
import com.data.repository.AccountRepository;
import com.data.repository.BeneficiaryRepository;

@Service
public class BeneficiaryServiceImpl implements BeneficiaryService {

	@Autowired
	private BeneficiaryRepository repository;
	@Autowired
	private AccountRepository accountRepository;

	@Override
	public Beneficiary addBeneficiary(String email, BeneficiaryRequest request) {
		String number = request.getAccountNumber().trim().toUpperCase();
		Account account = accountRepository.findByAccountNumber(number)
				.orElseThrow(() -> new ResourceNotFoundException("Account number not found"));
		if (account.getUser().getEmail().equals(email)) {
			throw new BadRequestException("You cannot add your own account");
		}
		if (repository.existsByOwnerEmailAndAccountNumber(email, number)) {
			throw new BadRequestException("Beneficiary already added");
		}
		String holderName = account.getUser().getName();
		Beneficiary b = new Beneficiary();
		b.setOwnerEmail(email);
		b.setAccountNumber(number);
		b.setAccountHolderName(holderName);
		b.setNickname(request.getNickname() == null || request.getNickname().isBlank() ? holderName
				: request.getNickname().trim());
		return repository.save(b);
	}

	@Override
	public List<Beneficiary> getMyBeneficiaries(String email) {
		return repository.findByOwnerEmail(email);
	}

	@Override
	public void deleteBeneficiary(String email, Long id) {
		Beneficiary b = repository.findByIdAndOwnerEmail(id, email)
				.orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
		repository.delete(b);
	}
}