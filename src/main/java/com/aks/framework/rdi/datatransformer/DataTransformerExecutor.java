package com.aks.framework.rdi.datatransformer;

import static com.aks.framework.rdi.base.DataFlowConstants.ROOT_OBJECT_PARENTHESIS;
import static com.aks.framework.rdi.datatransformer.DataGathererUtils.convertForTraversal;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.traversr.SimpleTraversal;
import com.aks.framework.rdi.base.DataFlowConfig.DataTransformerConfig;
import com.aks.framework.rdi.base.DataFlowConfig.ExpressionTraversalPath;
import com.aks.framework.rdi.base.DataFlowConfig.MapTraversalPath;
import com.aks.framework.rdi.base.DataFlowConfig.MatchTraversalPath;
import com.aks.framework.rdi.base.DataFlowConfig.TraversalPath;
import com.aks.framework.rdi.base.DataFlowConstants;
import com.aks.framework.rdi.base.MapperUtils;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ObjectUtils;

/** The type DataTransformerExecutor. */
@Slf4j
public class DataTransformerExecutor {
  /** The Data transformer name. */
  private final String dataTransformerName;
  /** The Data transformer. */
  private final DataTransformerConfig dataTransformerConfig;

  /** The To object. */
  private Object toObject;

  /** The From object. */
  private Object fromObject;

  /**
   * Instantiates a new Data transformer executor.
   *
   * @param dataTransformerName the data transformer name
   * @param dataTransformerConfig the data transformer
   * @param toObject the to object
   * @param fromObject the from object
   */
  public DataTransformerExecutor(
      String dataTransformerName,
      DataTransformerConfig dataTransformerConfig,
      Object toObject,
      Object fromObject) {
    this.dataTransformerName = dataTransformerName;
    this.dataTransformerConfig = dataTransformerConfig;
    this.toObject = convertForTraversal(toObject);
    this.fromObject = convertForTraversal(fromObject);
  }

  /**
   * Execute object.
   *
   * @return the object
   */
  public Object execute() {
    if (!dataTransformerConfig.isInApplication()) {
      executeReplaceWith(toObject, fromObject);
      executeReplaceWithType(toObject, fromObject);
      executeReplaceFixed(toObject);
      executeMapWith(toObject, fromObject);
      executeMapWithMatch(toObject, fromObject);
      executeReplaceWithMatch(toObject, fromObject);
    }
    return toObject;
  }

  /**
   * Execute replace with.
   *
   * @param toObject the to object
   * @param fromObject the from object
   */
  private void executeReplaceWith(Object toObject, Object fromObject) {
    if (!ObjectUtils.isEmpty(dataTransformerConfig.getReplaceWith())) {

      for (TraversalPath replaceWith : dataTransformerConfig.getReplaceWith()) {

        List<String> toObjectTraversalList = getTraversalList(replaceWith.getToPath(), toObject);
        List<String> fromObjectTraversalList =
            getTraversalList(replaceWith.getFromPath(), fromObject);
        if (toObjectTraversalList.size() != fromObjectTraversalList.size()) {
          throw new RuntimeException("Invalid transformer pattern [" + replaceWith + "]");
        }

        IntStream.range(0, toObjectTraversalList.size())
            .forEach(
                i ->
                    replaceWith(
                        toObject,
                        fromObject,
                        toObjectTraversalList.get(i),
                        fromObjectTraversalList.get(i)));
      }
    }
  }

  /**
   * Execute replace fixed.
   *
   * @param toObject the to object
   */
  private void executeReplaceFixed(Object toObject) {
    if (!ObjectUtils.isEmpty(dataTransformerConfig.getReplaceFixed())) {
      for (TraversalPath replaceFixed : dataTransformerConfig.getReplaceFixed()) {
        List<String> toObjectTraversalList = getTraversalList(replaceFixed.getToPath(), toObject);
        toObjectTraversalList.forEach(
            toObjectTraversal ->
                replaceFixed(toObject, toObjectTraversal, replaceFixed.getFromPath()));
      }
    }
  }

