package com.aks.framework.rdi.specExecutor;

import com.aks.framework.rdi.apiexecutor.AbstractBaseExecutorFlow;
import com.aks.framework.rdi.base.DataFlowBaseExecutor;

public abstract class AbstractSpecExecutorFlow extends AbstractBaseExecutorFlow {

  /**
   * Instantiates a new Abstract spec executor flow.
   *
   * @param dataFlowBaseExecutor the spec executor
   * @param dataFlowName the data flow
   */
  public AbstractSpecExecutorFlow(DataFlowBaseExecutor dataFlowBaseExecutor, String dataFlowName) {
    super(dataFlowBaseExecutor, dataFlowName);
  }
}
