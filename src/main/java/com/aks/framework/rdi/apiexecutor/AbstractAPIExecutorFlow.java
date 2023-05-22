package com.aks.framework.rdi.apiexecutor;

import com.aks.framework.rdi.base.DataFlowBaseExecutor;

public abstract class AbstractAPIExecutorFlow extends AbstractBaseExecutorFlow {

  /**
   * Instantiates a new Abstract api executor flow.
   *
   * @param dataFlowBaseExecutor the api executor
   * @param dataFlowName the data flow
   */
  public AbstractAPIExecutorFlow(DataFlowBaseExecutor dataFlowBaseExecutor, String dataFlowName) {
    super(dataFlowBaseExecutor, dataFlowName);
  }
}
