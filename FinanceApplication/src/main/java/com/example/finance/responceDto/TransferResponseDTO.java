package com.example.finance.responceDto;


import java.math.BigDecimal;

import com.example.finance.entity.enums.TransactionStatus;

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
public class TransferResponseDTO {

    private String transferReference;

    private BigDecimal amount;

    private TransactionStatus status;

    private AccountResponseDTO sourceAccount;

    private AccountResponseDTO destinationAccount;

    private TransactionResponseDTO debitTransaction;

    private TransactionResponseDTO creditTransaction;
}


