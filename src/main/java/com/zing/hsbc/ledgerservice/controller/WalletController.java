package com.zing.hsbc.ledgerservice.controller;

import com.zing.hsbc.ledgerservice.entity.Wallet;
import com.zing.hsbc.ledgerservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWallet(@PathVariable Long id) {
        return walletService.getWallet(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/update")
    public Wallet createOrUpdateWallet(@RequestBody Wallet wallet) {
        return walletService.createOrUpdateWallet(wallet);
    }

    //for testing purpose only
    @GetMapping("/findAll")
    public List<Wallet> findAll(){
        List<Wallet> wallets = walletService.findAll();
        return wallets;
    }
}

