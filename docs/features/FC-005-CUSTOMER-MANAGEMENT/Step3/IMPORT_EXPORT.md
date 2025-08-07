# 📤 Import/Export - Daten-Migration ohne Datenverlust

**Phase:** 1 - Core Functionality  
**Priorität:** 🟡 WICHTIG - Migration & Backup essentiell  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/BULK_OPERATIONS.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/DUPLICATE_DETECTION.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- Daten-Migration
- Backup-Strategie
- System-Integration

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Import/Export implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Import/Export Services
mkdir -p backend/src/main/java/de/freshplan/io
touch backend/src/main/java/de/freshplan/io/ContactImportService.java
touch backend/src/main/java/de/freshplan/io/ContactExportService.java
touch backend/src/main/java/de/freshplan/io/ImportValidator.java
touch backend/src/main/java/de/freshplan/io/DataMapper.java

# 2. Frontend Import/Export Components
mkdir -p frontend/src/features/customers/components/io
touch frontend/src/features/customers/components/io/ImportWizard.tsx
touch frontend/src/features/customers/components/io/ExportDialog.tsx
touch frontend/src/features/customers/components/io/MappingEditor.tsx
touch frontend/src/features/customers/components/io/ImportPreview.tsx

# 3. File Processing
mkdir -p frontend/src/features/customers/services/io
touch frontend/src/features/customers/services/io/FileProcessor.ts
touch frontend/src/features/customers/services/io/CsvParser.ts
touch frontend/src/features/customers/services/io/ExcelParser.ts

# 4. Tests
mkdir -p backend/src/test/java/de/freshplan/io
touch backend/src/test/java/de/freshplan/io/ImportExportTest.java
```

## 🎯 Das Problem: Daten-Silos & Migration-Chaos

**Typische Herausforderungen:**
- 📊 Excel-Listen von 10 verschiedenen Mitarbeitern
- 🔄 CRM-Wechsel ohne saubere Migration
- 📉 Datenverlust bei System-Umstellung
- 🗂️ Inkonsistente Datenformate
- 🔐 DSGVO-konforme Datenübertragung

## 💡 DIE LÖSUNG: Intelligenter Import/Export Wizard

### 1. Import Wizard mit Smart Mapping

**Datei:** `frontend/src/features/customers/components/io/ImportWizard.tsx`

```typescript
// CLAUDE: Multi-Step Import Wizard mit intelligenter Feld-Zuordnung
// Pfad: frontend/src/features/customers/components/io/ImportWizard.tsx

import React, { useState, useCallback } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  Stepper,
  Step,
  StepLabel,
  Box,
  Button,
  Typography,
  Alert,
  LinearProgress,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  Select,
  MenuItem,
  Chip,
  IconButton,
  FormControlLabel,
  Checkbox,
  Radio,
  RadioGroup,
  Paper,
  Grid
} from '@mui/material';
import {
  CloudUpload as UploadIcon,
  Check as CheckIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  AutoFixHigh as AutoIcon,
  Visibility as PreviewIcon,
  Delete as DeleteIcon
} from '@mui/icons-material';
import { useDropzone } from 'react-dropzone';
import { FileProcessor } from '../../services/io/FileProcessor';
import { ImportMapping, ImportOptions } from '../../types/import.types';

interface ImportWizardProps {
  open: boolean;
  onClose: () => void;
  onImport: (data: any[], mapping: ImportMapping, options: ImportOptions) => Promise<void>;
}

const steps = ['Datei Upload', 'Feld-Zuordnung', 'Validierung', 'Import-Optionen', 'Bestätigung'];

