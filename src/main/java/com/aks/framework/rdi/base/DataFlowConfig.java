package com.aks.framework.rdi.base;

import static java.util.stream.Collectors.toList;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import java.security.Key;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.ObjectUtils;

/** The type Data flow config. */
@Getter
@Setter
@Configuration
@ConfigurationProperties
@PropertySource(
    value = {"classpath:rdi-config.yml"},
    factory = RDIPropertySourceFactory.class)
public class DataFlowConfig {

  private Map<String, WebClientConfig> webClientProfile;

  private Map<String, ThreadExecutorConfig> threadExecutorProfile;

  private Map<String, RetryData> retryProfile;

  private Map<String, APIExecutorConfig> dataFlowAPIExecutor;

  private Map<String, DBExecutorConfig> dataFlowDBExecutor;

  private Map<String, SpecExecutorInfo> dataFlowSpecExecutor;

  private Map<String, DataTransformerConfig> dataFlowDataTransformer;

  private Map<String, PayloadTransformerConfig> dataFlowPayloadTransformer;

  private Map<String, DataGathererTemplateConfig> dataFlowDataGatherer;

  public WebClientConfig getWebClientByProfile(String profileName) {
    return ObjectUtils.isEmpty(webClientProfile) ? null : webClientProfile.get(profileName);
  }

  public ThreadExecutorConfig getThreadExecutorByProfile(String profileName) {
    return ObjectUtils.isEmpty(threadExecutorProfile)
        ? null
        : threadExecutorProfile.get(profileName);
  }

  /**
   * Gets retry data by flow or default.
   *
   * @param dataFlowName the data flow name
   * @return the retry data by flow or default
   */
  public RetryData getRetryDataByFlowOrDefault(String dataFlowName) {
    APIExecutorConfig apiExecutorConfig = this.dataFlowAPIExecutor.get(dataFlowName);
    if (apiExecutorConfig != null && !ObjectUtils.isEmpty(apiExecutorConfig.getRetryProfile())) {
      return this.getRetryProfile().get(apiExecutorConfig.getRetryProfile());
    }
    return this.getRetryProfile().get(ApplicationConstants.DEFAULT);
  }

  /**
   * Gets api executor.
   *
   * @param dataFlowName the data flow name
   * @return the api executor
   */
  public APIExecutorConfig getAPIExecutorConfig(String dataFlowName) {
    return this.getDataFlowAPIExecutor().get(dataFlowName);
  }

  /**
   * Gets db executor.
   *
   * @param dataFlowName the data flow name
   * @return the db executor
   */
  public DBExecutorConfig getDBExecutorConfig(String dataFlowName) {
    return this.getDataFlowDBExecutor().get(dataFlowName);
  }

  /**
   * Gets spec executor.
   *
   * @param dataFlowName the data flow name
   * @return the spec executor
   */
  public SpecExecutorInfo getSpecExecutorConfig(String dataFlowName) {
    return this.getDataFlowSpecExecutor().get(dataFlowName);
  }

  public DataGathererTemplateConfig getDataGathererConfig(String dataGathererTemplateName) {
    return this.getDataFlowDataGatherer().get(dataGathererTemplateName);
  }

  public DataTransformerConfig getDataTransformerConfig(String dataTransformerName) {
    return this.getDataFlowDataTransformer().get(dataTransformerName);
  }

  public PayloadTransformerConfig getPayloadTransformerConfig(String payloadTransformerName) {
    return this.getDataFlowPayloadTransformer().get(payloadTransformerName);
  }

  /** The type Web client config. */
  @Getter
  @Setter
  public static class WebClientConfig {
    private int maxTimeout;

    private boolean useProxy;
    private DataFlowProxy dataFlowProxy;
  }

  @Getter
  @Setter
  public static class ThreadExecutorConfig {
    private int queueCapacity;
    private int initialPoolSize;
    private int maximumPoolSize;
    private int threadKeepAliveTime;
  }

  /** The type Data flow proxy. */
  @Getter
  @Setter
  public static class DataFlowProxy {
    private String proxyHost;
    private int proxyPort;
  }

  /** The type Spec executor. */
  @Getter
  @Setter
  public static class SpecExecutorInfo {
    private String spec;
  }

  /** The type Db executor. */
  @Getter
  @Setter
  public static class DBExecutorConfig {
    private String entityClass;
    private String requestSpec;
    private String responseSpec;
    private String postExecutor;
    private String preExecutor;
    private String requestPayloadTransformer;
    private String responsePayloadTransformer;
  }

