package com.lbg.rsk.cdp.dataflow.specExecutor;

import static com.lbg.rsk.cdp.dataflow.base.DataFlowUtils.createChannel;

import com.lbg.rsk.cdp.dataflow.base.BeanUtils;
import com.lbg.rsk.cdp.dataflow.base.DataFlowBaseExecutor;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConfig;
import com.lbg.rsk.cdp.dataflow.base.DataFlowConstants;
import com.lbg.rsk.cdp.dataflow.base.DefaultTransformer;
import com.lbg.rsk.cdp.dataflow.base.MapperUtils;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.integration.dsl.IntegrationFlowExtension;

public class SpecExecutorDefinition extends IntegrationFlowExtension<SpecExecutorDefinition> {

  /** The Spec executor. */
  private final DataFlowBaseExecutor specExecutor;

  /** The Data flow. */
  private final String dataFlowName;

  /** The Data flow config. */
  private final DataFlowConfig dataFlowConfig;

  /**
   * Instantiates a new Spec executor definition.
   *
   * @param specExecutor the spec executor
   * @param dataFlowName the data flow
   */
  public SpecExecutorDefinition(DataFlowBaseExecutor specExecutor, String dataFlowName) {
    this.specExecutor = specExecutor;
    this.dataFlowName = dataFlowName;
    this.dataFlowConfig = BeanUtils.getDataFlowConfig();
  }

  public SpecExecutorDefinition from(String messageChannel) {
    return channel(messageChannel)
        .enrichHeaders(
            h -> {
              h.errorChannel(
                  createChannel(dataFlowName, DataFlowConstants.SPEC_EXECUTOR_ERROR_CHANNEL), true);
              h.header(DataFlowConstants.DATA_FLOW_HEADER_NAME, dataFlowName);
            });
  }

  SpecExecutorDefinition executeRequest() {
    String requestText = String.format("Spec-Flow [%s] ->", dataFlowName);
    String responseText = String.format("Spec-Flow [%s] <-", dataFlowName);
    return transform(this, "convertToMapBeforeApplyJolt")
        .log(requestText, message -> "Headers " + message.getHeaders())
        .handle(this, "executeJoltSpec")
        .log(responseText, message -> "Payload " + message.getPayload());
  }

  public Object convertToMapBeforeApplyJolt(@NotNull Object payload) {
    return MapperUtils.convertToType(payload, Map.class);
  }

  public Object executeJoltSpec(@NotNull Map payload) {
    return new DefaultTransformer()
        .transform(payload, dataFlowConfig.getSpecExecutorConfig(dataFlowName).getSpec());
  }
}
