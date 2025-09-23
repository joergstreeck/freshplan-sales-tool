package de.freshplan.infrastructure.settings;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for SettingsScope enum to PostgreSQL settings_scope type. Required for proper
 * mapping between Java enum and PostgreSQL custom type.
 */
@Converter(autoApply = true)
public class SettingsScopeConverter implements AttributeConverter<SettingsScope, String> {

  @Override
  public String convertToDatabaseColumn(SettingsScope scope) {
    if (scope == null) {
      return null;
    }
    return scope.name();
  }

  @Override
  public SettingsScope convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    try {
      return SettingsScope.valueOf(dbData);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unknown settings_scope value: " + dbData, e);
    }
  }
}
