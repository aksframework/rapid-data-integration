package com.lbg.rsk.cdp.dataflow.execption;

public class InternalServerErrorException extends DataFlowException {
  private static final long serialVersionUID = 1L;

  public InternalServerErrorException(String message) {
    super(HttpStatus.SC_INTERNAL_SERVER_ERROR, message);
  }

  public InternalServerErrorException(Throwable e) {
    super(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.toString(), e, null);
  }

  public InternalServerErrorException(String message, Throwable e) {
    super(HttpStatus.SC_INTERNAL_SERVER_ERROR, message, e, null);
  }
}
