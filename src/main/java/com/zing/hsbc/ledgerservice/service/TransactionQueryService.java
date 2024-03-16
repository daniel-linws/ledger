package com.zing.hsbc.ledgerservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.helper.Utils;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.repo.TransactionQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.zing.hsbc.ledgerservice.notification.KafkaTopic.TOPIC_TRANSACTION_QUERY_CHANGED;

@Service
public class TransactionQueryService {

    @Autowired
    private TransactionQueryRepository transactionQueryRepository;

    @Transactional
    @KafkaListener(topics = TOPIC_TRANSACTION_QUERY_CHANGED)
    public void updateTransactionQuery(String json) throws JsonProcessingException {
        List<TransactionQuery> transactionQueries = Utils.getMapper().readValue(json, new TypeReference<>() {
        });
        transactionQueries.stream().forEach(transactionQuery -> {
            transactionQueryRepository.save(transactionQuery);
        });
    }

    public Optional<BigDecimal> findWalletBalanceBeforeTimestamp(Long walletId, LocalDateTime timestamp) {
        return transactionQueryRepository.findWalletBalanceBeforeTimestamp(walletId, timestamp);
    }

}

