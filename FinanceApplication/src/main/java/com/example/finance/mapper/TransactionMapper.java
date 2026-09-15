package com.example.finance.mapper;

import org.mapstruct.Mapper;

import com.example.finance.entity.Transaction;
import com.example.finance.responceDto.TransactionResponseDTO;

@Mapper(
        componentModel = "spring",
        uses = AccountMapper.class
)
public interface TransactionMapper {

    TransactionResponseDTO toResponseDTO(
            Transaction transaction
    );
}


