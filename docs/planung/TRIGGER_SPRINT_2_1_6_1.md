---
sprint_id: "2.1.6.1"
title: "Enum-Migration Phase 2+3 - Customer-Harmonisierung & CRM-Vollausbau"
doc_type: "konzept"
status: "planned"
owner: "team/leads-backend"
date_start: "2025-10-09"
date_end: "2025-10-11"
modules: ["02_neukundengewinnung", "03_kundenmanagement"]
phases:
  - phase: "Phase 1"
    branch: "feature/mod02-sprint-2.1.6.1-customer-enum-migration"
    scope: "Customer BusinessType Migration (Industry → BusinessType)"
    status: "planned"
    effort: "~6h"
  - phase: "Phase 2"
    branch: "feature/mod02-sprint-2.1.6.1-crm-enum-harmonization"
    scope: "CRM-weite Enum-Harmonisierung (Activity, Opportunity, Payment, Delivery)"
    status: "planned"
    effort: "~10h"
entry_points:
  - "features-neu/02_neukundengewinnung/_index.md"
  - "features-neu/02_neukundengewinnung/backend/_index.md"
  - "features-neu/02_neukundengewinnung/SPRINT_MAP.md"
  - "features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md"
updated: "2025-10-08"
---

# Sprint 2.1.6.1 – Enum-Migration Phase 2+3

**📍 Navigation:** Home → Planung → Sprint 2.1.6.1

## 🚀 SPRINT-PHASEN ÜBERSICHT

| Phase | Branch | Scope | Status | Effort |
|-------|--------|-------|--------|--------|
| **Phase 1** | `feature/mod02-sprint-2.1.6.1-customer-enum-migration` | Customer BusinessType Migration (Industry → BusinessType) | 📋 PLANNED | ~6h |
| **Phase 2** | `feature/mod02-sprint-2.1.6.1-crm-enum-harmonization` | CRM-weite Enum-Harmonisierung (Activity, Opportunity, Payment, Delivery) | 📋 PLANNED | ~10h |

> **📚 WICHTIGE DOKUMENTE (entry_points - siehe YAML Header oben):**
> - **Enum-Migration Strategie:** [`ENUM_MIGRATION_STRATEGY.md`](features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md) - 3-Phasen-Plan + Code-Patterns
> - **Pre-Claim Logic:** [`PRE_CLAIM_LOGIC.md`](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/PRE_CLAIM_LOGIC.md) - MESSE/TELEFON Business-Rule
> - **Business Logic:** [`BUSINESS_LOGIC_LEAD_ERFASSUNG.md`](features-neu/02_neukundengewinnung/artefakte/SPRINT_2_1_5/BUSINESS_LOGIC_LEAD_ERFASSUNG.md) - Lead-Erfassung Workflows

> **⚠️ WICHTIG - PFLICHT-LESEFOLGE (7 Dokumente):**
> 1. **`/CLAUDE.md`** - Meta-Arbeitsregeln + Compact Contract
> 2. **`/docs/planung/TRIGGER_INDEX.md`** - Sprint-Übersicht
> 3. **`/docs/planung/CRM_AI_CONTEXT_SCHNELL.md`** - Business-Kontext
> 4. **`/docs/planung/PRODUCTION_ROADMAP_2025.md`** - Roadmap
> 5. **`/docs/planung/features-neu/02_neukundengewinnung/SPRINT_MAP.md`** - Modul-Navigation
> 6. **`/docs/planung/features-neu/02_neukundengewinnung/artefakte/ENUM_MIGRATION_STRATEGY.md`** - Enum-Strategie
> 7. **`/docs/planung/MIGRATIONS.md`** - Migration-Historie

## 🔧 GIT WORKFLOW (KRITISCH!)

**PFLICHT-REGELN für alle Sprint-Arbeiten:**

### ✅ ERLAUBT (ohne User-Freigabe):
- `git commit` - Commits erstellen wenn User darum bittet
- `git add` - Dateien stagen
- `git status` / `git diff` - Status prüfen
- Feature-Branches anlegen (`git checkout -b feature/...`)

### 🚫 VERBOTEN (ohne explizite User-Freigabe):
- **`git push`** - NIEMALS ohne User-Erlaubnis pushen!
- **PR-Erstellung** - Nur auf explizite Anforderung
- **PR-Merge** - Nur wenn User explizit zustimmt
- **Branch-Deletion** - Remote-Branches nur mit User-OK löschen

