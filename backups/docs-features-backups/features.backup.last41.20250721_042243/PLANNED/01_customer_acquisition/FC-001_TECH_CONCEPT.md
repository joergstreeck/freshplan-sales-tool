# FC-001 Customer Acquisition Engine - Tech Concept

**Feature-Code:** FC-001  
**Feature-Name:** Customer Acquisition Engine  
**Feature-Typ:** 🔀 FULLSTACK  
**Erstellt:** 2025-07-20  
**Status:** Tech Concept  
**Priorität:** HIGH - Core Business  
**Geschätzter Aufwand:** 8-10 Tage  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### 📍 Kontext laden für produktive Arbeit

**Was ist FC-001?** Intelligente Lead-Generation und Customer Acquisition mit Multi-Channel-Ansatz

**Warum wichtig?** Zentraler Baustein für Neukundengewinnung - ohne funktioniert kein Wachstum

**Abhängigkeiten verstehen:**
- **Benötigt:** FC-009 Permissions (Zugriffskontrolle), FC-025 DSGVO (Datenschutz)
- **Blockiert:** M4 Pipeline (braucht Acquisition Data), FC-015 Deal Loss (Analytics)
- **Integriert mit:** FC-035 Social Selling, FC-028 WhatsApp Business

**Technischer Kern:** 
```java
@ApplicationScoped
public class CustomerAcquisitionService {
    public CompletionStage<AcquisitionResult> processLead(LeadRequest request) {
        // Multi-Channel Lead Processing mit DSGVO-Compliance
    }
}
```

**Frontend-Integration:**
```typescript
export const useLeadGeneration = () => {
    const processLead = useMutation(leadService.processLead);
    // React Hook für Lead-Verarbeitung
};
```

---

## 🎯 ÜBERSICHT

### Business Value
**Problem:** Unstrukturierte Neukundengewinnung führt zu verpassten Opportunities und ineffizientem Sales-Prozess

**Lösung:** Intelligente Multi-Channel Customer Acquisition Engine mit automatisierter Lead-Qualifizierung

**ROI:** 
- **Kosten:** 8-10 Entwicklertage (~€8.000)
- **Ersparnis:** 3h/Tag Lead-Processing (~780h/Jahr = €39.000)
- **ROI-Ratio:** 4.9:1 (Break-even nach 2 Monaten)

### Kernfunktionen
1. **Multi-Channel Lead Capture** - Web, Social Media, Events, Referrals
2. **Automatische Lead-Qualifizierung** - Scoring basierend auf Fit & Intent
3. **DSGVO-konforme Datenverarbeitung** - Einwilligung und Datenschutz
4. **Integration in Sales Pipeline** - Nahtloser Übergang zu M4 Opportunity Pipeline

---

## 🏗️ ARCHITEKTUR

### System-Design
```
Lead Sources → Lead Capture → Qualification → Pipeline Integration
     ↓              ↓             ↓              ↓
Web Forms      Lead Entity    Scoring AI     M4 Pipeline
Social Media   DSGVO Check    A/B Testing    Notifications
Events         Dedup Logic    Analytics      CRM Update
Referrals      Validation     Routing        Follow-up
```

### Database Schema
```sql
-- Lead Management Tables
CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_channel VARCHAR(50) NOT NULL, -- 'web', 'social', 'event', 'referral'
    lead_status VARCHAR(20) DEFAULT 'new', -- 'new', 'qualified', 'converted', 'rejected'
    contact_data JSONB NOT NULL, -- Flexible contact information
    qualification_score INTEGER, -- 0-100 lead quality score
    gdpr_consent BOOLEAN DEFAULT false,
    consent_timestamp TIMESTAMP,
    acquisition_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_sales_rep UUID,
    conversion_opportunity_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lead_sources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_name VARCHAR(100) NOT NULL,
    source_type VARCHAR(50) NOT NULL, -- 'web_form', 'social_media', 'event', 'referral'
    tracking_parameters JSONB, -- UTM codes, campaign data
    conversion_rate DECIMAL(5,2), -- Track source effectiveness
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lead_qualification_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rule_name VARCHAR(100) NOT NULL,
    rule_type VARCHAR(50) NOT NULL, -- 'demographic', 'behavioral', 'firmographic'
    rule_criteria JSONB NOT NULL, -- Flexible rule definition
    score_impact INTEGER NOT NULL, -- Points added/subtracted
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_leads_status ON leads(lead_status);
CREATE INDEX idx_leads_source ON leads(source_channel);
CREATE INDEX idx_leads_score ON leads(qualification_score);
CREATE INDEX idx_leads_date ON leads(acquisition_date);
```

