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
  scope settings_scope NOT NULL,  -- ENUM
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

**Sprint 1.2 PR #2:** ✅ Implementiert
- Migration V228 erstellt
- Entity, Service, REST API fertig
- Tests geschrieben (Unit + Integration)
- Dokumentation komplett

**Nächste Schritte:**
- Sprint 1.2 PR #3: ETag-Caching Optimization
- Sprint 2.x: UI in Module 06 (Einstellungen)