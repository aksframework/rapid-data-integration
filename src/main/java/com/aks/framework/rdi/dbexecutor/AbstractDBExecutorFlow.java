package com.aks.framework.rdi.dbexecutor;

import com.aks.framework.rdi.apiexecutor.AbstractBaseExecutorFlow;
import com.aks.framework.rdi.base.DataFlowBaseExecutor;

public abstract class AbstractDBExecutorFlow extends AbstractBaseExecutorFlow {

  /**
   * Instantiates a new Abstract api executor flow.
   *
   * @param dataFlowBaseExecutor the api executor
   * @param dataFlowName the data flow
   */
  public AbstractDBExecutorFlow(DataFlowBaseExecutor dataFlowBaseExecutor, String dataFlowName) {
    super(dataFlowBaseExecutor, dataFlowName);
  }
}
