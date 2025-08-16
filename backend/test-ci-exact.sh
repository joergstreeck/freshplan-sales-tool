#!/bin/bash

# EXAKTE CI-Simulation - genau wie in GitHub Actions
echo "🎯 EXAKTE CI-SIMULATION"
echo "======================="

# Setze alle CI Environment Variables
export QUARKUS_PROFILE=ci
export MAVEN_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"
export RUN_SUFFIX=local-test-$$

# Clean build genau wie in CI
echo "🧹 Clean build..."
./mvnw -B clean

# Exakt die gleichen Parameter wie in CI
echo "🧪 Running tests EXACTLY like CI..."
./mvnw -B test \
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
  2>&1 | tee /tmp/ci-simulation.log

echo ""
echo "📊 Results:"
grep "Tests run:" /tmp/ci-simulation.log | tail -5
echo ""
echo "🔍 Failures:"
grep -E "(ERROR|FAILURE|Failed)" /tmp/ci-simulation.log | head -20