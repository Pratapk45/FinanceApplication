package com.example.finance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.finance.requestDto.CustomerRequestDTO;
import com.example.finance.responceDto.CustomerResponseDTO;
import com.example.finance.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/customers")
@Slf4j
@Tag(name = "Customer APIs", description = "APIs for managing finance customers")
public class CustomerController {

	@Autowired
	private CustomerService customerService;

	// =====================================================
	// CREATE CUSTOMER
	// =====================================================

	@PostMapping
	@Operation(summary = "Create customer", description = "Creates a new finance customer")
	@ApiResponses({ @ApiResponse(responseCode = "201", description = "Customer created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid customer data") })
	public ResponseEntity<CustomerResponseDTO> createCustomer(@Valid @RequestBody CustomerRequestDTO requestDTO) {

		log.info("POST /api/customers - Creating customer");

		CustomerResponseDTO response = customerService.createCustomer(requestDTO);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// =====================================================
	// GET CUSTOMER BY ID
	// =====================================================

	@GetMapping("/{id}")
	@Operation(summary = "Get customer by ID", description = "Fetches a customer using customer ID")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Customer found"),
			@ApiResponse(responseCode = "404", description = "Customer not found") })
	public ResponseEntity<CustomerResponseDTO> getCustomerById(

			@Parameter(description = "Customer ID", example = "1") @PathVariable Long id) {

		log.info("GET /api/customers/{}", id);

		return ResponseEntity.ok(customerService.getCustomerById(id));
	}

	// =====================================================
	// GET ALL CUSTOMERS
	// PAGINATION + SORTING
	// =====================================================

	@GetMapping
	@Operation(summary = "Get all customers", description = """
			Returns customers with pagination and sorting.

			Example:
			/api/customers?page=0&size=10&sort=firstName,asc
			""")
	public ResponseEntity<Page<CustomerResponseDTO>> getAllCustomers(

			@Parameter(description = "Pagination and sorting parameters") @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {

		log.info("GET /api/customers - page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

		return ResponseEntity.ok(customerService.getAllCustomers(pageable));
	}

	// =====================================================
	// SEARCH CUSTOMERS
	// JPQL + PAGINATION + SORTING
	// =====================================================

	@GetMapping("/search")
	@Operation(summary = "Search customers", description = """
			Searches customers by first name or last name.

			Uses JPQL with pagination and sorting.
			""")
	public ResponseEntity<Page<CustomerResponseDTO>> searchCustomers(

			@Parameter(description = "Search keyword", example = "Pratap") @RequestParam String keyword,

			@PageableDefault(page = 0, size = 10, sort = "firstName") Pageable pageable) {

		log.info("GET /api/customers/search?keyword={}", keyword);

		return ResponseEntity.ok(customerService.searchCustomers(keyword, pageable));
	}

	// =====================================================
	// UPDATE CUSTOMER
	// =====================================================

	@PutMapping("/{id}")
	@Operation(summary = "Update customer", description = "Updates an existing customer")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
			@ApiResponse(responseCode = "404", description = "Customer not found") })
	public ResponseEntity<CustomerResponseDTO> updateCustomer(

			@PathVariable Long id,

			@Valid @RequestBody CustomerRequestDTO requestDTO) {

		log.info("PUT /api/customers/{}", id);

		return ResponseEntity.ok(customerService.updateCustomer(id, requestDTO));
	}

	// =====================================================
	// DELETE CUSTOMER
	// =====================================================

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete customer", description = "Deletes an existing customer")
	@ApiResponses({ @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Customer not found") })
	public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {

		log.warn("DELETE /api/customers/{}", id);

		customerService.deleteCustomer(id);

		return ResponseEntity.noContent().build();
	}
}
