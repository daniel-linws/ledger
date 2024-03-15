package com.zing.hsbc.ledgerservice.helper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.entity.Wallet;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
public class Utils {
    private static ObjectMapper mapper;
    public static String getIdFromTransaction(List<Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> transaction.getId().toString())
                .collect(Collectors.joining(","));
    }

    public static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
        }
        return mapper;
    }

    public static TransactionQuery createTransactionQueryFromTransactionAndWallets(Transaction transaction, Wallet sourceWallet, Wallet targetWallet, BigDecimal sourceBalanceBefore, BigDecimal targetBalanceBefore) {
        return TransactionQuery.builder()
                .transactionId(transaction.getId())
                .sourceWalletId(transaction.getSourceWalletId())
                .targetWalletId(transaction.getTargetWalletId())
                .asset(sourceWallet.getAsset())
                .sourceBalanceBefore(sourceBalanceBefore)
                .targetBalanceBefore(targetBalanceBefore)
                .sourceBalanceAfter(sourceWallet.getBalance())
                .targetBalanceAfter(targetWallet.getBalance())
                .state(transaction.getState())
                .creationDate(transaction.getCreationDate())
                .transactionDate(transaction.getTransactionDate())
                .build();
    }
}
