package com.aks.framework.rdi.apiexecutor;

import com.aks.framework.rdi.base.DataFlowBaseExecutor;
import java.util.Map;

/** The interface Api executor. */
public interface APIExecutor extends DataFlowBaseExecutor {
  void saveExecutorPayload(String key, Object payload);

  Map getExecutorPayload(String key);
}
