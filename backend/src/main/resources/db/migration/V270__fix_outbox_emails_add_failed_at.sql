-- Migration V270: Fix outbox_emails table - Add missing failed_at column
-- Sprint 2.1.6 Phase 4 - Hotfix for V268 schema mismatch

-- Add failed_at column if it doesn't exist (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'outbox_emails'
          AND column_name = 'failed_at'
    ) THEN
        ALTER TABLE outbox_emails
        ADD COLUMN failed_at TIMESTAMP;

        COMMENT ON COLUMN outbox_emails.failed_at IS
            'Timestamp when email was marked as FAILED (Sprint 2.1.6 Phase 4 Hotfix)';
    END IF;
END $$;
