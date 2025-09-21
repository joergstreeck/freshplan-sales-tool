package de.freshplan.settings.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import de.freshplan.settings.repo.SettingsRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

/** Runtime JSON Schema validation with registry-backed schemas, cached per key+version. */
@ApplicationScoped
public class SettingsValidator {

  @Inject ObjectMapper om;
  @Inject MeterRegistry meter;

  private final Map<String, JsonSchema> cache = new HashMap<>();

  private JsonSchema compile(String key, int version, JsonNode schemaNode){
    String k = key+"#"+version;
    return cache.computeIfAbsent(k, kk -> JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(schemaNode));
  }

  public List<String> validateValue(SettingsRepository.RegistryEntry entry, JsonNode value){
    List<String> errors = new ArrayList<>();
    if (value == null){
      errors.add("value must not be null");
      return errors;
    }
    switch(entry.mergeStrategy()){
      case "scalar" -> { if (value.isObject() || value.isArray()) errors.add("scalar key must not be object or array"); }
      case "object" -> { if (!value.isObject()) errors.add("object key requires JSON object"); }
      case "list"   -> { if (!value.isArray()) errors.add("list key requires JSON array"); }
    }
    try{
      JsonSchema schema = compile(entry.key(), entry.version(), entry.jsonSchema());
      Set<ValidationMessage> msgs = schema.validate(value);
      for (var m : msgs) errors.add(m.getMessage());
    }catch(Exception e){
      errors.add("schema compilation/validation error: "+e.getMessage());
    }
    return errors;
  }
}
