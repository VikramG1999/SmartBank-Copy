package com.data.service;

import java.util.Map;

import com.data.dto.AuthResponse;
import com.data.dto.ForgotPasswordRequest;
import com.data.dto.LoginRequest;
import com.data.dto.RegisterRequest;
import com.data.dto.ResetPasswordRequest;

public interface AuthService {

	AuthResponse register(RegisterRequest request);

	AuthResponse login(LoginRequest request);

	Map<String, String> forgotPassword(ForgotPasswordRequest request);

	Map<String, String> resetPassword(ResetPasswordRequest request);
}