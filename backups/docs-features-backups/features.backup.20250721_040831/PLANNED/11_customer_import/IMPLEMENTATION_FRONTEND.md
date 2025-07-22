# FC-010: Customer Import - Frontend Implementation

**Fokus:** React Components, UI/UX und Import Wizard

## 🎨 Import Wizard UI

### Main Import Component

```typescript
// pages/ImportWizard.tsx
import React, { useState } from 'react';
import { Stepper, Step, StepLabel } from '@mui/material';
import { FileUpload } from '../components/FileUpload';
import { FieldMapping } from '../components/FieldMapping';
import { ValidationPreview } from '../components/ValidationPreview';
import { ImportProgress } from '../components/ImportProgress';

const steps = [
  'Datei hochladen',
  'Felder zuordnen',
  'Validierung',
  'Import'
];

export const ImportWizard: React.FC = () => {
  const [activeStep, setActiveStep] = useState(0);
  const [importData, setImportData] = useState<ImportData>({
    file: null,
    configuration: null,
    mappings: {},
    validationResults: null
  });

  const handleNext = () => {
    setActiveStep((prev) => prev + 1);
  };

  const handleBack = () => {
    setActiveStep((prev) => prev - 1);
  };

  return (
    <Box sx={{ width: '100%', p: 3 }}>
      <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
        {steps.map((label) => (
          <Step key={label}>
            <StepLabel>{label}</StepLabel>
          </Step>
        ))}
      </Stepper>

      {activeStep === 0 && (
        <FileUpload
          onFileSelect={(file) => {
            setImportData({ ...importData, file });
            handleNext();
          }}
        />
      )}

      {activeStep === 1 && (
        <FieldMapping
          file={importData.file!}
          onMappingComplete={(mappings) => {
            setImportData({ ...importData, mappings });
            handleNext();
          }}
          onBack={handleBack}
        />
      )}

      {activeStep === 2 && (
        <ValidationPreview
          importData={importData}
          onValidationComplete={(results) => {
            setImportData({ ...importData, validationResults: results });
            handleNext();
          }}
          onBack={handleBack}
        />
      )}

      {activeStep === 3 && (
        <ImportProgress
          importData={importData}
          onComplete={() => {
            // Navigate to customers list
          }}
        />
      )}
    </Box>
  );
};
```

## 📤 File Upload Component

```typescript
// components/FileUpload.tsx
export const FileUpload: React.FC<FileUploadProps> = ({ onFileSelect }) => {
  const [dragActive, setDragActive] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<ParsedData | null>(null);

  const handleDrop = useCallback((e: DragEvent) => {
    e.preventDefault();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFile(e.dataTransfer.files[0]);
    }
  }, []);

  const handleFile = async (file: File) => {
    setFile(file);
    
    // Parse first 10 rows for preview
    const preview = await parseFilePreview(file);
    setPreview(preview);
  };

  const parseFilePreview = async (file: File): Promise<ParsedData> => {
    const text = await file.text();
    
    if (file.name.endsWith('.csv')) {
      return parseCSV(text, { preview: 10 });
    } else if (file.name.endsWith('.xlsx')) {
      return parseExcel(await file.arrayBuffer(), { preview: 10 });
    }
    
    throw new Error('Unsupported file type');
  };

  return (
    <Paper
      sx={{
        p: 4,
        border: dragActive ? '2px dashed #94C456' : '2px dashed #ccc',
        backgroundColor: dragActive ? '#f0f9ff' : 'transparent',
        cursor: 'pointer',
        transition: 'all 0.3s'
      }}
      onDragEnter={() => setDragActive(true)}
      onDragLeave={() => setDragActive(false)}
      onDragOver={(e) => e.preventDefault()}
      onDrop={handleDrop}
    >
      {!file ? (
        <Box textAlign="center">
          <CloudUploadIcon sx={{ fontSize: 64, color: '#94C456', mb: 2 }} />
          <Typography variant="h6" gutterBottom>
            Datei hier ablegen oder klicken zum Auswählen
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Unterstützte Formate: CSV, Excel (.xlsx)
          </Typography>
          <input
            type="file"
            hidden
            accept=".csv,.xlsx"
            onChange={(e) => e.target.files && handleFile(e.target.files[0])}
          />
        </Box>
      ) : (
        <Box>
          <Alert severity="success" sx={{ mb: 2 }}>
            Datei erfolgreich geladen: {file.name}
          </Alert>
          
          {preview && (
            <DataPreview data={preview} maxRows={5} />
          )}
          
          <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
            <Button
              variant="contained"
              onClick={() => onFileSelect(file)}
              sx={{ backgroundColor: '#94C456' }}
            >
              Weiter mit Felderzuordnung
            </Button>
            <Button
              onClick={() => {
                setFile(null);
                setPreview(null);
              }}
            >
              Andere Datei wählen
            </Button>
          </Box>
        </Box>
      )}
    </Paper>
  );
};
```

