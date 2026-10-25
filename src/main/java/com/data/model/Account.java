package com.data.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String accountNumber;

	@Column(nullable = false, columnDefinition = "varchar(20) default 'SAVINGS'")
	private String accountType = "SAVINGS"; // SAVINGS or CURRENT

	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal balance = BigDecimal.ZERO;

	@Column(nullable = false, columnDefinition = "varchar(20) default 'ACTIVE'")
	private String status = "ACTIVE"; // ACTIVE or FROZEN

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@JsonIgnore
	@Version
	private long version;

	public boolean isPinSet() {
		return user != null && user.getTransactionPin() != null;
	}
}