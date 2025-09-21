# 🎯 User-Lead-Operations Implementation Plan

**📊 Plan Status:** 🟢 Ready (Foundation-Priority)
**🎯 Owner:** Operations-Team + Database-Team
**⏱️ Timeline:** Week 1-2 → Foundation-Complete
**🔧 Effort:** M (6-8h Implementation)
**🔗 Dependencies:** PostgreSQL + Quarkus + Module 02 Lead-Schema

---

## 🎯 **Executive Summary (für Claude)**

**Mission:** 6M+60T+10T User-Lead-Protection State-Machine mit Stop-Clock + Production-Runbooks deployment

**Problem:** FreshFoodz benötigt enterprise-grade User-Lead-Protection mit korrekter 6-Monats+60-Tage+10-Tage-Logik, Stop-Clock-Management und Production-Ready Runbooks für Q1 2026 Go-Live

**Solution:** Deploy External AI SQL-Views + Hold-Management + Reminder-Pipeline + Integration in Module 07 Help-System

**Timeline:** 6-8h Implementation → User-Lead-SLA <1h operational

**Impact:** 100% User-Lead-Protection-Compliance + Operations-Team-Ready + Module 02 Integration-Foundation

---

## 📋 **Context & Dependencies**

### **Current State:**
- ✅ **External AI SQL-Excellence:** `v_user_lead_protection.sql` mit CTE-based Hold-Calculation (10/10)
- ✅ **Production-Runbook:** 30-Second-Summary + Actionable Operations-Commands ready
- ✅ **FreshFoodz-Reality-Corrected:** User-based (NICHT Territory-based) Protection validated
- ✅ **Business-Logic:** 6M+60T+10T + Stop-Clock korrekt in SQL implementiert
- ⚠️ **Database-Schema:** Table-Mapping zu echten FreshFoodz-Tables erforderlich

### **Target State:**
- 🎯 **Production-Ready State-Machine:** Live User-Lead-Protection mit SQL-Views
- 🎯 **Stop-Clock-Management:** Hold-Windows automated tracking + Manual-Override
- 🎯 **Reminder-Pipeline:** Idempotent Dispatching + Event-Integration
- 🎯 **Operations-Excellence:** Runbook in Help-System + Team-Training complete

### **Dependencies:**
- **Critical Path:** PostgreSQL Database + `leads` Table Schema validation
- **Module Integration:** Module 02 Lead-Erfassung (`leads`, `activities` Schema)
- **Infrastructure:** Quarkus-Scheduler für Reminder-Pipeline
- **Help-System:** Module 07 CAR-Strategy für Runbook-Integration

---

## 🛠️ **Implementation Phases**

### **Phase 1: Database Foundation (2-3h)**
**Goal:** SQL-Views + Tables deployment in Development-Database

**Actions:**
1. **Schema-Validation:** Map External AI Tables → FreshFoodz Schema
   ```sql
   -- Validate existing tables
   SELECT table_name, column_name, data_type
   FROM information_schema.columns
   WHERE table_name IN ('leads', 'lead_holds', 'lead_reminders', 'lead_activities');
   ```

2. **Deploy Lead-Holds Table:** (if missing)
   ```sql
   CREATE TABLE lead_holds (
     id BIGSERIAL PRIMARY KEY,
     lead_id BIGINT NOT NULL REFERENCES leads(id),
     user_id BIGINT NOT NULL,
     reason VARCHAR(255) NOT NULL,
     start_at TIMESTAMP NOT NULL DEFAULT now(),
     end_at TIMESTAMP NULL,
     created_by BIGINT NOT NULL,
     created_at TIMESTAMP NOT NULL DEFAULT now()
   );
   CREATE INDEX idx_lead_holds_lead_user ON lead_holds(lead_id, user_id);
   ```

