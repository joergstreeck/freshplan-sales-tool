# Kampagnen - Technical Concept

**📊 Plan Status:** ✅ Ready for Implementation
**🎯 Owner:** Backend + Marketing Automation Team
**⏱️ Timeline:** Q4 2025 Woche 13-20 (8 Wochen nach E-Mail + Lead Foundation)
**🔧 Effort:** L (Complete Module mit A/B-Testing und Multi-Touch-Attribution)

## 🎯 Executive Summary (für Claude)

**Mission:** Vollständige Campaign-Engine mit A/B-Testing, Multi-Touch-Attribution, Automation-Workflows und all.inkl-Integration.
**Problem:** Salesteam benötigt professionelle Marketing-Automation für Lead-Nurturing mit messbaren ROI und Attribution-Tracking.
**Solution:** Complete Module Development mit Template-System, Audience-Management, A/B-Testing-Framework und Advanced-Analytics.
**Timeline:** 8 Wochen ab Woche 13 nach stabiler E-Mail+Lead-Foundation für optimale Integration.
**Impact:** Enterprise-Grade Marketing-Automation mit Attribution-Tracking und automatischen Lead-Scoring-Workflows.

**Quick Context:** Basiert auf KI-Production-Specs mit Outbox-Pattern für Deliverability, all.inkl SMTP-Integration, Lead-System-Kopplung für automatische Nurturing, und TestDataBuilder-System für Campaign-Mock-Replacement.

## 📋 Context & Dependencies

### Current State:
- ✅ all.inkl SMTP-Integration über E-Mail-Posteingang etabliert
- ✅ Lead-System mit State-Machine und UserLeadSettings funktional
- ✅ Event-System für E-Mail↔Lead-Integration verfügbar
- ✅ TestDataBuilder-System für Mock-Replacement etabliert
- ✅ Cockpit-Integration für Real-time Updates vorbereitet

### Target State:
- ✅ Template-System mit Content-Editor (Blöcke, Variablen, A/B-Testing)
- ✅ Audience-Management (statisch/dynamisch, Lead-Integration, Segmente)
- ✅ all.inkl SMTP-basiertes Campaign-Sending mit Queue-System
- ✅ A/B-Testing-Framework mit statistischer Signifikanz-Prüfung
- ✅ Multi-Touch-Attribution (Last/First/Linear/Time-Decay-Modelle)
- ✅ Automation-Workflows (Send/Wait/Condition/WebHook-Triggers)
- ✅ Open/Click/Reply-Tracking mit Analytics-Dashboard

### Dependencies:
→ [Email-Posteingang Technical Concept](../email-posteingang/technical-concept.md) - all.inkl SMTP-Infrastructure
→ [Lead-Erfassung Technical Concept](../lead-erfassung/technical-concept.md) - Lead-Integration + UserLeadSettings
→ [KI Production Specs](../diskussionen/2025-09-18_finale-ki-specs-bewertung.md) - Outbox-Pattern + Performance

## 🛠️ Implementation Phases

### Phase 1: Campaign-Foundation + Template-System (Woche 13-15)
**Goal:** Core Campaign-Entity mit Template-Engine und Content-Editor

**Actions:**
- [ ] Campaign-Entity mit Template-Reference und A/B-Testing-Support
- [ ] Template-System mit Block-based Content-Editor (Text, Image, CTA)
- [ ] Variable-System für Personalisierung ({{lead.company}}, {{user.name}})
- [ ] Campaign-Scheduler mit all.inkl SMTP-Queue-Integration
- [ ] Basic Campaign-Analytics-Schema (Sent, Delivered, Bounced)
- [ ] Campaign-Status-Machine (DRAFT, SCHEDULED, SENDING, COMPLETED, PAUSED)

