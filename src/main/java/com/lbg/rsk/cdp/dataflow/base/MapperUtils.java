package com.lbg.rsk.cdp.dataflow.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.springframework.util.ObjectUtils;

public class MapperUtils {

  private MapperUtils() {}

  public static JsonNode convertToJson(Object toObject) {
    if (toObject instanceof JsonNode) {
      return (JsonNode) toObject;
    } else if (toObject instanceof String) {
      try {
        return BeanUtils.getObjectMapper().readTree(toObject.toString());
      } catch (JsonProcessingException e) {
        Map<String, String> payload = new HashMap<>(1);
        payload.put(DataFlowConstants.DEFAULT_NODE_NAME_IF_OBJECT_IS_STRING, toObject.toString());
        return BeanUtils.getObjectMapper().valueToTree(payload);
      }
    } else {
      return BeanUtils.getObjectMapper().convertValue(toObject, JsonNode.class);
    }
  }

  public static Object convertToType(Object toObject, Class type) {
    return BeanUtils.getObjectMapper().convertValue(toObject, type);
  }

  public static Map convertToMap(Object toObject) {
    if (toObject instanceof Map) {
      return (Map) toObject;
    }
    try {
      return BeanUtils.getObjectMapper().convertValue(toObject, Map.class);
    } catch (IllegalArgumentException illegalArgumentException) {
      Map<String, String> payload = new HashMap<>(1);
      payload.put(DataFlowConstants.DEFAULT_NODE_NAME_IF_OBJECT_IS_STRING, toObject.toString());
      return BeanUtils.getObjectMapper().convertValue(payload, Map.class);
    }
  }

  public static JsonNode removeEmptyNodes(Object node) {
    JsonNode jsonNode = convertToJson(node);
    Iterator<JsonNode> it = jsonNode.iterator();
    while (it.hasNext()) {
      JsonNode child = it.next();
      if (child.isNull()) {
        it.remove();
      } else if (child.isObject() && (child.isEmpty(null))) {
        it.remove();
      } else if (child.isTextual()
          && (ObjectUtils.isEmpty(child.asText()) || child.asText().equals("null"))) {
        it.remove();
      } else {
        removeEmptyNodes(child);
      }
    }
    return jsonNode;
  }
}
