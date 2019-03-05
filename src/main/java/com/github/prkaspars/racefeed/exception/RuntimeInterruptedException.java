package com.github.prkaspars.racefeed.exception;

public class RuntimeInterruptedException extends RuntimeException {

  public RuntimeInterruptedException(String message) {
    super(message);
  }

  public RuntimeInterruptedException(String message, Throwable cause) {
    super(message, cause);
  }
}
