-- V211: Add Enhanced Soft-Delete Fields
-- Author: Claude
-- Date: 2025-08-08  
-- Description: Adds comprehensive soft-delete tracking with audit information

-- 1. Add enhanced soft-delete fields to contacts
ALTER TABLE customer_contacts 
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(100),
ADD COLUMN IF NOT EXISTS deletion_reason VARCHAR(500);

-- 2. Create index for soft-delete queries
-- Partial index only on deleted records for better performance
CREATE INDEX IF NOT EXISTS idx_contact_deleted 
ON customer_contacts(deleted_at) 
WHERE deleted_at IS NOT NULL;

-- 3. Migrate existing is_deleted flag to new structure
UPDATE customer_contacts 
SET 
    deleted_at = CASE 
        WHEN is_deleted = true AND deleted_at IS NULL 
        THEN CURRENT_TIMESTAMP 
        ELSE deleted_at 
    END,
    deleted_by = CASE 
        WHEN is_deleted = true AND deleted_by IS NULL 
        THEN 'MIGRATION_V211' 
        ELSE deleted_by 
    END,
    deletion_reason = CASE 
        WHEN is_deleted = true AND deletion_reason IS NULL 
        THEN 'Migrated from is_deleted flag' 
        ELSE deletion_reason 
    END
WHERE is_deleted = true;

-- 4. Add comments for documentation
COMMENT ON COLUMN customer_contacts.deleted_at IS 
'Timestamp when the contact was soft-deleted';

COMMENT ON COLUMN customer_contacts.deleted_by IS 
'User identifier who performed the soft-delete';

COMMENT ON COLUMN customer_contacts.deletion_reason IS 
'Reason for deletion (e.g., GDPR request, duplicate, data cleanup)';

COMMENT ON COLUMN customer_contacts.is_deleted IS 
'DEPRECATED: Use deleted_at instead. Kept for backward compatibility.';

-- 5. Create view for active (non-deleted) contacts
CREATE OR REPLACE VIEW v_active_contacts AS
SELECT * FROM customer_contacts
WHERE deleted_at IS NULL 
  AND (is_deleted = false OR is_deleted IS NULL)
  AND is_active = true;

COMMENT ON VIEW v_active_contacts IS 
'View showing only active, non-deleted contacts for simplified querying';

-- 6. Create function to soft-delete a contact
CREATE OR REPLACE FUNCTION soft_delete_contact(
    p_contact_id UUID,
    p_deleted_by VARCHAR(100),
    p_reason VARCHAR(500)
) RETURNS VOID AS $$
BEGIN
    UPDATE customer_contacts
    SET 
        deleted_at = CURRENT_TIMESTAMP,
        deleted_by = p_deleted_by,
        deletion_reason = p_reason,
        is_deleted = true,
        is_active = false
    WHERE id = p_contact_id
      AND deleted_at IS NULL;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION soft_delete_contact IS 
'Soft-deletes a contact with full audit information';

-- 7. Create function to restore a soft-deleted contact
CREATE OR REPLACE FUNCTION restore_contact(
    p_contact_id UUID,
    p_restored_by VARCHAR(100)
) RETURNS VOID AS $$
BEGIN
    UPDATE customer_contacts
    SET 
        deleted_at = NULL,
        deleted_by = NULL,
        deletion_reason = NULL,
        is_deleted = false,
        is_active = true,
        updated_by = p_restored_by,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_contact_id
      AND deleted_at IS NOT NULL;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION restore_contact IS 
'Restores a soft-deleted contact back to active status';