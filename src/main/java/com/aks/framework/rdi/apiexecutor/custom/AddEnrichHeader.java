package com.aks.framework.rdi.apiexecutor.custom;

import org.springframework.integration.dsl.HeaderEnricherSpec;

/** The interface Add enrich header. */
public interface AddEnrichHeader {
  /**
   * Enrich header.
   *
   * @param headerEnricherSpec the header enricher spec
   */
  void enrichHeader(HeaderEnricherSpec headerEnricherSpec);
}
