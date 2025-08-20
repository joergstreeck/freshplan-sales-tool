-- V10009__add_test_data_flag_to_users.sql
-- Fügt is_test_data Flag zur app_user Tabelle hinzu
-- Datum: 2025-08-20
-- Zweck: Phase 2A - UserTestDataFactory benötigt dieses Feld für Test-Isolation

-- Add is_test_data column to app_user table
ALTER TABLE app_user 
ADD COLUMN IF NOT EXISTS is_test_data BOOLEAN DEFAULT FALSE;

-- Create partial index for efficient test data queries
CREATE INDEX IF NOT EXISTS idx_app_user_test_data 
ON app_user(is_test_data) 
WHERE is_test_data = TRUE;

-- Add descriptive comment
COMMENT ON COLUMN app_user.is_test_data IS 
'Flag to identify test users that should not go to production. Used by UserTestDataFactory for test isolation.';

-- Verification: Count test users (should be 0 initially)
DO $$
DECLARE
  test_user_count INTEGER;
BEGIN
  SELECT COUNT(*) INTO test_user_count
  FROM app_user
  WHERE is_test_data = TRUE;
  
  RAISE NOTICE 'Test users after migration: %', test_user_count;
END $$;