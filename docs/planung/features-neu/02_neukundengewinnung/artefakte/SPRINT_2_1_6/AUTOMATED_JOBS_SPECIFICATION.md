---
module: "02_neukundengewinnung"
domain: "shared"
doc_type: "specification"
status: "ready_for_implementation"
sprint: "2.1.6"
phase: "3"
owner: "team/leads-backend"
created: "2025-10-06"
updated: "2025-10-06"
---

# Sprint 2.1.6 Phase 3 - Automated Jobs Specification

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.6 → Automated Jobs

## 🎯 Executive Summary

**Mission:** Automatisierung vertraglicher Pflichten für Lead-Schutz durch 4 Nightly Jobs

**Problem:** Vertragliche Regeln (60-Day Activity, Protection Expiry, DSGVO) erfordern manuelle Überwachung
**Solution:** Scheduled Jobs mit Email-Benachrichtigungen, Event-Publishing und Audit-Trail

**Timeline:** 2.0 Tage (06.10.2025 - 08.10.2025)
**Impact:** Compliance-Automation, fairere Lead-Verteilung, DSGVO-Konformität

---

## 📋 Architektur-Entscheidungen

### ADR-001: Email-Integration über Outbox-Pattern

**Status:** ✅ Entschieden (2025-10-06)

**Context:**
Jobs müssen Email-Benachrichtigungen versenden (Partner-Warnings, Manager-Alerts)

**Decision:**
Verwende `OutboxEmail` Entity aus Modul 05 (Kommunikation) statt direktem SMTP

**Rationale:**
- ✅ **Transaktionale Sicherheit:** Email wird nur versendet, wenn Job-Transaktion erfolgreich
- ✅ **Retry-Mechanismus:** `EmailOutboxProcessor` hat Exponential-Backoff
- ✅ **Performance:** Jobs schreiben nur DB-Einträge (<10ms), kein SMTP-Timeout
- ✅ **Separation of Concerns:** Jobs machen Business-Logik, Modul 05 macht Email

**Alternatives Considered:**
- ❌ Direktes SMTP in Jobs → Transaktions-Probleme, kein Retry
- ❌ Eigenes Email-System → Code-Duplikation zu Modul 05

**Consequences:**
- Jobs benötigen `OutboxEmailRepository` Dependency
- Email-Template-System muss vorbereitet sein (Modul 05)
- Fallback: Logging statt Email bis Modul 05 bereit

---

### ADR-002: Import Jobs Archivierung statt Löschung

**Status:** ✅ Entschieden (2025-10-06)

**Context:**
`import_jobs` Tabelle hat 7-Tage-TTL (`ttl_expires_at`), Cleanup nötig

**Decision:**
Status='ARCHIVED' statt DELETE, Hard-Delete erst nach 90 Tagen (Sprint 2.1.7)

**Rationale:**
- ✅ **Audit-Trail:** Admin kann Import-Historie nachvollziehen
- ✅ **Compliance:** GoBD-Anforderungen (Nachweispflichten)
- ✅ **Debugging:** Bei Problemen 30 Tage rückblickend analysieren
- ✅ **Stufenweise Löschung:** Archiv → Hard-Delete nach 90 Tagen

**Example:**
```sql
-- Tag 7: Archivierung
UPDATE import_jobs
SET status = 'ARCHIVED', archived_at = NOW()
WHERE ttl_expires_at < NOW() AND status = 'COMPLETED';

-- Tag 97 (später): Hard-Delete
DELETE FROM import_jobs
WHERE status = 'ARCHIVED' AND archived_at < NOW() - INTERVAL '90 days';
```

**Consequences:**
- Neuer Status-Wert in `ImportStatus` Enum: `ARCHIVED`
- Admin-UI muss Archiv-Filter bekommen
- Separater Cleanup-Job in Sprint 2.1.7

---

### ADR-003: B2B-Pseudonymisierung (DSGVO Art. 4)

**Status:** ✅ Entschieden (2025-10-06)

