# Migration Documentation for Contact Management System

## Overview
This document describes the database migrations V209-V211 that implement the Contact Management System with role-based access, multi-location support, and soft-delete functionality.

## Migration V209: Contact Roles System
**File:** `V209__add_contact_roles.sql`

### Purpose
Implements a flexible role-based system for customer contacts, replacing the simple boolean `is_decision_maker` flag with a more sophisticated multi-role approach.

### Changes
1. **New Table: `contact_roles`**
   - `contact_id`: UUID reference to customer_contacts
   - `role`: VARCHAR(50) for role name
   - `created_at`: Timestamp for audit
   - Primary key: composite (contact_id, role)

2. **New Column: `responsibility_scope`**
   - Added to `customer_contacts` table
   - VARCHAR(20) with default 'all'
   - Defines the scope of responsibility (all, location_specific, department_specific)

3. **Data Migration**
   - Existing contacts with `is_decision_maker = true` get 'DECISION_MAKER' role
   - Preserves existing decision maker information

### Security Considerations
- Role names are stored as strings to allow flexibility
- Foreign key constraint ensures referential integrity
- Case-insensitive role checking implemented in application layer

## Migration V210: Contact Location Assignments
**File:** `V210__add_contact_location_assignments.sql`

### Purpose
Enables contacts to be assigned to specific customer locations with primary/secondary designation.

### Changes
1. **New Table: `contact_location_assignments`**
   - `contact_id`: UUID reference to customer_contacts
   - `location_id`: UUID reference to customer_locations
   - `is_primary`: Boolean flag for primary location contact
   - `assigned_at`: Timestamp for tracking
   - Primary key: composite (contact_id, location_id)

2. **New View: `v_contact_primary_locations`**
   - Provides easy access to primary location assignments
   - Joins contacts with their primary locations
   - Includes customer and location names for reporting

3. **Data Migration**
   - Creates assignments for existing contacts to their customer's locations
   - Sets first contact as primary for each location

### Performance Optimizations
- Indexes on foreign keys for join performance
- Materialized view can be created for heavy read scenarios

## Migration V211: Soft Delete System
**File:** `V211__add_soft_delete_fields.sql`

### Purpose
Implements comprehensive soft-delete functionality with audit trail for GDPR compliance and data recovery.

### Changes
1. **New Columns in `customer_contacts`:**
   - `deleted_at`: Timestamp of deletion
   - `deleted_by`: User who performed deletion
   - `deletion_reason`: VARCHAR(500) for documentation
   - `reactivated_at`: Timestamp if contact is restored
   - `reactivated_by`: User who restored contact
   - `reactivation_reason`: Reason for restoration

2. **New Function: `audit_soft_delete()`**
   - PL/pgSQL function for tracking soft delete operations
   - Automatically logs deletion/reactivation events
   - Can be triggered on UPDATE of is_deleted flag

3. **New Views:**
   - `v_active_contacts`: Shows only non-deleted contacts
   - `v_contact_deletion_stats`: Aggregates deletion statistics

### GDPR Compliance
- Soft delete preserves audit trail while marking data as deleted
- Deletion reason field for compliance documentation
- Reactivation capability for error recovery
- All personal data can be anonymized while preserving structure

## Rollback Procedures

### V211 Rollback
```sql
DROP VIEW IF EXISTS v_contact_deletion_stats;
DROP VIEW IF EXISTS v_active_contacts;
DROP FUNCTION IF EXISTS audit_soft_delete();
ALTER TABLE customer_contacts DROP COLUMN IF EXISTS deleted_at;
ALTER TABLE customer_contacts DROP COLUMN IF EXISTS deleted_by;
ALTER TABLE customer_contacts DROP COLUMN IF EXISTS deletion_reason;
ALTER TABLE customer_contacts DROP COLUMN IF EXISTS reactivated_at;
ALTER TABLE customer_contacts DROP COLUMN IF EXISTS reactivated_by;
ALTER TABLE customer_contacts DROP COLUMN IF EXISTS reactivation_reason;
```

