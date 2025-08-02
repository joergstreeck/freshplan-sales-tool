-- V27: Create Cost Management Tables
-- Implements comprehensive cost tracking and budget management

-- Create cost_transactions table
CREATE TABLE IF NOT EXISTS cost_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service VARCHAR(50) NOT NULL,
    feature VARCHAR(100) NOT NULL,
    model VARCHAR(50) NOT NULL,
    estimated_cost DECIMAL(10,4) NOT NULL,
    actual_cost DECIMAL(10,4),
    tokens_used INTEGER,
    tokens_estimated INTEGER,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'STARTED',
    user_id VARCHAR(100),
    request_context VARCHAR(200),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_cost_status CHECK (status IN ('STARTED', 'COMPLETED', 'FAILED', 'CANCELLED')),
    CONSTRAINT chk_estimated_cost CHECK (estimated_cost >= 0),
    CONSTRAINT chk_actual_cost CHECK (actual_cost IS NULL OR actual_cost >= 0),
    CONSTRAINT chk_tokens CHECK (tokens_used IS NULL OR tokens_used >= 0)
);

-- Create budget_limits table
CREATE TABLE IF NOT EXISTS budget_limits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scope VARCHAR(50) NOT NULL,
    scope_value VARCHAR(100),
    period VARCHAR(20) NOT NULL,
    limit_amount DECIMAL(10,2) NOT NULL,
    alert_threshold DECIMAL(3,2) NOT NULL DEFAULT 0.8,
    hard_stop_threshold DECIMAL(3,2) NOT NULL DEFAULT 0.95,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT chk_scope CHECK (scope IN ('global', 'service', 'feature', 'user')),
    CONSTRAINT chk_period CHECK (period IN ('HOURLY', 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT chk_limit_amount CHECK (limit_amount > 0),
    CONSTRAINT chk_alert_threshold CHECK (alert_threshold >= 0 AND alert_threshold <= 1),
    CONSTRAINT chk_hard_stop_threshold CHECK (hard_stop_threshold >= 0 AND hard_stop_threshold <= 1),
    CONSTRAINT chk_threshold_order CHECK (alert_threshold <= hard_stop_threshold)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_cost_transactions_service_time ON cost_transactions(service, start_time);
CREATE INDEX IF NOT EXISTS idx_cost_transactions_feature_time ON cost_transactions(feature, start_time);
CREATE INDEX IF NOT EXISTS idx_cost_transactions_user_time ON cost_transactions(user_id, start_time);
CREATE INDEX IF NOT EXISTS idx_cost_transactions_status ON cost_transactions(status);
CREATE INDEX IF NOT EXISTS idx_cost_transactions_start_time ON cost_transactions(start_time);
CREATE INDEX IF NOT EXISTS idx_cost_transactions_created_at ON cost_transactions(created_at);

CREATE INDEX IF NOT EXISTS idx_budget_limits_scope ON budget_limits(scope, scope_value);
CREATE INDEX IF NOT EXISTS idx_budget_limits_period ON budget_limits(period);
CREATE INDEX IF NOT EXISTS idx_budget_limits_active ON budget_limits(active);

-- Create unique constraint for budget limits (one limit per scope/period combination)
CREATE UNIQUE INDEX IF NOT EXISTS idx_budget_limits_unique 
ON budget_limits(scope, COALESCE(scope_value, ''), period) 
WHERE active = true;

-- Insert default budget limits (only if they don't exist yet)
INSERT INTO budget_limits (scope, scope_value, period, limit_amount, description) 
SELECT 'global', NULL, 'DAILY', 100.00, 'Globales Tagesbudget für alle AI-Services'
WHERE NOT EXISTS (SELECT 1 FROM budget_limits WHERE scope = 'global' AND scope_value IS NULL AND period = 'DAILY');

INSERT INTO budget_limits (scope, scope_value, period, limit_amount, description) 
SELECT 'global', NULL, 'MONTHLY', 2000.00, 'Globales Monatsbudget für alle AI-Services'
WHERE NOT EXISTS (SELECT 1 FROM budget_limits WHERE scope = 'global' AND scope_value IS NULL AND period = 'MONTHLY');

INSERT INTO budget_limits (scope, scope_value, period, limit_amount, description) 
SELECT 'service', 'openai', 'DAILY', 50.00, 'Tagesbudget für OpenAI Services'
WHERE NOT EXISTS (SELECT 1 FROM budget_limits WHERE scope = 'service' AND scope_value = 'openai' AND period = 'DAILY');

INSERT INTO budget_limits (scope, scope_value, period, limit_amount, description) 
SELECT 'service', 'anthropic', 'DAILY', 30.00, 'Tagesbudget für Anthropic Services'
WHERE NOT EXISTS (SELECT 1 FROM budget_limits WHERE scope = 'service' AND scope_value = 'anthropic' AND period = 'DAILY');

INSERT INTO budget_limits (scope, scope_value, period, limit_amount, description) 
SELECT 'feature', 'smart-suggestions', 'DAILY', 20.00, 'Tagesbudget für Smart Suggestions Feature'
WHERE NOT EXISTS (SELECT 1 FROM budget_limits WHERE scope = 'feature' AND scope_value = 'smart-suggestions' AND period = 'DAILY');

-- Add comments for documentation
COMMENT ON TABLE cost_transactions IS 'Tracks individual API/service cost transactions';
COMMENT ON TABLE budget_limits IS 'Configurable budget limits for cost control';

COMMENT ON COLUMN cost_transactions.service IS 'Service provider (openai, anthropic, local, rules)';
COMMENT ON COLUMN cost_transactions.feature IS 'Feature using the service (smart-suggestions, data-analysis, etc.)';
COMMENT ON COLUMN cost_transactions.model IS 'Specific model used (gpt-4, claude-3, etc.)';
COMMENT ON COLUMN cost_transactions.estimated_cost IS 'Pre-transaction cost estimate';
COMMENT ON COLUMN cost_transactions.actual_cost IS 'Actual cost after transaction completion';
COMMENT ON COLUMN cost_transactions.status IS 'Transaction status (STARTED, COMPLETED, FAILED, CANCELLED)';

COMMENT ON COLUMN budget_limits.scope IS 'Budget scope (global, service, feature, user)';
COMMENT ON COLUMN budget_limits.scope_value IS 'Specific value for the scope (service name, feature name, user id)';
COMMENT ON COLUMN budget_limits.period IS 'Budget period (HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY)';
COMMENT ON COLUMN budget_limits.alert_threshold IS 'Percentage threshold for alerts (0.0-1.0)';
COMMENT ON COLUMN budget_limits.hard_stop_threshold IS 'Percentage threshold for hard stops (0.0-1.0)';