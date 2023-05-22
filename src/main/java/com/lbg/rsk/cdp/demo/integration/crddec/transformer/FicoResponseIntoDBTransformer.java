package com.lbg.rsk.cdp.demo.integration.crddec.transformer;

import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.apiexecutor.APIFlowData;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.RequestOverrideTransformer;
import com.lbg.rsk.cdp.dataflow.base.MapperUtils;
import com.lbg.rsk.cdp.demo.integration.crddec.entity.DecisionResponseEntity;
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
