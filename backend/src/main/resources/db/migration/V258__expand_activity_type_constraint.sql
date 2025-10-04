-- V258: Expand activity_type CHECK constraint (13 Types total)
-- Context: Sprint 2.1.5 Activity-Types Progress-Mapping
-- Reference: ACTIVITY_TYPES_PROGRESS_MAPPING.md
-- Blocker-Fix: Production würde ohne diese Migration fehlschlagen bei neuen Activity-Types

-- Drop old constraint (aus V238)
ALTER TABLE lead_activities DROP CONSTRAINT IF EXISTS chk_activity_type;

-- New constraint with all 13 types
ALTER TABLE lead_activities ADD CONSTRAINT chk_activity_type
CHECK (activity_type IN (
  -- Progress Activities (5) - countsAsProgress=true
  'QUALIFIED_CALL',
  'MEETING',
  'DEMO',
  'ROI_PRESENTATION',
  'SAMPLE_SENT',

  -- Non-Progress Activities (5) - countsAsProgress=false
  'NOTE',
  'FOLLOW_UP',
  'EMAIL',
  'CALL',
  'SAMPLE_FEEDBACK',

  -- System Activities (3) - countsAsProgress=false, kein V257 Trigger
  'FIRST_CONTACT_DOCUMENTED',  -- ⚠️ startet Schutz explizit via LeadService
  'EMAIL_RECEIVED',
  'LEAD_ASSIGNED'
));

COMMENT ON CONSTRAINT chk_activity_type ON lead_activities IS
  'All 13 activity types from ACTIVITY_TYPES_PROGRESS_MAPPING.md (Sprint 2.1.5)';
