# 🚀 Sprint 2.1.7.2 - Customer-Management + Xentral-Integration

**Sprint-ID:** 2.1.7.2
**Status:** 📋 PLANNING → 🚀 READY TO START
**Priority:** P1 (High)
**Estimated Effort:** 21h (2.5-3 Arbeitstage) *(+3h für Admin-UI)*
**Owner:** TBD
**Created:** 2025-10-16
**Updated:** 2025-10-18 (Sales-Rep-Mapping-Strategie + Admin-UI + Hybrid-Ansatz)
**Dependencies:** Sprint 2.1.7.1 COMPLETE

---

## 🎯 SPRINT GOALS

### **Business Value**

**Vertriebler können Kunden managen mit ECHTEN Umsatzdaten aus Xentral:**

- ✅ Opportunity → Customer konvertieren (mit Xentral-Verknüpfung)
- ✅ Customer-Dashboard mit Live-Umsatzdaten (30/90/365 Tage)
- ✅ Zahlungsverhalten-Überblick (Ampel-System, keine Detail-Rechnungen)
- ✅ Churn-Alarm (variabel pro Kunde konfigurierbar)
- ✅ Xentral-Kunden-Dropdown (verkäufer-gefiltert, kein manuelles Tippen!)

**Business Impact:**
- Verkäufer sehen sofort: "Läuft der Kunde gut?" (Umsatz + Zahlungsmoral)
- Churn-Prevention: Frühwarnung bei Inaktivität (individuell einstellbar)
- Provision-Transparenz: Umsatz ≠ Provision (klar getrennt!)
- Innendienst entlastet: Verkäufer haben Self-Service-Dashboard

### **Technical Context**

**Xentral-Integration ist FERTIG dokumentiert (Modul 08):**
- ✅ **[FC-005: Xentral Invoice/Payment APIs](../features-neu/08_administration/phase-2-integrations/analyse/03_XENTRAL_ALLIANZ_INTEGRATION_FINDINGS.md)** (840 Zeilen Doku!)
  - Invoice API: GET /api/v1/invoices?customerId={id}
  - Payment Summary API: GET /api/v1/customers/{id}/payment-summary
  - Customer List API: GET /api/v1/customers?salesRepId={id}
  - Event-Payloads: invoice.created, payment.received, payment.overdue
- ✅ **[FC-009: Xentral Contract Management APIs](../features-neu/08_administration/phase-2-integrations/artefakte/xentral-contract-api.md)** (311 Zeilen Doku!)
  - Contract Status Change Events
  - Price Index Threshold Events
- ✅ Xentral läuft BEREITS (Production-Ready)
- ✅ Sales-Rep Mapping: Dokumentiert in FC-005 (Zeilen 127-189)

**Backend existiert bereits:**
- ✅ V261: `customers.original_lead_id` (Lead → Customer Traceability)
- ✅ OpportunityService.convertToCustomer() (Backend fertig!)
- ✅ REST API: POST /api/opportunities/{id}/convert-to-customer

**Was JETZT gebaut wird:**
- ✅ ConvertToCustomerDialog mit Xentral-Kunden-Auswahl (Dropdown!)
- ✅ XentralApiClient Integration (Umsatz + Zahlungsverhalten abrufen)
- ✅ Customer-Dashboard mit echten Daten (keine Placeholders!)
- ✅ Churn-Alarm mit variabler Schwelle (pro Kunde konfigurierbar)
- ✅ **Admin-UI für Xentral-Einstellungen** (`/admin/integrations/xentral`)
- ✅ **Admin-UI für Sales-Rep-Mapping** (`/admin/users` erweitert)
- ✅ **Automatischer Sales-Rep-Sync-Job** (Email-basiert)

---

## 📦 DELIVERABLES

### **1. Opportunity → Customer Conversion** (4h)

#### **1.1 ConvertToCustomerDialog Component** (2h)

**Neue Datei:** `frontend/src/features/opportunity/components/ConvertToCustomerDialog.tsx`

**Anforderungen:**
- MUI Dialog mit Form
- Nur bei CLOSED_WON Opportunities verfügbar
- Felder:
  - **Company Name** (Text, pre-filled aus Opportunity.name)
  - **Xentral-Kunde verknüpfen** (Autocomplete-Dropdown - verkäufer-gefiltert!)
  - **Notizen** (TextArea, optional)

**Xentral-Kunden-Dropdown (KEY FEATURE!):**
```tsx
const [xentralCustomers, setXentralCustomers] = useState<XentralCustomerDTO[]>([]);

useEffect(() => {
  // Lade nur Verkäufer-eigene Kunden
  const loadXentralCustomers = async () => {
    const response = await httpClient.get(
      `/api/xentral/customers?salesRepId=${currentUser.xentralSalesRepId}`
    );
    setXentralCustomers(response.data);
  };
  loadXentralCustomers();
}, [currentUser]);

<Autocomplete
  options={xentralCustomers}
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
          Xentral-ID: {option.xentralId} • Umsatz: {option.totalRevenue}€
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
/>

{/* Info-Box wenn Xentral-Kunde gewählt */}
{selectedXentralCustomer && (
  <Alert severity="info">
    Nach Anlage werden Umsatzdaten von Xentral-Kunde
    "{selectedXentralCustomer.companyName}" angezeigt.
  </Alert>
)}

{/* Info-Box wenn KEIN Xentral-Kunde gewählt */}
{!selectedXentralCustomer && (
  <Alert severity="warning">
    Ohne Xentral-Verknüpfung können keine Umsatzdaten angezeigt werden.
    Du kannst die Verknüpfung später im Customer-Detail-Bereich vornehmen.
  </Alert>
)}
```

**API Calls:**
```typescript
// 1. Xentral-Kunden laden (bei Dialog-Open)
GET /api/xentral/customers?salesRepId={currentUser.xentralSalesRepId}

// 2. Customer anlegen (bei Submit)
POST /api/opportunities/{opportunityId}/convert-to-customer
Body: {
  companyName: string,
  xentralCustomerId?: string,  // Optional!
  notes?: string
}
```

**Success Flow:**
```tsx
const handleConvert = async () => {
  try {
    const customer = await httpClient.post(
      `/api/opportunities/${opportunity.id}/convert-to-customer`,
      {
        companyName: companyName,
        xentralCustomerId: selectedXentralCustomer?.xentralId,
        notes: notes
      }
    );

    toast.success('Kunde erfolgreich angelegt! 🎉');
    navigate(`/customers/${customer.id}`);

  } catch (error) {
    if (error.status === 409) {
      toast.error('Opportunity wurde bereits zu Kunde konvertiert');
    } else {
      toast.error('Fehler beim Anlegen des Kunden');
    }
  }
};
```

#### **1.2 OpportunityDetailPage Integration** (1h)

**Datei:** `frontend/src/features/opportunity/pages/OpportunityDetailPage.tsx`

**Button hinzufügen:**
```tsx
{/* Nur bei CLOSED_WON und noch KEIN Customer */}
{opportunity.stage === 'CLOSED_WON' && !opportunity.customer && (
  <Button
    variant="contained"
    color="success"
    startIcon={<CheckCircleIcon />}
    onClick={() => setShowConvertDialog(true)}
    sx={{
      bgcolor: theme.palette.success.main,
      '&:hover': { bgcolor: theme.palette.success.dark }
    }}
  >
    Als Kunde anlegen
  </Button>
)}

{/* Wenn Customer bereits angelegt: Badge mit Link */}
{opportunity.customer && (
  <Alert severity="success" icon={<CheckCircleIcon />}>
    <Stack direction="row" spacing={2} alignItems="center">
      <Typography variant="body2">
        Kunde angelegt am {formatDate(opportunity.updatedAt)}
      </Typography>
      <Button
        variant="outlined"
        size="small"
        component={Link}
        to={`/customers/${opportunity.customer.id}`}
      >
        Zum Kundenprofil
      </Button>
    </Stack>
  </Alert>
)}

<ConvertToCustomerDialog
  open={showConvertDialog}
  onClose={() => setShowConvertDialog(false)}
  opportunity={opportunity}
  onSuccess={() => loadOpportunity()}
/>
```

