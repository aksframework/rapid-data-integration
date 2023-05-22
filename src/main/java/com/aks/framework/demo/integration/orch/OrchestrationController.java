package com.aks.framework.demo.integration.orch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("orchestration")
public class OrchestrationController {
  @Autowired private OrchestrationGateway orchestrationGateway;

  @Autowired private ObjectMapper objectMapper;

  @PostMapping
  public ResponseEntity<JsonNode> dataFlowRequest(@RequestBody JsonNode request) {
    Object response =
        orchestrationGateway.processRequest(
            MessageBuilder.withPayload(request)
                .setHeader("cerdos-id", UUID.randomUUID().toString())
                .build());
    JsonNode responseJson = objectMapper.convertValue(response, JsonNode.class);
    return ResponseEntity.ok().body(responseJson);
  }
}
