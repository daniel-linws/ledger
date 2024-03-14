package com.zing.hsbc.ledgerservice.repo;

import com.zing.hsbc.ledgerservice.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}