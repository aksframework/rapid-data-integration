package com.lbg.rsk.cdp.dataflow.retry.base;

import com.lbg.rsk.cdp.dataflow.base.DataFlowConfig;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants;
import java.util.function.Supplier;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

/**
 * The type AbstractRequestRetryAdvice.
 *
 * @param <T> the type parameter
 */
public abstract class AbstractRequestRetryAdvice<T extends String>
    implements RequestRetryAdvice<T> {
  /** The Retry listeners. */
  protected RetryListener[] retryListeners;
  /** The Back off policy. */
  protected BackOffPolicy backOffPolicy;

  /** The Retry template. */
  protected RetryTemplate retryTemplate;

  /** The Retry policy. */
  protected RetryPolicy retryPolicy;

  /** The Data flow config. */
  protected DataFlowConfig dataFlowConfig;

  /**
   * Instantiates a new Abstract request retry advice.
   *
   * @param dataFlowConfig the data flow config
   */
  public AbstractRequestRetryAdvice(DataFlowConfig dataFlowConfig) {
    this.dataFlowConfig = dataFlowConfig;
  }

  /**
   * Build advice.
   *
   * @param dataFlowName the data flow enum
   */
  public abstract void buildAdvice(T dataFlowName);

  @Override
  public RequestHandlerRetryAdvice get(T dataFlowName) {
    buildAdvice(dataFlowName);
    Assert.notNull(retryTemplate, "'RetryTemplate' not defined");

    if (null != retryPolicy) retryTemplate.setRetryPolicy(retryPolicy);
    if (null != backOffPolicy) retryTemplate.setBackOffPolicy(backOffPolicy);
    if (null != retryListeners) retryTemplate.setListeners(retryListeners);

    Supplier<RetryListenerSupport> retryListenerSupportSupplier =
        () ->
            new RetryListenerSupport() {
              @Override
              public <T, E extends Throwable> boolean open(
                  RetryContext context, RetryCallback<T, E> callback) {
                context.setAttribute(
                    DataFlowConstants.RETRY_PROFILE,
                    dataFlowConfig.getAPIExecutorConfig(dataFlowName).getRetryProfile());
                context.setAttribute(DataFlowConstants.DATA_FLOW_HEADER_NAME, dataFlowName);
                return super.open(context, callback);
              }
            };

    retryTemplate.registerListener(retryListenerSupportSupplier.get(), 0);

    RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();
    requestHandlerRetryAdvice.setRetryTemplate(retryTemplate);

    return requestHandlerRetryAdvice;
  }

  /**
   * Sets retry listeners.
   *
   * @param retryListeners the retry listeners
   */
  protected void setRetryListeners(RetryListener[] retryListeners) {
    this.retryListeners = retryListeners;
  }

  /**
   * Sets back off policy.
   *
   * @param backOffPolicy the back off policy
   */
  protected void setBackOffPolicy(BackOffPolicy backOffPolicy) {
    this.backOffPolicy = backOffPolicy;
  }

  /**
   * Sets retry template.
   *
   * @param retryTemplate the retry template
   */
  protected void setRetryTemplate(RetryTemplate retryTemplate) {
    this.retryTemplate = retryTemplate;
  }

  /**
   * Sets retry policy.
   *
   * @param retryPolicy the retry policy
   */
  protected void setRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
  }
}
