# FC-009: Integration Points - E-Mail Template Integration

## KPI-Tracking Integration (FC-016)

### Renewal-Metriken für KPI Dashboard

```java
@ApplicationScoped
public class RenewalKpiProvider {
    
    @Inject ContractRepository contractRepo;
    @Inject EventBus eventBus;
    
    // Liefert Daten für FC-016 KPI Engine
    public RenewalKpiMetrics calculateRenewalMetrics(DateRange range) {
        return RenewalKpiMetrics.builder()
            .totalRenewalsInPeriod(countRenewalsInRange(range))
            .successfulRenewals(countSuccessfulRenewals(range))
            .averageRenewalTime(calculateAvgRenewalCycleTime(range))
            .renewalQuote(calculateRenewalSuccessRate(range))
            .atRiskValue(calculateRevenueAtRisk())
            .deferredRenewals(countDeferredRenewals(range))
            .build();
    }
    
    // Event Publishing für KPI Updates
    @Observes ContractRenewalStarted event
    public void publishRenewalKpiEvent(ContractRenewalStarted event) {
        eventBus.publish(new KpiEvent(
            KpiType.RENEWAL_STARTED,
            Map.of(
                "contractId", event.getContractId(),
                "contractValue", event.getContractValue(),
                "daysUntilExpiry", event.getDaysUntilExpiry()
            )
        ));
    }
}
```

## E-Mail Integration (FC-003)

### Renewal-spezifische E-Mail Templates

```java
public enum RenewalEmailTemplates {
    RENEWAL_90_DAYS("renewal_reminder_90_days"),
    RENEWAL_60_DAYS("renewal_reminder_60_days"),
    RENEWAL_30_DAYS("renewal_reminder_30_days"),
    RENEWAL_OVERDUE("renewal_overdue"),
    RENEWAL_CONFIRMATION("renewal_confirmation"),
    PRICE_ADJUSTMENT("price_adjustment_notice");
}

@ApplicationScoped
public class RenewalEmailService {
    
    @Inject EmailTemplateService templateService;
    @Inject EmailService emailService;
    
    public void sendRenewalReminder(Contract contract, int daysUntilExpiry) {
        String templateId = selectTemplate(daysUntilExpiry);
        
        TemplateContext context = buildRenewalContext(contract);
        
        // Special handling für Preisanpassungen
        if (contract.requiresPriceAdjustment()) {
            context.put("priceAdjustment", calculatePriceAdjustment(contract));
            context.put("consumerPriceIndex", getCurrentCPI());
        }
        
        emailService.sendTemplatedEmail(
            contract.getCustomer().getMainContact().getEmail(),
            templateId,
            context
        );
    }
}
```

### Template-Variablen für Renewals

```handlebars
{{!-- renewal_reminder_30_days.hbs --}}
<h2>Ihre FreshPlan-Partnerschaft läuft aus</h2>

<p>Sehr geehrte/r {{contract.contactPerson}},</p>

<p>Ihre FreshPlan-Partnerschaftsvereinbarung für {{contract.customerName}} 
läuft am <strong>{{date contract.endDate}}</strong> aus.</p>

{{#if contract.hasDiscount}}
<div class="alert">
    ⚠️ Ihre aktuellen Rabatte von {{contract.discountPercentage}}% 
    verfallen mit dem Vertragsende!
</div>
{{/if}}

<h3>Ihre Vorteile bei Verlängerung:</h3>
<ul>
    <li>✅ Fortsetzung Ihrer {{contract.discountPercentage}}% Rabatte</li>
    <li>✅ Planungssicherheit für weitere 12 Monate</li>
    <li>✅ Bevorzugter Support</li>
</ul>

{{#if priceAdjustment}}
<div class="price-info">
    <h4>Preisanpassung:</h4>
    <p>Aufgrund der Entwicklung des Verbraucherpreisindex ({{consumerPriceIndex}}%) 
    werden die Listenpreise zum {{date priceAdjustment.effectiveDate}} angepasst.</p>
    
    <table>
        <tr>
            <td>Ihr aktueller Jahreswert:</td>
            <td>{{currency contract.currentValue}}</td>
        </tr>
        <tr>
            <td>Neuer Jahreswert:</td>
            <td>{{currency priceAdjustment.newValue}}</td>
        </tr>
    </table>
</div>
{{/if}}

<div class="cta">
    <a href="{{renewalLink}}" class="button">Jetzt verlängern</a>
</div>
```