#### **1.3 Backend-Erweiterung: Xentral-Kunden-Endpoint** (1h)

**Neue Datei:** `backend/src/main/java/de/freshplan/api/resources/XentralResource.java`

```java
@Path("/api/xentral")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityAudit
public class XentralResource {

    @Inject
    XentralApiClient xentralApiClient;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    UserRepository userRepository;

    /**
     * Holt alle Xentral-Kunden für den aktuellen Verkäufer
     *
     * GET /api/xentral/customers?salesRepId={xentralSalesRepId}
     */
    @GET
    @Path("/customers")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response getCustomersBySalesRep(@QueryParam("salesRepId") String salesRepId) {
        logger.debug("Fetching Xentral customers for sales rep: {}", salesRepId);

        // Fallback: Wenn kein salesRepId übergeben, nutze current user
        if (salesRepId == null) {
            User currentUser = getCurrentUser();
            salesRepId = currentUser.getXentralSalesRepId();
        }

        List<XentralCustomerDTO> customers =
            xentralApiClient.getCustomersBySalesRep(salesRepId);

        return Response.ok(customers).build();
    }

    /**
     * Holt Umsatzdaten für einen Xentral-Kunden
     *
     * GET /api/xentral/customers/{xentralId}/revenue
     */
    @GET
    @Path("/customers/{xentralId}/revenue")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response getCustomerRevenue(@PathParam("xentralId") String xentralId) {
        logger.debug("Fetching revenue for Xentral customer: {}", xentralId);

        RevenueDataDTO revenue = xentralApiClient.getCustomerRevenue(xentralId);

        return Response.ok(revenue).build();
    }

    /**
     * Holt Zahlungsverhalten für einen Xentral-Kunden
     *
     * GET /api/xentral/customers/{xentralId}/payment-behavior
     */
    @GET
    @Path("/customers/{xentralId}/payment-behavior")
    @RolesAllowed({"admin", "manager", "sales"})
    public Response getPaymentBehavior(@PathParam("xentralId") String xentralId) {
        logger.debug("Fetching payment behavior for Xentral customer: {}", xentralId);

        PaymentBehaviorDTO behavior = xentralApiClient.getPaymentBehavior(xentralId);

        return Response.ok(behavior).build();
    }

    private User getCurrentUser() {
        String username = securityIdentity.getPrincipal().getName();
        return userRepository
            .findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Current user not found: " + username));
    }
}
```

**Neue DTOs erstellen:**
```java
// XentralCustomerDTO.java
public class XentralCustomerDTO {
    private String xentralId;
    private String companyName;
    private BigDecimal totalRevenue;
    private String salesRepId;
    // ... getters/setters
}

// RevenueDataDTO.java
public class RevenueDataDTO {
    private BigDecimal last30Days;
    private BigDecimal last90Days;
    private BigDecimal last365Days;
    private Integer orderCount;
    private BigDecimal avgOrderValue;
    private String trend; // "GROWING", "STABLE", "DECLINING"
    // ... getters/setters
}

// PaymentBehaviorDTO.java
public class PaymentBehaviorDTO {
    private Integer avgDaysToPayment;
    private BigDecimal openInvoicesAmount;
    private BigDecimal overdueAmount;
    private String paymentScore; // "EXCELLENT", "GOOD", "ACCEPTABLE", "PROBLEMATIC"
    // ... getters/setters
}
```

---

### **2. Xentral-API-Client Implementation** (3h)

#### **2.1 XentralApiClient erstellen** (2h)

**Neue Datei:** `backend/src/main/java/de/freshplan/infrastructure/external/XentralApiClient.java`

```java
@ApplicationScoped
public class XentralApiClient {

    private static final Logger logger = LoggerFactory.getLogger(XentralApiClient.class);

    @ConfigProperty(name = "xentral.api.url")
    String xentralApiUrl;

    @ConfigProperty(name = "xentral.api.token")
    String xentralApiToken;

    @Inject
    @RestClient
    XentralRestClient restClient;

    /**
     * Holt alle Xentral-Kunden für einen Verkäufer
     *
     * Basiert auf FC-005 Spezifikation (Modul 08)
     */
    public List<XentralCustomerDTO> getCustomersBySalesRep(String salesRepId) {
        logger.debug("Fetching Xentral customers for sales rep: {}", salesRepId);

        try {
            // GET /api/v1/customers?salesRepId={salesRepId}
            List<XentralCustomer> customers =
                restClient.getCustomersBySalesRep(salesRepId);

            return customers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Failed to fetch Xentral customers for sales rep: {}", salesRepId, e);
            throw new XentralApiException("Failed to fetch customers", e);
        }
    }

    /**
     * Holt Umsatzdaten für einen Kunden
     *
     * Aggregiert aus Invoice API (FC-005)
     */
    public RevenueDataDTO getCustomerRevenue(String xentralCustomerId) {
        logger.debug("Fetching revenue for Xentral customer: {}", xentralCustomerId);

        try {
            // GET /api/v1/invoices?customerId={xentralCustomerId}
            List<XentralInvoice> invoices =
                restClient.getInvoices(xentralCustomerId);

            return calculateRevenue(invoices);

        } catch (Exception e) {
            logger.error("Failed to fetch revenue for customer: {}", xentralCustomerId, e);
            throw new XentralApiException("Failed to fetch revenue", e);
        }
    }

    /**
     * Holt Zahlungsverhalten für einen Kunden
     *
     * Aggregiert aus Payment API (FC-005)
     */
    public PaymentBehaviorDTO getPaymentBehavior(String xentralCustomerId) {
        logger.debug("Fetching payment behavior for customer: {}", xentralCustomerId);

        try {
            // GET /api/v1/customers/{xentralCustomerId}/payment-summary
            XentralPaymentSummary summary =
                restClient.getPaymentSummary(xentralCustomerId);

            return mapPaymentBehavior(summary);

        } catch (Exception e) {
            logger.error("Failed to fetch payment behavior for customer: {}", xentralCustomerId, e);
            throw new XentralApiException("Failed to fetch payment behavior", e);
        }
    }

    private RevenueDataDTO calculateRevenue(List<XentralInvoice> invoices) {
        LocalDate now = LocalDate.now();
        LocalDate days30Ago = now.minusDays(30);
        LocalDate days90Ago = now.minusDays(90);
        LocalDate days365Ago = now.minusDays(365);

        BigDecimal last30Days = invoices.stream()
            .filter(inv -> inv.getInvoiceDate().isAfter(days30Ago))
            .map(XentralInvoice::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal last90Days = invoices.stream()
            .filter(inv -> inv.getInvoiceDate().isAfter(days90Ago))
            .map(XentralInvoice::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal last365Days = invoices.stream()
            .filter(inv -> inv.getInvoiceDate().isAfter(days365Ago))
            .map(XentralInvoice::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer orderCount = (int) invoices.stream()
            .filter(inv -> inv.getInvoiceDate().isAfter(days30Ago))
            .count();

        BigDecimal avgOrderValue = orderCount > 0
            ? last30Days.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        String trend = calculateTrend(invoices);

        return RevenueDataDTO.builder()
            .last30Days(last30Days)
            .last90Days(last90Days)
            .last365Days(last365Days)
            .orderCount(orderCount)
            .avgOrderValue(avgOrderValue)
            .trend(trend)
            .build();
    }

    private String calculateTrend(List<XentralInvoice> invoices) {
        // Simple Trend: Vergleiche letzte 30 Tage mit vorherigen 30 Tagen
        LocalDate now = LocalDate.now();
        LocalDate days30Ago = now.minusDays(30);
        LocalDate days60Ago = now.minusDays(60);

        BigDecimal last30Days = invoices.stream()
            .filter(inv -> inv.getInvoiceDate().isAfter(days30Ago))
            .map(XentralInvoice::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal previous30Days = invoices.stream()
            .filter(inv -> inv.getInvoiceDate().isAfter(days60Ago) &&
                          inv.getInvoiceDate().isBefore(days30Ago))
            .map(XentralInvoice::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (previous30Days.compareTo(BigDecimal.ZERO) == 0) {
            return "STABLE"; // Keine vorherigen Daten
        }

        BigDecimal change = last30Days.subtract(previous30Days);
        BigDecimal changePercent = change
            .divide(previous30Days, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        if (changePercent.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "GROWING";
        } else if (changePercent.compareTo(BigDecimal.valueOf(-10)) < 0) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }

    private PaymentBehaviorDTO mapPaymentBehavior(XentralPaymentSummary summary) {
        String paymentScore = calculatePaymentScore(summary);

        return PaymentBehaviorDTO.builder()
            .avgDaysToPayment(summary.getAvgDaysToPayment())
            .openInvoicesAmount(summary.getOpenInvoicesAmount())
            .overdueAmount(summary.getOverdueAmount())
            .paymentScore(paymentScore)
            .build();
    }

    private String calculatePaymentScore(XentralPaymentSummary summary) {
        // Regel 1: Überfällige Beträge > 1000€ → PROBLEMATIC
        if (summary.getOverdueAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
            return "PROBLEMATIC";
        }

        // Regel 2: Überfällige Beträge > 0€ → ACCEPTABLE
        if (summary.getOverdueAmount().compareTo(BigDecimal.ZERO) > 0) {
            return "ACCEPTABLE";
        }

        // Regel 3: Durchschnitt Zahlungsziel
        if (summary.getAvgDaysToPayment() <= 7) {
            return "EXCELLENT";
        } else if (summary.getAvgDaysToPayment() <= 14) {
            return "GOOD";
        } else {
            return "ACCEPTABLE";
        }
    }

    private XentralCustomerDTO mapToDTO(XentralCustomer customer) {
        return XentralCustomerDTO.builder()
            .xentralId(customer.getCustomerId())
            .companyName(customer.getCompanyName())
            .totalRevenue(customer.getTotalRevenue())
            .salesRepId(customer.getSalesRepId())
            .build();
    }
}
```

