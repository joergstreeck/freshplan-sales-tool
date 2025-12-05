-- V10053: Add original_created_at field for Lead import
-- Sprint 2.1.8 - Support for importing historical leads with original generation date
--
-- Purpose:
-- - created_at = System timestamp (when lead was added to system)
-- - original_created_at = Business date (when lead was actually generated, e.g., at trade fair)
--
-- For reports/analytics: Use COALESCE(original_created_at, created_at)

ALTER TABLE leads
ADD COLUMN original_created_at TIMESTAMP;

-- Add index for date-based queries (commonly used in reports)
CREATE INDEX idx_leads_original_created_at ON leads(original_created_at)
WHERE original_created_at IS NOT NULL;

COMMENT ON COLUMN leads.original_created_at IS 'Original lead generation date for imported historical data. NULL means created_at is the generation date.';
