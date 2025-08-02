-- =========================================
-- V120: Fix Migration Duplicate Issues
-- =========================================
-- 
-- Author: Claude (FreshPlan Team)
-- Date: 2025-08-02
-- Ticket: FRESH-CRITICAL
-- Sprint: Emergency Fix
-- 
-- Purpose: This migration fixes issues caused by duplicate migration files
-- without modifying existing migrations (following DATABASE_MIGRATION_GUIDE.md)
--
-- Problem: Multiple migrations with same version numbers exist
-- Solution: Ensure all necessary structures exist idempotently
-- =========================================

-- Note: This migration assumes Flyway has picked one of the duplicate files
-- We ensure all structures from all duplicate migrations exist

-- From V6__create_permission_system_core.sql (if not applied)
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    permission_code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    resource VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID NOT NULL REFERENCES app_user_roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    granted_by VARCHAR(100),
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX IF NOT EXISTS idx_permissions_resource_action ON permissions(resource, action);
CREATE INDEX IF NOT EXISTS idx_role_permissions_role ON role_permissions(role_id);

-- Insert default permissions if they don't exist
INSERT INTO permissions (permission_code, name, description, resource, action) VALUES
('customers:read', 'Kunden anzeigen', 'Kann Kundenliste und Details anzeigen', 'customers', 'read'),
('customers:create', 'Kunden erstellen', 'Kann neue Kunden anlegen', 'customers', 'create'),
('customers:update', 'Kunden bearbeiten', 'Kann Kundendaten ändern', 'customers', 'update'),
('customers:delete', 'Kunden löschen', 'Kann Kunden löschen', 'customers', 'delete')
ON CONFLICT (permission_code) DO NOTHING;

-- From V7__create_contact_interaction_table.sql (if not applied)
-- Skip if table already exists with different structure
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'contact_interactions') THEN
        CREATE TABLE contact_interactions (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            contact_id UUID NOT NULL,
            interaction_type VARCHAR(50) NOT NULL,
            interaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            notes TEXT,
            outcome VARCHAR(100),
            follow_up_required BOOLEAN DEFAULT FALSE,
            follow_up_date DATE,
            created_by VARCHAR(100) NOT NULL,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
        
        CREATE INDEX idx_interactions_contact ON contact_interactions(contact_id);
        CREATE INDEX idx_interactions_date ON contact_interactions(interaction_date DESC);
        CREATE INDEX idx_interactions_type ON contact_interactions(interaction_type);
        CREATE INDEX idx_interactions_follow_up ON contact_interactions(follow_up_date) WHERE follow_up_required = TRUE;
    END IF;
END $$;

-- From V8__create_opportunity_activities_table.sql (if not applied)
CREATE TABLE IF NOT EXISTS opportunity_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    opportunity_id UUID NOT NULL,
    created_by UUID NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    scheduled_date TIMESTAMP,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_opportunity_activities_opportunity ON opportunity_activities(opportunity_id);
CREATE INDEX IF NOT EXISTS idx_opportunity_activities_created_by ON opportunity_activities(created_by);
CREATE INDEX IF NOT EXISTS idx_opportunity_activities_type ON opportunity_activities(activity_type);
CREATE INDEX IF NOT EXISTS idx_opportunity_activities_timeline ON opportunity_activities(opportunity_id, created_at DESC);

-- Trigger for opportunity_activities (idempotent)
CREATE OR REPLACE FUNCTION set_completed_at()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.completed = TRUE AND OLD.completed = FALSE THEN
        NEW.completed_at = CURRENT_TIMESTAMP;
    ELSIF NEW.completed = FALSE THEN
        NEW.completed_at = NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_set_completed_at ON opportunity_activities;
CREATE TRIGGER trigger_set_completed_at
    BEFORE UPDATE ON opportunity_activities
    FOR EACH ROW
    EXECUTE FUNCTION set_completed_at();

-- From V10__create_contacts_table.sql (if not applied)
CREATE TABLE IF NOT EXISTS contacts (
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    salutation VARCHAR(20),
    title VARCHAR(50),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    position VARCHAR(100),
    decision_level VARCHAR(50),
    email VARCHAR(255),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_location_id UUID,
    birthday DATE,
    hobbies VARCHAR(500),
    family_status VARCHAR(50),
    children_count INTEGER,
    personal_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT pk_contacts PRIMARY KEY (id)
);

-- Add FK constraints only if tables exist
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'contacts') AND
       EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customers') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                      WHERE constraint_name = 'fk_contacts_customer') THEN
            ALTER TABLE contacts ADD CONSTRAINT fk_contacts_customer 
                FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE;
        END IF;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'contacts') AND
       EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customer_locations') THEN
        IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                      WHERE constraint_name = 'fk_contacts_location') THEN
            ALTER TABLE contacts ADD CONSTRAINT fk_contacts_location 
                FOREIGN KEY (assigned_location_id) REFERENCES customer_locations(id) ON DELETE SET NULL;
        END IF;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_contact_customer ON contacts(customer_id);
CREATE INDEX IF NOT EXISTS idx_contact_location ON contacts(assigned_location_id);
CREATE INDEX IF NOT EXISTS idx_contact_active ON contacts(is_active);
CREATE INDEX IF NOT EXISTS idx_contact_email ON contacts(LOWER(email));

CREATE UNIQUE INDEX IF NOT EXISTS idx_contact_primary_per_customer 
    ON contacts(customer_id) 
    WHERE is_primary = TRUE AND is_active = TRUE;

-- Trigger for contacts (idempotent)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_contacts_updated_at ON contacts;
CREATE TRIGGER update_contacts_updated_at 
    BEFORE UPDATE ON contacts 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Additional fixes for column existence
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'contacts') THEN
        ALTER TABLE contacts ADD COLUMN IF NOT EXISTS warmth_score INTEGER DEFAULT 50;
        ALTER TABLE contacts ADD COLUMN IF NOT EXISTS warmth_confidence INTEGER DEFAULT 0;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customer_contacts') THEN
        ALTER TABLE customer_contacts ADD COLUMN IF NOT EXISTS last_contact_date TIMESTAMP;
        CREATE INDEX IF NOT EXISTS idx_contact_last_contact_date ON customer_contacts(last_contact_date);
    END IF;
END $$;

-- Rename column if old name exists
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer_contacts' 
        AND column_name = 'preferred_contact_method'
    ) THEN
        ALTER TABLE customer_contacts 
        RENAME COLUMN preferred_contact_method TO preferred_communication_method;
    END IF;
END $$;

-- Fix für fehlende JSONB Spalten aus V33/V34 (werden nach diesem Fix gelöscht)
-- Diese waren out-of-order und wurden von Flyway ignoriert
ALTER TABLE customer_locations 
ADD COLUMN IF NOT EXISTS location_details JSONB;

ALTER TABLE customer_locations 
ADD COLUMN IF NOT EXISTS service_offerings JSONB;

CREATE INDEX IF NOT EXISTS idx_customer_locations_details 
ON customer_locations USING GIN (location_details);

CREATE INDEX IF NOT EXISTS idx_customer_locations_offerings 
ON customer_locations USING GIN (service_offerings);

-- This migration ensures all structures exist regardless of which duplicate Flyway chose
-- V33 and V34 will be deleted after this migration succeeds
-- Future migrations should use numbers > 120