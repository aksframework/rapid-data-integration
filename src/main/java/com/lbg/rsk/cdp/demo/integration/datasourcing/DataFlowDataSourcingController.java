package com.lbg.rsk.cdp.demo.integration.datasourcing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("data-sourcing")
public class DataFlowDataSourcingController {
  @Autowired private DataFlowDataSourcingGateway dataFlowDataSourcingGateway;

  @Autowired private ObjectMapper objectMapper;

  @PostMapping
  public ResponseEntity<JsonNode> dataFlowRequest(
      @RequestBody JsonNode request,
      @RequestHeader(value = "cerdos-id", required = false) String cerdosID) {
    Object response =
        dataFlowDataSourcingGateway.processRequest(
            MessageBuilder.withPayload(request).setHeader("cerdos-id", cerdosID).build());
    JsonNode responseJson = objectMapper.convertValue(response, JsonNode.class);
    return ResponseEntity.ok().body(responseJson);
  }
}
