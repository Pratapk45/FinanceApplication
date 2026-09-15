package com.example.finance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import com.example.finance.entity.Customer;
import com.example.finance.exception.AccountNotFoundException;
import com.example.finance.exception.CustomerNotFoundException;
import com.example.finance.exception.DuplicateCustomerException;
import com.example.finance.mapper.AccountMapper;
import com.example.finance.mapper.CustomerMapper;
import com.example.finance.repository.AccountRepository;
import com.example.finance.repository.CustomerRepository;
import com.example.finance.requestDto.AccountRequestDTO;
import com.example.finance.requestDto.CustomerRequestDTO;
import com.example.finance.responceDto.AccountResponseDTO;
import com.example.finance.responceDto.CustomerResponseDTO;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    private Customer customer;

    private Account account;

    private AccountRequestDTO requestDTO;

    private AccountResponseDTO responseDTO;


    @BeforeEach
    void setUp() {

        customer = new Customer();

        customer.setId(1L);
        customer.setFirstName("Pratap");
        customer.setLastName("Kachare");
        customer.setEmail("pratap@example.com");


        account = new Account();

        account.setId(1L);
        account.setAccountNumber("ACC1000001");
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(
                new BigDecimal("10000.00")
        );
        account.setStatus(AccountStatus.ACTIVE);
        account.setCustomer(customer);


        requestDTO = AccountRequestDTO.builder()
                .id(1L)
                .customerId(1L)
                .accountType(AccountType.SAVINGS)
                .build();


        responseDTO = AccountResponseDTO.builder()
                .id(1L)
                .accountNumber("ACC1000001")
                .accountType(AccountType.SAVINGS)
                .balance(
                        new BigDecimal("10000.00")
                )
                .status(AccountStatus.ACTIVE)
                .build();
    }


    // ============================================================
    // CREATE ACCOUNT
    // ============================================================

    @Test
    void createOrUpdateAccount_shouldCreateSuccessfully() {

        requestDTO.setId(null);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(accountMapper.toEntity(requestDTO))
                .thenReturn(account);

        when(accountRepository.save(account))
                .thenReturn(account);

        when(accountMapper.toResponseDTO(account))
                .thenReturn(responseDTO);


        AccountResponseDTO result =
                accountService.createOrUpdate(requestDTO);


        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "ACC1000001",
                result.getAccountNumber()
        );

        assertEquals(
                AccountType.SAVINGS,
                result.getAccountType()
        );

        assertEquals(
                new BigDecimal("10000.00"),
                result.getBalance()
        );


        verify(customerRepository)
                .findById(1L);

        verify(accountMapper)
                .toEntity(requestDTO);

        verify(accountRepository)
                .save(account);

        verify(accountMapper)
                .toResponseDTO(account);
    }


    @Test
    void createOrUpdateAccount_shouldThrowCustomerNotFoundException() {

        requestDTO.setId(null);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                CustomerNotFoundException.class,
                () -> accountService.createOrUpdate(requestDTO)
        );


        verify(customerRepository)
                .findById(1L);

        verify(accountRepository, never())
                .save(any(Account.class));
    }


    // ============================================================
    // UPDATE ACCOUNT
    // ============================================================

    @Test
    void createOrUpdateAccount_shouldUpdateSuccessfully() {

        requestDTO.setId(1L);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(accountRepository.save(account))
                .thenReturn(account);

        when(accountMapper.toResponseDTO(account))
                .thenReturn(responseDTO);


        AccountResponseDTO result =
                accountService.createOrUpdate(requestDTO);


        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                AccountType.SAVINGS,
                result.getAccountType()
        );


        verify(accountRepository)
                .findById(1L);

        verify(customerRepository)
                .findById(1L);

        verify(accountRepository)
                .save(account);

        verify(accountMapper)
                .toResponseDTO(account);

        verify(accountMapper, never())
                .toEntity(any(AccountRequestDTO.class));
    }


    @Test
    void createOrUpdateAccount_shouldThrowAccountNotFoundException() {

        requestDTO.setId(999L);

        when(accountRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.createOrUpdate(requestDTO)
        );


        verify(accountRepository)
                .findById(999L);

        verify(accountRepository, never())
                .save(any(Account.class));
    }


    // ============================================================
    // GET ACCOUNT BY ID
    // ============================================================

    @Test
    void getAccountById_shouldReturnAccount() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(accountMapper.toResponseDTO(account))
                .thenReturn(responseDTO);


        AccountResponseDTO result =
                accountService.getById(1L);


        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "ACC1000001",
                result.getAccountNumber()
        );

        assertEquals(
                "10000.00",
                result.getBalance().toString()
        );


        verify(accountRepository)
                .findById(1L);

        verify(accountMapper)
                .toResponseDTO(account);
    }


    @Test
    void getAccountById_shouldThrowAccountNotFoundException() {

        when(accountRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getById(999L)
        );


        verify(accountRepository)
                .findById(999L);

        verify(accountMapper, never())
                .toResponseDTO(any(Account.class));
    }



    // ============================================================
    // GET ACCOUNTS BY CUSTOMER
    // ============================================================

    @Test
    void getAccountsByCustomer_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Account> accountPage =
                new PageImpl<>(
                        java.util.List.of(account)
                );

        when(accountRepository.findByCustomerId(
                1L,
                pageable
        )).thenReturn(accountPage);

        when(accountMapper.toResponseDTO(account))
                .thenReturn(responseDTO);


        Page<AccountResponseDTO> result =
                accountService.getAccountsByCustomer(
                        1L,
                        pageable
                );


        assertEquals(
                1,
                result.getTotalElements()
        );


        verify(accountRepository)
                .findByCustomerId(
                        1L,
                        pageable
                );

        verify(accountMapper)
                .toResponseDTO(account);
    }


  
}

//One important point
//Your TransactionService is significantly more important to test than a simple CRUD service because it contains actual financial business logic. For example, the current implementation:
//
//validates transaction amount,
//
//handles DEPOSIT/WITHDRAWAL/TRANSFER,
//
//checks account status,
//
//prevents insufficient-balance withdrawals,
//
//locks the account for update,
//
//updates balances,
//
//creates debit/credit records for transfers. 
//
//
