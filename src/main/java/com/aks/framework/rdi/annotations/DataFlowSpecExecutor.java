package com.aks.framework.rdi.annotations;

import com.aks.framework.rdi.specExecutor.AbstractSpecExecutorFlow;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataFlowSpecExecutor {
  String name();

  Class<? extends AbstractSpecExecutorFlow> type();
}
