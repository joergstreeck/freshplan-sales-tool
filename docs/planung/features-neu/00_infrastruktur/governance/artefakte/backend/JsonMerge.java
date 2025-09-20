package de.freshplan.governance.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonMerge {

  public static JsonNode merge(String mergeStrategy, ObjectMapper mapper, JsonNode base, JsonNode override){
    if (override == null || override.isNull()) return base;
    if (base == null || base.isNull()) return override;
    if ("merge".equals(mergeStrategy)){
      if (base.isObject() && override.isObject()){
        ObjectNode out = ((ObjectNode) base).deepCopy();
        override.fieldNames().forEachRemaining(fn -> out.set(fn, merge("merge", mapper, out.get(fn), override.get(fn))));
        return out;
      }
      return override;
    } else if ("append".equals(mergeStrategy)){
      if (base.isArray() && override.isArray()){
        ArrayNode out = mapper.createArrayNode();
        out.addAll((ArrayNode) base);
        out.addAll((ArrayNode) override);
        return out;
      }
      return override;
    } else {
      return override; // replace
    }
  }
}
