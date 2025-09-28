-- V254__events_add_published_column.sql
-- Fix for EventPublisherTest failures
-- Adds missing 'published' column that tests expect
-- Guards against missing table (events table may not exist in all environments)

DO $$
BEGIN
  -- Only add column if events table exists
  IF EXISTS (SELECT 1 FROM information_schema.tables
             WHERE table_schema = 'public'
             AND table_name = 'events') THEN

    ALTER TABLE events
      ADD COLUMN IF NOT EXISTS published BOOLEAN NOT NULL DEFAULT TRUE;

    COMMENT ON COLUMN events.published IS
      'Event published status - true when event has been successfully published';

    RAISE NOTICE 'Added published column to events table';
  ELSE
    RAISE NOTICE 'Skipped: events table does not exist';
  END IF;
END $$;