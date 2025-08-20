# 🎯 Test-Migration Strategie: SEED-Rollback zu TestDataBuilder

**Datum:** 2025-08-19  
**Zweck:** Übergeordnete Roadmap für schrittweise Migration in kleinen PRs  
**Ziel:** Sofort grüne CI + langfristige Test-Qualität

---

## 📋 Migrations-Übersicht

### Problem Statement
- **SEED-Kunden-Strategie** ist over-engineered für Solo-Development
- **CI instabil** durch komplexe Daten-Dependencies zwischen Test-Läufen
- **146 Tests** müssen von SEED-Dependencies auf TestDataBuilder umgestellt werden
- **Sofortige Lösung nötig** für stabile Development-Pipeline

### Strategischer Ansatz: "Minimal Package First"
1. **Sofort grüne CI** mit 38 bereits sauberen Tests
2. **Schrittweise Migration** der restlichen 108 Tests in kleinen PRs
3. **Kein Großumbau** - bestehende Tests bleiben committet
4. **Tag-basierte Steuerung** für flexible CI-Pipeline

---

## 🚀 6-Phasen-Roadmap (Claude-optimiert)

### Phase 1: SEED-Infrastruktur entfernen
**Aufwand:** 30 Minuten  
**Ergebnis:** Komplexität sofort reduziert
- 7 SQL-Dateien löschen
- 4 Java-Klassen bereinigen  
- CI-Workflow vereinfachen
- Flyway nur noch `db/migration`

### Phase 2A: Builder Core-Verbesserungen
**Aufwand:** 25 Minuten  
**Ergebnis:** Stabile Basis-Builder
- CustomerTestDataFactory + UserTestDataFactory stärken
- Kollisionsfreie ID-Generierung (KD-TEST-...)
- `isTestData=true` durchgängig setzen
- Realistische Test-Daten generieren

### Phase 2B: Builder Advanced Features
**Aufwand:** 20 Minuten  
**Ergebnis:** Vollständige Builder-Suite
- ContactTestDataFactory + OpportunityTestDataFactory
- Persist-Methoden implementieren
- CDI-Integration für Integration-Tests
- Validation und Error-Handling

### Phase 3A: Core-Tests taggen
**Aufwand:** 25 Minuten  
**Ergebnis:** Stabile Test-Basis erkannt
- 38 Core-Tests mit `@Tag("core")` markieren
- Script-basiertes automatisches Tagging
- Erste Validierung der Core-Pipeline
- Import-Statements korrekt setzen

### Phase 3B: Migration-Tests + CI-Integration
**Aufwand:** 20 Minuten  
**Ergebnis:** Grüne CI-Pipeline
- 98 Tests als `@Tag("migrate")` markieren
- Maven-Profile konfigurieren (core-tests, migrate-tests)
- CI-Pipeline auf core-Tests umstellen
- Nightly-Pipeline für migrate-Tests

### Phase 4: CI vereinfachen + A00 Smart-Diagnostics
**Aufwand:** 15 Minuten
**Ergebnis:** Einfache, wartbare Pipeline mit früher Fehlerdiagnose
- Komplexe SEED-Validierung entfernen
- Single Maven-Run statt Multi-Step
- A00 Smart-Diagnostics implementieren
- Pre-Commit-Hooks für Cleanup-Verbote

---

## 📊 Test-Kategorisierung (146 Tests Total)

### ✅ Core Tests (38 Tests) - Bereits Builder-Ready
```
Kategorie: @Tag("core")
Status: Sofort grün lauffähig
Charakteristika:
- Nutzen bereits Builder/Factory-Injection
- Haben @TestTransaction oder korrekte Isolation  
- Keine problematischen Cleanup-Patterns
```

### 🔄 Migration Tests (98 Tests) - Benötigen @TestTransaction
```
Kategorie: @Tag("migrate") 
Status: Schrittweise Migration in kleinen PRs
Charakteristika:
- Nutzen manuelle Cleanup-Strategien
- Fehlt @TestTransaction für Auto-Rollback
- Potential für einfache Builder-Migration
```

