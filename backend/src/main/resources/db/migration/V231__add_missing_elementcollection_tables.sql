-- Sprint 2.1: Add missing ElementCollection tables
-- Diese Tabellen sollten in V229 erstellt worden sein, aber fehlen in einigen Umgebungen

-- Collaborators table for @ElementCollection in Lead entity
CREATE TABLE IF NOT EXISTS lead_collaborators (
    lead_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (lead_id, user_id),
    CONSTRAINT fk_lead_collaborators_lead
        FOREIGN KEY (lead_id)
        REFERENCES leads(id)
        ON DELETE CASCADE
);

-- Preferred territories table for @ElementCollection in UserLeadSettings
CREATE TABLE IF NOT EXISTS user_preferred_territories (
    user_settings_id BIGINT NOT NULL,
    territory_id VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_settings_id, territory_id),
    CONSTRAINT fk_user_pref_territories_settings
        FOREIGN KEY (user_settings_id)
        REFERENCES user_lead_settings(id)
        ON DELETE CASCADE
);

-- Indices for ElementCollections (if not exists)
CREATE INDEX IF NOT EXISTS idx_lead_collaborators_lead ON lead_collaborators(lead_id);
CREATE INDEX IF NOT EXISTS idx_lead_collaborators_user ON lead_collaborators(user_id);
CREATE INDEX IF NOT EXISTS idx_user_pref_territories ON user_preferred_territories(user_settings_id);

COMMENT ON TABLE lead_collaborators IS 'ElementCollection for Lead.collaboratorUserIds';
COMMENT ON TABLE user_preferred_territories IS 'ElementCollection for UserLeadSettings.preferredTerritories';