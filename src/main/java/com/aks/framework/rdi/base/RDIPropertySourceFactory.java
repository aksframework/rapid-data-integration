package com.aks.framework.rdi.base;

import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertySourceFactory;

public class RDIPropertySourceFactory implements PropertySourceFactory {
  @Override
  public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource)
      throws IOException {
    String profile = System.getProperty("spring.profiles.active");
    YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
    factory.setResources(
        new PathMatchingResourcePatternResolver().getResources("classpath*:**/*rdi-config.yml"));
    Properties properties = factory.getObject();
    return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
  }
}
