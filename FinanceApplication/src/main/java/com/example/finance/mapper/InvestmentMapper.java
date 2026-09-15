package com.example.finance.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.finance.entity.Investment;
import com.example.finance.requestDto.InvestmentRequestDTO;
import com.example.finance.responceDto.InvestmentResponseDTO;


@Mapper(componentModel = "spring")
public interface InvestmentMapper {

    /*
     * ============================================================
     * REQUEST DTO -> ENTITY
     * ============================================================
     *
     * ID and system-managed fields are not mapped from request.
     *
     * Customer is assigned by InvestmentService.
     *
     * investmentReference, currentValue and status are also
     * controlled by the service.
     */
	
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "investmentReference", ignore = true)
    @Mapping(target = "currentValue", ignore = true)
    @Mapping(target = "status", ignore = true)

    @Mapping(target = "investmentType", source = "investmentType")
    @Mapping(target = "investedAmount", source = "investedAmount")
    @Mapping(target = "returnRate", source = "returnRate")
    @Mapping(target = "description", source = "description")
    Investment toEntity(InvestmentRequestDTO requestDTO);


    /*
     * ============================================================
     * ENTITY -> RESPONSE DTO
     * ============================================================
     *
     * MapStruct automatically maps the nested Customer entity
     * to CustomerResponseDTO when the corresponding mapper
     * method is available.
     */
    InvestmentResponseDTO toResponseDTO(
            Investment investment
    );
}