# FC-010: Customer Import - Backend Implementation

**Fokus:** Backend-Services, Entities und Validierung

## 📋 Database Schema

```sql
-- Configuration Tables
CREATE TABLE import_configurations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    file_type VARCHAR(50) NOT NULL,
    field_mappings JSONB NOT NULL,
    validation_rules JSONB,
    transformation_rules JSONB,
    is_active BOOLEAN DEFAULT true,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE import_field_configs (
    id UUID PRIMARY KEY,
    field_id VARCHAR(100) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    field_type VARCHAR(50) NOT NULL,
    target_entity VARCHAR(100) NOT NULL,
    target_field VARCHAR(100) NOT NULL,
    validation_rules JSONB,
    transformation_rules JSONB,
    required BOOLEAN DEFAULT false,
    category VARCHAR(100),
    sort_order INTEGER,
    options JSONB,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE import_jobs (
    id UUID PRIMARY KEY,
    configuration_id UUID REFERENCES import_configurations(id),
    file_name VARCHAR(500) NOT NULL,
    file_size BIGINT,
    status VARCHAR(50) NOT NULL,
    total_rows INTEGER,
    processed_rows INTEGER DEFAULT 0,
    successful_rows INTEGER DEFAULT 0,
    failed_rows INTEGER DEFAULT 0,
    error_details JSONB,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

## 🏗️ Core Services

### ImportConfigurationService

```java
@ApplicationScoped
@Transactional
public class ImportConfigurationService {
    
    @Inject
    ImportConfigurationRepository repository;
    
    @Inject
    FieldConfigRepository fieldConfigRepository;
    
    @Inject
    ValidationPluginRegistry validationRegistry;
    
    public ImportConfiguration createConfiguration(
        CreateConfigurationRequest request
    ) {
        // Validate field mappings against available fields
        validateFieldMappings(request.getFieldMappings());
        
        ImportConfiguration config = new ImportConfiguration();
        config.setName(request.getName());
        config.setFileType(request.getFileType());
        config.setFieldMappings(request.getFieldMappings());
        config.setValidationRules(request.getValidationRules());
        
        repository.persist(config);
        return config;
    }
    
    private void validateFieldMappings(Map<String, FieldMapping> mappings) {
        Set<String> availableFields = fieldConfigRepository
            .findAll()
            .stream()
            .map(FieldConfig::getFieldId)
            .collect(Collectors.toSet());
            
        for (String fieldId : mappings.keySet()) {
            if (!availableFields.contains(fieldId)) {
                throw new ValidationException(
                    "Unknown field: " + fieldId
                );
            }
        }
    }
}
```

### ImportJobService

```java
@ApplicationScoped
@Transactional
public class ImportJobService {
    
    @Inject
    ImportJobRepository jobRepository;
    
    @Inject
    ImportProcessor importProcessor;
    
    @Inject
    Event<ImportJobEvent> jobEvents;
    
    @Async
    public CompletableFuture<ImportJob> startImport(
        UUID configurationId,
        InputStream fileStream,
        String fileName
    ) {
        // Create job record
        ImportJob job = createJob(configurationId, fileName);
        
        // Fire start event
        jobEvents.fire(ImportJobEvent.started(job));
        
        try {
            // Process import
            ImportResult result = importProcessor.process(
                job.getId(),
                configurationId,
                fileStream
            );
            
            // Update job with results
            updateJobResult(job, result);
            
            // Fire completion event
            jobEvents.fire(ImportJobEvent.completed(job));
            
        } catch (Exception e) {
            handleJobError(job, e);
            jobEvents.fire(ImportJobEvent.failed(job));
        }
        
        return CompletableFuture.completedFuture(job);
    }
}
```

## 🔌 Plugin Architecture

### Base Validation Plugin

```java
public interface ValidationPlugin {
    
    String getPluginId();
    
    String getDisplayName();
    
