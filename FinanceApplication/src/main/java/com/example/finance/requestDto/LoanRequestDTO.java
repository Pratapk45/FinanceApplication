package com.example.finance.requestDto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.finance.entity.enums.LoanStatus;
import com.example.finance.entity.enums.LoanType;
import com.example.finance.validation.OnCreate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
public class LoanRequestDTO {

    /*
     * NULL  -> CREATE
     * VALUE -> UPDATE
     */
    private Long id;


    /*
     * Required during CREATE.
     *
     * During UPDATE the existing customer is retained.
     */
    @NotNull(
            message = "Customer ID is required",
            groups = OnCreate.class
    )
    @Positive(
            message = "Customer ID must be greater than zero",
            groups = OnCreate.class
    )
    private Long customerId;


    /*
     * Required during CREATE.
     *
     * Optional during UPDATE.
     */
    @NotNull(
            message = "Loan type is required",
            groups = OnCreate.class
    )
    private LoanType loanType;


    /*
     * Required during CREATE.
     *
     * Cannot be changed during UPDATE.
     */
    @NotNull(
            message = "Principal amount is required",
            groups = OnCreate.class
    )
    @Positive(
            message = "Principal amount must be greater than zero",
            groups = OnCreate.class
    )
    @Digits(
            integer = 17,
            fraction = 2,
            message = "Principal amount must have maximum 17 integer digits and 2 decimal places"
    )
    private BigDecimal principalAmount;


    /*
     * Required during CREATE.
     *
     * Optional during UPDATE.
     */
    @NotNull(
            message = "Interest rate is required",
            groups = OnCreate.class
    )
    @PositiveOrZero(
            message = "Interest rate cannot be negative"
    )
    @DecimalMax(
            value = "100.0000",
            message = "Interest rate cannot exceed 100%"
    )
    @Digits(
            integer = 3,
            fraction = 4,
            message = "Interest rate must have maximum 3 integer digits and 4 decimal places"
    )
    private BigDecimal interestRate;


    /*
     * Required during CREATE.
     *
     * Optional during UPDATE.
     */
    @NotNull(
            message = "Tenure in months is required",
            groups = OnCreate.class
    )
    @Positive(
            message = "Tenure must be greater than zero"
    )
    private Integer tenureMonths;


    /*
     * Required during CREATE.
     *
     * Cannot be changed during UPDATE.
     */
    @NotNull(
            message = "Start date is required",
            groups = OnCreate.class
    )
    @FutureOrPresent(
            message = "Start date cannot be in the past",
            groups = OnCreate.class
    )
    private LocalDate startDate;


    /*
     * Used only during UPDATE.
     */
    private LoanStatus status;
}


