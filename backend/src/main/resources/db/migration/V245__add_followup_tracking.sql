-- V245: Add follow-up tracking fields to lead table
--
-- Sprint 2.1 - FP-235: T+3/T+7 Follow-up Automation
-- Fügt Tracking-Felder für automatisierte Follow-ups hinzu
--
-- Business Value: +40% Lead-Conversion durch systematische Follow-up Automation

-- Erweitere leads Tabelle um Follow-up Tracking
ALTER TABLE leads ADD COLUMN IF NOT EXISTS last_followup_at TIMESTAMP;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS followup_count INTEGER DEFAULT 0;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS t3_followup_sent BOOLEAN DEFAULT FALSE;
ALTER TABLE leads ADD COLUMN IF NOT EXISTS t7_followup_sent BOOLEAN DEFAULT FALSE;

-- Flexible metadata storage (JSONB) für DSGVO-Consent und Business-Type
ALTER TABLE leads ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}'::jsonb;

-- Index für effiziente Follow-up Queries
CREATE INDEX IF NOT EXISTS idx_lead_followup_tracking
    ON leads(status, registered_at, last_followup_at)
    WHERE clock_stopped_at IS NULL;

-- Index für T+3/T+7 Eligibility Checks
CREATE INDEX IF NOT EXISTS idx_lead_followup_eligibility
    ON leads(registered_at, t3_followup_sent, t7_followup_sent)
    WHERE status IN ('REGISTERED', 'ACTIVE');

-- Erweitere lead_activities für Follow-up Metadata
ALTER TABLE lead_activities
    ADD COLUMN IF NOT EXISTS followup_metadata JSONB;

-- Index für Follow-up Activity Queries
CREATE INDEX IF NOT EXISTS idx_lead_activity_followup
    ON lead_activities(lead_id, activity_date)
    WHERE metadata->>'followup_type' IS NOT NULL;

-- Sample Template für T+3 Follow-up
INSERT INTO campaign_templates (
    name,
    description,
    subject,
    html_content,
    text_content,
    template_type,
    active,
    created_at,
    updated_at,
    created_by
) VALUES (
    'T+3 Sample Follow-up',
    'Automatisches Follow-up nach 3 Tagen mit Sample-Angebot',
    'Gratis Produktproben für {{lead.company}} - Cook&Fresh® entdecken',
    '<html>
    <body style="font-family: Arial, sans-serif; color: #333;">
        <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
            <h2 style="color: #94C456;">Entdecken Sie Cook&Fresh® - Gratis Produktproben!</h2>
            <p>Guten Tag {{lead.contactPerson}},</p>
            <p>vielen Dank für Ihr Interesse an FreshFoodz Cook&Fresh®!</p>
            <p>Wir möchten Ihnen gerne unsere Premium-Qualität zeigen und haben ein spezielles Angebot für {{lead.company}}:</p>
            <ul style="color: #004F7B;">
                <li>Gratis Produktkatalog mit über 500 Artikeln</li>
                <li>Kostenlose Sample-Box: {{sample.products}}</li>
                <li>Persönliche Beratung durch Ihren Ansprechpartner</li>
            </ul>
            <p>Als {{lead.company}} im Bereich Gastronomie profitieren Sie von:</p>
            <ul>
                <li>Tagesfrischer Lieferung</li>
                <li>{{territory.name}}-spezifische Preise in {{territory.currency}}</li>
                <li>Flexible Bestellmengen ohne Mindestbestellwert für Samples</li>
            </ul>
            <div style="margin: 30px 0; text-align: center;">
                <a href="https://freshfoodz.de/sample-request?lead={{lead.id}}"
                   style="background-color: #94C456; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                   Jetzt Gratis-Proben anfordern
                </a>
            </div>
            <p>Bei Fragen stehe ich Ihnen gerne zur Verfügung.</p>
            <p>Mit freundlichen Grüßen<br>Ihr FreshFoodz Team</p>
            <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
            <p style="font-size: 12px; color: #666;">
                {{footer.legal}}<br>
                <a href="{{unsubscribe.url}}">Newsletter abbestellen</a>
            </p>
        </div>
    </body>
    </html>',
    'Entdecken Sie Cook&Fresh® - Gratis Produktproben für {{lead.company}}!',
    'SAMPLE_REQUEST',
    true,
    NOW(),
    NOW(),
    'SYSTEM'
) ON CONFLICT (name) DO NOTHING;

