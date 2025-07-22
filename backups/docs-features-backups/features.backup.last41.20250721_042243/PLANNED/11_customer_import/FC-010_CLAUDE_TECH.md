# FC-010: Data Import Configuration 📥 CLAUDE_TECH

**Feature Code:** FC-010  
**Optimiert für:** Claude's 30-Sekunden-Produktivität  
**Original:** 1234 Zeilen → **Optimiert:** ~550 Zeilen (55% Reduktion)

## 🎯 QUICK-LOAD: Sofort produktiv in 30 Sekunden!

### Was macht FC-010?
**Configuration-Driven Import System mit Plugin-Architektur für 5000+ Kunden-Migration**

### Die 4 Kern-Features:
1. **Dynamic Fields** → Neue Felder ohne Code-Änderung via UI
2. **Plugin System** → Branchen-spezifische Validierung erweiterbar
3. **Smart Mapping** → KI erkennt Spalten automatisch ("Firma" → "company_name")
4. **Wizard UX** → Geführter Import mit Live-Vorschau

### Sofort starten:
```bash
# Backend: Import Engine Setup
cd backend
./mvnw quarkus:add-extension -Dextensions="quarkus-hibernate-validator,quarkus-apache-poi"

# Frontend: Wizard Components
cd frontend
npm install react-dropzone papaparse @tanstack/react-table react-hook-form
```

---

## 📦 1. BACKEND: Copy-paste Recipes

### 1.1 Dynamic Field Configuration (5 Minuten)
```java
@Entity
@Table(name = "import_field_configs")
public class ImportFieldConfig {
    @Id @GeneratedValue
    private UUID id;
    
    private String fieldName;
    private String displayLabel;
    
    @Enumerated(EnumType.STRING)
    private FieldType fieldType;
    
    private boolean required;
    private String defaultValue;
    
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> validationRules;
    
    @ElementCollection
    private List<String> mappingPatterns; // ["firma", "company", "name"]
    
    private Integer uiOrder;
    private boolean active = true;
}

@ApplicationScoped
public class FieldConfigurationService {
    
    @Inject ImportFieldConfigRepository repository;
    
    public List<ImportFieldConfig> getActiveFields() {
        return repository.find("active = true ORDER BY uiOrder").list();
    }
    
    public void createField(CreateFieldRequest request) {
        ImportFieldConfig config = new ImportFieldConfig();
        config.setFieldName(request.getFieldName());
        config.setDisplayLabel(request.getDisplayLabel());
        config.setFieldType(request.getFieldType());
        config.setRequired(request.isRequired());
        
        // Auto-generate mapping patterns
        config.setMappingPatterns(generateMappingPatterns(request.getFieldName()));
        
        // Set validation rules based on type
        config.setValidationRules(getDefaultValidationRules(request.getFieldType()));
        
        repository.persist(config);
    }
    
    private List<String> generateMappingPatterns(String fieldName) {
        // Smart pattern generation
        List<String> patterns = new ArrayList<>();
        patterns.add(fieldName.toLowerCase());
        
        // Common variations
        if (fieldName.equals("companyName")) {
            patterns.addAll(List.of("firma", "company", "unternehmen", "name"));
        } else if (fieldName.equals("email")) {
            patterns.addAll(List.of("e-mail", "mail", "email address"));
        } else if (fieldName.equals("phone")) {
            patterns.addAll(List.of("telefon", "tel", "phone number", "handy"));
        }
        
        return patterns;
    }
}
```

