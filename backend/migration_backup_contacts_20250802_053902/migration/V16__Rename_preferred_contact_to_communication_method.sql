-- Rename preferred_contact_method to preferred_communication_method
-- to match the entity field name
ALTER TABLE customer_contacts 
RENAME COLUMN preferred_contact_method TO preferred_communication_method;