package com.example.finance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finance.entity.Customer;
import com.example.finance.exception.CustomerNotFoundException;
import com.example.finance.exception.DuplicateCustomerException;
import com.example.finance.mapper.CustomerMapper;
import com.example.finance.repository.CustomerRepository;
import com.example.finance.requestDto.CustomerRequestDTO;
import com.example.finance.responceDto.CustomerResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional
public class CustomerService {

	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private CustomerMapper customerMapper;

	/*
	 * CREATE CUSTOMER
	 */
	public CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO) {

		log.info("Creating customer with email: {}", requestDTO.getEmail());

		if (customerRepository.findByEmail(requestDTO.getEmail()).isPresent()) {

			log.warn("Customer already exists with email: {}", requestDTO.getEmail());

			throw new DuplicateCustomerException(requestDTO.getEmail());
		}

		Customer customer = customerMapper.toEntity(requestDTO);

		Customer savedCustomer = customerRepository.save(customer);

		log.info("Customer created successfully. ID: {}", savedCustomer.getId());

		return customerMapper.toResponseDTO(savedCustomer);
	}

	/*
	 * GET CUSTOMER BY ID
	 */
	@Transactional(readOnly = true)
	public CustomerResponseDTO getCustomerById(Long id) {

		log.info("Fetching customer with ID: {}", id);

		Customer customer = customerRepository.findById(id)
				.orElseThrow(() -> new CustomerNotFoundException(id));

		return customerMapper.toResponseDTO(customer);
	}

	/*
	 * GET ALL CUSTOMERS
	 *
	 * Pageable provides: - Page number - Page size - Sorting
	 */
	@Transactional(readOnly = true)
	public Page<CustomerResponseDTO> getAllCustomers(Pageable pageable) {

		log.info("Fetching customers. Page: {}, Size: {}, Sort: {}", pageable.getPageNumber(), pageable.getPageSize(),
				pageable.getSort());

		return customerRepository.findAll(pageable).map(customerMapper::toResponseDTO);
	}

	/*
	 * SEARCH CUSTOMERS
	 *
	 * Uses JPQL query from repository.
	 */
	@Transactional(readOnly = true)
	public Page<CustomerResponseDTO> searchCustomers(String keyword, Pageable pageable) {

		log.info("Searching customers with keyword: {}", keyword);

		return customerRepository.searchCustomers(keyword, pageable).map(customerMapper::toResponseDTO);
	}

	/*
	 * UPDATE CUSTOMER
	 */
	public CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO) {

		log.info("Updating customer with ID: {}", id);

		Customer customer = customerRepository.findById(id)
				.orElseThrow(() -> new CustomerNotFoundException(id)
		);

		customer.setFirstName(requestDTO.getFirstName());

		customer.setLastName(requestDTO.getLastName());

		customer.setEmail(requestDTO.getEmail());

		customer.setPhone(requestDTO.getPhone());

		customer.setAddress(requestDTO.getAddress());

		Customer updatedCustomer = customerRepository.save(customer);

		log.info("Customer updated successfully. ID: {}", updatedCustomer.getId());

		return customerMapper.toResponseDTO(updatedCustomer);
	}

	/*
	 * DELETE CUSTOMER
	 */
	public void deleteCustomer(Long id) {

		log.warn("Deleting customer with ID: {}", id);

		if (!customerRepository.existsById(id)) {

			log.error("Customer not found. ID: {}", id);

			throw new CustomerNotFoundException(id);
		}

		customerRepository.deleteById(id);

		log.info("Customer deleted successfully. ID: {}", id);
	}
}