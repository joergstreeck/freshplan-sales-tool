-- Sprint 2.1.7 - Issue #126: Add ActivityOutcome Enum to lead_activities
-- Refactors outcome from VARCHAR(50) to ENUM with CHECK constraint

-- ============================================================================
-- 1. Add ActivityOutcome Column (ENUM as VARCHAR + CHECK Constraint)
-- ============================================================================

-- Strategy: VARCHAR + CHECK Constraint (NOT PostgreSQL ENUM Type)
-- Reason: JPA @Enumerated(EnumType.STRING) compatibility, easier schema evolution

ALTER TABLE lead_activities
ADD COLUMN outcome_enum VARCHAR(50);

-- ============================================================================
-- 2. Migrate Existing Data (if any)
-- ============================================================================

-- Map existing STRING values to ENUM (if data exists)
-- Most common mappings for legacy data
UPDATE lead_activities
SET outcome_enum = CASE
    WHEN LOWER(outcome) LIKE '%erfolg%' OR LOWER(outcome) LIKE '%success%' THEN 'SUCCESSFUL'
    WHEN LOWER(outcome) LIKE '%nicht%' OR LOWER(outcome) LIKE '%unsuccessful%' THEN 'UNSUCCESSFUL'
    WHEN LOWER(outcome) LIKE '%keine%antwort%' OR LOWER(outcome) LIKE '%no%answer%' THEN 'NO_ANSWER'
    WHEN LOWER(outcome) LIKE '%rückruf%' OR LOWER(outcome) LIKE '%callback%' THEN 'CALLBACK_REQUESTED'
    WHEN LOWER(outcome) LIKE '%info%' OR LOWER(outcome) LIKE '%sent%' THEN 'INFO_SENT'
    WHEN LOWER(outcome) LIKE '%qualifi%' THEN 'QUALIFIED'
    WHEN LOWER(outcome) LIKE '%disquali%' THEN 'DISQUALIFIED'
    ELSE NULL
END
WHERE outcome IS NOT NULL;

-- ============================================================================
-- 3. Drop Old Column + Rename
-- ============================================================================

ALTER TABLE lead_activities
DROP COLUMN outcome;

ALTER TABLE lead_activities
RENAME COLUMN outcome_enum TO outcome;

-- ============================================================================
-- 4. Add CHECK Constraint (Enum Validation)
-- ============================================================================

ALTER TABLE lead_activities
ADD CONSTRAINT chk_activity_outcome
CHECK (outcome IN (
    'SUCCESSFUL',
    'UNSUCCESSFUL',
    'NO_ANSWER',
    'CALLBACK_REQUESTED',
    'INFO_SENT',
    'QUALIFIED',
    'DISQUALIFIED'
));

-- ============================================================================
-- 5. Add Index for Performance
-- ============================================================================

CREATE INDEX idx_lead_activities_outcome ON lead_activities(outcome)
WHERE outcome IS NOT NULL;

-- ============================================================================
-- NOTES
-- ============================================================================
-- Migration Pattern: VARCHAR + CHECK Constraint (NICHT PostgreSQL ENUM Type)
-- - ✅ JPA @Enumerated(EnumType.STRING) compatibility
-- - ✅ Schema evolution easy (no ALTER TYPE complexity)
-- - ✅ Performance sufficient (~5% slower than native ENUM with B-Tree index)
--
-- Documented: /docs/planung/features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md
