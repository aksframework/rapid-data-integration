package com.lbg.rsk.cdp.dataflow.execption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;

public class ErrorObjects {
  @Getter private final List<Map<String, Object>> errors;

  public ErrorObjects(List<Map<String, Object>> errors) {
    this.errors = Objects.requireNonNull(errors, "errors must not be null");
  }

  public static ErrorObjectsBuilder builder() {
    return new ErrorObjectsBuilder();
  }

  /** ErrorObjectsBuilder. */
  public static class ErrorObjectsBuilder {
    private final List<Map<String, Object>> errors;
    private Map<String, Object> currentError;

    ErrorObjectsBuilder() {
      this.errors = new ArrayList<>();
    }

    public ErrorObjectsBuilder withId(String id) {
      return with("id", id);
    }

    public ErrorObjectsBuilder withStatus(String status) {
      return with("status", status);
    }

    public ErrorObjectsBuilder withCode(String code) {
      return with("code", code);
    }

    public ErrorObjectsBuilder withTitle(String title) {
      return with("title", title);
    }

    public ErrorObjectsBuilder withDetail(String detail) {
      return with("detail", detail);
    }

    public ErrorObjectsBuilder with(String key, Object value) {
      if (currentError == null) {
        throw new UnsupportedOperationException("Must add an error before calling with");
      }
      currentError.put(key, value);
      return this;
    }

    public ErrorObjectsBuilder addError() {
      validateCurrentError();
      Map<String, Object> error = new HashMap<>();
      errors.add(error);
      currentError = error;
      return this;
    }

    public ErrorObjects build() {
      if (errors.isEmpty()) {
        throw new IllegalArgumentException("At least one error is required");
      }
      validateCurrentError();
      return new ErrorObjects(errors);
    }

    private void validateCurrentError() throws IllegalArgumentException {
      if (currentError != null && currentError.isEmpty()) {
        throw new IllegalArgumentException("Error must contain at least one key-value pair");
      }
    }
  }
}
