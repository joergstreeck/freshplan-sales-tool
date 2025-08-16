-- V221: Add performance indexes for customer queries
-- Created: 2025-08-16
-- Purpose: Optimize customer list queries that filter by is_deleted and sort by company_name
-- Impact: Expected 50-70% performance improvement for list operations

-- Create composite index for the most common query pattern:
-- WHERE is_deleted = false ORDER BY company_name
-- Using CONCURRENTLY to avoid blocking other operations
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_active_company_name
ON customers(is_deleted, company_name)
WHERE is_deleted = false;

-- Additional index for company_name alone (for searches and sorts)
-- This helps with LIKE queries on company_name
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_company_name
ON customers(company_name);

-- Index for updated_at sorting (common in admin views)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_updated_at
ON customers(updated_at DESC)
WHERE is_deleted = false;

-- Index for risk management queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_risk_score
ON customers(risk_score DESC)
WHERE is_deleted = false AND risk_score IS NOT NULL;

-- Index for follow-up date queries (for sales dashboard)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_next_follow_up
ON customers(next_follow_up_date)
WHERE is_deleted = false AND next_follow_up_date IS NOT NULL;

-- Note: ANALYZE must be run separately outside of transaction
-- Run manually after migration: ANALYZE customers;
-- Expected improvements:
--   - List queries: 50-70% faster
--   - Search queries: 40-60% faster
--   - Dashboard queries: 30-50% faster