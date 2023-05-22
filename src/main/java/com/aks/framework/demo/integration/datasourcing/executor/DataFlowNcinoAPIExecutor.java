package com.aks.framework.demo.integration.datasourcing.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.aks.framework.rdi.annotations.DataFlowAPIExecutor;
import com.aks.framework.rdi.annotations.RequestTransformer;
import com.aks.framework.rdi.apiexecutor.AbstractAPIExecutor;
import com.aks.framework.rdi.apiexecutor.DefaultAPIExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "ncino", type = DefaultAPIExecutorFlow.class)
public class DataFlowNcinoAPIExecutor extends AbstractAPIExecutor {
  @RequestTransformer
  public JsonNode transformRequest(JsonNode jsonNode) {
    return jsonNode.get("ProposalID");
  }
}
