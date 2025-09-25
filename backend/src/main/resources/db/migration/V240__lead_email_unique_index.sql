-- V240: Lead email deduplication unique index
-- Sprint 2.1: Verhindert doppelte Emails bei aktiven Leads
-- Der partielle Index ignoriert DELETED Status für Soft-Delete Support

-- Partieller Unique-Index auf email_normalized (ignoriert DELETED Leads)
CREATE UNIQUE INDEX IF NOT EXISTS ux_leads_email_norm_active
ON leads(email_normalized)
WHERE status != 'DELETED'
  AND email_normalized IS NOT NULL;

-- Index für Performance bei Email-Lookups
CREATE INDEX IF NOT EXISTS idx_leads_email_normalized
ON leads(email_normalized)
WHERE email_normalized IS NOT NULL;

COMMENT ON INDEX ux_leads_email_norm_active IS 'Unique constraint für aktive Leads - erlaubt Soft-Delete';
COMMENT ON INDEX idx_leads_email_normalized IS 'Performance-Index für Email-Deduplikation';