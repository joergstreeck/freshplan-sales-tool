# ADR-0008: Xentral API Configuration via @ConfigProperty (No XentralApiConfig)

**Status:** Accepted
**Datum:** 2025-10-24
**Entscheider:** Development Team
**Kontext:** Sprint 2.1.7.2 - Xentral Integration

## Kontext

Die Xentral Connect API (2024/25) verwendet **token-basierte Authentifizierung** (Personal Access Token / PAT) statt der älteren API-Key-basierten Authentifizierung. Die Integration benötigt nur 3 einfache Properties:
- `xentral.api.base-url` (String)
- `xentral.api.token` (String, PAT)
- `xentral.api.mock-mode` (Boolean)

Initial wurde eine `XentralApiConfig.java` mit `@ConfigMapping` erstellt, die zu **Configuration Validation Errors** führte:
- Quarkus validierte **ALLE** `xentral.api.*` Properties als Teil des Mappings
- Properties wie `xentral.api.connect-timeout` und `xentral.api.read-timeout` (für REST Clients) konnten nicht gemappt werden
- Die Timeout-Properties wurden in separaten `quarkus.rest-client.*` Konfigurationen definiert
- `@ConfigMapping` erzwang inkonsistente Property-Namenskonventionen

## Entscheidung

**Verwende `@ConfigProperty` Injection statt `@ConfigMapping` für Xentral API Configuration:**

```java
// ✅ RICHTIG: Direct @ConfigProperty Injection
@ConfigProperty(name = "xentral.api.base-url")
String baseUrl;

@ConfigProperty(name = "xentral.api.token")
String token;

@ConfigProperty(name = "xentral.api.mock-mode", defaultValue = "true")
boolean mockMode;

// ❌ FALSCH: XentralApiConfig mit @ConfigMapping
// → Verursacht Property Validation Errors
// → Nicht benötigt für neue API
```

**XentralApiConfig.java wurde gelöscht** (Commit: `015822ffa`)

## Begründung

**Warum @ConfigProperty statt @ConfigMapping:**
- **Einfachheit:** Nur 3 Properties - kein Typed Config Object nötig
- **Flexibilität:** REST Client Timeout-Properties können separat konfiguriert werden
- **Keine Validation Conflicts:** Quarkus validiert nur die injizierten Properties
- **Konsistenz:** Xentral-API-Properties und REST-Client-Properties können unterschiedliche Namenskonventionen verwenden

**Warum keine Type-Safe Config:**
- Xentral Connect API (2024/25) benötigt nur primitive Typen (String, Boolean)
- Keine komplexen Nested Objects oder Lists
- Keine Validierungslogik notwendig (PAT ist opaque String)

**Warum Token-basierte Auth einfacher ist:**
- Legacy API: Username/Password + API-Key → mehrere Config-Felder
- Neue API: Nur PAT Token → ein Config-Feld
- Keine Client-Secret-Rotation (PAT wird in Xentral-UI verwaltet)

## Konsequenzen

**Positive:**
- ✅ Keine Configuration Validation Errors mehr
- ✅ REST Client Timeouts können unabhängig konfiguriert werden
- ✅ Weniger Code (keine Config-Klasse nötig)
- ✅ Einfacher zu verstehen (direkte Property-Injection)
- ✅ Konsistent mit anderen Simple-Config-Patterns im Projekt

**Negative:**
- ⚠️ Kein Type-Safety für Config-Properties (Strings statt Typed Objects)
- ⚠️ Kein zentrales Config-Object für Xentral-Einstellungen
- ⚠️ Jede Klasse muss Properties einzeln injizieren

**Mitigationen:**
- Config-Properties sind in `application.properties` zentral dokumentiert
- Nur 2 Klassen benötigen Injection (`XentralApiService`, `XentralSettingsResource`)
- Default-Values sind in `@ConfigProperty` Annotations definiert

## Alternativen

**A) @ConfigMapping mit vollständigem Property-Mapping**
- Verworfen: Müsste ALLE `xentral.api.*` Properties mappen (inkl. REST Client Timeouts)
- Problem: REST Client Config ist Quarkus-managed, nicht Teil unserer Domain-Config

**B) Separate Config-Klasse für REST Client und Domain**
- Verworfen: Overhead für nur 3 Properties
- Keine Business-Logic auf Config-Object

**C) SmallRye Config @ConfigProperties**
- Verworfen: Ähnliche Validation-Issues wie @ConfigMapping
- Nicht standardmäßig in Quarkus enthalten

**D) Hardcoded Defaults in Services**
- Verworfen: Config soll in `application.properties` zentral liegen
- Nicht Dev/Prod-umschaltbar

## Implementation

**Betroffene Dateien:**
- ❌ **DELETED:** `XentralApiConfig.java` (war Legacy-Code, nie benötigt)
- ✅ **UPDATED:** `XentralSettingsResource.java` → Verwendet `@ConfigProperty`
- ✅ **UPDATED:** `XentralApiService.java` → Verwendet `@ConfigProperty`

**Commit:** `015822ffa` (2025-10-24)

**application.properties Pattern:**
```properties
# Xentral Connect API (2024/25) - Token-based Auth
xentral.api.base-url=https://644b6ff97320d.xentral.biz
xentral.api.token=dev-dummy-token-not-used-in-mock-mode
xentral.api.mock-mode=true

# Production: Real credentials from environment variables
%prod.xentral.api.base-url=${XENTRAL_API_URL}
%prod.xentral.api.token=${XENTRAL_API_TOKEN}

# REST Client Timeouts (separate namespace)
xentral.api.connect-timeout=5000
xentral.api.read-timeout=10000

quarkus.rest-client.xentral-customers-v2.connect-timeout=${xentral.api.connect-timeout}
quarkus.rest-client.xentral-customers-v2.read-timeout=${xentral.api.read-timeout}
```

## Monitoring

- ✅ Server startet ohne Configuration Validation Errors
- ✅ Mock-Mode funktioniert in Development
- ✅ Real-API-Mode funktioniert in Production (nach PAT-Konfiguration)

## Future Considerations

Wenn die Xentral-Integration komplexer wird (mehr als 5 Properties, komplexe Nested Objects), sollte **@ConfigMapping mit vollständigem Namespace** in Betracht gezogen werden:

```java
// Hypothetisches Future-Szenario (NICHT aktuell nötig!)
@ConfigMapping(prefix = "xentral")
interface XentralConfig {
  ApiConfig api();
  WebhookConfig webhook();

  interface ApiConfig {
    String baseUrl();
    String token();
    boolean mockMode();
  }
}
```

**Aktuell nicht nötig** - Simple @ConfigProperty reicht aus.

---

**Related:**
- [Sprint 2.1.7.2 Trigger](../TRIGGER_SPRINT_2_1_7_2.md)
- [Xentral API Integration Spec](../artefakte/SPEC_SPRINT_2_1_7_2_TECHNICAL.md)
- Commit: `015822ffa` - Remove legacy XentralApiConfig
- Commit: `3105ff68a` - Document architecture decision
