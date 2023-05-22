package com.aks.framework.demo.integration.datasourcing;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;

@MessagingGateway
public interface DataFlowDataSourcingGateway {
  @Gateway(requestChannel = "dataSourcingRequest.input")
  Object processRequest(@Payload Message message);
}
