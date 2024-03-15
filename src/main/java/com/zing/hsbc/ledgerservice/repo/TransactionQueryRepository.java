package com.zing.hsbc.ledgerservice.repo;

import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionQueryRepository extends JpaRepository<TransactionQuery, Long> {

//    @Query("SELECT SUM(t.amount) FROM TransactionQuery t WHERE t.walletId = :walletId AND t.transactionDate <= :timestamp")
//    BigDecimal findWalletBalanceUpTo(@Param("walletId") Long walletId, @Param("timestamp") LocalDateTime timestamp);
}