  /** The type Api executor. */
  @Getter
  @Setter
  public static class APIExecutorConfig {
    private String apiUrl;
    private String retryProfile;
    private String webClientProfile = "default";
    private String threadExecutorProfile;
    private String httpMethod;
    private String requestSpec;
    private String responseSpec;
    private UriVariables uriVariables;
    private String[] requestHeaderMappers;
    private String[] responseHeaderMappers;
    private String postExecutor;
    private String preExecutor;
    private boolean preExecutorOverride = true;
    private boolean postExecutorOverride = true;
    private String postExecutorTransformerClass;
    private String preExecutorTransformerClass;
    private String requestPayloadTransformer;
    private String responsePayloadTransformer;

    /**
     * Gets uri variables.
     *
     * @return the uri variables
     */
    public List<UriVariable> getUriVariables() {
      if (uriVariables != null) {
        if (uriVariables.expression == null
            || uriVariables.name == null
            || uriVariables.expression.length != uriVariables.name.length) {
          throw new RuntimeException(
              String.format(
                  "DataFlowConfig error uri-variables name and expression count mismatch, name: %s, definition: %s",
                  uriVariables.name, uriVariables.expression));
        }
      } else {
        return null;
      }
      return IntStream.range(0, uriVariables.name.length)
          .mapToObj(i -> new UriVariable(uriVariables.name[i], uriVariables.expression[i]))
          .collect(toList());
    }
  }

  /** The type Uri variables. */
  @Setter
  @Getter
  public static class UriVariables {
    private String[] name;
    private String[] expression;
  }

  /** The type Data gatherer template. */
  @Setter
  public static class DataGathererTemplateConfig {
    @Getter private String templateSpec;
    @Getter private String[] templateTransformers;
    private List<String[]> templatePlaceholders;
    /**
     * Gets template placeholders.
     *
     * @return the template placeholders
     */
    public List<PlaceHolder> getTemplatePlaceholders() {
      if (templatePlaceholders == null) return Collections.emptyList();
      return templatePlaceholders.stream()
          .map(strings -> new PlaceHolder(strings[0], strings[2], strings[1]))
          .collect(toList());
    }

    public void setTemplatePlaceholders(List<String[]> templatePlaceholders) {
      this.templatePlaceholders =
          templatePlaceholders.stream()
              .map(
                  strings ->
                      parseArray(strings, 3, "DataGathererTemplateConfig", "TemplatePlaceholders"))
              .collect(toList());
    }
  }

  /** The type Place holder. */
  @Getter
  @Setter
  @AllArgsConstructor
  public static class PlaceHolder {
    private String toPath;
    private String fromPath;
    private String fromSpec;

    @Override
    public String toString() {
      return String.format("%s,%s,%s", toPath, fromSpec, fromPath);
    }
  }

  /** The type Data transformer. */
  @Setter
  public static class PayloadTransformerConfig extends DataTransformerConfig {
    @Getter private String templateSpec;
    @Getter private String expressionSpec;

    /**
     * Gets replace fixed.
     *
     * @return the replace fixed
     */
    public List<TraversalPath> getReplaceFixed() {
      if (super.replaceFixed == null) return Collections.emptyList();
      return super.replaceFixed.stream()
          .map(strings -> new TraversalPath(strings[0], removeExtraDoubleQuotesChar(strings[1])))
          .collect(toList());
    }

    public void setReplaceFixed(List<String[]> replaceFixed) {
      super.replaceFixed =
          replaceFixed.stream()
              .map(strings -> parseArray(strings, 2, "PayloadTransformerConfig", "ReplaceFixed"))
              .collect(toList());
    }

    /**
     * Gets replace with.
     *
     * @return the replace with
     */
    public List<TraversalPath> getReplaceWith() {
      if (super.replaceWith == null) return Collections.emptyList();
      return super.replaceWith.stream()
          .map(strings -> new TraversalPath(strings[0], strings[1]))
          .collect(toList());
    }

    public void setReplaceWith(List<String[]> replaceWith) {
      super.replaceWith =
          replaceWith.stream()
              .map(strings -> parseArray(strings, 2, "PayloadTransformerConfig", "ReplaceWith"))
              .collect(toList());
    }

    public List<ExpressionTraversalPath> getReplaceWithType() {
      if (super.replaceWithType == null) return Collections.emptyList();
      return super.replaceWithType.stream()
          .map(
              strings ->
                  new ExpressionTraversalPath(
                      strings[0], strings[1], removeExtraDoubleQuotesChar(strings[2])))
          .collect(toList());
    }

    public void setReplaceWithType(List<String[]> replaceWithType) {
      super.replaceWithType =
          replaceWithType.stream()
              .map(strings -> parseArray(strings, 3, "PayloadTransformerConfig", "ReplaceWithType"))
              .collect(toList());
    }

    /**
     * Gets map with.
     *
     * @return the map with
     */
    public List<MapTraversalPath> getMapWith() {
      if (super.mapWith == null) return Collections.emptyList();
      return super.mapWith.stream()
          .map(strings -> new MapTraversalPath(strings[0], strings[1], strings[2]))
          .collect(toList());
    }

