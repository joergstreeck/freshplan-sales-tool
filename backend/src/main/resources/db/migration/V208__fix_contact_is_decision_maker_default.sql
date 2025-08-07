-- Fix is_decision_maker column to have a default value
-- This field exists in the database but not in the Contact entity
-- Tests are failing because it's NOT NULL without a default

-- First, update any NULL values to false
UPDATE customer_contacts 
SET is_decision_maker = false 
WHERE is_decision_maker IS NULL;

-- Then alter the column to have a default value
ALTER TABLE customer_contacts 
ALTER COLUMN is_decision_maker SET DEFAULT false;

-- Ensure the column is NOT NULL with default
ALTER TABLE customer_contacts 
ALTER COLUMN is_decision_maker SET NOT NULL;

COMMENT ON COLUMN customer_contacts.is_decision_maker IS 'Whether this contact is a decision maker (legacy field, not mapped in entity)';