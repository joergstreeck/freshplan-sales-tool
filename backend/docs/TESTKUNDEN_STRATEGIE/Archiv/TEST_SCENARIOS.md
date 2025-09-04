# 🧪 Test-Szenarien für Test-Daten-Migrationen

**Version:** 1.0  
**Status:** READY FOR TESTING  
**Datum:** 17.08.2025  

---

## 📋 Test-Matrix

| Szenario | V10004 | V10005 | V10000 | V10001 | Erwartetes Ergebnis |
|----------|-------|-------|---------|---------|---------------------|
| **Szenario 1: CI Frischer Start** | ✓ | ✓ | ✓ | ✓ | 20 SEEDs, keine Warnings |
| **Szenario 2: CI mit 150+ Test-Daten** | ✓ | ✓ | ✓ | ✓ | Cleanup ausgelöst, Warning |
| **Szenario 3: Lokale Entwicklung** | Skip | Skip | Skip | ✓ | Nur Monitoring läuft |
| **Szenario 4: Duplikate SEEDs** | ✓ | ✓ | Skip | ✓ | Bereinigt, dann 20 SEEDs |
| **Szenario 5: Unmarked Test Data** | Skip | Skip | Skip | ✓ | Warning generiert |

---

## 🔬 Detaillierte Test-Szenarien

### Szenario 1: CI Frischer Start (Happy Path)
**Setup:**
```sql
-- Leere Datenbank
TRUNCATE customers CASCADE;
-- CI Flag setzen
SET ci.build = 'true';
```

**Ablauf:**
1. V10004 läuft → Nichts zu löschen
2. V10005 läuft → 20 SEEDs erstellt
3. V10000 läuft → Threshold nicht erreicht (20 < 100)
4. V10001 läuft → Alle Checks OK

**Validierung:**
```sql
-- Sollte genau 20 zurückgeben
SELECT COUNT(*) FROM customers WHERE is_test_data = true;
-- Sollte 'OK' zeigen
SELECT * FROM test_data_contract_status;
```

---

### Szenario 2: CI mit vielen Test-Daten
**Setup:**
```sql
-- 150 Test-Daten erstellen
INSERT INTO customers (id, customer_number, company_name, is_test_data, created_at)
SELECT 
    gen_random_uuid(),
    'TEST-' || LPAD(i::text, 3, '0'),
    '[TEST-' || LPAD(i::text, 3, '0') || '] Test Company',
    true,
    NOW() - INTERVAL '2 days'
FROM generate_series(1, 150) i;

SET ci.build = 'true';
```

**Ablauf:**
1. V10004 läuft → Keine spurious SEEDs
2. V10005 läuft → 20 SEEDs hinzugefügt/aktualisiert
3. V10000 läuft → **CLEANUP TRIGGERED** (170 > 100)
4. V10001 läuft → **WARNING**: High test data count

**Validierung:**
```sql
-- Sollte <= 100 sein nach Cleanup
SELECT COUNT(*) FROM customers WHERE is_test_data = true;
-- SEEDs sollten erhalten bleiben
SELECT COUNT(*) FROM customers WHERE customer_number LIKE 'SEED-%';
-- Sollte 20 sein
```

---

### Szenario 3: Lokale Entwicklung (ohne CI Flag)
**Setup:**
```sql
-- KEIN ci.build Flag gesetzt
SET ci.build = '';
-- Oder
RESET ci.build;
```

**Ablauf:**
1. V10004 → **SKIP** (Guard blockiert)
2. V10005 → **SKIP** (Guard blockiert)
3. V10000 → **SKIP** (Guard blockiert)
4. V10001 → **LÄUFT** (kein Guard, nur Monitoring)

**Validierung:**
```sql
-- Check Flyway Log
SELECT version, description, success 
FROM flyway_schema_history 
WHERE version IN ('9995', '9999', '10000', '10001')
ORDER BY installed_rank DESC;
```

---

### Szenario 4: Duplikate SEEDs (Healing)
**Setup:**
```sql
-- Fehlerhafte SEEDs erstellen
INSERT INTO customers (id, customer_number, company_name, is_test_data)
VALUES 
    (gen_random_uuid(), 'SEED-001', 'WRONG NAME', false),
    (gen_random_uuid(), 'SEED-002', 'ALSO WRONG', false),
    (gen_random_uuid(), 'SEED-021', 'SPURIOUS SEED', true);

SET ci.build = 'true';
```