    public void setMapWith(List<String[]> mapWith) {
      super.mapWith =
          mapWith.stream()
              .map(strings -> parseArray(strings, 3, "PayloadTransformerConfig", "MapWith"))
              .collect(toList());
    }

    /**
     * Gets map with match.
     *
     * @return the map with match
     */
    public List<MatchTraversalPath> getMapWithMatch() {
      if (super.mapWithMatch == null) return Collections.emptyList();
      return super.mapWithMatch.stream()
          .map(strings -> new MatchTraversalPath(strings[0], strings[1], strings[2], strings[3]))
          .collect(toList());
    }

    public void setMapWithMatch(List<String[]> mapWithMatch) {
      super.mapWithMatch =
          mapWithMatch.stream()
              .map(strings -> parseArray(strings, 4, "PayloadTransformerConfig", "MapWithMatch"))
              .collect(toList());
    }

    /**
     * Gets replace with match.
     *
     * @return the replace with match
     */
    public List<MatchTraversalPath> getReplaceWithMatch() {
      if (super.replaceWithMatch == null) return Collections.emptyList();
      return super.replaceWithMatch.stream()
          .map(strings -> new MatchTraversalPath(strings[0], strings[1], strings[2], strings[3]))
          .collect(toList());
    }

    public void setReplaceWithMatch(List<String[]> replaceWithMatch) {
      super.replaceWithMatch =
          replaceWithMatch.stream()
              .map(
                  strings -> parseArray(strings, 4, "PayloadTransformerConfig", "ReplaceWithMatch"))
              .collect(toList());
    }
  }

  @Setter
  public static class DataTransformerConfig {
    private String[] dataSpec;
    @Getter private boolean inApplication;
    @Getter private String beanName;
    private List<String[]> mapWithMatch;
    private List<String[]> mapWith;
    private List<String[]> replaceFixed;
    private List<String[]> replaceWith;
    private List<String[]> replaceWithType;
    private List<String[]> replaceWithMatch;
    @Getter private List<String> removeField;

    /**
     * Gets data spec.
     *
     * @return the data spec
     */
    public DataSpec getDataSpec() {
      return new DataSpec(dataSpec[0], dataSpec[1]);
    }

    public void setDataSpec(String[] dataSpec) {
      this.dataSpec = parseArray(dataSpec, 2, "DataTransformerConfig", "DataSpec");
    }

    /**
     * Gets replace fixed.
     *
     * @return the replace fixed
     */
    public List<TraversalPath> getReplaceFixed() {
      if (replaceFixed == null) return Collections.emptyList();
      return replaceFixed.stream()
          .map(strings -> new TraversalPath(strings[0], removeExtraDoubleQuotesChar(strings[1])))
          .collect(toList());
    }

    public void setReplaceFixed(List<String[]> replaceFixed) {
      this.replaceFixed =
          replaceFixed.stream()
              .map(strings -> parseArray(strings, 2, "DataTransformerConfig", "ReplaceFixed"))
              .collect(toList());
    }

    /**
     * Gets replace with.
     *
     * @return the replace with
     */
    public List<TraversalPath> getReplaceWith() {
      if (replaceWith == null) return Collections.emptyList();
      return replaceWith.stream()
          .map(strings -> new TraversalPath(strings[0], strings[1]))
          .collect(toList());
    }

    public void setReplaceWith(List<String[]> replaceWith) {
      this.replaceWith =
          replaceWith.stream()
              .map(strings -> parseArray(strings, 2, "DataTransformerConfig", "ReplaceWith"))
              .collect(toList());
    }

    public List<ExpressionTraversalPath> getReplaceWithType() {
      if (replaceWithType == null) return Collections.emptyList();
      return replaceWithType.stream()
          .map(
              strings ->
                  new ExpressionTraversalPath(
                      strings[0], strings[1], removeExtraDoubleQuotesChar(strings[2])))
          .collect(toList());
    }

    public void setReplaceWithType(List<String[]> replaceWithType) {
      this.replaceWithType =
          replaceWithType.stream()
              .map(strings -> parseArray(strings, 3, "DataTransformerConfig", "ReplaceWithType"))
              .collect(toList());
    }

    /**
     * Gets map with.
     *
     * @return the map with
     */
    public List<MapTraversalPath> getMapWith() {
      if (mapWith == null) return Collections.emptyList();
      return mapWith.stream()
          .map(strings -> new MapTraversalPath(strings[0], strings[1], strings[2]))
          .collect(toList());
    }

    public void setMapWith(List<String[]> mapWith) {
      this.mapWith =
          mapWith.stream()
              .map(strings -> parseArray(strings, 3, "DataTransformerConfig", "MapWith"))
              .collect(toList());
    }

