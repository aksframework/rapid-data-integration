package com.lbg.rsk.cdp.dataflow.apiexecutor;

import static com.lbg.rsk.cdp.dataflow.base.DataFlowUtils.createChannel;

import com.lbg.rsk.cdp.dataflow.base.BeanUtils;
import com.lbg.rsk.cdp.dataflow.base.DataFlowBaseExecutor;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConfig;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants;
import com.lbg.rsk.cdp.dataflow.dbexecutor.DBExecutor;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.util.Assert;

/** The type Abstract api executor flow. */
@Slf4j
public abstract class AbstractBaseExecutorFlow extends IntegrationFlowAdapter {
  /** The Api executor. */
  protected final DataFlowBaseExecutor dataFlowBaseExecutor;
  /** The Data flow. */
  protected String dataFlowName;

  /** The Data flow config. */
  protected DataFlowConfig dataFlowConfig;

  /**
   * Instantiates a new Abstract api executor flow.
   *
   * @param dataFlowBaseExecutor the api executor
   * @param dataFlowName the data flow
   */
  public AbstractBaseExecutorFlow(DataFlowBaseExecutor dataFlowBaseExecutor, String dataFlowName) {
    this.dataFlowBaseExecutor = dataFlowBaseExecutor;
    this.dataFlowName = dataFlowName;
    this.dataFlowConfig = BeanUtils.getDataFlowConfig();

    Assert.notNull(dataFlowBaseExecutor, "'APIExecutorConfig' can not be 'Null");
    Assert.notNull(dataFlowName, "'DataFlow' can not be 'Null");
    Assert.notNull(dataFlowConfig, "'DataFlowConfig' can not be 'Null");
    if (dataFlowBaseExecutor instanceof APIExecutor) {
      Assert.notNull(
          dataFlowConfig.getAPIExecutorConfig(dataFlowName),
          "'api-executor' not available in configuration file");
      Assert.notNull(
          dataFlowConfig.getAPIExecutorConfig(dataFlowName).getApiUrl(),
          "'api-url' can not be 'Null'");
    }
    addErrorChannelHandler();
  }

  /** Add error channel handler. */
  public void addErrorChannelHandler() {
    String errorChannel =
        dataFlowBaseExecutor instanceof APIExecutor
            ? DataFlowConstants.API_EXECUTOR_ERROR_CHANNEL
            : dataFlowBaseExecutor instanceof DBExecutor
                ? DataFlowConstants.DB_EXECUTOR_ERROR_CHANNEL
                : DataFlowConstants.SPEC_EXECUTOR_ERROR_CHANNEL;

    IntegrationFlow integrationFlow =
        IntegrationFlows.from(createChannel(dataFlowName, errorChannel))
            .<MessagingException, Message<?>>transform(
                e -> {
                  MessageHeaders messageHeaders =
                      Objects.requireNonNull(e.getFailedMessage()).getHeaders();
                  log.error(
                      "Error in calling {}. Error message [{}] Payload: [{}]",
                      dataFlowName,
                      e.getFailedMessage().toString(),
                      e.toString());
                  return MessageBuilder.withPayload(
                          new APIExecutorException(
                                  String.format("DataFlow '%s' throws error.", dataFlowName), e)
                              .getFullErrorResponse()
                              .getSecond())
                      .setHeader(
                          MessageHeaders.REPLY_CHANNEL,
                          messageHeaders.get(DataFlowConstants.ORIGINAL_ERROR_CHANNEL))
                      .build();
                })
            .get();
    Optional<IntegrationFlowContext> integrationFlowContextOptional =
        BeanUtils.getBean(IntegrationFlowContext.class);
    if (integrationFlowContextOptional.isPresent()) {
      integrationFlowContextOptional.get().registration(integrationFlow).register();
    } else {
      throw new RuntimeException("IntegrationFlowContext not initialized");
    }
  }

  /**
   * Flow integration flow definition.
   *
   * @return the integration flow definition
   */
  public IntegrationFlowDefinition<?> flow() {
    return buildFlow();
  }
}
