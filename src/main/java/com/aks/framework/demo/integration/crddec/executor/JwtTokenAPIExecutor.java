package com.aks.framework.demo.integration.crddec.executor;

import com.aks.framework.rdi.annotations.DataFlowAPIExecutor;
import com.aks.framework.rdi.apiexecutor.AbstractAPIExecutor;
import com.aks.framework.rdi.apiexecutor.DefaultAPIExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "jwtToken", type = DefaultAPIExecutorFlow.class)
public class JwtTokenAPIExecutor extends AbstractAPIExecutor {}
