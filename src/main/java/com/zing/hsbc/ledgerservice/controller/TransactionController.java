package com.zing.hsbc.ledgerservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zing.hsbc.ledgerservice.entity.Account;
import com.zing.hsbc.ledgerservice.entity.Wallet;
import com.zing.hsbc.ledgerservice.exception.OperationForbiddenException;
import com.zing.hsbc.ledgerservice.exception.ResourceNotFoundException;
import com.zing.hsbc.ledgerservice.service.AccountService;
import com.zing.hsbc.ledgerservice.service.WalletService;
import com.zing.hsbc.ledgerservice.state.AccountState;
import com.zing.hsbc.ledgerservice.state.TransactionState;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.notification.NotificationSender;
import com.zing.hsbc.ledgerservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private AccountService accountService;
    @Autowired
    NotificationSender notificationSender;
    @PostMapping("/createTransactions")
    public ResponseEntity<List<Transaction>> createTransactions(@RequestBody List<Transaction> transactions) throws JsonProcessingException {
        transactions.stream().forEach(transaction -> {
            //only active account state can execute the transaction
            Wallet targetWallet = walletService.getWallet(transaction.getTargetWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet not found for id :: " + transaction.getTargetWalletId()));
            Wallet sourceWallet = walletService.getWallet(transaction.getSourceWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet not found for id :: " + transaction.getSourceWalletId()));
            Account targetAccount = accountService.getAccountById(targetWallet.getAccountId()).orElseThrow(() -> new ResourceNotFoundException("Account not found for id :: " + targetWallet.getAccountId()));
            Account sourceAccount = accountService.getAccountById(sourceWallet.getAccountId()).orElseThrow(() -> new ResourceNotFoundException("Account not found for id :: " + sourceWallet.getAccountId()));
            if (!AccountState.ACTIVE.equals(targetAccount.getState()) || !AccountState.ACTIVE.equals(sourceAccount.getState()))
                throw new OperationForbiddenException("Account State is not active");
            transaction.setState(TransactionState.PENDING);
            transaction.setCreationDate(LocalDateTime.now());
        });
        List<Transaction> savedTransactions = transactionService.createTransactions(transactions);
        notificationSender.sendProcessTransaction(transactions);
        return ResponseEntity.ok(savedTransactions);
    }

    /**
     * Endpoint to get the balance of a wallet at a specific timestamp.
     */
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getWalletBalanceAtTimestamp(@RequestParam Long walletId,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp) {
        BigDecimal balance = transactionService.getWalletBalanceAtTimestamp(walletId, timestamp);
        return ResponseEntity.ok(balance);
    }
    //2024-03-15T10:30:00
}

