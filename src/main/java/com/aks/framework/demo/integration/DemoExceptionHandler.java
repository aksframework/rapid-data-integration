package com.aks.framework.demo.integration;

import com.aks.framework.rdi.apiexecutor.APIExecutorException;
import java.util.function.BiFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DemoExceptionHandler {
  private final BiFunction<String, HttpHeaders, ResponseEntity<Object>> handleErrorResponse =
      (err, httpHeaders) ->
          new ResponseEntity<>(err, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleGenericException(Exception ex, WebRequest request) {
    log.error("Error: ", ex);
    return new ResponseEntity<>(ex.getCause().toString(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(APIExecutorException.class)
  public ResponseEntity<?> apiExecutorExceptionHandler(
      APIExecutorException ex, WebRequest request) {
    return handleErrorResponse.apply(
        ex.getErrorResponse().getSecond().toPrettyString(), HttpHeaders.EMPTY);
  }
}
