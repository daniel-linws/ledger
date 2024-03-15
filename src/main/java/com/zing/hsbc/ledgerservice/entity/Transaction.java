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
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    private BigDecimal amount;
    private LocalDateTime transactionDate;
    private LocalDateTime creationDate;
    private Long sourceWalletId;
    private Long targetWalletId;
    @Enumerated(EnumType.STRING)
    private TransactionState state;

}