    /**
     * Gets map with match.
     *
     * @return the map with match
     */
    public List<MatchTraversalPath> getMapWithMatch() {
      if (mapWithMatch == null) return Collections.emptyList();
      return mapWithMatch.stream()
          .map(strings -> new MatchTraversalPath(strings[0], strings[1], strings[2], strings[3]))
          .collect(toList());
    }

    public void setMapWithMatch(List<String[]> mapWithMatch) {
      this.mapWithMatch =
          mapWithMatch.stream()
              .map(strings -> parseArray(strings, 4, "DataTransformerConfig", "MapWithMatch"))
              .collect(toList());
    }

    /**
     * Gets replace with match.
     *
     * @return the replace with match
     */
    public List<MatchTraversalPath> getReplaceWithMatch() {
      if (replaceWithMatch == null) return Collections.emptyList();
      return replaceWithMatch.stream()
          .map(strings -> new MatchTraversalPath(strings[0], strings[1], strings[2], strings[3]))
          .collect(toList());
    }

    public void setReplaceWithMatch(List<String[]> replaceWithMatch) {
      this.replaceWithMatch =
          replaceWithMatch.stream()
              .map(strings -> parseArray(strings, 4, "DataTransformerConfig", "ReplaceWithMatch"))
              .collect(toList());
    }
  }

  /** The type Data spec. */
  @Getter
  @Setter
  @AllArgsConstructor
  public static class DataSpec {
    private String toSpec;
    private String fromSpec;

    @Override
    public String toString() {
      return String.format("%s,%s", toSpec, fromSpec);
    }
  }

  /** The type Traversal path. */
  @Getter
  @Setter
  @AllArgsConstructor
  public static class TraversalPath {
    private String toPath;
    private String fromPath;

    @Override
    public String toString() {
      return String.format("%s,%s", toPath, fromPath);
    }
  }

  /** The type Map traversal path. */
  @Getter
  @Setter
  @AllArgsConstructor
  public static class MapTraversalPath {
    private String toPath;
    private String fromPath;
    private String newNode;

    @Override
    public String toString() {
      return String.format("%s,%s,%s", toPath, fromPath, newNode);
    }
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public static class ExpressionTraversalPath {
    private String toPath;
    private String fromPath;
    private String expression;

    @Override
    public String toString() {
      return String.format("%s,%s,%s", toPath, fromPath, expression);
    }
  }

  /** The type Match traversal path. */
  @Getter
  @Setter
  @AllArgsConstructor
  public static class MatchTraversalPath {
    private String matchToPath;
    private String matchFromPath;
    private String toPath;
    private String fromPath;

    @Override
    public String toString() {
      return String.format("%s,%s,%s,%s", matchToPath, matchFromPath, toPath, fromPath);
    }
  }

  /** The type Retry data. */
  @Getter
  @Setter
  public static class RetryData {
    private int maxAttempt;
    private long backOffMaxInterval;
    private long backOffInitialInterval;
    private long backOffMultiplier;
  }

  /** The type Uri variable. */
  @Setter
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class UriVariable {
    private String name;
    private String expression;
  }

  private static String[] parseArray(
      String[] stringArray, int expectedSize, String name, String type) {
    if (stringArray.length == expectedSize) {
      stringArray[0] = removeEscapeCharFromStart(stringArray[0]);
      return stringArray;
    } else {
      throw new RuntimeException(
          String.format(
              "DataFlowConfig error in [%s-%s] expected elements %d, found %d in definition [%s]",
              name, type, expectedSize, stringArray.length, String.join(",", stringArray)));
    }
  }

  private static String removeEscapeCharFromStart(String string) {
    return string.startsWith("/") ? string.substring(1) : string;
  }

  private static String removeExtraDoubleQuotesChar(String string) {
    return !string.contains("\"")
        ? string
        : string.replaceFirst("\"", "").substring(0, string.length() - 2);
  }

  @Bean
  public void defaults() {
    com.jayway.jsonpath.Configuration.setDefaults(
        new com.jayway.jsonpath.Configuration.Defaults() {

          private final JsonProvider jsonProvider = new JacksonJsonNodeJsonProvider();
          private final MappingProvider mappingProvider = new JacksonMappingProvider();

          @Override
          public JsonProvider jsonProvider() {
            return jsonProvider;
          }

          @Override
          public MappingProvider mappingProvider() {
            return mappingProvider;
          }

          @Override
          public Set<Option> options() {
            return EnumSet.noneOf(Option.class);
          }
        });
  }

  @Bean
  public Cache<Key, Map> dataFlowCache() {
    return Caffeine.newBuilder().expireAfterWrite(20, TimeUnit.SECONDS).maximumSize(10_000).build();
  }
}
