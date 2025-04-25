package com.my.goldmanager.service.exception;

public class DuplicateItemTypeException extends RuntimeException {

	public DuplicateItemTypeException(String message, Exception e) {
		super(message, e);
	}

	private static final long serialVersionUID = 4140478470607123752L;


}
