-- Add missing family_status column that was overlooked in V201
-- This column is required by the Contact entity from Sprint 2 integration

ALTER TABLE customer_contacts 
ADD COLUMN IF NOT EXISTS family_status VARCHAR(50);