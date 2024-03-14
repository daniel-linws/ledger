package com.zing.hsbc.ledgerservice.entity;

import lombok.Data;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Set;

@Entity
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal balance ;
    private String currency;
    private Long accountId;

}
