package com.aks.framework.rdi.datagatherer;

import com.aks.framework.rdi.base.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class AbstractBaseDataFlowGatherer implements ApplicationContextAware {
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    BeanUtils.setContext(applicationContext);
  }
}
