# 🏗️ Foundation Standards Implementation Plan

**📊 Plan Status:** 🟢 Production-Ready
**🎯 Owner:** Architecture Team + Foundation Team
**⏱️ Timeline:** Woche 1-2 (3-4h Implementation)
**🔧 Effort:** S (Small - Standards + Documentation + Cross-Module Setup)

## 🎯 Executive Summary (für Claude)

**Mission:** Production-Ready Foundation Standards für 8-Module-Ecosystem mit Neukundengewinnung als Lead-Implementierung

**Problem:** 8 Module benötigen einheitliche Standards für Events, APIs, Security, Testing - aktuell inkonsistente Integration-Patterns

**Solution:** Foundation-Standards-Enhancement mit Neukundengewinnung-Patterns als Reference-Implementation + Cross-Module-Integration-Standards

**Timeline:** 3-4h von Current Foundation zu Enhanced Standards mit Copy-Paste-Templates

**Impact:** Alle 8 Module können einheitliche Integration-Standards nutzen + 80% weniger Integration-Aufwand für neue Module

## 📋 Context & Dependencies

### Current State:
- ✅ **Foundation-Basis:** EVENT_CATALOG.md + API_STANDARDS.md + Security-Framework vorhanden
- ✅ **Neukundengewinnung-Implementation:** Lead-Management + Email-Integration + Campaigns als Patterns
- ✅ **Cross-Module-Events:** Event-System operational für Lead-Status-Changes
- ✅ **ABAC Security:** Territory-basierte Zugriffskontrolle ready

### Target State:
- 🎯 **Enhanced EVENT_CATALOG:** Neukundengewinnung-Events als Reference-Patterns
- 🎯 **API Standards v2:** Idempotency + Correlation-ID + Multi-Tenancy-Patterns
- 🎯 **Security-Templates:** ABAC + Territory-Management für alle Module
- 🎯 **Testing-Standards:** Unit + Integration + E2E-Patterns aus Neukundengewinnung
- 🎯 **Cross-Module-Integration:** Copy-Paste-Templates für neue Module

### Dependencies:
- **Foundation-Framework:** EVENT_CATALOG.md + API_STANDARDS.md (READY)
- **Neukundengewinnung-Patterns:** Lead-Management + Email + Campaigns (FROM PLANS 01-03)
- **Event-System:** Cross-Module-Events operational (READY)
- **Security-Framework:** ABAC + Territory-Management (READY)

## 🛠️ Implementation Phases (3 Phasen = 3-4h Gesamt)

### Phase 1: EVENT_CATALOG Enhancement (1h)

**Goal:** Neukundengewinnung-Events als Reference-Patterns für alle Module

**Actions:**
1. **Lead-Management Event-Patterns:**
   ```yaml
   # Enhanced EVENT_CATALOG.md
   Domain Events - Lead Management (Reference Implementation):

   lead.status.changed:
     source: "neukundengewinnung.lead-management"
     type: "com.freshplan.lead.status.changed.v1"
     data:
       leadId: Long
       oldStatus: LeadStatus
       newStatus: LeadStatus
       triggeredBy: ActivityType
       timestamp: ISO8601
     subscribers: [cockpit, campaigns, analytics]

   lead.activity.detected:
     source: "neukundengewinnung.email-integration"
     type: "com.freshplan.lead.activity.detected.v1"
     data:
       leadId: Long
       activityType: ActivityType
       source: ActivitySource (EMAIL_DETECTED, MANUAL, API)
       confidence: Double
       metadata: Map<String, Object>
     subscribers: [lead-management, campaigns, analytics]
   ```

2. **Cross-Module Event-Patterns:**
   ```yaml
   Campaign Events (Cross-Module Reference):

   campaign.triggered:
     source: "neukundengewinnung.campaigns"
     type: "com.freshplan.campaign.triggered.v1"
     data:
       campaignId: Long
       leadId: Long
       trigger: TriggerType
       channels: List<Channel>
     subscribers: [analytics, audit]

   Standard Event Envelope (All Modules):
   {
     "specversion": "1.0",
     "type": "com.freshplan.{domain}.{aggregate}.{action}.v{version}",
     "source": "freshplan.{module}.{service}",
     "id": "UUID",
     "time": "ISO8601",
     "datacontenttype": "application/json",
     "subject": "{domain}/{aggregate-id}",
     "data": { ... }
   }
   ```

