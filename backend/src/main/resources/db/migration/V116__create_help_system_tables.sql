-- =====================================================
-- Help System Database Schema
-- Migration V116: In-App Help System Tables
-- Created: 2025-08-02 for TODO-66 Implementation
-- =====================================================

-- Help Contents Table
CREATE TABLE IF NOT EXISTS help_contents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature VARCHAR(100) NOT NULL,
    help_type VARCHAR(50) NOT NULL CHECK (help_type IN ('TOOLTIP', 'TOUR', 'FAQ', 'VIDEO', 'TUTORIAL', 'PROACTIVE', 'FIRST_TIME', 'CORRECTION')),
    title VARCHAR(200) NOT NULL,
    short_content TEXT,
    medium_content TEXT,
    detailed_content TEXT,
    video_url VARCHAR(500),
    target_user_level VARCHAR(20) NOT NULL DEFAULT 'BEGINNER' CHECK (target_user_level IN ('BEGINNER', 'INTERMEDIATE', 'EXPERT')),
    trigger_conditions TEXT,
    interaction_data TEXT,
    
    -- Analytics fields
    view_count BIGINT NOT NULL DEFAULT 0,
    helpful_count BIGINT NOT NULL DEFAULT 0,
    not_helpful_count BIGINT NOT NULL DEFAULT 0,
    avg_time_spent INTEGER, -- seconds
    
    -- Status and metadata
    is_active BOOLEAN NOT NULL DEFAULT true,
    priority INTEGER NOT NULL DEFAULT 10,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Help Content Target Roles Junction Table
