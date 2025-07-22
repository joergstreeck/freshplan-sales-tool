# FC-005: Xentral Integration & Provisions-Engine 🚀 CLAUDE_TECH

**Feature Code:** FC-005  
**Optimiert für:** Claude's 30-Sekunden-Produktivität  
**Original:** 1138 Zeilen → **Optimiert:** ~550 Zeilen (52% Reduktion)

## 🎯 QUICK-LOAD: Sofort produktiv in 30 Sekunden!

### Was macht FC-005?
**Xentral ERP Integration mit automatischer Provisionsberechnung via Webhooks + REST API**

### Die 3 Kern-Komponenten:
1. **Webhook Handler** → Empfängt Real-time Events von Xentral (Zahlungen, Aufträge)
2. **Commission Engine** → Berechnet Provisionen basierend auf konfigurierbaren Regeln
3. **Sync Service** → Bidirektionale Synchronisation von Kunden- und Auftragsdaten

### Sofort starten:
```bash
# Backend: Webhook Handler implementieren
cd backend
./mvnw quarkus:add-extension -Dextensions="resteasy-reactive-jackson,hibernate-validator"

# Frontend: Commission Dashboard
cd frontend
npm install recharts date-fns
```

---

## 📦 1. BACKEND: Copy-paste Recipes

### 1.1 Webhook Handler (5 Minuten)
```java
@Path("/api/webhooks/xentral")
@Tag(name = "Xentral Webhooks")
public class XentralWebhookResource {
    
    @Inject EventBus eventBus;
    @Inject WebhookValidator validator;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response handleWebhook(
            @HeaderParam("X-Xentral-Signature") String signature,
            JsonNode payload) {
        
        // Signature validation
        if (!validator.isValid(signature, payload)) {
            return Response.status(401).entity("Invalid signature").build();
        }
        
        // Event routing
        String eventType = payload.get("event_type").asText();
        switch (eventType) {
            case "payment.received":
                eventBus.publish(new PaymentReceivedEvent(payload));
                break;
            case "invoice.created":
                eventBus.publish(new InvoiceCreatedEvent(payload));
                break;
            case "customer.updated":
                eventBus.publish(new CustomerUpdatedEvent(payload));
                break;
        }
        
        return Response.ok().build();
    }
}

// Webhook Signature Validator
@ApplicationScoped
public class WebhookValidator {
    @ConfigProperty(name = "xentral.webhook.secret")
    String webhookSecret;
    
    public boolean isValid(String signature, JsonNode payload) {
        String expectedSignature = HmacUtils.hmacSha256Hex(
            webhookSecret, payload.toString()
        );
        return MessageDigest.isEqual(
            signature.getBytes(), 
            expectedSignature.getBytes()
        );
    }
}
```

### 1.2 Commission Engine (10 Minuten)
```java
@ApplicationScoped
public class CommissionEngine {
    
    @Inject CommissionRuleRepository ruleRepository;
    @Inject CommissionRepository commissionRepository;
    @Inject NotificationService notificationService;
    
    public void onPaymentReceived(@Observes PaymentReceivedEvent event) {
        // Extract payment data
        PaymentData payment = event.getPaymentData();
        
        // Find applicable commission rules
        List<CommissionRule> rules = ruleRepository.findActiveRulesForSalesperson(
            payment.getSalespersonId()
        );
        
        // Calculate commission
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (CommissionRule rule : rules) {
            if (rule.appliesTo(payment)) {
                BigDecimal commission = rule.calculate(payment.getAmount());
                totalCommission = totalCommission.add(commission);
                
                // Create commission record
                Commission c = new Commission();
                c.setInvoiceId(payment.getInvoiceId());
                c.setSalespersonId(payment.getSalespersonId());
                c.setRuleId(rule.getId());
                c.setAmount(commission);
                c.setStatus(CommissionStatus.PENDING);
                c.setCalculatedAt(LocalDateTime.now());
                commissionRepository.persist(c);
            }
        }
        
        // Notify salesperson
        if (totalCommission.compareTo(BigDecimal.ZERO) > 0) {
            notificationService.notifyCommissionEarned(
                payment.getSalespersonId(), 
                totalCommission,
                payment.getInvoiceNumber()
            );
        }
    }
}

// Commission Rules
@Entity
@Table(name = "commission_rules")
public class CommissionRule {
    @Id @GeneratedValue
    private UUID id;
    
    private String name;
    private BigDecimal percentage;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    
    @Enumerated(EnumType.STRING)
    private CommissionType type; // FLAT, TIERED, GRADUATED
    
    @ElementCollection
    @CollectionTable(name = "commission_tiers")
    private List<CommissionTier> tiers;
    
    public BigDecimal calculate(BigDecimal amount) {
        switch (type) {
            case FLAT:
                return amount.multiply(percentage).divide(new BigDecimal(100));
            case TIERED:
                return calculateTiered(amount);
            case GRADUATED:
                return calculateGraduated(amount);
        }
        return BigDecimal.ZERO;
    }
}
```

