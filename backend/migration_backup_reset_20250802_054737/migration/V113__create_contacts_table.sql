-- Create contacts table for multi-contact support per customer
-- Part of Sprint 2: Customer UI Integration (Step 3)
CREATE TABLE IF NOT EXISTS contacts (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    
    -- Basic Info
    salutation VARCHAR(20),
    title VARCHAR(50),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    position VARCHAR(100),
    decision_level VARCHAR(50),
    
    -- Contact Info
    email VARCHAR(255),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    
    -- Flags
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Location Assignment
    assigned_location_id UUID,
    
    -- Relationship Data (Beziehungsebene)
    birthday DATE,
    hobbies VARCHAR(500),
    family_status VARCHAR(50),
    children_count INTEGER,
    personal_notes TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT pk_contacts PRIMARY KEY (id),
    CONSTRAINT fk_contacts_customer FOREIGN KEY (customer_id) 
        REFERENCES customers(id) ON DELETE CASCADE,
    CONSTRAINT fk_contacts_location FOREIGN KEY (assigned_location_id) 
        REFERENCES customer_locations(id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_contact_customer ON contacts(customer_id);
CREATE INDEX IF NOT EXISTS idx_contact_location ON contacts(assigned_location_id);
CREATE INDEX IF NOT EXISTS idx_contact_active ON contacts(is_active);
CREATE INDEX IF NOT EXISTS idx_contact_email ON contacts(LOWER(email));

-- Ensure only one primary contact per customer
CREATE UNIQUE INDEX IF NOT EXISTS idx_contact_primary_per_customer 
    ON contacts(customer_id) 
    WHERE is_primary = TRUE AND is_active = TRUE;

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_contacts_updated_at 
    BEFORE UPDATE ON contacts 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();