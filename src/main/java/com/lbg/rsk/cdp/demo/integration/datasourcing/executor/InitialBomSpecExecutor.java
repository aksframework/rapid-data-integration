package com.lbg.rsk.cdp.demo.integration.datasourcing.executor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowSpecExecutor;
import com.lbg.rsk.cdp.dataflow.specExecutor.AbstractSpecExecutor;
import com.lbg.rsk.cdp.dataflow.specExecutor.DefaultSpecExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowSpecExecutor(name = "initialBOMPreparation", type = DefaultSpecExecutorFlow.class)
public class InitialBomSpecExecutor extends AbstractSpecExecutor {}
