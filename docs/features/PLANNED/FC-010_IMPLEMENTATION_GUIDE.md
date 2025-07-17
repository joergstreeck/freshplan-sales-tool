# 📘 FC-010 IMPLEMENTATION GUIDE

**Zweck:** Flexible Customer Import Architektur  
**Prerequisite:** [FC-010_KOMPAKT.md](./FC-010_KOMPAKT.md) gelesen  

---

## <a id="configuration-schema"></a>🗄️ Phase 1: Configuration Schema (Tag 1-2)

### 1.1: Database Migration erstellen

```sql
-- V4.0__create_import_configuration_tables.sql
CREATE TABLE import_field_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    field_id VARCHAR(100) NOT NULL UNIQUE,
    label VARCHAR(200) NOT NULL,
    field_type VARCHAR(50) NOT NULL CHECK (field_type IN ('text', 'number', 'email', 'date', 'select', 'boolean', 'iban', 'phone')),
    required BOOLEAN DEFAULT FALSE,
    validation_rules JSONB,
    mapping_patterns JSONB,
    business_logic TEXT,
    display_order INTEGER DEFAULT 0,
    category VARCHAR(100) DEFAULT 'general',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE import_field_options (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    field_config_id UUID NOT NULL REFERENCES import_field_configs(id) ON DELETE CASCADE,
    option_key VARCHAR(100) NOT NULL,
    option_value VARCHAR(200) NOT NULL,
    display_order INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE import_validation_plugins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plugin_name VARCHAR(100) NOT NULL UNIQUE,
    plugin_class VARCHAR(200) NOT NULL,
    description TEXT,
    target_industries JSONB,
    configuration JSONB,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE import_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    total_rows INTEGER,
    processed_rows INTEGER DEFAULT 0,
    success_count INTEGER DEFAULT 0,
    error_count INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED')),
    field_mappings JSONB,
    import_settings JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE TABLE import_errors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id UUID NOT NULL REFERENCES import_sessions(id) ON DELETE CASCADE,
    row_number INTEGER NOT NULL,
    field_name VARCHAR(100),
    error_type VARCHAR(50) NOT NULL,
    error_message TEXT NOT NULL,
    suggested_fix TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_import_field_configs_type ON import_field_configs(field_type);
CREATE INDEX idx_import_field_configs_category ON import_field_configs(category);
CREATE INDEX idx_import_sessions_user ON import_sessions(user_id);
CREATE INDEX idx_import_sessions_status ON import_sessions(status);
CREATE INDEX idx_import_errors_session ON import_errors(session_id);

-- Initial Data
INSERT INTO import_field_configs (field_id, label, field_type, required, validation_rules, mapping_patterns, category, display_order) VALUES
('companyName', 'Firmenname', 'text', true, '{"minLength": 2, "maxLength": 100}', '["firma", "company", "name", "unternehmen", "betrieb"]', 'basic', 1),
('customerNumber', 'Kundennummer', 'text', false, '{"pattern": "^[A-Z0-9]{3,20}$"}', '["nummer", "number", "kd_nr", "kundennummer", "customer_id"]', 'basic', 2),
('email', 'E-Mail', 'email', false, '{"format": "email"}', '["email", "e_mail", "mail", "kontakt"]', 'contact', 3),
('phone', 'Telefon', 'phone', false, '{"format": "international"}', '["telefon", "phone", "tel", "fon"]', 'contact', 4),
('street', 'Straße', 'text', false, '{"maxLength": 200}', '["strasse", "street", "straße", "adresse"]', 'address', 5),
('zipCode', 'PLZ', 'text', false, '{"pattern": "^[0-9]{5}$"}', '["plz", "zip", "postal", "postleitzahl"]', 'address', 6),
('city', 'Stadt', 'text', false, '{"maxLength": 100}', '["stadt", "city", "ort"]', 'address', 7),
('country', 'Land', 'select', false, '{"enum": ["DE", "AT", "CH"]}', '["land", "country", "staat"]', 'address', 8),
('industry', 'Branche', 'select', false, '{}', '["branche", "industry", "sektor", "bereich"]', 'classification', 9),
('revenue', 'Jahresumsatz', 'number', false, '{"min": 0, "max": 999999999}', '["umsatz", "revenue", "jahresumsatz", "turnover"]', 'financial', 10);

INSERT INTO import_field_options (field_config_id, option_key, option_value, display_order) VALUES
((SELECT id FROM import_field_configs WHERE field_id = 'country'), 'DE', 'Deutschland', 1),
((SELECT id FROM import_field_configs WHERE field_id = 'country'), 'AT', 'Österreich', 2),
((SELECT id FROM import_field_configs WHERE field_id = 'country'), 'CH', 'Schweiz', 3),
((SELECT id FROM import_field_configs WHERE field_id = 'industry'), 'IT', 'Informationstechnologie', 1),
((SELECT id FROM import_field_configs WHERE field_id = 'industry'), 'GASTRO', 'Gastronomie', 2),
((SELECT id FROM import_field_configs WHERE field_id = 'industry'), 'HANDEL', 'Handel', 3),
((SELECT id FROM import_field_configs WHERE field_id = 'industry'), 'HANDWERK', 'Handwerk', 4),
((SELECT id FROM import_field_configs WHERE field_id = 'industry'), 'DIENSTLEISTUNG', 'Dienstleistung', 5);
```

