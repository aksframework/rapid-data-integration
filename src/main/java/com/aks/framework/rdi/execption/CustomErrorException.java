package com.lbg.rsk.cdp.dataflow.execption;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Objects;
import org.springframework.data.util.Pair;

public class CustomErrorException extends DataFlowException {
  private static final long serialVersionUID = 1L;

  private final ErrorObjects errorObjects;

  /**
   * constructor.
   *
   * @param status http status
   * @param message exception message
   * @param errorObjects custom error objects, not {@code null}
   */
  public CustomErrorException(int status, String message, ErrorObjects errorObjects) {
    this(status, message, null, errorObjects);
  }

  /**
   * constructor.
   *
   * @param status http status
   * @param message exception message
   * @param cause the cause
   * @param errorObjects custom error objects, not {@code null}
   */
  public CustomErrorException(
      int status, String message, Throwable cause, ErrorObjects errorObjects) {
    super(status, message, cause, null);
    this.errorObjects = Objects.requireNonNull(errorObjects, "errorObjects must not be null");
  }

  @Override
  public Pair<Integer, JsonNode> getErrorResponse() {
    return buildCustomResponse();
  }

  @Override
  public Pair<Integer, JsonNode> getFullErrorResponse() {
    return buildCustomResponse();
  }

  private Pair<Integer, JsonNode> buildCustomResponse() {
    // TODO: should support for encoding custom responses be added?
    JsonNode responseBody = OBJECT_MAPPER.convertValue(errorObjects, JsonNode.class);
    return Pair.of(getStatus(), responseBody);
  }
}