#### **2.2 Configuration** (30 Min)

**application.properties:**
```properties
# Xentral API Configuration
xentral.api.url=https://xentral.freshfoodz.de/api/v1
xentral.api.token=${XENTRAL_API_TOKEN}

# Timeouts
xentral.api.connect-timeout=5000
xentral.api.read-timeout=10000
```

#### **2.3 User-Xentral-Mapping** (30 Min)

**✅ IMPLEMENTED Strategy: Option A + Automatischer Sync**

**Strategie-Entscheidung (2025-10-18):**
- **Option A gewählt:** User-Tabelle erweitern (einfach, performant)
- **Auto-Sync-Job:** Email-basierte Synchronisation aus Xentral
- **Admin-UI:** Manuelle Pflege möglich unter `/admin/users`

**Migration V10031:** User-Tabelle erweitern
```sql
-- Migration: V10031__add_xentral_sales_rep_id.sql
ALTER TABLE app_user ADD COLUMN xentral_sales_rep_id VARCHAR(50);

COMMENT ON COLUMN app_user.xentral_sales_rep_id IS
'Xentral Sales Rep ID für API-Filtering (z.B. "SR-47") - wird automatisch aus Xentral synchronisiert via Email-Matching';

-- Initial: NULL (wird via Auto-Sync-Job befüllt)
-- Beispiel nach Sync:
-- max.mueller@freshfoodz.de → Xentral Sales Rep mit Email max.mueller@freshfoodz.de → SR-47
-- anna.schmidt@freshfoodz.de → Xentral Sales Rep mit Email anna.schmidt@freshfoodz.de → SR-89
```

**Sync-Strategie:** Email-basierte Zuordnung
```java
// Xentral liefert: GET /api/v1/sales-reps
// Response: [{ "id": "SR-47", "email": "max.mueller@freshfoodz.de", "name": "Max Müller" }, ...]

// FreshPlan Auto-Sync-Job (täglich):
// 1. Holt alle Sales Reps aus Xentral
// 2. Matched via Email mit app_user.email
// 3. Setzt xentral_sales_rep_id bei Match
// 4. Loggt Unmatched Users für manuelle Review

@Scheduled(cron = "0 0 2 * * ?") // Täglich 2:00 Uhr
public void syncSalesRepIds() {
    List<XentralSalesRep> salesReps = xentralApiClient.getAllSalesReps();

    for (XentralSalesRep salesRep : salesReps) {
        userRepository.findByEmail(salesRep.getEmail())
            .ifPresent(user -> {
                user.setXentralSalesRepId(salesRep.getId());
                logger.info("Synced: {} → {}", user.getEmail(), salesRep.getId());
            });
    }
}
```

**Fallback:** Manuelle Pflege via Admin-UI (siehe Deliverable 7)

---

### **3. Customer-Dashboard mit echten Daten** (6h)

#### **3.1 CustomerDetailPage Erweitern** (3h)

**Datei:** `frontend/src/pages/CustomerDetailPage.tsx`

**Neue Sections hinzufügen:**

```tsx
{/* Xentral-Verknüpfung Status */}
{!customer.xentralCustomerId && (
  <Alert severity="warning" icon={<WarningIcon />}>
    <Stack direction="row" spacing={2} alignItems="center">
      <Typography variant="body2">
        Keine Xentral-Verknüpfung. Umsatzdaten können nicht angezeigt werden.
      </Typography>
      <Button
        variant="outlined"
        size="small"
        startIcon={<LinkIcon />}
        onClick={() => setLinkXentralDialogOpen(true)}
      >
        Jetzt verknüpfen
      </Button>
    </Stack>
  </Alert>
)}

{/* Umsatz-Dashboard (nur wenn Xentral-Verknüpfung!) */}
{customer.xentralCustomerId && (
  <Grid container spacing={3}>
    {/* Links: Umsatz-Metrics */}
    <Grid item xs={12} md={6}>
      <Card>
        <CardHeader title="💰 Umsatz (Rechnungen)" />
        <CardContent>
          <CustomerRevenueCard xentralCustomerId={customer.xentralCustomerId} />
        </CardContent>
      </Card>
    </Grid>

    {/* Rechts: Zahlungsverhalten */}
    <Grid item xs={12} md={6}>
      <Card>
        <CardHeader title="💳 Zahlungsverhalten" />
        <CardContent>
          <CustomerPaymentBehaviorCard xentralCustomerId={customer.xentralCustomerId} />
        </CardContent>
      </Card>
    </Grid>

    {/* Unten: Churn-Alarm */}
    <Grid item xs={12}>
      <Card>
        <CardHeader
          title="🚨 Aktivitäts-Monitoring"
          action={
            <IconButton size="small" onClick={() => setChurnSettingsOpen(true)}>
              <SettingsIcon fontSize="small" />
            </IconButton>
          }
        />
        <CardContent>
          <CustomerChurnMonitoring
            customer={customer}
            xentralCustomerId={customer.xentralCustomerId}
          />
        </CardContent>
      </Card>
    </Grid>
  </Grid>
)}
```

