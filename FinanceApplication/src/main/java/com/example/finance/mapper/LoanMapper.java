package com.example.finance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.finance.entity.Loan;
import com.example.finance.requestDto.LoanRequestDTO;
import com.example.finance.responceDto.LoanResponseDTO;

@Mapper(componentModel = "spring", uses = CustomerMapper.class)
public interface LoanMapper {

	/*
	 * ============================================================
	 *  REQUEST DTO ->ENTITY
	 * ============================================================
	 *
	 * These fields are controlled by the service/database.
	 */
	@Mapping(target = "customer", ignore = true)
	@Mapping(target = "loanNumber", ignore = true)
	@Mapping(target = "outstandingAmount", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "maturityDate", ignore = true)
	Loan toEntity(LoanRequestDTO request);

	/*
	 * ============================================================
	 *  ENTITY -> RESPONSE DTO
	 * ============================================================
	 *
	 * CustomerMapper automatically converts:
	 *
	 * Customer -> CustomerResponseDTO
	 */
	LoanResponseDTO toResponseDTO(Loan loan);
}
