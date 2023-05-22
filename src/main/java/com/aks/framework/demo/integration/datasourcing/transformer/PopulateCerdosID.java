package com.lbg.rsk.cdp.demo.integration.datasourcing.transformer;

import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.base.MapperUtils;
import com.lbg.rsk.cdp.dataflow.datatransformer.DataGathererUtils;
import com.lbg.rsk.cdp.dataflow.datatransformer.InApplicationDataTransformer;
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