### Backend-Architecture (Quarkus)
```java
// Domain Layer
@Entity
@Table(name = "leads")
public class Lead {
    @Id @GeneratedValue
    private UUID id;
    
    @Column(name = "source_channel")
    @Enumerated(EnumType.STRING)
    private LeadSource sourceChannel;
    
    @Column(name = "lead_status")
    @Enumerated(EnumType.STRING)
    private LeadStatus status;
    
    @Type(JsonType.class)
    @Column(name = "contact_data")
    private ContactData contactData;
    
    @Column(name = "qualification_score")
    private Integer qualificationScore;
    
    @Column(name = "gdpr_consent")
    private Boolean gdprConsent;
    
    // Constructors, getters, setters...
}

// Service Layer
@ApplicationScoped
@Transactional
public class CustomerAcquisitionService {
    
    @Inject LeadRepository leadRepository;
    @Inject LeadQualificationService qualificationService;
    @Inject GDPRComplianceService gdprService;
    @Inject NotificationService notificationService;
    
    public CompletionStage<AcquisitionResult> processLead(LeadRequest request) {
        return CompletableFuture
            .supplyAsync(() -> validateAndCreateLead(request))
            .thenCompose(lead -> qualifyLead(lead))
            .thenCompose(qualifiedLead -> routeToSalesTeam(qualifiedLead))
            .thenApply(this::buildAcquisitionResult);
    }
    
    private Lead validateAndCreateLead(LeadRequest request) {
        // 1. GDPR Compliance Check
        if (!gdprService.hasValidConsent(request)) {
            throw new GDPRComplianceException("Missing or invalid GDPR consent");
        }
        
        // 2. Duplicate Detection
        Optional<Lead> existingLead = leadRepository.findByContactData(request.getContactData());
        if (existingLead.isPresent()) {
            return updateExistingLead(existingLead.get(), request);
        }
        
        // 3. Create New Lead
        Lead lead = Lead.builder()
            .sourceChannel(request.getSourceChannel())
            .contactData(request.getContactData())
            .gdprConsent(true)
            .consentTimestamp(Instant.now())
            .status(LeadStatus.NEW)
            .build();
            
        return leadRepository.save(lead);
    }
    
    private CompletionStage<Lead> qualifyLead(Lead lead) {
        return qualificationService.calculateScore(lead)
            .thenApply(score -> {
                lead.setQualificationScore(score);
                lead.setStatus(determineStatusByScore(score));
                return leadRepository.save(lead);
            });
    }
    
    private CompletionStage<Lead> routeToSalesTeam(Lead lead) {
        if (lead.getQualificationScore() >= 70) { // High-quality lead
            return assignToSalesRep(lead)
                .thenCompose(assignedLead -> notifyAssignedRep(assignedLead));
        } else {
            return nurturingCampaignService.addToNurturing(lead);
        }
    }
}

// Qualification Service
@ApplicationScoped
public class LeadQualificationService {
    
    @Inject QualificationRulesRepository rulesRepository;
    
    public CompletionStage<Integer> calculateScore(Lead lead) {
        return CompletableFuture.supplyAsync(() -> {
            List<QualificationRule> activeRules = rulesRepository.findActiveRules();
            
            return activeRules.stream()
                .filter(rule -> rule.matches(lead))
                .mapToInt(QualificationRule::getScoreImpact)
                .sum();
        });
    }
}

// Repository Layer
@ApplicationScoped
public class LeadRepository implements PanacheRepositoryBase<Lead, UUID> {
    
    public Optional<Lead> findByContactData(ContactData contactData) {
        return find("JSON_EXTRACT(contact_data, '$.email') = ?1", contactData.getEmail())
            .firstResultOptional();
    }
    
    public List<Lead> findUnassignedHighQualityLeads() {
        return find("qualification_score >= 70 AND assigned_sales_rep IS NULL")
            .list();
    }
    
    public ConversionMetrics getConversionMetricsBySource(LeadSource source, LocalDate from, LocalDate to) {
        // Analytics query for source effectiveness
        return getEntityManager()
            .createNativeQuery("""
                SELECT 
                    source_channel,
                    COUNT(*) as total_leads,
                    COUNT(CASE WHEN lead_status = 'converted' THEN 1 END) as conversions,
                    AVG(qualification_score) as avg_score
                FROM leads 
                WHERE source_channel = :source 
                AND acquisition_date BETWEEN :from AND :to
                GROUP BY source_channel
                """, ConversionMetrics.class)
            .setParameter("source", source)
            .setParameter("from", from)
            .setParameter("to", to)
            .getSingleResult();
    }
}
```

