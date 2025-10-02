# Bestandsleads-Migrations-API Specification - Sprint 2.1.6

**Dokumenttyp:** API Specification (Modul 08 Administration)
**Status:** Planned (Sprint 2.1.6)
**Owner:** team/administration + team/leads-backend
**Sprint:** 2.1.6 (12-18.10.2025)

---

## 📋 Übersicht

Dieses Dokument spezifiziert die **Bestandsleads-Migrations-API** für den Import historischer Lead-Daten aus Legacy-Systemen (Excel, CSV, Alt-CRM).

**Business-Kontext:**
Partner haben oft bereits Leads in Excel/Alt-CRM erfasst. Diese müssen migriert werden OHNE Compliance-Verstöße (Backdating, Activity-Historie, Lead-Protection korrekt berechnen).

**Kritische Anforderungen:**
1. **Dry-Run Mode PFLICHT** vor echtem Import
2. **Historische Datumsfelder explizit** (NICHT automatisch berechnen)
3. **countsAsProgress explizit setzen** (NICHT ableiten)
4. **Duplikaten-Check** mit Warning-Report
5. **Audit-Log für jeden Import** (Nachvollziehbarkeit)
6. **Re-Import-Fähigkeit** bei Fehlern

---

## 🎯 Business-Kontext

### Typische Migrations-Szenarien

**Szenario 1: Excel-Export aus Partner-CRM**
- Partner hat 150 Leads in Excel (Name, Firma, Telefon, Registriert-Datum)
- Historische Datumsfelder: `registered_at` zwischen 2024-01-01 und 2025-09-01
- Activities: Manuell dokumentiert ("2024-05-15: Meeting", "2024-08-20: Sample-Box")

**Szenario 2: Alt-CRM Migration**
- Partner wechselt von Salesforce/Pipedrive zu FreshFoodz CRM
- 500 Leads mit kompletter Historie
- Muss 1:1 übernommen werden (ZERO Datenverlust)

**Szenario 3: Trade-Messe Nacherfassung**
- Partner hat Visitenkarten von Messe gesammelt
- Masse-Import von 50 Leads, alle registriert am Messetag
- Keine Activities, aber Notizen vorhanden

---

## 🏗️ API Design

### 1. Endpoint Definition

**Base URL:** `/api/admin/migration/leads/import` (Modul 08 Administration)

**Method:** POST

**RBAC:** `@RolesAllowed({"admin", "migration-officer"})`

**Request Body:**
```json
{
  "source": "excel-export-2024",  // Identifikator für Audit-Log
  "dryRun": true,  // PFLICHT: true für Validierung, false für echten Import
  "leads": [
    {
      "externalId": "EXCEL-ROW-123",  // Referenz für Error-Reporting
      "stage": 1,
      "companyName": "Restaurant Musterküche",
      "city": "Berlin",
      "businessType": "restaurant",
      "territory": "DE",
      "contact": {
        "firstName": "Max",
        "lastName": "Mustermann",
        "email": "max.mustermann@example.com",
        "phone": "+49 30 12345678",
        "role": "chef"
      },
      "ownerUserId": "partner-abc-123",
      "registeredAt": "2024-06-15T10:00:00Z",  // HISTORISCH!
      "consentGivenAt": "2024-06-15T10:00:00Z",
      "source": "trade_fair",
      "estimatedVolume": 5000.00,
      "notes": "Interessiert an Bio-Produkten",
      "activities": [
        {
          "activityType": "MEETING",
          "activityDate": "2024-07-20T14:00:00Z",
          "summary": "Meeting vor Ort, Produktvorstellung",
          "countsAsProgress": true,  // EXPLIZIT!
          "performedBy": "partner-abc-123"
        },
        {
          "activityType": "SAMPLE_SENT",
          "activityDate": "2024-08-10T09:00:00Z",
          "summary": "Sample-Box versendet (DHL-Tracking: 123456789)",
          "countsAsProgress": true,
          "outcome": "pending",
          "performedBy": "partner-abc-123"
        }
      ]
    }
    // ... weitere Leads (max. 1000 pro Batch)
  ]
}
```

