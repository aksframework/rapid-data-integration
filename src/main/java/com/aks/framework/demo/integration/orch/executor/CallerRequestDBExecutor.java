package com.aks.framework.demo.integration.orch.executor;

import com.aks.framework.demo.integration.orch.entity.CallerRequestEntity;
import com.aks.framework.demo.integration.orch.entity.CallerRequestRepository;
import com.aks.framework.rdi.annotations.DataFlowDBExecutor;
import com.aks.framework.rdi.dbexecutor.AbstractDBExecutor;
import com.aks.framework.rdi.dbexecutor.DefaultDBExecutorFlow;
import org.springframework.stereotype.Component;

@Component
@DataFlowDBExecutor(
    name = "callerrequest",
    type = DefaultDBExecutorFlow.class,
    entity = CallerRequestEntity.class,
    repository = CallerRequestRepository.class)
public class CallerRequestDBExecutor extends AbstractDBExecutor {}
