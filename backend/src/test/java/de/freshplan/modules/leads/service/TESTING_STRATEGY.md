# LeadMaintenanceService Testing Strategy

**Decision:** Use Integration Tests ONLY (no Unit Tests)

**Reason:**
- LeadMaintenanceService uses Panache entities (`OutboxEmail.persist()`)
- Panache entities **cannot be mocked** in pure Mockito unit tests
- Unit tests would fail with `IllegalStateException: This method is normally automatically overridden in subclasses`

**Coverage:**
- ✅ All business logic tested in `LeadMaintenanceSchedulerIT` (Integration Tests)
- ✅ Real database, real transactions, real Panache entities
- ✅ End-to-end validation: Scheduler → Service → DB → Events → Outbox

**Test File:**
- `/backend/src/test/java/de/freshplan/modules/leads/service/LeadMaintenanceSchedulerIT.java`

**ADR-005 Compliance:**
"Hybrid Test Strategy: Use Integration Tests when mocking is not feasible (e.g., Panache entities)"

**Sprint:** 2.1.6 Phase 3 - Automated Nightly Jobs
