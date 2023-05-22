package com.lbg.rsk.cdp.demo.integration.datasourcing.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.annotations.DataFlowAPIExecutor;
import com.lbg.rsk.cdp.dataflow.apiexecutor.AbstractAPIExecutor;
import com.lbg.rsk.cdp.dataflow.apiexecutor.ConcurrentAPIExecutorFlow;
import java.util.List;
import org.springframework.integration.annotation.Splitter;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "soe", type = ConcurrentAPIExecutorFlow.class)
public class DataFlowSOEAPIExecutor extends AbstractAPIExecutor {
  @Splitter
  public List<String> splitSOEPayload(JsonNode payload) {
    return payload.findValuesAsText("EntityId");
  }
}
