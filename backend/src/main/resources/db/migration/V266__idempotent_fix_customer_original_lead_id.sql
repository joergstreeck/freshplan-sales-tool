-- Migration V266: Idempotent Fix für V261 (Customer.originalLeadId)
-- Sprint 2.1.6 Phase 3 - Cleanup Migration
--
-- WARUM DIESE MIGRATION?
-- V261 ist bereits im Main-Branch und wurde dort ausgeführt.
-- Lokale Test-DBs haben die Spalte bereits (aus V261).
-- Diese Migration stellt sicher, dass die Spalte existiert - egal ob V261 lief oder nicht.
--
-- IDEMPOTENT: Kann mehrfach ausgeführt werden ohne Fehler.
-- PostgreSQL 9.6+ unterstützt "IF NOT EXISTS" nativ.

-- Add original_lead_id column if it doesn't exist yet
ALTER TABLE customers
ADD COLUMN IF NOT EXISTS original_lead_id BIGINT NULL;

-- Add comment (wird überschrieben falls Spalte schon existierte - das ist OK)
COMMENT ON COLUMN customers.original_lead_id IS
  'Sprint 2.1.6 Phase 2: Tracks which Lead was converted to this Customer (Soft Reference - no FK)';
