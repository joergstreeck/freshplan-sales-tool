-- Sprint 2.1.6 Phase 4: Lead-Scoring System (ADR-006 Phase 2)
-- Fügt lead_score Column für 0-100 Punkte Bewertungssystem hinzu

-- Add lead_score column
ALTER TABLE leads ADD COLUMN lead_score INTEGER;

-- Add constraint for valid score range (0-100)
ALTER TABLE leads ADD CONSTRAINT chk_lead_score
    CHECK (lead_score IS NULL OR (lead_score >= 0 AND lead_score <= 100));

-- Add index for sorting by score (most useful queries: "Top Leads")
CREATE INDEX idx_leads_score ON leads(lead_score DESC NULLS LAST);

-- Add comment for documentation
COMMENT ON COLUMN leads.lead_score IS 'Lead quality score 0-100 points (Sprint 2.1.6 ADR-006 Phase 2). Calculated from: Umsatzpotenzial (25%), Engagement (25%), Fit (25%), Dringlichkeit (25%)';
