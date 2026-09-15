package com.example.finance.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finance.entity.Account;
import com.example.finance.entity.AccountStatus;
import com.example.finance.entity.Customer;
import com.example.finance.exception.AccountNotFoundException;
import com.example.finance.exception.CustomerNotFoundException;
import com.example.finance.mapper.AccountMapper;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.CustomerRepository;
import com.example.finance.requestDto.AccountRequestDTO;
import com.example.finance.responceDto.AccountResponseDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountService {

	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private AccountMapper accountMapper;

	/**
	 * Creates a new account or updates an existing account.
	 *
	 * CREATE: request.id == null
	 *
	 * UPDATE: request.id != null
	 */
	@Transactional
	public AccountResponseDTO createOrUpdate(AccountRequestDTO requestDTO) {

		if (requestDTO.getId() == null) {

			log.info("Creating new account for customerId={}", requestDTO.getCustomerId());

			return createAccount(requestDTO);
		}

		log.info("Updating account id={}", requestDTO.getId());

		return updateAccount(requestDTO);
	}

	/**
	 * Internal CREATE logic.
	 */
	private AccountResponseDTO createAccount(AccountRequestDTO requestDTO) {

		Customer customer = customerRepository.findById(requestDTO.getCustomerId())
				.orElseThrow(() -> new CustomerNotFoundException(requestDTO.getCustomerId()));

		Account account = accountMapper.toEntity(requestDTO);

		/*
		 * Customer is deliberately not mapped by MapStruct. We set the managed Customer
		 * entity here.
		 */
		account.setCustomer(customer);

		/*
		 * Account number belongs to the application, not to the client.
		 */
		account.setAccountNumber(generateAccountNumber());

		/*
		 * Every newly created account starts with zero balance.
		 */
		account.setBalance(BigDecimal.ZERO);

		/*
		 * New accounts are ACTIVE by default.
		 */
		account.setStatus(AccountStatus.ACTIVE);

		Account savedAccount = accountRepository.save(account);

		log.info("Account created successfully. accountId={}, accountNumber={}, customerId={}", savedAccount.getId(),
				savedAccount.getAccountNumber(), customer.getId());

		return accountMapper.toResponseDTO(savedAccount);
	}

	/**
	 * Internal UPDATE logic.
	 *
	 * We load the existing managed entity and update only fields allowed by the
	 * AccountMapper.
	 */
	private AccountResponseDTO updateAccount(AccountRequestDTO requestDTO) {

		Account account = accountRepository.findById(requestDTO.getId()).orElseThrow(() -> {
			log.warn("Account not found for update. accountId={}", requestDTO.getId());

			return new AccountNotFoundException("Account not found with id: " + requestDTO.getId());
		});

		/*
		 * Closed accounts cannot be modified through normal account update.
		 */
		if (account.getStatus() == AccountStatus.CLOSED) {

			log.warn("Update rejected because account is closed. accountId={}", account.getId());

			throw new IllegalStateException("Closed account cannot be updated");
		}

		/*
		 * IMPORTANT:
		 *
		 * This updates the existing managed entity.
		 *
		 * It does NOT replace: - accountNumber - customer - balance - status - version
		 * - audit fields
		 */
		accountMapper.updateEntity(requestDTO, account);

		Account updatedAccount = accountRepository.save(account);

		log.info("Account updated successfully. accountId={}, version={}", updatedAccount.getId(),
				updatedAccount.getVersion());

		return accountMapper.toResponseDTO(updatedAccount);
	}

	/**
	 * Finds an account by its database ID.
	 */
	@Transactional(readOnly = true)
	public AccountResponseDTO getById(Long id) {

		log.debug("Fetching account. accountId={}", id);

		Account account = accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));

		return accountMapper.toResponseDTO(account);
	}

	/**
	 * Finds an account by account number.
	 */
	@Transactional(readOnly = true)
	public AccountResponseDTO getByAccountNumber(String accountNumber) {

		log.debug("Fetching account by accountNumber={}", accountNumber);

		Account account = accountRepository.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new AccountNotFoundException(accountNumber));
		return accountMapper.toResponseDTO(account);
	}

	/**
	 * Gets all accounts of a customer with pagination and sorting.
	 */
	@Transactional(readOnly = true)
	public Page<AccountResponseDTO> getAccountsByCustomer(Long customerId, Pageable pageable) {

		log.debug("Fetching accounts for customerId={}, page={}, size={}", customerId, pageable.getPageNumber(),
				pageable.getPageSize());

		if (!customerRepository.existsById(customerId)) {

			log.warn("Customer not found while fetching accounts. customerId={}", customerId);

			throw new CustomerNotFoundException(customerId);

		}

		return accountRepository.findByCustomerId(customerId, pageable).map(accountMapper::toResponseDTO);
	}

	/**
	 * Generates an application-owned account number.
	 *
	 * This is intentionally not accepted from the request.
	 */
	private String generateAccountNumber() {

		String accountNumber;

		do {
			accountNumber = "ACC" + UUID.randomUUID().toString().replace("-", "").substring(0, 15).toUpperCase();

		} while (accountRepository.existsByAccountNumber(accountNumber));

		return accountNumber;
	}
}
