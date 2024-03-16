package com.zing.hsbc.ledgerservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clientId;
    private Long walletId;
    public WalletSubscription(Long clientId, Long walletId) {
        this.clientId = clientId;
        this.walletId = walletId;
    }
}

