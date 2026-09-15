package com.example.finance.entity;

import java.math.BigDecimal;

import com.example.finance.entity.enums.InvestmentStatus;
import com.example.finance.entity.enums.InvestmentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "investments")
@NamedQuery(
        name = "Investment.findActiveByCustomerId",
        query = """
                SELECT i
                FROM Investment i
                WHERE i.customer.id = :customerId
                  AND i.status =InvestmentStatus.ACTIVE
                ORDER BY i.id DESC
                """
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Investment extends BaseEntity {

    /*
     * Application-generated investment reference.
     *
     * Example:
     * INV-A12B34C56D78
     */
    @Column(
            name = "investment_reference",
            nullable = false,
            unique = true,
            updatable = false,
            length = 30
    )
    private String investmentReference;


    /*
     * Customer who owns the investment.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "customer_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_investment_customer"
            )
    )
    private Customer customer;


    /*
     * Investment type.
     *
     * Example:
     * MUTUAL_FUND
     * FIXED_DEPOSIT
     * STOCK
     * BOND
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "investment_type",
            nullable = false,
            length = 30
    )
    private InvestmentType investmentType;


    /*
     * Original amount invested by customer.
     */
    @Column(
            name = "invested_amount",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal investedAmount;


    /*
     * Current market/value of investment.
     *
     * Initially equal to investedAmount.
     */
    @Column(
            name = "current_value",
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal currentValue;


    /*
     * Expected/recorded return rate.
     */
    @Column(
            name = "return_rate",
            precision = 5,
            scale = 2
    )
    private BigDecimal returnRate;


    /*
     * Investment lifecycle status.
     *
     * ACTIVE
     * CANCELLED
     * CLOSED
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private InvestmentStatus status = InvestmentStatus.ACTIVE;


    /*
     * Optional business description.
     */
    @Column(
            name = "description",
            length = 255
    )
    private String description;
}