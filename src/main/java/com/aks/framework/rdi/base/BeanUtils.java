package com.aks.framework.rdi.base;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.aks.framework.rdi.apiexecutor.custom.RequestOverrideTransformer;
import com.aks.framework.rdi.base.DataFlowConfig.ThreadExecutorConfig;
import com.aks.framework.rdi.base.DataFlowConfig.WebClientConfig;
import com.aks.framework.rdi.datatransformer.InApplicationDataTransformer;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.web.client.RestTemplate;

/** The type Bean utils. */
@Slf4j
public class BeanUtils {

  private static ApplicationContext context;
  private static DataFlowConfig dataFlowConfig;
  private static Map<String, RestTemplate> dataFlowRestTemplateMap = new HashMap<>(1);

  private BeanUtils() {}

  private static final Map<String, Class<? extends AbstractBaseExecutor>> executorBeanDictionary =
      new HashMap<>(2);

  /**
   * Gets bean.
   *
   * @param <T> the type parameter
   * @param beanClass the bean class
   * @return the bean
   */
  public static <T> Optional<T> getBean(Class<T> beanClass) {
    try {
      return Optional.of(context.getBean(beanClass));
    } catch (BeansException beansException) {
      log.error("Bean '{}' is not present in context", beanClass.getCanonicalName());
      return Optional.empty();
    }
  }

  /**
   * Gets bean.
   *
   * @param <T> the type parameter
   * @param beanName the bean name
   * @param beanClass the bean class
   * @return the bean
   */
  public static <T> Optional<T> getBean(String beanName, Class<T> beanClass) {
    try {
      return Optional.of(context.getBean(beanName, beanClass));
    } catch (BeansException beansException) {
      log.error(
          "Bean '{}' of type '{}' is not present in context",
          beanName,
          beanClass.getCanonicalName());
      return Optional.empty();
    }
  }

  public static Cache<String, Map<String, Object>> getCache() {
    return getBean("dataFlowCache", Cache.class)
        .orElseThrow(() -> new RuntimeException("BeanUtils unable to find dataFlowCache bean."));
  }

  /**
   * Sets context.
   *
   * @param context the context
   */
  public static synchronized void setContext(ApplicationContext context) {
    BeanUtils.context = context;
  }

  /**
   * Gets object mapper.
   *
   * @return the object mapper
   */
  public static ObjectMapper getObjectMapper() {
    Optional<ObjectMapper> objectMapperOptional = getBean(ObjectMapper.class);
    return objectMapperOptional.orElseGet(ObjectMapper::new);
  }

  public static DataFlowConfig getDataFlowConfig() {
    if (null == dataFlowConfig) {
      dataFlowConfig =
          getBean(DataFlowConfig.class)
              .orElseThrow(
                  () ->
                      new RuntimeException(
                          "BeanUtils unable to find DataFlowConfig bean. Please re-check RDI config file to fix error."));
    }
    return dataFlowConfig;
  }

  /**
   * Gets rest template.
   *
   * @return the rest template
   */
  public static RestTemplate getRestTemplate(String webClientProfile) {
    if (dataFlowRestTemplateMap.containsKey(webClientProfile)) {
      return dataFlowRestTemplateMap.get(webClientProfile);
    } else {
      return generateDataFlowRestTemplate(webClientProfile);
    }
  }

