package com.example.finance.requestDto;

import java.math.BigDecimal;

import com.example.finance.entity.enums.InvestmentType;
import com.example.finance.validation.OnCreate;
import com.example.finance.validation.OnUpdate;

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
public class InvestmentRequestDTO {

	/*
	 * Null for CREATE.
	 *
	 * Existing ID for UPDATE.
	 */
	private Long id;

	/*
	 * Customer is mandatory when creating.
	 */
	@NotNull(message = "Customer ID is required", groups = OnCreate.class)
	private Long customerId;

	/*
	 * Investment type is mandatory when creating.
	 */
	@NotNull(message = "Investment type is required", groups = OnCreate.class)
	private InvestmentType investmentType;

	/*
	 * Original investment amount.
	 *
	 * It is mandatory during creation. Existing investment amount cannot be changed
	 * during update.
	 */
	@NotNull(message = "Invested amount is required", groups = OnCreate.class)
	@DecimalMin(value = "0.01", message = "Invested amount must be greater than zero", groups = OnCreate.class)
	private BigDecimal investedAmount;

	/*
	 * Return rate is optional.
	 *
	 * If supplied, it cannot be negative.
	 */
	@DecimalMin(value = "0.00", message = "Return rate cannot be negative", groups = { OnCreate.class, OnUpdate.class })
	private BigDecimal returnRate;

	/*
	 * Optional description.
	 */
	@Size(max = 255, message = "Description cannot exceed 255 characters", groups = { OnCreate.class, OnUpdate.class })
	private String description;
}