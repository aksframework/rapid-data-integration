package com.aks.framework.rdi.datagatherer;

import static com.aks.framework.rdi.base.DataFlowConstants.ROOT_OBJECT_PARENTHESIS;

import com.aks.framework.rdi.base.DataFlowConfig.PlaceHolder;
import com.aks.framework.rdi.base.DataFlowConstants;
import com.aks.framework.rdi.datatransformer.DataGathererUtils;
import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.traversr.SimpleTraversal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;

/** The type Template place holder executor. */
@Slf4j
public class TemplatePlaceHolderExecutor {
  /** The Data gatherer template enum. */
  private final String dataGathererTemplateName;

  /**
   * Instantiates a new Template place holder executor.
   *
   * @param dataGathererTemplateName the data gatherer template enum
   */
  public TemplatePlaceHolderExecutor(String dataGathererTemplateName) {
    this.dataGathererTemplateName = dataGathererTemplateName;
  }

  /**
   * Replace placeholders object.
   *
   * @param toObject the to object
   * @param fromObject the from object
   * @param placeHolder the place holder
   * @return the object
   */
  public Object replacePlaceholders(Object toObject, Object fromObject, PlaceHolder placeHolder) {
    List<String> toObjectTraversalList =
        DataGathererUtils.getTraversalList(placeHolder.getToPath(), toObject);
    toObjectTraversalList.forEach(
        toObjectTraversal -> replaceWith(toObject, fromObject, toObjectTraversal, placeHolder));
    return toObject;
  }

  /**
   * Replace with.
   *
   * @param toObject the to object
   * @param fromObject the from object
   * @param toTraversalPath the to traversal path
   * @param placeHolder the place holder
   */
  private void replaceWith(
      Object toObject, Object fromObject, String toTraversalPath, PlaceHolder placeHolder) {
    Entry<SimpleTraversal<Object>, Optional<Object>> traversalAndObjectMap =
        DataGathererUtils.getTraversalAndObjectMap(toTraversalPath, toObject);

    Optional<Object> valueObject =
        DataGathererUtils.getObjectFromTraversal(placeHolder.getFromPath(), fromObject);

    if ((traversalAndObjectMap.getValue().isPresent()
            || toTraversalPath.equals(ROOT_OBJECT_PARENTHESIS))
        && valueObject.isPresent()) {
      if (toTraversalPath.equals(ROOT_OBJECT_PARENTHESIS)) {
        Object convertedObj = DataGathererUtils.convertForTraversal(valueObject.get());
        if (convertedObj instanceof Map) {
          ((Map) toObject).putAll((Map) convertedObj);
        } else {
          ((Map) toObject).put(DataFlowConstants.DEFAULT_NODE_NAME_IF_SOURCE_IS_ROOT, convertedObj);
        }
      } else {
        traversalAndObjectMap.getKey().set(toObject, valueObject.get());
      }
      log.info(
          "Placeholder replaced -> template[{}] for[{}|{}|{}] Result[{}]",
          dataGathererTemplateName,
          toTraversalPath,
          placeHolder.getFromSpec(),
          placeHolder.getFromPath(),
          toObject);
    }
  }
}
