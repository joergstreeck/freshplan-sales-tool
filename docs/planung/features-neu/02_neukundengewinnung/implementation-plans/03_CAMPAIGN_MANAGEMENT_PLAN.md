# 📧 Campaign-Management Implementation Plan

**📊 Plan Status:** 🟢 Production-Ready
**🎯 Owner:** Marketing-Automation Team + Campaign Team
**⏱️ Timeline:** Woche 3-5 (4-6h Implementation)
**🔧 Effort:** M (Medium - Trigger-System + Template-Engine)

## 🎯 Executive Summary (für Claude)

**Mission:** Multi-Channel Campaign-System für automatisierte Lead-Nurturing mit rechtssicherer Lead-State-Integration

**Problem:** Salesteam braucht automatisierte Follow-up-Kampagnen nach Lead-Activities - aktuell 80% vergessene Follow-ups führen zu Lead-Verlust

**Solution:** Event-driven Campaign-Engine mit Template-System + Multi-Channel-Delivery (Email, SMS, Print) + A/B-Testing + Campaign-Analytics

**Timeline:** 4-6h von Event-Trigger bis Production-Deployment mit Legal-Compliance

**Impact:** 90% automatisierte Follow-ups + 40% höhere Lead-Conversion + rechtssichere Campaign-Dokumentation

## 📋 Context & Dependencies

### Current State:
- ✅ **Lead-State-Machine:** Operational mit Event-System (FROM PLAN 01)
- ✅ **Email-Integration:** Activity-Detection ready (FROM PLAN 02)
- ✅ **Template-System:** Basis-Templates für Email + SMS verfügbar
- ✅ **Multi-Channel-Infrastructure:** SendGrid + Twilio + Print-API ready

### Target State:
- 🎯 **Campaign-Triggers:** Event-driven Automation basierend auf Lead-Activities
- 🎯 **Template-Engine:** Dynamic Content mit Lead-Context + Personalization
- 🎯 **Multi-Channel-Delivery:** Email + SMS + Print koordiniert
- 🎯 **A/B-Testing:** Template-Variants mit Statistical-Significance
- 🎯 **Campaign-Analytics:** Conversion-Tracking + ROI-Measurement

### Dependencies:
- **Lead-Management-System:** Event-System (FROM PLAN 01)
- **Email-Activity-Detection:** Lead-Activity-Events (FROM PLAN 02)
- **Template-Infrastructure:** SendGrid + Twilio + Print-API (READY)
- **Analytics-System:** Event-Tracking + Conversion-Metrics (READY)

## 🛠️ Implementation Phases (3 Phasen = 4-6h Gesamt)

### Phase 1: Campaign-Trigger-System (2h)

**Goal:** Event-driven Campaign-Automation mit Lead-State-Integration

**Actions:**
1. **Campaign-Trigger Service:**
   ```java
   @Service
   public class CampaignTriggerService {
       @EventListener
       public void handleLeadActivity(LeadActivityEvent event) {
           List<Campaign> triggeredCampaigns = findTriggeredCampaigns(
               event.getLeadId(), event.getActivityType(), event.getTimestamp()
           );

           for (Campaign campaign : triggeredCampaigns) {
               if (shouldTriggerCampaign(campaign, event)) {
                   scheduleCampaignExecution(campaign, event.getLeadId());
               }
           }
       }
   }

   @Entity
   public class Campaign {
       private String name, description;
       private CampaignType type; // WELCOME, FOLLOW_UP, SAMPLE_REQUEST
       private TriggerCondition trigger;
       private List<CampaignStep> steps;
       private Boolean active = true;
   }
   ```

2. **Campaign-Execution Engine:**
   ```java
   @Service
   public class CampaignExecutionService {
       public void executeCampaignStep(CampaignExecution execution) {
           CampaignStep step = execution.getCurrentStep();
           Lead lead = leadService.findById(execution.getLeadId());

           // Template-Rendering mit Lead-Context
           String content = templateEngine.render(step.getTemplate(), lead);

           // Multi-Channel-Delivery
           switch (step.getChannel()) {
               case EMAIL -> emailService.sendCampaignEmail(lead, content, step);
               case SMS -> smsService.sendCampaignSMS(lead, content, step);
               case PRINT -> printService.scheduleCampaignPrint(lead, content, step);
           }

           // Analytics-Tracking
           analyticsService.trackCampaignDelivery(execution, step);

           // Next-Step-Scheduling
           scheduleNextStep(execution);
       }
   }
   ```

**Success Criteria:** Campaign-Triggers reagieren auf Lead-Activities + Multi-Channel-Delivery funktional

