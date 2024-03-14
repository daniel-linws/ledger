package com.zing.hsbc.ledgerservice.controller;

import com.zing.hsbc.ledgerservice.TransactionState;
import com.zing.hsbc.ledgerservice.dto.TransactionRequest;
import com.zing.hsbc.ledgerservice.entity.Transaction;
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

    @PostMapping("/createTransactions")
    public ResponseEntity<List<Transaction>> createTransactions(@RequestBody List<Transaction> transactions) {
        transactions.stream().forEach(transaction -> {
            transaction.setState(TransactionState.PENDING);
            transaction.setCreationDate(LocalDateTime.now());
        });
        List<Transaction> savedTransactions = transactionService.createTransactions(transactions);
        List<Long> ids = transactions.stream().map(transaction -> transaction.getId()).collect(Collectors.toList());
        transactionService.processTransactions(ids);
        return ResponseEntity.ok(savedTransactions);
    }

    /**
     * Endpoint to get the balance of a wallet at a specific timestamp.
     *
     * @param walletId  the ID of the wallet
     * @param timestamp the timestamp for the balance query
     * @return the balance at the specified timestamp
     */
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getWalletBalanceAtTimestamp(@RequestParam Long walletId,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp) {
        BigDecimal balance = transactionService.getWalletBalanceAtTimestamp(walletId, timestamp);
        return ResponseEntity.ok(balance);
    }
    //2024-03-15T10:30:00
}

