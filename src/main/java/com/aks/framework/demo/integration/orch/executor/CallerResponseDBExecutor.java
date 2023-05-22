package com.lbg.rsk.cdp.demo.integration.orch.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lbg.rsk.cdp.dataflow.annotations.DataFlowDBExecutor;
import com.lbg.rsk.cdp.dataflow.annotations.ResponseTransformer;
import com.lbg.rsk.cdp.dataflow.dbexecutor.AbstractDBExecutor;
import com.lbg.rsk.cdp.dataflow.dbexecutor.DefaultDBExecutorFlow;
import com.lbg.rsk.cdp.demo.integration.orch.entity.CallerResponseEntity;
import com.lbg.rsk.cdp.demo.integration.orch.entity.CallerResponseRepository;
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
