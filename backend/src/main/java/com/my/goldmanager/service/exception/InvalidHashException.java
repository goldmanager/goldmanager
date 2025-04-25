package com.my.goldmanager.service.exception;

public class InvalidHashException extends Exception {

	private static final long serialVersionUID = 5690666423810347925L;

	public InvalidHashException(String message, Throwable cause) {
		super(message, cause);

	}

	public InvalidHashException(String message) {
		super(message);

	}

}
