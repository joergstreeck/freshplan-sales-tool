# 📊 MIGRATION BASELINE - Phase 3 Start

**Datum:** 17.08.2025 20:13  
**Branch:** feature/refactor-large-services  
**Erstellt von:** Claude

---

## 🎯 AUSGANGSLAGE VOR MIGRATION

### Gefundene Probleme:

| Kategorie | Anzahl | Status |
|-----------|--------|--------|
| **new Customer() in Tests** | 72 Vorkommen in 39 Dateien | ❌ ZU MIGRIEREN |
| **Direct persist() calls** | 22 Vorkommen | ⚠️ ZU PRÜFEN |
| **TestFixtures usage** | 2 Vorkommen | ⚠️ FACADE PATTERN |
| **Hardcoded [TEST-] patterns** | 8 Vorkommen | ⚠️ ZU PRÜFEN |

### Risiko-Kategorisierung:

| Risiko | Dateien | Details |
|--------|---------|---------|
| **HIGH** | 2 | CustomerMapperTest (9), CustomerQueryServiceIntegrationTest (8) |
| **MEDIUM** | 10 | 2-5 Vorkommen pro Datei |
| **LOW** | 27 | Nur 1 Vorkommen pro Datei |

### Test-Typen:

| Typ | Anzahl | Migration zu |
|-----|--------|--------------|
| **Integration-Tests** (@TestTransaction) | 13 | `.persist()` |
| **Unit-Tests** (ohne @TestTransaction) | 26 | `.build()` |

---

## ⚠️ GEFUNDENE PROBLEME

### 1. Audit-Script Bug
**Problem:** `audit-testdata-v2.sh` meldet fälschlicherweise 0 Vorkommen  
**Ursache:** Word boundary `\b` funktioniert nicht mit `(` Character  
**Pattern fehlerhaft:** `'new\s+Customer\b\s*\('`  
**Pattern korrekt:** `'new\s+Customer\s*\('` (ohne \b)  
**Status:** Muss korrigiert werden vor produktivem Einsatz

### 2. Direct persist() Calls
22 direkte persist() Aufrufe außerhalb TestDataBuilder gefunden.  
Diese müssen analysiert werden - könnten legitim sein oder migriert werden müssen.

---

## 📋 PILOT-TEST KANDIDAT

**ContactCommandServiceTest**
- Datei: `src/test/java/de/freshplan/domain/customer/service/command/ContactCommandServiceTest.java`
- Zeile: 52
- Code: `mockCustomer = new Customer();`
- Typ: Unit-Test (KEIN @TestTransaction)
- Migration: `.build()` verwenden
- **Status:** ✅ IDEAL für Pilot-Test

---

## 🚀 MIGRATIONS-STRATEGIE

### Phase 1: Pilot (1 Datei)
- ContactCommandServiceTest

### Phase 2: Low-Risk Batch (27 Dateien)
- Alle Dateien mit nur 1 Vorkommen
- Semi-automatisiert möglich

### Phase 3: Medium-Risk Manual (10 Dateien)
- 2-5 Vorkommen pro Datei
- Manuelle Aufmerksamkeit nötig

### Phase 4: High-Risk Critical (2 Dateien)
- CustomerMapperTest (9 Vorkommen)
- CustomerQueryServiceIntegrationTest (8 Vorkommen)
- Sehr vorsichtig, vollständig manuell

---

## 📝 NOTIZEN

- TestDataBuilder ist bereits vollständig implementiert ✅
- Alle Migration-Scripts sind vorhanden ✅
- Dokumentation ist vollständig ✅
- **Devise: Sicherheit geht vor Schnelligkeit!** 🛡️

---

**Dieser Baseline-Report dokumentiert den exakten Zustand vor Beginn der Migration.**