**Context:**
DSGVO-Pseudonymisierung nach 60 Tagen expired protection - was anonymisieren?

**Decision:**
Nur personenbezogene Daten (PII), Firmendaten für Analytics behalten

**Rationale:**
- ✅ **DSGVO Scope:** Schützt natürliche Personen, nicht Unternehmen (Art. 4 Nr. 1)
- ✅ **Analytics:** Firmendaten bleiben nutzbar (Territory-Statistiken, ROI)
- ✅ **Duplikat-Check:** Email-Hash ermöglicht weiterhin Matching
- ✅ **Berechtigtes Interesse:** Vertriebsstatistiken (Art. 6 Abs. 1 lit. f)

**Implementation:**

**Pseudonymisieren:**
| Feld | Aktion | Begründung |
|------|--------|------------|
| `email` | SHA256-Hash | Duplikat-Check bleibt funktional |
| `phone` | NULL | PII, kein Analytics-Nutzen |
| `contactPerson` | "ANONYMIZED" | PII, Name der natürlichen Person |
| `notes` | NULL | Kann PII enthalten (Freitext) |

**Behalten (DSGVO-Ausnahme für juristische Personen):**
| Feld | Behalten | Begründung |
|------|----------|------------|
| `companyName` | ✅ | Keine PII, Analytics-relevant |
| `city` | ✅ | Territory-Statistiken |
| `businessType` | ✅ | Branchenanalyse |
| `assignedTo` | ✅ | Vertriebspartner-Performance |
| `sourceCampaign` | ✅ | Campaign-ROI |

**Example:**
```sql
-- Vorher
{
  "email": "hans.mueller@hotel-mueller.de",
  "phone": "089-12345678",
  "contactPerson": "Hans Müller",
  "notes": "Interessiert an Frühstückssortiment",
  "companyName": "Hotel Müller GmbH",
  "city": "München"
}

-- Nachher
{
  "email": "sha256:a3f8b2c4d1e9f7a6b5c8d2e3f4a7b9c1",
  "phone": null,
  "contactPerson": "ANONYMIZED",
  "notes": null,
  "companyName": "Hotel Müller GmbH",
  "city": "München",
  "pseudonymizedAt": "2025-12-15T03:00:00Z"
}
```

**Consequences:**
- Neues Feld `pseudonymized_at` in Lead Entity
- SHA-256 Hash-Funktion in LeadService
- Analytics-Queries funktionieren weiterhin

---

### ADR-004: Event-Publishing für Dashboard-Updates

**Status:** ✅ Entschieden (2025-10-06)

**Context:**
Dashboard soll Echtzeit-Updates bei Job-Completion (kein Polling)

**Decision:**
CDI Events (`jakarta.enterprise.event.Event`) nach jedem Job

**Rationale:**
- ✅ **Referenz:** `FollowUpAutomationService.java` macht es bereits
- ✅ **Echtzeit-UX:** Dashboard reagiert ohne F5-Refresh
- ✅ **Entkopplung:** Jobs wissen nichts vom Dashboard
- ✅ **Multi-Subscriber:** Email-Worker + Dashboard + Audit hören zu

**Events:**
```java
// Job 1: Progress Warning
public class LeadProgressWarningIssuedEvent {
  public final Long leadId;
  public final String companyName;
  public final String assignedTo;
  public final LocalDateTime progressDeadline;
}

// Job 2: Protection Expiry
public class LeadProtectionExpiredEvent {
  public final Long leadId;
  public final String companyName;
  public final LocalDateTime expiredAt;
}

// Job 3: Pseudonymisierung
public class LeadsPseudonymizedEvent {
  public final int leadCount;
  public final LocalDateTime processedAt;
}

// Job 4: Import Cleanup
public class ImportJobsArchivedEvent {
  public final int jobCount;
  public final LocalDateTime archivedAt;
}
```

**Usage:**
```java
@Inject Event<LeadProgressWarningIssuedEvent> warningEvent;

public void checkProgressWarnings() {
  for (Lead lead : leads) {
    // ... update lead ...
    warningEvent.fire(new LeadProgressWarningIssuedEvent(lead));
  }
}
```

