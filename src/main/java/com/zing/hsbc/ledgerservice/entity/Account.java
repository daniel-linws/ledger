package com.zing.hsbc.ledgerservice.entity;
import com.zing.hsbc.ledgerservice.state.AccountState;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clientId;
    private String name;
    @Enumerated(EnumType.STRING)
    private AccountState state;
}

