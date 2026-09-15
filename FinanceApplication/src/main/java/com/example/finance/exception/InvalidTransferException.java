package com.example.finance.exception;


public class InvalidTransferException
        extends RuntimeException {

    public InvalidTransferException(String message) {
        super(message);
    }
}


