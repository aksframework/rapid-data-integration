package com.lbg.rsk.cdp.demo.integration.crddec;

import com.lbg.rsk.cdp.demo.integration.crddec.executor.CrdDecDataGatherer;
import com.lbg.rsk.cdp.demo.integration.crddec.executor.DecisionRequestDBExecutor;
import com.lbg.rsk.cdp.demo.integration.crddec.executor.DecisionResponseDBExecutor;
import com.lbg.rsk.cdp.demo.integration.crddec.executor.FicoAPIExecutor;
import com.lbg.rsk.cdp.demo.integration.crddec.executor.JwtTokenAPIExecutor;
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
public class CreditDecisionConfiguration {

  @Autowired private FicoAPIExecutor ficoAPIExecutor;
  @Autowired private JwtTokenAPIExecutor jwtTokenAPIExecutor;
  @Autowired private DecisionRequestDBExecutor decisionRequestDBExecutor;
  @Autowired private DecisionResponseDBExecutor decisionResponseDBExecutor;
  @Autowired private CrdDecDataGatherer crdDecDataGatherer;

  @Bean
  public IntegrationFlow creditDecisioningRequest() {
    log.info("S0. Entering into main integration flow");
    return flow ->
        flow.channel(MessageChannels.executor(Executors.newCachedThreadPool()))
            .log(Level.TRACE)
            .scatterGather(
                scatterer ->
                    scatterer
                        .applySequence(true)
                        .recipientFlow(decisionRequestDBExecutor)
                        .recipientFlow(ficoAPIExecutor),
                gatherer -> gatherer.processor(crdDecDataGatherer))
            .logAndReply("Final response");
  }
}