**Consequences:**
- Event-Classes in `de.freshplan.modules.leads.events` Package
- Dashboard benötigt WebSocket oder SSE für Browser-Push
- Tests müssen Event-Publishing mocken

---

### ADR-005: Hybrid-Test-Strategie (Option C)

**Status:** ✅ Entschieden (2025-10-06)

**Context:**
CI-Performance vs. Code-Coverage - Scheduler-Tests sind langsam

**Decision:**
80% Mock-Tests (Service-Layer) + 20% @QuarkusTest (Integration)

**Rationale:**
- ✅ **Service-Tests:** Schnell (<1s), Business-Logik isoliert, CI-freundlich
- ✅ **Integration-Tests:** Realistisch (echte DB), Scheduler-Validierung
- ✅ **CI bleibt performant:** Bulk der Tests läuft ohne Docker
- ✅ **Coverage ≥85%:** Service-Tests decken Business-Logik ab

**Architecture:**
```java
// Service-Layer (ohne @Scheduled) - 80% Tests hier
@ApplicationScoped
public class LeadMaintenanceService {
  public ProgressWarningResult checkProgressWarnings() {
    // Business-Logik
  }
}

// Scheduler-Layer (mit @Scheduled) - 20% Tests hier
@ApplicationScoped
public class LeadMaintenanceScheduler {
  @Inject LeadMaintenanceService service;

  @Scheduled(cron = "0 0 1 * * ?")
  @Transactional
  public void scheduledProgressWarningCheck() {
    service.checkProgressWarnings(); // Delegiert
  }
}
```

**Test Split:**
```java
// 80% - Mock-Tests (schnell)
@ExtendWith(MockitoExtension.class)
class LeadMaintenanceServiceTest {
  @Mock LeadRepository repo;
  @InjectMocks LeadMaintenanceService service;

  @Test void shouldWarnLeadAfter53Days() { ... }
  @Test void shouldSkipLeadsWithRecentActivity() { ... }
  // ... 15 weitere Szenarien ...
}

// 20% - Integration-Tests (komplett)
@QuarkusTest
class LeadMaintenanceSchedulerIT {
  @Inject LeadMaintenanceScheduler scheduler;

  @Test void shouldProcessRealLeadFromDatabase() { ... }
}
```

**Consequences:**
- Separate Service-Klassen (Testbarkeit)
- CI-Zeit: +2min statt +10min
- Coverage: 85%+ statt 60%

---

### ADR-006: Keine Vertragsreferenzen im Code

**Status:** ✅ Entschieden (2025-10-06)

**Context:**
Verträge können sich pro Partner unterscheiden (Paragraphen verschieben)

**Decision:**
Generische Business-Begriffe, keine §-Referenzen im Code

**Rationale:**
- ✅ **Verträge sind individuell:** Paragraphen-Nummerierung nicht einheitlich
- ✅ **Wartbarkeit:** Bei Vertragsänderung nur Docs anpassen, nicht Code
- ✅ **Verständlichkeit:** Business-Begriffe sind selbsterklärend
- ✅ **Separation:** Contract-Mapping in separaten Docs

**Examples:**
```java
// ❌ SO NICHT (hart codierte Vertragsreferenz)
/**
 * §2(8)c Erinnerung mit Nachfrist
 * 60 Tage kein Fortschritt → Erinnerung + 10 Tage
 */
public void checkProgressWarnings() { }

// ✅ SO BESSER (generische Business-Regel)
/**
 * Progress Warning Check (60-Day Activity Rule)
 *
 * Sends reminder 7 days before protection expires if no meaningful activity.
 * Contract Mapping: See docs/CONTRACT_MAPPING.md
 */
public void checkProgressWarnings() { }
```

**Consequences:**
- Contract-Mapping in `CONTRACT_MAPPING.md` pflegen
- Code bleibt stabil bei Vertragsänderungen
- Neue Partner mit anderen Verträgen = kein Code-Change

