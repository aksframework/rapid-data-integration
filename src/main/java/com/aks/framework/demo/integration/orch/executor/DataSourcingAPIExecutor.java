package com.aks.framework.demo.integration.orch.executor;

import com.aks.framework.rdi.annotations.DataFlowAPIExecutor;
import com.aks.framework.rdi.apiexecutor.AbstractAPIExecutor;
import com.aks.framework.rdi.apiexecutor.DefaultAPIExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "datasourcing", type = DefaultAPIExecutorFlow.class)
public class DataSourcingAPIExecutor extends AbstractAPIExecutor {}
