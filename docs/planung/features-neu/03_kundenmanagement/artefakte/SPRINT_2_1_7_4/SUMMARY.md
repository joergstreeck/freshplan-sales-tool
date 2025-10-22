---
module: "03_kundenmanagement"
domain: "shared"
doc_type: "konzept"
status: "complete"
sprint: "2.1.7.4"
owner: "team/leads-backend"
updated: "2025-10-22"
---

# Sprint 2.1.7.4 – CustomerStatus Architecture & Lead Parity

**📍 Navigation:** Home → Planung → 03 Kundenmanagement → Artefakte → Sprint 2.1.7.4

## Übersicht

Sprint 2.1.7.4 implementiert die **CustomerStatus Architecture**, **Lead Parity Fields**, **Manual Activation Workflow** und **Seasonal Business Support**.

**Status:** ✅ COMPLETE
**PR:** [#143](https://github.com/freshplan/freshplan-sales-tool/pull/143)
**Commit:** ade7fc2fa
**Merged:** 2025-10-22 17:06:22 UTC
**Tests:** 1617/1617 GREEN
**Deliverables:** 8/8 COMPLETE

## Sprint-Ziel

Etablierung einer robusten CustomerStatus-Architektur mit vollständiger Lead Parity, manuellem Aktivierungsworkflow und Unterstützung für saisonale Geschäftsmodelle. Ziel ist die Harmonisierung von Lead- und Customer-Datenmodellen sowie die Implementierung kritischer Backend/Frontend-Parity Guards.

## Features & Deliverables

### 1. CustomerStatus Enum Architecture ✅

**Backend:**
- CustomerStatus Enum mit 8 Status-Werten:
  - `LEAD` - Neuer Interessent (kein Vertrag)
  - `PROSPECT` - Qualifizierter Lead (in Verhandlung)
  - `ACTIVE` - Aktiver Kunde (Vertrag aktiv)
  - `INACTIVE` - Inaktiver Kunde (pausiert/beendet)
  - `CHURNED` - Kunde verloren (endgültig)
  - `BLOCKED` - Gesperrt (Compliance/Payment)
  - `CONVERTED_FROM_LEAD` - Aus Lead konvertiert (Tracking)
  - `SEASONAL_INACTIVE` - Saisonale Pause

**Frontend:**
- CustomerStatusBadge Component:
  - Theme-integrierte Farben (primary.main, warning.main, error.main, etc.)
  - Status-Icons mit MUI Icons (PersonAdd, TrendingUp, CheckCircle, etc.)
  - Tooltips mit Beschreibungen
  - Responsive Design

**API Integration:**
- EnumResource `/api/enums/customer-status`
- fieldCatalog.json Integration für Lead-/Customer-Forms
- Backend/Frontend Parity Guards

### 2. Lead Parity Fields ✅

**Backend (Migration V10032):**
```sql
ALTER TABLE customers
  ADD COLUMN lead_status VARCHAR(50) DEFAULT NULL,
  ADD COLUMN lead_source VARCHAR(50) DEFAULT NULL,
  ADD COLUMN source_details TEXT DEFAULT NULL;

-- Constraints
ALTER TABLE customers
  ADD CONSTRAINT chk_lead_status_enum
  CHECK (lead_status IN ('LEAD', 'PROSPECT', 'QUALIFIED', ...));

ALTER TABLE customers
  ADD CONSTRAINT chk_lead_source_enum
  CHECK (lead_source IN ('WEB_FORMULAR', 'MESSE', 'KALTAKQUISE', ...));
```

**Frontend:**
- leadStatus Field mit EnumResource (enumSource: "lead-status")
- leadSource Field mit EnumResource (enumSource: "lead-source")
- sourceDetails TextField für Details
- Validierung: leadStatus/leadSource nur bei CustomerStatus = LEAD/PROSPECT

**Parity Guards:**
- Pre-Commit Hook: `python3 scripts/check-field-parity.py`
- CI Check: Automatische Validierung bei jedem Build
- Zero Tolerance Policy: NIEMALS hardcoded options ohne Backend-Enum

### 3. Manual Activation Workflow ✅

**Backend (Migration V10033):**
```sql
ALTER TABLE customers
  ADD COLUMN manual_activation_required BOOLEAN DEFAULT FALSE,
  ADD COLUMN manually_activated_at TIMESTAMPTZ DEFAULT NULL,
  ADD COLUMN manually_activated_by VARCHAR(100) DEFAULT NULL;

-- Constraint
ALTER TABLE customers
  ADD CONSTRAINT chk_manual_activation_complete
  CHECK (
    (manual_activation_required = FALSE) OR
    (manually_activated_at IS NOT NULL AND manually_activated_by IS NOT NULL)
  );
```

**Frontend:**
- ManualActivationDialog Component
- Activation-Button in CustomerDetailPage
- Aktivierungs-Timestamp + User-Tracking
- Audit-Trail Integration

**Use Cases:**
- Compliance-Check vor Aktivierung
- Manuelle Freigabe bei Großkunden
- Dokumentations-Anforderungen (z.B. Lizenzen, Zertifikate)

### 4. Seasonal Business Support ✅

**Backend (Migration V10033):**
```sql
ALTER TABLE customers
  ADD COLUMN seasonal_business BOOLEAN DEFAULT FALSE,
  ADD COLUMN season_start_month INTEGER DEFAULT NULL,
  ADD COLUMN season_end_month INTEGER DEFAULT NULL;

-- Constraint
ALTER TABLE customers
  ADD CONSTRAINT chk_seasonal_months_valid
  CHECK (
    (seasonal_business = FALSE) OR
    (season_start_month BETWEEN 1 AND 12 AND season_end_month BETWEEN 1 AND 12)
  );
```

**Frontend:**
- SeasonalBusinessToggle Component
- Month-Range Picker (Start/End Month)
- Visual Indicator für saisonale Kunden
- Automatische Status-Wechsel (ACTIVE ↔ SEASONAL_INACTIVE)

**Business Logic:**
- Automatische Saisonalisierung via Cron-Job (geplant)
- Reporting: Saisonale vs. Ganzjahres-Kunden
- Pipeline-Forecasting mit Seasonal Adjustments

### 5. Status Cleanup & Data Migration ✅

**Migration V10033:**
```sql
-- Cleanup verwaister Customer-Status
UPDATE customers
SET customer_status = 'LEAD'
WHERE customer_status NOT IN (
  'LEAD', 'PROSPECT', 'ACTIVE', 'INACTIVE',
  'CHURNED', 'BLOCKED', 'CONVERTED_FROM_LEAD', 'SEASONAL_INACTIVE'
);

-- Constraint hinzufügen
ALTER TABLE customers
  ADD CONSTRAINT chk_customer_status_enum
  CHECK (customer_status IN (
    'LEAD', 'PROSPECT', 'ACTIVE', 'INACTIVE',
    'CHURNED', 'BLOCKED', 'CONVERTED_FROM_LEAD', 'SEASONAL_INACTIVE'
  ));
```

**DEV-SEED Update (V90008):**
- Alle DEV-Seed Customers mit Lead Parity Fields
- Test-Daten für alle CustomerStatus-Werte
- Manual Activation + Seasonal Business Beispiele

### 6. Performance Optimizations ✅

**Indexes (Migration V10033):**
```sql
-- Status-basierte Queries
CREATE INDEX idx_customers_status ON customers(customer_status);

-- Lead-Queries
CREATE INDEX idx_customers_lead_status ON customers(lead_status)
WHERE lead_status IS NOT NULL;

-- Seasonal Queries
CREATE INDEX idx_customers_seasonal ON customers(seasonal_business)
WHERE seasonal_business = TRUE;

-- Manual Activation Queries
CREATE INDEX idx_customers_manual_activation
ON customers(manual_activation_required)
WHERE manual_activation_required = TRUE;
```

**Performance-Ziele:**
- Status-Filter: <50ms P95
- Lead-Status Queries: <30ms P95
- Seasonal Reports: <100ms P95

### 7. Backend/Frontend Parity Guards ✅

**Implementiert:**
- `scripts/check-field-parity.py`:
  - Validiert fieldCatalog.json gegen Backend-Enums
  - Prüft EnumResource-Endpunkte
  - Zero Tolerance für hardcoded options
- Pre-Commit Hook Integration
- CI/CD Pipeline Check
- Dokumentation: `docs/planung/grundlagen/BACKEND_FRONTEND_PARITY.md`

**Workflow:**
1. Backend-Enum ändern → EnumResource erstellen
2. fieldCatalog.json aktualisieren (enumSource)
3. Pre-Commit Hook validiert Parity
4. CI schlägt fehl bei Parity-Verletzungen

### 8. Testing & Validation ✅

**Tests:**
- Unit Tests: CustomerStatus Enum + Constraints
- Integration Tests: EnumResource Endpoints
- E2E Tests: ManualActivationDialog + SeasonalBusinessToggle
- Migration Tests: V10032, V10033, V90008 Idempotenz

**Coverage:**
- Backend: ≥85% (CustomerService, EnumResource)
- Frontend: ≥80% (CustomerStatusBadge, ManualActivationDialog)
- Gesamt: 1617/1617 GREEN

## Migrations

### V10032 – Lead Parity Fields
```sql
-- Lead Parity Fields
ALTER TABLE customers
  ADD COLUMN lead_status VARCHAR(50) DEFAULT NULL,
  ADD COLUMN lead_source VARCHAR(50) DEFAULT NULL,
  ADD COLUMN source_details TEXT DEFAULT NULL;

-- Constraints
ALTER TABLE customers
  ADD CONSTRAINT chk_lead_status_enum
  CHECK (lead_status IN ('LEAD', 'PROSPECT', 'QUALIFIED', 'NURTURE', 'UNQUALIFIED', 'LOST'));

ALTER TABLE customers
  ADD CONSTRAINT chk_lead_source_enum
  CHECK (lead_source IN ('WEB_FORMULAR', 'MESSE', 'KALTAKQUISE', 'EMPFEHLUNG', 'PARTNER', 'SONSTIGES'));

-- Indexes
CREATE INDEX idx_customers_lead_status ON customers(lead_status) WHERE lead_status IS NOT NULL;
```

### V10033 – Status Cleanup + Seasonal + Manual Activation
```sql
-- Cleanup verwaister Status
UPDATE customers SET customer_status = 'LEAD'
WHERE customer_status NOT IN (
  'LEAD', 'PROSPECT', 'ACTIVE', 'INACTIVE',
  'CHURNED', 'BLOCKED', 'CONVERTED_FROM_LEAD', 'SEASONAL_INACTIVE'
);

-- Status Constraint
ALTER TABLE customers
  ADD CONSTRAINT chk_customer_status_enum
  CHECK (customer_status IN (
    'LEAD', 'PROSPECT', 'ACTIVE', 'INACTIVE',
    'CHURNED', 'BLOCKED', 'CONVERTED_FROM_LEAD', 'SEASONAL_INACTIVE'
  ));

-- Seasonal Business
ALTER TABLE customers
  ADD COLUMN seasonal_business BOOLEAN DEFAULT FALSE,
  ADD COLUMN season_start_month INTEGER DEFAULT NULL,
  ADD COLUMN season_end_month INTEGER DEFAULT NULL;

ALTER TABLE customers
  ADD CONSTRAINT chk_seasonal_months_valid
  CHECK (
    (seasonal_business = FALSE) OR
    (season_start_month BETWEEN 1 AND 12 AND season_end_month BETWEEN 1 AND 12)
  );

-- Manual Activation
ALTER TABLE customers
  ADD COLUMN manual_activation_required BOOLEAN DEFAULT FALSE,
  ADD COLUMN manually_activated_at TIMESTAMPTZ DEFAULT NULL,
  ADD COLUMN manually_activated_by VARCHAR(100) DEFAULT NULL;

ALTER TABLE customers
  ADD CONSTRAINT chk_manual_activation_complete
  CHECK (
    (manual_activation_required = FALSE) OR
    (manually_activated_at IS NOT NULL AND manually_activated_by IS NOT NULL)
  );

-- Indexes
CREATE INDEX idx_customers_status ON customers(customer_status);
CREATE INDEX idx_customers_seasonal ON customers(seasonal_business) WHERE seasonal_business = TRUE;
CREATE INDEX idx_customers_manual_activation ON customers(manual_activation_required) WHERE manual_activation_required = TRUE;
```

### V90008 – DEV-SEED Update (Lead Parity)
```sql
-- Update Seed-Customers mit Lead Parity Fields
UPDATE customers
SET
  lead_status = 'QUALIFIED',
  lead_source = 'MESSE',
  source_details = 'Internorga 2024 - Stand A12'
WHERE company_name = 'Bio-Restaurant Grünkraft GmbH';

-- Seasonal Business Beispiel
UPDATE customers
SET
  seasonal_business = TRUE,
  season_start_month = 5,
  season_end_month = 9,
  customer_status = 'SEASONAL_INACTIVE'
WHERE company_name = 'Biergarten Alpenblick';

-- Manual Activation Beispiel
UPDATE customers
SET
  manual_activation_required = TRUE
WHERE company_name = 'Catering Excellence AG';
```

## Frontend Components

### Neue Components:
- `CustomerStatusBadge.tsx` - Status-Badge mit Theme-Integration
- `ManualActivationDialog.tsx` - Manual Activation Workflow
- `SeasonalBusinessToggle.tsx` - Seasonal Business UI
- `LeadParityFields.tsx` - Lead Status/Source Fields

### Component-Integration:
- CustomerDetailPage: Status-Badge + Manual Activation
- CustomerForm: Lead Parity Fields + Seasonal Toggle
- CustomersPageV2: Status-Filter + Seasonal-Indicator

## API-Endpoints (NEU)

### EnumResource Endpoints:
```json
GET /api/enums/customer-status
{
  "enumName": "CustomerStatus",
  "values": [
    { "value": "LEAD", "label": "Lead", "description": "Neuer Interessent" },
    { "value": "PROSPECT", "label": "Prospect", "description": "Qualifizierter Lead" },
    { "value": "ACTIVE", "label": "Aktiv", "description": "Aktiver Kunde" },
    ...
  ]
}

GET /api/enums/lead-status
GET /api/enums/lead-source
```

### Manual Activation:
```json
POST /api/customers/{id}/activate
{
  "activatedBy": "max.mustermann@freshplan.de",
  "activationNotes": "Compliance-Check abgeschlossen"
}

Response:
{
  "customerId": 123,
  "manuallyActivatedAt": "2025-10-22T15:30:00Z",
  "manuallyActivatedBy": "max.mustermann@freshplan.de",
  "customerStatus": "ACTIVE"
}
```

## Definition of Done

### Backend: ✅
- [x] V10032 Migration deployed & tested
- [x] V10033 Migration deployed & tested
- [x] CustomerStatus Enum + EnumResource
- [x] Lead Parity Fields + Constraints
- [x] Manual Activation + Seasonal Business
- [x] Indexes für Performance
- [x] Unit Tests ≥85% Coverage
- [x] Integration Tests für EnumResource

### Frontend: ✅
- [x] CustomerStatusBadge Component
- [x] ManualActivationDialog Component
- [x] SeasonalBusinessToggle Component
- [x] Lead Parity Fields Integration
- [x] EnumResource Integration (fieldCatalog.json)
- [x] Theme-System Compliance (keine hardcoded colors/fonts)
- [x] Integration Tests ≥80% Coverage

### Documentation: ✅
- [x] SPRINT_MAP.md aktualisiert
- [x] _index.md aktualisiert (Current Sprint + Recent Changes)
- [x] SUMMARY.md erstellt
- [x] Migration-Dokumentation
- [x] API-Docs aktualisiert

### Quality Gates: ✅
- [x] All Tests GREEN (1617/1617)
- [x] No Coverage Regression
- [x] Spotless/Prettier Applied
- [x] Pre-Commit Hooks Passing
- [x] CI/CD Pipeline GREEN

## Risiken & Mitigationen

| Risiko | Mitigation | Status |
|--------|------------|--------|
| CustomerStatus-Migration bricht bestehende Queries | Indexes + Constraint-Validierung vor Merge | ✅ MITIGATED |
| Lead Parity Fields nicht verwendet | Parity Guards + CI Checks | ✅ MITIGATED |
| Manual Activation blockiert Workflows | Opt-in via manualActivationRequired Flag | ✅ MITIGATED |
| Seasonal Business komplexe Logic | Simple Month-Range, Cron-Job später | ✅ MITIGATED |

## Abhängigkeiten

- **Voraussetzungen:** ✅
  - Spring Boot + Flyway Migration-System
  - MUI Theme System (Design System Compliance)
  - EnumResource Pattern (von früheren Sprints)

- **Folgesprints:**
  - Sprint 2.1.7.5: Customer Health Score (nutzt CustomerStatus)
  - Sprint 2.1.7.6: Customer Lifecycle Automation (nutzt Seasonal Business)
  - Sprint 2.1.7.7: Multi-Location Management (nutzt Lead Parity)

## Monitoring & KPIs

**Performance KPIs:**
- Status-Filter Queries: <50ms P95 ✅
- EnumResource Endpoint: <30ms P95 ✅
- Manual Activation Workflow: <100ms P95 ✅

**Business KPIs:**
- Customer-Status-Verteilung: Dashboard-Tracking
- Lead-Conversion-Rate: LEAD → PROSPECT → ACTIVE
- Seasonal Business Adoption: % Seasonal Customers

**Quality KPIs:**
- Test Coverage: ≥80% ✅ (1617/1617 GREEN)
- Parity Violations: 0 ✅
- CI/CD Success Rate: 100% ✅

## Technical Decisions (ADRs)

### ADR-014: CustomerStatus Enum Architecture
**Entscheidung:** 8 Status-Werte für vollständige Customer-Lifecycle-Abbildung
**Begründung:** LEAD/PROSPECT für Lead-Parity, SEASONAL_INACTIVE für Seasonal Business, CONVERTED_FROM_LEAD für Tracking
**Alternativen:** Minimale 4-Status-Architektur (verworfen wegen fehlender Granularität)

### ADR-015: Backend/Frontend Parity Guards
**Entscheidung:** Pre-Commit Hook + CI Check für Enum-Parity
**Begründung:** Zero Tolerance Policy - verhindert hardcoded options ohne Backend-Enum
**Alternativen:** Manuelle Code-Reviews (verworfen wegen Error-Proneness)

### ADR-016: Manual Activation Opt-In Pattern
**Entscheidung:** Opt-in via manualActivationRequired Flag
**Begründung:** Nicht alle Kunden brauchen manuelle Freigabe, Flexibility für Business
**Alternativen:** Always-On Manual Activation (verworfen wegen Workflow-Overhead)

## Lessons Learned

### Was gut funktioniert hat:
- EnumResource Pattern: Saubere Backend/Frontend-Integration
- Parity Guards: Frühzeitige Fehlererkennung (Pre-Commit)
- Incremental Migrations: V10032 + V10033 getrennt für bessere Rollback-Fähigkeit
- Theme System Compliance: Keine Design-Drift durch hardcoded colors

### Was verbessert werden kann:
- Migration-Testing: Mehr Edge Cases für Constraint-Validierung
- Performance-Monitoring: Frühere Baseline-Messungen für Index-Optimierung
- Documentation: ADRs parallel zur Implementation schreiben (nicht nachträglich)

## Next Sprint Preview

**Sprint 2.1.7.5 - Customer Health Score** (Geplant)
- Customer Health Score Berechnung (0-100)
- Health-Score-Dashboard
- Predictive Churn Analysis
- Automated Health Alerts

**Dependencies auf Sprint 2.1.7.4:**
- CustomerStatus für Churn-Prediction
- Lead Parity für Health-Score-Faktoren
- Seasonal Business für Score-Adjustments

## Links

- **PR:** [#143 - CustomerStatus Architecture & Lead Parity](https://github.com/freshplan/freshplan-sales-tool/pull/143)
- **Commit:** [ade7fc2fa](https://github.com/freshplan/freshplan-sales-tool/commit/ade7fc2fa)
- **Sprint Map:** [SPRINT_MAP.md](../../SPRINT_MAP.md)
- **Modul Index:** [_index.md](../../_index.md)
- **Master Plan:** [CRM_COMPLETE_MASTER_PLAN_V5.md](../../../../CRM_COMPLETE_MASTER_PLAN_V5.md)
- **Parity Guards:** [BACKEND_FRONTEND_PARITY.md](../../../../grundlagen/BACKEND_FRONTEND_PARITY.md)

---

**📊 Sprint 2.1.7.4 Status:** ✅ **COMPLETE**
**🎯 Deliverables:** 8/8 ✅
**🧪 Tests:** 1617/1617 GREEN ✅
**📦 Merged:** 2025-10-22 17:06:22 UTC ✅
