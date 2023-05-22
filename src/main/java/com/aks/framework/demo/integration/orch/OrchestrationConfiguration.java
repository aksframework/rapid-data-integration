package com.aks.framework.demo.integration.orch;

import com.aks.framework.demo.integration.orch.executor.CallerRequestDBExecutor;
import com.aks.framework.demo.integration.orch.executor.CallerResponseDBExecutor;
import com.aks.framework.demo.integration.orch.executor.CreditDecisioningAPIExecutor;
import com.aks.framework.demo.integration.orch.executor.DataSourcingAPIExecutor;
import com.aks.framework.demo.integration.orch.executor.OrchSMERenewalGatherer;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.LoggingHandler.Level;

@Configuration
@Slf4j
public class OrchestrationConfiguration {

  @Autowired private CallerRequestDBExecutor callerRequestDBExecutor;

  @Autowired CallerResponseDBExecutor callerResponseDBExecutor;

  @Autowired DataSourcingAPIExecutor dataSourcingAPIExecutor;

  @Autowired CreditDecisioningAPIExecutor creditDecisioningAPIExecutor;

  @Autowired OrchSMERenewalGatherer orchSMERenewalGatherer;

  @Bean
  public IntegrationFlow orchestrationRequest() {
    log.info("S0. Entering into main integration flow");
    return flow ->
        flow.channel(MessageChannels.executor(Executors.newCachedThreadPool()))
            .log(Level.TRACE)
            .scatterGather(
                scatterer ->
                    scatterer
                        .applySequence(true)
                        .recipientFlow(callerRequestDBExecutor)
                        .recipientFlow(creditDecisioningAPIExecutor),
                gatherer -> gatherer.processor(orchSMERenewalGatherer))
            .logAndReply("Final response");
  }
}