### 1.2: Entity Templates erstellen

```java
package de.freshplan.domain.import.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.hibernate.orm.panache.PanacheQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "import_field_configs")
public class ImportFieldConfig extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    public UUID id;
    
    @NotBlank
    @Column(name = "field_id", unique = true)
    public String fieldId;
    
    @NotBlank
    public String label;
    
    @NotNull
    @Column(name = "field_type")
    @Enumerated(EnumType.STRING)
    public FieldType fieldType;
    
    public boolean required = false;
    
    @Column(name = "validation_rules", columnDefinition = "jsonb")
    public JsonNode validationRules;
    
    @Column(name = "mapping_patterns", columnDefinition = "jsonb")
    public JsonNode mappingPatterns;
    
    @Column(name = "business_logic", columnDefinition = "text")
    public String businessLogic;
    
    @Column(name = "display_order")
    public Integer displayOrder = 0;
    
    public String category = "general";
    
    public boolean active = true;
    
    @CreationTimestamp
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "fieldConfig", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<ImportFieldOption> options;
    
    // Business Methods
    public static List<ImportFieldConfig> findActiveByCategory(String category) {
        return find("category = ?1 AND active = true ORDER BY displayOrder", category).list();
    }
    
    public static List<ImportFieldConfig> findAllActive() {
        return find("active = true ORDER BY displayOrder").list();
    }
    
    public static PanacheQuery<ImportFieldConfig> findByFieldType(FieldType type) {
        return find("fieldType = ?1 AND active = true", type);
    }
    
    // Validation helpers
    public boolean isSelectType() {
        return fieldType == FieldType.SELECT;
    }
    
    public boolean hasValidationRules() {
        return validationRules != null && !validationRules.isEmpty();
    }
    
    public List<String> getMappingPatternsList() {
        if (mappingPatterns == null || !mappingPatterns.isArray()) {
            return List.of();
        }
        return mappingPatterns.findValuesAsText("");
    }
    
    public enum FieldType {
        TEXT, NUMBER, EMAIL, DATE, SELECT, BOOLEAN, IBAN, PHONE
    }
}
```

```java
package de.freshplan.domain.import.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "import_field_options")
public class ImportFieldOption extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    public UUID id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_config_id")
    public ImportFieldConfig fieldConfig;
    
    @NotBlank
    @Column(name = "option_key")
    public String optionKey;
    
    @NotBlank
    @Column(name = "option_value")
    public String optionValue;
    
    @Column(name = "display_order")
    public Integer displayOrder = 0;
    
    public boolean active = true;
    
    // Business Methods
    public static List<ImportFieldOption> findByFieldConfig(UUID fieldConfigId) {
        return find("fieldConfig.id = ?1 AND active = true ORDER BY displayOrder", fieldConfigId).list();
    }
    
    public static List<ImportFieldOption> findByFieldId(String fieldId) {
        return find("fieldConfig.fieldId = ?1 AND active = true ORDER BY displayOrder", fieldId).list();
    }
}
```

---

## <a id="plugin-architecture"></a>🔧 Phase 2: Plugin Architecture (Tag 3-4)

### 2.1: Validation Plugin Interface

```java
package de.freshplan.domain.import.plugin;

import de.freshplan.domain.import.dto.ImportCustomerData;
import de.freshplan.domain.import.dto.ValidationResult;

public interface CustomValidationPlugin {
    
    /**
     * Plugin name for identification
     */
    String getName();
    
    /**
     * Industries this plugin applies to
     */
    List<String> getTargetIndustries();
    
    /**
     * Validate import data
     */
    void validate(ImportCustomerData data, ValidationResult result);
    
    /**
     * Plugin priority (higher = executed first)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Plugin configuration
     */
    default JsonNode getConfiguration() {
        return null;
    }
}
```

### 2.2: Beispiel Plugin - Gastronomie

