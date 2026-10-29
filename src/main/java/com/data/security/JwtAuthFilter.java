package com.data.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String header = request.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);

			if (jwtUtil.isTokenValid(token)) {
				try {
					UserDetails user = userDetailsService.loadUserByUsername(jwtUtil.extractEmail(token));
					UsernamePasswordAuthenticationToken auth =
							new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(auth);
				} catch (UsernameNotFoundException e) {
					// user no longer exists: continue unauthenticated, Spring returns 401
				}
			}
		}
		filterChain.doFilter(request, response);
	}
}