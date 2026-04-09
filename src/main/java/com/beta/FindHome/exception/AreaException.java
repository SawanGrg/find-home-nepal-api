package com.beta.FindHome.exception;

public class AreaException extends RuntimeException {
    public AreaException(String message) {
        super(message);
    }

    // Add this new constructor while keeping the existing one
    public AreaException(String message, Throwable cause) {
        super(message, cause);
    }
}
