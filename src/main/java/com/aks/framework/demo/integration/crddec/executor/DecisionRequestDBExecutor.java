package com.aks.framework.demo.integration.crddec.executor;

import com.aks.framework.demo.integration.crddec.entity.DecisionRequestEntity;
import com.aks.framework.demo.integration.crddec.entity.DecisionRequestRepository;
import com.aks.framework.rdi.annotations.DataFlowDBExecutor;
import com.aks.framework.rdi.dbexecutor.AbstractDBExecutor;
import com.aks.framework.rdi.dbexecutor.DefaultDBExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowDBExecutor(
    name = "decisionRequestInDSOR",
    type = DefaultDBExecutorFlow.class,
    entity = DecisionRequestEntity.class,
    repository = DecisionRequestRepository.class)
public class DecisionRequestDBExecutor extends AbstractDBExecutor {}