CREATE TABLE IF NOT EXISTS help_content_roles (
    help_content_id UUID NOT NULL REFERENCES help_contents(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (help_content_id, role)
);

-- Help Feedback Table (für detailliertere Analytics)
CREATE TABLE IF NOT EXISTS help_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    help_content_id UUID NOT NULL REFERENCES help_contents(id) ON DELETE CASCADE,
    user_id VARCHAR(100) NOT NULL,
    session_id VARCHAR(100),
    helpful BOOLEAN NOT NULL,
    time_spent INTEGER, -- seconds
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Help Usage Tracking (für Struggle Detection)
CREATE TABLE IF NOT EXISTS help_usage_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(100) NOT NULL,
    session_id VARCHAR(100),
    feature VARCHAR(100) NOT NULL,
    help_content_id UUID REFERENCES help_contents(id),
    action VARCHAR(50) NOT NULL, -- 'requested', 'viewed', 'dismissed', 'completed'
    context JSONB,
    struggle_detected BOOLEAN DEFAULT false,
    struggle_type VARCHAR(50),
    struggle_severity INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- Indexes for Performance
-- =====================================================

-- Help Contents Indexes
CREATE INDEX IF NOT EXISTS idx_help_contents_feature 
    ON help_contents(feature) WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_help_contents_type_feature 
    ON help_contents(help_type, feature) WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_help_contents_user_level 
    ON help_contents(target_user_level) WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_help_contents_priority 
    ON help_contents(priority, created_at) WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_help_contents_view_count 
    ON help_contents(view_count DESC) WHERE is_active = true;

CREATE INDEX IF NOT EXISTS idx_help_contents_helpfulness 
    ON help_contents((helpful_count::float / NULLIF(helpful_count + not_helpful_count, 0)) DESC) 
    WHERE is_active = true AND (helpful_count + not_helpful_count) > 0;

-- Full-text search index for content
CREATE INDEX IF NOT EXISTS idx_help_contents_search 
    ON help_contents USING gin(to_tsvector('german', 
        COALESCE(title, '') || ' ' || 
        COALESCE(short_content, '') || ' ' || 
        COALESCE(medium_content, '') || ' ' || 
        COALESCE(detailed_content, ''))) 
    WHERE is_active = true;

-- Help Feedback Indexes  
CREATE INDEX IF NOT EXISTS idx_help_feedback_content_id 
    ON help_feedback(help_content_id);

CREATE INDEX IF NOT EXISTS idx_help_feedback_user_id 
    ON help_feedback(user_id);

CREATE INDEX IF NOT EXISTS idx_help_feedback_created_at 
    ON help_feedback(created_at DESC);

-- Help Usage Tracking Indexes
CREATE INDEX IF NOT EXISTS idx_help_usage_user_session 
    ON help_usage_tracking(user_id, session_id);

CREATE INDEX IF NOT EXISTS idx_help_usage_feature 
    ON help_usage_tracking(feature);

CREATE INDEX IF NOT EXISTS idx_help_usage_struggle 
    ON help_usage_tracking(struggle_detected, struggle_type) WHERE struggle_detected = true;

CREATE INDEX IF NOT EXISTS idx_help_usage_created_at 
    ON help_usage_tracking(created_at DESC);

-- =====================================================
-- Initial Help Content Data
-- =====================================================

-- Cost Management Help Content (von unserem vorherigen Feature)
INSERT INTO help_contents (
    feature, help_type, title, target_user_level,
    short_content, medium_content, detailed_content, priority
) VALUES 
(
    'cost-management',
    'TOOLTIP',
    'Cost Management System',
    'BEGINNER',
    'Überwacht und kontrolliert die Kosten für AI-Services.',
    'Das Cost Management System trackt alle Ausgaben für externe AI-Services (OpenAI, Anthropic) und warnt bei Budget-Überschreitungen.',
    'Unser intelligentes Cost Management System bietet Multi-Level Budget Control mit globalen, service-, feature- und user-spezifischen Limits. Es tracked Kosten in Echtzeit, sendet Alerts bei 80% der Budget-Limits und stoppt automatisch bei 95%. Alle Transaktionen werden vollständig protokolliert für Transparenz und Optimierung.',
    1
),
(
    'cost-management',
    'TUTORIAL', 
    'Cost Management Setup',
    'INTERMEDIATE',
    'Schritt-für-Schritt Anleitung zur Budget-Konfiguration.',
    'Lernen Sie, wie Sie Budget-Limits setzen, Alerts konfigurieren und Cost-Reports erstellen.',
    'Vollständige Anleitung: 1) Budget-Limits definieren (Global → Service → Feature), 2) Alert-Schwellenwerte festlegen (empfohlen: 80% Warning, 95% Stop), 3) Cost Dashboard überwachen, 4) Reports für Optimierung nutzen. Das System unterstützt tägliche, wöchentliche und monatliche Budget-Zyklen.',
    2
),
(
    'warmth-score',
    'TOOLTIP',
    'Beziehungswärme verstehen',
    'BEGINNER', 
    'Der Warmth Score zeigt die Qualität Ihrer Kundenbeziehung (0-100).',
    'Berechnet aus Interaktionshäufigkeit, Antwortzeiten, Meeting-Teilnahme und Kaufhistorie. Rot (0-40) = Handlungsbedarf, Gelb (41-70) = OK, Grün (71-100) = Exzellent.',
    'Der Warmth Score ist ein KI-gestützter Indikator für Beziehungsqualität. Faktoren: Häufigkeit der Kontakte (+), schnelle Antworten (+), Meeting-Teilnahme (+), positive Interaktionen (+), lange Pausen ohne Kontakt (-), verpasste Termine (-). Der Score wird täglich aktualisiert und hilft bei der Priorisierung von Follow-ups.',
    1
),
(
    'customer-wizard',
    'FIRST_TIME',
    'Neukunden-Wizard Einführung', 
    'BEGINNER',
    'Willkommen zum Neukunden-Wizard! Wir führen Sie durch alle wichtigen Schritte.',
    'Dieser Wizard hilft Ihnen dabei, neue Kunden systematisch zu erfassen: Basis-Daten, Ansprechpartner, Services und erste Angebote. Jeder Schritt baut auf dem vorherigen auf.',
    'Der 4-Schritt Neukunden-Wizard: Schritt 1 sammelt Grunddaten (Firma, Branche, Größe), Schritt 2 erfasst alle Ansprechpartner mit Rollen, Schritt 3 definiert gewünschte Services, Schritt 4 erstellt das erste Angebot. Alle Daten werden automatisch gespeichert und können jederzeit bearbeitet werden. Der Wizard nutzt KI-Unterstützung für Vorschläge und Validierung.',
    1
),
(
    'dashboard',
    'TOUR',
    'Dashboard Tour',
    'BEGINNER',
    'Entdecken Sie alle wichtigen Bereiche Ihres Sales-Dashboards.',
    'Interaktive Tour durch: Sales-Pipeline, Kunden-Übersicht, Aktivitäten-Feed, KPI-Widgets und Quick-Actions. Dauer: ca. 3 Minuten.',
    'Vollständige Dashboard-Tour mit 8 Stationen: 1) Pipeline-Übersicht mit Drag&Drop, 2) Kunden-Karten mit Warmth-Indikatoren, 3) Aktivitäten-Timeline, 4) KPI-Widgets (Umsatz, Abschlussrate, avg. Deal-Size), 5) Quick-Create Buttons, 6) Filteroptionen, 7) Export-Funktionen, 8) Personalisierung. Jede Station erklärt die wichtigsten Features und Best Practices.',
    1
);

-- Default Budget Limits für Cost Management Demo
INSERT INTO help_contents (
    feature, help_type, title, target_user_level,
    short_content, medium_content, priority
) VALUES 
(
    'cost-management',
    'FAQ',
    'Cost Management FAQ',
    'INTERMEDIATE',
    'Häufige Fragen zum Cost Management System.',
    'Q: Wie oft werden Budgets überprüft? A: In Echtzeit bei jeder Transaktion. Q: Was passiert bei Budget-Überschreitung? A: Automatischer Stop bei 95%, Warnung bei 80%. Q: Kann ich historische Kosten einsehen? A: Ja, über das Cost Dashboard mit Filteroptionen.',
    3
);

-- =====================================================
-- Constraints and Rules
-- =====================================================

-- Ensure at least one content type exists per feature
-- (This would be implemented as application logic, not DB constraint)

-- Ensure priority is reasonable (1-100)
ALTER TABLE help_contents ADD CONSTRAINT check_priority_range 
    CHECK (priority >= 1 AND priority <= 100);

-- Ensure avg_time_spent is positive
ALTER TABLE help_contents ADD CONSTRAINT check_avg_time_positive 
    CHECK (avg_time_spent IS NULL OR avg_time_spent > 0);

-- Ensure time_spent in feedback is positive  
ALTER TABLE help_feedback ADD CONSTRAINT check_time_spent_positive
    CHECK (time_spent IS NULL OR time_spent > 0);

-- Ensure struggle_severity is in range 1-10
ALTER TABLE help_usage_tracking ADD CONSTRAINT check_struggle_severity_range
    CHECK (struggle_severity IS NULL OR (struggle_severity >= 1 AND struggle_severity <= 10));

-- =====================================================
-- Functions for Analytics (Optional - PostgreSQL specific)
-- =====================================================

-- Function to calculate helpfulness rate
CREATE OR REPLACE FUNCTION calculate_helpfulness_rate(content_id UUID)
RETURNS DECIMAL(5,2) AS $$
DECLARE
    helpful_count BIGINT;
    total_count BIGINT;
BEGIN
    SELECT h.helpful_count, (h.helpful_count + h.not_helpful_count)
    INTO helpful_count, total_count
    FROM help_contents h
    WHERE h.id = content_id;
    
    IF total_count = 0 THEN
        RETURN 0.0;
    END IF;
    
    RETURN ROUND((helpful_count::DECIMAL / total_count) * 100, 2);
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- Completion Log
-- =====================================================

-- Log successful migration
INSERT INTO help_contents (
    feature, help_type, title, target_user_level,
    short_content, priority, created_by
) VALUES (
    'system',
    'TOOLTIP', 
    'Help System Migration Completed',
    'EXPERT',
    'V116 Migration: Help System erfolgreich initialisiert mit 5 Content-Beispielen.',
    100,
    'migration-v116'
) ON CONFLICT DO NOTHING;