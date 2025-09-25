-- V239: Update Lead Status Constraint
-- Sprint 2.1 PR #105: Add missing status values from LeadStatus enum

-- Drop the existing constraint
ALTER TABLE leads DROP CONSTRAINT IF EXISTS chk_lead_status;

-- Add the updated constraint with all status values from LeadStatus enum
ALTER TABLE leads ADD CONSTRAINT chk_lead_status
    CHECK (status IN (
        'REGISTERED',      -- Initial status when lead is created
        'ACTIVE',          -- Lead has meaningful contact and is actively worked on
        'REMINDER',        -- 60 days without activity - reminder sent
        'GRACE_PERIOD',    -- After reminder period - 10 day grace period
        'QUALIFIED',       -- Lead has been qualified as a real opportunity
        'CONVERTED',       -- Lead has been converted to a customer
        'LOST',            -- Lead was lost (not converted)
        'EXPIRED',         -- Lead protection expired - can be reassigned
        'EXTENDED',        -- Lead protection extended by management
        'DELETED'          -- Soft delete - lead is archived
    ));

COMMENT ON CONSTRAINT chk_lead_status ON leads IS 'Valid lead status values aligned with LeadStatus enum';