  /**
   * Replace fixed.
   *
   * @param toObject the to object
   * @param toTraversalPath the to traversal path
   * @param replaceFixedValue the replace fixed value
   */
  private void replaceFixed(Object toObject, String toTraversalPath, String replaceFixedValue) {
    Entry<SimpleTraversal<Object>, Optional<Object>> traversalAndObjectMap =
        DataGathererUtils.getTraversalAndObjectMap(toTraversalPath, toObject);

    if (traversalAndObjectMap.getValue().isPresent()) {
      Object value = evaluateExpression(replaceFixedValue);
      if (null != value) {
        traversalAndObjectMap.getKey().set(toObject, value);
        log.info(
            "Transformed -> transformer[{}] [replace-fixed] for[{}|{}] Result[{}]",
            dataTransformerName,
            toTraversalPath,
            replaceFixedValue,
            toObject);
      } else {
        log.info(
            "Error in -> transformer[{}] [replace-fixed] for[{}|{}]",
            dataTransformerName,
            toTraversalPath,
            replaceFixedValue);
      }
    }
  }

  /**
   * Replace with.
   *
   * @param toObject the to object
   * @param fromObject the from object
   * @param toTraversalPath the to traversal path
   * @param fromTraversalPath the from traversal path
   */
  private void replaceWith(
      Object toObject, Object fromObject, String toTraversalPath, String fromTraversalPath) {
    Entry<SimpleTraversal<Object>, Optional<Object>> traversalAndObjectMap =
        DataGathererUtils.getTraversalAndObjectMap(toTraversalPath, toObject);
    Optional<Object> valueObject =
        DataGathererUtils.getObjectFromTraversal(fromTraversalPath, fromObject);

    if ((traversalAndObjectMap.getValue().isPresent()
            || toTraversalPath.equals(ROOT_OBJECT_PARENTHESIS))
        && valueObject.isPresent()) {
      if (toTraversalPath.equals(ROOT_OBJECT_PARENTHESIS)) {
        Object convertedObj = convertForTraversal(valueObject.get());
        if (convertedObj instanceof Map) {
          ((Map) toObject).putAll((Map) convertedObj);
        } else {
          ((Map) toObject).put(DataFlowConstants.DEFAULT_NODE_NAME_IF_SOURCE_IS_ROOT, convertedObj);
        }
      } else {
        traversalAndObjectMap.getKey().set(toObject, valueObject.get());
      }
      log.info(
          "Transformed -> transformer[{}] [replace-with] for[{}|{}] Result[{}]",
          dataTransformerName,
          toTraversalPath,
          fromTraversalPath,
          toObject);
    }
  }

  private void executeReplaceWithType(Object toObject, Object fromObject) {
    if (!ObjectUtils.isEmpty(dataTransformerConfig.getReplaceWithType())) {
      for (ExpressionTraversalPath replaceWithType : dataTransformerConfig.getReplaceWithType()) {
        List<String> toObjectTraversalList =
            getTraversalList(replaceWithType.getToPath(), toObject);
        List<String> fromObjectTraversalList =
            getTraversalList(replaceWithType.getFromPath(), fromObject);
        if (toObjectTraversalList.size() != fromObjectTraversalList.size()) {
          throw new RuntimeException("Invalid transformer pattern [" + replaceWithType + "]");
        }
        IntStream.range(0, toObjectTraversalList.size())
            .forEach(
                i ->
                    replaceWithType(
                        toObject,
                        fromObject,
                        toObjectTraversalList.get(i),
                        fromObjectTraversalList.get(i),
                        replaceWithType.getExpression()));
      }
    }
  }

