package com.aks.framework.rdi.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The interface Data flow gatherer. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataFlowGatherer {
  /**
   * Template data gatherer template enum.
   *
   * @return the data gatherer template enum
   */
  String template();
}
