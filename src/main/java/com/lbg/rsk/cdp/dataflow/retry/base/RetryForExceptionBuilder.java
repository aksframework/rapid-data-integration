package com.lbg.rsk.cdp.dataflow.retry.base;

import com.lbg.rsk.cdp.dataflow.retry.RetryForExceptionEnum;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** The type RetryForExceptionBuilder. */
public class RetryForExceptionBuilder {
  /** The Enum set. */
  private Set<Class<? extends Throwable>> enumSet;

  /**
   * Add retry for exception builder.
   *
   * @param exceptionEnum the exception enum
   * @return the retry for exception builder
   */
  public RetryForExceptionBuilder add(RetryForExceptionEnum exceptionEnum) {
    if (null == enumSet) {
      enumSet = new HashSet<>();
    }
    enumSet.add(exceptionEnum.getException());
    return this;
  }

  /**
   * Build map.
   *
   * @return the map
   */
  public Map<Class<? extends Throwable>, Boolean> build() {
    return enumSet.stream().collect(Collectors.toMap(k -> k, v -> Boolean.TRUE));
  }
}
