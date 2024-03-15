package com.zing.hsbc.ledgerservice.eventSource;


import com.zing.hsbc.ledgerservice.entity.TransactionQuery;
import com.zing.hsbc.ledgerservice.state.TransactionState;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class TransactionClearEvent extends BaseEvent {
    private TransactionQuery transactionQuery;

    public TransactionClearEvent(Long id, TransactionQuery transactionQuery) {
        super(id);
        this.transactionQuery = transactionQuery;
    }

}