### 1.3 Xentral API Client (5 Minuten)
```java
@RegisterRestClient(configKey = "xentral-api")
@RegisterProvider(XentralAuthFilter.class)
public interface XentralApiClient {
    
    @GET
    @Path("/customers")
    List<XentralCustomer> getCustomers(@QueryParam("limit") int limit);
    
    @GET
    @Path("/invoices")
    List<XentralInvoice> getInvoices(
        @QueryParam("from") LocalDate from,
        @QueryParam("to") LocalDate to
    );
    
    @GET
    @Path("/payments")
    List<XentralPayment> getPayments(@QueryParam("invoice_id") String invoiceId);
}

// OAuth Filter
@Provider
public class XentralAuthFilter implements ClientRequestFilter {
    @Inject XentralTokenService tokenService;
    
    @Override
    public void filter(ClientRequestContext requestContext) {
        String token = tokenService.getValidToken();
        requestContext.getHeaders().add("Authorization", "Bearer " + token);
    }
}

// Token Management
@ApplicationScoped
public class XentralTokenService {
    @ConfigProperty(name = "xentral.oauth.client-id")
    String clientId;
    
    @ConfigProperty(name = "xentral.oauth.client-secret")
    String clientSecret;
    
    private String cachedToken;
    private LocalDateTime tokenExpiry;
    
    public synchronized String getValidToken() {
        if (cachedToken == null || LocalDateTime.now().isAfter(tokenExpiry)) {
            refreshToken();
        }
        return cachedToken;
    }
}
```

### 1.4 Sync Service (10 Minuten)
```java
@ApplicationScoped
public class XentralSyncService {
    
    @Inject XentralApiClient xentralApi;
    @Inject CustomerService customerService;
    @Inject SyncStateRepository syncStateRepo;
    
    @Scheduled(every = "30m")
    @Transactional
    public void syncCustomers() {
        SyncState lastSync = syncStateRepo.findLastSync("customers");
        LocalDateTime syncStart = LocalDateTime.now();
        
        try {
            List<XentralCustomer> xentralCustomers = xentralApi.getCustomers(1000);
            
            for (XentralCustomer xc : xentralCustomers) {
                Customer existing = customerService.findByXentralId(xc.getId());
                
                if (existing == null) {
                    // Create new
                    Customer c = mapToCustomer(xc);
                    customerService.create(c);
                } else if (xc.getUpdatedAt().isAfter(lastSync.getLastRun())) {
                    // Update existing
                    updateCustomer(existing, xc);
                    customerService.update(existing);
                }
            }
            
            // Update sync state
            lastSync.setLastRun(syncStart);
            lastSync.setStatus(SyncStatus.SUCCESS);
            lastSync.setRecordCount(xentralCustomers.size());
            syncStateRepo.persist(lastSync);
            
        } catch (Exception e) {
            lastSync.setStatus(SyncStatus.FAILED);
            lastSync.setErrorMessage(e.getMessage());
            throw e;
        }
    }
}
```

