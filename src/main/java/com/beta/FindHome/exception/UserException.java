package com.beta.FindHome.exception;

public class UserException extends RuntimeException {
    //    message means the error message
    public UserException(String message) {
        super(message);
    }

    //    message means the error message
    //    cause means the exception that caused this exception
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}
