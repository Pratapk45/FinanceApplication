package com.example.finance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.example.finance.entity.Customer;
import com.example.finance.entity.Loan;
import com.example.finance.entity.enums.LoanStatus;
import com.example.finance.entity.enums.LoanType;
import com.example.finance.exception.CustomerNotFoundException;
import com.example.finance.exception.LoanBusinessException;
import com.example.finance.exception.LoanNotFoundException;
import com.example.finance.mapper.LoanMapper;
import com.example.finance.repository.CustomerRepository;
import com.example.finance.repository.LoanRepository;
import com.example.finance.requestDto.LoanRequestDTO;
import com.example.finance.responceDto.LoanResponseDTO;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanService loanService;

    private Customer customer;

    private Loan loan;

    private LoanRequestDTO requestDTO;

    private LoanResponseDTO responseDTO;


    @BeforeEach
    void setUp() {

        customer = new Customer();

        customer.setId(1L);
        customer.setFirstName("Pratap");
        customer.setLastName("Kachare");
        customer.setEmail("pratap@example.com");


        loan = new Loan();

        loan.setId(1L);
        loan.setLoanNumber("LN-ABC123456789");
        loan.setCustomer(customer);
        loan.setLoanType(LoanType.PERSONAL);
        loan.setPrincipalAmount(
                new BigDecimal("500000.00")
        );
        loan.setOutstandingAmount(
                new BigDecimal("500000.00")
        );
        loan.setInterestRate(
                new BigDecimal("10.50")
        );
        loan.setTenureMonths(60);
        loan.setStartDate(
                LocalDate.of(2026, 9, 1)
        );
        loan.setMaturityDate(
                LocalDate.of(2031, 9, 1)
        );
        loan.setStatus(LoanStatus.ACTIVE);


        requestDTO = LoanRequestDTO.builder()
                .customerId(1L)
                .loanType(LoanType.PERSONAL)
                .principalAmount(
                        new BigDecimal("500000.00")
                )
                .interestRate(
                        new BigDecimal("10.50")
                )
                .tenureMonths(60)
                .startDate(
                        LocalDate.of(2026, 9, 1)
                )
                .status(LoanStatus.ACTIVE)
                .build();


        responseDTO = LoanResponseDTO.builder()
                .id(1L)
                .loanNumber("LN-ABC123456789")
                .loanType(LoanType.PERSONAL)
                .principalAmount(
                        new BigDecimal("500000.00")
                )
                .outstandingAmount(
                        new BigDecimal("500000.00")
                )
                .interestRate(
                        new BigDecimal("10.50")
                )
                .tenureMonths(60)
                .startDate(
                        LocalDate.of(2026, 9, 1)
                )
                .maturityDate(
                        LocalDate.of(2031, 9, 1)
                )
                .status(LoanStatus.ACTIVE)
                .build();
    }


    // ============================================================
    // CREATE LOAN
    // ============================================================

    @Test
    void createOrUpdate_shouldCreateLoanSuccessfully() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(loanMapper.toEntity(requestDTO))
                .thenReturn(loan);

        when(loanRepository.existsByLoanNumber(any(String.class)))
                .thenReturn(false);

        when(loanRepository.save(loan))
                .thenReturn(loan);

        when(loanMapper.toResponseDTO(loan))
                .thenReturn(responseDTO);


        LoanResponseDTO result =
                loanService.createOrUpdate(requestDTO);


        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "LN-ABC123456789",
                result.getLoanNumber()
        );

        assertEquals(
                LoanType.PERSONAL,
                result.getLoanType()
        );

        assertEquals(
                new BigDecimal("500000.00"),
                result.getPrincipalAmount()
        );


        assertEquals(
                customer,
                loan.getCustomer()
        );

        assertEquals(
                LoanStatus.ACTIVE,
                loan.getStatus()
        );

        assertEquals(
                new BigDecimal("500000.00"),
                loan.getOutstandingAmount()
        );

        assertEquals(
                LocalDate.of(2031, 9, 1),
                loan.getMaturityDate()
        );


        verify(customerRepository)
                .findById(1L);

        verify(loanMapper)
                .toEntity(requestDTO);

        verify(loanRepository)
                .existsByLoanNumber(any(String.class));

        verify(loanRepository)
                .save(loan);

        verify(loanMapper)
                .toResponseDTO(loan);
    }


    // ============================================================
    // CREATE - CUSTOMER NOT FOUND
    // ============================================================

    @Test
    void createOrUpdate_shouldThrowCustomerNotFoundException() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                CustomerNotFoundException.class,
                () -> loanService.createOrUpdate(requestDTO)
        );


        verify(customerRepository)
                .findById(1L);

        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @Test
    void getLoanById_shouldReturnLoan() {

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        when(loanMapper.toResponseDTO(loan))
                .thenReturn(responseDTO);


        LoanResponseDTO result =
                loanService.getLoanById(1L);


        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "LN-ABC123456789",
                result.getLoanNumber()
        );


        verify(loanRepository)
                .findById(1L);

        verify(loanMapper)
                .toResponseDTO(loan);
    }


    // ============================================================
    // GET BY ID - NOT FOUND
    // ============================================================

    @Test
    void getLoanById_shouldThrowLoanNotFoundException() {

        when(loanRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                LoanNotFoundException.class,
                () -> loanService.getLoanById(999L)
        );


        verify(loanRepository)
                .findById(999L);

        verify(loanMapper, never())
                .toResponseDTO(any(Loan.class));
    }


    // ============================================================
    // GET ALL
    // ============================================================

    @Test
    void getAllLoans_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Loan> loanPage =
                new PageImpl<>(
                        List.of(loan)
                );


        when(loanRepository.findAll(pageable))
                .thenReturn(loanPage);

        when(loanMapper.toResponseDTO(loan))
                .thenReturn(responseDTO);


        Page<LoanResponseDTO> result =
                loanService.getAllLoans(pageable);


        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "LN-ABC123456789",
                result.getContent()
                        .get(0)
                        .getLoanNumber()
        );


        verify(loanRepository)
                .findAll(pageable);

        verify(loanMapper)
                .toResponseDTO(loan);
    }


    // ============================================================
    // GET BY CUSTOMER
    // ============================================================

    @Test
    void getLoansByCustomer_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Loan> loanPage =
                new PageImpl<>(
                        List.of(loan)
                );


        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(loanRepository.findByCustomerId(
                1L,
                pageable
        )).thenReturn(loanPage);

        when(loanMapper.toResponseDTO(loan))
                .thenReturn(responseDTO);


        Page<LoanResponseDTO> result =
                loanService.getLoansByCustomer(
                        1L,
                        pageable
                );


        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );


        verify(customerRepository)
                .findById(1L);

        verify(loanRepository)
                .findByCustomerId(
                        1L,
                        pageable
                );

        verify(loanMapper)
                .toResponseDTO(loan);
    }


    // ============================================================
    // GET BY CUSTOMER - CUSTOMER NOT FOUND
    // ============================================================

    @Test
    void getLoansByCustomer_shouldThrowCustomerNotFoundException() {

        Pageable pageable =
                PageRequest.of(0, 10);


        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                CustomerNotFoundException.class,
                () -> loanService.getLoansByCustomer(
                        999L,
                        pageable
                )
        );


        verify(customerRepository)
                .findById(999L);

        verify(loanRepository, never())
                .findByCustomerId(
                        any(Long.class),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // GET BY STATUS
    // ============================================================

    @Test
    void getLoansByStatus_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Loan> loanPage =
                new PageImpl<>(
                        List.of(loan)
                );


        when(loanRepository.findByStatus(
                LoanStatus.ACTIVE,
                pageable
        )).thenReturn(loanPage);

        when(loanMapper.toResponseDTO(loan))
                .thenReturn(responseDTO);


        Page<LoanResponseDTO> result =
                loanService.getLoansByStatus(
                        LoanStatus.ACTIVE,
                        pageable
                );


        assertEquals(
                1,
                result.getTotalElements()
        );


        verify(loanRepository)
                .findByStatus(
                        LoanStatus.ACTIVE,
                        pageable
                );
    }


    // ============================================================
    // GET BY LOAN TYPE
    // ============================================================

    @Test
    void getLoansByType_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Loan> loanPage =
                new PageImpl<>(
                        List.of(loan)
                );


        when(loanRepository.findByLoanType(
                LoanType.PERSONAL,
                pageable
        )).thenReturn(loanPage);

        when(loanMapper.toResponseDTO(loan))
                .thenReturn(responseDTO);


        Page<LoanResponseDTO> result =
                loanService.getLoansByType(
                        LoanType.PERSONAL,
                        pageable
                );


        assertEquals(
                1,
                result.getTotalElements()
        );


        verify(loanRepository)
                .findByLoanType(
                        LoanType.PERSONAL,
                        pageable
                );
    }


    // ============================================================
    // UPDATE LOAN
    // ============================================================

    @Test
    void createOrUpdate_shouldUpdateLoanSuccessfully() {

        requestDTO.setId(1L);

        requestDTO.setLoanType(
                LoanType.HOME
        );

        requestDTO.setInterestRate(
                new BigDecimal("9.50")
        );

        requestDTO.setTenureMonths(48);


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));

        when(loanRepository.save(loan))
                .thenReturn(loan);

        when(loanMapper.toResponseDTO(loan))
                .thenReturn(responseDTO);


        LoanResponseDTO result =
                loanService.createOrUpdate(requestDTO);


        assertNotNull(result);

        assertEquals(
                LoanType.HOME,
                loan.getLoanType()
        );

        assertEquals(
                new BigDecimal("9.50"),
                loan.getInterestRate()
        );

        assertEquals(
                48,
                loan.getTenureMonths()
        );

        assertEquals(
                LocalDate.of(2030, 9, 1),
                loan.getMaturityDate()
        );


        verify(loanRepository)
                .findById(1L);

        verify(loanRepository)
                .save(loan);

        verify(loanMapper)
                .toResponseDTO(loan);

        verify(customerRepository, never())
                .findById(any(Long.class));

        verify(loanMapper, never())
                .toEntity(any(LoanRequestDTO.class));
    }


    // ============================================================
    // UPDATE - CLOSED LOAN
    // ============================================================

    @Test
    void createOrUpdate_closedLoan_shouldThrowBusinessException() {

        requestDTO.setId(1L);

        loan.setStatus(
                LoanStatus.CLOSED
        );


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.createOrUpdate(requestDTO)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // UPDATE - DEFAULTED LOAN
    // ============================================================

    @Test
    void createOrUpdate_defaultedLoan_shouldThrowBusinessException() {

        requestDTO.setId(1L);

        loan.setStatus(
                LoanStatus.DEFAULTED
        );


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.createOrUpdate(requestDTO)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // UPDATE - CANCELLED LOAN
    // ============================================================

    @Test
    void createOrUpdate_cancelledLoan_shouldThrowBusinessException() {

        requestDTO.setId(1L);

        loan.setStatus(
                LoanStatus.CANCELLED
        );


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.createOrUpdate(requestDTO)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // UPDATE - CUSTOMER CHANGE NOT ALLOWED
    // ============================================================

    @Test
    void createOrUpdate_customerChange_shouldThrowBusinessException() {

        requestDTO.setId(1L);
        requestDTO.setCustomerId(2L);


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.createOrUpdate(requestDTO)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // UPDATE - PRINCIPAL CHANGE NOT ALLOWED
    // ============================================================

    @Test
    void createOrUpdate_principalChange_shouldThrowBusinessException() {

        requestDTO.setId(1L);

        requestDTO.setPrincipalAmount(
                new BigDecimal("600000.00")
        );


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.createOrUpdate(requestDTO)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // UPDATE - START DATE CHANGE NOT ALLOWED
    // ============================================================

    @Test
    void createOrUpdate_startDateChange_shouldThrowBusinessException() {

        requestDTO.setId(1L);

        requestDTO.setStartDate(
                LocalDate.of(2026, 10, 1)
        );


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.createOrUpdate(requestDTO)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // CLOSE LOAN WITH OUTSTANDING BALANCE
    // ============================================================

    @Test
    void createOrUpdate_closeWithOutstandingAmount_shouldThrowException() {

        requestDTO.setId(1L);

        requestDTO.setStatus(
                LoanStatus.CLOSED
        );


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.createOrUpdate(requestDTO)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // DELETE / CANCEL
    // ============================================================

    @Test
    void deleteLoan_shouldCancelActiveLoan() {

        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        loanService.deleteLoan(1L);


        assertEquals(
                LoanStatus.CANCELLED,
                loan.getStatus()
        );


        verify(loanRepository)
                .findById(1L);

        verify(loanRepository)
                .save(loan);
    }


    // ============================================================
    // DELETE - ALREADY CANCELLED
    // ============================================================

    @Test
    void deleteLoan_alreadyCancelled_shouldThrowException() {

        loan.setStatus(
                LoanStatus.CANCELLED
        );


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.deleteLoan(1L)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // DELETE - CLOSED
    // ============================================================

    @Test
    void deleteLoan_closedLoan_shouldThrowException() {

        loan.setStatus(
                LoanStatus.CLOSED
        );


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.deleteLoan(1L)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // DELETE - DEFAULTED
    // ============================================================

    @Test
    void deleteLoan_defaultedLoan_shouldThrowException() {

        loan.setStatus(
                LoanStatus.DEFAULTED
        );


        when(loanRepository.findById(1L))
                .thenReturn(Optional.of(loan));


        assertThrows(
                LoanBusinessException.class,
                () -> loanService.deleteLoan(1L)
        );


        verify(loanRepository, never())
                .save(any(Loan.class));
    }


    // ============================================================
    // DELETE - NOT FOUND
    // ============================================================

    @Test
    void deleteLoan_notFound_shouldThrowLoanNotFoundException() {

        when(loanRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                LoanNotFoundException.class,
                () -> loanService.deleteLoan(999L)
        );


        verify(loanRepository)
                .findById(999L);

        verify(loanRepository, never())
                .save(any(Loan.class));
    }
}