```java
package de.freshplan.domain.import.plugin.impl;

import de.freshplan.domain.import.plugin.CustomValidationPlugin;
import de.freshplan.domain.import.dto.ImportCustomerData;
import de.freshplan.domain.import.dto.ValidationResult;
import de.freshplan.domain.import.dto.ValidationError;
import jakarta.enterprise.context.ApplicationScoped;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@ApplicationScoped
public class GastronomieValidationPlugin implements CustomValidationPlugin {
    
    @Override
    public String getName() {
        return "Gastronomie Validation";
    }
    
    @Override
    public List<String> getTargetIndustries() {
        return List.of("GASTRO", "GASTRONOMIE");
    }
    
    @Override
    public void validate(ImportCustomerData data, ValidationResult result) {
        // Sitzplätze validieren
        if (data.getCustomField("seatingCapacity") != null) {
            try {
                int capacity = Integer.parseInt(data.getCustomField("seatingCapacity"));
                if (capacity < 10) {
                    result.addError(new ValidationError(
                        data.getRowNumber(),
                        "seatingCapacity",
                        "Gastronomie-Betriebe müssen mindestens 10 Sitzplätze haben",
                        "Prüfen Sie die Sitzplatz-Anzahl oder ändern Sie die Branche"
                    ));
                }
            } catch (NumberFormatException e) {
                result.addError(new ValidationError(
                    data.getRowNumber(),
                    "seatingCapacity",
                    "Sitzplätze müssen eine Zahl sein",
                    "Geben Sie eine gültige Zahl ein"
                ));
            }
        }
        
        // Lizenz-Nummer validieren
        if (data.getCustomField("licenseNumber") == null || data.getCustomField("licenseNumber").isBlank()) {
            result.addWarning(new ValidationError(
                data.getRowNumber(),
                "licenseNumber",
                "Gastronomie-Lizenz fehlt",
                "Ergänzen Sie die Lizenz-Nummer für Compliance"
            ));
        }
        
        // Öffnungszeiten validieren
        validateOpeningHours(data, result);
    }
    
    private void validateOpeningHours(ImportCustomerData data, ValidationResult result) {
        String openingHours = data.getCustomField("openingHours");
        if (openingHours != null && !openingHours.matches("^\\d{2}:\\d{2}-\\d{2}:\\d{2}$")) {
            result.addError(new ValidationError(
                data.getRowNumber(),
                "openingHours",
                "Öffnungszeiten müssen im Format HH:MM-HH:MM angegeben werden",
                "Beispiel: 09:00-22:00"
            ));
        }
    }
    
    @Override
    public int getPriority() {
        return 10; // Höhere Priorität für Branchen-spezifische Validierung
    }
}
```

### 2.3: Plugin Manager Service

```java
package de.freshplan.domain.import.service;

import de.freshplan.domain.import.plugin.CustomValidationPlugin;
import de.freshplan.domain.import.dto.ImportCustomerData;
import de.freshplan.domain.import.dto.ValidationResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.enterprise.inject.Instance;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ValidationPluginManager {
    
    @Inject
    Instance<CustomValidationPlugin> plugins;
    
    public ValidationResult validateWithPlugins(ImportCustomerData data) {
        var result = new ValidationResult();
        
        // Alle Plugins die für diese Branche relevant sind
        List<CustomValidationPlugin> relevantPlugins = plugins.stream()
            .filter(plugin -> plugin.getTargetIndustries().isEmpty() || 
                           plugin.getTargetIndustries().contains(data.getIndustry()))
            .sorted((p1, p2) -> Integer.compare(p2.getPriority(), p1.getPriority()))
            .collect(Collectors.toList());
        
        // Plugins ausführen
        for (CustomValidationPlugin plugin : relevantPlugins) {
            try {
                plugin.validate(data, result);
            } catch (Exception e) {
                result.addError(new ValidationError(
                    data.getRowNumber(),
                    "plugin",
                    "Plugin-Fehler: " + plugin.getName() + " - " + e.getMessage(),
                    "Wenden Sie sich an den Administrator"
                ));
            }
        }
        
        return result;
    }
    
    public List<CustomValidationPlugin> getActivePlugins() {
        return plugins.stream().collect(Collectors.toList());
    }
    
    public List<CustomValidationPlugin> getPluginsForIndustry(String industry) {
        return plugins.stream()
            .filter(plugin -> plugin.getTargetIndustries().isEmpty() || 
                           plugin.getTargetIndustries().contains(industry))
            .collect(Collectors.toList());
    }
}
```

---

## <a id="dynamic-ui"></a>🎨 Phase 3: Dynamic UI Generation (Tag 5-6)

### 3.1: Field Configuration Hook

