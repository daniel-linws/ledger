package com.zing.hsbc.ledgerservice.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.helper.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.zing.hsbc.ledgerservice.notification.KafkaTopic.*;

@Service
public class NotificationSender {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendProcessTransaction(List<Transaction> transactions) throws JsonProcessingException {
        try {
            String message = Utils.getIdFromTransaction(transactions);
            kafkaTemplate.send(TOPIC_POSTING_PROCESS, message);
            String json = Utils.getMapper().writeValueAsString(transactions);
            kafkaTemplate.send(TOPIC_TRANSACTION_QUERY_CHANGED, json);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
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

//    public void sendBalanceChangedMsg(List<Transaction> transactions) {
//        try {
//            transactions.stream().forEach(transaction -> {
//                BalanceChangedMsg balanceChangedMsg = new BalanceChangedMsg(transaction.getId(),
//                        transaction.getAmount().negate(),
//                        transaction.getTransactionDate(),
//                        transaction.getSourceWalletId());
//                kafkaTemplate.send(TOPIC_BALANCE_CHANGED, Utils.getMapper().writeValueAsString(balanceChangedMsg));
//                balanceChangedMsg = new BalanceChangedMsg(transaction.getId(),
//                        transaction.getAmount(),
//                        transaction.getTransactionDate(),
//                        transaction.getTargetWalletId());
//                kafkaTemplate.send(TOPIC_BALANCE_CHANGED, Utils.getMapper().writeValueAsString(balanceChangedMsg));
//            });
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            throw ex;
//        }
//    }
}
