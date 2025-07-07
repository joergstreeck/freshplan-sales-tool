-- V101: Add performance indices for customer search functionality
-- These indices optimize the dynamic customer search API (FC-001)
-- Created: 2025-07-07

-- Index for text search on company_name and trading_name
-- Using GIN index for full-text search capabilities
CREATE INDEX IF NOT EXISTS idx_customer_search_text 
    ON customers 
    USING gin(to_tsvector('english', 
        COALESCE(company_name, '') || ' ' || 
        COALESCE(trading_name, '')
    ));

-- Composite index for status and risk_score filtering
-- Optimizes queries like: WHERE status = 'AKTIV' AND risk_score > 50
CREATE INDEX IF NOT EXISTS idx_customer_status_risk 
    ON customers(status, risk_score);

-- Index for last_contact_date sorting and filtering
-- Optimizes queries with ORDER BY last_contact_date DESC
CREATE INDEX IF NOT EXISTS idx_customer_last_contact 
    ON customers(last_contact_date DESC NULLS LAST);

-- Index for customer_number searches
-- Optimizes queries with WHERE customer_number LIKE 'prefix%'
CREATE INDEX IF NOT EXISTS idx_customer_number 
    ON customers(customer_number);

-- Composite index for common filter combinations
-- Optimizes queries filtering by status and sorting by risk
CREATE INDEX IF NOT EXISTS idx_customer_status_risk_desc 
    ON customers(status, risk_score DESC);

-- Index for industry filtering
CREATE INDEX IF NOT EXISTS idx_customer_industry 
    ON customers(industry);

-- Index for expected_annual_volume range queries
CREATE INDEX IF NOT EXISTS idx_customer_annual_volume 
    ON customers(expected_annual_volume);

-- Note: We're not creating the materialized view yet as it requires
-- the orders table which may not exist in all environments.
-- This can be added in a future migration when needed.

-- Performance notes:
-- These indices will significantly improve query performance for:
-- 1. Global text search across company and trading names
-- 2. Status-based filtering (most common filter)
-- 3. Risk score filtering and sorting
-- 4. Date-based sorting for "last contact" views
-- 5. Customer number prefix searches
-- 6. Industry-based filtering
-- 7. Revenue range filtering