package com.lbg.rsk.cdp.demo.integration.crddec.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lbg.rsk.cdp.dataflow.annotations.DataFlowDBExecutor;
import com.lbg.rsk.cdp.dataflow.annotations.ResponseTransformer;
import com.lbg.rsk.cdp.dataflow.dbexecutor.AbstractDBExecutor;
import com.lbg.rsk.cdp.dataflow.dbexecutor.DefaultDBExecutorFlow;
import com.lbg.rsk.cdp.demo.integration.crddec.entity.DecisionResponseEntity;
import com.lbg.rsk.cdp.demo.integration.crddec.entity.DecisionResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DataFlowDBExecutor(
    name = "decisionResponseInDSOR",
    type = DefaultDBExecutorFlow.class,
    entity = DecisionResponseEntity.class,
    repository = DecisionResponseRepository.class)
public class DecisionResponseDBExecutor extends AbstractDBExecutor {

  @Autowired private ObjectMapper objectMapper;

  @ResponseTransformer
  public JsonNode transformResponse(DecisionResponseEntity decisionResponseEntity)
      throws JsonProcessingException {
    return objectMapper.readValue(decisionResponseEntity.getPayload(), JsonNode.class);
  }
}
