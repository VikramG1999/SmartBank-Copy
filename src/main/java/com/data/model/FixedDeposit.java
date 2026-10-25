package com.data.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fixed_deposits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FixedDeposit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String referenceNo;

	@Column(nullable = false)
	private String ownerEmail;

	@Column(nullable = false)
	private String accountNumber;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal principal;

	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal annualRate;

	@Column(nullable = false)
	private int months;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal maturityAmount;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate maturityDate;

	@Column(nullable = false)
	private String status = "ACTIVE"; // ACTIVE, MATURED, BROKEN

	private LocalDateTime closedAt;
}