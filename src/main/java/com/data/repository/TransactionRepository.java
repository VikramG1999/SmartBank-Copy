package com.data.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.data.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	List<Transaction> findByTypeOrderByCreatedAtDesc(String type);

	@Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t "
			+ "WHERE t.fromAccount = :account AND t.type = 'TRANSFER' AND t.createdAt >= :start")
	BigDecimal sumTransfersSince(@Param("account") String account, @Param("start") LocalDateTime start);

	@Query("SELECT t FROM Transaction t WHERE (t.fromAccount = :account OR t.toAccount = :account) "
			+ "AND (:type = '' OR t.type = :type) "
			+ "AND (:search = '' OR LOWER(t.referenceNo) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR t.fromAccount LIKE CONCAT('%', :search, '%') OR t.toAccount LIKE CONCAT('%', :search, '%')) "
			+ "AND t.createdAt >= :start AND t.createdAt < :end")
	Page<Transaction> search(@Param("account") String account, @Param("type") String type,
			@Param("search") String search, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end, Pageable pageable);

	@Query("SELECT t FROM Transaction t WHERE (:account = '' OR t.fromAccount = :account OR t.toAccount = :account) "
			+ "AND (:type = '' OR t.type = :type) "
			+ "AND t.createdAt >= :start AND t.createdAt < :end")
	Page<Transaction> adminSearch(@Param("account") String account, @Param("type") String type,
			@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

	@Query("SELECT t FROM Transaction t WHERE (t.fromAccount = :account OR t.toAccount = :account) "
			+ "AND t.createdAt >= :start AND t.createdAt < :end ORDER BY t.createdAt ASC")
	List<Transaction> findStatement(@Param("account") String account, @Param("start") LocalDateTime start,
			@Param("end") LocalDateTime end);
}