  private static RestTemplate generateDataFlowRestTemplate(String webClientProfile) {
    getDataFlowConfig();
    Optional<WebClientConfig> webClientConfigOptional =
        Optional.ofNullable(dataFlowConfig.getWebClientByProfile(webClientProfile));

    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(ApplicationConstants.DEFAULT_CONNECT_TIMEOUT);
    requestFactory.setReadTimeout(ApplicationConstants.DEFAULT_READ_TIMEOUT);
    // requestFactory.setBufferRequestBody(false);

    if (webClientConfigOptional.isPresent()) {
      WebClientConfig webClientConfig = webClientConfigOptional.get();
      requestFactory.setConnectTimeout(webClientConfig.getMaxTimeout());
      requestFactory.setReadTimeout(webClientConfig.getMaxTimeout());
      if (webClientConfig.isUseProxy()) {
        Proxy proxy =
            new Proxy(
                Proxy.Type.HTTP,
                new InetSocketAddress(
                    webClientConfig.getDataFlowProxy().getProxyHost(),
                    webClientConfig.getDataFlowProxy().getProxyPort()));
        requestFactory.setProxy(proxy);
      }
    }
    dataFlowRestTemplateMap.put(
        webClientProfile,
        new GenerateTypeBean<RestTemplate>().get(RestTemplate.class, requestFactory));
    return dataFlowRestTemplateMap.get(webClientProfile);
  }

  public static IntegrationFlowAdapter getExecutorFlow(String executorName) {
    Optional<IntegrationFlowAdapter> integrationFlowAdapterOptional =
        getBean(executorName, IntegrationFlowAdapter.class);
    if (integrationFlowAdapterOptional.isPresent()) {
      return integrationFlowAdapterOptional.get();
    } else {
      if (executorBeanDictionary.containsKey(executorName)) {
        Optional<? extends AbstractBaseExecutor> abstractBaseExecutor =
            getBean(executorBeanDictionary.get(executorName));
        return abstractBaseExecutor.orElse(null);
      }
    }
    return null;
  }

  public static InApplicationDataTransformer getInApplicationDataTransformer(
      String beanName, Class<InApplicationDataTransformer> applicationTransformerClass) {
    Optional<InApplicationDataTransformer> applicationTransformerOptional =
        getBean(beanName, applicationTransformerClass);
    return applicationTransformerOptional.orElse(null);
  }

  public static RequestOverrideTransformer getRequestOverrideTransformer(String beanName) {
    Optional<RequestOverrideTransformer> requestOverrideTransformer =
        getBean(beanName, RequestOverrideTransformer.class);
    return requestOverrideTransformer.orElse(null);
  }

  public static void addExecutor(
      String executorName, Class<? extends AbstractBaseExecutor> executorClass) {
    executorBeanDictionary.put(executorName, executorClass);
  }

  public static Executor getConcurrentAPIThreadExecutor(String threadExecutorProfile) {
    getDataFlowConfig();
    Optional<ThreadExecutorConfig> threadExecutorByProfile =
        Optional.ofNullable(dataFlowConfig.getThreadExecutorByProfile(threadExecutorProfile));

    if (threadExecutorByProfile.isPresent()) {
      ThreadExecutorConfig threadExecutorConfig = threadExecutorByProfile.get();
      ArrayBlockingQueue<Runnable> boundedQueue =
          new ArrayBlockingQueue<>(threadExecutorConfig.getQueueCapacity());
      return new ThreadPoolExecutor(
          threadExecutorConfig.getInitialPoolSize(),
          threadExecutorConfig.getMaximumPoolSize(),
          threadExecutorConfig.getThreadKeepAliveTime(),
          SECONDS,
          boundedQueue,
          new AbortPolicy());
    } else {
      ArrayBlockingQueue<Runnable> boundedQueue =
          new ArrayBlockingQueue<>(ApplicationConstants.DEFAULT_THREAD_EXECUTOR_QUEUE_CAPACITY);
      return new ThreadPoolExecutor(
          ApplicationConstants.DEFAULT_THREAD_EXECUTOR_INITIAL_POOL_SIZE,
          ApplicationConstants.DEFAULT_THREAD_EXECUTOR_MAXIMUM_POOL_SIZE,
          ApplicationConstants.DEFAULT_THREAD_EXECUTOR_THREAD_KEEP_ALIVE_TIME,
          SECONDS,
          boundedQueue,
          new AbortPolicy());
    }
  }
}
