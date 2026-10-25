package com.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

	@NotBlank(message = "Email is required")
	@Email(message = "Enter a valid email")
	private String email;

	@NotBlank(message = "Reset token is required")
	private String token;

	@NotBlank(message = "New password is required")
	@Size(min = 6, message = "Password must be at least 6 characters")
	private String newPassword;

	public void setEmail(String email) {
		this.email = (email == null) ? null : email.trim().toLowerCase();
	}
}