package com.example.finance.exception;

public class DuplicateCustomerException extends RuntimeException {

    public DuplicateCustomerException(String email) {
        super("Customer already exists with email: " + email);
    }
}