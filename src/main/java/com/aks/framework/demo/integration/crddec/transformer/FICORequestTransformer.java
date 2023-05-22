package com.aks.framework.demo.integration.crddec.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.aks.framework.rdi.apiexecutor.APIFlowData;
import com.aks.framework.rdi.apiexecutor.custom.RequestOverrideTransformer;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component(value = "ficoRequestTransformer")
public class FICORequestTransformer implements RequestOverrideTransformer {

  @Override
  public JsonNode transformWithRequestAndResponse(Map requestHeaders, APIFlowData apiFlowData) {
    return apiFlowData.getRequest().get("payload");
  }
}
