package com.aks.framework.rdi.datatransformer;

import static com.aks.framework.rdi.datatransformer.DataGathererUtils.convertForTraversal;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.traversr.SimpleTraversal;
import com.aks.framework.rdi.base.ApplicationConstants;
import com.aks.framework.rdi.base.DataFlowConfig.DataTransformerConfig;
import com.aks.framework.rdi.base.DataFlowConfig.ExpressionTraversalPath;
import com.aks.framework.rdi.base.DataFlowConfig.MapTraversalPath;
import com.aks.framework.rdi.base.DataFlowConfig.MatchTraversalPath;
import com.aks.framework.rdi.base.DataFlowConfig.TraversalPath;
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

/**
 * The type Data transformer executor.
 */
@Slf4j
public class DataTransformerExecutor {
  private final String dataTransformerName;
  private final DataTransformerConfig dataTransformerConfig;

  private Object toObject;

  private Object fromObject;

  /**
   * Instantiates a new Data transformer executor.
   *
   * @param dataTransformerName the data transformer name
   * @param dataTransformerConfig the data transformer config
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
      executeRemoveField(toObject);
      executeMapWithMatch(toObject, fromObject);
      executeMapWith(toObject, fromObject);
      executeReplaceWithMatch(toObject, fromObject);
      executeReplaceWith(toObject, fromObject);
      executeReplaceFixed(toObject);
      executeReplaceWithType(toObject, fromObject);
    }
    return toObject;
  }

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

  private void executeRemoveField(Object toObject) {
    if (!ObjectUtils.isEmpty(dataTransformerConfig.getRemoveField())) {
      for (String replaceFixed : dataTransformerConfig.getRemoveField()) {
        List<String> toObjectTraversalList = getTraversalList(replaceFixed, toObject);
        toObjectTraversalList.forEach(
            toObjectTraversal -> removeField(toObject, toObjectTraversal));
      }
    }
  }

  private void removeField(Object toObject, String toTraversalPath) {
    Entry<SimpleTraversal<Object>, Optional<Object>> traversalAndObjectMap =
        DataGathererUtils.getTraversalAndObjectMap(toTraversalPath, toObject);
    if (traversalAndObjectMap.getValue().isPresent()) {
      traversalAndObjectMap.getKey().remove(toObject);
      log.info(
          "Transformed -> transformer[{}] [remove-field] for[{}] Result[{}]",
          dataTransformerName,
          toTraversalPath,
          toObject);
    } else {
      log.info(
          "Error in -> transformer[{}] [remove-field] for[{}], Field not present.",
          dataTransformerName,
          toTraversalPath);
    }
  }

  private void replaceWith(
      Object toObject, Object fromObject, String toTraversalPath, String fromTraversalPath) {
    Entry<SimpleTraversal<Object>, Optional<Object>> traversalAndObjectMap =
        DataGathererUtils.getTraversalAndObjectMap(toTraversalPath, toObject);
    Optional<Object> valueObject =
        DataGathererUtils.getObjectFromTraversal(fromTraversalPath, fromObject);

    if ((traversalAndObjectMap.getValue().isPresent()
            || toTraversalPath.equals(ApplicationConstants.ROOT_OBJECT_PARENTHESIS))
        && valueObject.isPresent()) {
      if (toTraversalPath.equals(ApplicationConstants.ROOT_OBJECT_PARENTHESIS)) {
        Object convertedObj = convertForTraversal(valueObject.get());
        if (convertedObj instanceof Map) {
          ((Map) toObject).putAll((Map) convertedObj);
        } else {
          ((Map) toObject)
              .put(ApplicationConstants.DEFAULT_NODE_NAME_IF_SOURCE_IS_ROOT, convertedObj);
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
            || toTraversalPath.equals(ApplicationConstants.ROOT_OBJECT_PARENTHESIS))
        && valueObject.isPresent()) {
      Object value = evaluateExpression(expression, valueObject.get());
      if (null != value) {
        if (toTraversalPath.equals(ApplicationConstants.ROOT_OBJECT_PARENTHESIS)) {
          Object convertedObj = convertForTraversal(value);
          if (convertedObj instanceof Map) {
            ((Map) toObject).putAll((Map) convertedObj);
          } else {
            ((Map) toObject)
                .put(ApplicationConstants.DEFAULT_NODE_NAME_IF_SOURCE_IS_ROOT, convertedObj);
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

  private SimpleTraversal getTraversal(String traversalPath) {
    return SimpleTraversal.newTraversal(traversalPath);
  }

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

  private List<String> getTraversalList(String traversalPath, Object traversalObject) {
    return DataGathererUtils.getTraversalList(traversalPath, traversalObject);
  }

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
            || toMatchObjectTraversalList.size() != mapToObjectTraversalList.size()
            || toMatchObjectTraversalList.size() != mapFromObjectTraversalList.size()) {
          throw new RuntimeException("Invalid transformer pattern [" + mapWithMatch + "]");
        }
        IntStream.range(0, toMatchObjectTraversalList.size())
            .forEach(
                i ->
                    withMapMatch(
                        toObject,
                        fromObject,
                        toMatchObjectTraversalList.get(i),
                        fromMatchObjectTraversalList,
                        mapToObjectTraversalList.get(i),
                        mapFromObjectTraversalList,
                        mapWithObjectName));
      }
    }
  }

  private void withMapMatch(
      Object toObject,
      Object fromObject,
      String matchToTraversalPath,
      List<String> matchFromTraversalPath,
      String mapToObjectPath,
      List<String> mapFromObjectPath,
      String mapWithObjectName) {
    Optional<Object> matchToObject =
        DataGathererUtils.getObjectFromTraversal(matchToTraversalPath, toObject);
    Optional<Object> mapToObject =
        DataGathererUtils.getObjectFromTraversal(mapToObjectPath, toObject);

    for (int i = 0; i < matchFromTraversalPath.size(); i++) {
      Optional<Object> matchFromObject =
          DataGathererUtils.getObjectFromTraversal(matchFromTraversalPath.get(i), fromObject);
      Optional<Object> mapFromObject =
          DataGathererUtils.getObjectFromTraversal(mapFromObjectPath.get(i), fromObject);
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
              matchFromTraversalPath.get(i),
              mapWithObjectName,
              toObject);
          break;
        } else {
          log.info(
              "Value not matched -> transformer[{}] [map-with-match] for[{}-{}-{}] source value[{}] target value[{}]",
              dataTransformerName,
              matchToTraversalPath,
              matchFromTraversalPath.get(i),
              mapWithObjectName,
              matchFromObject.get().toString(),
              matchToObject.get().toString());
        }
      }
    }
  }
}
