-- V210: Add Contact Location Assignments and Views
-- Author: Claude
-- Date: 2025-08-08
-- Description: Creates location assignment tables and supporting views for contact management

-- 1. Contact Location Assignments Table
CREATE TABLE IF NOT EXISTS contact_location_assignments (
    contact_id UUID NOT NULL,
    location_id UUID NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID,
    is_primary BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (contact_id, location_id),
    CONSTRAINT fk_cla_contact 
        FOREIGN KEY (contact_id) 
        REFERENCES customer_contacts(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_cla_location 
        FOREIGN KEY (location_id) 
        REFERENCES customer_locations(id) 
        ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_cla_contact ON contact_location_assignments(contact_id);
CREATE INDEX IF NOT EXISTS idx_cla_location ON contact_location_assignments(location_id);
CREATE INDEX IF NOT EXISTS idx_cla_primary ON contact_location_assignments(contact_id, is_primary);

-- 2. View for Contact Primary Locations
CREATE OR REPLACE VIEW v_contact_primary_locations AS
SELECT 
    cc.id as contact_id,
    cc.first_name,
    cc.last_name,
    cl.id as location_id,
    cl.location_name,
    cla.assigned_at
FROM customer_contacts cc
JOIN contact_location_assignments cla ON cc.id = cla.contact_id
JOIN customer_locations cl ON cla.location_id = cl.id
WHERE cla.is_primary = true;

-- Views v_active_contacts and v_contact_deletion_stats are created in V211

-- Add comment for documentation
COMMENT ON TABLE contact_location_assignments IS 'Maps contacts to their assigned locations with primary designation';
COMMENT ON VIEW v_contact_primary_locations IS 'Shows primary location assignments for each contact';