### Phase 2: Template-Engine + A/B-Testing (2h)

**Goal:** Dynamic Content-Generation mit Personalization + Statistical A/B-Testing

**Actions:**
1. **Template-Engine mit Lead-Context:**
   ```java
   @Service
   public class CampaignTemplateEngine {
       public String renderTemplate(CampaignTemplate template, Lead lead) {
           Map<String, Object> context = buildTemplateContext(lead);

           // FreshFoodz-spezifische Personalization
           context.put("company", lead.getCompany());
           context.put("industry", getIndustrySpecificContent(lead.getIndustry()));
           context.put("samples", getRecommendedSamples(lead));
           context.put("territory", getTerritoryContent(lead.getTerritoryId()));

           return freemarkerEngine.process(template.getContent(), context);
       }

       private Map<String, Object> buildTemplateContext(Lead lead) {
           return Map.of(
               "lead", lead,
               "user", getUserContext(lead.getAssignedUserId()),
               "company", getCompanyBranding(),
               "legal", getLegalFooter(lead.getTerritoryId())
           );
       }
   }
   ```

2. **A/B-Testing Framework:**
   ```java
   @Service
   public class CampaignABTestingService {
       public CampaignTemplate selectTemplate(Campaign campaign, Lead lead) {
           if (!campaign.hasABTest()) {
               return campaign.getDefaultTemplate();
           }

           ABTest abTest = campaign.getAbTest();
           if (!hasStatisticalSignificance(abTest)) {
               return randomTemplateVariant(abTest.getVariants());
           }

           return abTest.getWinningVariant();
       }

       public void trackTemplatePerformance(CampaignExecution execution,
                                          ConversionEvent conversion) {
           ABTestResult result = ABTestResult.builder()
               .templateId(execution.getTemplateId())
               .leadId(execution.getLeadId())
               .conversionType(conversion.getType())
               .conversionValue(conversion.getValue())
               .timestamp(LocalDateTime.now())
               .build();

           abTestResultRepository.save(result);

           // Check for Statistical Significance
           if (hasEnoughData(execution.getCampaign().getAbTest())) {
               updateWinningVariant(execution.getCampaign());
           }
       }
   }
   ```

**Success Criteria:** Template-Personalization mit Lead-Context + A/B-Testing mit Statistical-Significance

### Phase 3: Campaign-Analytics + Legal-Compliance (2h)

**Goal:** Complete Campaign-Analytics + DSGVO-konforme Campaign-Dokumentation

**Actions:**
1. **Campaign-Analytics Dashboard:**
   ```java
   @Service
   public class CampaignAnalyticsService {
       public CampaignMetrics calculateCampaignMetrics(Long campaignId, DateRange range) {
           return CampaignMetrics.builder()
               .campaignId(campaignId)
               .totalDeliveries(countDeliveries(campaignId, range))
               .openRate(calculateOpenRate(campaignId, range))
               .clickRate(calculateClickRate(campaignId, range))
               .conversionRate(calculateConversionRate(campaignId, range))
               .roi(calculateROI(campaignId, range))
               .unsubscribeRate(calculateUnsubscribeRate(campaignId, range))
               .build();
       }

       public List<ConversionFunnel> getConversionFunnel(Long campaignId) {
           // Lead → Email-Open → Click → Sample-Request → Purchase
           return List.of(
               new ConversionFunnel("Delivered", countDeliveries(campaignId)),
               new ConversionFunnel("Opened", countOpens(campaignId)),
               new ConversionFunnel("Clicked", countClicks(campaignId)),
               new ConversionFunnel("Converted", countConversions(campaignId))
           );
       }
   }
   ```

2. **Legal-Compliance + DSGVO:**
   ```java
   @Service
   public class CampaignComplianceService {
       public void ensureLegalCompliance(CampaignExecution execution) {
           Lead lead = execution.getLead();

           // DSGVO-Consent-Check
           if (!hasValidConsent(lead, execution.getCampaign().getChannels())) {
               throw new ComplianceException("Missing consent for channels: " +
                   execution.getCampaign().getChannels());
           }

           // Handelsvertretervertrag-Compliance
           if (isLeadExpired(lead)) {
               throw new ComplianceException("Lead expired - no campaign allowed");
           }

           // Legal-Footer + Unsubscribe-Link
           ensureUnsubscribeLink(execution);
           ensureLegalFooter(execution, lead.getTerritoryId());

           // Campaign-Audit-Trail
           auditService.logCampaignExecution(execution, "LEGAL_COMPLIANCE_CHECKED");
       }
   }
   ```