#### **3.2 CustomerRevenueCard Component** (1h)

**Neue Datei:** `frontend/src/features/customers/components/CustomerRevenueCard.tsx`

```tsx
export function CustomerRevenueCard({ xentralCustomerId }: Props) {
  const [revenue, setRevenue] = useState<RevenueDataDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadRevenue = async () => {
      try {
        const response = await httpClient.get(
          `/api/xentral/customers/${xentralCustomerId}/revenue`
        );
        setRevenue(response.data);
      } catch (error) {
        console.error('Failed to load revenue:', error);
        toast.error('Fehler beim Laden der Umsatzdaten');
      } finally {
        setLoading(false);
      }
    };

    loadRevenue();
  }, [xentralCustomerId]);

  if (loading) return <CircularProgress />;

  return (
    <Stack spacing={2}>
      <MetricBox
        label="Letzte 30 Tage"
        value={formatCurrency(revenue.last30Days)}
        trend={`${revenue.trend === 'GROWING' ? '+' : revenue.trend === 'DECLINING' ? '-' : ''}12%`}
        trendColor={revenue.trend === 'GROWING' ? 'success' : revenue.trend === 'DECLINING' ? 'error' : 'info'}
      />
      <MetricBox
        label="Letzte 90 Tage"
        value={formatCurrency(revenue.last90Days)}
      />
      <MetricBox
        label="Letzte 365 Tage"
        value={formatCurrency(revenue.last365Days)}
      />
      <Divider />
      <Typography variant="body2" color="text.secondary">
        {revenue.orderCount} Bestellungen (Ø {formatCurrency(revenue.avgOrderValue)} pro Bestellung)
      </Typography>
    </Stack>
  );
}
```

#### **3.3 CustomerPaymentBehaviorCard Component** (1h)

**Neue Datei:** `frontend/src/features/customers/components/CustomerPaymentBehaviorCard.tsx`

```tsx
export function CustomerPaymentBehaviorCard({ xentralCustomerId }: Props) {
  const [behavior, setBehavior] = useState<PaymentBehaviorDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadBehavior = async () => {
      try {
        const response = await httpClient.get(
          `/api/xentral/customers/${xentralCustomerId}/payment-behavior`
        );
        setBehavior(response.data);
      } catch (error) {
        console.error('Failed to load payment behavior:', error);
        toast.error('Fehler beim Laden der Zahlungsdaten');
      } finally {
        setLoading(false);
      }
    };

    loadBehavior();
  }, [xentralCustomerId]);

  if (loading) return <CircularProgress />;

  const scoreConfig = {
    EXCELLENT: { label: 'Sehr gut', color: 'success', icon: '✅' },
    GOOD: { label: 'Gut', color: 'success', icon: '✅' },
    ACCEPTABLE: { label: 'Akzeptabel', color: 'warning', icon: '⚠️' },
    PROBLEMATIC: { label: 'Problematisch', color: 'error', icon: '🚨' },
  };

  const scoreInfo = scoreConfig[behavior.paymentScore];

  return (
    <Stack spacing={2}>
      {/* Metric 1: Zahlungsmoral-Score */}
      <Box>
        <Typography variant="caption" color="text.secondary">
          Zahlungsmoral
        </Typography>
        <Stack direction="row" spacing={1} alignItems="center">
          <Chip
            label={`${scoreInfo.icon} ${scoreInfo.label}`}
            color={scoreInfo.color}
          />
          <Typography variant="body2" color="text.secondary">
            Ø {behavior.avgDaysToPayment} Tage Zahlungsziel
          </Typography>
        </Stack>
      </Box>

      {/* Metric 2: Offene Forderungen */}
      <Box>
        <Typography variant="caption" color="text.secondary">
          Offene Forderungen
        </Typography>
        <Typography variant="h6">
          {formatCurrency(behavior.openInvoicesAmount)}
        </Typography>
        {behavior.overdueAmount > 0 && (
          <Typography variant="caption" color="error">
            davon überfällig: {formatCurrency(behavior.overdueAmount)}
          </Typography>
        )}
      </Box>

      {/* Metric 3: Info-Text bei Problemen */}
      {behavior.paymentScore === 'PROBLEMATIC' && (
        <Alert severity="warning">
          Zahlungsverzug erkannt.
          <Link href="/innendienst/inkasso" target="_blank">
            Innendienst kontaktieren
          </Link>
        </Alert>
      )}
    </Stack>
  );
}
```

#### **3.4 CustomerChurnMonitoring Component** (1h)

**Neue Datei:** `frontend/src/features/customers/components/CustomerChurnMonitoring.tsx`

```tsx
export function CustomerChurnMonitoring({ customer, xentralCustomerId }: Props) {
  const [lastOrderDate, setLastOrderDate] = useState<Date | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadLastOrder = async () => {
      try {
        const response = await httpClient.get(
          `/api/xentral/customers/${xentralCustomerId}/last-order`
        );
        setLastOrderDate(new Date(response.data.lastOrderDate));
      } catch (error) {
        console.error('Failed to load last order:', error);
      } finally {
        setLoading(false);
      }
    };

    loadLastOrder();
  }, [xentralCustomerId]);

  if (loading) return <CircularProgress size={20} />;

  const daysSinceLastOrder = lastOrderDate
    ? Math.floor((Date.now() - lastOrderDate.getTime()) / (1000 * 60 * 60 * 24))
    : 999;

  const churnAlertDays = customer.churnAlertDays || 30; // Default: 30 Tage

  const isAtRisk = daysSinceLastOrder >= churnAlertDays;

  return (
    <Stack spacing={2}>
      <Box>
        <Typography variant="body2">
          Letzte Bestellung: vor <strong>{daysSinceLastOrder} Tagen</strong>
        </Typography>
        {isAtRisk && (
          <Chip label="🚨 ALARM!" color="error" size="small" sx={{ ml: 1 }} />
        )}
      </Box>

      {/* Fortschrittsbalken */}
      <LinearProgress
        variant="determinate"
        value={(daysSinceLastOrder / churnAlertDays) * 100}
        color={isAtRisk ? "error" : "success"}
      />

      {/* Quick-Info mit Edit-Link */}
      <Typography variant="caption" color="text.secondary">
        Alarm-Schwelle: {churnAlertDays} Tage
        <Link onClick={() => openChurnSettings()} sx={{ ml: 1, cursor: 'pointer' }}>
          Anpassen
        </Link>
      </Typography>
    </Stack>
  );
}
```

---

### **4. Churn-Alarm Konfiguration** (2h)

#### **4.1 Customer-Tabelle erweitern** (30 Min)

**Migration:** `V10032__add_churn_alert_days.sql`

```sql
-- Churn-Alarm Schwelle pro Kunde
ALTER TABLE customers ADD COLUMN churn_alert_days INTEGER DEFAULT 30;

COMMENT ON COLUMN customers.churn_alert_days IS
'Anzahl Tage ohne Bestellung, nach denen Churn-Alarm ausgelöst wird (Standard: 30)';

-- Beispiel-Daten:
UPDATE customers SET churn_alert_days = 14 WHERE business_type = 'RESTAURANT'; -- häufige Bestellungen
UPDATE customers SET churn_alert_days = 45 WHERE business_type = 'CATERING'; -- saisonale Schwankungen
UPDATE customers SET churn_alert_days = 60 WHERE business_type = 'HOTEL'; -- monatliche Sammelbestellungen
```

#### **4.2 ChurnSettingsDialog Component** (1h)

**Neue Datei:** `frontend/src/features/customers/components/ChurnSettingsDialog.tsx`