**Ablauf:**
1. V10004 → Löscht SEED-021 (spurious)
2. V10005 → **DO UPDATE** heilt SEED-001, SEED-002
3. V10000 → Kein Cleanup nötig
4. V10001 → Checks OK

**Validierung:**
```sql
-- SEED-001 sollte korrigiert sein
SELECT company_name, is_test_data 
FROM customers 
WHERE customer_number = 'SEED-001';
-- Sollte '[SEED] Restaurant München', true zeigen

-- SEED-021 sollte nicht existieren
SELECT COUNT(*) 
FROM customers 
WHERE customer_number = 'SEED-021';
-- Sollte 0 sein
```

---

### Szenario 5: Unmarked Test Data Detection
**Setup:**
```sql
-- Test-Daten ohne is_test_data Flag
INSERT INTO customers (id, customer_number, company_name, is_test_data)
VALUES 
    (gen_random_uuid(), 'KD-2025-99999', '[TEST-999] Unmarked Test', false),
    (gen_random_uuid(), 'KD-2025-99998', '[SEED] Should be marked', false);
```

**Ablauf:**
1. V10001 läuft → **WARNING**: Found unmarked test data

**Validierung:**
```sql
-- Check Contract Status View
SELECT unmarked_test_data, contract_status 
FROM test_data_contract_status;
-- Sollte 2, 'WARNING: Unmarked test data found' zeigen
```

---

## 🔄 Parallel Execution Test
**Setup:**
```bash
# Terminal 1
psql -d freshplan -c "SET ci.build = 'true'; \i V10000.sql" &

# Terminal 2 (gleichzeitig)
psql -d freshplan -c "SET ci.build = 'true'; \i V10000.sql" &
```

**Erwartung:**
- Eine Instanz läuft durch
- Andere zeigt: "V10000: Skipped (another cleanup in progress)"
- Keine Deadlocks oder Fehler

---

## 🧪 Automated Test Script
```bash
#!/bin/bash
# test_migrations.sh

echo "=== Test Szenario 1: Frischer Start ==="
psql -d freshplan_test -f reset_database.sql
psql -d freshplan_test -c "SET ci.build = 'true';"
./mvnw flyway:migrate -Dquarkus.profile=test
psql -d freshplan_test -f validate_scenario_1.sql

echo "=== Test Szenario 2: Viele Test-Daten ==="
psql -d freshplan_test -f setup_scenario_2.sql
./mvnw flyway:migrate -Dquarkus.profile=test
psql -d freshplan_test -f validate_scenario_2.sql

# ... weitere Szenarien
```

---

## ✅ Acceptance Criteria

### Für V10004:
- [ ] Löscht nur spurious SEEDs
- [ ] Behält SEED-001 bis SEED-020
- [ ] Läuft nur mit ci.build=true

### Für V10005:
- [ ] Erstellt genau 20 SEEDs
- [ ] DO UPDATE korrigiert falsche Daten
- [ ] Idempotent (mehrfach ausführbar)

### Für V10000:
- [ ] Cleanup nur bei > 100 Test-Daten
- [ ] Löscht keine SEEDs
- [ ] Löscht nur alte Daten (> 1 Tag)
- [ ] Advisory Lock verhindert parallele Läufe

### Für V10001:
- [ ] Generiert Warnings, keine Exceptions
- [ ] Läuft auch ohne ci.build Flag
- [ ] Erkennt unmarked test data
- [ ] View zeigt korrekten Status

---

## 🐛 Known Edge Cases

1. **NULL created_at**: V10000 behandelt diese korrekt mit OR
2. **Pattern [TEST-]**: Literal in PostgreSQL, nicht Regex
3. **Parallel Migrations**: Advisory Lock in V10000 verhindert Konflikte
4. **Empty Database**: Alle Migrationen sollten sauber durchlaufen

---

## 📊 Test-Report Template

```markdown
# Test Report - Test Data Migrations
**Date:** [DATE]
**Tester:** [NAME]
**Environment:** [CI/Local/Stage]

## Results Summary
- Szenario 1: ✅/❌ [Notes]
- Szenario 2: ✅/❌ [Notes]
- Szenario 3: ✅/❌ [Notes]
- Szenario 4: ✅/❌ [Notes]
- Szenario 5: ✅/❌ [Notes]

## Issues Found
1. [Issue description]

## Recommendations
- [Action items]
```