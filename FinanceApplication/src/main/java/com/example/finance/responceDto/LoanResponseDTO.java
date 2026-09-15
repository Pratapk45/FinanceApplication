package com.example.finance.responceDto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.finance.entity.enums.LoanStatus;
import com.example.finance.entity.enums.LoanType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanResponseDTO {

    private Long id;

    private String loanNumber;

    /*
     * Complete customer information.
     */
    private CustomerResponseDTO customer;

    private LoanType loanType;

    private BigDecimal principalAmount;

    private BigDecimal interestRate;

    private Integer tenureMonths;

    private BigDecimal outstandingAmount;

    private LoanStatus status;

    private LocalDate startDate;

    private LocalDate maturityDate;
}

