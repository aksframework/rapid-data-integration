package com.aks.framework.rdi.apiexecutor.custom;

import com.aks.framework.rdi.retry.base.RequestRetryAdvice;

/** The interface Add request retry advice. */
public interface AddRequestRetryAdvice {
  /**
   * Configure request retry advice request retry advice.
   *
   * @return the request retry advice
   */
  RequestRetryAdvice<String> configureRequestRetryAdvice();
}
