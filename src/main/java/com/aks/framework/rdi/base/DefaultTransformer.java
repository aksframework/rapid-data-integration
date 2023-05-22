package com.aks.framework.rdi.base;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import java.util.Map;

/** The type Default transformer. */
public class DefaultTransformer {
  /**
   * Transform object.
   *
   * @param objectToTransform the object to transform
   * @param specPath the spec path
   * @return the object
   */
  public Object transform(Map objectToTransform, String specPath) {
    Chainr chainr = Chainr.fromSpec(JsonUtils.classpathToList(specPath));
    return chainr.transform(objectToTransform);
  }
}
