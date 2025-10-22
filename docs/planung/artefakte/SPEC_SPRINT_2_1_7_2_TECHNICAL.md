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
7. Xentral Webhook Integration (Sprint 2.1.7.4 Synergy)
8. **Unified Communication System** (NEU - Pre-Live Quality Investment)
9. **Customer UX Polish** (NEU - Multi-Contact CRUD + Pain Points)
10. **Multi-Location Vorbereitung** (NEU - Prep für Sprint 2.1.7.7)

**Design System:** `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

**Related:** Sprint 2.1.7.4 (Customer Status Architecture - REQUIRED!)

---

## 🗺️ QUICK OVERVIEW (2.590 Zeilen)

**📦 Original Deliverables (1-7):** Zeilen 82-1191
- 1️⃣ [Opportunity → Customer Conversion](#1️⃣-opportunity--customer-conversion) (Zeile 82)
- 2️⃣ [Xentral-API-Client](#2️⃣-xentral-api-client-implementation) (Zeile 346)
- 3️⃣ [Customer-Dashboard](#3️⃣-customer-dashboard-mit-echten-daten) (Zeile 542)
- 4️⃣ [Churn-Alarm](#4️⃣-churn-alarm-konfiguration) (Zeile 820)
- 5️⃣ [Admin-UI Xentral](#5️⃣-admin-ui-für-xentral-einstellungen) (Zeile 863)
- 6️⃣ [Sales-Rep Sync](#6️⃣-sales-rep-mapping-auto-sync) (Zeile 972)
- 7️⃣ [Xentral Webhook](#7️⃣-xentral-webhook-integration) (Zeile 1067)

**⭐ NEU - Deliverable 8:** Zeilen 1192-2158 (Pre-Live Quality Investment!)
- 8️⃣ [Unified Communication System](#8️⃣-unified-communication-system-neu) (Zeile 1192)
  - 8.1 [Backend: Activity Entity](#81-backend-activity-entity-polymorphisch) (Zeile 1212)
  - 8.2 [Backend: ActivityService](#82-backend-activityservice) (Zeile 1372)
  - 8.3 [Migration V10033](#83-migration-lead_activities--activities) (Zeile 1595)
  - 8.4 [Frontend: ActivityTimeline](#84-frontend-activitytimeline-component) (Zeile 1722)
  - 8.5 [Frontend: ActivityDialog](#85-frontend-activitydialog-component) (Zeile 1926)
  - 8.6 [CustomerDetailPage Integration](#86-integration-customerdetailpage) (Zeile 2069)

**⭐ NEU - Deliverable 9:** Zeilen 2159-2473 (Customer UX Polish!)
- 9️⃣ [Customer UX Polish](#9️⃣-customer-ux-polish-neu) (Zeile 2159)
  - 9.1 [Pain Points registrieren](#91-pain-points-registrieren) (Zeile 2175)
  - 9.2 [Multi-Contact Migration](#92-multi-contact-migration) (Zeile 2263)
  - 9.3 [Dashboard Contact CRUD](#93-dashboard-contact-crud) (Zeile 2337)

**⭐ NEU - Deliverable 10:** Zeilen 2474-2600 (Multi-Location Prep!)
- 🔟 [Multi-Location Vorbereitung](#🔟-multi-location-vorbereitung-neu) (Zeile 2474)
  - 10.1 [ConvertDialog UI Vorbereitung](#101-convertdialog-ui-vorbereitung) (Zeile 2490)
  - 10.2 [Wizard hierarchyType Dropdown](#102-wizard-hierarchytype-dropdown) (Zeile 2550)
  - 10.3 [Review Checklist](#103-wizarddashboard-review-checklist) (Zeile 2580)

**📊 Testing & Summary:** Zeilen 2601-2720
- 🧪 [Test Summary](#-test-summary) (Zeile 2602) - **162 Tests total**
- 🔗 [Related Documentation](#-related-documentation) (Zeile 2691)

**💡 TIP:** Nutze Strg+F um direkt zu einer Section zu springen (z.B. "8.3 Migration")

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
   - [3.5 Kundenmanagement Dashboard KPIs](#35-kundenmanagement-dashboard-kpis-integration) **(NEU - Sprint 2.1.7.4 Vorbereitung)**

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

8. [Unified Communication System](#8️⃣-unified-communication-system-neu)
   - [8.1 Backend: Activity Entity (Polymorphisch)](#81-backend-activity-entity-polymorphisch)
   - [8.2 Backend: ActivityService](#82-backend-activityservice)
   - [8.3 Migration: lead_activities → activities](#83-migration-lead_activities--activities)
   - [8.4 Frontend: ActivityTimeline Component](#84-frontend-activitytimeline-component)
   - [8.5 Frontend: ActivityDialog Component](#85-frontend-activitydialog-component)
   - [8.6 Integration: CustomerDetailPage](#86-integration-customerdetailpage)

9. [Customer UX Polish](#9️⃣-customer-ux-polish-neu)
   - [9.1 Pain Points registrieren](#91-pain-points-registrieren)
   - [9.2 Multi-Contact Migration](#92-multi-contact-migration)
   - [9.3 Dashboard Contact CRUD](#93-dashboard-contact-crud)

10. [Multi-Location Vorbereitung](#🔟-multi-location-vorbereitung-neu)
   - [10.1 ConvertDialog UI Vorbereitung](#101-convertdialog-ui-vorbereitung)
   - [10.2 Wizard hierarchyType Dropdown](#102-wizard-hierarchytype-dropdown)
   - [10.3 Wizard/Dashboard Review Checklist](#103-wizarddashboard-review-checklist)

11. [Test Summary](#-test-summary)

---

## 1️⃣ Opportunity → Customer Conversion

[↑ Back to Overview](#🗺️-quick-overview-2590-zeilen) | [→ Next: Xentral-API-Client](#2️⃣-xentral-api-client-implementation)

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

[↑ Back to Overview](#🗺️-quick-overview-2590-zeilen) | [← Prev: Conversion](#1️⃣-opportunity--customer-conversion) | [→ Next: Customer-Dashboard](#3️⃣-customer-dashboard-mit-echten-daten)

### **2.1 XentralApiClient Service**

**Datei:** `backend/src/main/java/de/freshplan/infrastructure/xentral/XentralApiClient.java`

**Status:** 📋 PLANNING (NEU!)

**⚡ API-VERSION:** **Neue Xentral API (v25.39.5 PRO)** - NICHT Legacy v1 REST API!

**Implementierung:**
```java
package de.freshplan.infrastructure.xentral;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.arc.Arc;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.time.LocalDate;

/**
 * Xentral API Client (Neue Xentral API v25.39.5)
 *
 * Sprint 2.1.7.2: Xentral Integration
 *
 * ⚡ WICHTIG: Verwendet NEUE Xentral API (nicht Legacy v1 REST API!)
 *
 * 🚨 KRITISCHE SECURITY-REGEL: NUR LESEN (READ-ONLY)!
 * ❌ KEINE POST/PUT/PATCH/DELETE Operationen erlaubt!
 * ❌ KEINE Daten in Xentral schreiben/ändern/löschen!
 * ✅ NUR GET-Requests (Daten lesen)
 *
 * Begründung (User-Requirement):
 * - PAT (Personal Access Token) hat möglicherweise WRITE-Rechte
 * - Xentral kann PAT nicht auf READ-ONLY beschränken
 * - Application-Level Security: Code darf NUR lesen!
 * - Guardrails: Pre-Commit Hook verhindert WRITE-Operations
 *
 * Features:
 * - REST Client für Xentral APIs (v25.39+)
 * - JSON:API Response Format (meta, data, links)
 * - Personal Access Token (PAT) Authentication
 * - Standardisierte Filter-Syntax: filter[field]
 * - Built-in Pagination: page[number], page[size]
 * - Feature-Flag: mock-mode (Hybrid-Ansatz)
 * - Error Handling (Fallback zu Mock-Daten)
 *
 * Endpoints (ALL READ-ONLY):
 * - GET /api/customers (filter by salesRep.id)
 * - GET /api/customers/{id} (includes financial data since 2025)
 * - GET /api/invoices (filter by customer.id)
 * - GET /api/employees (filter by role=sales)
 *
 * Documentation:
 * - https://developer.xentral.com/reference/intro
 * - OpenAPI Spec: https://github.com/xentral/api-spec-public
 */
@ApplicationScoped
@RegisterRestClient(configKey = "xentral-api")
public interface XentralApiClient {

    /**
     * List Customers (filtered by Sales Rep)
     *
     * Neue Xentral API Endpoint: GET /api/customers
     * Filter-Syntax: ?filter[salesRep.id]={id}
     *
     * Response: JSON:API Format (meta, data, links)
     *
     * @param salesRepId Xentral Employee ID (Sales Rep)
     * @param pageNumber Page number (default: 1)
     * @param pageSize Page size (default: 50)
     * @return JSON:API Response with customers
     */
    @GET
    @Path("/api/customers")
    @Produces("application/vnd.api+json")
    XentralCustomerListResponse getCustomers(
        @QueryParam("filter[salesRep.id]") String salesRepId,
        @QueryParam("page[number]") Integer pageNumber,
        @QueryParam("page[size]") Integer pageSize
    );

    /**
     * Get Single Customer (with Financial Data)
     *
     * Neue Xentral API Endpoint: GET /api/customers/{id}
     *
     * ⚡ 2025 Feature: Includes financial information!
     * - totalRevenue
     * - paymentBehavior (EXCELLENT, GOOD, WARNING, CRITICAL)
     * - averagePaymentDays
     *
     * Response: JSON:API Format
     *
     * @param customerId Xentral Customer ID
     * @return JSON:API Response with customer + financial data
     */
    @GET
    @Path("/api/customers/{id}")
    @Produces("application/vnd.api+json")
    XentralCustomerResponse getCustomer(
        @PathParam("id") String customerId
    );

    /**
     * List Invoices (filtered by Customer)
     *
     * Neue Xentral API Endpoint: GET /api/invoices
     * Filter-Syntax: ?filter[customer.id]={id}&filter[date][gte]={date}
     *
     * Response: JSON:API Format (meta, data, links)
     *
     * @param customerId Xentral Customer ID
     * @param dateFrom Filter: invoiceDate >= dateFrom (ISO 8601: YYYY-MM-DD)
     * @param pageNumber Page number (default: 1)
     * @param pageSize Page size (default: 100)
     * @return JSON:API Response with invoices
     */
    @GET
    @Path("/api/invoices")
    @Produces("application/vnd.api+json")
    XentralInvoiceListResponse getInvoices(
        @QueryParam("filter[customer.id]") String customerId,
        @QueryParam("filter[date][gte]") String dateFrom,  // ISO 8601: "2024-01-01"
        @QueryParam("page[number]") Integer pageNumber,
        @QueryParam("page[size]") Integer pageSize
    );

    /**
     * List Employees (Sales Reps)
     *
     * Neue Xentral API Endpoint: GET /api/employees
     * Filter-Syntax: ?filter[role]=sales
     *
     * Used for Sales-Rep Auto-Sync (Email-based Matching)
     *
     * Response: JSON:API Format (meta, data, links)
     *
     * @param role Filter by role (e.g., "sales")
     * @param pageNumber Page number (default: 1)
     * @param pageSize Page size (default: 100)
     * @return JSON:API Response with employees
     */
    @GET
    @Path("/api/employees")
    @Produces("application/vnd.api+json")
    XentralEmployeeListResponse getEmployees(
        @QueryParam("filter[role]") String role,
        @QueryParam("page[number]") Integer pageNumber,
        @QueryParam("page[size]") Integer pageSize
    );
}

