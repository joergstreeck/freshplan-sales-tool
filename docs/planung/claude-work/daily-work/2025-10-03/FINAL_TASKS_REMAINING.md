# 📋 FINALE AUFGABEN - Sprint 2.1.5 Dokumentation

**Erstellt:** 2025-10-03 22:33
**Session:** Dokumentations-Validierung nach ChatGPT Feedback
**Branch:** feature/mod02-sprint-2.1.5-implementation
**Token-Verbrauch:** ~88K / 200K (44%)

---

## ✅ BEREITS ERLEDIGT (Session 2 + 3):

### Session 2 (Tasks 3-7 aus NEXT_STEPS.md):
- ✅ Master Plan V5 SESSION_LOG + DECISIONS aktualisiert
- ✅ CRM_AI_CONTEXT_SCHNELL.md erweitert (Pre-Claim, Dedupe, Activity-Types, DSGVO)
- ✅ TRIGGER_INDEX.md Sprint 2.1.5 Status aktualisiert
- ✅ SPRINT_MAP.md mit 3 neuen Artefakten verlinkt
- ✅ Validierung: Alle Links + Konsistenz geprüft

### Session 3 (Korrekturen nach meiner Analyse):
- ✅ TRIGGER_SPRINT_2_1_5.md: V249-Referenz entfernt (existiert nicht, korrigiert zu V258)
- ✅ TRIGGER_SPRINT_2_1_5.md: Erstkontakt-Pflichtblock Details ergänzt (Kanal, Datum, Notiz)
- ✅ TRIGGER_SPRINT_2_1_5.md: System-Activity-Types hinzugefügt (3 neue)
- ✅ TRIGGER_SPRINT_2_1_5.md: DoD-Sektion erweitert (8 Kategorien, ChatGPT-Plan)
- ✅ TRIGGER_SPRINT_2_1_5.md: V258 Migration-Warnung (DB-Constraint gefunden)
- ✅ TRIGGER_SPRINT_2_1_6.md: Bestandsleads-Migration API User Story + API-Spec
- ✅ TRIGGER_SPRINT_2_1_6.md: Migration-Nummern korrigiert (V258→V259)
- ✅ DB-Constraint Validierung: V238 chk_activity_type gefunden

---

## 🔧 NOCH ZU ERLEDIGEN (6 Tasks):

### **KRITISCH - Dokumentations-Fixes (basierend auf ChatGPT Validierung):**

#### Task 1: `consent_given_at` Feld-Timing klarstellen ⚠️
**Datei:** `TRIGGER_SPRINT_2_1_5.md`
**Zeilen:** 91, 204, 270

**Problem:** Feld wird 3x erwähnt, existiert aber NICHT in DB (keine Migration)

**Fix:**
```markdown
ALT (Zeile 91):
- `source = WEB_FORMULAR` → Consent-Checkbox PFLICHT (consent_given_at)

NEU:
- `source = WEB_FORMULAR` → Consent-Checkbox PFLICHT
  - **⚠️ WICHTIG:** Feld `consent_given_at` kommt erst in V259 (Sprint 2.1.6 Web-Intake)
  - Frontend in 2.1.5: UI vorbereitet, Backend-Feld NICHT vorhanden
  - Validierung erfolgt nur Frontend-seitig, keine Backend-Persistierung
```

**Auch ändern in:**
- Zeile 204: `lead.consent_given_at TIMESTAMPTZ` → mit Hinweis "Sprint 2.1.6"
- Zeile 270: `lead.consent_given_at Feld` → mit Hinweis "Sprint 2.1.6"

---

#### Task 2: Soft Duplicate Resubmit-Pfad dokumentieren ⚠️
**Datei:** `TRIGGER_SPRINT_2_1_5.md`
**Nach Zeile:** 130

**Einfügen:**
```markdown
### Dedupe Resubmit-Flow (Hard/Soft einheitlich)

**Client-Verhalten bei 409:**
1. **Hard Collision** (kein `severity` Feld):
   - Resubmit NUR mit `overrideReason` (Query-Param, min. 10 Zeichen)
   - Benötigt MANAGER oder ADMIN Role
   - Beispiel: `POST /api/leads?overrideReason=Unterschiedliche%20Standorte`
   - Audit-Log: `lead_duplicate_override`

2. **Soft Collision** (`severity: "WARNING"`):
   - Resubmit mit `reason` (Query-Param, min. 10 Zeichen)
   - Beliebige Role (kein Manager-Override nötig)
   - Beispiel: `POST /api/leads?reason=Neue%20Niederlassung`
   - Kein Audit-Log

**Frontend-Dialog:**
- Hard: "Duplikat existiert" → Button "Trotzdem anlegen" (nur Manager/Admin)
- Soft: "Ähnlicher Lead gefunden" → Button "Trotzdem anlegen" (alle)
- Beide: Reason-Textarea PFLICHT vor Resubmit
```