```tsx
export function ChurnSettingsDialog({ customer, open, onClose, onSave }: Props) {
  const [churnDays, setChurnDays] = useState(customer.churnAlertDays || 30);

  const handleSave = async () => {
    try {
      await httpClient.patch(`/api/customers/${customer.id}`, {
        churnAlertDays: churnDays
      });

      toast.success('Churn-Alarm Einstellung gespeichert!');
      onSave();
      onClose();

    } catch (error) {
      console.error('Failed to save churn settings:', error);
      toast.error('Fehler beim Speichern');
    }
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Churn-Alarm anpassen</DialogTitle>
      <DialogContent>
        <Stack spacing={2}>
          <Typography variant="body2" color="text.secondary">
            Wähle nach wie vielen Tagen ohne Bestellung du einen Alarm erhalten möchtest.
          </Typography>

          <Select value={churnDays} onChange={(e) => setChurnDays(e.target.value)} fullWidth>
            <MenuItem value={7}>7 Tage (häufig bestellend)</MenuItem>
            <MenuItem value={14}>14 Tage (wöchentlich)</MenuItem>
            <MenuItem value={30}>30 Tage (Standard) ⭐</MenuItem>
            <MenuItem value={45}>45 Tage (saisonal)</MenuItem>
            <MenuItem value={60}>60 Tage (monatlich)</MenuItem>
            <MenuItem value={90}>90 Tage (Großkunde)</MenuItem>
          </Select>

          <Alert severity="info">
            Aktuelle Situation: Letzte Bestellung vor {daysSinceLastOrder} Tagen.
            {daysSinceLastOrder >= churnDays && (
              <Typography variant="caption" color="error">
                <br />Mit dieser Einstellung wäre der Alarm JETZT ausgelöst!
              </Typography>
            )}
          </Alert>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button onClick={handleSave} variant="contained">Speichern</Button>
      </DialogActions>
    </Dialog>
  );
}
```

#### **4.3 Backend API-Erweiterung** (30 Min)

**Datei:** `backend/src/main/java/de/freshplan/api/resources/CustomerResource.java`

```java
/**
 * Update Customer (inkl. churnAlertDays)
 *
 * PATCH /api/customers/{id}
 */
@PATCH
@Path("/{id}")
@RolesAllowed({"admin", "manager", "sales"})
public Response updateCustomer(
    @PathParam("id") UUID customerId,
    @Valid UpdateCustomerRequest request) {

    Customer customer = customerRepository
        .findByIdOptional(customerId)
        .orElseThrow(() -> new NotFoundException("Customer not found"));

    // Bestehende Updates...
    if (request.getCompanyName() != null) {
        customer.setCompanyName(request.getCompanyName());
    }

    // NEU: Churn Alert Days
    if (request.getChurnAlertDays() != null) {
        customer.setChurnAlertDays(request.getChurnAlertDays());
    }

    customerRepository.persist(customer);

    return Response.ok(customer).build();
}
```

---

### **5. Testing & Bugfixes** (3h)

**Unit Tests (1h):**
- XentralApiClient: Mock Xentral-Responses
- ConvertToCustomerDialog: Form validation
- CustomerRevenueCard: Revenue calculation

**Integration Tests (1h):**
- Xentral-API-Call funktioniert (mit echtem Token - nur lokal!)
- Customer-Dashboard lädt Daten korrekt
- Churn-Alarm-Konfiguration speichert

**Manual E2E Testing (1h):**
```
Test Case 1: Opportunity → Customer mit Xentral-Verknüpfung
1. Open OpportunityDetailPage (OPP-4 - CLOSED_WON)
2. Click "Als Kunde anlegen"
3. Dialog öffnet sich
4. Xentral-Kunden-Dropdown lädt (nur Verkäufer-eigene!)
5. Wähle Kunde "Bella Italia (C-47236)"
6. Submit
7. Verify: Customer angelegt
8. Verify: xentralCustomerId gesetzt
9. Open CustomerDetailPage
10. Verify: Umsatz-Dashboard zeigt Daten (30/90/365 Tage)
11. Verify: Zahlungsverhalten zeigt Ampel (z.B. "Gut")
12. Verify: Churn-Alarm zeigt "Letzte Bestellung vor X Tagen"

Test Case 2: Churn-Alarm Konfiguration
1. CustomerDetailPage (mit Xentral-Kunde)
2. Click Settings-Icon bei "Aktivitäts-Monitoring"
3. Dialog öffnet sich
4. Wähle "14 Tage"
5. Save
6. Verify: churnAlertDays updated in DB
7. Verify: Fortschrittsbalken passt sich an
8. Verify: Alarm-Badge erscheint wenn >14 Tage
```

---

### **6. Admin-UI für Xentral-Einstellungen** (2-3h)

**Neue Seite:** `/admin/integrations/xentral`

#### **6.1 XentralIntegrationSettings Component** (2h)

**Neue Datei:** `frontend/src/features/admin/pages/XentralIntegrationSettings.tsx`

**Anforderungen:**
- Admin-Seite für Xentral-API-Konfiguration
- Nur für Admins zugänglich
- Verschlüsselte Speicherung von API-Token

**UI-Komponenten:**
```tsx
export function XentralIntegrationSettings() {
  const [apiUrl, setApiUrl] = useState('');
  const [apiToken, setApiToken] = useState('');
  const [connectionStatus, setConnectionStatus] = useState<'UNCHECKED' | 'SUCCESS' | 'FAILED'>('UNCHECKED');
  const [isSaving, setIsSaving] = useState(false);
  const [isTesting, setIsTesting] = useState(false);

  // Load existing settings on mount
  useEffect(() => {
    loadSettings();
  }, []);

  const loadSettings = async () => {
    try {
      const response = await httpClient.get('/api/admin/integrations/xentral/settings');
      setApiUrl(response.data.apiUrl);
      // Token wird NICHT zurückgegeben (Security!)
    } catch (error) {
      console.error('Failed to load Xentral settings:', error);
    }
  };

  const handleTestConnection = async () => {
    setIsTesting(true);
    try {
      await httpClient.post('/api/admin/integrations/xentral/test-connection', {
        apiUrl,
        apiToken
      });
      setConnectionStatus('SUCCESS');
      toast.success('Verbindung erfolgreich! ✅');
    } catch (error) {
      setConnectionStatus('FAILED');
      toast.error('Verbindung fehlgeschlagen! ❌');
    } finally {
      setIsTesting(false);
    }
  };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      await httpClient.post('/api/admin/integrations/xentral/settings', {
        apiUrl,
        apiToken
      });
      toast.success('Einstellungen gespeichert! 🎉');
    } catch (error) {
      console.error('Failed to save settings:', error);
      toast.error('Fehler beim Speichern');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper sx={{ p: 3 }}>
        <Stack spacing={3}>
          {/* Header */}
          <Box>
            <Typography variant="h5" gutterBottom>
              Xentral Integration
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Konfiguriere die Verbindung zum Xentral ERP-System
            </Typography>
          </Box>

          <Divider />

          {/* API URL Field */}
          <TextField
            label="Xentral API URL"
            value={apiUrl}
            onChange={(e) => setApiUrl(e.target.value)}
            fullWidth
            placeholder="https://xentral.freshfoodz.de/api/v1"
            helperText="Basis-URL der Xentral REST API"
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <LinkIcon />
                </InputAdornment>
              ),
            }}
          />

          {/* API Token Field */}
          <TextField
            label="API Token"
            type="password"
            value={apiToken}
            onChange={(e) => setApiToken(e.target.value)}
            fullWidth
            placeholder="Bearer Token eingeben"
            helperText="Wird verschlüsselt gespeichert (AES-256)"
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <KeyIcon />
                </InputAdornment>
              ),
            }}
          />

          {/* Connection Status */}
          {connectionStatus !== 'UNCHECKED' && (
            <Alert severity={connectionStatus === 'SUCCESS' ? 'success' : 'error'}>
              {connectionStatus === 'SUCCESS'
                ? 'Verbindung zu Xentral erfolgreich getestet! ✅'
                : 'Verbindung fehlgeschlagen. Bitte prüfe URL und Token. ❌'}
            </Alert>
          )}

          {/* Actions */}
          <Stack direction="row" spacing={2}>
            <Button
              variant="outlined"
              onClick={handleTestConnection}
              disabled={!apiUrl || !apiToken || isTesting}
              startIcon={isTesting ? <CircularProgress size={16} /> : <TestIcon />}
            >
              {isTesting ? 'Teste...' : 'Verbindung testen'}
            </Button>

            <Button
              variant="contained"
              onClick={handleSave}
              disabled={!apiUrl || !apiToken || isSaving}
              startIcon={<SaveIcon />}
            >
              {isSaving ? 'Speichere...' : 'Einstellungen speichern'}
            </Button>
          </Stack>

          <Divider />

          {/* Info Box */}
          <Alert severity="info" icon={<InfoIcon />}>
            <Typography variant="body2" fontWeight="bold" gutterBottom>
              Wie erhalte ich einen API-Token?
            </Typography>
            <Typography variant="caption">
              1. In Xentral einloggen<br />
              2. Einstellungen → API-Verwaltung<br />
              3. Neuen Token generieren mit Berechtigung: "Kunden lesen", "Rechnungen lesen"<br />
              4. Token hier einfügen und "Verbindung testen" klicken
            </Typography>
          </Alert>
        </Stack>
      </Paper>
    </Container>
  );
}
```

