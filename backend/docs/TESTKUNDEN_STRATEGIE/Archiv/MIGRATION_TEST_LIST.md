# TestDataBuilder Migration - Konkrete Test-Liste

**Stand: 18.08.2025**  
**Automatisch generiert mit grep-Suche**

## 📊 Zusammenfassung

- **12 Tests** mit `new Customer()`
- **12 Tests** mit `new Opportunity()`  
- **20 Tests** mit `new User()`
- **Gesamt: 34 unique Test-Dateien** (einige verwenden mehrere Konstruktoren)

---

## 🔴 Tests mit `new Customer()` - 12 Dateien

```
□ de.freshplan.domain.cockpit.service.query.SalesCockpitQueryServiceTest
□ de.freshplan.domain.customer.entity.CustomerContactTest
□ de.freshplan.domain.customer.service.command.CustomerCommandServiceTest
□ de.freshplan.domain.customer.service.query.ContactQueryServiceTest
□ de.freshplan.domain.customer.service.timeline.command.TimelineCommandServiceTest
□ de.freshplan.domain.customer.service.timeline.query.TimelineQueryServiceTest
□ de.freshplan.domain.export.service.query.HtmlExportQueryServiceTest
□ de.freshplan.domain.opportunity.entity.OpportunityEntityStageTest
□ de.freshplan.domain.search.service.query.SearchQueryServiceTest
□ de.freshplan.domain.search.service.SearchServiceTest
□ de.freshplan.test.TestDataBuilder (LEGACY)
□ de.freshplan.testsupport.TestFixtures (LEGACY)
```

---

## 🟠 Tests mit `new Opportunity()` - 12 Dateien

```
□ de.freshplan.api.resources.OpportunityResourceIntegrationTest
□ de.freshplan.domain.opportunity.entity.OpportunityEntityStageTest
□ de.freshplan.domain.opportunity.entity.OpportunityStageTest
□ de.freshplan.domain.opportunity.repository.OpportunityDatabaseIntegrationTest
□ de.freshplan.domain.opportunity.repository.OpportunityRepositoryBasicTest
□ de.freshplan.domain.opportunity.service.command.OpportunityCommandServiceTest
□ de.freshplan.domain.opportunity.service.mapper.OpportunityMapperTest
□ de.freshplan.domain.opportunity.service.OpportunityServiceMockTest
□ de.freshplan.domain.opportunity.service.OpportunityServiceStageTransitionTest
□ de.freshplan.domain.opportunity.service.query.OpportunityQueryServiceTest
□ de.freshplan.test.TestDataBuilder (LEGACY)
□ de.freshplan.testsupport.TestFixtures (LEGACY)
```

---

## 🔵 Tests mit `new User()` - 20 Dateien

```
□ de.freshplan.api.resources.OpportunityResourceIntegrationTest
□ de.freshplan.api.UserResourceIT
□ de.freshplan.api.UserRolesIT
□ de.freshplan.domain.cockpit.service.SalesCockpitServiceIntegrationTest
□ de.freshplan.domain.opportunity.entity.OpportunityEntityStageTest
□ de.freshplan.domain.opportunity.repository.OpportunityDatabaseIntegrationTest
□ de.freshplan.domain.opportunity.repository.OpportunityRepositoryBasicTest
□ de.freshplan.domain.opportunity.service.command.OpportunityCommandServiceTest
□ de.freshplan.domain.opportunity.service.mapper.OpportunityMapperTest
□ de.freshplan.domain.opportunity.service.query.OpportunityQueryServiceTest
□ de.freshplan.domain.user.entity.UserTest
□ de.freshplan.domain.user.repository.UserRepositoryTest
□ de.freshplan.domain.user.service.command.UserCommandServiceTest
□ de.freshplan.domain.user.service.mapper.UserMapperTest
□ de.freshplan.domain.user.service.query.UserQueryServiceTest
□ de.freshplan.domain.user.service.UserServiceCQRSIntegrationTest
□ de.freshplan.domain.user.service.UserServiceRolesTest
□ de.freshplan.domain.user.service.UserServiceTest
□ de.freshplan.greenpath.UserRepoSaveLoadIT
□ de.freshplan.test.TestDataInitializer
```

---

## 🎯 Migration Quick-Wins (Einfach zu migrieren)

Diese Tests haben nur wenige direkte Konstruktor-Aufrufe und können schnell migriert werden:

### Sehr einfach (< 5 Konstruktor-Aufrufe):
1. `CustomerContactTest` - Entity Test, wenige Aufrufe
2. `OpportunityStageTest` - Enum/Stage Test
3. `UserTest` - Entity Test
4. `UserMapperTest` - Mapper Test

