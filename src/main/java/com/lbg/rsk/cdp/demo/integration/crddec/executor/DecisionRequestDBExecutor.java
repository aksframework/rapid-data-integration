package com.lbg.rsk.cdp.demo.integration.crddec.executor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowDBExecutor;
import com.lbg.rsk.cdp.dataflow.dbexecutor.AbstractDBExecutor;
import com.lbg.rsk.cdp.dataflow.dbexecutor.DefaultDBExecutorFlow;
import com.lbg.rsk.cdp.demo.integration.crddec.entity.DecisionRequestEntity;
import com.lbg.rsk.cdp.demo.integration.crddec.entity.DecisionRequestRepository;
import org.springframework.stereotype.Component;

@Component
@DataFlowDBExecutor(
    name = "decisionRequestInDSOR",
    type = DefaultDBExecutorFlow.class,
    entity = DecisionRequestEntity.class,
    repository = DecisionRequestRepository.class)
public class DecisionRequestDBExecutor extends AbstractDBExecutor {}
