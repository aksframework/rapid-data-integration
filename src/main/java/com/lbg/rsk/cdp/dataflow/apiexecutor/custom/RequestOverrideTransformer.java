package com.lbg.rsk.cdp.dataflow.apiexecutor.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.apiexecutor.APIFlowData;
import java.util.Map;
import org.springframework.messaging.handler.annotation.Headers;

public interface RequestOverrideTransformer {
  JsonNode transformWithRequestAndResponse(@Headers Map requestHeaders, APIFlowData apiFlowData);
}
