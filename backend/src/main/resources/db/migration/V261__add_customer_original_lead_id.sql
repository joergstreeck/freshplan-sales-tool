-- V261: Add original_lead_id to customers table for Lead → Customer conversion tracking
-- Sprint 2.1.6 - User Story 2: Lead → Kunde Convert Flow
-- Author: Claude Code
-- Date: 2025-10-06

-- Add original_lead_id column to track which Lead was converted to this Customer
-- SOFT REFERENCE (no FK constraint): Allows Lead deletion without cascading to Customer
-- Business logic ensures originalLeadId is set correctly during conversion
ALTER TABLE customers
ADD COLUMN original_lead_id BIGINT NULL;

-- Add index for reverse lookup (find Customer from Lead ID)
CREATE INDEX idx_customers_original_lead_id
ON customers(original_lead_id)
WHERE original_lead_id IS NOT NULL;

-- Add comment for documentation
COMMENT ON COLUMN customers.original_lead_id IS
  'Sprint 2.1.6: ID of the Lead that was converted to this Customer. NULL if Customer was created directly (not from Lead conversion).';

-- Audit trail: No data migration needed (new column starts NULL for all existing customers)
