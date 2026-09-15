package com.example.finance.requestDto;

import java.math.BigDecimal;

import com.example.finance.entity.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class TransactionRequestDTO {

    @NotNull(message = "Account ID is required")
    private Long accountId;


    @NotNull(message = "Transaction amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Transaction amount must be greater than zero"
    )
    private BigDecimal amount;


    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;


    /*
     * Required only for TRANSFER.
     */
    private Long destinationAccountId;


    @Size(
            max = 255,
            message = "Description cannot exceed 255 characters"
    )
    private String description;
}