---

## 🎨 2. FRONTEND: Commission Dashboard

### 2.1 Commission Overview (5 Minuten)
```typescript
export const CommissionDashboard: React.FC = () => {
  const { data: commissions } = useQuery({
    queryKey: ['commissions', 'current-month'],
    queryFn: () => api.getCommissions({ 
      from: startOfMonth(new Date()),
      to: endOfMonth(new Date())
    })
  });

  const { data: syncStatus } = useQuery({
    queryKey: ['xentral-sync-status'],
    queryFn: api.getXentralSyncStatus,
    refetchInterval: 30000 // 30 seconds
  });

  return (
    <Box sx={{ p: 3 }}>
      <Grid container spacing={3}>
        {/* KPI Cards */}
        <Grid item xs={12} md={3}>
          <KpiCard
            title="Provisionen (Monat)"
            value={formatCurrency(commissions?.total || 0)}
            trend={commissions?.trend}
            icon={<EuroIcon />}
          />
        </Grid>
        
        {/* Commission Chart */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="h6">Provisionsverlauf</Typography>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={commissions?.daily}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip formatter={formatCurrency} />
                <Line 
                  type="monotone" 
                  dataKey="amount" 
                  stroke="#8884d8" 
                  strokeWidth={2}
                />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Sync Status */}
        <Grid item xs={12} md={4}>
          <SyncStatusPanel status={syncStatus} />
        </Grid>

        {/* Recent Commissions */}
        <Grid item xs={12}>
          <CommissionTable commissions={commissions?.recent || []} />
        </Grid>
      </Grid>
    </Box>
  );
};

// Sync Status Component
const SyncStatusPanel: React.FC<{ status: SyncStatus }> = ({ status }) => (
  <Paper sx={{ p: 2 }}>
    <Typography variant="h6" gutterBottom>
      Xentral Sync Status
    </Typography>
    
    {status?.modules.map(module => (
      <Box key={module.name} sx={{ mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
          <Typography>{module.name}</Typography>
          <Chip 
            label={module.status} 
            color={module.status === 'SUCCESS' ? 'success' : 'error'}
            size="small"
          />
        </Box>
        <Typography variant="caption" color="text.secondary">
          Letzter Sync: {formatRelative(module.lastSync)}
        </Typography>
      </Box>
    ))}
    
    <Button 
      variant="outlined" 
      size="small" 
      onClick={() => api.triggerSync()}
      sx={{ mt: 2 }}
    >
      Manueller Sync
    </Button>
  </Paper>
);
```

### 2.2 Commission Rules Configuration (5 Minuten)
```typescript
export const CommissionRulesConfig: React.FC = () => {
  const [rules, setRules] = useState<CommissionRule[]>([]);
  
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Provisionsregeln
      </Typography>
      
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Typ</TableCell>
              <TableCell>Prozentsatz</TableCell>
              <TableCell>Bedingungen</TableCell>
              <TableCell>Aktionen</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rules.map(rule => (
              <TableRow key={rule.id}>
                <TableCell>{rule.name}</TableCell>
                <TableCell>{rule.type}</TableCell>
                <TableCell>{rule.percentage}%</TableCell>
                <TableCell>
                  {rule.minAmount && `Min: ${formatCurrency(rule.minAmount)}`}
                  {rule.maxAmount && ` Max: ${formatCurrency(rule.maxAmount)}`}
                </TableCell>
                <TableCell>
                  <IconButton onClick={() => editRule(rule)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton onClick={() => deleteRule(rule.id)}>
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
      
      <Button 
        variant="contained" 
        startIcon={<AddIcon />}
        onClick={openRuleDialog}
        sx={{ mt: 2 }}
      >
        Neue Regel
      </Button>
    </Box>
  );
};
```

---

## 🗄️ 3. DATENBANK: Schema & Migration

