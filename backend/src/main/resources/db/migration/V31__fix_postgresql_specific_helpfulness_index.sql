-- Fix PostgreSQL-specific syntax in helpfulness index
-- Replace ::float cast with standard SQL CAST function for better database portability

-- Drop the old index with PostgreSQL-specific syntax
DROP INDEX IF EXISTS idx_help_contents_helpfulness;

-- Create new index using standard SQL CAST function
CREATE INDEX IF NOT EXISTS idx_help_contents_helpfulness 
    ON help_contents((CAST(helpful_count AS DOUBLE PRECISION) / NULLIF(helpful_count + not_helpful_count, 0)) DESC) 
    WHERE is_active = true AND (helpful_count + not_helpful_count) > 0;

-- Add comment explaining the index purpose
COMMENT ON INDEX idx_help_contents_helpfulness IS 'Index for sorting help content by helpfulness ratio (helpful votes / total votes)';