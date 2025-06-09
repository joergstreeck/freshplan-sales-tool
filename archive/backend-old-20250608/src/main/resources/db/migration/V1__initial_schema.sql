-- V1: Initial Schema for FreshPlan 2.0
-- Timestamp: 2025-01-05

CREATE TABLE IF NOT EXISTS system_info (
    id SERIAL PRIMARY KEY,
    key VARCHAR(100) NOT NULL UNIQUE,
    value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Initial system info
INSERT INTO system_info (key, value) VALUES 
    ('version', '1.0.0'),
    ('migration_date', CURRENT_TIMESTAMP::TEXT),
    ('environment', 'development');

-- Create index for faster lookups
CREATE INDEX idx_system_info_key ON system_info(key);