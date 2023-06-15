package com.aks.framework.rdi.base;

/** The type Data flow constants. */
public class ApplicationConstants {
  /** The constant API_EXECUTOR_CHANNEL. */
  public static final String API_EXECUTOR_CHANNEL = "APIExecutorChannel";
  /** The constant SPEC_EXECUTOR_CHANNEL. */
  public static final String SPEC_EXECUTOR_CHANNEL = "SPECExecutorChannel";
  /** The constant DB_EXECUTOR_CHANNEL. */
  public static final String DB_EXECUTOR_CHANNEL = "DBExecutorChannel";
  /** The constant API_EXECUTOR_ERROR_CHANNEL. */
  public static final String API_EXECUTOR_ERROR_CHANNEL = "APIErrorChannel";
  /** The constant SPEC_EXECUTOR_ERROR_CHANNEL. */
  public static final String SPEC_EXECUTOR_ERROR_CHANNEL = "SPECErrorChannel";
  /** The constant DB_EXECUTOR_ERROR_CHANNEL. */
  public static final String DB_EXECUTOR_ERROR_CHANNEL = "DBErrorChannel";
  /** The constant DATA_FLOW_HEADER_NAME. */
  public static final String DATA_FLOW_HEADER_NAME = "DataFlow";

  public static final String API_EXECUTOR_TEXT = "ThreadExecutor";

  /** The constant ORIGINAL_ERROR_CHANNEL. */
  public static final String ORIGINAL_ERROR_CHANNEL = "originalErrorChannel";
  /** The constant RETRY_PROFILE. */
  public static final String RETRY_PROFILE = "RetryProfile";
  /** The constant DEFAULT. */
  public static final String DEFAULT = "default";
  /** The constant ARRAY_ZEROTH_ELEMENT. */
  public static final String ARRAY_ZEROTH_ELEMENT = "[0]";
  /** The constant ARRAY_RECURSIVE_STRING_SPLITTER. */
  public static final String ARRAY_RECURSIVE_STRING_SPLITTER = "\\[\\*\\]";
  /** The constant ARRAY_RECURSIVE_STRING. */
  public static final String ARRAY_RECURSIVE_STRING = "[*]";
  /** The constant EMPTY_STRING. */
  public static final String EMPTY_STRING = "";
  /** The constant TRAVERSAL_PATH_DELIMITER. */
  public static final String TRAVERSAL_PATH_DELIMITER = ".";
  /** The constant ROOT_OBJECT_PARENTHESIS. */
  public static final String ROOT_OBJECT_PARENTHESIS = "{}";
  /** The constant DEFAULT_NODE_NAME_IF_SOURCE_IS_ROOT. */
  public static final String DEFAULT_NODE_NAME_IF_SOURCE_IS_ROOT = "output";
  /** The constant DATA_FLOW_STRING. */
  public static final String DATA_FLOW_STRING = "dataFlow";

  public static final String DEFAULT_NODE_NAME_IF_OBJECT_IS_STRING = "value";

  public static final int DEFAULT_CONNECT_TIMEOUT = 10000;
  public static final int DEFAULT_READ_TIMEOUT = 10000;
  public static final int DEFAULT_THREAD_EXECUTOR_QUEUE_CAPACITY = 1000;
  public static final int DEFAULT_THREAD_EXECUTOR_INITIAL_POOL_SIZE = 5;
  public static final int DEFAULT_THREAD_EXECUTOR_MAXIMUM_POOL_SIZE = 20;
  public static final long DEFAULT_THREAD_EXECUTOR_THREAD_KEEP_ALIVE_TIME = 20;

  public enum SPEC_TYPE {
    REQUEST,
    RESPONSE;
  }

  public enum EXECUTOR_TYPE {
    PRE_EXECUTOR,
    POST_EXECUTOR;
  }

  public enum CACHE_KEYS {
    PRE_EXECUTOR_REQUEST(SPEC_TYPE.REQUEST, EXECUTOR_TYPE.PRE_EXECUTOR),
    PRE_EXECUTOR_RESPONSE(SPEC_TYPE.RESPONSE, EXECUTOR_TYPE.PRE_EXECUTOR),
    POST_EXECUTOR_REQUEST(SPEC_TYPE.REQUEST, EXECUTOR_TYPE.POST_EXECUTOR),
    POST_EXECUTOR_RESPONSE(SPEC_TYPE.RESPONSE, EXECUTOR_TYPE.POST_EXECUTOR);

    private EXECUTOR_TYPE executorType;
    private SPEC_TYPE specType;

    CACHE_KEYS(SPEC_TYPE specType, EXECUTOR_TYPE executorType) {
      this.specType = specType;
      this.executorType = executorType;
    }

    public EXECUTOR_TYPE getExecutor() {
      return executorType;
    }

    public SPEC_TYPE getSpec() {
      return specType;
    }
  }
}
