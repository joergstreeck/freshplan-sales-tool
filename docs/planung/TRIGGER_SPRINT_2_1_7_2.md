# 🚀 Sprint 2.1.7.2 - Customer-Management + Xentral-Integration

**Sprint-ID:** 2.1.7.2
**Status:** 📋 PLANNING
**Priority:** P1 (High)
**Estimated Effort:** 18h (2-3 Arbeitstage)
**Owner:** TBD
**Created:** 2025-10-16
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
- ✅ FC-005: Invoice/Payment APIs dokumentiert
- ✅ FC-009: Contract APIs dokumentiert
- ✅ Xentral läuft BEREITS (Production-Ready)
- ✅ Event-Payloads spezifiziert (840 Zeilen Doku!)

**Backend existiert bereits:**
- ✅ V261: `customers.original_lead_id` (Lead → Customer Traceability)
- ✅ OpportunityService.convertToCustomer() (Backend fertig!)
- ✅ REST API: POST /api/opportunities/{id}/convert-to-customer

**Was JETZT gebaut wird:**
- ✅ ConvertToCustomerDialog mit Xentral-Kunden-Auswahl (Dropdown!)
- ✅ XentralApiClient Integration (Umsatz + Zahlungsverhalten abrufen)
- ✅ Customer-Dashboard mit echten Daten (keine Placeholders!)
- ✅ Churn-Alarm mit variabler Schwelle (pro Kunde konfigurierbar)

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

**KRITISCH zu klären (vor Sprint-Start!):**

**Option A: User-Tabelle erweitern (empfohlen!):**
```sql
-- Migration: V10031__add_xentral_sales_rep_id.sql
ALTER TABLE app_user ADD COLUMN xentral_sales_rep_id VARCHAR(50);

COMMENT ON COLUMN app_user.xentral_sales_rep_id IS
'Xentral Sales Rep ID für API-Filtering (z.B. "SR-47")';

-- Beispiel-Daten:
UPDATE app_user SET xentral_sales_rep_id = 'SR-47' WHERE username = 'max.mueller';
UPDATE app_user SET xentral_sales_rep_id = 'SR-89' WHERE username = 'anna.schmidt';
```

**Option B: Mapping-Tabelle (falls komplexer):**
```sql
CREATE TABLE user_xentral_mapping (
    freshfoodz_user_id UUID PRIMARY KEY REFERENCES app_user(id),
    xentral_sales_rep_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

**Option C: Username-basiert (falls möglich):**
```java
// Wenn FreshFoodz.username == Xentral.salesRepId
String salesRepId = currentUser.getUsername(); // "max.mueller"
```

**→ Muss VOR Sprint geklärt werden!**

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

## ✅ DEFINITION OF DONE

### **Functional:**
- [x] Opportunity → Customer Conversion funktioniert (mit Xentral-Dropdown)
- [x] Customer-Dashboard zeigt echte Umsatzdaten (30/90/365 Tage)
- [x] Customer-Dashboard zeigt Zahlungsverhalten (Ampel-System)
- [x] Churn-Alarm zeigt Tage seit letzter Bestellung
- [x] Churn-Alarm Schwelle ist pro Kunde konfigurierbar
- [x] Xentral-Kunden-Dropdown zeigt nur Verkäufer-eigene Kunden

### **Technical:**
- [x] Backend: XentralApiClient implementiert
- [x] Backend: GET /api/xentral/customers?salesRepId={id}
- [x] Backend: GET /api/xentral/customers/{id}/revenue
- [x] Backend: GET /api/xentral/customers/{id}/payment-behavior
- [x] Backend: PATCH /api/customers/{id} (churnAlertDays)
- [x] Frontend: ConvertToCustomerDialog mit Autocomplete
- [x] Frontend: CustomerRevenueCard mit echten Daten
- [x] Frontend: CustomerPaymentBehaviorCard mit Ampel
- [x] Frontend: CustomerChurnMonitoring mit Settings
- [x] Migration: V10031 (xentral_sales_rep_id)
- [x] Migration: V10032 (churn_alert_days)
- [x] Unit Tests: XentralApiClient Mocks
- [x] E2E Tests: Opportunity → Customer → Dashboard

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

### **KRITISCH zu klären VOR Sprint-Start:**

1. **Xentral Sales-Rep Mapping:**
   - [ ] Wie heißt das Feld in Xentral? (`salesRepId`, `assignedTo`, `owner`?)
   - [ ] Mapping-Strategie entscheiden (Option A/B/C)
   - [ ] Migration V10031 vorbereiten

2. **Xentral-API Zugriff testen:**
   - [ ] `GET /customers?salesRepId=???` funktioniert?
   - [ ] `GET /customers/{id}/payment-summary` funktioniert?
   - [ ] API-Token verfügbar?

### **Ready to Start:**
- ✅ Sprint 2.1.7.1 COMPLETE (Lead → Opportunity funktioniert)
- ✅ Xentral-Dokumentation vorhanden (FC-005, FC-009)
- ✅ Xentral läuft (Production-Ready)

### **Blockers:**
- ⚠️ Xentral Sales-Rep Mapping unklar (MUSS geklärt werden!)

---

## 📅 TIMELINE (Realistisch!)

**Tag 1 (8h):**
- Opportunity → Customer Conversion (4h)
  - ConvertToCustomerDialog (2h)
  - OpportunityDetailPage Integration (1h)
  - Backend Xentral-Kunden-Endpoint (1h)
- Xentral-API-Client Implementation (3h)
  - XentralApiClient (2h)
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

**Tag 3 (2h oder später):**
- Testing & Bugfixes (2h)
- Optional: Polish & Performance-Optimierung

**Total: 18h = 2-3 Arbeitstage** ✅

---

## 🔗 RELATED WORK

### **Dependent Sprints:**
- Sprint 2.1.7.1: Lead → Opportunity (COMPLETE erforderlich!)

### **Follow-up Sprints:**
- Sprint 2.1.7.3: RENEWAL-Workflow (Upsell/Cross-sell)
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

### **Technical Debt:**
- Xentral-API Caching: Aktuell keine Caching-Strategie (später: Redis/Caffeine)
- Error Recovery: Wenn Xentral down → Fallback-Daten? Retry-Logic?
- Webhook-Integration: Aktuell Polling - später: Xentral-Webhooks für Real-Time Updates

---

**Sprint bereit für Kickoff (nach Xentral-Mapping-Klärung)!** 🚀

**Nächster Schritt:**
1. **VOR Sprint-Start:** Xentral-API testen (30 Min)
   ```bash
   curl -X GET "https://xentral/api/v1/customers?salesRepId=???" \
     -H "Authorization: Bearer {token}"
   ```
2. **Migration V10031 erstellen:** User.xentralSalesRepId Feld
3. Feature-Branch: `git checkout -b feature/sprint-2-1-7-2-customer-xentral`
4. Los geht's! 💪
