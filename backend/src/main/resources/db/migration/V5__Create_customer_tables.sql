-- V5: Create customer-related tables
-- Date: 2025-07-05
-- Author: Claude (FreshPlan Team)
-- Description: Create all tables needed for the Customer module

-- 1. Create customers table (main entity)
CREATE TABLE IF NOT EXISTS customers (
    id UUID PRIMARY KEY,
    customer_number VARCHAR(20) UNIQUE NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    trading_name VARCHAR(255),
    legal_form VARCHAR(100),
    customer_type VARCHAR(20) NOT NULL DEFAULT 'UNTERNEHMEN',
    industry VARCHAR(30),
    classification VARCHAR(10),
    parent_customer_id UUID REFERENCES customers(id),
    hierarchy_type VARCHAR(20) DEFAULT 'STANDALONE',
    status VARCHAR(20) NOT NULL DEFAULT 'LEAD',
    lifecycle_stage VARCHAR(20) DEFAULT 'ACQUISITION',
    partner_status VARCHAR(20) DEFAULT 'KEIN_PARTNER',
    expected_annual_volume DECIMAL(12,2),
    actual_annual_volume DECIMAL(12,2),
    payment_terms VARCHAR(20) DEFAULT 'NETTO_30',
    credit_limit DECIMAL(12,2),
    delivery_condition VARCHAR(30) DEFAULT 'STANDARD',
    risk_score INTEGER DEFAULT 0,
    last_contact_date TIMESTAMP,
    next_follow_up_date TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(100)
);

-- 2. Create contact_roles table (lookup table)
CREATE TABLE IF NOT EXISTS contact_roles (
    id UUID PRIMARY KEY,
    role_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_decision_maker_role BOOLEAN NOT NULL DEFAULT FALSE,
    hierarchy_level INTEGER
);

-- 3. Create customer_contacts table
CREATE TABLE IF NOT EXISTS customer_contacts (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id),
    salutation VARCHAR(20),
    title VARCHAR(50),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    position VARCHAR(100),
    department VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    mobile VARCHAR(50),
    fax VARCHAR(50),
    preferred_contact_method VARCHAR(20),
    language_preference VARCHAR(5) DEFAULT 'de',
    reports_to_id UUID REFERENCES customer_contacts(id),
    is_primary BOOLEAN DEFAULT FALSE,
    is_decision_maker BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_marketing_allowed BOOLEAN DEFAULT FALSE,
    birthday DATE,
    notes TEXT,
    linkedin_profile VARCHAR(255),
    xing_profile VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

-- 4. Create many-to-many relationship table for contact roles
CREATE TABLE IF NOT EXISTS customer_contact_roles (
    contact_id UUID NOT NULL REFERENCES customer_contacts(id),
    role_id UUID NOT NULL REFERENCES contact_roles(id),
    PRIMARY KEY (contact_id, role_id)
);

-- 5. Create customer_locations table
CREATE TABLE IF NOT EXISTS customer_locations (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id),
    location_name VARCHAR(255) NOT NULL,
    location_code VARCHAR(50),
    location_type VARCHAR(30),
    category VARCHAR(30),
    is_main_location BOOLEAN DEFAULT FALSE,
    is_billing_location BOOLEAN DEFAULT FALSE,
    is_delivery_location BOOLEAN DEFAULT FALSE,
    max_delivery_weight DECIMAL(10,2),
    has_loading_dock BOOLEAN DEFAULT FALSE,
    has_forklift BOOLEAN DEFAULT FALSE,
    delivery_restrictions TEXT,
    operating_hours TEXT,
    contact_person VARCHAR(255),
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

