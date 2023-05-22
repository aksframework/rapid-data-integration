package com.lbg.rsk.cdp.demo.integration.orch.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.apiexecutor.APIFlowData;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.RequestOverrideTransformer;
import com.lbg.rsk.cdp.dataflow.base.MapperUtils;
import com.lbg.rsk.cdp.demo.integration.orch.entity.CreditDecisionRequest;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OverrideTransformerForCreditDecisioningRequest implements RequestOverrideTransformer {
  @Override
  public JsonNode transformWithRequestAndResponse(Map requestHeaders, APIFlowData apiFlowData) {
    CreditDecisionRequest creditDecisionRequest = new CreditDecisionRequest();
    JsonNode payload = apiFlowData.getRequest();
    creditDecisionRequest.setCrdRqsId(requestHeaders.get("cerdos-id").toString());
    creditDecisionRequest.setApplicationCustomerId(payload.findValuesAsText("EntityId").get(0));
    creditDecisionRequest.setCustomerSegmentType("SME");
    creditDecisionRequest.setStrategyType(payload.get("RequestType").asText());
    creditDecisionRequest.setChannelType(payload.get("ConsumingApplicationChannelTypeID").asText());
    creditDecisionRequest.setDbRequestType("ficoRequest");
    creditDecisionRequest.setPayload(apiFlowData.getResponse());
    return MapperUtils.convertToJson(creditDecisionRequest);
  }
}
