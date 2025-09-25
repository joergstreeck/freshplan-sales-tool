-- V241: Align lead_activities.activity_type with domain enum (incl. CLOCK_* types)
-- Sprint 2.1 PR #105: Safe re-create of check constraint with full allow-list
-- Fixes testStopTheClockFeature by including CLOCK_STOPPED and CLOCK_RESUMED

-- 1) Drop old constraint if present (idempotent)
ALTER TABLE lead_activities
  DROP CONSTRAINT IF EXISTS chk_activity_type;

-- 2) Re-create with the complete list from ActivityType enum
ALTER TABLE lead_activities
  ADD CONSTRAINT chk_activity_type CHECK (
    activity_type IN (
      -- Communication activities
      'EMAIL', 'CALL', 'MEETING', 'NOTE',

      -- Business activities
      'SAMPLE_SENT', 'SAMPLE_RECEIVED', 'ORDER', 'FOLLOW_UP',

      -- Lifecycle / Status changes
      'CREATED', 'UPDATED', 'DELETED', 'STATUS_CHANGE',

      -- System / Automation
      'REMINDER_SENT', 'GRACE_PERIOD_STARTED', 'EXPIRED', 'REACTIVATED',

      -- Stop-the-clock feature (NEW - fixes the test)
      'CLOCK_STOPPED', 'CLOCK_RESUMED',

      -- Import/Export
      'IMPORTED', 'EXPORTED'
    )
  );

COMMENT ON CONSTRAINT chk_activity_type ON lead_activities IS
  'Valid activity types aligned with ActivityType enum - includes stop-the-clock support';