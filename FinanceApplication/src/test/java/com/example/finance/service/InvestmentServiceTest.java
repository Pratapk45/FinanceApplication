package com.example.finance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

import com.example.finance.entity.Customer;
import com.example.finance.entity.Investment;
import com.example.finance.entity.enums.InvestmentStatus;
import com.example.finance.entity.enums.InvestmentType;
import com.example.finance.exception.CustomerNotFoundException;
import com.example.finance.exception.InvestmentBusinessException;
import com.example.finance.exception.InvestmentNotFoundException;
import com.example.finance.mapper.InvestmentMapper;
import com.example.finance.repository.CustomerRepository;
import com.example.finance.repository.InvestmentRepository;
import com.example.finance.requestDto.InvestmentRequestDTO;
import com.example.finance.responceDto.InvestmentResponseDTO;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private InvestmentMapper investmentMapper;

    @InjectMocks
    private InvestmentService investmentService;

    private Customer customer;

    private Investment investment;

    private InvestmentRequestDTO requestDTO;

    private InvestmentResponseDTO responseDTO;


    @BeforeEach
    void setUp() {

        customer = new Customer();

        customer.setId(1L);
        customer.setFirstName("Pratap");
        customer.setLastName("Kachare");
        customer.setEmail("pratap@example.com");


        investment = new Investment();

        investment.setId(1L);
        investment.setInvestmentReference(
                "INV-A12B34C56D78"
        );
        investment.setCustomer(customer);
        investment.setInvestmentType(
                InvestmentType.MUTUAL_FUND
        );
        investment.setInvestedAmount(
                new BigDecimal("100000.00")
        );
        investment.setCurrentValue(
                new BigDecimal("100000.00")
        );
        investment.setReturnRate(
                new BigDecimal("12.50")
        );
        investment.setStatus(
                InvestmentStatus.ACTIVE
        );
        investment.setDescription(
                "Long term investment"
        );


        requestDTO = InvestmentRequestDTO.builder()
                .customerId(1L)
                .investmentType(
                        InvestmentType.MUTUAL_FUND
                )
                .investedAmount(
                        new BigDecimal("100000.00")
                )
                .returnRate(
                        new BigDecimal("12.50")
                )
                .description(
                        "Long term investment"
                )
                .build();


        responseDTO = InvestmentResponseDTO.builder()
                .id(1L)
                .investmentReference(
                        "INV-A12B34C56D78"
                )
                .investmentType(
                        InvestmentType.MUTUAL_FUND
                )
                .investedAmount(
                        new BigDecimal("100000.00")
                )
                .currentValue(
                        new BigDecimal("100000.00")
                )
                .returnRate(
                        new BigDecimal("12.50")
                )
                .status(
                        InvestmentStatus.ACTIVE
                )
                .description(
                        "Long term investment"
                )
                .build();
    }


    // ============================================================
    // CREATE
    // ============================================================

    @Test
    void createOrUpdate_shouldCreateInvestmentSuccessfully() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(investmentMapper.toEntity(requestDTO))
                .thenReturn(investment);

        when(investmentRepository.save(investment))
                .thenReturn(investment);

        when(investmentMapper.toResponseDTO(investment))
                .thenReturn(responseDTO);


        InvestmentResponseDTO result =
                investmentService.createOrUpdate(
                        requestDTO
                );


        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "INV-A12B34C56D78",
                result.getInvestmentReference()
        );

        assertEquals(
                InvestmentType.MUTUAL_FUND,
                result.getInvestmentType()
        );

        assertEquals(
                new BigDecimal("100000.00"),
                result.getInvestedAmount()
        );


        assertEquals(
                customer,
                investment.getCustomer()
        );

        assertEquals(
                new BigDecimal("100000.00"),
                investment.getCurrentValue()
        );

        assertEquals(
                InvestmentStatus.ACTIVE,
                investment.getStatus()
        );


        verify(customerRepository)
                .findById(1L);

        verify(investmentMapper)
                .toEntity(requestDTO);

        verify(investmentRepository)
                .save(investment);

        verify(investmentMapper)
                .toResponseDTO(investment);
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
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(customerRepository)
                .findById(1L);

        verify(investmentRepository, never())
                .save(any(Investment.class));

        verify(investmentMapper, never())
                .toEntity(any(InvestmentRequestDTO.class));
    }


    // ============================================================
    // CREATE - INVALID INVESTED AMOUNT
    // ============================================================

    @Test
    void createOrUpdate_zeroInvestedAmount_shouldThrowException() {

        requestDTO.setInvestedAmount(
                BigDecimal.ZERO
        );


        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    @Test
    void createOrUpdate_negativeInvestedAmount_shouldThrowException() {

        requestDTO.setInvestedAmount(
                new BigDecimal("-1000.00")
        );


        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // CREATE - NEGATIVE RETURN RATE
    // ============================================================

    @Test
    void createOrUpdate_negativeReturnRate_shouldThrowException() {

        requestDTO.setReturnRate(
                new BigDecimal("-5.00")
        );


        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @Test
    void getById_shouldReturnInvestment() {

        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));

        when(investmentMapper.toResponseDTO(investment))
                .thenReturn(responseDTO);


        InvestmentResponseDTO result =
                investmentService.getById(1L);


        assertNotNull(result);

        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "INV-A12B34C56D78",
                result.getInvestmentReference()
        );


        verify(investmentRepository)
                .findById(1L);

        verify(investmentMapper)
                .toResponseDTO(investment);
    }


    // ============================================================
    // GET BY ID - NOT FOUND
    // ============================================================

    @Test
    void getById_shouldThrowInvestmentNotFoundException() {

        when(investmentRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                InvestmentNotFoundException.class,
                () -> investmentService.getById(999L)
        );


        verify(investmentRepository)
                .findById(999L);

        verify(investmentMapper, never())
                .toResponseDTO(any(Investment.class));
    }


    // ============================================================
    // SEARCH - ALL FILTERS
    // ============================================================

    @Test
    void searchInvestments_shouldReturnFilteredPage() {

        Pageable pageable =
                PageRequest.of(0, 10);


        Page<Investment> investmentPage =
                new PageImpl<>(
                        List.of(investment)
                );


        when(customerRepository.existsById(1L))
                .thenReturn(true);

        when(investmentRepository.searchInvestments(
                1L,
                InvestmentStatus.ACTIVE,
                InvestmentType.MUTUAL_FUND,
                pageable
        )).thenReturn(investmentPage);

        when(investmentMapper.toResponseDTO(investment))
                .thenReturn(responseDTO);


        Page<InvestmentResponseDTO> result =
                investmentService.searchInvestments(
                        1L,
                        InvestmentStatus.ACTIVE,
                        InvestmentType.MUTUAL_FUND,
                        pageable
                );


        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "INV-A12B34C56D78",
                result.getContent()
                        .get(0)
                        .getInvestmentReference()
        );


        verify(customerRepository)
                .existsById(1L);

        verify(investmentRepository)
                .searchInvestments(
                        1L,
                        InvestmentStatus.ACTIVE,
                        InvestmentType.MUTUAL_FUND,
                        pageable
                );

        verify(investmentMapper)
                .toResponseDTO(investment);
    }


    // ============================================================
    // SEARCH - NO FILTERS
    // ============================================================

    @Test
    void searchInvestments_withoutFilters_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);


        Page<Investment> investmentPage =
                new PageImpl<>(
                        List.of(investment)
                );


        when(investmentRepository.searchInvestments(
                null,
                null,
                null,
                pageable
        )).thenReturn(investmentPage);

        when(investmentMapper.toResponseDTO(investment))
                .thenReturn(responseDTO);


        Page<InvestmentResponseDTO> result =
                investmentService.searchInvestments(
                        null,
                        null,
                        null,
                        pageable
                );


        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );


        verify(customerRepository, never())
                .existsById(any(Long.class));

        verify(investmentRepository)
                .searchInvestments(
                        null,
                        null,
                        null,
                        pageable
                );
    }


    // ============================================================
    // SEARCH - CUSTOMER NOT FOUND
    // ============================================================

    @Test
    void searchInvestments_customerNotFound_shouldThrowException() {

        Pageable pageable =
                PageRequest.of(0, 10);


        when(customerRepository.existsById(999L))
                .thenReturn(false);


        assertThrows(
                CustomerNotFoundException.class,
                () -> investmentService.searchInvestments(
                        999L,
                        null,
                        null,
                        pageable
                )
        );


        verify(customerRepository)
                .existsById(999L);

        verify(investmentRepository, never())
                .searchInvestments(
                        any(),
                        any(),
                        any(),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // ACTIVE INVESTMENTS BY CUSTOMER
    // ============================================================

    @Test
    void getActiveByCustomerId_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);


        Page<Investment> investmentPage =
                new PageImpl<>(
                        List.of(investment)
                );


        when(customerRepository.existsById(1L))
                .thenReturn(true);

        when(investmentRepository.findActiveByCustomerId(
                1L,
                pageable
        )).thenReturn(investmentPage);

        when(investmentMapper.toResponseDTO(investment))
                .thenReturn(responseDTO);


        Page<InvestmentResponseDTO> result =
                investmentService.getActiveByCustomerId(
                        1L,
                        pageable
                );


        assertNotNull(result);

        assertEquals(
                1,
                result.getTotalElements()
        );


        verify(customerRepository)
                .existsById(1L);

        verify(investmentRepository)
                .findActiveByCustomerId(
                        1L,
                        pageable
                );

        verify(investmentMapper)
                .toResponseDTO(investment);
    }


    // ============================================================
    // ACTIVE INVESTMENTS - CUSTOMER NOT FOUND
    // ============================================================

    @Test
    void getActiveByCustomerId_customerNotFound_shouldThrowException() {

        Pageable pageable =
                PageRequest.of(0, 10);


        when(customerRepository.existsById(999L))
                .thenReturn(false);


        assertThrows(
                CustomerNotFoundException.class,
                () -> investmentService.getActiveByCustomerId(
                        999L,
                        pageable
                )
        );


        verify(customerRepository)
                .existsById(999L);

        verify(investmentRepository, never())
                .findActiveByCustomerId(
                        any(Long.class),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // UPDATE
    // ============================================================

    @Test
    void createOrUpdate_shouldUpdateInvestmentSuccessfully() {

        requestDTO.setId(1L);

        requestDTO.setInvestmentType(
                InvestmentType.STOCK
        );

        requestDTO.setReturnRate(
                new BigDecimal("15.00")
        );

        requestDTO.setDescription(
                "Updated investment"
        );


        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));

        when(investmentRepository.save(investment))
                .thenReturn(investment);

        when(investmentMapper.toResponseDTO(investment))
                .thenReturn(responseDTO);


        InvestmentResponseDTO result =
                investmentService.createOrUpdate(
                        requestDTO
                );


        assertNotNull(result);

        assertEquals(
                InvestmentType.STOCK,
                investment.getInvestmentType()
        );

        assertEquals(
                new BigDecimal("15.00"),
                investment.getReturnRate()
        );

        assertEquals(
                "Updated investment",
                investment.getDescription()
        );


        verify(investmentRepository)
                .findById(1L);

        verify(investmentRepository)
                .save(investment);

        verify(investmentMapper)
                .toResponseDTO(investment);

        verify(investmentMapper, never())
                .toEntity(any(InvestmentRequestDTO.class));
    }


    // ============================================================
    // UPDATE - NOT FOUND
    // ============================================================

    @Test
    void createOrUpdate_updateNotFound_shouldThrowException() {

        requestDTO.setId(999L);


        when(investmentRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                InvestmentNotFoundException.class,
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(investmentRepository)
                .findById(999L);

        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // UPDATE - CLOSED
    // ============================================================

    @Test
    void createOrUpdate_closedInvestment_shouldThrowException() {

        requestDTO.setId(1L);

        investment.setStatus(
                InvestmentStatus.CLOSED
        );


        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // UPDATE - CANCELLED
    // ============================================================

    @Test
    void createOrUpdate_cancelledInvestment_shouldThrowException() {

        requestDTO.setId(1L);

        investment.setStatus(
                InvestmentStatus.CANCELLED
        );


        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // UPDATE - CUSTOMER CHANGE
    // ============================================================

    @Test
    void createOrUpdate_customerChange_shouldThrowException() {

        requestDTO.setId(1L);

        requestDTO.setCustomerId(999L);


        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // UPDATE - INVESTED AMOUNT CHANGE
    // ============================================================

    @Test
    void createOrUpdate_investedAmountChange_shouldThrowException() {

        requestDTO.setId(1L);

        requestDTO.setInvestedAmount(
                new BigDecimal("150000.00")
        );


        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // UPDATE - NEGATIVE RETURN RATE
    // ============================================================

    @Test
    void createOrUpdate_updatedReturnRateNegative_shouldThrowException() {

        requestDTO.setId(1L);

        requestDTO.setReturnRate(
                new BigDecimal("-1.00")
        );


        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.createOrUpdate(
                        requestDTO
                )
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // DELETE / CANCEL
    // ============================================================

    @Test
    void delete_shouldCancelInvestment() {

        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));


        investmentService.delete(1L);


        assertEquals(
                InvestmentStatus.CANCELLED,
                investment.getStatus()
        );


        verify(investmentRepository)
                .findById(1L);

        verify(investmentRepository)
                .save(investment);
    }


    // ============================================================
    // DELETE - ALREADY CANCELLED
    // ============================================================

    @Test
    void delete_alreadyCancelled_shouldThrowException() {

        investment.setStatus(
                InvestmentStatus.CANCELLED
        );


        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.delete(1L)
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // DELETE - CLOSED
    // ============================================================

    @Test
    void delete_closedInvestment_shouldThrowException() {

        investment.setStatus(
                InvestmentStatus.CLOSED
        );


        when(investmentRepository.findById(1L))
                .thenReturn(Optional.of(investment));


        assertThrows(
                InvestmentBusinessException.class,
                () -> investmentService.delete(1L)
        );


        verify(investmentRepository, never())
                .save(any(Investment.class));
    }


    // ============================================================
    // DELETE - NOT FOUND
    // ============================================================

    @Test
    void delete_notFound_shouldThrowInvestmentNotFoundException() {

        when(investmentRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                InvestmentNotFoundException.class,
                () -> investmentService.delete(999L)
        );


        verify(investmentRepository)
                .findById(999L);

        verify(investmentRepository, never())
                .save(any(Investment.class));
    }
}
