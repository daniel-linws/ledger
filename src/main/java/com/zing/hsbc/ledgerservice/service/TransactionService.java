package com.zing.hsbc.ledgerservice.service;
import com.zing.hsbc.ledgerservice.TransactionState;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import com.zing.hsbc.ledgerservice.entity.Wallet;
import com.zing.hsbc.ledgerservice.exception.InsufficientFundException;
import com.zing.hsbc.ledgerservice.exception.ResourceNotFoundException;
import com.zing.hsbc.ledgerservice.repo.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletService walletService; // Assuming this service exists to manage Wallet entities

    @Transactional
    public void processTransactions(List<Long> transactionIds) {
        List<Transaction> transactions = transactionRepository.findAllById(transactionIds);
        try {
            for (Transaction transaction : transactions) {
                // Fetch source and target wallets
                Wallet sourceWallet = walletService.getWallet(transaction.getSourceWalletId()).orElseThrow(()->new ResourceNotFoundException("Account not Found for transaction ID " + transaction.getId()));
                Wallet targetWallet = walletService.getWallet(transaction.getTargetWalletId()).orElseThrow(()->new ResourceNotFoundException("Account not Found for transaction ID " + transaction.getId()));

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
            }
        } catch (Exception e) {
            transactions.forEach(transaction -> {
                transaction.setState(TransactionState.FAILED);
                transaction.setTransactionDate(LocalDateTime.now());
            });
            transactionRepository.saveAll(transactions);
            throw e; // Rethrow to ensure rollback
        }
        // If all transactions are processed successfully, save the updated status
        transactionRepository.saveAll(transactions);
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
