-- Sprint 2.1.6 Phase 2 - Fix #2 & #4: Stop-the-Clock Kumulative Pause + Idempotency-Key Tracking
-- Issue #130 (Bestandsleads-Migration)
-- 2025-10-06

-- =============================================================================
-- Fix #4: Stop-the-Clock Kumulative Pausenzeit
-- =============================================================================

-- Add cumulative pause tracking to leads table
ALTER TABLE leads
ADD COLUMN progress_pause_total_seconds BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN leads.progress_pause_total_seconds IS
  'Sprint 2.1.6: Kumulative Pausenzeit für Stop-the-Clock in Sekunden.
   Wird bei jedem Resume um die letzte Pause-Dauer erhöht.
   Formel: progressDeadline = registeredAt + 60 Tage + (progress_pause_total_seconds / 86400) Tage';

-- Create index for queries filtering by paused leads
CREATE INDEX idx_leads_clock_stopped
ON leads(clock_stopped_at)
WHERE clock_stopped_at IS NOT NULL;

-- =============================================================================
-- Fix #2: Idempotency-Key Tracking für Lead Import
-- =============================================================================

-- Create import_jobs table for idempotency tracking
CREATE TABLE import_jobs (
  id BIGSERIAL PRIMARY KEY,
  idempotency_key VARCHAR(255) NOT NULL UNIQUE,
  request_fingerprint TEXT NOT NULL,
  status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
  total_leads INTEGER NOT NULL DEFAULT 0,
  success_count INTEGER NOT NULL DEFAULT 0,
  failure_count INTEGER NOT NULL DEFAULT 0,
  duplicate_warnings INTEGER NOT NULL DEFAULT 0,
  result_summary JSONB,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  completed_at TIMESTAMP,
  created_by VARCHAR(50) NOT NULL,
  ttl_expires_at TIMESTAMP NOT NULL DEFAULT (NOW() + INTERVAL '7 days'),
  CONSTRAINT import_jobs_ttl_check CHECK (ttl_expires_at > created_at)
);

CREATE INDEX idx_import_jobs_idempotency_key ON import_jobs(idempotency_key);
CREATE INDEX idx_import_jobs_status ON import_jobs(status);
CREATE INDEX idx_import_jobs_ttl ON import_jobs(ttl_expires_at) WHERE status = 'COMPLETED';

COMMENT ON TABLE import_jobs IS
  'Sprint 2.1.6: Idempotency-Key tracking für Lead-Batch-Import.
   TTL: 7 Tage nach Completion, danach Cleanup durch Nightly Job (Phase 3).';

COMMENT ON COLUMN import_jobs.idempotency_key IS
  'Client-provided unique key für Idempotenz (Header: Idempotency-Key).
   Format: UUID v4 empfohlen. Bei Replay liefert Server den gleichen Result zurück.';

COMMENT ON COLUMN import_jobs.request_fingerprint IS
  'SHA-256 Hash der Request-Daten (Fallback-Check bei fehlendem Idempotency-Key).';

COMMENT ON COLUMN import_jobs.result_summary IS
  'JSONB mit Import-Statistiken: {successCount, failureCount, duplicateWarnings, results[]}';
