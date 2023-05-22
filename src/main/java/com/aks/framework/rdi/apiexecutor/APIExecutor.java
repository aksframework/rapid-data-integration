package com.lbg.rsk.cdp.dataflow.apiexecutor;

import com.lbg.rsk.cdp.dataflow.base.DataFlowBaseExecutor;
import java.util.Map;

/** The interface Api executor. */
public interface APIExecutor extends DataFlowBaseExecutor {
  void saveExecutorPayload(String key, Object payload);

  Map getExecutorPayload(String key);
}
