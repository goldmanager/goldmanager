package com.my.goldmanager.service.exception;

public class DuplicateItemException extends RuntimeException {

	private static final long serialVersionUID = 1942277383885269496L;

	public DuplicateItemException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateItemException(String message) {
		super(message);
	}

}
