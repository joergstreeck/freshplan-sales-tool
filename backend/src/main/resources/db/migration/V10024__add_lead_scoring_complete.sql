-- V10024: Add complete lead scoring fields
-- Sprint 2.1.6+: Lead Scoring System (4-dimensional scoring)
--
-- Adds:
-- - Revenue scoring fields (budget_confirmed, deal_size)
-- - Score cache columns (pain_score, revenue_score, fit_score, engagement_score)
-- - Indexes for filtering/sorting by scores
--
-- Author: Sprint 2.1.6+ Lead Scoring System
-- Date: 2025-10-10
-- References: Lead Scoring System Design, 4-dimensional scoring (Revenue, Fit, Pain, Engagement)

-- ============================================================================
-- 1. REVENUE SCORING FIELDS
-- ============================================================================

-- Budget confirmation flag (user input)
ALTER TABLE leads
ADD COLUMN IF NOT EXISTS budget_confirmed BOOLEAN DEFAULT FALSE;

-- Deal size category (auto-calculated from estimated_volume or user override)
ALTER TABLE leads
ADD COLUMN IF NOT EXISTS deal_size VARCHAR(20);

-- ============================================================================
-- 2. SCORE CACHE COLUMNS (Performance optimization)
-- ============================================================================

-- Pain Points Score (0-100): pain count + severity + urgency
ALTER TABLE leads
ADD COLUMN IF NOT EXISTS pain_score INTEGER DEFAULT 0;

-- Revenue Score (0-100): volume + budget + deal size
ALTER TABLE leads
ADD COLUMN IF NOT EXISTS revenue_score INTEGER DEFAULT 0;

-- Fit Score (0-100): segment + location + source quality
ALTER TABLE leads
ADD COLUMN IF NOT EXISTS fit_score INTEGER DEFAULT 0;

-- Engagement Score (0-100): relationship quality + activities
ALTER TABLE leads
ADD COLUMN IF NOT EXISTS engagement_score INTEGER DEFAULT 0;

-- Note: lead_score (total 0-100) already exists

-- ============================================================================
-- 3. INDEXES (for filtering/sorting by scores)
-- ============================================================================

CREATE INDEX IF NOT EXISTS idx_leads_pain_score ON leads(pain_score);
CREATE INDEX IF NOT EXISTS idx_leads_revenue_score ON leads(revenue_score);
CREATE INDEX IF NOT EXISTS idx_leads_fit_score ON leads(fit_score);
CREATE INDEX IF NOT EXISTS idx_leads_engagement_score ON leads(engagement_score);

-- Composite index for multi-dimensional scoring queries
CREATE INDEX IF NOT EXISTS idx_leads_scoring_composite ON leads(lead_score, revenue_score, fit_score);

-- ============================================================================
-- 4. COLUMN COMMENTS (Documentation)
-- ============================================================================

COMMENT ON COLUMN leads.budget_confirmed IS 'Customer budget approved/confirmed (user input)';
COMMENT ON COLUMN leads.deal_size IS 'Deal size category: SMALL, MEDIUM, LARGE, ENTERPRISE (auto-calculated or manual override)';
COMMENT ON COLUMN leads.pain_score IS 'Pain Points Score (0-100): pain count + severity + urgency';
COMMENT ON COLUMN leads.revenue_score IS 'Revenue Score (0-100): volume + budget + deal size';
COMMENT ON COLUMN leads.fit_score IS 'Fit Score (0-100): segment + location + source quality';
COMMENT ON COLUMN leads.engagement_score IS 'Engagement Score (0-100): relationship quality + activities';

-- ============================================================================
-- MIGRATION SUMMARY
-- ============================================================================
-- Added: 2 revenue scoring fields (budget_confirmed, deal_size)
-- Added: 4 score cache columns (pain_score, revenue_score, fit_score, engagement_score)
-- Added: 5 indexes for score-based queries
-- Impact: Enables 4-dimensional lead scoring (Revenue 25% + Fit 25% + Pain 25% + Engagement 25%)
