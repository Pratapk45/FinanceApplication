package com.example.finance.exception;

public class LoanNotFoundException extends RuntimeException {

	public LoanNotFoundException(String message) {
		super(message);
	}
}