**Code Changes:**
```java
// Campaign-Entity mit A/B-Testing-Support
@Entity
@Table(name = "campaign")
public class Campaign {
    @Id private UUID id;

    // Campaign-Meta
    private String name;
    private String description;
    @Enumerated(EnumType.STRING) private CampaignStatus status;

    // Template-Integration
    @OneToOne private CampaignTemplate primaryTemplate;
    @OneToOne private CampaignTemplate variantTemplate; // für A/B-Testing

    // Audience-Management
    @ManyToOne private Audience targetAudience;
    private Integer estimatedRecipients;

    // Scheduling
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime completedAt;

    // A/B-Testing-Configuration
    private Boolean abTestingEnabled = false;
    private BigDecimal variantPercentage; // 0.1 = 10% Variant, 90% Primary
    private Integer minSampleSize; // Mindest-Sample für Signifikanz
}

// Template-System mit Block-based Content
@Entity
public class CampaignTemplate {
    @Id private UUID id;
    private String name;

    // Content-Blocks (JSON-basiert)
    @Column(columnDefinition = "jsonb")
    private List<ContentBlock> contentBlocks;

    // Template-Variables
    @Column(columnDefinition = "jsonb")
    private Map<String, VariableDefinition> variables;

    // Personalization-Settings
    private String subjectTemplate; // "{{lead.company}} - Ihre Anfrage"
    private String fromNameTemplate; // "{{user.firstName}} {{user.lastName}}"
}

// Content-Block für Template-Editor
public class ContentBlock {
    private ContentBlockType type; // TEXT, IMAGE, CTA_BUTTON, SPACER
    private Map<String, Object> configuration;

    // Für A/B-Testing: Block-spezifische Varianten
    private Map<String, Object> variantConfiguration;
}
```

**Success Criteria:**
- Campaign-Entity mit Template-Integration funktional
- Content-Editor erstellt valide E-Mail-Templates
- Variable-System ersetzt Platzhalter korrekt
- Campaign-Scheduler integriert mit all.inkl SMTP-Queue

