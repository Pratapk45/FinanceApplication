package com.example.finance.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finance.entity.Customer;
import com.example.finance.entity.Investment;
import com.example.finance.entity.enums.InvestmentStatus;
import com.example.finance.entity.enums.InvestmentType;
import com.example.finance.exception.CustomerNotFoundException;
import com.example.finance.exception.InvestmentBusinessException;
import com.example.finance.exception.InvestmentNotFoundException;
import com.example.finance.mapper.InvestmentMapper;
import com.example.finance.repository.CustomerRepository;
import com.example.finance.repository.InvestmentRepository;
import com.example.finance.requestDto.InvestmentRequestDTO;
import com.example.finance.responceDto.InvestmentResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InvestmentService {

	@Autowired
	private InvestmentRepository investmentRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private InvestmentMapper investmentMapper;

	/*
	 * ============================================================ 
	 * CREATE OR UPDATE
	 * ============================================================
	 */
	@Transactional
	public InvestmentResponseDTO createOrUpdate(InvestmentRequestDTO requestDTO) {

		/*
		 * CREATE
		 */
		if (requestDTO.getId() == null) {

			log.info("Creating investment for customerId={}", requestDTO.getCustomerId());

			Investment investment = createInvestment(requestDTO);

			Investment savedInvestment = investmentRepository.save(investment);

			log.info("Investment created successfully. id={}, reference={}", savedInvestment.getId(),
					savedInvestment.getInvestmentReference());

			return investmentMapper.toResponseDTO(savedInvestment);
		}

		/*
		 * UPDATE
		 */
		log.info("Updating investment id={}", requestDTO.getId());

		Investment investment = investmentRepository.findById(requestDTO.getId()).orElseThrow(
				() -> new InvestmentNotFoundException("Investment not found with id: " + requestDTO.getId()));

		updateInvestment(investment, requestDTO);

		Investment savedInvestment = investmentRepository.save(investment);

		log.info("Investment updated successfully. id={}", savedInvestment.getId());

		return investmentMapper.toResponseDTO(savedInvestment);
	}

	/*
	 * ============================================================ 
	 * CREATE
	 * ============================================================
	 */
	private Investment createInvestment(InvestmentRequestDTO requestDTO) {

		Customer customer = customerRepository.findById(requestDTO.getCustomerId()).orElseThrow(
				() -> new CustomerNotFoundException("Customer not found with id: " + requestDTO.getCustomerId()));

		validateInvestedAmount(requestDTO.getInvestedAmount());

		validateReturnRate(requestDTO.getReturnRate());

		Investment investment = investmentMapper.toEntity(requestDTO);

		investment.setCustomer(customer);

		/*
		 * Generate application-controlled reference.
		 */
		investment.setInvestmentReference(generateInvestmentReference());

		/*
		 * Initial current value equals original invested amount.
		 */
		investment.setCurrentValue(requestDTO.getInvestedAmount());

		/*
		 * Every newly created investment starts ACTIVE.
		 */
		investment.setStatus(InvestmentStatus.ACTIVE);

		return investment;
	}

	/*
	 * ============================================================
	 *  UPDATE
	 * ============================================================
	 */
	private void updateInvestment(Investment investment, InvestmentRequestDTO requestDTO) {

		/*
		 * Closed investment is final.
		 */
		if (investment.getStatus() == InvestmentStatus.CLOSED) {

			throw new InvestmentBusinessException("Closed investment cannot be updated");
		}

		/*
		 * Cancelled investment cannot be modified.
		 */
		if (investment.getStatus() == InvestmentStatus.CANCELLED) {

			throw new InvestmentBusinessException("Cancelled investment cannot be updated");
		}

		/*
		 * Customer cannot be changed.
		 */
		if (requestDTO.getCustomerId() != null
				&& !investment.getCustomer().getId().equals(requestDTO.getCustomerId())) {

			throw new InvestmentBusinessException("Customer cannot be changed for an existing investment");
		}

		/*
		 * Original invested amount cannot be changed.
		 */
		if (requestDTO.getInvestedAmount() != null
				&& investment.getInvestedAmount().compareTo(requestDTO.getInvestedAmount()) != 0) {

			throw new InvestmentBusinessException("Invested amount cannot be changed for an existing investment");
		}

		/*
		 * Investment type can be updated.
		 */
		if (requestDTO.getInvestmentType() != null) {

			investment.setInvestmentType(requestDTO.getInvestmentType());
		}

		/*
		 * Return rate can be updated.
		 */
		if (requestDTO.getReturnRate() != null) {

			validateReturnRate(requestDTO.getReturnRate());

			investment.setReturnRate(requestDTO.getReturnRate());
		}

		/*
		 * Description can be updated.
		 */
		if (requestDTO.getDescription() != null) {

			investment.setDescription(requestDTO.getDescription());
		}
	}

	/*
	 * ============================================================ 
	 * GET BY ID
	 * ============================================================
	 */
	@Transactional(readOnly = true)
	public InvestmentResponseDTO getById(Long id) {

		log.debug("Fetching investment id={}", id);

		Investment investment = investmentRepository.findById(id)
				.orElseThrow(() -> new InvestmentNotFoundException("Investment not found with id: " + id));

		return investmentMapper.toResponseDTO(investment);
	}

	/*
	 * ============================================================ 
	 * SEARCH
	 * ============================================================
	 *
	 * Handles every combination:
	 *
	 * customerId status investmentType
	 *
	 * Pagination + sorting through Pageable.
	 */
	@Transactional(readOnly = true)
	public Page<InvestmentResponseDTO> searchInvestments(Long customerId, InvestmentStatus status,
			InvestmentType investmentType, Pageable pageable) {

		/*
		 * Verify customer before searching.
		 */
		if (customerId != null && !customerRepository.existsById(customerId)) {

			throw new CustomerNotFoundException("Customer not found with id: " + customerId);
		}

		log.debug("Searching investments. customerId={}, status={}, type={}", customerId, status, investmentType);

		return investmentRepository.searchInvestments(customerId, status, investmentType, pageable)
				.map(investmentMapper::toResponseDTO);
	}

	/*
	 * ============================================================ 
	 * ACTIVE INVESTMENTS BY CUSTOMER
	 * ============================================================
	 *
	 * Uses Named Query.
	 */
	@Transactional(readOnly = true)
	public Page<InvestmentResponseDTO> getActiveByCustomerId(Long customerId, Pageable pageable) {

		if (!customerRepository.existsById(customerId)) {

			throw new CustomerNotFoundException("Customer not found with id: " + customerId);
		}

		return investmentRepository.findActiveByCustomerId(customerId, pageable).map(investmentMapper::toResponseDTO);
	}

	/*
	 * ============================================================ 
	 * DELETE / CANCEL
	 * ============================================================
	 *
	 * Financial records are logically cancelled. They are not physically deleted.
	 */
	@Transactional
	public void delete(Long id) {

		log.info("Cancelling investment id={}", id);

		Investment investment = investmentRepository.findById(id)
				.orElseThrow(() -> new InvestmentNotFoundException("Investment not found with id: " + id));

		if (investment.getStatus() == InvestmentStatus.CANCELLED) {

			throw new InvestmentBusinessException("Investment is already cancelled");
		}

		if (investment.getStatus() == InvestmentStatus.CLOSED) {

			throw new InvestmentBusinessException("Closed investment cannot be cancelled");
		}

		investment.setStatus(InvestmentStatus.CANCELLED);

		investmentRepository.save(investment);

		log.info("Investment cancelled successfully. id={}", id);
	}

	/*
	 * ============================================================ 
	 * VALIDATE INVESTED AMOUNT
	 * ============================================================
	 */
	private void validateInvestedAmount(BigDecimal investedAmount) {

		if (investedAmount == null || investedAmount.compareTo(BigDecimal.ZERO) <= 0) {

			throw new InvestmentBusinessException("Invested amount must be greater than zero");
		}
	}

	/*
	 * ============================================================ 
	 * VALIDATE RETURN RATE \
	 * ============================================================
	 */
	private void validateReturnRate(BigDecimal returnRate) {

		if (returnRate == null) {
			return;
		}

		if (returnRate.compareTo(BigDecimal.ZERO) < 0) {

			throw new InvestmentBusinessException("Return rate cannot be negative");
		}
	}

	/*
	 * ============================================================
	 *  GENERATE INVESTMENT REFERENCE
	 * ============================================================
	 */
	private String generateInvestmentReference() {

		return "INV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
	}
}
