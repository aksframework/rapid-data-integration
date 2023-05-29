package com.aks.framework.demo.integration.datasourcing.transformer;

import com.aks.framework.rdi.base.MapperUtils;
import com.aks.framework.rdi.datatransformer.DataGathererUtils;
import com.aks.framework.rdi.datatransformer.InApplicationDataTransformer;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component(value = "populateCerdosID")
public class PopulateCerdosID implements InApplicationDataTransformer {
  @Override
  public JsonNode transform(MessageGroup messageGroup) {
    Map<String, MessageHeaders> dataFlowHeaders =
        DataGathererUtils.getDataFlowHeaders(messageGroup);
    return MapperUtils.convertToJson(dataFlowHeaders.get("soe").get("cerdos-id").toString());
  }
}
