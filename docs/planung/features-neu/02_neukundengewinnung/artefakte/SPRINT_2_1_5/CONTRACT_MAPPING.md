---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "contract"
status: "in_progress"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-10-01"
---

# Sprint 2.1.5 – Contract Mapping

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Contract Mapping

## Vertrag → Implementation Mapping

### Lead-Schutz (§3.2 Handelsvertretervertrag)

**Vertragstext:**
> "Der Lead-Schutz beträgt 6 Monate ab Registrierung (Firma, Ort und zentraler Kontakt oder dokumentierter Erstkontakt)."

**Implementation (ADR-004: Inline-First Architecture):**
```sql
-- leads.protection_start_at (bereits vorhanden in Lead.java)
-- Berechnung: registered_at + protection_months * INTERVAL '1 month'
-- Function: calculate_protection_until(registered_at, protection_months)
-- Kein separates lead_protection table (siehe ADR-003)

-- Nutzung in LeadProtectionService.java:
Duration protectionDuration = Duration.ofDays(lead.getProtectionMonths() * 30L);
LocalDateTime protectionUntil = lead.getProtectionStartAt().plus(protectionDuration);
```

### Aktivitätsstandard (§3.3)

**Vertragstext:**
> "Aktivitätsstandard: belegbarer Fortschritt je 60 Tage"

**Implementation (V255-V256: Inline Fields + Trigger):**
```sql
-- V255: Neue Felder in leads
ALTER TABLE leads ADD COLUMN progress_warning_sent_at TIMESTAMPTZ NULL;
ALTER TABLE leads ADD COLUMN progress_deadline TIMESTAMPTZ NULL;

-- V256: lead_activities augmentation
ALTER TABLE lead_activities ADD COLUMN counts_as_progress BOOLEAN DEFAULT FALSE;

-- V257: Trigger update_progress_on_activity
-- Bei INSERT/UPDATE lead_activities mit counts_as_progress=true:
UPDATE leads
SET progress_deadline = NOW() + INTERVAL '60 days',
    last_activity_at = NEW.performed_at
WHERE id = NEW.lead_id;
```

### Stop-the-Clock (§3.3.2)

**Vertragstext:**
> "Stop-the-Clock bei FreshFoodz-Zuarbeit/Sperrfristen"

**Implementation (Bestehende Felder in Lead.java):**
```java
// Lead.java - bereits vorhanden:
@Column(name = "clock_stopped_at")
private LocalDateTime clockStoppedAt;

@Column(name = "stop_reason", length = 500)
private String stopReason;

@Column(name = "stop_approved_by", length = 50)
private String stopApprovedBy;

// LeadProtectionService.java - Business Logic:
public boolean canStopClock(Lead lead, String reason, String approvedBy) {
    if (lead.getClockStoppedAt() != null) return false; // bereits gestoppt
    lead.setClockStoppedAt(LocalDateTime.now());
    lead.setStopReason(reason);
    lead.setStopApprovedBy(approvedBy);
    return true;
}
```

### Datenminimierung (§5.1 DSGVO-Annex)

**Vertragstext:**
> "Vormerkung: nur Firma/Ort (optional Branche)"

**Implementation (V255: stage Feld + Business Logic):**
```sql
-- V255: Progressive Profiling Stage
ALTER TABLE leads ADD COLUMN stage SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE leads ADD CONSTRAINT leads_stage_chk CHECK (stage BETWEEN 0 AND 2);

COMMENT ON COLUMN leads.stage IS
  'DSGVO Art.5: Progressive Profiling Stage (0=Vormerkung, 1=Registrierung, 2=Qualifiziert)';
```

