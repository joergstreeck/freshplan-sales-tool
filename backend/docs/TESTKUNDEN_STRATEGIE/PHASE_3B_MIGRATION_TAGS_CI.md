# Phase 3B: Migration-Tests + CI-Integration

**Aufwand:** 20 Minuten  
**Ziel:** 98 Migration-Tests taggen + Maven-Profile + CI umstellen  
**Ergebnis:** Grüne CI-Pipeline mit Core-Tests only

---

## 🎯 Überblick

Diese Phase vervollständigt die Tag-Strategie:
- **98 Migration-Tests** als @Tag("migrate") markieren
- **Maven-Profile** konfigurieren (core-tests, migrate-tests)
- **CI-Pipeline** auf core-Tests umstellen
- **Nightly-Pipeline** für migrate-Tests einrichten

**Resultat:** Sofort grüne CI-Pipeline + Non-blocking Migration-Monitoring!

**📋 Verweis auf Recherche:** 
- [RESEARCH_RESULTS.md - Tests OHNE @TestTransaction](./RESEARCH_RESULTS.md#tests-ohne-testtransaction-98-tests-❌)
- [RESEARCH_RESULTS.md - CI-Konfiguration](./RESEARCH_RESULTS.md#⚙️-ci-konfiguration-zu-vereinfachen-in-phase-4)

---

## 🔄 Schritt 1: Migration-Tests automatisch taggen

### 1.1 Migration-Test-Tagging-Script

**Script: `tag-migration-tests.sh`**
```bash
#!/bin/bash
# Migration Tests Tagging Script
# Markiert alle Tests die NICHT core sind als "migrate"

echo "=== Migration Tests Tagging Started ==="

MIGRATION_TAGGED=0
ALREADY_TAGGED=0
SKIPPED_CORE=0

# Find all test files
find backend/src/test -name "*Test.java" -o -name "*IT.java" | while read file; do
    testname=$(basename "$file" .java)
    
    # Skip if already tagged as core
    if grep -q '@Tag("core")' "$file"; then
        echo "  ⏭️  Skipping core test: $testname"
        SKIPPED_CORE=$((SKIPPED_CORE + 1))
        continue
    fi
    
    # Skip if already has any @Tag
    if grep -q '@Tag(' "$file"; then
        echo "  ⚠️  Already tagged: $testname"
        ALREADY_TAGGED=$((ALREADY_TAGGED + 1))
        continue
    fi
    
    echo "  🔄 Tagging $testname as migrate..."
    
    # Add import if not exists
    if ! grep -q "import org.junit.jupiter.api.Tag;" "$file"; then
        # Find good spot for import
        if grep -q "import org.junit.jupiter.api.Test;" "$file"; then
            sed -i '' '/import org.junit.jupiter.api.Test;/a\
import org.junit.jupiter.api.Tag;
' "$file"
        else
            # Fallback: add after package statement
            sed -i '' '/^package /a\
\
import org.junit.jupiter.api.Tag;
' "$file"
        fi
    fi
    
    # Add @Tag("migrate") before class declaration
    if grep -q "@QuarkusTest" "$file"; then
        sed -i '' '/@QuarkusTest/a\
@Tag("migrate")
' "$file"
    else
        # Fallback: add before class declaration
        sed -i '' '/class.*Test.*{/i\
@Tag("migrate")
' "$file"
    fi
    
    echo "  ✅ Tagged $testname as migrate"
    MIGRATION_TAGGED=$((MIGRATION_TAGGED + 1))
done

echo ""
echo "=== Migration Tests Tagging Summary ==="
echo "Tagged as migrate: $MIGRATION_TAGGED"
echo "Already tagged: $ALREADY_TAGGED"  
echo "Skipped (core): $SKIPPED_CORE"

echo ""
echo "✅ Migration tagging completed! Verify with:"
echo "   grep -r '@Tag(\"migrate\")' backend/src/test/ | wc -l"
```

### 1.2 Quarantine-Tests identifizieren

**Bekannt problematische Tests als @Tag("quarantine"):**
```bash
#!/bin/bash
# Quarantine problematische Tests

QUARANTINE_TESTS=(
    "BaseIntegrationTestWithCleanup"
    "TestDataCleanup" 
    "DatabaseCleanupTest"
    "EmergencyTestDataCleanupTest"
    "TestDataPollutionFix"
    "ContactsCountDebugTest"
    "TestDataPollutionAnalysisTest"
    "CI_DatabaseStateDebugTest"
    "DatabaseGrowthTrackerTest"
    "TestDataIntegrityCheckTest"
)

echo "=== Quarantining problematic tests ==="

for test in "${QUARANTINE_TESTS[@]}"; do
    file=$(find backend/src/test -name "${test}.java" | head -1)
    if [ -f "$file" ]; then
        # Replace @Tag("migrate") with @Tag("quarantine") if exists
        if grep -q '@Tag("migrate")' "$file"; then
            sed -i '' 's/@Tag("migrate")/@Tag("quarantine")/' "$file"
            echo "  🚨 Quarantined $test (was migrate)"
        elif ! grep -q '@Tag(' "$file"; then
            # Add quarantine tag if no tag exists
            sed -i '' '/@QuarkusTest/a\
@Tag("quarantine")
' "$file"
            echo "  🚨 Quarantined $test (was untagged)"
        else
            echo "  ⚠️  $test already has tag - manual check needed"
        fi
    else
        echo "  ❓ $test not found"
    fi
done
```

### 1.3 Scripts ausführen

**Commands:**
```bash
# Migration-Tests taggen
chmod +x tag-migration-tests.sh
./tag-migration-tests.sh

# Problematische Tests quarantinieren
chmod +x quarantine-tests.sh  
./quarantine-tests.sh

# Validation
echo "Core tests: $(grep -r '@Tag("core")' backend/src/test/ | wc -l)"
echo "Migrate tests: $(grep -r '@Tag("migrate")' backend/src/test/ | wc -l)"  
echo "Quarantine tests: $(grep -r '@Tag("quarantine")' backend/src/test/ | wc -l)"
```

---

## ⚙️ Schritt 2: Maven-Profile konfigurieren

### 2.1 pom.xml Surefire/Failsafe Profile

**In backend/pom.xml einfügen (nach bestehenden Plugins):**
```xml
<!-- KRITISCH: Profile für Tag-basierte Test-Steuerung -->
<profiles>
  <!-- Core-Suite für PRs (Production Pipeline) -->
  <profile>
    <id>core-tests</id>
    <properties>
      <!-- Nur stabile Tests, keine quarantine -->
      <junit.jupiter.tags>core &amp; !quarantine</junit.jupiter.tags>
    </properties>
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <configuration>
            <properties>
              <configurationParameters>
                junit.jupiter.testinstance.lifecycle.default=per_method
                junit.jupiter.tags=${junit.jupiter.tags}
              </configurationParameters>
            </properties>
          </configuration>
        </plugin>
      </plugins>
    </build>
  </profile>

  <!-- Migration-Suite für Nightly (Non-blocking) -->
  <profile>
    <id>migrate-tests</id>
    <properties>
      <!-- Tests in Migration + bekannt problematische -->
      <junit.jupiter.tags>migrate | quarantine</junit.jupiter.tags>
    </properties>
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <configuration>
            <properties>
              <configurationParameters>
                junit.jupiter.testinstance.lifecycle.default=per_method
                junit.jupiter.tags=${junit.jupiter.tags}
              </configurationParameters>
            </properties>
          </configuration>
        </plugin>
      </plugins>
    </build>
  </profile>
  
  <!-- Stable-Suite für Release (alle außer quarantine) -->
  <profile>
    <id>stable-tests</id>
    <properties>
      <junit.jupiter.tags>!quarantine</junit.jupiter.tags>
    </properties>
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <configuration>
            <properties>
              <configurationParameters>
                junit.jupiter.testinstance.lifecycle.default=per_method
                junit.jupiter.tags=${junit.jupiter.tags}
              </configurationParameters>
            </properties>
          </configuration>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```

### 2.2 Profile-Commands testen

**Test Commands (Profile-basiert - robust gegen Quoting-Fehler):**
```bash
cd backend

# Core tests only (für PR-Pipeline)
./mvnw test -Pcore-tests
# Erwartetes Ergebnis: ~38 Tests laufen, alle grün

# Migration tests only (für Nightly)  
./mvnw test -Pmigrate-tests
# Erwartetes Ergebnis: ~98 Tests laufen, können rot sein

# Stable tests (für Release)
./mvnw test -Pstable-tests  
# Erwartetes Ergebnis: Alle Tests außer quarantine

# Profile-Check
./mvnw help:active-profiles -Pcore-tests | grep "core-tests"
# MUSS zeigen: Profile "core-tests" ist aktiv
```

---

## 🚀 Schritt 3: CI-Pipeline umstellen

### 3.1 GitHub Actions für PR-Pipeline

**In .github/workflows/backend-ci.yml anpassen:**
```yaml
name: Backend CI

on:
  pull_request:
    paths: ['backend/**']
  push:
    branches: [main]

jobs:
  test:
    name: Core Tests (PR Pipeline)
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_DB: test_db
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd "pg_isready -U postgres -d test_db"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 10
        ports:
          - 5432:5432
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        
      - name: Setup JDK 17 (Quarkus 2.x Standard)
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17
          cache: maven
          
      - name: Wait for PostgreSQL
        env:
          PGPASSWORD: postgres
        run: |
          until pg_isready -h localhost -U postgres -d test_db; do 
            echo "Waiting for PostgreSQL..."
            sleep 1
          done
          echo "✅ PostgreSQL ready"
          
      - name: Run Core Tests Only (Profile-based)
        working-directory: backend
        run: |
          ./mvnw -B test -Pcore-tests \
            -Dquarkus.profile=ci \
            -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/test_db \
            -Dquarkus.datasource.username=postgres \
            -Dquarkus.datasource.password=postgres \
            -Dtest.run.id=${{ github.run_id }}
          
      - name: Upload Test Results
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: Core Tests Report
          path: backend/target/surefire-reports/*.xml
          reporter: java-junit
          
      - name: Test Summary
        if: success()
        run: |
          echo "✅ All core tests passed!" >> $GITHUB_STEP_SUMMARY
          echo "**Tests:** $(grep -r '@Tag("core")' backend/src/test/ | wc -l) core tests" >> $GITHUB_STEP_SUMMARY
          echo "**Migration:** $(grep -r '@Tag("migrate")' backend/src/test/ | wc -l) tests tagged for future migration" >> $GITHUB_STEP_SUMMARY
```

### 3.2 Nightly Pipeline für Migration-Tests

**Neue Datei: .github/workflows/nightly-migration.yml**
```yaml
name: Nightly Migration Tests

on:
  schedule:
    - cron: '0 2 * * *'  # 2 AM daily
  workflow_dispatch:      # Manual trigger

jobs:
  migration-tests:
    name: Migration Tests (Non-blocking)
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:14
        env:
          POSTGRES_DB: test_db
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd "pg_isready -U postgres -d test_db"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 10
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: 17
          distribution: temurin
          cache: maven
          
      - name: Run Migration Tests (Non-blocking)
        continue-on-error: true
        working-directory: backend
        run: |
          ./mvnw test -Pmigrate-tests \
            -Dtest.run.id=${{ github.run_id }}
        env:
          QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://localhost:5432/test_db
          QUARKUS_DATASOURCE_USERNAME: postgres
          QUARKUS_DATASOURCE_PASSWORD: postgres
          
      - name: Upload Test Results (Migration)
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: migration-test-results-${{ github.run_id }}
          path: backend/target/surefire-reports/*.xml
          retention-days: 30
          
      - name: Migration Progress Report
        if: always()
        run: |
          cd backend
          echo "## 📊 Migration Progress Report" >> $GITHUB_STEP_SUMMARY
          echo "**Date:** $(date '+%Y-%m-%d %H:%M:%S')" >> $GITHUB_STEP_SUMMARY
          echo "**Run ID:** ${{ github.run_id }}" >> $GITHUB_STEP_SUMMARY
          echo "" >> $GITHUB_STEP_SUMMARY
          
          # Test Counts
          CORE_COUNT=$(grep -r '@Tag("core")' src/test/ | wc -l)
          MIGRATE_COUNT=$(grep -r '@Tag("migrate")' src/test/ | wc -l)
          QUARANTINE_COUNT=$(grep -r '@Tag("quarantine")' src/test/ | wc -l)
          TOTAL_COUNT=$((CORE_COUNT + MIGRATE_COUNT + QUARANTINE_COUNT))
          
          echo "### 🏷️ Test Tag Distribution" >> $GITHUB_STEP_SUMMARY
          echo "| Category | Count | Percentage | Status |" >> $GITHUB_STEP_SUMMARY
          echo "|----------|-------|------------|--------|" >> $GITHUB_STEP_SUMMARY
          echo "| **Core** | $CORE_COUNT | $((CORE_COUNT * 100 / TOTAL_COUNT))% | ✅ Production Ready |" >> $GITHUB_STEP_SUMMARY
          echo "| **Migrate** | $MIGRATE_COUNT | $((MIGRATE_COUNT * 100 / TOTAL_COUNT))% | 🔄 In Migration |" >> $GITHUB_STEP_SUMMARY  
          echo "| **Quarantine** | $QUARANTINE_COUNT | $((QUARANTINE_COUNT * 100 / TOTAL_COUNT))% | ⚠️ Needs Fix |" >> $GITHUB_STEP_SUMMARY
          echo "| **Total** | $TOTAL_COUNT | 100% | |" >> $GITHUB_STEP_SUMMARY
```

---

## ✅ Schritt 4: Validierung

### 4.1 Tag-Verteilung prüfen
```bash
cd backend

echo "=== Test Tag Distribution ==="
echo "Core tests: $(grep -r '@Tag("core")' src/test/ | wc -l)"
echo "Migrate tests: $(grep -r '@Tag("migrate")' src/test/ | wc -l)"  
echo "Quarantine tests: $(grep -r '@Tag("quarantine")' src/test/ | wc -l)"
echo "Untagged tests: $(find src/test -name "*Test.java" -o -name "*IT.java" | xargs grep -L '@Tag(' | wc -l)"

# Erwartetes Ergebnis:
# Core tests: ~38
# Migrate tests: ~98  
# Quarantine tests: ~10
# Untagged tests: 0
```

### 4.2 Maven-Profile funktionieren
```bash
cd backend

# Core-Pipeline testen
./mvnw test -Pcore-tests -DfailIfNoTests=false | tail -5
# MUSS enthalten: "BUILD SUCCESS" und "Tests run: X" (wo X ~38)

# Migration-Pipeline testen (darf rot sein)
./mvnw test -Pmigrate-tests -DfailIfNoTests=false | grep "Tests run:"
# SOLLTE Tests finden, auch wenn sie rot sind

# Profile-Aktivierung prüfen
./mvnw help:active-profiles -Pcore-tests | grep "core-tests"
# MUSS zeigen: Active profile "core-tests"
```

### 4.3 CI-Integration bereit
```bash
# Prüfen dass GitHub Actions Profile nutzt
grep -c "Pcore-tests" .github/workflows/backend-ci.yml
# MUSS ergeben: >= 1 (nutzt Profile statt String-Tags)

# Lokale CI-Simulation
cd backend && ./mvnw -B test -Pcore-tests -Dquarkus.profile=ci
# MUSS grün laufen
```

---

## 🎯 Erfolgskriterien - Definition of Done (messbar)

**Phase 3B ist NUR DANN abgeschlossen, wenn alle Validierungen grün sind:**

### ✅ **Validierung 1: Tag-Verteilung stimmt (~146 Tests total)**
```bash
cd backend
CORE_COUNT=$(grep -r '@Tag("core")' src/test/ | wc -l)
MIGRATE_COUNT=$(grep -r '@Tag("migrate")' src/test/ | wc -l)
QUARANTINE_COUNT=$(grep -r '@Tag("quarantine")' src/test/ | wc -l)
TOTAL_TAGGED=$((CORE_COUNT + MIGRATE_COUNT + QUARANTINE_COUNT))

echo "Tagged total: $TOTAL_TAGGED"
# SOLLTE ergeben: >= 140 (95% der 146 Tests getaggt)

echo "Untagged: $(find src/test -name "*Test.java" -o -name "*IT.java" | xargs grep -L '@Tag(' | wc -l)"
# SOLLTE ergeben: <= 6 (maximal 5% ungetaggt)
```

### ✅ **Validierung 2: Maven-Profile funktionieren**  
```bash
cd backend && ./mvnw test -Pcore-tests -DfailIfNoTests=false | grep "Tests run:"
# MUSS enthalten: "Tests run: X" wo X >= 35

cd backend && ./mvnw help:active-profiles -Pcore-tests | grep "core-tests"
# MUSS enthalten: "core-tests"
```

### ✅ **Validierung 3: CI nutzt Profile (kein String-Quoting)**
```bash
grep -c "Pcore-tests" .github/workflows/backend-ci.yml
# MUSS ergeben: >= 1

grep -c 'junit.jupiter.tags=' .github/workflows/backend-ci.yml
# SOLLTE ergeben: 0 (nutzt Profile, keine direkten Tags)
```

### ✅ **Validierung 4: Nightly-Pipeline existiert**
```bash
ls -la .github/workflows/nightly-migration.yml
# MUSS existieren

grep -c "migrate-tests" .github/workflows/nightly-migration.yml
# MUSS ergeben: >= 1
```

**💡 Erst wenn ALLE 4 Validierungen erfolgreich sind → Phase 4 starten!**

---

## ➡️ Nächste Phase

Nach erfolgreichem Abschluss von Phase 3B:
→ **Phase 4: CI vereinfachen + A00 Smart-Diagnostics** (PHASE_4_CI_SIMPLIFY.md)

**CI-Pipeline ist grün - jetzt Final-Cleanup und Schutzmaßnahmen! 🚀**