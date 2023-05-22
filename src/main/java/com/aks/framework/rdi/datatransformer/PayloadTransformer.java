package com.aks.framework.rdi.datatransformer;

import com.aks.framework.rdi.base.BeanUtils;
import com.aks.framework.rdi.base.DataFlowConfig.PayloadTransformerConfig;
import com.aks.framework.rdi.base.MapperUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ObjectUtils;
import org.yaml.snakeyaml.Yaml;

public class PayloadTransformer {

  private PayloadTransformer() {}

  public static Object execute(Map headers, Object payload, String payloadTransformerName) {
    PayloadTransformerConfig payloadTransformerConfig =
        BeanUtils.getDataFlowConfig().getPayloadTransformerConfig(payloadTransformerName);

    Map<String, Map<String, Object>> fromDataObject = new HashMap<>();
    fromDataObject.put("headers", headers);
    fromDataObject.put("payload", MapperUtils.convertToMap(payload));

    Object toDataObject = new HashMap<>();

    if (!ObjectUtils.isEmpty(payloadTransformerConfig.getTemplateSpec())) {
      toDataObject = getDataSpec(payloadTransformerConfig.getTemplateSpec());
    } else if (!ObjectUtils.isEmpty(payloadTransformerConfig.getExpressionSpec())) {
      ExpressionParser parser = new SpelExpressionParser();
      toDataObject =
          parser.parseExpression(payloadTransformerConfig.getExpressionSpec()).getValue();
      if (null == toDataObject) {
        throw new RuntimeException(
            String.format(
                "Payload transformer '%s' failed. Expression spec '%s' returned null",
                payloadTransformerName, payloadTransformerConfig.getExpressionSpec()));
      }
      toDataObject = MapperUtils.convertToMap(toDataObject);
    }

    return new DataTransformerExecutor(
            payloadTransformerName, payloadTransformerConfig, toDataObject, fromDataObject)
        .execute();
  }

  private static Object getDataSpec(String dataSpecName) {
    if (DataGathererUtils.isJSONFileSpec(dataSpecName)) {
      try {
        return DataGathererUtils.convertForTraversal(
            BeanUtils.getObjectMapper()
                .readTree(new ClassPathResource(dataSpecName.toLowerCase()).getInputStream()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else if (DataGathererUtils.isYAMLFileSpec(dataSpecName)) {
      Yaml yaml = new Yaml();
      try {
        return DataGathererUtils.convertForTraversal(
            yaml.load(new ClassPathResource(dataSpecName.toLowerCase()).getInputStream()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      return new HashMap<>();
    }
  }
}
