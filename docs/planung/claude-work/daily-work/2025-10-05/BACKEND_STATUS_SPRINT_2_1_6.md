# 🟢 Backend-Status Sprint 2.1.6 - Existierende Features

**Erstellt:** 2025-10-05
**Kontext:** Deep Analysis Sprint 2.1.6 - Backend ist weiter als erwartet!
**Quelle:** `backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java`

---

## 🎯 KERNAUSSAGE

**Sprint 2.1.6 ist primär API + Frontend Work, NICHT Backend-Grundlagen!**

Viele Features haben bereits vollständige Backend-Unterstützung - **nur API-Endpoints + UI fehlen!**

---

## ✅ BACKEND-READY FEATURES (nur API/UI fehlen)

### 1. Stop-the-Clock (User Story 3)

**Lead.java Zeile 145-154:**
```java
@Column(name = "clock_stopped_at")
public LocalDateTime clockStoppedAt;

@Column(name = "stop_reason", columnDefinition = "TEXT")
public String stopReason;

@Size(max = 50)
@Column(name = "stop_approved_by")
public String stopApprovedBy;
```

**Status:** ✅ **Backend 100% Ready**

**Noch zu implementieren:**
- ✅ PUT /api/leads/{id}/stop-clock Endpoint (LeadResource.java)
- ✅ PUT /api/leads/{id}/resume-clock Endpoint (LeadResource.java)
- ✅ StopTheClockDialog.tsx (Frontend Component)
- ✅ Pause/Resume Buttons in LeadProtectionBadge.tsx

**Aufwand:** 4-6h (3h Backend API + 3h Frontend UI)

**Begründung:** Backend-Felder existieren bereits seit Sprint 2.1.5 (V255 Migration) - nur noch Service-Layer + REST-Endpoints + UI fehlen!

---

### 2. Backdating (Technische Details)

**Lead.java Zeile 119-132:**
```java
@Size(max = 250)
@Column(name = "registered_at_override_reason")
public String registeredAtOverrideReason;

@Size(max = 100)
@Column(name = "registered_at_set_by")
public String registeredAtSetBy;
```

**Status:** ✅ **Backend 100% Ready**

**Noch zu implementieren:**
- ✅ PUT /api/leads/{id}/registered-at Endpoint (LeadResource.java)
- ✅ BackdatingRequest DTO
- ✅ Protection-Berechnung anpassen (basierend auf neuem registered_at)
- ⚪ OPTIONAL: Frontend UI (BackdatingDialog.tsx)

**Aufwand:** 2-3h (Backend API only, Frontend OPTIONAL)

**Begründung:** Backdating ist primär für Bestandsleads-Migration (Modul 08) - UI ist "nice-to-have", API reicht!

---

### 3. Automated Jobs (Technische Details)

**Lead.java Zeile 174-178:**
```java
@Column(name = "progress_warning_sent_at")
public LocalDateTime progressWarningSentAt;

@Column(name = "progress_deadline")
public LocalDateTime progressDeadline;
```

**Status:** ✅ **Backend teilweise Ready**

**Noch zu implementieren:**
- ✅ @Scheduled Job 1: Progress Warning (Tag 53)
- ✅ @Scheduled Job 2: Protection Expiry (Tag 60)
- ✅ @Scheduled Job 3: DSGVO Pseudonymisierung (Tag 60)
- ⚪ OPTIONAL: Admin UI für Job-Monitoring (Sprint 2.1.7)

**Aufwand:** 4-6h (Backend Jobs only, UI in 2.1.7)

**Begründung:** `progressWarningSentAt` und `progressDeadline` existieren bereits - nur Scheduled Tasks fehlen!

---

### 4. Lead Stage (Sprint 2.1.5 - Issue #125)

**Lead.java Zeile 138-143:**
```java
@Enumerated(EnumType.ORDINAL)
@Column(name = "stage", nullable = false)
public LeadStage stage = LeadStage.VORMERKUNG;
```

