# 📋 Settings Registry Documentation

**📅 Erstellt:** 2025-09-23
**🎯 Sprint:** 1.2 PR #2
**📦 PR:** FP-229
**🔢 Migration:** V228

## 🎯 Übersicht

Die Settings Registry ist ein zentrales System zur Verwaltung von Anwendungseinstellungen mit einer 5-stufigen Scope-Hierarchie und ETag-basiertem Caching für optimale Performance.

## 🏗️ Architektur

### 5-Level Scope Hierarchie

```yaml
GLOBAL:        # System-weite Defaults
  ↓
TENANT:        # FreshFoodz-spezifische Settings
  ↓
TERRITORY:     # Deutschland vs Schweiz Settings
  ↓
ACCOUNT:       # Kunde-spezifische Settings
  ↓
CONTACT_ROLE:  # CHEF vs BUYER Settings
```

**Auflösungs-Priorität:** Spezifischere Scopes überschreiben allgemeinere. Ein Setting auf ACCOUNT-Level überschreibt das gleiche Setting auf TERRITORY-Level.

## 🚀 Features

### 1. ETag-basiertes Caching
- HTTP ETag Header für optimale Cache-Performance
- Target: ≥70% Hit-Rate für internes Tool (5-50 User)
- Automatische ETag-Generierung bei Änderungen
- If-None-Match Support für conditional GET (304 Not Modified)

### 2. Optimistic Locking
- Version-basierte Konfliktauflösung
- If-Match Header für sichere Updates
- 412 Precondition Failed bei ETag-Mismatch
- 409 Conflict bei gleichzeitigen Änderungen

### 3. PostgreSQL LISTEN/NOTIFY
- Echtzeit Cache-Invalidierung
- Event: `settings_changed` mit JSON Payload
- Automatische Benachrichtigung bei INSERT/UPDATE/DELETE

### 4. JSON Schema Validation
- Werte als JSONB gespeichert
- Metadata für zusätzliche Informationen
- Flexible Struktur für verschiedene Setting-Typen

## 📡 REST API

### Endpoints

#### GET /api/settings
Holt ein spezifisches Setting.

```bash
GET /api/settings?scope=GLOBAL&key=system.feature_flags
Headers:
  If-None-Match: "abc123"

Response:
  Status: 200 OK (oder 304 Not Modified)
  Headers:
    ETag: "def456"
    Cache-Control: max-age=60, must-revalidate
```

#### GET /api/settings/resolve/{key}
Löst ein Setting hierarchisch auf.

```bash
GET /api/settings/resolve/tax.config?tenantId=freshfoodz&territory=DE

Response:
  {
    "scope": "TERRITORY",
    "scopeId": "DE",
    "key": "tax.config",
    "value": {
      "currency": "EUR",
      "vatRate": 19
    },
    "etag": "xyz789"
  }
```

#### PUT /api/settings/{id}
Aktualisiert ein Setting mit Optimistic Locking.

```bash
PUT /api/settings/123e4567-e89b-12d3-a456-426614174000
Headers:
  If-Match: "abc123"
  Content-Type: application/json

Body:
  {
    "value": {
      "enabled": true,
      "threshold": 150
    }
  }

Response:
  Status: 200 OK (oder 412 Precondition Failed)
```

#### POST /api/settings
Erstellt ein neues Setting (Admin only).

```bash
POST /api/settings
Content-Type: application/json

Body:
  {
    "scope": "TERRITORY",
    "scopeId": "DE",
    "key": "business.hours",
    "value": {
      "open": "09:00",
      "close": "18:00"
    },
    "metadata": {
      "description": "Geschäftszeiten für Deutschland"
    }
  }

Response:
  Status: 201 Created
  Headers:
    Location: /api/settings/new-uuid
    ETag: "initial-etag"
```

## 🔒 Security

### Berechtigungen
- **Read:** Alle authentifizierten User
- **Create/Update/Delete:** Nur Admin-Rolle
- **Cache Stats:** Nur Admin-Rolle

### Territory Isolation
Settings mit Territory-Scope sind strikt getrennt:
- DE-User sehen nur DE-Settings
- CH-User sehen nur CH-Settings
- Admins können mit Override-Flag zugreifen (mit Audit)

## 📊 Performance

### Cache-Metriken
```java
GET /api/settings/stats/cache

Response:
{
  "hitRate": 72.5,        // Prozent
  "availability": 0.99,   // Verfügbarkeit
  "avgResponseTimeMs": 45 // Millisekunden
}
```

### Performance-Ziele
- **P95 Response Time:** < 200ms (ohne Cache)
- **P95 Response Time:** < 50ms (mit ETag Cache-Hit)
- **Cache Hit-Rate:** ≥ 70%
- **Concurrent Updates:** Optimistic Locking verhindert Lost Updates

## 🗄️ Datenbank

### Tabelle: security_settings
```sql
CREATE TABLE security_settings (
  id UUID PRIMARY KEY,
  scope TEXT NOT NULL,             -- ENUM as TEXT ('GLOBAL', 'TENANT', ...)
  scope_id TEXT,
  key TEXT NOT NULL,
  value JSONB NOT NULL,
  metadata JSONB,
  etag TEXT,
  version INTEGER DEFAULT 1,
  created_at TIMESTAMPTZ,
  updated_at TIMESTAMPTZ,
  created_by TEXT,
  updated_by TEXT
);
```

### Indizes
- `UNIQUE(scope, scope_id, key)` - Eindeutigkeit
- `INDEX(etag)` - ETag-basierte Queries
- `INDEX(scope, scope_id, key, version DESC)` - Hierarchische Auflösung

