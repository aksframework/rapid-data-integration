package com.aks.framework.rdi.base;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.integration.dsl.IntegrationFlowAdapter;

public abstract class AbstractBaseExecutor extends IntegrationFlowAdapter
    implements ApplicationContextAware {
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    BeanUtils.setContext(applicationContext);
  }
}
