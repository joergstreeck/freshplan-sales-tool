-- V24: Add Data Quality and Freshness Tracking fields to customer_contacts table
-- Migration für Data Freshness Tracking Feature (TODO-80)

-- Add data quality score field (0-100)
ALTER TABLE customer_contacts ADD COLUMN IF NOT EXISTS data_quality_score INTEGER;

-- Add recommendations field for storing improvement suggestions
ALTER TABLE customer_contacts ADD COLUMN IF NOT EXISTS data_quality_recommendations TEXT;

-- Add comments for documentation
COMMENT ON COLUMN customer_contacts.data_quality_score IS 'Overall data quality score (0-100) calculated by DataHygieneService';
COMMENT ON COLUMN customer_contacts.data_quality_recommendations IS 'Semicolon-separated list of recommendations for improving data quality';

-- Create index for efficient querying by data quality score
CREATE INDEX IF NOT EXISTS idx_customer_contacts_data_quality_score ON customer_contacts(data_quality_score);

-- Create index for freshness queries (using existing updated_at field)
CREATE INDEX IF NOT EXISTS idx_customer_contacts_updated_at ON customer_contacts(updated_at);

-- Create standard indices for better query performance
CREATE INDEX IF NOT EXISTS idx_customer_contacts_data_quality_low ON customer_contacts(data_quality_score);

-- Note: Partial indices with time-based predicates removed due to PostgreSQL IMMUTABLE function requirements
-- These queries will use the main updated_at index instead