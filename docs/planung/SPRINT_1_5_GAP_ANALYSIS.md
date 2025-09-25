# 🔍 Sprint 1.5 RLS Gap Analysis - Module & Services

**Datum:** 2025-09-25
**Sprint:** 1.5 Security Retrofit
**Zweck:** Identifikation aller Services, die @RlsContext noch nicht verwenden

## 📊 Executive Summary

Nach Sprint 1.5 müssen **ALLE** Services mit DB-Zugriff die `@RlsContext` Annotation verwenden.
Aktueller Stand: **7 von ~40+ Services** haben @RlsContext implementiert.

## ✅ Bereits angepasste Services (mit @RlsContext)

### Infrastructure
- ✅ `UniversalExportService`
- ✅ `SettingsService`
- ✅ `EventPublisher`
- ✅ `RlsHealthCheckResource`

### Module Leads
- ✅ `LeadResource` (API Layer)
- ✅ `RlsGucFilter`

## ⚠️ Services OHNE @RlsContext (müssen angepasst werden)

### 🎯 Modul 02: Neukundengewinnung (Leads)
**Services die @Transactional haben aber KEIN @RlsContext:**
- ❌ `LeadService` - processReminders(), processGracePeriod(), processExpirations(), reactivateLead()
- ❌ `UserLeadSettingsService` - getOrCreateForUser(), updateSettings(), deleteSettings()
- ❌ `TerritoryService` - initializeDefaultTerritories()
- ❌ `EmailActivityDetectionService` - createEmailActivity()
- ❌ `LeadProtectionService` - Alle DB-Zugriffe

**Impact:** HOCH - Dies ist das aktive Sprint 2.1 Modul!

### 🎯 Modul 03: Kundenmanagement
**Services die @Transactional haben:**
- ❌ `CustomerService` - Mehrere Methoden mit @Transactional
- ❌ `CustomerNumberGeneratorService` - generateCustomerNumber()
- ❌ `ContactService` - Klassen-Level @Transactional
- ❌ `CustomerChainService` - Klassen-Level @Transactional
- ❌ `CustomerTimelineService` - Klassen-Level @Transactional
- ❌ `DataHygieneService` - Klassen-Level @Transactional
- ❌ `TimelineCommandService` - Mehrere Methoden

**Query Services (Read-only, aber prüfen ob RLS nötig):**
- ⚠️ `ContactQueryService` - Explizit KEIN @Transactional
- ⚠️ `TimelineQueryService` - Explizit KEIN @Transactional
- ⚠️ `ContactInteractionQueryService` - Explizit KEIN @Transactional

### 🎯 Domain Services (verschiedene Module)
- ❌ `UserService` / `UserCommandService` / `UserQueryService`
- ❌ `TestDataService` / `TestDataCommandService` / `TestDataQueryService`
- ❌ `SearchService` / `SearchQueryService`
- ❌ `ProfileService` / `ProfileCommandService` / `ProfileQueryService`
- ❌ `OpportunityService` / `OpportunityCommandService` / `OpportunityQueryService`

## 🔧 Erforderliche Arbeiten

### Phase 1: KRITISCH - Sprint 2.1 Module (SOFORT)
```java
// Beispiel-Anpassung für LeadService
@ApplicationScoped
@Transactional  // Klassen-Level bleibt
public class LeadService {

    @RlsContext  // NEU: Für jede DB-Zugriff Methode
    public int processReminders() {
        // Existing code
    }
}
```

**Betroffene Services (PRIORITÄT HOCH):**
1. `LeadService` - 4 Methoden
2. `UserLeadSettingsService` - 3 Methoden
3. `TerritoryService` - 1 Methode
4. `EmailActivityDetectionService` - 1 Methode
5. `LeadProtectionService` - Prüfen

**Geschätzter Aufwand:** 2-3h

### Phase 2: Modul 03 Kundenmanagement (Sprint 2.2)
**Betroffene Services:**
- 7 Command Services
- 3 Query Services (prüfen ob RLS nötig)

**Geschätzter Aufwand:** 3-4h

### Phase 3: Domain Services (Sprint-übergreifend)
**Betroffene Services:**
- 15+ Domain Services

**Geschätzter Aufwand:** 4-5h

## 🚨 Risiken ohne Anpassung

1. **Connection Affinity Probleme:** GUC-Variablen landen auf falschen Connections
2. **Territory Leaks:** User A könnte Daten von Territory B sehen
3. **Fail-closed verletzt:** System sollte bei fehlendem Context keine Daten zurückgeben
4. **Inkonsistente Security:** Manche Services geschützt, andere nicht

## 📋 Migration Checklist

### Für jeden Service:
- [ ] Import hinzufügen: `import de.freshplan.infrastructure.security.RlsContext;`
- [ ] @RlsContext zu allen DB-Zugriff Methoden
- [ ] Tests erweitern mit @TestSecurity und @Transactional
- [ ] Verify: EntityManager Zugriffe nur in @RlsContext Methoden
- [ ] Integration Tests ausführen

### Test-Pattern:
```java
@Test
@TestSecurity(user = "test-user", roles = "user")
@Transactional
void testWithRls() {
    // Service method call
    // Assertions
}
```

## 🎯 Empfehlung

**SOFORT (vor Sprint 2.1 Fortsetzung):**
1. Alle Lead-Module Services mit @RlsContext annotieren
2. Integration Tests ausführen
3. PR erstellen mit "fix: Add @RlsContext to lead services"

**GEPLANT (Sprint 2.2):**
- Customer-Module Services systematisch migrieren
- Query Services evaluieren (brauchen sie RLS?)

**LANGFRISTIG:**
- Alle Domain Services migrieren
- CI-Check für neue Services ohne @RlsContext

## 📊 Tracking

| Modul | Services Total | Mit @RlsContext | Ohne @RlsContext | Status |
|-------|---------------|-----------------|------------------|--------|
| Infrastructure | 4 | 4 | 0 | ✅ |
| 02 Leads | 5 | 5 | 0 | ✅ FIXED (Sprint 1.6) |
| 03 Kunden | 10 | 0 | 10 | ⚠️ Geplant |
| Domain | 15+ | 0 | 15+ | 📋 Backlog |

**Gesamt:** ~40 Services, davon 7 (17%) migriert

## Next Steps

1. **FP-272:** Lead-Services mit @RlsContext annotieren (P0)
2. **FP-273:** Customer-Services Migration planen
3. **FP-274:** CI-Rule für @RlsContext bei @Transactional