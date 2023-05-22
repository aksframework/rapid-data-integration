package com.lbg.rsk.cdp.dataflow.specExecutor;

import static com.lbg.rsk.cdp.dataflow.base.DataFlowUtils.createChannel;

import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants;
import org.springframework.integration.dsl.IntegrationFlowDefinition;

public class DefaultSpecExecutorFlow extends AbstractSpecExecutorFlow {
  /**
   * Instantiates a new Default spec executor flow.
   *
   * @param specExecutor the spec executor
   * @param dataFlowName the data flow
   */
  public DefaultSpecExecutorFlow(SpecExecutor specExecutor, String dataFlowName) {
    super(specExecutor, dataFlowName);
  }

  @Override
  public IntegrationFlowDefinition<?> buildFlow() {
    return new SpecExecutorDefinition(dataFlowBaseExecutor, dataFlowName)
        .from(createChannel(dataFlowName, DataFlowConstants.SPEC_EXECUTOR_CHANNEL))
        .executeRequest()
        .bridge();
  }
}
