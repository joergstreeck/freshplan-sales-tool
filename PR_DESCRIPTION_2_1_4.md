# mod02/sprint-2.1.4 – Lead Deduplication & Data Quality (V247, V248) + Tests

## 📋 Scope

### Database
- **V247**: Lead normalization functions + partial UNIQUE indices
  - Email/Phone/Company normalization
  - Partial unique indices on `email_normalized` (WHERE status != 'DELETED')
  - SQL normalization functions
- **V248**: CREATE UNIQUE INDEX CONCURRENTLY for zero-downtime
  - Java-based migration with test detection
  - Production-safe concurrent index creation

### Application
- **LeadNormalizationService**: Email/Phone/Company/Domain normalization
  - E.164 phone format
  - Company suffix removal (GmbH, AG, etc.)
  - Domain extraction from URLs
- **IdempotencyService**: 24h TTL deduplication
  - SHA-256 request hashing
  - Atomic INSERT with ON CONFLICT
  - Tenant isolation

### Tests
- **31 Normalization Tests** - All green ✅
- **8 Idempotency Tests** - All green ✅
- **Total: 39 Tests, 0 Failures**

### Documentation
- SPRINT_MAP.md - Scope clarified (Fuzzy-Matching → 2.1.6)
- CONTRACT_MAPPING.md - § 2(8) compliance documented
- Data-Retention-Plan - DSGVO compliance
- ADR-003 - Row-Level-Security design
- TRIGGER_SPRINT_2_1_4/2_1_5/2_1_6 - Updated
- Master Plan V5 - Session log updated

## ✅ Pre-Merge Checklist (All Blockers Fixed)

### Blocker #1: Status Consistency ✅
- [x] Index uses `status != 'DELETED'`
- [x] Code uses `status != 'DELETED'` (was LOST)
- [x] Both are now aligned

### Blocker #2: Idempotency Race-Condition ✅
- [x] Changed to atomic `INSERT ... ON CONFLICT DO NOTHING`
- [x] SHA-256 hash instead of Java hashCode()
- [x] No more check-then-insert pattern

### Blocker #3: Flyway Hygiene ✅
- [x] V249__lead_protection_tables.sql.sprint215 moved out of migration path
- [x] Clean migration path (V247, V248 only)
- [x] No out-of-order issues

## 🧪 Technical Verification

```bash
# Local tests passing
./mvnw clean test -Dtest="LeadNormalizationServiceTest,IdempotencyServiceTest"
# Result: 39 Tests, 0 Failures ✅

# Flyway status clean
./mvnw flyway:info
# V247 Success
# V248 Success
# No out-of-order

# Fresh database test
dropdb freshplan_test && createdb freshplan_test
./mvnw flyway:migrate
# No errors, sequential execution
```

## 🔄 Rollback Strategy

### Application Rollback
- Previous version remains compatible
- No breaking API changes

### Database Rollback
```sql
-- V248 rollback (index only)
DROP INDEX CONCURRENTLY IF EXISTS uq_leads_email_canonical_v2;

-- V247 rollback (additive only, low risk)
-- Optional: DROP added columns if needed
ALTER TABLE leads
  DROP COLUMN IF EXISTS email_normalized,
  DROP COLUMN IF EXISTS phone_e164,
  DROP COLUMN IF EXISTS company_name_normalized,
  DROP COLUMN IF EXISTS website_domain;

DROP FUNCTION IF EXISTS normalize_email(text);
DROP FUNCTION IF EXISTS normalize_phone(text);
DROP FUNCTION IF EXISTS normalize_company_name(text);
```

## 📊 Performance Impact

- Index creation: CONCURRENTLY (zero downtime)
- Normalization: < 1ms per field
- Idempotency check: Index-backed O(1) lookup
- No N+1 queries introduced

## 🔒 Security

- No hardcoded secrets
- SHA-256 for request hashing
- Tenant isolation enforced
- Input validation on all normalizations

## 📝 Follow-up Tasks (Non-blocking)

### High Priority (Sprint 2.1.5)
- [ ] Harmonize Java/SQL normalization (unaccent, whitespace)
- [ ] Add phone_e164 unique index if needed
- [ ] Backfill existing leads with normalized values

### Medium Priority
- [ ] Flyway placeholders for CONCURRENTLY
- [ ] Monitoring for duplicate rejection rate
- [ ] Performance metrics for normalization

## 🔗 References

- JIRA: FP-234 (Lead Deduplication)
- Sprint: 2.1.4
- Contract: § 2(8) Handelsvertretervertrag
- ADR-002: RBAC Lead Protection
- ADR-003: Row-Level-Security (proposed)

## 👥 Reviewers

- @backend-team - Service implementation
- @db-team - Migration review
- @security - Idempotency pattern
- @compliance - DSGVO aspects

---

**Ready for Review** ✅ All blockers resolved, tests passing, documentation complete.