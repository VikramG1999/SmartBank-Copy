package com.data.service;

import java.util.List;

import com.data.dto.BeneficiaryRequest;
import com.data.model.Beneficiary;

public interface BeneficiaryService {

	Beneficiary addBeneficiary(String email, BeneficiaryRequest request);

	List<Beneficiary> getMyBeneficiaries(String email);

	void deleteBeneficiary(String email, Long id);
}