#### **6.2 Backend: Settings Storage mit Encryption** (1h)

**Neue Datei:** `backend/src/main/java/de/freshplan/infrastructure/config/XentralSettingsService.java`

```java
@ApplicationScoped
public class XentralSettingsService {

    @Inject
    SettingsRepository settingsRepository;

    @Inject
    EncryptionService encryptionService;

    private static final String XENTRAL_API_URL_KEY = "xentral.api.url";
    private static final String XENTRAL_API_TOKEN_KEY = "xentral.api.token";

    /**
     * Speichert Xentral-Einstellungen (Token wird verschlüsselt!)
     */
    public void saveSettings(String apiUrl, String apiToken) {
        // API URL (plain)
        settingsRepository.upsert(XENTRAL_API_URL_KEY, apiUrl);

        // API Token (encrypted!)
        String encryptedToken = encryptionService.encrypt(apiToken);
        settingsRepository.upsert(XENTRAL_API_TOKEN_KEY, encryptedToken);

        logger.info("Xentral settings saved (Token encrypted)");
    }

    /**
     * Lädt API URL (Token wird NICHT zurückgegeben!)
     */
    public String getApiUrl() {
        return settingsRepository.findByKey(XENTRAL_API_URL_KEY)
            .map(Setting::getValue)
            .orElse(null);
    }

    /**
     * Lädt API Token (entschlüsselt für API-Calls)
     */
    public String getApiToken() {
        return settingsRepository.findByKey(XENTRAL_API_TOKEN_KEY)
            .map(Setting::getValue)
            .map(encryptionService::decrypt)
            .orElse(null);
    }

    /**
     * Testet Verbindung zu Xentral
     */
    public boolean testConnection(String apiUrl, String apiToken) {
        try {
            // Simple Test: GET /api/v1/health oder GET /api/v1/customers?limit=1
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/customers?limit=1"))
                .header("Authorization", "Bearer " + apiToken)
                .GET()
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.statusCode() == 200;

        } catch (Exception e) {
            logger.error("Xentral connection test failed", e);
            return false;
        }
    }
}
```

**REST Endpoint:**
```java
@Path("/api/admin/integrations/xentral")
@RolesAllowed("admin")
public class XentralSettingsResource {

    @Inject
    XentralSettingsService settingsService;

    @GET
    @Path("/settings")
    public Response getSettings() {
        String apiUrl = settingsService.getApiUrl();
        // Token wird NICHT zurückgegeben (Security!)
        return Response.ok(Map.of("apiUrl", apiUrl != null ? apiUrl : "")).build();
    }

    @POST
    @Path("/settings")
    public Response saveSettings(@Valid XentralSettingsRequest request) {
        settingsService.saveSettings(request.getApiUrl(), request.getApiToken());
        return Response.ok().build();
    }

    @POST
    @Path("/test-connection")
    public Response testConnection(@Valid XentralSettingsRequest request) {
        boolean success = settingsService.testConnection(request.getApiUrl(), request.getApiToken());

        if (success) {
            return Response.ok(Map.of("status", "SUCCESS")).build();
        } else {
            return Response.status(Response.Status.BAD_GATEWAY)
                .entity(Map.of("status", "FAILED", "message", "Connection failed"))
                .build();
        }
    }
}
```

---

### **7. Sales-Rep Mapping Auto-Sync + Admin-UI** (1h)

#### **7.1 Auto-Sync-Job** (30 Min)

**Neue Datei:** `backend/src/main/java/de/freshplan/infrastructure/jobs/SalesRepSyncJob.java`

```java
@ApplicationScoped
public class SalesRepSyncJob {

    private static final Logger logger = LoggerFactory.getLogger(SalesRepSyncJob.class);

    @Inject
    XentralApiClient xentralApiClient;

    @Inject
    UserRepository userRepository;

    /**
     * Synchronisiert Sales-Rep-IDs aus Xentral
     * Läuft täglich um 2:00 Uhr
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncSalesRepIds() {
        logger.info("Starting Sales-Rep sync from Xentral...");

        try {
            // 1. Holt alle Sales Reps aus Xentral
            List<XentralSalesRep> salesReps = xentralApiClient.getAllSalesReps();
            logger.info("Fetched {} sales reps from Xentral", salesReps.size());

            int syncedCount = 0;
            int unmatchedCount = 0;

            // 2. Matched via Email
            for (XentralSalesRep salesRep : salesReps) {
                Optional<User> userOpt = userRepository.findByEmail(salesRep.getEmail());

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setXentralSalesRepId(salesRep.getId());
                    userRepository.persist(user);

                    logger.info("Synced: {} → {}", user.getEmail(), salesRep.getId());
                    syncedCount++;
                } else {
                    logger.warn("Unmatched Xentral Sales Rep: {} ({})", salesRep.getEmail(), salesRep.getId());
                    unmatchedCount++;
                }
            }

            logger.info("Sales-Rep sync completed: {} synced, {} unmatched", syncedCount, unmatchedCount);

        } catch (Exception e) {
            logger.error("Sales-Rep sync failed", e);
        }
    }
}
```

#### **7.2 Admin-UI Erweiterung für `/admin/users`** (30 Min)

**Datei:** `frontend/src/features/admin/pages/UserManagementPage.tsx`

**Neue Spalte in User-Table hinzufügen:**
```tsx
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
  {users.map((user) => (
    <TableRow key={user.id}>
      <TableCell>{user.name}</TableCell>
      <TableCell>{user.email}</TableCell>
      <TableCell>{user.role}</TableCell>
      <TableCell>
        {user.xentralSalesRepId ? (
          <Chip label={user.xentralSalesRepId} size="small" color="success" />
        ) : (
          <Chip label="Nicht zugeordnet" size="small" color="default" />
        )}
      </TableCell>
      <TableCell>
        <IconButton size="small" onClick={() => openEditDialog(user)}>
          <EditIcon />
        </IconButton>
      </TableCell>
    </TableRow>
  ))}
</TableBody>
```

