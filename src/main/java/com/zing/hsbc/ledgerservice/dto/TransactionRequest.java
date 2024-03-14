package com.zing.hsbc.ledgerservice.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionRequest {
    private Long sourceWalletId;
    private Long targetWalletId;
    private BigDecimal amount;
}
