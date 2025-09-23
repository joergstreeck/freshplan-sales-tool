# ADR-0003: Settings-Registry Hybrid JSONB + Registry

**Status:** Akzeptiert
**Datum:** 15.09.2025
**Autor:** Development Team

## Kontext

Das FreshPlan Sales Tool benötigt ein flexibles Settings-System mit:
- User/Tenant/Global Scope-Hierarchie
- Type-Safe Zugriff auf Settings
- Performance (<50ms p95)
- Cache-Invalidation
- Schema-Validation

Zur Auswahl stehen:
- Pure JSONB Storage
- Separate Tables pro Setting-Type
- Hybrid: JSONB + Type-Registry

## Entscheidung

Wir implementieren **Settings-Registry Hybrid** mit:
- JSONB-Storage für Settings-Werte
- Type-Registry für Schema-Definition + Validation
- ETag-basiertes Caching
- LISTEN/NOTIFY für Cache-Invalidation
- Scope-Hierarchie mit Merge-Engine

## Begründung

### Pro Hybrid-Ansatz:
- **Flexibilität:** JSONB für dynamische Settings
- **Type-Safety:** Registry für Schema-Validation
- **Performance:** ETag-Caching + JSONB-Indexing
- **Developer-Experience:** IntelliSense durch Type-Registry
- **Future-Proof:** Einfache Erweiterung neuer Setting-Types

### Contra Pure JSONB:
- **Keine Type-Safety:** Fehler erst zur Laufzeit
- **Schwierige Validation:** Keine zentrale Schema-Definition
- **Poor DX:** Keine IDE-Unterstützung

### Contra Separate Tables:
- **Schema-Migrations:** Bei jedem neuen Setting-Type
- **Query-Komplexität:** JOINs über viele Tables
- **Schlechte Performance:** Bei vielen Setting-Types

## Konsequenzen

### Positive:
- Best-of-Both-Worlds: Flexibilität + Type-Safety
- Hohe Performance durch Caching
- Entwicklerfreundlich durch Registry
- Einfache Schema-Evolution

### Negative:
- Leichte Komplexität durch Dual-System
- Cache-Konsistenz muss sichergestellt werden
- Registry-Maintenance erforderlich

### Mitigationen:
- Automatische Cache-Invalidation via LISTEN/NOTIFY
- Unit-Tests für Registry-Schema-Konsistenz
- Monitoring für Cache-Hit-Rates

## Implementation Details

### Settings-Schema:
```sql
CREATE TABLE settings (
    id UUID PRIMARY KEY,
    scope_type TEXT NOT NULL, -- 'user', 'tenant', 'global'
    scope_id TEXT, -- userId, tenantId, NULL
    setting_type TEXT NOT NULL, -- Registry-Key
    value JSONB NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    etag TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### Type-Registry:
```typescript
interface SettingTypeRegistry {
  'notification.email': {
    enabled: boolean;
    frequency: 'immediate' | 'daily' | 'weekly';
  };
  'ui.theme': {
    mode: 'light' | 'dark';
    primaryColor: string;
  };
}
```

### Performance SLOs:
- **Cache-Hit-Rate:** `etag_hit_rate_pct >= 70`
- **Read-Performance:** `settings_fetch_p95_ms < 50`
- **Invalidation-Lag:** `cache_invalidation_lag_ms < 1000`

## Alternativen

1. **Pure JSONB:** Abgelehnt wegen fehlender Type-Safety
2. **Separate Tables:** Abgelehnt wegen Schema-Migrations
3. **External Config-Service:** Abgelehnt wegen zusätzlicher Infrastruktur
4. **File-based Config:** Abgelehnt wegen fehlender Runtime-Updates

## Compliance

- **Performance SLO:** `settings_fetch_p95_ms < 50` erfüllt
- **Cache-SLO:** `etag_hit_rate_pct >= 70` erfüllt
- **Schema-Validation:** JSON-Schema für alle Setting-Types
- **Scope-Hierarchie:** User overrides Tenant overrides Global