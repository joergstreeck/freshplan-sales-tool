-- =====================================================
-- V90010: DEV-SEED – Add Xentral IDs to Seed Customers
-- =====================================================
-- Created: 2025-10-24
-- Author: Claude
-- Sprint: 2.1.7.2 - Xentral Integration Testing
-- Purpose: Alle Seed-Kunden mit Xentral-IDs ausstatten für UI-Testing
-- =====================================================

-- ====================
-- 1. UPDATE SEED CUSTOMERS WITH XENTRAL IDs
-- ====================

-- Biergarten Augustiner München GmbH → Matches Mock "Biergarten am See"
UPDATE customers
SET xentral_customer_id = 'XENT-004'
WHERE customer_number = 'KD-SEASON-004';

-- Eiscafé Venezia GmbH → Matches Mock "Eisdiele Venezia"
UPDATE customers
SET xentral_customer_id = 'XENT-005'
WHERE customer_number = 'KD-SEASON-001';

-- Remaining Customers → Unique Xentral IDs
UPDATE customers
SET xentral_customer_id = 'XENT-006'
WHERE customer_number = 'KD-SEASON-002'; -- Eisdiele Dolce Vita

UPDATE customers
SET xentral_customer_id = 'XENT-007'
WHERE customer_number = 'KD-SEASON-006'; -- Skihütte Zugspitze

UPDATE customers
SET xentral_customer_id = 'XENT-008'
WHERE customer_number = 'KD-SEASON-003'; -- Strandbar Sylt

UPDATE customers
SET xentral_customer_id = 'XENT-009'
WHERE customer_number = 'KD-TEST-001'; -- Test Hotel GmbH

UPDATE customers
SET xentral_customer_id = 'XENT-010'
WHERE customer_number = 'KD-TEST-002'; -- Test Restaurant GmbH

UPDATE customers
SET xentral_customer_id = 'XENT-011'
WHERE customer_number = 'KD-SEASON-005'; -- Weihnachtsmarkt Nürnberg

-- ====================
-- 2. VERIFY UPDATE
-- ====================
DO $$
DECLARE
  v_count INT;
BEGIN
  SELECT COUNT(*) INTO v_count FROM customers WHERE xentral_customer_id IS NOT NULL;

  RAISE NOTICE '✅ V90010 DEV-SEED Xentral IDs Added!';
  RAISE NOTICE '   - % customers now have Xentral IDs', v_count;
  RAISE NOTICE '';
  RAISE NOTICE '📋 TEST INSTRUCTIONS:';
  RAISE NOTICE '   1. Open Kundenmanagement → Customer Detail Page';
  RAISE NOTICE '   2. Open "Biergarten Augustiner" (KD-SEASON-004)';
  RAISE NOTICE '   3. → Should show "Umsatzdaten (Xentral)" widget with mock data';
  RAISE NOTICE '   4. Try other customers - all should show Xentral widgets now!';
END $$;