```typescript
// hooks/useFieldConfigs.ts
import { useState, useEffect } from 'react';
import { api } from '../services/api';

export interface ImportFieldConfig {
  id: string;
  fieldId: string;
  label: string;
  fieldType: 'text' | 'number' | 'email' | 'date' | 'select' | 'boolean' | 'iban' | 'phone';
  required: boolean;
  validationRules: Record<string, any>;
  mappingPatterns: string[];
  businessLogic?: string;
  displayOrder: number;
  category: string;
  active: boolean;
  options?: Array<{
    optionKey: string;
    optionValue: string;
    displayOrder: number;
  }>;
}

export const useFieldConfigs = () => {
  const [configs, setConfigs] = useState<ImportFieldConfig[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchConfigs = async () => {
      try {
        setLoading(true);
        const response = await api.get('/api/import/field-configs');
        setConfigs(response.data);
      } catch (err) {
        setError('Failed to load field configurations');
      } finally {
        setLoading(false);
      }
    };

    fetchConfigs();
  }, []);

  const getConfigsByCategory = (category: string) => {
    return configs.filter(config => config.category === category);
  };

  const getRequiredConfigs = () => {
    return configs.filter(config => config.required);
  };

  const getConfigByFieldId = (fieldId: string) => {
    return configs.find(config => config.fieldId === fieldId);
  };

  return {
    configs,
    loading,
    error,
    getConfigsByCategory,
    getRequiredConfigs,
    getConfigByFieldId
  };
};
```

### 3.2: Dynamic Field Component

```typescript
// components/DynamicField.tsx
import React from 'react';
import {
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  FormHelperText,
  Switch,
  FormControlLabel,
  Checkbox,
  Box
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { ImportFieldConfig } from '../hooks/useFieldConfigs';

interface DynamicFieldProps {
  config: ImportFieldConfig;
  value: any;
  onChange: (fieldId: string, value: any) => void;
  error?: string;
  helperText?: string;
}

export const DynamicField: React.FC<DynamicFieldProps> = ({
  config,
  value,
  onChange,
  error,
  helperText
}) => {
  const handleChange = (newValue: any) => {
    onChange(config.fieldId, newValue);
  };

  const renderField = () => {
    switch (config.fieldType) {
      case 'text':
      case 'email':
      case 'phone':
        return (
          <TextField
            fullWidth
            label={config.label}
            type={config.fieldType === 'email' ? 'email' : 'text'}
            value={value || ''}
            onChange={(e) => handleChange(e.target.value)}
            required={config.required}
            error={!!error}
            helperText={error || helperText}
            inputProps={{
              minLength: config.validationRules?.minLength,
              maxLength: config.validationRules?.maxLength,
              pattern: config.validationRules?.pattern
            }}
          />
        );

      case 'number':
        return (
          <TextField
            fullWidth
            label={config.label}
            type="number"
            value={value || ''}
            onChange={(e) => handleChange(Number(e.target.value))}
            required={config.required}
            error={!!error}
            helperText={error || helperText}
            inputProps={{
              min: config.validationRules?.min,
              max: config.validationRules?.max,
              step: config.validationRules?.step || 1
            }}
          />
        );

      case 'select':
        return (
          <FormControl fullWidth error={!!error}>
            <InputLabel>{config.label}</InputLabel>
            <Select
              value={value || ''}
              onChange={(e) => handleChange(e.target.value)}
              label={config.label}
              required={config.required}
            >
              {config.options?.map((option) => (
                <MenuItem key={option.optionKey} value={option.optionKey}>
                  {option.optionValue}
                </MenuItem>
              ))}
            </Select>
            {(error || helperText) && (
              <FormHelperText>{error || helperText}</FormHelperText>
            )}
          </FormControl>
        );

      case 'boolean':
        return (
          <FormControlLabel
            control={
              <Switch
                checked={value || false}
                onChange={(e) => handleChange(e.target.checked)}
              />
            }
            label={config.label}
          />
        );

      case 'date':
        return (
          <DatePicker
            label={config.label}
            value={value}
            onChange={handleChange}
            slotProps={{
              textField: {
                fullWidth: true,
                required: config.required,
                error: !!error,
                helperText: error || helperText
              }
            }}
          />
        );

      case 'iban':
        return (
          <TextField
            fullWidth
            label={config.label}
            value={value || ''}
            onChange={(e) => handleChange(e.target.value.toUpperCase())}
            required={config.required}
            error={!!error}
            helperText={error || helperText}
            inputProps={{
              pattern: '[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}',
              maxLength: 34
            }}
          />
        );

      default:
        return (
          <TextField
            fullWidth
            label={config.label}
            value={value || ''}
            onChange={(e) => handleChange(e.target.value)}
            required={config.required}
            error={!!error}
            helperText={error || helperText}
          />
        );
    }
  };

  return (
    <Box sx={{ mb: 2 }}>
      {renderField()}
    </Box>
  );
};
```

### 3.3: Smart Column Mapping

