package com.beta.FindHome.exception;


public class AssetException extends RuntimeException {
    public AssetException(String message) {
        super(message);
    }

    public AssetException(String message, Throwable cause) {
        super(message, cause);
    }
}
