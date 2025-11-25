package org.example.microserviceaccount.mapper;

import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Account accountCreateDTOToAccount(AccountCreateDTO dto);

    AccountResponseDTO accountToAccountResponseDTO(Account account);

    List<AccountResponseDTO> accountsToAccountResponseDTOs(List<Account> accounts);
}