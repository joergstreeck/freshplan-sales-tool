-- V215: Add search indexes for universal search functionality
-- FC-005 PR4: Enhanced search performance
-- Date: 2025-08-10

-- ========== CUSTOMER SEARCH INDEXES ==========

-- Index for company name search (case-insensitive)
CREATE INDEX IF NOT EXISTS idx_customers_company_name_lower 
ON customers(lower(company_name));

-- Index for customer number search
CREATE INDEX IF NOT EXISTS idx_customers_customer_number 
ON customers(customer_number);

-- Index for trading name search (case-insensitive)
CREATE INDEX IF NOT EXISTS idx_customers_trading_name_lower 
ON customers(lower(trading_name));

-- Composite index for full-text search
CREATE INDEX IF NOT EXISTS idx_customers_search_composite 
ON customers(lower(company_name), customer_number, is_deleted);

-- Index for soft delete filtering
CREATE INDEX IF NOT EXISTS idx_customers_is_deleted 
ON customers(is_deleted);

-- Index for status filtering
CREATE INDEX IF NOT EXISTS idx_customers_status 
ON customers(status) WHERE is_deleted = false;

-- ========== CONTACT SEARCH INDEXES ==========

-- Index for contact name search (concatenated)
CREATE INDEX IF NOT EXISTS idx_contacts_name_search 
ON customer_contacts(lower(first_name || ' ' || last_name));

-- Index for email search (case-insensitive)
CREATE INDEX IF NOT EXISTS idx_contacts_email_lower 
ON customer_contacts(lower(email));

-- Index for phone search
CREATE INDEX IF NOT EXISTS idx_contacts_phone 
ON customer_contacts(phone);

-- Index for mobile phone search
CREATE INDEX IF NOT EXISTS idx_contacts_mobile 
ON customer_contacts(mobile);

-- Index for active contacts
CREATE INDEX IF NOT EXISTS idx_contacts_is_active 
ON customer_contacts(is_active);

-- Composite index for contact search
CREATE INDEX IF NOT EXISTS idx_contacts_search_composite 
ON customer_contacts(is_active, lower(email), lower(first_name || ' ' || last_name));

-- Index for primary contact lookup
CREATE INDEX IF NOT EXISTS idx_contacts_primary 
ON customer_contacts(customer_id, is_primary) WHERE is_active = true;

-- ========== PERFORMANCE INDEXES ==========

-- Index for customer last contact date (for sorting and filtering)
CREATE INDEX IF NOT EXISTS idx_customers_last_contact_date 
ON customers(last_contact_date DESC) WHERE is_deleted = false;

-- Index for customer created date (for "new customers" filter)
CREATE INDEX IF NOT EXISTS idx_customers_created_at 
ON customers(created_at DESC) WHERE is_deleted = false;

-- Index for risk score (for risk filtering)
CREATE INDEX IF NOT EXISTS idx_customers_risk_score 
ON customers(risk_score DESC) WHERE is_deleted = false;

-- ========== ANALYZE TABLES FOR QUERY OPTIMIZATION ==========
ANALYZE customers;
ANALYZE customer_contacts;