/**
 * Xentral API Service (mit Feature-Flag + JSON:API Parsing)
 *
 * Wraps XentralApiClient mit:
 * - Mock-Mode Support (Feature-Flag)
 * - JSON:API Response Parsing (data extraction)
 * - Error Handling (Fallback zu Mock-Daten)
 * - Simplified DTOs (kein JSON:API Overhead für Business-Code)
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

    /**
     * Get Customers by Sales Rep ID
     *
     * Wraps JSON:API Response → Simple DTO List
     *
     * @param salesRepId Xentral Employee ID (Sales Rep)
     * @return List of Customers (simplified DTOs)
     */
    public List<XentralCustomerDTO> getCustomersBySalesRep(String salesRepId) {
        if (mockMode) {
            logger.debug("Using Mock Xentral API (mock-mode=true)");
            return mockClient.getCustomersBySalesRep(salesRepId);
        }

        try {
            // Call new Xentral API
            XentralCustomerListResponse response = realClient.getCustomers(
                salesRepId,
                1,    // page 1
                100   // max 100 customers
            );

            // Parse JSON:API Response → Simple DTOs
            return response.data().stream()
                .map(data -> new XentralCustomerDTO(
                    data.id(),
                    data.attributes().name(),
                    data.attributes().financial().totalRevenue()
                ))
                .toList();

        } catch (Exception e) {
            logger.error("Xentral API call failed - falling back to mock data", e);
            return mockClient.getCustomersBySalesRep(salesRepId);
        }
    }

    /**
     * Get Customer with Financial Data
     *
     * @param customerId Xentral Customer ID
     * @return Customer DTO with financial data
     */
    public XentralCustomerDTO getCustomer(String customerId) {
        if (mockMode) {
            return mockClient.getCustomer(customerId);
        }

        try {
            XentralCustomerResponse response = realClient.getCustomer(customerId);
            XentralCustomerData data = response.data();

            return new XentralCustomerDTO(
                data.id(),
                data.attributes().name(),
                data.attributes().financial().totalRevenue()
            );
        } catch (Exception e) {
            logger.error("Failed to get customer from Xentral API: {}", customerId, e);
            return mockClient.getCustomer(customerId);
        }
    }

    /**
     * Get Invoices by Customer ID
     *
     * Wraps JSON:API Response → Simple DTO List
     *
     * @param customerId Xentral Customer ID
     * @return List of Invoices (simplified DTOs)
     */
    public List<XentralInvoiceDTO> getInvoicesByCustomer(String customerId) {
        if (mockMode) {
            return mockClient.getInvoicesByCustomer(customerId);
        }

        try {
            // Get invoices from last 365 days
            LocalDate dateFrom = LocalDate.now().minusDays(365);

            XentralInvoiceListResponse response = realClient.getInvoices(
                customerId,
                dateFrom.toString(),  // "2024-01-01"
                1,    // page 1
                500   // max 500 invoices
            );

            // Parse JSON:API Response → Simple DTOs
            return response.data().stream()
                .map(data -> new XentralInvoiceDTO(
                    data.attributes().invoiceNumber(),
                    data.relationships().customer().data().id(),
                    data.attributes().totalAmount(),
                    LocalDate.parse(data.attributes().invoiceDate()),
                    LocalDate.parse(data.attributes().dueDate()),
                    data.attributes().status()
                ))
                .toList();

        } catch (Exception e) {
            logger.error("Xentral API call failed - falling back to mock data", e);
            return mockClient.getInvoicesByCustomer(customerId);
        }
    }

    /**
     * Get Payment Summary from Customer Financial Data
     *
     * Uses new GET /api/customers/{id} endpoint (includes financial data)
     *
     * @param customerId Xentral Customer ID
     * @return Payment Summary DTO
     */
    public XentralPaymentSummaryDTO getPaymentSummary(String customerId) {
        if (mockMode) {
            return mockClient.getPaymentSummary(customerId);
        }

        try {
            XentralCustomerResponse response = realClient.getCustomer(customerId);
            XentralFinancialInfo financial = response.data().attributes().financial();

            return new XentralPaymentSummaryDTO(
                customerId,
                financial.totalRevenue(),
                financial.paidAmount(),
                financial.pendingAmount(),
                financial.overdueAmount(),
                financial.averagePaymentDays(),
                financial.paymentBehavior()
            );
        } catch (Exception e) {
            logger.error("Failed to get payment summary from Xentral API: {}", customerId, e);
            return mockClient.getPaymentSummary(customerId);
        }
    }

    /**
     * Get All Sales Reps (for Auto-Sync)
     *
     * @return List of Employees (role=sales)
     */
    public List<XentralSalesRepDTO> getAllSalesReps() {
        if (mockMode) {
            return mockClient.getAllSalesReps();
        }

        try {
            XentralEmployeeListResponse response = realClient.getEmployees(
                "sales",  // filter by role
                1,        // page 1
                100       // max 100 sales reps
            );

            return response.data().stream()
                .map(data -> new XentralSalesRepDTO(
                    data.id(),
                    data.attributes().name(),
                    data.attributes().email()
                ))
                .toList();

        } catch (Exception e) {
            logger.error("Xentral API call failed - falling back to mock data", e);
            return mockClient.getAllSalesReps();
        }
    }
}
```

**application.properties:**
```properties
# Xentral API Configuration (Neue Xentral API v25.39+)
xentral.api.mock-mode=true
xentral.api.base-url=https://instance.xentral.biz
xentral.api.personal-access-token=xentral_pat_YOUR_TOKEN_HERE

# Quarkus REST Client (Personal Access Token Authentication)
quarkus.rest-client.xentral-api.url=${xentral.api.base-url}
quarkus.rest-client.xentral-api.headers.Authorization=Bearer ${xentral.api.personal-access-token}
quarkus.rest-client.xentral-api.headers.Accept=application/vnd.api+json
quarkus.rest-client.xentral-api.headers.Content-Type=application/vnd.api+json
```

**Tests:**
- Unit Test: Mock-Mode enabled → returns mock data
- Unit Test: Mock-Mode disabled → calls real API
- Unit Test: Real API fails → fallback to mock data
- Integration Test: XentralApiService with real client

---

### **2.2 DTOs & Responses (JSON:API Format)**

**⚡ Neue Xentral API verwendet JSON:API Standard (RFC 7159)**

**Response-Struktur:**
```json
{
  "meta": { "total": 42, "page": 1, "pageSize": 100 },
  "data": [ /* resources */ ],
  "links": { "self": "...", "next": "..." },
  "extra": { /* additional info */ }
}
```

**Implementation:**

```java
// ============================================================================
// 1️⃣ JSON:API RESPONSE WRAPPER (für Xentral API Responses)
// ============================================================================

/**
 * JSON:API Response for Customer List
 */
public record XentralCustomerListResponse(
    XentralMeta meta,
    List<XentralCustomerData> data,
    XentralLinks links,
    Map<String, Object> extra
) {}

/**
 * JSON:API Response for Single Customer
 */
public record XentralCustomerResponse(
    XentralMeta meta,
    XentralCustomerData data,
    XentralLinks links
) {}

/**
 * JSON:API Response for Invoice List
 */
public record XentralInvoiceListResponse(
    XentralMeta meta,
    List<XentralInvoiceData> data,
    XentralLinks links,
    Map<String, Object> extra
) {}

/**
 * JSON:API Response for Employee List
 */
public record XentralEmployeeListResponse(
    XentralMeta meta,
    List<XentralEmployeeData> data,
    XentralLinks links
) {}

// ============================================================================
// 2️⃣ JSON:API RESOURCE DATA (Customer)
// ============================================================================

/**
 * JSON:API Resource: Customer
 *
 * Struktur:
 * {
 *   "id": "12345",
 *   "type": "customer",
 *   "attributes": { ... },
 *   "relationships": { ... }
 * }
 */
public record XentralCustomerData(
    String id,
    String type,  // "customer"
    XentralCustomerAttributes attributes,
    XentralCustomerRelationships relationships
) {}

/**
 * Customer Attributes (Business Data)
 *
 * ⚡ 2025 Feature: Includes financial info!
 */
public record XentralCustomerAttributes(
    String name,
    String customerNumber,
    String email,
    String phone,
    XentralAddress address,
    XentralFinancialInfo financial  // ← 2025 NEU!
) {}

/**
 * Customer Financial Information (2025 Feature)
 *
 * Included in GET /api/customers/{id} since Xentral v25+
 */
public record XentralFinancialInfo(
    Double totalRevenue,
    Double paidAmount,
    Double pendingAmount,
    Double overdueAmount,
    Double averagePaymentDays,
    String paymentBehavior  // EXCELLENT, GOOD, WARNING, CRITICAL
) {}

/**
 * Customer Relationships (Links to related resources)
 */
public record XentralCustomerRelationships(
    XentralRelationship salesRep,  // Link to Employee
    XentralRelationship invoices   // Link to Invoice List
) {}

// ============================================================================
// 3️⃣ JSON:API RESOURCE DATA (Invoice)
// ============================================================================

/**
 * JSON:API Resource: Invoice
 */
public record XentralInvoiceData(
    String id,
    String type,  // "invoice"
    XentralInvoiceAttributes attributes,
    XentralInvoiceRelationships relationships
) {}

/**
 * Invoice Attributes (Business Data)
 */
public record XentralInvoiceAttributes(
    String invoiceNumber,
    String invoiceDate,  // ISO 8601: "2024-01-15"
    String dueDate,      // ISO 8601: "2024-02-15"
    Double totalAmount,
    String currency,     // "EUR"
    String status        // PAID, PENDING, OVERDUE
) {}

/**
 * Invoice Relationships
 */
public record XentralInvoiceRelationships(
    XentralRelationship customer,
    XentralRelationship salesOrder
) {}

// ============================================================================
// 4️⃣ JSON:API RESOURCE DATA (Employee / Sales Rep)
// ============================================================================

/**
 * JSON:API Resource: Employee
 */
public record XentralEmployeeData(
    String id,
    String type,  // "employee"
    XentralEmployeeAttributes attributes
) {}

/**
 * Employee Attributes (Business Data)
 */
public record XentralEmployeeAttributes(
    String name,
    String email,
    String role,      // "sales", "manager", "admin"
    String department
) {}

// ============================================================================
// 5️⃣ JSON:API HELPER TYPES (Meta, Links, Relationships)
// ============================================================================

/**
 * JSON:API Meta (Pagination Info)
 */
public record XentralMeta(
    Integer total,      // Total count of resources
    Integer page,       // Current page number
    Integer pageSize,   // Items per page
    Integer totalPages  // Total pages
) {}

/**
 * JSON:API Links (Pagination URLs)
 */
public record XentralLinks(
    String self,
    String first,
    String prev,
    String next,
    String last
) {}

/**
 * JSON:API Relationship (Link to related resource)
 */
public record XentralRelationship(
    XentralRelationshipData data,
    XentralLinks links
) {}

/**
 * JSON:API Relationship Data (Resource Identifier)
 */
public record XentralRelationshipData(
    String id,
    String type
) {}

/**
 * Address (nested in Customer Attributes)
 */
public record XentralAddress(
    String street,
    String zipCode,
    String city,
    String country
) {}

// ============================================================================
// 6️⃣ SIMPLIFIED DTOs (für Business-Code - KEIN JSON:API Overhead!)
// ============================================================================

/**
 * Xentral Customer DTO (Simplified for Business Code)
 *
 * Parsed from JSON:API Response → Simple POJO
 */
public record XentralCustomerDTO(
    String xentralId,
    String companyName,
    double totalRevenue
) {}

/**
 * Xentral Invoice DTO (Simplified for Business Code)
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
 * Xentral Payment Summary DTO (Simplified for Business Code)
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
 * Xentral Sales Rep DTO (Simplified for Business Code)
 */
public record XentralSalesRepDTO(
    String id,
    String name,
    String email
) {}
```

**⚡ WICHTIG: JSON:API Parsing in XentralApiService**

```java
// Example: Parse JSON:API Response → Simple DTO
XentralCustomerListResponse response = realClient.getCustomers(...);

List<XentralCustomerDTO> simpleDTOs = response.data().stream()
    .map(data -> new XentralCustomerDTO(
        data.id(),
        data.attributes().name(),
        data.attributes().financial().totalRevenue()
    ))
    .toList();

// ✅ Business-Code arbeitet mit XentralCustomerDTO (kein JSON:API Overhead!)
```

---

### **2.3 SECURITY GUARDRAILS (READ-ONLY Enforcement)**

**🚨 KRITISCHES USER-REQUIREMENT:** Xentral-Integration MUSS READ-ONLY sein!

**Problem:**
- Xentral PAT (Personal Access Token) hat möglicherweise WRITE-Rechte
- Xentral kann PAT nicht auf READ-ONLY beschränken
- **Risiko:** Versehentliche Daten-Änderungen in Xentral (Production!)

**Lösung: Multi-Layer Security**

#### **Layer 1: Code-Level (Interface Contract)**

```java
/**
 * XentralApiClient Interface
 *
 * 🚨 SECURITY: ALL methods MUST be @GET (READ-ONLY)!
 * ❌ NO @POST, @PUT, @PATCH, @DELETE allowed!
 */