### Frontend-Architecture (React + TypeScript)
```typescript
// Lead Capture Form Component
interface LeadCaptureFormProps {
    sourceChannel: LeadSource;
    campaignData?: CampaignData;
    onLeadSubmitted: (result: AcquisitionResult) => void;
}

export const LeadCaptureForm: React.FC<LeadCaptureFormProps> = ({
    sourceChannel,
    campaignData,
    onLeadSubmitted
}) => {
    const { processLead, isLoading } = useLeadSubmission();
    const { trackEvent } = useAnalytics();
    
    const handleSubmit = async (formData: LeadFormData) => {
        try {
            // GDPR Consent Validation
            if (!formData.gdprConsent) {
                throw new Error('GDPR consent required');
            }
            
            // Process lead
            const result = await processLead({
                sourceChannel,
                contactData: formData.contactData,
                campaignData,
                gdprConsent: formData.gdprConsent,
                consentTimestamp: new Date().toISOString()
            });
            
            // Analytics tracking
            trackEvent('lead_submitted', {
                source: sourceChannel,
                qualification_score: result.qualificationScore
            });
            
            onLeadSubmitted(result);
            
        } catch (error) {
            console.error('Lead submission failed:', error);
            // Error handling...
        }
    };
    
    return (
        <Paper sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
                Interesse an FreshPlan?
            </Typography>
            
            <LeadForm 
                onSubmit={handleSubmit}
                isLoading={isLoading}
                requiredFields={['firstName', 'lastName', 'email', 'company']}
            />
            
            <GDPRConsentCheckbox required />
            
            <Button 
                type="submit" 
                variant="contained" 
                disabled={isLoading}
                fullWidth
                sx={{ mt: 2 }}
            >
                {isLoading ? 'Verarbeite...' : 'Kostenlose Demo anfragen'}
            </Button>
        </Paper>
    );
};

// Lead Management Dashboard
export const LeadManagementDashboard: React.FC = () => {
    const { data: leads, isLoading } = useLeads({
        status: 'new',
        minScore: 50
    });
    
    const { data: sourceMetrics } = useSourceMetrics();
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={12} md={8}>
                <Paper sx={{ p: 2 }}>
                    <Typography variant="h6">Neue Leads</Typography>
                    <LeadList 
                        leads={leads}
                        onLeadAssign={handleLeadAssignment}
                        onLeadQualify={handleLeadQualification}
                    />
                </Paper>
            </Grid>
            
            <Grid item xs={12} md={4}>
                <Paper sx={{ p: 2 }}>
                    <Typography variant="h6">Source Performance</Typography>
                    <SourceMetricsChart data={sourceMetrics} />
                </Paper>
            </Grid>
        </Grid>
    );
};

// Custom Hooks
export const useLeadSubmission = () => {
    return useMutation({
        mutationFn: (leadData: LeadSubmissionData) => 
            leadService.submitLead(leadData),
        onSuccess: (result) => {
            queryClient.invalidateQueries(['leads']);
            // Success handling...
        }
    });
};

export const useLeads = (filters: LeadFilters) => {
    return useQuery({
        queryKey: ['leads', filters],
        queryFn: () => leadService.getLeads(filters),
        staleTime: 30000 // 30 seconds
    });
};
```

---

## 🔄 ABHÄNGIGKEITEN

