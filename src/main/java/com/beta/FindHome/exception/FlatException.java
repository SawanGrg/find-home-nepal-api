package com.beta.FindHome.exception;

public class FlatException extends RuntimeException {
    public FlatException(String message) {
        super(message);
    }
    public FlatException(String message, Throwable cause) {
        super(message, cause);
    }
}
