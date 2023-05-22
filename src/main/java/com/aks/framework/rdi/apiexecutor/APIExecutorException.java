package com.aks.framework.rdi.apiexecutor;

import com.aks.framework.rdi.execption.InternalServerErrorException;

/** The type Api executor exception. */
public class APIExecutorException extends InternalServerErrorException {
  public APIExecutorException(String errorMessage) {
    super(errorMessage);
  }

  public APIExecutorException(String message, Throwable e) {
    super(message, e);
  }
}
