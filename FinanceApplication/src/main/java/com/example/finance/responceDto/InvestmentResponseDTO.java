package com.example.finance.responceDto;

import java.math.BigDecimal;

import com.example.finance.entity.enums.InvestmentStatus;
import com.example.finance.entity.enums.InvestmentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentResponseDTO {

    private Long id;

    private String investmentReference;

    private InvestmentType investmentType;

    private BigDecimal investedAmount;

    private BigDecimal currentValue;

    private BigDecimal returnRate;

    private InvestmentStatus status;

    private String description;

    /*
     * Complete nested customer details.
     */
    private CustomerResponseDTO customer;
}