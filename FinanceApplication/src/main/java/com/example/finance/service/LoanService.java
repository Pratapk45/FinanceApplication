package com.example.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finance.entity.Customer;
import com.example.finance.entity.Loan;
import com.example.finance.entity.enums.LoanStatus;
import com.example.finance.entity.enums.LoanType;
import com.example.finance.exception.CustomerNotFoundException;
import com.example.finance.exception.LoanBusinessException;
import com.example.finance.exception.LoanNotFoundException;
import com.example.finance.mapper.LoanMapper;
import com.example.finance.repository.CustomerRepository;
import com.example.finance.repository.LoanRepository;
import com.example.finance.requestDto.LoanRequestDTO;
import com.example.finance.responceDto.LoanResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LoanService {

	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private LoanMapper loanMapper;

	// ============================================================
	// CREATE OR UPDATE
	// ============================================================

	@Transactional
	public LoanResponseDTO createOrUpdate(LoanRequestDTO request) {

		/*
		 * ======================================================== CREATE
		 * ========================================================
		 */

		if (request.getId() == null) {

			log.info("Creating loan for customerId={}", request.getCustomerId());

			Customer customer = getCustomer(request.getCustomerId());

			Loan loan = loanMapper.toEntity(request);

			loan.setCustomer(customer);

			loan.setLoanNumber(generateLoanNumber());

			loan.setOutstandingAmount(request.getPrincipalAmount());

			loan.setStatus(LoanStatus.ACTIVE);

			loan.setMaturityDate(calculateMaturityDate(request.getStartDate(), request.getTenureMonths()));

			Loan savedLoan = loanRepository.save(loan);

			log.info("Loan created successfully. loanId={}, loanNumber={}", savedLoan.getId(),
					savedLoan.getLoanNumber());

			return loanMapper.toResponseDTO(savedLoan);
		}

		/*
		 * ======================================================== UPDATE
		 * ========================================================
		 */

		Loan loan = getLoan(request.getId());

		validateUpdate(loan, request);

		/*
		 * Customer cannot be changed.
		 */

		/*
		 * Principal cannot be changed.
		 */

		/*
		 * Start date cannot be changed.
		 */

		/*
		 * Loan type can be changed.
		 */
		if (request.getLoanType() != null) {

			loan.setLoanType(request.getLoanType());
		}

		/*
		 * Interest rate can be changed.
		 */
		if (request.getInterestRate() != null) {

			loan.setInterestRate(request.getInterestRate());
		}

		/*
		 * Tenure can be changed.
		 */
		if (request.getTenureMonths() != null) {

			loan.setTenureMonths(request.getTenureMonths());

			loan.setMaturityDate(calculateMaturityDate(loan.getStartDate(), request.getTenureMonths()));
		}

		/*
		 * Status is controlled through business rules.
		 */
		if (request.getStatus() != null) {

			validateStatusTransition(loan.getStatus(), request.getStatus(), loan.getOutstandingAmount());

			loan.setStatus(request.getStatus());
		}

		Loan updatedLoan = loanRepository.save(loan);

		log.info("Loan updated successfully. loanId={}, loanNumber={}", updatedLoan.getId(),
				updatedLoan.getLoanNumber());

		return loanMapper.toResponseDTO(updatedLoan);
	}

	// ============================================================
	// GET ALL
	// ============================================================

	@Transactional(readOnly = true)
	public Page<LoanResponseDTO> getAllLoans(Pageable pageable) {

		return loanRepository.findAll(pageable).map(loanMapper::toResponseDTO);
	}

	// ============================================================
	// GET BY ID
	// ============================================================

	@Transactional(readOnly = true)
	public LoanResponseDTO getLoanById(Long id) {

		Loan loan = getLoan(id);

		return loanMapper.toResponseDTO(loan);
	}

	// ============================================================
	// GET BY CUSTOMER
	// ============================================================

	@Transactional(readOnly = true)
	public Page<LoanResponseDTO> getLoansByCustomer(Long customerId, Pageable pageable) {

		getCustomer(customerId);

		return loanRepository.findByCustomerId(customerId, pageable).map(loanMapper::toResponseDTO);
	}

	// ============================================================
	// GET BY STATUS
	// ============================================================

	@Transactional(readOnly = true)
	public Page<LoanResponseDTO> getLoansByStatus(LoanStatus status, Pageable pageable) {

		return loanRepository.findByStatus(status, pageable).map(loanMapper::toResponseDTO);
	}

	// ============================================================
	// GET BY LOAN TYPE
	// ============================================================

	@Transactional(readOnly = true)
	public Page<LoanResponseDTO> getLoansByType(LoanType loanType, Pageable pageable) {

		return loanRepository.findByLoanType(loanType, pageable).map(loanMapper::toResponseDTO);
	}

	// ============================================================
	// DELETE / CANCEL
	// ============================================================

	@Transactional
	public void deleteLoan(Long id) {

		Loan loan = getLoan(id);

		if (loan.getStatus() == LoanStatus.CANCELLED) {

			throw new LoanBusinessException("Loan is already cancelled");
		}

		if (loan.getStatus() == LoanStatus.CLOSED) {

			throw new LoanBusinessException("Closed loan cannot be cancelled");
		}

		if (loan.getStatus() == LoanStatus.DEFAULTED) {

			throw new LoanBusinessException("Defaulted loan cannot be cancelled");
		}

		loan.setStatus(LoanStatus.CANCELLED);

		loanRepository.save(loan);

		log.info("Loan cancelled successfully. loanId={}", loan.getId());
	}

	// ============================================================
	// UPDATE VALIDATION
	// ============================================================

	private void validateUpdate(Loan loan, LoanRequestDTO request) {

		if (loan.getStatus() == LoanStatus.CLOSED) {

			throw new LoanBusinessException("Closed loan cannot be updated");
		}

		if (loan.getStatus() == LoanStatus.DEFAULTED) {

			throw new LoanBusinessException("Defaulted loan cannot be updated");
		}

		if (loan.getStatus() == LoanStatus.CANCELLED) {

			throw new LoanBusinessException("Cancelled loan cannot be updated");
		}

		/*
		 * Customer cannot change.
		 */
		if (request.getCustomerId() != null && !request.getCustomerId().equals(loan.getCustomer().getId())) {

			throw new LoanBusinessException("Customer cannot be changed for an existing loan");
		}

		/*
		 * Principal cannot change.
		 */
		if (request.getPrincipalAmount() != null
				&& request.getPrincipalAmount().compareTo(loan.getPrincipalAmount()) != 0) {

			throw new LoanBusinessException("Principal amount cannot be changed for an existing loan");
		}

		/*
		 * Start date cannot change.
		 */
		if (request.getStartDate() != null && !request.getStartDate().equals(loan.getStartDate())) {

			throw new LoanBusinessException("Start date cannot be changed for an existing loan");
		}

		/*
		 * Closed status requires zero outstanding balance.
		 */
		if (request.getStatus() == LoanStatus.CLOSED && loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) != 0) {

			throw new LoanBusinessException("Loan cannot be closed while outstanding amount is greater than zero");
		}
	}

	// ============================================================
	// STATUS TRANSITION
	// ============================================================

	private void validateStatusTransition(LoanStatus currentStatus, LoanStatus requestedStatus,
			BigDecimal outstandingAmount) {

		if (currentStatus == requestedStatus) {
			return;
		}

		/*
		 * ACTIVE -> CLOSED
		 */
		if (currentStatus == LoanStatus.ACTIVE && requestedStatus == LoanStatus.CLOSED) {

			if (outstandingAmount.compareTo(BigDecimal.ZERO) != 0) {

				throw new LoanBusinessException("Loan cannot be closed while outstanding amount is greater than zero");
			}

			return;
		}

		/*
		 * ACTIVE -> DEFAULTED
		 */
		if (currentStatus == LoanStatus.ACTIVE && requestedStatus == LoanStatus.DEFAULTED) {

			return;
		}

		/*
		 * ACTIVE -> CANCELLED
		 */
		if (currentStatus == LoanStatus.ACTIVE && requestedStatus == LoanStatus.CANCELLED) {

			return;
		}

		throw new LoanBusinessException(
				"Invalid loan status transition from " + currentStatus + " to " + requestedStatus);
	}

	// ============================================================
	// GET CUSTOMER
	// ============================================================

	private Customer getCustomer(Long customerId) {

		if (customerId == null) {

			throw new CustomerNotFoundException("Customer ID is required");
		}

		return customerRepository.findById(customerId)
				.orElseThrow(() -> new CustomerNotFoundException(customerId));
	}

	// ============================================================
	// GET LOAN
	// ============================================================

	private Loan getLoan(Long id) {

		return loanRepository.findById(id)
				.orElseThrow(() -> new LoanNotFoundException("Loan not found with id: " + id));
	}

	// ============================================================
	// MATURITY DATE
	// ============================================================

	private LocalDate calculateMaturityDate(LocalDate startDate, Integer tenureMonths) {

		return startDate.plusMonths(tenureMonths);
	}

	// ============================================================
	// GENERATE LOAN NUMBER
	// ============================================================

	private String generateLoanNumber() {

		String loanNumber;

		do {

			loanNumber = "LN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

		} while (loanRepository.existsByLoanNumber(loanNumber));

		return loanNumber;
	}
}
