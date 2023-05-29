package com.aks.framework.demo.integration.crddec.executor;

import com.aks.framework.demo.integration.crddec.entity.DecisionResponseEntity;
import com.aks.framework.demo.integration.crddec.entity.DecisionResponseRepository;
import com.aks.framework.rdi.annotations.DataFlowDBExecutor;
import com.aks.framework.rdi.annotations.ResponseTransformer;
import com.aks.framework.rdi.dbexecutor.AbstractDBExecutor;
import com.aks.framework.rdi.dbexecutor.DefaultDBExecutorFlow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
