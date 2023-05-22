package com.lbg.rsk.cdp.demo.integration.orch.transformer;

import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.apiexecutor.APIFlowData;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.RequestOverrideTransformer;
import com.lbg.rsk.cdp.dataflow.base.MapperUtils;
import com.lbg.rsk.cdp.demo.integration.orch.entity.CallerResponseEntity;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CreditDecisionResponseInDsorTransformer implements RequestOverrideTransformer {

  @Override
  public JsonNode transformWithRequestAndResponse(Map requestHeaders, APIFlowData apiFlowData) {
    JsonNode payload = apiFlowData.getRequest();
    CallerResponseEntity callerResponseEntity = new CallerResponseEntity();
    callerResponseEntity.setCrdRqsId(requestHeaders.get("cerdos-id").toString());
    callerResponseEntity.setApplicationCustomerId(payload.findValuesAsText("EntityId").get(0));
    callerResponseEntity.setCustomerSegmentType("SME");
    callerResponseEntity.setStrategyType(payload.get("RequestType").asText());
    callerResponseEntity.setChannelType(payload.get("ConsumingApplicationChannelTypeID").asText());
    callerResponseEntity.setDbRequestType("ficoRequest");
    callerResponseEntity.setPayload(JsonUtils.toJsonString(apiFlowData.getResponse()));

    return MapperUtils.convertToJson(callerResponseEntity);
  }
}
