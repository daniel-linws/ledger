package com.zing.hsbc.ledgerservice.eventSource;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public abstract class BaseEvent {
    private final Long id;
    private final LocalDateTime timestamp;

    protected BaseEvent(Long id) {
        this.id = id;
        this.timestamp = LocalDateTime.now();
    }
}

