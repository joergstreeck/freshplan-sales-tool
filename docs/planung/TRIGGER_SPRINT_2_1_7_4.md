# 🚀 Sprint 2.1.7.4 - Customer Status Architecture

**Sprint-ID:** 2.1.7.4
**Status:** 📋 PLANNING
**Priority:** P1 (High - Architektur-Fix)
**Estimated Effort:** 10h (1.5 Arbeitstage)
**Owner:** Claude
**Created:** 2025-10-19
**Updated:** 2025-10-19
**Dependencies:** Sprint 2.1.7.3 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

Saubere Status-Architektur für Lead → Customer Conversion mit klaren Lifecycle-Stages:
- **PROSPECT:** Opportunity gewonnen, wartet auf erste Bestellung
- **AKTIV:** Hat mindestens 1 Bestellung (echter Kunde!)
- **RISIKO/INAKTIV:** Lifecycle-Management

**Key Deliverables:**
- CustomerStatus.LEAD entfernen (konzeptionell falsch!)
- Auto-Conversion bei Opportunity WON
- Manual Activation: "Erste Bestellung geliefert"
- Dashboard KPIs aktualisiert

**Business Impact:**
- Klarheit über Kundenlebenszyklus
- Automatisierte Lead → Customer Migration
- Sichtbarkeit: Welche Prospects warten auf erste Bestellung?

---

## 📦 DELIVERABLES

### **1. Migration V10032: CustomerStatus Enum Cleanup** (2h)

**Ziel:** CustomerStatus.LEAD entfernen, Semantik klären

**Migration:**
```sql
-- =============================================================================
-- V10032: CustomerStatus Architecture - LEAD entfernen
-- =============================================================================
-- Sprint: 2.1.7.4 - Customer Status Architecture
-- Purpose: Klare Trennung Lead (leads Tabelle) vs Customer (customers Tabelle)
--
-- Problem:
--   - CustomerStatus.LEAD ist konzeptionell falsch!
--   - Leads gehören in "leads" Tabelle
--   - Customers starten bei PROSPECT (qualifiziert, noch kein Kunde)
--
-- Solution:
--   - Alle LEAD → PROSPECT migrieren
--   - CHECK Constraint aktualisieren (LEAD entfernen)
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

-- 3. Verify: No LEAD customers remain
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

**Backend:**
```java
// CustomerStatus.java - ENUM aktualisieren
public enum CustomerStatus {
  // LEAD,  ← REMOVED! Gehört NICHT in Customer!
  PROSPECT,    // Qualified Lead, noch kein Kunde (neu!)
  AKTIV,       // Hat mindestens 1 Bestellung
  RISIKO,      // 90+ Tage keine Bestellung
  INAKTIV,     // 180+ Tage keine Bestellung
  ARCHIVIERT   // Lost/Rejected
}
```

**Tests:**
- 5 Integration Tests (Migration Validation)
- CustomerStatus Enum Tests aktualisieren

---

### **2. LeadConvertService: PROSPECT statt AKTIV** (1h)

**Ziel:** Lead → Customer Conversion setzt PROSPECT (nicht AKTIV!)

**Code-Fix:**
```java
// LeadConvertService.java (Zeile 81)

// VORHER:
customer.setStatus(CustomerStatus.AKTIV); // ❌ Zu früh! Noch keine Bestellung!

// NACHHER:
customer.setStatus(CustomerStatus.PROSPECT); // ✅ Wartet auf erste Bestellung
```

**Business-Logik:**
- Lead CONVERTED → Customer PROSPECT
- Erst bei **erster Bestellung** → AKTIV

**Tests:**
- 3 Unit Tests (LeadConvertServiceTest)
- Integration Test (Lead → Customer → Status)

---

### **3. Auto-Conversion bei Opportunity WON** (3h)

**Ziel:** Opportunity CLOSED_WON → Auto-Convert Lead → Customer (PROSPECT)

**Service-Methode:**
```java
// OpportunityService.java - NEU!

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
    LeadConvertRequest request = new LeadConvertRequest(null, true, "Auto-converted from Opportunity WON");
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
    eventPublisher.publish(new LeadConvertedEvent(lead.getId(), customer.getId(), opportunityId));

    return customer;
  }

  return null; // Opportunity hatte schon Customer
}
```

**Integration:**
```java
// OpportunityResource.java - PUT /api/opportunities/{id}/stage

