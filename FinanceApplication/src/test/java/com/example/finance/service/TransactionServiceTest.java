package com.example.finance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.finance.entity.Account;
import com.example.finance.entity.AccountStatus;
import com.example.finance.entity.AccountType;
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
import com.example.finance.mapper.TransactionMapper;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.TransactionRepository;
import com.example.finance.requestDto.TransactionRequestDTO;
import com.example.finance.responceDto.TransactionResponseDTO;
import com.example.finance.responceDto.TransactionResultResponseDTO;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;

    private Account destinationAccount;

    private TransactionRequestDTO depositRequest;

    private TransactionRequestDTO withdrawalRequest;

    private TransactionRequestDTO transferRequest;

    private TransactionResponseDTO transactionResponse;


    @BeforeEach
    void setUp() {

        sourceAccount = new Account();

        sourceAccount.setId(1L);
        sourceAccount.setAccountNumber("ACC1000001");
        sourceAccount.setAccountType(AccountType.SAVINGS);
        sourceAccount.setBalance(
                new BigDecimal("10000.00")
        );
        sourceAccount.setStatus(AccountStatus.ACTIVE);


        destinationAccount = new Account();

        destinationAccount.setId(2L);
        destinationAccount.setAccountNumber("ACC1000002");
        destinationAccount.setAccountType(AccountType.SAVINGS);
        destinationAccount.setBalance(
                new BigDecimal("5000.00")
        );
        destinationAccount.setStatus(AccountStatus.ACTIVE);


        depositRequest = TransactionRequestDTO.builder()
                .accountId(1L)
                .amount(new BigDecimal("1000.00"))
                .transactionType(TransactionType.DEPOSIT)
                .description("Cash deposit")
                .build();


        withdrawalRequest = TransactionRequestDTO.builder()
                .accountId(1L)
                .amount(new BigDecimal("2000.00"))
                .transactionType(TransactionType.WITHDRAWAL)
                .description("Cash withdrawal")
                .build();


        transferRequest = TransactionRequestDTO.builder()
                .accountId(1L)
                .destinationAccountId(2L)
                .amount(new BigDecimal("3000.00"))
                .transactionType(TransactionType.TRANSFER)
                .description("Transfer to another account")
                .build();


        transactionResponse =
                TransactionResponseDTO.builder()
                        .id(1L)
                        .transactionReference("TXN-A12B34C56D78")
                        .amount(new BigDecimal("1000.00"))
                        .transactionType(TransactionType.DEPOSIT)
                        .direction(TransactionDirection.CREDIT)
                        .status(TransactionStatus.SUCCESS)
                        .build();
    }


    // ============================================================
    // DEPOSIT
    // ============================================================

    @Test
    void createTransaction_deposit_shouldSucceed() {

        Transaction savedTransaction =
                Transaction.builder()
                        .transactionReference(
                                "TXN-A12B34C56D78"
                        )
                        .account(sourceAccount)
                        .amount(new BigDecimal("1000.00"))
                        .balanceBefore(
                                new BigDecimal("10000.00")
                        )
                        .balanceAfter(
                                new BigDecimal("11000.00")
                        )
                        .transactionType(
                                TransactionType.DEPOSIT
                        )
                        .direction(
                                TransactionDirection.CREDIT
                        )
                        .status(
                                TransactionStatus.SUCCESS
                        )
                        .description("Cash deposit")
                        .build();


        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(sourceAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        when(transactionMapper.toResponseDTO(savedTransaction))
                .thenReturn(transactionResponse);


        TransactionResultResponseDTO result =
                transactionService.createTransaction(
                        depositRequest
                );


        assertNotNull(result);

        assertEquals(
                TransactionType.DEPOSIT,
                result.getTransactionType()
        );

        assertEquals(
                new BigDecimal("11000.00"),
                sourceAccount.getBalance()
        );


        verify(accountRepository)
                .findByIdForUpdate(1L);

        verify(transactionRepository)
                .save(any(Transaction.class));

        verify(transactionMapper)
                .toResponseDTO(savedTransaction);
    }


    // ============================================================
    // WITHDRAWAL
    // ============================================================

    @Test
    void createTransaction_withdrawal_shouldSucceed() {

        Transaction savedTransaction =
                Transaction.builder()
                        .transactionReference(
                                "TXN-W12B34C56D78"
                        )
                        .account(sourceAccount)
                        .amount(new BigDecimal("2000.00"))
                        .balanceBefore(
                                new BigDecimal("10000.00")
                        )
                        .balanceAfter(
                                new BigDecimal("8000.00")
                        )
                        .transactionType(
                                TransactionType.WITHDRAWAL
                        )
                        .direction(
                                TransactionDirection.DEBIT
                        )
                        .status(
                                TransactionStatus.SUCCESS
                        )
                        .description("Cash withdrawal")
                        .build();


        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(sourceAccount));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        when(transactionMapper.toResponseDTO(savedTransaction))
                .thenReturn(transactionResponse);


        TransactionResultResponseDTO result =
                transactionService.createTransaction(
                        withdrawalRequest
                );


        assertNotNull(result);

        assertEquals(
                TransactionType.WITHDRAWAL,
                result.getTransactionType()
        );

        assertEquals(
                new BigDecimal("8000.00"),
                sourceAccount.getBalance()
        );


        verify(accountRepository)
                .findByIdForUpdate(1L);

        verify(transactionRepository)
                .save(any(Transaction.class));
    }


    // ============================================================
    // INSUFFICIENT BALANCE - WITHDRAWAL
    // ============================================================

    @Test
    void createTransaction_withdrawalWithInsufficientBalance_shouldThrowException() {

        withdrawalRequest.setAmount(
                new BigDecimal("15000.00")
        );


        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(sourceAccount));


        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.createTransaction(
                        withdrawalRequest
                )
        );


        assertEquals(
                new BigDecimal("10000.00"),
                sourceAccount.getBalance()
        );


        verify(transactionRepository, never())
                .save(any(Transaction.class));

        verify(transactionMapper, never())
                .toResponseDTO(any(Transaction.class));
    }


    // ============================================================
    // TRANSFER
    // ============================================================

    @Test
    void createTransaction_transfer_shouldCreateDebitAndCredit() {

        Transaction debitTransaction =
                Transaction.builder()
                        .account(sourceAccount)
                        .amount(new BigDecimal("3000.00"))
                        .balanceBefore(
                                new BigDecimal("10000.00")
                        )
                        .balanceAfter(
                                new BigDecimal("7000.00")
                        )
                        .transactionType(
                                TransactionType.TRANSFER
                        )
                        .direction(
                                TransactionDirection.DEBIT
                        )
                        .status(
                                TransactionStatus.SUCCESS
                        )
                        .build();


        Transaction creditTransaction =
                Transaction.builder()
                        .account(destinationAccount)
                        .amount(new BigDecimal("3000.00"))
                        .balanceBefore(
                                new BigDecimal("5000.00")
                        )
                        .balanceAfter(
                                new BigDecimal("8000.00")
                        )
                        .transactionType(
                                TransactionType.TRANSFER
                        )
                        .direction(
                                TransactionDirection.CREDIT
                        )
                        .status(
                                TransactionStatus.SUCCESS
                        )
                        .build();


        TransactionResponseDTO debitResponse =
                TransactionResponseDTO.builder()
                        .id(1L)
                        .transactionType(
                                TransactionType.TRANSFER
                        )
                        .direction(
                                TransactionDirection.DEBIT
                        )
                        .status(
                                TransactionStatus.SUCCESS
                        )
                        .build();


        TransactionResponseDTO creditResponse =
                TransactionResponseDTO.builder()
                        .id(2L)
                        .transactionType(
                                TransactionType.TRANSFER
                        )
                        .direction(
                                TransactionDirection.CREDIT
                        )
                        .status(
                                TransactionStatus.SUCCESS
                        )
                        .build();


        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(destinationAccount));


        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(
                        debitTransaction,
                        creditTransaction
                );


        when(transactionMapper.toResponseDTO(
                debitTransaction
        )).thenReturn(debitResponse);


        when(transactionMapper.toResponseDTO(
                creditTransaction
        )).thenReturn(creditResponse);


        TransactionResultResponseDTO result =
                transactionService.createTransaction(
                        transferRequest
                );


        assertNotNull(result);

        assertEquals(
                TransactionType.TRANSFER,
                result.getTransactionType()
        );


        assertEquals(
                new BigDecimal("7000.00"),
                sourceAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("8000.00"),
                destinationAccount.getBalance()
        );


        verify(accountRepository)
                .findByIdForUpdate(1L);

        verify(accountRepository)
                .findByIdForUpdate(2L);

        verify(transactionRepository, times(2))
                .save(any(Transaction.class));

        verify(transactionMapper)
                .toResponseDTO(debitTransaction);

        verify(transactionMapper)
                .toResponseDTO(creditTransaction);
    }


    // ============================================================
    // TRANSFER - SAME ACCOUNT
    // ============================================================

    @Test
    void createTransaction_transferToSameAccount_shouldThrowException() {

        transferRequest.setDestinationAccountId(1L);


        assertThrows(
                InvalidTransferException.class,
                () -> transactionService.createTransaction(
                        transferRequest
                )
        );


        verify(
                accountRepository,
                never()
        ).findByIdForUpdate(any(Long.class));

        verify(
                transactionRepository,
                never()
        ).save(any(Transaction.class));
    }


    // ============================================================
    // TRANSFER - DESTINATION ACCOUNT REQUIRED
    // ============================================================

    @Test
    void createTransaction_transferWithoutDestination_shouldThrowException() {

        transferRequest.setDestinationAccountId(null);


        assertThrows(
                InvalidTransferException.class,
                () -> transactionService.createTransaction(
                        transferRequest
                )
        );


        verify(
                accountRepository,
                never()
        ).findByIdForUpdate(any(Long.class));

        verify(
                transactionRepository,
                never()
        ).save(any(Transaction.class));
    }


    // ============================================================
    // TRANSFER - INSUFFICIENT BALANCE
    // ============================================================

    @Test
    void createTransaction_transferWithInsufficientBalance_shouldThrowException() {

        transferRequest.setAmount(
                new BigDecimal("15000.00")
        );


        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByIdForUpdate(2L))
                .thenReturn(Optional.of(destinationAccount));


        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.createTransaction(
                        transferRequest
                )
        );


        assertEquals(
                new BigDecimal("10000.00"),
                sourceAccount.getBalance()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                destinationAccount.getBalance()
        );


        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    // ============================================================
    // ACCOUNT NOT FOUND
    // ============================================================

    @Test
    void createTransaction_accountNotFound_shouldThrowException() {

        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.createTransaction(
                        depositRequest
                )
        );


        verify(accountRepository)
                .findByIdForUpdate(1L);

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    // ============================================================
    // ACCOUNT NOT ACTIVE
    // ============================================================

    @Test
    void createTransaction_inactiveAccount_shouldThrowException() {

        sourceAccount.setStatus(
                AccountStatus.BLOCKED
        );


        when(accountRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(sourceAccount));


        assertThrows(
                AccountNotActiveException.class,
                () -> transactionService.createTransaction(
                        depositRequest
                )
        );


        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }


    // ============================================================
    // INVALID AMOUNT
    // ============================================================

    @Test
    void createTransaction_zeroAmount_shouldThrowException() {

        depositRequest.setAmount(
                BigDecimal.ZERO
        );


        assertThrows(
                InvalidTransactionAmountException.class,
                () -> transactionService.createTransaction(
                        depositRequest
                )
        );


        verify(
                accountRepository,
                never()
        ).findByIdForUpdate(any(Long.class));

        verify(
                transactionRepository,
                never()
        ).save(any(Transaction.class));
    }


    @Test
    void createTransaction_negativeAmount_shouldThrowException() {

        depositRequest.setAmount(
                new BigDecimal("-100.00")
        );


        assertThrows(
                InvalidTransactionAmountException.class,
                () -> transactionService.createTransaction(
                        depositRequest
                )
        );


        verify(
                accountRepository,
                never()
        ).findByIdForUpdate(any(Long.class));

        verify(
                transactionRepository,
                never()
        ).save(any(Transaction.class));
    }


    @Test
    void createTransaction_nullAmount_shouldThrowException() {

        depositRequest.setAmount(null);


        assertThrows(
                InvalidTransactionAmountException.class,
                () -> transactionService.createTransaction(
                        depositRequest
                )
        );


        verify(
                accountRepository,
                never()
        ).findByIdForUpdate(any(Long.class));

        verify(
                transactionRepository,
                never()
        ).save(any(Transaction.class));
    }


    // ============================================================
    // TRANSACTION HISTORY
    // ============================================================

    @Test
    void getTransactionsByAccount_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Transaction transaction =
                Transaction.builder()
                        .transactionReference(
                                "TXN-A12B34C56D78"
                        )
                        .account(sourceAccount)
                        .amount(new BigDecimal("1000.00"))
                        .balanceBefore(
                                new BigDecimal("10000.00")
                        )
                        .balanceAfter(
                                new BigDecimal("11000.00")
                        )
                        .transactionType(
                                TransactionType.DEPOSIT
                        )
                        .direction(
                                TransactionDirection.CREDIT
                        )
                        .status(
                                TransactionStatus.SUCCESS
                        )
                        .build();


        Page<Transaction> transactionPage =
                new PageImpl<>(
                        List.of(transaction)
                );


        when(accountRepository.existsById(1L))
                .thenReturn(true);

        when(transactionRepository.findByAccountId(
                1L,
                pageable
        )).thenReturn(transactionPage);

        when(transactionMapper.toResponseDTO(transaction))
                .thenReturn(transactionResponse);


        Page<TransactionResponseDTO> result =
                transactionService.getTransactionsByAccount(
                        1L,
                        pageable
                );


        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );


        verify(accountRepository)
                .existsById(1L);

        verify(transactionRepository)
                .findByAccountId(1L, pageable);

        verify(transactionMapper)
                .toResponseDTO(transaction);
    }


    // ============================================================
    // TRANSACTION HISTORY - ACCOUNT NOT FOUND
    // ============================================================

    @Test
    void getTransactionsByAccount_accountNotFound_shouldThrowException() {

        Pageable pageable =
                PageRequest.of(0, 10);


        when(accountRepository.existsById(999L))
                .thenReturn(false);


        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.getTransactionsByAccount(
                        999L,
                        pageable
                )
        );


        verify(accountRepository)
                .existsById(999L);

        verify(transactionRepository, never())
                .findByAccountId(
                        any(Long.class),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // GET TRANSACTION BY REFERENCE
    // ============================================================

    @Test
    void getByReference_shouldReturnTransaction() {

        String reference =
                "TXN-A12B34C56D78";


        Transaction transaction =
                Transaction.builder()
                        .transactionReference(reference)
                        .account(sourceAccount)
                        .amount(new BigDecimal("1000.00"))
                        .balanceBefore(
                                new BigDecimal("10000.00")
                        )
                        .balanceAfter(
                                new BigDecimal("11000.00")
                        )
                        .transactionType(
                                TransactionType.DEPOSIT
                        )
                        .direction(
                                TransactionDirection.CREDIT
                        )
                        .status(
                                TransactionStatus.SUCCESS
                        )
                        .build();


        when(transactionRepository
                .findByTransactionReference(reference))
                .thenReturn(Optional.of(transaction));

        when(transactionMapper.toResponseDTO(transaction))
                .thenReturn(transactionResponse);


        TransactionResponseDTO result =
                transactionService.getByReference(
                        reference
                );


        assertNotNull(result);

        assertEquals(
                "TXN-A12B34C56D78",
                result.getTransactionReference()
        );


        verify(transactionRepository)
                .findByTransactionReference(reference);

        verify(transactionMapper)
                .toResponseDTO(transaction);
    }


    // ============================================================
    // GET TRANSACTION BY REFERENCE - NOT FOUND
    // ============================================================

    @Test
    void getByReference_notFound_shouldThrowException() {

        String reference =
                "TXN-NOTFOUND";


        when(transactionRepository
                .findByTransactionReference(reference))
                .thenReturn(Optional.empty());


        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getByReference(
                        reference
                )
        );


        verify(transactionRepository)
                .findByTransactionReference(reference);

        verify(transactionMapper, never())
                .toResponseDTO(any(Transaction.class));
    }
}


