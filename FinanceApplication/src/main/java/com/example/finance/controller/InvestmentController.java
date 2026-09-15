package com.example.finance.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

import com.example.finance.entity.enums.InvestmentStatus;
import com.example.finance.entity.enums.InvestmentType;
import com.example.finance.requestDto.InvestmentRequestDTO;
import com.example.finance.responceDto.InvestmentResponseDTO;
import com.example.finance.service.InvestmentService;
import com.example.finance.validation.OnCreate;
import com.example.finance.validation.OnUpdate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Investment Management", description = "APIs for managing customer investments")
public class InvestmentController {

	private final InvestmentService investmentService;

	/*
	 * ============================================================ CREATE
	 * ============================================================
	 */
	@PostMapping
	@Operation(summary = "Create investment", description = "Creates a new investment for an existing customer")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Investment created successfully", content = @Content(schema = @Schema(implementation = InvestmentResponseDTO.class))),
			@ApiResponse(responseCode = "400", description = "Validation or business error"),
			@ApiResponse(responseCode = "404", description = "Customer not found") })
	public ResponseEntity<InvestmentResponseDTO> createInvestment(

			@Validated(OnCreate.class) @Valid @RequestBody InvestmentRequestDTO requestDTO) {

		log.info("POST /api/investments - customerId={}", requestDTO.getCustomerId());

		/*
		 * POST represents CREATE.
		 *
		 * ID should not be supplied.
		 */
		if (requestDTO.getId() != null) {

			log.warn("Create request contains id={}", requestDTO.getId());
		}

		InvestmentResponseDTO response = investmentService.createOrUpdate(requestDTO);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/*
	 * ============================================================ GET BY ID
	 * ============================================================
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Get investment by ID", description = "Returns investment with complete customer details")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Investment found"),
			@ApiResponse(responseCode = "404", description = "Investment not found") })
	public ResponseEntity<InvestmentResponseDTO> getById(

			@Parameter(description = "Investment ID", required = true, example = "1") @PathVariable Long id) {

		log.info("GET /api/investments/{}", id);

		return ResponseEntity.ok(investmentService.getById(id));
	}

	/*
	 * ============================================================ GET ALL / FILTER
	 * ============================================================
	 *
	 * Examples:
	 *
	 * /api/investments
	 *
	 * /api/investments?customerId=1
	 *
	 * /api/investments?status=ACTIVE
	 *
	 * /api/investments?investmentType=MUTUAL_FUND
	 *
	 * /api/investments?customerId=1&status=ACTIVE
	 *
	 * /api/investments?customerId=1 &status=ACTIVE &investmentType=MUTUAL_FUND
	 *
	 * Pagination:
	 *
	 * page=0&size=10
	 *
	 * Sorting:
	 *
	 * sort=investedAmount,desc
	 */
	@GetMapping
	@Operation(summary = "Get investments", description = "Returns investments with optional filters, pagination and sorting")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Investments retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Customer not found") })
	public ResponseEntity<Page<InvestmentResponseDTO>> getInvestments(

			@Parameter(description = "Customer ID filter", in = ParameterIn.QUERY, example = "1") @RequestParam(required = false) Long customerId,

			@Parameter(description = "Investment status filter", in = ParameterIn.QUERY, example = "ACTIVE") @RequestParam(required = false) InvestmentStatus status,

			@Parameter(description = "Investment type filter", in = ParameterIn.QUERY, example = "MUTUAL_FUND") @RequestParam(required = false) InvestmentType investmentType,

			@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

		log.info("GET /api/investments - customerId={}, status={}, type={}", customerId, status, investmentType);

		Page<InvestmentResponseDTO> response = investmentService.searchInvestments(customerId, status, investmentType,
				pageable);

		return ResponseEntity.ok(response);
	}

	/*
	 * ============================================================ ACTIVE
	 * INVESTMENTS BY CUSTOMER
	 * ============================================================
	 *
	 * Uses Named Query.
	 */
	@GetMapping("/customer/{customerId}/active")
	@Operation(summary = "Get active investments by customer", description = "Returns active investments for a customer")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Active investments retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Customer not found") })
	public ResponseEntity<Page<InvestmentResponseDTO>> getActiveByCustomer(

			@PathVariable Long customerId,

			@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

		log.info("GET /api/investments/customer/{}/active", customerId);

		return ResponseEntity.ok(investmentService.getActiveByCustomerId(customerId, pageable));
	}

	/*
	 * ============================================================ UPDATE
	 * ============================================================
	 */
	@PutMapping("/{id}")
	@Operation(summary = "Update investment", description = "Updates an existing investment")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Investment updated successfully"),
			@ApiResponse(responseCode = "400", description = "Validation or business error"),
			@ApiResponse(responseCode = "404", description = "Investment not found") })
	public ResponseEntity<InvestmentResponseDTO> updateInvestment(

			@PathVariable Long id,

			@Validated(OnUpdate.class) @Valid @RequestBody InvestmentRequestDTO requestDTO) {

		log.info("PUT /api/investments/{}", id);

		/*
		 * Path variable is authoritative.
		 */
		requestDTO.setId(id);

		InvestmentResponseDTO response = investmentService.createOrUpdate(requestDTO);

		return ResponseEntity.ok(response);
	}

	/*
	 * ============================================================ DELETE / CANCEL
	 * ============================================================
	 */
	@DeleteMapping("/{id}")
	@Operation(summary = "Cancel investment", description = "Logically cancels an investment")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Investment cancelled successfully"),
			@ApiResponse(responseCode = "400", description = "Investment cannot be cancelled"),
			@ApiResponse(responseCode = "404", description = "Investment not found") })
	public ResponseEntity<Void> deleteInvestment(

			@PathVariable Long id) {

		log.info("DELETE /api/investments/{}", id);

		investmentService.delete(id);

		return ResponseEntity.noContent().build();
	}
}