package com.lbg.rsk.cdp.demo.integration;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowAPIExecutor;
import com.lbg.rsk.cdp.dataflow.apiexecutor.AbstractAPIExecutor;
import com.lbg.rsk.cdp.dataflow.apiexecutor.DefaultAPIExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowAPIExecutor(name = "weather", type = DefaultAPIExecutorFlow.class)
public class WeatherAPIExecutor extends AbstractAPIExecutor {}
