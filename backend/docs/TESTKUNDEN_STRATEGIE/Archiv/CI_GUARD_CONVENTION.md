# 🔒 CI Guard Convention - Einheitliche Sicherheitsmechanismen

**Version:** 1.0  
**Status:** VERBINDLICH  
**Datum:** 17.08.2025  

---

## ⚠️ SINGLE SOURCE OF TRUTH

### Das offizielle Guard-Flag ist: `ci.build=true`

**Alle CI-spezifischen Migrationen MÜSSEN dieses Flag prüfen:**

```sql
-- Standard Guard Pattern für alle CI-Migrationen
DO $$
BEGIN
    -- Guard: Only run in CI/Test environments
    IF current_setting('ci.build', true) <> 'true' THEN
        RAISE NOTICE 'Migration skipped (not CI, ci.build=%)', current_setting('ci.build', true);
        RETURN;
    END IF;
    
    -- Migration logic here...
END $$;
```

---

## 📋 Wo wird das Flag gesetzt?

### ✅ **BEVORZUGTE METHODE: JDBC-URL options**

```yaml
# 1. CI Pipeline (GitHub Actions) - EMPFOHLEN
- name: Run tests
  run: |
    ./mvnw test \
      -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan?options=-c%20ci.build%3Dtrue
```

```properties
# 2. Test Environment (application-test.properties) - EMPFOHLEN
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan?options=-c%20ci.build%3Dtrue
```

### ⚠️ **ALTERNATIVE METHODE: init-sql (Fallback)**

```properties
# Nur verwenden, wenn JDBC-URL nicht möglich
quarkus.flyway.init-sql=SET ci.build = 'true';
```

### 🔧 **Lokales Testing (manuell)**
```bash
# Option 1: Via JDBC URL (empfohlen)
export DB_URL="jdbc:postgresql://localhost:5432/freshplan?options=-c%20ci.build%3Dtrue"

# Option 2: Via psql (temporär)
psql -d freshplan -c "SET ci.build = 'true';"
```

**💡 Copy&Paste-Fehler vermeiden:** Nutze durchgängig JDBC-URL options als Standard!

---

## ✅ Migrationen die Guards verwenden

| Migration | Zweck | Guard Status |
|-----------|-------|--------------|
| **V10000** | Zwei-Stufen-Cleanup | ✅ ci.build |
| **V10001** | Contract Guard | ❌ Kein Guard (läuft immer für Monitoring) |
| **V10002** | Unique Constraints | ⚠️ Teilweise (verhaltensgesteuert per ci.build) |
| **V10003** | Dashboard View | ❌ Kein Guard (strukturelle Migration) |
| **V10004** | Cleanup spurious SEEDs | ✅ ci.build (nur in test/) |
| **V10005** | Create 20 SEEDs | ✅ ci.build (nur in test/) |

### Erklärung V10002:
V10002 läuft immer, entscheidet aber per `ci.build` über den Verhaltenspfad:
- **CI-Umgebung**: Cleanup von Test-Duplikaten
- **Außerhalb CI**: Exception bei Duplikaten (Schutz für Production)

---

## ⚠️ WICHTIG: Keine alternativen Guards!

**NICHT verwenden:**
- ❌ `freshplan.environment` (veraltet)
- ❌ `GITHUB_ACTIONS` Environment Variable
- ❌ Database name checks (`LIKE '%test%'`)
- ❌ User-based checks (`current_user = 'test'`)

**NUR verwenden:**
- ✅ `current_setting('ci.build', true)`

---

## 🔍 Debugging

### Prüfen ob Flag gesetzt ist:
```sql
-- In psql oder Migration
SELECT current_setting('ci.build', true);
-- Sollte 'true' zurückgeben in CI/Test
```

### Troubleshooting:
```sql
-- Alle Session-Settings anzeigen
SELECT name, setting 
FROM pg_settings 
WHERE name LIKE '%ci%' OR name LIKE '%build%';
```

---

## 📝 Checkliste für neue Migrationen

- [ ] Braucht die Migration einen CI-Guard?
- [ ] Guard-Pattern aus diesem Dokument kopiert?
- [ ] `ci.build` als einziges Guard-Flag verwendet?
- [ ] RAISE NOTICE für Skip-Fall vorhanden?
- [ ] In CI-Pipeline getestet?

---

## 🔐 CI-Pipeline Guard Verification ✅ NEU

### In CI-Pipeline hinzufügen für zusätzliche Sicherheit:
```yaml
# .github/workflows/backend-ci.yml
- name: Verify CI guard flag is set
  run: |
    psql -h localhost -U freshplan -d freshplan \
         -c "SELECT current_setting('ci.build', true);" | grep -q "true" || \
    (echo "ERROR: ci.build flag not set in CI!" && exit 1)
```

Dies stellt sicher, dass das ci.build Flag in der CI-Umgebung IMMER korrekt gesetzt ist.

---

**Diese Konvention ist VERBINDLICH für alle Team-Mitglieder!**