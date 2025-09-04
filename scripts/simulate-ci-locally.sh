#!/bin/bash

# CI-Umgebung lokal simulieren
# Exakt wie in GitHub Actions

set -e  # Bei Fehler stoppen

echo "🔬 CI-SIMULATION STARTEN"
echo "========================"

# Farben für Output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. PostgreSQL vorbereiten (wie in CI)
echo -e "${YELLOW}1. PostgreSQL Reset...${NC}"
PGPASSWORD=freshplan psql -h localhost -U freshplan -d freshplan -c "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public;"
echo -e "${GREEN}✅ Schema reset${NC}"

# 2. Migrations anwenden (CI-Profile)
echo -e "${YELLOW}2. Migrations mit CI-Profile...${NC}"
cd backend
export QUARKUS_PROFILE=ci
export RUN_SUFFIX=local-test-$$

# Bootstrap Migration (wenn MigrationBootstrapIT existiert)
if [ -f "src/test/java/de/freshplan/test/MigrationBootstrapIT.java" ]; then
    echo "Running MigrationBootstrapIT..."
    ./mvnw -q -Dtest=de.freshplan.test.MigrationBootstrapIT test \
      -Dquarkus.devservices.enabled=false \
      -Dquarkus.datasource.devservices.enabled=false \
      -Dquarkus.flyway.migrate-at-start=true \
      -Dquarkus.flyway.out-of-order=true \
      -Dquarkus.flyway.locations=classpath:db/migration,classpath:db/testdata,classpath:db/ci-migrations \
      -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan \
      -Dquarkus.datasource.username=freshplan \
      -Dquarkus.datasource.password=freshplan
else
    echo "MigrationBootstrapIT not found, applying migrations normally..."
    ./mvnw flyway:migrate \
      -Dquarkus.profile=ci \
      -Dquarkus.flyway.out-of-order=true
fi

# 3. Customers vor Tests zählen
echo -e "${YELLOW}3. Initiale Customer-Anzahl...${NC}"
BEFORE=$(PGPASSWORD=freshplan psql -h localhost -U freshplan -d freshplan -tAc "SELECT COUNT(*) FROM customers;")
echo -e "Customers VOR Tests: ${GREEN}$BEFORE${NC}"

# 4. Tests mit CI-Profile ausführen (genau wie in GitHub)
echo -e "${YELLOW}4. Tests mit CI-Profile ausführen...${NC}"
./mvnw test \
  -B \
  -T 1C \
  -Djunit.jupiter.execution.timeout.default=2m \
  -Dmaven.test.failure.ignore=false \
  -DtrimStackTrace=false \
  -DRUN_SUFFIX=$RUN_SUFFIX \
  -Dquarkus.devservices.enabled=false \
  -Dquarkus.datasource.devservices.enabled=false \
  -Dquarkus.flyway.migrate-at-start=true \
  -Dquarkus.flyway.out-of-order=true \
  -Dquarkus.flyway.locations=classpath:db/migration,classpath:db/testdata,classpath:db/ci-migrations \
  -Dquarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/freshplan \
  -Dquarkus.datasource.username=freshplan \
  -Dquarkus.datasource.password=freshplan \
  2>&1 | tee /tmp/ci-test-output.log

TEST_RESULT=$?

# 5. Customers nach Tests zählen
echo -e "${YELLOW}5. Finale Customer-Anzahl...${NC}"
AFTER=$(PGPASSWORD=freshplan psql -h localhost -U freshplan -d freshplan -tAc "SELECT COUNT(*) FROM customers;")
echo -e "Customers NACH Tests: ${GREEN}$AFTER${NC}"

# 6. Database Growth Check
DELTA=$((AFTER - BEFORE))
echo -e "${YELLOW}6. Database Growth Check...${NC}"
echo "Delta: $DELTA customers"

if [ "$DELTA" -gt "0" ]; then
    echo -e "${RED}❌ FEHLER: Database ist um $DELTA Customers gewachsen!${NC}"
    echo -e "${RED}Tests räumen nicht auf!${NC}"
    
    # Zeige welche Tests problematisch sein könnten
    echo -e "${YELLOW}Suche nach Tests ohne @TestTransaction...${NC}"
    grep -r "@Test" src/test --include="*.java" | \
      xargs grep -L "@TestTransaction" | \
      xargs grep -l "customerRepository\|Customer.persist\|save\|insert" | \
      head -10
    
    exit 1
else
    echo -e "${GREEN}✅ Keine Database Growth - Tests sind sauber!${NC}"
fi

# 7. Test-Ergebnis
if [ "$TEST_RESULT" -ne "0" ]; then
    echo -e "${RED}❌ Tests fehlgeschlagen!${NC}"
    echo "Siehe /tmp/ci-test-output.log für Details"
    exit 1
fi

echo -e "${GREEN}✅ CI-Simulation erfolgreich!${NC}"