-- V209: Add Contact Roles and Responsibility Scope
-- Author: Claude
-- Date: 2025-08-08
-- Description: Implements multi-role support for contacts and responsibility scope

-- 1. Contact Roles Table for multi-role support
-- Drop if exists to avoid conflicts
DROP TABLE IF EXISTS contact_roles CASCADE;

CREATE TABLE contact_roles (
    contact_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (contact_id, role),
    CONSTRAINT fk_contact_roles_contact 
        FOREIGN KEY (contact_id) 
        REFERENCES customer_contacts(id) 
        ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_contact_roles_contact ON contact_roles(contact_id);
CREATE INDEX idx_contact_roles_role ON contact_roles(role);

-- 2. Add responsibility_scope to contacts
ALTER TABLE customer_contacts 
ADD COLUMN IF NOT EXISTS responsibility_scope VARCHAR(20) DEFAULT 'all';

-- Add comment for documentation
COMMENT ON COLUMN customer_contacts.responsibility_scope IS 
'Scope of responsibility: all = all locations, specific = selected locations only';

-- 3. Migrate existing decision_maker flag to roles
-- This preserves backward compatibility
INSERT INTO contact_roles (contact_id, role)
SELECT id, 'DECISION_MAKER'
FROM customer_contacts
WHERE is_decision_maker = true
  AND NOT EXISTS (
    SELECT 1 FROM contact_roles 
    WHERE contact_id = customer_contacts.id 
      AND role = 'DECISION_MAKER'
  );

-- 4. Add default role for contacts without any role
-- Every contact should have at least one role
INSERT INTO contact_roles (contact_id, role)
SELECT id, 'OPERATIONS_CONTACT'
FROM customer_contacts c
WHERE NOT EXISTS (
    SELECT 1 FROM contact_roles 
    WHERE contact_id = c.id
  )
  AND c.is_deleted = false
  AND c.is_active = true;