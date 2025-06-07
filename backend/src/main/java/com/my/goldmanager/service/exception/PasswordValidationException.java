package com.my.goldmanager.service.exception;

public class PasswordValidationException extends Exception {

	private static final long serialVersionUID = 3078802833848354118L;

	public PasswordValidationException(String message, Throwable cause) {
		super(message, cause);

	}

	public PasswordValidationException(String message) {
		super(message);
	}


}
