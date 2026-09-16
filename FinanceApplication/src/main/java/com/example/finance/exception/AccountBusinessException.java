package com.example.finance.exception;

public class AccountBusinessException extends RuntimeException {

    public AccountBusinessException(String message) {
        super(message);
    }
}
