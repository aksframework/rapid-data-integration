package com.aks.framework.demo.integration.orch.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aks.framework.rdi.annotations.DataFlowDBExecutor;
import com.aks.framework.rdi.annotations.ResponseTransformer;
import com.aks.framework.rdi.dbexecutor.AbstractDBExecutor;
import com.aks.framework.rdi.dbexecutor.DefaultDBExecutorFlow;
import com.aks.framework.demo.integration.orch.entity.CallerResponseEntity;
import com.aks.framework.demo.integration.orch.entity.CallerResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DataFlowDBExecutor(
    name = "callerresponse",
    type = DefaultDBExecutorFlow.class,
    entity = CallerResponseEntity.class,
    repository = CallerResponseRepository.class)
public class CallerResponseDBExecutor extends AbstractDBExecutor {

  @Autowired ObjectMapper objectMapper;

  @ResponseTransformer
  public JsonNode transformResponse(CallerResponseEntity callerResponseEntity)
      throws JsonProcessingException {
    return objectMapper.readValue(callerResponseEntity.getPayload(), JsonNode.class);
  }
}
