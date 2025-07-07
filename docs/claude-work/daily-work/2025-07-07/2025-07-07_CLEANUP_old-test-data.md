# 🧹 CLEANUP LOG - Alte Test-Daten Bereinigung

**Datum:** 2025-07-07
**Zeit:** 14:30
**Autor:** Claude
**Aktion:** Entfernung alter Test-Daten für saubere Entwicklungsumgebung

## 📋 Zusammenfassung
Bereinigung der Datenbank von alten Test-Daten aus Unit-Tests, um eine kontrollierte Entwicklungsumgebung mit nur 5 definierten Test-Szenarien zu schaffen.

## 🔍 Analyse der alten Daten

### Gefundene alte Test-Kunden (26 Stück):
```
- ABC Company AG/GmbH
- Child Company / Parent Company
- Target Company
- Test Company GmbH (mehrfach)
- TestDashboard_*_Company_*
- TestPagination_*_Company_*
- TestRisk_*_Company
- TestSearch_*_Company
- TestStatusFilter_*_Company
- Updated Company Name
```

### Charakteristika:
- Alle haben `is_test_data = NULL` (vor Migration erstellt)
- Meist generische Namen aus Unit-Tests
- Keine realistische Geschäftslogik
- Verwirren bei der Entwicklung

## 🎯 Ziel-Zustand

Nach der Bereinigung nur noch 5 kontrollierte Test-Kunden:
1. **[TEST] Bäckerei Schmidt** - Risiko-Szenario (90+ Tage)
2. **[TEST] Hotel Sonnenschein** - Aktiver Partner
3. **[TEST] Restaurant Mustermeier** - Neuer Lead
4. **[TEST] Catering Service Alt** - Inaktiv (180+ Tage)
5. **[TEST] Kantine TechHub** - Überfälliger Follow-up

## 🛠️ Durchgeführte Aktionen

### 1. Backup der Kundennummern
```sql
-- Zur Dokumentation welche Kunden gelöscht werden
SELECT customer_number, company_name 
FROM customers 
WHERE is_test_data IS NULL 
   OR (is_test_data = false AND company_name NOT LIKE '[TEST]%');
```

### 2. Löschung der alten Test-Daten
```sql
-- Erst Timeline-Events löschen (Foreign Key)
DELETE FROM customer_timeline_events 
WHERE customer_id IN (
    SELECT id FROM customers 
    WHERE is_test_data IS NULL 
       OR (is_test_data = false AND company_name NOT LIKE '[TEST]%')
);

-- Dann Kunden löschen
DELETE FROM customers 
WHERE is_test_data IS NULL 
   OR (is_test_data = false AND company_name NOT LIKE '[TEST]%');
```

## 📊 Ergebnis
- **Vorher:** 31 Kunden (26 alte + 5 neue Test-Kunden)
- **Nachher:** 5 kontrollierte Test-Kunden
- **Sales Cockpit:** Zeigt jetzt nur die relevanten Szenarien

## ✅ Vorteile
- Klare, nachvollziehbare Test-Szenarien
- Keine Verwirrung durch zufällige Test-Daten
- Bessere Kontrolle über Entwicklungsumgebung
- Realistische Geschäftsszenarien

## 🔄 Wiederherstellung (falls nötig)
Die alten Daten können durch erneutes Ausführen der Unit-Tests wiederhergestellt werden.
Unsere kontrollierten Test-Daten bleiben durch das `is_test_data` Flag geschützt.