### Benötigt diese Features:
- **FC-009 Advanced Permissions** - Zugriffssteuerung für Lead-Management
- **FC-025 DSGVO Compliance** - Datenschutz-konforme Lead-Verarbeitung
- **FC-013 Duplicate Detection** - Verhindert doppelte Lead-Erfassung

### Ermöglicht diese Features:
- **M4 Opportunity Pipeline** - Qualified Leads werden zu Opportunities
- **FC-015 Deal Loss Analysis** - Analyse der Lead-to-Deal Conversion
- **FC-026 Analytics Platform** - Lead-Performance Dashboards

### Integriert mit:
- **FC-035 Social Selling** - Social Media Lead Capture
- **FC-028 WhatsApp Business** - Lead-Kommunikation über WhatsApp
- **FC-034 Instant Insights** - Lead-Intelligence und Scoring

---

## 🧪 TESTING-STRATEGIE

### Unit Tests (Backend)
```java
@QuarkusTest
class CustomerAcquisitionServiceTest {
    
    @Test
    void testLeadProcessing_withValidData_shouldCreateLead() {
        // Given
        LeadRequest request = LeadRequest.builder()
            .sourceChannel(LeadSource.WEB_FORM)
            .contactData(validContactData())
            .gdprConsent(true)
            .build();
        
        // When
        AcquisitionResult result = acquisitionService.processLead(request).await().indefinitely();
        
        // Then
        assertThat(result.getLeadId()).isNotNull();
        assertThat(result.getQualificationScore()).isBetween(0, 100);
        assertThat(result.getStatus()).isIn(LeadStatus.NEW, LeadStatus.QUALIFIED);
    }
    
    @Test
    void testDuplicateDetection_withExistingEmail_shouldUpdateExistingLead() {
        // Test duplicate handling...
    }
    
    @Test
    void testGDPRCompliance_withoutConsent_shouldThrowException() {
        // Test GDPR validation...
    }
}
```

### Integration Tests
```java
@QuarkusTest
@TestProfile(IntegrationTestProfile.class)
class LeadProcessingIntegrationTest {
    
    @Test
    @Transactional
    void testFullLeadProcessingWorkflow() {
        // Test entire workflow from submission to assignment
    }
}
```

### Frontend Tests
```typescript
describe('LeadCaptureForm', () => {
    it('should validate GDPR consent before submission', async () => {
        render(<LeadCaptureForm sourceChannel="web_form" onLeadSubmitted={mockHandler} />);
        
        // Fill form without GDPR consent
        await userEvent.type(screen.getByLabelText('E-Mail'), 'test@example.com');
        await userEvent.click(screen.getByRole('button', { name: /anfragen/i }));
        
        expect(screen.getByText(/consent required/i)).toBeInTheDocument();
    });
    
    it('should submit lead successfully with valid data', async () => {
        const mockSubmit = jest.fn().mockResolvedValue({
            leadId: 'test-id',
            qualificationScore: 75
        });
        
        jest.mocked(leadService.submitLead).mockImplementation(mockSubmit);
        
        render(<LeadCaptureForm sourceChannel="web_form" onLeadSubmitted={mockHandler} />);
        
        // Fill and submit form...
        
        expect(mockSubmit).toHaveBeenCalledWith(expect.objectContaining({
            sourceChannel: 'web_form',
            gdprConsent: true
        }));
    });
});
```

---

## 🚀 IMPLEMENTATION GUIDE

### Phase 1: Database & Backend Foundation (3 Tage)
1. **Database Schema** - Erstelle Lead-Management Tabellen
2. **Entity & Repository** - JPA Entities und Repository Pattern
3. **Basic CRUD** - Lead-Verwaltung Grundfunktionen
4. **GDPR Compliance** - Einwilligung und Datenschutz-Validierung

### Phase 2: Lead Processing Engine (3 Tage)
1. **Lead Qualification** - Scoring-Algorithmus implementieren
2. **Multi-Channel Support** - Verschiedene Lead-Quellen integrieren
3. **Duplicate Detection** - E-Mail-basierte Duplikatserkennung
4. **Sales Team Assignment** - Automatische Zuweisung an Verkäufer

