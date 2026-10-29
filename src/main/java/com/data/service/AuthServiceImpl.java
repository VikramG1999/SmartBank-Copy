package com.data.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.data.dto.AuthResponse;
import com.data.dto.ForgotPasswordRequest;
import com.data.dto.LoginRequest;
import com.data.dto.RegisterRequest;
import com.data.dto.ResetPasswordRequest;
import com.data.exception.BadRequestException;
import com.data.exception.ResourceNotFoundException;
import com.data.model.User;
import com.data.repository.UserRepository;
import com.data.security.JwtUtil;

@Service
public class AuthServiceImpl implements AuthService {

	private static final int RESET_MINUTES = 15;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AccountService accountService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired(required = false)
	private JavaMailSender mailSender;

	@Value("${app.mail.from:}")
	private String mailFrom;
	@Value("${app.frontend-url:http://localhost:5173}")
	private String frontendUrl;

	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new BadRequestException("Email is already registered");
		}
		User user = new User();
		user.setName(request.getName().trim());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		userRepository.save(user);

		accountService.createAccount(user, request.getAccountType());

		return new AuthResponse(jwtUtil.generateToken(user.getEmail(), user.getRole()), user.getName(), user.getRole());
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		} catch (AuthenticationException e) {
			throw new BadRequestException("Invalid email or password");
		}
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return new AuthResponse(jwtUtil.generateToken(user.getEmail(), user.getRole()), user.getName(), user.getRole());
	}

	@Override
	public Map<String, String> forgotPassword(ForgotPasswordRequest request) {
		userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
			byte[] bytes = new byte[32];
			new SecureRandom().nextBytes(bytes);
			String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

			user.setResetCode(passwordEncoder.encode(token));
			user.setResetExpiry(LocalDateTime.now().plusMinutes(RESET_MINUTES));
			userRepository.save(user);

			String link = frontendUrl + "/reset-password?email="
					+ URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8) + "&token=" + token;
			sendResetEmail(user, link);
		});
		return Map.of("message", "If this email is registered, a password reset link has been sent");
	}

	@Override
	public Map<String, String> resetPassword(ResetPasswordRequest request) {
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new BadRequestException("Invalid or expired reset link"));
		if (user.getResetCode() == null || user.getResetExpiry() == null
				|| user.getResetExpiry().isBefore(LocalDateTime.now())
				|| !passwordEncoder.matches(request.getToken(), user.getResetCode())) {
			throw new BadRequestException("Invalid or expired reset link");
		}
		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		user.setResetCode(null);
		user.setResetExpiry(null);
		userRepository.save(user);
		return Map.of("message", "Password changed. Please login");
	}

	private void sendResetEmail(User user, String link) {
		String body = "Hello " + user.getName() + ",\n\n"
				+ "We received a request to reset your SmartBank password.\n\n"
				+ "Click the link below to reset your password:\n\n"
				+ link + "\n\n"
				+ "This link is valid for " + RESET_MINUTES + " minutes and can be used only once.\n"
				+ "If you did not request this, you can ignore this email. Your password will not change.\n\n"
				+ "- SmartBank";

		if (mailSender != null && !mailFrom.isBlank()) {
			try {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(mailFrom);
				message.setTo(user.getEmail());
				message.setSubject("Reset your SmartBank password");
				message.setText(body);
				mailSender.send(message);
				return;
			} catch (Exception e) {
				System.out.println("Email could not be sent: " + e.getMessage());
			}
		}
		System.out.println("RESET LINK (email not configured) for " + user.getEmail() + " : " + link);
	}
}