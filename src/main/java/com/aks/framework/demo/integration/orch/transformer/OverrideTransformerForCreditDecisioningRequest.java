package com.aks.framework.demo.integration.orch.transformer;

import com.aks.framework.demo.integration.orch.entity.CreditDecisionRequest;
import com.aks.framework.rdi.apiexecutor.APIFlowData;
import com.aks.framework.rdi.apiexecutor.custom.RequestOverrideTransformer;
import com.aks.framework.rdi.base.MapperUtils;
import com.fasterxml.jackson.databind.JsonNode;
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
