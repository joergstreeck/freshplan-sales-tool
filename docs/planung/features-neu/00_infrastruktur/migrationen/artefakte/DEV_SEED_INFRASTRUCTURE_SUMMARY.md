# DEV-SEED Infrastructure + Frontend Bugfixes - Session Summary

**📅 Datum:** 2025-10-13
**⏱️ Dauer:** ~5 Stunden
**👤 Owner:** Jörg + Claude
**🎯 Typ:** DEV Infrastructure + Bugfixing
**✅ Status:** COMPLETE

---

## 🎯 Ziel der Session

**Primäres Ziel:**
- DEV-SEED Infrastruktur aufbauen mit realistischen Testdaten (Customers + Leads)
- Frontend-Bugfixes für Auto-Save Race Condition und Auth Bypass

**Sekundäres Ziel:**
- Neue Migration-Strategie etablieren (db/dev-seed/ Folder)
- Production-ähnliche Testdaten für bessere Entwicklungserfahrung

---

## ✅ Deliverables

### 1. DEV-SEED Migrations (Production-Ready)

#### V90001: Seed DEV Customers Complete
**Pfad:** `backend/src/main/resources/db/dev-seed/V90001__seed_dev_customers_complete.sql`

**Inhalt:**
- 5 realistische Customer-Szenarien (IDs 90001-90005)
- Verschiedene Branchen: Hotel, Catering, Betriebskantine, Restaurant, Bäckerei
- Verschiedene Größen: 50-500 Mitarbeiter
- Territory-Zuordnungen: Berlin, München, Freiburg, Hamburg, Dresden
- BusinessTypes: GASTRONOMIE, CATERING, GEMEINSCHAFTSVERPFLEGUNG, BAECKEREI
- Vollständige Daten: Adressen, Kontaktpersonen, Notes, Email/Phone

**Szenarien:**
1. **90001** - Fresh Hotel GmbH (Berlin, Hotel, 200 MA)
2. **90002** - Catering Excellence AG (München, Catering, 150 MA)
3. **90003** - Campus Gastro Service (Freiburg, Betriebskantine, 500 MA)
4. **90004** - Restaurant Bella Vista (Hamburg, Restaurant, 50 MA)
5. **90005** - Bäckerei Müller KG (Dresden, Bäckerei, 80 MA)

#### V90002: Seed DEV Leads Complete
**Pfad:** `backend/src/main/resources/db/dev-seed/V90002__seed_dev_leads_complete.sql`

**Inhalt:**
- 10 Lead-Szenarien (IDs 90001-90010)
- 21 Lead Contacts (0-3 pro Lead, verteilt)
- 21 Lead Activities (CREATED + Business Activities)
- Score-Range: 21-59 (System Ceiling validiert)
- Verschiedene Stati: REGISTERED, LEAD, LOST, INTERESSENT
- Edge Cases abgedeckt: PreClaim, Grace Period, LOST

**Hot Leads:**
- **90003** - Hotel Seeblick (Score 59): ADVOCATE Relationship + EMERGENCY Urgency
- **90007** - Uni Mensa Frankfurt (Score 57): DIRECT Decision Maker + EMERGENCY Urgency

**Edge Cases:**
- **90006** - Catering BüroFit (PreClaim): Stage 0, keine Contacts, first_contact_documented_at = NULL
- **90005** - Bistro Alpenblick (Grace Period): 68 Tage alt, kritischer Protection-Status
- **90004** - Mensa Köln-Nord (LOST): Competitor Won, lost_reason gesetzt

**Score-Verteilung:**
- 21-30 Points: 3 Leads (Cold/Low Engagement)
- 31-40 Points: 3 Leads (Moderate Fit)
- 41-50 Points: 2 Leads (Good Potential)
- 51-59 Points: 2 Leads (Hot Leads)

### 2. Frontend Bugfixes (3 kritische Bugs)

