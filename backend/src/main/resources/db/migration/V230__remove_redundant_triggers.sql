-- Sprint 2.1: Remove redundant database triggers for updated_at
-- JPA @PreUpdate handles this on the application level

DROP TRIGGER IF EXISTS leads_updated_at ON leads;
DROP TRIGGER IF EXISTS territories_updated_at ON territories;
DROP TRIGGER IF EXISTS user_lead_settings_updated_at ON user_lead_settings;
DROP TRIGGER IF EXISTS lead_activities_updated_at ON lead_activities;

DROP FUNCTION IF EXISTS update_updated_at();

-- Add comment to document that updated_at is managed by JPA
COMMENT ON COLUMN leads.updated_at IS 'Managed by JPA @PreUpdate';
COMMENT ON COLUMN territories.updated_at IS 'Managed by JPA @PreUpdate';
COMMENT ON COLUMN user_lead_settings.updated_at IS 'Managed by JPA @PreUpdate';