### V210 Rollback
```sql
DROP VIEW IF EXISTS v_contact_primary_locations;
DROP TABLE IF EXISTS contact_location_assignments;
```

### V209 Rollback
```sql
DROP TABLE IF EXISTS contact_roles;
ALTER TABLE customer_contacts DROP COLUMN IF EXISTS responsibility_scope;
```

## Testing Strategy

### Unit Tests
- `CustomerContactTest`: Entity-level testing for role management
- `ContactMigrationTest`: Validates migration execution and schema

### Integration Tests
- Test role assignment and retrieval
- Test location assignment functionality
- Test soft delete and recovery operations

### Performance Tests
- `ContactPerformanceTest`: Bulk operations and query performance
- Tests handle 1000+ contacts efficiently
- Concurrent access patterns validated

### Security Tests
- `ContactSecurityTest`: Input validation and injection prevention
- XSS prevention in all text fields
- SQL injection prevention through parameterized queries
- GDPR compliance validation

## Known Issues and Limitations

1. **Partial Migration Success**
   - Some columns may already exist from previous partial migrations
   - Use `IF NOT EXISTS` clauses to handle idempotent execution

2. **Role System**
   - Currently using string-based roles (temporary implementation)
   - Future iteration will implement proper role entity

3. **Performance Considerations**
   - Large contact lists should use pagination
   - Consider materialized views for complex queries
   - Index on is_deleted for active contact queries

## Monitoring and Maintenance

### Key Metrics to Monitor
- Number of active vs. deleted contacts
- Role distribution across contacts
- Location assignment coverage
- Query performance on contact searches

### Regular Maintenance Tasks
1. Vacuum deleted contacts table quarterly
2. Reindex contact tables monthly
3. Archive old soft-deleted records annually
4. Review and optimize slow queries

## Security Checklist

- [x] Input validation on all fields
- [x] SQL injection prevention through JPA
- [x] XSS prevention at presentation layer
- [x] Role-based access control ready
- [x] Audit trail for all changes
- [x] GDPR soft delete support
- [x] Personal data encryption (database level)
- [ ] Field-level encryption for sensitive data (future)
- [ ] Data masking for non-production environments (future)

## Future Enhancements

1. **Role Entity Implementation**
   - Replace string-based roles with proper entity
   - Add role permissions and hierarchies
   - Implement role templates

2. **Advanced Location Features**
   - Location-based access control
   - Geographic clustering
   - Multi-location contact sharing

3. **Enhanced Audit System**
   - Detailed change tracking
   - Automated compliance reports
   - Data retention policies

## Support and Troubleshooting

### Common Issues

**Issue:** Migration fails with "column already exists"
**Solution:** Run with `IF NOT EXISTS` clauses or check migration history

**Issue:** Performance degradation with many contacts
**Solution:** Ensure indexes are present, consider partitioning

**Issue:** Soft-deleted contacts still visible
**Solution:** Use v_active_contacts view instead of direct table access

### Contact Information
For migration support, contact the development team through the standard channels.

## Appendix: SQL Scripts

### Useful Queries

```sql
-- Find all decision makers
SELECT c.* FROM customer_contacts c
JOIN contact_roles r ON c.id = r.contact_id
WHERE r.role = 'DECISION_MAKER' AND c.deleted_at IS NULL;

-- Get contact distribution by location
SELECT cl.location_name, COUNT(DISTINCT cla.contact_id) as contact_count
FROM customer_locations cl
LEFT JOIN contact_location_assignments cla ON cl.id = cla.location_id
GROUP BY cl.location_name;

-- Audit trail for deleted contacts
SELECT deleted_at, deleted_by, deletion_reason, COUNT(*) 
FROM customer_contacts
WHERE is_deleted = true
GROUP BY deleted_at, deleted_by, deletion_reason
ORDER BY deleted_at DESC;
```