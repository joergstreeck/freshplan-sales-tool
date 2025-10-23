-- ========================================================================================================
-- Migration: V10036 - Add churn_threshold_days to customers table
-- Sprint: 2.1.7.2 - D4 (Churn-Alarm Configuration)
-- Author: FreshPlan Team
-- Date: 2025-10-23
-- ========================================================================================================
--
-- CONTEXT:
-- Sprint 2.1.7.2 D4 - Churn-Alarm Konfiguration pro Kunde
--
-- CHANGES:
-- - Add customers.churn_threshold_days (INTEGER, DEFAULT 90)
-- - CHECK constraint: value between 14 and 365 days
-- - Non-nullable with default (business requirement: always monitor)
--
-- BUSINESS RULE:
-- - Default 90 days (3 months without order = churn risk)
-- - Configurable per customer (14-365 days range)
-- - Used by ChurnDetectionService (Sprint 2.1.7.4)
-- - Used by ChurnRiskAlert Component (Frontend)
--
-- IDEMPOTENCY:
-- - Uses IF NOT EXISTS for idempotent schema changes
-- ========================================================================================================

-- Add churn_threshold_days column to customers table
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS churn_threshold_days INTEGER NOT NULL DEFAULT 90;

-- Add CHECK constraint for valid range (14-365 days)
-- Use DO block for idempotent constraint creation
DO $$
BEGIN
    -- Check if constraint does NOT exist
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_customer_churn_threshold_days_range'
          AND conrelid = 'customers'::regclass
    ) THEN
        -- Add constraint
        ALTER TABLE customers
        ADD CONSTRAINT chk_customer_churn_threshold_days_range
        CHECK (churn_threshold_days BETWEEN 14 AND 365);
    END IF;
END $$;

-- Add comment for documentation
COMMENT ON COLUMN customers.churn_threshold_days IS 'Churn-Alarm Schwelle in Tagen (14-365). Default: 90 Tage. Kundenspezifisch konfigurierbar.';