### 📋 Standard-Workflow:
1. **Feature-Branch anlegen:** `git checkout -b feature/mod02-sprint-2.1.6.1-customer-enum-migration`
2. **Arbeiten & Committen:** Code schreiben, Tests validieren, `git commit`
3. **User fragen:** "Branch ist bereit. Soll ich pushen und PR erstellen?"
4. **Erst nach Freigabe:** `git push` + PR-Erstellung

**Referenz:** `/CLAUDE.md` → Sektion "🚫 GIT PUSH POLICY (KRITISCH!)"

---

## Sprint-Ziel

**Kern-Deliverables:**

### **Phase 1: Customer BusinessType Migration (6h)**
1. **Customer.industry → Customer.businessType Migration**
   - Bestehende `Customer.industry` Enum deprecaten (Legacy-Support beibehalten)
   - Neues Feld `Customer.businessType` mit 9 harmonisierten Werten
   - Daten-Migration: Industry-Werte auf BusinessType mappen
   - Dual-Mode: Beide Felder temporär parallel für Rückwärtskompatibilität

2. **Customer-Formulare auf BusinessType umstellen**
   - Frontend: CustomerEditDialog, CustomerCreateForm
   - Hook: `useBusinessTypes()` bereits vorhanden (aus Sprint 2.1.6 Phase 2)
   - Dropdown-Werte von Backend-API laden (GET /api/enums/business-types)

3. **Migration V27X erstellen**
   - Nummer dynamisch ermitteln via `./scripts/get-next-migration.sh`
   - Data-Migration: Industry → BusinessType Mapping
   - Idempotent: Sichere Wiederholbarkeit bei Fehler

### **Phase 2: CRM-weite Enum-Harmonisierung (10h)**

4. **ActivityType erweitern**
   - Neue Activity-Types für erweiterte Lead-/Customer-Workflows
   - DB: CREATE TYPE + ALTER TABLE lead_activities.activity_type
   - Backend: ActivityType Enum erweitern
   - Frontend: Activity-Formulare aktualisieren

5. **OpportunityStatus Enum**
   - Opportunity-Status als Enum statt String
   - DB: CREATE TYPE + ALTER TABLE opportunities.status
   - Backend: OpportunityStatus Enum erstellen
   - Frontend: Opportunity-Workflows mit Enum-Validierung

6. **Payment/Delivery-Enums**
   - Payment-Methods als Enum (SEPA, Kreditkarte, Rechnung)
   - Delivery-Methods als Enum (Standard, Express, Sameday)
   - DB: CREATE TYPE für beide Enums
   - Backend: PaymentMethod, DeliveryMethod Enums

7. **Vollständige CRM-Harmonisierung**
   - Alle String-basierte Enumerations in echte Enums konvertieren
   - Konsistentes Enum-Pattern im gesamten CRM
   - Single Source of Truth: Backend-API für alle Enum-Werte
   - Frontend: Dynamische Dropdowns ohne Hardcoding

---

## 📋 STRATEGISCHE BEGRÜNDUNG

### **Warum JETZT? (Pre-Production = goldene Zeit)**

**✅ VORTEILE:**
- **Keine Daten-Migration in Production** - Clean Slate, keine Legacy-Daten
- **Technische Schulden vermeiden** - Type-Safety von Anfang an
- **Performance-Gewinn** - Enums ~10x schneller als String-Matching (Index-Lookup vs. LIKE)
- **MESSE/TELEFON-Check funktioniert** - Pre-Claim Logic erfordert Enum (siehe PRE_CLAIM_LOGIC.md)

**❌ RISIKO OHNE ENUM-MIGRATION:**
- MESSE/TELEFON Erstkontakt-PFLICHT nicht prüfbar (String-Matching fehleranfällig)
- Technische Schulden wachsen (später 3x teurer zu fixen)
- Performance-Impact steigt mit Datenmenge (String-LIKE vs. Enum-Index)
- Daten-Migration später nötig (komplexer mit Live-Daten)

### **3-Phasen-Plan Recap**

**Phase 1 (Sprint 2.1.6 Phase 5):** ✅ COMPLETE
- Lead-Modul: LeadSource, BusinessType, KitchenSize
- Migration V273 deployed
- Frontend: useLeadSources(), useBusinessTypes(), useKitchenSizes() Hooks

**Phase 2 (Sprint 2.1.6.1 Phase 1):** 📋 PLANNED (diese Phase)
- Customer-Modul: Industry → BusinessType Migration
- Migration V27X (dynamisch)
- Customer-Formulare auf BusinessType umstellen