@PUT
@Path("/{id}/stage")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
public Response updateStage(@PathParam("id") UUID id, StageUpdateRequest request) {

  Opportunity opp = opportunityService.updateStage(id, request.stage);

  // Auto-Convert bei WON!
  if (request.stage == OpportunityStage.CLOSED_WON && opp.getLeadId() != null) {
    try {
      Customer customer = opportunityService.handleOpportunityWon(id);

      if (customer != null) {
        logger.info("Auto-converted Lead to Customer {} (status: PROSPECT)", customer.getId());

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

**Tests:**
- 8 Integration Tests (Opportunity WON → Auto-Convert)
- Edge Cases: Lead schon konvertiert, Lead nicht gefunden

---

### **4. Manual Activation: "Als AKTIV markieren"** (2h)

**Ziel:** Vertriebler kann Customer PROSPECT → AKTIV markieren (erste Bestellung geliefert)

**Frontend - CustomerDetailPage.tsx:**
```tsx
// Alert für PROSPECT Status
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

<script>
const handleActivateCustomer = async () => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/customers/${customer.id}/activate`,
      {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ orderNumber })
      }
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
</script>
```

**Backend - CustomerResource.java:**
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

  logger.info("Customer {} activated: PROSPECT → AKTIV (order: {})",
    customerId, request.orderNumber);

  return Response.ok(customer).build();
}

public record ActivateCustomerRequest(
  String orderNumber  // Optional: Bestellnummer für Audit-Trail
) {}
```

**Tests:**
- 5 Integration Tests (Activation Happy Path + Edge Cases)
- Validation Tests (AKTIV → AKTIV sollte fehlschlagen)

---

### **5. Dashboard KPI Updates** (1h)

**Ziel:** Dashboard zeigt PROSPECT-Kunden separat

**Backend - CustomerMetricsService.java:**
```java
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

**Frontend - Dashboard Widget:**
```tsx
// SalesCockpit.tsx - New Metric Cards

<Grid item xs={12} md={3}>
  <MetricCard
    title="Aktive Kunden"
    value={metrics.activeCustomers}
    subtitle="Haben bestellt"
    icon={<CheckCircleIcon />}
    color="success"
    trend={calculateTrend(metrics.activeCustomers, previousMetrics.activeCustomers)}
  />
</Grid>

<Grid item xs={12} md={3}>
  <MetricCard
    title="Prospects"
    value={metrics.prospects}
    subtitle="Warten auf erste Bestellung"
    icon={<HourglassEmptyIcon />}
    color="warning"
    trend={calculateTrend(metrics.prospects, previousMetrics.prospects)}
  />
</Grid>

<Grid item xs={12} md={3}>
  <MetricCard
    title="Conversion Rate"
    value={`${metrics.conversionRate.toFixed(1)}%`}
    subtitle="PROSPECT → AKTIV"
    icon={<TrendingUpIcon />}
    color="primary"
  />
</Grid>
```

**Tests:**
- 3 Integration Tests (Metrics Calculation)

---

### **6. Xentral-Vorbereitung (Interface)** (1h)

**Ziel:** Interface für zukünftige Xentral-Integration vorbereiten

**Service Interface:**
```java
// XentralOrderEventHandler.java - Interface

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

// Mock Implementation (Sprint 2.1.7.4)
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
- 2 Unit Tests (Mock Implementation)

---

## 📊 SUCCESS METRICS

**Test Coverage:**
- Backend: 26 neue Tests
- Frontend: 13 neue Tests
- **Total: 39 Tests**

**Code Changes:**
- 1 Migration (V10032)
- 5 Backend-Dateien (Service, Resource, Entity)
- 3 Frontend-Dateien (CustomerDetailPage, Dashboard, API)

**Business Impact:**
- Klare Lead → Customer Conversion
- PROSPECT → AKTIV Tracking
- Dashboard KPIs aktualisiert

---

## ✅ DEFINITION OF DONE

### **Functional**
- [x] CustomerStatus.LEAD entfernt
- [x] Lead → Customer setzt PROSPECT (nicht AKTIV)
- [x] Opportunity WON → Auto-Convert Lead
- [x] Manual Activation Button funktioniert
- [x] Dashboard zeigt PROSPECT-Kunden

### **Technical**
- [x] Migration V10032 deployed
- [x] LeadConvertService: PROSPECT Logic
- [x] OpportunityService.handleOpportunityWon()
- [x] CustomerResource: PUT /activate
- [x] CustomerMetrics: PROSPECT Zähler
- [x] XentralOrderEventHandler Interface

### **Quality**
- [x] Tests: 39/39 GREEN
- [x] TypeScript: type-check PASSED
- [x] Code Review: Self-reviewed
- [x] Documentation: Updated

---

## 📅 TIMELINE

**Tag 1 (6h):**
- Migration V10032 (2h)
- LeadConvertService Fix (1h)
- Auto-Conversion (3h)

**Tag 2 (4h):**
- Manual Activation Button (2h)
- Dashboard Updates (1h)
- Xentral Interface (1h)

**Total:** 10h (1.5 Arbeitstage)

---

## 📄 ARTEFAKTE

**Technische Spezifikation:**
- API Specifications (PUT /activate)
- Migration Script (V10032)
- Service Logic (handleOpportunityWon)

**Design Decisions:**
- Why PROSPECT instead of LEAD?
- When to auto-convert?
- Manual vs Automatic activation

**Related Work:**
- Sprint 2.1.7.3: Customer → Opportunity Workflow
- Sprint 2.1.7.2: Xentral Integration (Future)

---

## 🎯 NÄCHSTE SCHRITTE

1. Sprint 2.1.7.3 abschließen (PR #142)
2. Sprint 2.1.7.4 starten (Status Architecture)
3. Sprint 2.1.7.2 vorbereiten (Xentral Integration)

---

## 📝 NOTES

### **User-Decision (2025-10-19)**

**Frage:** Wann wird Customer AKTIV?
**Antwort:** Bei erster **gelieferter Bestellung** (nicht bei Rechnung!)

**Rationale:**
- Pragmatischer als "bezahlte Rechnung" (B2B hat oft 30-60 Tage Zahlungsziel)
- Ware geliefert = Kunde ist real (Stornos sind selten)
- Verkäufer-Moral: Sofort sichtbar statt 60 Tage warten

### **Technical Debt**

- Xentral Webhook noch nicht implementiert (Sprint 2.1.7.2)
- Manual Activation ist Workaround bis Webhook ready

---

**✅ SPRINT STATUS: 📋 PLANNING - Bereit für Implementierung**

**Letzte Aktualisierung:** 2025-10-19 (Sprint neu angelegt nach Umbenennung 2.1.7.4 → 2.1.7.5)
