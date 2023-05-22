package com.aks.framework.rdi.annotations;

import com.aks.framework.rdi.dbexecutor.AbstractDBExecutorFlow;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.data.repository.CrudRepository;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataFlowDBExecutor {
  /**
   * Type class.
   *
   * @return the class
   */
  Class<? extends AbstractDBExecutorFlow> type();
  /**
   * Name data flow enum.
   *
   * @return the data flow enum
   */
  String name();

  Class<? extends Object> entity();

  Class<? extends CrudRepository> repository();
}
