package com.zing.hsbc.ledgerservice.entity;

import com.zing.hsbc.ledgerservice.state.AssetType;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private AssetType asset;
    private Long accountId;

}
