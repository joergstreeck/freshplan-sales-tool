-- V254__events_add_published_column.sql
-- Fix for EventPublisherTest failures
-- Adds missing 'published' column that tests expect
-- Now checks for both events and domain_events tables

DO $$
BEGIN
  -- Check if 'events' table exists
  IF EXISTS (SELECT 1 FROM information_schema.tables
             WHERE table_schema = 'public'
             AND table_name = 'events') THEN

    ALTER TABLE events
      ADD COLUMN IF NOT EXISTS published BOOLEAN NOT NULL DEFAULT TRUE;

    COMMENT ON COLUMN events.published IS
      'Event published status - true when event has been successfully published';

    RAISE NOTICE 'Added published column to events table';

  -- Check if 'domain_events' table exists (the actual table name)
  ELSIF EXISTS (SELECT 1 FROM information_schema.tables
                WHERE table_schema = 'public'
                AND table_name = 'domain_events') THEN

    ALTER TABLE domain_events
      ADD COLUMN IF NOT EXISTS published BOOLEAN NOT NULL DEFAULT TRUE;

    COMMENT ON COLUMN domain_events.published IS
      'Event published status - true when event has been successfully published';

    RAISE NOTICE 'Added published column to domain_events table';
  ELSE
    RAISE NOTICE 'Skipped: neither events nor domain_events table exists';
  END IF;
END $$;