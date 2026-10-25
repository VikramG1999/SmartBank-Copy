package com.data.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequest {

	@NotBlank(message = "Receiver account number is required")
	private String toAccount;

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "0.01", message = "Amount must be greater than 0")
	@Digits(integer = 13, fraction = 2, message = "Amount can have only 2 digits after the decimal point")
	private BigDecimal amount;
}