-- V16: Add last_contact_date column to customer_contacts table
-- Date: 2025-07-05
-- Purpose: This column tracks when the contact person was last contacted
ALTER TABLE customer_contacts 
ADD COLUMN last_contact_date TIMESTAMP;

-- Create index for performance when searching by last contact date
CREATE INDEX idx_contact_last_contact_date ON customer_contacts(last_contact_date);