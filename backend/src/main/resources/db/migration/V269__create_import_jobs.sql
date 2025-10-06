-- Migration V269: Create import_jobs table (Sprint 2.1.6 Phase 3)
-- Batch Import Idempotency for Lead Bulk Imports
--
-- SCOPE:
-- Prevents duplicate batch imports by tracking idempotency keys and request fingerprints.
-- Supports 7-day TTL with automatic cleanup via Nightly Job (Job 4).
--
-- DESIGN:
-- - Idempotency-Key (client-provided UUID) as primary deduplication
-- - request_fingerprint (SHA-256 hash) as fallback
-- - status: PENDING → PROCESSING → COMPLETED/FAILED
-- - TTL: 7 days after completion → Hard-Delete by Job 4
--
-- ADR-002: Import Job Archival (7-Day TTL + Hard-Delete)
-- Issue #134: Batch Import Idempotency

-- Drop table if exists (for idempotency during development)
DROP TABLE IF EXISTS import_jobs CASCADE;

CREATE TABLE import_jobs (
  id BIGSERIAL PRIMARY KEY,

  -- Idempotency
  idempotency_key VARCHAR(255) NOT NULL UNIQUE,
  request_fingerprint TEXT NOT NULL,

  -- Status & Counts
  status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
  total_leads INTEGER NOT NULL DEFAULT 0,
  success_count INTEGER NOT NULL DEFAULT 0,
  failure_count INTEGER NOT NULL DEFAULT 0,
  duplicate_warnings INTEGER NOT NULL DEFAULT 0,

  -- Result Summary (JSONB for detailed import statistics)
  result_summary JSONB,

  -- Audit
  created_by VARCHAR(50) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  completed_at TIMESTAMP,

  -- TTL (7 days after completion)
  ttl_expires_at TIMESTAMP NOT NULL
);

-- Indexes (Code Review ChatGPT: IF NOT EXISTS für Idempotenz)
CREATE INDEX IF NOT EXISTS idx_import_jobs_idempotency_key ON import_jobs(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_import_jobs_status ON import_jobs(status);
CREATE INDEX IF NOT EXISTS idx_import_jobs_ttl_cleanup ON import_jobs(ttl_expires_at) WHERE status IN ('COMPLETED', 'FAILED');
CREATE INDEX IF NOT EXISTS idx_import_jobs_fingerprint ON import_jobs(request_fingerprint);

-- Comments
COMMENT ON TABLE import_jobs IS
  'Sprint 2.1.6 Phase 3: Batch Import Idempotency Tracking.
   Prevents duplicate imports via Idempotency-Key + request_fingerprint.
   TTL: 7 days → Hard-Delete via Job 4 (archiveCompletedImportJobs).
   ADR-002: Import Job Archival, Issue #134: Batch Import Idempotency';

COMMENT ON COLUMN import_jobs.idempotency_key IS
  'Client-provided unique key (HTTP Header: Idempotency-Key). Primary deduplication mechanism.';

COMMENT ON COLUMN import_jobs.request_fingerprint IS
  'SHA-256 hash of request body. Fallback deduplication if no Idempotency-Key provided.';

COMMENT ON COLUMN import_jobs.status IS
  'PENDING: Waiting, PROCESSING: In progress, COMPLETED: Success, FAILED: Error';

COMMENT ON COLUMN import_jobs.result_summary IS
  'JSONB with detailed statistics: {successCount, failureCount, duplicateWarnings, results[]}';

COMMENT ON COLUMN import_jobs.ttl_expires_at IS
  '7-Day TTL for cleanup. Set to (completed_at + 7 days). Job 4 deletes expired jobs.';
