-- V211: Add Enhanced Soft-Delete Fields
-- Author: Claude
-- Date: 2025-08-08  
-- Description: Adds comprehensive soft-delete tracking with audit information

-- 1. Add enhanced soft-delete fields to contacts
ALTER TABLE customer_contacts
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS deletion_reason VARCHAR(500);

-- 2. Add reactivation tracking
ALTER TABLE customer_contacts
ADD COLUMN IF NOT EXISTS reactivated_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS reactivated_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS reactivation_reason VARCHAR(500);

-- 3. Create index for efficient soft-delete queries  
CREATE INDEX IF NOT EXISTS idx_contacts_deleted 
ON customer_contacts(deleted_at) 
WHERE deleted_at IS NULL;

-- 4. Create audit function for soft-delete operations
CREATE OR REPLACE FUNCTION audit_soft_delete()
RETURNS TRIGGER AS $$
BEGIN
    -- When a record is soft-deleted
    IF NEW.deleted_at IS NOT NULL AND OLD.deleted_at IS NULL THEN
        INSERT INTO audit_trail(
            entity_type,
            entity_id,
            action,
            performed_by,
            performed_at,
            old_value,
            new_value
        ) VALUES (
            'Contact',
            NEW.id::VARCHAR,
            'SOFT_DELETE',
            NEW.deleted_by,
            NEW.deleted_at,
            jsonb_build_object('active', true),
            jsonb_build_object('active', false, 'reason', NEW.deletion_reason)
        );
    END IF;
    
    -- When a record is reactivated
    IF NEW.deleted_at IS NULL AND OLD.deleted_at IS NOT NULL THEN
        INSERT INTO audit_trail(
            entity_type,
            entity_id,
            action,
            performed_by,
            performed_at,
            old_value,
            new_value
        ) VALUES (
            'Contact',
            NEW.id::VARCHAR,
            'REACTIVATE',
            NEW.reactivated_by,
            NEW.reactivated_at,
            jsonb_build_object('active', false),
            jsonb_build_object('active', true, 'reason', NEW.reactivation_reason)
        );
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 5. Create trigger for audit
DROP TRIGGER IF EXISTS trigger_audit_contact_soft_delete ON customer_contacts;
CREATE TRIGGER trigger_audit_contact_soft_delete
    AFTER UPDATE ON customer_contacts
    FOR EACH ROW
    WHEN (OLD.deleted_at IS DISTINCT FROM NEW.deleted_at)
    EXECUTE FUNCTION audit_soft_delete();

-- 6. Create view for active contacts only
CREATE OR REPLACE VIEW v_active_contacts AS
SELECT *
FROM customer_contacts
WHERE deleted_at IS NULL;

COMMENT ON VIEW v_active_contacts IS 
'View showing only active (non-deleted) contacts for easier querying';

-- 7. Create statistics view for soft-delete metrics
CREATE OR REPLACE VIEW v_contact_deletion_stats AS
SELECT 
    COUNT(*) FILTER (WHERE deleted_at IS NULL) as active_contacts,
    COUNT(*) FILTER (WHERE deleted_at IS NOT NULL) as deleted_contacts,
    COUNT(*) FILTER (WHERE reactivated_at IS NOT NULL) as reactivated_contacts,
    COUNT(DISTINCT deleted_by) as deletion_actors,
    MAX(deleted_at) as last_deletion,
    MAX(reactivated_at) as last_reactivation
FROM customer_contacts;

COMMENT ON VIEW v_contact_deletion_stats IS 
'Dashboard view for monitoring contact soft-delete metrics';