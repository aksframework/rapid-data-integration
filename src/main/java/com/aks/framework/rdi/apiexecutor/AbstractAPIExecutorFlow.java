package com.lbg.rsk.cdp.dataflow.apiexecutor;

import com.lbg.rsk.cdp.dataflow.base.DataFlowBaseExecutor;

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