```typescript
// components/SmartColumnMapping.tsx
import React, { useState, useEffect } from 'react';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Select,
  MenuItem,
  FormControl,
  Chip,
  Box,
  Button,
  Alert,
  AlertTitle
} from '@mui/material';
import { AutoFixHigh, Psychology } from '@mui/icons-material';
import { ImportFieldConfig } from '../hooks/useFieldConfigs';

interface SmartColumnMappingProps {
  columns: string[];
  fieldConfigs: ImportFieldConfig[];
  mappings: Record<string, string>;
  onMappingChange: (mappings: Record<string, string>) => void;
}

export const SmartColumnMapping: React.FC<SmartColumnMappingProps> = ({
  columns,
  fieldConfigs,
  mappings,
  onMappingChange
}) => {
  const [autoMappedCount, setAutoMappedCount] = useState(0);
  const [suggestions, setSuggestions] = useState<Record<string, string[]>>({});

  useEffect(() => {
    generateSuggestions();
  }, [columns, fieldConfigs]);

  const generateSuggestions = () => {
    const newSuggestions: Record<string, string[]> = {};
    
    columns.forEach(column => {
      const columnLower = column.toLowerCase();
      const matchedConfigs = fieldConfigs.filter(config => 
        config.mappingPatterns.some(pattern => 
          columnLower.includes(pattern.toLowerCase())
        )
      ).sort((a, b) => {
        // Bessere Matches first
        const aMatch = a.mappingPatterns.find(p => columnLower.includes(p.toLowerCase()));
        const bMatch = b.mappingPatterns.find(p => columnLower.includes(p.toLowerCase()));
        return (bMatch?.length || 0) - (aMatch?.length || 0);
      });
      
      newSuggestions[column] = matchedConfigs.map(config => config.fieldId);
    });
    
    setSuggestions(newSuggestions);
  };

  const performAutoMapping = () => {
    const newMappings: Record<string, string> = {};
    let mappedCount = 0;
    
    columns.forEach(column => {
      const bestMatch = suggestions[column]?.[0];
      if (bestMatch) {
        newMappings[column] = bestMatch;
        mappedCount++;
      }
    });
    
    onMappingChange(newMappings);
    setAutoMappedCount(mappedCount);
  };

  const handleMappingChange = (column: string, fieldId: string) => {
    const newMappings = { ...mappings };
    if (fieldId === '') {
      delete newMappings[column];
    } else {
      newMappings[column] = fieldId;
    }
    onMappingChange(newMappings);
  };

  const getConfidenceColor = (column: string) => {
    const suggestionCount = suggestions[column]?.length || 0;
    if (suggestionCount === 0) return 'default';
    if (suggestionCount === 1) return 'success';
    if (suggestionCount <= 3) return 'warning';
    return 'error';
  };

  const requiredFields = fieldConfigs.filter(config => config.required);
  const mappedRequiredFields = requiredFields.filter(field => 
    Object.values(mappings).includes(field.fieldId)
  );

  return (
    <Box>
      {/* Auto-Mapping Results */}
      {autoMappedCount > 0 && (
        <Alert severity="success" sx={{ mb: 3 }}>
          <AlertTitle>Automatische Zuordnung erfolgreich</AlertTitle>
          {autoMappedCount} von {columns.length} Spalten wurden automatisch zugeordnet.
          {mappedRequiredFields.length} von {requiredFields.length} Pflichtfeldern sind abgedeckt.
        </Alert>
      )}

      {/* Quick Actions */}
      <Box sx={{ mb: 3, display: 'flex', gap: 2 }}>
        <Button
          variant="outlined"
          startIcon={<AutoFixHigh />}
          onClick={performAutoMapping}
        >
          Automatisch zuordnen
        </Button>
        <Button
          variant="outlined"
          startIcon={<Psychology />}
          onClick={() => onMappingChange({})}
        >
          Alle löschen
        </Button>
      </Box>

      {/* Mapping Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Ihre Spalte</TableCell>
              <TableCell>Erkannte Daten</TableCell>
              <TableCell>→</TableCell>
              <TableCell>FreshPlan Feld</TableCell>
              <TableCell>Vertrauen</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {columns.map((column) => (
              <TableRow key={column}>
                <TableCell>
                  <Box sx={{ fontWeight: 'bold' }}>
                    {column}
                  </Box>
                </TableCell>
                <TableCell>
                  <Box sx={{ fontSize: '0.8rem', color: 'text.secondary' }}>
                    Beispiel: "Mustermann GmbH"
                  </Box>
                </TableCell>
                <TableCell>→</TableCell>
                <TableCell>
                  <FormControl fullWidth size="small">
                    <Select
                      value={mappings[column] || ''}
                      onChange={(e) => handleMappingChange(column, e.target.value)}
                    >
                      <MenuItem value="">
                        <em>Nicht zuordnen</em>
                      </MenuItem>
                      {fieldConfigs.map((config) => (
                        <MenuItem key={config.fieldId} value={config.fieldId}>
                          {config.label}
                          {config.required && (
                            <Chip 
                              label="Pflicht" 
                              size="small" 
                              color="error" 
                              sx={{ ml: 1 }}
                            />
                          )}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </TableCell>
                <TableCell>
                  <Chip
                    label={`${suggestions[column]?.length || 0} Vorschläge`}
                    size="small"
                    color={getConfidenceColor(column)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Required Fields Status */}
      <Box sx={{ mt: 3 }}>
        <Alert severity={mappedRequiredFields.length === requiredFields.length ? 'success' : 'warning'}>
          <AlertTitle>Pflichtfelder</AlertTitle>
          {mappedRequiredFields.length} von {requiredFields.length} Pflichtfeldern zugeordnet.
          {requiredFields.length > mappedRequiredFields.length && (
            <Box sx={{ mt: 1 }}>
              Fehlende Pflichtfelder: {requiredFields
                .filter(field => !Object.values(mappings).includes(field.fieldId))
                .map(field => field.label)
                .join(', ')
              }
            </Box>
          )}
        </Alert>
      </Box>
    </Box>
  );
};
```

