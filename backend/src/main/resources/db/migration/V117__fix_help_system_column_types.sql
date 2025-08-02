-- =====================================================
-- Help System Column Type Fix
-- Migration V117: Convert JSONB columns to TEXT for Hibernate compatibility
-- Created: 2025-08-02 for TODO-66 Backend Fix
-- =====================================================

-- Convert trigger_conditions from JSONB to TEXT
ALTER TABLE help_contents 
ALTER COLUMN trigger_conditions TYPE TEXT 
USING trigger_conditions::TEXT;

-- Convert interaction_data from JSONB to TEXT  
ALTER TABLE help_contents
ALTER COLUMN interaction_data TYPE TEXT
USING interaction_data::TEXT;

-- Also fix help_usage_tracking context column if it exists
-- (It was JSONB in the original migration)
ALTER TABLE help_usage_tracking
ALTER COLUMN context TYPE TEXT
USING context::TEXT;

-- Log successful fix
INSERT INTO help_contents (
    feature, help_type, title, target_user_level,
    short_content, priority, created_by
) VALUES (
    'system',
    'TOOLTIP', 
    'Help System Column Types Fixed',
    'EXPERT',
    'V117 Migration: JSONB columns converted to TEXT for Hibernate compatibility.',
    100,
    'migration-v117'
) ON CONFLICT DO NOTHING;