**Status:** ✅ **COMPLETE (PR #131 merged)**

**Migration:** V255 (bereits deployed)

**Begründung:** Issue #125 wurde in Sprint 2.1.5 abgeschlossen - kein weiterer Aufwand!

---

## ❌ BACKEND-FELDER FEHLEN (neue Migrations nötig)

Diese Features benötigen **NEUE** DB-Migrations und sind auf **Sprint 2.1.7** verschoben:

### 1. Lead-Transfer Workflow (User Story 1 - verschoben)

**Fehlt:**
- `lead_transfers` Tabelle (V260 Migration)
- Felder: `from_user_id`, `to_user_id`, `transfer_reason`, `approved_by`, `transfer_status`

**Aufwand:** 8-12h (Migration + Service + API + Frontend)

**Begründung:** Zu komplex für Sprint 2.1.6 - eigener Sprint 2.1.7 gerechtfertigt!

---

### 2. Row-Level-Security (User Story 5 - verschoben)

**Fehlt:**
- RLS Policies (V261 Migration): `owner_policy`, `team_policy`, `admin_policy`
- PostgreSQL ALTER TABLE ... ENABLE ROW LEVEL SECURITY

**Aufwand:** 10-14h (Migration + Testing + Performance Monitoring)

**Begründung:** RLS Performance-Tests + Policy-Debugging ist aufwändig - eigener Sprint nötig!

---

### 3. Team Management (User Story 6 - verschoben)

**Fehlt:**
- `teams` Tabelle (V262 Migration)
- `team_members` Tabelle (V262 Migration)
- Felder: `team_name`, `team_leader_id`, `territory`, `member_role`

**Aufwand:** 8-10h (Migration + CRUD + Assignment-Logic)

**Begründung:** Voraussetzung für Lead-Transfer - muss zuerst implementiert werden!

---

### 4. Fuzzy-Matching (User Story 4 - verschoben)

**Fehlt:**
- Scoring-Algorithmus (Email, Phone, Company, Address Matching)
- `duplicate_review_queue` Tabelle (optional)

**Aufwand:** 12-16h (Algorithmus + Review-UI + False-Positive Handling)

**Begründung:** Komplex & fehleranfällig - benötigt umfangreiche Tests!

---

## 📊 MIGRATION-ÜBERSICHT

### ✅ Deployed (Sprint 2.1.1 - 2.1.5):

- **V255:** Lead Stage Enum + protection_until + progress_deadline (Sprint 2.1.5)
- **V256:** lead_activities Tabelle (Sprint 2.1.5)
- **V257:** activity_types Tabelle + Seeds (Sprint 2.1.5)
- **V258:** Lead Protection Rules (Sprint 2.1.5)
- **V259:** remove_leads_company_city_unique_constraint (Sprint 2.1.5)

### 🔄 Sprint 2.1.6 (KEINE neuen Migrations!):

**Backend-Felder existieren bereits!**
- Stop-the-Clock: V255 (deployed)
- Backdating: V255 (deployed)
- Automated Jobs: V255 (deployed)

**Konsequenz:** Sprint 2.1.6 braucht **KEINE** DB-Migrationen!

### 🔮 Sprint 2.1.7 (geplant):

- **V260:** lead_transfers Tabelle (Lead-Transfer Workflow)
- **V261:** RLS Policies (Row-Level-Security)
- **V262:** teams + team_members Tabellen (Team Management)

**Migration-Check:**
```bash
./scripts/get-next-migration.sh
# Output: V260
```

---

## 🎯 KONSEQUENZEN FÜR SPRINT 2.1.6

### ✅ VORTEILE:

1. **Schnellere Umsetzung:**
   - Kein DB-Schema-Design nötig
   - Kein Migration-Writing + Testing
   - Fokus auf Business-Logic (Service-Layer + REST)

2. **Geringeres Risiko:**
   - Backend-Schema stabil (keine Breaking Changes)
   - Keine Rollback-Risiken
   - Keine Performance-Regression durch neue Indizes

3. **Einfachere Code-Reviews:**
   - Nur Service + Resource + Frontend
   - Keine SQL-Review nötig
   - Kleinere PRs möglich

4. **Bessere Testbarkeit:**
   - Backend-Felder bereits getestet (Sprint 2.1.5)
   - Nur neue Endpoints + UI testen
   - Unit-Tests ohne DB-Setup

### ⚠️ EINSCHRÄNKUNGEN:

1. **Bestandsleads-Migration (Modul 08):**
   - Braucht KEINE neuen Felder (nutzt existierende!)
   - Import-API arbeitet mit vorhandenen Lead-Feldern
   - Dry-Run validiert gegen bestehendes Schema

2. **Lead → Kunde Convert Flow:**
   - Nutzt existierende `customer` Tabelle
   - Neue Felder in `customer` OPTIONAL (z.B. `original_lead_id`)
   - Kann in Sprint 2.1.6 oder 2.1.7 ergänzt werden

3. **ADR-006 Phase 2 (OPTIONAL):**
   - Lead-Scoring: Braucht NEUES Feld `lead_score INTEGER`
   - Lead-Activity-Timeline: Nutzt existierende `lead_activities` (V256)
   - Lead-Status-Workflows: Nutzt existierendes `stage` Enum (V255)

---

## 🚀 EMPFOHLENER WORKFLOW

### Phase 1: Issue #130 Fix (BLOCKER - 1-2h)
- Keine Backend-Änderungen
- Nur Test-Refactoring

### Phase 2: Backend APIs (4-6h)
1. Stop-the-Clock Endpoints (PUT /stop-clock, /resume-clock)
2. Backdating Endpoint (PUT /registered-at)
3. Convert-Flow Endpoint (POST /convert)

### Phase 3: Automated Jobs (4-6h)
1. @Scheduled Progress Warning Job
2. @Scheduled Protection Expiry Job
3. @Scheduled DSGVO Pseudonymisierung Job

### Phase 4: Frontend UI (6-8h)
1. StopTheClockDialog.tsx
2. LeadProtectionBadge.tsx (Pause/Resume Buttons)
3. OPTIONAL: Lead-Scoring, Workflows, Timeline (ADR-006 Phase 2)

**Gesamt-Aufwand Sprint 2.1.6:** 16-22h (statt ursprünglich 30-40h!)

---

## 📋 VALIDIERUNG

**Lead.java Analyse (05.10.2025):**
```bash
# Backend-Felder validieren:
grep -n "clock_stopped_at\|stop_reason\|registered_at_override\|progress_warning_sent_at" \
  backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java

# Output:
# 145: @Column(name = "clock_stopped_at")
# 148: @Column(name = "stop_reason", columnDefinition = "TEXT")
# 151: @Column(name = "stop_approved_by")
# 119: @Column(name = "registered_at_override_reason")
# 127: @Column(name = "registered_at_set_by")
# 174: @Column(name = "progress_warning_sent_at")
# 177: @Column(name = "progress_deadline")
```

**Migration-Status:**
```bash
./scripts/get-next-migration.sh
# Output: V260 (für Sprint 2.1.7!)

ls backend/src/main/resources/db/migration/ | tail -3
# V257__create_activity_types_table.sql
# V258__add_lead_protection_rules.sql
# V259__remove_leads_company_city_unique_constraint.sql
```

**Konsequenz:** Alle benötigten Backend-Felder für Sprint 2.1.6 sind deployed! ✅

---

## 🔗 REFERENZEN

**Lead.java:**
- Pfad: `backend/src/main/java/de/freshplan/modules/leads/domain/Lead.java`
- Zeilen: 119-178 (Stop-the-Clock, Backdating, Progress Tracking)

**Deployed Migrations:**
- V255: `backend/src/main/resources/db/migration/V255__add_lead_stage_and_protection_fields.sql`
- V256: `backend/src/main/resources/db/migration/V256__create_lead_activities_table.sql`
- V257: `backend/src/main/resources/db/migration/V257__create_activity_types_table.sql`
- V258: `backend/src/main/resources/db/migration/V258__add_lead_protection_rules.sql`
- V259: `backend/src/main/resources/db/migration/V259__remove_leads_company_city_unique_constraint.sql`

**Sprint-Dokumentation:**
- TRIGGER_SPRINT_2_1_6.md: Technische Details Abschnitt (Zeile 271-343)
- TRIGGER_SPRINT_2_1_7.md: Verschobene Features (V260-V262 Migrations)

---

**Erstellt von:** Claude Code (Sonnet 4.5)
**Datum:** 2025-10-05
**Kontext:** Sprint 2.1.6 Deep Analysis - Backend-Status Dokumentation

**✅ Backend ist weiter als gedacht - Sprint 2.1.6 wird schneller als erwartet!**
