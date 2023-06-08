package com.aks.framework.rdi.retry.base;

import com.aks.framework.rdi.base.ApplicationConstants;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;

/** The type DefaultRetryListener. */
@Slf4j
public class DefaultRetryListener extends RetryListenerSupport {

  /** The Retry for exceptions. */
  private Set<Class<? extends Throwable>> retryForExceptions;

  /**
   * Instantiates a new Default retry listener.
   *
   * @param retryForExceptions the retry for exceptions
   */
  public DefaultRetryListener(Set<Class<? extends Throwable>> retryForExceptions) {
    this.retryForExceptions = retryForExceptions;
  }

  @Override
  public <T, E extends Throwable> void onError(
      RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
    if (retryForExceptions.contains(throwable.getClass())
        || retryForExceptions.contains(Exception.class)) {
      if (retryForExceptions.contains(throwable.getClass())) {
        log.error(
            "Retry attempt[{}] profile[{}] data-flow[{}] for exception[{}]",
            context.getRetryCount(),
            context.getAttribute(ApplicationConstants.RETRY_PROFILE),
            context.getAttribute(ApplicationConstants.DATA_FLOW_HEADER_NAME),
            throwable.getClass().getCanonicalName());
      } else {
        log.error(
            "Retry attempt [{}] profile[{}] data-flow[{}] for exception[{}] actual exception[{}] stacktrace[{}]",
            context.getRetryCount(),
            context.getAttribute(ApplicationConstants.RETRY_PROFILE),
            context.getAttribute(ApplicationConstants.DATA_FLOW_HEADER_NAME),
            Exception.class.getCanonicalName(),
            throwable.getClass().getCanonicalName(),
            throwable.getMessage());
      }
    } else {
      log.error(
          "Retry profile[{}] data-flow[{}] not applicable for caught exception[{}]  retry-able exceptions{} stacktrace[{}]",
          context.getAttribute(ApplicationConstants.RETRY_PROFILE),
          context.getAttribute(ApplicationConstants.DATA_FLOW_HEADER_NAME),
          throwable.getClass().getCanonicalName(),
          retryForExceptions.toString(),
          throwable.getMessage());
    }
  }

  @Override
  public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {

    return super.open(context, callback);
  }
}
