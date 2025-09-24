-- Sprint 2.1 PR #1: Territory Management for Lead System
-- Territory-Assignment ohne Gebietsschutz (deutschlandweite Lead-Verfügbarkeit)
-- Territory nur für Currency/Tax/Business-Rules

-- Territory configuration table
CREATE TABLE territories (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    country_code VARCHAR(2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    tax_rate DECIMAL(5,2) NOT NULL,
    language_code VARCHAR(5) NOT NULL,
    business_rules JSONB DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Insert default territories (Germany and Switzerland)
INSERT INTO territories (id, name, country_code, currency_code, tax_rate, language_code, business_rules) VALUES
('DE', 'Deutschland', 'DE', 'EUR', 19.00, 'de-DE', '{"invoicing": "monthly", "payment_terms": 30, "delivery_zones": ["north", "south", "east", "west"]}'),
('CH', 'Schweiz', 'CH', 'CHF', 7.70, 'de-CH', '{"invoicing": "monthly", "payment_terms": 45, "delivery_zones": ["zurich", "basel", "bern"]}');

-- Leads table with territory assignment
CREATE TABLE leads (
    id BIGSERIAL PRIMARY KEY,
    -- Basic lead information
    company_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(50),
    website VARCHAR(255),

    -- Address and territory
    street VARCHAR(255),
    postal_code VARCHAR(20),
    city VARCHAR(100),
    country_code VARCHAR(2) NOT NULL DEFAULT 'DE',
    territory_id VARCHAR(10) NOT NULL REFERENCES territories(id),

    -- Industry specific
    industry VARCHAR(50),
    business_type VARCHAR(100), -- Restaurant/Hotel/Kantinen/Catering
    kitchen_size VARCHAR(20), -- small/medium/large
    employee_count INTEGER,
    estimated_volume DECIMAL(12,2),

    -- Lead status and ownership (NO geographical protection)
    status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED',
    owner_user_id VARCHAR(50) NOT NULL,
    collaborator_user_ids TEXT[], -- Array of additional users with access

    -- State machine timestamps
    registered_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMP,
    reminder_sent_at TIMESTAMP,
    grace_period_start_at TIMESTAMP,
    expired_at TIMESTAMP,

    -- Stop-the-clock system
    clock_stopped_at TIMESTAMP,
    stop_reason TEXT,
    stop_approved_by VARCHAR(50),

    -- Protection system (user-based, not territory-based)
    protection_start_at TIMESTAMP NOT NULL DEFAULT NOW(),
    protection_months INTEGER NOT NULL DEFAULT 6,
    protection_days_60 INTEGER NOT NULL DEFAULT 60,
    protection_days_10 INTEGER NOT NULL DEFAULT 10,

    -- Metadata
    source VARCHAR(100), -- web/email/phone/event/partner
    source_campaign VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(50) NOT NULL,
    updated_by VARCHAR(50)
);

-- User lead settings for territory preferences
CREATE TABLE user_lead_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE,

    -- Provision settings
    default_provision_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0700, -- 7%
    reduced_provision_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0200, -- 2%

    -- Protection timing
    lead_protection_months INTEGER NOT NULL DEFAULT 6,
    activity_reminder_days INTEGER NOT NULL DEFAULT 60,
    grace_period_days INTEGER NOT NULL DEFAULT 10,

    -- Territory preferences (NOT restrictions - leads are available nationwide)
    preferred_territories VARCHAR(10)[] DEFAULT ARRAY['DE'],
    can_access_all_territories BOOLEAN DEFAULT true,

    -- Permissions
    can_stop_clock BOOLEAN DEFAULT false,
    can_override_protection BOOLEAN DEFAULT false,
    max_leads_per_month INTEGER DEFAULT 100,

    -- Settings
    email_notifications BOOLEAN DEFAULT true,
    push_notifications BOOLEAN DEFAULT false,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Lead activity tracking
CREATE TABLE lead_activities (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id VARCHAR(50) NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    activity_date TIMESTAMP NOT NULL DEFAULT NOW(),
    description TEXT,
    metadata JSONB DEFAULT '{}',

    -- Activity impact on lead status
    is_meaningful_contact BOOLEAN DEFAULT false,
    resets_timer BOOLEAN DEFAULT false,

    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_leads_territory ON leads(territory_id);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_owner ON leads(owner_user_id);
CREATE INDEX idx_leads_protection_dates ON leads(protection_start_at, protection_months);
CREATE INDEX idx_leads_last_activity ON leads(last_activity_at);
CREATE INDEX idx_lead_activities_lead ON lead_activities(lead_id, activity_date);
CREATE INDEX idx_lead_activities_user ON lead_activities(user_id, activity_date);

-- Check constraints
ALTER TABLE leads ADD CONSTRAINT chk_lead_status
    CHECK (status IN ('REGISTERED', 'ACTIVE', 'REMINDER_SENT', 'GRACE_PERIOD', 'EXPIRED', 'EXTENDED', 'STOP_CLOCK'));

ALTER TABLE lead_activities ADD CONSTRAINT chk_activity_type
    CHECK (activity_type IN ('EMAIL', 'CALL', 'MEETING', 'SAMPLE_SENT', 'ORDER', 'NOTE', 'STATUS_CHANGE'));

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER leads_updated_at
    BEFORE UPDATE ON leads
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER territories_updated_at
    BEFORE UPDATE ON territories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER user_lead_settings_updated_at
    BEFORE UPDATE ON user_lead_settings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- Lead status change events for CQRS
CREATE OR REPLACE FUNCTION notify_lead_status_change()
RETURNS TRIGGER AS $$
DECLARE
    event_payload JSONB;
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        event_payload := jsonb_build_object(
            'lead_id', NEW.id,
            'old_status', OLD.status,
            'new_status', NEW.status,
            'owner_user_id', NEW.owner_user_id,
            'territory_id', NEW.territory_id,
            'timestamp', NOW()
        );

        PERFORM pg_notify('lead_status_changed', event_payload::text);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER lead_status_change_notify
    AFTER UPDATE ON leads
    FOR EACH ROW EXECUTE FUNCTION notify_lead_status_change();

COMMENT ON TABLE territories IS 'Territory configuration for currency, tax and business rules - NO geographical protection';
COMMENT ON TABLE leads IS 'Lead management with user-based protection (not territory-based)';
COMMENT ON TABLE user_lead_settings IS 'User preferences for lead management and territory operations';
COMMENT ON TABLE lead_activities IS 'Activity tracking for leads with meaningful contact detection';