-- V239: Update Lead Status Constraint
-- Sprint 2.1 PR #105: Add missing status values from LeadStatus enum

-- Drop the existing constraint
ALTER TABLE leads DROP CONSTRAINT IF EXISTS chk_lead_status;

-- Add the updated constraint with all status values from LeadStatus enum
ALTER TABLE leads ADD CONSTRAINT chk_lead_status
    CHECK (status IN (
        'REGISTERED',      -- Initial status
        'ACTIVE',          -- Lead is actively worked on
        'REMINDER',        -- Reminder sent (was REMINDER_SENT)
        'GRACE_PERIOD',    -- In grace period
        'EXPIRED',         -- Lead expired
        'EXTENDED',        -- Lead extended
        'STOP_CLOCK',      -- Clock stopped
        'DELETED',         -- Soft deleted
        'WON',             -- Lead converted to customer
        'LOST'             -- Lead lost
    ));

COMMENT ON CONSTRAINT chk_lead_status ON leads IS 'Valid lead status values aligned with LeadStatus enum';