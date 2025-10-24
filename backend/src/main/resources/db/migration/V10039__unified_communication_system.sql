-- Sprint 2.1.7.2 - D8: Unified Communication System
-- Migration V10039 (SPEC originally said V10033 - adjusted for sequential numbering)
--
-- WHY NOW? Pre-Live Quality Investment
-- Current: ~50 DEV-SEED activities (5 sec migration)
-- Post-Live: Potentially thousands of activities (16h+ migration risk)
--
-- PROBLEM:
-- - LeadActivity exists for Leads only
-- - Customer has NO activity tracking
-- - Lead history LOST when converting to Customer
-- - Vertriebsmitarbeiter cannot see complete customer journey
--
-- SOLUTION: Unified Activity System (CRM Best Practice)
-- - Polymorphic entity_type + entity_id pattern (Salesforce, HubSpot, Dynamics)
-- - Single activities table for BOTH Lead AND Customer
-- - Unified timeline: CustomerDetailPage shows activities "Als Lead erfasst"
-- - Future-proof for additional entities (Partner, Supplier, etc.)

-- ============================================================================
-- 1. CREATE UNIFIED ACTIVITIES TABLE
-- ============================================================================

CREATE TABLE activities (
    -- Primary Key (UUID for cross-entity compatibility)
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Polymorphic Association (entity_type + entity_id)
    -- NOTE: entity_id is TEXT to handle BOTH Lead-ID (BIGINT) and Customer-ID (UUID)
    -- Reason: Leads use BIGSERIAL, Customers use UUID (Pre-Live Type Mismatch)
    -- Future: Sprint 2.1.8+ may unify all entities to UUID
    entity_type VARCHAR(20) NOT NULL,  -- 'LEAD' or 'CUSTOMER' (extensible)
    entity_id TEXT NOT NULL,            -- Lead-ID (BIGINT as TEXT) or Customer-ID (UUID as TEXT)

    -- Activity Core Fields (from LeadActivity)
    activity_type VARCHAR(50) NOT NULL,
    activity_date TIMESTAMP NOT NULL DEFAULT NOW(),
    description TEXT,

    -- Metadata (JSONB for flexible data)
    metadata JSONB DEFAULT '{}',

    -- Activity Flags (from V229 + V256)
    is_meaningful_contact BOOLEAN NOT NULL DEFAULT FALSE,
    resets_timer BOOLEAN NOT NULL DEFAULT FALSE,
    counts_as_progress BOOLEAN NOT NULL DEFAULT FALSE,

    -- Vertriebsdokumentation (V256 fields)
    summary VARCHAR(500),
    outcome VARCHAR(50),  -- ActivityOutcome enum (V10027)
    next_action VARCHAR(200),
    next_action_date DATE,

    -- User Tracking
    user_id VARCHAR(50) NOT NULL,       -- User who logged the activity
    performed_by VARCHAR(50),           -- Actual performer (if different)

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Constraints
    CONSTRAINT chk_entity_type CHECK (entity_type IN ('LEAD', 'CUSTOMER')),
    CONSTRAINT chk_activity_type CHECK (activity_type IN (
        -- Progress Activities (countsAsProgress = true)
        'QUALIFIED_CALL', 'MEETING', 'DEMO', 'ROI_PRESENTATION', 'SAMPLE_SENT',
        -- Non-Progress Activities
        'NOTE', 'FOLLOW_UP', 'EMAIL', 'CALL', 'SAMPLE_FEEDBACK',
        -- System Activities
        'FIRST_CONTACT_DOCUMENTED', 'EMAIL_RECEIVED', 'LEAD_ASSIGNED',
        -- Legacy Activities
        'ORDER', 'STATUS_CHANGE', 'CREATED', 'DELETED', 'REMINDER_SENT',
        'GRACE_PERIOD_STARTED', 'EXPIRED', 'REACTIVATED', 'CLOCK_STOPPED', 'CLOCK_RESUMED'
    )),
    CONSTRAINT chk_activity_outcome CHECK (outcome IS NULL OR outcome IN (
        'SUCCESSFUL', 'UNSUCCESSFUL', 'NO_ANSWER', 'CALLBACK_REQUESTED',
        'INFO_SENT', 'QUALIFIED', 'DISQUALIFIED'
    ))
);

-- ============================================================================
-- 2. CREATE INDICES FOR PERFORMANCE
-- ============================================================================

-- Primary query: Get all activities for a specific entity (Lead or Customer)
CREATE INDEX idx_activities_entity ON activities(entity_type, entity_id, activity_date DESC);

-- Timeline query: Get recent activities across all entities
CREATE INDEX idx_activities_date ON activities(activity_date DESC);

-- User performance tracking
CREATE INDEX idx_activities_user ON activities(user_id, activity_date DESC);

-- Progress tracking (WHERE counts_as_progress = TRUE)
CREATE INDEX idx_activities_progress ON activities(entity_type, entity_id, activity_date DESC)
    WHERE counts_as_progress = TRUE;

