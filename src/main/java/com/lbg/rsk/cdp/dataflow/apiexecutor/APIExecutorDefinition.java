package com.lbg.rsk.cdp.dataflow.apiexecutor;

import static com.lbg.rsk.cdp.dataflow.base.DataFlowConstants.SPEC_TYPE.REQUEST;
import static com.lbg.rsk.cdp.dataflow.base.DataFlowConstants.SPEC_TYPE.RESPONSE;
import static com.lbg.rsk.cdp.dataflow.base.DataFlowUtils.createChannel;
import static com.lbg.rsk.cdp.dataflow.base.MapperUtils.convertToJson;

import com.fasterxml.jackson.databind.JsonNode;
import com.lbg.rsk.cdp.dataflow.annotations.OnRequest;
import com.lbg.rsk.cdp.dataflow.annotations.RequestTransformer;
import com.lbg.rsk.cdp.dataflow.annotations.ResponseTransformer;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.AddEnrichHeader;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.AddGatewayHandler;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.AddRequestRetryAdvice;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.RequestOverrideTransformer;
import com.lbg.rsk.cdp.dataflow.base.BeanUtils;
import com.lbg.rsk.cdp.dataflow.base.DataFlowBaseExecutor;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConfig;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConfig.APIExecutorConfig;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConfig.UriVariable;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants.CACHE_KEYS;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants.SPEC_TYPE;
import com.lbg.rsk.cdp.dataflow.base.DefaultTransformer;
import com.lbg.rsk.cdp.dataflow.base.MapperUtils;
import com.lbg.rsk.cdp.dataflow.datatransformer.PayloadTransformer;
import com.lbg.rsk.cdp.dataflow.retry.DefaultRequestRetryAdvice.DefaultRequestRetryAdviceBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpMethod;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowExtension;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler.Level;
import org.springframework.integration.handler.advice.AbstractRequestHandlerAdvice;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.http.dsl.HttpMessageHandlerSpec;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

public class APIExecutorDefinition extends IntegrationFlowExtension<APIExecutorDefinition> {

  private final APIExecutor dataFlowBaseExecutor;

  private final String dataFlowName;

  private final DataFlowConfig dataFlowConfig;

  private APIExecutorConfig apiExecutorConfig;

  public APIExecutorDefinition(DataFlowBaseExecutor dataFlowBaseExecutor, String dataFlowName) {
    this.dataFlowBaseExecutor = (APIExecutor) dataFlowBaseExecutor;
    this.dataFlowName = dataFlowName;
    this.dataFlowConfig = BeanUtils.getDataFlowConfig();
    this.apiExecutorConfig = dataFlowConfig.getAPIExecutorConfig(dataFlowName);
  }

  public APIExecutorDefinition aggregateConcurrentResponse() {
    return aggregate(a -> a.processor(this, "aggregatePayload"));
  }

  public JsonNode aggregatePayload(@NotNull List<JsonNode> payloads) {
    return convertToJson(Map.of(dataFlowName, payloads));
  }

  public APIExecutorDefinition transformRequest() {
    return preExecutor().getTransformer(REQUEST);
  }

  public APIExecutorDefinition transformResponse() {
    return getTransformer(RESPONSE).postExecutor();
  }

  private APIExecutorDefinition preExecutor() {
    String executorName = apiExecutorConfig.getPreExecutor();
    boolean doOverride = apiExecutorConfig.isPreExecutorOverride();
    APIExecutorDefinition apiExecutorDefinition = log(Level.TRACE);

    if (!doOverride
        && !ObjectUtils.isEmpty(apiExecutorConfig.getPostExecutor())
        && !apiExecutorConfig.isPostExecutorOverride()) {
      apiExecutorDefinition = transform(this, "cachePostExecutorRequest");
    }

    if (!ObjectUtils.isEmpty(executorName)) {
      IntegrationFlowAdapter integrationFlow = BeanUtils.getExecutorFlow(executorName);
      apiExecutorDefinition =
          doOverride
              ? apiExecutorDefinition.gateway(integrationFlow)
              : apiExecutorDefinition
                  .transform(this, "cachePreExecutorRequest")
                  .gateway(integrationFlow)
                  .transform(this, "cachePreExecutorResponse");
    }
    return apiExecutorDefinition;
  }