#### Bug 1: Auto-Save Race Condition (409 Conflict)

**Problem:**
- React StrictMode mounted Components zweimal → beide Mounts triggerten Auto-Save
- OptimisticLockException → 409 Conflict Error
- User Experience: Fehlermeldung beim Lead Detail Page Load

**Root Cause:**
```typescript
// VORHER: Auto-Save wurde IMMER bei useEffect getriggert
useEffect(() => {
  autoSave(); // FEHLER: Auch beim Mount!
}, [formData]);
```

**Fix:**
```typescript
// NACHHER: Explizite Change-Detection
useEffect(() => {
  const hasChanges =
    formData.estimatedVolume !== (lead.estimatedVolume || null) ||
    formData.budgetConfirmed !== (lead.budgetConfirmed || false) ||
    formData.dealSize !== (lead.dealSize || '');

  if (hasChanges) {
    autoSave(true); // Nur bei tatsächlichen Änderungen!
  }
}, [formData]);
```

**Betroffene Dateien:**
- `frontend/src/features/leads/components/RevenueScoreForm.tsx`
- `frontend/src/features/leads/components/PainScoreForm.tsx`
- `frontend/src/features/leads/components/EngagementScoreForm.tsx`

#### Bug 2: Auth Bypass Permission Failure

**Problem:**
- Stop-the-Clock Button zeigte "Keine Berechtigung" im DEV-Mode
- User konnte Feature nicht testen trotz Auth-Bypass

**Root Cause:**
```typescript
// AuthContext.tsx - Mock-User hatte lowercase Rollen
roles: ['admin', 'manager', 'sales']

// StopTheClockDialog.tsx - Check prüfte UPPERCASE
const canStopClock = hasRole('ADMIN') || hasRole('MANAGER'); // FALSE!
```

**Fix:**
```typescript
// AuthContext.tsx - Rollen auf UPPERCASE + case-insensitive hasRole()
roles: ['ADMIN', 'MANAGER', 'SALES'],
hasRole: (role: string) => ['ADMIN', 'MANAGER', 'SALES'].includes(role.toUpperCase()),
```

**ENV-Konfiguration:**
```env
# .env.development
VITE_USE_KEYCLOAK_IN_DEV=false  # Keycloak deaktivieren
VITE_AUTH_BYPASS=true            # Mock-User aktivieren
```

#### Bug 3: GRACE_PERIOD Translation

**Problem:**
- Lead-Stage "GRACE_PERIOD" hatte keine deutsche Übersetzung
- UI zeigte englischen Enum-Namen statt lesbare Bezeichnung

**Fix:**
```typescript
// frontend/src/features/leads/types.ts
export const LEAD_STAGE_LABELS: Record<LeadStage, string> = {
  // ... existing stages
  GRACE_PERIOD: 'Schonfrist',
};
```

### 3. Backend Error Handling

#### OptimisticLockException → 409 Conflict

**Datei:** `backend/src/main/java/de/freshplan/api/exception/GlobalExceptionMapper.java`

**Änderung:**
```java
// Added proper handling for OptimisticLockException
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof OptimisticLockException) {
            return Response.status(Response.Status.CONFLICT)
                .entity(Map.of(
                    "status", 409,
                    "title", "Conflict",
                    "detail", "Resource was modified by another user. Please reload and try again."
                ))
                .build();
        }
        // ... existing handling
    }
}
```

**Benefit:** Proper HTTP status code statt 500 Internal Server Error

#### RLS Connection Affinity Guard

**Datei:** `backend/src/main/java/de/freshplan/infrastructure/security/RlsConnectionAffinityGuard.java`

**Änderung:** Verbesserte SUPERUSER detection für DEV-SEED Migrations

#### Application Properties

**Datei:** `backend/src/main/resources/application.properties`

**Änderung:** RLS fallback user für DEV-SEED

---

## 🏗️ Neue Migration-Strategie

