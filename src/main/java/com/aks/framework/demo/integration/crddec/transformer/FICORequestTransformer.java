package com.lbg.rsk.cdp.demo.integration.crddec.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.apiexecutor.APIFlowData;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.RequestOverrideTransformer;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component(value = "ficoRequestTransformer")
public class FICORequestTransformer implements RequestOverrideTransformer {

  @Override
  public JsonNode transformWithRequestAndResponse(Map requestHeaders, APIFlowData apiFlowData) {
    return apiFlowData.getRequest().get("payload");
  }
}
