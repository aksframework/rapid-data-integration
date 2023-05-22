package com.lbg.rsk.cdp.demo.integration;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class DemoController {
  @GetMapping()
  public ResponseEntity<String> dataFlowRequest() {
    return ResponseEntity.ok().body("DataFlow demo");
  }
}
