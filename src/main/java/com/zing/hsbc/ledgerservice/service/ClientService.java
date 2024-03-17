package com.zing.hsbc.ledgerservice.service;

import com.zing.hsbc.ledgerservice.entity.Client;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.entity.WalletSubscription;
import com.zing.hsbc.ledgerservice.repo.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.zing.hsbc.ledgerservice.notification.KafkaTopic.TOPIC_BALANCE_CHANGED;

@Service
public class ClientService {
    @Autowired
    private TransactionQueryService transactionQueryService;
    @Autowired
    private WalletSubscriptionService walletSubscriptionService;
    @Autowired
    private ClientRepository clientRepository;
    @KafkaListener(topics = TOPIC_BALANCE_CHANGED)
    public void balanceChangeListener(List<Long> transactionIds) {
        List<TransactionQuery> transactions = transactionQueryService.findAllById(transactionIds);
        transactions.stream().forEach(transactionQuery -> {
            List<WalletSubscription> sourceWalletSubscriptions = walletSubscriptionService.findAllByWalletId(transactionQuery.getSourceWalletId());
            List<WalletSubscription> targetWalletSubscriptions = walletSubscriptionService.findAllByWalletId(transactionQuery.getTargetWalletId());
            sendNotificationToUser(sourceWalletSubscriptions, transactionQuery);
            sendNotificationToUser(targetWalletSubscriptions, transactionQuery);
        });
    }

    public void sendNotificationToUser(List<WalletSubscription> sourceWalletSubscriptions ,TransactionQuery transactionQuery){
        /*
         * implement logic for send notification to clients who interested in the wallet balance with #id by email or publish notification etc.
         */
    }

    public Optional<Client> findById(Long id){
        return clientRepository.findById(id);
    }
}