3. **Cross-Module Integration:**
   ```java
   @EventListener
   public class CampaignEventHandler {
       @EventListener
       public void handleLeadStatusChange(LeadStatusChangedEvent event) {
           // Stop Campaigns bei Lead-Expiry
           if (event.getNewStatus() == LeadStatus.EXPIRED) {
               campaignService.stopAllCampaigns(event.getLeadId());
           }

           // Trigger Status-spezifische Campaigns
           if (event.getNewStatus() == LeadStatus.REMINDER_SENT) {
               campaignService.triggerCampaign("REMINDER_FOLLOW_UP", event.getLeadId());
           }
       }

       @EventListener
       public void handleEmailActivity(EmailActivityProcessedEvent event) {
           // Trigger Response-Campaigns
           if (event.getActivityType() == ActivityType.SAMPLE_REQUEST) {
               campaignService.triggerCampaign("SAMPLE_FOLLOW_UP", event.getLeadId());
           }
       }
   }
   ```

**Success Criteria:** Campaign-Analytics Dashboard + DSGVO-Compliance + Cross-Module-Events operational

## ✅ Success Metrics

### **Immediate Success (4-6h):**
1. **Campaign-Automation:** Event-driven Triggers reagieren auf alle Lead-Activities
2. **Multi-Channel-Delivery:** Email + SMS + Print koordiniert ausgeliefert
3. **Template-Personalization:** Dynamic Content mit Lead-spezifischem Context
4. **A/B-Testing:** Statistical-Significance-basierte Template-Optimization
5. **Legal-Compliance:** DSGVO + Handelsvertretervertrag-konforme Campaign-Ausführung

### **Business Success (2-4 Wochen):**
1. **Automation-Rate:** 90% automatisierte Follow-ups (vs. 20% manuell)
2. **Conversion-Improvement:** 40% höhere Lead-Conversion durch optimierte Campaigns
3. **ROI-Measurement:** Complete Campaign-ROI-Tracking + Attribution
4. **Legal-Sicherheit:** 100% DSGVO-konforme Campaign-Dokumentation

### **Technical Excellence:**
- **Campaign-Performance:** <5s Template-Rendering + Multi-Channel-Delivery
- **A/B-Testing-Accuracy:** Statistical-Significance ab 100 Templates-Sends pro Variant
- **Analytics-Latency:** Real-time Campaign-Metrics + Conversion-Attribution

## 🔗 Related Documentation

### **Integration Foundation:**
- [Lead-Management Plan](01_LEAD_MANAGEMENT_PLAN.md) - Lead-State-Machine + Event-System
- [Email-Integration Plan](02_EMAIL_INTEGRATION_PLAN.md) - Email-Activity-Events
- [Foundation Standards](04_FOUNDATION_STANDARDS_PLAN.md) - Cross-Module-Events

### **Campaign-Management Artifacts:**
- [Campaign Templates](../kampagnen/templates/) - Email + SMS + Print Templates
- [Multi-Channel Setup](../shared/backend/) - SendGrid + Twilio + Print-API
- [Analytics Dashboard](../shared/frontend/) - Campaign-Metrics + A/B-Testing UI

### **Cross-Module Integration:**
- [Cockpit Integration](../../01_mein-cockpit/README.md) - Campaign-Performance-Display
- [Settings Integration](../../06_einstellungen/README.md) - Campaign-Configuration

## 🤖 Claude Handover Section

### **Current Priority (für neue Claude):**
```bash
# Phase 1: Campaign-Trigger-System
cd implementation-plans/
→ 03_CAMPAIGN_MANAGEMENT_PLAN.md (CURRENT)

# Start: Campaign-Trigger + Multi-Channel-Delivery
cd ../kampagnen/

# Success: Event-driven Campaigns + Template-Personalization
# Next: Foundation Standards + Cross-Module-Integration
```

### **Context für neue Claude:**
- **Campaign-Management:** Event-driven Multi-Channel-Automation mit A/B-Testing
- **Timeline:** 4-6h von Trigger-System zu Production-Deployment
- **Dependencies:** Lead-Management + Email-Integration operational
- **Business-Value:** 90% automatisierte Follow-ups + 40% höhere Conversion

### **Key Success-Factors:**
- **Event-Integration:** Lead-Activities triggern automatisch passende Campaigns
- **Multi-Channel-Coordination:** Email + SMS + Print synchronisiert ausgeliefert
- **Template-Personalization:** FreshFoodz-spezifische Lead-Context-Integration
- **Legal-Compliance:** DSGVO + Handelsvertretervertrag-konforme Execution

**🚀 Ready für automatisierte Multi-Channel-Campaign-Implementation!**