package com.zing.hsbc.ledgerservice.service;

import com.zing.hsbc.ledgerservice.entity.WalletSubscription;
import com.zing.hsbc.ledgerservice.repo.WalletSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WalletSubscriptionService {

    @Autowired
    private WalletSubscriptionRepository subscriptionRepository;

    @Transactional
    public WalletSubscription subscribe(Long clientId, Long walletId) {
        WalletSubscription subscription = new WalletSubscription(clientId, walletId);
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public void unsubscribe(Long clientId, Long walletId) {
        subscriptionRepository.deleteByClientIdAndWalletId(clientId, walletId);
    }

    public List<WalletSubscription> findAllByWalletId(Long walletId){
        return subscriptionRepository.findAllByWalletId(walletId);
    }
}

