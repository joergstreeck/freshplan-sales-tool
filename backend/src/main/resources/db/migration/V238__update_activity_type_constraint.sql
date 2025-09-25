-- V238: Update Activity Type Constraint
-- Sprint 2.1 PR #105: Add missing activity types from domain model

-- Drop the existing constraint
ALTER TABLE lead_activities DROP CONSTRAINT IF EXISTS chk_activity_type;

-- Add the updated constraint with all activity types from ActivityType enum
ALTER TABLE lead_activities ADD CONSTRAINT chk_activity_type
    CHECK (activity_type IN (
        -- Communication activities
        'EMAIL', 'CALL', 'MEETING', 'NOTE',
        -- Business activities
        'SAMPLE_SENT', 'SAMPLE_RECEIVED', 'ORDER', 'FOLLOW_UP',
        -- Status changes
        'STATUS_CHANGE', 'CREATED', 'UPDATED', 'DELETED',
        -- System activities
        'REMINDER_SENT', 'GRACE_PERIOD_STARTED', 'EXPIRED', 'REACTIVATED',
        -- Import/Export
        'IMPORTED', 'EXPORTED'
    ));

COMMENT ON CONSTRAINT chk_activity_type ON lead_activities IS 'Valid activity types aligned with ActivityType enum';