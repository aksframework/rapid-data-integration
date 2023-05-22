package com.lbg.rsk.cdp.dataflow.datagatherer;

import com.lbg.rsk.cdp.dataflow.base.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class AbstractBaseDataFlowGatherer implements ApplicationContextAware {
  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    BeanUtils.setContext(applicationContext);
  }
}
