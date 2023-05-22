package com.aks.framework.rdi.apiexecutor;

import static com.aks.framework.rdi.base.DataFlowUtils.createChannel;

import com.aks.framework.rdi.base.DataFlowConstants;
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
        .from(createChannel(dataFlowName, DataFlowConstants.API_EXECUTOR_CHANNEL))
        .addConcurrency()
        .transformRequest()
        .executeRequest()
        .transformResponse()
        .resequence()
        .aggregateConcurrentResponse();
  }
}
