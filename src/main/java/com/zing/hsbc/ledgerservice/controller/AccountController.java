package com.zing.hsbc.ledgerservice.controller;

import com.zing.hsbc.ledgerservice.dto.AccountUpdateDto;
import com.zing.hsbc.ledgerservice.entity.Account;
import com.zing.hsbc.ledgerservice.exception.OperationForbiddenException;
import com.zing.hsbc.ledgerservice.exception.ResourceNotFoundException;
import com.zing.hsbc.ledgerservice.service.AccountService;
import com.zing.hsbc.ledgerservice.state.AccountState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/create")
    public Account createAccount(@RequestBody Account account) {

        return accountService.createAccount(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable(value = "id") Long accountId) {
        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for this id :: " + accountId));
        return ResponseEntity.ok().body(account);
    }

    @GetMapping("/findAll")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PutMapping("/update")
    public ResponseEntity<Account> updateAccount(@RequestBody AccountUpdateDto dto) {
        Account account = accountService.getAccountById(dto.getId()).orElseThrow(() -> new ResourceNotFoundException("Account not found for this id :: " + dto.getId()));
        if(AccountState.CREATED.equals(dto.getState()))
            throw new OperationForbiddenException("Account state can't revert back to Created for id :: " + dto.getId());
        account.setName(dto.getName());
        account.setState(dto.getState());
        final Account updatedAccount = accountService.updateAccount(account);
        return ResponseEntity.ok(updatedAccount);
    }

}
