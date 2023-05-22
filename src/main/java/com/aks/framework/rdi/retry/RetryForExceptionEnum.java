package com.aks.framework.rdi.retry;

import java.io.IOException;
import org.springframework.messaging.MessagingException;

/** The enum RetryForExceptionEnum. */
public enum RetryForExceptionEnum {
  /** Message exception retry for exception enum. */
  MessageException(MessagingException.class),
  /** Generic exception retry for exception enum. */
  GenericException(Exception.class),
  /** Io exception retry for exception enum. */
  IOException(IOException.class);

  /** The Throwable. */
  Class<? extends Throwable> throwable;

  /**
   * Instantiates a new Retry for exception enum.
   *
   * @param throwable the throwable
   */
  RetryForExceptionEnum(Class<? extends Throwable> throwable) {
    this.throwable = throwable;
  }

  /**
   * Gets exception.
   *
   * @return the exception
   */
  public Class<? extends Throwable> getException() {
    return this.throwable;
  }
}
