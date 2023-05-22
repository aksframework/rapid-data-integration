package com.aks.framework.rdi.annotations;

import com.aks.framework.rdi.apiexecutor.AbstractAPIExecutorFlow;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The interface Data flow api executor. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataFlowAPIExecutor {
  /**
   * Type class.
   *
   * @return the class
   */
  Class<? extends AbstractAPIExecutorFlow> type();
  /**
   * Name data flow enum.
   *
   * @return the data flow enum
   */
  String name();
}
