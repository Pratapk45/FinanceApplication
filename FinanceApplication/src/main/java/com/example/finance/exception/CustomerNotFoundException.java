package com.example.finance.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long id) {
        super("Customer not found with ID: " + id);
    }
	public CustomerNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
    
    
}