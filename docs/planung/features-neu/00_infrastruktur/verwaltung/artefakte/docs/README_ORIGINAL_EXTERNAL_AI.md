# Settings-MVP Pack (Governance – Hybrid Registry)

## Inhalt
- **SQL Templates**
  - `sql/templates/settings_registry_core.sql` – Registry/Store/Effective Tabellen, RLS, NOTIFY, Seeds
- **Java/Quarkus**
  - `backend/java/de/freshplan/verwaltung/settings/SettingsPrincipal.java`
  - `backend/java/de/freshplan/verwaltung/settings/JsonMerge.java`
  - `backend/java/de/freshplan/verwaltung/settings/JsonSchemaValidator.java`
  - `backend/java/de/freshplan/verwaltung/settings/SettingsService.java` (ETag, L1-Cache, Validation, Metrics)
  - `backend/java/de/freshplan/verwaltung/settings/SettingsResource.java` (GET /api/settings/effective, PATCH /api/settings)
  - `backend/java/de/freshplan/verwaltung/settings/SettingsNotifyListener.java` (LISTEN/NOTIFY Cache-Bust)
  - `backend/java/de/freshplan/verwaltung/settings/RegistryOrConfig.java` (Read-through Adapter)

## Quarkus/Maven Hinweise (pom.xml)
Abhängigkeiten (Beispiele):
```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-jdbc-postgresql</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-micrometer</artifactId>
</dependency>
<dependency>
  <groupId>com.networknt</groupId>
  <artifactId>json-schema-validator</artifactId>
  <version>1.0.86</version>
</dependency>
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

## Verwendung
1. **Migration erzeugen**  
```bash
cp sql/templates/settings_registry_core.sql sql/$(./scripts/get-next-migration.sh)__settings_registry_core.sql
```
2. **Deploy** DB-Migration.
3. **Quarkus**: Klassen in euer Projekt kopieren, Packages beibehalten (`de.freshplan.governance.settings`).
4. **Security**: Stellt sicher, dass die App DB-Session-Variablen setzt (user_id, roles, tenant_id, org_id) – z. B. via ConnectionInterceptor.
5. **API**:
   - `GET /api/settings/effective?keys=ai.cache.ttl,credit.peak.slo.p95.ms`
   - `PATCH /api/settings`  (Body: `{ key, scope, tenantId?, orgId?, userId?, value }`)

## Metriken
- `settings_fetch_ms` (Timer) – p95 sollte < 50 ms liegen.
- `settings_etag_hits` (Counter)
- `settings_lookup_source{module,source="registry|config"}`

## Hinweise
- Deep-Merge/Append erfolgt in Java (JsonMerge), weil SQL-Implementierung komplexer wäre.
- Bei globalen Änderungen genügt Cache-Invalidation; effektive Werte werden „on-read“ recomputed und persistiert.
