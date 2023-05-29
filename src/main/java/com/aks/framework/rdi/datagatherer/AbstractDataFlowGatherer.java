package com.aks.framework.rdi.datagatherer;

import com.aks.framework.rdi.annotations.DataFlowGatherer;
import com.aks.framework.rdi.base.BeanUtils;
import com.aks.framework.rdi.base.DataFlowConfig;
import com.aks.framework.rdi.base.DataFlowConfig.DataGathererTemplateConfig;
import com.aks.framework.rdi.base.DataFlowConfig.DataTransformerConfig;
import com.aks.framework.rdi.base.DataFlowConfig.PlaceHolder;
import com.aks.framework.rdi.base.DataFlowConstants;
import com.aks.framework.rdi.base.MapperUtils;
import com.aks.framework.rdi.datatransformer.DataGathererUtils;
import com.aks.framework.rdi.datatransformer.DataTransformerExecutor;
import com.aks.framework.rdi.datatransformer.InApplicationDataTransformer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.aggregator.MessageGroupProcessor;
import org.springframework.integration.store.MessageGroup;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.yaml.snakeyaml.Yaml;

/** The type Abstract data flow gatherer. */
@Slf4j
public abstract class AbstractDataFlowGatherer extends AbstractBaseDataFlowGatherer
    implements MessageGroupProcessor {

  /** The Data flow config. */
  private DataFlowConfig dataFlowConfig;

  /** The Collected data map. */
  private Map<String, Object> collectedDataMap;

  /** The Data gatherer template enum. */
  private String dataGathererTemplateName;

  /** Instantiates a new Abstract data flow gatherer. */
  protected AbstractDataFlowGatherer() {
    if (this.getClass().isAnnotationPresent(DataFlowGatherer.class)) {
      this.dataGathererTemplateName =
          this.getClass().getAnnotation(DataFlowGatherer.class).template();
    } else {
      log.error(
          "DataGatherer is not defined correctly. Use Annotation '@DataFlowGatherer' to define a 'DataGatherer'");
      throw new RuntimeException(
          "DataGatherer ['"
              + this.getClass()
              + "'] not defined correctly. Use Annotation '@DataFlowGatherer' to define a 'DataGatherer'");
    }
  }

  @Override
  public Object processMessageGroup(MessageGroup group) {
    this.dataFlowConfig = BeanUtils.getDataFlowConfig();
    Assert.notNull(dataGathererTemplateName, "'DataGathererTemplateName' can not be 'Null");
    Assert.notNull(dataFlowConfig, "'DataFlowConfig' can not be 'Null");
    collectedDataMap = DataGathererUtils.getDataFlowPayloads(group);

    if (!ObjectUtils.isEmpty(dataFlowConfig.getDataFlowDataGatherer())
        && !ObjectUtils.isEmpty(dataFlowConfig.getDataGathererConfig(dataGathererTemplateName))) {
      DataGathererTemplateConfig dataGathererTemplateConfig =
          dataFlowConfig.getDataGathererConfig(dataGathererTemplateName);

      if (ObjectUtils.isEmpty(dataGathererTemplateConfig.getTemplateSpec())
          && ObjectUtils.isEmpty(dataGathererTemplateConfig.getTemplatePlaceholders())
          && ObjectUtils.isEmpty(dataGathererTemplateConfig.getTemplateTransformers())) {
        return collectedDataMap;
      }

      if (ObjectUtils.isEmpty(dataGathererTemplateConfig.getTemplateSpec())) {
        dataGathererTemplateConfig.setTemplateSpec("{}");
      }
      executeTemplateTransformers(group, dataGathererTemplateName, dataGathererTemplateConfig);
      return executeTemplatePlaceHolders(dataGathererTemplateName, dataGathererTemplateConfig);
    } else {
      log.error(
          "Data gatherer template [{}] not present in configuration", dataGathererTemplateName);
      return DataGathererUtils.getDataFlowMessages(group);
    }
  }

  /**
   * Execute template place holders object.
   *
   * @param dataGathererTemplateName the data gatherer template enum
   * @param dataGathererTemplateConfig the data gatherer template
   * @return the object
   */
  private Object executeTemplatePlaceHolders(
      String dataGathererTemplateName, DataGathererTemplateConfig dataGathererTemplateConfig) {
    Object toDataObject = getDataSpec(dataGathererTemplateConfig.getTemplateSpec());
    TemplatePlaceHolderExecutor templatePlaceHolderExecutor =
        new TemplatePlaceHolderExecutor(dataGathererTemplateName);
    if (!ObjectUtils.isEmpty(dataGathererTemplateConfig.getTemplatePlaceholders())) {
      for (PlaceHolder placeHolder : dataGathererTemplateConfig.getTemplatePlaceholders()) {
        Object fromDataObject = getDataSpec(placeHolder.getFromSpec());
        toDataObject =
            templatePlaceHolderExecutor.replacePlaceholders(
                toDataObject, fromDataObject, placeHolder);
      }
    }
    return toDataObject;
  }

  /**
   * Execute template transformers.
   *
   * @param group the group
   * @param dataGathererTemplateName the data gatherer template enum
   * @param dataGathererTemplateConfig the data gatherer template
   */
  private void executeTemplateTransformers(
      MessageGroup group,
      String dataGathererTemplateName,
      DataGathererTemplateConfig dataGathererTemplateConfig) {
    if (!ObjectUtils.isEmpty(dataGathererTemplateConfig.getTemplateTransformers())) {
      for (String transformerName : dataGathererTemplateConfig.getTemplateTransformers()) {
        DataTransformerConfig dataTransformerConfigObject =
            dataFlowConfig.getDataTransformerConfig(transformerName);
        if (!ObjectUtils.isEmpty(dataTransformerConfigObject)) {
          if (dataTransformerConfigObject.isInApplication()) {
            InApplicationDataTransformer inApplicationDataTransformer =
                BeanUtils.getInApplicationDataTransformer(
                    dataTransformerConfigObject.getBeanName(), InApplicationDataTransformer.class);
            if (null != inApplicationDataTransformer) {
              JsonNode transformed = inApplicationDataTransformer.transform(group);
              collectedDataMap.put(transformerName, MapperUtils.convertToMap(transformed));
            }
          } else {
            Object toDataObject =
                getDataSpec(dataTransformerConfigObject.getDataSpec().getToSpec());
            Object fromDataObject =
                getDataSpec(dataTransformerConfigObject.getDataSpec().getFromSpec());

            Object transformed =
                new DataTransformerExecutor(
                        transformerName, dataTransformerConfigObject, toDataObject, fromDataObject)
                    .execute();
            if (DataGathererUtils.isFileBasedSpec(
                dataTransformerConfigObject.getDataSpec().getToSpec())) {
              collectedDataMap.put(transformerName, transformed);
            } else {
              collectedDataMap.put(
                  dataTransformerConfigObject.getDataSpec().getToSpec(), transformed);
            }
          }
        } else {
          log.error(
              "Data transformer [{}] mentioned in template [{}] not present in configuration",
              transformerName,
              dataGathererTemplateName);
        }
      }
    }
  }

  /**
   * Gets data spec.
   *
   * @param dataSpecName the data spec name
   * @return the data spec
   */
  private Object getDataSpec(String dataSpecName) {
    if (dataSpecName.equals(DataFlowConstants.ROOT_OBJECT_PARENTHESIS)) {
      return new HashMap<>();
    } else if (DataGathererUtils.isJSONFileSpec(dataSpecName)) {
      try {
        return DataGathererUtils.convertForTraversal(
            BeanUtils.getObjectMapper()
                .readTree(new ClassPathResource(dataSpecName.toLowerCase()).getInputStream()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else if (DataGathererUtils.isYAMLFileSpec(dataSpecName)) {
      Yaml yaml = new Yaml();
      try {
        return DataGathererUtils.convertForTraversal(
            yaml.load(new ClassPathResource(dataSpecName.toLowerCase()).getInputStream()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      return DataGathererUtils.convertForTraversal(collectedDataMap.get(dataSpecName));
    }
  }
}
