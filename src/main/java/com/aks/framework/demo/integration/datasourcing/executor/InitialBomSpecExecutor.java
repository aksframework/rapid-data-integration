package com.aks.framework.demo.integration.datasourcing.executor;

import com.aks.framework.rdi.annotations.DataFlowSpecExecutor;
import com.aks.framework.rdi.specExecutor.AbstractSpecExecutor;
import com.aks.framework.rdi.specExecutor.DefaultSpecExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowSpecExecutor(name = "initialBOMPreparation", type = DefaultSpecExecutorFlow.class)
public class InitialBomSpecExecutor extends AbstractSpecExecutor {}
