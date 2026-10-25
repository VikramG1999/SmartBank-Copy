package com.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.data.model.FixedDeposit;

@Repository
public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {

	List<FixedDeposit> findByOwnerEmailOrderByIdDesc(String ownerEmail);

	List<FixedDeposit> findAllByOrderByIdDesc();

	List<FixedDeposit> findByStatus(String status);

	Optional<FixedDeposit> findByIdAndOwnerEmail(Long id, String ownerEmail);

	long countByStatus(String status);
}