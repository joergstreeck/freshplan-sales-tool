-- Sprint 2.1.7.2: Xentral Integration - Required Fields
-- 3 neue Felder für Xentral-API-Integration und RLS Security

-- ============================================================================
-- FELD 1: users.xentral_sales_rep_id
-- ============================================================================
-- Zweck: Verknüpfung FreshPlan-User ↔ Xentral-Mitarbeiter
-- Befüllung: Automatisch via Nightly Sales-Rep Sync Job (Email-Matching)
-- Verwendung:
--   - RLS Security: Sales sieht nur eigene Kunden
--   - Customer-Filter: GET /api/xentral/customers?salesRepId={id}
--   - Dashboard: Revenue-Daten des Verkäufers

ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS xentral_sales_rep_id VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_app_user_xentral_sales_rep_id
ON app_user(xentral_sales_rep_id);

COMMENT ON COLUMN app_user.xentral_sales_rep_id IS
  'Xentral Employee ID - Auto-synced via Nightly Job (Email-Matching). Maps FreshPlan User → Xentral Sales Rep.';


-- ============================================================================
-- FELD 2: customers.xentral_customer_id
-- ============================================================================
-- Zweck: Verknüpfung FreshPlan-Kunde ↔ Xentral-Kunde
-- Befüllung: Manuell via ConvertToCustomerDialog (User wählt aus Xentral-Dropdown)
-- Verwendung:
--   - Revenue-Daten holen: GET /api/xentral/customers/{xentralCustomerId}
--   - RLS Security: Filter nach Xentral-Kunden-IDs
--   - Webhook: Auto-Aktivierung bei Order Delivered

ALTER TABLE customers
ADD COLUMN IF NOT EXISTS xentral_customer_id VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_customers_xentral_customer_id
ON customers(xentral_customer_id);

COMMENT ON COLUMN customers.xentral_customer_id IS
  'Xentral Customer ID - Set manually in ConvertToCustomerDialog or via API sync. Maps FreshPlan Customer → Xentral Customer.';