### 1.2 Plugin-Based Validation Engine (10 Minuten)
```java
// Plugin Interface
public interface ValidationPlugin {
    String getName();
    boolean supports(String fieldType);
    ValidationResult validate(String value, Map<String, Object> rules);
}

// Plugin Registry
@ApplicationScoped
public class ValidationPluginRegistry {
    
    private final Map<String, ValidationPlugin> plugins = new ConcurrentHashMap<>();
    
    @Inject
    void registerPlugins(@Any Instance<ValidationPlugin> pluginInstances) {
        pluginInstances.forEach(plugin -> 
            plugins.put(plugin.getName(), plugin)
        );
    }
    
    public ValidationResult validate(String fieldName, String value, 
                                   ImportFieldConfig config) {
        List<ValidationResult> results = new ArrayList<>();
        
        // Built-in validations
        if (config.isRequired() && StringUtils.isBlank(value)) {
            return ValidationResult.error("Field is required");
        }
        
        // Type-specific plugins
        plugins.values().stream()
            .filter(p -> p.supports(config.getFieldType().name()))
            .forEach(plugin -> {
                ValidationResult result = plugin.validate(value, 
                    config.getValidationRules());
                if (!result.isValid()) {
                    results.add(result);
                }
            });
            
        return results.isEmpty() ? 
            ValidationResult.success() : 
            ValidationResult.merge(results);
    }
}

// Example Plugin: German Phone Validation
@ApplicationScoped
public class GermanPhoneValidationPlugin implements ValidationPlugin {
    
    private static final Pattern GERMAN_PHONE = Pattern.compile(
        "^(\\+49|0)[1-9][0-9]{1,14}$"
    );
    
    @Override
    public String getName() {
        return "german-phone-validator";
    }
    
    @Override
    public boolean supports(String fieldType) {
        return "phone".equals(fieldType);
    }
    
    @Override
    public ValidationResult validate(String value, Map<String, Object> rules) {
        if (value == null) return ValidationResult.success();
        
        String normalized = value.replaceAll("[\\s\\-\\/()]", "");
        
        if (!GERMAN_PHONE.matcher(normalized).matches()) {
            return ValidationResult.error(
                "Ungültige deutsche Telefonnummer"
            );
        }
        
        return ValidationResult.success();
    }
}

// Business Logic Plugin
@ApplicationScoped  
public class CateringBusinessPlugin implements ValidationPlugin {
    
    @Inject CustomerRepository customerRepo;
    
    @Override
    public ValidationResult validate(String value, Map<String, Object> rules) {
        // Check for duplicate customers
        if ("companyName".equals(rules.get("fieldName"))) {
            Optional<Customer> existing = customerRepo.findByName(value);
            if (existing.isPresent()) {
                return ValidationResult.warning(
                    "Kunde existiert bereits",
                    Map.of("existingId", existing.get().getId())
                );
            }
        }
        
        return ValidationResult.success();
    }
}
```

### 1.3 Smart Column Mapping AI (5 Minuten)
```java
@ApplicationScoped
public class ColumnMappingService {
    
    @Inject ImportFieldConfigRepository fieldConfigRepo;
    
    public Map<String, String> autoMapColumns(List<String> csvHeaders) {
        List<ImportFieldConfig> configs = fieldConfigRepo.listAll();
        Map<String, String> mappings = new HashMap<>();
        
        for (String header : csvHeaders) {
            String normalized = normalizeHeader(header);
            
            // Find best match using patterns
            ImportFieldConfig bestMatch = configs.stream()
                .filter(config -> 
                    config.getMappingPatterns().stream()
                        .anyMatch(pattern -> 
                            normalized.contains(pattern.toLowerCase())
                        )
                )
                .max(Comparator.comparingInt(config -> 
                    calculateMatchScore(normalized, config)
                ))
                .orElse(null);
                
            if (bestMatch != null) {
                mappings.put(header, bestMatch.getFieldName());
            }
        }
        
        return mappings;
    }
    
    private String normalizeHeader(String header) {
        return header.toLowerCase()
            .replaceAll("[^a-z0-9äöüß]", "")
            .trim();
    }
    
    private int calculateMatchScore(String header, ImportFieldConfig config) {
        return config.getMappingPatterns().stream()
            .mapToInt(pattern -> {
                if (header.equals(pattern)) return 100;
                if (header.contains(pattern)) return 50;
                return levenshteinDistance(header, pattern) > 0.7 ? 25 : 0;
            })
            .max()
            .orElse(0);
    }
}
```

