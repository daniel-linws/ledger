package com.zing.hsbc.ledgerservice.repo;

import com.zing.hsbc.ledgerservice.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}