**Response (Dry-Run):**
```json
{
  "importId": "uuid-1234-5678",
  "mode": "dry_run",
  "timestamp": "2025-10-12T14:30:00Z",
  "summary": {
    "total_leads": 150,
    "valid_leads": 145,
    "invalid_leads": 5,
    "duplicate_warnings": 3
  },
  "validation_errors": [
    {
      "externalId": "EXCEL-ROW-42",
      "field": "contact.email",
      "error": "Invalid email format: 'max.mustermann@'"
    },
    {
      "externalId": "EXCEL-ROW-89",
      "field": "registeredAt",
      "error": "Date in future: 2026-01-01"
    }
  ],
  "duplicate_warnings": [
    {
      "externalId": "EXCEL-ROW-15",
      "matched_lead_id": 12345,
      "match_score": 0.95,
      "match_reason": "email_exact_match",
      "recommendation": "skip_or_merge"
    }
  ],
  "valid_leads_sample": [
    {
      "externalId": "EXCEL-ROW-1",
      "companyName": "Restaurant Musterküche",
      "protectionUntil": "2025-01-15T10:00:00Z",  // Berechnet
      "progressDeadline": "2024-09-19T14:00:00Z"  // Berechnet (letzte Activity + 60 Tage)
    }
  ]
}
```

**Response (Echter Import):**
```json
{
  "importId": "uuid-1234-5678",
  "mode": "production",
  "timestamp": "2025-10-12T14:35:00Z",
  "summary": {
    "total_leads": 145,
    "imported_leads": 145,
    "failed_leads": 0,
    "skipped_duplicates": 0
  },
  "imported_lead_ids": [12346, 12347, 12348, ...],
  "audit_log_id": "audit-9876"
}
```

---

## 📝 Validierungsregeln

### 1. Pflichtfelder

**Lead-Level:**
- `companyName` (min. 2 Zeichen)
- `city` (min. 2 Zeichen)
- `territory` (enum: "DE", "CH")
- `ownerUserId` (existierender User)
- `registeredAt` (NICHT in Zukunft)

**Contact-Level (wenn vorhanden):**
- `contact.email` ODER `contact.phone` (mindestens eins)
- `consentGivenAt` PFLICHT wenn Contact vorhanden

**Activity-Level (wenn vorhanden):**
- `activityType` (enum)
- `activityDate` (zwischen `registeredAt` und NOW)
- `countsAsProgress` EXPLIZIT (true/false) PFLICHT

### 2. Business-Validierung

```java
public class LeadImportValidator {

    public ValidationResult validate(ImportLeadRequest request) {
        List<String> errors = new ArrayList<>();

        // 1. Datum-Validierung
        if (request.registeredAt().isAfter(Instant.now())) {
            errors.add("registeredAt cannot be in future");
        }

        // 2. Activities Chronologie
        for (var activity : request.activities()) {
            if (activity.activityDate().isBefore(request.registeredAt())) {
                errors.add("Activity date before lead registration");
            }
            if (activity.activityDate().isAfter(Instant.now())) {
                errors.add("Activity date in future");
            }
        }

        // 3. Owner existiert?
        if (!userService.exists(request.ownerUserId())) {
            errors.add("Owner user not found: " + request.ownerUserId());
        }

        // 4. Consent-Regel
        if (request.contact() != null && request.consentGivenAt() == null) {
            errors.add("Consent required when contact data present");
        }

        // 5. countsAsProgress EXPLIZIT
        for (var activity : request.activities()) {
            if (activity.countsAsProgress() == null) {
                errors.add("countsAsProgress must be explicitly set for activity: " +
                          activity.activityType());
            }
        }

        return errors.isEmpty() ? ValidationResult.valid() :
                                  ValidationResult.invalid(errors);
    }
}
```

---

## 🔍 Duplikaten-Check

### 1. Matching-Algorithmus

**Priorität:**
1. **Email Exact Match** (Score: 1.0) → Hard Duplicate
2. **Phone Normalized Match** (Score: 0.9) → Soft Duplicate
3. **Company + City Fuzzy Match** (Score: 0.7-0.85) → Soft Duplicate

```java
public DuplicateCheckResult checkDuplicate(ImportLeadRequest request) {
    // 1. Email Exact
    Optional<Lead> emailMatch = leadRepository.findByContactEmailNormalized(
        normalizeEmail(request.contact().email())
    );
    if (emailMatch.isPresent()) {
        return DuplicateCheckResult.hardDuplicate(emailMatch.get(), "email_exact");
    }

    // 2. Phone Normalized
    Optional<Lead> phoneMatch = leadRepository.findByContactPhoneNormalized(
        normalizePhone(request.contact().phone())
    );
    if (phoneMatch.isPresent()) {
        return DuplicateCheckResult.softDuplicate(phoneMatch.get(), 0.9, "phone_normalized");
    }

    // 3. Fuzzy Company + City
    List<Lead> fuzzyMatches = leadRepository.findFuzzyCandidates(
        request.companyName(),
        request.city()
    );
    for (Lead candidate : fuzzyMatches) {
        double score = calculateFuzzyScore(request, candidate);
        if (score >= 0.75) {
            return DuplicateCheckResult.softDuplicate(candidate, score, "company_fuzzy");
        }
    }

    return DuplicateCheckResult.noDuplicate();
}
```

