package com.zing.hsbc.ledgerservice.entity;

import com.zing.hsbc.ledgerservice.state.AssetType;
import com.zing.hsbc.ledgerservice.state.TransactionState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionQuery {
    @Id
    private Long transactionId;
    private Long sourceWalletId;
    private Long targetWalletId;
    private BigDecimal sourceBalanceBefore;
    private BigDecimal sourceBalanceAfter;
    private BigDecimal targetBalanceBefore;
    private BigDecimal targetBalanceAfter;
    @Enumerated(EnumType.STRING)
    private AssetType asset;
    private LocalDateTime transactionDate;
    private LocalDateTime creationDate;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private TransactionState state;

}
