package com.zing.hsbc.ledgerservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zing.hsbc.ledgerservice.dto.TransactionUpdateDto;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.eventSource.TransactionClearEvent;
import com.zing.hsbc.ledgerservice.eventSource.TransactionCreatedEvent;
import com.zing.hsbc.ledgerservice.eventSource.TransactionFailedEvent;
import com.zing.hsbc.ledgerservice.exception.OperationForbiddenException;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.zing.hsbc.ledgerservice.notification.KafkaTopic.TOPIC_POSTING_PROCESS;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    NotificationSender notificationSender;
    @Autowired
    private WalletService walletService;
    @Autowired
    private EventStoreService eventStoreService;

    @KafkaListener(topics = TOPIC_POSTING_PROCESS)
    public void processTransactions(List<Long> transactionIds) {
        List<Transaction> transactions = transactionRepository.findAllById(transactionIds).stream().collect(Collectors.toUnmodifiableList());
        try {
            doprocessTransactions(transactions);
        } catch (Exception e) {
            handleErrorOccur(transactions);
        }
    }

    @Transactional
    public void doprocessTransactions(List<Transaction> transactions) throws JsonProcessingException {
        List<TransactionQuery> transactionQueries = new ArrayList<>();
        for (Transaction transaction : transactions) {
            // Fetch source and target wallets
            Wallet sourceWallet = walletService.getWallet(transaction.getSourceWalletId()).orElseThrow(() -> new ResourceNotFoundException("Account not Found for transaction ID " + transaction.getTransactionId()));
            Wallet targetWallet = walletService.getWallet(transaction.getTargetWalletId()).orElseThrow(() -> new ResourceNotFoundException("Account not Found for transaction ID " + transaction.getTransactionId()));
            BigDecimal sourceBalanceBefore = sourceWallet.getBalance();
            BigDecimal targetBalanceBefore = targetWallet.getBalance();

            // Perform balance check and update
            BigDecimal amount = transaction.getAmount();
            if (sourceWallet.getBalance().compareTo(amount) >= 0) {
                sourceWallet.setBalance(sourceWallet.getBalance().subtract(amount));
                targetWallet.setBalance(targetWallet.getBalance().add(amount));
            } else {
                // Insufficient funds in source wallet
                throw new InsufficientFundException("Insufficient funds in source wallet for transaction ID " + transaction.getTransactionId());
            }

            // Persist wallet updates
            walletService.createOrUpdateWallet(sourceWallet);
            walletService.createOrUpdateWallet(targetWallet);
            // Update transaction status to Cleared
            transaction.setState(TransactionState.CLEAR);
            transaction.setTransactionDate(LocalDateTime.now());
            TransactionQuery transactionQuery = Utils.createTransactionQueryFromTransactionAndWallets(transaction, sourceWallet, targetWallet, sourceBalanceBefore, targetBalanceBefore);
            transactionQueries.add(transactionQuery);
            TransactionClearEvent event = new TransactionClearEvent(transaction.getTransactionId(), transactionQuery);
            eventStoreService.saveEvent(transaction.getTransactionId(), event);

        }

        // If all transactions are processed successfully, save the updated status
        transactionRepository.saveAll(transactions);
        notificationSender.sendClearTransaction(transactionQueries);
    }

    @Transactional
    public void handleErrorOccur(List<Transaction> transactions) {
        transactions.forEach(transaction -> {
            transaction.setState(TransactionState.FAILED);
            transaction.setTransactionDate(LocalDateTime.now());
        });
        transactionRepository.saveAll(transactions);
        notificationSender.sendFailedTransaction(transactions);
        transactions.forEach(transaction -> {
            try {
                TransactionFailedEvent event = new TransactionFailedEvent(transaction.getTransactionId(), transaction);
                eventStoreService.saveEvent(transaction.getTransactionId(), event);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Calculates the balance of a wallet at a given timestamp by summing
     * the credits and debits up to that point.
     *
     * @param walletId  ID of the wallet
     * @param timestamp The timestamp up to which to calculate the balance
     * @return The calculated balance
     */

    @Transactional
    public List<Transaction> createTransactions(List<Transaction> transactions) {
        List<Transaction> updatedTransactions = transactionRepository.saveAll(transactions);
        updatedTransactions.stream().forEach(transaction -> {
            TransactionCreatedEvent event = new TransactionCreatedEvent(transaction.getTransactionId(), transaction);
            try {
                eventStoreService.saveEvent(transaction.getTransactionId(), event);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
        notificationSender.sendProcessTransaction(updatedTransactions);
        return updatedTransactions;
    }

    public Transaction updateTransactionIfPending(Long transactionID, TransactionUpdateDto dto) {
        Transaction transaction = transactionRepository.findById(transactionID).orElseThrow(() -> new ResourceNotFoundException("Transaction not found for this id :: " + transactionID));
        if (transaction.getState().equals(TransactionState.PENDING)) {
            transaction.setAmount(dto.getAmount());
            transaction.setTargetWalletId(dto.getTargetWalletId());
            transaction.setSourceWalletId(dto.getSourceWalletId());
            transactionRepository.save(transaction);
            return transaction;
        }else throw new OperationForbiddenException("Update transaction forbidden due to pending state for id ::" + transactionID);
    }
}