  private void replaceWithType(
      Object toObject,
      Object fromObject,
      String toTraversalPath,
      String fromTraversalPath,
      String expression) {
    Entry<SimpleTraversal<Object>, Optional<Object>> traversalAndObjectMap =
        DataGathererUtils.getTraversalAndObjectMap(toTraversalPath, toObject);
    Optional<Object> valueObject =
        DataGathererUtils.getObjectFromTraversal(fromTraversalPath, fromObject);

    if ((traversalAndObjectMap.getValue().isPresent()
            || toTraversalPath.equals(ROOT_OBJECT_PARENTHESIS))
        && valueObject.isPresent()) {
      Object value = evaluateExpression(expression, valueObject.get());
      if (null != value) {
        if (toTraversalPath.equals(ROOT_OBJECT_PARENTHESIS)) {
          Object convertedObj = convertForTraversal(value);
          if (convertedObj instanceof Map) {
            ((Map) toObject).putAll((Map) convertedObj);
          } else {
            ((Map) toObject)
                .put(DataFlowConstants.DEFAULT_NODE_NAME_IF_SOURCE_IS_ROOT, convertedObj);
          }
        } else {
          traversalAndObjectMap.getKey().set(toObject, value);
        }
        log.info(
            "Transformed -> transformer[{}] [replace-with-type] for[{}|{}] Result[{}]",
            dataTransformerName,
            toTraversalPath,
            fromTraversalPath,
            toObject);
      } else {
        log.error(
            "Error in -> transformer[{}] [replace-with-type] for[{}|{}]",
            dataTransformerName,
            toTraversalPath,
            fromTraversalPath);
      }
    }
  }

  private static Object evaluateExpression(String expression, Object valueObject) {
    try {
      ExpressionParser parser = new SpelExpressionParser();
      StandardEvaluationContext context = new StandardEvaluationContext();
      context.setVariable("object", valueObject);
      context.registerFunction(
          "convertToJson", MapperUtils.class.getDeclaredMethod("convertToJson", Object.class));
      context.registerFunction(
          "convertToMap", MapperUtils.class.getDeclaredMethod("convertToMap", Object.class));
      context.registerFunction(
          "removeEmptyNodes",
          MapperUtils.class.getDeclaredMethod("removeEmptyNodes", Object.class));

      return parser.parseExpression(expression).getValue(context);
    } catch (Exception e) {
      log.error(
          String.format("Error in expression '%s' value object: %s", expression, valueObject), e);
      return null;
    }
  }

  private static Object evaluateExpression(String expression) {
    try {
      ExpressionParser parser = new SpelExpressionParser();
      return parser.parseExpression(expression).getValue();
    } catch (Exception e) {
      log.error(String.format("Error in expression '%s'", expression), e);
      return null;
    }
  }

  /**
   * Gets traversal.
   *
   * @param traversalPath the traversal path
   * @return the traversal
   */
  private SimpleTraversal getTraversal(String traversalPath) {
    return SimpleTraversal.newTraversal(traversalPath);
  }

  /**
   * Execute map with.
   *
   * @param toObject the to object
   * @param fromObject the from object
   */
  private void executeMapWith(Object toObject, Object fromObject) {
    if (!ObjectUtils.isEmpty(dataTransformerConfig.getMapWith())) {
      for (MapTraversalPath mapWith : dataTransformerConfig.getMapWith()) {
        List<String> toObjectTraversalList = getTraversalList(mapWith.getToPath(), toObject);
        List<String> fromObjectTraversalList = getTraversalList(mapWith.getFromPath(), fromObject);
        if (toObjectTraversalList.size() != fromObjectTraversalList.size()) {
          throw new RuntimeException("Invalid transformer pattern [" + mapWith + "]");
        }
        IntStream.range(0, toObjectTraversalList.size())
            .forEach(
                i ->
                    withMap(
                        toObject,
                        fromObject,
                        toObjectTraversalList.get(i),
                        fromObjectTraversalList.get(i),
                        mapWith.getNewNode()));
      }
    }
  }

  /**
   * With map.
   *
   * @param toObject the to object
   * @param fromObject the from object
   * @param toTraversalPath the to traversal path
   * @param fromTraversalPath the from traversal path
   * @param mapWithObjectName the map with object name
   */
  private void withMap(
      Object toObject,
      Object fromObject,
      String toTraversalPath,
      String fromTraversalPath,
      String mapWithObjectName) {
    Optional<Object> fieldObject =
        DataGathererUtils.getObjectFromTraversal(toTraversalPath, toObject);
    Optional<Object> valueObject =
        DataGathererUtils.getObjectFromTraversal(fromTraversalPath, fromObject);

    if (fieldObject.isPresent() && valueObject.isPresent()) {
      ((Map) fieldObject.get()).put(mapWithObjectName, "-");
      getTraversal(toTraversalPath + "." + mapWithObjectName).set(toObject, valueObject.get());
      log.info(
          "Transformed -> transformer[{}] [map-with] for[{}-{}-{}] Result[{}]",
          dataTransformerName,
          toTraversalPath,
          fromTraversalPath,
          mapWithObjectName,
          toObject);
    }
  }