    List<ConfigOption> getConfigOptions();
    
    ValidationResult validate(
        String value,
        Map<String, Object> config,
        ImportContext context
    );
}

// Example Implementation
@ApplicationScoped
@ValidationPluginType("EMAIL_VALIDATOR")
public class EmailValidationPlugin implements ValidationPlugin {
    
    @Override
    public ValidationResult validate(
        String value,
        Map<String, Object> config,
        ImportContext context
    ) {
        if (value == null || value.isEmpty()) {
            return ValidationResult.valid();
        }
        
        boolean isValid = EmailValidator
            .getInstance()
            .isValid(value);
            
        return isValid 
            ? ValidationResult.valid()
            : ValidationResult.error("Invalid email format");
    }
}
```

### Plugin Registry

```java
@ApplicationScoped
public class ValidationPluginRegistry {
    
    @Inject
    Instance<ValidationPlugin> plugins;
    
    private Map<String, ValidationPlugin> pluginMap;
    
    @PostConstruct
    void init() {
        pluginMap = StreamSupport
            .stream(plugins.spliterator(), false)
            .collect(Collectors.toMap(
                ValidationPlugin::getPluginId,
                Function.identity()
            ));
    }
    
    public ValidationPlugin getPlugin(String pluginId) {
        ValidationPlugin plugin = pluginMap.get(pluginId);
        if (plugin == null) {
            throw new PluginNotFoundException(pluginId);
        }
        return plugin;
    }
    
    public Collection<ValidationPlugin> getAllPlugins() {
        return pluginMap.values();
    }
}
```

## 🔄 Import Processor

```java
@ApplicationScoped
public class ImportProcessor {
    
    @Inject
    FieldMappingService mappingService;
    
    @Inject
    ValidationService validationService;
    
    @Inject
    TransformationService transformationService;
    
    @Inject
    CustomerService customerService;
    
    public ImportResult process(
        UUID jobId,
        UUID configurationId,
        InputStream fileStream
    ) {
        ImportConfiguration config = loadConfiguration(configurationId);
        List<Map<String, String>> rows = parseFile(fileStream, config);
        
        ImportResult result = new ImportResult();
        
        for (int i = 0; i < rows.size(); i++) {
            try {
                processRow(rows.get(i), config, result);
            } catch (Exception e) {
                result.addError(i, e.getMessage());
            }
        }
        
        return result;
    }
    
    private void processRow(
        Map<String, String> row,
        ImportConfiguration config,
        ImportResult result
    ) {
        // 1. Map fields
        Map<String, Object> mappedData = mappingService
            .mapFields(row, config.getFieldMappings());
            
        // 2. Validate
        ValidationResult validation = validationService
            .validate(mappedData, config.getValidationRules());
            
        if (!validation.isValid()) {
            result.addValidationError(
                row.get("rowNumber"),
                validation.getErrors()
            );
            return;
        }
        
        // 3. Transform
        Map<String, Object> transformedData = transformationService
            .transform(mappedData, config.getTransformationRules());
            
        // 4. Create/Update customer
        Customer customer = createOrUpdateCustomer(transformedData);
        result.addSuccess(customer.getId());
    }
}
```

## 📝 Testing Strategy

```java
@QuarkusTest
class ImportProcessorTest {
    
    @Test
    void testSuccessfulImport() {
        // Given
        ImportConfiguration config = createTestConfig();
        InputStream csvStream = getTestCsv("valid-customers.csv");
        
        // When
        ImportResult result = processor.process(
            UUID.randomUUID(),
            config.getId(),
            csvStream
        );
        
        // Then
        assertThat(result.getSuccessCount()).isEqualTo(10);
        assertThat(result.getErrorCount()).isEqualTo(0);
    }
    
    @Test
    void testValidationErrors() {
        // Test with invalid email addresses
        // Test with missing required fields
        // Test with duplicate detection
    }
}
```