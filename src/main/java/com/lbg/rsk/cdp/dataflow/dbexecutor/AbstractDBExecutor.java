package com.lbg.rsk.cdp.dataflow.dbexecutor;

import com.lbg.rsk.cdp.dataflow.annotations.DataFlowDBExecutor;
import com.lbg.rsk.cdp.dataflow.apiexecutor.AbstractBaseExecutorFlow;
import com.lbg.rsk.cdp.dataflow.base.AbstractBaseExecutor;
import com.lbg.rsk.cdp.dataflow.base.GenerateTypeBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.util.Assert;

@Slf4j
public class AbstractDBExecutor extends AbstractBaseExecutor implements DBExecutor {

  @Override
  protected IntegrationFlowDefinition<?> buildFlow() {
    if (this.getClass().isAnnotationPresent(DataFlowDBExecutor.class)) {
      DataFlowDBExecutor dataFlowDBExecutor =
          this.getClass().getAnnotation(DataFlowDBExecutor.class);

      Assert.notNull(
          dataFlowDBExecutor.name(), "'name' can not be 'Null' in 'DataFlowDBExecutor' Annotation");
      Assert.notNull(
          dataFlowDBExecutor.type(), "'type' can not be 'Null' in 'DataFlowDBExecutor' Annotation");
      Assert.notNull(
          dataFlowDBExecutor.entity(),
          "'entity' can not be 'Null' in 'DataFlowDBExecutor' Annotation");
      Assert.notNull(
          dataFlowDBExecutor.repository(),
          "'repository' can not be 'Null' in 'DataFlowDBExecutor' Annotation");

      return new GenerateTypeBean<AbstractBaseExecutorFlow>(this, dataFlowDBExecutor.name())
          .get(
              dataFlowDBExecutor.type(),
              this,
              dataFlowDBExecutor.name(),
              dataFlowDBExecutor.entity(),
              dataFlowDBExecutor.repository())
          .flow();
    } else {
      log.error(
          "DBExecutorConfig is not defined correctly. Use Annotation '@DataFlowDBExecutor' to define a 'DBExecutorConfig'");
      throw new RuntimeException(
          "DBExecutorConfig ['"
              + this.getClass()
              + "'] not defined correctly. Use Annotation '@DataFlowDBExecutor' to define a 'DBExecutorConfig'");
    }
  }
}
