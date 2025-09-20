# FC-003: Template System - Detailspezifikation

## Template Engine Architektur

### 1. Template-Datenmodell

```java
@Entity
public class EmailTemplate {
    @Id UUID id;
    String name;
    String category; // RENEWAL, OPPORTUNITY, WELCOME, CUSTOM
    String subject;
    
    @Column(columnDefinition = "TEXT")
    String bodyHtml;
    
    @Column(columnDefinition = "TEXT")
    String bodyPlain;
    
    @ElementCollection
    Set<String> requiredVariables;
    
    @ElementCollection
    Set<String> conditionalBlocks;
    
    boolean isActive;
    UUID createdBy;
    Instant createdAt;
}

@Entity
public class EmailTemplateVersion {
    @Id UUID id;
    UUID templateId;
    Integer version;
    String changes;
    UUID modifiedBy;
    Instant modifiedAt;
}
```

### 2. Template Engine Implementation

```java
@ApplicationScoped
public class TemplateEngine {
    
    private final Handlebars handlebars;
    
    @Inject
    public TemplateEngine() {
        this.handlebars = new Handlebars()
            .registerHelpers(CustomHelpers.class)
            .with(EscapingStrategy.HTML);
    }
    
    public RenderedEmail render(EmailTemplate template, 
                                TemplateContext context) {
        validateRequiredVariables(template, context);
        
        Template subjectTemplate = handlebars.compileInline(template.subject);
        Template bodyTemplate = handlebars.compileInline(template.bodyHtml);
        
        return RenderedEmail.builder()
            .subject(subjectTemplate.apply(context))
            .bodyHtml(bodyTemplate.apply(context))
            .bodyPlain(renderPlainText(template.bodyPlain, context))
            .build();
    }
}
```

### 3. Template-Variablen und Helpers

```java
public class CustomHelpers {
    
    // Formatierung für Währungen
    public CharSequence currency(Double value, Options options) {
        String currency = options.param(0, "EUR");
        return NumberFormat.getCurrencyInstance(Locale.GERMANY)
            .format(value);
    }
    
    // Datumsformatierung
    public CharSequence date(Instant instant, Options options) {
        String pattern = options.param(0, "dd.MM.yyyy");
        return DateTimeFormatter.ofPattern(pattern)
            .withZone(ZoneId.of("Europe/Berlin"))
            .format(instant);
    }
    
    // Bedingte Blöcke
    public CharSequence ifRenewal(Options options) throws IOException {
        TemplateContext ctx = (TemplateContext) options.context;
        if (ctx.isRenewalContext()) {
            return options.fn();
        }
        return options.inverse();
    }
}
```

### 4. Template-Kategorien und Vorlagen

#### Renewal Templates (FC-009 Integration)
```handlebars
<!-- renewal_reminder_30_days.hbs -->
<h2>Vertragsverlängerung steht bevor</h2>
<p>Sehr geehrte/r {{customer.contactPerson}},</p>

<p>Ihr Vertrag für <strong>{{contract.productName}}</strong> läuft am 
{{date contract.endDate "dd.MM.yyyy"}} aus.</p>

{{#if hasDiscount}}
<div class="highlight">
    🎉 Bei Verlängerung bis zum {{date renewalDeadline}} erhalten Sie 
    {{discount.percentage}}% Rabatt!
</div>
{{/if}}

<table class="contract-details">
    <tr>
        <td>Aktueller Vertragswert:</td>
        <td>{{currency contract.value}}</td>
    </tr>
    <tr>
        <td>Neue Konditionen:</td>
        <td>{{currency renewal.newValue}}</td>
    </tr>
</table>
```

