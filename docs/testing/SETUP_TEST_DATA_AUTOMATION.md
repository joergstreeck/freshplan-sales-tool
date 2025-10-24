# Test-Daten Automation Setup

**Sprint 2.1.7.2 - Test-Daten Management System**
**Erstellt:** 2025-10-24
**Version:** 1.0.0

---

## 📚 ÜBERBLICK

Dieses Dokument beschreibt die Installation und Nutzung des **automatisierten Test-Daten Management Systems** für FreshPlan.

**Das System besteht aus 3 Stufen:**

### Stufe 1: Dokumentation (IMMER AKTIV)
- ✅ `docs/testing/TEST_DATA_SCENARIOS.md` - Szenarien-Matrix (69 Szenarien)
- ✅ `docs/testing/TEST_DATA_GUIDE.md` - Developer Quick Reference

### Stufe 2: Pre-Commit Hook (EMPFOHLEN)
- ✅ `scripts/pre-commit-test-data-check.sh` - Automatische Dokumentations-Prüfung
- ⚠️ Sanfte Erinnerung bei Entity/Enum/Migration-Änderungen
- 🚨 **SCHARFER Hook:** Blockiert Commits wenn TestDataService ohne Docs geändert wird

### Stufe 3: CI Coverage Check (OPTIONAL)
- ✅ `scripts/check-test-data-coverage.py` - Prüft ob alle Entities Test-Daten haben
- 🤖 Integration in CI Pipeline (GitHub Actions, GitLab CI, etc.)

---

## 🚀 INSTALLATION

### Schritt 1: Dokumentation prüfen (READY TO USE)

Die Dokumentation ist bereits erstellt und einsatzbereit:

```bash
# Szenarien-Matrix anschauen
cat docs/testing/TEST_DATA_SCENARIOS.md

# Developer Guide anschauen
cat docs/testing/TEST_DATA_GUIDE.md
```

**Keine Installation nötig!** ✅

---

### Schritt 2: Pre-Commit Hook installieren (EMPFOHLEN)

**Option A: Manuelle Installation**

```bash
# 1. Hook in .git/hooks/ kopieren
cp scripts/pre-commit-test-data-check.sh .git/hooks/pre-commit

# 2. Ausführbar machen
chmod +x .git/hooks/pre-commit

# 3. Testen
git add docs/testing/TEST_DATA_SCENARIOS.md
git commit -m "test: Pre-Commit Hook testen"
# → Hook sollte durchlaufen (keine Entity-Änderungen)
```

**Option B: Husky (Empfohlen für Teams)**

```bash
# 1. Husky installieren (falls noch nicht vorhanden)
npm install --save-dev husky

# 2. Husky initialisieren
npx husky install

# 3. Pre-Commit Hook hinzufügen
npx husky add .husky/pre-commit "sh scripts/pre-commit-test-data-check.sh"

# 4. Testen
git add docs/testing/TEST_DATA_SCENARIOS.md
git commit -m "test: Husky Pre-Commit Hook testen"
```

**Option C: Lefthook (Alternative zu Husky)**

```yaml
# lefthook.yml (im Project Root)
pre-commit:
  parallel: true
  commands:
    test-data-check:
      run: sh scripts/pre-commit-test-data-check.sh
      stage_fixed: true
```

```bash
# Installation
gem install lefthook
lefthook install

# Testen
git add docs/testing/TEST_DATA_SCENARIOS.md
git commit -m "test: Lefthook Pre-Commit Hook testen"
```

---

### Schritt 3: CI Coverage Check installieren (OPTIONAL)

**GitHub Actions:**

```yaml
# .github/workflows/test-data-coverage.yml
name: Test Data Coverage Check

on:
  push:
    branches: [ main, develop, feature/* ]
  pull_request:
    branches: [ main, develop ]

jobs:
  check-test-data-coverage:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.11'

      - name: Check Test Data Coverage
        run: |
          python3 scripts/check-test-data-coverage.py
```

**GitLab CI:**

```yaml
# .gitlab-ci.yml
test-data-coverage:
  stage: test
  image: python:3.11
  script:
    - python3 scripts/check-test-data-coverage.py
  only:
    - merge_requests
    - main
    - develop
```

**Lokaler Test:**

```bash
# Direkt ausführen
python3 scripts/check-test-data-coverage.py

# Erwartete Ausgabe (bei 100% Coverage):
# ✅ TEST-DATEN COVERAGE: 100%
```

---

## 📖 USAGE

### Szenario 1: Neue Entity hinzufügen