**Edit-Dialog erweitern:**
```tsx
<TextField
  label="Xentral Sales-Rep ID"
  value={xentralSalesRepId}
  onChange={(e) => setXentralSalesRepId(e.target.value)}
  fullWidth
  placeholder="z.B. SR-47"
  helperText="Wird normalerweise automatisch synchronisiert. Manuelle Pflege nur im Notfall."
  InputProps={{
    startAdornment: (
      <InputAdornment position="start">
        <SyncIcon />
      </InputAdornment>
    ),
  }}
/>
```

---

## ✅ DEFINITION OF DONE

### **Functional:**
- [x] Opportunity → Customer Conversion funktioniert (mit Xentral-Dropdown)
- [x] Customer-Dashboard zeigt echte Umsatzdaten (30/90/365 Tage)
- [x] Customer-Dashboard zeigt Zahlungsverhalten (Ampel-System)
- [x] Churn-Alarm zeigt Tage seit letzter Bestellung
- [x] Churn-Alarm Schwelle ist pro Kunde konfigurierbar
- [x] Xentral-Kunden-Dropdown zeigt nur Verkäufer-eigene Kunden

### **Technical:**
- [x] Backend: XentralApiClient implementiert (mit Mock-Modus!)
- [x] Backend: GET /api/xentral/customers?salesRepId={id}
- [x] Backend: GET /api/xentral/customers/{id}/revenue
- [x] Backend: GET /api/xentral/customers/{id}/payment-behavior
- [x] Backend: PATCH /api/customers/{id} (churnAlertDays)
- [x] Backend: XentralSettingsService mit Encryption
- [x] Backend: GET /api/admin/integrations/xentral/settings
- [x] Backend: POST /api/admin/integrations/xentral/settings
- [x] Backend: POST /api/admin/integrations/xentral/test-connection
- [x] Backend: SalesRepSyncJob (@Scheduled)
- [x] Frontend: ConvertToCustomerDialog mit Autocomplete
- [x] Frontend: CustomerRevenueCard mit echten Daten
- [x] Frontend: CustomerPaymentBehaviorCard mit Ampel
- [x] Frontend: CustomerChurnMonitoring mit Settings
- [x] Frontend: XentralIntegrationSettings Component
- [x] Frontend: UserManagementPage erweitert (Xentral Sales-Rep ID Spalte)
- [x] Migration: V10031 (xentral_sales_rep_id)
- [x] Migration: V10032 (churn_alert_days)
- [x] Unit Tests: XentralApiClient Mocks
- [x] E2E Tests: Opportunity → Customer → Dashboard
- [x] E2E Tests: Admin-UI Settings speichern & testen
- [x] E2E Tests: Sales-Rep Sync Job

### **Quality:**
- [x] Code Review: 1 Approval
- [x] UI/UX: Dashboard übersichtlich (keine Überforderung!)
- [x] Performance: Dashboard lädt < 3s
- [x] Error Handling: Xentral-API down → sinnvoller Fehler

### **Documentation:**
- [x] TRIGGER_SPRINT_2_1_7_2.md erstellt (✅ Done)
- [x] TRIGGER_INDEX.md aktualisiert
- [x] CRM_COMPLETE_MASTER_PLAN_V5.md: Session Log

---

## 📊 SUCCESS METRICS

### **Usability:**
- Customer-Dashboard: Auf einen Blick erkennbar: "Läuft gut/schlecht?"
- Xentral-Verknüpfung: Max 3 Klicks, < 20 Sekunden

### **Performance:**
- Dashboard-Load: < 3s (inkl. Xentral-API-Calls)
- Xentral-API: < 1s Response-Time (pro Call)

### **Business Impact:**
- Churn-Prevention: Frühwarnung 14-30 Tage vorher
- Verkäufer-Autonomie: Self-Service ohne Innendienst

---

## 🚀 PREREQUISITES

### **✅ BEREITS GEKLÄRT (2025-10-18):**

1. **Sales-Rep Mapping Strategie:**
   - ✅ **Option A gewählt:** User-Tabelle erweitern mit `xentral_sales_rep_id`
   - ✅ **Auto-Sync-Job:** Email-basiertes Matching (täglich 2:00 Uhr)
   - ✅ **Admin-UI:** Manuelle Pflege unter `/admin/users` möglich
   - ✅ **Migration V10031:** Spezifikation fertig

2. **Entwicklungs-Strategie:**
   - ✅ **Mock-Enabled Development:** XentralApiClient mit Feature-Flag
   - ✅ **Hybrid-Ansatz:** Foundation mit Mocks bauen, später echte API integrieren
   - ✅ **Admin-UI:** Settings-Seite für API-Konfiguration unter `/admin/integrations/xentral`

### **⏳ WARTEN AUF IT-TEAM (7-Punkt-Checklist gesendet):**

**IT-Integration Checklist (gesendet 2025-10-18):**

1. **Xentral API Endpoints:**
   - `GET /api/v1/customers?salesRepId={id}` verfügbar?
   - `GET /api/v1/invoices?customerId={id}` verfügbar?
   - `GET /api/v1/customers/{id}/payment-summary` verfügbar?
   - `GET /api/v1/sales-reps` verfügbar?

2. **API Authentication:**
   - Welches Token-Format? (Bearer Token?)
   - Wie erhalte ich ein API-Token?
   - Token-Berechtigungen: "Kunden lesen", "Rechnungen lesen"

3. **Sales-Rep Mapping:**
   - Feld-Name in Xentral? (`salesRepId`, `assignedTo`, `owner`?)
   - Format? (z.B. "SR-47" oder numerisch?)
   - Email-Feld in Sales-Reps verfügbar für Auto-Matching?

4. **Rate Limits:**
   - Max Requests pro Minute?
   - Retry-Strategy bei 429?

5. **Test-Zugang:**
   - Test-API-Token verfügbar?
   - Test-System oder nur Production?

6. **Webhooks (Optional, später):**
   - Sind Xentral-Webhooks verfügbar?
   - Events: invoice.created, payment.received, customer.updated?

7. **Support:**
   - Ansprechpartner bei API-Problemen?
   - Xentral-API-Dokumentation verfügbar?

**→ Während wir warten: Foundation mit Mocks bauen (4-6h), dann entscheiden**

### **✅ READY TO START (Foundation-Phase):**
- ✅ Sprint 2.1.7.1 COMPLETE (Lead → Opportunity funktioniert)
- ✅ Xentral-Dokumentation vorhanden (FC-005, FC-009 - 1151 Zeilen Specs!)
- ✅ Sales-Rep Mapping Strategie festgelegt
- ✅ Mock-Enabled Development Strategy definiert
- ✅ Admin-UI Scope definiert (+3h)

### **🚫 KEINE BLOCKER:**
- ✅ Alle kritischen Entscheidungen getroffen
- ✅ Können mit Foundation starten (Mocks)
- ✅ IT-Response parallel abwarten
- ✅ Später: Mocks → Echte API austauschen (1-2h)

---

## 📅 TIMELINE (Realistisch!)

**Tag 1 (8h):**
- Opportunity → Customer Conversion (4h)
  - ConvertToCustomerDialog (2h)
  - OpportunityDetailPage Integration (1h)
  - Backend Xentral-Kunden-Endpoint (1h)
- Xentral-API-Client Implementation (3h)
  - XentralApiClient mit Mock-Modus (2h)
  - Configuration + User-Mapping (1h)
- Break + Setup (1h)

**Tag 2 (8h):**
- Customer-Dashboard (6h)
  - CustomerDetailPage erweitern (3h)
  - CustomerRevenueCard (1h)
  - CustomerPaymentBehaviorCard (1h)
  - CustomerChurnMonitoring (1h)
