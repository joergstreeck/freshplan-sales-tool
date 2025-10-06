---
module: "02_neukundengewinnung"
sprint: "2.1.5"
domain: "business-logic"
doc_type: "konzept"
status: "approved"
owner: "team/product"
updated: "2025-10-04"
---

# Business-Logik: Lead-Erfassung (Progressive Profiling)

**📍 Navigation:** Home → Planung → 02 Neukundengewinnung → Artefakte → Sprint 2.1.5 → Business Logic

> **🎯 Zweck:**
> Zentrale Übersicht der Business-Logik für Lead-Erfassung im B2B-Kontext.
> Verweist auf detaillierte Artefakte, vermeidet Redundanz.

---

## 0. Kern-Prinzipien (Quick-Start)

**Lead-Stage vs. Lead-Schutz vs. Pre-Claim:**

| Konzept | Bedeutung | Trigger |
|---------|-----------|---------|
| **Lead-Stage** (0→1→2) | Inhaltsreife: Vormerkung → Registrierung → Qualifizierung | Daten-Vollständigkeit |
| **Lead-Schutz** (6M) | Exklusives Bearbeitungsrecht (6 Monate ab `registeredAt`) | **NUR** Erstkontakt-Dokumentation |
| **Pre-Claim** (10T) | Vormerkung ohne Schutz, 10-Tage-Frist | Kein Erstkontakt dokumentiert |

**⚠️ Wichtig:** Stage 1 (Registrierung) bedeutet **NICHT** automatisch Schutz! Ein Lead kann Stage 1 haben (Kontaktdaten erfasst), aber im **Pre-Claim** bleiben (kein Erstkontakt dokumentiert). Schutz startet **ausschließlich** durch **Erstkontakt-Dokumentation** (Activity-Type: FIRST_CONTACT_DOCUMENTED).

---

## 1. Progressive Profiling (3-Stufen-Modell)

### Grundprinzip: Jede Karte separat speicherbar

**Best Practice:** User kann nach jeder Karte speichern und Dialog schließen.
- **KEIN Zwang**, alle Karten durchzugehen
- **Niedrige Hürde** für schnelle Vormerkung (Messe/Telefon)
- **Flexible Datenentwicklung** über Zeit

### Karte 0: Vormerkung (stage=0)

**Zweck:** Lead mit minimalen Infos anlegen

**Pflichtfelder:**
- Firmenname (min. 2 Zeichen)
- **Quelle (LeadSource):** MESSE, EMPFEHLUNG, TELEFON, WEB_FORMULAR, PARTNER, SONSTIGE
  - **Definition:** Herkunft des Leads (Pflicht in Karte 0)
  - **Zulässige Werte:**
    - MESSE: Lead von Messe/Event
    - EMPFEHLUNG: Lead durch Referral
    - TELEFON: Lead durch Telefonakquise
    - WEB_FORMULAR: Lead durch Web-Selbstregistrierung
    - PARTNER: Lead durch Partnervertrieb
    - SONSTIGE: Andere Quellen

**Optionale Felder:**
- Stadt, PLZ, Branche
- **Notizen/Quelle:** Freies Textfeld für Kontext (z.B. "Empfehlung von Herrn Schulz", "Partner-Liste Nr. 47") - **KEIN Einfluss auf Schutz**
- **Erstkontakt-Dokumentation:** Kanal, Datum, Gesprächsnotiz (≥10 Zeichen) - **Aktiviert Schutz**

**Zwei-Felder-Lösung (Design-Entscheidung 2025-10-04):**
- **Feld 1: Notizen/Quelle** (immer sichtbar, optional) → Für Hintergrund-Infos, kein Schutz-Einfluss
- **Feld 2: Erstkontakt-Dokumentation** (conditional) → Strukturiert, aktiviert `registered_at`

