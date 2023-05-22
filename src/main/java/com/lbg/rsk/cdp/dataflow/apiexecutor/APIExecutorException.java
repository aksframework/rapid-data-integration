package com.lbg.rsk.cdp.dataflow.apiexecutor;

import com.lbg.rsk.cdp.dataflow.execption.InternalServerErrorException;

/** The type Api executor exception. */
public class APIExecutorException extends InternalServerErrorException {
  public APIExecutorException(String errorMessage) {
    super(errorMessage);
  }

  public APIExecutorException(String message, Throwable e) {
    super(message, e);
  }
}
