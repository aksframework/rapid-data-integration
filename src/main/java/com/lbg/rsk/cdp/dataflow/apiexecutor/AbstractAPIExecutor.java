package com.lbg.rsk.cdp.dataflow.apiexecutor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowAPIExecutor;
import com.lbg.rsk.cdp.dataflow.base.AbstractBaseExecutor;
import com.lbg.rsk.cdp.dataflow.base.BeanUtils;
import com.lbg.rsk.cdp.dataflow.base.GenerateTypeBean;
import com.lbg.rsk.cdp.dataflow.base.MapperUtils;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.util.Assert;

/** The type Abstract api executor. */
@Slf4j
public abstract class AbstractAPIExecutor extends AbstractBaseExecutor implements APIExecutor {

  @Override
  protected IntegrationFlowDefinition<?> buildFlow() {
    if (this.getClass().isAnnotationPresent(DataFlowAPIExecutor.class)) {
      DataFlowAPIExecutor dataFlowAPIExecutor =
          this.getClass().getAnnotation(DataFlowAPIExecutor.class);

      Assert.notNull(
          dataFlowAPIExecutor.name(),
          "'name' can not be 'Null' in 'DataFlowAPIExecutor' Annotation");
      Assert.notNull(
          dataFlowAPIExecutor.type(),
          "'type' can not be 'Null' in 'DataFlowAPIExecutor' Annotation");
      return new GenerateTypeBean<AbstractAPIExecutorFlow>(this, dataFlowAPIExecutor.name())
          .get(dataFlowAPIExecutor.type(), this, dataFlowAPIExecutor.name())
          .flow();
    } else {
      log.error(
          "APIExecutorConfig is not defined correctly. Use Annotation '@DataFlowAPIExecutor' to define a 'APIExecutorConfig'");
      throw new RuntimeException(
          "APIExecutorConfig ['"
              + this.getClass()
              + "'] not defined correctly. Use Annotation '@DataFlowAPIExecutor' to define a 'APIExecutorConfig'");
    }
  }

  @Override
  public void saveExecutorPayload(String key, Object payload) {
    BeanUtils.getCache().put(key, MapperUtils.convertToMap(payload));
  }

  @Override
  public Map getExecutorPayload(String key) {
    return BeanUtils.getCache().getIfPresent(key);
  }
}