---

## <a id="rule-engine"></a>🧠 Phase 4: Rule Engine Integration (Tag 7-8)

### 4.1: Rule Engine Service

```java
package de.freshplan.domain.import.service;

import de.freshplan.domain.import.dto.ImportCustomerData;
import de.freshplan.domain.import.dto.ValidationResult;
import de.freshplan.domain.import.dto.ValidationError;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.script.ScriptEngine;
import jakarta.script.ScriptEngineManager;
import jakarta.script.ScriptException;

import java.util.Map;
import java.util.HashMap;

@ApplicationScoped
public class BusinessRuleEngine {
    
    private final ScriptEngine scriptEngine;
    
    public BusinessRuleEngine() {
        this.scriptEngine = new ScriptEngineManager().getEngineByName("javascript");
    }
    
    public ValidationResult applyCustomRules(ImportCustomerData data, String businessLogic) {
        var result = new ValidationResult();
        
        if (businessLogic == null || businessLogic.isBlank()) {
            return result;
        }
        
        try {
            // Kontext für Script bereitstellen
            var context = createScriptContext(data);
            scriptEngine.put("data", context);
            scriptEngine.put("result", result);
            
            // Helper-Funktionen
            scriptEngine.eval("""
                function addError(field, message, suggestion) {
                    result.addError(new ValidationError(data.rowNumber, field, message, suggestion));
                }
                
                function addWarning(field, message, suggestion) {
                    result.addWarning(new ValidationError(data.rowNumber, field, message, suggestion));
                }
                
                function isEmpty(value) {
                    return value == null || value === '' || value === undefined;
                }
                
                function isValidEmail(email) {
                    return /^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$/.test(email);
                }
                
                function isValidPhone(phone) {
                    return /^[\\+]?[0-9\\s\\-\\(\\)]{10,}$/.test(phone);
                }
                
                function isValidIBAN(iban) {
                    return /^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$/.test(iban);
                }
            """);
            
            // Custom Business Logic ausführen
            scriptEngine.eval(businessLogic);
            
        } catch (ScriptException e) {
            result.addError(new ValidationError(
                data.getRowNumber(),
                "businessLogic",
                "Fehler in Business Logic: " + e.getMessage(),
                "Wenden Sie sich an den Administrator"
            ));
        }
        
        return result;
    }
    
    private Map<String, Object> createScriptContext(ImportCustomerData data) {
        var context = new HashMap<String, Object>();
        context.put("rowNumber", data.getRowNumber());
        context.put("companyName", data.getCompanyName());
        context.put("email", data.getEmail());
        context.put("phone", data.getPhone());
        context.put("industry", data.getIndustry());
        context.put("revenue", data.getRevenue());
        context.put("customFields", data.getCustomFields());
        return context;
    }
}
```

### 4.2: Beispiel Business Logic Scripts

```javascript
// Beispiel 1: Gastronomie-spezifische Regeln
if (data.industry === 'GASTRO') {
    if (isEmpty(data.customFields['seatingCapacity'])) {
        addWarning('seatingCapacity', 'Sitzplätze fehlen für Gastronomie-Betrieb', 'Ergänzen Sie die Sitzplatz-Anzahl');
    } else {
        var capacity = parseInt(data.customFields['seatingCapacity']);
        if (capacity < 10) {
            addError('seatingCapacity', 'Mindestens 10 Sitzplätze erforderlich', 'Erhöhen Sie die Sitzplatz-Anzahl');
        }
    }
    
    if (isEmpty(data.customFields['licenseNumber'])) {
        addWarning('licenseNumber', 'Gastronomie-Lizenz fehlt', 'Lizenz-Nummer für Compliance erforderlich');
    }
}

// Beispiel 2: Umsatz-basierte Validierung
if (data.revenue != null && data.revenue > 1000000) {
    if (isEmpty(data.customFields['creditRating'])) {
        addWarning('creditRating', 'Kreditbewertung fehlt für Großkunden', 'Bewertung für Risikoanalyse erforderlich');
    }
}

// Beispiel 3: Konsistenz-Prüfungen
if (!isEmpty(data.email) && !isValidEmail(data.email)) {
    addError('email', 'E-Mail-Format ungültig', 'Korrigieren Sie die E-Mail-Adresse');
}

if (!isEmpty(data.phone) && !isValidPhone(data.phone)) {
    addError('phone', 'Telefonnummer-Format ungültig', 'Verwenden Sie internationales Format');
}

// Beispiel 4: Branchen-übergreifende Regeln
if (data.industry === 'IT' && data.revenue < 50000) {
    addWarning('revenue', 'Niedriger Umsatz für IT-Unternehmen', 'Prüfen Sie die Umsatz-Angabe');
}
```

