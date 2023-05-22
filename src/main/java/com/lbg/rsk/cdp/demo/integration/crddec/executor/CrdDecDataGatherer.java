package com.lbg.rsk.cdp.demo.integration.crddec.executor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowGatherer;
import com.lbg.rsk.cdp.dataflow.datagatherer.AbstractDataFlowGatherer;
import org.springframework.stereotype.Component;

/** The type DecisionDataGatherer. */
@Component
@DataFlowGatherer(template = "crddecgatherer")
public class CrdDecDataGatherer extends AbstractDataFlowGatherer {}
