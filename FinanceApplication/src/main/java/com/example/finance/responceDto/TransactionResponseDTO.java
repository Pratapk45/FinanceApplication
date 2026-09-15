package com.example.finance.responceDto;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.finance.entity.enums.TransactionDirection;
import com.example.finance.entity.enums.TransactionStatus;
import com.example.finance.entity.enums.TransactionType;

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
public class TransactionResponseDTO {

    private Long id;

    private String transactionReference;

    /*
     * Account details.
     *
     * AccountResponseDTO should contain customer details.
     */
    private AccountResponseDTO account;

    private BigDecimal amount;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private TransactionType transactionType;

    private TransactionDirection direction;

    private TransactionStatus status;

    private String transferReference;

    private String description;

    private LocalDateTime createdAt;
}