### Automatisierte E-Mail-Workflows

```java
@ApplicationScoped
public class RenewalWorkflowOrchestrator {
    
    @Inject RenewalEmailService emailService;
    @Inject ActivityService activityService;
    
    @Scheduled(every = "1d", hour = "9")
    public void processRenewalReminders() {
        // 90-Tage Reminder
        contractRepository.findExpiringIn(90).forEach(contract -> {
            if (!hasRecentReminder(contract, 90)) {
                emailService.sendRenewalReminder(contract, 90);
                logActivity(contract, "90-Tage Renewal Reminder versendet");
            }
        });
        
        // 60-Tage Reminder mit Eskalation
        contractRepository.findExpiringIn(60).forEach(contract -> {
            if (!hasRecentReminder(contract, 60)) {
                emailService.sendRenewalReminder(contract, 60);
                
                // CC an Sales Manager
                emailService.notifySalesManager(contract, 
                    "Renewal läuft in 60 Tagen aus");
            }
        });
        
        // 30-Tage Final Reminder
        contractRepository.findExpiringIn(30).forEach(contract -> {
            if (!hasRecentReminder(contract, 30)) {
                emailService.sendRenewalReminder(contract, 30);
                
                // Opportunity automatisch in RENEWAL Stage
                moveToRenewalStage(contract);
            }
        });
    }
}
```

### E-Mail Tracking Integration

```java
@ApplicationScoped
public class RenewalEmailTracker {
    
    @Inject EmailTrackingService trackingService;
    
    public void trackRenewalEngagement(Contract contract) {
        List<EmailMetrics> renewalEmails = trackingService
            .getEmailsByTemplateCategory("RENEWAL", contract.getCustomerId());
            
        RenewalEngagement engagement = RenewalEngagement.builder()
            .contractId(contract.getId())
            .emailsSent(renewalEmails.size())
            .emailsOpened(countOpened(renewalEmails))
            .linksClicked(countClicked(renewalEmails))
            .lastEngagement(getLastEngagement(renewalEmails))
            .engagementScore(calculateScore(renewalEmails))
            .build();
            
        // Update Contract mit Engagement-Daten
        contract.setRenewalEngagement(engagement);
        
        // Trigger Follow-up wenn niedrige Engagement
        if (engagement.getEngagementScore() < 0.3) {
            triggerPersonalFollowUp(contract);
        }
    }
}
```

### Template A/B Testing für Renewals

```java
@ApplicationScoped
public class RenewalTemplateOptimizer {
    
    @Inject ABTestService abTestService;
    
    public void optimizeRenewalTemplates() {
        // Test verschiedene Betreffzeilen
        ABTest subjectTest = ABTest.builder()
            .name("Renewal 30 Days Subject Line")
            .variantA("Ihre FreshPlan-Partnerschaft läuft aus")
            .variantB("Sichern Sie sich Ihre Rabatte für weitere 12 Monate")
            .metric(ABTestMetric.OPEN_RATE)
            .build();
            
        // Test verschiedene CTAs
        ABTest ctaTest = ABTest.builder()
            .name("Renewal CTA Button")
            .variantA("Jetzt verlängern")
            .variantB("Rabatte sichern")
            .metric(ABTestMetric.CLICK_RATE)
            .build();
            
        abTestService.run(subjectTest);
        abTestService.run(ctaTest);
    }
}
```