-- ============================================================================
-- FELD 3: users.manager_id
-- ============================================================================
-- Zweck: Team-Hierarchie (Vorgesetzter-Zuordnung)
-- Befüllung: Manuell via Admin-UI Benutzerverwaltung (/admin/users)
-- Verwendung:
--   - RLS Security: Manager sieht alle Kunden seines Teams
--   - Dashboard: Team-Revenue-Übersicht

ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS manager_id UUID REFERENCES app_user(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_app_user_manager_id
ON app_user(manager_id);

COMMENT ON COLUMN app_user.manager_id IS
  'Vorgesetzter (für Team-Hierarchie) - Manager sehen alle Kunden ihres Teams. Referenz auf app_user.id.';

-- ============================================================================
-- FELD 4: app_user.can_see_unassigned_customers (Flexibilität für Manager)
-- ============================================================================
-- Zweck: Manager kann zusätzlich "Kunden ohne Verkäufer" sehen
-- Befüllung: Manuell via Admin-UI Benutzerverwaltung (Checkbox)
-- Verwendung:
--   - Manager-Kunden = Team-Kunden + (optional) Unassigned-Kunden
--   - Unassigned = Kunden ohne xentralCustomerId ODER ohne zugeordnetem Sales Rep in Xentral
-- Erweiterbarkeit:
--   - Später migrierbar zu Gruppen-System (customer_groups, user_group_assignments)
--   - Jetzt: Einfacher Boolean für "Unassigned"-Gruppe

ALTER TABLE app_user
ADD COLUMN IF NOT EXISTS can_see_unassigned_customers BOOLEAN DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_app_user_can_see_unassigned
ON app_user(can_see_unassigned_customers)
WHERE can_see_unassigned_customers = true;

COMMENT ON COLUMN app_user.can_see_unassigned_customers IS
  'Manager Privilege: Can see customers without assigned sales rep. Default: false. Future: Migrate to customer_groups system if more flexibility needed.';


-- ============================================================================
-- FELD 5-7: customers.revenue30Days / revenue90Days / revenue365Days
-- ============================================================================
-- Zweck: Umsatz-Tracking für Customer-Scoring + Revenue-Analysen
-- Befüllung: Später via Xentral API (GET /api/xentral/customers/{id})
-- Verwendung:
--   - CustomerSchemaResource: Server-Driven UI Fields (Card "Geschäftspotenzial")
--   - Scoring: Kunden-Ranking nach Umsatz
--   - Dashboard: Revenue-Entwicklung visualisieren (30/90/365 Tage Trend)

ALTER TABLE customers
ADD COLUMN IF NOT EXISTS revenue_30_days NUMERIC(12, 2);

ALTER TABLE customers
ADD COLUMN IF NOT EXISTS revenue_90_days NUMERIC(12, 2);

ALTER TABLE customers
ADD COLUMN IF NOT EXISTS revenue_365_days NUMERIC(12, 2);

CREATE INDEX IF NOT EXISTS idx_customers_revenue_365_days
ON customers(revenue_365_days DESC NULLS LAST);

COMMENT ON COLUMN customers.revenue_30_days IS
  'Umsatz der letzten 30 Tage (aus Xentral) - für Scoring + Dashboards';

COMMENT ON COLUMN customers.revenue_90_days IS
  'Umsatz der letzten 90 Tage (aus Xentral) - für Trend-Analysen';

COMMENT ON COLUMN customers.revenue_365_days IS
  'Umsatz der letzten 365 Tage (aus Xentral) - für Jahres-Scoring';


-- ============================================================================
-- VALIDATION & CONSTRAINTS
-- ============================================================================

-- Verhindere Endlos-Schleifen: User kann nicht sein eigener Manager sein
-- Note: Flyway ensures idempotency (script runs only once), so no IF EXISTS check needed
ALTER TABLE app_user
ADD CONSTRAINT chk_app_user_manager_not_self
CHECK (manager_id IS NULL OR manager_id != id);

COMMENT ON CONSTRAINT chk_app_user_manager_not_self ON app_user IS
  'Prevents circular hierarchy: User cannot be their own manager';

-- Verhindere negative Revenue-Werte
ALTER TABLE customers
ADD CONSTRAINT chk_customers_revenue_non_negative
CHECK (
  (revenue_30_days IS NULL OR revenue_30_days >= 0) AND
  (revenue_90_days IS NULL OR revenue_90_days >= 0) AND
  (revenue_365_days IS NULL OR revenue_365_days >= 0)
);

COMMENT ON CONSTRAINT chk_customers_revenue_non_negative ON customers IS
  'Revenue values must be >= 0 or NULL';


-- ============================================================================
-- SUCCESS MESSAGE
-- ============================================================================

DO $$
BEGIN
  RAISE NOTICE '✅ Migration V10035 erfolgreich!';
  RAISE NOTICE '   - app_user.xentral_sales_rep_id (Xentral Employee ID)';
  RAISE NOTICE '   - customers.xentral_customer_id (Xentral Customer ID)';
  RAISE NOTICE '   - app_user.manager_id (Team-Hierarchie)';
  RAISE NOTICE '   - app_user.can_see_unassigned_customers (Manager Unassigned-Privilege)';
  RAISE NOTICE '   - customers.revenue_30_days, revenue_90_days, revenue_365_days (Umsatz-Tracking)';
  RAISE NOTICE '';
  RAISE NOTICE '📋 NEXT STEPS:';
  RAISE NOTICE '   1. User-Entity: @Column xentralSalesRepId + managerId + canSeeUnassignedCustomers';
  RAISE NOTICE '   2. Customer-Entity: @Column xentralCustomerId + revenue30Days + revenue90Days + revenue365Days';
  RAISE NOTICE '   3. Nightly Sync Job: SalesRepSyncJob implementieren';
  RAISE NOTICE '   4. RLS Security: getUnassignedCustomers() Methode';
  RAISE NOTICE '   5. ConvertToCustomerDialog: Xentral-Dropdown + xentralCustomerId';
  RAISE NOTICE '   6. Admin-UI Benutzerverwaltung:';
  RAISE NOTICE '      - Spalte "Vorgesetzter" (Dropdown)';
  RAISE NOTICE '      - Checkbox "Darf Kunden ohne Verkäufer sehen"';
END $$;