```typescript
// Frontend: LeadWizard.vue - Stage 0 (Vormerkung)
interface LeadStage0 {
  stage: 0,
  companyName: string;  // PFLICHT
  city: string;         // PFLICHT
  industry?: string;    // OPTIONAL
  // KEINE personenbezogenen Daten!
}

// Stage 1 - Registrierung mit Kontakt
interface LeadStage1 extends LeadStage0 {
  stage: 1,
  contact?: {
    firstName?: string;
    lastName?: string;
    email?: string;
    phone?: string;
  };
}

// Stage 2 - Qualifiziert
interface LeadStage2 extends LeadStage1 {
  stage: 2,
  vatId?: string;
  expectedVolume?: number;
}
```

## § 2(8) Abdeckung - Vollständige vertragliche Anforderungen

| Klausel | Implementierung | Artefakt/Endpoint | Sprint | Audit-Event |
|---------|----------------|-------------------|--------|-------------|
| §2(8)(a) Lead-Schutz 6 Monate | `leads.protection_start_at` + `protection_months` (bereits vorhanden in Lead.java) | `calculate_protection_until()` Function (V257) | 2.1.5 | `lead_protection_started` |
| §2(8)(a) **Lead-Registrierung & Schutzbeginn** | Backdating durch Admin/Manager; Felder bereits vorhanden (`registered_at_override_reason`) | ⏸️ **PUT /api/leads/{id}/registered-at** (verschoben auf 2.1.6) | 2.1.6 | `lead_registered_at_backdated` |
| §2(8)(b) 60-Tage-Aktivitätsstandard | `leads.progress_deadline` (V255) + Trigger (V257) bei `counts_as_progress=true` | V255-V257 Migrations | 2.1.5 | `lead_activity_logged` |
| §2(8)(c) **Erinnerung + 10 Tage Nachfrist** | `leads.progress_warning_sent_at` (V255) + Nightly Job | Nightly Job (2.1.6) | 2.1.6 | `lead_protection_warning` |
| §2(8)(d) Stop-the-Clock | `leads.clock_stopped_at`, `stop_reason`, `stop_approved_by` (bereits in Lead.java) | LeadProtectionService.canStopClock() | 2.1.5 | `lead_stop_clock_started` |
| §2(8)(e) **Verlängerung auf Antrag** | Workflow mit Begründung und Genehmigung | ⏸️ Endpoints (verschoben auf 2.1.6) | 2.1.6 | `lead_protection_extend_requested` |
| §2(8)(f) Erfolgsprovision | Nicht Teil Modul 02 | - | - | - |
| §2(8)(g) DSGVO-Minimierung | `leads.stage` (0/1/2) - V255 | POST /api/leads mit stage-Parameter | 2.1.5 | `lead_stage_upgraded` |
| §2(8)(h) Datenumfang & Weitergabe | Stage-basierte Validierung in LeadService | Frontend: LeadWizard.vue (3 Stufen) | 2.1.5 | - |
| §2(8)(i) **Löschung/Pseudonymisierung** | Retention-Plan | ⏸️ Nightly Job + Endpoint (2.1.6) | 2.1.6 | `lead_personal_data_pseudonymized` |

### Detaillierte Prozessflows

#### §2(8)(c) - Erinnerung + 10-Tage-Nachfrist ✅ Sprint 2.1.6 Phase 3

**Vertragstext (Handelsvertretervertrag Seite 3):**
> "Liegt 60 Tage kein belegbarer Fortschritt vor, kann Freshfoodz in Textform um ein kurzes Update bitten und eine Nachfrist von 10 Kalendertagen setzen. Erfolgt keine Abhilfe, erlischt der Lead‑Schutz und der Lead wird freigegeben."

**Prozessfluss:**
1. **Tag 53**: Nightly Job prüft `leads.progress_deadline < NOW() + INTERVAL '7 days'`
2. **Tag 53**: Automatische Erinnerung (Email) + `progress_warning_sent_at` gesetzt
3. **Tag 53-63**: 10 Kalendertage Nachfrist (Partner kann reagieren)
4. **Tag 63**: Ohne neue Aktivität → Lead-Schutz erlischt (`protection_expired = TRUE`)
5. **Notification**: Email an Partner (Tag 53), Email an Manager (Tag 63), Dashboard-Alert, Audit-Event