---

#### Task 3: Pre-Claim Filter/Badge hinzufügen ❌ **FEHLT KOMPLETT**
**Datei:** `TRIGGER_SPRINT_2_1_5.md`
**Nach Zeile:** 106

**Einfügen:**
```markdown
### Pre-Claim UI-Komponenten (Sprint 2.1.5 Frontend Phase 2)

**Listen-Filter:**
- **"Alle Pre-Claim Leads"**: `WHERE registered_at IS NULL`
- **"Pre-Claim (läuft ab ≤3 Tage)"**: `WHERE registered_at IS NULL AND created_at < NOW() - INTERVAL '7 days'`
- **"Pre-Claim (läuft ab ≤10 Tage)"**: `WHERE registered_at IS NULL AND created_at >= NOW() - INTERVAL '10 days'`
- **"Pre-Claim (abgelaufen)"**: `WHERE registered_at IS NULL AND created_at < NOW() - INTERVAL '10 days'`

**Badge im LeadHeader:**
- **Text:** "Pre-Claim"
- **Color:** Orange (#FFA500)
- **Tooltip:** "Kein Schutz aktiv – Vervollständigen bis [created_at + 10 Tage]"
- **Icon:** ⏳ (Sanduhr)

**Lead-Liste Spalte:**
- Neue Spalte "Status" zeigt Pre-Claim Badge prominent
- Sortierung: Pre-Claim Leads zuerst (ASC created_at)
```

---

#### Task 4: Erstkontakt-Flow NOTE in CONTRACT_MAPPING.md ℹ️
**Datei:** `features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/CONTRACT_MAPPING.md`
**Abschnitt:** "Vertragliche Anforderungen"

**Ergänzen:**
```markdown
### Pre-Claim & Erstkontakt (§2(8)(a))

**WICHTIG:** Activity `FIRST_CONTACT_DOCUMENTED` startet Schutz **OHNE** V257-Trigger.

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
```

---

#### Task 5: LEAD_ASSIGNED Metadata spezifizieren 📋
**Datei:** `TRIGGER_SPRINT_2_1_5.md`
**Zeilen:** 326-329

**ALT:**
```markdown
- [ ] **Assignment Service (Interface-Vorbereitung):**
  - [ ] AssignmentService.assign(Lead) Interface definiert
  - [ ] Geo → Segment → Workload Logik implementiert
  - [ ] Activity LEAD_ASSIGNED wird persistiert
```

**NEU:**
```markdown
- [ ] **Assignment Service (Interface-Vorbereitung):**
  - [ ] AssignmentService.assign(Lead) Interface definiert
  - [ ] Geo → Segment → Workload Logik implementiert
  - [ ] Activity LEAD_ASSIGNED wird persistiert mit Metadata:
    - `method`: "GEO_WORKLOAD" | "MANUAL" | "WEB_INTAKE_AUTO"
    - `metadata.previousOwner`: UUID (falls Re-Assign, sonst NULL)
    - `metadata.assignmentReason`: String (z.B. "Geo-Zone München, Workload 3/10")
    - `metadata.geoZone`: String (z.B. "DE-BY-München")
    - `metadata.workloadScore`: Integer (aktueller Workload 0-100)
```

---

#### Task 6: Observability Metriken dokumentieren 📊
**Datei:** `TRIGGER_SPRINT_2_1_5.md`
**Nach:** DoD-Sektion (nach Zeile 342)

**Einfügen:**
```markdown
## Observability & Metriken

### Pre-Claim Metriken:
```java
// Micrometer Gauges
@Gauge(name = "leads_preclaim_open_total", description = "Anzahl Pre-Claim Leads (registered_at IS NULL)")
public long getPreClaimOpenCount() { ... }

@Gauge(name = "leads_preclaim_expiring_3d", description = "Pre-Claim Leads ablaufend ≤3 Tage")
public long getPreClaimExpiring3Days() { ... }

// Micrometer Counters
@Counter(name = "leads_preclaim_expired_total", description = "Abgelaufene Pre-Claim Leads (created_at + 10d)")
public void incrementPreClaimExpired() { ... }

