package com.example.finance.exception;

public class UnsupportedTransactionTypeException extends RuntimeException {

	public UnsupportedTransactionTypeException(String message) {
		super(message);
	}
}
