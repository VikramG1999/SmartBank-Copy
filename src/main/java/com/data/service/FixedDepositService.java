package com.data.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.data.dto.FdRequest;
import com.data.model.FixedDeposit;

public interface FixedDepositService {

	Map<String, Object> getOptions();

	Map<String, Object> quote(BigDecimal amount, int months);

	FixedDeposit open(String email, String accountNumber, FdRequest request);

	List<FixedDeposit> getMyDeposits(String email);

	FixedDeposit close(String email, Long id);

	List<FixedDeposit> getAllDeposits(String status);
}