// Micrometer Histograms
@Timer(name = "lead_first_contact_to_protection_ms", description = "Zeit von Erstellung bis Schutzbeginn")
public void recordFirstContactToProtection(Duration duration) { ... }
```

### Dedupe Metriken:
```java
@Counter(name = "dedupe_block_total", description = "Harte Kollisionen geblockt")
public void incrementDedupeBlock() { ... }

@Counter(name = "dedupe_warn_total", description = "Weiche Kollisionen gewarnt")
public void incrementDedupeWarn() { ... }

@Counter(name = "duplicate_overrides_total", description = "Manager-Overrides bei Hard Collisions")
public void incrementDuplicateOverride() { ... }
```

### Assignment Metriken:
```java
@Timer(name = "lead_assignment_duration_ms", description = "Dauer der Lead-Zuweisung")
public void recordAssignmentDuration(Duration duration) { ... }

@Counter(name = "lead_assignments_total", description = "Anzahl Lead-Zuweisungen", tags = ["method"])
public void incrementAssignment(String method) { ... } // method: GEO_WORKLOAD, MANUAL, WEB_INTAKE_AUTO
```

### Dashboard-Queries (für Prometheus/Grafana):
```promql
# Pre-Claim Leads ablaufend in 3 Tagen
leads_preclaim_expiring_3d

# Dedupe Block-Rate
rate(dedupe_block_total[5m])

# Manager-Override-Rate
rate(duplicate_overrides_total[5m]) / rate(dedupe_block_total[5m])

# Durchschnittliche Assignment-Dauer
histogram_quantile(0.95, rate(lead_assignment_duration_ms_bucket[5m]))
```
```

---

## 📝 OPTIONALE ERGÄNZUNGEN (Nice-to-Have):

### Optional 1: Live-Dedupe-Hints (ChatGPT Empfehlung)
**Datei:** `TRIGGER_SPRINT_2_1_5.md`
**Sektion:** Frontend Components

**Einfügen:**
```markdown
### Live-Dedupe-Hints (UX-Optimierung)

**Verhalten:**
- Throttle: 300ms nach letzter Eingabe
- API-Endpoint: `GET /api/leads/check-duplicate?email=...&phone=...&company=...&postalCode=...`
- Response: `{ "isDuplicate": boolean, "duplicateLeads": [...], "severity": "HARD"|"SOFT" }`

**UI-Feedback:**
- Email-Feld: Inline-Warnung "Ähnlicher Lead existiert" (orange) bei Soft
- Email-Feld: Inline-Error "Lead existiert bereits" (rot) bei Hard
- Tooltip zeigt Duplikat-Details (Firma, Stadt, Owner)
```

### Optional 2: Consent-Text finalisieren
**Datei:** `features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/DSGVO_CONSENT_SPECIFICATION.md`

**Hinweis:** Erst bei Web-Intake (Sprint 2.1.6) relevant, kann jetzt vorbereitet werden.

---

## 🎯 QUICK-START FÜR NÄCHSTE SESSION:

```bash
# 1. Handover lesen
cat docs/planung/claude-work/daily-work/2025-10-03/2025-10-03_HANDOVER_22-32.md

# 2. Diese Datei öffnen
cat docs/planung/claude-work/daily-work/2025-10-03/FINAL_TASKS_REMAINING.md

# 3. Task 1 starten (consent_given_at Timing)
# Öffne TRIGGER_SPRINT_2_1_5.md Zeile 91, 204, 270
```

---

## ✅ CHECKLISTE (zum Abhaken):

- [ ] Task 1: `consent_given_at` Timing klargestellt (3 Stellen)
- [ ] Task 2: Soft Duplicate Resubmit-Pfad dokumentiert
- [ ] Task 3: Pre-Claim Filter/Badge hinzugefügt
- [ ] Task 4: Erstkontakt-Flow NOTE in CONTRACT_MAPPING.md
- [ ] Task 5: LEAD_ASSIGNED Metadata spezifiziert
- [ ] Task 6: Observability Metriken dokumentiert

**Optional:**
- [ ] Live-Dedupe-Hints spezifiziert
- [ ] Consent-Text in DSGVO_CONSENT_SPECIFICATION.md

---

**Geschätzte Zeit:** ~45min (6 x ~7-8min pro Task)
**Priorität:** P0 (KRITISCH für Sprint 2.1.5 Implementation)
**Nächster Commit:** Nach Abschluss aller 6 Tasks → Git Commit + Push

**Erstellt:** 2025-10-03 22:33
**Token-Verbrauch bei Erstellung:** ~88K / 200K (44%)
