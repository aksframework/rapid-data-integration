package com.lbg.rsk.cdp.demo.integration.orch.executor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowGatherer;
import com.lbg.rsk.cdp.dataflow.datagatherer.AbstractDataFlowGatherer;
import org.springframework.stereotype.Component;

@Component
@DataFlowGatherer(template = "smeRenewalGatherer")
public class OrchSMERenewalGatherer extends AbstractDataFlowGatherer {}
