package com.example.finance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.finance.requestDto.TransactionRequestDTO;
import com.example.finance.responceDto.TransactionResponseDTO;
import com.example.finance.responceDto.TransactionResultResponseDTO;
import com.example.finance.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction API", description = "Financial transaction operations")
public class TransactionController {

	@Autowired
	private TransactionService transactionService;

	/*
	 * ============================================================ CREATE
	 * TRANSACTION ============================================================
	 *
	 * DEPOSIT WITHDRAWAL TRANSFER
	 */
	@PostMapping
	@Operation(summary = "Create financial transaction", description = """
			Creates a deposit, withdrawal or transfer.

			DEPOSIT:
			accountId + amount required.

			WITHDRAWAL:
			accountId + amount required.

			TRANSFER:
			accountId + destinationAccountId + amount required.
			""")
	public ResponseEntity<TransactionResultResponseDTO> createTransaction(
			@Valid @RequestBody TransactionRequestDTO requestDTO) {

		TransactionResultResponseDTO response = transactionService.createTransaction(requestDTO);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	/*
	 * ============================================================ ACCOUNT
	 * TRANSACTION HISTORY
	 * ============================================================
	 */
	@GetMapping("/account/{accountId}")
	@Operation(summary = "Get account transaction history")
	public ResponseEntity<Page<TransactionResponseDTO>> getTransactionsByAccount(

			@Parameter(description = "Account ID", example = "101") @PathVariable Long accountId,

			@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {

		return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId, pageable));
	}

	/*
	 * ============================================================ GET TRANSACTION
	 * BY REFERENCE ============================================================
	 */
	@GetMapping("/reference/{transactionReference}")
	@Operation(summary = "Get transaction by reference")
	public ResponseEntity<TransactionResponseDTO> getByReference(

			@Parameter(description = "Transaction reference", example = "TXN-A12B34C56D78") @PathVariable String transactionReference) {

		return ResponseEntity.ok(transactionService.getByReference(transactionReference));
	}
}
//```java  ```