@RegisterRestClient(configKey = "xentral-api")
public interface XentralApiClient {

    @GET  // ✅ READ-ONLY
    @Path("/api/customers")
    XentralCustomerListResponse getCustomers(...);

    @GET  // ✅ READ-ONLY
    @Path("/api/customers/{id}")
    XentralCustomerResponse getCustomer(...);

    // ❌ VERBOTEN - Beispiel was NICHT erlaubt ist:
    // @POST
    // @Path("/api/customers")
    // XentralCustomerResponse createCustomer(...);  // ← NICHT erlaubt!
}
```

#### **Layer 2: Pre-Commit Hook (Automated Check)**

**Datei:** `scripts/pre-commit-xentral-security-check.sh`

```bash
#!/bin/bash

# Pre-Commit Hook: Xentral READ-ONLY Security Check
# Verhindert POST/PUT/PATCH/DELETE in XentralApiClient

echo "🔒 Checking Xentral API for WRITE operations..."

XENTRAL_CLIENT="backend/src/main/java/de/freshplan/infrastructure/xentral/XentralApiClient.java"

if [ -f "$XENTRAL_CLIENT" ]; then
    # Check for forbidden HTTP methods
    if grep -E "@(POST|PUT|PATCH|DELETE)" "$XENTRAL_CLIENT"; then
        echo "❌ ERROR: XentralApiClient contains WRITE operations!"
        echo ""
        echo "🚨 SECURITY VIOLATION:"
        echo "   Xentral API MUST be READ-ONLY (only @GET allowed)!"
        echo ""
        echo "   Found forbidden annotations:"
        grep -n -E "@(POST|PUT|PATCH|DELETE)" "$XENTRAL_CLIENT"
        echo ""
        echo "   Allowed: @GET"
        echo "   Forbidden: @POST, @PUT, @PATCH, @DELETE"
        echo ""
        exit 1
    fi

    echo "✅ Xentral API is READ-ONLY (only @GET found)"
fi

exit 0
```

**Installation:**
```bash
# Make executable
chmod +x scripts/pre-commit-xentral-security-check.sh

# Add to .git/hooks/pre-commit
echo "./scripts/pre-commit-xentral-security-check.sh" >> .git/hooks/pre-commit
```

#### **Layer 3: Code Review Checkliste**

**Pull Request Template erweitern:**

```markdown
## 🔒 Security Checklist

### Xentral API Integration
- [ ] ✅ Alle XentralApiClient-Methoden sind @GET (READ-ONLY)
- [ ] ❌ Keine @POST, @PUT, @PATCH, @DELETE Annotationen
- [ ] ✅ Keine Daten-Änderungen in Xentral
- [ ] ✅ Pre-Commit Hook läuft grün
```

#### **Layer 4: Integration Tests**

**Datei:** `XentralApiClientTest.java`

```java
@Test
public void testXentralApiClient_isReadOnly() {
    // Verify: All methods in XentralApiClient use @GET annotation

    Method[] methods = XentralApiClient.class.getDeclaredMethods();

    for (Method method : methods) {
        // Check: Method has @GET annotation
        assertThat(method.isAnnotationPresent(GET.class))
            .as("Method %s must be @GET (READ-ONLY)", method.getName())
            .isTrue();

        // Check: Method does NOT have WRITE annotations
        assertThat(method.isAnnotationPresent(POST.class))
            .as("Method %s must NOT be @POST", method.getName())
            .isFalse();
        assertThat(method.isAnnotationPresent(PUT.class))
            .as("Method %s must NOT be @PUT", method.getName())
            .isFalse();
        assertThat(method.isAnnotationPresent(PATCH.class))
            .as("Method %s must NOT be @PATCH", method.getName())
            .isFalse();
        assertThat(method.isAnnotationPresent(DELETE.class))
            .as("Method %s must NOT be @DELETE", method.getName())
            .isFalse();
    }
}
```

#### **Layer 5: Documentation**

**README.md erweitern:**

```markdown
## 🚨 Xentral API Security (READ-ONLY)

**CRITICAL:** Xentral API integration is **READ-ONLY**!

### Rules:
- ✅ **Allowed:** GET requests (read data)
- ❌ **Forbidden:** POST, PUT, PATCH, DELETE (write/modify/delete data)

### Why?
- Xentral PAT has WRITE permissions
- Xentral cannot restrict PAT to READ-ONLY
- Application-level security prevents accidental data changes

### Guardrails:
1. Code-Level: XentralApiClient interface (only @GET methods)
2. Pre-Commit Hook: Automated check (blocks WRITE operations)
3. Code Review: PR checklist
4. Integration Tests: Verify READ-ONLY at runtime
```

#### **Summary: 5-Layer Security**

| Layer | Type | Protection |
|-------|------|------------|
| 1 | Code | Interface Contract (@GET only) |
| 2 | Automation | Pre-Commit Hook (blocks commit) |
| 3 | Human | Code Review Checklist |
| 4 | Runtime | Integration Tests (CI fails) |
| 5 | Documentation | README warning |

**Result:** **Zero-Tolerance** für WRITE-Operations auf Xentral API!

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

### **3.5 Kundenmanagement Dashboard KPIs (Integration)**

**Route:** `/customer-management` (`KundenmanagementDashboard.tsx`)

**Problem:** Dashboard zeigt aktuell Hardcoded Mock-Daten für alle KPIs.

**Lösung:** In Sprint 2.1.7.2 werden ALLE KPIs auf einmal an echte Backend-Daten angebunden (gemeinsam mit Xentral-Daten).

---

#### **3.5.1 Backend: CustomerManagementMetrics Service (NEU)**

**Datei:** `backend/src/main/java/de/freshplan/domain/cockpit/service/CustomerManagementMetricsService.java`

```java
@ApplicationScoped
public class CustomerManagementMetricsService {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    OpportunityRepository opportunityRepository;

    /**
     * Berechnet KPIs für Kundenmanagement Dashboard
     *
     * Sprint 2.1.7.2: Alle KPIs auf einmal anbinden
     */
    public CustomerManagementMetrics getMetrics() {
        CustomerManagementMetrics metrics = new CustomerManagementMetrics();

        // 1. Aktive Kunden (aus DashboardStatistics)
        int activeCustomers = (int) customerRepository.countByStatus(CustomerStatus.AKTIV);
        metrics.setActiveCustomers(activeCustomers);

        // 2. Neue Kunden (letzten 30 Tage)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int newCustomers = (int) customerRepository.count(
            "status = ?1 AND createdAt >= ?2",
            CustomerStatus.AKTIV, thirtyDaysAgo
        );
        metrics.setNewCustomers(newCustomers);

        // 3. Pipeline Wert (Summe aller offenen Opportunities)
        List<Opportunity> openOpportunities = opportunityRepository.list(
            "status IN ('OFFEN', 'IN_PROGRESS')"
        );
        double pipelineValue = openOpportunities.stream()
            .mapToDouble(opp -> opp.getEstimatedRevenue() != null ? opp.getEstimatedRevenue() : 0.0)
            .sum();
        metrics.setPipelineValue(pipelineValue);
        metrics.setOpportunityCount(openOpportunities.size());

        // 4. MRR (Monthly Recurring Revenue - aus Xentral)
        // TODO Sprint 2.1.7.2: Von Xentral API laden
        // double mrr = xentralApiService.calculateMRR();
        // metrics.setMrr(mrr);
        metrics.setMrr(0.0); // Placeholder

        // 5. Kundenzufriedenheit / NPS
        // TODO: Zukünftiges Feature (Customer Feedback System)
        metrics.setCustomerSatisfaction(0.0); // Placeholder
        metrics.setNpsScore(0); // Placeholder

        // 6. Durchschnittliche Bestellungen pro Kunde
        // TODO Sprint 2.1.7.2: Aus Xentral-Invoices berechnen
        metrics.setAverageOrdersPerCustomer(0.0); // Placeholder

        // 7. Risiko-Kunden (aus ChurnDetectionService)
        // TODO Sprint 2.1.7.4: ChurnDetectionService.getAtRiskCustomers().size()
        metrics.setCustomersAtRisk(0); // Placeholder

        // 8. Retention Rate
        // TODO Sprint 2.1.7.2: Aus historischen Daten berechnen
        metrics.setRetentionRate(0.0); // Placeholder

        return metrics;
    }
}
```

---

#### **3.5.2 DTO: CustomerManagementMetrics**

**Datei:** `backend/src/main/java/de/freshplan/domain/cockpit/service/dto/CustomerManagementMetrics.java`

```java
/**
 * DTO für Kundenmanagement Dashboard KPIs
 *
 * Sprint 2.1.7.2: Alle KPIs für /customer-management Route
 */
public class CustomerManagementMetrics {

    // Kunden-KPIs
    private int activeCustomers;           // Aktive Kunden (Status = AKTIV)
    private int newCustomers;              // Neue Kunden (letzten 30 Tage)
    private int customersAtRisk;           // Risiko-Kunden (ChurnDetectionService)

    // Opportunity-KPIs
    private double pipelineValue;          // Summe aller offenen Opportunities
    private int opportunityCount;          // Anzahl offener Opportunities

    // Umsatz-KPIs (Xentral)
    private double mrr;                    // Monthly Recurring Revenue
    private double averageOrdersPerCustomer; // Ø Bestellungen pro Kunde

    // Zufriedenheit-KPIs (Zukünftiges Feature)
    private double customerSatisfaction;   // Kundenzufriedenheit (0-100%)
    private int npsScore;                  // Net Promoter Score (-100 bis +100)

    // Performance-KPIs
    private double retentionRate;          // Kundenbindungsrate (0-100%)

    // Getters + Setters
    // ...
}
```

---

#### **3.5.3 REST Endpoint**

**Datei:** `backend/src/main/java/de/freshplan/api/resources/CustomerManagementResource.java`

```java
@Path("/api/customer-management")
@ApplicationScoped
public class CustomerManagementResource {

    @Inject
    CustomerManagementMetricsService metricsService;

