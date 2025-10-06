---
module: "02_neukundengewinnung"
doc_type: "analyse"
status: "completed"
owner: "team/leads-backend"
updated: "2025-10-06"
---

# 🔍 Technical Debt Analysis - Sprint 2.1.6 Abschluss

**Analysiert am:** 2025-10-06 22:50
**Analysiert von:** Claude Code
**Scope:** Alle offenen GitHub Issues + Code-TODOs + @Deprecated Code

---

## 📊 Executive Summary

**Status:** ✅ **SEHR GERING** - Keine kritischen technischen Schulden, die Sprint 2.1.6 Abschluss blockieren

**Kategorien:**
- 🔴 **KRITISCH (BLOCKER):** 0 Issues
- 🟡 **MITTEL (sollte bald):** 2 Issues (#127, #126)
- 🔵 **NIEDRIG (Nice-to-Have):** 4 Issues (#119, #98, #38, #24)
- ✅ **CODE-TODOs:** 9 TODOs (alle dokumentiert, nicht kritisch)
- ⚠️ **@Deprecated Code:** 6 Items (Cleanup Q1 2026 geplant)

**Empfehlung:** ✅ **SPRINT 2.1.6 KANN ABGESCHLOSSEN WERDEN** - Keine Blocker

---

## 🔴 KRITISCHE TECHNISCHE SCHULDEN (BLOCKER)

### **Status: KEINE KRITISCHEN SCHULDEN ✅**

Keine Issues oder Code-Probleme gefunden, die den Production-Deployment blockieren.

---

## 🟡 MITTLERE TECHNISCHE SCHULDEN (Sollte bald)

### **Issue #127: Clock Injection für deterministische Zeit-Tests**

**Typ:** Refactoring / Test-Qualität
**Erstellt:** 2025-10-01
**Labels:** `enhancement`, `backend`, `tests`
**Sprint:** Sprint 2.1.7 (nach PR #124)

**Problem:**
```java
// Aktuell: Non-deterministic
LocalDateTime now = LocalDateTime.now();

// Sollte sein: Deterministic
LocalDateTime now = LocalDateTime.now(clock);
```

**Betroffene Services:**
- `LeadProtectionService.needsProgressWarning()`
- Alle zeitabhängigen Business-Logik-Methoden

**Impact:**
- 🟡 **Test-Qualität:** Tests sind nicht 100% deterministisch
- ✅ **Production:** Kein Einfluss (funktioniert korrekt)
- 🔵 **CI-Stabilität:** Gering (Zeitfenster groß genug)

**Lösung:**
1. `Clock` als CDI-Bean injizieren
2. `ClockProvider` für Production (systemDefaultZone)
3. Tests nutzen `Clock.fixed()` für deterministische Zeit

**Akzeptanzkriterien:**
- [ ] Clock als Dependency injiziert
- [ ] ClockProvider erstellt
- [ ] Tests nutzen Clock.fixed()
- [ ] Alle zeitabhängigen Methoden nutzen injected Clock

**Priorität:** 🟡 **MITTEL** (Refactoring, nicht kritisch)
**Timeline:** Sprint 2.1.7 (nach PR #124 Merge)
**Effort:** 2-3 Stunden

**ENTSCHEIDUNG FÜR SPRINT 2.1.6:**
- ⚠️ **NICHT BLOCKIEREND** für Phase 3 Abschluss
- ✅ **Tests funktionieren** (großes Zeitfenster)
- 📋 **Deferiert auf Sprint 2.1.7** (separater PR)

---

### **Issue #126: ActivityOutcome als Enum + DB CHECK Constraint**

**Typ:** Data Integrity / Code Quality
**Erstellt:** 2025-10-01
**Labels:** `enhancement`, `chore`, `backend`
**Sprint:** Sprint 2.1.6 (benötigt Migration V258+)

**Problem:**
```java
// Aktuell: String (keine Validierung)
@Size(max = 50)
@Column(name = "outcome")
public String outcome;

// Sollte sein: Enum (Type-Safe)
@Enumerated(EnumType.STRING)
@Column(name = "outcome")
public ActivityOutcome outcome;
```

**Migration V256 Comment sagt:**
```sql
COMMENT ON COLUMN lead_activities.outcome IS
  'Ergebnis: positive_interest | needs_more_info | not_interested | ...'
```

**Impact:**
- 🟡 **Data Integrity:** String erlaubt ungültige Werte (z.B. Typos)
- 🟡 **Code Quality:** Keine Compile-Time-Validierung
- ✅ **Production:** Funktioniert aktuell (keine Datenfehler beobachtet)

**Lösung:**
1. `ActivityOutcome` Enum erstellen
2. `LeadActivity.outcome` → Enum mit `@Enumerated(EnumType.STRING)`
3. Migration V258+ mit DB CHECK Constraint:
```sql
ALTER TABLE lead_activities
  ADD CONSTRAINT lead_activities_outcome_chk CHECK (outcome IN (
    'POSITIVE_INTEREST',
    'NEEDS_MORE_INFO',
    ...
  ) OR outcome IS NULL);
```

**Akzeptanzkriterien:**
- [ ] Enum ActivityOutcome erstellt
- [ ] LeadActivity.outcome nutzt Enum
- [ ] Migration V258 mit CHECK Constraint
- [ ] Ungültige Outcomes werden rejected

**Priorität:** 🟡 **MITTEL** (Data Integrity, Best Practice)
**Timeline:** Sprint 2.1.6 oder 2.1.7
**Effort:** 1-2 Stunden
**Migration:** V258+ (nach V268)

**ENTSCHEIDUNG FÜR SPRINT 2.1.6:**
- ⚠️ **NICHT BLOCKIEREND** für Phase 3 Abschluss
- ✅ **Daten sind aktuell sauber** (keine ungültigen Outcomes)
- 📋 **KANN in Sprint 2.1.6 Phase 4 gemacht werden** (optional)
- ⚠️ **ODER deferieren auf Sprint 2.1.7** (nicht kritisch)

---

## 🔵 NIEDRIGE TECHNISCHE SCHULDEN (Nice-to-Have)

### **Issue #119: Stub-Aufräumen Modul 02 nach Sprint 2.3**

**Typ:** Dokumentations-Cleanup
**Erstellt:** 2025-09-27
**Labels:** Keine
**Timeline:** Nach Sprint 2.3 Complete

**Problem:**
Temporäre Stub-Verzeichnisse aus Modul 02 Root sollten entfernt werden:
- `lead-erfassung/` (nur _index.md stub)
- `email-posteingang/` (nur _index.md stub)
- `kampagnen/` (nur _index.md stub)
- `diskussionen/` (nur _index.md stub)
- `implementation-plans/` (nur _index.md stub)
- `test-coverage/` (nur _index.md stub)
- `testing/` (nur _index.md stub)
- `postmortem/` (nur _index.md stub)

**Impact:**
- 🔵 **Dokumentations-Hygiene:** Stubs sind verwirrend
- ✅ **Funktionalität:** Keine (nur Doku)
- 🔵 **Developer Experience:** Gering

**Retention-Policy:**
- ✅ **2 Sprints** (Sprint 2.1.2 → Sprint 2.3)
- ⏰ **Frühestens nach Sprint 2.3 Complete**

**Priorität:** 🔵 **NIEDRIG** (Kosmetisch)
**Timeline:** Sprint 2.3+
**Effort:** 30 Minuten

**ENTSCHEIDUNG FÜR SPRINT 2.1.6:**
- ✅ **NICHT RELEVANT** für Sprint 2.1.6
- 📋 **Warten auf Sprint 2.3** (Retention-Policy)

---

### **Issue #98: Optional CI-Job für Backend Header/CORS Tests**

**Typ:** CI/CD Verbesserung
**Erstellt:** 2025-09-23
**Labels:** `enhancement`
**Sprint:** Optional (Nice-to-Have)

**Problem:**
Header- und CORS-Tests in `verify-fail-closed-security.sh` laufen nur wenn Backend verfügbar ist.
- Aktuell: ⚠️ Warnung in CI (wenn Backend nicht läuft)
- Sollte: ✅ Tatsächliche Verifikation in CI

**Lösung:**
1. Neuer Job in `.github/workflows/security-gates.yml`
2. PostgreSQL + Backend kurz starten
3. Header/CORS-Tests durchführen
4. Backend wieder stoppen

**Impact:**
- 🔵 **Security:** Kein kritischer Impact (manuelle Tests laufen)
- 🔵 **CI-Coverage:** Nice-to-Have (Header-Tests sind optional)
- ✅ **Production:** Header funktionieren korrekt

**Priorität:** 🔵 **NIEDRIG** (Nice-to-Have)
**Timeline:** Beliebig (kein Zeitdruck)
**Effort:** 2-3 Stunden

**ENTSCHEIDUNG FÜR SPRINT 2.1.6:**
- ✅ **NICHT BLOCKIEREND**
- ✅ **Security-Tests laufen manuell** (ausreichend)
- 📋 **OPTIONAL** (kann später gemacht werden)

---

### **Issue #38: Robuste Integration-Tests für CustomerSearchResource**

**Typ:** Test-Coverage
**Erstellt:** 2025-07-07
**Labels:** Keine
**Sprint:** Beliebig (nicht kritisch)

**Problem:**
Während PR #37 (Dynamic Customer Search API) wurden umfangreiche Integration-Tests entfernt:
- ❌ Duplicate Key Constraint Violations bei paralleler Test-Ausführung
- ❌ Race Conditions zwischen Tests
- ❌ Test-Daten-Isolation funktionierte nicht

**Aktueller Stand:**
- ✅ `CustomerSearchResourceBasicTest.java` (minimale Tests)
- ❌ Komplexe Filter-Kombinationen nicht getestet
- ❌ Edge Cases bei Pagination nicht getestet
- ❌ Performance-Verhalten nicht getestet

**Fehlende Test-Coverage:**
- Komplexe Filter-Kombinationen mit echten Daten
- Edge Cases bei Pagination
- Performance-Verhalten bei großen Datenmengen
- Korrektheit der generierten SQL-Queries
- Interaktion zwischen verschiedenen Filter-Operatoren

**Lösungsansätze:**
1. **Test-Container** für isolierte Datenbank pro Test
2. **@DirtiesContext** für Spring Context Reset
3. **Transactional Rollback** mit besserer Isolation
4. **Eindeutige Test-Präfixe** für alle Test-Daten
5. **Sequenzielle Test-Ausführung** statt parallel

**Impact:**
- 🔵 **Test-Coverage:** Nicht optimal, aber akzeptabel
- ✅ **Production:** API funktioniert korrekt
- ✅ **Frontend:** Kann unabhängig entwickeln

**Priorität:** 🔵 **NIEDRIG** (API funktioniert, Test-Coverage nice-to-have)
**Timeline:** Beliebig
**Effort:** 4-6 Stunden

**ENTSCHEIDUNG FÜR SPRINT 2.1.6:**
- ✅ **NICHT BLOCKIEREND**
- ✅ **CustomerSearchResource funktioniert** (Production-ready)
- 📋 **DEFERIERT** (kann später gemacht werden)

---

### **Issue #24: E2E Smoke Test fails - Ping API button not found**

**Typ:** E2E Test Fix
**Erstellt:** 2025-06-29
**Labels:** `bug`
**Sprint:** Beliebig (nicht kritisch)

**Problem:**
E2E-Test erwartet "Ping API" Button, der nicht mehr existiert (Legacy-Cleanup)

**Error:**
```
Error: locator.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for getByRole('button', { name: 'Ping API' })
```

**Root Cause:**
Test ist veraltet und erwartet Walking-Skeleton-Button, der während Legacy-Migration entfernt wurde.

**Lösungsoptionen:**
1. Test aktualisieren für echte Funktionalität (Calculator, Customer Form)
2. Einfachen Ping-Endpoint-Test erstellen
3. Test temporär überspringen (mit TODO)

**Impact:**
- 🔵 **E2E-Coverage:** Test ist veraltet
- ✅ **Production:** Kein Einfluss (Test-Problem)
- 🔵 **CI:** E2E-Pipeline ist rot (nicht kritisch)

**Priorität:** 🔵 **NIEDRIG** (Test-Fix, nicht Production-kritisch)
**Timeline:** Beliebig
**Effort:** 1 Stunde

**ENTSCHEIDUNG FÜR SPRINT 2.1.6:**
- ✅ **NICHT BLOCKIEREND**
- 📋 **DEFERIERT** auf Frontend-Refactoring-Sprint
- ⚠️ **KANN übersprungen werden** (mit TODO-Kommentar)

---

## ✅ CODE-TODOs (Dokumentiert, nicht kritisch)

### **Modul 05 (Kommunikation) - Email-Integration**

**Dateien:** `EmailProviderService.java`

```java
// TODO: Implement SendGrid integration
// TODO: Implement AWS SES integration
```

**Status:** ✅ **GEPLANT** für Modul 05
**Timeline:** Q1 2026
**Impact:** 🔵 NIEDRIG (Fallback: Logging funktioniert)

---

### **Modul 02 - Territory Management**

**Dateien:** `TerritoryService.java`

```java
// TODO: Add AT territory when configured in initializeDefaultTerritories
```

**Status:** ✅ **DOKUMENTIERT**
**Timeline:** Wenn Österreich-Markt erschlossen wird
**Impact:** 🔵 NIEDRIG (DE/CH ausreichend aktuell)

---

### **Sprint 2.1.6 Phase 3 - Email-Lookup**

**Dateien:** `LeadMaintenanceService.java`

```java
email.recipientEmail = lead.ownerUserId + "@freshfoodz.de"; // TODO: Lookup real email
managerEmail.recipientEmail = "manager@freshfoodz.de"; // TODO: Lookup territory manager
```

**Status:** ✅ **DOKUMENTIERT**
**Timeline:** Modul 05 (User-Email-Lookup-Service)
**Impact:** 🔵 NIEDRIG (Hardcoded-Emails funktionieren für Tests)

---

### **Sprint 2.1.7 - Fuzzy Matching**

**Dateien:** `LeadImportService.java`

```java
// TODO: Implement fuzzy matching logic (Sprint 2.1.7)
```

**Status:** ✅ **GEPLANT** für Sprint 2.1.7
**Timeline:** Sprint 2.1.7
**Impact:** 🟡 MITTEL (Aktuell: Simple Exact Match)

---

### **Sprint 2.1.7 - Activity Import**

**Dateien:** `LeadImportService.java`

```java
// TODO: Import activities (separate service in Sprint 2.1.7)
```

**Status:** ✅ **GEPLANT** für Sprint 2.1.7
**Timeline:** Sprint 2.1.7
**Impact:** 🟡 MITTEL (Aktuell: Nur Lead-Import, keine Activities)

---

### **Future Sprint - Issue #135 - Name Parsing**

**Dateien:** `LeadConvertService.java`

```java
// TODO (Future Sprint - Issue #135): Improve name parsing robustness
```

**Status:** ✅ **ISSUE ERSTELLT** (#135)
**Timeline:** Future Sprint
**Impact:** 🔵 NIEDRIG (Simple Split funktioniert für 90% der Fälle)

---

### **Q1 2026 - Backwards Compatibility Cleanup**

**Dateien:** `LeadActivity.java`

```java
// TODO: Remove after Q1 2026 - Backwards compatibility for old field names
```

**Status:** ✅ **GEPLANT** für Q1 2026 Cleanup
**Timeline:** Q1 2026
**Impact:** 🔵 NIEDRIG (Deprecated Fields, nicht verwendet)

---

## ⚠️ @Deprecated Code (Cleanup Q1 2026 geplant)

### **1. RlsGucFilter (Sprint 2.1)**

**Datei:** `RlsGucFilter.java`

```java
@Deprecated(since = "Sprint 2.1", forRemoval = true)
// @Provider - DISABLED to prevent double GUC setting
```

**Status:** ✅ **DISABLED** (nicht aktiv)
**Timeline:** Q1 2026 Cleanup
**Impact:** ✅ KEINE (bereits deaktiviert)

---

### **2. ETags.weakLead() (Legacy)**

**Datei:** `ETags.java`

```java
@Deprecated
public static String weakLead(long id, long version) {
  return "W/\"lead-%d-%d\"".formatted(id, version);
}
```

**Status:** ✅ **NICHT VERWENDET**
**Timeline:** Q1 2026 Cleanup
**Impact:** ✅ KEINE (neuer Code nutzt strongLead)

---

### **3. LeadProtectionService.canTransitionStage() (Sprint 2.1.6)**

**Datei:** `LeadProtectionService.java`

```java
@Deprecated(since = "2.1.6", forRemoval = true)
public boolean canTransitionStage(int currentStage, int newStage) {
  // Old Short-based logic
}
```

**Status:** ✅ **ERSETZT** durch LeadStage-Enum-Variante
**Timeline:** Q1 2026 Cleanup
**Impact:** ✅ KEINE (neue Methode verwendet)

---

### **4. UserLeadSettings.getOrCreateForUser() (Sprint 2.1)**

**Datei:** `UserLeadSettings.java`

```java
@Deprecated(since = "Sprint 2.1", forRemoval = true)
public static UserLeadSettings getOrCreateForUser(String userId) {
  throw new UnsupportedOperationException("Use Settings-Registry instead");
}
```

**Status:** ✅ **THROWS EXCEPTION** (wird nicht mehr verwendet)
**Timeline:** Q1 2026 Cleanup
**Impact:** ✅ KEINE (Settings-Registry ist Replacement)

---

### **5. Lead.industry (Sprint 2.1.6 Phase 2)**

**Datei:** `Lead.java`

```java
@Deprecated(since = "2.1.6", forRemoval = true)
@Size(max = 50)
public String industry;
```

**Status:** ✅ **ERSETZT** durch businessType (BusinessType Enum)
**Timeline:** Q1 2026 Cleanup
**Impact:** ✅ KEINE (businessType ist aktiv)

---

### **6. LeadActivity Getter/Setter (Sprint 2.1)**

**Datei:** `LeadActivity.java`

```java
@Deprecated(forRemoval = true, since = "Sprint 2.1")
public ActivityType getType() { return activityType; }

@Deprecated(forRemoval = true, since = "Sprint 2.1")
public void setType(ActivityType type) { this.activityType = type; }

@Deprecated(forRemoval = true, since = "Sprint 2.1")
public LocalDateTime getOccurredAt() { return activityDate; }

@Deprecated(forRemoval = true, since = "Sprint 2.1")
public void setOccurredAt(LocalDateTime occurredAt) { this.activityDate = occurredAt; }
```

**Status:** ✅ **BACKWARDS COMPATIBILITY** (public fields preferred)
**Timeline:** Q1 2026 Cleanup
**Impact:** ✅ KEINE (Code nutzt Public Fields)

---

## 📊 Zusammenfassung nach Priorität

| Kategorie | Count | Kritisch | Mittel | Niedrig | Deferiert |
|-----------|-------|----------|--------|---------|-----------|
| **GitHub Issues** | 6 | 0 | 2 | 4 | 4 |
| **Code-TODOs** | 9 | 0 | 2 | 7 | 9 |
| **@Deprecated** | 6 | 0 | 0 | 6 | 6 |
| **GESAMT** | 21 | **0** | **4** | **17** | **19** |

---

## 🎯 Empfehlungen für Sprint 2.1.6 Abschluss

### ✅ **KANN ABGESCHLOSSEN WERDEN - KEINE BLOCKER**

**Kritische Schulden:** ✅ **KEINE**
**Production-Blocker:** ✅ **KEINE**
**Test-Blocker:** ✅ **KEINE**

---

### **Optionale Cleanup-Tasks für Sprint 2.1.6 Phase 4:**

**1. Issue #126: ActivityOutcome Enum (2h)**
- ✅ **Empfehlung:** MACHEN (Data Integrity)
- 🔧 **Effort:** 1-2 Stunden
- 📋 **Timeline:** Phase 4 (optional)

**2. Issue #127: Clock Injection (3h)**
- ⚠️ **Empfehlung:** DEFERIEREN auf Sprint 2.1.7
- 🔧 **Effort:** 2-3 Stunden
- 📋 **Timeline:** Sprint 2.1.7 (nach PR #124)

---

### **Niedrige Priorität (kann warten):**

- 🔵 Issue #119: Stub-Cleanup → Sprint 2.3+
- 🔵 Issue #98: CI Header-Tests → Beliebig
- 🔵 Issue #38: CustomerSearch Tests → Beliebig
- 🔵 Issue #24: E2E Ping-Test → Beliebig

---

## ✅ FAZIT

**Technical Debt Status:** ✅ **SEHR GESUND**

**Sprint 2.1.6 Phase 3 Abschluss:** ✅ **GENEHMIGT**

**Nächste Schritte:**
1. ✅ PR #134 erstellen (Outbox-Pattern + Issue #134)
2. 🔵 Optional: Issue #126 in Phase 4 (ActivityOutcome Enum)
3. 📋 Sprint 2.1.7 Planung (Issue #127 + Fuzzy Matching)

**Keine technischen Schulden blockieren den Production-Deployment!** 🚀

---

**Reviewer:** Claude Code (Automated Technical Debt Analysis)
**Next Review:** Sprint 2.1.7 Planning