**Erstkontakt-Logik (Option A - Finale Regelung 2025-10-04):**
- **PFLICHT bei MESSE/TELEFON:** Partner HAT direkten Erstkontakt → Erstkontakt-Block immer sichtbar, muss dokumentiert werden
- **OPTIONAL bei EMPFEHLUNG/WEB_FORMULAR/PARTNER/SONSTIGE:**
  - Checkbox: "☑ Ich hatte bereits Erstkontakt (für sofortigen Lead-Schutz)"
  - Nur wenn aktiviert → Erstkontakt-Block erscheint
  - Partner kann unbedenklich Notizen-Feld nutzen (keine Schutz-Aktivierung)
- **WENN Erstkontakt dokumentiert →** Schutz startet (registered_at = now, protection_until = +6 Monate)
- **WENN NICHT →** Pre-Claim (registered_at = NULL, 10-Tage-Frist)
- **Nachträglich möglich:** Quick-Action in Liste oder Lead bearbeiten → Erstkontakt hinzufügen

**Button:** `[Vormerkung speichern]` → POST /api/leads mit stage=0, schließt Dialog

📖 **Details:** [FRONTEND_DELTA.md Section 0](./FRONTEND_DELTA.md#0-progressive-profiling-ux-regeln-best-practice)

---

### Karte 1: Registrierung (stage=1)

**Zweck:** Kontaktdaten erfassen (Lead erreichbar machen)

**Pflichtfelder:**
- **Mind. ein Kontaktkanal:** E-Mail ODER Telefon

**Optionale Felder:**
- Vorname, Nachname

**DSGVO-Logik:**
- **Vertriebs-Wizard (Sprint 2.1.5 ✅):** KEINE Consent-Checkbox (Kunde sitzt nicht daneben)
  - Stattdessen: **Hinweis "Berechtigtes Interesse (Art. 6 Abs. 1 lit. f DSGVO)"**
  - Link zu Gesetzestext (Popup/Neues Fenster)
  - **Rechtsgrundlage:** B2B-Geschäftsanbahnung (lit. f)
- **Web-Formular (Sprint 2.1.6 geplant):** Consent-Checkbox PFLICHT (Kunde gibt selbst Daten ein)
  - **Rechtsgrundlage:** Einwilligung (Art. 6 Abs. 1 lit. a)

**Schutz:** Startet **NICHT** automatisch bei Kontaktdaten (nur bei Erstkontakt-Dokumentation)

**UX-Hinweis:** Wenn Kontaktdaten erfasst wurden, aber **kein Erstkontakt** vorliegt, bleibt der Lead im **Pre-Claim**. Die UI weist auf das **Nachtragen des Erstkontakts** hin, um den Schutz zu starten.

**Button:** `[Registrierung speichern]` → POST /api/leads mit stage=1, schließt Dialog

📖 **Details:** [DSGVO_CONSENT_SPECIFICATION.md](./DSGVO_CONSENT_SPECIFICATION.md)

---

### Karte 2: Qualifizierung (stage=2)

**Zweck:** Geschäftliche Details nachtragen

**Alle Felder optional:**
- Geschätztes Volumen, Küchengröße, Mitarbeiterzahl, Website, Branche (Details)

**Button:** `[Qualifizierung speichern]` → POST /api/leads mit stage=2, schließt Dialog

📖 **Details:** [FRONTEND_DELTA.md Section 0](./FRONTEND_DELTA.md#karte-2-qualifizierung)

---

## 2. Pre-Claim Mechanik

**Definition:** Vormerkung ohne aktiven Schutz

**Bedingungen:**
- `registered_at IS NULL` (kein Erstkontakt dokumentiert)
- 10-Tage-Frist ab `created_at`
- **KEINE Zuordnung** zum Partner (kann von anderen übernommen werden)

**UI:**
- Badge: "⏳ Vormerkung läuft ab in X Tagen"
- Filter: "Pre-Claim Leads" (eigener Filter in LeadList)
- Warn-Farbe bei < 3 Tagen

**Übergang zu Schutz:**
- Erstkontakt nachträglich dokumentieren → Pre-Claim endet, Schutz startet
- **Backdating:** Erst Sprint 2.1.6 (außer Altdaten-Migration)

📖 **Details:** [PRE_CLAIM_LOGIC.md](./PRE_CLAIM_LOGIC.md)

---

## 3. Lead-Schutz (6 Monate + 60-Tage-Regel)

**Schutz-Start:**
- **NUR** bei Erstkontakt-Dokumentation (FIRST_CONTACT_DOCUMENTED Activity)
- **NICHT** bei Kontaktdaten-Erfassung
- **NICHT** bei Vormerkung ohne Erstkontakt

**Schutz-Dauer:**
- **6 Monate** ab registered_at (Basis-Schutz, Vertrag §3.2)
- **60-Tage-Progress-Regel:** Timer resettet bei Progress-Aktivität (Vertrag §3.3)

**Erinnerung & Nachfrist (Sprint 2.1.6, Vertrag §3.3.2):**
- **Tag 60:** Keine Progress-Aktivität → Automatische Erinnerung an Partner
- **10 Tage Nachfrist:** Tag 60-70 für Abhilfe
- **Tag 70:** Schutz erlischt ohne neue Progress-Aktivität
- **Feld:** `progress_warning_sent_at` (bereits in V255 vorhanden)

**Progress-Aktivitäten (countsAsProgress = TRUE):**
- QUALIFIED_CALL
- MEETING
- DEMO
- ROI_PRESENTATION
- SAMPLE_SENT

**Non-Progress-Aktivitäten (countsAsProgress = FALSE):**
- NOTE, FOLLOW_UP, EMAIL, CALL, SAMPLE_FEEDBACK
- FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, LEAD_ASSIGNED (System)

📖 **Details:** [ACTIVITY_TYPES_PROGRESS_MAPPING.md](./ACTIVITY_TYPES_PROGRESS_MAPPING.md)

---

## 4. Dedupe-Logik (Hard vs. Soft Collisions)

**HTTP 409 Conflict** mit RFC 7807 Problem+JSON

### Hard Collision (Exact Match)
- **Trigger:** companyName + city + postalCode identisch (normalisiert)
- **Response:** Problem+JSON **OHNE** `extensions.severity`
- **Override:** Nur Manager/Admin mit `overrideReason` (min. 10 Zeichen)
- **Berechtigung:** Ein Hard-Override darf ausschließlich von Nutzer*innen mit **Sales-Manager-Rolle (ROLE_SALES_MANAGER)** oder **Admin-Rolle** vorgenommen werden
- **UI:** DuplicateLeadDialog (Manager-only)

### Soft Collision (Similar Match)
- **Trigger:** companyName ähnlich (Levenshtein ≤ 2) ODER email identisch
- **Response:** Problem+JSON **MIT** `extensions.severity: "WARNING"`
- **Override:** Alle Rollen mit `reason` (min. 10 Zeichen)
- **UI:** SimilarLeadDialog (alle Rollen)

**Problem+JSON Structure:**
```json
{
  "type": "https://api.freshfoodz.com/problems/duplicate-lead",
  "title": "Duplicate Lead",
  "status": 409,
  "detail": "Lead mit ähnlichen Daten existiert bereits.",
  "extensions": {
    "severity": "WARNING",  // NUR bei Soft Collision
    "duplicates": [
      {
        "leadId": "uuid",
        "companyName": "Pizza Roma GmbH",
        "city": "München",
        "postalCode": "80331",
        "ownerUserId": "uuid"
      }
    ]
  }
}
```

📖 **Details:** [DEDUPE_POLICY.md](./DEDUPE_POLICY.md)

---

## 5. DSGVO Compliance (Berechtigtes Interesse vs. Einwilligung)

### Vertriebs-Wizard (Sprint 2.1.5)
- **KEINE Consent-Checkbox** (Kunde sitzt nicht daneben)
- **Rechtsgrundlage:** Art. 6 Abs. 1 lit. f DSGVO (Berechtigtes Interesse)
- **UI:** Hinweisbox mit Link zu Gesetzestext
- **Verarbeitung:** B2B-Geschäftsanbahnung

### Web-Formular (Sprint 2.1.6)
- **Consent-Checkbox PFLICHT** (Kunde gibt selbst Daten ein)
- **Rechtsgrundlage:** Art. 6 Abs. 1 lit. a DSGVO (Einwilligung)
- **Backend-Feld:** `consent_given_at` (Migration-Nummer: siehe `get-next-migration.sh`)
- **Widerruf:** Jederzeit möglich

**Quellenabhängige Logik:**
```typescript
if (source === 'WEB_FORMULAR') {
  // Einwilligung erforderlich
  consentRequired = true;
} else {
  // Berechtigtes Interesse (Art. 6 Abs. 1 lit. f)
  consentRequired = false;
}
```

📖 **Details:** [DSGVO_CONSENT_SPECIFICATION.md](./DSGVO_CONSENT_SPECIFICATION.md)

---

## 6. Erstkontakt-Dokumentation

**Zweck:** Initialen Kontakt festhalten (startet Schutz)

**Activity-Type:** FIRST_CONTACT_DOCUMENTED
- `countsAsProgress: false` (System Activity)
- **Schutz** startet beim Erstkontakt
- **Progress-Timer (60 Tage)** wird **nur** durch Activities mit `countsAsProgress=true` zurückgesetzt (Erstkontakt selbst **kein** Progress-Reset)

**Felder:**
- **Kanal:** MESSE, PHONE, EMAIL, REFERRAL, OTHER (Dropdown, Pflicht)
- **Zeitpunkt:** datetime-local (ISO 8601, darf nicht in der Zukunft liegen)
- **Notizen:** min. 10 Zeichen (Textarea, Pflicht)

**Mindestanforderungen:**
Der Erstkontakt gilt als dokumentiert, wenn **Kanal** gesetzt ist, **Zeitpunkt** nicht in der Zukunft liegt und die **Notiz mindestens 10 Zeichen** umfasst.

**Transformation (Frontend → Backend):**
```typescript
activities: [{
  activityType: 'FIRST_CONTACT_DOCUMENTED',
  performedAt: formData.firstContact.performedAt,
  summary: `${channel}: ${notes}`,
  countsAsProgress: false,
  metadata: {
    channel: formData.firstContact.channel,
    notes: formData.firstContact.notes
  }
}]
```

**Nachträglich:**
- Lead öffnen → Activity hinzufügen (Type: FIRST_CONTACT_DOCUMENTED)
- **Zeitpunkt:** Aktuell (Backdating erst Sprint 2.1.6)

📖 **Details:** [FRONTEND_DELTA.md Section 3](./FRONTEND_DELTA.md#3-erstkontakt-block-ui-transformation)

---

## 7. Stop-the-Clock (Sprint 2.1.6, Vertrag §3.3.2)

**Zweck:** Schutzfristen pausieren bei FreshFoodz-Verzögerungen oder Kundensperrfristen

**Bedingungen:**
- **Zwingend:** FreshFoodz-Zuarbeit (Preisfreigaben, Muster, Unterlagen)
- **Optional:** Kundenseitige Sperrfristen (z.B. Urlaub, Budgetfreeze)

**Berechtigung:** Manager/Admin only (ROLE_SALES_MANAGER oder Admin)

**Felder (bereits in V255 vorhanden):**
- `clock_stopped_at` - Zeitpunkt der Pausierung
- `stop_reason` - Begründung (Pflicht, min. 10 Zeichen)
- `stop_approved_by` - Genehmiger (Manager/Admin)

**Effekt:**
- Schutzfristen ruhen während Stop-the-Clock
- `protection_until` verlängert sich entsprechend
- Audit-Log: `lead_stop_clock_started` / `lead_stop_clock_resumed`

**UI:** StopTheClockDialog (Sprint 2.1.6, Manager-only)

📖 **Details:** [CONTRACT_MAPPING.md](./CONTRACT_MAPPING.md#stop-the-clock-§332)

---

## 8. Anti-Patterns (was NICHT tun)

1. ❌ **Consent-Checkbox im Vertriebs-Wizard** (nur bei WEB_FORMULAR)
2. ❌ **Erstkontakt mehrfach abfragen** (nur auf Karte 0)
3. ❌ **Speichern ans Ende koppeln** (jede Karte separat speicherbar)
4. ❌ **Schutz bei Kontaktdaten starten** (nur bei Erstkontakt)
5. ❌ **Alle Karten erzwingen** (User kann jederzeit speichern & schließen)
6. ❌ **Hardcoded `source: 'manual'`** (User wählt Source explizit)
7. ❌ **consentGivenAt in Sprint 2.1.5 senden** (Backend-Feld erst V259)

📖 **Details:** [FRONTEND_DELTA.md Section 0](./FRONTEND_DELTA.md#-nicht-tun-anti-patterns)

---

## 9. Verwandte Artefakte (Sprint 2.1.5)

- 📋 **[FRONTEND_DELTA.md](./FRONTEND_DELTA.md)** - Zentrale Frontend-Spezifikation (877 Zeilen)
- 🔒 **[PRE_CLAIM_LOGIC.md](./PRE_CLAIM_LOGIC.md)** - Pre-Claim Mechanik (10-Tage-Frist)
- 🔒 **[DSGVO_CONSENT_SPECIFICATION.md](./DSGVO_CONSENT_SPECIFICATION.md)** - DSGVO Compliance
- 📊 **[ACTIVITY_TYPES_PROGRESS_MAPPING.md](./ACTIVITY_TYPES_PROGRESS_MAPPING.md)** - 13 Activity-Types
- 🔄 **[DEDUPE_POLICY.md](./DEDUPE_POLICY.md)** - Hard/Soft Collisions (409 Handling)
- 📝 **[CONTRACT_MAPPING.md](./CONTRACT_MAPPING.md)** - Vertragliche Regeln (6M + 60T + 10T)

---

## 10. Sprint 2.1.6 Erweiterungen (geplant, Vertrag §3.3)

**🚨 Migration-Nummern:** Verwende IMMER `get-next-migration.sh` statt feste V-Nummern!

- ✅ **Backdating:** Activity-Zeitpunkte in Vergangenheit setzen (PUT /api/leads/{id}/registered-at)
- ✅ **consent_given_at:** Backend-Feld für Web-Formular (Migration: siehe Script)
- ✅ **Bestandsleads-Migration:** Altdaten-Import mit historischen Timestamps (POST /api/admin/migration/leads/import)
- ✅ **Nightly Jobs:** Automatische Warn-/Expiry-Emails (Tag 53/60/70)
- ✅ **Schutz-Verlängerung:** Antrag mit Begründung + Manager-Genehmigung (Vertrag §3.3.2e)
- ✅ **Stop-the-Clock UI:** StopTheClockDialog (Manager-only, Vertrag §3.3.2d)
- ✅ **Fuzzy-Matching:** Erweiterte Dedupe mit Levenshtein + pg_trgm

📖 **Details:** [SPRINT_2_1_6/SUMMARY.md](../SPRINT_2_1_6/SUMMARY.md) | [CONTRACT_MAPPING.md](./CONTRACT_MAPPING.md)

---

## 11. Lead-Management UI (Sprint 2.1.5) - ADR-006 Hybrid-Ansatz

**Zweck:** Vollständige CRUD-Oberfläche für Lead-Verwaltung

**⚠️ ARCHITEKTUR-ENTSCHEIDUNG (ADR-006):**
- ✅ **Phase 1 (Sprint 2.1.5):** Leads als Customer-Status (CustomersPageV2-Wiederverwendung)
- 🔄 **Phase 2 (Sprint 2.1.6):** Lead-spezifische Erweiterungen (Scoring, Workflows, Timeline)

📋 **Siehe:** [ADR-006-lead-management-hybrid-architecture.md](../../shared/adr/ADR-006-lead-management-hybrid-architecture.md)

---

### Phase 1: Lead-Management via CustomersPageV2 (IMPLEMENTIERT)

**Architektur:**
```typescript
// /pages/LeadsPage.tsx
export default function LeadsPage() {
  return <CustomersPageV2 defaultFilter={{ status: ['LEAD'] }} />;
}
```

**Verfügbare Features:**
1. **Intelligent Filter Bar**
   - Quick Filters: "Neue Leads", "Heiße Leads", "Meine Leads"
   - Advanced Filters: Status, Branche, Umsatzbereich, Risiko-Level
   - Universal Search (Debounce: 300ms, Cache: 60s)

2. **Smart Sort** (verkaufsorientiert)
   - 🔥 "Heiße Leads zuerst" → status ASC, expectedAnnualVolume DESC
   - ⏰ "Auslaufende Verträge" → nextFollowUpDate ASC
   - 💰 "Umsatz: Hoch → Niedrig" → expectedAnnualVolume DESC

3. **Virtualisierte Tabelle**
   - react-window (Performance für >20 Leads)
   - Column Manager (Sichtbarkeit, Reihenfolge)
   - Zustand Store (Persistent Config)

4. **Actions**
   - ✏️ Bearbeiten → LeadWizard (Progressive Profiling)
   - 🗑️ Löschen → Confirmation Dialog
   - 📊 Details → Lead-Detail-Page

### Implementierte Komponenten (Phase 1 - Sprint 2.1.5)

**LeadsPage.tsx** (Lifecycle-Context-Architektur)
- Wrapper um CustomersPageV2 mit `context="leads"` Prop
- Default-Filter: `status: ['LEAD']`
- Alle Customer-Features verfügbar: Filter, Sort, Columns, Search
- **Lifecycle-basiertes Denken:** Lead ist eine Phase, kein separates Entity

**IntelligentFilterBar.tsx** (mit Context-Prop)
- `context?: 'customers' | 'leads'` für kontext-sensitive Status-Filter
- Lead-Context: Zeigt nur LEAD, PROSPECT (Baby-Kunden)
- Customer-Context: Zeigt nur AKTIV, INAKTIV, ARCHIVIERT (Erwachsene Kunden)
- Alle anderen Filter identisch (Risiko, Umsatz, Kontakte)

**LeadSourceIcon.tsx** (Lead-spezifisch)
- Emoji-Icons mit Tooltips für Lead-Quellen
- 🎪 MESSE, 📞 TELEFON, 🤝 EMPFEHLUNG
- 🌐 WEB_FORMULAR, 🔗 PARTNER, ❓ SONSTIGE
- Verbessert Scannability in Listen

**LeadProtectionBadge.tsx** (Lead-spezifisch)
- Zeigt Schutzstatus: Geschützt, Vormerkung, Abgelaufen
- Countdown für Pre-Claim (10 Tage)
- Farbkodierung: grün/gelb/rot

**~~LeadListEnhanced.tsx~~ [OBSOLET]**
- Card-basierte UI ersetzt durch CustomersPageV2 (Tabellen-basiert)
- Begründung: Konsistente UX, keine Code-Duplikation, bessere Performance
- 📄 [ADR-006](../../shared/adr/ADR-006-lead-management-hybrid-architecture.md)

**~~LeadStageBadge.tsx~~ [OBSOLET]**
- Duplikat zu CustomerStatusBadge → gelöscht

### Phase 2: Lead-Erweiterungen (Sprint 2.1.6) [GEPLANT]

**1. Lead-Scoring-System**
- **Lead-Score-Berechnung:** 0-100 Punkte
  - Umsatzpotenzial (25%)
  - Engagement (25%)
  - Fit (25%)
  - Dringlichkeit (25%)
- **UI:** LeadScoreIndicator mit Progress Bar
- **Backend:** `ALTER TABLE leads ADD COLUMN lead_score INTEGER`

**2. Lead-Status-Workflows**
- **Status-Übergänge:**
  - LEAD → PROSPECT (Qualifizierung)
  - PROSPECT → AKTIV (Konversion)
- **UI:** LeadStatusWorkflow, LeadQualificationForm
- **Tracking:** Lead-Konversions-Metriken

**3. Lead-Activity-Timeline**
- **Activity-Log:** Chronologische Interaktions-Historie
- **Activity-Types:** EMAIL_SENT, CALL_MADE, MEETING_SCHEDULED, QUOTE_SENT
- **UI:** LeadActivityTimeline mit Icons

**4. Lead-Protection aktivieren**
- **Backend:** `GET /api/leads?assignedTo={userId}`
- **Index:** `CREATE INDEX idx_leads_assigned_to ON leads(assigned_to) WHERE status IN ('LEAD', 'PROSPECT')`
- **Frontend:** Quick Filter "Meine Leads" (assignedTo = currentUser)

### ~~Noch ausstehend (Backend-Abhängigkeiten)~~ [ERLEDIGT]

**✅ Backend-Anpassungen (2025-10-04):**
- ✅ GET /api/leads liefert vollständiges DTO
- ✅ Felder: `{id, companyName, stage, source, city, contact, protectionUntil, progressDeadline, lastActivityAt}`
- ⏳ PUT /api/leads/{id} → Sprint 2.1.6
- ⏳ DELETE /api/leads/{id} → Sprint 2.1.6

### UI-Routing (Phase 1)

**Route:** `/leads` (existiert in LeadsPage.tsx)
**Layout:** MainLayoutV2 (Navigation, Header, Footer)
**Komponenten-Hierarchie:**
```
LeadsPage.tsx
  └─ MainLayoutV2
      └─ CustomersPageV2 (mit Lead-Filter)
          ├─ IntelligentFilterBar
          ├─ VirtualizedCustomerTable
          └─ LeadWizard (Create/Edit Dialog)
```

📖 **Details:**
- [FRONTEND_DELTA.md Section 8](./FRONTEND_DELTA.md#8-lead-management-ui-2025-10-04---adr-006-hybrid-ansatz)
- [ADR-006](../../shared/adr/ADR-006-lead-management-hybrid-architecture.md)

---

## 11. Bestandsleads-Migration & Admin-Features (Sprint 2.1.6 Phase 2)

### 11.1 Batch-Import (LeadImportService)

**Zweck:** Massenmigration von Altdaten aus Excel/CSV ins CRM

**API:** `POST /api/admin/migration/leads/import`
**RBAC:** Admin-only

**Features:**
- **Batch-Verarbeitung:** Bis zu 1000 Leads pro Request
- **Dry-Run Mode:** Test-Import ohne DB-Änderung (`dryRun: true`)
- **Idempotency:** SHA-256 Request-Hash verhindert doppelte Imports
- **Duplikaten-Handling:**
  - Check: `companyName` + `city` (exact match)
  - Warnung zurückgeben, aber **trotzdem importieren**
  - Import mit `isCanonical=false` (vermeidet Unique Constraint Violation)
- **Validation:**
  - `registeredAt` nicht in Zukunft
  - Alle Activities müssen `countsAsProgress` explizit setzen

**Daten-Mapping:**
```java
Lead lead = new Lead();
lead.registeredAt = importData.registeredAt; // Historisches Datum!
lead.registeredAtSource = "import"; // lowercase - DB Constraint
lead.registeredAtOverrideReason = importData.importReason;
lead.protectionStartAt = importData.registeredAt;
lead.lastActivityAt = importData.registeredAt;
```

### 11.2 Backdating (LeadBackdatingService)

**Zweck:** Registrierungsdatum nachträglich korrigieren + Fristen neu berechnen

**API:** `PUT /api/leads/{id}/registered-at`
**RBAC:** Admin + Manager

**Business-Logik:**
```java
// 1. Update registeredAt
lead.registeredAt = request.registeredAt;
lead.registeredAtSource = "backdated"; // lowercase
lead.registeredAtSetBy = currentUserId;
lead.registeredAtOverrideReason = request.reason; // Audit PFLICHT

// 2. Recalculate Deadlines
lead.protectionStartAt = request.registeredAt;
LocalDateTime protectionUntil = request.registeredAt.plusMonths(6);
LocalDateTime baseProgressDeadline = request.registeredAt.plusDays(60);

// 3. Stop-the-Clock Integration
if (lead.clockStoppedAt != null) {
  long pauseDays = Duration.between(lead.clockStoppedAt, LocalDateTime.now()).toDays();
  lead.progressDeadline = baseProgressDeadline.plusDays(pauseDays);
}
```

**Anwendungsfall:** Bestandsleads von März 2024 heute importiert → Backdating setzt `registeredAt = 2024-03-15` → Protection läuft bis September 2024 (historisch korrekt)

### 11.3 Lead → Customer Conversion (LeadConvertService)

**Zweck:** Interessent automatisch in Kunde umwandeln (mit vollständiger Datenübernahme)

**API:** `POST /api/leads/{id}/convert`
**RBAC:** All roles (Admin, Manager, User)

**Daten-Harmonisierung (vollständig):**

```java
// 1. Customer (Hauptentität)
Customer customer = new Customer();
customer.setCompanyName(lead.companyName);
customer.setStatus(CustomerStatus.AKTIV);
customer.setOriginalLeadId(leadId); // V261 Tracking
customer.persist();

// 2. CustomerLocation (Haupt-Standort)
CustomerLocation location = new CustomerLocation();
location.setLocationName(lead.companyName + " - Hauptsitz");
location.setCategory(LocationCategory.HEADQUARTERS);
location.setIsMainLocation(true);
location.persist();

// 3. CustomerAddress (Rechnungsadresse)
CustomerAddress address = new CustomerAddress();
address.setAddressType(AddressType.BILLING);
address.setStreet(lead.street);
address.setCity(lead.city);
address.setCountry(mapCountryCode(lead.countryCode)); // DE → DEU via Java Locale
address.persist();

// 4. CustomerContact (Hauptansprechpartner)
CustomerContact contact = new CustomerContact();
String[] nameParts = lead.contactPerson.split("\\s+", 2);
contact.setFirstName(nameParts[0]);
contact.setLastName(nameParts.length > 1 ? nameParts[1] : "");
contact.setEmail(lead.email);
contact.setPhone(lead.phone);
contact.setIsPrimary(true);
contact.persist();
```

**Country Code Mapping (Smart Engineering):**
```java
// Java Locale automatisches ISO-3166-1 alpha-2 → alpha-3 Mapping
Locale locale = new Locale("", "DE");
String iso3 = locale.getISO3Country(); // → "DEU"
// Funktioniert für 200+ Länder, 0 Wartung!
```

**Keep/Delete Option:**
- `keepLeadRecord=true`: Lead.status → CONVERTED (Audit-Trail)
- `keepLeadRecord=false`: Lead hard-deleted (Clean-up)

**Migration V261:**
```sql
ALTER TABLE customers ADD COLUMN original_lead_id BIGINT NULL;
-- SOFT REFERENCE (kein FK!) - erlaubt Lead-Löschung ohne Cascade
```

---

**📅 Erstellt:** 2025-10-04
**🔄 Letzte Aktualisierung:** 2025-10-06 (Sprint 2.1.6 Phase 2 ergänzt)
**✅ Status:** Approved (Production-Ready)
