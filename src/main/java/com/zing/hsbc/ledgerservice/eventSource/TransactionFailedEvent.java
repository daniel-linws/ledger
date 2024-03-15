package com.zing.hsbc.ledgerservice.eventSource;

import com.zing.hsbc.ledgerservice.entity.Transaction;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransactionFailedEvent extends BaseEvent {
    private final Transaction transaction;

    public TransactionFailedEvent(Long id, Transaction transaction) {
        super(id);
        this.transaction = transaction;
    }

}
