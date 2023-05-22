package com.lbg.rsk.cdp.dataflow.annotations;

import com.lbg.rsk.cdp.dataflow.dbexecutor.AbstractDBExecutorFlow;
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
