# SQL Migration Templates

**Zweck:** Production-ready SQL Schemas für Phase 2 Lead Protection & Sample Management

## Migration Deployment

```bash
# Variable Migration Numbers verwenden
cp lead_protection_and_rls.sql ../../../../../../backend/db/migrations/V$(./scripts/get-next-migration.sh)__lead_protection_and_rls.sql
cp sample_cdm_extension.sql ../../../../../../backend/db/migrations/V$(./scripts/get-next-migration.sh)__sample_cdm_extension.sql
cp settings_lead_ai.sql ../../../../../../backend/db/migrations/V$(./scripts/get-next-migration.sh)__settings_lead_ai.sql

# Deploy Migrations
cd backend && ./mvnw flyway:migrate
```

## Enthaltene Templates

### `lead_protection_and_rls.sql` (165 Zeilen)
- **v_lead_protection View:** 6M+60T+Stop-the-Clock Business Logic
- **RLS Policies:** Fail-closed Security für ACTIVE/GRACE Leads
- **Multi-Contact Collaboration:** lead_collaborator Table + Views
- **Background Job Support:** lead_protection_reminder + hold Tables

### `sample_cdm_extension.sql` (72 Zeilen)
- **Remote Testing Pipeline:** sample_box → test_phase → feedback
- **Customer Feedback Portal:** external_feedback_token Support
- **Product Feedback Tracking:** Quantitative + Qualitative Data
- **Activity Integration:** Automatic SAMPLE_FEEDBACK Activities

### `settings_lead_ai.sql` (59 Zeilen)
- **Settings Registry Integration:** Modul 06 Compatibility
- **Policy Keys:** Global/Org/User Scope Configuration
- **AI Budget Management:** Monthly Caps + Confidence Thresholds
- **Lead Protection Settings:** Base Months + Progress Days

## Business Logic Features

### Lead Protection System
```sql
-- Kernlogik für Handelsvertretervertrag
valid_until = GREATEST(
  assigned_at + 6 months + hold_duration_since(),
  last_qual_activity + 60 days + hold_duration_since()
)

-- 3-Stage Pipeline
ACTIVE → GRACE (10 days) → EXPIRED
```

### Stop-the-Clock Support
- `FFZ_PRICE_APPROVAL` - FreshFoodz Preisfreigabe
- `FFZ_SAMPLE_DELAY` - Sample Delivery Verzögerungen
- `CUSTOMER_BLOCKED` - Customer-side Blockierung

### Multi-Contact Collaboration
- **OWNER:** Vollzugriff + Schutzrecht
- **VIEW:** Read-only Access
- **ASSIST:** Support bei Admin-Tasks
- **NEGOTIATE:** Verhandlungsunterstützung

## Foundation Standards Compliance

✅ **RLS Security:** Fail-closed Policies
✅ **Named Parameters:** $1, $2 statt String Concatenation
✅ **Idempotent Operations:** CREATE IF NOT EXISTS
✅ **Index Optimization:** Alle Critical Queries abgedeckt
✅ **Audit Trail:** Vollständige Change Tracking

## Performance Considerations

- **Normal Load:** p95 <50ms für v_lead_protection
- **Peak Load (5x):** Graceful degradation via Caching
- **Scale (10k+ Leads):** Materialized View Option verfügbar
- **Multi-Tenant:** RLS Policy Optimization

## Testing Strategy

### Unit Tests erforderlich:
- Lead Protection Business Logic (6M+60T+Holds)
- RLS Policy Enforcement
- Multi-Contact Collaboration Rules
- Settings Registry Integration

### Integration Tests erforderlich:
- Background Job Idempotency
- Stop-the-Clock Hold Management
- Sample Feedback Pipeline
- Cross-Module Dependencies

---

**📋 Ready for immediate deployment mit Variable Migration Strategy!**