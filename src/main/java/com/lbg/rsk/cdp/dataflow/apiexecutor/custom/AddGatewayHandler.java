package com.lbg.rsk.cdp.dataflow.apiexecutor.custom;

import org.springframework.integration.dsl.MessageHandlerSpec;
import org.springframework.messaging.Message;

/** The interface Add gateway handler. */
public interface AddGatewayHandler {
  /**
   * Configure gateway message handler spec.
   *
   * @return the message handler spec
   */
  MessageHandlerSpec configureGateway(Message<?> h);
}
