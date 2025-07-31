-- V7__create_contacts_table.sql
-- Creates the contacts table for multi-contact support with relationship data

CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    
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
    is_primary BOOLEAN NOT NULL DEFAULT false,
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    -- Location Assignment
    assigned_location_id UUID REFERENCES customer_locations(id) ON DELETE SET NULL,
    
    -- Relationship Data (Beziehungsebene)
    birthday DATE,
    hobbies VARCHAR(500),
    family_status VARCHAR(50),
    children_count INTEGER,
    personal_notes TEXT,
    
    -- Audit Fields
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    
    -- Constraints
    CONSTRAINT chk_decision_level CHECK (
        decision_level IS NULL OR 
        decision_level IN ('executive', 'manager', 'operational', 'influencer')
    ),
    CONSTRAINT chk_family_status CHECK (
        family_status IS NULL OR 
        family_status IN ('single', 'married', 'divorced', 'widowed')
    ),
    CONSTRAINT chk_children_count CHECK (
        children_count IS NULL OR 
        children_count >= 0
    )
);

-- Ensure only one primary contact per customer
CREATE UNIQUE INDEX idx_unique_primary_per_customer 
ON contacts(customer_id) 
WHERE is_primary = true AND is_active = true;

-- Performance indices
CREATE INDEX idx_contact_customer ON contacts(customer_id);
CREATE INDEX idx_contact_location ON contacts(assigned_location_id);
CREATE INDEX idx_contact_active ON contacts(is_active);
CREATE INDEX idx_contact_email ON contacts(lower(email));
CREATE INDEX idx_contact_birthday ON contacts(
    EXTRACT(MONTH FROM birthday), 
    EXTRACT(DAY FROM birthday)
) WHERE birthday IS NOT NULL;

-- Audit table for Hibernate Envers
CREATE TABLE contacts_aud (
    id UUID NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    customer_id UUID,
    salutation VARCHAR(20),
    title VARCHAR(50),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    position VARCHAR(100),
    decision_level VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    is_primary BOOLEAN,
    is_active BOOLEAN,
    assigned_location_id UUID,
    birthday DATE,
    hobbies VARCHAR(500),
    family_status VARCHAR(50),
    children_count INTEGER,
    personal_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    PRIMARY KEY (id, rev)
);

-- Add foreign key to revinfo table (created by Envers)
ALTER TABLE contacts_aud 
ADD CONSTRAINT fk_contacts_aud_rev 
FOREIGN KEY (rev) REFERENCES revinfo(rev);

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_contacts_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_contacts_updated_at
BEFORE UPDATE ON contacts
FOR EACH ROW
EXECUTE FUNCTION update_contacts_updated_at();

-- Comments for documentation
COMMENT ON TABLE contacts IS 'Stores contact persons for customers with relationship data';
COMMENT ON COLUMN contacts.decision_level IS 'Decision-making level: executive, manager, operational, influencer';
COMMENT ON COLUMN contacts.is_primary IS 'Primary contact flag - only one per customer';
COMMENT ON COLUMN contacts.hobbies IS 'Comma-separated list of hobbies for relationship building';
COMMENT ON COLUMN contacts.personal_notes IS 'Internal notes for better customer relationships';