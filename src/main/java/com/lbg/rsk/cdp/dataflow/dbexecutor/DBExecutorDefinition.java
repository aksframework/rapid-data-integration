package com.lbg.rsk.cdp.dataflow.dbexecutor;

import static com.lbg.rsk.cdp.dataflow.base.DataFlowUtils.createChannel;
import static com.lbg.rsk.cdp.dataflow.base.MapperUtils.convertToJson;

import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.annotations.RequestTransformer;
import com.lbg.rsk.cdp.dataflow.annotations.ResponseTransformer;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.AddEnrichHeader;
import com.lbg.rsk.cdp.dataflow.base.BeanUtils;
import com.lbg.rsk.cdp.dataflow.base.DataFlowBaseExecutor;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConfig.DBExecutorConfig;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants.SPEC_TYPE;
import com.lbg.rsk.cdp.dataflow.base.DefaultTransformer;
import com.lbg.rsk.cdp.dataflow.base.MapperUtils;
import com.lbg.rsk.cdp.dataflow.datatransformer.PayloadTransformer;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.integration.dsl.IntegrationFlowExtension;
import org.springframework.messaging.MessageHeaders;

public class DBExecutorDefinition extends IntegrationFlowExtension<DBExecutorDefinition> {
  private final DataFlowBaseExecutor dbExecutor;

  /** The Data flow. */
  private final String dataFlowName;

  private DBExecutorConfig dbExecutorConfig;

  private final Class<?> entityClass;
  private final Class<? extends CrudRepository> repositoryClass;

  public DBExecutorDefinition(
      DataFlowBaseExecutor dbExecutor,
      String dataFlowName,
      Class<?> entityClass,
      Class<? extends CrudRepository> repositoryClass) {
    this.dbExecutor = dbExecutor;
    this.dataFlowName = dataFlowName;
    this.entityClass = entityClass;
    this.repositoryClass = repositoryClass;
    this.dbExecutorConfig = BeanUtils.getDataFlowConfig().getDBExecutorConfig(dataFlowName);
  }

  public DBExecutorDefinition from(String messageChannel) {
    return channel(messageChannel)
        .enrichHeaders(
            h -> {
              h.errorChannel(
                  createChannel(dataFlowName, DataFlowConstants.DB_EXECUTOR_ERROR_CHANNEL), true);
              h.header(DataFlowConstants.DATA_FLOW_HEADER_NAME, dataFlowName);
              if (dbExecutor instanceof AddEnrichHeader) {
                ((AddEnrichHeader) dbExecutor).enrichHeader(h);
              }
            })
        .transformToMap();
  }

  public DBExecutorDefinition transformRequest() {
    return getTransformer(SPEC_TYPE.REQUEST);
  }

  public DBExecutorDefinition transformResponse() {
    return getTransformer(SPEC_TYPE.RESPONSE);
  }

  public Object joltRequestTransformer(@NotNull Map objectToTransform) {
    return getTransform(objectToTransform, dbExecutorConfig.getRequestSpec());
  }

  public Object joltResponseTransformer(@NotNull Map objectToTransform) {
    return getTransform(objectToTransform, dbExecutorConfig.getResponseSpec());
  }

  public Object requestPayloadTransformer(
      @NotNull MessageHeaders headers, @NotNull JsonNode payload) {
    return PayloadTransformer.execute(
        headers, payload, dbExecutorConfig.getRequestPayloadTransformer());
  }

  public Object responsePayloadTransformer(
      @NotNull MessageHeaders headers, @NotNull JsonNode payload) {
    return PayloadTransformer.execute(
        headers, payload, dbExecutorConfig.getResponsePayloadTransformer());
  }

  private Object getTransform(Map objectToTransform, String specName) {
    return new DefaultTransformer().transform(objectToTransform, specName);
  }

