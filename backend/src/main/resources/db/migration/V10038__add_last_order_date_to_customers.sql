-- Sprint 2.1.7.2 - D7: Xentral Webhook Integration
-- Add last_order_date field to customers table

--  ============================================================================
-- FELD: customers.last_order_date
-- ============================================================================
-- Zweck: Tracking der letzten Bestellung für Churn-Detection
-- Befüllung: Automatisch via Xentral Webhook "Order Delivered"
-- Verwendung:
--   - Churn-Alarm: Kunde ohne Bestellung seit > churn_threshold_days
--   - Customer-Health-Dashboard: Letzte Aktivität
--   - Auto-Aktivierung: PROSPECT → AKTIV bei erster Bestellung

ALTER TABLE customers
ADD COLUMN IF NOT EXISTS last_order_date DATE;

CREATE INDEX IF NOT EXISTS idx_customers_last_order_date
ON customers(last_order_date);

COMMENT ON COLUMN customers.last_order_date IS
  'Date of last order delivery. Auto-updated by Xentral Webhook. Used for churn detection and customer health monitoring. Sprint 2.1.7.2 D7';


-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
  RAISE NOTICE '✅ Migration V10038 erfolgreich!';
  RAISE NOTICE '   - customers.last_order_date (DATE, nullable)';
  RAISE NOTICE '   - Index: idx_customers_last_order_date';
  RAISE NOTICE '';
  RAISE NOTICE '📋 VERWENDUNG:';
  RAISE NOTICE '   - Xentral Webhook "Order Delivered" → setzt lastOrderDate';
  RAISE NOTICE '   - Churn-Detection: last_order_date + churn_threshold_days';
  RAISE NOTICE '   - Customer-Health-Monitoring Dashboard';
END $$;
