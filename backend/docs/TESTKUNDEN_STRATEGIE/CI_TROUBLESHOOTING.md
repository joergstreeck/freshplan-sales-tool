# CI Troubleshooting Guide

## CI Pipeline Overview

### PR Pipeline (must be green):
- Runs only `@Tag("core")` tests
- ~38 stable tests
- < 3 minutes
- Command: `./mvnw test -Pcore-tests`

### Nightly Pipeline (may be red):
- Runs `@Tag("migrate")` tests  
- ~98 tests being migrated
- Non-blocking for PRs
- Command: `./mvnw test -Pmigrate-tests`

## Common Issues and Solutions

### 🔴 "Test tagged as migrate fails"
✅ **Expected behavior** - migrate tests may fail
📝 **Action:** Create small PR to fix 5-10 tests
🎯 **Goal:** Move them to @Tag("core")

### 🔴 "Pre-commit hook blocks commit"
❌ **You used forbidden pattern**
📝 **Fix:** Replace with @TestTransaction or TestDataBuilder
⚠️  **Emergency bypass:** `git commit --no-verify` (discouraged)

### 🔴 "New test not tagged"
📝 **Fix:** Add `@Tag("core")` or `@Tag("migrate")`
🎯 **Rule:** All new tests must be tagged

### 🔴 "A00 Environment Check failed"
```
DIAG[CFG-001] flyway.locations = classpath:db/migration,classpath:testdata
```
📝 **Fix:** Remove testdata from flyway.locations
✅ **Should be:** Only `classpath:db/migration`

### 🔴 "Schema not empty at start"
```
DIAG[DB-001] Startzustand nicht leer: customers=42
```
📝 **Fix:** CI should reset schema before tests
✅ **Check:** Schema reset step in CI workflow

### 🔴 "DevServices active in CI"
```
DIAG[DEV-001] DevServices aktiv in CI
```
📝 **Fix:** Add to application-test.properties:
```properties
quarkus.datasource.devservices.enabled=false
```

### 🔴 "Test data remains from previous runs"
```
DIAG[DATA-001] Test data remains from previous runs: 12345, 12346
```
📝 **Fix:** Ensure tests use @TestTransaction
✅ **Check:** Builder sets isTestData=true

## Debugging Steps

### 1. Run Environment Diagnostics Locally
```bash
cd backend
./mvnw test -Dtest=A00_EnvDiagTest
```

### 2. Check Test Tags
```bash
# Count tests by tag
grep -r '@Tag("core")' src/test | wc -l      # Should be ~38
grep -r '@Tag("migrate")' src/test | wc -l   # Should be ~98
grep -r '@Tag("quarantine")' src/test | wc -l # Should be ~23
```

### 3. Run Core Tests Only
```bash
# Simulate CI pipeline
./mvnw test -Pcore-tests
```

### 4. Check for Forbidden Patterns
```bash
# Find dangerous DELETE statements
grep -r "DELETE FROM customers" src/test --include="*.java"

# Find untagged tests
for f in $(find src/test -name "*Test.java" -o -name "*IT.java"); do
  if ! grep -q "@Tag(" "$f"; then
    echo "Untagged: $f"
  fi
done
```

## CI Configuration

### Maven Profiles:
```xml
<!-- PR Pipeline -->
<profile>
  <id>core-tests</id>
  <properties>
    <junit.jupiter.tags>core & !quarantine</junit.jupiter.tags>
  </properties>
</profile>

<!-- Nightly Pipeline -->
<profile>
  <id>migrate-tests</id>
  <properties>
    <junit.jupiter.tags>migrate | quarantine</junit.jupiter.tags>
  </properties>
</profile>
```

### GitHub Actions Workflow:
```yaml
- name: Core Tests
  run: |
    ./mvnw -B verify -Pcore-tests \
      -Dquarkus.profile=ci \
      -Dtest.run.id=${{ github.run_id }}
```

## Performance Tips

### Speed up CI:
1. Use `-T 1C` for parallel builds
2. Cache Maven dependencies
3. Use `verify` instead of separate compile+test
4. Run only core tests in PR pipeline

### Reduce flakiness:
1. Use @TestTransaction for isolation
2. Set unique IDs with run_id
3. Avoid time-dependent assertions
4. Don't rely on test execution order

## Migration Checklist

When migrating a test from `@Tag("migrate")` to `@Tag("core")`:

- [ ] Replace SEED dependencies with TestDataBuilder
- [ ] Add @TestTransaction for auto-rollback
- [ ] Ensure isTestData=true on all entities
- [ ] Remove manual cleanup code
- [ ] Fix any DELETE statements to filter by isTestData
- [ ] Run test locally 3 times to ensure stability
- [ ] Change tag from "migrate" to "core"
- [ ] Create PR with 5-10 migrated tests

## Emergency Procedures

### CI completely broken:
```bash
# Revert to last known good state
git revert HEAD
git push origin main

# Or temporarily disable failing tests
@Tag("quarantine")  # Instead of @Tag("core")
```

### Too many test failures:
```bash
# Run subset of core tests
./mvnw test -Dtest="*Service*Test" -Pcore-tests
```

### Database issues:
```bash
# Reset local database
docker-compose down -v
docker-compose up -d postgres
./mvnw flyway:clean flyway:migrate
```

## Monitoring Progress

### Test Migration Dashboard:
```bash
# Create simple progress report
echo "=== Test Migration Progress ==="
echo "Core: $(grep -r '@Tag("core")' src/test | wc -l)"
echo "Migrate: $(grep -r '@Tag("migrate")' src/test | wc -l)"
echo "Quarantine: $(grep -r '@Tag("quarantine")' src/test | wc -l)"
echo "Total: $(find src/test -name "*Test.java" -o -name "*IT.java" | wc -l)"
```

### Weekly Goals:
- Move 5-10 tests from "migrate" to "core"
- Fix at least 1 quarantine test
- Keep core tests green
- Reduce total test runtime

## Contact & Escalation

### For help:
1. Check this guide first
2. Run A00_EnvDiagTest for diagnostics
3. Ask in #backend-help Slack
4. Create GitHub issue with "ci-failure" label

### Escalation path:
1. Team Lead - for blocked PRs
2. DevOps Team - for infrastructure issues
3. Architecture Team - for design decisions