package com.aks.framework.demo.integration.orch;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;

@MessagingGateway
public interface OrchestrationGateway {
  @Gateway(requestChannel = "orchestrationRequest.input")
  Object processRequest(@Payload Message message);
}
