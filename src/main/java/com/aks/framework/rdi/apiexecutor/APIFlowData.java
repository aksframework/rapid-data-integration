package com.aks.framework.rdi.apiexecutor;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class APIFlowData {
  private JsonNode request;
  private JsonNode response;
}
