# Sprint 2.1.7.4 - Technical Specification

**Sprint-ID:** 2.1.7.4
**Created:** 2025-10-19
**Status:** 📋 PLANNING
**Owner:** Claude

---

## 📋 ÜBERSICHT

Technische Spezifikation für Sprint 2.1.7.4 - Customer Status Architecture.

**Deliverables:**
1. Migration V10032 (CustomerStatus Enum Cleanup + Seasonal Business)
2. LeadConvertService Fix (PROSPECT statt AKTIV)
3. Auto-Conversion bei Opportunity WON
4. Manual Activation Button
5. Dashboard KPI Updates
6. Xentral Interface (Mock)
7. Seasonal Business Support (ChurnDetectionService)

**Design System:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

---

## 📑 TABLE OF CONTENTS

**Quick Navigation:**

1. [Migration V10032: CustomerStatus Enum Cleanup + Seasonal Business](#1️⃣-migration-v10032-customerstatus-enum-cleanup--seasonal-business)
   - [1.1 Migration SQL](#11-migration-sql)
   - [1.2 Backend Enum Update](#12-backend-enum-update)
   - [1.3 Backend Entity Update](#13-backend-entity-update)

2. [LeadConvertService: PROSPECT statt AKTIV](#2️⃣-leadconvertservice-prospect-statt-aktiv)
   - [2.1 Service Fix](#21-service-fix)

3. [Auto-Conversion bei Opportunity WON](#3️⃣-auto-conversion-bei-opportunity-won)
   - [3.1 OpportunityService.handleOpportunityWon()](#31-opportunityservicehandleopportunitywon)
   - [3.2 OpportunityResource Integration](#32-opportunityresource-integration)

4. [Manual Activation: "Als AKTIV markieren"](#4️⃣-manual-activation-als-aktiv-markieren)
   - [4.1 Backend Endpoint](#41-backend-endpoint)
   - [4.2 Frontend CustomerDetailPage](#42-frontend-customerdetailpage)

5. [Dashboard KPI Updates](#5️⃣-dashboard-kpi-updates)
   - [5.1 CustomerMetrics Service](#51-customermetrics-service)
   - [5.2 Dashboard Widgets](#52-dashboard-widgets)

6. [Xentral-Vorbereitung (Interface)](#6️⃣-xentral-vorbereitung-interface)
   - [6.1 XentralOrderEventHandler Interface](#61-xentralordereventhandler-interface)
   - [6.2 Mock Implementation](#62-mock-implementation)

7. [Seasonal Business Support](#7️⃣-seasonal-business-support)
   - [7.1 ChurnDetectionService](#71-churndetectionservice)
   - [7.2 CustomerMetrics Extended](#72-customermetrics-extended)
   - [7.3 CustomerResponse DTO](#73-customerresponse-dto)
   - [7.4 Frontend - Seasonal Business Indicator](#74-frontend---seasonal-business-indicator)
   - [7.5 Dashboard - Seasonal Widget](#75-dashboard---seasonal-widget)

8. [Test Summary](#-test-summary)

---

## 1️⃣ Migration V10032: CustomerStatus Enum Cleanup + Seasonal Business

### **1.1 Migration SQL**

**Datei:** `backend/src/main/resources/db/migration/V10032__customer_status_architecture.sql`

**Status:** 📋 PLANNING

**Implementierung:**
```sql
-- =============================================================================
-- V10032: CustomerStatus Architecture - LEAD entfernen + Seasonal Business
-- =============================================================================
-- Sprint: 2.1.7.4 - Customer Status Architecture
-- Purpose: Klare Trennung Lead (leads Tabelle) vs Customer (customers Tabelle)
--
-- Problem:
--   - CustomerStatus.LEAD ist konzeptionell falsch!
--   - Leads gehören in "leads" Tabelle
--   - Customers starten bei PROSPECT (qualifiziert, noch kein Kunde)
--   - Food-Branche: Saisonbetriebe (Eisdielen, Biergärten) produzieren falsche Churn-Alarme
--
-- Solution:
--   - Alle LEAD → PROSPECT migrieren
--   - CHECK Constraint aktualisieren (LEAD entfernen)
--   - Seasonal Business Support (Flag + Monate für Saisonbetriebe)
-- =============================================================================

-- 1. Migrate all LEAD customers to PROSPECT
UPDATE customers
SET status = 'PROSPECT',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'LEAD';

-- Log affected rows
DO $$
DECLARE
    affected_count INTEGER;
BEGIN
    GET DIAGNOSTICS affected_count = ROW_COUNT;
    RAISE NOTICE 'Migrated % customers from LEAD to PROSPECT', affected_count;
END $$;

-- 2. Update CHECK constraint (remove LEAD)
ALTER TABLE customers
DROP CONSTRAINT IF EXISTS customer_status_check;

ALTER TABLE customers
ADD CONSTRAINT customer_status_check
CHECK (status IN ('PROSPECT', 'AKTIV', 'RISIKO', 'INAKTIV', 'ARCHIVIERT'));

-- 3. Add Seasonal Business Support (NEW - Sprint 2.1.7.4)
-- Kritisch für Food-Branche: Eisdielen, Biergärten, Ski-Hütten, etc.
ALTER TABLE customers
ADD COLUMN is_seasonal_business BOOLEAN DEFAULT FALSE,
ADD COLUMN seasonal_months INTEGER[] DEFAULT NULL,
ADD COLUMN seasonal_pattern VARCHAR(50) DEFAULT NULL;

COMMENT ON COLUMN customers.is_seasonal_business IS 'Flag: Saisonbetrieb (z.B. Eisdiele, Biergarten)';
COMMENT ON COLUMN customers.seasonal_months IS 'Array von Monaten (1-12), in denen Kunde aktiv ist';
COMMENT ON COLUMN customers.seasonal_pattern IS 'Pattern-Name: SUMMER, WINTER, CUSTOM, etc.';

-- Example: Mark known seasonal businesses in DEV-SEED
UPDATE customers
SET is_seasonal_business = TRUE,
    seasonal_months = ARRAY[3,4,5,6,7,8,9,10],
    seasonal_pattern = 'SUMMER'
WHERE (company_name ILIKE '%eis%' OR company_name ILIKE '%biergarten%')
  AND customer_number LIKE 'KD-DEV-%';

-- 4. Verify: No LEAD customers remain
DO $$
DECLARE
    remaining_leads INTEGER;
BEGIN
    SELECT COUNT(*) INTO remaining_leads
    FROM customers
    WHERE status = 'LEAD';

    IF remaining_leads > 0 THEN
        RAISE EXCEPTION 'Migration failed: % customers still have LEAD status', remaining_leads;
    ELSE
        RAISE NOTICE '✓ Migration successful: All LEAD customers converted to PROSPECT';
    END IF;
END $$;
```

**Tests:**
- Integration Test: Verify LEAD → PROSPECT migration
- Integration Test: Verify CHECK constraint updated
- Integration Test: Verify seasonal_months column exists
- Unit Test: CustomerStatus Enum (LEAD should not exist)
- Unit Test: Customer Entity (seasonal fields)

---

### **1.2 Backend Enum Update**

**Datei:** `backend/src/main/java/de/freshplan/domain/customer/entity/CustomerStatus.java`

```java
package de.freshplan.domain.customer.entity;

/**
 * Customer Status Lifecycle
 *
 * Sprint 2.1.7.4: LEAD removed (conceptually wrong!)
 *
 * Lifecycle:
 * - PROSPECT: Opportunity gewonnen, wartet auf erste Bestellung
 * - AKTIV: Hat mindestens 1 Bestellung (echter Kunde!)
 * - RISIKO: 90+ Tage keine Bestellung
 * - INAKTIV: 180+ Tage keine Bestellung
 * - ARCHIVIERT: Lost/Rejected
 */
public enum CustomerStatus {
  // LEAD,  ← REMOVED! Gehört NICHT in Customer-Tabelle!
  PROSPECT,    // Qualified Lead, noch kein Kunde (Sprint 2.1.7.4)
  AKTIV,       // Hat mindestens 1 Bestellung
  RISIKO,      // 90+ Tage keine Bestellung
  INAKTIV,     // 180+ Tage keine Bestellung
  ARCHIVIERT   // Lost/Rejected
}
```

---

### **1.3 Backend Entity Update**

**Datei:** `backend/src/main/java/de/freshplan/domain/customer/entity/Customer.java`

```java
// Add Seasonal Business Support (Sprint 2.1.7.4)

@Column(name = "is_seasonal_business")
private Boolean isSeasonalBusiness = false;

@Column(name = "seasonal_months", columnDefinition = "integer[]")
private List<Integer> seasonalMonths; // Array[1-12] für aktive Monate

@Column(name = "seasonal_pattern", length = 50)
private String seasonalPattern; // 'SUMMER', 'WINTER', 'CUSTOM', etc.

// Getters & Setters
public Boolean getIsSeasonalBusiness() {
  return isSeasonalBusiness;
}

public void setIsSeasonalBusiness(Boolean isSeasonalBusiness) {
  this.isSeasonalBusiness = isSeasonalBusiness;
}

public List<Integer> getSeasonalMonths() {
  return seasonalMonths;
}

public void setSeasonalMonths(List<Integer> seasonalMonths) {
  this.seasonalMonths = seasonalMonths;
}

public String getSeasonalPattern() {
  return seasonalPattern;
}

public void setSeasonalPattern(String seasonalPattern) {
  this.seasonalPattern = seasonalPattern;
}
```

---

## 2️⃣ LeadConvertService: PROSPECT statt AKTIV

### **2.1 Service Fix**

**Datei:** `backend/src/main/java/de/freshplan/modules/leads/service/LeadConvertService.java`

**Status:** 📋 PLANNING (Fix erforderlich - Zeile 81)

**Code-Fix:**
```java
// VORHER (Sprint 2.1.7.3 und früher):
customer.setStatus(CustomerStatus.AKTIV); // ❌ Zu früh! Noch keine Bestellung!

// NACHHER (Sprint 2.1.7.4):
customer.setStatus(CustomerStatus.PROSPECT); // ✅ Wartet auf erste Bestellung
```

**Business-Logik:**
- Lead CONVERTED → Customer PROSPECT
- Erst bei **erster Bestellung** → AKTIV (via Manual Activation oder Xentral Webhook)

**Tests:**
- Unit Test: LeadConvertService sets PROSPECT status
- Integration Test: Lead → Customer conversion workflow
- Integration Test: Verify AKTIV is NOT set immediately

---

## 3️⃣ Auto-Conversion bei Opportunity WON

### **3.1 OpportunityService.handleOpportunityWon()**

**Datei:** `backend/src/main/java/de/freshplan/modules/opportunities/service/OpportunityService.java`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```java
/**
 * Handle Opportunity Won → Auto-Convert Lead to Customer
 *
 * Sprint 2.1.7.4 - Customer Status Architecture
 *
 * Business Rule:
 * - Opportunity WON + leadId != null → Convert Lead to Customer (PROSPECT)
 * - Customer wartet auf erste Bestellung
 *
 * @param opportunityId Opportunity UUID
 * @return Converted Customer (or null if already Customer)
 */
@Transactional
public Customer handleOpportunityWon(UUID opportunityId) {
  Opportunity opp = Opportunity.findById(opportunityId);

  if (opp == null) {
    throw new NotFoundException("Opportunity not found: " + opportunityId);
  }

  if (opp.getStage() != OpportunityStage.CLOSED_WON) {
    throw new IllegalStateException("Opportunity not WON: " + opportunityId);
  }

  // Wenn Opportunity von Lead kommt → Auto-Convert!
  if (opp.getLeadId() != null) {
    Lead lead = Lead.findById(opp.getLeadId());

    if (lead == null) {
      logger.warn("Lead {} not found for Opportunity {}", opp.getLeadId(), opportunityId);
      return null;
    }

    // Convert Lead → Customer (status = PROSPECT)
    LeadConvertRequest request = new LeadConvertRequest(
      null,
      true,
      "Auto-converted from Opportunity WON"
    );

    LeadConvertResponse response = leadConvertService.convertToCustomer(
      lead.getId(),
      request,
      getCurrentUserId()
    );

    Customer customer = Customer.findById(response.customerId());

    // Link Opportunity → Customer
    opp.setCustomerId(UUID.fromString(customer.getId()));
    opp.setLeadId(null); // Lead ist jetzt Customer!
    opp.persist();

    logger.info(
      "Auto-converted Lead {} to Customer {} via Opportunity WON {}",
      lead.getId(), customer.getId(), opportunityId
    );

    // Event: lead_converted_to_customer
    eventPublisher.publish(
      new LeadConvertedEvent(lead.getId(), customer.getId(), opportunityId)
    );

    return customer;
  }

  return null; // Opportunity hatte schon Customer
}
```

**Tests:**
- Integration Test: Opportunity WON → Auto-Convert Lead
- Integration Test: Opportunity-Link updated (leadId → customerId)
- Integration Test: Event published (LeadConvertedEvent)
- Edge Case: Lead not found (should log warning, not crash)
- Edge Case: Lead already converted (should skip conversion)
- Edge Case: Opportunity already has Customer (should skip conversion)

---

### **3.2 OpportunityResource Integration**

**Datei:** `backend/src/main/java/de/freshplan/modules/opportunities/api/OpportunityResource.java`

**Status:** 📋 PLANNING (Enhancement)

**Implementierung:**
```java
/**
 * Update Opportunity Stage
 *
 * Sprint 2.1.7.4: Auto-Convert Lead on WON
 */
@PUT
@Path("/{id}/stage")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
public Response updateStage(@PathParam("id") UUID id, StageUpdateRequest request) {

  Opportunity opp = opportunityService.updateStage(id, request.stage);

  // Auto-Convert bei WON! (Sprint 2.1.7.4)
  if (request.stage == OpportunityStage.CLOSED_WON && opp.getLeadId() != null) {
    try {
      Customer customer = opportunityService.handleOpportunityWon(id);

      if (customer != null) {
        logger.info(
          "Auto-converted Lead to Customer {} (status: PROSPECT)",
          customer.getId()
        );

        return Response.ok(Map.of(
          "opportunity", opp,
          "customer", customer,
          "message", "Lead erfolgreich zu Customer konvertiert (Status: PROSPECT)"
        )).build();
      }
    } catch (Exception e) {
      logger.error("Failed to auto-convert Lead for Opportunity {}", id, e);
      // Continue without conversion - Opportunity still marked as WON
    }
  }

  return Response.ok(opp).build();
}
```

---

## 4️⃣ Manual Activation: "Als AKTIV markieren"

### **4.1 Backend Endpoint**

**Datei:** `backend/src/main/java/de/freshplan/modules/customers/api/CustomerResource.java`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```java
/**
 * Activate Customer: PROSPECT → AKTIV
 *
 * Sprint 2.1.7.4 - Customer Status Architecture
 *
 * Business Rule: First order delivered → Customer becomes AKTIV
 *
 * @param customerId Customer UUID
 * @param request Activation request (optional order number)
 * @return Updated Customer
 */
@PUT
@Path("/{id}/activate")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
@Transactional
public Response activateCustomer(
  @PathParam("id") UUID customerId,
  ActivateCustomerRequest request
) {
  logger.info("PUT /api/customers/{}/activate", customerId);

  // 1. Find Customer
  Customer customer = Customer.findById(customerId);
  if (customer == null) {
    return Response.status(Response.Status.NOT_FOUND)
      .entity(Map.of("error", "Customer not found"))
      .build();
  }

  // 2. Validate: Must be PROSPECT
  if (customer.getStatus() != CustomerStatus.PROSPECT) {
    return Response.status(Response.Status.BAD_REQUEST)
      .entity(Map.of(
        "error", "Customer must be PROSPECT to activate",
        "currentStatus", customer.getStatus()
      ))
      .build();
  }

  // 3. Update Status
  customer.setStatus(CustomerStatus.AKTIV);
  customer.setUpdatedAt(LocalDateTime.now());
  customer.setUpdatedBy(securityContext.getUserPrincipal().getName());
  customer.persist();

  // 4. Audit-Log
  auditLogger.log(
    AuditAction.CUSTOMER_ACTIVATED,
    customerId.toString(),
    Map.of(
      "previous_status", "PROSPECT",
      "new_status", "AKTIV",
      "order_number", request.orderNumber != null ? request.orderNumber : "N/A",
      "activated_by", securityContext.getUserPrincipal().getName()
    )
  );

  // 5. Event
  eventPublisher.publish(new CustomerActivatedEvent(
    customerId,
    request.orderNumber,
    LocalDateTime.now()
  ));

  logger.info(
    "Customer {} activated: PROSPECT → AKTIV (order: {})",
    customerId, request.orderNumber
  );

  return Response.ok(customer).build();
}

/**
 * Activation Request DTO
 */
public record ActivateCustomerRequest(
  String orderNumber  // Optional: Bestellnummer für Audit-Trail
) {}
```

**Tests:**
- Integration Test: PROSPECT → AKTIV activation
- Integration Test: Audit-Log entry created
- Integration Test: Event published (CustomerActivatedEvent)
- Validation Test: AKTIV → AKTIV should fail
- Validation Test: Customer not found should return 404

---

### **4.2 Frontend CustomerDetailPage**

**Datei:** `frontend/src/pages/CustomerDetailPage.tsx`

**Status:** 📋 PLANNING (Enhancement)

**Implementierung:**
```tsx
// PROSPECT Status Alert
{customer.status === 'PROSPECT' && (
  <Alert severity="info" sx={{ mb: 2 }}>
    <AlertTitle>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <HourglassEmptyIcon />
        Kunde wartet auf erste Bestellung
      </Box>
    </AlertTitle>

    <Typography variant="body2" sx={{ mb: 2 }}>
      <strong>{customer.companyName}</strong> wurde aus einer gewonnenen Opportunity konvertiert.
      <br />
      Sobald die erste Bestellung geliefert wurde, können Sie den Kunden als AKTIV markieren.
    </Typography>

    <Stack direction="row" spacing={2}>
      <Button
        variant="contained"
        color="success"
        startIcon={<CheckCircleIcon />}
        onClick={() => setShowActivateDialog(true)}
      >
        Erste Bestellung geliefert → AKTIV markieren
      </Button>

      <Typography variant="caption" color="text.secondary" sx={{ alignSelf: 'center' }}>
        (Wird automatisch via Xentral-Integration erfolgen - Sprint 2.1.7.2)
      </Typography>
    </Stack>
  </Alert>
)}

{/* Activation Dialog */}
<Dialog
  open={showActivateDialog}
  onClose={() => setShowActivateDialog(false)}
  maxWidth="sm"
  fullWidth
>
  <DialogTitle>
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <CheckCircleIcon color="success" />
      Kunde als AKTIV markieren
    </Box>
  </DialogTitle>

  <DialogContent>
    <Typography sx={{ mb: 2 }}>
      Wurde die erste Bestellung für <strong>{customer.companyName}</strong> erfolgreich geliefert?
    </Typography>

    <TextField
      fullWidth
      label="Bestellnummer (optional)"
      value={orderNumber}
      onChange={(e) => setOrderNumber(e.target.value)}
      helperText="Für Audit-Trail - wird im System protokolliert"
      sx={{ mt: 2 }}
    />

    <Alert severity="success" sx={{ mt: 2 }}>
      <AlertTitle>Was passiert:</AlertTitle>
      <ul style={{ margin: '8px 0', paddingLeft: '20px' }}>
        <li>Status wird von <strong>PROSPECT</strong> → <strong>AKTIV</strong> geändert</li>
        <li>Kunde erscheint in Dashboard als "Aktiver Kunde"</li>
        <li>Aktion wird im Audit-Log protokolliert</li>
      </ul>
    </Alert>
  </DialogContent>

  <DialogActions sx={{ px: 3, pb: 2 }}>
    <Button onClick={() => setShowActivateDialog(false)}>
      Abbrechen
    </Button>
    <Button
      variant="contained"
      color="success"
      onClick={handleActivateCustomer}
      startIcon={<CheckCircleIcon />}
    >
      Ja, als AKTIV markieren
    </Button>
  </DialogActions>
</Dialog>
```

**Handler:**
```tsx
const handleActivateCustomer = async () => {
  try {
    const response = await httpClient.put(
      `/api/customers/${customer.id}/activate`,
      { orderNumber }
    );

    if (!response.ok) throw new Error('Activation failed');

    // Success!
    queryClient.invalidateQueries(['customer', customer.id]);
    setShowActivateDialog(false);

    // Show success message
    enqueueSnackbar('Kunde erfolgreich als AKTIV markiert!', { variant: 'success' });

  } catch (error) {
    enqueueSnackbar('Fehler beim Aktivieren', { variant: 'error' });
  }
};
```

**Tests:**
- Component Test: PROSPECT Alert renders
- Component Test: Activation Dialog opens
- Component Test: Form validation (optional order number)
- Integration Test: API call to /activate

---

## 5️⃣ Dashboard KPI Updates

### **5.1 CustomerMetrics Service**

**Datei:** `backend/src/main/java/de/freshplan/modules/metrics/service/CustomerMetricsService.java`

**Status:** 📋 PLANNING (Enhancement)

**Implementierung:**
```java
/**
 * Customer Metrics
 *
 * Sprint 2.1.7.4: Added PROSPECT counter + Conversion Rate
 */
public record CustomerMetrics(
  int totalCustomers,      // Alle außer ARCHIVIERT
  int activeCustomers,     // Status = AKTIV
  int prospects,           // Status = PROSPECT ← NEU!
  int atRisk,              // Status = RISIKO
  int inactive,            // Status = INAKTIV
  double conversionRate    // PROSPECT → AKTIV % ← NEU!
) {

  public static CustomerMetrics calculate() {
    int total = Customer.count("status != 'ARCHIVIERT'");
    int active = Customer.count("status = 'AKTIV'");
    int prospects = Customer.count("status = 'PROSPECT'");
    int atRisk = Customer.count("status = 'RISIKO'");
    int inactive = Customer.count("status = 'INAKTIV'");

    // Conversion Rate: PROSPECT → AKTIV
    // (Aktive Kunden / (Aktive + Prospects)) * 100
    double conversionRate = (active + prospects > 0)
      ? ((double) active / (active + prospects)) * 100
      : 0.0;

    return new CustomerMetrics(
      total, active, prospects, atRisk, inactive, conversionRate
    );
  }
}
```

**Tests:**
- Unit Test: CustomerMetrics.calculate() with PROSPECT customers
- Unit Test: Conversion Rate calculation
- Integration Test: API /api/metrics/customers returns prospects field

---

### **5.2 Dashboard Widgets**

**Datei:** `frontend/src/pages/SalesCockpit.tsx`

**Status:** 📋 PLANNING (Enhancement)

**Implementierung:**
```tsx
// New PROSPECT Metric Card
<Grid item xs={12} md={3}>
  <MetricCard
    title="Prospects"
    value={metrics.prospects}
    subtitle="Warten auf erste Bestellung"
    icon={<HourglassEmptyIcon />}
    color="warning"
    trend={calculateTrend(metrics.prospects, previousMetrics.prospects)}
    onClick={() => navigate('/customers?status=PROSPECT')}
  />
</Grid>

// Conversion Rate Metric Card
<Grid item xs={12} md={3}>
  <MetricCard
    title="Conversion Rate"
    value={`${metrics.conversionRate.toFixed(1)}%`}
    subtitle="PROSPECT → AKTIV"
    icon={<TrendingUpIcon />}
    color="primary"
    tooltip="Prozentsatz der Prospects die zu aktiven Kunden wurden"
  />
</Grid>
```

**Tests:**
- Component Test: PROSPECT MetricCard renders
- Component Test: Conversion Rate MetricCard renders
- Integration Test: Dashboard fetches metrics with prospects field

---

## 6️⃣ Xentral-Vorbereitung (Interface)

### **6.1 XentralOrderEventHandler Interface**

**Datei:** `backend/src/main/java/de/freshplan/modules/xentral/service/XentralOrderEventHandler.java`

**Status:** 📋 PLANNING (NEU! - Interface only)

**Implementierung:**
```java
/**
 * Handles Xentral Order Events
 *
 * Sprint 2.1.7.4: Interface definition
 * Sprint 2.1.7.2: Full implementation with Webhook
 */
public interface XentralOrderEventHandler {

  /**
   * Handle "Order Delivered" event from Xentral
   *
   * Automatically activates PROSPECT customers on first order.
   *
   * @param xentralCustomerId Customer ID in Xentral
   * @param orderNumber Order number
   * @param deliveryDate Delivery date
   */
  void handleOrderDelivered(
    String xentralCustomerId,
    String orderNumber,
    LocalDate deliveryDate
  );
}
```

---

### **6.2 Mock Implementation**

**Datei:** `backend/src/main/java/de/freshplan/modules/xentral/service/MockXentralOrderEventHandler.java`

**Status:** 📋 PLANNING (NEU! - Mock only)

**Implementierung:**
```java
/**
 * Mock Implementation for Xentral Order Event Handler
 *
 * Sprint 2.1.7.4: Mock for testing
 * Sprint 2.1.7.2: Replace with real Xentral Webhook
 */
@ApplicationScoped
public class MockXentralOrderEventHandler implements XentralOrderEventHandler {

  private static final Logger logger = LoggerFactory.getLogger(MockXentralOrderEventHandler.class);

  @Override
  public void handleOrderDelivered(
    String xentralCustomerId,
    String orderNumber,
    LocalDate deliveryDate
  ) {
    // TODO: Sprint 2.1.7.2 - Implement real Xentral Webhook
    logger.info(
      "Mock Xentral Event: Order delivered - customer={}, order={}, date={}",
      xentralCustomerId, orderNumber, deliveryDate
    );
  }
}
```

**Tests:**
- Unit Test: MockXentralOrderEventHandler logs event
- Integration Test: Interface can be injected

---

## 7️⃣ Seasonal Business Support

### **7.1 ChurnDetectionService**

**Datei:** `backend/src/main/java/de/freshplan/modules/customers/service/ChurnDetectionService.java`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```java
/**
 * Churn Detection Service
 *
 * Sprint 2.1.7.4: Seasonal Business Support
 *
 * Business Rule:
 * - Regular customers: 90+ days no order → RISIKO
 * - Seasonal businesses: Exclude off-season (expected inactivity)
 */
@ApplicationScoped
public class ChurnDetectionService {

  private static final Logger logger = LoggerFactory.getLogger(ChurnDetectionService.class);

  /**
   * Check if customer should be monitored for churn
   *
   * Seasonal businesses are excluded during off-season
   */
  public boolean shouldCheckForChurn(Customer customer) {
    // Skip seasonal customers outside their season
    if (Boolean.TRUE.equals(customer.getIsSeasonalBusiness())) {
      int currentMonth = LocalDate.now().getMonthValue();

      List<Integer> activeMonths = customer.getSeasonalMonths();
      if (activeMonths != null && !activeMonths.isEmpty()) {
        // Wenn NICHT in Saison → kein Churn-Check!
        if (!activeMonths.contains(currentMonth)) {
          logger.debug(
            "Customer {} is seasonal and out-of-season (month {}). Skipping churn check.",
            customer.getId(), currentMonth
          );
          return false; // Outside season = expected inactivity
        }
      }
    }

    return true; // Regular churn monitoring
  }

  /**
   * Get at-risk customers (excluding seasonal businesses outside season)
   */
  public List<Customer> getAtRiskCustomers() {
    List<Customer> allAtRisk = Customer.find(
      "status = 'AKTIV' AND lastOrderDate < ?1",
      LocalDate.now().minusDays(90)
    ).list();

    return allAtRisk.stream()
      .filter(this::shouldCheckForChurn)
      .collect(Collectors.toList());
  }
}
```

**Tests:**
- Unit Test: shouldCheckForChurn() - regular customer (should return true)
- Unit Test: shouldCheckForChurn() - seasonal in-season (should return true)
- Unit Test: shouldCheckForChurn() - seasonal off-season (should return false)
- Integration Test: getAtRiskCustomers() excludes off-season seasonal businesses

---

### **7.2 CustomerMetrics Extended**

**Datei:** `backend/src/main/java/de/freshplan/modules/metrics/service/CustomerMetricsService.java`

**Status:** 📋 PLANNING (Enhancement)

**Implementierung:**
```java
/**
 * Customer Metrics - Extended with Seasonal Support
 *
 * Sprint 2.1.7.4: Added seasonalActive, seasonalPaused
 */
public record CustomerMetrics(
  int totalCustomers,
  int activeCustomers,
  int prospects,
  int atRisk,               // ← NOW: Excludes seasonal out-of-season!
  int inactive,
  double conversionRate,
  // NEW (Sprint 2.1.7.4):
  int seasonalActive,       // In-season seasonal businesses
  int seasonalPaused        // Out-of-season (not counted as at-risk!)
) {

  public static CustomerMetrics calculate() {
    int currentMonth = LocalDate.now().getMonthValue();

    // ... existing metrics ...

    // Regular customers at risk
    int regularAtRisk = Customer.count(
      "status = 'AKTIV' AND lastOrderDate < ?1 AND (is_seasonal_business IS NULL OR is_seasonal_business = FALSE)",
      LocalDate.now().minusDays(90)
    );

    // Seasonal businesses IN season at risk
    long seasonalAtRisk = Customer.<Customer>find(
      "status = 'AKTIV' AND is_seasonal_business = TRUE AND lastOrderDate < ?1",
      LocalDate.now().minusDays(90)
    ).stream()
      .filter(c -> c.getSeasonalMonths().contains(currentMonth))
      .count();

    // Seasonal paused (out of season - NOT at risk!)
    long seasonalPaused = Customer.<Customer>find(
      "status = 'AKTIV' AND is_seasonal_business = TRUE"
    ).stream()
      .filter(c -> c.getSeasonalMonths() != null && !c.getSeasonalMonths().contains(currentMonth))
      .count();

    // Seasonal active (in season)
    long seasonalActive = Customer.<Customer>find(
      "status = 'AKTIV' AND is_seasonal_business = TRUE"
    ).stream()
      .filter(c -> c.getSeasonalMonths() != null && c.getSeasonalMonths().contains(currentMonth))
      .count();

    return new CustomerMetrics(
      total,
      active,
      prospects,
      regularAtRisk + (int) seasonalAtRisk, // ← Korrigiert!
      inactive,
      conversionRate,
      (int) seasonalActive,
      (int) seasonalPaused
    );
  }
}
```

**Tests:**
- Unit Test: Seasonal metrics calculation
- Integration Test: API returns seasonalActive, seasonalPaused fields

---

### **7.3 CustomerResponse DTO**

**Datei:** `backend/src/main/java/de/freshplan/modules/customers/api/CustomerResponse.java`

**Status:** 📋 PLANNING (Enhancement)

**Implementierung:**
```java
/**
 * Customer Response DTO
 *
 * Sprint 2.1.7.4: Added Seasonal Business fields
 */
public record CustomerResponse(
  // ... existing fields ...

  // Seasonal Business (Sprint 2.1.7.4)
  Boolean isSeasonalBusiness,
  List<Integer> seasonalMonths,
  String seasonalPattern
) {

  public static CustomerResponse from(Customer customer) {
    return new CustomerResponse(
      // ... existing mappings ...
      customer.getIsSeasonalBusiness(),
      customer.getSeasonalMonths(),
      customer.getSeasonalPattern()
    );
  }
}
```

---

### **7.4 Frontend - Seasonal Business Indicator**

**Datei:** `frontend/src/pages/CustomerDetailPage.tsx`

**Status:** 📋 PLANNING (Enhancement)

**Implementierung:**
```tsx
// Seasonal Business Indicator
{customer.isSeasonalBusiness && (
  <Alert severity="info" icon={<NaturePeopleIcon />} sx={{ mb: 2 }}>
    <AlertTitle>Saisonbetrieb</AlertTitle>
    <Typography variant="body2">
      Aktive Monate: {getSeasonalPatternLabel(customer.seasonalPattern, customer.seasonalMonths)}
    </Typography>
    <Typography variant="caption" color="text.secondary">
      Churn-Monitoring ist außerhalb der Saison pausiert
    </Typography>
  </Alert>
)}
```

**Helper Function:**
```tsx
const getSeasonalPatternLabel = (pattern: string, months: number[]): string => {
  const PATTERN_LABELS: Record<string, string> = {
    'SUMMER': 'März-Oktober',
    'WINTER': 'Dezember-März',
    'SPRING': 'März-Mai',
    'AUTUMN': 'September-November',
    'CHRISTMAS': 'November-Dezember',
    'CUSTOM': months?.map(m => MONTH_NAMES[m-1]).join(', ') || 'Benutzerdefiniert'
  };

  return PATTERN_LABELS[pattern] || 'Unbekannt';
};
```

**Tests:**
- Component Test: Seasonal indicator renders for seasonal businesses
- Component Test: Pattern label helper function
- Integration Test: CustomerDetailPage fetches seasonal fields

---

### **7.5 Dashboard - Seasonal Widget**

**Datei:** `frontend/src/pages/SalesCockpit.tsx`

**Status:** 📋 PLANNING (Enhancement)

**Implementierung:**
```tsx
// Seasonal Paused Widget
<Grid item xs={12} md={3}>
  <MetricCard
    title="Saisonal Pausiert"
    value={metrics.seasonalPaused}
    subtitle="Außerhalb Saison (normal)"
    icon={<NaturePeopleIcon />}
    color="info"
    tooltip="Saisonbetriebe die aktuell nicht aktiv sind (kein Grund zur Sorge!)"
  />
</Grid>
```

**Tests:**
- Component Test: Seasonal Paused MetricCard renders
- Integration Test: Dashboard fetches seasonalPaused metric

---

## 📊 TEST SUMMARY

**Total Tests: 50**

**Backend Tests: 34**
- Migration V10032: 5 Tests
- LeadConvertService: 3 Tests
- OpportunityService.handleOpportunityWon(): 8 Tests
- CustomerResource.activateCustomer(): 5 Tests
- CustomerMetrics: 6 Tests (3 original + 3 seasonal)
- XentralOrderEventHandler: 2 Tests
- ChurnDetectionService: 5 Tests

**Frontend Tests: 16**
- CustomerDetailPage (PROSPECT Alert): 3 Tests
- Activation Dialog: 3 Tests
- Dashboard PROSPECT Widget: 2 Tests
- Seasonal Business Indicator: 3 Tests
- Dashboard Seasonal Widget: 2 Tests
- Integration Tests: 3 Tests

---

## 🔗 RELATED DOCUMENTATION

**Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_4_DESIGN_DECISIONS.md`

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

**Related Sprints:**
- Sprint 2.1.7.3: Customer → Opportunity Workflow (COMPLETE)
- Sprint 2.1.7.6: Customer Lifecycle Management (PROSPECT Lifecycle, Advanced Seasonal Patterns)
- Sprint 2.1.7.2: Xentral Integration (Automatic Activation via Webhook)

---

**✅ SPEC STATUS: 📋 COMPLETE - Ready for Implementation**

**Letzte Aktualisierung:** 2025-10-19 (Initial Creation)