---

## <a id="batch-processing"></a>⚡ Phase 5: Batch Processing & Integration (Tag 9-10)

### 5.1: Streaming Import Service

```java
package de.freshplan.domain.import.service;

import de.freshplan.domain.import.dto.ImportSession;
import de.freshplan.domain.import.dto.ImportProgress;
import de.freshplan.domain.import.dto.ImportResult;
import de.freshplan.domain.customer.service.CustomerService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class StreamingImportService {
    
    @Inject
    CustomerService customerService;
    
    @Inject
    ValidationPluginManager pluginManager;
    
    @Inject
    BusinessRuleEngine ruleEngine;
    
    @Inject
    ImportFieldConfigService fieldConfigService;
    
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    
    @Transactional
    public CompletableFuture<ImportResult> processImportAsync(
        UUID sessionId, 
        Multi<ImportCustomerData> dataStream
    ) {
        return CompletableFuture.supplyAsync(() -> {
            var session = ImportSession.findById(sessionId);
            var result = new ImportResult(sessionId);
            
            try {
                session.status = ImportStatus.PROCESSING;
                session.startedAt = LocalDateTime.now();
                session.persistAndFlush();
                
                // Stream processing mit Batching
                dataStream
                    .group().intoSetsOf(100) // Batch Size
                    .onItem().transformToUniAndConcatenate(batch -> 
                        processBatch(batch, session, result)
                    )
                    .subscribe().with(
                        batchResult -> updateProgress(session, batchResult),
                        failure -> handleFailure(session, failure),
                        () -> completeImport(session, result)
                    );
                
                return result;
            } catch (Exception e) {
                session.status = ImportStatus.FAILED;
                session.persistAndFlush();
                throw new RuntimeException("Import failed", e);
            }
        }, executor);
    }
    
    private Uni<BatchResult> processBatch(
        Set<ImportCustomerData> batch, 
        ImportSession session,
        ImportResult result
    ) {
        return Uni.createFrom().item(() -> {
            var batchResult = new BatchResult();
            
            batch.parallelStream().forEach(data -> {
                try {
                    // 1. Field-based Validation
                    var fieldValidation = validateFields(data);
                    
                    // 2. Plugin-based Validation
                    var pluginValidation = pluginManager.validateWithPlugins(data);
                    
                    // 3. Custom Business Rules
                    var ruleValidation = applyBusinessRules(data);
                    
                    // 4. Validation Results kombinieren
                    var validationResult = mergeValidationResults(
                        fieldValidation, pluginValidation, ruleValidation
                    );
                    
                    if (validationResult.hasErrors()) {
                        batchResult.addError(data, validationResult);
                    } else {
                        // 5. Customer erstellen via bestehender Service
                        var customer = createCustomerFromImportData(data);
                        batchResult.addSuccess(customer);
                    }
                    
                } catch (Exception e) {
                    batchResult.addError(data, e);
                }
            });
            
            return batchResult;
        });
    }
    
    private Customer createCustomerFromImportData(ImportCustomerData data) {
        // Mapping zu bestehendem CustomerService
        var createRequest = new CreateCustomerRequest();
        createRequest.setCompanyName(data.getCompanyName());
        createRequest.setEmail(data.getEmail());
        createRequest.setPhone(data.getPhone());
        createRequest.setStreet(data.getStreet());
        createRequest.setZipCode(data.getZipCode());
        createRequest.setCity(data.getCity());
        createRequest.setIndustry(data.getIndustry());
        createRequest.setRevenue(data.getRevenue());
        
        // Custom Fields als JSON
        createRequest.setCustomFields(data.getCustomFields());
        
        return customerService.createCustomer(createRequest);
    }
    
    private ValidationResult validateFields(ImportCustomerData data) {
        var result = new ValidationResult();
        var fieldConfigs = fieldConfigService.getActiveConfigs();
        
        for (var config : fieldConfigs) {
            var value = data.getFieldValue(config.getFieldId());
            
            // Required validation
            if (config.isRequired() && (value == null || value.toString().isBlank())) {
                result.addError(new ValidationError(
                    data.getRowNumber(),
                    config.getFieldId(),
                    config.getLabel() + " ist ein Pflichtfeld",
                    "Ergänzen Sie den Wert"
                ));
                continue;
            }
            
            // Type-specific validation
            if (value != null) {
                validateFieldType(config, value, result, data.getRowNumber());
            }
        }
        
        return result;
    }
    
    private void validateFieldType(
        ImportFieldConfig config,
        Object value,
        ValidationResult result,
        int rowNumber
    ) {
        switch (config.getFieldType()) {
            case EMAIL:
                if (!isValidEmail(value.toString())) {
                    result.addError(new ValidationError(
                        rowNumber,
                        config.getFieldId(),
                        "Ungültige E-Mail-Adresse",
                        "Korrigieren Sie das E-Mail-Format"
                    ));
                }
                break;
                
            case PHONE:
                if (!isValidPhone(value.toString())) {
                    result.addError(new ValidationError(
                        rowNumber,
                        config.getFieldId(),
                        "Ungültige Telefonnummer",
                        "Verwenden Sie internationales Format"
                    ));
                }
                break;
                
            case IBAN:
                if (!isValidIBAN(value.toString())) {
                    result.addError(new ValidationError(
                        rowNumber,
                        config.getFieldId(),
                        "Ungültige IBAN",
                        "Prüfen Sie die IBAN-Nummer"
                    ));
                }
                break;
                
            case NUMBER:
                try {
                    var number = Double.parseDouble(value.toString());
                    var rules = config.getValidationRules();
                    
                    if (rules.has("min") && number < rules.get("min").asDouble()) {
                        result.addError(new ValidationError(
                            rowNumber,
                            config.getFieldId(),
                            "Wert zu klein (Minimum: " + rules.get("min").asDouble() + ")",
                            "Erhöhen Sie den Wert"
                        ));
                    }
                    
                    if (rules.has("max") && number > rules.get("max").asDouble()) {
                        result.addError(new ValidationError(
                            rowNumber,
                            config.getFieldId(),
                            "Wert zu groß (Maximum: " + rules.get("max").asDouble() + ")",
                            "Reduzieren Sie den Wert"
                        ));
                    }
                } catch (NumberFormatException e) {
                    result.addError(new ValidationError(
                        rowNumber,
                        config.getFieldId(),
                        "Ungültige Zahl",
                        "Geben Sie eine gültige Zahl ein"
                    ));
                }
                break;
        }
    }
}
```

