# 📝 Lead-Management Implementation Plan

**📊 Plan Status:** 🟢 Production-Ready
**🎯 Owner:** Backend Team + Business Logic Team
**⏱️ Timeline:** Woche 1-4 (8-10h Implementation)
**🔧 Effort:** L (Large - State-Machine + Legal Compliance)

## 🎯 Executive Summary (für Claude)

**Mission:** Lead-Management mit Handelsvertretervertrag-konformer State-Machine und UserLeadSettings

**Problem:** Salesteam braucht rechtssichere Lead-Verwaltung mit 6-Monats-Schutz, 60-Tage-Aktivitätschecks und benutzerspezifischen Provisionen

**Solution:** Lead-State-Machine (REGISTERED→ACTIVE→REMINDER→GRACE→EXPIRED) + Stop-the-Clock-System + UserLeadSettings für 7%/2% Provisionen

**Timeline:** 8-10h von Entity-Design bis Production-Deployment mit Legal-Compliance-Validierung

**Impact:** Rechtssichere Lead-Verwaltung eliminiert Disputes + 60% weniger manuelle Administration

## 📋 Context & Dependencies

### Current State:
- ✅ **Handelsvertretervertrag-Analyse:** 6/60/10-Regelung definiert
- ✅ **Backend-Struktur:** `/backend/modules/customer/` ready
- ✅ **Event-System:** Ready für Cross-Module-Integration
- ✅ **TestDataBuilder:** Mock-Replacement etabliert

### Target State:
- 🎯 **Lead-State-Machine:** 7 Zustände mit vertragskonformen Transitions
- 🎯 **UserLeadSettings:** Benutzerspezifische Provisionen + Zeiträume
- 🎯 **Stop-Clock-System:** FreshFoodz-Verzögerungen pausieren Timer
- 🎯 **Event-Integration:** Lead-Status-Changes für Cockpit + Email

### Dependencies:
- **PostgreSQL:** Database-Schema + Constraints (READY)
- **Event-System:** Lead-Status-Events (READY)
- **ABAC Security:** Territory-basierte Zugriffskontrolle (READY)

## 🛠️ Implementation Phases (3 Phasen = 8-10h Gesamt)

### Phase 1: Lead-Entity + State-Machine (3-4h)

**Goal:** Core Lead-Entity mit vertragskonformer State-Machine

**Actions:**
1. **Lead-Entity Design:**
   ```java
   @Entity
   public class Lead {
       // Pflichtfelder (§2(8) Handelsvertretervertrag)
       private String company, location, centralContact;
       private LeadStatus status = REGISTERED;
       private Industry industry;

       // State-Machine Timestamps
       private LocalDateTime registeredAt, lastActivityAt;
       private LocalDateTime reminderSentAt, gracePeriodStartAt;

       // Stop-Clock System
       private LocalDateTime clockStoppedAt;
       private String stopReason;

       // Territory + Provisions
       private String territoryId, assignedUserId;
       private BigDecimal estimatedValue, provisionRate;
   }

   enum LeadStatus { REGISTERED, ACTIVE, REMINDER_SENT,
                    GRACE_PERIOD, EXPIRED, EXTENDED, STOP_CLOCK }
   ```

2. **State-Machine Service:**
   ```java
   @Service
   public class LeadStateMachineService {
       public void processActivityUpdate(Long leadId, ActivityType activity) {
           Lead lead = findLead(leadId);
           LeadStatus newStatus = calculateNewStatus(lead, activity);

           if (newStatus != lead.getStatus()) {
               updateLeadStatus(lead, newStatus);
               publishStatusChangeEvent(lead, newStatus, activity);
           }
       }

       private LeadStatus calculateNewStatus(Lead lead, ActivityType activity) {
           // 6/60/10-Regelung Implementation
           // REGISTERED → ACTIVE bei meaningful contact
           // ACTIVE → REMINDER_SENT nach 60 Tage ohne Aktivität
           // REMINDER_SENT → GRACE_PERIOD nach weiteren 60 Tage
           // GRACE_PERIOD → EXPIRED nach 10 Tage ohne Aktivität
       }
   }
   ```

