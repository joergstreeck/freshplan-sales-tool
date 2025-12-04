# Lead-Import Technical Specification

**Sprint:** 2.1.8
**Stand:** 2025-12-04
**Status:** IMPLEMENTATION READY

---

## 1. Übersicht

Self-Service Lead-Import für alle Benutzer mit Quota-System und automatischer Freigabe.

### Business Case

| Szenario | Leads | Manuelle Eingabe | Import |
|----------|-------|------------------|--------|
| Messe | 50-200 | 8-16h | 5min |
| Partner-Liste | 100-500 | 16-40h | 10min |
| Event-Teilnehmer | 20-100 | 3-8h | 3min |

**ROI:** 95% Zeitersparnis bei Bulk-Leads

---

## 2. Quota-System

### Import-Limits pro Rolle

| Rolle | Offene Leads (max) | Imports/Tag | Leads/Import |
|-------|-------------------|-------------|--------------|
| SALES | 100 | 3 | 100 |
| MANAGER | 200 | 5 | 200 |
| ADMIN | ∞ | ∞ | 1000 |

### Auto-Approval Logik

```
IF duplicate_rate < 10%
   → Auto-Approve (Import sofort)
ELSE
   → Escalation an MANAGER/ADMIN
   → User erhält Notification: "Import wartet auf Freigabe"
```

### Quota-Prüfung

```java
@ApplicationScoped
public class ImportQuotaService {

    public ImportQuotaResult checkQuota(UUID userId, UserRole role, int leadCount) {
        int currentOpenLeads = leadRepository.countOpenLeadsByOwner(userId);
        int todayImports = importLogRepository.countTodayImports(userId);

        QuotaLimits limits = getQuotaLimits(role);

        if (currentOpenLeads + leadCount > limits.maxOpenLeads()) {
            return ImportQuotaResult.rejected(
                "Limit erreicht: Sie haben bereits %d offene Leads (max. %d)"
                    .formatted(currentOpenLeads, limits.maxOpenLeads())
            );
        }

        if (todayImports >= limits.maxImportsPerDay()) {
            return ImportQuotaResult.rejected(
                "Tageslimit erreicht: %d Imports heute (max. %d)"
                    .formatted(todayImports, limits.maxImportsPerDay())
            );
        }

        if (leadCount > limits.maxLeadsPerImport()) {
            return ImportQuotaResult.rejected(
                "Zu viele Leads: %d (max. %d pro Import)"
                    .formatted(leadCount, limits.maxLeadsPerImport())
            );
        }

        return ImportQuotaResult.approved();
    }

    private QuotaLimits getQuotaLimits(UserRole role) {
        return switch (role) {
            case SALES -> new QuotaLimits(100, 3, 100);
            case MANAGER -> new QuotaLimits(200, 5, 200);
            case ADMIN -> new QuotaLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, 1000);
            default -> new QuotaLimits(50, 1, 50); // Fallback
        };
    }
}
```

---

## 3. Import-Prozess

### 3.1 Flow-Diagramm

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  1. Upload  │ ──► │  2. Mapping │ ──► │  3. Preview │ ──► │  4. Import  │
│  CSV/Excel  │     │  Spalten    │     │  Validation │     │  Execute    │
└─────────────┘     └─────────────┘     └─────────────┘     └─────────────┘
       │                   │                   │                   │
       ▼                   ▼                   ▼                   ▼
  Max 5MB            Auto-Detect         Dedupe-Check       Batch-Insert
  UTF-8/Win1252      + Manual Map        + Quota-Check      + Audit-Log
```

### 3.2 Schritt 1: Upload

**Unterstützte Formate:**
- CSV (UTF-8, Windows-1252, ISO-8859-1)
- XLSX (Excel 2007+)
- XLS (Excel 97-2003) - optional

**Limits:**
- Max. Dateigröße: 5 MB
- Max. Zeilen: 1000 (ADMIN), 200 (MANAGER), 100 (SALES)

**Backend Endpoint:**
```java
@POST
@Path("/api/leads/import/upload")
@Consumes(MediaType.MULTIPART_FORM_DATA)
public ImportUploadResponse uploadFile(
    @FormDataParam("file") InputStream fileStream,
    @FormDataParam("file") FormDataContentDisposition fileDetail
) {
    // 1. Validate file type & size
    // 2. Parse headers (first row)
    // 3. Return column names for mapping
    return new ImportUploadResponse(uploadId, columns, rowCount);
}
```

### 3.3 Schritt 2: Field Mapping

**Auto-Detection Regeln:**

| Spaltenname (DE) | Spaltenname (EN) | Lead-Feld |
|------------------|------------------|-----------|
| Firma, Firmenname, Unternehmen | Company, CompanyName | companyName |
| E-Mail, Email, Mail | Email | email |
| Telefon, Tel, Fon | Phone, Tel | phone |
| PLZ, Postleitzahl | PostalCode, ZIP | postalCode |
| Ort, Stadt | City, Town | city |
| Straße, Strasse, Adresse | Street, Address | street |
| Branche | Industry | businessType |
| Quelle, Herkunft | Source, Origin | source |
| Ansprechpartner, Kontakt | Contact, ContactPerson | contactPerson |
| Position, Funktion | Position, Title | contactPosition |
| Notiz, Notizen, Bemerkung | Notes, Comments | notes |

**Mapping UI:**

```typescript
interface FieldMappingProps {
  uploadId: string;
  columns: string[];
  onMappingComplete: (mapping: Record<string, string>) => void;
}

