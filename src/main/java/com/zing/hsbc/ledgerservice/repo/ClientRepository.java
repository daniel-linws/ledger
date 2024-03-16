package com.zing.hsbc.ledgerservice.repo;

import com.zing.hsbc.ledgerservice.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}