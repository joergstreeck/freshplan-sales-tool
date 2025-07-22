# FC-001 Customer Acquisition - CLAUDE TECH 🤖

## 🧠 QUICK-LOAD (30 Sekunden bis zur Produktivität)

**Feature:** Multi-Channel Lead Capture mit automatischer Qualifizierung  
**Stack:** React + Quarkus + PostgreSQL + GDPR-Compliance  
**Status:** 🟡 READY TO START - Dependencies vorhanden  
**Dependencies:** FC-009 Permissions (✅) | FC-025 DSGVO (pending) | M4 Pipeline (✅)  

**Jump to:** [📚 Recipes](#-implementation-recipes) | [🧪 Tests](#-test-patterns) | [🔌 Integration](#-integration-cookbook) | [🎯 Analytics](#-analytics-dashboard)

**Core Purpose in 1 Line:** `Lead Sources → Smart Qualification → Sales Assignment → Pipeline Feed`

---

## 🍳 IMPLEMENTATION RECIPES

### Recipe 1: Lead Database in 5 Minuten
```sql
-- Copy-paste ready für Flyway Migration
CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_channel VARCHAR(50) NOT NULL CHECK (source_channel IN ('web', 'social', 'event', 'referral')),
    lead_status VARCHAR(20) DEFAULT 'new' CHECK (lead_status IN ('new', 'qualified', 'converted', 'rejected')),
    contact_data JSONB NOT NULL,
    qualification_score INTEGER CHECK (qualification_score BETWEEN 0 AND 100),
    gdpr_consent BOOLEAN DEFAULT false NOT NULL,
    consent_timestamp TIMESTAMP,
    acquisition_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_sales_rep UUID REFERENCES users(id),
    conversion_opportunity_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance Indexes
CREATE INDEX idx_leads_status ON leads(lead_status);
CREATE INDEX idx_leads_score ON leads(qualification_score) WHERE qualification_score IS NOT NULL;
CREATE INDEX idx_leads_sales_rep ON leads(assigned_sales_rep) WHERE assigned_sales_rep IS NOT NULL;
CREATE INDEX idx_leads_email ON leads((contact_data->>'email'));

-- Lead Sources für Analytics
CREATE TABLE lead_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_name VARCHAR(100) NOT NULL UNIQUE,
    source_type VARCHAR(50) NOT NULL,
    tracking_parameters JSONB,
    conversion_rate DECIMAL(5,2),
    active BOOLEAN DEFAULT true
);

-- Qualification Rules Engine
CREATE TABLE lead_qualification_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(100) NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    rule_criteria JSONB NOT NULL,
    score_impact INTEGER NOT NULL,
    active BOOLEAN DEFAULT true,
    priority INTEGER DEFAULT 0
);

-- Beispiel-Rules einfügen
INSERT INTO lead_qualification_rules (rule_name, rule_type, rule_criteria, score_impact) VALUES
('Company Size Large', 'firmographic', '{"company_size": {"min": 100}}', 20),
('Budget Available', 'behavioral', '{"budget_range": {"min": 10000}}', 30),
('Decision Maker', 'demographic', '{"job_title": ["CEO", "CTO", "Head of Sales"]}', 25);
```

### Recipe 2: Lead Service Backend (copy-paste ready)
```java
// 1. Lead Entity mit allen Features
@Entity
@Table(name = "leads")
@EntityListeners(AuditingEntityListener.class)
public class Lead {
    @Id
    @GeneratedValue
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "source_channel", nullable = false)
    private LeadSource sourceChannel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lead_status")
    private LeadStatus status = LeadStatus.NEW;
    
    @Type(JsonBinaryType.class)
    @Column(name = "contact_data", columnDefinition = "jsonb")
    private ContactData contactData;
    
    @Column(name = "qualification_score")
    private Integer qualificationScore;
    
    @Column(name = "gdpr_consent", nullable = false)
    private Boolean gdprConsent = false;
    
    @Column(name = "consent_timestamp")
    private Instant consentTimestamp;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_sales_rep")
    private User assignedSalesRep;
    
    // Auto-generated timestamps
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}

// 2. Service Layer mit Business Logic
@ApplicationScoped
@Transactional
public class CustomerAcquisitionService {
    
    @Inject LeadRepository leadRepository;
    @Inject LeadQualificationService qualificationService;
    @Inject NotificationService notificationService;
    @Inject SecurityContext securityContext;
    
    public AcquisitionResult processLead(LeadRequest request) {
        // GDPR Check first!
        if (!request.hasValidGdprConsent()) {
            throw new WebApplicationException("GDPR consent required", 422);
        }
        
        // Duplicate Detection
        var existingLead = leadRepository.findByEmail(request.getEmail());
        if (existingLead.isPresent()) {
            return updateExistingLead(existingLead.get(), request);
        }
        
        // Create & Qualify
        var lead = createLead(request);
        var score = qualificationService.calculateScore(lead);
        lead.setQualificationScore(score);
        lead.setStatus(score >= 70 ? LeadStatus.QUALIFIED : LeadStatus.NEW);
        
        leadRepository.persist(lead);
        
        // Auto-Assignment for hot leads
        if (score >= 70) {
            assignToAvailableSalesRep(lead);
        }
        
        return AcquisitionResult.of(lead);
    }
    
    private void assignToAvailableSalesRep(Lead lead) {
        var availableRep = userRepository.findNextAvailableSalesRep();
        if (availableRep.isPresent()) {
            lead.setAssignedSalesRep(availableRep.get());
            notificationService.notifyLeadAssignment(lead, availableRep.get());
        }
    }
}

// 3. Qualification Engine
@ApplicationScoped
public class LeadQualificationService {
    
    @Inject EntityManager em;
    
    public int calculateScore(Lead lead) {
        var rules = em.createQuery(
            "SELECT r FROM LeadQualificationRule r WHERE r.active = true ORDER BY r.priority DESC",
            LeadQualificationRule.class
        ).getResultList();
        
        return rules.stream()
            .filter(rule -> matchesRule(lead, rule))
            .mapToInt(LeadQualificationRule::getScoreImpact)
            .sum();
    }
    
    private boolean matchesRule(Lead lead, LeadQualificationRule rule) {
        var criteria = rule.getRuleCriteria();
        var contactData = lead.getContactData();
        
        return switch (rule.getRuleType()) {
            case "firmographic" -> checkFirmographicRule(contactData, criteria);
            case "behavioral" -> checkBehavioralRule(contactData, criteria);
            case "demographic" -> checkDemographicRule(contactData, criteria);
            default -> false;
        };
    }
}
```

### Recipe 3: Lead Capture Frontend
```typescript
// 1. Lead Capture Form mit GDPR (copy-paste ready)
export const LeadCaptureForm: React.FC<{sourceChannel: string}> = ({ sourceChannel }) => {
    const [gdprAccepted, setGdprAccepted] = useState(false);
    const { mutate: submitLead, isLoading } = useLeadSubmission();
    
    const {
        register,
        handleSubmit,
        formState: { errors }
    } = useForm<LeadFormData>({
        resolver: zodResolver(leadSchema)
    });
    
    const onSubmit = (data: LeadFormData) => {
        if (!gdprAccepted) {
            toast.error('Bitte akzeptieren Sie die Datenschutzerklärung');
            return;
        }
        
        submitLead({
            ...data,
            sourceChannel,
            gdprConsent: true,
            consentTimestamp: new Date().toISOString()
        });
    };
    
    return (
        <Paper component="form" onSubmit={handleSubmit(onSubmit)} sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
                Interesse an FreshPlan?
            </Typography>
            
            <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                    <TextField
                        {...register('firstName')}
                        label="Vorname"
                        fullWidth
                        error={!!errors.firstName}
                        helperText={errors.firstName?.message}
                    />
                </Grid>
                
                <Grid item xs={12} sm={6}>
                    <TextField
                        {...register('lastName')}
                        label="Nachname"
                        fullWidth
                        error={!!errors.lastName}
                        helperText={errors.lastName?.message}
                    />
                </Grid>
                
                <Grid item xs={12}>
                    <TextField
                        {...register('email')}
                        label="E-Mail"
                        type="email"
                        fullWidth
                        error={!!errors.email}
                        helperText={errors.email?.message}
                    />
                </Grid>
                
                <Grid item xs={12}>
                    <TextField
                        {...register('company')}
                        label="Unternehmen"
                        fullWidth
                    />
                </Grid>
                
                <Grid item xs={12}>
                    <FormControlLabel
                        control={
                            <Checkbox
                                checked={gdprAccepted}
                                onChange={(e) => setGdprAccepted(e.target.checked)}
                                color="primary"
                            />
                        }
                        label={
                            <Typography variant="body2">
                                Ich akzeptiere die{' '}
                                <Link href="/datenschutz" target="_blank">
                                    Datenschutzerklärung
                                </Link>
                            </Typography>
                        }
                    />
                </Grid>
                
                <Grid item xs={12}>
                    <LoadingButton
                        type="submit"
                        variant="contained"
                        fullWidth
                        loading={isLoading}
                        disabled={!gdprAccepted}
                        size="large"
                    >
                        Kostenlose Demo anfragen
                    </LoadingButton>
                </Grid>
            </Grid>
        </Paper>
    );
};

// 2. Lead Management Dashboard
export const LeadDashboard: React.FC = () => {
    const { data: leads } = useLeads({ status: 'new', minScore: 50 });
    const { mutate: assignLead } = useLeadAssignment();
    
    const columns: GridColDef[] = [
        { field: 'name', headerName: 'Name', flex: 1 },
        { field: 'company', headerName: 'Unternehmen', flex: 1 },
        { 
            field: 'qualificationScore', 
            headerName: 'Score',
            width: 100,
            renderCell: (params) => (
                <Chip 
                    label={params.value}
                    color={params.value >= 70 ? 'success' : 'default'}
                    size="small"
                />
            )
        },
        {
            field: 'actions',
            headerName: 'Aktionen',
            width: 150,
            renderCell: (params) => (
                <Button
                    size="small"
                    onClick={() => assignLead(params.row.id)}
                >
                    Zuweisen
                </Button>
            )
        }
    ];
    
    return (
        <Box>
            <Typography variant="h4" gutterBottom>
                Lead Management
            </Typography>
            
            <DataGrid
                rows={leads || []}
                columns={columns}
                pageSize={25}
                autoHeight
                disableSelectionOnClick
            />
        </Box>
    );
};
```

---

## 🧪 TEST PATTERNS

### Pattern 1: Lead Processing Tests
```java
@QuarkusTest
class CustomerAcquisitionServiceTest {
    
    @Inject CustomerAcquisitionService service;
    
    @Test
    void processLead_withValidData_createsAndQualifies() {
        var request = LeadRequest.builder()
            .sourceChannel("web")
            .firstName("Max")
            .lastName("Mustermann")
            .email("max@example.com")
            .company("Big Corp")
            .gdprConsent(true)
            .build();
        
        var result = service.processLead(request);
        
        assertThat(result.getLeadId()).isNotNull();
        assertThat(result.getQualificationScore()).isGreaterThanOrEqualTo(0);
        assertThat(result.getStatus()).isIn("new", "qualified");
    }
    
    @Test
    void processLead_withoutGdpr_throws422() {
        var request = LeadRequest.builder()
            .email("test@example.com")
            .gdprConsent(false)
            .build();
        
        assertThatThrownBy(() -> service.processLead(request))
            .isInstanceOf(WebApplicationException.class)
            .hasFieldOrPropertyWithValue("status", 422);
    }
}
```

### Pattern 2: Frontend Form Tests
```typescript
describe('LeadCaptureForm', () => {
    it('requires GDPR consent', async () => {
        render(<LeadCaptureForm sourceChannel="web" />);
        
        // Fill form
        await userEvent.type(screen.getByLabelText(/vorname/i), 'Max');
        await userEvent.type(screen.getByLabelText(/e-mail/i), 'max@test.de');
        
        // Try submit without GDPR
        await userEvent.click(screen.getByRole('button', { name: /demo/i }));
        
        expect(screen.getByText(/datenschutz/i)).toBeInTheDocument();
    });
});
```

---

## 🔌 INTEGRATION COOKBOOK

### Mit M4 Opportunity Pipeline
```java
// Auto-convert qualified leads to opportunities
@Observes
void onLeadQualified(@LeadQualified LeadQualifiedEvent event) {
    if (event.getScore() >= 80) { // Hot lead!
        var opportunity = Opportunity.fromLead(event.getLead());
        opportunityService.create(opportunity);
    }
}
```

### Mit FC-009 Permissions
```java
// Permission check for lead access
@RolesAllowed({"sales", "manager", "admin"})
public List<Lead> getLeads(LeadFilter filter) {
    var user = securityContext.getUserPrincipal();
    
    // Sales users see only their leads
    if (user.hasRole("sales") && !user.hasRole("manager")) {
        filter.setAssignedTo(user.getId());
    }
    
    return leadRepository.findByFilter(filter);
}
```

### Mit Analytics Module
```typescript
// Lead source performance tracking
export const useLeadSourceMetrics = () => {
    return useQuery({
        queryKey: ['metrics', 'lead-sources'],
        queryFn: async () => {
            const data = await api.get('/api/leads/metrics/by-source');
            return data.map(source => ({
                ...source,
                conversionRate: (source.converted / source.total) * 100
            }));
        }
    });
};
```

---

## 🎯 ANALYTICS DASHBOARD

### Recipe 4: Lead Analytics Queries
```sql
-- Top performing lead sources
SELECT 
    source_channel,
    COUNT(*) as total_leads,
    COUNT(CASE WHEN lead_status = 'converted' THEN 1 END) as converted,
    ROUND(AVG(qualification_score), 1) as avg_score,
    ROUND(COUNT(CASE WHEN lead_status = 'converted' THEN 1 END)::numeric / COUNT(*) * 100, 1) as conversion_rate
FROM leads
WHERE acquisition_date >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY source_channel
ORDER BY conversion_rate DESC;

-- Sales rep performance
SELECT 
    u.name as sales_rep,
    COUNT(l.id) as assigned_leads,
    COUNT(CASE WHEN l.lead_status = 'converted' THEN 1 END) as conversions,
    ROUND(AVG(l.qualification_score), 1) as avg_lead_quality
FROM leads l
JOIN users u ON l.assigned_sales_rep = u.id
WHERE l.acquisition_date >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY u.id, u.name
ORDER BY conversions DESC;
```

---

## 📚 DEEP KNOWLEDGE (Bei Bedarf expandieren)

<details>
<summary>🔐 GDPR Compliance Details</summary>

### Double Opt-In Implementation
```java
@Path("/api/leads/confirm/{token}")
public Response confirmLeadEmail(@PathParam("token") String token) {
    var lead = leadRepository.findByConfirmationToken(token)
        .orElseThrow(() -> new NotFoundException());
    
    lead.setEmailConfirmed(true);
    lead.setConfirmationDate(Instant.now());
    
    // Now eligible for marketing
    marketingService.addToNewsletter(lead);
    
    return Response.ok().build();
}
```

</details>

<details>
<summary>📊 Advanced Scoring Algorithm</summary>

### Machine Learning Integration
```python
# Lead scoring ML model (Python microservice)
from sklearn.ensemble import RandomForestClassifier

features = ['company_size', 'budget_range', 'industry_fit', 'engagement_score']
model = RandomForestClassifier(n_estimators=100)
model.fit(X_train, y_train)

# Expose as REST API for Quarkus
@app.route('/score', methods=['POST'])
def score_lead():
    data = request.json
    score = model.predict_proba([extract_features(data)])[0][1] * 100
    return {'score': int(score)}
```

</details>

---

**🎯 Nächster Schritt:** Database Migration erstellen und Lead Entity implementieren