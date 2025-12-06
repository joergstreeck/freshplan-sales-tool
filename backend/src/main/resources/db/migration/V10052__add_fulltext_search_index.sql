-- ============================================================================
-- V10052: Full-Text Search Index mit pg_trgm
-- Sprint 2.1.8 Phase 4: Advanced Search
-- ============================================================================
--
-- Aktiviert pg_trgm Extension für Fuzzy-Suche (Ähnlichkeitssuche)
-- Erstellt GIN-Indizes für schnelle Textsuche in Leads
--
-- Vorteile:
-- - Findet "Müler" wenn User "Mueller" sucht (Tippfehler-tolerant)
-- - Findet "FreshFood" wenn User "Fresh Food" sucht (Leerzeichen-tolerant)
-- - Similarity-basierte Suche statt exakter Match
-- ============================================================================

-- 1. pg_trgm Extension aktivieren (falls nicht vorhanden)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2. GIN Index für Leads Suche
-- company_name ist das wichtigste Suchfeld
CREATE INDEX IF NOT EXISTS idx_leads_company_name_trgm
ON leads USING gin (company_name gin_trgm_ops);

-- email für Duplikat-Erkennung
CREATE INDEX IF NOT EXISTS idx_leads_email_trgm
ON leads USING gin (email gin_trgm_ops);

-- contact_person für Personensuche
CREATE INDEX IF NOT EXISTS idx_leads_contact_person_trgm
ON leads USING gin (contact_person gin_trgm_ops);

-- city für geografische Suche
CREATE INDEX IF NOT EXISTS idx_leads_city_trgm
ON leads USING gin (city gin_trgm_ops);

-- 3. Kombinierter Index für die häufigste Suche (company_name + city)
-- Verwendet für: "Restaurant Berlin" oder "Catering München"
CREATE INDEX IF NOT EXISTS idx_leads_company_city_trgm
ON leads USING gin ((company_name || ' ' || COALESCE(city, '')) gin_trgm_ops);

-- 4. Normalisierte Felder für exakte Duplikat-Suche
CREATE INDEX IF NOT EXISTS idx_leads_company_name_normalized_trgm
ON leads USING gin (company_name_normalized gin_trgm_ops)
WHERE company_name_normalized IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_leads_email_normalized_trgm
ON leads USING gin (email_normalized gin_trgm_ops)
WHERE email_normalized IS NOT NULL;

-- 5. Kommentar für Dokumentation
COMMENT ON INDEX idx_leads_company_name_trgm IS 'Sprint 2.1.8: pg_trgm Fuzzy-Suche für Firmennamen';
COMMENT ON INDEX idx_leads_email_trgm IS 'Sprint 2.1.8: pg_trgm Fuzzy-Suche für E-Mail (Duplikaterkennung)';
COMMENT ON INDEX idx_leads_contact_person_trgm IS 'Sprint 2.1.8: pg_trgm Fuzzy-Suche für Ansprechpartner';
