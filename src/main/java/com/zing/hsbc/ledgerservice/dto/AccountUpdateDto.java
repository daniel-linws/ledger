package com.zing.hsbc.ledgerservice.dto;

import com.zing.hsbc.ledgerservice.state.AccountState;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class AccountUpdateDto {
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private AccountState state;
}