**Next:** → [Phase 2](#phase-2)

### Phase 2: Audience-Management + Lead-Integration (Woche 16-17)
**Goal:** Dynamische Audience-Segmentierung mit Lead-System-Integration

**Actions:**
- [ ] Audience-Entity mit statischen und dynamischen Segmenten
- [ ] Lead-Integration für automatische Audience-Updates
- [ ] Segment-Builder mit Lead-Status/Activity-Filtern
- [ ] Customer-Integration für bestehende Kundenstamm-Kampagnen
- [ ] Audience-Preview mit Recipient-Count-Estimation
- [ ] DSGVO-Compliance: Opt-out-Management und Blacklist-Integration

**Code Changes:**
```java
// Audience-Management mit Lead-Integration
@Entity
public class Audience {
    @Id private UUID id;
    private String name;
    private String description;

    // Audience-Types
    @Enumerated(EnumType.STRING) private AudienceType type; // STATIC, DYNAMIC

    // Static-Audience: Explicit Recipients
    @ManyToMany private Set<Lead> staticLeads;
    @ManyToMany private Set<Customer> staticCustomers;

    // Dynamic-Audience: Filter-Rules
    @Column(columnDefinition = "jsonb")
    private AudienceFilterCriteria filterCriteria;

    // DSGVO-Compliance
    @ManyToMany private Set<Contact> optedOutContacts;
    private LocalDateTime lastCalculatedAt;
    private Integer lastCalculatedSize;
}

// Dynamic-Audience Filter-System
public class AudienceFilterCriteria {
    // Lead-Status-Filter
    private Set<LeadStatus> leadStatuses; // ACTIVE, REMINDER_SENT, etc.

    // Activity-Filter
    private Integer daysSinceLastActivity;
    private Set<ActivityType> requiredActivityTypes;

    // Geographic/Company-Filter
    private Set<String> locations;
    private Set<String> industries;

    // User-Assignment-Filter
    private Set<UUID> assignedUserIds;

    // Zeitraum-Filter
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
}

// Dynamic-Audience-Calculator
@Service
public class AudienceCalculatorService {

    public AudienceCalculationResult calculateDynamicAudience(Audience audience) {
        AudienceFilterCriteria criteria = audience.getFilterCriteria();

        // Lead-basierte Recipients
        List<Lead> leads = leadRepository.findByCriteria(
            criteria.getLeadStatuses(),
            criteria.getDaysSinceLastActivity(),
            criteria.getLocations()
        );

        // Customer-basierte Recipients
        List<Customer> customers = customerRepository.findByCriteria(criteria);

        // DSGVO-Filter: Opt-out-Contacts entfernen
        Set<Contact> optedOut = audience.getOptedOutContacts();
        leads = leads.stream()
            .filter(lead -> !optedOut.contains(lead.getContact()))
            .collect(toList());

        return AudienceCalculationResult.builder()
            .totalRecipients(leads.size() + customers.size())
            .leadRecipients(leads)
            .customerRecipients(customers)
            .calculatedAt(LocalDateTime.now())
            .build();
    }
}
```

**Success Criteria:**
- Dynamic-Audience berechnet Recipients basierend auf Lead-Criteria
- Static-Audience ermöglicht manuelle Recipient-Listen
- DSGVO-Opt-out-Filter funktional
- Audience-Size-Estimation vor Campaign-Versand

**Next:** → [Phase 3](#phase-3)

### Phase 3: A/B-Testing + Analytics-Foundation (Woche 18-19)
**Goal:** Statistisch valides A/B-Testing mit Tracking-Infrastructure

**Actions:**
- [ ] A/B-Testing-Engine mit Variant-Assignment-Logic
- [ ] Statistical-Significance-Calculator (Chi-Square/T-Test)
- [ ] Tracking-Pixel-System für Open/Click-Measurement
- [ ] Campaign-Analytics-Dashboard (Open-Rate, Click-Rate, Conversion)
- [ ] Winner-Selection automatisch bei Signifikanz-Erreichen
- [ ] UTM-Parameter-Management für Attribution-Tracking

**Code Changes:**
```java
// A/B-Testing-Engine
@Service
public class ABTestingService {

    public CampaignVariant assignVariant(Campaign campaign, Contact recipient) {
        if (!campaign.getAbTestingEnabled()) {
            return CampaignVariant.PRIMARY;
        }

        // Consistent Assignment basierend auf Recipient-ID
        int hash = Objects.hash(campaign.getId(), recipient.getId());
        double percentage = (hash % 100) / 100.0;

        return percentage < campaign.getVariantPercentage()
            ? CampaignVariant.VARIANT
            : CampaignVariant.PRIMARY;
    }

    public ABTestResult calculateTestResults(Campaign campaign) {
        CampaignAnalytics primaryStats = analyticsService.getStats(campaign, PRIMARY);
        CampaignAnalytics variantStats = analyticsService.getStats(campaign, VARIANT);

        // Chi-Square-Test für Click-Through-Rate
        double chiSquare = calculateChiSquare(
            primaryStats.getClicks(), primaryStats.getSent(),
            variantStats.getClicks(), variantStats.getSent()
        );

        boolean isSignificant = chiSquare > SIGNIFICANCE_THRESHOLD; // p < 0.05

        return ABTestResult.builder()
            .primaryStats(primaryStats)
            .variantStats(variantStats)
            .isSignificant(isSignificant)
            .confidenceLevel(calculateConfidence(chiSquare))
            .recommendedWinner(determineWinner(primaryStats, variantStats, isSignificant))
            .build();
    }
}

// Campaign-Analytics mit Tracking
@Entity
public class CampaignDelivery {
    @Id private UUID id;
    @ManyToOne private Campaign campaign;
    private Contact recipient;

    // Variant-Assignment für A/B-Testing
    @Enumerated(EnumType.STRING) private CampaignVariant variant;

    // Delivery-Status
    private LocalDateTime sentAt;
    private Boolean delivered;
    private LocalDateTime deliveredAt;
    private Boolean bounced;
    private String bounceReason;

    // Engagement-Tracking
    private Boolean opened;
    private LocalDateTime firstOpenedAt;
    private Integer openCount;
    private Boolean clicked;
    private LocalDateTime firstClickedAt;
    private Integer clickCount;

    // Attribution für Multi-Touch
    @Column(columnDefinition = "jsonb")
    private Map<String, String> utmParameters;
}

// Tracking-Pixel-Service
@RestController
public class CampaignTrackingController {

    @GetMapping("/track/open/{deliveryId}")
    public ResponseEntity<byte[]> trackOpen(@PathVariable UUID deliveryId) {
        campaignAnalyticsService.recordOpen(deliveryId);

        // 1x1 transparentes Pixel zurückgeben
        byte[] pixel = Base64.getDecoder().decode(TRANSPARENT_PIXEL_BASE64);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(pixel);
    }

    @GetMapping("/track/click/{deliveryId}")
    public ResponseEntity<Void> trackClick(@PathVariable UUID deliveryId,
                                          @RequestParam String targetUrl) {
        campaignAnalyticsService.recordClick(deliveryId, targetUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(targetUrl))
            .build();
    }
}
```

**Success Criteria:**
- A/B-Testing weist Varianten konsistent zu
- Statistical-Significance-Berechnung funktional
- Open/Click-Tracking erfasst Engagement-Metriken
- Campaign-Analytics-Dashboard zeigt Echtzeit-Resultate

**Next:** → [Phase 4](#phase-4)

### Phase 4: Multi-Touch-Attribution + Automation (Woche 20)
**Goal:** Advanced Attribution-Modelle und Automation-Workflows

**Actions:**
- [ ] Multi-Touch-Attribution-Engine (Last/First/Linear/Time-Decay)
- [ ] Touch-Event-Tracking für Customer-Journey-Mapping
- [ ] Automation-Workflow-Engine (Trigger→Condition→Action)
- [ ] Lead-Scoring-Integration mit Campaign-Engagement
- [ ] Advanced Analytics-Reports (Funnel, Cohort, Attribution)
- [ ] Campaign-Performance-Optimization-Recommendations

**Code Changes:**
```java
// Multi-Touch-Attribution-Engine
@Service
public class AttributionService {

    public AttributionResult calculateAttribution(Customer customer,
                                                ConversionEvent conversion,
                                                AttributionModel model) {
        List<TouchEvent> touchEvents = touchEventRepository
            .findByCustomerAndDateRange(customer, conversion.getTimeWindow());

        return switch (model) {
            case LAST_TOUCH -> calculateLastTouch(touchEvents, conversion);
            case FIRST_TOUCH -> calculateFirstTouch(touchEvents, conversion);
            case LINEAR -> calculateLinearAttribution(touchEvents, conversion);
            case TIME_DECAY -> calculateTimeDecayAttribution(touchEvents, conversion);
        };
    }

    private AttributionResult calculateTimeDecayAttribution(List<TouchEvent> touches,
                                                          ConversionEvent conversion) {
        double totalScore = 0.0;
        Map<UUID, Double> campaignScores = new HashMap<>();

        for (TouchEvent touch : touches) {
            // Exponential Decay: Je näher zur Conversion, desto höher die Gewichtung
            long daysToConversion = ChronoUnit.DAYS.between(touch.getOccurredAt(), conversion.getOccurredAt());
            double weight = Math.exp(-daysToConversion / DECAY_CONSTANT);

            campaignScores.merge(touch.getCampaignId(), weight, Double::sum);
            totalScore += weight;
        }

        // Normalisierung auf 100%
        campaignScores.replaceAll((id, score) -> score / totalScore);

        return AttributionResult.builder()
            .model(TIME_DECAY)
            .campaignAttributions(campaignScores)
            .totalTouches(touches.size())
            .conversionValue(conversion.getValue())
            .build();
    }
}

// Touch-Event für Customer-Journey
@Entity
public class TouchEvent {
    @Id private UUID id;
    @ManyToOne private Customer customer;
    @ManyToOne private Lead lead; // Kann null sein für bestehende Kunden

    // Touch-Source
    @ManyToOne private Campaign campaign;
    @ManyToOne private CampaignDelivery delivery;

    // Touch-Type
    @Enumerated(EnumType.STRING) private TouchType type; // EMAIL_OPEN, EMAIL_CLICK, WEBSITE_VISIT, FORM_SUBMIT

    // Attribution-Data
    private LocalDateTime occurredAt;
    private String sourceUrl;
    @Column(columnDefinition = "jsonb")
    private Map<String, String> utmParameters;

    // Engagement-Score für Lead-Scoring
    private Integer engagementScore; // 1-10 basierend auf Touch-Type
}

// Automation-Workflow-Engine
@Entity
public class AutomationWorkflow {
    @Id private UUID id;
    private String name;
    private Boolean active = true;

    // Workflow-Trigger
    @Enumerated(EnumType.STRING) private WorkflowTrigger trigger; // LEAD_CREATED, EMAIL_OPENED, DAYS_SINCE_ACTIVITY
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> triggerConfiguration;

    // Workflow-Steps
    @OneToMany(cascade = CascadeType.ALL)
    @OrderBy("stepOrder")
    private List<WorkflowStep> steps;
}

@Entity
public class WorkflowStep {
    @Id private UUID id;
    @ManyToOne private AutomationWorkflow workflow;
    private Integer stepOrder;

    // Step-Type
    @Enumerated(EnumType.STRING) private WorkflowStepType type; // SEND_EMAIL, WAIT, CONDITION, UPDATE_LEAD_SCORE

    // Step-Configuration
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> configuration;

    // Conditions für Branching
    @Column(columnDefinition = "jsonb")
    private List<WorkflowCondition> conditions;
}
```

**Success Criteria:**
- Multi-Touch-Attribution berechnet Campaign-ROI korrekt
- Automation-Workflows triggern basierend auf Lead/Campaign-Events
- Touch-Events erfassen vollständige Customer-Journey
- Advanced Analytics-Reports verfügbar für Marketing-Team

**Next:** → [Production-Readiness](../../infrastruktur/PERFORMANCE_OPTIMIZATION_PLAN.md)

## ✅ Success Metrics

**Quantitative:**
- Campaign-Delivery-Rate: >98% via all.inkl SMTP
- A/B-Testing-Accuracy: >95% korrekte Variant-Assignment
- Attribution-Calculation: <5s für Complex-Customer-Journeys
- API Response Time: P95 <200ms (Campaign-Endpoints)
- Automation-Trigger-Latency: <30s für Event-based Workflows

**Qualitative:**
- Template-Editor ermöglicht Marketing-Team selbstständige Campaign-Erstellung
- A/B-Testing liefert statistisch signifikante Results
- Multi-Touch-Attribution zeigt ROI pro Campaign/Channel
- Automation-Workflows reduzieren manuellen Aufwand um >70%
- Campaign-Analytics-Dashboard unterstützt datengetriebene Entscheidungen

**Timeline:**
- Phase 1-2: Woche 13-17 (Foundation + Audience-Management)
- Phase 3-4: Woche 18-20 (A/B-Testing + Attribution)
- Production-Ready: Ende Woche 20

## 🔗 Related Documentation

**Foundation Knowledge:**
- **Marketing Automation:** → [MARKETING_AUTOMATION_STANDARDS.md](../../grundlagen/MARKETING_AUTOMATION_STANDARDS.md)
- **A/B-Testing Patterns:** → [AB_TESTING_STANDARDS.md](../../grundlagen/AB_TESTING_STANDARDS.md)
- **Attribution Models:** → [ATTRIBUTION_MODELS.md](../../grundlagen/ATTRIBUTION_MODELS.md)

**Implementation Details:**
- **Code Location:** `backend/modules/customer/core/src/main/java/campaigns/`
- **Database Schema:** `V227__kampagnen_schema.sql`
- **Test Files:** `CampaignServiceTest.java`, `ABTestingServiceTest.java`, `AttributionServiceTest.java`
- **Config Files:** `campaign-automation.yml`, `attribution-models.yml`

**KI-Production-Specs (verfügbar in Email-Posteingang):**
- **SMTP-Integration:** `../email-posteingang/workers/outbox-dispatcher.kt` für Campaign-Sending
- **Bounce-Handling:** `../email-posteingang/workers/bounce-handler.kt` für Deliverability
- **Event-Schema:** `../email-posteingang/events/cockpit-event-schema.json` für Campaign-Analytics
- **Production-Config:** `../email-posteingang/config/production-ready-config.yaml` (all.inkl SMTP-Settings)

**Related Plans:**
- **Dependencies:** → [Email-Posteingang Technical Concept](../email-posteingang/technical-concept.md)
- **Integration:** → [Lead-Erfassung Technical Concept](../lead-erfassung/technical-concept.md)
- **Performance:** → [Performance Optimization Plan](../../infrastruktur/PERFORMANCE_OPTIMIZATION_PLAN.md)

## 🤖 Claude Handover Section

**Für nächsten Claude:**

**Aktueller Stand:**
Technical Concept für Kampagnen nach Planungsmethodik erstellt. Complete Module Development mit A/B-Testing, Multi-Touch-Attribution, Automation-Workflows und all.inkl-Integration. 4 Phasen für 8-Wochen-Implementation nach stabiler E-Mail+Lead-Foundation.

**Nächster konkreter Schritt:**
1. **Nutze SMTP-Infrastructure** aus `../email-posteingang/workers/outbox-dispatcher.kt` für Campaign-Sending
2. **Integriere Bounce-Handling** aus `../email-posteingang/workers/bounce-handler.kt` für Campaign-Deliverability
3. **Verwende all.inkl-Config** aus `../email-posteingang/config/production-ready-config.yaml`

**Wichtige Dateien für Context:**
- `../email-posteingang/workers/` - **Outbox-Dispatcher + Bounce-Handler bereits implementiert**
- `../email-posteingang/config/` - **all.inkl SMTP-Konfiguration production-ready**
- `../email-posteingang/events/` - **Event-Schema für Campaign-Analytics verfügbar**
- `diskussionen/2025-09-18_finale-entwicklungsroadmap.md` - Complete Module Roadmap
- `backend/modules/customer/core/src/main/java/` - Bestehende Module-Struktur

**Offene Entscheidungen:**
- Beginne Campaign-Implementation oder integriere KI-Deliverables zuerst?
- Template-Editor als separater Service oder integriert in Campaign-Service?
- Attribution-Model-Defaults: Time-Decay vs. Linear für B2B-Context?

**Kontext-Links:**
- **Grundlagen:** → [Planungsmethodik](../../PLANUNGSMETHODIK.md)
- **Email-Foundation:** → [Email-Posteingang Technical Concept](../email-posteingang/technical-concept.md)
- **Lead-Integration:** → [Lead-Erfassung Technical Concept](../lead-erfassung/technical-concept.md)