```bash
# 1. Entity entwickeln
vim backend/src/main/java/de/freshplan/domain/newfeature/entity/NewFeature.java

# 2. Migration erstellen
./scripts/get-next-migration.sh
# Ergibt z.B.: V10040
vim backend/src/main/resources/db/migration/V10040__add_new_feature_table.sql

# 3. Commit versuchen
git add backend/src/main/java/de/freshplan/domain/newfeature/entity/NewFeature.java
git add backend/src/main/resources/db/migration/V10040__add_new_feature_table.sql
git commit -m "feat: Add NewFeature entity"

# ⚠️  TESTDATEN-REMINDER:
#
#    Du hast Entities/Enums/Migrations geändert.
#    Bitte prüfe, ob TEST-DATEN aktualisiert werden müssen:
#
#    1. Szenarien-Matrix: docs/testing/TEST_DATA_SCENARIOS.md
#    2. TestDataService: Neue Szenarien hinzufügen?
#    3. Test-Daten-Guide: docs/testing/TEST_DATA_GUIDE.md
#
#    Testdaten geprüft? (y/n)

# 4. Entscheidung treffen:
#    - "y" → Commit geht durch (Test-Daten werden später hinzugefügt)
#    - "n" → Commit abgebrochen, Test-Daten jetzt hinzufügen
```

---

### Szenario 2: TestDataService erweitern (SCHARFER HOOK)

```bash
# 1. Test-Daten hinzufügen
vim backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java

# Füge hinzu:
/**
 * NEW_FEATURE-01: Beschreibung des Szenarios.
 *
 * <p>Abgedeckte Szenarien:
 * <ul>
 *   <li>FEAT-01: Feature XYZ mit Variante A</li>
 *   <li>FEAT-02: Feature XYZ mit Variante B</li>
 * </ul>
 */
private List<NewFeature> seedNewFeature() {
  // Implementation...
}

# 2. Commit OHNE Docs versuchen (WIRD BLOCKIERT)
git add backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java
git commit -m "feat: Add test data for NewFeature"

# 🚨 KRITISCH: TestDataService wurde geändert!
#
#    Du MUSST die Dokumentationen aktualisieren:
#
#    ❌ TEST_DATA_SCENARIOS.md nicht geändert
#    ❌ TEST_DATA_GUIDE.md nicht geändert
#
#    Commit BLOCKIERT. Bitte aktualisiere BEIDE Docs.

# 3. Docs aktualisieren
vim docs/testing/TEST_DATA_SCENARIOS.md
# → Szenario FEAT-01, FEAT-02 hinzufügen
# → Coverage-Tabelle aktualisieren

vim docs/testing/TEST_DATA_GUIDE.md
# → Quick Reference erweitern (welcher Test-Kunde hat was?)
# → Feature-Testing Matrix erweitern

# 4. Erneut committen (MIT Docs)
git add backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java
git add docs/testing/TEST_DATA_SCENARIOS.md
git add docs/testing/TEST_DATA_GUIDE.md
git commit -m "feat: Add test data for NewFeature with docs"

# ✅ TestDataService + Docs aktualisiert
# ✅ Commit erfolgreich
```

---

### Szenario 3: Hook überspringen (NOT RECOMMENDED)

```bash
# Wenn du den Hook überspringen MUSST (z.B. WIP-Commit):
git commit -m "WIP: Work in progress" --no-verify

# ⚠️  WICHTIG: Docs MÜSSEN vor dem finalen Merge aktualisiert werden!
```

---

## 🧪 TESTING

### Pre-Commit Hook testen

```bash
# Test 1: Sanfte Erinnerung (Entity geändert)
git add backend/src/main/java/de/freshplan/domain/customer/entity/Customer.java
git commit -m "test: Entity ändern"
# Erwartung: Reminder, aber durchlassen bei "y"

# Test 2: Scharfer Hook (TestDataService ohne Docs)
git add backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java
git commit -m "test: TestDataService ohne Docs"
# Erwartung: BLOCKIERT

# Test 3: Scharfer Hook (TestDataService MIT Docs)
git add backend/src/main/java/de/freshplan/domain/testdata/service/TestDataService.java
git add docs/testing/TEST_DATA_SCENARIOS.md
git add docs/testing/TEST_DATA_GUIDE.md
git commit -m "test: TestDataService mit Docs"
# Erwartung: ✅ Durchgelassen
```

### CI Coverage Check testen

```bash
# Lokal testen
python3 scripts/check-test-data-coverage.py

# Erwartete Ausgabe (bei vollständiger Coverage):
# ================================================================================
# 🧪 TEST-DATEN COVERAGE REPORT
# ================================================================================
#
# 📊 Coverage: 15/15 (100.0%)
#
# ✅ Alle kritischen Entities abgedeckt (8/8)
#
# ✅ Alle relevanten Entities haben Test-Daten
#
# ℹ️  Ausgeschlossene Entities (14):
#    - AuditEntry
#    - BudgetLimit
#    - ...
#
# ================================================================================
# ✅ TEST-DATEN COVERAGE: 100%
# ================================================================================
```

---

## 🔧 MAINTENANCE

### Hook deaktivieren (temporär)