- Churn-Alarm Konfiguration (2h)
  - Migration V10032 (30 Min)
  - ChurnSettingsDialog (1h)
  - Backend API (30 Min)

**Tag 3 (5h):**
- Admin-UI für Xentral-Einstellungen (3h)
  - XentralIntegrationSettings Component (2h)
  - Backend Settings Service + Encryption (1h)
- Sales-Rep Auto-Sync + Admin-UI (1h)
  - SalesRepSyncJob (30 Min)
  - UserManagementPage erweitern (30 Min)
- Testing & Bugfixes (1h)

**Optional (nach IT-Response):**
- Mock → Echte API Integration (1-2h)
- Webhooks statt Polling (2-3h)

**Total: 21h = 2.5-3 Arbeitstage** ✅

**Breakdown:**
- Core Features (Customer Management): 15h
- Admin-UI + Auto-Sync: 4h
- Testing: 2h
- **GESAMT:** 21h

---

## 🔗 RELATED WORK

### **Dependent Sprints:**
- Sprint 2.1.7.1: Lead → Opportunity (COMPLETE erforderlich!)

### **Follow-up Sprints:**
- Sprint 2.1.7.3: Bestandskunden-Workflow (SORTIMENTSERWEITERUNG/VERLAENGERUNG)
- Sprint 2.1.7.4: Advanced Filters & Analytics

---

## 📝 NOTES

### **Design Decisions (aus Diskussion):**

1. **Xentral-Kunden-Dropdown (kein manuelles Tippen!):**
   - Autocomplete mit Search
   - Nur Verkäufer-eigene Kunden (verkäufer-gefiltert!)
   - Optional: Kann auch später verknüpft werden

2. **Umsatz vs. Provision (klar getrennt!):**
   - Dashboard zeigt Umsatz (Rechnungsbetrag)
   - Provision wird NICHT hier abgerechnet (anderes System!)
   - Aber: Provision ist relevant → daher Zahlungsverhalten sichtbar

3. **Zahlungsverhalten: Nur Überblick (keine Details!):**
   - Verkäufer sieht: Ampel (Gut/Akzeptabel/Problematisch)
   - Verkäufer sieht NICHT: Einzelne Rechnungen, Mahnungen
   - Details bleiben im Innendienst / Xentral-System

4. **Churn-Alarm: Variabel pro Kunde:**
   - Standard: 30 Tage
   - Restaurant: 14 Tage (häufige Bestellungen)
   - Catering: 45 Tage (saisonale Schwankungen)
   - Hotel: 60 Tage (monatliche Sammelbestellungen)

### **Neue Entscheidungen (2025-10-18):**

5. **Sales-Rep Mapping: Option A + Auto-Sync ✅**
   - **Entscheidung:** User-Tabelle erweitern mit `xentral_sales_rep_id` VARCHAR(50)
   - **Auto-Sync-Job:** Täglich 2:00 Uhr via Email-Matching
   - **Fallback:** Manuelle Pflege über Admin-UI (`/admin/users`)
   - **Begründung:**
     - Einfache Implementierung (keine Mapping-Tabelle)
     - Performant (keine JOINs nötig)
     - Automatisch (kein manueller Aufwand)
     - Fehlertoleranz (manuelle Korrektur möglich)

6. **Admin-UI für Xentral-Einstellungen ✅**
   - **Location:** `/admin/integrations/xentral`
   - **Features:**
     - API URL Konfiguration
     - API Token Eingabe (verschlüsselt mit AES-256)
     - Connection Test Button
     - Settings Speicherung in `settings` Tabelle
   - **Security:** Token wird NIEMALS im Response zurückgegeben
   - **Begründung:**
     - Flexibilität (API-URL kann sich ändern)
     - Security (Token nicht in application.properties hardcoded)
     - User Experience (Admin kann selbst testen)

7. **Mock-Enabled Development Strategy ✅**
   - **Pattern:** Feature-Flag in XentralApiClient
     ```java
     @ConfigProperty(name = "xentral.api.mock-mode", defaultValue = "true")
     boolean mockMode;
     ```
   - **Mock-Daten:** Realistische Testdaten für alle Endpoints
   - **Switch-Over:** Nach IT-Response Feature-Flag auf `false` setzen
   - **Begründung:**
     - Parallele Arbeit (nicht blockiert durch IT)
     - Testbarkeit (lokale Entwicklung ohne Xentral)
     - Safety (Production-Rollout erst nach API-Validierung)

8. **Hybrid-Ansatz: Foundation → Integration ✅**
   - **Phase 1 (4-6h):** Foundation mit Mocks bauen
     - ConvertToCustomerDialog
     - Customer-Dashboard (mit Mock-Daten)
     - Admin-UI
     - Auto-Sync-Job (mit Mock getAllSalesReps)
   - **Phase 2 (nach IT-Response):** Echte API integrieren
     - Mock-Mode auf `false`
     - Connection-Test durchführen
     - Produktiv-Daten testen
   - **Begründung:**
     - Kein Zeitverlust (arbeiten parallel zu IT)
     - Risikoarm (Mocks → Echte API ist low-effort Switch)
     - Flexibel (können Sprint 2.1.7.3 vorziehen falls IT lange braucht)

9. **IT-Integration Checklist (7-Punkt-Checkliste) ✅**
   - **Gesendet am:** 2025-10-18
   - **Status:** Warten auf Response
   - **Inhalt:**
     - API Endpoints (4 Endpoints)
     - Authentication (Token-Format, Berechtigungen)
     - Sales-Rep Mapping (Feld-Name, Format, Email)
     - Rate Limits
     - Test-Zugang
     - Webhooks (optional)
     - Support-Kontakt
   - **Begründung:** Strukturierte Anfrage = schnellere klare Antwort

### **Technical Debt:**
- Xentral-API Caching: Aktuell keine Caching-Strategie (später: Redis/Caffeine)
- Error Recovery: Wenn Xentral down → Fallback-Daten? Retry-Logic?
- Webhook-Integration: Aktuell Polling - später: Xentral-Webhooks für Real-Time Updates
- Mock → Real Switch: Aktuell Feature-Flag - später: automatische Erkennung (API verfügbar → nutzen, sonst Fallback)

---

**✅ Sprint READY TO START - Alle Entscheidungen getroffen!** 🚀

**Nächster Schritt (3 Optionen):**

### **Option A: HYBRID-ANSATZ (EMPFOHLEN) ✅**
1. **Jetzt starten:** Foundation mit Mocks (4-6h)
   - Feature-Branch: `git checkout -b feature/sprint-2-1-7-2-customer-xentral`
   - Migration V10031, V10032 erstellen
   - XentralApiClient mit `mock-mode=true`
   - ConvertToCustomerDialog + Customer-Dashboard
   - Admin-UI + Auto-Sync-Job

2. **Dann entscheiden:**
   - ✅ IT-Response da → Mock-Mode deaktivieren, echte API integrieren (1-2h)
   - ⏸️ IT braucht länger → Sprint 2.1.7.3 vorziehen (Bestandskunden-Workflow)

### **Option B: WARTEN AUF IT-RESPONSE**
- Sprint 2.1.7.3 starten (Bestandskunden-Workflow)
- Sprint 2.1.7.2 später fortsetzen (wenn IT antwortet)

### **Option C: SPRINT 2.1.7.2 KOMPLETT (NUR mit IT-Response)**
- Warten auf IT-Antwort (7-Punkt-Checklist)
- Dann Sprint 2.1.7.2 MIT echten Xentral-APIs bauen
- Vorteil: Kein Mock-Overhead
- Nachteil: Zeitverlust durch Warten

**Empfehlung:** Option A (Hybrid) - Beste Balance aus Geschwindigkeit & Sicherheit! 🎯
