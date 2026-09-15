package com.example.finance.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.finance.entity.enums.InvestmentType;
import com.example.finance.exception.GlobalExceptionHandler;
import com.example.finance.exception.InvestmentBusinessException;
import com.example.finance.exception.InvestmentNotFoundException;
import com.example.finance.requestDto.InvestmentRequestDTO;
import com.example.finance.responceDto.InvestmentResponseDTO;
import com.example.finance.service.InvestmentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
class InvestmentControllerTest {

    private MockMvc mockMvc;

    private InvestmentService investmentService;

    private ObjectMapper objectMapper;

    private InvestmentController investmentController;

    @BeforeEach
    void setUp() {

        investmentService = mock(InvestmentService.class);

        investmentController =
                new InvestmentController(investmentService);

        PageableHandlerMethodArgumentResolver pageableResolver =
                new PageableHandlerMethodArgumentResolver();

        mockMvc = MockMvcBuilders
                .standaloneSetup(investmentController)
                .setCustomArgumentResolvers(pageableResolver)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }


    // ============================================================
    // POST - CREATE INVESTMENT
    // ============================================================

    @Test
    void createInvestment_shouldReturn201()
            throws Exception {

        InvestmentRequestDTO request =
                new InvestmentRequestDTO();

        request.setCustomerId(1L);

        request.setInvestedAmount(
                new BigDecimal("10000.00")
        );

        request.setInvestmentType(
                InvestmentType.MUTUAL_FUND
        );

        InvestmentResponseDTO response =
                new InvestmentResponseDTO();

        when(investmentService.createOrUpdate(
                any(InvestmentRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/investments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(request)
                        )
        )
        .andExpect(status().isCreated());

        verify(investmentService)
                .createOrUpdate(
                        any(InvestmentRequestDTO.class)
                );
    }


    // ============================================================
    // POST - INVALID JSON
    // ============================================================

    @Test
    void createInvestment_withInvalidJson_shouldReturn400()
            throws Exception {

        String invalidJson =
                "{ \"customerId\": 1, \"investedAmount\": }";

        mockMvc.perform(
                post("/api/investments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
        )
        .andExpect(status().isBadRequest());
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @Test
    void getInvestmentById_shouldReturn200()
            throws Exception {

        InvestmentResponseDTO response =
                new InvestmentResponseDTO();

        when(investmentService.getById(1L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/investments/1")
        )
        .andExpect(status().isOk());

        verify(investmentService)
                .getById(1L);
    }


    // ============================================================
    // GET BY ID - NOT FOUND
    // ============================================================

    @Test
    void getInvestmentById_whenNotFound_shouldReturn404()
            throws Exception {

        when(investmentService.getById(99L))
                .thenThrow(
                        new InvestmentNotFoundException(
                                "Investment not found with id: 99"
                        )
                );

        mockMvc.perform(
                get("/api/investments/99")
        )
        .andExpect(status().isNotFound())
        .andExpect(
                jsonPath("$.status")
                        .value(404)
        )
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "Investment not found with id: 99"
                        )
        )
        .andExpect(
                jsonPath("$.path")
                        .value(
                                "/api/investments/99"
                        )
        );
    }


    // ============================================================
    // GET ALL INVESTMENTS
    // ============================================================

    @Test
    void getAllInvestments_shouldReturn200()
            throws Exception {

        Page<InvestmentResponseDTO> page =
                new PageImpl<>(
                        Collections.emptyList()
                );

        when(investmentService.searchInvestments(
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/investments")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,desc")
        )
        .andExpect(status().isOk());

        verify(investmentService)
                .searchInvestments(
                        eq(null),
                        eq(null),
                        eq(null),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // GET INVESTMENTS - CUSTOMER FILTER
    // ============================================================

    @Test
    void getInvestmentsByCustomer_shouldReturn200()
            throws Exception {

        Page<InvestmentResponseDTO> page =
                new PageImpl<>(
                        Collections.emptyList()
                );

        when(investmentService.searchInvestments(
                eq(1L),
                eq(null),
                eq(null),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/investments")
                        .param("customerId", "1")
                        .param("page", "0")
                        .param("size", "10")
        )
        .andExpect(status().isOk());

        verify(investmentService)
                .searchInvestments(
                        eq(1L),
                        eq(null),
                        eq(null),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // GET INVESTMENTS - STATUS FILTER
    // ============================================================

    @Test
    void getInvestmentsByStatus_shouldReturn200()
            throws Exception {

        Page<InvestmentResponseDTO> page =
                new PageImpl<>(
                        Collections.emptyList()
                );

        when(investmentService.searchInvestments(
                eq(null),
                any(),
                eq(null),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/investments")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10")
        )
        .andExpect(status().isOk());

        verify(investmentService)
                .searchInvestments(
                        eq(null),
                        any(),
                        eq(null),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // GET INVESTMENTS - TYPE FILTER
    // ============================================================

    @Test
    void getInvestmentsByType_shouldReturn200()
            throws Exception {

        Page<InvestmentResponseDTO> page =
                new PageImpl<>(
                        Collections.emptyList()
                );

        when(investmentService.searchInvestments(
                eq(null),
                eq(null),
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/investments")
                        .param(
                                "investmentType",
                                "MUTUAL_FUND"
                        )
                        .param("page", "0")
                        .param("size", "10")
        )
        .andExpect(status().isOk());

        verify(investmentService)
                .searchInvestments(
                        eq(null),
                        eq(null),
                        any(),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // GET INVESTMENTS - ALL FILTERS
    // ============================================================

    @Test
    void getInvestmentsWithAllFilters_shouldReturn200()
            throws Exception {

        Page<InvestmentResponseDTO> page =
                new PageImpl<>(
                        Collections.emptyList()
                );

        when(investmentService.searchInvestments(
                eq(1L),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/investments")
                        .param("customerId", "1")
                        .param("status", "ACTIVE")
                        .param(
                                "investmentType",
                                "MUTUAL_FUND"
                        )
                        .param("page", "0")
                        .param("size", "10")
                        .param(
                                "sort",
                                "investedAmount,desc"
                        )
        )
        .andExpect(status().isOk());

        verify(investmentService)
                .searchInvestments(
                        eq(1L),
                        any(),
                        any(),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // PAGINATION AND SORTING
    // ============================================================

    @Test
    void getInvestments_shouldApplyPaginationAndSorting()
            throws Exception {

        Page<InvestmentResponseDTO> page =
                new PageImpl<>(
                        Collections.emptyList()
                );

        when(investmentService.searchInvestments(
                any(),
                any(),
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/investments")
                        .param("page", "2")
                        .param("size", "5")
                        .param(
                                "sort",
                                "investedAmount,desc"
                        )
        )
        .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(investmentService)
                .searchInvestments(
                        eq(null),
                        eq(null),
                        eq(null),
                        pageableCaptor.capture()
                );

        Pageable pageable =
                pageableCaptor.getValue();

        assertEquals(
                2,
                pageable.getPageNumber()
        );

        assertEquals(
                5,
                pageable.getPageSize()
        );

        assertTrue(
                pageable.getSort()
                        .getOrderFor("investedAmount")
                        .isDescending()
        );
    }


    // ============================================================
    // GET ACTIVE INVESTMENTS BY CUSTOMER
    // ============================================================

    @Test
    void getActiveInvestmentsByCustomer_shouldReturn200()
            throws Exception {

        Page<InvestmentResponseDTO> page =
                new PageImpl<>(
                        Collections.emptyList()
                );

        when(investmentService.getActiveByCustomerId(
                eq(1L),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(
                get("/api/investments/customer/1/active")
                        .param("page", "0")
                        .param("size", "10")
        )
        .andExpect(status().isOk());

        verify(investmentService)
                .getActiveByCustomerId(
                        eq(1L),
                        any(Pageable.class)
                );
    }


    // ============================================================
    // PUT - UPDATE INVESTMENT
    // ============================================================

    @Test
    void updateInvestment_shouldReturn200()
            throws Exception {

        InvestmentRequestDTO request =
                new InvestmentRequestDTO();

        request.setCustomerId(1L);

        request.setInvestedAmount(
                new BigDecimal("10000.00")
        );

        request.setInvestmentType(
                InvestmentType.MUTUAL_FUND
        );

        InvestmentResponseDTO response =
                new InvestmentResponseDTO();

        when(investmentService.createOrUpdate(
                any(InvestmentRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(
                put("/api/investments/1")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(status().isOk());

        ArgumentCaptor<InvestmentRequestDTO> captor =
                ArgumentCaptor.forClass(
                        InvestmentRequestDTO.class
                );

        verify(investmentService)
                .createOrUpdate(
                        captor.capture()
                );

        assertEquals(
                1L,
                captor.getValue().getId()
        );
    }


    // ============================================================
    // PUT - INVESTMENT NOT FOUND
    // ============================================================

    @Test
    void updateInvestment_whenNotFound_shouldReturn404()
            throws Exception {

        InvestmentRequestDTO request =
                new InvestmentRequestDTO();

        request.setCustomerId(1L);

        request.setInvestedAmount(
                new BigDecimal("10000.00")
        );

        request.setInvestmentType(
                InvestmentType.MUTUAL_FUND
        );

        when(investmentService.createOrUpdate(
                any(InvestmentRequestDTO.class)))
                .thenThrow(
                        new InvestmentNotFoundException(
                                "Investment not found with id: 99"
                        )
                );

        mockMvc.perform(
                put("/api/investments/99")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        )
        )
        .andExpect(status().isNotFound())
        .andExpect(
                jsonPath("$.status")
                        .value(404)
        )
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "Investment not found with id: 99"
                        )
        );
    }


    // ============================================================
    // DELETE - CANCEL INVESTMENT
    // ============================================================

    @Test
    void deleteInvestment_shouldReturn204()
            throws Exception {

        doNothing()
                .when(investmentService)
                .delete(1L);

        mockMvc.perform(
                delete("/api/investments/1")
        )
        .andExpect(status().isNoContent());

        verify(investmentService)
                .delete(1L);
    }


    // ============================================================
    // DELETE - NOT FOUND
    // ============================================================

    @Test
    void deleteInvestment_whenNotFound_shouldReturn404()
            throws Exception {

        doThrow(
                new InvestmentNotFoundException(
                        "Investment not found with id: 99"
                )
        )
        .when(investmentService)
        .delete(99L);

        mockMvc.perform(
                delete("/api/investments/99")
        )
        .andExpect(status().isNotFound())
        .andExpect(
                jsonPath("$.status")
                        .value(404)
        )
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "Investment not found with id: 99"
                        )
        );
    }


    // ============================================================
    // DELETE - BUSINESS EXCEPTION
    // ============================================================

    @Test
    void deleteInvestment_whenBusinessRuleFails_shouldReturn400()
            throws Exception {

        doThrow(
                new InvestmentBusinessException(
                        "Closed investment cannot be cancelled"
                )
        )
        .when(investmentService)
        .delete(1L);

        mockMvc.perform(
                delete("/api/investments/1")
        )
        .andExpect(status().isBadRequest())
        .andExpect(
                jsonPath("$.status")
                        .value(400)
        )
        .andExpect(
                jsonPath("$.message")
                        .value(
                                "Closed investment cannot be cancelled"
                        )
        );
    }


    // ============================================================
    // INVALID PATH VARIABLE
    // ============================================================

    @Test
    void getInvestmentById_withInvalidId_shouldReturn400()
            throws Exception {

        mockMvc.perform(
                get("/api/investments/abc")
        )
        .andExpect(status().isBadRequest());
    }


    // ============================================================
    // INVALID ENUM
    // ============================================================

    @Test
    void getInvestments_withInvalidStatus_shouldReturn400()
            throws Exception {

        mockMvc.perform(
                get("/api/investments")
                        .param(
                                "status",
                                "INVALID_STATUS"
                        )
        )
        .andExpect(status().isBadRequest());
    }


    // ============================================================
    // WRONG ENDPOINT
    // ============================================================

    @Test
    void wrongEndpoint_shouldReturn404()
            throws Exception {

        mockMvc.perform(
                get("/api/investmentsss")
        )
        .andExpect(status().isNotFound());
    }
}