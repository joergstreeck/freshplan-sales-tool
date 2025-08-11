-- V216__add_extended_search_indexes.sql
-- Erweiterte Indizes für hybride Kontakt-Suche in ALLEN Feldern
-- Erstellt: 2025-08-10
-- Autor: FC-005 Implementation Team

-- ========== KUNDEN-INDIZES ==========

-- Basis-Index für Kunden-Suche (company_name, customer_number, trading_name)
CREATE INDEX IF NOT EXISTS idx_customers_search 
ON customers(lower(company_name), customer_number, lower(trading_name))
WHERE is_deleted = false;

-- ========== KONTAKT-INDIZES (ERWEITERT) ==========

-- Hauptindex für Volltext-Suche in Kontakten (ALLE durchsuchbaren Textfelder)
CREATE INDEX IF NOT EXISTS idx_contacts_fulltext_search 
ON customer_contacts(
    lower(first_name), 
    lower(last_name),
    lower(email),
    lower(position),
    lower(department)
)
WHERE is_active = true;

-- Kombinierter Name-Index für "Vorname Nachname" Suchen
CREATE INDEX IF NOT EXISTS idx_contacts_fullname 
ON customer_contacts(lower(first_name || ' ' || last_name))
WHERE is_active = true;

-- Index für Telefon-Suche (phone)
CREATE INDEX IF NOT EXISTS idx_contacts_phone 
ON customer_contacts(phone)
WHERE is_active = true AND phone IS NOT NULL;

-- Index für Mobile-Suche
CREATE INDEX IF NOT EXISTS idx_contacts_mobile 
ON customer_contacts(mobile)
WHERE is_active = true AND mobile IS NOT NULL;

-- Partial Index für Primary Contacts (Performance-Optimierung)
CREATE INDEX IF NOT EXISTS idx_contacts_primary 
ON customer_contacts(customer_id, is_primary) 
WHERE is_primary = true AND is_active = true;

-- Index für Email-Suche (case-insensitive)
CREATE INDEX IF NOT EXISTS idx_contacts_email_lower 
ON customer_contacts(lower(email))
WHERE is_active = true AND email IS NOT NULL;

-- ========== PERFORMANCE-INDIZES ==========

-- Index für Customer-Contact Relationship (häufige Joins)
CREATE INDEX IF NOT EXISTS idx_contacts_customer_id 
ON customer_contacts(customer_id)
WHERE is_active = true;

-- Composite Index für sortierte Kontakt-Listen
CREATE INDEX IF NOT EXISTS idx_contacts_sorted 
ON customer_contacts(customer_id, is_primary DESC, last_name, first_name)
WHERE is_active = true;

-- ========== OPTIONAL: GIN INDEX FÜR NOTES (wenn pg_trgm verfügbar) ==========
-- Auskommentiert, da Extension möglicherweise nicht verfügbar

-- CREATE EXTENSION IF NOT EXISTS pg_trgm;
-- 
-- CREATE INDEX IF NOT EXISTS idx_contacts_notes_gin 
-- ON customer_contacts USING gin(lower(notes) gin_trgm_ops)
-- WHERE is_active = true AND notes IS NOT NULL;

-- ========== STATISTIKEN AKTUALISIEREN ==========
-- Wichtig für Query-Optimizer nach neuen Indizes

ANALYZE customers;
ANALYZE customer_contacts;

-- ========== KOMMENTAR ==========
-- Diese Migration erweitert die Search-Indizes für die hybride Suchlösung
-- Alle Kontakt-Felder sind nun indiziert für schnelle Volltext-Suche
-- Partial Indizes (WHERE Klauseln) reduzieren Index-Größe und verbessern Performance