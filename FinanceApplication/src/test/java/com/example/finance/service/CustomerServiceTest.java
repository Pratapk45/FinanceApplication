package com.example.finance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.finance.exception.CustomerNotFoundException;
import com.example.finance.exception.DuplicateCustomerException;
import com.example.finance.mapper.CustomerMapper;
import com.example.finance.repository.CustomerRepository;
import com.example.finance.requestDto.CustomerRequestDTO;
import com.example.finance.responceDto.CustomerResponseDTO;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    private CustomerRequestDTO requestDTO;

    private CustomerResponseDTO responseDTO;


    @BeforeEach
    void setUp() {

        customer = new Customer();

        customer.setId(1L);
        customer.setFirstName("Pratap");
        customer.setLastName("Kachare");
        customer.setEmail("pratap@example.com");
        customer.setPhone("9876543210");

        requestDTO = CustomerRequestDTO.builder()
                .firstName("Pratap")
                .lastName("Kachare")
                .email("pratap@example.com")
                .phone("9876543210")
                .build();

        responseDTO = CustomerResponseDTO.builder()
                .id(1L)
                .firstName("Pratap")
                .lastName("Kachare")
                .email("pratap@example.com")
                .phone("9876543210")
                .build();
    }


    // ============================================================
    // CREATE CUSTOMER
    // ============================================================

    @Test
    void createCustomer_shouldCreateSuccessfully() {

        when(customerRepository.findByEmail(
                requestDTO.getEmail()))
                .thenReturn(Optional.empty());

        when(customerMapper.toEntity(requestDTO))
                .thenReturn(customer);

        when(customerRepository.save(customer))
                .thenReturn(customer);

        when(customerMapper.toResponseDTO(customer))
                .thenReturn(responseDTO);


        CustomerResponseDTO result =
                customerService.createCustomer(requestDTO);


        assertEquals(1L, result.getId());
        assertEquals(
                "Pratap",
                result.getFirstName()
        );
        assertEquals(
                "pratap@example.com",
                result.getEmail()
        );


        verify(customerRepository)
                .findByEmail(requestDTO.getEmail());

        verify(customerMapper)
                .toEntity(requestDTO);

        verify(customerRepository)
                .save(customer);

        verify(customerMapper)
                .toResponseDTO(customer);
    }


    @Test
    void createCustomer_shouldThrowDuplicateCustomerException() {

        when(customerRepository.findByEmail(
                requestDTO.getEmail()))
                .thenReturn(Optional.of(customer));


        assertThrows(
                DuplicateCustomerException.class,
                () -> customerService.createCustomer(requestDTO)
        );


        verify(customerRepository)
                .findByEmail(requestDTO.getEmail());

        verify(customerRepository, never())
                .save(any(Customer.class));

        verify(customerMapper, never())
                .toEntity(any(CustomerRequestDTO.class));
    }


    // ============================================================
    // GET CUSTOMER BY ID
    // ============================================================

    @Test
    void getCustomerById_shouldReturnCustomer() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(customerMapper.toResponseDTO(customer))
                .thenReturn(responseDTO);


        CustomerResponseDTO result =
                customerService.getCustomerById(1L);


        assertEquals(1L, result.getId());

        assertEquals(
                "Pratap",
                result.getFirstName()
        );

        assertEquals(
                "pratap@example.com",
                result.getEmail()
        );


        verify(customerRepository)
                .findById(1L);

        verify(customerMapper)
                .toResponseDTO(customer);
    }


    @Test
    void getCustomerById_shouldThrowCustomerNotFoundException() {

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.getCustomerById(999L)
        );


        verify(customerRepository)
                .findById(999L);

        verify(customerMapper, never())
                .toResponseDTO(any(Customer.class));
    }


    // ============================================================
    // GET ALL CUSTOMERS
    // ============================================================

    @Test
    void getAllCustomers_shouldReturnPage() {

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Customer> customerPage =
                new PageImpl<>(
                        java.util.List.of(customer)
                );

        when(customerRepository.findAll(pageable))
                .thenReturn(customerPage);

        when(customerMapper.toResponseDTO(customer))
                .thenReturn(responseDTO);


        Page<CustomerResponseDTO> result =
                customerService.getAllCustomers(pageable);


        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "Pratap",
                result.getContent()
                        .get(0)
                        .getFirstName()
        );


        verify(customerRepository)
                .findAll(pageable);

        verify(customerMapper)
                .toResponseDTO(customer);
    }


    // ============================================================
    // SEARCH CUSTOMERS
    // ============================================================

    @Test
    void searchCustomers_shouldReturnMatchingCustomers() {

        Pageable pageable =
                PageRequest.of(0, 10);

        String keyword = "Pratap";

        Page<Customer> customerPage =
                new PageImpl<>(
                        java.util.List.of(customer)
                );

        when(customerRepository.searchCustomers(
                eq(keyword),
                eq(pageable)))
                .thenReturn(customerPage);

        when(customerMapper.toResponseDTO(customer))
                .thenReturn(responseDTO);


        Page<CustomerResponseDTO> result =
                customerService.searchCustomers(
                        keyword,
                        pageable
                );


        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "Pratap",
                result.getContent()
                        .get(0)
                        .getFirstName()
        );


        verify(customerRepository)
                .searchCustomers(
                        keyword,
                        pageable
                );

        verify(customerMapper)
                .toResponseDTO(customer);
    }


    // ============================================================
    // UPDATE CUSTOMER
    // ============================================================

    @Test
    void updateCustomer_shouldUpdateSuccessfully() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(customerRepository.save(customer))
                .thenReturn(customer);

        when(customerMapper.toResponseDTO(customer))
                .thenReturn(responseDTO);


        CustomerResponseDTO result =
                customerService.updateCustomer(
                        1L,
                        requestDTO
                );


        assertEquals(
                1L,
                result.getId()
        );

        assertEquals(
                "Pratap",
                result.getFirstName()
        );


        verify(customerRepository)
                .findById(1L);

        verify(customerRepository)
                .save(customer);

        verify(customerMapper)
                .toResponseDTO(customer);
    }


    @Test
    void updateCustomer_shouldThrowCustomerNotFoundException() {

        when(customerRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.updateCustomer(
                        999L,
                        requestDTO
                )
        );


        verify(customerRepository)
                .findById(999L);

        verify(customerRepository, never())
                .save(any(Customer.class));
    }


    // ============================================================
    // DELETE CUSTOMER
    // ============================================================

    @Test
    void deleteCustomer_shouldDeleteSuccessfully() {

        when(customerRepository.existsById(1L))
                .thenReturn(true);


        customerService.deleteCustomer(1L);


        verify(customerRepository)
                .existsById(1L);

        verify(customerRepository)
                .deleteById(1L);
    }


    @Test
    void deleteCustomer_shouldThrowCustomerNotFoundException() {

        when(customerRepository.existsById(999L))
                .thenReturn(false);


        assertThrows(
                CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(999L)
        );


        verify(customerRepository)
                .existsById(999L);

        verify(customerRepository, never())
                .deleteById(999L);
    }
}
