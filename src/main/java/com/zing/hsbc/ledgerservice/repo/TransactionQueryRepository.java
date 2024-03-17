package com.zing.hsbc.ledgerservice.repo;

import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionQueryRepository extends JpaRepository<TransactionQuery, Long> {
    @Query(value = "SELECT (case when t.target_wallet_id = :walletId then t.target_balance_after " +
            "when t.source_wallet_id = :walletId then t.source_balance_after end) " +
            "FROM transaction_query t WHERE (t.source_wallet_id = :walletId OR t.target_wallet_id = :walletId) " +
            "AND t.transaction_date <= :timestamp ORDER BY t.transaction_date DESC LIMIT 1", nativeQuery = true)
    Optional<BigDecimal> findWalletBalanceBeforeTimestamp(@Param("walletId") Long walletId, @Param("timestamp") LocalDateTime timestamp);

    List<TransactionQuery> findAllByOrderByTransactionIdDesc();
}
