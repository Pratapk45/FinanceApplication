package com.example.finance.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.finance.entity.Loan;
import com.example.finance.entity.enums.LoanStatus;
import com.example.finance.entity.enums.LoanType;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

	Optional<Loan> findByLoanNumber(String loanNumber);

	boolean existsByLoanNumber(String loanNumber);

	Page<Loan> findByCustomerId(Long customerId, Pageable pageable);

	Page<Loan> findByCustomerIdAndStatus(Long customerId, LoanStatus status, Pageable pageable);

	Page<Loan> findByLoanType(LoanType loanType, Pageable pageable);

	Page<Loan> findByStatus(LoanStatus status, Pageable pageable);
}
