-- Sprint 2.1.7.2 - D5: Admin-UI für Xentral-Einstellungen
-- Migration: V10037__create_xentral_settings.sql
-- Author: FreshPlan Team
-- Date: 2025-10-23
--
-- Purpose: Create xentral_settings table for storing Xentral API configuration.
--          Supports Admin-UI configuration override (database has priority over application.properties).
--
-- Architecture:
--   - SINGLETON pattern: Only one row allowed (enforced by CHECK constraint)
--   - Database settings override application.properties defaults
--   - If no row exists, application.properties values are used as fallback
--
-- Fields:
--   - api_url: Xentral ERP base URL (e.g., https://644b6ff97320d.xentral.biz)
--   - api_token: API authentication token (encrypted/hashed in production)
--   - mock_mode: Feature flag to use MockXentralApiClient instead of real API
--   - created_at / updated_at: Audit timestamps
--
-- Security:
--   - api_token should be encrypted at rest (future enhancement)
--   - Admin-only access enforced by @RolesAllowed("ADMIN") in Resource
--
-- IDEMPOTENT: Can be run multiple times without errors (Sprint 2.1.7.4 Best Practice)

-- ============================================================================
-- CREATE TABLE (IDEMPOTENT)
-- ============================================================================

CREATE TABLE IF NOT EXISTS xentral_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Xentral API Configuration
    api_url VARCHAR(255) NOT NULL,
    api_token VARCHAR(500) NOT NULL,
    mock_mode BOOLEAN NOT NULL DEFAULT true,

    -- Audit Fields
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,

    -- Singleton Constraint: Only one configuration row allowed
    -- (Uses CHECK constraint with constant expression for singleton pattern)
    singleton_guard BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT xentral_settings_singleton_check CHECK (singleton_guard = true)
);

-- ============================================================================
-- UNIQUE INDEX (IDEMPOTENT - SINGLETON ENFORCEMENT)
-- ============================================================================

-- Enforce singleton pattern: Only one row can have singleton_guard = true
CREATE UNIQUE INDEX IF NOT EXISTS idx_xentral_settings_singleton
    ON xentral_settings (singleton_guard);

-- ============================================================================
-- COMMENT (DOCUMENTATION)
-- ============================================================================

COMMENT ON TABLE xentral_settings IS 'Sprint 2.1.7.2 - D5: Xentral API configuration (Admin-UI managed, overrides application.properties). SINGLETON pattern: only one row allowed.';
COMMENT ON COLUMN xentral_settings.api_url IS 'Xentral ERP base URL (e.g., https://644b6ff97320d.xentral.biz)';
COMMENT ON COLUMN xentral_settings.api_token IS 'Xentral API token (TODO: encrypt at rest in production)';
COMMENT ON COLUMN xentral_settings.mock_mode IS 'Feature flag: true = use MockXentralApiClient, false = use real Xentral API';
COMMENT ON COLUMN xentral_settings.singleton_guard IS 'Singleton enforcement: always true, enforced by UNIQUE INDEX';
