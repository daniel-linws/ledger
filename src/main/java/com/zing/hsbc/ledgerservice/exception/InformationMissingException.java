package com.zing.hsbc.ledgerservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InformationMissingException extends RuntimeException {

    public InformationMissingException(String message) {
        super(message);
    }
}
