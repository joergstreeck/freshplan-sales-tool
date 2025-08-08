-- Add missing fields to customer_contacts table from Sprint 2 integration
-- Diese Felder werden von der Contact Entity erwartet

ALTER TABLE customer_contacts 
ADD COLUMN IF NOT EXISTS decision_level VARCHAR(50),
ADD COLUMN IF NOT EXISTS buying_role VARCHAR(100),
ADD COLUMN IF NOT EXISTS relationship_score INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS industry_experience TEXT,
ADD COLUMN IF NOT EXISTS linkedin_url VARCHAR(255),
ADD COLUMN IF NOT EXISTS xing_url VARCHAR(255),
ADD COLUMN IF NOT EXISTS budget_authority BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS technical_evaluator BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS internal_champion BOOLEAN DEFAULT false;