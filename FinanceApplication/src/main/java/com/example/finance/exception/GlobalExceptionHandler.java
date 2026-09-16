package com.example.finance.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	// ============================================================
	// CUSTOMER EXCEPTIONS
	// ============================================================

	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleCustomerNotFound(CustomerNotFoundException ex,
			HttpServletRequest request) {

		log.warn("Customer not found. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(DuplicateCustomerException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateCustomer(DuplicateCustomerException ex,
			HttpServletRequest request) {

		log.warn("Duplicate customer. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
	}

	// ============================================================
	// ACCOUNT EXCEPTIONS
	// ============================================================

	@ExceptionHandler(AccountNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex,
			HttpServletRequest request) {

		log.warn("Account not found. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(AccountNotActiveException.class)
	public ResponseEntity<ErrorResponse> handleAccountNotActive(AccountNotActiveException ex,
			HttpServletRequest request) {

		log.warn("Account is not active. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), null);
	}

	// ============================================================
	// TRANSACTION EXCEPTIONS
	// ============================================================

	@ExceptionHandler(TransactionNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleTransactionNotFound(TransactionNotFoundException ex,
			HttpServletRequest request) {

		log.warn("Transaction not found. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientBalance(InsufficientBalanceException ex,
			HttpServletRequest request) {

		log.warn("Insufficient balance. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(InvalidTransactionAmountException.class)
	public ResponseEntity<ErrorResponse> handleInvalidTransactionAmount(InvalidTransactionAmountException ex,
			HttpServletRequest request) {

		log.warn("Invalid transaction amount. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(InvalidTransferException.class)
	public ResponseEntity<ErrorResponse> handleInvalidTransfer(InvalidTransferException ex,
			HttpServletRequest request) {

		log.warn("Invalid transfer. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(UnsupportedTransactionTypeException.class)
	public ResponseEntity<ErrorResponse> handleUnsupportedTransactionType(UnsupportedTransactionTypeException ex,
			HttpServletRequest request) {

		log.warn("Unsupported transaction type. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
	}
	
	@ExceptionHandler(AccountBusinessException.class)
	public ResponseEntity<ErrorResponse> handleAccountBusinessException(
	        AccountBusinessException ex,
	        HttpServletRequest request) {

	    log.warn(
	            "Account business rule violation. path={}, message={}",
	            request.getRequestURI(),
	            ex.getMessage()
	    );

	    return buildErrorResponse(
	            HttpStatus.BAD_REQUEST,
	            ex.getMessage(),
	            request.getRequestURI(),
	            null
	    );
	}

	// ============================================================
	// LOAN EXCEPTIONS
	// ============================================================

	@ExceptionHandler(LoanNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleLoanNotFound(LoanNotFoundException ex, HttpServletRequest request) {

		log.warn("Loan not found. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(LoanBusinessException.class)
	public ResponseEntity<ErrorResponse> handleLoanBusinessException(LoanBusinessException ex,
			HttpServletRequest request) {

		log.warn("Loan business exception. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
	}

	// ============================================================
	// INVESTMENT EXCEPTIONS
	// ============================================================

	@ExceptionHandler(InvestmentNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleInvestmentNotFound(InvestmentNotFoundException ex,
			HttpServletRequest request) {

		log.warn("Investment not found. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), null);
	}

	@ExceptionHandler(InvestmentBusinessException.class)
	public ResponseEntity<ErrorResponse> handleInvestmentBusinessException(InvestmentBusinessException ex,
			HttpServletRequest request) {

		log.warn("Investment business exception. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
	}

	// ============================================================
	// DTO FIELD VALIDATION
	// @Valid
	// ============================================================

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
			HttpServletRequest request) {

		Map<String, String> validationErrors = new LinkedHashMap<>();

		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {

			validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}

		log.warn("Validation failed. path={}, errors={}", request.getRequestURI(), validationErrors);

		return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(),
				validationErrors);
	}

	// ============================================================
	// BIND VALIDATION
	// ============================================================

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(BindException ex, HttpServletRequest request) {

		Map<String, String> validationErrors = new LinkedHashMap<>();

		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {

			validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}

		log.warn("Binding validation failed. path={}, errors={}", request.getRequestURI(), validationErrors);

		return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(),
				validationErrors);
	}

	// ============================================================
	// CONSTRAINT VALIDATION
	// @RequestParam / @PathVariable
	// ============================================================

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
			HttpServletRequest request) {

		Map<String, String> validationErrors = new LinkedHashMap<>();

		ex.getConstraintViolations().forEach(
				violation -> validationErrors.put(violation.getPropertyPath().toString(), violation.getMessage()));

		log.warn("Constraint validation failed. path={}, errors={}", request.getRequestURI(), validationErrors);

		return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(),
				validationErrors);
	}

	// ============================================================
	// MISSING REQUEST PARAMETER
	// ============================================================

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingRequestParameter(MissingServletRequestParameterException ex,
			HttpServletRequest request) {

		String message = "Required request parameter '" + ex.getParameterName() + "' is missing.";

		log.warn("Missing request parameter. path={}, parameter={}", request.getRequestURI(), ex.getParameterName());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), null);
	}

	// ============================================================
	// MISSING PATH VARIABLE
	// ============================================================

	@ExceptionHandler(MissingPathVariableException.class)
	public ResponseEntity<ErrorResponse> handleMissingPathVariable(MissingPathVariableException ex,
			HttpServletRequest request) {

		String message = "Required path variable '" + ex.getVariableName() + "' is missing.";

		log.warn("Missing path variable. path={}, variable={}", request.getRequestURI(), ex.getVariableName());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), null);
	}

	// ============================================================
	// INVALID PARAMETER TYPE
	// ============================================================

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
			HttpServletRequest request) {

		String message = "Invalid value for parameter '" + ex.getName() + "'.";

		log.warn("Parameter type mismatch. path={}, parameter={}, value={}", request.getRequestURI(), ex.getName(),
				ex.getValue());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), null);
	}

	// ============================================================
	// INVALID JSON / INVALID ENUM
	// ============================================================

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleInvalidRequestBody(HttpMessageNotReadableException ex,
			HttpServletRequest request) {

		log.warn("Invalid request body. path={}", request.getRequestURI());

		return buildErrorResponse(HttpStatus.BAD_REQUEST,
				"Invalid request body. Please check the JSON format and field values.", request.getRequestURI(), null);
	}

	// ============================================================
	// DATABASE CONSTRAINT
	// ============================================================

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
			HttpServletRequest request) {

		log.error("Database constraint violation. path={}", request.getRequestURI(), ex);

		return buildErrorResponse(HttpStatus.CONFLICT,
				"Database constraint violation. The requested operation could not be completed.",
				request.getRequestURI(), null);
	}

	// ============================================================
	// ENDPOINT NOT FOUND
	// ============================================================

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
			HttpServletRequest request) {

		log.warn("Endpoint not found. path={}", request.getRequestURI());

		return buildErrorResponse(HttpStatus.NOT_FOUND, "The requested endpoint was not found.",
				request.getRequestURI(), null);
	}

	// ============================================================
	// ILLEGAL ARGUMENT
	// ============================================================

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
			HttpServletRequest request) {

		log.warn("Illegal argument. path={}, message={}", request.getRequestURI(), ex.getMessage());

		return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), null);
	}

	// ============================================================
	// FINAL FALLBACK
	// ============================================================

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {

		log.error("Unexpected application error. path={}", request.getRequestURI(), ex);

		return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
				"An unexpected error occurred. Please contact support.", request.getRequestURI(), null);
	}

	// ============================================================
	// COMMON ERROR RESPONSE BUILDER
	// ============================================================

	private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, String path,
			Map<String, String> validationErrors) {

		ErrorResponse response = ErrorResponse.builder().timestamp(LocalDateTime.now()).status(status.value())
				.error(status.getReasonPhrase()).message(message).path(path).validationErrors(validationErrors).build();

		return ResponseEntity.status(status).body(response);
	}
}