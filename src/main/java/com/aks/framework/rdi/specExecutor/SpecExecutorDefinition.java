package com.aks.framework.rdi.specExecutor;

import com.aks.framework.rdi.base.ApplicationConstants;
import com.aks.framework.rdi.base.BeanUtils;
import com.aks.framework.rdi.base.DataFlowBaseExecutor;
import com.aks.framework.rdi.base.DataFlowConfig;
import com.aks.framework.rdi.base.DefaultTransformer;
import com.aks.framework.rdi.base.MapperUtils;
import com.aks.framework.rdi.base.RDIUtils;
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
                  RDIUtils.createChannel(
                      dataFlowName, ApplicationConstants.SPEC_EXECUTOR_ERROR_CHANNEL),
                  true);
              h.header(ApplicationConstants.DATA_FLOW_HEADER_NAME, dataFlowName);
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
