---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "contract"
status: "draft"
sprint: "2.1.5"
owner: "team/leads-backend"
updated: "2025-09-28"
---

# Sprint 2.1.5 – Contract Mapping

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Contract Mapping

## Vertrag → Implementation Mapping

### Lead-Schutz (§3.2 Handelsvertretervertrag)

**Vertragstext:**
> "Der Lead-Schutz beträgt 6 Monate ab Registrierung (Firma, Ort und zentraler Kontakt oder dokumentierter Erstkontakt)."

**Implementation:**
```sql
-- lead_protection.protection_until
protection_until = registered_at + INTERVAL '6 months'

-- Trigger bei Lead-Erstellung
CREATE TRIGGER create_lead_protection_on_insert
AFTER INSERT ON leads
FOR EACH ROW EXECUTE FUNCTION create_lead_protection_trigger();
```

### Aktivitätsstandard (§3.3)

**Vertragstext:**
> "Aktivitätsstandard: belegbarer Fortschritt je 60 Tage"

**Implementation:**
```sql
-- lead_protection.progress_deadline
progress_deadline = last_progress_at + INTERVAL '60 days'

-- Status-Übergang
UPDATE lead_protection
SET status = 'warning'
WHERE progress_deadline < NOW() + INTERVAL '7 days'
```

### Stop-the-Clock (§3.3.2)

**Vertragstext:**
> "Stop-the-Clock bei FreshFoodz-Zuarbeit/Sperrfristen"

**Implementation:**
```sql
-- lead_protection Felder
stop_the_clock_reason TEXT  -- Pflichtfeld mit Grund
stop_the_clock_start TIMESTAMP
stop_the_clock_end TIMESTAMP

-- Business Logic
IF stop_the_clock_start IS NOT NULL
  AND stop_the_clock_end IS NULL THEN
  -- Deadline pausiert
END IF
```

### Datenminimierung (§5.1 DSGVO-Annex)

**Vertragstext:**
> "Vormerkung: nur Firma/Ort (optional Branche)"

**Implementation:**
```typescript
// Stage 0 - Vormerkung
interface LeadStage0 {
  companyName: string;  // PFLICHT
  city: string;         // PFLICHT
  industry?: string;    // OPTIONAL
  // KEINE personenbezogenen Daten!
}

// Stage 1 - Mit Kontakt
interface LeadStage1 extends LeadStage0 {
  contact?: {
    firstName?: string;
    lastName?: string;
    email?: string;
    phone?: string;
  };
  source: 'manual' | 'partner' | 'marketing';
  ownerUserId?: string;
}
```

## § 2(8) Abdeckung - Vollständige vertragliche Anforderungen

| Klausel | Implementierung | Artefakt/Endpoint | Job/Timer | Audit-Event |
|---------|----------------|-------------------|-----------|-------------|
| §2(8)(a) Lead-Schutz 6 Monate | `protection_until = registered_at + INTERVAL '6 months'` | Trigger `create_lead_protection_on_insert` | - | `lead_protection_started` |
| §2(8)(a) **Lead-Registrierung & Schutzbeginn** | **Backdating durch Admin/Manager** auf dokumentierten Erstkontakt; 6-Monate Schutz ab `registered_at` | **PUT /api/leads/{id}/registered-at** (Body: `registeredAt`, `reason`) | - | `lead_registered_at_backdated` |
| §2(8)(b) 60-Tage-Aktivitätsstandard | `progress_deadline = last_progress_at + INTERVAL '60 days'` | Status-Check Job | Nightly Job | `lead_activity_logged` |
| §2(8)(c) **Erinnerung + 10 Tage Nachfrist** | Reminder-Flow mit Statuswechsel zu `warning` → nach 10 Tagen zu `expired` | **POST /lead-protection/{id}/reminder** | Nightly Job + 10d Timer | `lead_protection_warning` → `lead_protection_expired` |
| §2(8)(d) Stop-the-Clock | `stop_the_clock_start/end` mit Grund-Dokumentation | POST/DELETE /lead-protection/{id}/stop-clock | - | `lead_stop_clock_started/stopped` |
| §2(8)(e) **Verlängerung auf Antrag** | Antrags- und Genehmigungsprozess mit Begründungspflicht | **POST /lead-protection/{id}/extend** + **PUT /lead-protection/extend/{requestId}** | Scheduler aktualisiert Schutzende | `lead_protection_extend_requested/approved/denied` |
| §2(8)(f) Erfolgsprovision | Nicht Teil von Sprint 2.1.5 | - | - | - |
| §2(8)(g) DSGVO-Minimierung | Progressive Profiling (Stage 0/1/2) implementiert | POST /api/leads mit stage-Parameter | - | `lead_stage_upgraded` |
| §2(8)(h) Datenumfang & Weitergabe | Stufen-basierte Datenfelder definiert | Siehe Progressive Profiling unten | - | - |
| §2(8)(i) **Löschung/Pseudonymisierung** | Retention-Plan + Pseudonymisierung nach 60 Tagen ohne Progress - [Details siehe Data-Retention-Plan](/docs/compliance/data-retention-leads.md) | **DELETE /lead-protection/{id}/personal-data** | Nightly Job + Report | `lead_personal_data_pseudonymized` |

### Detaillierte Prozessflows

#### §2(8)(c) - Erinnerung + 10-Tage-Nachfrist

**Prozessfluss:**
1. **Tag 53**: System prüft `progress_deadline < NOW() + INTERVAL '7 days'`
2. **Tag 60**: Automatische Erinnerung an Partner + Status = `warning`
3. **Tag 60-70**: 10 Kalendertage Nachfrist läuft
4. **Tag 70**: Ohne neue Aktivität → Status = `expired`, Lead-Schutz erlischt
5. **Notification**: Email an Partner, Dashboard-Alert, Audit-Event

**UI/System-Events:**
- Dashboard zeigt Warning-Badge ab Tag 53
- Email-Reminder an Tag 60
- Countdown-Timer in UI während Nachfrist
- Auto-Expiry nach 10 Tagen ohne Aktion

#### §2(8)(e) - Verlängerung auf Antrag

**Antragsweg:**
1. Partner stellt Antrag über UI/API mit Begründung
2. Admin/Manager erhält Benachrichtigung
3. Entscheidung innerhalb 48h (SLA)
4. Bei Genehmigung: `protection_until` wird aktualisiert
5. Protokollierung in Audit-Trail

**Berechtigungen:**
- Antragsteller: `sales` Role
- Genehmiger: `manager` oder `admin` Role
- Automatische Eskalation nach 48h an `admin`

#### §2(8)(i) - Löschung/Pseudonymisierung

**Retention-Regeln:**
- **Vormerkung ohne weitere Bearbeitung (60 Tage)**: Pseudonymisierung aller personenbezogenen Daten
- **Nach Schutz-Ablauf**: Löschung/Pseudonymisierung gemäß Datenschutzrichtlinie
- **Ausnahmen**: Gesetzliche Aufbewahrungsfristen, berechtigte Interessen

**Technische Umsetzung:**
```sql
-- Pseudonymisierung
UPDATE leads SET
  contact_first_name = 'DELETED',
  contact_last_name = 'DELETED',
  contact_email = NULL,
  contact_phone = NULL,
  notes = 'Pseudonymisiert gem. §2(8)(i)',
  pseudonymized_at = NOW()
WHERE protection_status = 'expired'
  AND last_activity_at < NOW() - INTERVAL '60 days';
```

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