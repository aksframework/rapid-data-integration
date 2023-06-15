package com.aks.framework.rdi.base;

import static com.aks.framework.rdi.base.ApplicationConstants.API_EXECUTOR_TEXT;

public class RDIUtils {
  public static String createChannel(String dataFlowName, String channelType) {
    return dataFlowName + channelType;
  }

  public static String executorName(String dataFlowName) {
    return dataFlowName + API_EXECUTOR_TEXT;
  }
}
