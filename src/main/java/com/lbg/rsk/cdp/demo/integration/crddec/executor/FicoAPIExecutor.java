package com.lbg.rsk.cdp.demo.integration.crddec.executor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowAPIExecutor;
import com.lbg.rsk.cdp.dataflow.annotations.OnResponse;
import com.lbg.rsk.cdp.dataflow.apiexecutor.AbstractAPIExecutor;
import com.lbg.rsk.cdp.dataflow.apiexecutor.DefaultAPIExecutorFlow;
import com.lbg.rsk.cdp.dataflow.apiexecutor.custom.AddEnrichHeader;
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
