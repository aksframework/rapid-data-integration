package com.lbg.rsk.cdp.demo.integration.orch.executor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowAPIExecutor;
import com.lbg.rsk.cdp.dataflow.apiexecutor.AbstractAPIExecutor;
import com.lbg.rsk.cdp.dataflow.apiexecutor.DefaultAPIExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "datasourcing", type = DefaultAPIExecutorFlow.class)
public class DataSourcingAPIExecutor extends AbstractAPIExecutor {}
