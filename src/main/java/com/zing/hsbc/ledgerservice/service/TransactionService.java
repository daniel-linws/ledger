package com.zing.hsbc.ledgerservice.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.helper.Utils;
import com.zing.hsbc.ledgerservice.state.TransactionState;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.entity.Wallet;
import com.zing.hsbc.ledgerservice.exception.InsufficientFundException;
import com.zing.hsbc.ledgerservice.exception.ResourceNotFoundException;
import com.zing.hsbc.ledgerservice.notification.NotificationSender;
import com.zing.hsbc.ledgerservice.repo.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zing.hsbc.ledgerservice.notification.KafkaTopic.TOPIC_POSTING_CLEAR;
import static com.zing.hsbc.ledgerservice.notification.KafkaTopic.TOPIC_POSTING_PROCESS;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    NotificationSender notificationSender;
    @Autowired
    private WalletService walletService;

    @Transactional
    @KafkaListener(topics = TOPIC_POSTING_PROCESS)
    public void processTransactions(List<Long> transactionIds) throws JsonProcessingException {
        List<Transaction> transactions = transactionRepository.findAllById(transactionIds);
        List<TransactionQuery> transactionQueries=new ArrayList<>();

        try {
            for (Transaction transaction : transactions) {
                // Fetch source and target wallets
                Wallet sourceWallet = walletService.getWallet(transaction.getSourceWalletId()).orElseThrow(()->new ResourceNotFoundException("Account not Found for transaction ID " + transaction.getId()));
                Wallet targetWallet = walletService.getWallet(transaction.getTargetWalletId()).orElseThrow(()->new ResourceNotFoundException("Account not Found for transaction ID " + transaction.getId()));
                BigDecimal sourceBalanceBefore = sourceWallet.getBalance();
                BigDecimal targetBalanceBefore = targetWallet.getBalance();

                // Perform balance check and update
                BigDecimal amount = transaction.getAmount();
                if (sourceWallet.getBalance().compareTo(amount) >= 0) {
                    sourceWallet.setBalance(sourceWallet.getBalance().subtract(amount));
                    targetWallet.setBalance(targetWallet.getBalance().add(amount));
                } else {
                    // Insufficient funds in source wallet
                    throw new InsufficientFundException("Insufficient funds in source wallet for transaction ID " + transaction.getId());
                }

                // Persist wallet updates
                walletService.createOrUpdateWallet(sourceWallet);
                walletService.createOrUpdateWallet(targetWallet);
                // Update transaction status to Cleared
                transaction.setState(TransactionState.CLEAR);
                transaction.setTransactionDate(LocalDateTime.now());
                TransactionQuery transactionQuery = Utils.createTransactionQueryFromTransactionAndWallets(transaction, sourceWallet, targetWallet,sourceBalanceBefore, targetBalanceBefore);
                transactionQueries.add(transactionQuery);
            }
        } catch (Exception e) {
            transactions.forEach(transaction -> {
                transaction.setState(TransactionState.FAILED);
                transaction.setTransactionDate(LocalDateTime.now());
            });
            transactionRepository.saveAll(transactions);
            notificationSender.sendFailedTransaction(transactions);
            throw e; // Rethrow to ensure rollback
        }
        // If all transactions are processed successfully, save the updated status
        transactionRepository.saveAll(transactions);
        notificationSender.sendClearTransaction(transactionQueries);
    }

    /**
     * Calculates the balance of a wallet at a given timestamp by summing
     * the credits and debits up to that point.
     *
     * @param walletId  ID of the wallet
     * @param timestamp The timestamp up to which to calculate the balance
     * @return The calculated balance
     */
    public BigDecimal getWalletBalanceAtTimestamp(Long walletId, LocalDateTime timestamp) {
        BigDecimal credits = transactionRepository.sumCreditsUpTo(walletId, timestamp);
        BigDecimal debits = transactionRepository.sumDebitsUpTo(walletId, timestamp);
        return credits.subtract(debits);
    }

    @Transactional
    public List<Transaction> createTransactions(List<Transaction> transactions) {
        return transactionRepository.saveAll(transactions);
    }

}
