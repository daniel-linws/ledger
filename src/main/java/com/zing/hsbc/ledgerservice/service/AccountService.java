package com.zing.hsbc.ledgerservice.service;

import com.zing.hsbc.ledgerservice.entity.Account;
import com.zing.hsbc.ledgerservice.exception.ResourceNotFoundException;
import com.zing.hsbc.ledgerservice.repo.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    public Account createOrUpdateAccount(Account account) {
        return accountRepository.save(account);
    }
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }


}