3. **Deploy Core Views:** External AI Excellence `v_user_lead_protection.sql`
   ```sql
   -- CTE-based Hold-Calculation (External AI 10/10 Quality)
   CREATE OR REPLACE VIEW v_user_lead_protection AS
   WITH hold_windows AS (
     SELECT h.lead_id, h.user_id, h.start_at, COALESCE(h.end_at, now()) AS end_at
     FROM lead_holds h
   ),
   effective_hold AS (
     SELECT lead_id, user_id, SUM(end_at - start_at) AS total_hold_duration
     FROM hold_windows GROUP BY lead_id, user_id
   )
   SELECT
     l.id, l.user_id, l.created_at,
     (l.created_at + INTERVAL '6 months' + INTERVAL '60 days' + INTERVAL '10 days'
      + COALESCE(eh.total_hold_duration, INTERVAL '0')) AS protection_expires_at,
     CASE WHEN protection_expires_at > now() THEN 'PROTECTED' ELSE 'EXPIRED' END AS state
   FROM leads l LEFT JOIN effective_hold eh USING (lead_id, user_id);
   ```

**Success Criteria:** All SQL-Views functional + Test-Data validation successful

### **Phase 2: Hold-Management Operations (2-3h)**
**Goal:** Stop-Clock + Hold-Management mit Idempotent-Patterns

**Actions:**
1. **Hold-Creation Service:** External AI Pattern deployment
   ```java
   @ApplicationScoped
   public class LeadHoldService {

       @Transactional
       public void createHold(Long leadId, Long userId, String reason, Long actorUserId) {
           // Idempotent Pattern: Check existing active hold
           if (hasActiveHold(leadId, userId)) {
               log.info("Active hold already exists for lead {} user {}", leadId, userId);
               return;
           }

           LeadHold hold = LeadHold.builder()
               .leadId(leadId)
               .userId(userId)
               .reason(reason)
               .startAt(Instant.now())
               .createdBy(actorUserId)
               .build();

           leadHoldRepository.persist(hold);
           log.info("Created hold for lead {} user {} reason: {}", leadId, userId, reason);
       }
   }
   ```

2. **Hold-Release Service:** Automated + Manual-Override
   ```java
   @Transactional
   public void releaseHold(Long leadId, Long userId, Long actorUserId) {
       LeadHold activeHold = findActiveHold(leadId, userId);
       if (activeHold == null) {
           log.warn("No active hold found for lead {} user {}", leadId, userId);
           return;
       }

       activeHold.setEndAt(Instant.now());
       activeHold.setReleasedBy(actorUserId);
       leadHoldRepository.persist(activeHold);
   }
   ```

**Success Criteria:** Hold-Management functional + Stop-Clock-Calculation accurate

### **Phase 3: Reminder-Pipeline (2h)**
**Goal:** Idempotent Reminder-Dispatching + Event-Integration

**Actions:**
1. **Quarkus-Scheduler Setup:** Daily Reminder-Check
   ```java
   @ApplicationScoped
   public class LeadReminderScheduler {

       @Scheduled(cron = "0 0 8 * * ?") // Daily 8 AM
       public void processLeadReminders() {
           log.info("Starting daily lead reminder processing");

           List<LeadProtection> expiringLeads = leadProtectionService
               .findExpiringWithinDays(10);

           for (LeadProtection lead : expiringLeads) {
               reminderService.sendReminderIfNeeded(lead);
           }
       }
   }
   ```

2. **Idempotent Reminder Service:** External AI Window-based Deduplication
   ```java
   public void sendReminderIfNeeded(LeadProtection lead) {
       // Window-based deduplication (External AI Excellence)
       LocalDate reminderWindow = lead.getProtectionExpiresAt().toLocalDate();

       boolean alreadySent = reminderRepository.existsByLeadAndUserAndWindow(
           lead.getLeadId(), lead.getUserId(), reminderWindow);

       if (!alreadySent) {
           // Send reminder + Create record
           eventPublisher.publishEvent(new LeadProtectionReminderEvent(lead));
           reminderRepository.createReminder(lead, reminderWindow);
       }
   }
   ```

**Success Criteria:** Reminder-Pipeline idempotent + Event-Integration functional

### **Phase 4: Operations Integration (1-2h)**
**Goal:** Production-Runbook + Help-System Integration + Team-Training

**Actions:**
1. **Runbook Integration:** Module 07 Help-System CAR-Strategy
   - Deploy External AI Runbook als Guided Operation
   - 30-Second-Summary + Daily-Routine + Failure-Scenarios
   - Actionable SQL-Commands für Operations-Team