#### Opportunity Templates (M4 Integration)
```handlebars
<!-- opportunity_follow_up.hbs -->
<p>Sehr geehrte/r {{contact.name}},</p>

<p>vielen Dank für unser Gespräch am {{date lastActivity.date}}.</p>

{{#each discussedProducts}}
<h3>{{this.name}}</h3>
<ul>
    <li>Preis: {{currency this.price}}/Monat</li>
    <li>Mindestlaufzeit: {{this.minimumTerm}} Monate</li>
    {{#if this.specialOffer}}
    <li class="special">🔥 {{this.specialOffer}}</li>
    {{/if}}
</ul>
{{/each}}

<p>Nächste Schritte: {{opportunity.nextSteps}}</p>
```

### 5. Template-Builder UI Components

```typescript
// Template Editor Component
export const TemplateEditor: React.FC = () => {
    const [variables, setVariables] = useState<TemplateVariable[]>([]);
    const [preview, setPreview] = useState<RenderedEmail>();
    
    return (
        <Grid container spacing={3}>
            <Grid item xs={6}>
                <VariablePalette 
                    category={selectedCategory}
                    onInsert={handleVariableInsert}
                />
                <CodeEditor
                    language="handlebars"
                    value={template}
                    onChange={handleTemplateChange}
                />
            </Grid>
            <Grid item xs={6}>
                <PreviewPane 
                    preview={preview}
                    testData={sampleData}
                />
            </Grid>
        </Grid>
    );
};
```

### 6. Template-Personalisierung

```java
@ApplicationScoped
public class PersonalizationService {
    
    public TemplateContext buildContext(Customer customer, 
                                        Opportunity opportunity,
                                        Contract contract) {
        return TemplateContext.builder()
            // Customer Context
            .customer(CustomerContext.builder()
                .name(customer.getName())
                .contactPerson(customer.getMainContact())
                .industry(customer.getIndustry())
                .customerSince(customer.getCreatedAt())
                .build())
            
            // Opportunity Context (wenn vorhanden)
            .opportunity(opportunity != null ? 
                OpportunityContext.from(opportunity) : null)
            
            // Contract/Renewal Context
            .contract(contract != null ? 
                ContractContext.from(contract) : null)
            
            // Activity Context (FC-013 Integration)
            .recentActivities(activityService
                .getRecentActivities(customer.getId(), 5))
            
            // Computed Fields
            .totalRevenue(calculateTotalRevenue(customer))
            .isVipCustomer(customer.getSegment() == CustomerSegment.VIP)
            .build();
    }
}
```

### 7. A/B Testing für Templates

```java
@Entity
public class EmailTemplateABTest {
    @Id UUID id;
    UUID templateAId;
    UUID templateBId;
    
    Integer splitPercentage; // 0-100 für Template A
    
    // Metrics
    Integer sentCountA;
    Integer sentCountB;
    Integer openCountA;
    Integer openCountB;
    Integer clickCountA;
    Integer clickCountB;
    
    @Enumerated(EnumType.STRING)
    ABTestStatus status; // DRAFT, RUNNING, COMPLETED
}

@ApplicationScoped
public class ABTestService {
    
    public EmailTemplate selectTemplate(UUID customerId, String category) {
        Optional<EmailTemplateABTest> activeTest = 
            findActiveTest(category);
            
        if (activeTest.isPresent()) {
            return selectVariant(activeTest.get(), customerId);
        }
        
        return getDefaultTemplate(category);
    }
}
```

### 8. Template-Performance Monitoring

```java
@ApplicationScoped
public class TemplateMetricsService {
    
    @Inject MeterRegistry meterRegistry;
    
    public void recordTemplateUsage(UUID templateId, 
                                    TemplateMetrics metrics) {
        meterRegistry.counter("email.template.sent", 
            "template", templateId.toString()).increment();
            
        if (metrics.wasOpened()) {
            meterRegistry.counter("email.template.opened",
                "template", templateId.toString()).increment();
        }
        
        if (metrics.wasClicked()) {
            meterRegistry.counter("email.template.clicked",
                "template", templateId.toString()).increment();
        }
    }
}
```