**Phase 3 (Sprint 2.1.6.1 Phase 2):** 📋 PLANNED (diese Phase)
- CRM-weit: ActivityType, OpportunityStatus, PaymentMethod, DeliveryMethod
- Vollständige Enum-Harmonisierung
- Alle Modules nutzen Backend-Enum-API

---

## User Stories

### 1. Customer BusinessType Migration (Phase 1)

**Akzeptanzkriterien:**
- [ ] Migration V27X erstellt (Nummer via `./scripts/get-next-migration.sh`)
- [ ] Customer.businessType Feld hinzugefügt (9 Werte, konsistent mit Lead)
- [ ] Daten-Migration: Industry → BusinessType Mapping
  - RESTAURANT → RESTAURANT
  - HOTEL → HOTEL
  - CATERING → CATERING
  - CANTEEN/KANTINE → KANTINE
  - WHOLESALE → GROSSHANDEL
  - RETAIL → LEH
  - EDUCATION → BILDUNG
  - HEALTHCARE → GESUNDHEIT
  - OTHER → SONSTIGES
- [ ] Customer.industry deprecaten (Legacy-Support für 1 Sprint)
- [ ] Auto-Sync: Setter für industry aktualisiert businessType und vice versa
- [ ] CustomerDTO: businessType Feld hinzugefügt
- [ ] Frontend: CustomerEditDialog nutzt useBusinessTypes() Hook
- [ ] Tests: CustomerServiceTest mit BusinessType-Validierung
- [ ] API: GET /api/customers/{id} enthält businessType

**Technische Details:**
```sql
-- Migration V27X: Customer BusinessType Migration
-- Sprint 2.1.6.1 Phase 1: Customer-Modul BusinessType-Harmonisierung

-- Step 1: Add businessType column (nullable initially)
ALTER TABLE customers
ADD COLUMN business_type VARCHAR(50);

-- Step 2: Data Migration (Industry → BusinessType)
UPDATE customers SET business_type =
  CASE
    WHEN industry = 'RESTAURANT' THEN 'RESTAURANT'
    WHEN industry = 'HOTEL' THEN 'HOTEL'
    WHEN industry = 'CATERING' THEN 'CATERING'
    WHEN industry IN ('CANTEEN', 'KANTINE') THEN 'KANTINE'
    WHEN industry = 'WHOLESALE' THEN 'GROSSHANDEL'
    WHEN industry = 'RETAIL' THEN 'LEH'
    WHEN industry = 'EDUCATION' THEN 'BILDUNG'
    WHEN industry = 'HEALTHCARE' THEN 'GESUNDHEIT'
    WHEN industry = 'OTHER' THEN 'SONSTIGES'
    ELSE 'SONSTIGES' -- Fallback für unbekannte Werte
  END;

-- Step 3: Make businessType NOT NULL (after data migration)
ALTER TABLE customers
ALTER COLUMN business_type SET NOT NULL;

-- Step 4: Add CHECK constraint (9 valid values)
ALTER TABLE customers
ADD CONSTRAINT chk_customer_business_type
CHECK (business_type IN (
  'RESTAURANT', 'HOTEL', 'CATERING', 'KANTINE',
  'GROSSHANDEL', 'LEH', 'BILDUNG', 'GESUNDHEIT', 'SONSTIGES'
));

-- Step 5: Add Index for performance
CREATE INDEX idx_customers_business_type ON customers(business_type);

-- Step 6: Industry bleibt vorerst (Legacy-Support)
-- Kommentar: "DEPRECATED - Use business_type instead. Remove in Sprint 2.1.7"
COMMENT ON COLUMN customers.industry IS 'DEPRECATED - Use business_type instead. Remove in Sprint 2.1.7';
```

