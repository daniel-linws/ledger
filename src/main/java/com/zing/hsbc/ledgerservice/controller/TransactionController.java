package com.zing.hsbc.ledgerservice.controller;

import com.zing.hsbc.ledgerservice.dto.TransactionUpdateDto;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.service.TransactionService;
import com.zing.hsbc.ledgerservice.state.TransactionState;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    @Autowired
    private TransactionService transactionService;

    @PostMapping("/createTransactions")
    public ResponseEntity<List<Transaction>> createTransactions(@RequestBody List<Transaction> transactions) {
        log.info("createTransactions called");
        LocalDateTime now = LocalDateTime.now();
        transactions.forEach(transaction -> {
            transactionService.validateTransaction(transaction);
            transaction.setState(TransactionState.PENDING);
            transaction.setCreationDate(now);
            log.info("Transaction set to PENDING with ID: {}", transaction.getTransactionId());
        });
        List<Transaction> savedTransactions = transactionService.createTransactions(transactions);
        log.info("Transactions created successfully");
        return ResponseEntity.ok(savedTransactions);
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long transactionId, @Valid @RequestBody TransactionUpdateDto updateDTO) {
        Transaction updatedTransaction = transactionService.updateTransactionIfPending(transactionId, updateDTO);
        log.info("Transaction updated successfully for ID: {}", transactionId);
        return ResponseEntity.ok(updatedTransaction);
    }


}
