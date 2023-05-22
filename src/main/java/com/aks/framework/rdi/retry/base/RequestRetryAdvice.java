package com.lbg.rsk.cdp.dataflow.retry.base;

import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;

/**
 * The interface RequestRetryAdvice.
 *
 * @param <T> the type parameter
 */
public interface RequestRetryAdvice<T extends String> {
  /**
   * Get request handler retry advice.
   *
   * @param dataFlowEnum the data flow enum
   * @return the request handler retry advice
   */
  RequestHandlerRetryAdvice get(T dataFlowEnum);
}
