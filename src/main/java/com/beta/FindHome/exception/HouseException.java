package com.beta.FindHome.exception;

public class HouseException extends RuntimeException {
    public HouseException(String message) {
        super(message);
    }
    public HouseException(String message, Throwable cause) {
        super(message, cause);
    }
}
