package com.zing.hsbc.ledgerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientFundException extends RuntimeException {

    public InsufficientFundException(String message) {
        super(message);
    }
}
