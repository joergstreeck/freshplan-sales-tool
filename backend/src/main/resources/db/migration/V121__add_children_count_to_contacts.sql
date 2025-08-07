-- V121: Add children_count column to customer_contacts table
-- Required for Contact entity with childrenCount field

ALTER TABLE customer_contacts 
ADD COLUMN IF NOT EXISTS children_count INTEGER;

COMMENT ON COLUMN customer_contacts.children_count IS 'Number of children the contact has (for personal relationship data)';