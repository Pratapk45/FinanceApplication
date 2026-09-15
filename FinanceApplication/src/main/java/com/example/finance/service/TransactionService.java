package com.example.finance.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.finance.entity.Account;
import com.example.finance.entity.AccountStatus;
import com.example.finance.entity.Transaction;
import com.example.finance.entity.enums.TransactionDirection;
import com.example.finance.entity.enums.TransactionStatus;
import com.example.finance.entity.enums.TransactionType;
import com.example.finance.exception.AccountNotActiveException;
import com.example.finance.exception.AccountNotFoundException;
import com.example.finance.exception.InsufficientBalanceException;
import com.example.finance.exception.InvalidTransactionAmountException;
import com.example.finance.exception.InvalidTransferException;
import com.example.finance.exception.TransactionNotFoundException;
import com.example.finance.exception.UnsupportedTransactionTypeException;
import com.example.finance.mapper.TransactionMapper;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.requestDto.TransactionRequestDTO;
import com.example.finance.responceDto.TransactionResponseDTO;
import com.example.finance.responceDto.TransactionResultResponseDTO;
import com.example.finance.responceDto.TransferResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

	private final TransactionRepository transactionRepository;

	private final AccountRepository accountRepository;

	private final TransactionMapper transactionMapper;

	/*
	 * ============================================================ CREATE
	 * TRANSACTION ============================================================
	 *
	 * One method handles:
	 *
	 * DEPOSIT WITHDRAWAL TRANSFER
	 */
	@Transactional
	public TransactionResultResponseDTO createTransaction(TransactionRequestDTO requestDTO) {

		validateAmount(requestDTO.getAmount());

		if (requestDTO.getTransactionType() == null) {

			throw new UnsupportedTransactionTypeException("Transaction type is required");
		}

		log.info("Starting transaction. accountId={}, type={}, amount={}", requestDTO.getAccountId(),
				requestDTO.getTransactionType(), requestDTO.getAmount());

		switch (requestDTO.getTransactionType()) {

		case DEPOSIT -> {

			Account account = getActiveAccountForUpdate(requestDTO.getAccountId());

			Transaction transaction = processDeposit(account, requestDTO);

			Transaction saved = transactionRepository.save(transaction);

			log.info("Deposit successful. reference={}, accountId={}, amount={}", saved.getTransactionReference(),
					account.getId(), saved.getAmount());

			return TransactionResultResponseDTO.builder().transactionType(TransactionType.DEPOSIT)
					.transaction(transactionMapper.toResponseDTO(saved)).build();
		}

		case WITHDRAWAL -> {

			Account account = getActiveAccountForUpdate(requestDTO.getAccountId());

			Transaction transaction = processWithdrawal(account, requestDTO);

			Transaction saved = transactionRepository.save(transaction);

			log.info("Withdrawal successful. reference={}, accountId={}, amount={}", saved.getTransactionReference(),
					account.getId(), saved.getAmount());

			return TransactionResultResponseDTO.builder().transactionType(TransactionType.WITHDRAWAL)
					.transaction(transactionMapper.toResponseDTO(saved)).build();
		}

		case TRANSFER -> {

			return processTransfer(requestDTO);
		}

		default -> throw new UnsupportedTransactionTypeException(
				"Unsupported transaction type: " + requestDTO.getTransactionType());
		}
	}

	/*
	 * ============================================================ DEPOSIT
	 * ============================================================
	 */
	private Transaction processDeposit(Account account, TransactionRequestDTO requestDTO) {

		BigDecimal balanceBefore = account.getBalance();

		BigDecimal balanceAfter = balanceBefore.add(requestDTO.getAmount());

		account.setBalance(balanceAfter);

		return Transaction.builder()
				.transactionReference(generateTransactionReference())
				.account(account)
				.amount(requestDTO.getAmount())
				.balanceBefore(balanceBefore)
				.balanceAfter(balanceAfter)
				.transactionType(TransactionType.DEPOSIT)
				.direction(TransactionDirection.CREDIT)
				.status(TransactionStatus.SUCCESS)
				.description(requestDTO.getDescription())
				.build();
	}

	/*
	 * ============================================================ WITHDRAWAL
	 * ============================================================
	 */
	private Transaction processWithdrawal(Account account, TransactionRequestDTO requestDTO) {

		BigDecimal balanceBefore = account.getBalance();

		BigDecimal amount = requestDTO.getAmount();

		if (balanceBefore.compareTo(amount) < 0) {

			throw new InsufficientBalanceException("Insufficient account balance");
		}

		BigDecimal balanceAfter = balanceBefore.subtract(amount);

		account.setBalance(balanceAfter);

		return Transaction.builder().transactionReference(generateTransactionReference()).account(account)
				.amount(amount).balanceBefore(balanceBefore).balanceAfter(balanceAfter)
				.transactionType(TransactionType.WITHDRAWAL).direction(TransactionDirection.DEBIT)
				.status(TransactionStatus.SUCCESS).description(requestDTO.getDescription()).build();
	}

	/*
	 * ============================================================ 
	 * TRANSFER
	 * ============================================================
	 *
	 * One transfer creates TWO transaction records:
	 *
	 * Source: DEBIT
	 *
	 * Destination: CREDIT
	 */
	private TransactionResultResponseDTO processTransfer(TransactionRequestDTO requestDTO) {

		validateTransferRequest(requestDTO);

		Long sourceId = requestDTO.getAccountId();

		Long destinationId = requestDTO.getDestinationAccountId();

		if (sourceId.equals(destinationId)) {

			throw new InvalidTransferException("Source and destination accounts must be different");
		}

		/*
		 * Lock accounts in consistent ID order.
		 *
		 * This reduces deadlock risk when two transfers happen at the same time in
		 * opposite directions.
		 */
		Account firstLockedAccount;

		Account secondLockedAccount;

		if (sourceId < destinationId) {

			firstLockedAccount = getActiveAccountForUpdate(sourceId);

			secondLockedAccount = getActiveAccountForUpdate(destinationId);

		} else {

			firstLockedAccount = getActiveAccountForUpdate(destinationId);

			secondLockedAccount = getActiveAccountForUpdate(sourceId);
		}

		/*
		 * Identify source and destination after both rows have been locked.
		 */
		Account sourceAccount = sourceId.equals(firstLockedAccount.getId()) ? firstLockedAccount : secondLockedAccount;

		Account destinationAccount = destinationId.equals(firstLockedAccount.getId()) ? firstLockedAccount
				: secondLockedAccount;

		BigDecimal amount = requestDTO.getAmount();

		/*
		 * Check source balance.
		 */
		BigDecimal sourceBalanceBefore = sourceAccount.getBalance();

		if (sourceBalanceBefore.compareTo(amount) < 0) {

			throw new InsufficientBalanceException("Insufficient account balance for transfer");
		}

		/*
		 * One reference for the complete transfer.
		 */
		String transferReference = generateTransferReference();

		/*
		 * ======================================================== SOURCE ACCOUNT
		 * ========================================================
		 */

		BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(amount);

		sourceAccount.setBalance(sourceBalanceAfter);

		Transaction debitTransaction = Transaction.builder().transactionReference(generateTransactionReference())
				.account(sourceAccount).amount(amount).balanceBefore(sourceBalanceBefore)
				.balanceAfter(sourceBalanceAfter).transactionType(TransactionType.TRANSFER)
				.direction(TransactionDirection.DEBIT).status(TransactionStatus.SUCCESS)
				.transferReference(transferReference).description(requestDTO.getDescription()).build();

		/*
		 * ======================================================== DESTINATION ACCOUNT
		 * ========================================================
		 */

		BigDecimal destinationBalanceBefore = destinationAccount.getBalance();

		BigDecimal destinationBalanceAfter = destinationBalanceBefore.add(amount);

		destinationAccount.setBalance(destinationBalanceAfter);

		Transaction creditTransaction = Transaction.builder().transactionReference(generateTransactionReference())
				.account(destinationAccount).amount(amount).balanceBefore(destinationBalanceBefore)
				.balanceAfter(destinationBalanceAfter).transactionType(TransactionType.TRANSFER)
				.direction(TransactionDirection.CREDIT).status(TransactionStatus.SUCCESS)
				.transferReference(transferReference).description(requestDTO.getDescription()).build();

		/*
		 * Save both records.
		 */
		Transaction savedDebit = transactionRepository.save(debitTransaction);

		Transaction savedCredit = transactionRepository.save(creditTransaction);

		log.info("Transfer successful. reference={}, sourceAccountId={}, destinationAccountId={}, amount={}",
				transferReference, sourceAccount.getId(), destinationAccount.getId(), amount);

		/*
		 * Map both transaction records.
		 */
		TransactionResponseDTO debitResponse = transactionMapper.toResponseDTO(savedDebit);

		TransactionResponseDTO creditResponse = transactionMapper.toResponseDTO(savedCredit);

		TransferResponseDTO transferResponse = TransferResponseDTO.builder().transferReference(transferReference)
				.amount(amount).status(TransactionStatus.SUCCESS).sourceAccount(debitResponse.getAccount())
				.destinationAccount(creditResponse.getAccount()).debitTransaction(debitResponse)
				.creditTransaction(creditResponse).build();

		return TransactionResultResponseDTO.builder().transactionType(TransactionType.TRANSFER)
				.transfer(transferResponse).build();
	}

	/*
	 * ============================================================ 
	 * ACCOUNT LOOKUP WITH DATABASE LOCK
	 * ============================================================
	 */
	private Account getActiveAccountForUpdate(Long accountId) {

		Account account = accountRepository.findByIdForUpdate(accountId)
				.orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));

		if (account.getStatus() != AccountStatus.ACTIVE) {

			throw new AccountNotActiveException("Account is not active: " + accountId);
		}

		return account;
	}

	/*
	 * ============================================================ 
	 * VALIDATE AMOUNT
	 * ============================================================
	 */
	private void validateAmount(BigDecimal amount) {

		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {

			throw new InvalidTransactionAmountException("Transaction amount must be greater than zero");
		}
	}

	/*
	 * ============================================================ VALIDATE
	 * TRANSFER ============================================================
	 */
	private void validateTransferRequest(TransactionRequestDTO requestDTO) {

		if (requestDTO.getDestinationAccountId() == null) {

			throw new InvalidTransferException("Destination account ID is required for transfer");
		}
	}

	/*
	 * ============================================================ 
	 * TRANSACTION HISTORY
	 *  ============================================================
	 */
	@Transactional(readOnly = true)
	public Page<TransactionResponseDTO> getTransactionsByAccount(Long accountId, Pageable pageable) {

		if (!accountRepository.existsById(accountId)) {

			throw new AccountNotFoundException("Account not found with id: " + accountId);
		}

		log.debug("Fetching transaction history. accountId={}, page={}, size={}", accountId, pageable.getPageNumber(),
				pageable.getPageSize());

		return transactionRepository.findByAccountId(accountId, pageable).map(transactionMapper::toResponseDTO);
	}

	/*
	 * ============================================================ 
	 * GET TRANSACTION BY REFERENCE 
	 * ============================================================
	 */
	@Transactional(readOnly = true)
	public TransactionResponseDTO getByReference(String transactionReference) {

		Transaction transaction = transactionRepository.findByTransactionReference(transactionReference)
				.orElseThrow(() -> new TransactionNotFoundException(
						"Transaction not found with reference: " + transactionReference));

		return transactionMapper.toResponseDTO(transaction);
	}

	/*
	 * ============================================================
	 *  REFERENCE GENERATORS
	 *  ============================================================
	 */
	private String generateTransactionReference() {

		return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
	}

	private String generateTransferReference() {

		return "TRF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
	}
}
