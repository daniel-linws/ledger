package com.zing.hsbc.ledgerservice.service;

import com.zing.hsbc.ledgerservice.entity.Wallet;
import com.zing.hsbc.ledgerservice.exception.OperationForbiddenException;
import com.zing.hsbc.ledgerservice.repo.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    public Optional<Wallet> getWallet(Long id) {
        return walletRepository.findById(id);
    }

    public Wallet createOrUpdateWallet(Wallet wallet) {
        if(StringUtils.isEmpty(wallet.getAsset()) || StringUtils.isEmpty(wallet.getAccountId()))
            throw new OperationForbiddenException("Information missing");
        return walletRepository.save(wallet);
    }

    public List<Wallet> findAll(){
        List<Wallet> wallets = walletRepository.findAllByOrderByIdAsc();
        return wallets;
    }
}