    @GET
    @Path("/metrics")
    @RolesAllowed({"admin", "manager", "sales"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetrics() {
        CustomerManagementMetrics metrics = metricsService.getMetrics();
        return Response.ok(metrics).build();
    }
}
```

---

#### **3.5.4 Frontend: KundenmanagementDashboard Integration**

**Datei:** `frontend/src/pages/KundenmanagementDashboard.tsx`

**Aktueller Stand (Sprint 2.1.7.4):**
```tsx
// ❌ Hardcoded Mock-Daten
<Typography variant="h4">1.247</Typography>
<Typography variant="body2">Aktive Kunden</Typography>

<Typography variant="h4">€2.3M</Typography>
<Typography variant="body2">Pipeline Wert</Typography>

<Typography variant="h4">89%</Typography>
<Typography variant="body2">Kundenzufriedenheit</Typography>

<Typography variant="h4">€145k</Typography>
<Typography variant="body2">MRR</Typography>
```

**Sprint 2.1.7.2 Änderungen:**
```tsx
// ✅ API Integration mit React Query
import { useQuery } from '@tanstack/react-query';
import { httpClient } from '../services/httpClient';

export function KundenmanagementDashboard() {
  const navigate = useNavigate();
  const theme = useTheme();

  // ✅ Fetch echte Daten vom Backend
  const { data: metrics, isLoading } = useQuery({
    queryKey: ['customer-management-metrics'],
    queryFn: async () => {
      const response = await httpClient.get('/api/customer-management/metrics');
      return response.data;
    },
    refetchInterval: 60000, // Refresh alle 60 Sekunden
  });

  if (isLoading) {
    return <CircularProgress />;
  }

  return (
    <MainLayoutV2 maxWidth="full">
      <Box sx={{ py: 4 }}>
        {/* KPI Cards - Jetzt mit echten Daten */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: theme.palette.secondary.main, fontWeight: 'bold' }}>
                {metrics.activeCustomers.toLocaleString()}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Aktive Kunden
              </Typography>
              <Typography variant="caption" sx={{ color: theme.palette.primary.main }}>
                +{metrics.newCustomers} diesen Monat
              </Typography>
            </Paper>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: theme.palette.primary.main, fontWeight: 'bold' }}>
                €{(metrics.pipelineValue / 1000000).toFixed(1)}M
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Pipeline Wert
              </Typography>
              <Typography variant="caption" sx={{ color: theme.palette.primary.main }}>
                {metrics.opportunityCount} Opportunities
              </Typography>
            </Paper>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: theme.palette.secondary.main, fontWeight: 'bold' }}>
                {metrics.customerSatisfaction > 0 ? `${metrics.customerSatisfaction.toFixed(0)}%` : '-'}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Kundenzufriedenheit
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {metrics.npsScore > 0 ? `NPS Score: ${metrics.npsScore}` : 'Noch nicht verfügbar'}
              </Typography>
            </Paper>
          </Grid>

          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper sx={{ p: 3, textAlign: 'center' }}>
              <Typography variant="h4" sx={{ color: theme.palette.primary.main, fontWeight: 'bold' }}>
                {metrics.mrr > 0 ? `€${(metrics.mrr / 1000).toFixed(0)}k` : '-'}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                MRR
              </Typography>
              <Typography variant="caption" sx={{ color: theme.palette.primary.main }}>
                {metrics.mrr > 0 ? 'Aus Xentral-Daten' : 'Xentral-Integration ausstehend'}
              </Typography>
            </Paper>
          </Grid>
        </Grid>

        {/* Quick Stats - Sidebar */}
        <Paper sx={{ p: 3, mb: 3 }}>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Quick Stats
          </Typography>
          <Grid container spacing={2}>
            <Grid size={6}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h5" sx={{ color: theme.palette.primary.main, fontWeight: 'bold' }}>
                  {metrics.newCustomers}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Neue Kunden (30T)
                </Typography>
              </Box>
            </Grid>
            <Grid size={6}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h5" sx={{ color: theme.palette.secondary.main, fontWeight: 'bold' }}>
                  {metrics.averageOrdersPerCustomer > 0 ? metrics.averageOrdersPerCustomer.toFixed(1) : '-'}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Ø Bestellungen/Kunde
                </Typography>
              </Box>
            </Grid>
            <Grid size={6}>
              <Box sx={{ textAlign: 'center', mt: 2 }}>
                <Typography variant="h5" sx={{ color: theme.palette.secondary.main, fontWeight: 'bold' }}>
                  {metrics.customersAtRisk}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Risiko-Kunden
                </Typography>
              </Box>
            </Grid>
            <Grid size={6}>
              <Box sx={{ textAlign: 'center', mt: 2 }}>
                <Typography variant="h5" sx={{ color: theme.palette.primary.main, fontWeight: 'bold' }}>
                  {metrics.retentionRate > 0 ? `${metrics.retentionRate.toFixed(0)}%` : '-'}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Retention Rate
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Paper>
      </Box>
    </MainLayoutV2>
  );
}
```

---

#### **3.5.5 Implementierungs-Reihenfolge**

**Sprint 2.1.7.2 - Phase 1 (Sofort verfügbar):**
1. ✅ **Aktive Kunden** - `customerRepository.countByStatus(AKTIV)`
2. ✅ **Neue Kunden** - `createdAt >= thirtyDaysAgo`
3. ✅ **Pipeline Wert** - Summe aus `opportunities` Tabelle
4. ✅ **Opportunity Count** - Count aus `opportunities` Tabelle

**Sprint 2.1.7.2 - Phase 2 (Nach Xentral-Integration):**
5. ✅ **MRR** - Aus Xentral Invoice API berechnen
6. ✅ **Ø Bestellungen/Kunde** - Aus Xentral Invoice Count / Customers

**Sprint 2.1.7.2 - Phase 3 (Nach ChurnDetection-Integration):**
7. ✅ **Risiko-Kunden** - `ChurnDetectionService.getAtRiskCustomers().size()`
8. ✅ **Retention Rate** - Historische Berechnung aus Xentral-Daten

**Zukünftige Features (später):**
9. ⏸️ **Kundenzufriedenheit** - Braucht Customer Feedback System
10. ⏸️ **NPS Score** - Braucht Customer Feedback System

---

#### **3.5.6 Test Plan**

**Backend Tests:** `CustomerManagementMetricsServiceTest.java`
- ✅ `testGetMetrics_withActiveCustomers()`
- ✅ `testGetMetrics_withOpportunities()`
- ✅ `testGetMetrics_withNoData()`
- ✅ `testGetMetrics_withXentralData()` (Sprint 2.1.7.2 Phase 2)

**Frontend Tests:** `KundenmanagementDashboard.test.tsx`
- ✅ `should render loading state`
- ✅ `should render metrics from API`
- ✅ `should handle API errors gracefully`
- ✅ `should refresh metrics every 60 seconds`

---

#### **3.5.7 Notizen**

**Warum alles auf einmal in Sprint 2.1.7.2?**
- ✅ **Konsistenz:** Alle Daten kommen aus einer API-Integration
- ✅ **Effizienz:** Xentral-Daten werden eh geladen, dann auch MRR berechnen
- ✅ **User Experience:** Dashboard ist dann "komplett" statt "teilweise Mock"
- ✅ **Testing:** Eine Integration = ein Test-Plan

**Aktueller Stand (Sprint 2.1.7.4):**
- ❌ Alle KPIs sind Hardcoded Mock-Daten
- ✅ Routen sind verdrahtet (`/customer-management` funktioniert)
- ✅ UI ist fertig (Layout, Cards, Sidebar)

**Sprint 2.1.7.2 Aufgabe:**
- ✅ Backend Service + DTO + REST Endpoint erstellen
- ✅ Frontend React Query Integration
- ✅ Phase 1-3 schrittweise implementieren (siehe 3.5.5)

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

### **5.3 Existierende UI-Struktur (Bereits implementiert)**

**⚠️ WICHTIG:** Die Route `/admin/integrations/xentral` existiert bereits als Placeholder!

**Route:** `/admin/integrations/xentral`

**Aktueller Stand:** Placeholder-Seite (seit Sprint 2.1.7.4)

**Dateien:**
- `frontend/src/pages/placeholders/integrationen.tsx:28-41` - `XentralIntegration` Komponente
- `frontend/src/providers.tsx:374-379` - Route Definition mit `ProtectedRoute` (Admin-only)

**Placeholder-Implementierung:**
```tsx
export const XentralIntegration = () => (
  <PlaceholderPage
    title="Xentral Integration"
    subtitle="Nahtlose Verbindung zu Ihrem ERP-System"
    description="Synchronisieren Sie Kunden, Aufträge und Produkte automatisch mit Xentral."
    expectedDate="Q1 2025"
    features={[
      'Bidirektionale Datensynchronisation',
      'Automatischer Auftragsimport',
      'Lagerbestands-Abgleich',
    ]}
    icon={<InventoryIcon sx={{ fontSize: 80, color: 'secondary.main' }} />}
  />
);
```

**PlaceholderPage Layout:**
- Layout: `MainLayoutV2`
- Icon: Lagerbestand-Symbol (📦 `InventoryIcon`)
- "Voraussichtlich verfügbar: Q1 2025" Badge
- Feature-Liste mit 3 Cards
- Info-Box "Möchten Sie benachrichtigt werden?"
- "Zurück" Button

**Sprint 2.1.7.2 Aufgaben:**
- ✅ Route existiert bereits (`/admin/integrations/xentral`)
- ✅ Admin-Zugriffsschutz bereits implementiert (`ProtectedRoute` mit `allowedRoles={['admin']}`)
- ⚠️ **TODO:** Placeholder durch `XentralSettingsPage` ersetzen (Section 5.2)

**Änderungen erforderlich:**

1. **Erstelle neue Seite:** `frontend/src/pages/admin/XentralSettingsPage.tsx` (siehe Section 5.2)

2. **Update Routing in `providers.tsx`:**
   ```tsx
   // providers.tsx - VORHER
   import * as Placeholders from './pages/placeholders';

   <Route
     path="/admin/integrations/xentral"
     element={
       <ProtectedRoute allowedRoles={['admin']}>
         <Placeholders.XentralIntegration /> {/* PLACEHOLDER */}
       </ProtectedRoute>
     }
   />

   // providers.tsx - NACHHER (Sprint 2.1.7.2)
   const XentralSettingsPage = lazy(() =>
     import('./pages/admin/XentralSettingsPage').then(m => ({ default: m.XentralSettingsPage }))
   );

   <Route
     path="/admin/integrations/xentral"
     element={
       <ProtectedRoute allowedRoles={['admin']}>
         <XentralSettingsPage /> {/* ECHTE IMPLEMENTIERUNG */}
       </ProtectedRoute>
     }
   />
   ```

3. **Placeholder behalten (optional):**
   - Placeholder kann als Fallback behalten werden (bei Mock-Mode)
   - Oder komplett löschen (nicht mehr benötigt)

**Vorteile der existierenden Struktur:**
- ✅ Routing bereits definiert
- ✅ Admin-Schutz bereits implementiert
- ✅ Navigation bereits vorhanden (über `/admin/integrations` Dashboard)
- ✅ Layout-Konsistenz durch `MainLayoutV2`
- ✅ Weniger Arbeit in Sprint 2.1.7.2 (nur Content ersetzen)

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

**⚡ WICHTIG:** Xentral Webhooks sind **BETA-Feature** in v25.39+!

**Xentral Webhook-Konfiguration (via Admin-UI):**
- URL: `https://freshplan.example.com/api/xentral/webhook/order-delivered`
- Event: `salesorder.delivery_confirmed` (oder ähnlich - prüfen!)
- Authentication: Bearer Token oder Webhook-Secret (prüfen!)
- Dokumentation: https://developer.xentral.com/reference/webhooks

**Setup-Schritte:**
1. Xentral Admin → Settings → Webhooks (BETA)
2. Webhook anlegen: Event "Order Delivered"
3. Target URL: FreshPlan Webhook Endpoint
4. Test-Event senden (Xentral Test-Button)
5. In FreshPlan Logs prüfen (ob Event empfangen wurde)

---

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

## 8️⃣ Unified Communication System (NEU)