### 1.4 Import Pipeline & Batch Processing (10 Minuten)
```java
@ApplicationScoped
public class ImportPipeline {
    
    @Inject ValidationPluginRegistry validationRegistry;
    @Inject CustomerService customerService;
    @Inject EventBus eventBus;
    
    @Transactional
    public ImportResult processImport(ImportRequest request) {
        ImportResult result = new ImportResult();
        List<ImportError> errors = new ArrayList<>();
        
        // Parse CSV
        List<Map<String, String>> rows = parseCSV(request.getFileData());
        
        // Batch processing with progress
        int batchSize = 100;
        for (int i = 0; i < rows.size(); i += batchSize) {
            List<Map<String, String>> batch = rows.subList(i, 
                Math.min(i + batchSize, rows.size()));
                
            ProcessBatchResult batchResult = processBatch(batch, 
                request.getFieldMappings());
                
            result.addProcessed(batchResult.getSuccessCount());
            errors.addAll(batchResult.getErrors());
            
            // Publish progress
            eventBus.publish(new ImportProgressEvent(
                request.getImportId(),
                (i + batch.size()) * 100.0 / rows.size()
            ));
        }
        
        result.setErrors(errors);
        return result;
    }
    
    private ProcessBatchResult processBatch(List<Map<String, String>> batch,
                                          Map<String, String> mappings) {
        ProcessBatchResult result = new ProcessBatchResult();
        
        for (int rowNum = 0; rowNum < batch.size(); rowNum++) {
            Map<String, String> row = batch.get(rowNum);
            
            try {
                // Map and validate
                Customer customer = mapToCustomer(row, mappings);
                ValidationResult validation = validateCustomer(customer);
                
                if (validation.isValid()) {
                    customerService.createOrUpdate(customer);
                    result.incrementSuccess();
                } else {
                    result.addError(new ImportError(
                        rowNum,
                        validation.getErrors()
                    ));
                }
            } catch (Exception e) {
                result.addError(new ImportError(rowNum, e.getMessage()));
            }
        }
        
        return result;
    }
}

// Import Job for large files
@ApplicationScoped
public class ImportJobService {
    
    @Inject ImportPipeline pipeline;
    @Inject Scheduler scheduler;
    
    public void scheduleImport(ImportRequest request) {
        String jobId = UUID.randomUUID().toString();
        
        // For files > 10MB, process async
        if (request.getFileSize() > 10_000_000) {
            scheduler.schedule(() -> 
                processLargeImport(jobId, request)
            );
            
            // Return job ID immediately
            request.setJobId(jobId);
            request.setStatus(ImportStatus.SCHEDULED);
        } else {
            // Process inline for small files
            ImportResult result = pipeline.processImport(request);
            request.setStatus(ImportStatus.COMPLETED);
            request.setResult(result);
        }
    }
}
```

---

## 🎨 2. FRONTEND: Dynamic Import Wizard

