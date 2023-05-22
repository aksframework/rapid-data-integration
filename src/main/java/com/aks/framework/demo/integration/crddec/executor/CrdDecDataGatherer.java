package com.aks.framework.demo.integration.crddec.executor;

import com.aks.framework.rdi.annotations.DataFlowGatherer;
import com.aks.framework.rdi.datagatherer.AbstractDataFlowGatherer;
import org.springframework.stereotype.Component;

/** The type DecisionDataGatherer. */
@Component
@DataFlowGatherer(template = "crddecgatherer")
public class CrdDecDataGatherer extends AbstractDataFlowGatherer {}