  private APIExecutorDefinition postExecutor() {
    String executorName = apiExecutorConfig.getPostExecutor();
    boolean doOverride = apiExecutorConfig.isPostExecutorOverride();

    if (!ObjectUtils.isEmpty(executorName)) {
      IntegrationFlowAdapter integrationFlow = BeanUtils.getExecutorFlow(executorName);
      if (doOverride) {
        return gateway(integrationFlow);
      } else {
        if (apiExecutorConfig.isPreExecutorOverride()) return gateway(integrationFlow);
        else return transform(this, "cachePostExecutorRequest").gateway(integrationFlow);
      }
    }
    return log(Level.TRACE);
  }

  public APIExecutorDefinition executeRequest() {
    return applyContentTypeHeader().getExecutor();
  }

  private APIExecutorDefinition applyContentTypeHeader() {
    return enrichHeaders(
        h1 -> {
          h1.header("Content-Type", "application/json; charset=utf-8");
        });
  }

  public APIExecutorDefinition from(String messageChannel) {
    return channel(messageChannel).applyDefaultHeaders();
  }

  private APIExecutorDefinition applyDefaultHeaders() {
    return transformToMap()
        .enrichHeaders(
            h1 -> {
              h1.errorChannel(
                  createChannel(dataFlowName, DataFlowConstants.API_EXECUTOR_ERROR_CHANNEL), true);
              h1.header(DataFlowConstants.DATA_FLOW_HEADER_NAME, dataFlowName);
              // h1.header("Content-Type", "application/json");
            });
  }

  private APIExecutorDefinition applyCustomHeaders(boolean isOverride) {
    if (dataFlowBaseExecutor instanceof AddEnrichHeader) {
      boolean onRequestAnnotationPresent = false;
      try {
        onRequestAnnotationPresent =
            dataFlowBaseExecutor
                .getClass()
                .getMethod("enrichHeader", HeaderEnricherSpec.class)
                .isAnnotationPresent(OnRequest.class);
      } catch (NoSuchMethodException e) {
        new RuntimeException("enrichHeader method not present");
      }

      if (isOverride) {
        if (!onRequestAnnotationPresent) {
          return customHeaders();
        } else {
          return log(
              Level.WARN,
              "Invalid Configuration",
              message ->
                  String.format(
                      "AddEnrichHeader can't apply as @OnRequest, if *-executor-override is true. Check %s configuration.",
                      dataFlowName));
        }
      } else {
        if (!onRequestAnnotationPresent) {
          return customHeaders();
        } else {
          return transform(this, "getPreExecutorRequest").customHeaders();
        }
      }
    }
    return log(Level.TRACE);
  }

  private APIExecutorDefinition customHeaders() {
    return transformToMap()
        .enrichHeaders(h1 -> ((AddEnrichHeader) dataFlowBaseExecutor).enrichHeader(h1));
  }

  public APIExecutorDefinition addConcurrency() {
    return transformToJsonNode()
        .split(dataFlowBaseExecutor)
        .channel(MessageChannels.executor(Executors.newCachedThreadPool()))
        .transformToJsonNode()
        .enrichHeaders(
            h -> {
              h.errorChannel(
                  createChannel(dataFlowName, DataFlowConstants.API_EXECUTOR_ERROR_CHANNEL), true);
            });
  }

  public Object joltRequestTransformer(@NotNull Map objectToTransform) {
    return getTransform(objectToTransform, apiExecutorConfig.getRequestSpec());
  }

  public Object joltResponseTransformer(@NotNull Map objectToTransform) {
    return getTransform(objectToTransform, apiExecutorConfig.getResponseSpec());
  }

  private Object getTransform(@NotNull Map objectToTransform, String specName) {
    return new DefaultTransformer().transform(objectToTransform, specName);
  }

