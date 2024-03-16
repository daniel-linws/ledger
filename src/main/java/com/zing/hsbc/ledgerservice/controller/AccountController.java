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
        // When an account is first registered or created in the system,
        // the default state is "Created". This status is used for accounts that have been initiated but are not yet active,
        // often because they require further steps such as email verification.
        account.setState(AccountState.CREATED);
        return accountService.createAccount(account);
    }

    @PatchMapping("/updateState/{accountId}/{newState}")
    public ResponseEntity<Account> updateAccountState(@PathVariable Long accountId,
                                                      @PathVariable("newState") AccountState newState) {
        // can add logic to prevent set the state back to "Created" if necessary
        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for this id :: " + accountId));
        account.setState(newState);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable(value = "id") Long accountId) {
        Account account = accountService.getAccountById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for this id :: " + accountId));
        return ResponseEntity.ok().body(account);
    }

    @GetMapping
    public List<Account> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Account> updateAccount(@PathVariable(value = "id") Long accountId,
                                                 @RequestBody AccountUpdateDto dto) {
        Account account = accountService.getAccountById(accountId).orElseThrow(() -> new ResourceNotFoundException("Account not found for this id :: " + accountId));
        //the account info can't be updated if the state is CLOSED
        if (AccountState.CLOSED.equals(account.getState()))
            throw new OperationForbiddenException("Account state is closed for id :: " + accountId);
        account.setName(dto.getName());
        account.setState(dto.getState());
        final Account updatedAccount = accountService.createOrUpdateAccount(account);
        return ResponseEntity.ok(updatedAccount);
    }

}
