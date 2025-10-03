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

### Pre-Claim & Erstkontakt (§2(8)(a))

**⚠️ WICHTIG:** Activity `FIRST_CONTACT_DOCUMENTED` startet Schutz **OHNE** V257-Trigger.

**Ablauf:**
1. User erstellt Lead Stage 0 ohne Kontakt
2. User füllt "Erstkontakt dokumentieren" Block aus
3. LeadService erstellt Activity `FIRST_CONTACT_DOCUMENTED` (countsAsProgress=false)
4. LeadService setzt **explizit**:
   - `lead.registered_at = NOW()`
   - `lead.progress_deadline = NOW() + INTERVAL '60 days'`
   - `lead.protected_until = calculate_protection_until(NOW())`
5. V257 Trigger feuert NICHT (da countsAsProgress=false)

**Rationale:** Erstkontakt ist Schutzbeginn laut Vertrag, aber KEIN "belegbarer Fortschritt" im Sinne der 60-Tage-Regel.

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

#### §2(8)(c) - Erinnerung + 10-Tage-Nachfrist (⏸️ Sprint 2.1.6)

**Prozessfluss:**
1. **Tag 53**: Nightly Job prüft `leads.progress_deadline < NOW() + INTERVAL '7 days'`
2. **Tag 60**: Automatische Erinnerung + `progress_warning_sent_at` gesetzt
3. **Tag 60-70**: 10 Kalendertage Nachfrist
4. **Tag 70**: Ohne neue Aktivität → Lead-Schutz erlischt
5. **Notification**: Email, Dashboard-Alert, Audit-Event

**Sprint 2.1.5 Scope:**
- ✅ `leads.progress_warning_sent_at` Feld (V255)
- ✅ `leads.progress_deadline` Berechnung (V257 Trigger)
- ⏸️ Nightly Job für Warning/Expiry (2.1.6)

#### §2(8)(e) - Verlängerung auf Antrag (⏸️ Sprint 2.1.6)

**Antragsweg:**
1. Partner stellt Antrag mit Begründung
2. Admin/Manager erhält Benachrichtigung
3. Genehmigung aktualisiert `protection_months`
4. Audit-Trail Protokollierung

**Sprint 2.1.5 Scope:**
- ✅ Bestehende Felder nutzbar (`protection_months` in Lead.java)
- ⏸️ Workflow-Endpoints (2.1.6)

#### §2(8)(i) - Löschung/Pseudonymisierung (⏸️ Sprint 2.1.6)

**Retention-Regeln:**
- Vormerkung ohne Bearbeitung (60 Tage): Pseudonymisierung
- Nach Schutz-Ablauf: Löschung gemäß DSGVO
- Ausnahmen: Gesetzliche Aufbewahrungsfristen

**Sprint 2.1.5 Scope:**
- ✅ Stage-basierte Datenfelder (V255)
- ⏸️ Nightly Job für Pseudonymisierung (2.1.6)

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

### Lead-Assignment Metadata (Sprint 2.1.5)

**Enum-Wertebereich für `metadata.method` (verbindlich):**
```typescript
type AssignmentMethod =
  | "GEO_WORKLOAD"      // Automatisch via Geo-Zone + Workload-Verteilung
  | "MANUAL"            // Manager-Override (explizite Zuweisung)
  | "WEB_INTAKE_AUTO";  // Self-Service Web-Intake (Sprint 2.1.6)
```

**Activity-Event `LEAD_ASSIGNED` Struktur:**
```json
{
  "event": "lead.activity.recorded",
  "leadId": "uuid",
  "activityType": "LEAD_ASSIGNED",
  "summary": "Lead automatisch zugewiesen (Geo-Zone München)",
  "metadata": {
    "method": "GEO_WORKLOAD",           // ENUM (siehe oben)
    "previousOwner": "uuid-or-null",    // Falls Re-Assignment, sonst NULL
    "assignmentReason": "Geo-Zone DE-BY-München, Workload 3/10",
    "geoZone": "DE-BY-München",         // ISO-artig (DE-<Bundesland>-<Stadt>)
    "workloadScore": 30                 // 0-100 (aktueller Workload des Empfängers)
  },
  "performedBy": "system-or-manager-uuid",
  "timestamp": "2025-09-28T10:00:00Z"
}
```

**Frontend/Backend Alignment:**
- Backend-Enum: `AssignmentMethod.java` (GEO_WORKLOAD | MANUAL | WEB_INTAKE_AUTO)
- Frontend-Type: `AssignmentMethod` TypeScript (identische Werte)
- API-Validierung: RFC 7807 Problem+JSON bei ungültigem `method`-Wert

### Standard Audit Events

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
  "activityType": "QUALIFIED_CALL",
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