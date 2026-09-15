package com.example.finance.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.DynamicUpdate;

import com.example.finance.entity.enums.LoanStatus;
import com.example.finance.entity.enums.LoanType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicUpdate
public class Loan extends BaseEntity {

    /*
     * Loan ID is inherited from BaseEntity.
     *
     * DO NOT declare another id here.
     */


    // ============================================================
    // LOAN NUMBER
    // ============================================================

    @Column(
            name = "loan_number",
            nullable = false,
            unique = true,
            updatable = false,
            length = 30
    )
    private String loanNumber;


    // ============================================================
    // CUSTOMER
    // ============================================================

    /*
     * Many loans can belong to one customer.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_loan_customer"
            )
    )
    private Customer customer;


    // ============================================================
    // LOAN TYPE
    // ============================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "loan_type",
            nullable = false,
            length = 30
    )
    private LoanType loanType;


    // ============================================================
    // PRINCIPAL AMOUNT
    // ============================================================

    @Column(
            name = "principal_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal principalAmount;


    // ============================================================
    // INTEREST RATE
    // ============================================================

    @Column(
            name = "interest_rate",
            nullable = false,
            precision = 7,
            scale = 4
    )
    private BigDecimal interestRate;


    // ============================================================
    // TENURE
    // ============================================================

    @Column(
            name = "tenure_months",
            nullable = false
    )
    private Integer tenureMonths;


    // ============================================================
    // OUTSTANDING AMOUNT
    // ============================================================

    /*
     * This is controlled by loan/repayment transactions.
     *
     * It must NOT be directly changed by a normal update request.
     */
    @Column(
            name = "outstanding_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    @Builder.Default
    private BigDecimal outstandingAmount =
            BigDecimal.ZERO;


    // ============================================================
    // STATUS
    // ============================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private LoanStatus status =
            LoanStatus.ACTIVE;


    // ============================================================
    // START DATE
    // ============================================================

    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;


    // ============================================================
    // MATURITY DATE
    // ============================================================

    @Column(
            name = "maturity_date",
            nullable = false
    )
    private LocalDate maturityDate;
}

