package com.my.goldmanager.service.exception;

public class InvalidAlgorithmException extends Exception {

	private static final long serialVersionUID = 2847549380508642010L;

	public InvalidAlgorithmException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidAlgorithmException(String message) {
		super(message);
	}

}
