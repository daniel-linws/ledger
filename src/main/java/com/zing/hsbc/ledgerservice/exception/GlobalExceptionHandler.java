package com.zing.hsbc.ledgerservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientFundException.class)
    @ResponseBody
    public ErrorResponse handleInsufficientFundException(InsufficientFundException ex) {
        return new ErrorResponse("INSUFFICIENT_FUNDS", ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(OperationForbiddenException.class)
    @ResponseBody
    public ErrorResponse handleOperationForbiddenException(OperationForbiddenException ex) {
        return new ErrorResponse("OPERATION_FORBIDDEN", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorResponse handleException(Exception ex) {
        return new ErrorResponse("GENERAL_EXCEPTION", ex.getMessage());
    }

    // Define ErrorResponse class or use an existing error response structure
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String errorCode;
        private String errorMessage;
    }
}

