-- Migration V271: Fix - Add missing lead_score column (V269 Hotfix)
-- Sprint 2.1.6 Phase 4 - ADR-006 Phase 2: Lead-Scoring System

-- V269 was marked as "Out of Order" and not applied due to schema history conflict
-- This migration applies the same changes idempotently

DO $$
BEGIN
    -- Add lead_score column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'leads'
          AND column_name = 'lead_score'
    ) THEN
        ALTER TABLE leads ADD COLUMN lead_score INTEGER;

        RAISE NOTICE 'Added column leads.lead_score';
    END IF;

    -- Add constraint if it doesn't exist
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.constraint_column_usage
        WHERE table_name = 'leads'
          AND constraint_name = 'chk_lead_score'
    ) THEN
        ALTER TABLE leads ADD CONSTRAINT chk_lead_score
            CHECK (lead_score IS NULL OR (lead_score >= 0 AND lead_score <= 100));

        RAISE NOTICE 'Added constraint chk_lead_score';
    END IF;

    -- Add index if it doesn't exist
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE tablename = 'leads'
          AND indexname = 'idx_leads_score'
    ) THEN
        CREATE INDEX idx_leads_score ON leads(lead_score DESC NULLS LAST);

        RAISE NOTICE 'Added index idx_leads_score';
    END IF;
END $$;

-- Add comment (outside DO block to avoid syntax issues)
COMMENT ON COLUMN leads.lead_score IS 'Lead quality score 0-100 points (Sprint 2.1.6 ADR-006 Phase 2). Calculated from: Umsatzpotenzial (25%), Engagement (25%), Fit (25%), Dringlichkeit (25%)';
