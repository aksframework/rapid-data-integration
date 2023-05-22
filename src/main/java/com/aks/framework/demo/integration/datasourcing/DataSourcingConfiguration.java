package com.aks.framework.demo.integration.datasourcing;

import com.aks.framework.demo.integration.datasourcing.executor.DataFlowNcinoAPIExecutor;
import com.aks.framework.demo.integration.datasourcing.executor.DataFlowSOEAPIExecutor;
import com.aks.framework.demo.integration.datasourcing.executor.DataSourcingDataGatherer;
import com.aks.framework.demo.integration.datasourcing.executor.InitialBomSpecExecutor;
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
public class DataSourcingConfiguration {

  @Autowired private DataFlowSOEAPIExecutor dataFlowSoeapiExecutor;

  @Autowired private DataFlowNcinoAPIExecutor dataFlowNcinoAPIExecutor;
  @Autowired private DataSourcingDataGatherer dataSourcingDataGatherer;

  @Autowired private InitialBomSpecExecutor initialBomSpecExecutor;

  @Bean
  public IntegrationFlow dataSourcingRequest() {
    log.info("S0. Entering into main integration flow");
    return flow ->
        flow.channel(MessageChannels.executor(Executors.newCachedThreadPool()))
            .log(Level.TRACE)
            .scatterGather(
                scatterer ->
                    scatterer
                        .applySequence(true)
                        .recipientFlow(initialBomSpecExecutor)
                        .recipientFlow(dataFlowSoeapiExecutor)
                        .recipientFlow(dataFlowNcinoAPIExecutor),
                gatherer -> gatherer.processor(dataSourcingDataGatherer))
            .logAndReply("Final response");
  }
}
