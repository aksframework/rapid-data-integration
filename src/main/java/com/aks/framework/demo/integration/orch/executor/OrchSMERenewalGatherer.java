package com.aks.framework.demo.integration.orch.executor;

import com.aks.framework.rdi.annotations.DataFlowGatherer;
import com.aks.framework.rdi.datagatherer.AbstractDataFlowGatherer;
import org.springframework.stereotype.Component;

@Component
@DataFlowGatherer(template = "smeRenewalGatherer")
public class OrchSMERenewalGatherer extends AbstractDataFlowGatherer {}
