package com.aks.framework.rdi.specExecutor;

import com.aks.framework.rdi.base.ApplicationConstants;
import com.aks.framework.rdi.base.RDIUtils;
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
        .from(RDIUtils.createChannel(dataFlowName, ApplicationConstants.SPEC_EXECUTOR_CHANNEL))
        .executeRequest()
        .bridge();
  }
}