3. **Database-Schema + Constraints:**
   ```sql
   CREATE TABLE leads (
       id BIGSERIAL PRIMARY KEY,
       company VARCHAR(255) NOT NULL,
       status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
       registered_at TIMESTAMP NOT NULL DEFAULT NOW(),
       territory_id VARCHAR(50) NOT NULL,
       assigned_user_id VARCHAR(50) NOT NULL
   );

   ALTER TABLE leads ADD CONSTRAINT chk_lead_status
       CHECK (status IN ('REGISTERED', 'ACTIVE', 'REMINDER_SENT',
                        'GRACE_PERIOD', 'EXPIRED', 'EXTENDED', 'STOP_CLOCK'));

   CREATE INDEX idx_leads_status_territory ON leads(status, territory_id);
   ```

**Success Criteria:** Lead-Entity mit State-Machine + Database-Constraints operational

### Phase 2: UserLeadSettings + Stop-Clock-System (3-4h)

**Goal:** Benutzerspezifische Konfiguration + Verzögerungs-Management

**Actions:**
1. **UserLeadSettings-Entity:**
   ```java
   @Entity
   public class UserLeadSettings {
       private String userId;
       private BigDecimal defaultProvisionRate = new BigDecimal("0.07"); // 7%
       private BigDecimal reducedProvisionRate = new BigDecimal("0.02"); // 2%
       private Integer leadProtectionMonths = 6;
       private Integer activityReminderDays = 60;
       private Integer gracePeriodDays = 10;
       private Set<String> assignedTerritories;
       private Boolean canStopClock = false;
   }
   ```

2. **Stop-Clock-Service:**
   ```java
   @Service
   public class StopClockService {
       public void stopLeadClock(Long leadId, String userId, String reason) {
           validateStopClockPermission(userId, reason);
           Lead lead = findLead(leadId);

           lead.setClockStoppedAt(LocalDateTime.now());
           lead.setStopReason(reason);
           lead.setStatus(STOP_CLOCK);

           auditService.logStopClock(leadId, userId, reason);
           publishClockStoppedEvent(leadId, userId, reason);
       }

       public void resumeLeadClock(Long leadId, String userId) {
           Lead lead = findLead(leadId);
           Duration stoppedDuration = calculateStoppedDuration(lead);

           // Adjust all timestamps by stopped duration
           adjustLeadTimestamps(lead, stoppedDuration);
           lead.setStatus(calculateResumedStatus(lead));

           auditService.logClockResume(leadId, userId, stoppedDuration);
       }
   }
   ```

3. **Provisions-Integration:**
   ```java
   @Service
   public class LeadProvisionService {
       public ProvisionCalculation calculateProvision(Lead lead, BigDecimal orderValue) {
           UserLeadSettings settings = getUserSettings(lead.getAssignedUserId());
           BigDecimal rate = determineProvisionRate(lead, settings);

           return ProvisionCalculation.builder()
               .leadId(lead.getId())
               .orderValue(orderValue)
               .provisionRate(rate)
               .provisionAmount(orderValue.multiply(rate))
               .build();
       }

       private BigDecimal determineProvisionRate(Lead lead, UserLeadSettings settings) {
           Duration leadDuration = Duration.between(lead.getRegisteredAt(), LocalDateTime.now());
           return leadDuration.toDays() > 120
               ? settings.getReducedProvisionRate()  // 2% nach 4 Monaten
               : settings.getDefaultProvisionRate(); // 7% normal
       }
   }
   ```

**Success Criteria:** UserLeadSettings + Stop-Clock-System with Legal-Compliance operational

### Phase 3: Event-Integration + Production-Hardening (2h)

**Goal:** Cross-Module-Integration + Monitoring + Testing

**Actions:**
1. **Event-System Integration:**
   ```java
   @EventListener
   public class LeadEventHandler {
       @EventListener
       public void handleLeadStatusChange(LeadStatusChangedEvent event) {
           cockpitUpdateService.updateLeadStatus(event.getLeadId(), event.getNewStatus());
           emailIntegrationService.notifyStatusChange(event.getLeadId(), event.getActivityType());
           analyticsService.trackLeadTransition(event);
       }

       @EventListener
       public void handleLeadClockStop(LeadClockStoppedEvent event) {
           managementNotificationService.sendStopClockAlert(event);
           performanceService.pauseLeadMetrics(event.getLeadId());
       }
   }
   ```

