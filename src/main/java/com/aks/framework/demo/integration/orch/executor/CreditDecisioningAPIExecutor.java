package com.aks.framework.demo.integration.orch.executor;

import com.aks.framework.rdi.annotations.DataFlowAPIExecutor;
import com.aks.framework.rdi.apiexecutor.AbstractAPIExecutor;
import com.aks.framework.rdi.apiexecutor.DefaultAPIExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "creditdecision", type = DefaultAPIExecutorFlow.class)
public class CreditDecisioningAPIExecutor extends AbstractAPIExecutor {}
