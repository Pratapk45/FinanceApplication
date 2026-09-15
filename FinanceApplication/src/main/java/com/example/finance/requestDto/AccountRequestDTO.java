package com.example.finance.requestDto;

import com.example.finance.entity.AccountType;

import jakarta.validation.constraints.NotNull;
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
public class AccountRequestDTO {

    /*
     * CREATE:
     * id = null
     *
     * UPDATE:
     * id = existing account id
     */
    private Long id;

    /*
     * Customer to whom the account belongs.
     *
     * We accept customerId instead of the complete
     * Customer entity in the request.
     */
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    /*
     * Account type can be changed only if our
     * business rules allow it during update.
     */
    @NotNull(message = "Account type is required")
    private AccountType accountType;
}