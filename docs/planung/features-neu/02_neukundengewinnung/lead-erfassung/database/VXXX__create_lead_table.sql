-- VXXX: Create Lead Management Tables (NUMMER AKTUALISIEREN!)
-- Foundation Standards: Performance indices + ABAC territory scoping
--
-- ⚠️ WICHTIG: Migration-Nummer vor Implementation prüfen!
-- Kommando: ./scripts/get-next-migration.sh
-- Diese Datei dann umbenennen zu: VXXX__create_lead_table.sql

CREATE TABLE lead (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    territory VARCHAR(10) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT chk_lead_status CHECK (status IN ('NEW', 'ACTIVE', 'WON', 'LOST')),
    CONSTRAINT chk_lead_territory CHECK (territory ~ '^[A-Z]{2,10}$')
);

-- Performance indices (Foundation Standards)
CREATE INDEX idx_lead_territory_status ON lead(territory, status)
WHERE status IN ('NEW', 'ACTIVE');

CREATE INDEX idx_lead_created_at ON lead(created_at DESC);

-- ABAC territory scoping index
CREATE INDEX idx_lead_territory ON lead(territory);

-- Updated timestamp trigger
CREATE OR REPLACE FUNCTION update_lead_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_lead_updated_at
    BEFORE UPDATE ON lead
    FOR EACH ROW
    EXECUTE FUNCTION update_lead_updated_at();

-- Sample data for development/testing
INSERT INTO lead (name, territory, status) VALUES
('Restaurant Alpha', 'BER', 'NEW'),
('Hotel Beta', 'HH', 'ACTIVE'),
('Kantine Gamma', 'NRW', 'NEW');