### 2.1 Import Wizard Component (10 Minuten)
```typescript
export const ImportWizard: React.FC = () => {
  const [step, setStep] = useState(0);
  const [importData, setImportData] = useState<ImportData>({
    file: null,
    mappings: {},
    preview: [],
    validation: {}
  });

  const steps = [
    { label: 'Upload', component: FileUploadStep },
    { label: 'Mapping', component: ColumnMappingStep },
    { label: 'Validation', component: ValidationStep },
    { label: 'Import', component: ImportStep }
  ];

  return (
    <Box sx={{ width: '100%' }}>
      <Stepper activeStep={step}>
        {steps.map((s) => (
          <Step key={s.label}>
            <StepLabel>{s.label}</StepLabel>
          </Step>
        ))}
      </Stepper>
      
      <Box sx={{ mt: 4 }}>
        {React.createElement(steps[step].component, {
          data: importData,
          onUpdate: setImportData,
          onNext: () => setStep(step + 1),
          onBack: () => setStep(step - 1)
        })}
      </Box>
    </Box>
  );
};

// File Upload with Preview
const FileUploadStep: React.FC<StepProps> = ({ data, onUpdate, onNext }) => {
  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    const file = acceptedFiles[0];
    
    // Parse first 10 rows for preview
    const text = await file.text();
    const parsed = Papa.parse(text, { header: true });
    
    onUpdate({
      ...data,
      file,
      preview: parsed.data.slice(0, 10),
      headers: parsed.meta.fields || []
    });
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'text/csv': ['.csv'],
      'application/vnd.ms-excel': ['.xls', '.xlsx']
    },
    maxFiles: 1
  });

  return (
    <Box>
      <Paper
        {...getRootProps()}
        sx={{
          p: 4,
          border: '2px dashed',
          borderColor: isDragActive ? 'primary.main' : 'grey.300',
          bgcolor: isDragActive ? 'action.hover' : 'background.paper',
          cursor: 'pointer'
        }}
      >
        <input {...getInputProps()} />
        <Box textAlign="center">
          <CloudUploadIcon sx={{ fontSize: 48, color: 'primary.main' }} />
          <Typography variant="h6">
            {isDragActive ? 'Drop file here' : 'Drag & drop CSV/Excel file'}
          </Typography>
        </Box>
      </Paper>

      {data.preview.length > 0 && (
        <Box sx={{ mt: 3 }}>
          <Typography variant="h6" gutterBottom>
            Preview (First 10 rows)
          </Typography>
          <DataPreviewTable data={data.preview} />
        </Box>
      )}

      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end' }}>
        <Button
          variant="contained"
          onClick={onNext}
          disabled={!data.file}
        >
          Next: Map Columns
        </Button>
      </Box>
    </Box>
  );
};

// Smart Column Mapping
const ColumnMappingStep: React.FC<StepProps> = ({ data, onUpdate, onNext, onBack }) => {
  const { data: fields } = useQuery({
    queryKey: ['import-fields'],
    queryFn: api.getImportFields
  });

  const { data: autoMappings } = useQuery({
    queryKey: ['auto-mappings', data.headers],
    queryFn: () => api.getAutoMappings(data.headers),
    enabled: !!data.headers
  });

  const [mappings, setMappings] = useState(data.mappings);

  useEffect(() => {
    if (autoMappings) {
      setMappings(autoMappings);
    }
  }, [autoMappings]);

  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Map CSV Columns to Fields
      </Typography>
      
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>CSV Column</TableCell>
              <TableCell>Sample Data</TableCell>
              <TableCell>Map to Field</TableCell>
              <TableCell>Auto-Detected</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data.headers?.map((header) => (
              <TableRow key={header}>
                <TableCell>{header}</TableCell>
                <TableCell>
                  <Chip label={data.preview[0]?.[header] || 'Empty'} />
                </TableCell>
                <TableCell>
                  <Select
                    value={mappings[header] || ''}
                    onChange={(e) => setMappings({
                      ...mappings,
                      [header]: e.target.value
                    })}
                    fullWidth
                  >
                    <MenuItem value="">Skip</MenuItem>
                    {fields?.map(field => (
                      <MenuItem key={field.id} value={field.fieldName}>
                        {field.displayLabel}
                      </MenuItem>
                    ))}
                  </Select>
                </TableCell>
                <TableCell>
                  {autoMappings?.[header] && (
                    <Chip
                      label="✓ Auto"
                      color="success"
                      size="small"
                    />
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'space-between' }}>
        <Button onClick={onBack}>Back</Button>
        <Button
          variant="contained"
          onClick={() => {
            onUpdate({ ...data, mappings });
            onNext();
          }}
        >
          Next: Validate Data
        </Button>
      </Box>
    </Box>
  );
};
```

### 2.2 Field Configuration UI (5 Minuten)
```typescript
export const FieldConfigurationPanel: React.FC = () => {
  const { data: fields, refetch } = useQuery({
    queryKey: ['import-fields'],
    queryFn: api.getImportFields
  });

  const [isAddingField, setIsAddingField] = useState(false);

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" mb={2}>
        <Typography variant="h5">Import Field Configuration</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setIsAddingField(true)}
        >
          Add Field
        </Button>
      </Box>

      <DragDropContext onDragEnd={handleDragEnd}>
        <Droppable droppableId="fields">
          {(provided) => (
            <List {...provided.droppableProps} ref={provided.innerRef}>
              {fields?.map((field, index) => (
                <Draggable key={field.id} draggableId={field.id} index={index}>
                  {(provided) => (
                    <ListItem
                      ref={provided.innerRef}
                      {...provided.draggableProps}
                      {...provided.dragHandleProps}
                    >
                      <FieldConfigItem
                        field={field}
                        onUpdate={handleFieldUpdate}
                        onDelete={handleFieldDelete}
                      />
                    </ListItem>
                  )}
                </Draggable>
              ))}
              {provided.placeholder}
            </List>
          )}
        </Droppable>
      </DragDropContext>

      <Dialog open={isAddingField} onClose={() => setIsAddingField(false)}>
        <AddFieldDialog
          onAdd={(field) => {
            api.createImportField(field);
            refetch();
            setIsAddingField(false);
          }}
        />
      </Dialog>
    </Box>
  );
};
```

