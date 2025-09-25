# 🔒 TRIGGER: Sprint 1.5 - Security Retrofit (RLS Connection Affinity)

**Sprint:** 1.5 (Foundation - Kritischer Security Fix)
**Dauer:** 3-4h
**Priorität:** P0 - KRITISCH
**Status:** ✅ COMPLETE (PR #106 merged)

## 🎯 Sprint-Ziel

**KRITISCHER SICHERHEITSFIX:** Behebung der RLS Connection Affinity Schwachstelle, bei der GUC-Variablen aufgrund von Connection Pooling auf unterschiedlichen Datenbankverbindungen landen konnten, was zu potenziellen Datenleaks zwischen Territories/Tenants führen konnte.

## ⚠️ Problem

- GUC-Variablen (User/Territory Context) landen auf falschen DB-Connections
- Connection Pool kann Context zwischen Requests mischen
- Potenzielle Datenleaks zwischen Mandanten/Territories
- ContainerRequestFilter kann Connection Affinity nicht garantieren

## ✅ Implementierte Lösung

### 1. CDI Interceptor Pattern
```java
@RlsContext  // Neue Annotation
@Transactional  // Pflicht für Connection Affinity
public void doSomething() {
    // GUCs sind garantiert auf gleicher Connection
}
```

### 2. Neue GUC-Keys (PostgreSQL-kompatibel)
- `app.user_context` (statt app.current_user)
- `app.role_context` (statt app.current_role)
- `app.territory_context` (statt app.current_territory)

### 3. Fail-closed Security
- RlsBootstrapGuard: Fail-fast in Production
- RLS Policies: Kein Zugriff ohne Context
- Transaction Check via TSR

## 📊 Ergebnisse

- **Performance:** <5ms Overhead
- **Security:** 100% Connection Affinity
- **Tests:** Alle grün
- **CI:** Pipeline grün
- **Review:** Gemini: "Exzellent und äußerst wichtig"

## 🔧 Technische Details

### Migrations
- V242: Fail-closed RLS Policies
- V243: GUC Keys Update

### Geänderte Komponenten
- `RlsConnectionAffinityGuard`: CDI Interceptor
- `AppGuc`: Zentrale GUC-Verwaltung
- `EventSubscriber`: Session-scoped GUCs
- `RlsBootstrapGuard`: Production Safety

### Tests
- `RlsConnectionAffinityTest`: Connection Affinity
- `RlsRoleConsistencyTest`: Role Uppercase
- `EventSubscriberReconnectTest`: Reconnect Logic

## 📝 Follow-up Tasks

- FP-267: EventSubscriber Retry-Config externalisieren
- FP-268: Redundante @Transactional bereinigen
- FP-269: pg_stat_activity durch Agroal Metrics ersetzen
- FP-270: Migration V243 Redundanz bereinigen
- FP-271: Test-Naming modernisieren

## 🚀 Deployment

```bash
# Automatisch via Flyway
# Migrations V242 + V243 werden ausgeführt
# Kein manueller Eingriff nötig
```

## 📚 Dokumentation

- [ADR-0007: RLS Connection Affinity Pattern](../adr/ADR-0007-rls-connection-affinity.md)
- [Infrastructure Security Retrofit](./INFRASTRUCTURE_SECURITY_RETROFIT.md)
- [Security Update für alle Module](./SECURITY_UPDATE_SPRINT_1_5.md)

## ✅ Definition of Done

- [x] CDI Interceptor implementiert
- [x] Fail-closed Policies aktiv
- [x] Alle Services nutzen @RlsContext
- [x] Tests grün
- [x] CI Pipeline grün
- [x] Dokumentation komplett
- [x] Code Review bestanden
- [x] Gemini Review positiv
- [x] In main gemerged

---

**Sprint abgeschlossen:** 25.09.2025, 18:42 Uhr
**Merge:** PR #106
**Review:** "Exzellent und äußerst wichtig"