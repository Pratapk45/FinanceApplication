package com.example.finance.exception;

public class InvalidTransactionAmountException extends RuntimeException {

	public InvalidTransactionAmountException(String message) {
		super(message);
	}
}