---

## 🗄️ 3. DATENBANK: Schema & Migration

### 3.1 Complete Schema (Copy-paste ready)
```sql
-- V1.0.0__import_configuration.sql

-- Field Configuration
CREATE TABLE import_field_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    field_name VARCHAR(50) NOT NULL UNIQUE,
    display_label VARCHAR(100) NOT NULL,
    field_type VARCHAR(20) NOT NULL,
    is_required BOOLEAN DEFAULT FALSE,
    default_value TEXT,
    validation_rules JSONB DEFAULT '{}',
    mapping_patterns TEXT[] DEFAULT '{}',
    ui_order INTEGER NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Import History
CREATE TABLE import_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename VARCHAR(255) NOT NULL,
    file_size BIGINT,
    total_rows INTEGER,
    processed_rows INTEGER DEFAULT 0,
    success_count INTEGER DEFAULT 0,
    error_count INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'PENDING',
    field_mappings JSONB,
    error_details JSONB,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Validation Plugins
CREATE TABLE validation_plugins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    plugin_class VARCHAR(255) NOT NULL,
    supported_types TEXT[],
    configuration JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Import Templates
CREATE TABLE import_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    field_mappings JSONB NOT NULL,
    validation_overrides JSONB,
    is_default BOOLEAN DEFAULT FALSE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_import_jobs_status ON import_jobs(status);
CREATE INDEX idx_import_jobs_created_by ON import_jobs(created_by);
CREATE INDEX idx_field_configs_active ON import_field_configs(is_active, ui_order);

-- Default Fields
INSERT INTO import_field_configs (field_name, display_label, field_type, is_required, ui_order, mapping_patterns) VALUES
('companyName', 'Firmenname', 'text', true, 1, ARRAY['firma', 'company', 'unternehmen', 'name']),
('email', 'E-Mail', 'email', true, 2, ARRAY['email', 'e-mail', 'mail']),
('phone', 'Telefon', 'phone', false, 3, ARRAY['telefon', 'tel', 'phone', 'handy']),
('address', 'Adresse', 'text', false, 4, ARRAY['adresse', 'address', 'strasse']),
('postalCode', 'PLZ', 'text', false, 5, ARRAY['plz', 'postleitzahl', 'zip']),
('city', 'Stadt', 'text', false, 6, ARRAY['stadt', 'city', 'ort']);
```

---

## ✅ 4. TESTING & VALIDATION

```java
@QuarkusTest
class ImportPipelineTest {
    
    @Test
    void testSmartColumnMapping() {
        List<String> headers = List.of("Firma", "E-Mail Adresse", "Tel.-Nr.");
        
        Map<String, String> mappings = mappingService.autoMapColumns(headers);
        
        assertThat(mappings)
            .containsEntry("Firma", "companyName")
            .containsEntry("E-Mail Adresse", "email")
            .containsEntry("Tel.-Nr.", "phone");
    }
    
    @Test
    void testPluginValidation() {
        String germanPhone = "+49 30 12345678";
        
        ValidationResult result = validator.validate(
            "phone", germanPhone, phoneConfig
        );
        
        assertThat(result.isValid()).isTrue();
    }
}
```

---

## 🎯 IMPLEMENTATION PRIORITIES

1. **Phase 1 (3 Tage)**: Field Configuration + Basic Import
2. **Phase 2 (3 Tage)**: Plugin System + Validation Engine
3. **Phase 3 (2 Tage)**: Smart Mapping + Import Wizard
4. **Phase 4 (2 Tage)**: Batch Processing + Error Handling

**Geschätzter Aufwand:** 10 Entwicklungstage