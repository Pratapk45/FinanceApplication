package com.example.finance.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.finance.entity.Investment;
import com.example.finance.entity.enums.InvestmentStatus;
import com.example.finance.entity.enums.InvestmentType;

public interface InvestmentRepository extends JpaRepository<Investment, Long> {

	
	  List<Investment> findByStatus(InvestmentStatus status);
	/*
	 * ============================================================ 
	 * JPQL - COMBINED FILTER
	 * ============================================================
	 *
	 * All parameters are optional.
	 *
	 * Supports: customerId status investmentType
	 *
	 * Pageable provides: pagination sorting
	 */
	@Query("""
			SELECT i
			FROM Investment i
			WHERE (:customerId IS NULL
			       OR i.customer.id = :customerId)
			  AND (:status IS NULL
			       OR i.status = :status)
			  AND (:investmentType IS NULL
			       OR i.investmentType = :investmentType)
			""")
	Page<Investment> searchInvestments(@Param("customerId") Long customerId, @Param("status") InvestmentStatus status,
			@Param("investmentType") InvestmentType investmentType, Pageable pageable);

	/*
	 * ============================================================
	 *  NATIVE SQL
	 * ============================================================
	 *
	 * Included because the project requires Native SQL.
	 */
	@Query(value = """
			SELECT *
			FROM investments
			WHERE (:customerId IS NULL
			       OR customer_id = :customerId)
			  AND (:status IS NULL
			       OR status = :status)
			  AND (:investmentType IS NULL
			       OR investment_type = :investmentType)
			""", countQuery = """
			SELECT COUNT(*)
			FROM investments
			WHERE (:customerId IS NULL
			       OR customer_id = :customerId)
			  AND (:status IS NULL
			       OR status = :status)
			  AND (:investmentType IS NULL
			       OR investment_type = :investmentType)
			""", nativeQuery = true)
	Page<Investment> searchInvestmentsNative(@Param("customerId") Long customerId, @Param("status") String status,
			@Param("investmentType") String investmentType, Pageable pageable);

	/*
	 * ============================================================ 
	 * NAMED QUERY
	 * ============================================================
	 *
	 * The actual query is declared in Investment.java.
	 */
	Page<Investment> findActiveByCustomerId(@Param("customerId") Long customerId, Pageable pageable);
}