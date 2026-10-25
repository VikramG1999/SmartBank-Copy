package com.data.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@JsonIgnore
	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String role = "CUSTOMER";

	private String phone;

	@Column(length = 300)
	private String address;

	@JsonIgnore
	@Column(length = 100)
	private String transactionPin;

	@JsonIgnore
	@Column(length = 100)
	private String resetCode;

	@JsonIgnore
	private LocalDateTime resetExpiry;

	@JsonIgnore
	@Column(length = 100)
	private String pinResetCode;

	@JsonIgnore
	private LocalDateTime pinResetExpiry;

	@JsonIgnore
	@Column(columnDefinition = "LONGTEXT")
	private String profilePicture;
}