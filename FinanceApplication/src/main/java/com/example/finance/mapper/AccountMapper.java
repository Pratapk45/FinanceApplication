package com.example.finance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.finance.entity.Account;
import com.example.finance.requestDto.AccountRequestDTO;
import com.example.finance.responceDto.AccountResponseDTO;

@Mapper(componentModel = "spring")
public interface AccountMapper {

	/*
	 * Request DTO → Account Entity
	 *
	 * The following fields are controlled by the application/JPA:
	 *
	 * id accountNumber customer balance status version auditing fields
	 *
	 * Therefore MapStruct must NOT map them.
	 */
	@Mapping(target = "accountNumber", ignore = true)
	@Mapping(target = "customer", ignore = true)
	@Mapping(target = "balance", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "transactions", ignore = true)
	Account toEntity(AccountRequestDTO requestDTO);

	/*
	 * Update existing Account entity.
	 *
	 * IMPORTANT: We use @MappingTarget because during UPDATE we must modify the
	 * existing managed entity.
	 *
	 * We do NOT create a new Account object.
	 */
	@Mapping(target = "id", ignore = true)
	@Mapping(target = "accountNumber", ignore = true)
	@Mapping(target = "customer", ignore = true)
	@Mapping(target = "balance", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "version", ignore = true)
	@Mapping(target = "transactions", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "createdBy", ignore = true)
	@Mapping(target = "updatedBy", ignore = true)
	void updateEntity(AccountRequestDTO requestDTO, @MappingTarget Account account);

	/*
	 * Account Entity → Response DTO
	 *
	 * Account.customer.id → customerId
	 */
	@Mapping(target = "customerId", source = "customer.id")
	AccountResponseDTO toResponseDTO(Account account);
}
