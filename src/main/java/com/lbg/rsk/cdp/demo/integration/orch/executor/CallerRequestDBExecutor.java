package com.lbg.rsk.cdp.demo.integration.orch.executor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowDBExecutor;
import com.lbg.rsk.cdp.dataflow.dbexecutor.AbstractDBExecutor;
import com.lbg.rsk.cdp.dataflow.dbexecutor.DefaultDBExecutorFlow;
import com.lbg.rsk.cdp.demo.integration.orch.entity.CallerRequestEntity;
import com.lbg.rsk.cdp.demo.integration.orch.entity.CallerRequestRepository;
import org.springframework.stereotype.Component;

@Component
@DataFlowDBExecutor(
    name = "callerrequest",
    type = DefaultDBExecutorFlow.class,
    entity = CallerRequestEntity.class,
    repository = CallerRequestRepository.class)
public class CallerRequestDBExecutor extends AbstractDBExecutor {}