### 2. Duplicate-Handling Strategies

**Dry-Run:**
- **Hard Duplicate (Score ≥0.95):** Warning + Recommendation: "skip_or_merge"
- **Soft Duplicate (0.75-0.94):** Warning + Recommendation: "review_manually"
- **No Duplicate:** Proceed

**Production Import:**
- **Hard Duplicate:** Skip by default (konfigurierbar: `skipDuplicates=true`)
- **Soft Duplicate:** Import, aber Audit-Log mit Duplicate-Warnung
- **No Duplicate:** Import

---

## 🔒 Protection & Progress Calculation

### 1. Protection-Until Berechnung

**Regel:** `registeredAt` + 6 Monate (180 Tage)

```java
Instant protectionUntil = request.registeredAt().plus(180, ChronoUnit.DAYS);
lead.setProtectionUntil(protectionUntil);
```

### 2. Progress-Deadline Berechnung

**Regel:** Letzte Progress-Activity + 60 Tage

```java
Instant lastProgressActivity = request.activities().stream()
    .filter(a -> a.countsAsProgress())
    .map(ImportActivityRequest::activityDate)
    .max(Instant::compareTo)
    .orElse(request.registeredAt());

Instant progressDeadline = lastProgressActivity.plus(60, ChronoUnit.DAYS);
lead.setProgressDeadline(progressDeadline);
```

### 3. Protection-Status Ableitung

```java
ProtectionStatus status;

if (Instant.now().isBefore(protectionUntil)) {
    if (progressDeadline.isBefore(Instant.now())) {
        status = ProtectionStatus.EXPIRED;  // Kein Progress in 60 Tagen
    } else {
        status = ProtectionStatus.PROTECTED;  // Aktiver Schutz
    }
} else {
    status = ProtectionStatus.EXPIRED;  // Schutzfrist abgelaufen
}

lead.setProtectionStatus(status);
```

---

## 📊 Batch-Processing

### 1. Batch-Size Limits

**Max. Batch-Size:** 1000 Leads pro Request

**Begründung:**
- Performance: ~10ms pro Lead → ~10s für 1000 Leads
- Memory: ~1KB pro Lead → ~1MB Payload
- Timeout: Request-Timeout 30s → sicher für 1000 Leads

**Bei größeren Imports:**
```bash
# Split 5000 Leads in 5 Batches:
split -l 1000 leads.json leads-batch-

# Import sequenziell:
for batch in leads-batch-*; do
  curl -X POST /api/admin/migration/leads/import \
    -H "Content-Type: application/json" \
    -d @$batch
done
```

### 2. Transaction-Handling

**Strategy:** Batch-Transactional (ganzer Batch oder nichts)

```java
@POST
@Transactional
public Response importLeads(LeadImportRequest request) {
    if (request.dryRun()) {
        // Dry-Run: Keine Transaktion, nur Validierung
        return Response.ok(validateBatch(request)).build();
    }

    try {
        List<Long> importedIds = new ArrayList<>();

        for (ImportLeadRequest leadRequest : request.leads()) {
            Lead lead = createLeadFromImport(leadRequest);
            leadRepository.persist(lead);
            importedIds.add(lead.getId());
        }

        auditService.logBatchImport(request.importId(), importedIds);

        return Response.ok(new ImportResult(importedIds)).build();

    } catch (Exception e) {
        // Rollback: Keine Leads importiert
        log.error("Batch import failed, rolling back", e);
        throw e;
    }
}
```

---

## 🔄 Re-Import bei Fehlern

### 1. Error-Report

**Dry-Run Output speichern:**
```json
{
  "import_id": "uuid-1234",
  "failed_external_ids": ["EXCEL-ROW-42", "EXCEL-ROW-89"],
  "validation_errors": [...]
}
```

**Fehlerhafte Datensätze korrigieren:**
```bash
# 1. Fehlerhafte Rows aus Original-File extrahieren
grep -f failed_ids.txt leads.json > failed_leads.json

# 2. Manuell korrigieren

# 3. Re-Import nur der korrigierten Datensätze
curl -X POST /api/admin/migration/leads/import \
  -d @failed_leads_corrected.json
```

### 2. Partial Import Strategy

**Option 1: Skip-on-Error (Default)**
```json
{
  "dryRun": false,
  "skipOnError": true,  // Bei Fehler: Lead überspringen, Rest importieren
  "leads": [...]
}
```

**Option 2: Fail-Fast**
```json
{
  "dryRun": false,
  "skipOnError": false,  // Bei erstem Fehler: Kompletter Rollback
  "leads": [...]
}
```

