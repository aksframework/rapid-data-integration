package com.aks.framework.rdi.apiexecutor;

import com.aks.framework.rdi.base.DataFlowConstants;
import com.aks.framework.rdi.base.DataFlowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.dsl.IntegrationFlowDefinition;

/** The type Concurrent api executor flow. */
@Slf4j
public class ConcurrentAPIExecutorFlow extends AbstractAPIExecutorFlow {

  /**
   * Instantiates a new Concurrent api executor flow.
   *
   * @param apiExecutor the api executor
   * @param dataFlowName the data flow
   */
  public ConcurrentAPIExecutorFlow(APIExecutor apiExecutor, String dataFlowName) {
    super(apiExecutor, dataFlowName);
  }

  @Override
  public IntegrationFlowDefinition<?> buildFlow() {
    return new APIExecutorDefinition(dataFlowBaseExecutor, dataFlowName)
        .from(DataFlowUtils.createChannel(dataFlowName, DataFlowConstants.API_EXECUTOR_CHANNEL))
        .addConcurrency()
        .transformRequest()
        .executeRequest()
        .transformResponse()
        .resequence()
        .aggregateConcurrentResponse();
  }
}
