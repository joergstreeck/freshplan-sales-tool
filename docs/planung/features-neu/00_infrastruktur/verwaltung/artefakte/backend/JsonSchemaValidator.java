package de.freshplan.governance.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public class JsonSchemaValidator {

  private final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

  public void validate(JsonNode schemaNode, JsonNode value){
    if (schemaNode == null || schemaNode.isNull()) return;
    JsonSchema schema = factory.getSchema(schemaNode);
    Set<ValidationMessage> errors = schema.validate(value);
    if (!errors.isEmpty()){
      StringBuilder sb = new StringBuilder("Settings validation failed: ");
      for (ValidationMessage e : errors) sb.append(e.getMessage()).append("; ");
      throw new IllegalArgumentException(sb.toString());
    }
  }
}
