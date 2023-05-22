package com.aks.framework.demo.integration.crddec.transformer;

import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.aks.framework.rdi.apiexecutor.APIFlowData;
import com.aks.framework.rdi.apiexecutor.custom.RequestOverrideTransformer;
import com.aks.framework.rdi.base.MapperUtils;
import com.aks.framework.demo.integration.crddec.entity.DecisionResponseEntity;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component(value = "ficoResponseForDSOR")
public class FicoResponseIntoDBTransformer implements RequestOverrideTransformer {

  @Override
  public JsonNode transformWithRequestAndResponse(Map requestHeaders, APIFlowData apiFlowData) {
    JsonNode payload = apiFlowData.getRequest();
    DecisionResponseEntity decisionResponseEntity = new DecisionResponseEntity();
    decisionResponseEntity.setCrdRqsId(requestHeaders.get("cerdos-id").toString());
    decisionResponseEntity.setApplicationCustomerId(payload.get("applicationCustomerId").asText());
    decisionResponseEntity.setCustomerSegmentType(payload.get("customerSegmentType").asText());
    decisionResponseEntity.setStrategyType(payload.get("strategyType").asText());
    decisionResponseEntity.setChannelType(payload.get("channelType").asText());
    decisionResponseEntity.setDbRequestType(payload.get("dbRequestType").asText());
    decisionResponseEntity.setPayload(JsonUtils.toJsonString(apiFlowData.getResponse()));

    return MapperUtils.convertToJson(decisionResponseEntity);
  }
}
