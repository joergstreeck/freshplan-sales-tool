-- V260: Add Sprint 2.1.5 Progressive Profiling Activity Types to Constraint
-- Issue #130: Fixes LeadResourceTest.testCreateLead constraint violation
-- Adds: LEAD_ASSIGNED, FIRST_CONTACT_DOCUMENTED, EMAIL_RECEIVED, QUALIFIED_CALL, DEMO, ROI_PRESENTATION, SAMPLE_FEEDBACK

-- Drop existing constraint
ALTER TABLE lead_activities
  DROP CONSTRAINT IF EXISTS chk_activity_type;

-- Re-create with extended list (all V241 values + Sprint 2.1.5 additions)
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

      -- Stop-the-clock feature (from V241)
      'CLOCK_STOPPED', 'CLOCK_RESUMED',

      -- Import/Export (from V241)
      'IMPORTED', 'EXPORTED',

      -- Sprint 2.1.5 System Activities (Progressive Profiling) - NEW
      'LEAD_ASSIGNED', 'FIRST_CONTACT_DOCUMENTED', 'EMAIL_RECEIVED',

      -- Sprint 2.1.5 Progress Activities - NEW
      'QUALIFIED_CALL', 'DEMO', 'ROI_PRESENTATION', 'SAMPLE_FEEDBACK'
    )
  );

COMMENT ON CONSTRAINT chk_activity_type ON lead_activities IS
  'Valid activity types aligned with ActivityType enum - includes Sprint 2.1.5 Progressive Profiling types';
