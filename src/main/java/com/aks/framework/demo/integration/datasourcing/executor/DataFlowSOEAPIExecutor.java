package com.aks.framework.demo.integration.datasourcing.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.aks.framework.rdi.annotations.DataFlowAPIExecutor;
import com.aks.framework.rdi.apiexecutor.AbstractAPIExecutor;
import com.aks.framework.rdi.apiexecutor.ConcurrentAPIExecutorFlow;
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
