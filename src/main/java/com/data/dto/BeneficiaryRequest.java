package com.data.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BeneficiaryRequest {

	@NotBlank(message = "Account number is required")
	private String accountNumber;

	@Size(max = 30, message = "Nickname can have at most 30 characters")
	private String nickname;
}