  DBExecutorDefinition executeRequest() {
    String requestText = String.format("DB-Flow [%s] ->", dataFlowName);
    String responseText = String.format("DB-Flow [%s] <-", dataFlowName);
    return log(requestText, message -> "Headers " + message.getHeaders())
        .transformToEntity()
        .handle(this, "saveEntity")
        .log(responseText, message -> "Payload " + message.getPayload());
  }

  public Object saveEntity(@NotNull Object entityObject) {
    Optional<? extends CrudRepository> optionalJpaRepository = BeanUtils.getBean(repositoryClass);
    if (optionalJpaRepository.isPresent()) {
      CrudRepository crudRepository = optionalJpaRepository.get();
      return crudRepository.save(entityObject);
    }
    throw new RuntimeException(
        "Repository not found in context. Check annotation @Repository in your repository class");
  }

  private DBExecutorDefinition getTransformer(SPEC_TYPE specType) {
    String specName =
        specType.equals(SPEC_TYPE.REQUEST)
            ? dbExecutorConfig.getRequestSpec()
            : dbExecutorConfig.getResponseSpec();
    String transformMethod =
        specType.equals(SPEC_TYPE.REQUEST) ? "joltRequestTransformer" : "joltResponseTransformer";

    Optional<String> transformerPresent =
        Arrays.stream(dbExecutor.getClass().getDeclaredMethods())
            .filter(
                m ->
                    (m.isAnnotationPresent(RequestTransformer.class)
                            && specType.equals(SPEC_TYPE.REQUEST))
                        || m.isAnnotationPresent(ResponseTransformer.class)
                            && specType.equals(SPEC_TYPE.RESPONSE))
            .map(method -> method.getName())
            .findFirst();

    Optional<String> payloadTransformerPresent = Optional.empty();
    String payloadTransformerMethod =
        specType.equals(SPEC_TYPE.REQUEST)
            ? "requestPayloadTransformer"
            : "responsePayloadTransformer";

    switch (specType) {
      case REQUEST:
        {
          payloadTransformerPresent =
              Optional.ofNullable(dbExecutorConfig.getRequestPayloadTransformer());
        }
        break;
      case RESPONSE:
        {
          payloadTransformerPresent =
              Optional.ofNullable(dbExecutorConfig.getResponsePayloadTransformer());
        }
    }

    if (payloadTransformerPresent.isPresent()) { // Execute Payload Transformer
      return transformToJsonNode()
          .transform(this, payloadTransformerMethod)
          .log(dataFlowName + " " + specType + " transformed ", message -> message.getPayload());
    } else if (null != specName) { // Apply JOLT Spec
      return transformToMap()
          .transform(this, transformMethod)
          .log(dataFlowName + " " + specType + " transformed ", message -> message.getPayload());
    } else if (transformerPresent.isPresent()) { // Execute IN-APP Transformer
      DBExecutorDefinition transformForSpec;
      if (specType.equals(SPEC_TYPE.REQUEST)) {
        transformForSpec = transformToJsonNode().transform(dbExecutor, transformerPresent.get());
      } else {
        transformForSpec = transform(dbExecutor, transformerPresent.get());
      }
      return transformForSpec.log(
          dataFlowName + " " + specType + " transformed ", message -> message.getPayload());
    } else {
      return transform(this, "dummyTransformer");
    }
  }

  DBExecutorDefinition transformToJsonNode() {
    return transform(this, "convertToJsonNode");
  }

  DBExecutorDefinition transformToEntity() {
    return transform(this, "convertToEntity");
  }

  DBExecutorDefinition transformToMap() {
    return transform(this, "convertToMap");
  }

  public JsonNode convertToJsonNode(@NotNull Object payload) {
    return convertToJson(payload);
  }

  public Map convertToMap(@NotNull Object payload) {
    return MapperUtils.convertToMap(payload);
  }

  public Object convertToEntity(@NotNull Object payload) {
    return MapperUtils.convertToType(payload, entityClass);
  }

  public Object dummyTransformer(@NotNull Object payload) {
    return payload;
  }
}
