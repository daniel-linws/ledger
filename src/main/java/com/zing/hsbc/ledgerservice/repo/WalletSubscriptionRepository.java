package com.zing.hsbc.ledgerservice.repo;

import com.zing.hsbc.ledgerservice.entity.WalletSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletSubscriptionRepository extends JpaRepository<WalletSubscription, Long> {
    void deleteByClientIdAndWalletId(Long clientId, Long walletId);
    List<WalletSubscription> findAllByWalletId(Long walletId);
}
