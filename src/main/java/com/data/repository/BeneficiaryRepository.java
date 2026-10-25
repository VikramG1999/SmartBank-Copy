package com.data.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.data.model.Beneficiary;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

	List<Beneficiary> findByOwnerEmail(String ownerEmail);

	boolean existsByOwnerEmailAndAccountNumber(String ownerEmail, String accountNumber);

	Optional<Beneficiary> findByIdAndOwnerEmail(Long id, String ownerEmail);
}