  private APIExecutorDefinition getTransformer(SPEC_TYPE specType) {
    String specName =
        specType.equals(REQUEST)
            ? apiExecutorConfig.getRequestSpec()
            : apiExecutorConfig.getResponseSpec();
    String transformMethod =
        specType.equals(REQUEST) ? "joltRequestTransformer" : "joltResponseTransformer";

    Optional<String> transformerPresent =
        Arrays.stream(dataFlowBaseExecutor.getClass().getDeclaredMethods())
            .filter(
                m ->
                    (m.isAnnotationPresent(RequestTransformer.class) && specType.equals(REQUEST))
                        || (m.isAnnotationPresent(ResponseTransformer.class)
                            && specType.equals(RESPONSE)))
            .map(method -> method.getName())
            .findFirst();

    Optional<String> payloadTransformerPresent = Optional.empty();
    String payloadTransformerMethod =
        specType.equals(REQUEST) ? "requestPayloadTransformer" : "responsePayloadTransformer";

    if (specType == REQUEST) {
      payloadTransformerPresent =
          Optional.ofNullable(apiExecutorConfig.getRequestPayloadTransformer());
    } else if (specType == RESPONSE) {
      payloadTransformerPresent =
          Optional.ofNullable(apiExecutorConfig.getResponsePayloadTransformer());
    }

    APIExecutorDefinition transformerDefinition;

    if (null != specName) { // Apply JOLT Spec
      transformerDefinition =
          applyPreExecutor(specType)
              .transformToMap()
              .transform(this, transformMethod)
              .log(dataFlowName + " " + specType + " transformed ", Message::getPayload)
              .transformToJsonNode();
    } else if (transformerPresent.isPresent()) { // Execute IN-APP Transformer
      transformerDefinition =
          applyPreExecutor(specType)
              .transformToJsonNode()
              .transform(dataFlowBaseExecutor, transformerPresent.get())
              .log(dataFlowName + " " + specType + " transformed ", Message::getPayload);
    } else {
      transformerDefinition =
          applyPreExecutor(specType)
              .transformToJsonNode()
              .transform(this, "dummyTransformer"); // Adding this for debugging purpose.
    }

    if (payloadTransformerPresent.isPresent()) { // Execute Payload Transformer
      return transformerDefinition
          .transformToJsonNode()
          .transform(this, payloadTransformerMethod)
          .log(dataFlowName + " " + specType + " transformed ", Message::getPayload)
          .applyPostExecutor(specType);
    } else {
      return transformerDefinition.applyPostExecutor(specType);
    }
  }

  private APIExecutorDefinition applyPreExecutor(SPEC_TYPE specType) {
    if (!specType.equals(REQUEST)) {
      return log(Level.TRACE);
    }
    APIExecutorDefinition apiExecutorDefinition =
        applyCustomHeaders(apiExecutorConfig.isPreExecutorOverride());

    if (!ObjectUtils.isEmpty(apiExecutorConfig.getPreExecutor())
        && !apiExecutorConfig
            .isPreExecutorOverride()) { // If pre-executor present and override is false.

      if (!ObjectUtils.isEmpty(
          apiExecutorConfig
              .getPreExecutorTransformerClass())) { // If transformer class present set request and
        // response for transformer class
        RequestOverrideTransformer requestOverrideTransformer =
            BeanUtils.getRequestOverrideTransformer(
                apiExecutorConfig.getPreExecutorTransformerClass());
        apiExecutorDefinition =
            apiExecutorDefinition
                .transform(this, "preExecutorRequestAndResponse")
                .transform(requestOverrideTransformer, "transformWithRequestAndResponse");
      }
    }
    return apiExecutorDefinition;
  }

