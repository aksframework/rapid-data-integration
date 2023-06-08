package com.aks.framework.rdi.base;

public class RDIUtils {
  public static String createChannel(String dataFlowName, String channelType) {
    return dataFlowName + channelType;
  }
}
