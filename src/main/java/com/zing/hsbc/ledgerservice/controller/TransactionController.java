package com.zing.hsbc.ledgerservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zing.hsbc.ledgerservice.dto.TransactionUpdateDto;
import com.zing.hsbc.ledgerservice.entity.Account;
import com.zing.hsbc.ledgerservice.entity.Wallet;
import com.zing.hsbc.ledgerservice.exception.OperationForbiddenException;
import com.zing.hsbc.ledgerservice.exception.ResourceNotFoundException;
import com.zing.hsbc.ledgerservice.service.AccountService;
import com.zing.hsbc.ledgerservice.service.WalletService;
import com.zing.hsbc.ledgerservice.state.AccountState;
import com.zing.hsbc.ledgerservice.state.TransactionState;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private AccountService accountService;

    @PostMapping("/createTransactions")
    public ResponseEntity<List<Transaction>> createTransactions(@RequestBody List<Transaction> transactions) throws JsonProcessingException {
        LocalDateTime now = LocalDateTime.now();
        transactions.stream().forEach(transaction -> {
            //only active account state can execute the transaction
            Wallet targetWallet = walletService.getWallet(transaction.getTargetWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet not found for id :: " + transaction.getTargetWalletId()));
            Wallet sourceWallet = walletService.getWallet(transaction.getSourceWalletId()).orElseThrow(() -> new ResourceNotFoundException("Wallet not found for id :: " + transaction.getSourceWalletId()));
            Account targetAccount = accountService.getAccountById(targetWallet.getAccountId()).orElseThrow(() -> new ResourceNotFoundException("Account not found for id :: " + targetWallet.getAccountId()));
            Account sourceAccount = accountService.getAccountById(sourceWallet.getAccountId()).orElseThrow(() -> new ResourceNotFoundException("Account not found for id :: " + sourceWallet.getAccountId()));
            if (!AccountState.ACTIVE.equals(targetAccount.getState()) || !AccountState.ACTIVE.equals(sourceAccount.getState()))
                throw new OperationForbiddenException("Account State is not active");
            transaction.setState(TransactionState.PENDING);
            transaction.setCreationDate(now);
        });
        List<Transaction> savedTransactions = transactionService.createTransactions(transactions);
        return ResponseEntity.ok(savedTransactions);
    }

    @PutMapping("/{updateTransaction}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long transactionId, @Valid @RequestBody TransactionUpdateDto updateDTO) {
        Transaction update =  transactionService.updateTransactionIfPending(transactionId, updateDTO);
        return ResponseEntity.ok(update);
    }
}