## 🔄 Dynamic Field Mapping

```typescript
// components/FieldMapping.tsx
export const FieldMapping: React.FC<FieldMappingProps> = ({
  file,
  onMappingComplete,
  onBack
}) => {
  const { configs } = useFieldConfigs();
  const [mappings, setMappings] = useState<FieldMappings>({});
  const [autoMapped, setAutoMapped] = useState<Set<string>>(new Set());

  useEffect(() => {
    // Auto-map fields based on column names
    autoMapFields();
  }, [file, configs]);

  const autoMapFields = async () => {
    const columns = await getFileColumns(file);
    const newMappings: FieldMappings = {};
    const mapped = new Set<string>();

    columns.forEach((column) => {
      const match = findBestFieldMatch(column, configs);
      if (match && match.confidence > 0.8) {
        newMappings[match.field.fieldId] = {
          sourceColumn: column,
          targetField: match.field.fieldId,
          transformation: null
        };
        mapped.add(match.field.fieldId);
      }
    });

    setMappings(newMappings);
    setAutoMapped(mapped);
  };

  const handleFieldMap = (fieldId: string, sourceColumn: string | null) => {
    if (sourceColumn === null) {
      const { [fieldId]: removed, ...rest } = mappings;
      setMappings(rest);
    } else {
      setMappings({
        ...mappings,
        [fieldId]: {
          sourceColumn,
          targetField: fieldId,
          transformation: null
        }
      });
    }
  };

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Felder zuordnen
      </Typography>
      
      <Alert severity="info" sx={{ mb: 3 }}>
        {autoMapped.size} Felder wurden automatisch zugeordnet. 
        Bitte überprüfen und fehlende Felder ergänzen.
      </Alert>

      <Grid container spacing={2}>
        {Object.entries(groupFieldsByCategory(configs)).map(([category, fields]) => (
          <Grid item xs={12} key={category}>
            <Card>
              <CardHeader title={category} />
              <CardContent>
                <Grid container spacing={2}>
                  {fields.map((field) => (
                    <Grid item xs={12} md={6} key={field.fieldId}>
                      <MappingField
                        field={field}
                        columns={columns}
                        mapping={mappings[field.fieldId]}
                        isAutoMapped={autoMapped.has(field.fieldId)}
                        onChange={(column) => handleFieldMap(field.fieldId, column)}
                      />
                    </Grid>
                  ))}
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
        <Button onClick={onBack}>
          Zurück
        </Button>
        <Button
          variant="contained"
          onClick={() => onMappingComplete(mappings)}
          disabled={!hasRequiredMappings(mappings, configs)}
          sx={{ backgroundColor: '#94C456' }}
        >
          Weiter zur Validierung
        </Button>
      </Box>
    </Box>
  );
};
```

## ✅ Validation Preview

