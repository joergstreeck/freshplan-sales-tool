-- V237: Performance Indexes for Lead Search - Function-based indexes
-- Sprint 2.1 PR #105: Optimize search queries with lower() function

-- Function-based indexes for case-insensitive search
-- These indexes allow the database to efficiently use lower(column) LIKE patterns

CREATE INDEX IF NOT EXISTS idx_leads_company_name_lower
ON leads(lower(company_name))
WHERE status != 'DELETED';

CREATE INDEX IF NOT EXISTS idx_leads_contact_person_lower
ON leads(lower(contact_person))
WHERE status != 'DELETED';

CREATE INDEX IF NOT EXISTS idx_leads_email_lower
ON leads(lower(email))
WHERE status != 'DELETED';

CREATE INDEX IF NOT EXISTS idx_leads_city_lower
ON leads(lower(city))
WHERE status != 'DELETED';

-- Composite index for common filter combinations
CREATE INDEX IF NOT EXISTS idx_leads_owner_status_updated
ON leads(owner_user_id, status, updated_at DESC)
WHERE status != 'DELETED';

-- Comments for documentation
COMMENT ON INDEX idx_leads_company_name_lower IS 'Function-based index for case-insensitive company name search';
COMMENT ON INDEX idx_leads_contact_person_lower IS 'Function-based index for case-insensitive contact person search';
COMMENT ON INDEX idx_leads_email_lower IS 'Function-based index for case-insensitive email search';
COMMENT ON INDEX idx_leads_city_lower IS 'Function-based index for case-insensitive city search';
COMMENT ON INDEX idx_leads_owner_status_updated IS 'Composite index for owner-based queries with status filter';