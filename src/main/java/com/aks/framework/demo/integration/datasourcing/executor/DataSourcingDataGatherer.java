package com.lbg.rsk.cdp.demo.integration.datasourcing.executor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowGatherer;
import com.lbg.rsk.cdp.dataflow.datagatherer.AbstractDataFlowGatherer;
import org.springframework.stereotype.Component;

@Component
@DataFlowGatherer(template = "smeRenewal")
public class DataSourcingDataGatherer extends AbstractDataFlowGatherer {}
