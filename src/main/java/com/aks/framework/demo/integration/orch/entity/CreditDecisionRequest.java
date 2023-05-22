package com.aks.framework.demo.integration.orch.entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreditDecisionRequest {
  private String crdRqsId;

  private String applicationCustomerId;

  private String customerSegmentType;

  private String strategyType;

  private String dbRequestType;

  private String channelType;
  private JsonNode payload;
}