---

## 📝 Audit-Log Format

### Batch-Import Event

```json
{
  "event_type": "leads_batch_import",
  "timestamp": "2025-10-12T14:35:00Z",
  "user_id": "admin-xyz-789",
  "user_role": "admin",
  "import_id": "uuid-1234-5678",
  "data": {
    "source": "excel-export-2024",
    "mode": "production",
    "total_leads": 145,
    "imported_leads": 145,
    "failed_leads": 0,
    "skipped_duplicates": 0,
    "imported_lead_ids": [12346, 12347, 12348, ...],
    "duplicate_warnings": 3,
    "batch_duration_ms": 8234
  },
  "ip_address": "192.168.1.100"
}
```

### Per-Lead Audit-Trail

Jeder importierte Lead erhält zusätzlich:
```json
{
  "event_type": "lead_created_via_migration",
  "lead_id": 12346,
  "data": {
    "import_id": "uuid-1234-5678",
    "external_id": "EXCEL-ROW-1",
    "source": "excel-export-2024",
    "registered_at_backdated": "2024-06-15T10:00:00Z",
    "activities_imported": 2
  }
}
```

---

## 🧪 Test-Szenarien

### 1. Dry-Run Happy Path

```java
@Test
void dryRunShouldValidateSuccessfully() {
    LeadImportRequest request = new LeadImportRequest(
        "test-source",
        true,  // Dry-Run
        List.of(validLeadRequest())
    );

    Response response = importResource.importLeads(request);

    assertEquals(200, response.getStatus());
    ImportResult result = response.readEntity(ImportResult.class);
    assertEquals("dry_run", result.mode());
    assertEquals(1, result.summary().validLeads());
}
```

### 2. Validation Error Detection

```java
@Test
void shouldDetectFutureDateValidationError() {
    ImportLeadRequest invalidLead = new ImportLeadRequest(
        "EXCEL-ROW-42",
        1,
        "Test GmbH",
        "Berlin",
        Instant.parse("2026-01-01T00:00:00Z"),  // Zukunft!
        null,
        ...
    );

    ValidationResult result = validator.validate(invalidLead);

    assertFalse(result.isValid());
    assertTrue(result.errors().contains("registeredAt cannot be in future"));
}
```

### 3. Duplicate Detection

```java
@Test
void shouldDetectEmailDuplicate() {
    // Existing lead in DB
    createLead("existing@example.com");

    ImportLeadRequest newLead = importLeadWithEmail("existing@example.com");

    DuplicateCheckResult result = duplicateChecker.check(newLead);

    assertTrue(result.isDuplicate());
    assertEquals(1.0, result.score());
    assertEquals("email_exact", result.reason());
}
```

---

## 📚 Workflow-Diagramm

```
┌─────────────────────────────────────────────────────────────┐
│ 1. PREPARATION                                              │
│    - Export Bestandsleads aus Alt-System (Excel/CSV)       │
│    - Format mapping auf API-Schema                          │
│    - countsAsProgress explizit setzen                       │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. DRY-RUN (PFLICHT)                                        │
│    POST /import { dryRun: true, leads: [...] }              │
│    → Validierung + Duplikaten-Check                         │
│    → Error-Report analysieren                               │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. ERROR-CORRECTION (falls nötig)                           │
│    - Validation-Errors korrigieren                          │
│    - Duplicates entscheiden (Skip/Merge/Review)             │
│    - Erneuter Dry-Run bis 100% valid                        │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. PRODUCTION IMPORT                                        │
│    POST /import { dryRun: false, leads: [...] }             │
│    → Echte Datenbank-Inserts                                │
│    → Protection-Until & Progress-Deadline Berechnung        │
│    → Audit-Log Eintrag                                      │
└─────────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. VERIFICATION                                             │
│    - Imported Leads in UI prüfen                            │
│    - Protection-Status validieren                           │
│    - Activity-Historie komplett?                            │
│    - Audit-Log überprüfen                                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 📚 Referenzen

- **TRIGGER_SPRINT_2_1_6.md:** Zeile 52-61 (Bestandsleads-Migrations-API)
- **TRIGGER_SPRINT_2_1_5.md:** Zeile 95-99 (Backdating Endpoint - verwandt)
- **Modul 08 Administration:** Migration-API gehört zu Admin-Modul
- **Lead.java Entity:** `backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java`
- **LeadActivity.java:** `backend/src/main/java/de/freshplan/modules/leads/domain/LeadActivity.java`

---

**Dokument-Owner:** Jörg Streeck + Claude Code
**Letzte Änderung:** 2025-10-02
**Version:** 1.0 (Planned for Sprint 2.1.6)
