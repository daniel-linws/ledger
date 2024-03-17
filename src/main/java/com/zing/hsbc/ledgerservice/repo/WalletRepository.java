package com.zing.hsbc.ledgerservice.repo;

import com.zing.hsbc.ledgerservice.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findAllByOrderByIdAsc();
}