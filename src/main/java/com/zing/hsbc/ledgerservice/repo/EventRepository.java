package com.zing.hsbc.ledgerservice.repo;


import com.zing.hsbc.ledgerservice.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
}
