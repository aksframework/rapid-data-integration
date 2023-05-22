package com.lbg.rsk.cdp.dataflow.base;

public class DataFlowUtils {
  public static String createChannel(String dataFlowName, String channelType) {
    return dataFlowName + channelType;
  }
}
