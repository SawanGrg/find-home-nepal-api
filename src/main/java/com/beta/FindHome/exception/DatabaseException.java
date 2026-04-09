package com.beta.FindHome.exception;

public class DatabaseException extends RuntimeException {
    //    message means the error message
    public DatabaseException(String message) {
        super(message);
    }

    //    message means the error message
    //    cause means the exception that caused this exception
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
