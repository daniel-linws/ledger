package com.zing.hsbc.ledgerservice.entity;

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
    private String asset;
    private LocalDateTime transactionDate;
    private LocalDateTime creationDate;
    @Enumerated(EnumType.STRING)
    private TransactionState state;

}