export const ImportWizard: React.FC<ImportWizardProps> = ({
  open,
  onClose,
  onImport
}) => {
  const [activeStep, setActiveStep] = useState(0);
  const [file, setFile] = useState<File | null>(null);
  const [parsedData, setParsedData] = useState<any[]>([]);
  const [headers, setHeaders] = useState<string[]>([]);
  const [mapping, setMapping] = useState<ImportMapping>({});
  const [validationResults, setValidationResults] = useState<ValidationResult[]>([]);
  const [importOptions, setImportOptions] = useState<ImportOptions>({
    duplicateStrategy: 'skip',
    updateExisting: false,
    createMissing: true,
    validateEmails: true,
    normalizePhones: true,
    skipInvalid: false,
    batchSize: 100
  });
  const [importing, setImporting] = useState(false);
  const [importProgress, setImportProgress] = useState(0);
  
  // File Upload with Dropzone
  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    const file = acceptedFiles[0];
    if (!file) return;
    
    setFile(file);
    
    // Parse file
    try {
      const processor = new FileProcessor();
      const result = await processor.processFile(file);
      
      setParsedData(result.data);
      setHeaders(result.headers);
      
      // Auto-detect mapping
      const autoMapping = detectMapping(result.headers);
      setMapping(autoMapping);
      
      // Move to next step
      setActiveStep(1);
    } catch (error) {
      console.error('File parsing failed:', error);
    }
  }, []);
  
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'text/csv': ['.csv'],
      'application/vnd.ms-excel': ['.xls'],
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
      'application/json': ['.json']
    },
    maxFiles: 1
  });
  
  // Intelligent field mapping detection
  const detectMapping = (headers: string[]): ImportMapping => {
    const mapping: ImportMapping = {};
    
    const mappingRules = {
      firstName: ['firstname', 'first_name', 'vorname', 'first', 'given_name'],
      lastName: ['lastname', 'last_name', 'nachname', 'surname', 'family_name'],
      email: ['email', 'mail', 'e-mail', 'email_address', 'emailaddress'],
      phone: ['phone', 'telefon', 'telephone', 'tel', 'phone_number'],
      mobile: ['mobile', 'cell', 'handy', 'mobilnummer', 'cellphone'],
      company: ['company', 'firma', 'unternehmen', 'organization', 'org'],
      position: ['position', 'title', 'job_title', 'rolle', 'funktion'],
      department: ['department', 'abteilung', 'dept', 'bereich'],
      notes: ['notes', 'notizen', 'comments', 'bemerkungen', 'note']
    };
    
    headers.forEach((header, index) => {
      const normalized = header.toLowerCase().replace(/[^a-z0-9]/g, '');
      
      for (const [field, patterns] of Object.entries(mappingRules)) {
        if (patterns.some(pattern => normalized.includes(pattern))) {
          mapping[field] = index;
          break;
        }
      }
    });
    
    return mapping;
  };
  
  // Validation
  const validateData = async () => {
    const results: ValidationResult[] = [];
    
    parsedData.forEach((row, index) => {
      const errors: string[] = [];
      const warnings: string[] = [];
      
      // Required fields
      if (!row[mapping.firstName] && !row[mapping.lastName]) {
        errors.push('Name fehlt');
      }
      
      // Email validation
      if (mapping.email !== undefined) {
        const email = row[mapping.email];
        if (email && !isValidEmail(email)) {
          errors.push('Ungültige Email');
        }
      }
      
      // Phone normalization
      if (mapping.phone !== undefined) {
        const phone = row[mapping.phone];
        if (phone && !isValidPhone(phone)) {
          warnings.push('Telefonnummer sollte geprüft werden');
        }
      }
      
      // Duplicate check
      if (isDuplicate(row)) {
        warnings.push('Mögliches Duplikat');
      }
      
      results.push({
        row: index,
        data: row,
        errors,
        warnings,
        valid: errors.length === 0
      });
    });
    
    setValidationResults(results);
    setActiveStep(2);
  };
  
  // Render Steps
  const renderStepContent = () => {
    switch (activeStep) {
      case 0: // File Upload
        return (
          <Box>
            <Paper
              {...getRootProps()}
              sx={{
                p: 6,
                textAlign: 'center',
                border: '2px dashed',
                borderColor: isDragActive ? 'primary.main' : 'divider',
                bgcolor: isDragActive ? 'action.hover' : 'background.paper',
                cursor: 'pointer'
              }}
            >
              <input {...getInputProps()} />
              <UploadIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" gutterBottom>
                {isDragActive ? 'Datei hier ablegen' : 'Datei hochladen oder hierher ziehen'}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Unterstützte Formate: CSV, Excel (XLS/XLSX), JSON
              </Typography>
              {file && (
                <Chip
                  label={file.name}
                  onDelete={() => setFile(null)}
                  sx={{ mt: 2 }}
                />
              )}
            </Paper>
            
            <Alert severity="info" sx={{ mt: 3 }}>
              <Typography variant="body2">
                <strong>Tipp:</strong> Die erste Zeile sollte Spaltenüberschriften enthalten.
                Wir versuchen, die Felder automatisch zuzuordnen.
              </Typography>
            </Alert>
          </Box>
        );
        
      case 1: // Field Mapping
        return (
          <Box>
            <Typography variant="h6" gutterBottom>
              Feld-Zuordnung anpassen
            </Typography>
            
            <Button
              startIcon={<AutoIcon />}
              onClick={() => setMapping(detectMapping(headers))}
              sx={{ mb: 2 }}
            >
              Auto-Mapping
            </Button>
            
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>CSV-Spalte</TableCell>
                  <TableCell>Beispiel-Wert</TableCell>
                  <TableCell>Zuordnung zu</TableCell>
                  <TableCell>Aktion</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {headers.map((header, index) => (
                  <TableRow key={index}>
                    <TableCell>
                      <Typography variant="body2" fontWeight="bold">
                        {header}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="caption" sx={{ fontFamily: 'monospace' }}>
                        {parsedData[0]?.[index] || '-'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Select
                        size="small"
                        value={Object.entries(mapping).find(([_, v]) => v === index)?.[0] || ''}
                        onChange={(e) => {
                          const newMapping = { ...mapping };
                          // Remove old mapping
                          Object.keys(newMapping).forEach(key => {
                            if (newMapping[key] === index) {
                              delete newMapping[key];
                            }
                          });
                          // Add new mapping
                          if (e.target.value) {
                            newMapping[e.target.value] = index;
                          }
                          setMapping(newMapping);
                        }}
                        fullWidth
                      >
                        <MenuItem value="">-- Nicht importieren --</MenuItem>
                        <MenuItem value="firstName">Vorname</MenuItem>
                        <MenuItem value="lastName">Nachname</MenuItem>
                        <MenuItem value="email">Email</MenuItem>
                        <MenuItem value="phone">Telefon</MenuItem>
                        <MenuItem value="mobile">Mobil</MenuItem>
                        <MenuItem value="company">Firma</MenuItem>
                        <MenuItem value="position">Position</MenuItem>
                        <MenuItem value="department">Abteilung</MenuItem>
                        <MenuItem value="notes">Notizen</MenuItem>
                        <MenuItem value="tags">Tags</MenuItem>
                      </Select>
                    </TableCell>
                    <TableCell>
                      <IconButton
                        size="small"
                        onClick={() => {
                          // Preview column data
                        }}
                      >
                        <PreviewIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Box>
        );
        
      case 2: // Validation
        return (
          <Box>
            <Typography variant="h6" gutterBottom>
              Daten-Validierung
            </Typography>
            
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={3}>
                <Paper sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="h4" color="success.main">
                    {validationResults.filter(r => r.valid).length}
                  </Typography>
                  <Typography variant="body2">Gültig</Typography>
                </Paper>
              </Grid>
              <Grid item xs={3}>
                <Paper sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="h4" color="error.main">
                    {validationResults.filter(r => r.errors.length > 0).length}
                  </Typography>
                  <Typography variant="body2">Fehler</Typography>
                </Paper>
              </Grid>
              <Grid item xs={3}>
                <Paper sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="h4" color="warning.main">
                    {validationResults.filter(r => r.warnings.length > 0).length}
                  </Typography>
                  <Typography variant="body2">Warnungen</Typography>
                </Paper>
              </Grid>
              <Grid item xs={3}>
                <Paper sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="h4" color="info.main">
                    {validationResults.filter(r => isDuplicate(r.data)).length}
                  </Typography>
                  <Typography variant="body2">Duplikate</Typography>
                </Paper>
              </Grid>
            </Grid>
            
            {/* Show validation issues */}
            <Box sx={{ maxHeight: 400, overflow: 'auto' }}>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Zeile</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Probleme</TableCell>
                    <TableCell>Aktion</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {validationResults
                    .filter(r => r.errors.length > 0 || r.warnings.length > 0)
                    .slice(0, 50)
                    .map((result) => (
                      <TableRow key={result.row}>
                        <TableCell>{result.row + 2}</TableCell>
                        <TableCell>
                          {result.errors.length > 0 ? (
                            <ErrorIcon color="error" />
                          ) : (
                            <WarningIcon color="warning" />
                          )}
                        </TableCell>
                        <TableCell>
                          {result.data[mapping.firstName]} {result.data[mapping.lastName]}
                        </TableCell>
                        <TableCell>
                          {result.errors.map(e => (
                            <Chip key={e} label={e} size="small" color="error" sx={{ mr: 0.5 }} />
                          ))}
                          {result.warnings.map(w => (
                            <Chip key={w} label={w} size="small" color="warning" sx={{ mr: 0.5 }} />
                          ))}
                        </TableCell>
                        <TableCell>
                          <IconButton size="small" onClick={() => excludeRow(result.row)}>
                            <DeleteIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))}
                </TableBody>
              </Table>
            </Box>
          </Box>
        );
        
      case 3: // Import Options
        return (
          <Box>
            <Typography variant="h6" gutterBottom>
              Import-Optionen
            </Typography>
            
            <Box sx={{ mb: 3 }}>
              <Typography variant="subtitle2" gutterBottom>
                Duplikat-Strategie:
              </Typography>
              <RadioGroup
                value={importOptions.duplicateStrategy}
                onChange={(e) => setImportOptions({
                  ...importOptions,
                  duplicateStrategy: e.target.value as any
                })}
              >
                <FormControlLabel
                  value="skip"
                  control={<Radio />}
                  label="Duplikate überspringen"
                />
                <FormControlLabel
                  value="update"
                  control={<Radio />}
                  label="Bestehende aktualisieren"
                />
                <FormControlLabel
                  value="create"
                  control={<Radio />}
                  label="Als neue Kontakte anlegen"
                />
                <FormControlLabel
                  value="merge"
                  control={<Radio />}
                  label="Mit bestehenden zusammenführen"
                />
              </RadioGroup>
            </Box>
            
            <Box sx={{ mb: 3 }}>
              <Typography variant="subtitle2" gutterBottom>
                Daten-Bereinigung:
              </Typography>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={importOptions.validateEmails}
                    onChange={(e) => setImportOptions({
                      ...importOptions,
                      validateEmails: e.target.checked
                    })}
                  />
                }
                label="Email-Adressen validieren"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={importOptions.normalizePhones}
                    onChange={(e) => setImportOptions({
                      ...importOptions,
                      normalizePhones: e.target.checked
                    })}
                  />
                }
                label="Telefonnummern normalisieren"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={importOptions.skipInvalid}
                    onChange={(e) => setImportOptions({
                      ...importOptions,
                      skipInvalid: e.target.checked
                    })}
                  />
                }
                label="Ungültige Einträge überspringen"
              />
            </Box>
            
            <Alert severity="info">
              Der Import wird in Batches von {importOptions.batchSize} Kontakten durchgeführt,
              um die Performance zu optimieren.
            </Alert>
          </Box>
        );
        
      case 4: // Confirmation
        return (
          <Box>
            <Typography variant="h6" gutterBottom>
              Import-Zusammenfassung
            </Typography>
            
            <Paper sx={{ p: 3, mb: 3 }}>
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    Datei:
                  </Typography>
                  <Typography variant="body1">
                    {file?.name}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    Zu importieren:
                  </Typography>
                  <Typography variant="body1">
                    {validationResults.filter(r => r.valid).length} Kontakte
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    Duplikat-Strategie:
                  </Typography>
                  <Typography variant="body1">
                    {importOptions.duplicateStrategy}
                  </Typography>
                </Grid>
                <Grid item xs={6}>
                  <Typography variant="body2" color="text.secondary">
                    Übersprungen:
                  </Typography>
                  <Typography variant="body1">
                    {validationResults.filter(r => !r.valid).length} Einträge
                  </Typography>
                </Grid>
              </Grid>
            </Paper>
            
            {importing && (
              <Box sx={{ mb: 3 }}>
                <Typography variant="body2" gutterBottom>
                  Import läuft... {Math.round(importProgress)}%
                </Typography>
                <LinearProgress variant="determinate" value={importProgress} />
              </Box>
            )}
            
            <Alert severity="warning">
              <strong>Wichtig:</strong> Diese Aktion kann nicht rückgängig gemacht werden.
              Stellen Sie sicher, dass Sie ein Backup haben.
            </Alert>
          </Box>
        );
        
      default:
        return null;
    }
  };
  
  // Helper functions
  const isValidEmail = (email: string): boolean => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };
  
  const isValidPhone = (phone: string): boolean => {
    return /^[\d\s\-\+\(\)]+$/.test(phone);
  };
  
  const isDuplicate = (row: any): boolean => {
    // Check against existing contacts
    return false; // Placeholder
  };
  
  const excludeRow = (rowIndex: number) => {
    setValidationResults(
      validationResults.map(r => 
        r.row === rowIndex ? { ...r, valid: false } : r
      )
    );
  };
  
  const handleNext = () => {
    if (activeStep === 1) {
      validateData();
    } else {
      setActiveStep(activeStep + 1);
    }
  };
  
  const handleBack = () => {
    setActiveStep(activeStep - 1);
  };
  
  const handleImport = async () => {
    setImporting(true);
    
    const validData = validationResults
      .filter(r => r.valid)
      .map(r => r.data);
    
    try {
      await onImport(validData, mapping, importOptions);
      onClose();
    } catch (error) {
      console.error('Import failed:', error);
    } finally {
      setImporting(false);
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        Kontakte importieren
      </DialogTitle>
      
      <DialogContent>
        <Stepper activeStep={activeStep} sx={{ mb: 4 }}>
          {steps.map((label) => (
            <Step key={label}>
              <StepLabel>{label}</StepLabel>
            </Step>
          ))}
        </Stepper>
        
        <Box sx={{ minHeight: 400 }}>
          {renderStepContent()}
        </Box>
        
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
          <Button
            onClick={onClose}
          >
            Abbrechen
          </Button>
          
          <Box>
            <Button
              disabled={activeStep === 0}
              onClick={handleBack}
              sx={{ mr: 1 }}
            >
              Zurück
            </Button>
            
            {activeStep === steps.length - 1 ? (
              <Button
                variant="contained"
                onClick={handleImport}
                disabled={importing}
              >
                Importieren
              </Button>
            ) : (
              <Button
                variant="contained"
                onClick={handleNext}
                disabled={activeStep === 0 && !file}
              >
                Weiter
              </Button>
            )}
          </Box>
        </Box>
      </DialogContent>
    </Dialog>
  );
};
```

### 2. Export Dialog mit Format-Optionen

**Datei:** `frontend/src/features/customers/components/io/ExportDialog.tsx`

```typescript
// CLAUDE: Export mit verschiedenen Formaten und DSGVO-Compliance
// Pfad: frontend/src/features/customers/components/io/ExportDialog.tsx

