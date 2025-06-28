-- V2: Create user table for FreshPlan 2.0
-- Timestamp: 2025-01-06
-- Sprint 1: User Management MVP

CREATE TABLE IF NOT EXISTS app_user (
    id           UUID         DEFAULT RANDOM_UUID() PRIMARY KEY,
    username     VARCHAR(60)  UNIQUE NOT NULL,
    first_name   VARCHAR(60)  NOT NULL,
    last_name    VARCHAR(60)  NOT NULL,
    email        VARCHAR(120) UNIQUE NOT NULL,
    enabled      BOOLEAN      DEFAULT TRUE,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_app_user_username ON app_user(username);
CREATE INDEX idx_app_user_email ON app_user(email);
CREATE INDEX idx_app_user_enabled ON app_user(enabled);

-- Add comment for documentation
COMMENT ON TABLE app_user IS 'Application users managed by FreshPlan';
COMMENT ON COLUMN app_user.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN app_user.username IS 'Unique username for login';
COMMENT ON COLUMN app_user.email IS 'User email address (must be unique)';
COMMENT ON COLUMN app_user.enabled IS 'Whether the user account is active';