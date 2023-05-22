package com.aks.framework.demo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class WeatherConfiguration {
  @Bean
  public IntegrationFlow weatherRequest() {
    return IntegrationFlows.from(
            Http.inboundGateway("/weather-api")
                .requestMapping(m -> m.methods(HttpMethod.POST))
                .mappedRequestHeaders("request-type", "content-type", "cerdos-id")
                .requestPayloadType(JsonNode.class))
        .route(headerRouter())
        .get();
  }

  @Bean
  public AbstractMappingMessageRouter headerRouter() {
    HeaderValueRouter router = new HeaderValueRouter("request-type");
    router.setDefaultOutputChannelName("wrongMessageChannel");
    router.setChannelMapping("weather", "weatherAPIExecutorChannel");
    router.setChannelMapping("orchestration", "orchestrationRequest.input");
    router.setChannelMapping("data-sourcing", "dataSourcingRequest.input");
    router.setChannelKeyFallback(false);
    return router;
  }

  @Bean
  public MessageChannel wrongMessageChannel() {
    return new DirectChannel();
  }

  @ServiceActivator(inputChannel = "wrongMessageChannel")
  public JsonNode handleWrongMessages(Map headers, Object payload) {
    ObjectNode errorResponse = new ObjectMapper().createObjectNode();
    errorResponse.put(
        "errorMessage",
        String.format("%s not a valid request type.", headers.get("request-type").toString()));
    return errorResponse;
  }
}