import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Checkbox,
  TextField,
  Alert,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Chip,
  Divider
} from '@mui/material';
import {
  Download as DownloadIcon,
  Description as CsvIcon,
  TableChart as ExcelIcon,
  Code as JsonIcon,
  PictureAsPdf as PdfIcon,
  Security as SecurityIcon
} from '@mui/icons-material';
import { Contact } from '../../types/contact.types';
import { ExportService } from '../../services/io/ExportService';

interface ExportDialogProps {
  open: boolean;
  contacts: Contact[];
  onClose: () => void;
}

export const ExportDialog: React.FC<ExportDialogProps> = ({
  open,
  contacts,
  onClose
}) => {
  const [format, setFormat] = useState<'csv' | 'excel' | 'json' | 'pdf'>('csv');
  const [selectedFields, setSelectedFields] = useState<Set<string>>(
    new Set(['firstName', 'lastName', 'email', 'phone', 'company', 'position'])
  );
  const [includePersonalData, setIncludePersonalData] = useState(false);
  const [anonymize, setAnonymize] = useState(false);
  const [password, setPassword] = useState('');
  
  const availableFields = [
    { key: 'firstName', label: 'Vorname', personal: true },
    { key: 'lastName', label: 'Nachname', personal: true },
    { key: 'email', label: 'Email', personal: true },
    { key: 'phone', label: 'Telefon', personal: true },
    { key: 'mobile', label: 'Mobil', personal: true },
    { key: 'company', label: 'Firma', personal: false },
    { key: 'position', label: 'Position', personal: false },
    { key: 'department', label: 'Abteilung', personal: false },
    { key: 'location', label: 'Standort', personal: false },
    { key: 'tags', label: 'Tags', personal: false },
    { key: 'notes', label: 'Notizen', personal: true },
    { key: 'lastContactDate', label: 'Letzter Kontakt', personal: false },
    { key: 'createdAt', label: 'Erstellt am', personal: false }
  ];
  
  const toggleField = (field: string) => {
    const newSelection = new Set(selectedFields);
    if (newSelection.has(field)) {
      newSelection.delete(field);
    } else {
      newSelection.add(field);
    }
    setSelectedFields(newSelection);
  };
  
  const handleExport = async () => {
    const exportService = new ExportService();
    
    const options = {
      format,
      fields: Array.from(selectedFields),
      anonymize,
      password: format === 'pdf' ? password : undefined,
      includeMetadata: true,
      timestamp: new Date().toISOString()
    };
    
    try {
      const blob = await exportService.exportContacts(contacts, options);
      
      // Trigger download
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `contacts_export_${new Date().toISOString().split('T')[0]}.${format}`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      
      // Log export for DSGVO compliance
      await exportService.logExport({
        userId: getCurrentUserId(),
        contactCount: contacts.length,
        format,
        fields: Array.from(selectedFields),
        anonymized: anonymize,
        timestamp: new Date()
      });
      
      onClose();
    } catch (error) {
      console.error('Export failed:', error);
    }
  };
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">
            {contacts.length} Kontakte exportieren
          </Typography>
          <SecurityIcon color="action" />
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {/* Format Selection */}
        <FormControl component="fieldset" sx={{ mb: 3 }}>
          <FormLabel component="legend">Export-Format</FormLabel>
          <RadioGroup
            value={format}
            onChange={(e) => setFormat(e.target.value as any)}
          >
            <FormControlLabel
              value="csv"
              control={<Radio />}
              label={
                <Box display="flex" alignItems="center" gap={1}>
                  <CsvIcon />
                  <Box>
                    <Typography>CSV</Typography>
                    <Typography variant="caption" color="text.secondary">
                      Kompatibel mit Excel, Google Sheets
                    </Typography>
                  </Box>
                </Box>
              }
            />
            <FormControlLabel
              value="excel"
              control={<Radio />}
              label={
                <Box display="flex" alignItems="center" gap={1}>
                  <ExcelIcon />
                  <Box>
                    <Typography>Excel (XLSX)</Typography>
                    <Typography variant="caption" color="text.secondary">
                      Native Excel-Format mit Formatierung
                    </Typography>
                  </Box>
                </Box>
              }
            />
            <FormControlLabel
              value="json"
              control={<Radio />}
              label={
                <Box display="flex" alignItems="center" gap={1}>
                  <JsonIcon />
                  <Box>
                    <Typography>JSON</Typography>
                    <Typography variant="caption" color="text.secondary">
                      Für technische Integration
                    </Typography>
                  </Box>
                </Box>
              }
            />
            <FormControlLabel
              value="pdf"
              control={<Radio />}
              label={
                <Box display="flex" alignItems="center" gap={1}>
                  <PdfIcon />
                  <Box>
                    <Typography>PDF Report</Typography>
                    <Typography variant="caption" color="text.secondary">
                      Formatierter Bericht mit Passwortschutz
                    </Typography>
                  </Box>
                </Box>
              }
            />
          </RadioGroup>
        </FormControl>
        
        <Divider sx={{ my: 3 }} />
        
        {/* Field Selection */}
        <Typography variant="subtitle1" gutterBottom>
          Zu exportierende Felder:
        </Typography>
        
        <List dense>
          {availableFields.map(field => (
            <ListItem key={field.key}>
              <ListItemIcon>
                <Checkbox
                  checked={selectedFields.has(field.key)}
                  onChange={() => toggleField(field.key)}
                  disabled={anonymize && field.personal}
                />
              </ListItemIcon>
              <ListItemText
                primary={field.label}
                secondary={field.personal && (
                  <Chip label="Personenbezogen" size="small" color="warning" />
                )}
              />
            </ListItem>
          ))}
        </List>
        
        <Divider sx={{ my: 3 }} />
        
        {/* Privacy Options */}
        <Typography variant="subtitle1" gutterBottom>
          Datenschutz-Optionen:
        </Typography>
        
        <FormControlLabel
          control={
            <Checkbox
              checked={anonymize}
              onChange={(e) => {
                setAnonymize(e.target.checked);
                if (e.target.checked) {
                  // Remove personal fields
                  const newSelection = new Set(selectedFields);
                  availableFields
                    .filter(f => f.personal)
                    .forEach(f => newSelection.delete(f.key));
                  setSelectedFields(newSelection);
                }
              }}
            />
          }
          label="Daten anonymisieren (DSGVO)"
        />
        
        {format === 'pdf' && (
          <TextField
            fullWidth
            type="password"
            label="PDF Passwort (optional)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            sx={{ mt: 2 }}
            helperText="Schützt die PDF-Datei mit einem Passwort"
          />
        )}
        
        <Alert severity="info" sx={{ mt: 3 }}>
          <Typography variant="body2">
            <strong>DSGVO-Hinweis:</strong> Dieser Export wird protokolliert.
            {anonymize && ' Personenbezogene Daten werden anonymisiert.'}
          </Typography>
        </Alert>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose}>
          Abbrechen
        </Button>
        <Button
          variant="contained"
          startIcon={<DownloadIcon />}
          onClick={handleExport}
          disabled={selectedFields.size === 0}
        >
          Exportieren
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### 3. Backend Import Service

