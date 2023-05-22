package com.lbg.rsk.cdp.dataflow.specExecutor;

import com.lbg.rsk.cdp.dataflow.apiexecutor.AbstractBaseExecutorFlow;
import com.lbg.rsk.cdp.dataflow.base.DataFlowBaseExecutor;

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