  /**
   * Gets traversal list.
   *
   * @param traversalPath the traversal path
   * @param traversalObject the traversal object
   * @return the traversal list
   */
  private List<String> getTraversalList(String traversalPath, Object traversalObject) {
    return DataGathererUtils.getTraversalList(traversalPath, traversalObject);
  }

  /**
   * Execute replace with match.
   *
   * @param toObject the to object
   * @param fromObject the from object
   */
  private void executeReplaceWithMatch(Object toObject, Object fromObject) {
    if (!ObjectUtils.isEmpty(dataTransformerConfig.getReplaceWithMatch())) {
      for (MatchTraversalPath replaceWithMatch : dataTransformerConfig.getReplaceWithMatch()) {
        List<String> matchToObjectTraversalList =
            getTraversalList(replaceWithMatch.getMatchToPath(), toObject);
        List<String> matchFromObjectTraversalList =
            getTraversalList(replaceWithMatch.getMatchFromPath(), fromObject);
        List<String> toObjectTraversalList =
            getTraversalList(replaceWithMatch.getToPath(), toObject);
        List<String> fromObjectTraversalList =
            getTraversalList(replaceWithMatch.getFromPath(), fromObject);

        IntStream.range(0, matchToObjectTraversalList.size())
            .forEach(
                i ->
                    replaceWithMatch(
                        toObject,
                        fromObject,
                        matchToObjectTraversalList.get(i),
                        matchFromObjectTraversalList.get(i),
                        toObjectTraversalList.get(i),
                        fromObjectTraversalList.get(i)));
      }
    }
  }

  /**
   * Replace with match.
   *
   * @param toObject the to object
   * @param fromObject the from object
   * @param matchToTraversalPath the match to traversal path
   * @param matchFromTraversalPath the match from traversal path
   * @param toTraversalPath the to traversal path
   * @param fromTraversalPath the from traversal path
   */
  private void replaceWithMatch(
      Object toObject,
      Object fromObject,
      String matchToTraversalPath,
      String matchFromTraversalPath,
      String toTraversalPath,
      String fromTraversalPath) {
    Entry<SimpleTraversal<Object>, Optional<Object>> toTraversalAndObjectMap =
        DataGathererUtils.getTraversalAndObjectMap(toTraversalPath, toObject);

    Optional<Object> valueObject =
        DataGathererUtils.getObjectFromTraversal(fromTraversalPath, fromObject);
    Optional<Object> matchToObject =
        DataGathererUtils.getObjectFromTraversal(matchToTraversalPath, toObject);
    Optional<Object> matchFromObject =
        DataGathererUtils.getObjectFromTraversal(matchFromTraversalPath, fromObject);

    if (toTraversalAndObjectMap.getValue().isPresent()
        && valueObject.isPresent()
        && matchToObject.isPresent()
        && matchFromObject.isPresent()) {
      if (matchToObject.get().toString().equals(matchFromObject.get().toString())) {
        toTraversalAndObjectMap.getKey().set(toObject, valueObject.get());
        log.info(
            "Transformed -> transformer[{}] [replace-with-match] for[{}-{}] Result[{}]",
            dataTransformerName,
            matchToTraversalPath,
            matchFromTraversalPath,
            toObject);
      } else {
        log.info(
            "Value not matched -> transformer[{}] [replace-with-matched] for[{}-{}] source value[{}] target value[{}]",
            dataTransformerName,
            matchToTraversalPath,
            matchFromTraversalPath,
            matchFromObject.get().toString(),
            matchToObject.get().toString());
      }
    }
  }

