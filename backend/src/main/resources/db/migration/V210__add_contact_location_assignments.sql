-- V210: Add Multi-Location Assignment Support
-- Author: Claude
-- Date: 2025-08-08
-- Description: Enables contacts to be assigned to multiple locations

-- 1. Multi-Location Assignment Table
DROP TABLE IF EXISTS contact_location_assignments CASCADE;

CREATE TABLE contact_location_assignments (
    contact_id UUID NOT NULL,
    location_id UUID NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by VARCHAR(100),
    is_primary BOOLEAN DEFAULT false,
    PRIMARY KEY (contact_id, location_id),
    CONSTRAINT fk_contact_location_contact 
        FOREIGN KEY (contact_id) 
        REFERENCES customer_contacts(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_contact_location_location 
        FOREIGN KEY (location_id) 
        REFERENCES customer_locations(id) 
        ON DELETE CASCADE
);

-- Indexes for query performance
CREATE INDEX idx_contact_location_contact ON contact_location_assignments(contact_id);
CREATE INDEX idx_contact_location_location ON contact_location_assignments(location_id);
CREATE INDEX idx_contact_location_primary ON contact_location_assignments(is_primary) 
WHERE is_primary = true;

-- 2. Migrate existing single location assignments
-- Preserve existing data by copying single assignments to multi-assignment table
INSERT INTO contact_location_assignments (contact_id, location_id, is_primary)
SELECT id, assigned_location_id, true
FROM customer_contacts 
WHERE assigned_location_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM contact_location_assignments 
    WHERE contact_id = customer_contacts.id 
      AND location_id = customer_contacts.assigned_location_id
  );

-- 3. Add comment for backward compatibility
COMMENT ON COLUMN customer_contacts.assigned_location_id IS 
'DEPRECATED: Use contact_location_assignments table for multi-location support. Kept for backward compatibility.';

-- 4. Add view for easy querying of primary locations
CREATE OR REPLACE VIEW v_contact_primary_locations AS
SELECT 
    cc.id as contact_id,
    cc.first_name,
    cc.last_name,
    cc.email,
    cl.id as location_id,
    cl.name as location_name,
    cla.is_primary
FROM customer_contacts cc
LEFT JOIN contact_location_assignments cla ON cc.id = cla.contact_id
LEFT JOIN customer_locations cl ON cla.location_id = cl.id
WHERE cla.is_primary = true OR cla.is_primary IS NULL;

COMMENT ON VIEW v_contact_primary_locations IS 
'View showing contacts with their primary location assignments for easy querying';