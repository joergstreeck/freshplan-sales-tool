# 🔒 Security Update: Sprint 1.5 - RLS Connection Affinity

**Status:** ✅ DEPLOYED (PR #106)
**Datum:** 25.09.2025
**Kritikalität:** P0 - KRITISCH
**Betrifft:** ALLE Module

## Executive Summary

Sprint 1.5 hat eine **kritische Sicherheitslücke** im Row-Level Security (RLS) System behoben. Das Problem war, dass GUC-Variablen (User/Territory Context) aufgrund von Connection Pooling auf unterschiedlichen Datenbankverbindungen landen konnten, was zu potenziellen **Datenleaks zwischen Territories/Tenants** führen konnte.

## Was wurde geändert?

### 1. Neue @RlsContext Annotation (PFLICHT)

**Alle Datenbankzugriffe müssen jetzt die `@RlsContext` Annotation verwenden:**

```java
@Transactional
@RlsContext  // NEU: Garantiert Connection Affinity
public Lead createLead(Lead lead) {
    // Ihre DB-Operationen
}
```

### 2. Neue GUC-Keys (Breaking Change)

**Alt (funktioniert nicht mehr):**
- `app.current_user`
- `app.current_role`
- `app.current_territory`

**Neu (ab sofort verwenden):**
- `app.user_context`
- `app.role_context`
- `app.territory_context`

### 3. AppGuc Enum verwenden

**Niemals hardcoden:**
```java
// ❌ FALSCH
em.createNativeQuery("SET app.user_context = 'user'");

// ✅ RICHTIG
em.createNativeQuery(AppGuc.USER_CONTEXT.setLocalConfigSql())
   .setParameter(1, AppGuc.USER_CONTEXT.getKey())
   .setParameter(2, "user")
   .getSingleResult();
```

## Impact auf Module

### 🎯 Modul 01: Mein Cockpit
- Dashboard-Queries müssen `@RlsContext` verwenden
- Widgets mit DB-Zugriff anpassen

### 🎯 Modul 02: Neukundengewinnung
- ✅ LeadResource bereits angepasst
- ✅ LeadService mit `@RlsContext` (Sprint 1.6)
- ✅ UserLeadSettingsService mit `@RlsContext` (Sprint 1.6)
- ✅ TerritoryService mit `@RlsContext` (Sprint 1.6)
- ✅ EmailActivityDetectionService mit `@RlsContext` (Sprint 1.6)
- Lead Protection funktioniert korrekt mit RLS

### 🎯 Modul 03: Kundenmanagement
- CustomerService muss `@RlsContext` hinzufügen
- ContactService muss `@RlsContext` hinzufügen
- Territory-Filter automatisch durch RLS

### 🎯 Modul 04: Auswertungen
- ReportService muss `@RlsContext` verwenden
- Analytics-Queries mit neuen GUC-Keys

### 🎯 Modul 05: Kommunikation
- MessageService muss `@RlsContext` verwenden
- NotificationService anpassen

### 🎯 Modul 06: Einstellungen
- ✅ SettingsService bereits angepasst
- Nutzt neue RLS-Implementation

### 🎯 Modul 07: Hilfe & Support
- Keine DB-Zugriffe, kein Impact

### 🎯 Modul 08: Administration
- AdminService muss `@RlsContext` verwenden
- Rolle ADMIN bypassed automatisch RLS

### 🏗️ Infrastruktur Mini-Module
- ✅ **Security:** Komplett überarbeitet
- ✅ **CQRS:** EventPublisher/Subscriber angepasst
- ✅ **Export:** UniversalExportService angepasst
- ⚠️ **Import:** Muss noch geprüft werden
- ⚠️ **Monitoring:** Health-Check implementiert

## Migration Guide

### Schritt 1: Dependencies prüfen
```java
import de.freshplan.infrastructure.security.RlsContext;
import de.freshplan.infrastructure.security.AppGuc;
```

### Schritt 2: Service-Methoden annotieren
```java
@ApplicationScoped
@Transactional  // Klassen-Level
public class YourService {

    @RlsContext  // Methoden-Level
    public void doSomething() {
        // DB-Operations
    }
}
```

### Schritt 3: Tests anpassen
```java
// Test mit RLS-Context
@Test
@TestSecurity(user = "test-user", roles = "user")
@Transactional
void testWithRls() {
    // Test-Code
}
```

## Fail-Closed Security

**Das System ist jetzt fail-closed konfiguriert:**

1. **Ohne @RlsContext:** Exception wird geworfen
2. **Ohne Transaction:** Exception wird geworfen
3. **Ohne GUC-Context:** 0 Rows returned
4. **In Production:** Boot schlägt fehl wenn RLS disabled

## Performance Impact

- **Overhead:** <5ms pro Request
- **Connection Pool:** Keine Änderung
- **API P95:** Weiterhin <200ms

## Konfiguration

```properties
# application.properties
security.rls.interceptor.enabled=true
security.rls.fail-closed=true
security.rls.system-user=events-bus@freshplan

# Event System
cqrs.subscriber.reconnect.max-retries=3
cqrs.subscriber.reconnect.initial-delay-ms=1000
```

## Testing

### Unit Tests
```java
@QuarkusTest
class YourServiceTest {
    @Test
    @TestSecurity(user = "test", roles = "user")
    @Transactional
    void testRlsContext() {
        // Automatisch mit RLS-Context
    }
}
```

### Integration Tests
```java
// DisableEventSubscriberProfile für Mock-Tests
@TestProfile(DisableEventSubscriberProfile.class)
```

## Troubleshooting

### Problem: "No active transaction for RLS context"
**Lösung:** `@Transactional` zur Methode hinzufügen

### Problem: "RLS context not set"
**Lösung:** `@RlsContext` zur Methode hinzufügen

### Problem: Alte GUC-Keys funktionieren nicht
**Lösung:** Migration V243 prüfen, neue Keys verwenden

### Problem: Tests schlagen fehl
**Lösung:** `@TestSecurity` und `@Transactional` hinzufügen

## Health Check

**Neuer Endpoint:** `/health/rls`

```bash
curl http://localhost:8080/health/rls
```

Zeigt:
- RLS Status (enabled/disabled)
- GUC Context
- Connection Stats
- Policy Status

## Rollback (Notfall)

Falls kritische Probleme auftreten:

```bash
# 1. Feature deaktivieren
echo "security.rls.interceptor.enabled=false" >> application.properties

# 2. Alte Migration reaktivieren
psql -c "DELETE FROM flyway_schema_history WHERE version IN ('242', '243');"

# 3. Restart
./mvnw quarkus:dev
```

## Support

Bei Problemen:
- Slack: #dev-security
- Issue: FP-262
- ADR: ADR-0007

## Follow-up Tasks

- FP-267: EventSubscriber Retry-Config externalisieren
- FP-268: Redundante @Transactional bereinigen
- FP-269: pg_stat_activity durch Agroal Metrics ersetzen
- FP-270: Migration V243 Redundanz bereinigen
- FP-271: Test-Naming modernisieren

---

**⚠️ WICHTIG:** Alle Teams müssen ihre Services bis **Ende Sprint 2.2** anpassen!