**Sprint 2.1.5 Scope:**
- ✅ `leads.progress_warning_sent_at` Feld (V255)
- ✅ `leads.progress_deadline` Berechnung (V257 Trigger)

**Sprint 2.1.6 Phase 3 Scope (Implementation):**
- ✅ **Job 1:** Progress Warning Check (1:00 Uhr nachts)
- ✅ **Job 2:** Protection Expiry Check (2:00 Uhr nachts)
- ✅ **Email-Integration:** OutboxEmail-Pattern (Modul 05)
- ✅ **Events:** `LeadProgressWarningIssuedEvent`, `LeadProtectionExpiredEvent`

**Business-Regel (Code-Referenz):**
- Service-Klasse: `LeadMaintenanceService.java`
- Business-Regel: "60-Day Activity Rule - Warning 7 days before expiry"
- KEINE Paragraphen-Referenz im Code (Verträge sind individuell)

#### §2(8)(e) - Verlängerung auf Antrag (⏸️ Sprint 2.1.6)

**Antragsweg:**
1. Partner stellt Antrag mit Begründung
2. Admin/Manager erhält Benachrichtigung
3. Genehmigung aktualisiert `protection_months`
4. Audit-Trail Protokollierung

**Sprint 2.1.5 Scope:**
- ✅ Bestehende Felder nutzbar (`protection_months` in Lead.java)
- ⏸️ Workflow-Endpoints (2.1.6)

#### §2(8)(i) - Löschung/Pseudonymisierung ✅ Sprint 2.1.6 Phase 3

**Vertragstext (Handelsvertretervertrag Seite 3):**
> "Nicht weiterverfolgte Vormerkungen werden nach Ablauf der 60‑Tage‑Frist gelöscht oder pseudonymisiert. Mit Erlöschen des Lead‑Schutzes (f) werden personenbezogene Daten, die ausschließlich dem Lead‑Schutz dienten, gelöscht oder pseudonymisiert, soweit keine gesetzlichen Aufbewahrungspflichten oder berechtigten Interessen entgegenstehen."

**Retention-Regeln:**
- **60 Tage nach protection_expired:** Pseudonymisierung personenbezogener Daten
- **Berechtigtes Interesse (Art. 6 Abs. 1 lit. f DSGVO):** Firmendaten für Analytics behalten
- **Gesetzliche Aufbewahrungsfristen:** Handelsbriefe 6 Jahre (HGB), Rechnungen 10 Jahre (AO)

**DSGVO-Konform (B2B-Leads):**
**Pseudonymisieren (PII):**
- `email` → SHA256-Hash (Duplikat-Check bleibt funktional)
- `phone` → NULL
- `contactPerson` → "ANONYMIZED"
- `notes` → NULL

**Behalten (DSGVO-Ausnahme für juristische Personen):**
- `companyName` (Analytics, Territory-Statistiken)
- `city` (Branchenanalyse)
- `businessType` (ROI-Tracking)
- `assignedTo` (Vertriebspartner-Performance)
- `sourceCampaign` (Campaign-Erfolg)

**Sprint 2.1.5 Scope:**
- ✅ Stage-basierte Datenfelder (V255)
- ✅ Felder für Pseudonymisierung vorhanden

**Sprint 2.1.6 Phase 3 Scope (Implementation):**
- ✅ **Job 3:** DSGVO Pseudonymization (3:00 Uhr nachts)
- ✅ **SHA-256 Hash-Funktion:** Email-Hashing für Duplikat-Check
- ✅ **Event:** `LeadsPseudonymizedEvent` (Compliance-Dashboard)
- ✅ **Neues Feld:** `pseudonymized_at` (Timestamp)

**Business-Regel (Code-Referenz):**
- Service-Klasse: `LeadMaintenanceService.java`
- Business-Regel: "B2B Personal Data Pseudonymization (DSGVO Art. 4)"
- KEINE Paragraphen-Referenz im Code (Verträge sind individuell)

## API Contract

