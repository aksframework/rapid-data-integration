package com.aks.framework.rdi.specExecutor;

import com.aks.framework.rdi.annotations.DataFlowSpecExecutor;
import com.aks.framework.rdi.base.AbstractBaseExecutor;
import com.aks.framework.rdi.base.GenerateTypeBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.util.Assert;

@Slf4j
public abstract class AbstractSpecExecutor extends AbstractBaseExecutor implements SpecExecutor {
  @Override
  protected IntegrationFlowDefinition<?> buildFlow() {
    if (this.getClass().isAnnotationPresent(DataFlowSpecExecutor.class)) {
      DataFlowSpecExecutor dataFlowSpecExecutor =
          this.getClass().getAnnotation(DataFlowSpecExecutor.class);

      Assert.notNull(
          dataFlowSpecExecutor.name(),
          "'name' can not be 'Null' in 'DataFlowSpecExecutor' Annotation");
      Assert.notNull(
          dataFlowSpecExecutor.type(),
          "'type' can not be 'Null' in 'DataFlowSpecExecutor' Annotation");

      return new GenerateTypeBean<AbstractSpecExecutorFlow>(this, dataFlowSpecExecutor.name())
          .get(dataFlowSpecExecutor.type(), this, dataFlowSpecExecutor.name())
          .flow();
    } else {
      log.error(
          "SpecExecutorInfo is not defined correctly. Use Annotation '@DataFlowSpecExecutor' to define a 'SpecExecutorInfo'");
      throw new RuntimeException(
          "SpecExecutorInfo ['"
              + this.getClass()
              + "'] not defined correctly. Use Annotation '@DataFlowSpecExecutor' to define a 'SpecExecutorInfo'");
    }
  }
}
