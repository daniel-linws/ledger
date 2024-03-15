package com.zing.hsbc.ledgerservice.eventSource;
import com.zing.hsbc.ledgerservice.entity.Transaction;
import lombok.*;

@Setter
@Getter
public class TransactionCreatedEvent extends BaseEvent {
    private final Transaction transaction;

    public TransactionCreatedEvent(Long id,Transaction transaction) {
        super(id);
        this.transaction = transaction;
    }

}
