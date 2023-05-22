package com.lbg.rsk.cdp.dataflow.dbexecutor;

import com.lbg.rsk.cdp.dataflow.apiexecutor.AbstractBaseExecutorFlow;
import com.lbg.rsk.cdp.dataflow.base.DataFlowBaseExecutor;

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
