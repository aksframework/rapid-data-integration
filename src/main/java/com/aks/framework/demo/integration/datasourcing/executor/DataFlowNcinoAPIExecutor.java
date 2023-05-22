package com.lbg.rsk.cdp.demo.integration.datasourcing.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.annotations.DataFlowAPIExecutor;
import com.lbg.rsk.cdp.dataflow.annotations.RequestTransformer;
import com.lbg.rsk.cdp.dataflow.apiexecutor.AbstractAPIExecutor;
import com.lbg.rsk.cdp.dataflow.apiexecutor.DefaultAPIExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "ncino", type = DefaultAPIExecutorFlow.class)
public class DataFlowNcinoAPIExecutor extends AbstractAPIExecutor {
  @RequestTransformer
  public JsonNode transformRequest(JsonNode jsonNode) {
    return jsonNode.get("ProposalID");
  }
}
