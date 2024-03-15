package com.zing.hsbc.ledgerservice.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BalanceChangedMsg {
    private Long transactionId;
    private BigDecimal amountChange;
    private LocalDateTime transactionDate;
    private Long walletId;
}