**Backend Code-Pattern:**
```java
// Customer.java (Entity)
@Entity
@Table(name = "customers")
public class Customer {

    // NEU: businessType (Primary)
    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false, length = 50)
    private BusinessType businessType;

    // DEPRECATED: industry (Legacy-Support)
    @Deprecated
    @Enumerated(EnumType.STRING)
    @Column(name = "industry", length = 50)
    private CustomerIndustry industry;

    // Auto-Sync Setter (Dual-Mode für Rückwärtskompatibilität)
    public void setBusinessType(BusinessType businessType) {
        this.businessType = businessType;
        // Legacy-Support: Sync industry
        this.industry = mapBusinessTypeToIndustry(businessType);
    }

    public void setIndustry(CustomerIndustry industry) {
        this.industry = industry;
        // Forward-Migration: Sync businessType
        this.businessType = mapIndustryToBusinessType(industry);
    }

    // Mapping-Helfer
    private CustomerIndustry mapBusinessTypeToIndustry(BusinessType bt) {
        return switch (bt) {
            case RESTAURANT -> CustomerIndustry.RESTAURANT;
            case HOTEL -> CustomerIndustry.HOTEL;
            case CATERING -> CustomerIndustry.CATERING;
            case KANTINE -> CustomerIndustry.CANTEEN;
            case GROSSHANDEL -> CustomerIndustry.WHOLESALE;
            case LEH -> CustomerIndustry.RETAIL;
            case BILDUNG -> CustomerIndustry.EDUCATION;
            case GESUNDHEIT -> CustomerIndustry.HEALTHCARE;
            case SONSTIGES -> CustomerIndustry.OTHER;
        };
    }

    private BusinessType mapIndustryToBusinessType(CustomerIndustry ind) {
        return switch (ind) {
            case RESTAURANT -> BusinessType.RESTAURANT;
            case HOTEL -> BusinessType.HOTEL;
            case CATERING -> BusinessType.CATERING;
            case CANTEEN, KANTINE -> BusinessType.KANTINE;
            case WHOLESALE -> BusinessType.GROSSHANDEL;
            case RETAIL -> BusinessType.LEH;
            case EDUCATION -> BusinessType.BILDUNG;
            case HEALTHCARE -> BusinessType.GESUNDHEIT;
            case OTHER -> BusinessType.SONSTIGES;
        };
    }
}

// CustomerDTO.java (DTO)
public record CustomerDTO(
    UUID id,
    String companyName,
    BusinessType businessType, // NEU: Primary
    @Deprecated CustomerIndustry industry, // Legacy-Support
    // ... weitere Felder
) {
    // Factory-Methode für Backward-Compatibility
    public static CustomerDTO fromEntity(Customer customer) {
        return new CustomerDTO(
            customer.getId(),
            customer.getCompanyName(),
            customer.getBusinessType(), // Primary
            customer.getIndustry(), // Legacy (1 Sprint)
            // ... weitere Felder
        );
    }
}
```

**Frontend Code-Pattern:**
```typescript
// CustomerEditDialog.tsx (React Component)
import { useBusinessTypes } from '@/hooks/useEnums';

const CustomerEditDialog = ({ customer, onSave, onCancel }) => {
  const { businessTypes, isLoading } = useBusinessTypes();

  return (
    <Dialog open={true}>
      <DialogTitle>Kunde bearbeiten</DialogTitle>
      <DialogContent>
        <Select
          label="Branche *"
          value={customer.businessType}
          onChange={(e) => handleChange('businessType', e.target.value)}
        >
          {businessTypes.map((type) => (
            <MenuItem key={type.value} value={type.value}>
              {type.label}
            </MenuItem>
          ))}
        </Select>

        {/* Legacy-Feld NICHT mehr anzeigen */}
        {/* customer.industry wurde automatisch gesetzt via Auto-Sync */}
      </DialogContent>
    </Dialog>
  );
};
```

**Aufwand:** 6h (2h Migration, 2h Backend, 1h Frontend, 1h Tests)

---

### 2. CRM-weite Enum-Harmonisierung (Phase 2)

**Akzeptanzkriterien:**
- [ ] **ActivityType Enum erweitert:**
  - Neue Types: SAMPLE_REQUEST, SAMPLE_FEEDBACK, CONTRACT_SIGNED, INVOICE_SENT, PAYMENT_RECEIVED
  - Migration: ALTER TYPE lead_activity_type ADD VALUE
  - Backend: ActivityType.java erweitert
  - Frontend: AddLeadActivityDialog, AddCustomerActivityDialog nutzen Backend-Enum
- [ ] **OpportunityStatus Enum:**
  - CREATE TYPE opportunity_status_type (LEAD, QUALIFIED, PROPOSAL, NEGOTIATION, WON, LOST)
  - Backend: OpportunityStatus.java Enum
  - Migration: opportunities.status String → opportunity_status_type
  - Frontend: OpportunityWorkflow mit Enum-Validierung
- [ ] **PaymentMethod Enum:**
  - CREATE TYPE payment_method_type (SEPA_LASTSCHRIFT, SEPA_UEBERWEISUNG, KREDITKARTE, RECHNUNG)
  - Backend: PaymentMethod.java Enum
  - Migration: orders.payment_method String → payment_method_type
  - Frontend: PaymentSelector Component
