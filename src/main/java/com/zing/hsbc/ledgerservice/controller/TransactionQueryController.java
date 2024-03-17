package com.zing.hsbc.ledgerservice.controller;

import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.entity.Wallet;
import com.zing.hsbc.ledgerservice.exception.ResourceNotFoundException;
import com.zing.hsbc.ledgerservice.service.TransactionQueryService;
import com.zing.hsbc.ledgerservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/query")
public class TransactionQueryController {

    @Autowired
    private TransactionQueryService transactionQueryService;
    @Autowired
    private WalletService walletService;

    /**
     * Endpoint to get the balance of a wallet at a specific timestamp.
     */
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getWalletBalanceAtTimestamp(@RequestParam Long walletId,
                                                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp) {
        Optional<BigDecimal> option = transactionQueryService.findWalletBalanceBeforeTimestamp(walletId, timestamp);
        if (option.isPresent())
            return ResponseEntity.ok(option.get());
        else {
            Optional<Wallet> walletOpt =  walletService.getWallet(walletId);
            if(walletOpt.isPresent())
                return ResponseEntity.ok(walletOpt.get().getBalance());
            else
                throw new ResourceNotFoundException("Wallet not found for this id :: " + walletId);
        }
    }

    //for testing purpose only
    @GetMapping("/findAll")
    public List<TransactionQuery> getAllTransactionQuery(){
        List<TransactionQuery> transactionQueries = transactionQueryService.findAllByOrderByIdAsc();
        return transactionQueries;
    }
}