### 5.2: Import Progress Tracking

```typescript
// hooks/useImportProgress.ts
import { useState, useEffect } from 'react';

export interface ImportProgress {
  sessionId: string;
  totalRows: number;
  processedRows: number;
  successCount: number;
  errorCount: number;
  warningCount: number;
  percentComplete: number;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  currentBatch?: number;
  estimatedTimeRemaining?: number;
  recentActivities: Array<{
    timestamp: string;
    message: string;
    type: 'info' | 'warning' | 'error';
  }>;
}

export const useImportProgress = (sessionId: string) => {
  const [progress, setProgress] = useState<ImportProgress | null>(null);
  const [eventSource, setEventSource] = useState<EventSource | null>(null);

  useEffect(() => {
    if (!sessionId) return;

    // Server-Sent Events für Progress Updates
    const source = new EventSource(`/api/import/sessions/${sessionId}/progress`);
    
    source.onmessage = (event) => {
      const data = JSON.parse(event.data) as ImportProgress;
      setProgress(data);
    };

    source.onerror = (error) => {
      console.error('Progress stream error:', error);
      source.close();
    };

    setEventSource(source);

    return () => {
      source.close();
    };
  }, [sessionId]);

  const cancelImport = async () => {
    if (eventSource) {
      eventSource.close();
    }
    
    try {
      await fetch(`/api/import/sessions/${sessionId}/cancel`, {
        method: 'POST'
      });
    } catch (error) {
      console.error('Failed to cancel import:', error);
    }
  };

  return {
    progress,
    cancelImport,
    isComplete: progress?.status === 'COMPLETED',
    isFailed: progress?.status === 'FAILED',
    isProcessing: progress?.status === 'PROCESSING'
  };
};
```

---

## 🎯 ZUSAMMENFASSUNG

Die flexible FC-010 Architektur bietet:

### **Sofortige Vorteile:**
- **Configuration-Driven:** Neue Felder ohne Code-Änderung
- **Plugin-System:** Branchen-spezifische Validierung
- **Dynamic UI:** Automatisch generierte Formulare
- **Smart Mapping:** KI-basierte Spalten-Erkennung

### **Langzeit-Flexibilität:**
- **Rule Engine:** JavaScript-basierte Business Logic
- **Custom Field Types:** Erweiterbare Feld-Typen
- **Streaming Processing:** Skalierbar für große Dateien
- **Legacy Integration:** Nutzt bestehende CustomerService

### **Aufwand-Ersparnis:**
- **Neue Felder:** 5 Min statt 2-3 Tage
- **Branchen-Logic:** 30 Min statt 1-2 Tage
- **UI-Anpassungen:** Automatisch generiert
- **Wartung:** Zentrale Konfiguration

**Die Architektur ist bereit für Implementation!**