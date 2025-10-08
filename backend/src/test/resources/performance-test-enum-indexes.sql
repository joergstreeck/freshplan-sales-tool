-- Performance Test: B-Tree Index Usage für Enum-Felder
-- Sprint 2.1.6 Phase 5: Validierung VARCHAR + CHECK + B-Tree Pattern

-- =====================================================
-- 1. Index-Existenz prüfen
-- =====================================================

SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'leads'
  AND indexname IN ('idx_leads_source', 'idx_leads_business_type', 'idx_leads_kitchen_size')
ORDER BY indexname;

-- Erwartung: 3 B-Tree Indizes vorhanden

-- =====================================================
-- 2. Query Performance mit Index
-- =====================================================

-- Test 1: LeadSource Equality (sollte Index nutzen)
EXPLAIN ANALYZE
SELECT id, company_name, source, business_type, kitchen_size, lead_score
FROM leads
WHERE source = 'MESSE'
LIMIT 100;

-- Erwartung: "Index Scan using idx_leads_source" (NICHT Seq Scan!)

-- Test 2: BusinessType Equality (sollte Index nutzen)
EXPLAIN ANALYZE
SELECT id, company_name, source, business_type, kitchen_size, lead_score
FROM leads
WHERE business_type = 'RESTAURANT'
LIMIT 100;

-- Erwartung: "Index Scan using idx_leads_business_type"

-- Test 3: KitchenSize Equality (sollte Index nutzen)
EXPLAIN ANALYZE
SELECT id, company_name, source, business_type, kitchen_size, lead_score
FROM leads
WHERE kitchen_size = 'SEHR_GROSS'
LIMIT 100;

-- Erwartung: "Index Scan using idx_leads_kitchen_size"

-- =====================================================
-- 3. Multi-Column Query Performance
-- =====================================================

EXPLAIN ANALYZE
SELECT id, company_name, source, business_type, kitchen_size, lead_score
FROM leads
WHERE source = 'MESSE'
  AND business_type = 'RESTAURANT'
  AND kitchen_size = 'GROSS'
LIMIT 100;

-- Erwartung: Bitmap Index Scan (kombiniert idx_leads_source + idx_leads_business_type)

-- =====================================================
-- 4. CHECK Constraint Validierung
-- =====================================================

SELECT
    con.conname AS constraint_name,
    con.consrc AS constraint_definition
FROM pg_constraint con
JOIN pg_class rel ON rel.oid = con.conrelid
WHERE rel.relname = 'leads'
  AND con.conname IN ('chk_lead_source', 'chk_lead_business_type', 'chk_lead_kitchen_size')
ORDER BY con.conname;

-- Erwartung: 3 CHECK Constraints mit IN (...) Definition

-- =====================================================
-- 5. Performance-Vergleich: Index vs. LIKE
-- =====================================================

-- Baseline: String-LIKE (LANGSAM - kein Index)
EXPLAIN ANALYZE
SELECT id, company_name, source
FROM leads
WHERE source LIKE 'MESSE%'
LIMIT 100;

-- Erwartung: Seq Scan (kein Index möglich bei LIKE)

-- Optimized: Equality + Index (SCHNELL)
EXPLAIN ANALYZE
SELECT id, company_name, source
FROM leads
WHERE source = 'MESSE'
LIMIT 100;

-- Erwartung: Index Scan (9x schneller als LIKE)

-- =====================================================
-- PERFORMANCE-ZIELE (testing_guide.md)
-- =====================================================

-- ✅ Index Scan statt Seq Scan (für Equality-Queries)
-- ✅ Execution Time <10ms bei 1000 Leads
-- ✅ ~9x schneller als String-LIKE Pattern
-- ✅ Nur ~5% langsamer als PostgreSQL ENUM Type (akzeptabel)
