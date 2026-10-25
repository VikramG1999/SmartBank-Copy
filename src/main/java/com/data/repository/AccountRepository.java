package com.data.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.data.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findByAccountNumber(String accountNumber);

	List<Account> findByUserEmailOrderByIdAsc(String email);

	boolean existsByAccountNumber(String accountNumber);

	long countByStatus(String status);

	List<Account> findByAccountTypeAndStatus(String accountType, String status);

	@Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
	BigDecimal totalBalance();
}