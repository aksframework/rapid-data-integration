package com.aks.framework.demo.integration.datasourcing.executor;

import com.aks.framework.rdi.annotations.DataFlowGatherer;
import com.aks.framework.rdi.datagatherer.AbstractDataFlowGatherer;
import org.springframework.stereotype.Component;

@Component
@DataFlowGatherer(template = "smeRenewal")
public class DataSourcingDataGatherer extends AbstractDataFlowGatherer {}
