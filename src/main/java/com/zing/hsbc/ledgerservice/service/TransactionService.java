package com.zing.hsbc.ledgerservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zing.hsbc.ledgerservice.dto.TransactionUpdateDto;
import com.zing.hsbc.ledgerservice.entity.Account;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.entity.Wallet;
import com.zing.hsbc.ledgerservice.eventSource.TransactionClearEvent;
import com.zing.hsbc.ledgerservice.eventSource.TransactionCreatedEvent;
import com.zing.hsbc.ledgerservice.eventSource.TransactionFailedEvent;
import com.zing.hsbc.ledgerservice.exception.InsufficientFundException;
import com.zing.hsbc.ledgerservice.exception.OperationForbiddenException;
import com.zing.hsbc.ledgerservice.exception.ResourceNotFoundException;
import com.zing.hsbc.ledgerservice.helper.Utils;
import com.zing.hsbc.ledgerservice.notification.NotificationProducer;
import com.zing.hsbc.ledgerservice.repo.TransactionRepository;
import com.zing.hsbc.ledgerservice.state.AccountState;
import com.zing.hsbc.ledgerservice.state.TransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.zing.hsbc.ledgerservice.notification.KafkaTopic.TOPIC_POSTING_PROCESS;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private NotificationProducer notificationProducer;
    @Autowired
    private WalletService walletService;
    @Autowired
    private EventStoreService eventStoreService;
    @Autowired
    AccountService accountService;
    @Autowired
    TransactionQueryService transactionQueryService;
    @Autowired
    PlatformTransactionManager transactionManager;

    // Handles incoming Kafka messages containing transaction IDs for processing.
    // Initiates the transaction processing workflow for each received ID.
    @KafkaListener(topics = TOPIC_POSTING_PROCESS)
    public void processTransactions(String idsString) {
        List<Long> transactionIds = Arrays.stream(idsString.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        log.info("Starting to process transactions with IDs: {}", transactionIds);
        List<Transaction> transactions = transactionRepository.findAllById(transactionIds).stream().collect(Collectors.toUnmodifiableList());
        try {
            doProcessTransactions(transactions);
        } catch (Exception e) {
            log.error("An error occurred during transaction processing: ", e);
            handleErrorOccur(transactions);
        }
    }

    // Processes a list of transactions by executing financial operations between source and target wallets.
    // Ensures sufficient funds, updates wallet balances, and records transactions as either CLEAR or FAILED.
    public void doProcessTransactions(List<Transaction> transactions) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            log.info("Processing {} transactions.", transactions.size());
            List<TransactionQuery> transactionQueries = new ArrayList<>();
            for (Transaction transaction : transactions) {
                Wallet sourceWallet = walletService.getWallet(transaction.getSourceWalletId()).orElseThrow(() -> new ResourceNotFoundException("Source account not found for transaction ID " + transaction.getTransactionId()));
                Wallet targetWallet = walletService.getWallet(transaction.getTargetWalletId()).orElseThrow(() -> new ResourceNotFoundException("Target account not found for transaction ID " + transaction.getTransactionId()));
                BigDecimal sourceBalanceBefore = sourceWallet.getBalance();
                BigDecimal targetBalanceBefore = targetWallet.getBalance();

                if (sourceWallet.getBalance().compareTo(transaction.getAmount()) >= 0) {
                    sourceWallet.setBalance(sourceWallet.getBalance().subtract(transaction.getAmount()));
                    targetWallet.setBalance(targetWallet.getBalance().add(transaction.getAmount()));
                    log.debug("Transferred {} from wallet {} to wallet {}.", transaction.getAmount(), transaction.getSourceWalletId(), transaction.getTargetWalletId());
                } else {
                    log.debug("Insufficient funds in source wallet {} for transaction ID {}", transaction.getSourceWalletId(), transaction.getTransactionId());
                    throw new InsufficientFundException("Insufficient funds in source wallet for transaction ID " + transaction.getTransactionId());
                }

                walletService.createOrUpdateWallet(sourceWallet);
                walletService.createOrUpdateWallet(targetWallet);
                transaction.setState(TransactionState.CLEAR);
                transaction.setTransactionDate(LocalDateTime.now());
                TransactionQuery transactionQuery = Utils.createTransactionQueryFromTransactionAndWallets(transaction, sourceWallet, targetWallet, sourceBalanceBefore, targetBalanceBefore);
                transactionQueries.add(transactionQuery);
                TransactionClearEvent event = new TransactionClearEvent(transaction.getTransactionId(), transactionQuery);
                eventStoreService.saveEvent(transaction.getTransactionId(), event);
            }

            transactionRepository.saveAll(transactions);
            notificationProducer.sendClearTransaction(transactionQueries);
            notificationProducer.sendBalanceChangedMsg(transactions);
            log.info("Completed processing transactions.");
            transactionManager.commit(status);
        } catch (Exception ex) {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
            throw new RuntimeException(ex);
        }
    }

    // Handles exceptions encountered during transaction processing.
    // Marks affected transactions as FAILED, updates their status, and notifies the necessary parties.
    public void handleErrorOccur(List<Transaction> transactions) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            log.warn("Handling error for {} transactions.", transactions.size());
            LocalDateTime now = LocalDateTime.now();
            transactions.forEach(transaction -> {
                transaction.setState(TransactionState.FAILED);
                transaction.setTransactionDate(now);
                log.info("Marked transaction ID {} as FAILED.", transaction.getTransactionId());
                TransactionQuery transactionQuery = Utils.createTransactionQueryFromTransaction(transaction);
                transactionQueryService.save(transactionQuery);
            });
            transactionRepository.saveAll(transactions);
            notificationProducer.sendFailedTransaction(transactions);
            transactions.forEach(transaction -> {
                try {
                    TransactionFailedEvent event = new TransactionFailedEvent(transaction.getTransactionId(), transaction);
                    eventStoreService.saveEvent(transaction.getTransactionId(), event);
                } catch (JsonProcessingException e) {
                    log.error("Failed to serialize failed event for transaction ID {}: ", transaction.getTransactionId(), e);
                    throw new RuntimeException(e);
                }
            });
            transactionManager.commit(status);
        } catch (Exception ex) {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
            throw new RuntimeException(ex);
        }
    }

    // Creates and persists new transactions based on input, generating necessary events for each.
    // Utilizes Event Sourcing to record the creation of transactions and their initial state.
    @Transactional
    public List<Transaction> createTransactions(List<Transaction> transactions) {
        log.info("Creating {} transactions.", transactions.size());
        List<Transaction> updatedTransactions = transactionRepository.saveAll(transactions);
        updatedTransactions.forEach(transaction -> {
            TransactionCreatedEvent event = new TransactionCreatedEvent(transaction.getTransactionId(), transaction);
            try {
                eventStoreService.saveEvent(transaction.getTransactionId(), event);
                TransactionQuery transactionQuery = Utils.createTransactionQueryFromTransaction(transaction);
                transactionQueryService.save(transactionQuery);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize created event for transaction", e);
                throw new RuntimeException(e);
            }
        });


        notificationProducer.sendProcessTransaction(updatedTransactions);
        return updatedTransactions;
    }

    // Updates a transaction if it's in a PENDING state with new details from the provided DTO.
    // Throws exceptions if the transaction is not found or not in a PENDING state.
    @Transactional
    public Transaction updateTransactionIfPending(Long transactionID, TransactionUpdateDto dto) {
        log.info("Updating transaction with ID: {}", transactionID);
        Transaction transaction = transactionRepository.findById(transactionID).orElseThrow(() -> new ResourceNotFoundException("Transaction not found for this ID: " + transactionID));
        if (transaction.getState().equals(TransactionState.PENDING)) {
            transaction.setAmount(dto.getAmount());
            transaction.setTargetWalletId(dto.getTargetWalletId());
            transaction.setSourceWalletId(dto.getSourceWalletId());
            transactionRepository.save(transaction);
            log.info("Transaction ID {} updated successfully.", transactionID);
            return transaction;
        } else {
            log.warn("Attempted to update transaction ID {} not in PENDING state.", transactionID);
            throw new OperationForbiddenException("Update transaction forbidden for ID: " + transactionID);
        }
    }

    // Validates a transaction's eligibility based on wallet existence, account validity, and other business rules.
    // Ensures transactions comply with predefined constraints (e.g., active accounts, matching asset types).
    public void validateTransaction(Transaction transaction) {
        Wallet targetWallet = walletService.getWallet(transaction.getTargetWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for id :: " + transaction.getTargetWalletId()));
        Wallet sourceWallet = walletService.getWallet(transaction.getSourceWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for id :: " + transaction.getSourceWalletId()));

        Account targetAccount = accountService.getAccountById(targetWallet.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for id :: " + targetWallet.getAccountId()));
        Account sourceAccount = accountService.getAccountById(sourceWallet.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for id :: " + sourceWallet.getAccountId()));

        if (sourceWallet.getId().equals(targetWallet.getId())) {
            throw new OperationForbiddenException("source wallet is same as target wallet id ::" + sourceWallet.getId());
        }

        // Check if both accounts are active
        if (!AccountState.ACTIVE.equals(targetAccount.getState())) {
            throw new OperationForbiddenException("account is not in ACTIVE state id ::" + targetAccount.getId());
        }

        if (!AccountState.ACTIVE.equals(sourceAccount.getState())) {
            throw new OperationForbiddenException("account is not in ACTIVE state id ::" + sourceAccount.getId());
        }

        // Check if the asset types of both wallets are the same
        if (!targetWallet.getAsset().equals(sourceWallet.getAsset())) {
            throw new OperationForbiddenException("Asset types do not match between source and target wallets");
        }
    }
}