  /**
   * Execute map with match.
   *
   * @param toObject the to object
   * @param fromObject the from object
   */
  private void executeMapWithMatch(Object toObject, Object fromObject) {
    if (!ObjectUtils.isEmpty(dataTransformerConfig.getMapWithMatch())) {
      for (MatchTraversalPath mapWithMatch : dataTransformerConfig.getMapWithMatch()) {
        String[] matchWithArr = mapWithMatch.getToPath().split("\\.");
        String collect =
            IntStream.range(0, matchWithArr.length - 1)
                .mapToObj(i -> matchWithArr[i])
                .collect(Collectors.joining("."));
        String mapWithObjectName = matchWithArr[matchWithArr.length - 1];

        List<String> toMatchObjectTraversalList =
            getTraversalList(mapWithMatch.getMatchToPath(), toObject);
        List<String> fromMatchObjectTraversalList =
            getTraversalList(mapWithMatch.getMatchFromPath(), fromObject);
        List<String> mapToObjectTraversalList = getTraversalList(collect, toObject);
        List<String> mapFromObjectTraversalList =
            getTraversalList(mapWithMatch.getFromPath(), fromObject);

        if (toMatchObjectTraversalList.size() != fromMatchObjectTraversalList.size()
            && toMatchObjectTraversalList.size() != mapToObjectTraversalList.size()
            && toMatchObjectTraversalList.size() != mapFromObjectTraversalList.size()) {
          throw new RuntimeException("Invalid transformer pattern [" + mapWithMatch + "]");
        }
        IntStream.range(0, toMatchObjectTraversalList.size())
            .forEach(
                i ->
                    withMapMatch(
                        toObject,
                        fromObject,
                        toMatchObjectTraversalList.get(i),
                        fromMatchObjectTraversalList.get(i),
                        mapToObjectTraversalList.get(i),
                        mapFromObjectTraversalList.get(i),
                        mapWithObjectName));
      }
    }
  }

  /**
   * With map match.
   *
   * @param toObject the to object
   * @param fromObject the from object
   * @param matchToTraversalPath the match to traversal path
   * @param matchFromTraversalPath the match from traversal path
   * @param mapToObjectPath the map to object path
   * @param mapFromObjectPath the map from object path
   * @param mapWithObjectName the map with object name
   */
  private void withMapMatch(
      Object toObject,
      Object fromObject,
      String matchToTraversalPath,
      String matchFromTraversalPath,
      String mapToObjectPath,
      String mapFromObjectPath,
      String mapWithObjectName) {
    Optional<Object> matchToObject =
        DataGathererUtils.getObjectFromTraversal(matchToTraversalPath, toObject);
    Optional<Object> matchFromObject =
        DataGathererUtils.getObjectFromTraversal(matchFromTraversalPath, fromObject);
    Optional<Object> mapToObject =
        DataGathererUtils.getObjectFromTraversal(mapToObjectPath, toObject);
    Optional<Object> mapFromObject =
        DataGathererUtils.getObjectFromTraversal(mapFromObjectPath, fromObject);

    if (matchToObject.isPresent()
        && matchFromObject.isPresent()
        && mapToObject.isPresent()
        && mapFromObject.isPresent()) {
      if (matchToObject.get().toString().equals(matchFromObject.get().toString())) {
        ((Map) mapToObject.get()).put(mapWithObjectName, "-");
        getTraversal(mapToObjectPath + "." + mapWithObjectName).set(toObject, mapFromObject.get());
        log.info(
            "Transformed -> transformer[{}] [map-with-match] for[{}-{}-{}] Result[{}]",
            dataTransformerName,
            matchToTraversalPath,
            matchFromTraversalPath,
            mapWithObjectName,
            toObject);
      } else {
        log.info(
            "Value not matched -> transformer[{}] [map-with-matched] for[{}-{}-{}] source value[{}] target value[{}]",
            dataTransformerName,
            matchToTraversalPath,
            matchFromTraversalPath,
            mapWithObjectName,
            matchFromObject.get().toString(),
            matchToObject.get().toString());
      }
    }
  }
}