### Phase 3: Frontend Integration (2 Tage)
1. **Lead Capture Forms** - Web-Formulare für Lead-Erfassung
2. **Lead Management Dashboard** - Übersicht für Sales-Team
3. **Analytics Integration** - Performance-Tracking und Reporting

### Phase 4: Testing & Optimization (2 Tage)
1. **Unit & Integration Tests** - Vollständige Test-Abdeckung
2. **Performance Optimization** - Database-Queries und Caching
3. **Security Audit** - GDPR-Compliance und Data Protection
4. **Documentation** - API-Docs und User Guidelines

---

## 📊 SUCCESS CRITERIA

### Funktionale Anforderungen
- ✅ Lead-Erfassung von mindestens 4 Kanälen (Web, Social, Event, Referral)
- ✅ Automatische Lead-Qualifizierung mit 80%+ Genauigkeit
- ✅ GDPR-konforme Datenverarbeitung mit Audit-Trail
- ✅ Integration in bestehende M4 Opportunity Pipeline
- ✅ Real-time Analytics für Lead-Performance

### Performance-Anforderungen
- ✅ Lead-Processing < 2 Sekunden P95
- ✅ Form-Submission < 1 Sekunde P95
- ✅ Dashboard-Loading < 3 Sekunden
- ✅ Support für 1000+ Leads/Tag
- ✅ 99.5% Uptime für Lead-Capture

### Business-Metriken
- ✅ 40%+ Increase in qualified leads
- ✅ 60%+ Reduction in manual lead processing
- ✅ 25%+ Improvement in lead-to-opportunity conversion
- ✅ ROI Break-even nach 2 Monaten

---

## 🔗 NAVIGATION ZU ALLEN 40 FEATURES

### Core Sales Features
- [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_TECH_CONCEPT.md) | [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_TECH_CONCEPT.md) | [M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_TECH_CONCEPT.md)
- [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md) | [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_TECH_CONCEPT.md) | [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md)
- [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_TECH_CONCEPT.md) | [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md) | [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md)

### Security & Foundation Features  
- [FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_TECH_CONCEPT.md) | [FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_TECH_CONCEPT.md) | [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md)
- [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md) | [FC-038 Multi-Tenant Architecture](/docs/features/PLANNED/38_multi_tenant/FC-038_TECH_CONCEPT.md) | [FC-039 API Gateway](/docs/features/PLANNED/39_api_gateway/FC-039_TECH_CONCEPT.md)

### Mobile & Field Service Features
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md) | [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md) | [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_TECH_CONCEPT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md) | [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md) | [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)

### Communication Features
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md) | [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_TECH_CONCEPT.md) | [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_TECH_CONCEPT.md)
- [FC-035 Social Selling Helper](/docs/features/PLANNED/35_social_selling/FC-035_TECH_CONCEPT.md)

### Analytics & Intelligence Features
- [M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_TECH_CONCEPT.md) | [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md) | [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md)
- [FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_TECH_CONCEPT.md) | [FC-037 Advanced Reporting](/docs/features/PLANNED/37_advanced_reporting/FC-037_TECH_CONCEPT.md) | [FC-040 Performance Monitoring](/docs/features/PLANNED/40_performance_monitoring/FC-040_TECH_CONCEPT.md)

### Integration & Infrastructure Features
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_TECH_CONCEPT.md) | [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_TECH_CONCEPT.md) | [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md) | [M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_TECH_CONCEPT.md)

### UI & Productivity Features
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_TECH_CONCEPT.md) | [M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_TECH_CONCEPT.md) | [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_TECH_CONCEPT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md) | [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md) | [FC-033 Visual Customer Cards](/docs/features/PLANNED/33_visual_cards/FC-033_TECH_CONCEPT.md)

### Advanced Features
- [FC-011 Bonitätsprüfung](/docs/features/ACTIVE/02_opportunity_pipeline/integrations/FC-011_TECH_CONCEPT.md) | [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_TECH_CONCEPT.md) | [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md)
- [FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_TECH_CONCEPT.md)

---

**🎯 FC-001 Customer Acquisition Engine - Ready for Implementation!**  
**⏱️ Geschätzter Aufwand:** 8-10 Tage | **ROI:** 4.9:1 | **Priorität:** HIGH