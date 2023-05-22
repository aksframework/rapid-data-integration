package com.lbg.rsk.cdp.dataflow.apiexecutor.custom;

import com.lbg.rsk.cdp.dataflow.retry.base.RequestRetryAdvice;

/** The interface Add request retry advice. */
public interface AddRequestRetryAdvice {
  /**
   * Configure request retry advice request retry advice.
   *
   * @return the request retry advice
   */
  RequestRetryAdvice<String> configureRequestRetryAdvice();
}
