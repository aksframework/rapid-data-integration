package com.lbg.rsk.cdp.dataflow.apiexecutor;

import static com.lbg.rsk.cdp.dataflow.base.DataFlowUtils.createChannel;

import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.dsl.IntegrationFlowDefinition;

/** The type Default api executor flow. */
@Slf4j
public class DefaultAPIExecutorFlow extends AbstractAPIExecutorFlow {
  /**
   * Instantiates a new Default api executor flow.
   *
   * @param apiExecutor the api executor
   * @param dataFlowName the data flow
   */
  public DefaultAPIExecutorFlow(APIExecutor apiExecutor, String dataFlowName) {
    super(apiExecutor, dataFlowName);
  }

  @Override
  public IntegrationFlowDefinition<?> buildFlow() {
    return new APIExecutorDefinition(dataFlowBaseExecutor, dataFlowName)
        .from(createChannel(dataFlowName, DataFlowConstants.API_EXECUTOR_CHANNEL))
        .transformRequest()
        .executeRequest()
        .transformResponse()
        .bridge();
  }
}