- [ ] **DeliveryMethod Enum:**
  - CREATE TYPE delivery_method_type (STANDARD, EXPRESS, SAMEDAY, PICKUP)
  - Backend: DeliveryMethod.java Enum
  - Migration: orders.delivery_method String → delivery_method_type
  - Frontend: DeliverySelector Component
- [ ] **EnumResource API erweitert:**
  - GET /api/enums/activity-types
  - GET /api/enums/opportunity-statuses
  - GET /api/enums/payment-methods
  - GET /api/enums/delivery-methods
- [ ] **Frontend Hooks:**
  - useActivityTypes()
  - useOpportunityStatuses()
  - usePaymentMethods()
  - useDeliveryMethods()
- [ ] **Tests:** 85% Coverage für alle neuen Enum-Services
- [ ] **Dokumentation:** ENUM_MIGRATION_STRATEGY.md aktualisiert mit Phase 2+3 Details

**Technische Details:**
```sql
-- Migration V27Y: ActivityType Enum erweitern
ALTER TYPE lead_activity_type ADD VALUE 'SAMPLE_REQUEST';
ALTER TYPE lead_activity_type ADD VALUE 'SAMPLE_FEEDBACK';
ALTER TYPE lead_activity_type ADD VALUE 'CONTRACT_SIGNED';
ALTER TYPE lead_activity_type ADD VALUE 'INVOICE_SENT';
ALTER TYPE lead_activity_type ADD VALUE 'PAYMENT_RECEIVED';

-- Migration V27Z: OpportunityStatus Enum
CREATE TYPE opportunity_status_type AS ENUM (
  'LEAD', 'QUALIFIED', 'PROPOSAL', 'NEGOTIATION', 'WON', 'LOST'
);

ALTER TABLE opportunities
ALTER COLUMN status TYPE opportunity_status_type
USING status::opportunity_status_type;

-- Migration V280: PaymentMethod Enum
CREATE TYPE payment_method_type AS ENUM (
  'SEPA_LASTSCHRIFT', 'SEPA_UEBERWEISUNG', 'KREDITKARTE', 'RECHNUNG'
);

ALTER TABLE orders
ALTER COLUMN payment_method TYPE payment_method_type
USING payment_method::payment_method_type;

-- Migration V281: DeliveryMethod Enum
CREATE TYPE delivery_method_type AS ENUM (
  'STANDARD', 'EXPRESS', 'SAMEDAY', 'PICKUP'
);

ALTER TABLE orders
ALTER COLUMN delivery_method TYPE delivery_method_type
USING delivery_method::delivery_method_type;
```

**Backend Code-Pattern:**
```java
// EnumResource.java (REST API)
@Path("/api/enums")
@Produces(MediaType.APPLICATION_JSON)
public class EnumResource {

    @GET
    @Path("/activity-types")
    public List<EnumValueDTO> getActivityTypes() {
        return Arrays.stream(ActivityType.values())
            .map(type -> new EnumValueDTO(
                type.name(),
                type.getLabel()
            ))
            .collect(Collectors.toList());
    }

    @GET
    @Path("/opportunity-statuses")
    public List<EnumValueDTO> getOpportunityStatuses() {
        return Arrays.stream(OpportunityStatus.values())
            .map(status -> new EnumValueDTO(
                status.name(),
                status.getLabel()
            ))
            .collect(Collectors.toList());
    }

    // ... weitere Enum-Endpoints
}

// EnumValueDTO.java (DTO)
public record EnumValueDTO(
    String value,  // z.B. "RESTAURANT"
    String label   // z.B. "Restaurant"
) {}
```

**Aufwand:** 10h (3h ActivityType, 2h OpportunityStatus, 2h Payment/Delivery, 2h Frontend, 1h Tests)

---

## Definition of Done

**Phase 1 COMPLETE wenn:**
- [x] Migration V27X deployed (Customer BusinessType)
- [x] Customer.businessType Feld operational
- [x] Daten-Migration erfolgreich (alle Customer haben businessType)
- [x] Customer.industry deprecaten (Legacy-Support aktiv)
- [x] Auto-Sync Setter funktionieren (industry ↔ businessType)
- [x] CustomerDTO enthält businessType
- [x] Frontend: CustomerEditDialog nutzt useBusinessTypes()
- [x] Tests: CustomerServiceTest GREEN (≥85% Coverage)
- [x] API: GET /api/customers/{id} liefert businessType

