package com.example.finance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.finance.entity.enums.LoanStatus;
import com.example.finance.entity.enums.LoanType;
import com.example.finance.requestDto.LoanRequestDTO;
import com.example.finance.responceDto.LoanResponseDTO;
import com.example.finance.service.LoanService;
import com.example.finance.validation.OnCreate;
import com.example.finance.validation.OnUpdate;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/loans")
@Slf4j
public class LoanController {

	@Autowired
	private LoanService loanService;

	// ============================================================
	// CREATE
	// ============================================================

	@PostMapping
	public ResponseEntity<LoanResponseDTO> createLoan(

			@Validated(OnCreate.class) @RequestBody LoanRequestDTO request) {

		log.info("POST /api/loans - Creating loan");

		/*
		 * POST always creates.
		 */
		request.setId(null);

		LoanResponseDTO response = loanService.createOrUpdate(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// ============================================================
	// UPDATE
	// ============================================================

	@PutMapping("/{id}")
	public ResponseEntity<LoanResponseDTO> updateLoan(

			@PathVariable @Positive(message = "Loan ID must be greater than zero") Long id,

			@Validated(OnUpdate.class) @RequestBody LoanRequestDTO request) {

		log.info("PUT /api/loans/{} - Updating loan", id);

		/*
		 * Path variable is authoritative.
		 */
		request.setId(id);

		LoanResponseDTO response = loanService.createOrUpdate(request);

		return ResponseEntity.ok(response);
	}

	// ============================================================
	// GET ALL / FILTER
	// ============================================================

	@GetMapping
	public ResponseEntity<Page<LoanResponseDTO>> getAllLoans(

			@RequestParam(required = false) Long customerId,

			@RequestParam(required = false) LoanStatus status,

			@RequestParam(required = false) LoanType loanType,

			@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {

		log.info("GET /api/loans customerId={}, status={}, loanType={}", customerId, status, loanType);

		if (customerId != null) {

			return ResponseEntity.ok(loanService.getLoansByCustomer(customerId, pageable));
		}

		if (status != null) {

			return ResponseEntity.ok(loanService.getLoansByStatus(status, pageable));
		}

		if (loanType != null) {

			return ResponseEntity.ok(loanService.getLoansByType(loanType, pageable));
		}

		return ResponseEntity.ok(loanService.getAllLoans(pageable));
	}

	// ============================================================
	// GET BY ID
	// ============================================================

	@GetMapping("/{id}")
	public ResponseEntity<LoanResponseDTO> getLoanById(

			@PathVariable @Positive(message = "Loan ID must be greater than zero") Long id) {

		log.info("GET /api/loans/{}", id);

		return ResponseEntity.ok(loanService.getLoanById(id));
	}

	// ============================================================
	// DELETE / CANCEL
	// ============================================================

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteLoan(

			@PathVariable @Positive(message = "Loan ID must be greater than zero") Long id) {

		log.info("DELETE /api/loans/{}", id);

		loanService.deleteLoan(id);

		return ResponseEntity.noContent().build();
	}
}
