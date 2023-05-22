package com.lbg.rsk.cdp.dataflow.datatransformer;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.traversr.SimpleTraversal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

/** The type Data gatherer utils. */
@Slf4j
public class DataGathererUtils {

  private DataGathererUtils() {}

  /**
   * Gets traversal list.
   *
   * @param traversalPath the traversal path
   * @param traversalObject the traversal object
   * @return the traversal list
   */
  public static List<String> getTraversalList(String traversalPath, Object traversalObject) {
    if (!traversalPath.contains(DataFlowConstants.ARRAY_RECURSIVE_STRING)) {
      return List.of(traversalPath);
    } else {
      List<String> paths =
          Arrays.stream(traversalPath.split(DataFlowConstants.ARRAY_RECURSIVE_STRING_SPLITTER))
              .map(DataGathererUtils::removeUnRequiredChars)
              .collect(Collectors.toList());

      List<String> finalTraversalList = new ArrayList<>();
      if (paths.isEmpty()) {
        paths.add(DataFlowConstants.ARRAY_ZEROTH_ELEMENT);
      }
      for (String path : paths) {
        if (finalTraversalList.isEmpty()) {
          finalTraversalList.add(DataFlowConstants.EMPTY_STRING);
        } else {
          path = DataFlowConstants.TRAVERSAL_PATH_DELIMITER + path;
        }

        String finalPath = path;
        List<Map<String, List<String>>> collect =
            finalTraversalList.stream()
                .map(p -> generatePathList(p + finalPath, traversalObject))
                .collect(Collectors.toList());

        finalTraversalList.clear();
        List<String> tempList = new ArrayList<>();
        collect.forEach(map -> map.values().forEach(tempList::addAll));

        finalTraversalList = tempList;
      }
      return finalTraversalList;
    }
  }

  private static String removeUnRequiredChars(String string) {
    if (string.endsWith(DataFlowConstants.TRAVERSAL_PATH_DELIMITER)) {
      string = string.substring(0, string.length() - 1);
    }
    if (string.startsWith(DataFlowConstants.TRAVERSAL_PATH_DELIMITER)) {
      string = string.substring(1);
    }
    return string;
  }

  private static Map<String, List<String>> generatePathList(String path, Object traversalObject) {
    SimpleTraversal<Object> toTraversal = SimpleTraversal.newTraversal(path);
    Optional<Object> objectOptional = toTraversal.get(traversalObject);
    if (objectOptional.isPresent()) {
      if (objectOptional.get() instanceof List) {
        List<String> collect =
            IntStream.range(0, ((List<?>) objectOptional.get()).size())
                .mapToObj(i -> String.format("%s.[%d]", path, i))
                .collect(Collectors.toList());
        return Map.of(path, collect);
      } else {
        return Map.of(path, List.of(path));
      }
    } else {
      log.error("Incorrect matcher [{}] Path not present", path);
      return null;
    }
  }

  /**
   * Gets traversal and object map.
   *
   * @param traversalPath the traversal path
   * @param objectForTraversal the object for traversal
   * @return the traversal and object map
   */
  public static Map.Entry<SimpleTraversal<Object>, Optional<Object>> getTraversalAndObjectMap(
      String traversalPath, Object objectForTraversal) {
    SimpleTraversal<Object> toTraversal = SimpleTraversal.newTraversal(traversalPath);
    return new AbstractMap.SimpleEntry<>(toTraversal, toTraversal.get(objectForTraversal));
  }

  /**
   * Gets object from traversal.
   *
   * @param traversalPath the traversal path
   * @param objectForTraversal the object for traversal
   * @return the object from traversal
   */
  public static Optional<Object> getObjectFromTraversal(
      String traversalPath, Object objectForTraversal) {
    if (traversalPath.equals(DataFlowConstants.ROOT_OBJECT_PARENTHESIS)) {
      return Optional.of(objectForTraversal);
    }
    SimpleTraversal<Object> toTraversal = SimpleTraversal.newTraversal(traversalPath);
    return toTraversal.get(objectForTraversal);
  }

  /**
   * Convert for traversal object.
   *
   * @param toObject the to object
   * @return the object
   */
  public static Object convertForTraversal(Object toObject) {
    ObjectMapper mapper = new ObjectMapper();
    if (toObject instanceof Map) {
      return toObject;
    } else if (toObject instanceof JsonNode) {
      return mapper.convertValue(toObject, Map.class);
    } else if (toObject instanceof ArrayList) {
      return ((List<?>) toObject)
          .stream().map(DataGathererUtils::convertForTraversal).collect(Collectors.toList());
    }
    return toObject;
  }

  /**
   * Is file based spec boolean.
   *
   * @param dataSpecName the data spec name
   * @return the boolean
   */
  public static boolean isFileBasedSpec(String dataSpecName) {
    return isYAMLFileSpec(dataSpecName) || isJSONFileSpec(dataSpecName);
  }

  /**
   * Is json file boolean.
   *
   * @param dataSpecName the data spec name
   * @return the boolean
   */
  public static boolean isJSONFileSpec(String dataSpecName) {
    return dataSpecName.toUpperCase().contains(".JSON");
  }

  /**
   * Is yaml file boolean.
   *
   * @param dataSpecName the data spec name
   * @return the boolean
   */
  public static boolean isYAMLFileSpec(String dataSpecName) {
    return dataSpecName.toUpperCase().contains(".YAML")
        || dataSpecName.toUpperCase().contains(".YML");
  }

  /**
   * Gets data flow payloads.
   *
   * @param group the group
   * @return the data flow payloads
   */
  public static Map<String, Object> getDataFlowPayloads(MessageGroup group) {
    return getDataFlowMessages(group).entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, v -> v.getValue().getPayload()));
  }

  /**
   * Gets data flow headers.
   *
   * @param group the group
   * @return the data flow headers
   */
  public static Map<String, MessageHeaders> getDataFlowHeaders(MessageGroup group) {
    return getDataFlowMessages(group).entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, v -> v.getValue().getHeaders()));
  }

  /**
   * Gets data flow messages.
   *
   * @param group the group
   * @return the data flow messages
   */
  public static Map<String, Message<?>> getDataFlowMessages(MessageGroup group) {
    return group
        .streamMessages()
        .filter(
            message ->
                Objects.nonNull(message.getHeaders().get(DataFlowConstants.DATA_FLOW_HEADER_NAME)))
        .collect(
            Collectors.toMap(
                k ->
                    Objects.requireNonNull(
                            k.getHeaders().get(DataFlowConstants.DATA_FLOW_HEADER_NAME))
                        .toString(),
                v -> v));
  }
}
