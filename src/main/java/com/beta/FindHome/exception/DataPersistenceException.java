package com.beta.FindHome.exception;
/**
 * Exception thrown when data persistence operations fail
 */
public class DataPersistenceException extends RuntimeException {

  public DataPersistenceException(String message) {
    super(message);
  }

  public DataPersistenceException(String message, Throwable cause) {
    super(message, cause);
  }
}