### db/dev-seed/ Folder

**Entscheidung:**
- DEV-SEED Migrations in separatem Folder: `db/dev-seed/`
- Nummerierung: V90000+ (deutlich erkennbar als DEV-only)
- Flyway lädt automatisch aus beiden Folders

**Vorteile:**
- ✅ Klare Trennung Production vs. DEV-only Daten
- ✅ DEV-SEED Daten können einfach ausgeschlossen werden (Production)
- ✅ Bessere Organisation und Wartbarkeit
- ✅ Keine Konflikte mit Production Migrations (V10000+)

**Flyway Configuration:**
```properties
quarkus.flyway.locations=classpath:db/migration,classpath:db/dev-seed
```

---

## 📊 Statistiken

### Migrations
- **V90001:** ~200 LOC (5 Customers + Contacts + Notes)
- **V90002:** ~600 LOC (10 Leads + 21 Contacts + 21 Activities)
- **Total:** ~800 LOC DEV-SEED Daten

### Frontend
- **3 Bugs fixed:** Auto-Save Race Condition, Auth Bypass, GRACE_PERIOD Translation
- **3 Score-Forms refactored:** Explizite Change-Detection hinzugefügt
- **0 Console-Errors:** Keine Fehler mehr im DEV-Mode

### Backend
- **1 Error Handler:** OptimisticLockException → 409 Conflict
- **1 Guard:** RlsConnectionAffinityGuard SUPERUSER detection
- **1 Config:** application.properties RLS fallback user

---

## 🧪 Tests & Validation

### DEV-SEED Validation
- ✅ **Score-Range:** 21-59 (System Ceiling confirmed)
- ✅ **Hot Leads:** 90003 (59), 90007 (57) korrekt berechnet
- ✅ **Edge Cases:** PreClaim, Grace Period, LOST funktional
- ✅ **UI:** Alle 10 Leads sichtbar in Frontend
- ✅ **Links:** Klickbare Lead-Links funktionieren

### Bugfix Validation
- ✅ **Auto-Save:** Keine 409 Conflicts mehr bei Lead Detail Page Load
- ✅ **Auth Bypass:** Stop-the-Clock Button funktioniert im DEV-Mode
- ✅ **Translation:** GRACE_PERIOD zeigt "Schonfrist" statt Enum-Namen
- ✅ **Console:** Keine Errors mehr

---

## 📝 Commit & Documentation

### Commit
**Commit:** 8884e2cb7
**Message:** "fix: Frontend Bugfixes - Auto-Save Race Condition + Auth Bypass + UI Fixes"

**Umfang:**
- 14 files changed
- 2944 insertions(+)
- 68 deletions(-)

**Breakdown:**
- 2 neue Migrations (V90001, V90002)
- 3 Frontend Bugfixes (RevenueScoreForm, PainScoreForm, EngagementScoreForm)
- 1 Backend Error Handler (GlobalExceptionMapper)
- 1 Backend Guard (RlsConnectionAffinityGuard)
- 1 Config (application.properties)
- 1 Translation (types.ts)
- 1 ENV Configuration (.env.development)

### Documentation Updates
- ✅ Master Plan V5: Session Log + Next Steps
- ✅ Production Roadmap: Sprint 2.1.6.2 hinzugefügt
- ✅ DOCUMENTATION_CHECKLIST: Befolgt (Master Plan, Roadmap)

---

## 🎯 Lessons Learned

### React StrictMode & Auto-Save
**Problem:** Component wird zweimal gemounted → doppeltes Auto-Save
**Lösung:** Explizite Change-Detection vor Auto-Save
**Pattern:** `useEffect` mit Change-Detection + `isFirstRenderRef`

### Auth Bypass Case Sensitivity
**Problem:** Rollen lowercase, aber Check UPPERCASE
**Lösung:** Case-insensitive `hasRole()` mit `.toUpperCase()`
**Pattern:** Rollen immer UPPERCASE speichern