### Einfach (5-10 Konstruktor-Aufrufe):
5. `ContactQueryServiceTest` - Query Service
6. `SearchQueryServiceTest` - Search Service
7. `HtmlExportQueryServiceTest` - Export Service
8. `OpportunityMapperTest` - Mapper Test

---

## 🔥 Migration Hot-Spots (Viele Änderungen nötig)

Diese Tests haben viele direkte Konstruktor-Aufrufe und benötigen mehr Zeit:

### Komplex (> 20 Konstruktor-Aufrufe):
1. `UserServiceTest` - Hauptservice mit vielen Tests
2. `OpportunityServiceMockTest` - Mock-basierter Test
3. `CustomerCommandServiceTest` - Command Service
4. `OpportunityCommandServiceTest` - Command Service
5. `UserCommandServiceTest` - Command Service

### Sehr komplex (> 30 Konstruktor-Aufrufe):
6. `OpportunityResourceIntegrationTest` - API Integration Test
7. `UserResourceIT` - API Integration Test
8. `SalesCockpitServiceIntegrationTest` - Dashboard Service

---

## 📈 Priorisierte Migrations-Reihenfolge

### Woche 1 - Quick Wins (8 Tests)
```
Tag 1-2: Entity Tests
✅ CustomerContactTest
✅ OpportunityStageTest  
✅ UserTest
✅ OpportunityEntityStageTest

Tag 3-4: Mapper Tests
✅ UserMapperTest
✅ OpportunityMapperTest

Tag 5: Query Services
✅ ContactQueryServiceTest
✅ SearchQueryServiceTest
```

### Woche 2 - Command Services (6 Tests)
```
Tag 1-2:
✅ CustomerCommandServiceTest
✅ UserCommandServiceTest

Tag 3-4:
✅ OpportunityCommandServiceTest
✅ TimelineCommandServiceTest

Tag 5:
✅ OpportunityQueryServiceTest
✅ UserQueryServiceTest
```

### Woche 3 - Integration Tests (8 Tests)
```
Tag 1-2:
✅ OpportunityResourceIntegrationTest
✅ UserResourceIT

Tag 3-4:
✅ UserRolesIT
✅ SalesCockpitServiceIntegrationTest

Tag 5:
✅ OpportunityServiceMockTest
✅ OpportunityServiceStageTransitionTest
```

### Woche 4 - Services & Cleanup (12 Tests)
```
Tag 1-3:
✅ UserServiceTest
✅ UserServiceRolesTest
✅ UserServiceCQRSIntegrationTest
✅ SearchServiceTest
✅ SalesCockpitQueryServiceTest

Tag 4-5:
✅ TimelineQueryServiceTest
✅ HtmlExportQueryServiceTest
✅ OpportunityDatabaseIntegrationTest
✅ OpportunityRepositoryBasicTest
✅ UserRepositoryTest
✅ UserRepoSaveLoadIT
✅ TestDataInitializer
```

---

## 🛠️ Migrations-Skript Ideen

Für häufige Patterns können wir Regex-Replace verwenden:

```bash
# Beispiel: Customer Migration
sed -i 's/new Customer()/customerBuilder.build()/g' TestFile.java

# Beispiel: User mit Parametern
sed -i 's/new User(\([^)]*\))/userBuilder.withUsername(\1).build()/g' TestFile.java

# Beispiel: Opportunity
sed -i 's/new Opportunity()/opportunityBuilder.build()/g' TestFile.java
```

---

## 📊 Fortschritts-Dashboard

```
═══════════════════════════════════════════════════════
GESAMT: 34 Tests
═══════════════════════════════════════════════════════

Customer Tests:  [░░░░░░░░░░░░] 0/12 (0%)
Opportunity:     [░░░░░░░░░░░░] 0/12 (0%)  
User Tests:      [░░░░░░░░░░░░░░░░░░░░] 0/20 (0%)

═══════════════════════════════════════════════════════
Nach Priorität:
═══════════════════════════════════════════════════════

🔴 KRITISCH:     [░░░░░░░░░░░░] 0/12
🟡 MITTEL:       [░░░░░░░░░░░░░░░] 0/15
🟢 NIEDRIG:      [░░░░░░░] 0/7

═══════════════════════════════════════════════════════
Geschätzter Aufwand: 40-60 Stunden
═══════════════════════════════════════════════════════
```

---

## 🚀 Nächste Schritte

1. **Sofort**: Diese Liste im Team-Meeting besprechen
2. **Morgen**: Mit Quick-Wins starten (Entity & Mapper Tests)
3. **Diese Woche**: Mindestens 8 Tests migrieren
4. **Ziel**: 2 Tests pro Tag = Fertig in 3-4 Wochen

---

**Automatisch generiert am**: 18.08.2025  
**Durch**: Claude mit grep-Analyse  
**Validiert**: ❌ Noch nicht