package com.aks.framework.rdi.specExecutor;

import static com.aks.framework.rdi.base.DataFlowUtils.createChannel;

import com.aks.framework.rdi.base.DataFlowConstants;
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
