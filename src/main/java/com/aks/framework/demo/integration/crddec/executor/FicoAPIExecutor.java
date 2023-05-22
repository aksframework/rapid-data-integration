package com.aks.framework.demo.integration.crddec.executor;

import com.aks.framework.rdi.annotations.DataFlowAPIExecutor;
import com.aks.framework.rdi.annotations.OnResponse;
import com.aks.framework.rdi.apiexecutor.AbstractAPIExecutor;
import com.aks.framework.rdi.apiexecutor.DefaultAPIExecutorFlow;
import com.aks.framework.rdi.apiexecutor.custom.AddEnrichHeader;
import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "fico", type = DefaultAPIExecutorFlow.class)
public class FicoAPIExecutor extends AbstractAPIExecutor implements AddEnrichHeader {

  @Override
  @OnResponse
  public void enrichHeader(HeaderEnricherSpec headerEnricherSpec) {
    headerEnricherSpec.headerExpression("jwt", "payload.jwt");
  }
}
