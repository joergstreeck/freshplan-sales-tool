# 🚀 SPRINT 1.4: FOUNDATION QUICK-WINS - CACHE & PROD-HÄRTUNG

## ⚠️ WICHTIGE QUALITÄTSREGELN

**HINWEIS:** Dieser Sprint entstand aus der externen KI-Review vom 24.09.2025.
- Behebt die letzten operativen Lücken der Phase 1 Foundation
- Minimales Risiko, maximaler Nutzen
- Keine Business-Logic-Änderungen

## 📋 DOKUMENTE-VALIDIERUNG (PFLICHT-REIHENFOLGE)

**1. ROADMAP-ORIENTIERUNG:**
Lies: `./docs/planung/PRODUCTION_ROADMAP_2025.md`
- Phase 1 Foundation ist zu 95% complete
- Dieser Sprint bringt sie auf 100%

**2. ARBEITSREGELN:**
Lies: `./docs/CLAUDE.md`
- Die 17 kritischen Regeln beachten

**3. VALIDATION REPORT:**
Lies: `./docs/planung/claude-work/daily-work/2025-09-24/2025-09-24_SPRINT_VALIDATION_REPORT.md`
- Externe KI-Review Erkenntnisse
- Berechtigte Quick-Wins identifiziert

**4. MASTER PLAN V5:**
Lies: `./docs/planung/CRM_COMPLETE_MASTER_PLAN_V5.md`
- Foundation-Status prüfen

## 🔒 MIGRATION-CHECK

**Keine Migration notwendig** - nur Application-Level-Änderungen

## 🎯 IMPLEMENTIERUNGS-AUFTRAG

**SPRINT:** Sprint 1.4: Foundation Quick-Wins
**GESCHÄTZTE ARBEITSZEIT:** 1-2 Stunden
**PR:** feature/sprint-1-4-quickwins-cache-prod-v229

**SCOPE (genau 4 Punkte):**
1. Quarkus-Cache für Settings-Service aktivieren
2. Prod-Configs explizit härten
3. Cache-Invalidierung implementieren
4. Verifikationstest hinzufügen

**SUCCESS-CRITERIA:**
- [ ] Settings-Service mit Cache-Annotations versehen
- [ ] Prod-Defaults explizit in application-prod.properties
- [ ] Cache-Invalidierung bei Writes funktional
- [ ] Test für Cache-Invalidierung grün
- [ ] Performance-Improvement messbar

## 🚀 IMPLEMENTIERUNGS-SCHRITTE

### 1. BRANCH ERSTELLEN
```bash
git checkout main && git pull
git checkout -b feature/sprint-1-4-quickwins-cache-prod-v229
```

### 2. QUARKUS-CACHE DEPENDENCY
**backend/pom.xml:**
```xml
<!-- Phase-1 Quick Win: Settings caching -->
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-cache</artifactId>
</dependency>
```

### 3. CACHE-KONFIGURATION
**backend/src/main/resources/application.properties:**
```properties
# Settings cache (Caffeine) - Dev/Test
quarkus.cache.caffeine."settings-cache".maximum-size=5000
quarkus.cache.caffeine."settings-cache".expire-after-write=5M
```

**backend/src/main/resources/application-prod.properties:**
```properties
# Explizite Prod-Defaults
app.default.org-id=freshfoodz
app.default.territory=DE

# Settings cache in Prod
quarkus.cache.caffeine."settings-cache".maximum-size=20000
quarkus.cache.caffeine."settings-cache".expire-after-write=10M
```

### 4. SETTINGS-SERVICE CACHE-ANNOTATIONS

**backend/src/main/java/de/freshplan/infrastructure/settings/SettingsService.java:**

Imports ergänzen:
```java
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CacheInvalidateAll;
```

Konstante hinzufügen:
```java
private static final String CACHE_NAME = "settings-cache";
```

Read-Methoden mit Cache:
```java
@CacheResult(cacheName = CACHE_NAME)
public Optional<Setting> getSetting(SettingsScope scope, String scopeId, String key) { ... }

@CacheResult(cacheName = CACHE_NAME)
public Optional<Setting> resolveSetting(String key, SettingsContext context) { ... }

@CacheResult(cacheName = CACHE_NAME)
public List<Setting> listSettings(SettingsScope scope, String scopeId) { ... }
```

Write-Methoden mit Invalidierung:
```java
@CacheInvalidateAll(cacheName = CACHE_NAME)
@Transactional
public Setting saveSetting(...) { ... }

@CacheInvalidateAll(cacheName = CACHE_NAME)
@Transactional
public Setting updateSettingWithEtag(...) { ... }

@CacheInvalidateAll(cacheName = CACHE_NAME)
@Transactional
public boolean deleteSetting(UUID id) { ... }
```

### 5. VERIFIKATIONSTEST

**backend/src/test/java/de/freshplan/infrastructure/settings/SettingsServiceCachingTest.java:**
```java
package de.freshplan.infrastructure.settings;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SettingsServiceCachingTest {

  @Inject SettingsService service;

  @Test
  void update_shouldInvalidateCache() {
    // Create
    var created = service.saveSetting(
        SettingsScope.GLOBAL, null, "cache.test",
        new JsonObject().put("v", 1), null, "tester");

    // First resolve -> v=1
    var ctx = SettingsService.SettingsContext.global();
    Optional<Setting> r1 = service.resolveSetting("cache.test", ctx);
    assertTrue(r1.isPresent());
    assertEquals(1, r1.get().value.getInteger("v"));

    // Update with ETag
    UUID id = created.id;
    String etag = created.etag;
    var updated = service.updateSettingWithEtag(
        id, new JsonObject().put("v", 2), null, etag, "tester2");

    // Second resolve -> muss v=2 sein (Cache invalidiert)
    Optional<Setting> r2 = service.resolveSetting("cache.test", ctx);
    assertTrue(r2.isPresent());
    assertEquals(2, r2.get().value.getInteger("v"));

    // Cleanup
    service.deleteSetting(id);
  }
}
```

## ⚠️ KRITISCHE HINWEISE

**SETTINGSCONTEXT IST EIN RECORD:**
- Definiert als innerer Record in SettingsService
- Hat automatisch equals() und hashCode()
- Perfekt für Cache-Keys

**CACHE-STRATEGIE:**
- Phase 1: Globale Invalidierung (einfach & sicher)
- Writes sind selten, daher akzeptabel
- Phase 2: Granulare Invalidierung bei Bedarf

## ✅ ERFOLGSMESSUNG

**SPRINT 1.4 IST FERTIG WENN:**
- [ ] PR gemerged
- [ ] Settings-Cache operational
- [ ] Prod-Configs gehärtet
- [ ] Tests grün
- [ ] Performance verbessert (messbar)

**ROADMAP-UPDATE:**
Nach Sprint 1.4 Complete:
- Progress: 8/35 → 9/35 (1 PR)
- Phase 1: 100% COMPLETE ✅
- Status: Ready für Phase 2

**BUSINESS-VALUE:**
- Performance-Verbesserung für Settings-API
- Produktions-Härtung abgeschlossen
- Foundation operativ wasserdicht

---

_Erstellt am 2025-09-24 nach externer KI-Review_
_Sprint 1.4 schließt Phase 1 Foundation vollständig ab_