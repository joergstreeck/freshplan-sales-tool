# Sprint 2.1.7.2 - Technical Specification

**Sprint-ID:** 2.1.7.2
**Created:** 2025-10-19
**Status:** 📋 PLANNING
**Owner:** Claude

---

## 📋 ÜBERSICHT

Technische Spezifikation für Sprint 2.1.7.2 - Customer-Management + Xentral-Integration.

**Deliverables:**
1. Opportunity → Customer Conversion (ConvertToCustomerDialog)
2. Xentral-API-Client Implementation
3. Customer-Dashboard mit echten Daten
4. Churn-Alarm Konfiguration
5. Admin-UI für Xentral-Einstellungen
6. Sales-Rep Mapping Auto-Sync + Admin-UI
7. Testing & Bugfixes
8. Xentral Webhook Integration (Sprint 2.1.7.4 Synergy)

**Design System:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

**Related:** Sprint 2.1.7.4 (Customer Status Architecture - REQUIRED!)

---

## 📑 TABLE OF CONTENTS

**Quick Navigation:**

1. [Opportunity → Customer Conversion](#1️⃣-opportunity--customer-conversion)
   - [1.1 ConvertToCustomerDialog Component](#11-converttocustomerdialog-component)
   - [1.2 API Integration](#12-api-integration)

2. [Xentral-API-Client Implementation](#2️⃣-xentral-api-client-implementation)
   - [2.1 XentralApiClient Service](#21-xentralapiclient-service)
   - [2.2 DTOs & Responses](#22-dtos--responses)

3. [Customer-Dashboard mit echten Daten](#3️⃣-customer-dashboard-mit-echten-daten)
   - [3.1 Backend: RevenueMetrics Service](#31-backend-revenuemetrics-service)
   - [3.2 Frontend: CustomerDetailPage](#32-frontend-customerdetailpage)
   - [3.3 RevenueMetricsWidget Component](#33-revenuemetricswidget-component)
   - [3.4 PaymentBehaviorIndicator](#34-paymentbehaviorindicator)

4. [Churn-Alarm Konfiguration](#4️⃣-churn-alarm-konfiguration)
   - [4.1 Migration: churn_threshold_days](#41-migration-churn_threshold_days)
   - [4.2 Frontend: Edit Customer Dialog](#42-frontend-edit-customer-dialog)

5. [Admin-UI für Xentral-Einstellungen](#5️⃣-admin-ui-für-xentral-einstellungen)
   - [5.1 Backend: XentralSettingsResource](#51-backend-xentralsettingsresource)
   - [5.2 Frontend: XentralSettingsPage](#52-frontend-xentralsettingspage)

6. [Sales-Rep Mapping Auto-Sync](#6️⃣-sales-rep-mapping-auto-sync)
   - [6.1 SalesRepSyncJob](#61-salesrepsyncjob)
   - [6.2 Admin-UI: UserManagementPage](#62-admin-ui-usermanagementpage)

7. [Xentral Webhook Integration](#7️⃣-xentral-webhook-integration)
   - [7.1 XentralOrderEventHandlerImpl](#71-xentralordereventhandlerimpl)
   - [7.2 XentralWebhookResource](#72-xentralwebhookresource)

8. [Test Summary](#-test-summary)

---

## 1️⃣ Opportunity → Customer Conversion

### **1.1 ConvertToCustomerDialog Component**

**Datei:** `frontend/src/features/opportunity/components/ConvertToCustomerDialog.tsx`

**Status:** 📋 PLANNING

**Implementierung:**
```tsx
import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Autocomplete,
  Alert,
  AlertTitle,
  Typography,
  Box,
  Stack
} from '@mui/material';
import { HourglassEmptyIcon, CheckCircleIcon } from '@mui/icons-material';
import { useSnackbar } from 'notistack';
import { useNavigate } from 'react-router-dom';
import { httpClient } from '../../../lib/httpClient';

/**
 * Convert Opportunity to Customer Dialog
 *
 * Sprint 2.1.7.2: Xentral Integration
 * Sprint 2.1.7.4: PROSPECT Status Integration
 *
 * Features:
 * - Xentral-Kunden-Dropdown (verkäufer-gefiltert)
 * - PROSPECT Status Info-Box
 * - Automatic Status handling (Sprint 2.1.7.4)
 */
interface ConvertToCustomerDialogProps {
  open: boolean;
  onClose: () => void;
  opportunity: Opportunity;
}

interface XentralCustomerDTO {
  xentralId: string;
  companyName: string;
  totalRevenue: number;
}

export const ConvertToCustomerDialog: React.FC<ConvertToCustomerDialogProps> = ({
  open,
  onClose,
  opportunity
}) => {
  const { enqueueSnackbar } = useSnackbar();
  const navigate = useNavigate();
  const currentUser = useCurrentUser();

  // Form State
  const [companyName, setCompanyName] = useState(opportunity.name || '');
  const [selectedXentralCustomer, setSelectedXentralCustomer] = useState<XentralCustomerDTO | null>(null);
  const [notes, setNotes] = useState('');
  const [loading, setLoading] = useState(false);

  // Xentral Customers
  const [xentralCustomers, setXentralCustomers] = useState<XentralCustomerDTO[]>([]);
  const [loadingCustomers, setLoadingCustomers] = useState(false);

  // Load Xentral Customers (verkäufer-gefiltert)
  useEffect(() => {
    if (open && currentUser.xentralSalesRepId) {
      loadXentralCustomers();
    }
  }, [open, currentUser.xentralSalesRepId]);

  const loadXentralCustomers = async () => {
    setLoadingCustomers(true);
    try {
      const response = await httpClient.get(
        `/api/xentral/customers?salesRepId=${currentUser.xentralSalesRepId}`
      );
      setXentralCustomers(response.data);
    } catch (error) {
      console.error('Failed to load Xentral customers:', error);
      enqueueSnackbar('Fehler beim Laden der Xentral-Kunden', { variant: 'error' });
    } finally {
      setLoadingCustomers(false);
    }
  };

  const handleConvert = async () => {
    setLoading(true);
    try {
      const response = await httpClient.post(
        `/api/opportunities/${opportunity.id}/convert-to-customer`,
        {
          companyName: companyName,
          xentralCustomerId: selectedXentralCustomer?.xentralId,
          notes: notes
        }
      );

      const customer = response.data;

      enqueueSnackbar(
        `Customer "${customer.companyName}" erfolgreich angelegt (Status: PROSPECT)`,
        { variant: 'success' }
      );

      onClose();
      navigate(`/customers/${customer.id}`);

    } catch (error) {
      console.error('Failed to convert opportunity:', error);
      enqueueSnackbar('Fehler beim Anlegen des Customers', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <CheckCircleIcon color="success" />
          Opportunity zu Customer konvertieren
        </Box>
      </DialogTitle>

      <DialogContent>
        {/* Company Name */}
        <TextField
          fullWidth
          label="Firmenname"
          value={companyName}
          onChange={(e) => setCompanyName(e.target.value)}
          required
          sx={{ mb: 2 }}
        />

        {/* Xentral-Kunden-Dropdown */}
        <Autocomplete
          options={xentralCustomers}
          loading={loadingCustomers}
          getOptionLabel={(option) =>
            `${option.companyName} (${option.xentralId})`
          }
          renderInput={(params) => (
            <TextField
              {...params}
              label="Xentral-Kunde verknüpfen (optional)"
              helperText="Wähle einen bestehenden Xentral-Kunden oder lasse leer für manuelles Verknüpfen später"
            />
          )}
          renderOption={(props, option) => (
            <Box component="li" {...props}>
              <Stack>
                <Typography variant="body1">
                  {option.companyName}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Xentral-ID: {option.xentralId} • Umsatz: {option.totalRevenue.toLocaleString()}€
                </Typography>
              </Stack>
            </Box>
          )}
          onChange={(event, value) => setSelectedXentralCustomer(value)}
          filterOptions={(options, { inputValue }) => {
            // Suche in Company-Name UND Xentral-ID
            return options.filter(option =>
              option.companyName.toLowerCase().includes(inputValue.toLowerCase()) ||
              option.xentralId.toLowerCase().includes(inputValue.toLowerCase())
            );
          }}
          sx={{ mb: 2 }}
        />

        {/* Info-Box: Xentral-Kunde gewählt */}
        {selectedXentralCustomer && (
          <Alert severity="info" sx={{ mb: 2 }}>
            Nach Anlage werden Umsatzdaten von Xentral-Kunde
            "{selectedXentralCustomer.companyName}" angezeigt.
          </Alert>
        )}

        {/* Info-Box: KEIN Xentral-Kunde gewählt */}
        {!selectedXentralCustomer && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            Ohne Xentral-Verknüpfung können keine Umsatzdaten angezeigt werden.
            Du kannst die Verknüpfung später im Customer-Detail-Bereich vornehmen.
          </Alert>
        )}

        {/* Info-Box: PROSPECT Status (Sprint 2.1.7.4 Integration) */}
        <Alert severity="info" icon={<HourglassEmptyIcon />} sx={{ mb: 2 }}>
          <AlertTitle>Customer Status: PROSPECT</AlertTitle>
          <Typography variant="body2">
            Der neue Customer wird mit Status <strong>PROSPECT</strong> angelegt.
            <br />
            Status wechselt automatisch zu <strong>AKTIV</strong> bei erster Xentral-Bestellung.
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
            ℹ️ Siehe Sprint 2.1.7.4: Customer Status Architecture
          </Typography>
        </Alert>

        {/* Notizen */}
        <TextField
          fullWidth
          label="Notizen (optional)"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          multiline
          rows={3}
        />
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onClose} disabled={loading}>
          Abbrechen
        </Button>
        <Button
          variant="contained"
          color="primary"
          onClick={handleConvert}
          disabled={loading || !companyName}
          startIcon={<CheckCircleIcon />}
        >
          Customer anlegen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

**Tests:**
- Component Test: Dialog renders correctly
- Component Test: Company name validation
- Component Test: Xentral customers loading
- Component Test: Autocomplete filtering (Company-Name + Xentral-ID)
- Integration Test: API call to /convert-to-customer

---

### **1.2 API Integration**

**Backend:** Bereits vorhanden in Sprint 2.1.7.1!

**Endpoint:** `POST /api/opportunities/{id}/convert-to-customer`

**Request:**
```json
{
  "companyName": "Restaurant Silbertanne",
  "xentralCustomerId": "12345",
  "notes": "Converted from Opportunity"
}
```

**Response:**
```json
{
  "id": "uuid",
  "companyName": "Restaurant Silbertanne",
  "status": "PROSPECT",
  "xentralCustomerId": "12345",
  "originalLeadId": "lead-uuid",
  "createdAt": "2025-10-19T10:00:00Z"
}
```

---

## 2️⃣ Xentral-API-Client Implementation

### **2.1 XentralApiClient Service**

**Datei:** `backend/src/main/java/de/freshplan/infrastructure/xentral/XentralApiClient.java`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```java
package de.freshplan.infrastructure.xentral;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.arc.Arc;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Xentral API Client
 *
 * Sprint 2.1.7.2: Xentral Integration
 *
 * Features:
 * - REST Client für Xentral APIs
 * - Feature-Flag: mock-mode (Hybrid-Ansatz)
 * - Error Handling (Fallback zu Mock-Daten)
 *
 * Endpoints:
 * - GET /api/v1/customers?salesRepId={id}
 * - GET /api/v1/invoices?customerId={id}
 * - GET /api/v1/customers/{id}/payment-summary
 * - GET /api/v1/sales-reps
 */
@ApplicationScoped
@RegisterRestClient(configKey = "xentral-api")
public interface XentralApiClient {

    /**
     * Get Customers by Sales Rep ID
     *
     * @param salesRepId Sales Rep ID
     * @return List of Xentral Customers
     */
    @GET
    @Path("/api/v1/customers")
    @Produces(MediaType.APPLICATION_JSON)
    List<XentralCustomerDTO> getCustomersBySalesRep(
        @QueryParam("salesRepId") String salesRepId
    );

    /**
     * Get Invoices by Customer ID
     *
     * @param customerId Xentral Customer ID
     * @return List of Invoices
     */
    @GET
    @Path("/api/v1/invoices")
    @Produces(MediaType.APPLICATION_JSON)
    List<XentralInvoiceDTO> getInvoicesByCustomer(
        @QueryParam("customerId") String customerId
    );

    /**
     * Get Payment Summary by Customer ID
     *
     * @param customerId Xentral Customer ID
     * @return Payment Summary
     */
    @GET
    @Path("/api/v1/customers/{customerId}/payment-summary")
    @Produces(MediaType.APPLICATION_JSON)
    XentralPaymentSummaryDTO getPaymentSummary(
        @PathParam("customerId") String customerId
    );

    /**
     * Get All Sales Reps (for Auto-Sync)
     *
     * @return List of Sales Reps
     */
    @GET
    @Path("/api/v1/sales-reps")
    @Produces(MediaType.APPLICATION_JSON)
    List<XentralSalesRepDTO> getAllSalesReps();
}

/**
 * Xentral API Service (mit Feature-Flag)
 *
 * Wraps XentralApiClient mit Mock-Mode Support
 */
@ApplicationScoped
public class XentralApiService {

    private static final Logger logger = LoggerFactory.getLogger(XentralApiService.class);

    @Inject
    @RestClient
    XentralApiClient realClient;

    @Inject
    MockXentralApiClient mockClient;

    @ConfigProperty(name = "xentral.api.mock-mode", defaultValue = "true")
    boolean mockMode;

    public List<XentralCustomerDTO> getCustomersBySalesRep(String salesRepId) {
        if (mockMode) {
            logger.debug("Using Mock Xentral API (mock-mode=true)");
            return mockClient.getCustomersBySalesRep(salesRepId);
        }

        try {
            return realClient.getCustomersBySalesRep(salesRepId);
        } catch (Exception e) {
            logger.error("Xentral API call failed - falling back to mock data", e);
            return mockClient.getCustomersBySalesRep(salesRepId);
        }
    }

    // Similar methods for other endpoints...
}
```

**application.properties:**
```properties
# Xentral API Configuration
xentral.api.mock-mode=true
xentral.api.base-url=https://xentral.example.com
xentral.api.token=YOUR_TOKEN_HERE

# Quarkus REST Client
quarkus.rest-client.xentral-api.url=${xentral.api.base-url}
quarkus.rest-client.xentral-api.headers.Authorization=Bearer ${xentral.api.token}
```

**Tests:**
- Unit Test: Mock-Mode enabled → returns mock data
- Unit Test: Mock-Mode disabled → calls real API
- Unit Test: Real API fails → fallback to mock data
- Integration Test: XentralApiService with real client

---

### **2.2 DTOs & Responses**

```java
/**
 * Xentral Customer DTO
 */
public record XentralCustomerDTO(
    String xentralId,
    String companyName,
    double totalRevenue
) {}

/**
 * Xentral Invoice DTO
 */
public record XentralInvoiceDTO(
    String invoiceNumber,
    String customerId,
    double amount,
    LocalDate invoiceDate,
    LocalDate dueDate,
    String status  // PAID, PENDING, OVERDUE
) {}

/**
 * Xentral Payment Summary DTO
 */
public record XentralPaymentSummaryDTO(
    String customerId,
    double totalRevenue,
    double paidAmount,
    double pendingAmount,
    double overdueAmount,
    double averageDaysToPay,
    String paymentBehavior  // EXCELLENT, GOOD, WARNING, CRITICAL
) {}

/**
 * Xentral Sales Rep DTO
 */
public record XentralSalesRepDTO(
    String id,
    String name,
    String email
) {}
```

---

## 3️⃣ Customer-Dashboard mit echten Daten

### **3.1 Backend: RevenueMetrics Service**

**Datei:** `backend/src/main/java/de/freshplan/modules/customers/service/RevenueMetricsService.java`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```java
/**
 * Revenue Metrics Service
 *
 * Sprint 2.1.7.2: Xentral Integration
 *
 * Calculates revenue metrics from Xentral data
 */
@ApplicationScoped
public class RevenueMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(RevenueMetricsService.class);

    @Inject
    XentralApiService xentralApiService;

    /**
     * Get Revenue Metrics for Customer
     *
     * @param customerId Customer UUID
     * @return Revenue Metrics (30/90/365 days)
     */
    public RevenueMetrics getRevenueMetrics(UUID customerId) {
        // 1. Find Customer
        Customer customer = Customer.findById(customerId);
        if (customer == null) {
            throw new NotFoundException("Customer not found: " + customerId);
        }

        // 2. If no Xentral-ID → return empty metrics
        if (customer.getXentralCustomerId() == null) {
            logger.debug("Customer {} has no Xentral-ID - returning empty metrics", customerId);
            return RevenueMetrics.empty();
        }

        // 3. Get Invoices from Xentral
        List<XentralInvoiceDTO> invoices = xentralApiService.getInvoicesByCustomer(
            customer.getXentralCustomerId()
        );

        // 4. Calculate Metrics
        LocalDate now = LocalDate.now();
        LocalDate date30 = now.minusDays(30);
        LocalDate date90 = now.minusDays(90);
        LocalDate date365 = now.minusDays(365);

        double revenue30Days = invoices.stream()
            .filter(inv -> inv.invoiceDate().isAfter(date30))
            .mapToDouble(XentralInvoiceDTO::amount)
            .sum();

        double revenue90Days = invoices.stream()
            .filter(inv -> inv.invoiceDate().isAfter(date90))
            .mapToDouble(XentralInvoiceDTO::amount)
            .sum();

        double revenue365Days = invoices.stream()
            .filter(inv -> inv.invoiceDate().isAfter(date365))
            .mapToDouble(XentralInvoiceDTO::amount)
            .sum();

        // 5. Get Payment Summary
        XentralPaymentSummaryDTO paymentSummary = xentralApiService.getPaymentSummary(
            customer.getXentralCustomerId()
        );

        return new RevenueMetrics(
            revenue30Days,
            revenue90Days,
            revenue365Days,
            paymentSummary.paymentBehavior(),
            paymentSummary.averageDaysToPay()
        );
    }
}

/**
 * Revenue Metrics DTO
 */
public record RevenueMetrics(
    double revenue30Days,
    double revenue90Days,
    double revenue365Days,
    String paymentBehavior,  // EXCELLENT, GOOD, WARNING, CRITICAL
    double averageDaysToPay
) {
    public static RevenueMetrics empty() {
        return new RevenueMetrics(0, 0, 0, "N/A", 0);
    }
}
```

**REST Endpoint:**
```java
/**
 * CustomerResource - Revenue Metrics Endpoint
 */
@GET
@Path("/{id}/revenue-metrics")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
public Response getRevenueMetrics(@PathParam("id") UUID customerId) {
    RevenueMetrics metrics = revenueMetricsService.getRevenueMetrics(customerId);
    return Response.ok(metrics).build();
}
```

**Tests:**
- Unit Test: Calculate revenue for 30/90/365 days
- Unit Test: Customer without Xentral-ID → empty metrics
- Integration Test: GET /api/customers/{id}/revenue-metrics

---

### **3.2 Frontend: CustomerDetailPage**

**Datei:** `frontend/src/pages/CustomerDetailPage.tsx`

**Status:** 📋 ENHANCEMENT

**Implementierung:**
```tsx
// Add Revenue Metrics Section
const { data: revenueMetrics, isLoading: loadingMetrics } = useQuery(
  ['customer-revenue-metrics', customer.id],
  () => httpClient.get(`/api/customers/${customer.id}/revenue-metrics`),
  { enabled: !!customer.xentralCustomerId }
);

// Render Revenue Cards
{customer.xentralCustomerId && (
  <Box sx={{ mb: 3 }}>
    <Typography variant="h6" gutterBottom>
      Umsatzdaten (Xentral)
    </Typography>

    {loadingMetrics ? (
      <CircularProgress />
    ) : (
      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <RevenueMetricsWidget
            title="Umsatz (30 Tage)"
            value={revenueMetrics.revenue30Days}
            color="primary"
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <RevenueMetricsWidget
            title="Umsatz (90 Tage)"
            value={revenueMetrics.revenue90Days}
            color="info"
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <RevenueMetricsWidget
            title="Umsatz (365 Tage)"
            value={revenueMetrics.revenue365Days}
            color="success"
          />
        </Grid>
      </Grid>
    )}

    {/* Payment Behavior Indicator */}
    <Box sx={{ mt: 2 }}>
      <PaymentBehaviorIndicator
        behavior={revenueMetrics.paymentBehavior}
        averageDaysToPay={revenueMetrics.averageDaysToPay}
      />
    </Box>

    {/* Churn Risk Alert */}
    <ChurnRiskAlert customer={customer} />
  </Box>
)}

{!customer.xentralCustomerId && (
  <Alert severity="warning" sx={{ mb: 3 }}>
    Keine Xentral-Verknüpfung vorhanden.
    Umsatzdaten können nicht angezeigt werden.
  </Alert>
)}
```

---

### **3.3 RevenueMetricsWidget Component**

**Datei:** `frontend/src/features/customers/components/RevenueMetricsWidget.tsx`

```tsx
interface RevenueMetricsWidgetProps {
  title: string;
  value: number;
  color: 'primary' | 'info' | 'success';
}

export const RevenueMetricsWidget: React.FC<RevenueMetricsWidgetProps> = ({
  title,
  value,
  color
}) => {
  return (
    <Paper sx={{ p: 2, textAlign: 'center' }}>
      <Typography variant="caption" color="text.secondary">
        {title}
      </Typography>
      <Typography variant="h4" color={color} sx={{ mt: 1 }}>
        {value.toLocaleString('de-DE', {
          style: 'currency',
          currency: 'EUR',
          minimumFractionDigits: 0
        })}
      </Typography>
    </Paper>
  );
};
```

---

### **3.4 PaymentBehaviorIndicator**

**Datei:** `frontend/src/features/customers/components/PaymentBehaviorIndicator.tsx`

```tsx
interface PaymentBehaviorIndicatorProps {
  behavior: string; // EXCELLENT, GOOD, WARNING, CRITICAL
  averageDaysToPay: number;
}

export const PaymentBehaviorIndicator: React.FC<PaymentBehaviorIndicatorProps> = ({
  behavior,
  averageDaysToPay
}) => {
  const getColor = () => {
    switch (behavior) {
      case 'EXCELLENT': return 'success';
      case 'GOOD': return 'info';
      case 'WARNING': return 'warning';
      case 'CRITICAL': return 'error';
      default: return 'default';
    }
  };

  const getIcon = () => {
    switch (behavior) {
      case 'EXCELLENT': return '🟢';
      case 'GOOD': return '🟡';
      case 'WARNING': return '🟠';
      case 'CRITICAL': return '🔴';
      default: return '⚪';
    }
  };

  return (
    <Alert severity={getColor()} icon={getIcon()}>
      <AlertTitle>Zahlungsverhalten: {behavior}</AlertTitle>
      <Typography variant="body2">
        Durchschnittliche Zahlungsdauer: {averageDaysToPay.toFixed(0)} Tage
      </Typography>
    </Alert>
  );
};
```

---

## 4️⃣ Churn-Alarm Konfiguration

### **4.1 Migration: churn_threshold_days**

**Datei:** `backend/src/main/resources/db/migration/V10032__add_churn_threshold_days.sql`

```sql
-- Add churn_threshold_days column to customers table
ALTER TABLE customers
ADD COLUMN churn_threshold_days INTEGER DEFAULT 90;

COMMENT ON COLUMN customers.churn_threshold_days IS 'Days without order before churn alarm (default: 90, range: 14-365)';

-- Constraint: 14-365 days
ALTER TABLE customers
ADD CONSTRAINT churn_threshold_days_check
CHECK (churn_threshold_days BETWEEN 14 AND 365);
```

---

### **4.2 Frontend: Edit Customer Dialog**

**Datei:** `frontend/src/features/customers/components/EditCustomerDialog.tsx`

```tsx
// Add Churn Threshold Field
<TextField
  fullWidth
  type="number"
  label="Churn-Alarm Schwelle (Tage)"
  value={churnThresholdDays}
  onChange={(e) => setChurnThresholdDays(Number(e.target.value))}
  helperText="Tage ohne Bestellung bevor Churn-Alarm ausgelöst wird (14-365 Tage)"
  InputProps={{
    inputProps: { min: 14, max: 365 }
  }}
  sx={{ mt: 2 }}
/>
```

---

## 5️⃣ Admin-UI für Xentral-Einstellungen

### **5.1 Backend: XentralSettingsResource**

**Datei:** `backend/src/main/java/de/freshplan/modules/admin/api/XentralSettingsResource.java`

```java
@Path("/api/admin/xentral")
@ApplicationScoped
public class XentralSettingsResource {

    @PUT
    @Path("/settings")
    @RolesAllowed("ADMIN")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSettings(XentralSettingsDTO settings) {
        // Save to database or config
        return Response.ok().build();
    }

    @GET
    @Path("/test-connection")
    @RolesAllowed("ADMIN")
    public Response testConnection() {
        try {
            xentralApiService.getAllSalesReps();
            return Response.ok(Map.of("status", "success")).build();
        } catch (Exception e) {
            return Response.status(500)
                .entity(Map.of("status", "error", "message", e.getMessage()))
                .build();
        }
    }
}
```

---

### **5.2 Frontend: XentralSettingsPage**

**Datei:** `frontend/src/pages/admin/XentralSettingsPage.tsx`

```tsx
export const XentralSettingsPage: React.FC = () => {
  const [apiUrl, setApiUrl] = useState('');
  const [apiToken, setApiToken] = useState('');
  const [mockMode, setMockMode] = useState(true);

  const handleSave = async () => {
    await httpClient.put('/api/admin/xentral/settings', {
      apiUrl,
      apiToken,
      mockMode
    });
  };

  const handleTestConnection = async () => {
    const result = await httpClient.get('/api/admin/xentral/test-connection');
    // Show result
  };

  return (
    <Container maxWidth="md">
      <Typography variant="h4" gutterBottom>
        Xentral-Einstellungen
      </Typography>

      <TextField
        fullWidth
        label="API URL"
        value={apiUrl}
        onChange={(e) => setApiUrl(e.target.value)}
        sx={{ mb: 2 }}
      />

      <TextField
        fullWidth
        type="password"
        label="API Token"
        value={apiToken}
        onChange={(e) => setApiToken(e.target.value)}
        sx={{ mb: 2 }}
      />

      <FormControlLabel
        control={
          <Switch
            checked={mockMode}
            onChange={(e) => setMockMode(e.target.checked)}
          />
        }
        label="Mock-Mode (Development)"
      />

      <Box sx={{ mt: 3 }}>
        <Button variant="outlined" onClick={handleTestConnection}>
          Verbindung testen
        </Button>
        <Button variant="contained" onClick={handleSave} sx={{ ml: 2 }}>
          Speichern
        </Button>
      </Box>
    </Container>
  );
};
```

---

## 6️⃣ Sales-Rep Mapping Auto-Sync

### **6.1 SalesRepSyncJob**

**Datei:** `backend/src/main/java/de/freshplan/infrastructure/jobs/SalesRepSyncJob.java`

```java
@ApplicationScoped
public class SalesRepSyncJob {

    @Inject
    XentralApiService xentralApiService;

    @Inject
    UserRepository userRepository;

    /**
     * Sync Sales-Rep-IDs from Xentral
     * Runs daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncSalesRepIds() {
        logger.info("Starting Sales-Rep sync from Xentral...");

        try {
            List<XentralSalesRepDTO> salesReps = xentralApiService.getAllSalesReps();
            logger.info("Fetched {} sales reps from Xentral", salesReps.size());

            int syncedCount = 0;
            int unmatchedCount = 0;

            for (XentralSalesRepDTO salesRep : salesReps) {
                Optional<User> userOpt = userRepository.findByEmail(salesRep.email());

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setXentralSalesRepId(salesRep.id());
                    userRepository.persist(user);

                    logger.info("Synced: {} → {}", user.getEmail(), salesRep.id());
                    syncedCount++;
                } else {
                    logger.warn("Unmatched Xentral Sales Rep: {} ({})",
                        salesRep.email(), salesRep.id());
                    unmatchedCount++;
                }
            }

            logger.info("Sales-Rep sync completed: {} synced, {} unmatched",
                syncedCount, unmatchedCount);

        } catch (Exception e) {
            logger.error("Sales-Rep sync failed", e);
        }
    }
}
```

---

### **6.2 Admin-UI: UserManagementPage**

**Datei:** `frontend/src/pages/admin/UserManagementPage.tsx`

```tsx
// Add column to User Table
<TableHead>
  <TableRow>
    <TableCell>Name</TableCell>
    <TableCell>Email</TableCell>
    <TableCell>Role</TableCell>
    <TableCell>Xentral Sales-Rep ID</TableCell> {/* NEU! */}
    <TableCell>Actions</TableCell>
  </TableRow>
</TableHead>

<TableBody>
  {users.map(user => (
    <TableRow key={user.id}>
      <TableCell>{user.name}</TableCell>
      <TableCell>{user.email}</TableCell>
      <TableCell>{user.role}</TableCell>
      <TableCell>
        {user.xentralSalesRepId || (
          <Typography color="text.secondary">-</Typography>
        )}
      </TableCell>
      <TableCell>...</TableCell>
    </TableRow>
  ))}
</TableBody>
```

---

## 7️⃣ Xentral Webhook Integration

### **7.1 XentralOrderEventHandlerImpl**

**Datei:** `backend/src/main/java/de/freshplan/modules/xentral/service/XentralOrderEventHandlerImpl.java`

**Status:** 📋 PLANNING (Sprint 2.1.7.4 Interface Implementation)

**Implementierung:**
```java
/**
 * Xentral Order Event Handler Implementation
 *
 * Sprint 2.1.7.2: Automatic PROSPECT → AKTIV activation
 * Sprint 2.1.7.4: Interface definition (XentralOrderEventHandler)
 */
@ApplicationScoped
public class XentralOrderEventHandlerImpl implements XentralOrderEventHandler {

    @Inject
    CustomerService customerService;

    @Override
    public void handleOrderDelivered(
        String xentralCustomerId,
        String orderNumber,
        LocalDate deliveryDate
    ) {
        logger.info("Xentral Order Delivered: customerId={}, order={}",
            xentralCustomerId, orderNumber);

        try {
            // Find Customer by Xentral-ID
            Optional<Customer> customerOpt = Customer.find(
                "xentralCustomerId = ?1",
                xentralCustomerId
            ).firstResultOptional();

            if (customerOpt.isEmpty()) {
                logger.warn("Customer not found for Xentral-ID: {}", xentralCustomerId);
                return;
            }

            Customer customer = customerOpt.get();

            // Activate PROSPECT customers (Sprint 2.1.7.4 Logic!)
            if (customer.getStatus() == CustomerStatus.PROSPECT) {
                customerService.activateCustomer(
                    customer.getId(),
                    orderNumber,
                    "Activated via Xentral Order Delivered Event"
                );

                logger.info("Customer {} activated: PROSPECT → AKTIV",
                    customer.getId());
            }

        } catch (Exception e) {
            logger.error("Failed to handle Order Delivered Event", e);
        }
    }
}
```

---

### **7.2 XentralWebhookResource**

**Datei:** `backend/src/main/java/de/freshplan/modules/xentral/api/XentralWebhookResource.java`

```java
@Path("/api/xentral/webhook")
@ApplicationScoped
public class XentralWebhookResource {

    @Inject
    XentralOrderEventHandler orderEventHandler;

    @POST
    @Path("/order-delivered")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleOrderDelivered(XentralOrderDeliveredEvent event) {
        logger.info("Received Xentral Webhook: Order Delivered - {}", event);

        try {
            orderEventHandler.handleOrderDelivered(
                event.customerId(),
                event.orderNumber(),
                event.deliveryDate()
            );

            return Response.ok(Map.of("status", "success")).build();

        } catch (Exception e) {
            logger.error("Failed to process webhook", e);
            return Response.status(500)
                .entity(Map.of("error", "Internal server error"))
                .build();
        }
    }
}

public record XentralOrderDeliveredEvent(
    String customerId,
    String orderNumber,
    LocalDate deliveryDate
) {}
```

---

## 📊 TEST SUMMARY

**Total Tests: 72**

**Backend Tests: 46**
- XentralApiClient: 10 Tests
- RevenueMetricsService: 7 Tests
- ChurnDetectionService: 5 Tests
- XentralWebhookResource: 4 Tests
- XentralSettingsResource: 6 Tests
- SalesRepSyncJob: 3 Tests
- Integration Tests: 11 Tests

**Frontend Tests: 26**
- ConvertToCustomerDialog: 5 Tests
- CustomerDetailPage: 4 Tests
- RevenueMetricsWidget: 3 Tests
- PaymentBehaviorIndicator: 2 Tests
- XentralSettingsPage: 2 Tests
- UserManagementPage: 2 Tests
- Integration Tests: 8 Tests

---

## 🔗 RELATED DOCUMENTATION

**Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md`

**Trigger Document:**
→ `/docs/planung/TRIGGER_SPRINT_2_1_7_2.md`

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

**Related Sprints:**
- Sprint 2.1.7.4: Customer Status Architecture (REQUIRED!)
- Sprint 2.1.7.1: Lead → Opportunity UI Integration (COMPLETE)

---

**✅ SPEC STATUS: 📋 COMPLETE - Ready for Implementation**

**Letzte Aktualisierung:** 2025-10-19 (Initial Creation mit Sprint 2.1.7.4 Integration)