```typescript
// components/ValidationPreview.tsx
export const ValidationPreview: React.FC<ValidationPreviewProps> = ({
  importData,
  onValidationComplete,
  onBack
}) => {
  const [validating, setValidating] = useState(true);
  const [results, setResults] = useState<ValidationResults | null>(null);

  useEffect(() => {
    validateData();
  }, []);

  const validateData = async () => {
    setValidating(true);
    
    const response = await apiClient.post('/api/import/validate', {
      file: importData.file,
      mappings: importData.mappings,
      configurationId: importData.configuration?.id
    });
    
    setResults(response.data);
    setValidating(false);
  };

  if (validating) {
    return <ValidationProgress />;
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Validierungsergebnisse
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <MetricCard
            title="Gültige Zeilen"
            value={results!.validRows}
            total={results!.totalRows}
            color="success"
            icon={<CheckCircleIcon />}
          />
        </Grid>
        
        <Grid item xs={12} md={4}>
          <MetricCard
            title="Warnungen"
            value={results!.warnings.length}
            color="warning"
            icon={<WarningIcon />}
          />
        </Grid>
        
        <Grid item xs={12} md={4}>
          <MetricCard
            title="Fehler"
            value={results!.errors.length}
            color="error"
            icon={<ErrorIcon />}
          />
        </Grid>
      </Grid>

      {results!.errors.length > 0 && (
        <ErrorDetails
          errors={results!.errors}
          onFix={(error) => handleErrorFix(error)}
        />
      )}

      {results!.warnings.length > 0 && (
        <WarningDetails
          warnings={results!.warnings}
          onIgnore={(warning) => handleWarningIgnore(warning)}
        />
      )}

      <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
        <Button onClick={onBack}>
          Zurück
        </Button>
        <Button
          variant="contained"
          onClick={() => onValidationComplete(results!)}
          disabled={results!.errors.length > 0}
          sx={{ backgroundColor: '#94C456' }}
        >
          Import starten ({results!.validRows} Zeilen)
        </Button>
      </Box>
    </Box>
  );
};
```

## 📊 Real-time Import Progress

```typescript
// components/ImportProgress.tsx
export const ImportProgress: React.FC<ImportProgressProps> = ({
  importData,
  onComplete
}) => {
  const [job, setJob] = useState<ImportJob | null>(null);
  const [logs, setLogs] = useState<ImportLog[]>([]);

  useEffect(() => {
    startImport();
  }, []);

  useEffect(() => {
    if (!job) return;

    // Subscribe to SSE for real-time updates
    const eventSource = new EventSource(
      `/api/import/jobs/${job.id}/progress`
    );

    eventSource.onmessage = (event) => {
      const update = JSON.parse(event.data);
      setJob(update.job);
      if (update.log) {
        setLogs((prev) => [...prev, update.log]);
      }
    };

    return () => eventSource.close();
  }, [job]);

  const startImport = async () => {
    const response = await apiClient.post('/api/import/start', {
      file: importData.file,
      configurationId: importData.configuration?.id,
      mappings: importData.mappings
    });
    
    setJob(response.data);
  };

  const progress = job
    ? (job.processedRows / job.totalRows) * 100
    : 0;

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Import läuft...
      </Typography>

      <LinearProgress
        variant="determinate"
        value={progress}
        sx={{ height: 20, borderRadius: 10, mb: 2 }}
      />

      <Typography variant="body1" gutterBottom>
        {job?.processedRows || 0} von {job?.totalRows || 0} Zeilen verarbeitet
      </Typography>

      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={4}>
          <Chip
            label={`✅ ${job?.successfulRows || 0} Erfolgreich`}
            color="success"
            sx={{ width: '100%' }}
          />
        </Grid>
        <Grid item xs={4}>
          <Chip
            label={`⚠️ ${job?.skippedRows || 0} Übersprungen`}
            color="warning"
            sx={{ width: '100%' }}
          />
        </Grid>
        <Grid item xs={4}>
          <Chip
            label={`❌ ${job?.failedRows || 0} Fehler`}
            color="error"
            sx={{ width: '100%' }}
          />
        </Grid>
      </Grid>

      <LogViewer logs={logs} maxHeight={300} />

      {job?.status === 'COMPLETED' && (
        <Box sx={{ mt: 3 }}>
          <Alert severity="success" sx={{ mb: 2 }}>
            Import erfolgreich abgeschlossen!
          </Alert>
          <Button
            variant="contained"
            onClick={onComplete}
            sx={{ backgroundColor: '#94C456' }}
          >
            Zu den importierten Kunden
          </Button>
        </Box>
      )}
    </Box>
  );
};
```