---

## 🚀 Implementation Plan

### Job 1: Progress Warning (60-Day Activity Rule)

**Business-Ziel:** Partner 7 Tage vor Ablauf warnen

**Trigger:** Täglich 1:00 Uhr nachts

**Query:**
```sql
SELECT * FROM leads
WHERE progress_deadline < NOW() + INTERVAL '7 days'
  AND progress_warning_sent_at IS NULL
  AND stage != 2 -- QUALIFIZIERT (konvertiert)
ORDER BY progress_deadline ASC
LIMIT 100; -- Batch-Processing
```

**Actions:**
1. Update: `progress_warning_sent_at = NOW()`
2. Create: `OutboxEmail` entry
3. Publish: `LeadProgressWarningIssuedEvent`
4. Log: `LeadMaintenanceJob: Issued ${count} warnings`

**Email-Template:**
```
TO: ${lead.assignedTo.email}
SUBJECT: Erinnerung: Lead "${lead.companyName}" - Aktivität erforderlich

Hallo ${lead.assignedTo.firstName},

Dein Lead "${lead.companyName}" (${lead.city}) läuft in 7 Tagen ab.

Deadline: ${lead.progressDeadline}
Letzter Kontakt: ${lead.lastActivityAt}

Bitte dokumentiere eine Aktivität oder beantrage eine Verlängerung.

Link: ${baseUrl}/leads/${lead.id}
```

**Tests:**
- ✅ Warning bei Lead 53 Tage alt
- ✅ Keine Warning bei Lead mit recent activity
- ✅ Keine Warning bei bereits gesendeter Warning
- ✅ Keine Warning bei converted Lead
- ✅ Event wird published

---

### Job 2: Protection Expiry (60-Day Deadline)

**Business-Ziel:** Lead-Schutz nach Ablauf automatisch beenden

**Trigger:** Täglich 2:00 Uhr nachts

**Query:**
```sql
SELECT * FROM leads
WHERE progress_deadline < NOW()
  AND protection_expired = FALSE
  AND stage != 2 -- QUALIFIZIERT
ORDER BY progress_deadline ASC
LIMIT 100;
```

**Actions:**
1. Update: `protection_expired = TRUE`
2. Create: `OutboxEmail` entry (Manager-Benachrichtigung)
3. Publish: `LeadProtectionExpiredEvent`
4. Log: `LeadMaintenanceJob: Expired ${count} protections`

**Email-Template (Manager):**
```
TO: manager@freshfoodz.de
SUBJECT: Lead-Schutz abgelaufen: "${lead.companyName}"

Lead-Schutz ist abgelaufen:

Lead: ${lead.companyName} (${lead.city})
Partner: ${lead.assignedTo.name}
Registriert: ${lead.registeredAt}
Deadline: ${lead.progressDeadline}

Lead ist jetzt wieder freigegeben für andere Partner.

Dashboard: ${baseUrl}/admin/expired-leads
```

**Tests:**
- ✅ Expiry bei Lead nach Deadline
- ✅ Keine Expiry bei Lead mit Stop-the-Clock
- ✅ Keine Expiry bei converted Lead
- ✅ Event wird published
- ✅ Manager-Email wird erstellt

---

### Job 3: DSGVO Pseudonymisierung (B2B Personal Data)

**Business-Ziel:** Personenbezogene Daten nach 60 Tagen pseudonymisieren

**Trigger:** Täglich 3:00 Uhr nachts

**Query:**
```sql
SELECT * FROM leads
WHERE protection_until < NOW() - INTERVAL '60 days'
  AND pseudonymized_at IS NULL
  AND stage != 2 -- QUALIFIZIERT
ORDER BY protection_until ASC
LIMIT 100;
```

**Actions:**
1. Pseudonymize:
   - `email` → SHA256-Hash
   - `phone` → NULL
   - `contactPerson` → "ANONYMIZED"
   - `notes` → NULL
