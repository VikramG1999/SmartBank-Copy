package com.data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String referenceNo;

	private String fromAccount;

	private String toAccount;

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	@Column(precision = 15, scale = 2)
	private BigDecimal fromBalanceAfter;

	@Column(precision = 15, scale = 2)
	private BigDecimal toBalanceAfter;

	@Column(nullable = false)
	private String type; // DEPOSIT, WITHDRAW, TRANSFER, INTEREST, LOAN_DISBURSAL, LOAN_EMI, FD_OPEN, FD_MATURITY, FD_BREAK

	private String category;

	@Column(nullable = false)
	private String status; // SUCCESS, FAILED

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	public void setCreatedAt() {
		this.createdAt = LocalDateTime.now();
	}
}