-- 6. Create customer_addresses table
CREATE TABLE IF NOT EXISTS customer_addresses (
    id UUID PRIMARY KEY,
    location_id UUID NOT NULL REFERENCES customer_locations(id),
    address_type VARCHAR(30) NOT NULL,
    care_of VARCHAR(100),
    street VARCHAR(255),
    street_number VARCHAR(20),
    additional_line VARCHAR(255),
    po_box VARCHAR(50),
    postal_code VARCHAR(20),
    city VARCHAR(100),
    state_province VARCHAR(100),
    country VARCHAR(3) DEFAULT 'DEU',
    building_name VARCHAR(100),
    floor_apartment VARCHAR(50),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    geocoded_at TIMESTAMP,
    is_validated BOOLEAN DEFAULT FALSE,
    validated_at TIMESTAMP,
    validation_service VARCHAR(50),
    is_primary_for_type BOOLEAN DEFAULT FALSE,
    delivery_instructions TEXT,
    access_instructions TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

-- 7. Create customer_timeline_events table
CREATE TABLE IF NOT EXISTS customer_timeline_events (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES customers(id),
    event_type VARCHAR(50) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(30) NOT NULL,
    importance VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    performed_by VARCHAR(100) NOT NULL,
    performed_by_role VARCHAR(50),
    source_system VARCHAR(50) DEFAULT 'FreshPlan CRM',
    source_reference VARCHAR(255),
    related_contact_id UUID,
    related_location_id UUID,
    related_document_id UUID,
    communication_channel VARCHAR(30),
    communication_direction VARCHAR(20),
    communication_duration INTEGER,
    requires_follow_up BOOLEAN NOT NULL DEFAULT FALSE,
    follow_up_date TIMESTAMP,
    follow_up_notes TEXT,
    follow_up_completed BOOLEAN NOT NULL DEFAULT FALSE,
    old_values VARCHAR(2000),
    new_values VARCHAR(2000),
    changed_fields VARCHAR(500),
    business_impact VARCHAR(500),
    revenue_impact DECIMAL(12,2),
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    is_customer_visible BOOLEAN NOT NULL DEFAULT FALSE,
    access_level VARCHAR(20) DEFAULT 'INTERNAL',
    tags VARCHAR(500),
    labels VARCHAR(500),
    external_id VARCHAR(100),
    external_url VARCHAR(500),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100)
);

-- 8. Create indexes for performance
CREATE INDEX idx_customer_status ON customers(status) WHERE is_deleted = FALSE;
CREATE INDEX idx_customer_deleted ON customers(is_deleted);
CREATE INDEX idx_customer_parent ON customers(parent_customer_id) WHERE parent_customer_id IS NOT NULL;
CREATE INDEX idx_customer_risk_score ON customers(risk_score) WHERE is_deleted = FALSE;
CREATE INDEX idx_customer_last_contact ON customers(last_contact_date) WHERE is_deleted = FALSE;

CREATE INDEX idx_contact_customer ON customer_contacts(customer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_contact_primary ON customer_contacts(customer_id, is_primary) WHERE is_deleted = FALSE AND is_primary = TRUE;
CREATE INDEX idx_contact_email ON customer_contacts(email) WHERE is_deleted = FALSE;

CREATE INDEX idx_location_customer ON customer_locations(customer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_location_main ON customer_locations(customer_id, is_main_location) WHERE is_deleted = FALSE AND is_main_location = TRUE;

CREATE INDEX idx_address_location ON customer_addresses(location_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_address_type ON customer_addresses(location_id, address_type) WHERE is_deleted = FALSE;

CREATE INDEX idx_timeline_customer ON customer_timeline_events(customer_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_timeline_date ON customer_timeline_events(event_date DESC) WHERE is_deleted = FALSE;
CREATE INDEX idx_timeline_category ON customer_timeline_events(category) WHERE is_deleted = FALSE;
CREATE INDEX idx_timeline_importance ON customer_timeline_events(importance) WHERE is_deleted = FALSE;

-- 9. Insert default contact roles
INSERT INTO contact_roles (id, role_name, description, is_decision_maker_role, hierarchy_level) VALUES
    (gen_random_uuid(), 'Geschäftsführer', 'Geschäftsführung', true, 1),
    (gen_random_uuid(), 'Einkaufsleiter', 'Leitung Einkauf', true, 2),
    (gen_random_uuid(), 'Einkäufer', 'Einkauf Sachbearbeitung', false, 3),
    (gen_random_uuid(), 'Küchenleiter', 'Leitung Küche', true, 2),
    (gen_random_uuid(), 'Koch', 'Küchenmitarbeiter', false, 3),
    (gen_random_uuid(), 'Verwaltung', 'Administration', false, 3),
    (gen_random_uuid(), 'Buchhaltung', 'Finanzbuchhaltung', false, 3),
    (gen_random_uuid(), 'IT-Verantwortlicher', 'IT-Administration', false, 3),
    (gen_random_uuid(), 'Qualitätsmanager', 'Qualitätssicherung', false, 2);

-- 10. Add comments for documentation
COMMENT ON TABLE customers IS 'Main customer entity with full CRM capabilities';
COMMENT ON TABLE customer_contacts IS 'Contact persons associated with customers';
COMMENT ON TABLE customer_locations IS 'Physical locations/sites of customers';
COMMENT ON TABLE customer_addresses IS 'Addresses for customer locations';
COMMENT ON TABLE customer_timeline_events IS 'Activity and change history for customers';
COMMENT ON TABLE contact_roles IS 'Predefined roles for customer contacts';