[↑ Back to Overview](#🗺️-quick-overview-2590-zeilen) | [← Prev: Xentral Webhook](#7️⃣-xentral-webhook-integration) | [→ Next: Customer UX Polish](#9️⃣-customer-ux-polish-neu)

### **WHY NOW? Pre-Live Quality Investment**

**Problem:** Lead hat Activities (LeadActivity), Customer hat NICHTS
- ❌ Nach Lead→Customer Conversion geht Activity-Historie verloren
- ❌ Vertriebler muss in zwei Systemen suchen
- ❌ Keine durchgängige Kundenhistorie

**Solution:** Unified Activity System (CRM Best Practice!)
- ✅ **Eine Timeline** für Lead + Customer (wie Salesforce, HubSpot)
- ✅ **Pre-Live Vorteil:** Zero Daten-Migration-Aufwand (nur DEV-SEED!)
- ✅ **Vertriebssicht:** "Komplette Kundenbeziehung vom Erstkontakt bis heute"

**Timing:** JETZT oder NIE!
- Pre-Live: 8h Refactoring + 0h Migration
- Post-Live: 8h Refactoring + 12-16h Migration + Risiko

---

### **8.1 Backend: Activity Entity (Polymorphisch)**

**Datei:** `backend/src/main/java/de/freshplan/domain/communication/entity/Activity.java`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```java
package de.freshplan.domain.communication.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Unified Activity Entity
 *
 * Sprint 2.1.7.2: Unified Communication System
 *
 * Replaces:
 * - LeadActivity (leads module)
 * - (future) CustomerActivity
 *
 * Design: Polymorphic association (entity_type + entity_id)
 * - entity_type = 'LEAD' → entity_id = lead.id
 * - entity_type = 'CUSTOMER' → entity_id = customer.id
 *
 * CRM Best Practice: Salesforce, HubSpot, Dynamics nutzen ALLE dieses Pattern!
 */
@Entity
@Table(name = "activities")
public class Activity extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    /**
     * Polymorphic Entity Type
     * Values: LEAD, CUSTOMER
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    public EntityType entityType;

    /**
     * Polymorphic Entity ID
     * Points to: Lead.id OR Customer.id
     */
    @Column(name = "entity_id", nullable = false)
    public UUID entityId;

    /**
     * Activity Type
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 20)
    public ActivityType activityType;

    /**
     * Activity Outcome (optional - from Sprint 2.1.7)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", length = 30)
    public ActivityOutcome outcome;

    /**
     * Subject/Title
     */
    @Column(name = "subject", length = 255)
    public String subject;

    /**
     * Notes/Details
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    public String notes;

    /**
     * Activity Date/Time
     */
    @Column(name = "activity_date", nullable = false)
    public LocalDateTime activityDate;

    /**
     * Created By (User ID)
     */
    @Column(name = "created_by", length = 100)
    public String createdBy;

    /**
     * Created At (Timestamp)
     */
    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    /**
     * Updated At (Timestamp)
     */
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (activityDate == null) activityDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

/**
 * Entity Type Enum
 */
public enum EntityType {
    LEAD,
    CUSTOMER
}

/**
 * Activity Type Enum (from LeadActivity)
 */
public enum ActivityType {
    CALL,
    EMAIL,
    MEETING,
    NOTE,
    SYSTEM
}

/**
 * Activity Outcome Enum (from Sprint 2.1.7)
 */
public enum ActivityOutcome {
    SUCCESSFUL,
    UNSUCCESSFUL,
    NO_ANSWER,
    CALLBACK_REQUESTED,
    INFO_SENT,
    QUALIFIED,
    DISQUALIFIED
}
```

**Database Indices:**
```sql
-- Composite Index for fast lookups
CREATE INDEX idx_activities_entity ON activities(entity_type, entity_id);

-- Activity Date Index (for timeline sorting)
CREATE INDEX idx_activities_date ON activities(activity_date DESC);
```

---

### **8.2 Backend: ActivityService**

**Datei:** `backend/src/main/java/de/freshplan/domain/communication/service/ActivityService.java`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```java
package de.freshplan.domain.communication.service;

import de.freshplan.domain.communication.entity.Activity;
import de.freshplan.domain.communication.entity.EntityType;
import de.freshplan.domain.communication.entity.ActivityType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Unified Activity Service
 *
 * Sprint 2.1.7.2: Unified Communication System
 *
 * Features:
 * - Create activities for Lead OR Customer
 * - List activities by entity (Lead/Customer)
 * - Unified timeline (includes Lead→Customer history!)
 */
@ApplicationScoped
public class ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);

    /**
     * Get Activities for Entity
     *
     * @param entityType LEAD or CUSTOMER
     * @param entityId Lead-ID or Customer-ID
     * @return List of Activities (sorted by date DESC)
     */
    public List<Activity> getActivities(EntityType entityType, UUID entityId) {
        return Activity.find(
            "entityType = ?1 AND entityId = ?2 ORDER BY activityDate DESC",
            entityType,
            entityId
        ).list();
    }

    /**
     * Get Activities for Customer (includes Lead history!)
     *
     * Wenn Customer aus Lead konvertiert wurde:
     * - Zeigt Activities aus Lead-Phase (entity_type = LEAD, entity_id = originalLeadId)
     * - Zeigt Activities aus Customer-Phase (entity_type = CUSTOMER, entity_id = customerId)
     *
     * @param customerId Customer UUID
     * @return Unified Timeline (Lead + Customer Activities)
     */
    public List<Activity> getCustomerActivitiesIncludingLeadHistory(UUID customerId) {
        // 1. Find Customer
        Customer customer = Customer.findById(customerId);
        if (customer == null) {
            throw new NotFoundException("Customer not found: " + customerId);
        }

        // 2. Get Customer Activities
        List<Activity> activities = getActivities(EntityType.CUSTOMER, customerId);

        // 3. If converted from Lead → include Lead Activities!
        if (customer.getOriginalLeadId() != null) {
            List<Activity> leadActivities = getActivities(
                EntityType.LEAD,
                customer.getOriginalLeadId()
            );
            activities.addAll(leadActivities);

            // Re-sort by date
            activities.sort((a, b) -> b.activityDate.compareTo(a.activityDate));

            logger.debug("Included {} Lead activities for Customer {}",
                leadActivities.size(), customerId);
        }

        return activities;
    }

    /**
     * Create Activity
     *
     * @param entityType LEAD or CUSTOMER
     * @param entityId Entity ID
     * @param request Activity Creation Request
     * @return Created Activity
     */
    @Transactional
    public Activity createActivity(
        EntityType entityType,
        UUID entityId,
        CreateActivityRequest request
    ) {
        Activity activity = new Activity();
        activity.entityType = entityType;
        activity.entityId = entityId;
        activity.activityType = request.activityType;
        activity.outcome = request.outcome;
        activity.subject = request.subject;
        activity.notes = request.notes;
        activity.activityDate = request.activityDate != null
            ? request.activityDate
            : LocalDateTime.now();
        activity.createdBy = request.createdBy;

        activity.persist();

        logger.info("Created {} activity for {} {}",
            request.activityType, entityType, entityId);

        return activity;
    }

    /**
     * Update Activity
     */
    @Transactional
    public Activity updateActivity(UUID activityId, UpdateActivityRequest request) {
        Activity activity = Activity.findById(activityId);
        if (activity == null) {
            throw new NotFoundException("Activity not found: " + activityId);
        }

        if (request.subject != null) activity.subject = request.subject;
        if (request.notes != null) activity.notes = request.notes;
        if (request.outcome != null) activity.outcome = request.outcome;

        activity.persist();
        return activity;
    }

    /**
     * Delete Activity
     */
    @Transactional
    public void deleteActivity(UUID activityId) {
        Activity activity = Activity.findById(activityId);
        if (activity != null) {
            activity.delete();
            logger.info("Deleted activity {}", activityId);
        }
    }
}

/**
 * Create Activity Request DTO
 */
public record CreateActivityRequest(
    ActivityType activityType,
    ActivityOutcome outcome,
    String subject,
    String notes,
    LocalDateTime activityDate,
    String createdBy
) {}

/**
 * Update Activity Request DTO
 */
public record UpdateActivityRequest(
    String subject,
    String notes,
    ActivityOutcome outcome
) {}
```

**REST Endpoints:**
```java
/**
 * ActivityResource
 */
@Path("/api/activities")
@ApplicationScoped
public class ActivityResource {

    @Inject
    ActivityService activityService;

    /**
     * Get Activities for Customer (includes Lead history!)
     */
    @GET
    @Path("/customer/{customerId}")
    @RolesAllowed({"USER", "MANAGER", "ADMIN"})
    public Response getCustomerActivities(@PathParam("customerId") UUID customerId) {
        List<Activity> activities = activityService
            .getCustomerActivitiesIncludingLeadHistory(customerId);
        return Response.ok(activities).build();
    }

    /**
     * Create Activity for Customer
     */
    @POST
    @Path("/customer/{customerId}")
    @RolesAllowed({"USER", "MANAGER", "ADMIN"})
    @Transactional
    public Response createActivityForCustomer(
        @PathParam("customerId") UUID customerId,
        CreateActivityRequest request
    ) {
        Activity activity = activityService.createActivity(
            EntityType.CUSTOMER,
            customerId,
            request
        );
        return Response.status(201).entity(activity).build();
    }

    // Similar endpoints for Lead...
}
```

---

### **8.3 Migration: lead_activities → activities**

**Datei:** `backend/src/main/resources/db/migration/V10033__unified_communication_system.sql`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```sql
-- =============================================================================
-- V10033: Unified Communication System - lead_activities → activities
-- =============================================================================
-- Sprint: 2.1.7.2 - Customer-Management KOMPLETT
-- Purpose: Unified Activity Timeline für Lead + Customer (CRM Best Practice)
--
-- Problem:
--   - Lead hat lead_activities Tabelle
--   - Customer hat KEINE Activity-Tabelle
--   - Nach Conversion geht Historie verloren
--
-- Solution:
--   - Neue activities Tabelle (polymorphisch: entity_type + entity_id)
--   - Migration: lead_activities → activities (entity_type = 'LEAD')
--   - Später: activities auch für Customer (entity_type = 'CUSTOMER')
--
-- CRM Best Practice:
--   - Salesforce: Universal Activity Log
--   - HubSpot: Activity Stream
--   - Dynamics: Activity Feed
-- =============================================================================

-- 1. Create new activities table (polymorphisch)
CREATE TABLE activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(20) NOT NULL,     -- 'LEAD' oder 'CUSTOMER'
    entity_id UUID NOT NULL,              -- Lead-ID oder Customer-ID
    activity_type VARCHAR(20) NOT NULL,   -- CALL, EMAIL, MEETING, NOTE, SYSTEM
    outcome VARCHAR(30),                  -- Sprint 2.1.7: SUCCESSFUL, UNSUCCESSFUL, etc.
    subject VARCHAR(255),
    notes TEXT,
    activity_date TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 2. Add indices for fast lookups
CREATE INDEX idx_activities_entity ON activities(entity_type, entity_id);
CREATE INDEX idx_activities_date ON activities(activity_date DESC);
CREATE INDEX idx_activities_created_by ON activities(created_by);

-- 3. Add CHECK constraint for entity_type
ALTER TABLE activities
ADD CONSTRAINT activities_entity_type_check
CHECK (entity_type IN ('LEAD', 'CUSTOMER'));

-- 4. Migrate data from lead_activities → activities
INSERT INTO activities (
    id,
    entity_type,
    entity_id,
    activity_type,
    outcome,
    subject,
    notes,
    activity_date,
    created_by,
    created_at,
    updated_at
)
SELECT
    id,
    'LEAD' AS entity_type,              -- ← Alle bisherigen sind Lead-Activities
    lead_id AS entity_id,
    activity_type,
    outcome,
    subject,
    notes,
    activity_date,
    created_by,
    created_at,
    updated_at
FROM lead_activities;

-- Log migration
DO $$
DECLARE
    migrated_count INTEGER;
BEGIN
    GET DIAGNOSTICS migrated_count = ROW_COUNT;
    RAISE NOTICE 'Migrated % lead activities to unified activities table', migrated_count;
END $$;

-- 5. Verify migration (all lead_activities should be in activities)
DO $$
DECLARE
    lead_count INTEGER;
    activity_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO lead_count FROM lead_activities;
    SELECT COUNT(*) INTO activity_count FROM activities WHERE entity_type = 'LEAD';

    IF lead_count != activity_count THEN
        RAISE EXCEPTION 'Migration failed: % lead_activities but % activities',
            lead_count, activity_count;
    ELSE
        RAISE NOTICE '✓ Migration successful: All % lead_activities migrated',
            lead_count;
    END IF;
END $$;

-- 6. Drop old lead_activities table (AFTER verification!)
-- COMMENTED OUT for safety - uncomment after testing!
-- DROP TABLE lead_activities;

COMMENT ON TABLE activities IS 'Unified Activity Log für Leads + Customers (Sprint 2.1.7.2)';
COMMENT ON COLUMN activities.entity_type IS 'Polymorphic: LEAD oder CUSTOMER';
COMMENT ON COLUMN activities.entity_id IS 'Polymorphic: Lead-ID oder Customer-ID';
COMMENT ON COLUMN activities.outcome IS 'Activity Outcome (Sprint 2.1.7)';
```

**Migration Safety:**
- ✅ **Verification Step:** Prüft, ob alle Daten migriert wurden
- ✅ **Rollback-Safe:** `lead_activities` bleibt vorerst erhalten
- ✅ **Pre-Live Vorteil:** Nur ~50 DEV-SEED Einträge (5 Sekunden Migration!)

---

### **8.4 Frontend: ActivityTimeline Component**

**Datei:** `frontend/src/features/communication/components/ActivityTimeline.tsx`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```tsx
import React from 'react';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineOppositeContent
} from '@mui/lab';
import {
  Typography,
  Paper,
  Box,
  Chip,
  IconButton
} from '@mui/material';
import {
  Phone as PhoneIcon,
  Email as EmailIcon,
  EventNote as MeetingIcon,
  Notes as NoteIcon,
  Settings as SystemIcon,
  Edit as EditIcon
} from '@mui/icons-material';
import { format } from 'date-fns';
import { de } from 'date-fns/locale';

/**
 * Unified Activity Timeline
 *
 * Sprint 2.1.7.2: Unified Communication System
 *
 * Features:
 * - Zeigt Activities für Lead ODER Customer
 * - Bei Customer: Zeigt auch Lead-History! (via originalLeadId)
 * - Vertikale Timeline (chronologisch absteigend)
 * - Activity-Type Icons (CALL, EMAIL, MEETING, NOTE)
 * - "Als Lead erfasst" Badge für Lead-Activities
 *
 * Code-Reuse:
 * - Component funktioniert für BEIDE Entities (Lead + Customer)
 * - Keine Duplikation!
 */
interface ActivityTimelineProps {
  entityType: 'lead' | 'customer';
  entityId: string;
  onEdit?: (activity: Activity) => void;
}

interface Activity {
  id: string;
  entityType: 'LEAD' | 'CUSTOMER';
  activityType: 'CALL' | 'EMAIL' | 'MEETING' | 'NOTE' | 'SYSTEM';
  outcome?: string;
  subject: string;
  notes: string;
  activityDate: string;
  createdBy: string;
}

export const ActivityTimeline: React.FC<ActivityTimelineProps> = ({
  entityType,
  entityId,
  onEdit
}) => {
  // Fetch activities
  const { data: activities, isLoading } = useQuery(
    ['activities', entityType, entityId],
    () => httpClient.get(`/api/activities/${entityType}/${entityId}`)
  );

  if (isLoading) return <CircularProgress />;

  if (!activities || activities.length === 0) {
    return (
      <Alert severity="info">
        Noch keine Aktivitäten vorhanden.
      </Alert>
    );
  }

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'CALL': return <PhoneIcon />;
      case 'EMAIL': return <EmailIcon />;
      case 'MEETING': return <MeetingIcon />;
      case 'NOTE': return <NoteIcon />;
      case 'SYSTEM': return <SystemIcon />;
      default: return <NoteIcon />;
    }
  };

  const getActivityColor = (type: string) => {
    switch (type) {
      case 'CALL': return 'primary';
      case 'EMAIL': return 'info';
      case 'MEETING': return 'success';
      case 'NOTE': return 'default';
      case 'SYSTEM': return 'secondary';
      default: return 'default';
    }
  };

  return (
    <Timeline position="right">
      {activities.map((activity: Activity, index: number) => (
        <TimelineItem key={activity.id}>
          <TimelineOppositeContent color="text.secondary">
            <Typography variant="caption">
              {format(new Date(activity.activityDate), 'dd.MM.yyyy', { locale: de })}
            </Typography>
            <Typography variant="caption" display="block">
              {format(new Date(activity.activityDate), 'HH:mm', { locale: de })} Uhr
            </Typography>
          </TimelineOppositeContent>

          <TimelineSeparator>
            <TimelineDot color={getActivityColor(activity.activityType)}>
              {getActivityIcon(activity.activityType)}
            </TimelineDot>
            {index < activities.length - 1 && <TimelineConnector />}
          </TimelineSeparator>

          <TimelineContent>
            <Paper sx={{ p: 2, mb: 2 }}>
              {/* Header */}
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Chip
                    label={activity.activityType}
                    size="small"
                    color={getActivityColor(activity.activityType)}
                  />

                  {/* "Als Lead erfasst" Badge (nur bei Customer-Ansicht!) */}
                  {entityType === 'customer' && activity.entityType === 'LEAD' && (
                    <Chip
                      label="Als Lead erfasst"
                      size="small"
                      variant="outlined"
                      color="secondary"
                    />
                  )}

                  {activity.outcome && (
                    <Chip
                      label={activity.outcome}
                      size="small"
                      variant="outlined"
                    />
                  )}
                </Box>

                {/* Edit Button */}
                {onEdit && (
                  <IconButton size="small" onClick={() => onEdit(activity)}>
                    <EditIcon fontSize="small" />
                  </IconButton>
                )}
              </Box>

              {/* Subject */}
              <Typography variant="subtitle1" fontWeight="bold">
                {activity.subject}
              </Typography>

              {/* Notes */}
              {activity.notes && (
                <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                  {activity.notes}
                </Typography>
              )}

              {/* Footer */}
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                Erfasst von: {activity.createdBy}
              </Typography>
            </Paper>
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
};
```

**Tests:**
- Component Test: Renders timeline with activities
- Component Test: Shows "Als Lead erfasst" badge for customer view
- Component Test: Activity type icons are correct
- Component Test: Date formatting (German locale)
- Integration Test: Fetches activities from API

---

### **8.5 Frontend: ActivityDialog Component**

**Datei:** `frontend/src/features/communication/components/ActivityDialog.tsx`

**Status:** 📋 PLANNING (NEU - Code-Reuse von LeadActivityDialog!)

**Implementierung:**
```tsx
/**
 * Unified Activity Dialog
 *
 * Sprint 2.1.7.2: Unified Communication System
 *
 * Code-Reuse:
 * - Basiert auf LeadActivityDialog (Sprint 2.1.7.1)
 * - Funktioniert für Lead UND Customer!
 * - Keine Duplikation!
 */
interface ActivityDialogProps {
  open: boolean;
  onClose: () => void;
  entityType: 'lead' | 'customer';
  entityId: string;
  activity?: Activity; // If editing
}

export const ActivityDialog: React.FC<ActivityDialogProps> = ({
  open,
  onClose,
  entityType,
  entityId,
  activity
}) => {
  const [activityType, setActivityType] = useState<ActivityType>(
    activity?.activityType || 'CALL'
  );
  const [outcome, setOutcome] = useState<ActivityOutcome | null>(
    activity?.outcome || null
  );
  const [subject, setSubject] = useState(activity?.subject || '');
  const [notes, setNotes] = useState(activity?.notes || '');
  const [activityDate, setActivityDate] = useState(
    activity?.activityDate || new Date().toISOString()
  );

  const handleSave = async () => {
    const endpoint = activity
      ? `/api/activities/${activity.id}` // UPDATE
      : `/api/activities/${entityType}/${entityId}`; // CREATE

    const method = activity ? 'PUT' : 'POST';

    await httpClient[method](endpoint, {
      activityType,
      outcome,
      subject,
      notes,
      activityDate,
      createdBy: currentUser.email
    });

    enqueueSnackbar('Aktivität gespeichert', { variant: 'success' });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {activity ? 'Aktivität bearbeiten' : 'Neue Aktivität'}
      </DialogTitle>

      <DialogContent>
        {/* Activity Type */}
        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel>Typ</InputLabel>
          <Select value={activityType} onChange={(e) => setActivityType(e.target.value)}>
            <MenuItem value="CALL">Anruf</MenuItem>
            <MenuItem value="EMAIL">E-Mail</MenuItem>
            <MenuItem value="MEETING">Meeting</MenuItem>
            <MenuItem value="NOTE">Notiz</MenuItem>
          </Select>
        </FormControl>

        {/* Outcome */}
        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel>Ergebnis</InputLabel>
          <Select value={outcome || ''} onChange={(e) => setOutcome(e.target.value || null)}>
            <MenuItem value="">-</MenuItem>
            <MenuItem value="SUCCESSFUL">Erfolgreich</MenuItem>
            <MenuItem value="UNSUCCESSFUL">Nicht erfolgreich</MenuItem>
            <MenuItem value="NO_ANSWER">Nicht erreicht</MenuItem>
            <MenuItem value="CALLBACK_REQUESTED">Rückruf gewünscht</MenuItem>
            <MenuItem value="INFO_SENT">Informationen gesendet</MenuItem>
            <MenuItem value="QUALIFIED">Qualifiziert</MenuItem>
            <MenuItem value="DISQUALIFIED">Disqualifiziert</MenuItem>
          </Select>
        </FormControl>

        {/* Subject */}
        <TextField
          fullWidth
          label="Betreff"
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
          required
          sx={{ mb: 2 }}
        />

        {/* Notes */}
        <TextField
          fullWidth
          label="Notizen"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          multiline
          rows={4}
          sx={{ mb: 2 }}
        />

        {/* Activity Date */}
        <TextField
          fullWidth
          type="datetime-local"
          label="Datum/Zeit"
          value={activityDate.substring(0, 16)}
          onChange={(e) => setActivityDate(e.target.value + ':00')}
          InputLabelProps={{ shrink: true }}
        />
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button variant="contained" onClick={handleSave} disabled={!subject}>
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

---

### **8.6 Integration: CustomerDetailPage**

**Datei:** `frontend/src/pages/CustomerDetailPage.tsx`

**Status:** 📋 ENHANCEMENT

**Implementierung:**
```tsx
// Add Activity Timeline Tab
<Tabs value={tabValue} onChange={handleTabChange}>
  <Tab label="Übersicht" />
  <Tab label="Kontakte" />
  <Tab label="Kommunikation" /> {/* ← NEU! */}
  <Tab label="Standorte" />
  <Tab label="Opportunities" />
</Tabs>

{/* Tab: Kommunikation */}
{tabValue === 2 && (
  <Box sx={{ p: 3 }}>
    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
      <Typography variant="h6">Kommunikation</Typography>
      <Button
        variant="contained"
        startIcon={<AddIcon />}
        onClick={() => setActivityDialogOpen(true)}
      >
        Neue Aktivität
      </Button>
    </Box>

    {/* Timeline Component (zeigt AUCH Lead-Activities!) */}
    <ActivityTimeline
      entityType="customer"
      entityId={customer.id}
      onEdit={(activity) => {
        setEditActivity(activity);
        setActivityDialogOpen(true);
      }}
    />

    {/* Activity Dialog */}
    <ActivityDialog
      open={activityDialogOpen}
      onClose={() => {
        setActivityDialogOpen(false);
        setEditActivity(null);
      }}
      entityType="customer"
      entityId={customer.id}
      activity={editActivity}
    />
  </Box>
)}

{/* Quick-Add Button (rechts, immer sichtbar - wie bei Lead!) */}
<Box sx={{ position: 'fixed', right: 24, top: 100 }}>
  <Paper sx={{ p: 2, width: 300 }}>
    <Typography variant="subtitle2" gutterBottom>
      Schnelle Aktivität
    </Typography>
    <Button
      fullWidth
      variant="outlined"
      startIcon={<AddIcon />}
      onClick={() => setActivityDialogOpen(true)}
      sx={{ mb: 1 }}
    >
      Neue Aktivität
    </Button>

    {/* Letzte 3 Aktivitäten */}
    <Typography variant="caption" color="text.secondary">
      Letzte Aktivitäten
    </Typography>
    <List dense>
      {recentActivities.slice(0, 3).map(activity => (
        <ListItem key={activity.id}>
          <ListItemText
            primary={activity.subject}
            secondary={format(new Date(activity.activityDate), 'dd.MM.yyyy')}
          />
        </ListItem>
      ))}
    </List>
  </Paper>
</Box>
```

---

## 9️⃣ Customer UX Polish (NEU)

[↑ Back to Overview](#🗺️-quick-overview-2590-zeilen) | [← Prev: Unified Communication](#8️⃣-unified-communication-system-neu) | [→ Next: Test Summary](#-test-summary)

### **WHY NOW? 100% Lead-Customer Parity**

**Problem:**
- Pain Points existieren in DB (V10032), aber nicht im Wizard sichtbar
- Multi-Contact-Erfassung im Wizard, aber kein Dashboard-Edit
- Dashboard-UX: Nur Anzeige, kein Quick-Edit

**Solution: 3 Quick-Wins (2.5h)**
1. Pain Points in fieldCatalog.json registrieren (15 min)
2. Multi-Contact Migration (LeadConvertService - 30 min)
3. Dashboard Contact CRUD (ContactFormDialog - 2h)

---

### **9.1 Pain Points registrieren**

**Datei:** `frontend/src/features/customers/data/fieldCatalog.json`

**Status:** 📋 PLANNING (NEU!)

**Implementierung:**
```json
{
  "customer": {
    "base": [...],
    "painPoints": {
      "label": "Herausforderungen",
      "description": "Welche Herausforderungen hat der Kunde?",
      "fields": [
        {
          "key": "painStaffShortage",
          "label": "Personalmangel",
          "type": "boolean",
          "defaultValue": false
        },
        {
          "key": "painHighCosts",
          "label": "Hoher Kostendruck",
          "type": "boolean",
          "defaultValue": false
        },
        {
          "key": "painQualityIssues",
          "label": "Qualitätsprobleme",
          "type": "boolean",
          "defaultValue": false
        },
        {
          "key": "painSupplyChain",
          "label": "Lieferkettenprobleme",
          "type": "boolean",
          "defaultValue": false
        },
        {
          "key": "painFoodSafety",
          "label": "Lebensmittelsicherheit",
          "type": "boolean",
          "defaultValue": false
        },
        {
          "key": "painSustainability",
          "label": "Nachhaltigkeit",
          "type": "boolean",
          "defaultValue": false
        },
        {
          "key": "painDigitalization",
          "label": "Digitalisierung",
          "type": "boolean",
          "defaultValue": false
        },
        {
          "key": "painCustomerRetention",
          "label": "Kundenbindung",
          "type": "boolean",
          "defaultValue": false
        }
      ]
    }
  }
}
```

**Wizard Integration:**
```tsx
// CustomerOnboardingWizard.tsx - Step 2 erweitern

import { GlobalChallengesSection } from '../components/GlobalChallengesSection';

// Step 2: Pain Points (nutzt existierenden Component!)
{currentStep === 2 && (
  <GlobalChallengesSection
    value={formData.painPoints}
    onChange={(painPoints) => setFormData({ ...formData, painPoints })}
  />
)}
```

**Aufwand:** 15 Minuten (Copy-Paste von Lead!)

---

### **9.2 Multi-Contact Migration**

**Datei:** `backend/src/main/java/de/freshplan/modules/leads/service/LeadConvertService.java`

**Status:** 📋 ENHANCEMENT

**Implementierung:**
```java
/**
 * Convert Lead to Customer
 *
 * Sprint 2.1.7.2: Multi-Contact Migration (NEU!)
 */
@Transactional
public Customer convertToCustomer(UUID leadId, ConvertRequest request) {
    Lead lead = leadRepository.findByIdOptional(leadId)
        .orElseThrow(() -> new NotFoundException("Lead not found"));

    // Create Customer
    Customer customer = new Customer();
    customer.setCompanyName(lead.getCompanyName());
    customer.setStatus(CustomerStatus.PROSPECT); // Sprint 2.1.7.4
    customer.setOriginalLeadId(lead.getId());
    customer.setXentralCustomerId(request.xentralCustomerId());
    // ... weitere Felder ...

    // ALLE Kontakte kopieren (NICHT nur Primary!)
    lead.getContacts().forEach(leadContact -> {
        CustomerContact customerContact = new CustomerContact();
        customerContact.setSalutation(leadContact.getSalutation());
        customerContact.setFirstName(leadContact.getFirstName());
        customerContact.setLastName(leadContact.getLastName());
        customerContact.setEmail(leadContact.getEmail());
        customerContact.setPhone(leadContact.getPhone());
        customerContact.setMobile(leadContact.getMobile());
        customerContact.setIsPrimary(leadContact.getIsPrimary());
        customerContact.setWarmthScore(leadContact.getWarmthScore());
        customerContact.setDataQualityScore(leadContact.getDataQualityScore());
        customer.addContact(customerContact);
    });

    // Pain Points kopieren (NEU!)
    customer.setPainStaffShortage(lead.getPainStaffShortage());
    customer.setPainHighCosts(lead.getPainHighCosts());
    customer.setPainQualityIssues(lead.getPainQualityIssues());
    customer.setPainSupplyChain(lead.getPainSupplyChain());
    customer.setPainFoodSafety(lead.getPainFoodSafety());
    customer.setPainSustainability(lead.getPainSustainability());
    customer.setPainDigitalization(lead.getPainDigitalization());
    customer.setPainCustomerRetention(lead.getPainCustomerRetention());

    customerRepository.persist(customer);

    // Update Lead Status
    lead.setStatus(LeadStatus.CONVERTED);
    lead.persist();

    logger.info("Converted Lead {} to Customer {} (contacts: {}, pain points migrated)",
        leadId, customer.getId(), customer.getContacts().size());

    return customer;
}
```

**Tests:**
- Unit Test: All contacts are migrated (not just primary)
- Unit Test: Pain points are copied
- Integration Test: LeadConvertService full flow

**Aufwand:** 30 Minuten

---

### **9.3 Dashboard Contact CRUD**

**Datei:** `frontend/src/features/customers/components/ContactGridContainer.tsx`

**Status:** 📋 ENHANCEMENT

**Implementierung:**
```tsx
/**
 * Contact Grid Container (CRUD)
 *
 * Sprint 2.1.7.2: Dashboard Contact CRUD
 *
 * Features:
 * - Click-to-Edit (ContactCard onClick)
 * - Add new contacts
 * - Edit existing contacts (ContactFormDialog wiederverwenden!)
 */
interface ContactGridContainerProps {
  customerId: string;
  contacts: CustomerContact[];
  onContactsUpdated: () => void;
}

export const ContactGridContainer: React.FC<ContactGridContainerProps> = ({
  customerId,
  contacts,
  onContactsUpdated
}) => {
  const [editContact, setEditContact] = useState<CustomerContact | null>(null);
  const [isAddingContact, setIsAddingContact] = useState(false);

  const handleSaveContact = async (contactData: ContactFormData) => {
    if (editContact) {
      // UPDATE
      await httpClient.put(`/api/customers/${customerId}/contacts/${editContact.id}`, contactData);
      enqueueSnackbar('Kontakt aktualisiert', { variant: 'success' });
    } else {
      // CREATE
      await httpClient.post(`/api/customers/${customerId}/contacts`, contactData);
      enqueueSnackbar('Kontakt hinzugefügt', { variant: 'success' });
    }

    setEditContact(null);
    setIsAddingContact(false);
    onContactsUpdated();
  };

  return (
    <Box>
      {/* Add Button */}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
        <Button
          variant="outlined"
          startIcon={<AddIcon />}
          onClick={() => setIsAddingContact(true)}
        >
          Kontakt hinzufügen
        </Button>
      </Box>

      {/* Contact Grid */}
      <Grid container spacing={2}>
        {contacts.map(contact => (
          <Grid item xs={12} md={6} lg={4} key={contact.id}>
            <ContactCard
              contact={contact}
              onClick={() => setEditContact(contact)} {/* ← CLICK-TO-EDIT! */}
            />
          </Grid>
        ))}
      </Grid>

      {/* Contact Form Dialog (Code-Reuse vom Wizard!) */}
      <ContactFormDialog
        open={Boolean(editContact) || isAddingContact}
        contact={editContact}
        onSave={handleSaveContact}
        onClose={() => {
          setEditContact(null);
          setIsAddingContact(false);
        }}
      />
    </Box>
  );
};
```

**Backend Endpoints (NEU!):**
```java
/**
 * CustomerResource - Contact CRUD
 */
@POST
@Path("/{id}/contacts")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
@Transactional
public Response addContact(@PathParam("id") UUID customerId, CustomerContact contact) {
    Customer customer = Customer.findById(customerId);
    customer.addContact(contact);
    customer.persist();
    return Response.status(201).entity(contact).build();
}

@PUT
@Path("/{id}/contacts/{contactId}")
@RolesAllowed({"USER", "MANAGER", "ADMIN"})
@Transactional
public Response updateContact(
    @PathParam("id") UUID customerId,
    @PathParam("contactId") UUID contactId,
    CustomerContact updatedContact
) {
    Customer customer = Customer.findById(customerId);
    CustomerContact contact = customer.getContacts().stream()
        .filter(c -> c.getId().equals(contactId))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Contact not found"));

    // Update fields
    contact.setFirstName(updatedContact.getFirstName());
    contact.setLastName(updatedContact.getLastName());
    // ... weitere Felder ...

    customer.persist();
    return Response.ok(contact).build();
}
```

**Tests:**
- Component Test: Click on card opens edit dialog
- Component Test: Add button opens empty dialog
- Integration Test: POST /api/customers/{id}/contacts
- Integration Test: PUT /api/customers/{id}/contacts/{contactId}

**Aufwand:** 2 Stunden

---

## 🔟 Multi-Location Vorbereitung (NEU)

[↑ Back to Overview](#🗺️-quick-overview-2590-zeilen) | [← Prev: Customer UX Polish](#9️⃣-customer-ux-polish-neu) | [→ Next: Test Summary](#-test-summary)

### **WHY NOW? Zero Rework für Sprint 2.1.7.7**

**Problem:** Wenn wir Multi-Location UI ERST in Sprint 2.1.7.7 bauen:
- ❌ ConvertToCustomerDialog muss komplett umgebaut werden (4h Rework!)
- ❌ User sehen kein "Coming Soon" → Verwirrung
- ❌ Sprint 2.1.7.7 wird länger (36h statt 30h)

**Solution:** UI JETZT vorbereiten (disabled), Sprint 2.1.7.7 aktiviert nur!
- ✅ ConvertToCustomerDialog hat bereits alle Felder (disabled)
- ✅ User sehen: "Multi-Location bald verfügbar"
- ✅ Sprint 2.1.7.7: Nur 30h (statt 36h) → **6h gespart!**

**Timing:** JETZT oder NIE!
- Sprint 2.1.7.2: +1h (UI vorbereiten)
- Sprint 2.1.7.7: -6h (UI existiert schon)
- **Netto: -5h Einsparung!** ✅

---

### **10.1 ConvertDialog UI Vorbereitung**

**Datei:** `frontend/src/features/opportunity/components/ConvertToCustomerDialog.tsx`

**Status:** 📋 ENHANCEMENT (erweitern um hierarchyType)

**Implementierung:**
```tsx
/**
 * Convert Opportunity to Customer Dialog
 *
 * Sprint 2.1.7.2: Multi-Location UI Vorbereitung (disabled)
 * Sprint 2.1.7.7: Aktivierung (disabled entfernen + Parent-Selection)
 */
export const ConvertToCustomerDialog: React.FC<ConvertToCustomerDialogProps> = ({
  open,
  onClose,
  opportunity
}) => {
  // ... existing state ...

  // NEU: hierarchyType State (Sprint 2.1.7.2)
  const [hierarchyType, setHierarchyType] = useState<HierarchyType>('STANDALONE');
  const [parentCustomer, setParentCustomer] = useState<Customer | null>(null);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Opportunity zu Customer konvertieren</DialogTitle>

      <DialogContent>
        {/* Existing: Company Name */}
        <TextField
          fullWidth
          label="Firmenname"
          value={companyName}
          onChange={(e) => setCompanyName(e.target.value)}
          required
          sx={{ mb: 2 }}
        />

        {/* NEU: Unternehmenstyp (Sprint 2.1.7.2 - UI vorbereitet!) */}
        <FormControl fullWidth sx={{ mb: 2 }}>
          <InputLabel>Unternehmenstyp</InputLabel>
          <Select
            value={hierarchyType}
            onChange={(e) => setHierarchyType(e.target.value)}
            disabled={false} // Sprint 2.1.7.2: NICHT disabled (User kann wählen)
          >
            <MenuItem value="STANDALONE">
              Einzelbetrieb
            </MenuItem>
            <MenuItem value="HEADQUARTER">
              Zentrale/Hauptbetrieb (mit Filialen)
            </MenuItem>
            <MenuItem value="FILIALE" disabled> {/* ← Sprint 2.1.7.7: disabled entfernen */}
              Filiale (gehört zu Zentrale)
            </MenuItem>
          </Select>
          <FormHelperText>
            {hierarchyType === 'STANDALONE' && 'Einzelner Standort ohne Filialen'}
            {hierarchyType === 'HEADQUARTER' && 'Hauptbetrieb - Filialen können später angelegt werden'}
            {hierarchyType === 'FILIALE' && '⏳ Multi-Location Management verfügbar ab Sprint 2.1.7.7'}
          </FormHelperText>
        </FormControl>

        {/* NEU: Parent-Selection (nur wenn FILIALE - Sprint 2.1.7.7 aktiviert!) */}
        {hierarchyType === 'FILIALE' && (
          <Alert severity="info" sx={{ mb: 2 }}>
            <AlertTitle>Multi-Location Management</AlertTitle>
            <Typography variant="body2">
              Diese Funktion wird in Kürze verfügbar sein (Sprint 2.1.7.7).
              <br />
              Bis dahin können Sie den Kunden als <strong>STANDALONE</strong> anlegen
              und später zur Filiale konvertieren.
            </Typography>
          </Alert>
        )}

        {/* INFO-BOX: HEADQUARTER gewählt */}
        {hierarchyType === 'HEADQUARTER' && (
          <Alert severity="success" sx={{ mb: 2 }}>
            <AlertTitle>Hauptbetrieb mit Filialen</AlertTitle>
            <Typography variant="body2">
              Nach Anlage können Sie Filialen im Customer-Dashboard hinzufügen
              (Tab "Standorte" → "Neue Filiale anlegen").
            </Typography>
          </Alert>
        )}

        {/* Existing: Xentral-Kunde, PROSPECT Info, Notizen */}
        {/* ... */}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          variant="contained"
          onClick={handleConvert}
          disabled={!companyName || hierarchyType === 'FILIALE'}
        >
          Customer anlegen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

**Backend-Änderung (minimal!):**
```java
// OpportunityService.convertToCustomer() erweitern

Customer customer = new Customer();
customer.setCompanyName(request.companyName());
customer.setStatus(CustomerStatus.PROSPECT);

// NEU: hierarchyType aus Request (Sprint 2.1.7.2)
customer.setHierarchyType(request.hierarchyType() != null
    ? request.hierarchyType()
    : CustomerHierarchyType.STANDALONE  // Default
);

// Sprint 2.1.7.7: Parent-Link
if (request.hierarchyType() == CustomerHierarchyType.FILIALE) {
    // Wird erst in Sprint 2.1.7.7 aktiv (aktuell disabled in UI)
    customer.setParentCustomer(parentCustomer);
}
```

**Tests:**
- Component Test: hierarchyType Dropdown renders
- Component Test: FILIALE option is disabled (Sprint 2.1.7.2)
- Component Test: Info-Box shows for HEADQUARTER

**Aufwand:** 30 Minuten

---

### **10.2 Wizard hierarchyType Dropdown**

**Datei:** `frontend/src/features/customers/components/CustomerOnboardingWizard.tsx`

**Status:** 📋 ENHANCEMENT

**Implementierung:**
```tsx
/**
 * Customer Onboarding Wizard
 *
 * Sprint 2.1.7.2: hierarchyType Dropdown hinzufügen (Step 1)
 */
export const CustomerOnboardingWizard: React.FC = () => {
  const [formData, setFormData] = useState({
    companyName: '',
    hierarchyType: 'STANDALONE', // NEU!
    // ... existing fields ...
  });

  return (
    <Stepper activeStep={currentStep}>
      {/* Step 1: Stammdaten */}
      {currentStep === 0 && (
        <Box>
          {/* Existing: companyName */}
          <TextField
            fullWidth
            label="Firmenname"
            value={formData.companyName}
            onChange={(e) => setFormData({ ...formData, companyName: e.target.value })}
            sx={{ mb: 2 }}
          />

          {/* NEU: hierarchyType (Sprint 2.1.7.2) */}
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Unternehmenstyp</InputLabel>
            <Select
              value={formData.hierarchyType}
              onChange={(e) => setFormData({ ...formData, hierarchyType: e.target.value })}
            >
              <MenuItem value="STANDALONE">Einzelbetrieb</MenuItem>
              <MenuItem value="HEADQUARTER">Zentrale/Hauptbetrieb</MenuItem>
              <MenuItem value="FILIALE" disabled>
                Filiale (bald verfügbar)
              </MenuItem>
            </Select>
            <FormHelperText>
              Filialen-Management verfügbar ab Sprint 2.1.7.7
            </FormHelperText>
          </FormControl>

          {/* Existing: businessType, etc. */}
        </Box>
      )}

      {/* Step 2: Business Info + Pain Points (Deliverable 9.1) */}
      {/* Step 3: Multi-Contact (Deliverable 9.3) */}
      {/* Step 4: Zusammenfassung */}
    </Stepper>
  );
};
```

**Aufwand:** 15 Minuten

---

### **10.3 Wizard/Dashboard Review Checklist**

**Sprint 2.1.7.2 Review-Aufgaben (beim Testing!):**

#### **✅ Wizard Review**

**Step 1: Stammdaten**
- [ ] Firmenname-Feld sichtbar?
- [ ] hierarchyType Dropdown sichtbar?
- [ ] FILIALE disabled mit Hinweis "bald verfügbar"?
- [ ] HEADQUARTER/STANDALONE wählbar?

**Step 2: Business Info**
- [ ] Pain Points Karte sichtbar? (Deliverable 9.1)
- [ ] 8 Pain Point Checkboxen sichtbar?
- [ ] Beschriftung Deutsch?

**Step 3: Multi-Contact**
- [ ] Contact-Karten sichtbar?
- [ ] "Kontakt hinzufügen" Button funktioniert?
- [ ] Primary Contact markierbar?

**Step 4: Zusammenfassung**
- [ ] Alle Felder korrekt angezeigt?
- [ ] hierarchyType = HEADQUARTER → Hinweis "Filialen später anlegbar"?

#### **✅ Dashboard Review**

**CustomerDetailPage - Tab "Übersicht"**
- [ ] Stammdaten sichtbar?
- [ ] hierarchyType Badge sichtbar? (STANDALONE/HEADQUARTER)
- [ ] Bei HEADQUARTER: Hinweis "0 Filialen" (Sprint 2.1.7.7 wird aktiv)?

**CustomerDetailPage - Tab "Kontakte"**
- [ ] Kontakte angezeigt?
- [ ] Click auf Kontakt-Karte → Edit-Dialog öffnet? (Deliverable 9.3)
- [ ] "Kontakt hinzufügen" Button funktioniert?

**CustomerDetailPage - Tab "Kommunikation" (NEU!)**
- [ ] Tab sichtbar?
- [ ] Activity-Timeline zeigt Activities?
- [ ] Bei konvertiertem Lead: "Als Lead erfasst" Badge sichtbar? (Deliverable 8.4)
- [ ] "Neue Aktivität" Button funktioniert?
- [ ] Quick-Add (rechts) funktioniert?

**CustomerDetailPage - Tab "Standorte"**
- [ ] Nur bei HEADQUARTER sichtbar?
- [ ] Bei STANDALONE: Tab nicht vorhanden?
- [ ] Hinweis: "Filialen-Management verfügbar ab Sprint 2.1.7.7"?

**CustomerDetailPage - Tab "Opportunities"**
- [ ] Opportunities angezeigt?
- [ ] "Neue Opportunity" Button funktioniert?

#### **✅ ConvertToCustomerDialog Review**

- [ ] hierarchyType Dropdown sichtbar?
- [ ] STANDALONE/HEADQUARTER wählbar?
- [ ] FILIALE disabled?
- [ ] Info-Box bei HEADQUARTER: "Filialen später anlegbar"?
- [ ] Info-Box bei FILIALE: "Bald verfügbar Sprint 2.1.7.7"?
- [ ] Button disabled wenn FILIALE gewählt?

**Aufwand:** 15 Minuten (während Testing)

---

**Deliverable 10 Total Aufwand: 1h**
- 10.1 ConvertDialog UI: 30 min
- 10.2 Wizard Dropdown: 15 min
- 10.3 Review Checklist: 15 min (integriert in Testing)

---

## 📊 TEST SUMMARY

**Total Tests: 162** ← **+90 neue Tests (Sections 8+9+10)**

### **Backend Tests: 105** (46 original + 58 neu + 1 Section 10)

**Original Deliverables 1-7:**
- XentralApiClient: 10 Tests
- RevenueMetricsService: 7 Tests
- ChurnDetectionService: 5 Tests
- XentralWebhookResource: 4 Tests
- XentralSettingsResource: 6 Tests
- SalesRepSyncJob: 3 Tests
- Integration Tests: 11 Tests

**Deliverable 8 - Unified Communication System (+50 Tests):**
- Activity Entity: 8 Tests
- ActivityService: 15 Tests
  - getActivities (LEAD + CUSTOMER): 4 Tests
  - getCustomerActivitiesIncludingLeadHistory: 5 Tests
  - createActivity: 3 Tests
  - updateActivity, deleteActivity: 3 Tests
- ActivityResource (REST Endpoints): 12 Tests
  - GET /api/activities/customer/{id}: 4 Tests
  - POST /api/activities/customer/{id}: 4 Tests
  - PUT /api/activities/{id}: 2 Tests
  - DELETE /api/activities/{id}: 2 Tests
- Migration V10033: 5 Tests
  - Data migration verification: 2 Tests
  - Constraint validation: 2 Tests
  - Rollback safety: 1 Test
- Integration Tests: 10 Tests
  - Lead→Customer Activity Migration: 3 Tests
  - Activity Timeline (includes Lead history): 3 Tests
  - Error Handling (entity not found): 2 Tests
  - Permission Tests (RBAC): 2 Tests

**Deliverable 9 - Customer UX Polish (+8 Tests):**
- LeadConvertService (Multi-Contact Migration): 5 Tests
  - All contacts migrated: 2 Tests
  - Pain points copied: 1 Test
  - Integration test: 2 Tests
- CustomerResource (Contact CRUD): 3 Tests
  - POST /api/customers/{id}/contacts: 1 Test
  - PUT /api/customers/{id}/contacts/{contactId}: 1 Test
  - Integration test: 1 Test

**Deliverable 10 - Multi-Location Vorbereitung (+1 Test):**
- OpportunityService (hierarchyType Support): 1 Test
  - hierarchyType STANDALONE/HEADQUARTER saved correctly: 1 Test

### **Frontend Tests: 57** (26 original + 30 neu + 1 Section 10)

**Original Deliverables 1-7:**
- ConvertToCustomerDialog: 5 Tests
- CustomerDetailPage: 4 Tests
- RevenueMetricsWidget: 3 Tests
- PaymentBehaviorIndicator: 2 Tests
- XentralSettingsPage: 2 Tests
- UserManagementPage: 2 Tests
- Integration Tests: 8 Tests

**Deliverable 8 - Unified Communication System (+20 Tests):**
- ActivityTimeline Component: 8 Tests
  - Renders timeline correctly: 2 Tests
  - "Als Lead erfasst" badge (customer view): 2 Tests
  - Activity type icons: 2 Tests
  - Date formatting (German locale): 1 Test
  - Integration test (API fetch): 1 Test
- ActivityDialog Component: 7 Tests
  - Form validation: 2 Tests
  - Create activity (POST): 2 Tests
  - Update activity (PUT): 2 Tests
  - Error handling: 1 Test
- CustomerDetailPage Integration: 5 Tests
  - Tab "Kommunikation" renders: 1 Test
  - Quick-Add button works: 1 Test
  - Timeline shows Lead + Customer activities: 2 Tests
  - Integration test: 1 Test

**Deliverable 9 - Customer UX Polish (+10 Tests):**
- Pain Points Registration: 2 Tests
  - fieldCatalog.json validation: 1 Test
  - Wizard integration: 1 Test
- ContactGridContainer (CRUD): 8 Tests
  - Click-to-Edit opens dialog: 2 Tests
  - Add button opens empty dialog: 1 Test
  - Save contact (CREATE): 2 Tests
  - Update contact (UPDATE): 2 Tests
  - Integration test: 1 Test

**Deliverable 10 - Multi-Location Vorbereitung (+1 Test):**
- ConvertToCustomerDialog: 1 Test
  - hierarchyType Dropdown renders: 1 Test
  - FILIALE option disabled: (tested with above)
  - Info-Box HEADQUARTER shows: (tested with above)

---

## 🔗 RELATED DOCUMENTATION

**Design Decisions:**
→ `/docs/planung/artefakte/SPEC_SPRINT_2_1_7_2_DESIGN_DECISIONS.md`

**Trigger Document:**
→ `/docs/planung/TRIGGER_SPRINT_2_1_7_2.md`

**Design System:**
→ `/docs/planung/grundlagen/DESIGN_SYSTEM.md`

**Communication Architecture (NEU):**
→ See Section 8: Unified Communication System (CRM Best Practice)

**Related Sprints:**
- Sprint 2.1.7.4: Customer Status Architecture (REQUIRED!)
- Sprint 2.1.7.1: Lead → Opportunity UI Integration (COMPLETE)
- Sprint 2.1.7: Opportunity Backend + ActivityOutcome Enum (COMPLETE)

---

**✅ SPEC STATUS: 📋 COMPLETE - Ready for Implementation**

**Letzte Aktualisierung:** 2025-10-21 (Erweitert: Deliverable 8/9/10 - Unified Communication + UX Polish + Multi-Location Prep)

**Aufwand:** 36h (25h original + 10h erweitert + 1h Multi-Location Prep)

**Sprint-Reihenfolge (Option A):**
1. Sprint 2.1.7.4 - Customer Status (14h)
2. Sprint 2.1.7.2 - Customer-Management KOMPLETT (36h) ← **DIESER SPRINT**
3. Sprint 2.1.7.7 - Multi-Location Aktivierung (30h) ← **Reduziert! (war 36h)**
4. Sprint 2.1.7.6 - Customer Lifecycle (8h)
5. Sprint 2.1.7.5 - Opportunity Management (35-37h)

**Aufwands-Einsparung durch Option A:** -5h (Sprint 2.1.7.2: +1h, Sprint 2.1.7.7: -6h)
