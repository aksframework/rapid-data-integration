package com.lbg.rsk.cdp.dataflow.execption;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.util.ObjectUtils;

@Slf4j
public abstract class DataFlowException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  protected final int status;
  private final Optional<Supplier<String>> verboseMessageSupplier;

  public DataFlowException(int status, String message) {
    this(status, message, (Throwable) null, null);
  }

  public DataFlowException(
      int status, String message, Throwable cause, Supplier<String> verboseMessageSupplier) {
    super(message, cause, true, log.isTraceEnabled());
    this.status = status;
    this.verboseMessageSupplier = Optional.ofNullable(verboseMessageSupplier);
  }

  private String formatExceptionCause(Throwable e) {
    // if the throwable has a cause use that, otherwise the throwable is the cause
    Throwable error = e.getCause() == null ? e : e.getCause();
    return error != null
        ? ObjectUtils.isEmpty(error.getMessage()) ? error.toString() : error.getMessage()
        : null;
  }

  public Pair<Integer, JsonNode> getErrorResponse() {
    return buildResponse(getMessage());
  }

  public Pair<Integer, JsonNode> getFullErrorResponse() {
    return buildResponse(getMessageWithCause());
  }

  private Pair<Integer, JsonNode> buildResponse(String message) {
    String errorDetail = message;

    ErrorObjects errors = ErrorObjects.builder().addError().withDetail(errorDetail).build();
    JsonNode responseBody = OBJECT_MAPPER.convertValue(errors, JsonNode.class);

    return Pair.of(getStatus(), responseBody);
  }

  public String getMessageWithCause() {
    String result =
        Optional.ofNullable(getCause()).isPresent()
            ? formatExceptionCause(getCause())
            : getMessage();
    if (result.equals(this.getMessage())) {
      return result;
    } else {
      return String.format("%s %s", this.getMessage(), result);
    }
  }

  public String getMessageWithRootCause() {
    String result =
        Optional.ofNullable(getCause()).isPresent()
            ? formatExceptionCause(getCause())
            : getMessage();
    if (result.equals(this.getMessage())) {
      return result;
    } else {
      return String.format("%s %s", this.getMessage(), result);
    }
  }

  public int getStatus() {
    return status;
  }

  @Override
  public String toString() {
    String message = getMessage();
    String className = getClass().getSimpleName();

    if (message == null) {
      message = className;
    } else {
      message = className + ": " + message;
    }

    return message;
  }
}
