package com.lbg.rsk.cdp.dataflow.datatransformer;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.integration.store.MessageGroup;

/** The interface Application transformer. */
public interface InApplicationDataTransformer {
  /**
   * Transform json node.
   *
   * @param messageGroup the message group
   * @return the json node
   */
  JsonNode transform(MessageGroup messageGroup);
}