**Success Criteria:** EVENT_CATALOG.md mit Neukundengewinnung-Patterns + Standard-Event-Envelope

### Phase 2: API Standards v2 + Security-Templates (1-2h)

**Goal:** Enhanced API-Standards mit Multi-Tenancy + Security-Templates für alle Module

**Actions:**
1. **API Standards v2 Enhancement:**
   ```markdown
   # API_STANDARDS.md v2 (Enhanced)

   ## Idempotency (Neukundengewinnung-Pattern)

   ### Headers:
   - Idempotency-Key: UUID (24-48h TTL)
   - If-Match: ETag für Optimistic Locking
   - X-Correlation-ID: End-to-End Tracing

   ### Scope-Definition:
   - Key: Unique operation identifier
   - Route: API endpoint path
   - Actor: User/System performing operation

   ### Implementation (Reference from Lead-Management):
   ```java
   @RestController
   public class LeadController {
       @PostMapping("/leads")
       @Idempotent(scope = {KEY, ROUTE, ACTOR}, ttl = "24h")
       public ResponseEntity<Lead> createLead(
           @RequestBody CreateLeadRequest request,
           @Header("Idempotency-Key") String idempotencyKey
       ) {
           // Idempotency-Check via Spring AOP
           return leadService.createLead(request);
       }
   }
   ```

2. **Multi-Tenancy + Territory-Security:**
   ```java
   // Security-Template für alle Module
   @Component
   public class TerritorySecurityFilter {
       @PreAuthorize("@territorySecurityService.hasAccess(#leadId, authentication)")
       public Lead findLead(@Param("leadId") Long leadId) {
           return leadRepository.findById(leadId);
       }
   }

   @Service
   public class TerritorySecurityService {
       public boolean hasAccess(Long resourceId, Authentication auth) {
           UserContext user = extractUserContext(auth);
           Resource resource = resourceService.findById(resourceId);

           return user.getTerritories().contains(resource.getTerritoryId()) ||
                  user.hasRole("ADMIN");
       }
   }
   ```

**Success Criteria:** API_STANDARDS.md v2 + Security-Templates für alle Module copy-paste-ready

### Phase 3: Testing-Standards + Cross-Module-Templates (1h)

**Goal:** Testing-Patterns aus Neukundengewinnung + Integration-Templates für neue Module

**Actions:**
1. **Testing-Standards (Reference Implementation):**
   ```java
   // Unit-Testing-Pattern (aus Lead-Management)
   @ExtendWith(MockitoExtension.class)
   class LeadStateMachineServiceTest {
       @Test
       void processActivityUpdate_meaningfulContact_shouldTransitionToActive() {
           // Given - Testdaten mit TestDataBuilder
           Lead lead = LeadTestDataBuilder.aLead()
               .withStatus(REGISTERED)
               .withRegisteredAt(LocalDateTime.now().minusDays(1))
               .build();

           // When - System under Test ausführen
           leadStateMachineService.processActivityUpdate(
               lead.getId(), ActivityType.MEANINGFUL_CONTACT
           );

           // Then - Ergebnis validieren
           Lead updatedLead = leadRepository.findById(lead.getId());
           assertThat(updatedLead.getStatus()).isEqualTo(ACTIVE);
           assertThat(updatedLead.getLastActivityAt()).isNotNull();
       }
   }
   ```

2. **Integration-Testing-Pattern:**
   ```java
   @QuarkusTest
   @TestMethodOrder(OrderAnnotation.class)
   class LeadManagementIntegrationTest {
       @Test
       @Order(1)
       void createLead_validData_shouldTriggerEvents() {
           // API-Call
           given()
               .contentType(ContentType.JSON)
               .body(validLeadRequest())
           .when()
               .post("/api/leads")
           .then()
               .statusCode(201)
               .body("id", notNullValue())
               .body("status", equalTo("REGISTERED"));

           // Event-Validation
           assertThat(eventCollector.getEvents())
               .hasSize(1)
               .extracting("type")
               .contains("com.freshplan.lead.created.v1");
       }
   }
   ```

3. **Cross-Module-Integration-Template:**
   ```java
   // Template für neue Module
   @Component
   public class ModuleIntegrationTemplate {
       // Standard Event-Publisher
       @EventPublisher
       public void publishDomainEvent(DomainEvent event) {
           eventBus.publish(enhanceWithStandardFields(event));
       }

       // Standard Event-Listener
       @EventListener
       public void handleCrossDomainEvent(CrossDomainEvent event) {
           if (isRelevantForThisModule(event)) {
               processEvent(event);
           }
       }

       // Standard Territory-Security
       @PreAuthorize("@territorySecurityService.hasAccess(#resourceId, authentication)")
       public Resource findResource(Long resourceId) {
           return resourceService.findById(resourceId);
       }
   }
   ```

**Success Criteria:** Testing-Standards + Cross-Module-Templates copy-paste-ready für alle 8 Module

## ✅ Success Metrics

### **Immediate Success (3-4h):**
1. **EVENT_CATALOG Enhanced:** Neukundengewinnung-Events als Reference-Patterns documented
2. **API Standards v2:** Idempotency + Multi-Tenancy + Correlation-ID Standards operational
3. **Security-Templates:** Territory-Security + ABAC-Patterns für alle Module copy-paste-ready
4. **Testing-Standards:** Unit + Integration + E2E-Patterns als Copy-Paste-Templates
5. **Cross-Module-Integration:** Standard-Templates für neue Module ready

### **Foundation Success (1-2 Wochen):**
1. **Integration-Consistency:** Alle 8 Module nutzen einheitliche Event + API + Security-Patterns
2. **Development-Velocity:** 80% weniger Integration-Aufwand für neue Module
3. **Code-Quality:** Consistent Testing + Security + Event-Standards across all modules
4. **Maintainability:** Standard-Patterns reduzieren Technical-Debt + Training-Aufwand

### **Technical Excellence:**
- **Event-Consistency:** 100% CloudEvents 1.0 Compliance + Standard-Envelope
- **API-Consistency:** 100% Idempotency + Correlation-ID + ETag-Support
- **Security-Consistency:** 100% Territory-Security + ABAC-Patterns
- **Testing-Consistency:** ≥80% Coverage mit Standard-Patterns

## 🔗 Related Documentation

### **Foundation Base:**
- [Foundation Framework](../../../grundlagen/) - Basis EVENT_CATALOG + API_STANDARDS
- [Security Framework](../../../grundlagen/SECURITY_STANDARDS.md) - ABAC + Territory-Management
- [Testing Framework](../../../grundlagen/TESTING_STANDARDS.md) - Testing-Patterns

### **Reference Implementation:**
- [Lead-Management Plan](01_LEAD_MANAGEMENT_PLAN.md) - Event + Security-Patterns
- [Email-Integration Plan](02_EMAIL_INTEGRATION_PLAN.md) - Cross-Module-Events
- [Campaign-Management Plan](03_CAMPAIGN_MANAGEMENT_PLAN.md) - Multi-Channel-Events

### **Cross-Module Integration:**
- [Integration Infrastructure](../../00_infrastruktur/integration/README.md) - Event-Bus + Gateway
- [All 8 Modules](../../) - Foundation-Standards-Integration

## 🤖 Claude Handover Section

### **Current Priority (für neue Claude):**
```bash
# Phase 1: EVENT_CATALOG Enhancement
cd implementation-plans/
→ 04_FOUNDATION_STANDARDS_PLAN.md (CURRENT)

# Start: Foundation-Standards Enhancement
cd ../../../grundlagen/

# Success: Enhanced Standards + Copy-Paste-Templates
# Next: Cross-Module-Integration + Rollout
```

### **Context für neue Claude:**
- **Foundation-Standards:** EVENT_CATALOG + API + Security + Testing Enhancement
- **Timeline:** 3-4h Enhancement mit Neukundengewinnung-Patterns als Reference
- **Dependencies:** Foundation-Framework + Neukundengewinnung-Implementation ready
- **Business-Value:** 80% weniger Integration-Aufwand für alle 8 Module

### **Key Success-Factors:**
- **Reference-Implementation:** Neukundengewinnung-Patterns als Standard-Templates
- **Copy-Paste-Ready:** Alle Templates sofort für neue Module nutzbar
- **Cross-Module-Consistency:** Einheitliche Event + API + Security-Standards
- **Development-Velocity:** Standard-Patterns beschleunigen neue Module-Entwicklung

**🚀 Ready für Foundation-Standards-Enhancement mit Production-Ready Templates!**