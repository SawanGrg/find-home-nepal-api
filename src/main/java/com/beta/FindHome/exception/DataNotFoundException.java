package com.beta.FindHome.exception;

public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String message) {
        super(message);
    }

    // Add this new constructor while keeping the existing one
    public DataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}