### DEV-SEED Organization
**Problem:** Wo speichert man DEV-only Testdaten?
**Lösung:** Separater Folder `db/dev-seed/` mit V90000+ Nummerierung
**Pattern:** Klare Trennung Production vs. DEV-only

### Migration Safety
**Problem:** Wie verhindert man Migration-Fehler?
**Lösung:** 3-Layer Safety System (Pre-Commit Hook, GitHub Workflow, get-next-migration.sh)
**Pattern:** Mehrschichtige Validierung

---

## 🚀 Next Steps

### Empfohlener nächster Schritt
**Option 1: Sprint 2.1.7 Track 0 - Warm-Up Refactorings** (optional, ~30min)
- useBusinessTypes Hook zu /hooks/ verschieben
- Vorbereitung für Track 2 Test Infrastructure

**Option 2: Sprint 2.1.7 - Team Management & Test Infrastructure** (Start 19.10.2025)
- Track 1: Business Features (Lead-Transfer, RLS, Team Management, Fuzzy-Matching)
- Track 2: Test Infrastructure Overhaul (Szenario-Builder, Faker, Test-Patterns)
- Track 3: Code Quality (Name Parsing, EnumResource Refactoring)

**Option 3: Sprint 2.2 - Kundenmanagement** (Field-based Architecture)
- 39 Production-Ready Artefakte verfügbar
- Multi-Contact Support nutzt Security/Performance Patterns aus Sprint 2.1

---

## 📋 Cross-References

**Dokumentation:**
- [Master Plan V5](../../../../../CRM_COMPLETE_MASTER_PLAN_V5.md) - Session Log Zeile 164-197
- [Production Roadmap](../../../../../PRODUCTION_ROADMAP_2025.md) - Sprint 2.1.6.2 Zeile 186-205
- [TRIGGER_SPRINT_2_1_7.md](../../../../../TRIGGER_SPRINT_2_1_7.md) - Track 0 Warm-Up
- [TRIGGER_INDEX.md](../../../../../TRIGGER_INDEX.md) - Sprint Overview

**Code:**
- [V90001 Migration](../../../../../../../backend/src/main/resources/db/dev-seed/V90001__seed_dev_customers_complete.sql)
- [V90002 Migration](../../../../../../../backend/src/main/resources/db/dev-seed/V90002__seed_dev_leads_complete.sql)
- [DEV-SEED README](../../../../../../../backend/src/main/resources/db/dev-seed/README.md) - Migration Strategy Documentation
- [GlobalExceptionMapper.java](../../../../../../../backend/src/main/java/de/freshplan/api/exception/GlobalExceptionMapper.java)
- [AuthContext.tsx](../../../../../../../frontend/src/contexts/AuthContext.tsx)

**Commits:**
- 8884e2cb7 - fix: Frontend Bugfixes - Auto-Save Race Condition + Auth Bypass + UI Fixes

---

**✅ Session erfolgreich abgeschlossen!**

**Highlights:**
- 🎉 DEV-SEED Infrastructure komplett
- 🐛 3 kritische Frontend-Bugs behoben
- 📚 Neue Migration-Strategie etabliert
- 📊 Score-System validiert (21-59 Ceiling confirmed)
- 🏗️ Production-ähnliche Testdaten verfügbar
- 📖 DEV-SEED README erstellt

**Nächste Claude-Instanz:**
1. Lese [TRIGGER_INDEX.md](../../../../../TRIGGER_INDEX.md) für aktuellen Sprint
2. Lese [CRM_COMPLETE_MASTER_PLAN_V5.md](../../../../../CRM_COMPLETE_MASTER_PLAN_V5.md) Session Log (neueste Einträge)
3. Entscheide: Sprint 2.1.7 Track 0 (Warm-Up) oder Sprint 2.1.7 Full (Team Management)
