-- Cleanup Script: Entfernt alte Test-Daten
-- Datum: 2025-07-07
-- Zweck: Nur kontrollierte [TEST] Kunden behalten

-- Schritt 1: Timeline Events der alten Kunden löschen
DELETE FROM customer_timeline_events 
WHERE customer_id IN (
    SELECT id FROM customers 
    WHERE (is_test_data IS NULL OR is_test_data = false)
      AND company_name NOT LIKE '[TEST]%'
);

-- Schritt 2: Alte Kunden löschen
DELETE FROM customers 
WHERE (is_test_data IS NULL OR is_test_data = false)
  AND company_name NOT LIKE '[TEST]%';

-- Schritt 3: Verifikation
SELECT COUNT(*) as remaining_customers FROM customers;
SELECT customer_number, company_name, status FROM customers ORDER BY customer_number;