const LEAD_FIELDS = [
  { key: 'companyName', label: 'Firmenname', required: true },
  { key: 'email', label: 'E-Mail', required: false },
  { key: 'phone', label: 'Telefon', required: false },
  { key: 'city', label: 'Stadt', required: false },
  { key: 'postalCode', label: 'PLZ', required: false },
  { key: 'street', label: 'Straße', required: false },
  { key: 'businessType', label: 'Branche', required: false },
  { key: 'source', label: 'Quelle', required: true },
  { key: 'contactPerson', label: 'Ansprechpartner', required: false },
  { key: 'contactPosition', label: 'Position', required: false },
  { key: 'notes', label: 'Notizen', required: false },
];
```

### 3.4 Schritt 3: Preview & Validation

**Validation Rules:**

```java
public record ValidationResult(
    int totalRows,
    int validRows,
    int errorRows,
    int duplicateRows,
    List<ValidationError> errors
) {}

public record ValidationError(
    int row,
    String column,
    String message,
    String value
) {}

// Validation Examples:
// - Row 23: "email" - "Ungültiges E-Mail-Format" - "nicht@gültig"
// - Row 45: "companyName" - "Pflichtfeld fehlt" - ""
// - Row 67: "phone" - "Ungültiges Telefonnummer-Format" - "abc123"
```

**Dedupe-Check:**

```java
public enum DuplicateType {
    HARD_COLLISION,  // Exakte Übereinstimmung (Name + Stadt)
    SOFT_COLLISION   // Ähnliche Daten (Levenshtein ≤ 2)
}

public record DuplicateMatch(
    int row,
    UUID existingLeadId,
    String existingCompanyName,
    DuplicateType type,
    double similarity
) {}
```

**Preview Response:**

```json
{
  "uploadId": "abc-123",
  "validation": {
    "totalRows": 100,
    "validRows": 85,
    "errorRows": 5,
    "duplicateRows": 10
  },
  "previewRows": [
    {"companyName": "Pizza Roma", "city": "München", "status": "VALID"},
    {"companyName": "Burger King", "city": "Berlin", "status": "DUPLICATE", "matchId": "xyz-789"}
  ],
  "errors": [
    {"row": 23, "column": "email", "message": "Ungültiges Format", "value": "nicht@gültig"}
  ],
  "duplicates": [
    {"row": 45, "existingCompanyName": "Pizza Roma GmbH", "type": "SOFT_COLLISION", "similarity": 0.92}
  ],
  "quotaCheck": {
    "approved": true,
    "currentOpenLeads": 45,
    "maxOpenLeads": 100,
    "remainingCapacity": 55
  }
}
```

### 3.5 Schritt 4: Import Execute

**Duplicate Handling Options:**

```java
public enum DuplicateAction {
    SKIP,      // Überspringen (default)
    OVERRIDE,  // Bestehenden Lead aktualisieren
    CREATE     // Trotzdem anlegen (mit Warnung)
}
```

**Import Request:**

```json
{
  "uploadId": "abc-123",
  "mapping": {
    "Firma": "companyName",
    "E-Mail": "email",
    "Stadt": "city"
  },
  "duplicateAction": "SKIP",
  "source": "MESSE_FRANKFURT_2025",
  "assignToTeam": null
}
```

**Import Response:**

```json
{
  "success": true,
  "imported": 85,
  "skipped": 10,
  "errors": 5,
  "importId": "import-456",
  "errorReportUrl": "/api/leads/import/import-456/errors.csv"
}
```

---

## 4. Backend Implementation

### 4.1 LeadImportService.java

```java
@ApplicationScoped
public class LeadImportService {

    @Inject ImportQuotaService quotaService;
    @Inject LeadRepository leadRepository;
    @Inject ImportLogRepository importLogRepository;