```bash
# Option 1: --no-verify bei jedem Commit
git commit -m "message" --no-verify

# Option 2: Hook umbenennen (deaktiviert)
mv .git/hooks/pre-commit .git/hooks/pre-commit.disabled

# Option 3: Hook löschen
rm .git/hooks/pre-commit
```

### Hook reaktivieren

```bash
# Nach Option 2 (umbenennen):
mv .git/hooks/pre-commit.disabled .git/hooks/pre-commit

# Nach Option 3 (gelöscht):
cp scripts/pre-commit-test-data-check.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### Hook aktualisieren

```bash
# Wenn scripts/pre-commit-test-data-check.sh geändert wurde:
cp scripts/pre-commit-test-data-check.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

---

## ❓ FAQ

### Q: Warum wird mein Commit blockiert?

**A:** Der Pre-Commit Hook hat erkannt, dass du `TestDataService.java` geändert hast, aber **NICHT** beide Dokumentationen (`TEST_DATA_SCENARIOS.md` UND `TEST_DATA_GUIDE.md`) aktualisiert hast. Dies ist KRITISCH, weil sonst die Dokumentation veraltet und das System unwartbar wird.

**Lösung:** Aktualisiere BEIDE Docs und committe erneut.

---

### Q: Kann ich den Hook überspringen?

**A:** Ja, mit `git commit --no-verify`. **ABER:** Das ist **NOT RECOMMENDED**! Die Docs MÜSSEN synchron bleiben, sonst wird das System unwartbar für zukünftige Entwickler und Claude-Instanzen.

**Ausnahme:** WIP-Commits (Work in Progress). Aber vor dem finalen Merge MÜSSEN die Docs aktualisiert werden!

---

### Q: Was ist der Unterschied zwischen dem sanften und scharfen Hook?

**A:**
- **Sanft (Entity/Enum/Migration geändert):** Erinnerung, aber überspringbar mit "y". Du kannst Test-Daten später hinzufügen.
- **SCHARF (TestDataService geändert):** BLOCKIERT Commit, wenn Docs fehlen. Test-Daten und Docs MÜSSEN zusammen committed werden.

---

### Q: Warum zwei Dokumentationen (SCENARIOS + GUIDE)?

**A:**
- **TEST_DATA_SCENARIOS.md:** Vollständige Szenarien-Matrix, alle 69 Szenarien, Coverage-Tracking (für Architekten/Tech Leads)
- **TEST_DATA_GUIDE.md:** Developer-freundlicher Quick Reference, "Welcher Kunde hat was?" (für Entwickler/QA)

Beide haben unterschiedliche Zielgruppen, aber beide MÜSSEN synchron gehalten werden.

---

### Q: Was passiert wenn ich eine neue Entity erstelle?

**A:**
1. **Sanfter Hook:** Erinnert dich, Test-Daten zu prüfen (überspringbar)
2. **CI Coverage Check:** Erkennt fehlende Entity (falls aktiviert)
3. **Du entscheidest:** Test-Daten jetzt oder später hinzufügen

---

### Q: Wie füge ich Test-Daten für eine neue Entity hinzu?

**A:** Siehe "Workflow für Entwickler" in `TEST_DATA_GUIDE.md`:

1. TestDataService erweitern (neue `seedXYZ()` Methode mit JavaDoc)
2. TEST_DATA_SCENARIOS.md aktualisieren (Szenario-ID, Details, Coverage)
3. TEST_DATA_GUIDE.md aktualisieren (Quick Reference, Feature-Testing Matrix)
4. Alle 3 Dateien zusammen committen → Hook lässt durch ✅

---

## 🎯 BEST PRACTICES

1. **Commit TestDataService + Docs zusammen:**
   - ✅ `git add TestDataService.java TEST_DATA_SCENARIOS.md TEST_DATA_GUIDE.md`
   - ✅ `git commit -m "feat: Add test data for XYZ with docs"`

2. **Docs aktuell halten:**
   - Bei JEDER Änderung an TestDataService auch Docs aktualisieren
   - Pre-Commit Hook hilft dabei (blockiert Commits wenn Docs fehlen)

3. **Coverage regelmäßig prüfen:**
   - `python3 scripts/check-test-data-coverage.py`
   - Target: >90% Coverage für kritische Entities

4. **Szenarien-Matrix ist Single Source of Truth:**
   - ALLE Szenarien sind dort dokumentiert
   - Bei Fragen → Szenarien-Matrix konsultieren

---

## 📞 SUPPORT

**Bei Problemen:**
1. Siehe FAQ oben
2. Siehe `TEST_DATA_GUIDE.md` - "Häufige Fragen"
3. Siehe `TEST_DATA_SCENARIOS.md` - "Neue Features hinzufügen"

**Verantwortlich:** FreshPlan Development Team
**Letzte Aktualisierung:** 2025-10-24
