-- V203: Add remaining Contact entity fields that were missed in V201 and V202
-- This completes the Sprint 2 Contact entity integration
-- Analysis showed these 3 fields are required by Contact.java but missing in DB

-- 1. Hobbies field for relationship management
ALTER TABLE customer_contacts 
ADD COLUMN IF NOT EXISTS hobbies VARCHAR(500);

-- 2. Personal notes field for detailed relationship information
ALTER TABLE customer_contacts 
ADD COLUMN IF NOT EXISTS personal_notes TEXT;

-- 3. Location assignment (foreign key to customer_locations table)
-- Only add if customer_locations table exists
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables 
               WHERE table_schema = 'public' 
               AND table_name = 'customer_locations') THEN
        
        -- Add column if not exists
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_schema = 'public' 
                       AND table_name = 'customer_contacts' 
                       AND column_name = 'assigned_location_id') THEN
            ALTER TABLE customer_contacts 
            ADD COLUMN assigned_location_id UUID;
            
            -- Add foreign key constraint
            ALTER TABLE customer_contacts 
            ADD CONSTRAINT fk_contact_location 
            FOREIGN KEY (assigned_location_id) 
            REFERENCES customer_locations(id);
            
            -- Add index for performance
            CREATE INDEX IF NOT EXISTS idx_contact_assigned_location 
            ON customer_contacts(assigned_location_id);
        END IF;
    ELSE
        -- If customer_locations doesn't exist yet, just add the column without FK
        -- The FK can be added later when customer_locations is created
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_schema = 'public' 
                       AND table_name = 'customer_contacts' 
                       AND column_name = 'assigned_location_id') THEN
            ALTER TABLE customer_contacts 
            ADD COLUMN assigned_location_id UUID;
        END IF;
    END IF;
END $$;

-- Add comment for documentation
COMMENT ON COLUMN customer_contacts.hobbies IS 'Comma-separated list of contact hobbies for relationship building';
COMMENT ON COLUMN customer_contacts.personal_notes IS 'Free text field for detailed personal information about the contact';
COMMENT ON COLUMN customer_contacts.assigned_location_id IS 'Reference to specific customer location this contact is assigned to';