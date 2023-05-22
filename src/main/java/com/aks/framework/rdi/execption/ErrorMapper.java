package com.lbg.rsk.cdp.dataflow.execption;

import org.springframework.lang.Nullable;

@FunctionalInterface
public interface ErrorMapper {
  @Nullable
  CustomErrorException map(Exception origin);
}
