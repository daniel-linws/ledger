package com.zing.hsbc.ledgerservice.repo;


import com.zing.hsbc.ledgerservice.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findAllByOrderByIdDesc();
}
