package com.aks.framework.demo.integration.crddec;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;

@MessagingGateway
public interface CreditDecisioningGateway {
  @Gateway(requestChannel = "creditDecisioningRequest.input")
  Object processRequest(@Payload Message message);
}