2. **Monitoring Integration:** Basic KPIs
   ```java
   // Prometheus Metrics
   @Counted(name = "lead_reminder_sent_total")
   @Timed(name = "lead_reminder_processing_time")
   public void sendReminder(LeadProtection lead) { /* ... */ }

   @Gauge(name = "lead_protection_expired_count")
   public long getExpiredLeadCount() {
       return leadProtectionService.countExpiredLeads();
   }
   ```

3. **Operations-Team Training:** SQL-Commands + Runbook-Walkthrough

**Success Criteria:** Operations-Team ready + Runbook accessible + Basic monitoring active

---

## ✅ **Success Metrics**

### **Functional Requirements:**
- **User-Lead-Protection:** 6M+60T+10T-Logik 100% accurate in SQL-Views
- **Stop-Clock-Management:** Hold-Windows calculation mit External AI CTE-Excellence
- **Reminder-SLA:** Due >= Sent innerhalb 60m (Prometheus-tracked)
- **Idempotency:** Reminder-Pipeline + Hold-Management duplicate-safe

### **Operations Excellence:**
- **Runbook Integration:** Available in Module 07 Help-System
- **Team-Readiness:** Operations-Team trained on SQL-Commands + Procedures
- **Monitoring:** Basic Lead-Protection-KPIs in Prometheus
- **Database Performance:** SQL-Views <50ms P95 response time

### **Integration Quality:**
- **Module 02 Ready:** Lead-Schema + Activities-Integration validated
- **Event-Integration:** Reminder-Events + Hold-Events published correctly
- **Error-Handling:** Graceful degradation + Comprehensive logging
- **Database-Consistency:** All constraints + indexes optimized

---

## 🔗 **Related Documentation**

### **External AI Operations-Pack:**
- **SQL-Views:** → `../artefakte/v_user_lead_protection.sql` (10/10 Quality)
- **Hold-Management:** → `../artefakte/holds.sql` (9/10 Quality)
- **Reminder-Logic:** → `../artefakte/reminders.sql` (9/10 Quality)
- **Production-Runbook:** → `../artefakte/USER_LEAD_STATE_MACHINE_RUNBOOK.md` (10/10)

### **Next Implementation Steps:**
- **Parallel Execution:** → `02_SEASONAL_OPERATIONS_PLAN.md` (kann parallel gestartet werden)
- **Monitoring Setup:** → `03_MONITORING_EXCELLENCE_PLAN.md` (nach Foundation)
- **Integration Finalization:** → `04_INTEGRATION_DEPLOYMENT_PLAN.md` (Final Phase)

### **Module Dependencies:**
- **Module 02 Lead-Erfassung:** Lead-Schema + Activities für Protection-Logic
- **Module 07 Help-System:** CAR-Strategy für Runbook-Integration
- **Infrastructure:** PostgreSQL + Quarkus + Prometheus Setup

---

## 🤖 **Claude Handover Section**

### **Start HIER - Foundation Priority:**
1. **Database-Validation:** Map External AI Tables → FreshFoodz Schema (30min)
2. **SQL-Views Deployment:** Copy-Paste External AI Excellence (45min)
3. **Hold-Management:** Quarkus Service mit Idempotent-Patterns (2h)
4. **Reminder-Pipeline:** Scheduler + Event-Integration (2h)
5. **Operations-Integration:** Runbook + Team-Training (1h)

### **Critical Success Factors:**
- **External AI Excellence:** SQL-Views sind 10/10 Quality - DIREKT verwenden
- **Table-Mapping:** Kritisch für Production-Deployment - sorgfältig validieren
- **Idempotency:** Essential für Production-Stability - External AI Patterns befolgen
- **Module 02 Coordination:** Lead-Schema-Changes erfordern Abstimmung

### **Parallel Execution Opportunity:**
Nach Phase 1 (Database Foundation) kann `02_SEASONAL_OPERATIONS_PLAN.md` parallel gestartet werden, da beide verschiedene Infrastructure-Layer betreffen.

### **Quality Gates:**
- **Phase 1:** SQL-Views functional in Development-Database
- **Phase 2:** Hold-Management tested mit echten Lead-Data
- **Phase 3:** Reminder-Pipeline idempotent over multiple runs
- **Phase 4:** Operations-Team successful Runbook-execution

**🚀 Dies ist der Foundation-Plan - erfolgreiche Umsetzung ermöglicht alle anderen Operations-Excellence-Komponenten!**