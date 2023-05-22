package com.aks.framework.rdi.retry;

import com.aks.framework.rdi.base.DataFlowConfig;
import com.aks.framework.rdi.base.DataFlowConfig.RetryData;
import com.aks.framework.rdi.retry.base.AbstractRequestRetryAdvice;
import com.aks.framework.rdi.retry.base.DefaultRetryListener;
import com.aks.framework.rdi.retry.base.RetryForExceptionBuilder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * The type DefaultRequestRetryAdvice.
 *
 * @param <T> the type parameter
 */
public final class DefaultRequestRetryAdvice<T extends String>
    extends AbstractRequestRetryAdvice<T> {
  /** The Retry for exceptions. */
  private Map<Class<? extends Throwable>, Boolean> retryForExceptions;

  /**
   * Instantiates a new Default request retry advice.
   *
   * @param builder the builder
   */
  private DefaultRequestRetryAdvice(DefaultRequestRetryAdviceBuilder builder) {
    super(builder.dataFlowConfig);
    setRetryListeners(builder.retryListeners);
    setRetryPolicy(builder.retryPolicy);
    setRetryTemplate(builder.retryTemplate);
    setBackOffPolicy(builder.backOffPolicy);
    setRetryForExceptions(builder.retryForExceptions);
  }

  @Override
  public void buildAdvice(T dataFlowName) {
    RetryData retryData = dataFlowConfig.getRetryDataByFlowOrDefault(dataFlowName);
    if (null == backOffPolicy) {
      ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
      backOffPolicy.setMaxInterval(retryData.getBackOffMaxInterval());
      backOffPolicy.setInitialInterval(retryData.getBackOffInitialInterval());
      backOffPolicy.setMultiplier(retryData.getBackOffMultiplier());
      setBackOffPolicy(backOffPolicy);
    }

    if (null == retryForExceptions) {
      retryForExceptions =
          new RetryForExceptionBuilder().add(RetryForExceptionEnum.GenericException).build();
    }

    if (null == retryPolicy) {
      setRetryPolicy(new SimpleRetryPolicy(retryData.getMaxAttempt(), retryForExceptions, true));
    }

    if (null == retryTemplate) {
      setRetryTemplate(new RetryTemplate());
    }

    if (null == retryListeners) {
      setRetryListeners(
          new RetryListenerSupport[] {new DefaultRetryListener(retryForExceptions.keySet())});
    }
  }

  @Override
  public void setRetryPolicy(RetryPolicy retryPolicy) {
    super.setRetryPolicy(retryPolicy);
  }

  @Override
  public void setRetryTemplate(RetryTemplate retryTemplate) {
    super.setRetryTemplate(retryTemplate);
  }

  @Override
  public void setBackOffPolicy(BackOffPolicy backOffPolicy) {
    super.setBackOffPolicy(backOffPolicy);
  }

  @Override
  public void setRetryListeners(RetryListener[] retryListeners) {
    super.setRetryListeners(retryListeners);
  }

  /**
   * Sets retry for exceptions.
   *
   * @param retryForExceptions the retry for exceptions
   */
  public void setRetryForExceptions(Map<Class<? extends Throwable>, Boolean> retryForExceptions) {
    this.retryForExceptions = retryForExceptions;
  }

  /**
   * The type DefaultRequestRetryAdviceBuilder.
   *
   * @param <T> the type parameter
   */
  public static class DefaultRequestRetryAdviceBuilder<T> {

    /** The Retry listeners. */
    private RetryListener[] retryListeners;
    /** The Back off policy. */
    private BackOffPolicy backOffPolicy;

    /** The Retry template. */
    private RetryTemplate retryTemplate;

    /** The Retry policy. */
    private RetryPolicy retryPolicy;
    /** The Retry for exceptions. */
    private Map<Class<? extends Throwable>, Boolean> retryForExceptions;

    /** The Data flow config. */
    private DataFlowConfig dataFlowConfig;

    /**
     * Instantiates a new Default request retry advice builder.
     *
     * @param dataFlowConfig the data flow config
     */
    public DefaultRequestRetryAdviceBuilder(DataFlowConfig dataFlowConfig) {
      this.dataFlowConfig = dataFlowConfig;
    }

    /**
     * Retry for exceptions default request retry advice builder.
     *
     * @param exception the exception
     * @return the default request retry advice builder
     */
    public DefaultRequestRetryAdviceBuilder retryForExceptions(
        Map<Class<? extends Throwable>, Boolean> exception) {
      this.retryForExceptions = exception;
      return this;
    }

    /**
     * Add retry for exceptions default request retry advice builder.
     *
     * @param retryForExceptionEnum the retry for exception enum
     * @return the default request retry advice builder
     */
    public DefaultRequestRetryAdviceBuilder addRetryForExceptions(
        RetryForExceptionEnum retryForExceptionEnum) {
      if (this.retryForExceptions == null) {
        this.retryForExceptions = new HashMap<>();
      }
      this.retryForExceptions.put(retryForExceptionEnum.getException(), Boolean.TRUE);
      return this;
    }

    /**
     * Retry listener default request retry advice builder.
     *
     * @param retryListeners the retry listeners
     * @return the default request retry advice builder
     */
    public DefaultRequestRetryAdviceBuilder retryListener(RetryListener[] retryListeners) {
      this.retryListeners = retryListeners;
      return this;
    }

    /**
     * Retry policy default request retry advice builder.
     *
     * @param retryPolicy the retry policy
     * @return the default request retry advice builder
     */
    public DefaultRequestRetryAdviceBuilder retryPolicy(RetryPolicy retryPolicy) {
      this.retryPolicy = retryPolicy;
      return this;
    }

    /**
     * Retry template default request retry advice builder.
     *
     * @param retryTemplate the retry template
     * @return the default request retry advice builder
     */
    public DefaultRequestRetryAdviceBuilder retryTemplate(RetryTemplate retryTemplate) {
      this.retryTemplate = retryTemplate;
      return this;
    }

    /**
     * Back off policy default request retry advice builder.
     *
     * @param backOffPolicy the back off policy
     * @return the default request retry advice builder
     */
    public DefaultRequestRetryAdviceBuilder backOffPolicy(BackOffPolicy backOffPolicy) {
      this.backOffPolicy = backOffPolicy;
      return this;
    }

    /**
     * Build default request retry advice.
     *
     * @return the default request retry advice
     */
    public DefaultRequestRetryAdvice build() {
      if (this.dataFlowConfig == null) {
        throw new IllegalStateException("'DataFlowConfig' can not be 'Null'");
      }
      DefaultRequestRetryAdvice defaultRequestRetryAdvice = new DefaultRequestRetryAdvice<>(this);
      return defaultRequestRetryAdvice;
    }
  }
}
