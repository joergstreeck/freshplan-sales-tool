# Phase 3A: Core-Tests taggen

**Aufwand:** 25 Minuten  
**Ziel:** 38 stabile Tests als @Tag("core") markieren  
**Ergebnis:** Basis für sofort grüne CI-Pipeline

---

## 🎯 Überblick

Diese Phase identifiziert und taggt die 38 bereits stabilen Tests:
- **Automatisches Tagging** via Script
- **Import-Statements** korrekt setzen
- **Erste Validierung** der Core-Pipeline
- **Basis für grüne CI** legen

**Resultat:** Sofort lauffähige Core-Test-Suite ohne SEED-Dependencies!

**📋 Verweis auf Recherche:** Siehe [RESEARCH_RESULTS.md - Tests mit Builder/Factory-Injection](./RESEARCH_RESULTS.md#tests-mit-builderfactory-injection-bereits-gut-38-tests-✅)

---

## 🏷️ Schritt 1: Core-Tests identifizieren

### 1.1 Die 38 Core-Tests (Builder-ready)

**Copy-Paste Liste der bereits stabilen Tests:**
```bash
# Diese Tests nutzen bereits Builder/Factories und sind Core-ready:
ContactRepositoryTest
CustomerRepositoryTest  
CustomerResourceIntegrationTest
CustomerServiceIntegrationTest
CustomerCQRSIntegrationTest
OpportunityResourceIntegrationTest
OpportunityServiceIntegrationTest
UserRepoSaveLoadIT
CustomerCommandServiceIntegrationTest
ContactCommandServiceTest
ContactInteractionCommandServiceTest
CustomerCommandServiceTest
ContactInteractionQueryServiceTest
ContactQueryServiceTest
CustomerQueryServiceIntegrationTest
TimelineCommandServiceTest
TimelineQueryServiceTest
HtmlExportQueryServiceTest
HelpContentCQRSIntegrationTest
OpportunityEntityStageTest
OpportunityStageTest
OpportunityDatabaseIntegrationTest
OpportunityCQRSIntegrationTest
OpportunityServiceMockTest
OpportunityServiceStageTransitionTest
OpportunityCommandServiceTest
OpportunityMapperTest
OpportunityQueryServiceTest
SearchServiceTest
SearchQueryServiceTest
UserTest
UserRepositoryTest
UserServiceCQRSIntegrationTest
UserServiceRolesTest
UserServiceTest
UserCommandServiceTest
UserMapperTest
UserQueryServiceTest
```

### 1.2 Warum sind diese Tests "Core"?

**Charakteristika der Core-Tests:**
- ✅ Nutzen bereits Builder/Factory-Injection
- ✅ Haben @TestTransaction oder korrekte Isolation  
- ✅ Keine problematischen Cleanup-Patterns
- ✅ Keine SEED-Dependencies
- ✅ Laufen bereits stabil

---

## 🤖 Schritt 2: Automatisches Tagging via Script

### 2.1 Core-Test-Tagging-Script

**Script: `tag-core-tests.sh`**
```bash
#!/bin/bash
# Core Tests Tagging Script
# Automatisches Hinzufügen von @Tag("core") zu stabilen Tests

echo "=== Core Tests Tagging Started ==="

# Array der 38 Core-Tests
CORE_TESTS=(
    "ContactRepositoryTest"
    "CustomerRepositoryTest"  
    "CustomerResourceIntegrationTest"
    "CustomerServiceIntegrationTest"
    "CustomerCQRSIntegrationTest"
    "OpportunityResourceIntegrationTest"
    "OpportunityServiceIntegrationTest"
    "UserRepoSaveLoadIT"
    "CustomerCommandServiceIntegrationTest"
    "ContactCommandServiceTest"
    "ContactInteractionCommandServiceTest"
    "CustomerCommandServiceTest"
    "ContactInteractionQueryServiceTest"
    "ContactQueryServiceTest"
    "CustomerQueryServiceIntegrationTest"
    "TimelineCommandServiceTest"
    "TimelineQueryServiceTest"
    "HtmlExportQueryServiceTest"
    "HelpContentCQRSIntegrationTest"
    "OpportunityEntityStageTest"
    "OpportunityStageTest"
    "OpportunityDatabaseIntegrationTest"
    "OpportunityCQRSIntegrationTest"
    "OpportunityServiceMockTest"
    "OpportunityServiceStageTransitionTest"
    "OpportunityCommandServiceTest"
    "OpportunityMapperTest"
    "OpportunityQueryServiceTest"
    "SearchServiceTest"
    "SearchQueryServiceTest"
    "UserTest"
    "UserRepositoryTest"
    "UserServiceCQRSIntegrationTest"
    "UserServiceRolesTest"
    "UserServiceTest"
    "UserCommandServiceTest"
    "UserMapperTest"
    "UserQueryServiceTest"
)

TAGGED_COUNT=0
ALREADY_TAGGED=0
NOT_FOUND=0

for test in "${CORE_TESTS[@]}"; do
    echo "Processing $test..."
    
    # Find test file (both .java und IT.java patterns)
    file=$(find backend/src/test -name "${test}.java" -o -name "${test}IT.java" | head -1)
    
    if [ -f "$file" ]; then
        # Check if @Tag already exists
        if grep -q "@Tag(" "$file"; then
            echo "  ⚠️  $test already has @Tag - skipping"
            ALREADY_TAGGED=$((ALREADY_TAGGED + 1))
            continue
        fi
        
        # Add import if not exists
        if ! grep -q "import org.junit.jupiter.api.Tag;" "$file"; then
            # Find a good spot for import (after other junit imports)
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
            echo "  ✅ Added Tag import to $test"
        fi
        
        # Add @Tag("core") before class declaration
        # Look for @QuarkusTest or class declaration
        if grep -q "@QuarkusTest" "$file"; then
            sed -i '' '/@QuarkusTest/a\
@Tag("core")
' "$file"
        else
            # Fallback: add before class declaration
            sed -i '' '/class.*Test.*{/i\
@Tag("core")
' "$file"
        fi
        
        echo "  ✅ Tagged $test as core"
        TAGGED_COUNT=$((TAGGED_COUNT + 1))
    else
        echo "  ❌ $test not found"
        NOT_FOUND=$((NOT_FOUND + 1))
    fi
done

echo ""
echo "=== Core Tests Tagging Summary ==="
echo "Successfully tagged: $TAGGED_COUNT"
echo "Already tagged: $ALREADY_TAGGED"  
echo "Not found: $NOT_FOUND"
echo "Expected total: ${#CORE_TESTS[@]}"

if [ $TAGGED_COUNT -gt 0 ]; then
    echo ""
    echo "✅ Core tagging completed! Run validation:"
    echo "   grep -r '@Tag(\"core\")' backend/src/test/ | wc -l"
fi
```

### 2.2 Script ausführen

**Commands:**
```bash
# Script ausführbar machen und ausführen
chmod +x tag-core-tests.sh
./tag-core-tests.sh

# Erwartetes Ergebnis:
# Successfully tagged: 38 (oder weniger wenn einige schon getaggt)
# Already tagged: 0 (bei erster Ausführung)
# Not found: 0 (alle Tests sollten gefunden werden)
```

---

## 📝 Schritt 3: Manuelle Beispiele (Falls Script-Problem)

### 3.1 Beispiel 1 - CustomerRepositoryTest

**Falls Script nicht funktioniert, manuell:**
```java
package de.freshplan.domain.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;        // HINZUFÜGEN
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("core")                            // HINZUFÜGEN
class CustomerRepositoryTest {
    
    @Inject CustomerRepository repository;
    
    @Test
    @Transactional
    void shouldSaveAndLoad() {
        // Test bleibt gleich...
    }
}
```

### 3.2 Beispiel 2 - UserServiceTest

**Manual Pattern:**
```java
package de.freshplan.domain.user.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;        // HINZUFÜGEN
import org.junit.jupiter.api.Test;

@QuarkusTest  
@Tag("core")                            // HINZUFÜGEN
class UserServiceTest {
    // Tests bleiben gleich...
}
```

---

## ✅ Schritt 4: Erste Validierung

### 4.1 Tag-Count prüfen
```bash
# Prüfen dass Core-Tags gesetzt wurden
cd backend
grep -r '@Tag("core")' src/test/ | wc -l

# Erwartetes Ergebnis: ~38 (alle Core-Tests getaggt)
```

### 4.2 Import-Statements prüfen
```bash
# Prüfen dass Tag-Imports hinzugefügt wurden
grep -r "import org.junit.jupiter.api.Tag;" backend/src/test/ | wc -l

# Sollte >= 38 sein (alle getaggten Tests haben Import)
```

### 4.3 Core-Pipeline testen
```bash
# Test dass Core-Tests alleine laufen (ohne Maven-Profile erstmal)
cd backend
./mvnw test -Djunit.jupiter.tags="core" -DfailIfNoTests=false

# Erwartetes Ergebnis:
# Tests run: ~38, Failures: 0, Errors: 0, Skipped: 0
# BUILD SUCCESS
```

### 4.4 Syntax-Check
```bash
# Prüfen dass keine Syntax-Fehler durch Tagging entstanden
cd backend
./mvnw compile test-compile

# MUSS erfolgreich kompilieren
```

---

## 🔍 Schritt 5: Test-Qualität verifizieren

### 5.1 Core-Tests nutzen Builder-Pattern
```bash
# Prüfen dass Core-Tests Builder verwenden
grep -r "TestDataFactory\|Builder" backend/src/test/ | grep -f <(grep -l '@Tag("core")' backend/src/test/*) | wc -l

# Sollte > 20 sein (Core-Tests nutzen mehrheitlich Builder)
```

### 5.2 Core-Tests haben @TestTransaction oder korrekte Isolation
```bash
# Prüfen @TestTransaction in Core-Tests
for file in $(grep -l '@Tag("core")' backend/src/test/**/*.java); do
    if grep -q "@TestTransaction" "$file"; then
        echo "✅ $(basename $file) has @TestTransaction"
    else
        echo "⚠️  $(basename $file) might need @TestTransaction check"
    fi
done
```

### 5.3 Keine problematischen Cleanup-Patterns in Core-Tests
```bash
# Prüfen dass Core-Tests keine gefährlichen Patterns haben
for file in $(grep -l '@Tag("core")' backend/src/test/**/*.java); do
    if grep -q "deleteAll\|DELETE FROM\|TRUNCATE" "$file"; then
        echo "❌ $(basename $file) has dangerous cleanup pattern!"
    else
        echo "✅ $(basename $file) clean"
    fi
done
```

---

## 🎯 Erfolgskriterien - Definition of Done (messbar)

**Phase 3A ist NUR DANN abgeschlossen, wenn alle Validierungen grün sind:**

### ✅ **Validierung 1: Mindestens 35 Tests mit @Tag("core")**
```bash
grep -r '@Tag("core")' backend/src/test/ | wc -l
# MUSS ergeben: >= 35 (95% der 38 Core-Tests erfolgreich getaggt)
```

### ✅ **Validierung 2: Core-Pipeline läuft grün**  
```bash
cd backend && ./mvnw test -Djunit.jupiter.tags="core" -DfailIfNoTests=false | tail -10
# MUSS enthalten: "BUILD SUCCESS"
# MUSS enthalten: "Tests run: X" (wo X >= 35)
```

### ✅ **Validierung 3: Import-Statements korrekt**
```bash
# Alle getaggten Tests haben korrekte Imports:
TAGGED_COUNT=$(grep -r '@Tag("core")' backend/src/test/ | wc -l)
IMPORT_COUNT=$(grep -r "import org.junit.jupiter.api.Tag;" backend/src/test/ | wc -l)

if [ $IMPORT_COUNT -ge $TAGGED_COUNT ]; then
    echo "✅ Import validation passed"
else
    echo "❌ Missing imports detected"
fi
```

### ✅ **Validierung 4: Compilation erfolgreich**
```bash
cd backend && ./mvnw compile test-compile -q
# MUSS mit Exit-Code 0 beenden (erfolgreiche Compilation)
echo "Exit code: $?"
# MUSS ausgeben: "Exit code: 0"
```

**💡 Erst wenn ALLE 4 Validierungen erfolgreich sind → Phase 3B starten!**

---

## ➡️ Nächste Phase

Nach erfolgreichem Abschluss von Phase 3A:
→ **Phase 3B: Migration-Tests + CI-Integration** (PHASE_3B_MIGRATION_TAGS_CI.md)

**Core-Basis ist gelegt - jetzt Migration-Tests taggen und CI umstellen! 🚀**