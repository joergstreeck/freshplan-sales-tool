-- V233: Add active column to territories table
-- Required for: Lead Territory Management with business rules

ALTER TABLE territories ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT true;

-- Set all existing territories as active
UPDATE territories SET active = true WHERE active IS NULL;

-- Make column NOT NULL after setting defaults
ALTER TABLE territories ALTER COLUMN active SET NOT NULL;

COMMENT ON COLUMN territories.active IS 'Territory availability for new leads';