  private APIExecutorDefinition applyPostExecutor(SPEC_TYPE specType) {
    if (specType.equals(RESPONSE)
        && !ObjectUtils.isEmpty(apiExecutorConfig.getPostExecutor())
        && !apiExecutorConfig.isPostExecutorOverride()) {
      if (!ObjectUtils.isEmpty(apiExecutorConfig.getPostExecutorTransformerClass())) {
        RequestOverrideTransformer requestOverrideTransformer =
            BeanUtils.getRequestOverrideTransformer(
                apiExecutorConfig.getPostExecutorTransformerClass());
        return transform(this, "cachePostExecutorResponse")
            .transform(this, "postExecutorRequestAndResponse")
            .transform(requestOverrideTransformer, "transformWithRequestAndResponse");
      } else {
        return transform(this, "getPostExecutorRequest");
      }
    } else {
      return log(Level.TRACE);
    }
  }

  private APIExecutorDefinition getExecutor() {
    Optional<HttpMethod> httpMethodOptional = getHttpMethod();
    HttpMethod httpMethod =
        httpMethodOptional.isPresent() ? httpMethodOptional.get() : HttpMethod.GET;

    String requestText = String.format("API-Flow [%s:%s] ->", httpMethod, dataFlowName);
    String responseText = String.format("API-Flow [%s:%s] <-", httpMethod, dataFlowName);

    APIExecutorDefinition logRequest =
        log(requestText, message -> "Headers " + message.getHeaders());

    if (dataFlowBaseExecutor instanceof AddGatewayHandler) {
      return logRequest
          .transformToJsonNode()
          .handle(
              h -> ((AddGatewayHandler) dataFlowBaseExecutor).configureGateway(h),
              e -> e.advice(getAdvice()))
          .log(responseText, message -> "Payload " + message.getPayload());
    } else {
      return logRequest
          .handle(getOutBoundGateway(httpMethod), e -> e.advice(getAdvice()))
          .log(responseText);
    }
  }

  private HttpMessageHandlerSpec getOutBoundGateway(HttpMethod httpMethod) {
    HttpMessageHandlerSpec httpMessageHandlerSpec =
        Http.outboundGateway(apiExecutorConfig.getApiUrl(), restTemplate())
            .httpMethod(httpMethod)
            .expectedResponseType(JsonNode.class);

    Map<String, Expression> uriExpressionMap = getURIExpressionMap();

    if (!ObjectUtils.isEmpty(uriExpressionMap)) {
      httpMessageHandlerSpec.uriVariableExpressions(uriExpressionMap);
    }

    if (!ObjectUtils.isEmpty(apiExecutorConfig.getRequestHeaderMappers())) {
      httpMessageHandlerSpec.mappedRequestHeaders(apiExecutorConfig.getRequestHeaderMappers());
    }
    if (!ObjectUtils.isEmpty(apiExecutorConfig.getResponseHeaderMappers())) {
      httpMessageHandlerSpec.mappedResponseHeaders(apiExecutorConfig.getResponseHeaderMappers());
    }
    return httpMessageHandlerSpec;
  }

  private Map<String, Expression> getURIExpressionMap() {
    List<UriVariable> uriVariables = apiExecutorConfig.getUriVariables();
    if (!ObjectUtils.isEmpty(uriVariables)) {
      SpelExpressionParser parser = new SpelExpressionParser();
      return uriVariables.stream()
          .collect(
              Collectors.toMap(
                  UriVariable::getName, v -> parser.parseExpression(v.getExpression())));
    }
    return Collections.emptyMap();
  }

  private RestTemplate restTemplate() {
    return BeanUtils.getRestTemplate(apiExecutorConfig.getWebClientProfile());
  }

  private Optional<HttpMethod> getHttpMethod() {
    return Optional.ofNullable(HttpMethod.resolve(apiExecutorConfig.getHttpMethod()));
  }

  private AbstractRequestHandlerAdvice getAdvice() {
    if (dataFlowBaseExecutor instanceof AddRequestRetryAdvice) {
      return ((AddRequestRetryAdvice) dataFlowBaseExecutor)
          .configureRequestRetryAdvice()
          .get(dataFlowName);
    } else if (null != apiExecutorConfig.getRetryProfile()) {
      return new DefaultRequestRetryAdviceBuilder<String>(dataFlowConfig).build().get(dataFlowName);
    } else {
      return dummyRequestHandlerAdvice().get();
    }
  }

