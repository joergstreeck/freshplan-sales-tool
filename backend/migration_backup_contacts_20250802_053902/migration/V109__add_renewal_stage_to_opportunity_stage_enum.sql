-- V109: Add RENEWAL stage to opportunity_stage CHECK constraint
-- Purpose: Extend the opportunity pipeline with contract renewal capabilities
-- Author: FreshPlan Team
-- Date: 2025-07-25

-- Drop the existing check constraint
ALTER TABLE opportunities DROP CONSTRAINT IF EXISTS opportunities_stage_check;

-- Add new check constraint including RENEWAL
ALTER TABLE opportunities ADD CONSTRAINT opportunities_stage_check 
    CHECK (stage IN (
        'NEW_LEAD', 
        'QUALIFICATION', 
        'NEEDS_ANALYSIS', 
        'PROPOSAL', 
        'NEGOTIATION', 
        'CLOSED_WON', 
        'CLOSED_LOST',
        'RENEWAL'
    ));

-- Update the index hint for active stages to include RENEWAL
DROP INDEX IF EXISTS idx_opportunities_active_stage;
CREATE INDEX idx_opportunities_active_stage ON opportunities(stage, stage_changed_at) 
    WHERE is_deleted = FALSE AND stage NOT IN ('CLOSED_WON', 'CLOSED_LOST');

-- Update comment for documentation
COMMENT ON COLUMN opportunities.stage IS 'Current stage in the sales pipeline process (including renewal phase)';