2. **Production-Monitoring:**
   ```java
   @Component
   public class LeadMetricsService {
       private final Counter leadsRegistered = Counter.builder("leads_registered_total")
           .tag("territory", "unknown").register(meterRegistry);

       private final Timer stateTransitionTimer = Timer.builder("lead_state_transition_duration_seconds")
           .register(meterRegistry);

       private final Gauge activeLeadsGauge = Gauge.builder("leads_active_count")
           .register(meterRegistry, this, LeadMetricsService::getActiveLeadsCount);
   }
   ```

3. **Integration-Testing:**
   ```java
   @SpringBootTest
   class LeadManagementIntegrationTest {
       @Test
       void testHandelsvertretervertrageCompliance() {
           Lead lead = createTestLead();

           // Test 6-Monats-Schutz
           assertThat(lead.getStatus()).isEqualTo(REGISTERED);

           // Test 60-Tage-Aktivitäts-Requirement
           leadStateMachineService.processActivityUpdate(lead.getId(), MEANINGFUL_CONTACT);
           assertThat(lead.getStatus()).isEqualTo(ACTIVE);

           // Test Grace-Period
           timeService.advanceBy(Duration.ofDays(65));
           scheduledLeadService.processExpiringLeads();
           assertThat(findLead(lead.getId()).getStatus()).isEqualTo(REMINDER_SENT);
       }

       @Test
       void testStopClockSystem() {
           Lead lead = createActiveLead();
           stopClockService.stopLeadClock(lead.getId(), "user123", "FRESHFOODZ_DELAY");

           Lead stopped = findLead(lead.getId());
           assertThat(stopped.getStatus()).isEqualTo(STOP_CLOCK);
           assertThat(stopped.getClockStoppedAt()).isNotNull();
       }
   }
   ```

**Success Criteria:** Cross-Module-Events + Monitoring + 100% Legal-Compliance-Testing operational

## ✅ Success Metrics

### **Immediate Success (8-10h):**
1. **Lead-State-Machine:** 7 Zustände mit vertragskonformen Transitions
2. **UserLeadSettings:** Benutzerspezifische Provisionen + Territory-Management
3. **Stop-Clock-System:** FreshFoodz-Verzögerungen mit fair Timer-Adjustment
4. **Event-Integration:** Real-time Updates zu Cockpit + Email-System
5. **Legal-Compliance:** 100% Handelsvertretervertrag-Konformität verified

### **Business Success (2-4 Wochen):**
1. **Dispute-Prevention:** Zero Handelsvertreter-Disputes durch Legal-Automation
2. **Administrative Efficiency:** 60% weniger manuelle Lead-Verwaltung
3. **Provision-Accuracy:** 100% korrekte 7%/2% Provisions-Berechnung

## 🔗 Related Documentation

### **Legal Foundation:**
- [Handelsvertretervertrag Requirements](../diskussionen/2025-09-18_handelsvertretervertrag-lead-requirements.md)
- [Enterprise Assessment](../ENTERPRISE_ASSESSMENT_FINAL.md)

### **Cross-Module Integration:**
- [Email-Integration Plan](02_EMAIL_INTEGRATION_PLAN.md) - Activity-Detection
- [Cockpit Integration](../../01_mein-cockpit/README.md) - Lead-Status-Display

### **Implementation Artifacts:**
- [Backend Implementation](../backend/) - Lead-Management Services
- [Testing Strategy](../testing/) - Legal-Compliance-Testing

## 🤖 Claude Handover Section

### **Current Priority (für neue Claude):**
```bash
# Phase 1: Lead-Entity + State-Machine
cd implementation-plans/
→ 01_LEAD_MANAGEMENT_PLAN.md (CURRENT)

# Start: Lead-Entity mit State-Machine
cd ../backend/modules/customer/

# Success: State-Machine operational + Legal-Compliance verified
# Next: UserLeadSettings + Stop-Clock-System
```

### **Context für neue Claude:**
- **Lead-Management:** Handelsvertretervertrag-konforme Lead-Verwaltung
- **Timeline:** 8-10h Implementation mit Legal-Compliance-Validierung
- **Dependencies:** Event-System + ABAC Security ready
- **Legal-Compliance:** 6/60/10-Regelung exakt implementiert

**🚀 Ready für rechtssichere Lead-Management-Implementation!**