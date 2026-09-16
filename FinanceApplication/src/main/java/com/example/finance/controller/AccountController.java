package com.example.finance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.finance.requestDto.AccountRequestDTO;
import com.example.finance.responceDto.AccountResponseDTO;
import com.example.finance.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/accounts")
@Slf4j
@Tag(name = "Account Management", description = "APIs for managing customer accounts")
public class AccountController {

	@Autowired
	private AccountService accountService;

	/**
	 * Creates a new account or updates an existing account.
	 *
	 * CREATE: id = null
	 *
	 * UPDATE: id = existing account id
	 */
	@PostMapping
	@Operation(summary = "Create or update account", description = """
			Creates a new account when id is null.
			Updates an existing account when id is provided.
			""")
	public ResponseEntity<AccountResponseDTO> createOrUpdate(@Valid @RequestBody AccountRequestDTO requestDTO) {

		log.info("Create/update account request received. id={}, customerId={}", requestDTO.getId(),
				requestDTO.getCustomerId());

		AccountResponseDTO response = accountService.createOrUpdate(requestDTO);

		/*
		 * We return 201 for a new account and 200 for an update.
		 *
		 * Since the service currently returns only the DTO, we determine this from the
		 * request ID.
		 */
		HttpStatus status = requestDTO.getId() == null ? HttpStatus.CREATED : HttpStatus.OK;

		return ResponseEntity.status(status).body(response);
	}

	/**
	 * Get account by database ID.
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Get account by ID", description = "Returns account details using the account ID")
	public ResponseEntity<AccountResponseDTO> getById(
			@Parameter(description = "Account database ID") @PathVariable Long id) {

		log.info("Get account request received. accountId={}", id);

		return ResponseEntity.ok(accountService.getById(id));
	}

	/**
	 * Get account by generated account number.
	 */
	@GetMapping("/account-number/{accountNumber}")
	@Operation(summary = "Get account by account number", description = "Returns account details using the generated account number")
	public ResponseEntity<AccountResponseDTO> getByAccountNumber(
			@Parameter(description = "System generated account number") @PathVariable String accountNumber) {

		log.info("Get account by account number request received. accountNumber={}", accountNumber);

		return ResponseEntity.ok(accountService.getByAccountNumber(accountNumber));
	}

	/**
     * Get all accounts belonging to a customer.
     *
     * Supports:
     * - Pagination
     * - Sorting
     *
     * Example:
     *
     * GET /api/accounts/customer/1?page=0&size=10
     *
     * Sorting:
     *
     * GET /api/accounts/customer/1?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping("/customer/{customerId}")
    @Operation(
            summary = "Get customer accounts",
            description = """
                    Returns all accounts belonging to a customer.
                    Supports pagination and sorting.
                    """
    )
    public ResponseEntity<Page<AccountResponseDTO>> getAccountsByCustomer(
            @Parameter(description = "Customer ID")
            @PathVariable Long customerId,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt"
            )
            Pageable pageable
    ) {

        log.info(
                "Get customer accounts request received. customerId={}, page={}, size={}",
                customerId,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Page<AccountResponseDTO> accounts =
                accountService.getAccountsByCustomer(
                        customerId,
                        pageable
                );

        return ResponseEntity.ok(accounts);
    }
    
    /**
     * Closes an account without physically deleting it.
     *
     * This preserves the financial history associated
     * with the account.
     */
    @PatchMapping("/{id}/close")
    @Operation(
            summary = "Close account",
            description = """
                    Closes an account when its balance is zero.
                    The account is not physically deleted.
                    """
    )
    public ResponseEntity<AccountResponseDTO> closeAccount(
            @Parameter(description = "Account ID")
            @PathVariable Long id) {

        log.info(
                "Close account request received. accountId={}",
                id
        );

        AccountResponseDTO response =
                accountService.closeAccount(id);

        return ResponseEntity.ok(response);
    }
}