  private APIExecutorDefinition transformToJsonNode() {
    return transform(this, "convertToJsonNode");
  }

  private APIExecutorDefinition transformToMap() {
    return transform(this, "convertToMap");
  }

  public JsonNode convertToJsonNode(@NotNull Object payload) {
    return convertToJson(payload);
  }

  public Map convertToMap(@NotNull Object payload) {
    return MapperUtils.convertToMap(payload);
  }

  private Supplier<AbstractRequestHandlerAdvice> dummyRequestHandlerAdvice() {
    return () ->
        new AbstractRequestHandlerAdvice() {
          @Override
          protected Object doInvoke(ExecutionCallback callback, Object target, Message<?> message) {
            return callback.execute();
          }
        };
  }

  public Object dummyTransformer(@NotNull MessageHeaders headers, @NotNull Object payload) {
    return payload;
  }

  public Object requestPayloadTransformer(
      @NotNull MessageHeaders headers, @NotNull Object payload) {
    return PayloadTransformer.execute(
        headers, payload, apiExecutorConfig.getRequestPayloadTransformer());
  }

  public Object responsePayloadTransformer(
      @NotNull MessageHeaders headers, @NotNull Object payload) {
    return PayloadTransformer.execute(
        headers, payload, apiExecutorConfig.getResponsePayloadTransformer());
  }

  public Object cachePreExecutorRequest(@NotNull MessageHeaders headers, @NotNull Object payload) {
    dataFlowBaseExecutor.saveExecutorPayload(
        cacheKey(headers, CACHE_KEYS.PRE_EXECUTOR_REQUEST), payload);
    return payload;
  }

  public Object cachePreExecutorResponse(@NotNull MessageHeaders headers, @NotNull Object payload) {
    dataFlowBaseExecutor.saveExecutorPayload(
        cacheKey(headers, CACHE_KEYS.PRE_EXECUTOR_RESPONSE), payload);
    return payload;
  }

  public Object cachePostExecutorRequest(@NotNull MessageHeaders headers, @NotNull Object payload) {
    dataFlowBaseExecutor.saveExecutorPayload(
        cacheKey(headers, CACHE_KEYS.POST_EXECUTOR_REQUEST), payload);
    return payload;
  }

  public Object cachePostExecutorResponse(
      @NotNull MessageHeaders headers, @NotNull Object payload) {
    dataFlowBaseExecutor.saveExecutorPayload(
        cacheKey(headers, CACHE_KEYS.POST_EXECUTOR_RESPONSE), payload);
    return payload;
  }

  public APIFlowData preExecutorRequestAndResponse(
      @NotNull MessageHeaders headers, @NotNull Object payload) {
    return new APIFlowData(
        MapperUtils.convertToJson(getPayload(headers, CACHE_KEYS.PRE_EXECUTOR_REQUEST)),
        MapperUtils.convertToJson(getPayload(headers, CACHE_KEYS.PRE_EXECUTOR_RESPONSE)));
  }

  public APIFlowData postExecutorRequestAndResponse(
      @NotNull MessageHeaders headers, @NotNull Object payload) {
    return new APIFlowData(
        MapperUtils.convertToJson(getPayload(headers, CACHE_KEYS.POST_EXECUTOR_REQUEST)),
        MapperUtils.convertToJson(getPayload(headers, CACHE_KEYS.POST_EXECUTOR_RESPONSE)));
  }

  private String cacheKey(MessageHeaders headers, CACHE_KEYS cacheKeys) {
    return String.format(
        "%s.%s.%s.%s",
        headers.get(IntegrationMessageHeaderAccessor.CORRELATION_ID),
        dataFlowName,
        cacheKeys.getExecutor(),
        cacheKeys.getSpec());
  }

  private Map getPayload(@NotNull MessageHeaders headers, @NotNull CACHE_KEYS cacheKeys) {
    return dataFlowBaseExecutor.getExecutorPayload(cacheKey(headers, cacheKeys));
  }
}
