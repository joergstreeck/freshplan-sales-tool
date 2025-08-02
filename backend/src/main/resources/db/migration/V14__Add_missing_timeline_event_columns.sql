-- V14: Add missing columns to customer_timeline_events table
-- Date: 2025-07-05
-- Author: Claude (FreshPlan Team)
-- Description: Add all missing columns that are defined in CustomerTimelineEvent entity

-- First check if the table exists (created by Hibernate in update mode)
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customer_timeline_events') THEN
        -- CRITICAL: Fix column name mismatch - rename event_description to description
        IF EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'customer_timeline_events' 
                  AND column_name = 'event_description') THEN
            ALTER TABLE customer_timeline_events RENAME COLUMN event_description TO description;
            -- Also drop NOT NULL constraint as the entity defines it as nullable
            ALTER TABLE customer_timeline_events ALTER COLUMN description DROP NOT NULL;
            RAISE NOTICE 'Renamed column event_description to description and made it nullable';
        END IF;
        -- Add missing columns one by one
        
        -- Category column (ENUM)
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'category') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN category VARCHAR(30) NOT NULL DEFAULT 'SYSTEM';
        END IF;
        
        -- Title column
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'title') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN title VARCHAR(255) NOT NULL DEFAULT 'Event';
        END IF;
        
        -- Importance column
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'importance') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN importance VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';
        END IF;
        
        -- Performed by columns
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'performed_by') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN performed_by VARCHAR(100) NOT NULL DEFAULT 'system';
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'performed_by_role') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN performed_by_role VARCHAR(50);
        END IF;
        
        -- Source columns
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'source_system') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN source_system VARCHAR(50) DEFAULT 'FreshPlan CRM';
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'source_reference') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN source_reference VARCHAR(255);
        END IF;
        
        -- Related entity columns
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'related_contact_id') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN related_contact_id UUID;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'related_location_id') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN related_location_id UUID;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'related_document_id') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN related_document_id UUID;
        END IF;
        
        -- Communication columns
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'communication_channel') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN communication_channel VARCHAR(30);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'communication_direction') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN communication_direction VARCHAR(20);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'communication_duration') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN communication_duration INTEGER;
        END IF;
        
        -- Follow-up columns
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'requires_follow_up') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN requires_follow_up BOOLEAN NOT NULL DEFAULT FALSE;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'follow_up_date') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN follow_up_date TIMESTAMP;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'follow_up_notes') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN follow_up_notes TEXT;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'follow_up_completed') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN follow_up_completed BOOLEAN NOT NULL DEFAULT FALSE;
        END IF;
        
        -- Audit trail columns
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'old_values') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN old_values VARCHAR(2000);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'new_values') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN new_values VARCHAR(2000);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'changed_fields') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN changed_fields VARCHAR(500);
        END IF;
        
        -- Business impact columns
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'business_impact') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN business_impact VARCHAR(500);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'revenue_impact') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN revenue_impact DECIMAL(12,2);
        END IF;
        
        -- Visibility columns
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'is_public') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN is_public BOOLEAN NOT NULL DEFAULT TRUE;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'is_customer_visible') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN is_customer_visible BOOLEAN NOT NULL DEFAULT FALSE;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'access_level') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN access_level VARCHAR(20) DEFAULT 'INTERNAL';
        END IF;
        
        -- Tags and labels
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'tags') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN tags VARCHAR(500);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'labels') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN labels VARCHAR(500);
        END IF;
        
        -- External references
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'external_id') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN external_id VARCHAR(100);
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'external_url') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN external_url VARCHAR(500);
        END IF;
        
        -- Soft delete columns
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'is_deleted') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'deleted_at') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN deleted_at TIMESTAMP;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'deleted_by') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN deleted_by VARCHAR(100);
        END IF;
        
        -- Audit fields
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'created_at') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'updated_at') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN updated_at TIMESTAMP;
        END IF;
        
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                      WHERE table_name = 'customer_timeline_events' 
                      AND column_name = 'updated_by') THEN
            ALTER TABLE customer_timeline_events ADD COLUMN updated_by VARCHAR(100);
        END IF;
        
        RAISE NOTICE 'Successfully added missing columns to customer_timeline_events table';
    ELSE
        RAISE NOTICE 'Table customer_timeline_events does not exist yet - will be created by Hibernate';
    END IF;
END $$;