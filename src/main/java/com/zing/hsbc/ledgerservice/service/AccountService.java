package com.zing.hsbc.ledgerservice.service;

import com.zing.hsbc.ledgerservice.entity.Account;
import com.zing.hsbc.ledgerservice.exception.InformationMissingException;
import com.zing.hsbc.ledgerservice.exception.ResourceNotFoundException;
import com.zing.hsbc.ledgerservice.repo.AccountRepository;
import com.zing.hsbc.ledgerservice.state.AccountState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private ClientService clientService;
    @Autowired
    private AccountRepository accountRepository;
    public Account updateAccount(Account account) {
        clientService.findById(account.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found for ID :: " + account.getClientId()));
        if(account.getClientId()==null || StringUtils.isEmpty(account.getName()))
            throw new InformationMissingException("Missing account information");
        return accountRepository.save(account);
    }

    public Account createAccount(Account account) {
        clientService.findById(account.getClientId()).orElseThrow(() -> new ResourceNotFoundException("Client not found for ID :: " + account.getClientId()));
        if(account.getClientId()==null || StringUtils.isEmpty(account.getName()))
            throw new InformationMissingException("Missing account information");
        account.setId(null);
        account.setState(AccountState.CREATED);
        return accountRepository.save(account);
    }
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }


}

