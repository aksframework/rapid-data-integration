package com.aks.framework.demo.integration;

import com.aks.framework.rdi.annotations.DataFlowAPIExecutor;
import com.aks.framework.rdi.apiexecutor.AbstractAPIExecutor;
import com.aks.framework.rdi.apiexecutor.DefaultAPIExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "weather", type = DefaultAPIExecutorFlow.class)
public class WeatherAPIExecutor extends AbstractAPIExecutor {}
