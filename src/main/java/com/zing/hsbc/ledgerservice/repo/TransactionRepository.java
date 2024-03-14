package com.zing.hsbc.ledgerservice.repo;

import com.zing.hsbc.ledgerservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.targetWalletId = :walletId AND t.transactionDate <= :timestamp")
    BigDecimal sumCreditsUpTo(@Param("walletId") Long walletId, @Param("timestamp") LocalDateTime timestamp);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.sourceWalletId = :walletId AND t.transactionDate <= :timestamp")
    BigDecimal sumDebitsUpTo(@Param("walletId") Long walletId, @Param("timestamp") LocalDateTime timestamp);
}
