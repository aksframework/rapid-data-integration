package com.aks.framework.rdi.apiexecutor.custom;

import com.aks.framework.rdi.apiexecutor.APIFlowData;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.messaging.handler.annotation.Headers;

public interface RequestOverrideTransformer {
  JsonNode transformWithRequestAndResponse(@Headers Map requestHeaders, APIFlowData apiFlowData);
}
