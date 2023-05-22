package com.aks.framework.rdi.execption;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface ErrorMapper {
  @Nullable
  CustomErrorException map(Exception origin);
}
