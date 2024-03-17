package com.zing.hsbc.ledgerservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.helper.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.zing.hsbc.ledgerservice.kafka.KafkaTopic.*;

@Service
public class NotificationProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendProcessTransaction(List<Transaction> transactions) {
        String ids = Utils.getIdFromTransaction(transactions);
        kafkaTemplate.send(TOPIC_POSTING_PROCESS, ids);
    }

    public void sendClearTransaction(List<TransactionQuery> transactionQueries) throws JsonProcessingException {
        String ids = transactionQueries.stream()
                .map(transaction -> transaction.getTransactionId().toString())
                .collect(Collectors.joining(","));
        kafkaTemplate.send(TOPIC_POSTING_CLEAR, ids);
        String json = Utils.getMapper().writeValueAsString(transactionQueries);
        kafkaTemplate.send(TOPIC_TRANSACTION_QUERY_CHANGED, json);
    }

    public void sendFailedTransaction(List<Transaction> transactions) {
        String json = Utils.getIdFromTransaction(transactions);
        kafkaTemplate.send(TOPIC_POSTING_FAILED, json);

    }

    public void sendBalanceChangedMsg(List<Transaction> transactions) {
        String ids = transactions.stream()
                .map(transaction -> transaction.getTransactionId().toString())
                .collect(Collectors.joining(","));
        kafkaTemplate.send(TOPIC_BALANCE_CHANGED, ids);

    }
}
