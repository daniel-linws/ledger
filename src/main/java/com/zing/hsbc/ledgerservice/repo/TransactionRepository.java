package com.zing.hsbc.ledgerservice.repo;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