-- Sample Template für T+7 Follow-up
INSERT INTO campaign_templates (
    name,
    description,
    subject,
    html_content,
    text_content,
    template_type,
    active,
    created_at,
    updated_at,
    created_by
) VALUES (
    'T+7 Bulk Order Follow-up',
    'Automatisches Follow-up nach 7 Tagen mit Mengenrabatt',
    '{{bulk.discount}}% Rabatt für {{lead.company}} - Jetzt Großbestellung sichern!',
    '<html>
    <body style="font-family: Arial, sans-serif; color: #333;">
        <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
            <h2 style="color: #004F7B;">Exklusives Angebot: {{bulk.discount}}% Mengenrabatt!</h2>
            <p>Guten Tag {{lead.contactPerson}},</p>
            <p>als Dankeschön für Ihr Interesse an FreshFoodz Cook&Fresh® haben wir ein besonderes Angebot für {{lead.company}}:</p>
            <div style="background-color: #f5f5f5; padding: 20px; border-left: 4px solid #94C456; margin: 20px 0;">
                <h3 style="color: #94C456; margin-top: 0;">Ihr Vorteil:</h3>
                <ul style="margin: 0; padding-left: 20px;">
                    <li><strong>{{bulk.discount}}% Rabatt</strong> auf Ihre erste Großbestellung</li>
                    <li>Gültig ab nur <strong>{{bulk.minimum}} {{territory.currency}}</strong> Bestellwert</li>
                    <li>Kostenlose Lieferung inklusive</li>
                    <li>Persönliche Betreuung durch Ihren Account Manager</li>
                </ul>
            </div>
            <p>Dieses Angebot ist speziell auf Ihren Bedarf als Gastronomiebetrieb zugeschnitten:</p>
            <ul>
                <li>Premium-Qualität zu Großhandelspreisen</li>
                <li>Flexible Lieferzeiten (täglich 5-11 Uhr)</li>
                <li>{{territory.taxRate}}% MwSt ({{territory.name}})</li>
            </ul>
            <div style="margin: 30px 0; text-align: center;">
                <a href="https://freshfoodz.de/bulk-order?lead={{lead.id}}&discount={{bulk.discount}}"
                   style="background-color: #004F7B; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">
                   Jetzt {{bulk.discount}}% Rabatt sichern
                </a>
            </div>
            <p><strong>Angebot gültig bis: 14 Tage ab Erhalt dieser E-Mail</strong></p>
            <p>Mit freundlichen Grüßen<br>Ihr FreshFoodz Team</p>
            <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
            <p style="font-size: 12px; color: #666;">
                {{footer.legal}}<br>
                <a href="{{unsubscribe.url}}">Newsletter abbestellen</a>
            </p>
        </div>
    </body>
    </html>',
    'Exklusives Angebot: {{bulk.discount}}% Mengenrabatt für {{lead.company}}!',
    'FOLLOW_UP',
    true,
    NOW(),
    NOW(),
    'SYSTEM'
) ON CONFLICT (name) DO NOTHING;

-- Dokumentation
COMMENT ON COLUMN leads.last_followup_at IS 'Zeitpunkt des letzten automatisierten Follow-ups';
COMMENT ON COLUMN leads.followup_count IS 'Anzahl gesendeter automatisierter Follow-ups';
COMMENT ON COLUMN leads.t3_followup_sent IS 'Flag ob T+3 Sample Follow-up bereits gesendet wurde';
COMMENT ON COLUMN leads.t7_followup_sent IS 'Flag ob T+7 Bulk Order Follow-up bereits gesendet wurde';
COMMENT ON COLUMN lead_activities.followup_metadata IS 'Metadata für Follow-up Tracking (followup_type, template_id, etc.)';

-- Performance-Hinweis: Die Indizes sind optimiert für die häufigsten Follow-up Queries:
-- 1. Finde Leads die für T+3 oder T+7 Follow-up qualifiziert sind
-- 2. Prüfe ob Lead bereits Follow-up erhalten hat
-- 3. Analysiere Follow-up Performance über Activities