    @Transactional
    public ImportResult executeImport(ImportRequest request, UUID userId, UserRole role) {
        // 1. Quota-Check
        ImportQuotaResult quota = quotaService.checkQuota(userId, role, request.rowCount());
        if (!quota.approved()) {
            throw new QuotaExceededException(quota.message());
        }

        // 2. Parse & Map
        List<LeadImportRow> rows = parseAndMap(request.uploadId(), request.mapping());

        // 3. Validate
        ValidationResult validation = validate(rows);
        if (validation.errorRows() > 0 && !request.ignoreErrors()) {
            return ImportResult.validationFailed(validation);
        }

        // 4. Dedupe-Check (Escalation if > 10%)
        List<DuplicateMatch> duplicates = findDuplicates(rows);
        double duplicateRate = (double) duplicates.size() / rows.size();
        if (duplicateRate > 0.10) {
            return ImportResult.escalationRequired(duplicates, duplicateRate);
        }

        // 5. Batch Insert
        int imported = 0;
        int skipped = 0;
        for (LeadImportRow row : rows) {
            if (row.isDuplicate() && request.duplicateAction() == DuplicateAction.SKIP) {
                skipped++;
                continue;
            }
            Lead lead = mapToLead(row, userId, request.source());
            leadRepository.persist(lead);
            imported++;
        }

        // 6. Import Log
        importLogRepository.persist(new ImportLog(
            userId, imported, skipped, validation.errorRows(), request.source()
        ));

        return ImportResult.success(imported, skipped, validation.errorRows());
    }
}
```

### 4.2 LeadImportResource.java

```java
@Path("/api/leads/import")
@Authenticated
public class LeadImportResource {

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@MultipartForm FileUploadForm form) { /* ... */ }

    @POST
    @Path("/{uploadId}/preview")
    public Response previewImport(
        @PathParam("uploadId") String uploadId,
        ImportMappingRequest mapping
    ) { /* ... */ }

    @POST
    @Path("/{uploadId}/execute")
    public Response executeImport(
        @PathParam("uploadId") String uploadId,
        ImportExecuteRequest request
    ) { /* ... */ }

    @GET
    @Path("/{importId}/errors.csv")
    @Produces("text/csv")
    public Response downloadErrorReport(@PathParam("importId") String importId) { /* ... */ }
}
```

---

## 5. Frontend Components

### 5.1 LeadImportWizard.tsx

```typescript
export function LeadImportWizard() {
  const [activeStep, setActiveStep] = useState(0);
  const [uploadId, setUploadId] = useState<string | null>(null);
  const [columns, setColumns] = useState<string[]>([]);
  const [mapping, setMapping] = useState<Record<string, string>>({});
  const [preview, setPreview] = useState<ImportPreview | null>(null);

  const steps = [
    { label: 'Datei hochladen', component: <FileUploadStep /> },
    { label: 'Spalten zuordnen', component: <FieldMappingStep /> },
    { label: 'Vorschau & Prüfung', component: <PreviewStep /> },
    { label: 'Import starten', component: <ExecuteStep /> },
  ];

  return (
    <Dialog open fullWidth maxWidth="md">
      <DialogTitle>Lead-Import</DialogTitle>
      <DialogContent>
        <Stepper activeStep={activeStep}>
          {steps.map((step) => (
            <Step key={step.label}>
              <StepLabel>{step.label}</StepLabel>
            </Step>
          ))}
        </Stepper>

        <Box sx={{ mt: 3 }}>
          {steps[activeStep].component}
        </Box>
      </DialogContent>
    </Dialog>
  );
}
```

### 5.2 FileUploadStep.tsx

```typescript
export function FileUploadStep({ onUpload }: { onUpload: (result: UploadResult) => void }) {
  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: {
      'text/csv': ['.csv'],
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'],
    },
    maxSize: 5 * 1024 * 1024, // 5 MB
    onDrop: async (files) => {
      const result = await uploadFile(files[0]);
      onUpload(result);
    },
  });

  return (
    <Box
      {...getRootProps()}
      sx={{
        border: '2px dashed',
        borderColor: isDragActive ? 'primary.main' : 'grey.400',
        borderRadius: 2,
        p: 4,
        textAlign: 'center',
        cursor: 'pointer',
        '&:hover': { borderColor: 'primary.main' },
      }}
    >
      <input {...getInputProps()} />
      <CloudUploadIcon sx={{ fontSize: 48, color: 'grey.500' }} />
      <Typography variant="h6" sx={{ mt: 2 }}>
        {isDragActive ? 'Datei hier ablegen...' : 'CSV oder Excel-Datei hier ablegen'}
      </Typography>
      <Typography variant="body2" color="text.secondary">
        oder klicken zum Auswählen (max. 5 MB)
      </Typography>
    </Box>
  );
}
```

### 5.3 PreviewStep.tsx

```typescript
export function PreviewStep({ preview }: { preview: ImportPreview }) {
  return (
    <>
      {/* Quota Info */}
      <Alert severity={preview.quotaCheck.approved ? 'success' : 'error'} sx={{ mb: 2 }}>
        {preview.quotaCheck.approved
          ? `Quota OK: ${preview.quotaCheck.remainingCapacity} Leads verfügbar`
          : preview.quotaCheck.message}
      </Alert>

      {/* Validation Summary */}
      <Grid container spacing={2} sx={{ mb: 2 }}>
        <Grid item xs={3}>
          <StatCard label="Gesamt" value={preview.validation.totalRows} color="info" />
        </Grid>
        <Grid item xs={3}>
          <StatCard label="Gültig" value={preview.validation.validRows} color="success" />
        </Grid>
        <Grid item xs={3}>
          <StatCard label="Duplikate" value={preview.validation.duplicateRows} color="warning" />
        </Grid>
        <Grid item xs={3}>
          <StatCard label="Fehler" value={preview.validation.errorRows} color="error" />
        </Grid>
      </Grid>

      {/* Error List */}
      {preview.errors.length > 0 && (
        <Accordion>
          <AccordionSummary>
            <Typography color="error">
              {preview.errors.length} Fehler gefunden
            </Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Zeile</TableCell>
                  <TableCell>Spalte</TableCell>
                  <TableCell>Fehler</TableCell>
                  <TableCell>Wert</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {preview.errors.map((error, i) => (
                  <TableRow key={i}>
                    <TableCell>{error.row}</TableCell>
                    <TableCell>{error.column}</TableCell>
                    <TableCell>{error.message}</TableCell>
                    <TableCell><code>{error.value}</code></TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </AccordionDetails>
        </Accordion>
      )}

      {/* Preview Table */}
      <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
        Vorschau (erste 5 Zeilen)
      </Typography>
      <DataTable data={preview.previewRows} />
    </>
  );
}
```

---

## 6. Admin-UI Route

Neue Route: `/admin/imports`

```typescript
// AdminImportsPage.tsx
export function AdminImportsPage() {
  return (
    <MainLayoutV2>
      <Typography variant="h4">Import-Verwaltung</Typography>

      <Tabs>
        <Tab label="Import-Historie" />
        <Tab label="Wartende Freigaben" />
        <Tab label="Quota-Übersicht" />
      </Tabs>

      {/* Tab 1: Alle Imports */}
      <ImportHistoryTable />

      {/* Tab 2: Imports mit > 10% Duplikaten */}
      <PendingApprovalsTable />

      {/* Tab 3: User-Quotas */}
      <QuotaOverviewTable />
    </MainLayoutV2>
  );
}
```

---

## 7. Datenbankschema

### Migration: Import-Tracking

```sql
-- Migration: V{NEXT}_add_import_tracking.sql

CREATE TABLE import_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    imported_at TIMESTAMP NOT NULL DEFAULT NOW(),
    total_rows INTEGER NOT NULL,
    imported_count INTEGER NOT NULL,
    skipped_count INTEGER NOT NULL,
    error_count INTEGER NOT NULL,
    source VARCHAR(255),
    file_name VARCHAR(255),
    duplicate_rate DECIMAL(5,2),
    status VARCHAR(50) DEFAULT 'COMPLETED', -- COMPLETED, PENDING_APPROVAL, REJECTED
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMP
);

CREATE INDEX idx_import_logs_user ON import_logs(user_id);
CREATE INDEX idx_import_logs_date ON import_logs(imported_at);
CREATE INDEX idx_import_logs_status ON import_logs(status);
```

---

## 8. Checkliste

- [ ] ImportQuotaService implementieren
- [ ] LeadImportService implementieren
- [ ] LeadImportResource implementieren
- [ ] CSV/Excel Parser (Apache Commons CSV + POI)
- [ ] Frontend: LeadImportWizard
- [ ] Frontend: FileUploadStep (react-dropzone)
- [ ] Frontend: FieldMappingStep
- [ ] Frontend: PreviewStep
- [ ] Frontend: ExecuteStep
- [ ] Frontend: AdminImportsPage
- [ ] Migration: import_logs Tabelle
- [ ] Unit Tests (≥80% Coverage)
- [ ] Integration Tests
- [ ] Route `/admin/imports` hinzufügen