### ⚠️ Quarantine Tests (10 Tests) - Komplexe Probleme
```
Kategorie: @Tag("quarantine")
Status: Separate Analyse nötig
Charakteristika:
- Verwenden DELETE FROM customers ohne Filter
- TRUNCATE Statements
- Komplexe Base-Class-Dependencies
```

---

## 🎛️ Tag-basierte CI-Steuerung

### PR-Pipeline (Production)
```bash
./mvnw test -Dgroups="core" -DexcludedGroups="quarantine"
```
- **Muss grün sein** für Merge
- Läuft nur stabile 38 Tests
- Schnell (< 2 Minuten)

### Nightly Pipeline (Monitoring)
```bash  
./mvnw test -Dgroups="migrate" -DfailIfNoTests=false
```
- **Darf rot sein** (non-blocking)
- Überwacht Migration-Progress
- Reports zeigen Fortschritt

### Full Pipeline (Optional)
```bash
./mvnw test -DexcludedGroups="quarantine"
```
- Alle Tests außer bekannt problematischen
- Für Release-Validierung

---

## 🛣️ Langfristige Migration-Roadmap

### Sofort (Phase 1-4): CI stabilisieren
- **Zeitaufwand:** 2-3 Stunden
- **Ergebnis:** Stabile Development-Pipeline
- **Nutzen:** Team kann produktiv arbeiten

### Wöchentlich: Kleine Migration-PRs
- **Pro PR:** 5-10 Tests von "migrate" zu "core"
- **Aufwand:** 1-2 Stunden/Woche  
- **Fortschritt:** ~10 Tests/Woche = 10 Wochen für alle

### Monatlich: Quality Gates verstärken
- Pre-Commit-Hooks erweitern
- Test-Coverage-Monitoring
- Performance-Regression-Tests

---

## 📋 Phasen-Dokumente für Claude

### Arbeitsweise für neuen Claude:
1. **Linear durch Phasen** - keine Sprünge zwischen Dokumenten
2. **Komplette Dateilisten** - keine Recherche nötig  
3. **Copy-Paste-Commands** - minimale Interpretation
4. **Klare Validierung** - Erfolg messbar

### Dokument-Struktur:
```
PHASE_1_SEED_ROLLBACK.md        - SEED-Infrastruktur entfernen
PHASE_2A_BUILDER_CORE.md        - Core Builder stärken (Customer, User)
PHASE_2B_BUILDER_ADVANCED.md    - Advanced Builder (Contact, Opportunity, CDI)  
PHASE_3A_CORE_TAGS.md           - Core-Tests taggen (38 Tests)
PHASE_3B_MIGRATION_TAGS_CI.md   - Migration-Tags + CI umstellen
PHASE_4_CI_SIMPLIFY.md          - CI vereinfachen + A00 Diagnostics
```

---

## 🎯 Success Metrics

### Sofort-Erfolg (nach Phase 1-4):
- ✅ CI-Pipeline grün bei PR
- ✅ 38 Tests laufen stabil  
- ✅ Keine SEED-Dependencies mehr
- ✅ Build-Zeit < 2 Minuten

### Langfrist-Erfolg (nach vollständiger Migration):
- ✅ 146 Tests mit TestDataBuilder
- ✅ Test-Coverage > 80%
- ✅ Keine manuellen Cleanup-Patterns
- ✅ Entwickler-Produktivität gestiegen

---

## 🚨 Kritische Erfolgsfaktoren

1. **Phase-Reihenfolge einhalten** - Jede Phase baut auf vorheriger auf
2. **Kleine PRs** - Max 10 Tests pro Migration-PR
3. **Tag-Disziplin** - Jeden neuen Test korrekt taggen
4. **Pre-Commit-Hooks** - Verhindern Rückfall in alte Muster
5. **Progress-Monitoring** - Wöchentliche Migration-Reports

**➡️ Bereit für Erstellung der 4 Phasen-Dokumente**