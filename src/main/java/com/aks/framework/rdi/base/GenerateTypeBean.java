package com.aks.framework.rdi.base;

import java.util.stream.IntStream;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * The type Generate type bean.
 *
 * @param <T> the type parameter
 */
public class GenerateTypeBean<T> {

  /**
   * Instantiates a new Generate type bean.
   *
   * @param executorObject the executor object
   * @param executorName the executor name
   */
  public GenerateTypeBean(AbstractBaseExecutor executorObject, String executorName) {
    BeanUtils.addExecutor(executorName, executorObject.getClass());
  }

  /** Instantiates a new Generate type bean. */
  public GenerateTypeBean() {}

  /**
   * Get t.
   *
   * @param <S> the type parameter
   * @param typeOfClass the type of class
   * @param arguments the arguments
   * @return the t
   */
  public <S extends T> T get(Class<S> typeOfClass, Object... arguments) {
    ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
    IntStream.range(0, arguments.length)
        .forEach(i -> constructorArgumentValues.addGenericArgumentValue(arguments[i]));

    GenericBeanDefinition genericBeanDefinition = new GenericBeanDefinition();
    genericBeanDefinition.setBeanClass(typeOfClass);
    genericBeanDefinition.setConstructorArgumentValues(constructorArgumentValues);

    String beanName = ApplicationConstants.DATA_FLOW_STRING + typeOfClass.getSimpleName();
    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    beanFactory.registerBeanDefinition(beanName, genericBeanDefinition);
    T beanObject = beanFactory.getBean(beanName, typeOfClass);
    beanFactory.removeBeanDefinition(beanName);

    return beanObject;
  }
}
