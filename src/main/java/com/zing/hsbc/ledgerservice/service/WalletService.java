package com.zing.hsbc.ledgerservice.service;

import com.zing.hsbc.ledgerservice.entity.Wallet;
import com.zing.hsbc.ledgerservice.repo.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public Optional<Wallet> getWallet(Long id) {
        return walletRepository.findById(id);
    }

    public Wallet createOrUpdateWallet(Wallet wallet) {
        return walletRepository.save(wallet);
    }
}
