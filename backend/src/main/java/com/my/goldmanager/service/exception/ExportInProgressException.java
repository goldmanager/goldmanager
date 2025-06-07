package com.my.goldmanager.service.exception;

public class ExportInProgressException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ExportInProgressException(String message) {
        super(message);
    }
}