2. Update: `pseudonymized_at = NOW()`
3. Publish: `LeadsPseudonymizedEvent`
4. Log: `LeadMaintenanceJob: Pseudonymized ${count} leads`

**Implementation:**
```java
private void pseudonymizeLead(Lead lead) {
  // Hash email for duplicate-check retention
  if (lead.email != null) {
    lead.email = "sha256:" + sha256Hash(lead.email);
  }

  // Clear PII
  lead.phone = null;
  lead.contactPerson = "ANONYMIZED";
  lead.notes = null;

  // Keep company data (DSGVO exemption)
  // companyName, city, businessType, assignedTo, sourceCampaign

  lead.pseudonymizedAt = LocalDateTime.now();
}

private String sha256Hash(String input) {
  MessageDigest digest = MessageDigest.getInstance("SHA-256");
  byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
  return Base64.getEncoder().encodeToString(hash);
}
```

**Tests:**
- ✅ Pseudonymisierung nach 60 Tagen expired
- ✅ Email wird gehasht (nicht NULL)
- ✅ Firmendaten bleiben erhalten
- ✅ Keine Pseudonymisierung bei converted Lead
- ✅ Event wird published

---

### Job 4: Import Jobs Archival (7-Day TTL)

**Business-Ziel:** Audit-Trail für Import-Jobs aufbewahren

**Trigger:** Täglich 4:00 Uhr nachts

**Query:**
```sql
SELECT * FROM import_jobs
WHERE ttl_expires_at < NOW()
  AND status = 'COMPLETED'
ORDER BY created_at ASC
LIMIT 100;
```

**Actions:**
1. Update: `status = 'ARCHIVED', archived_at = NOW()`
2. Publish: `ImportJobsArchivedEvent`
3. Log: `LeadMaintenanceJob: Archived ${count} import jobs`

**Tests:**
- ✅ Archivierung nach 7 Tagen TTL
- ✅ Keine Archivierung bei FAILED jobs
- ✅ Keine Archivierung bei noch laufenden jobs
- ✅ Event wird published

---

## 📊 Quality Gates

### Code Coverage
- ✅ **Service-Tests:** ≥90% Coverage
- ✅ **Integration-Tests:** ≥80% Coverage
- ✅ **Gesamt:** ≥85% Coverage

### Performance
- ✅ **Job-Laufzeit:** <30 Sekunden (100 Leads/Batch)
- ✅ **DB-Queries:** P95 <100ms
- ✅ **Event-Publishing:** <10ms

### Security
- ✅ **Keine Secrets im Code:** Email-Templates in Config
- ✅ **SQL-Injection-Safe:** Prepared Statements
- ✅ **Audit-Trail:** Alle Actions geloggt

### Monitoring
- ✅ **Prometheus-Metrics:**
  - `lead_maintenance_warnings_issued_total`
  - `lead_maintenance_protections_expired_total`
  - `lead_maintenance_pseudonymizations_total`
  - `lead_maintenance_import_jobs_archived_total`
- ✅ **Alerting:** Slack-Notification bei >10 Failures

---

## 🔗 Related Documents

- **Contract Mapping:** [CONTRACT_MAPPING.md](../SPRINT_2_1_5/CONTRACT_MAPPING.md)
- **Trigger:** [TRIGGER_SPRINT_2_1_6.md](../../../../TRIGGER_SPRINT_2_1_6.md)
- **Business Logic:** [BUSINESS_LOGIC_LEAD_ERFASSUNG.md](../SPRINT_2_1_5/BUSINESS_LOGIC_LEAD_ERFASSUNG.md)
- **Event System Pattern:** [EVENT_SYSTEM_PATTERN.md](../EVENT_SYSTEM_PATTERN.md)
- **Security Test Pattern:** [SECURITY_TEST_PATTERN.md](../SECURITY_TEST_PATTERN.md)

---

**Status:** ✅ Ready for Implementation
**Reviewer:** @joergstreeck
**Next Step:** Start Production mit `LeadMaintenanceService.java`
