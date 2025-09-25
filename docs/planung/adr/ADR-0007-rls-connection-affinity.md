# ADR-0007: RLS Connection Affinity Pattern

**Status:** ✅ ACCEPTED & IMPLEMENTED
**Datum:** 2025-09-25
**Sprint:** 1.5 Security Retrofit
**PR:** #106 (merged)

## Context

Kritische Sicherheitslücke im Row-Level Security (RLS) System entdeckt: GUC-Variablen (User/Territory Context) konnten aufgrund von Connection Pooling auf unterschiedlichen Datenbankverbindungen landen, was zu potenziellen Datenleaks zwischen Territories/Tenants führen konnte.

### Problem Details
- PostgreSQL GUC-Variablen sind connection-scoped
- Connection Pool kann Connections zwischen Requests recyclen
- ContainerRequestFilter kann keine Connection Affinity garantieren
- SET LOCAL ohne Transaction wird zu SET (session-scoped)
- Risk: Territory A könnte Daten von Territory B sehen

## Decision

Implementierung eines **CDI Interceptor Patterns** mit expliziter Connection Affinity durch `@RlsContext` Annotation.

### Gewählte Lösung
```java
@RlsContext      // NEU: CDI Interceptor
@Transactional   // PFLICHT: Garantiert gleiche Connection
public void businessMethod() {
    // GUCs sind garantiert auf gleicher Connection
}
```

### Technische Details

1. **CDI Interceptor statt Filter**
   - `RlsConnectionAffinityGuard` als `@Interceptor`
   - Priority: `Interceptor.Priority.LIBRARY_BEFORE + 100`
   - Scope: `@Dependent` (CDI Requirement)

2. **Transaction Check via TSR**
   ```java
   @Inject TransactionSynchronizationRegistry tsr;
   if (tsr.getTransactionKey() == null) {
       throw new IllegalStateException("No active transaction");
   }
   ```

3. **Neue GUC-Keys (PostgreSQL-kompatibel)**
   - `app.user_context` (statt app.current_user)
   - `app.role_context` (statt app.current_role)
   - `app.territory_context` (statt app.current_territory)

4. **Prepared Statement Pattern**
   ```java
   // Sicherer als SET-Commands
   em.createNativeQuery("SELECT set_config(?, ?, true)")
      .setParameter(1, AppGuc.USER_CONTEXT.getKey())
      .setParameter(2, value)
      .getSingleResult();
   ```

## Consequences

### Positive
- ✅ **100% Connection Affinity** garantiert
- ✅ **Fail-closed Security** - ohne Context kein Zugriff
- ✅ **Performance** <5ms Overhead
- ✅ **Explizite Sicherheit** - @RlsContext macht Intent klar
- ✅ **CI-kompatibel** - GRANT IF EXISTS für freshplan role

### Negative
- ⚠️ **Breaking Change** - alle Services müssen @RlsContext hinzufügen
- ⚠️ **Migration erforderlich** - V242 + V243
- ⚠️ **Transaction-Pflicht** - ohne @Transactional Exception

### Neutral
- 📝 Entwickler müssen neues Pattern lernen
- 📝 Tests benötigen @Transactional + @TestSecurity

## Implementation

### Migrations
- **V242:** Fail-closed RLS Policies
- **V243:** GUC Keys Update

### Core Components
```
infrastructure/security/
├── RlsConnectionAffinityGuard.java  # CDI Interceptor
├── RlsContext.java                  # Annotation
├── AppGuc.java                      # Zentrale GUC-Keys
└── RlsBootstrapGuard.java          # Production Safety
```

### Test Coverage
- `RlsConnectionAffinityTest` - Connection Affinity
- `RlsRoleConsistencyTest` - Role Uppercase
- `EventSubscriberReconnectTest` - Reconnect Logic

## Alternatives Considered

1. **ContainerRequestFilter erweitern**
   - ❌ Kann Connection Affinity nicht garantieren
   - ❌ Filter läuft vor Transaction-Start

2. **EntityListener Pattern**
   - ❌ Zu spät im Lifecycle
   - ❌ Schwer testbar

3. **Custom Transaction Manager**
   - ❌ Zu invasiv
   - ❌ Inkompatibel mit Quarkus

## References

- [SECURITY_UPDATE_SPRINT_1_5.md](../SECURITY_UPDATE_SPRINT_1_5.md)
- [TRIGGER_SPRINT_1_5.md](../TRIGGER_SPRINT_1_5.md)
- [PostgreSQL GUC Documentation](https://www.postgresql.org/docs/current/runtime-config.html)
- [CDI Interceptor Specification](https://jakarta.ee/specifications/interceptors/)

## Review

**Gemini Review:** "Exzellent und äußerst wichtig - kritische Sicherheitslücke professionell behoben"

## Follow-up

- FP-267: EventSubscriber Retry-Config externalisieren
- FP-268: Redundante @Transactional bereinigen
- FP-269: pg_stat_activity durch Agroal Metrics ersetzen