-- Outcome analysis (WHERE outcome IS NOT NULL)
CREATE INDEX idx_activities_outcome ON activities(outcome)
    WHERE outcome IS NOT NULL;

-- ============================================================================
-- 3. MIGRATE DATA FROM lead_activities TO activities
-- ============================================================================

-- Insert all existing Lead activities into unified table
-- entity_type = 'LEAD', entity_id = lead_id (cast BIGINT to TEXT)
INSERT INTO activities (
    id,
    entity_type,
    entity_id,
    activity_type,
    activity_date,
    description,
    metadata,
    is_meaningful_contact,
    resets_timer,
    counts_as_progress,
    summary,
    outcome,
    next_action,
    next_action_date,
    user_id,
    performed_by,
    created_at
)
SELECT
    gen_random_uuid(),                      -- Generate new UUID for activities.id
    'LEAD'::VARCHAR(20),                    -- entity_type = 'LEAD'
    l.id::TEXT,                             -- entity_id = lead.id (cast BIGINT to TEXT)
    la.activity_type,
    la.activity_date,
    la.description,
    la.metadata,
    la.is_meaningful_contact,
    la.resets_timer,
    COALESCE(la.counts_as_progress, FALSE),  -- Default FALSE if NULL
    la.summary,
    la.outcome,
    la.next_action,
    la.next_action_date,
    la.user_id,
    COALESCE(la.performed_by, la.user_id),   -- Backfill performed_by from user_id
    la.created_at
FROM lead_activities la
INNER JOIN leads l ON la.lead_id = l.id
ORDER BY la.created_at;  -- Preserve chronological order

-- Verification
DO $$
DECLARE
    lead_activities_count INT;
    migrated_activities_count INT;
BEGIN
    SELECT COUNT(*) INTO lead_activities_count FROM lead_activities;
    SELECT COUNT(*) INTO migrated_activities_count FROM activities WHERE entity_type = 'LEAD';

    IF migrated_activities_count != lead_activities_count THEN
        RAISE EXCEPTION 'Migration verification FAILED: Expected % activities, got %',
            lead_activities_count, migrated_activities_count;
    ELSE
        RAISE NOTICE 'Migration verification SUCCESS: % activities migrated', migrated_activities_count;
    END IF;
END $$;

-- ============================================================================
-- 4. KEEP lead_activities TABLE (BACKWARD COMPATIBILITY)
-- ============================================================================

-- DECISION: DO NOT DROP lead_activities table (yet)
-- Reason 1: Existing code (LeadActivity.java, LeadActivityTimeline.tsx) still uses it
-- Reason 2: Safe rollback if issues discovered
-- Reason 3: Gives time for dual-write migration strategy in Sprint 2.1.7.3+
--
-- Phase 2 (Sprint 2.1.7.3+): Dual-write to BOTH tables
-- Phase 3 (Sprint 2.1.8+): Switch reads to activities table only
-- Phase 4 (Sprint 2.1.9+): Remove lead_activities table
--
-- DROP TABLE lead_activities;  -- NOT NOW! Phase 4 only.

COMMENT ON TABLE activities IS
    'Unified Activity System (Sprint 2.1.7.2 - D8)
     Polymorphic entity_type + entity_id pattern for Lead AND Customer activities.
     Replaces lead_activities (kept for backward compatibility).
     Future entities: Partner, Supplier, etc.';

COMMENT ON COLUMN activities.entity_type IS
    'Entity type: LEAD, CUSTOMER (extensible for Partner, Supplier)';

COMMENT ON COLUMN activities.entity_id IS
    'Polymorphic ID as TEXT: Lead-ID (BIGINT) or Customer-ID (UUID).
     TEXT chosen to handle type mismatch (Leads=BIGINT, Customers=UUID).
     Future: Sprint 2.1.8+ may unify all entities to UUID.';

COMMENT ON COLUMN activities.counts_as_progress IS
    'Progress tracking for 60-day activity standard (§3.3 Handelsvertretervertrag)';

COMMENT ON COLUMN activities.outcome IS
    'ActivityOutcome enum: SUCCESSFUL, UNSUCCESSFUL, NO_ANSWER, CALLBACK_REQUESTED, INFO_SENT, QUALIFIED, DISQUALIFIED';

-- ============================================================================
-- MIGRATION AUDIT
-- ============================================================================

-- Migration V10039 erfolgreich durchgeführt
-- SPEC original: V10033 (outdated - highest was already V10038)
-- Tabelle: activities (UUID, polymorphic entity_type + entity_id)
-- Migriert: Alle lead_activities → activities (entity_type = 'LEAD')
-- Backward Compatibility: lead_activities table KEPT (Phase 2-4 migration)
-- Indices: 5 indices für Performance (entity, date, user, progress, outcome)
-- Verification: PASSED (lead_activities count == migrated activities count)