**Phase 2 COMPLETE wenn:**
- [x] Migrations V27Y-V281 deployed (ActivityType, OpportunityStatus, PaymentMethod, DeliveryMethod)
- [x] Alle Enums als Backend-Types (PostgreSQL + Java)
- [x] EnumResource API vollständig (4 neue Endpoints)
- [x] Frontend Hooks operational (useActivityTypes, useOpportunityStatuses, usePaymentMethods, useDeliveryMethods)
- [x] Alle Formulare nutzen Backend-Enum-API (kein Hardcoding)
- [x] Tests: ≥85% Coverage für alle Enum-Services
- [x] Dokumentation: ENUM_MIGRATION_STRATEGY.md aktualisiert

**Sprint 2.1.6.1 COMPLETE wenn:**
- [x] Beide Phasen erfolgreich abgeschlossen
- [x] KEINE String-basierte Enumerations mehr im CRM
- [x] Single Source of Truth: Backend-API für alle Enum-Werte
- [x] Performance-Tests: Enum-basierte Queries <100ms P95
- [x] MESSE/TELEFON Erstkontakt-PFLICHT funktioniert (Pre-Claim Logic validiert)

---

## Risiken & Mitigation

**Sprint 2.1.6.1 spezifisch:**
- **Daten-Migration Fehler:** Unbekannte Industry-Werte → Fallback auf SONSTIGES
  - **Mitigation:** Dry-Run-Mode, Validierung vor Migration, Rollback-SQL bereit
- **Legacy-Code-Kompatibilität:** Alte APIs erwarten industry statt businessType
  - **Mitigation:** Dual-Mode (1 Sprint), Auto-Sync Setter, Deprecated-Warnings
- **Frontend-Regression:** Hardcoded Enum-Values in alten Komponenten
  - **Mitigation:** Code-Search nach hardcoded Enums, Refactoring PFLICHT

---

## Abhängigkeiten

- **Sprint 2.1.6 Phase 5** (Lead-Enums) muss COMPLETE sein (LeadSource, BusinessType, KitchenSize)
- **Migration V273** muss deployed sein (Lead BusinessType)
- **EnumResource API** muss existieren (GET /api/enums/business-types)
- **Frontend Hooks** müssen existieren (useBusinessTypes, useLeadSources, useKitchenSizes)

---

## Test Strategy

```java
@QuarkusTest
class CustomerBusinessTypeMigrationTest {

    @Test
    void shouldMigrateIndustryToBusinessType() {
        // Given: Customer mit OLD industry Wert
        Customer customer = new Customer();
        customer.setIndustry(CustomerIndustry.RESTAURANT);

        // When: Migration läuft (Auto-Sync Setter)
        // (passiert automatisch via Setter)

        // Then: businessType ist gesetzt
        assertThat(customer.getBusinessType()).isEqualTo(BusinessType.RESTAURANT);
    }

    @Test
    void shouldSyncBusinessTypeToIndustry() {
        // Given: Customer mit NEU businessType
        Customer customer = new Customer();
        customer.setBusinessType(BusinessType.KANTINE);

        // Then: industry ist automatisch gesetzt (Legacy-Support)
        assertThat(customer.getIndustry()).isEqualTo(CustomerIndustry.CANTEEN);
    }

    @Test
    void shouldHandleUnknownIndustryValues() {
        // Given: SQL Migration mit unbekanntem Wert
        // (simuliert via direktes SQL)

        // When: SELECT customer mit unbekanntem industry
        // Then: businessType = SONSTIGES (Fallback)

        // Validierung via Integration-Test
    }
}
```

---

## Monitoring & KPIs

- **Migration Success Rate:** 100% aller Customer mit businessType
- **API Response Time:** GET /api/enums/* <50ms P95
- **Frontend Performance:** Dropdown-Rendering <100ms
- **Enum-Query Performance:** <100ms P95 (vs. 1000ms String-LIKE)

---

## Next Sprint Preview

Sprint 2.1.7: Team Management & Test Infrastructure
- Lead-Transfer Workflow
- Fuzzy-Matching & Review
- Row-Level-Security (RLS)
- CRM Szenario-Builder

---

**📋 Dokumentstand:** 2025-10-08
**🎯 Zweck:** Vollständige Enum-Migration für Type-Safety & Performance
**✅ Status:** PLANNED (Start: 09.10.2025)