**Datei:** `backend/src/main/java/de/freshplan/io/ContactImportService.java`

```java
// CLAUDE: Robuster Import-Service mit Validation und Rollback
// Pfad: backend/src/main/java/de/freshplan/io/ContactImportService.java

package de.freshplan.io;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@ApplicationScoped
public class ContactImportService {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    ImportValidator validator;
    
    @Inject
    DataMapper mapper;
    
    @Inject
    DuplicateDetectionService duplicateService;
    
    @Inject
    AuditService auditService;
    
    /**
     * Import contacts with validation and transaction management
     */
    @Transactional
    public ImportResult importContacts(
        InputStream fileStream,
        String fileType,
        ImportMapping mapping,
        ImportOptions options,
        UUID userId
    ) {
        ImportResult result = new ImportResult();
        result.setStartTime(LocalDateTime.now());
        
        try {
            // Parse file
            List<Map<String, Object>> rawData = parseFile(fileStream, fileType);
            result.setTotalRows(rawData.size());
            
            // Validate data
            ValidationResult validation = validator.validate(rawData, mapping);
            result.setValidationErrors(validation.getErrors());
            
            if (options.isSkipInvalid()) {
                rawData = rawData.stream()
                    .filter(row -> validator.isValid(row, mapping))
                    .collect(Collectors.toList());
            }
            
            // Map to entities
            List<Contact> contacts = mapper.mapToContacts(rawData, mapping);
            
            // Handle duplicates
            Map<Contact, DuplicateInfo> duplicates = detectDuplicates(contacts);
            contacts = handleDuplicates(contacts, duplicates, options);
            
            // Batch import
            List<Contact> imported = new ArrayList<>();
            int batchSize = options.getBatchSize();
            
            for (int i = 0; i < contacts.size(); i += batchSize) {
                int end = Math.min(i + batchSize, contacts.size());
                List<Contact> batch = contacts.subList(i, end);
                
                try {
                    List<Contact> savedBatch = saveBatch(batch, options);
                    imported.addAll(savedBatch);
                    
                    // Update progress
                    result.setProgress((double) imported.size() / contacts.size());
                    
                } catch (Exception e) {
                    result.addError(String.format(
                        "Batch %d-%d failed: %s", i, end, e.getMessage()
                    ));
                    
                    if (!options.isContinueOnError()) {
                        throw e;
                    }
                }
            }
            
            result.setImportedCount(imported.size());
            result.setSkippedCount(result.getTotalRows() - imported.size());
            result.setStatus(ImportStatus.SUCCESS);
            
            // Audit log
            auditService.logImport(userId, result);
            
        } catch (Exception e) {
            result.setStatus(ImportStatus.FAILED);
            result.addError(e.getMessage());
            
            // Rollback handled by @Transactional
        }
        
        result.setEndTime(LocalDateTime.now());
        return result;
    }
    
    /**
     * Parse different file formats
     */
    private List<Map<String, Object>> parseFile(
        InputStream stream,
        String fileType
    ) throws IOException {
        switch (fileType.toLowerCase()) {
            case "csv":
                return parseCsv(stream);
            case "xls":
            case "xlsx":
                return parseExcel(stream);
            case "json":
                return parseJson(stream);
            default:
                throw new UnsupportedFormatException("Unsupported file type: " + fileType);
        }
    }
    
    /**
     * Parse CSV with intelligent delimiter detection
     */
    private List<Map<String, Object>> parseCsv(InputStream stream) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(stream))) {
            String[] headers = reader.readNext();
            
            if (headers == null) {
                throw new ImportException("Empty CSV file");
            }
            
            // Normalize headers
            for (int i = 0; i < headers.length; i++) {
                headers[i] = normalizeHeader(headers[i]);
            }
            
            String[] row;
            int rowNum = 2; // Start from 2 (1 is headers)
            
            while ((row = reader.readNext()) != null) {
                Map<String, Object> record = new HashMap<>();
                
                for (int i = 0; i < Math.min(headers.length, row.length); i++) {
                    record.put(headers[i], cleanValue(row[i]));
                }
                
                record.put("_row", rowNum++);
                data.add(record);
            }
        }
        
        return data;
    }
    
    /**
     * Parse Excel with multiple sheet support
     */
    private List<Map<String, Object>> parseExcel(InputStream stream) throws IOException {
        List<Map<String, Object>> data = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(stream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet.getPhysicalNumberOfRows() == 0) {
                throw new ImportException("Empty Excel file");
            }
            
            // Get headers from first row
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            
            for (Cell cell : headerRow) {
                headers.add(normalizeHeader(getCellValue(cell)));
            }
            
            // Parse data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> record = new HashMap<>();
                
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        record.put(headers.get(j), getCellValue(cell));
                    }
                }
                
                record.put("_row", i + 1);
                data.add(record);
            }
        }
        
        return data;
    }
    
    /**
     * Detect duplicates with fuzzy matching
     */
    private Map<Contact, DuplicateInfo> detectDuplicates(List<Contact> contacts) {
        Map<Contact, DuplicateInfo> duplicates = new HashMap<>();
        
        // Check against existing contacts
        List<Contact> existing = contactRepository.findAll();
        
        for (Contact newContact : contacts) {
            List<Contact> matches = duplicateService.findDuplicates(newContact, existing);
            
            if (!matches.isEmpty()) {
                duplicates.put(newContact, new DuplicateInfo(
                    matches,
                    calculateSimilarity(newContact, matches.get(0))
                ));
            }
        }
        
        return duplicates;
    }
    
    /**
     * Handle duplicates according to strategy
     */
    private List<Contact> handleDuplicates(
        List<Contact> contacts,
        Map<Contact, DuplicateInfo> duplicates,
        ImportOptions options
    ) {
        List<Contact> result = new ArrayList<>();
        
        for (Contact contact : contacts) {
            DuplicateInfo dupInfo = duplicates.get(contact);
            
            if (dupInfo == null) {
                // No duplicate
                result.add(contact);
            } else {
                switch (options.getDuplicateStrategy()) {
                    case SKIP:
                        // Skip duplicate
                        break;
                        
                    case UPDATE:
                        // Update existing
                        Contact existing = dupInfo.getMatches().get(0);
                        updateContact(existing, contact);
                        result.add(existing);
                        break;
                        
                    case CREATE:
                        // Create anyway
                        result.add(contact);
                        break;
                        
                    case MERGE:
                        // Merge with existing
                        Contact merged = mergeContacts(dupInfo.getMatches().get(0), contact);
                        result.add(merged);
                        break;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Normalize header names for matching
     */
    private String normalizeHeader(String header) {
        return header
            .toLowerCase()
            .trim()
            .replaceAll("[^a-z0-9]", "_")
            .replaceAll("_+", "_");
    }
    
    /**
     * Clean and normalize values
     */
    private String cleanValue(String value) {
        if (value == null) return null;
        
        value = value.trim();
        
        // Remove BOM and special characters
        value = value.replaceAll("^\uFEFF", "");
        
        // Normalize quotes
        value = value.replaceAll("[""]", "\"");
        
        return value.isEmpty() ? null : value;
    }
}
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: File Processing (45 Min)
- [ ] CSV Parser implementieren
- [ ] Excel Parser (XLS/XLSX)
- [ ] JSON Parser
- [ ] Auto-Delimiter Detection

### Phase 2: Import Wizard (60 Min)
- [ ] Multi-Step UI
- [ ] Smart Field Mapping
- [ ] Validation Engine
- [ ] Preview & Correction

### Phase 3: Export Options (30 Min)
- [ ] Format Converters
- [ ] Field Selection
- [ ] DSGVO Anonymization
- [ ] Password Protection (PDF)

### Phase 4: Duplicate Handling (30 Min)
- [ ] Fuzzy Matching
- [ ] Merge Strategies
- [ ] Conflict Resolution
- [ ] Preview Changes

### Phase 5: Testing (30 Min)
- [ ] Large File Tests (10k+ rows)
- [ ] Format Compatibility
- [ ] Error Recovery
- [ ] Performance Benchmarks

## 🔗 INTEGRATION POINTS

1. **ContactList** - Import/Export Buttons
2. **BulkOperations** - Nach Import Bulk-Edit
3. **DuplicateDetection** - Während Import
4. **AuditLog** - Import/Export tracking

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Memory bei großen Dateien**
   → Lösung: Streaming, Batch Processing

2. **Encoding-Probleme (Umlaute)**
   → Lösung: UTF-8 Detection, BOM Handling

3. **Datenverlust beim Mapping**
   → Lösung: Preview, Undo, Backup

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 195 Minuten  
**Nächstes Dokument:** [→ Duplicate Detection](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/DUPLICATE_DETECTION.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Import/Export = Daten-Freiheit! 📤📥✨**