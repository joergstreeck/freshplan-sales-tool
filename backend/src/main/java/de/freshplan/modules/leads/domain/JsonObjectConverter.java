package de.freshplan.modules.leads.domain;

import io.vertx.core.json.JsonObject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for JsonObject to JSONB column mapping. Sprint 2.1: Used for business rules and
 * metadata storage.
 */
@Converter
public class JsonObjectConverter implements AttributeConverter<JsonObject, String> {

  @Override
  public String convertToDatabaseColumn(JsonObject jsonObject) {
    if (jsonObject == null) {
      return null;
    }
    return jsonObject.encode();
  }

  @Override
  public JsonObject convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return new JsonObject();
    }
    try {
      return new JsonObject(dbData);
    } catch (Exception e) {
      // Log error and return empty object instead of failing
      return new JsonObject();
    }
  }
}