### Trigger
- `trg_update_settings_etag` - Auto-Update von ETag und Version
- `trg_insert_settings_etag` - Initial ETag bei INSERT
- `trg_notify_settings_change` - LISTEN/NOTIFY für Cache

## 🧪 Tests

### Unit Tests (SettingsServiceTest)
- ✅ Create und Retrieve von Settings
- ✅ Hierarchische Auflösung
- ✅ ETag-basiertes Optimistic Locking
- ✅ Territory Isolation
- ✅ Cache-Verhalten

### Integration Tests (SettingsResourceTest)
- ✅ REST API mit ETag Headers
- ✅ 304 Not Modified Responses
- ✅ 412 Precondition Failed bei falscher ETag
- ✅ Authorisierung (Admin-only für Writes)
- ✅ Hierarchische Resolution via API

## 📝 Verwendungsbeispiele

### Feature Flags
```json
{
  "scope": "GLOBAL",
  "key": "system.feature_flags",
  "value": {
    "etag_caching": true,
    "security_gates": true,
    "new_dashboard": false
  }
}
```

### Territory-spezifische Business Rules
```json
{
  "scope": "TERRITORY",
  "scopeId": "CH",
  "key": "business.rules",
  "value": {
    "currency": "CHF",
    "vatRate": 7.7,
    "requiresCanton": true,
    "languages": ["de", "fr", "it"]
  }
}
```

### Account-spezifische Limits
```json
{
  "scope": "ACCOUNT",
  "scopeId": "acc-12345",
  "key": "limits.orders",
  "value": {
    "maxOrdersPerDay": 100,
    "maxItemsPerOrder": 500,
    "creditLimit": 50000
  }
}
```

## 🔗 Integration

### Mit anderen Modulen
- **Module 01 (Cockpit):** Dashboard-Konfiguration
- **Module 04 (Auswertungen):** Report-Settings
- **Module 06 (Einstellungen):** UI für Settings-Verwaltung
- **Module 07 (Hilfe):** Hilfe-Text Konfiguration
- **Module 08 (Administration):** System-Settings

### Mit Security Foundation
- Nutzt `security_settings` Tabelle (aus V227)
- Integriert mit ABAC für Admin-Checks
- Territory-Scoping via RLS (später)

## 🚦 Status

### ✅ Phase 1 Abgeschlossen (Sprint 1.1-1.4)

**Sprint 1.2:** ✅ Core Implementation
- **PR #95, #96, #99-101:** Settings Registry mit ETag
- **Migration V228:** Settings Tabelle (+ V10010/V10011 Scope Fixes)
- **SessionSettingsFilter:** GUC Integration implementiert
- **Tests:** Race Condition Prevention (createSettingStrict)
- **Coverage:** 85% Unit Tests, Integration Tests vorhanden

**Sprint 1.3:** ✅ Security & CI Integration
- **PR #97:** Security Gates implementiert
- **Performance:** P95 < 200ms erreicht
- **CI/CD:** 3-stufige Pipeline (PR/Nightly/Security)

**Sprint 1.4:** ✅ Cache & Production Hardening
- **PR #102:** Quarkus-Cache implementiert (24.09.2025)
- **Cache-Metriken:** 70% Hit-Rate, 90% Performance-Gain (50ms → 5ms)
- **TTL:** 5min, Max 5000 Entries
- **Invalidierung:** Global bei Mutations (pragmatisch für Phase 1)
- **Prod-Config:** DB_PASSWORD Pflicht, CSP gehärtet

### 🎯 Nächste Schritte (Phase 2)
- Sprint 2.1: Integration mit Business-Modulen (Lead-Management)
- Sprint 2.x: UI in Module 06 (Einstellungen)

## 🎨 Frontend-Integration / ETag

### React Query Setup

Die Frontend-Integration nutzt React Query mit ETag-Support für optimales Caching:

```typescript
// API Client mit ETag-Store
const etagStore = new Map<string, string>();

export async function fetchSetting(key: string) {
  const etag = etagStore.get(key);
  const headers: HeadersInit = {};

  if (etag) {
    headers['If-None-Match'] = etag;
  }

  const response = await fetch(
    `/api/settings?scope=GLOBAL&key=${key}`,
    { headers }
  );

  if (response.status === 304) {
    return getCachedData(key); // Cache hit
  }

  const newEtag = response.headers.get('ETag');
  if (newEtag) {
    etagStore.set(key, newEtag);
  }

  return response.json();
}
```

### React Hooks

```typescript
// Settings Hook
export function useSetting(key: string) {
  return useQuery({
    queryKey: ['setting', key],
    queryFn: () => fetchSetting(key),
    staleTime: 60_000,  // 1 minute
    gcTime: 600_000,    // 10 minutes
    retry: 1,
  });
}

// Feature Flag Hook
export function useFeatureFlag(flag: string) {
  const { data } = useSetting('system.feature_flags');
  return data?.value?.[flag] ?? false;
}
```

### Theme Integration

```typescript
const DEFAULT_THEME = {
  primary: '#94C456',   // FreshFoodz Green
  secondary: '#004F7B', // FreshFoodz Blue
};

export function ThemeProvider({ children }) {
  const { data: themeSetting } = useSetting('ui.theme');

  const theme = {
    ...DEFAULT_THEME,
    ...(themeSetting?.value || {}),
  };

  return (
    <ThemeContext.Provider value={theme}>
      {children}
    </ThemeContext.Provider>
  );
}
```

### Performance Benefits

- **Cache Hit Rate:** ≥70% durch ETag-Validation
- **Network Traffic:** -60% durch 304 Responses
- **Response Time:** <50ms für cached Settings
- **Fallback:** Defaults bei API-Fehlern