### 3.1 Flyway Migration (Copy-paste ready)
```sql
-- V1.0.0__xentral_integration.sql

-- Commission Rules
CREATE TABLE commission_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('FLAT', 'TIERED', 'GRADUATED')),
    percentage DECIMAL(5,2),
    min_amount DECIMAL(10,2),
    max_amount DECIMAL(10,2),
    salesperson_id UUID REFERENCES users(id),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Commission Tiers (for tiered/graduated rules)
CREATE TABLE commission_tiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_id UUID REFERENCES commission_rules(id) ON DELETE CASCADE,
    from_amount DECIMAL(10,2) NOT NULL,
    to_amount DECIMAL(10,2),
    percentage DECIMAL(5,2) NOT NULL,
    tier_order INTEGER NOT NULL
);

-- Commissions
CREATE TABLE commissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id VARCHAR(255) NOT NULL,
    xentral_invoice_id VARCHAR(255),
    salesperson_id UUID REFERENCES users(id),
    rule_id UUID REFERENCES commission_rules(id),
    amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    calculated_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Xentral Sync State
CREATE TABLE xentral_sync_state (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module VARCHAR(100) NOT NULL UNIQUE,
    last_run TIMESTAMP,
    status VARCHAR(50),
    record_count INTEGER,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Invoice Mappings
CREATE TABLE invoice_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crm_invoice_id UUID,
    xentral_invoice_id VARCHAR(255) UNIQUE,
    customer_id UUID REFERENCES customers(id),
    amount DECIMAL(10,2),
    status VARCHAR(50),
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_commissions_salesperson ON commissions(salesperson_id);
CREATE INDEX idx_commissions_status ON commissions(status);
CREATE INDEX idx_invoice_mappings_xentral ON invoice_mappings(xentral_invoice_id);
```

---

## 🚀 4. DEPLOYMENT & CONFIGURATION

### 4.1 Environment Variables
```bash
# .env.production
XENTRAL_API_URL=https://api.xentral.com/v1
XENTRAL_OAUTH_CLIENT_ID=your-client-id
XENTRAL_OAUTH_CLIENT_SECRET=your-secret
XENTRAL_WEBHOOK_SECRET=webhook-signing-secret
XENTRAL_WEBHOOK_ENDPOINT=https://crm.freshplan.de/api/webhooks/xentral
```

### 4.2 Xentral Webhook Configuration
```json
{
  "webhook_config": {
    "url": "https://crm.freshplan.de/api/webhooks/xentral",
    "events": [
      "payment.received",
      "invoice.created", 
      "invoice.updated",
      "customer.created",
      "customer.updated"
    ],
    "signing_secret": "generate-secure-secret",
    "retry_policy": {
      "max_attempts": 3,
      "backoff_seconds": 60
    }
  }
}
```

---

## ✅ 5. TESTING CHECKLIST

```java
// Integration Test Example
@QuarkusTest
class XentralIntegrationTest {
    
    @Test
    void testWebhookPaymentReceived() {
        given()
            .header("X-Xentral-Signature", validSignature())
            .contentType(ContentType.JSON)
            .body(paymentWebhookPayload())
        .when()
            .post("/api/webhooks/xentral")
        .then()
            .statusCode(200);
            
        // Verify commission was created
        List<Commission> commissions = Commission.findAll().list();
        assertThat(commissions).hasSize(1);
        assertThat(commissions.get(0).getAmount()).isEqualTo("150.00");
    }
}
```

---

## 🎯 IMPLEMENTATION PRIORITIES

1. **Phase 1 (2 Tage)**: Webhook Handler + Basic Commission Engine
2. **Phase 2 (2 Tage)**: Xentral API Client + Customer Sync
3. **Phase 3 (1 Tag)**: Frontend Dashboard
4. **Phase 4 (1 Tag)**: Testing & Error Handling

**Geschätzter Aufwand:** 6 Entwicklungstage

---

## 🔗 QUICK LINKS
- [Xentral API Docs](https://docs.xentral.com/api)
- [Webhook Best Practices](https://docs.xentral.com/webhooks)
- [Commission Engine Patterns](https://martinfowler.com/articles/commission-engine.html)