### POST /api/leads

**Request:**
```json
{
  "stage": 0,
  "companyName": "Hotel Beispiel",
  "city": "Berlin",
  "industry": "Hospitality",
  "registeredAt": "2024-06-03T10:15:00Z"  // Optional - nur für Admin/Manager
}
```

**Validierung:**
- Optionales Feld `registeredAt` wird **nur** beachtet, wenn Aufrufer Rolle ∈ {`admin`, `manager`} hat. Andernfalls wird es ignoriert.
- Validierung: `registeredAt <= now() + 1min` (kleiner Buffer für Clock-Drift); bei Verstoß → `400`.
- Idempotency: `Idempotency-Key` Header (24h TTL). Wiederholte identische Requests liefern **gleiche** Antwort (aus Store).

**Responses:**

#### 201 Created
```json
{
  "id": "uuid",
  "protection": {
    "status": "protected",
    "until": "2026-03-28T10:00:00Z",
    "nextActivityDue": "2025-11-28T10:00:00Z"
  }
}
```

#### 202 Accepted (Soft Duplicates)
```json
{
  "message": "Potential duplicates found",
  "candidates": [
    {
      "leadId": "uuid",
      "score": 0.85,
      "matchReasons": ["similar_name", "same_city"],
      "companyName": "Hotel Beispiel GmbH"
    }
  ],
  "reviewUrl": "/leads/review?candidates=..."
}
```

#### 409 Conflict (Hard Duplicate)
```json
{
  "type": "https://api.freshfoodz.de/problems/duplicate-lead",
  "title": "Duplicate Lead",
  "status": 409,
  "detail": "Lead with email 'example@hotel.de' already exists",
  "instance": "/leads/uuid-existing"
}
```

### PUT /api/leads/{id}/registered-at

**Rollen:** `admin`, `manager`

**Request:**
```json
{
  "registeredAt": "2024-06-03T10:15:00Z",
  "reason": "Import Altbestand / Erstkontakt auf Messe",
  "evidenceUrl": "https://crm.freshfoodz.de/docs/import-q2-2025.pdf"  // Optional
}
```

**Responses:**

#### 204 No Content
Erfolgreich aktualisiert

#### 400 Bad Request
```json
{
  "type": "https://api.freshfoodz.de/problems/invalid-date",
  "title": "Invalid Date",
  "status": 400,
  "detail": "registeredAt cannot be in the future"
}
```

#### 403 Forbidden
```json
{
  "type": "https://api.freshfoodz.de/problems/insufficient-permissions",
  "title": "Insufficient Permissions",
  "status": 403,
  "detail": "Only managers and admins can backdate lead registration"
}
```

#### 404 Not Found
```json
{
  "type": "https://api.freshfoodz.de/problems/lead-not-found",
  "title": "Lead Not Found",
  "status": 404,
  "detail": "Lead with ID 'uuid' not found"
}
```

**Audit:**
- Event `lead_registered_at_backdated` mit {leadId, oldRegisteredAt, newRegisteredAt, reason, setBy, setAt, evidenceUrl?}

## Audit Events

```json
{
  "event": "lead.protection.created",
  "leadId": "uuid",
  "protectionUntil": "2026-03-28",
  "userId": "creator-uuid",
  "timestamp": "2025-09-28T10:00:00Z"
}

{
  "event": "lead.activity.recorded",
  "leadId": "uuid",
  "activityType": "call",
  "progressDeadlineNew": "2025-11-28",
  "userId": "sales-uuid"
}

{
  "event": "lead.protection.stop_clock",
  "leadId": "uuid",
  "reason": "Customer vacation 2 weeks",
  "pausedUntil": "2025-10-12",
  "approvedBy": "manager-uuid"
}
```

## Feature Flags

```yaml
lead_protection:
  enabled: true
  warning_days: 7

progressive_profiling:
  enabled: true
  require_stage_0: true

fuzzy_matching:
  enabled: true
  threshold: 0.7
  max_candidates: 5
```