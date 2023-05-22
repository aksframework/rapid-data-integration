package com.aks.framework.rdi.apiexecutor.custom;

import com.fasterxml.jackson.databind.JsonNode;
import com.aks.framework.rdi.apiexecutor.APIFlowData;
import java.util.Map;
import org.springframework.messaging.handler.annotation.Headers;

public interface RequestOverrideTransformer {
  JsonNode transformWithRequestAndResponse(@Headers Map requestHeaders, APIFlowData apiFlowData);
}
