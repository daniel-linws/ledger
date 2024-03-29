package com.zing.hsbc.ledgerservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.helper.Utils;
import com.zing.hsbc.ledgerservice.repo.TransactionQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.zing.hsbc.ledgerservice.kafka.KafkaTopic.TOPIC_TRANSACTION_QUERY_CHANGED;

@Service
public class TransactionQueryService {

    @Autowired
    private TransactionQueryRepository transactionQueryRepository;

    // Implements the CQRS pattern by aggregating wallet and transaction details, consolidating this information into a single record.
    // This record is then saved to the TransactionQuery table, facilitating streamlined query operations and enhanced data retrieval efficiency.
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

    public List<TransactionQuery> findAllById(List<Long> transactionIds) {
        return transactionQueryRepository.findAllById(transactionIds);
    }

    public List<TransactionQuery> findAllByOrderByIdAsc() {
        return transactionQueryRepository.findAllByOrderByTransactionIdDesc();
    }

    public TransactionQuery save(TransactionQuery transactionQuery) {
        return transactionQueryRepository.save(transactionQuery);
    }

}

