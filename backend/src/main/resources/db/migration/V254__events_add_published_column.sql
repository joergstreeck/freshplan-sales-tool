-- V254__events_add_published_column.sql
-- Fix for EventPublisherTest failures
-- Adds missing 'published' column that tests expect

ALTER TABLE events
  ADD COLUMN IF NOT EXISTS published BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN events.published IS
  'Event published status - true when event has been successfully published';