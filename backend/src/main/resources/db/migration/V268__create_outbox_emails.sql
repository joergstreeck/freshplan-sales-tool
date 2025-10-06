-- Migration V268: Create outbox_emails table (Sprint 2.1.6 Phase 3)
-- Minimal Outbox Pattern for Email Notifications
--
-- SCOPE:
-- This is a MINIMAL implementation for Phase 3 (Automated Jobs).
-- Full Email-System (Templates, Processor, Retry-Logic) comes in Modul 05 (Kommunikation).
--
-- DESIGN:
-- - Jobs write email notifications to outbox (transactional safety)
-- - Processor reads outbox and sends emails (implemented in Modul 05)
-- - Status: PENDING → SENT / FAILED
--
-- ADR-001: Email-Integration über Outbox-Pattern
-- Future: EmailOutboxProcessor with Exponential-Backoff (Modul 05)

CREATE TABLE outbox_emails (
  id BIGSERIAL PRIMARY KEY,

  -- Recipient & Content
  recipient_email VARCHAR(255) NOT NULL,
  subject VARCHAR(500) NOT NULL,
  body TEXT NOT NULL,

  -- Template (optional - used by Modul 05 template engine)
  template_name VARCHAR(100),
  template_data JSONB,

  -- Status & Retry
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
  attempts INTEGER NOT NULL DEFAULT 0,
  last_error TEXT,

  -- Timestamps
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  sent_at TIMESTAMP,

  -- Metadata
  created_by VARCHAR(50) NOT NULL,
  correlation_id VARCHAR(255) -- For tracing (e.g., leadId, importJobId)
);

-- Indexes (Code Review ChatGPT: IF NOT EXISTS für Idempotenz)
CREATE INDEX IF NOT EXISTS idx_outbox_emails_status ON outbox_emails(status) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_outbox_emails_created_at ON outbox_emails(created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_emails_correlation ON outbox_emails(correlation_id);

-- Comments
COMMENT ON TABLE outbox_emails IS
  'Sprint 2.1.6 Phase 3: Minimal Outbox Pattern for Email Notifications.
   Jobs write emails here (transactional). Processor sends them (Modul 05).
   ADR-001: Email-Integration über Outbox-Pattern';

COMMENT ON COLUMN outbox_emails.status IS
  'PENDING: Waiting to be sent, SENT: Successfully sent, FAILED: Max retries exceeded';

COMMENT ON COLUMN outbox_emails.template_name IS
  'Optional: Template name for Modul 05 template engine (e.g., "lead_progress_warning")';

COMMENT ON COLUMN outbox_emails.correlation_id IS
  'Tracing ID for debugging (e.g., "lead:12345", "import_job:678")';
