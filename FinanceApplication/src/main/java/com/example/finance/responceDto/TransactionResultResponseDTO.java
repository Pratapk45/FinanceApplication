package com.example.finance.responceDto;

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
public class TransactionResultResponseDTO {

    private TransactionType transactionType;

    /*
     * Used by DEPOSIT and WITHDRAWAL.
     */
    private TransactionResponseDTO transaction;

    /*
     * Used by TRANSFER.
     */
    private TransferResponseDTO transfer;
}


