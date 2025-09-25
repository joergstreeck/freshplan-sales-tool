-- V232: Campaign Templates Foundation for Sprint 2.1
-- Based on: modules/leads/domain/CampaignTemplate.java
-- Purpose: Foundation for future A/B testing and personalization (Phase 3)

CREATE TABLE IF NOT EXISTS campaign_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    subject VARCHAR(255) NOT NULL,
    html_content TEXT,
    text_content TEXT,
    variables JSONB DEFAULT '{}'::jsonb,
    template_type VARCHAR(50) DEFAULT 'STANDARD',
    primary_color VARCHAR(7) DEFAULT '#94C456', -- FreshFoodz Green
    secondary_color VARCHAR(7) DEFAULT '#004F7B', -- FreshFoodz Blue
    active BOOLEAN NOT NULL DEFAULT true,
    times_used INTEGER DEFAULT 0,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),

    CONSTRAINT ck_template_type CHECK (template_type IN (
        'STANDARD', 'SAMPLE_REQUEST', 'FOLLOW_UP',
        'SEASONAL', 'NEWSLETTER', 'WELCOME', 'REACTIVATION'
    ))
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_campaign_templates_active ON campaign_templates(active);
CREATE INDEX IF NOT EXISTS idx_campaign_templates_type ON campaign_templates(template_type);
CREATE INDEX IF NOT EXISTS idx_campaign_templates_created_by ON campaign_templates(created_by);

-- Comments for documentation
COMMENT ON TABLE campaign_templates IS 'Email campaign templates with FreshFoodz CI compliance';
COMMENT ON COLUMN campaign_templates.variables IS 'JSON object containing template variables for personalization';
COMMENT ON COLUMN campaign_templates.template_type IS 'Type of campaign template (STANDARD, SAMPLE_REQUEST, etc.)';
COMMENT ON COLUMN campaign_templates.primary_color IS 'FreshFoodz primary brand color';
COMMENT ON COLUMN campaign_templates.secondary_color IS 'FreshFoodz secondary brand color';

-- Sample templates for FreshFoodz B2B Food business
INSERT INTO campaign_templates (name, description, subject, html_content, text_content, template_type, created_by)
VALUES
    (
        'Cook&Fresh Sample Welcome',
        'Template for new leads requesting Cook&Fresh samples',
        'Ihre Cook&Fresh Probe ist unterwegs, {{lead.contactPerson}}!',
        '<html><body style="font-family: Poppins, sans-serif; color: #333;">
        <h1 style="color: #94C456;">Willkommen bei FreshFoodz!</h1>
        <p>Sehr geehrte(r) {{lead.contactPerson}},</p>
        <p>vielen Dank für Ihr Interesse an unseren Cook&Fresh Produkten.</p>
        <p>Ihre kostenlose Probe für {{lead.companyName}} ist bereits unterwegs!</p>
        </body></html>',
        'Willkommen bei FreshFoodz! Ihre Cook&Fresh Probe ist unterwegs.',
        'SAMPLE_REQUEST',
        'SYSTEM'
    ),
    (
        'Follow-Up nach 3 Tagen',
        'Automatisches Follow-Up 3 Tage nach Sample-Versand',
        'Wie schmeckt Ihnen Cook&Fresh, {{lead.contactPerson}}?',
        '<html><body style="font-family: Poppins, sans-serif; color: #333;">
        <h2 style="color: #004F7B;">Ihre Meinung ist uns wichtig!</h2>
        <p>Haben Sie unsere Proben bereits testen können?</p>
        <p>Wir würden uns über Ihr Feedback freuen.</p>
        </body></html>',
        'Wie hat Ihnen unsere Cook&Fresh Probe geschmeckt?',
        'FOLLOW_UP',
        'SYSTEM'
    );