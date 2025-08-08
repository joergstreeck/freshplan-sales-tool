-- V32: Create contact_interactions table for Data Strategy Intelligence
-- This table tracks all interactions with contacts to enable warmth scoring,
-- timeline visualization, and predictive analytics

CREATE TABLE contact_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    
    -- Sentiment and engagement metrics
    sentiment_score DOUBLE PRECISION, -- -1.0 to +1.0
    engagement_score INTEGER, -- 0-100
    
    -- Auto-captured metadata
    response_time_minutes INTEGER,
    word_count INTEGER,
    initiated_by VARCHAR(50), -- CUSTOMER, SALES, SYSTEM
    
    -- Content and details
    subject VARCHAR(500),
    summary TEXT,
    full_content TEXT,
    
    -- Channel information
    channel VARCHAR(50), -- EMAIL, PHONE, MEETING, CHAT, SOCIAL
    channel_details VARCHAR(255), -- e.g., "LinkedIn", "WhatsApp", "Zoom"
    
    -- Outcome tracking
    outcome VARCHAR(50), -- POSITIVE, NEUTRAL, NEGATIVE, PENDING
    next_action VARCHAR(500),
    next_action_date TIMESTAMP,
    
    -- External references
    external_ref_id VARCHAR(100), -- ID from email system, CRM, etc.
    external_ref_type VARCHAR(50), -- OUTLOOK, GMAIL, SALESFORCE, etc.
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    
    -- Foreign key constraints
    CONSTRAINT fk_interaction_contact FOREIGN KEY (contact_id) 
        REFERENCES customer_contacts(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_interaction_contact ON contact_interactions(contact_id);
CREATE INDEX idx_interaction_timestamp ON contact_interactions(timestamp);
CREATE INDEX idx_interaction_type ON contact_interactions(type);
CREATE INDEX idx_interaction_initiated_by ON contact_interactions(initiated_by);

-- Index for finding overdue follow-ups
CREATE INDEX idx_interaction_next_action_date 
    ON contact_interactions(next_action_date) 
    WHERE next_action_date IS NOT NULL;

-- Index for sentiment analysis queries
CREATE INDEX idx_interaction_sentiment 
    ON contact_interactions(contact_id, sentiment_score) 
    WHERE sentiment_score IS NOT NULL;

-- Add warmth_score to contacts table for caching calculated values
ALTER TABLE customer_contacts ADD COLUMN warmth_score INTEGER DEFAULT 50;
ALTER TABLE customer_contacts ADD COLUMN warmth_confidence INTEGER DEFAULT 0;
ALTER TABLE customer_contacts ADD COLUMN last_interaction_date TIMESTAMP;
ALTER TABLE customer_contacts ADD COLUMN interaction_count INTEGER DEFAULT 0;

-- Create index for warmth-based queries
CREATE INDEX idx_contact_warmth ON customer_contacts(warmth_score) WHERE warmth_score IS NOT NULL;

-- Add comment to explain the purpose
COMMENT ON TABLE contact_interactions IS 'Tracks all interactions with contacts for relationship intelligence and predictive analytics';
COMMENT ON COLUMN contact_interactions.sentiment_score IS 'Sentiment analysis score from -1.0 (very negative) to +1.0 (very positive)';
COMMENT ON COLUMN contact_interactions.engagement_score IS 'Engagement level from 0 (no engagement) to 100 (highly engaged)';
COMMENT ON COLUMN customer_contacts.warmth_score IS 'Calculated relationship warmth from 0 (cold) to 100 (hot), default 50 (neutral)';
COMMENT ON COLUMN customer_contacts.warmth_confidence IS 'Confidence level of warmth score from 0 to 100 based on data quantity';