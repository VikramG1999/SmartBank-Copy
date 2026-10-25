package com.data.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FdRequest {

	@NotNull(message = "Amount is required")
	@DecimalMin(value = "1000", message = "Minimum deposit amount is 1000")
	@Digits(integer = 13, fraction = 2, message = "Amount can have only 2 digits after the decimal point")
	private BigDecimal amount;

	@NotNull(message = "Tenure is required")
	private Integer months;
}