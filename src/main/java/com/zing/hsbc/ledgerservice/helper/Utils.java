package com.zing.hsbc.ledgerservice.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.entity.Wallet;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
public class Utils {
    private static ObjectMapper mapper;
    public static String getIdFromTransaction(List<Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> transaction.getTransactionId().toString())
                .collect(Collectors.joining(","));
    }
    public static TransactionQuery createTransactionQueryFromTransactionAndWallets(Transaction transaction, Wallet sourceWallet, Wallet targetWallet, BigDecimal sourceBalanceBefore, BigDecimal targetBalanceBefore) {
        return TransactionQuery.builder()
                .transactionId(transaction.getTransactionId())
                .sourceWalletId(transaction.getSourceWalletId())
                .targetWalletId(transaction.getTargetWalletId())
                .asset(sourceWallet.getAsset())
                .sourceBalanceBefore(sourceBalanceBefore)
                .targetBalanceBefore(targetBalanceBefore)
                .sourceBalanceAfter(sourceWallet.getBalance())
                .targetBalanceAfter(targetWallet.getBalance())
                .state(transaction.getState())
                .amount(transaction.getAmount())
                .creationDate(transaction.getCreationDate())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }


    public static TransactionQuery createTransactionQueryFromTransaction(Transaction transaction) {
        return TransactionQuery.builder()
                .transactionId(transaction.getTransactionId())
                .sourceWalletId(transaction.getSourceWalletId())
                .targetWalletId(transaction.getTargetWalletId())
                .state(transaction.getState())
                .amount(transaction.getAmount())
                .creationDate(transaction.getCreationDate())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }



    public static String generateUniqueTransactionId(String prefix) {
        try {
            // Get the host name to add spatial uniqueness
            String hostName = InetAddress.getLocalHost().getHostName();
            // Generate a UUID for further uniqueness
            String uuid = UUID.randomUUID().toString();
            // Current time for temporal uniqueness
            long currentTimeMillis = System.currentTimeMillis();

            // Combine them to form a unique transactional.id
            return String.format("%s-%s-%d-%s", prefix, hostName, currentTimeMillis, uuid);
        } catch (Exception e) {
            // In case getting the hostname fails, fallback to just using UUID and time
            return String.format("%s-%d-%s", prefix, System.currentTimeMillis(), UUID.randomUUID().toString());
        }
    }

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
        }
        return mapper;
    }
}
