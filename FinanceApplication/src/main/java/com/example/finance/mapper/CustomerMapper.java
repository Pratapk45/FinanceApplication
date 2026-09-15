package com.example.finance.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.finance.entity.Customer;
import com.example.finance.requestDto.CustomerRequestDTO;
import com.example.finance.responceDto.CustomerResponseDTO;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    /*
     * Request DTO → Entity
     *
     * id, auditing fields and version are NOT mapped
     * because they are managed by JPA.
     */

    @Mapping(target = "accounts", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "investments", ignore = true)
    Customer toEntity(CustomerRequestDTO requestDTO);


    /*
     * Entity → Response DTO
     */
    CustomerResponseDTO toResponseDTO(Customer customer);
}
