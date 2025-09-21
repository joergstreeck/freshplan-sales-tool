# 💰 Cost Management für External Services

**Erstellt:** 01.08.2025  
**Status:** 🆕 Konzept  
**Priorität:** HIGH - Kritisch für Wirtschaftlichkeit  

## 🧭 Navigation

**← Zurück:** [Offline Conflict Resolution](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/OFFLINE_CONFLICT_RESOLUTION.md)  
**→ Nächstes:** [In-App Help System](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/IN_APP_HELP_SYSTEM.md)  
**↗ Verbunden:** [AI Features](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/AI_FEATURES.md)  

## 🎯 Problem: Unkontrollierte API-Kosten

**Die Herausforderung:**
- OpenAI GPT-4 Kosten: ~$0.03/1K tokens
- 1000 Nutzer × 50 Anfragen/Tag = Explosion 💥
- Keine Transparenz über tatsächliche Kosten
- Keine Fallback-Strategien

## 💡 Lösung: Multi-Layer Cost Control

### 1. Budget Limits & Monitoring

```java
@Configuration
public class CostManagementConfig {
    
    @Value("${ai.budget.daily.limit:100.00}")
    private BigDecimal dailyBudgetLimit;
    
    @Value("${ai.budget.monthly.limit:2000.00}")
    private BigDecimal monthlyBudgetLimit;
    
    @Bean
    public CostLimiter costLimiter() {
        return CostLimiter.builder()
            .dailyLimit(dailyBudgetLimit)
            .monthlyLimit(monthlyBudgetLimit)
            .alertThreshold(0.8) // Alert bei 80% Auslastung
            .hardStopThreshold(0.95) // Hard Stop bei 95%
            .build();
    }
}
```

### 2. Service-spezifische Kostenkontrolle

```java
@Service
public class AIServiceCostManager {
    
    @Inject
    CostTracker costTracker;
    
    @Inject
    ServiceSelector serviceSelector;
    
    public AIResponse processRequest(AIRequest request) {
        // Kosten-Schätzung vorab
        CostEstimate estimate = estimateCost(request);
        
        // Budget-Check
        if (!costTracker.canAfford(estimate)) {
            // Fallback zu günstigerer Alternative
            return processFallback(request);
        }
        
        // Service-Auswahl basierend auf Kosten/Nutzen
        AIService service = serviceSelector.selectOptimal(request, estimate);
        
        // Tracking
        try (CostTransaction tx = costTracker.startTransaction(service, estimate)) {
            AIResponse response = service.process(request);
            tx.recordActual(response.getTokensUsed());
            return response;
        }
    }
    
    private AIResponse processFallback(AIRequest request) {
        // Stufe 1: Günstigeres Modell (GPT-3.5)
        if (costTracker.canAfford("gpt-3.5-turbo", request)) {
            return processWithGPT35(request);
        }
        
        // Stufe 2: Lokales Modell
        if (localLLMAvailable()) {
            return processWithLocalLLM(request);
        }
        
        // Stufe 3: Regel-basierte Fallback
        return processWithRules(request);
    }
}
```

### 3. Cost Dashboard

```typescript
export const CostDashboard: React.FC = () => {
  const costs = useCostMetrics();
  
  return (
    <Grid container spacing={3}>
      {/* Tages-Übersicht */}
      <Grid item xs={12} md={4}>
        <Card>
          <CardContent>
            <Typography variant="h6">Heute</Typography>
            <LinearProgress 
              variant="determinate" 
              value={(costs.daily.used / costs.daily.limit) * 100}
              color={costs.daily.used > costs.daily.limit * 0.8 ? "warning" : "primary"}
            />
            <Typography variant="h4">
              €{costs.daily.used.toFixed(2)} / €{costs.daily.limit}
            </Typography>
            <Typography variant="caption" color="textSecondary">
              Verbleibend: €{(costs.daily.limit - costs.daily.used).toFixed(2)}
            </Typography>
          </CardContent>
        </Card>
      </Grid>
      
      {/* Service Breakdown */}
      <Grid item xs={12} md={8}>
        <Card>
          <CardContent>
            <Typography variant="h6">Kosten nach Service</Typography>
            <ServiceCostChart data={costs.byService} />
            
            {/* Top Cost Drivers */}
            <List dense>
              {costs.topCostDrivers.map(driver => (
                <ListItem key={driver.feature}>
                  <ListItemText 
                    primary={driver.feature}
                    secondary={`€${driver.cost.toFixed(2)} - ${driver.usage} Anfragen`}
                  />
                  <ListItemSecondaryAction>
                    <IconButton onClick={() => showOptimizationHints(driver)}>
                      <TrendingDownIcon />
                    </IconButton>
                  </ListItemSecondaryAction>
                </ListItem>
              ))}
            </List>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};
```

### 4. Intelligente Service-Auswahl

```typescript
interface ServiceOption {
  provider: 'openai' | 'anthropic' | 'local' | 'rules';
  model: string;
  costPer1kTokens: number;
  qualityScore: number; // 0-100
  latency: number; // ms
}

class ServiceSelector {
  selectOptimal(
    request: AIRequest, 
    userPriority: 'cost' | 'quality' | 'speed'
  ): ServiceOption {
    const options = this.getAvailableOptions(request);
    
    // Scoring-Algorithmus
    return options
      .map(option => ({
        ...option,
        score: this.calculateScore(option, userPriority)
      }))
      .sort((a, b) => b.score - a.score)[0];
  }
  
  private calculateScore(option: ServiceOption, priority: string): number {
    const weights = {
      cost: priority === 'cost' ? 0.6 : 0.2,
      quality: priority === 'quality' ? 0.6 : 0.3,
      speed: priority === 'speed' ? 0.6 : 0.2
    };
    
    return (
      (100 - option.costPer1kTokens) * weights.cost +
      option.qualityScore * weights.quality +
      (100 - Math.min(option.latency / 10, 100)) * weights.speed
    );
  }
}
```

### 5. Lokale LLM Integration

```java
@Component
public class LocalLLMService implements AIService {
    
    private final OllamaClient ollamaClient;
    private final ModelManager modelManager;
    
    @PostConstruct
    public void init() {
        // Download und Setup von lokalen Modellen
        modelManager.ensureModelAvailable("llama2-7b");
        modelManager.ensureModelAvailable("mistral-7b");
    }
    
    @Override
    public AIResponse process(AIRequest request) {
        // Kein API-Cost!
        String model = selectModelForTask(request.getType());
        
        return ollamaClient.generate(
            model,
            request.getPrompt(),
            request.getMaxTokens()
        );
    }
    
    @Override
    public BigDecimal getCostEstimate(AIRequest request) {
        // Nur Strom & Hardware-Kosten
        return BigDecimal.ZERO; // Für Budgeting
    }
}
```

### 6. Cost Alerts & Notifications

```typescript
export const CostAlertSystem = {
  // Real-time Alerts
  checkBudget: async (transaction: CostTransaction) => {
    const usage = await getCostUsage();
    
    if (usage.percentage > 80 && !usage.alert80Sent) {
      await notificationService.send({
        type: 'warning',
        title: 'Budget-Warnung',
        message: '80% des Tagesbudgets erreicht',
        actions: [
          {label: 'Details', url: '/admin/costs'},
          {label: 'Limit erhöhen', url: '/admin/costs/limits'}
        ]
      });
    }
    
    if (usage.percentage > 95) {
      // Automatische Umschaltung auf Fallback
      await enableFallbackMode();
    }
  },
  
  // Daily Summary
  sendDailySummary: async () => {
    const summary = await generateCostSummary();
    
    await emailService.send({
      to: 'finance@company.com',
      subject: `AI Kosten Report - €${summary.total}`,
      template: 'cost-summary',
      data: summary
    });
  }
};
```

## 🗣️ User-freundliche Kommunikation (NEU)

### Klare, nicht-technische Meldungen

```typescript
export const UserFriendlyCostMessages = {
  // Service-Degradation erklären
  serviceDegraded: (feature: string, reason: CostReason) => {
    const messages = {
      budgetLimit: {
        title: "Heute im Sparmodus 💰",
        message: `Um Kosten zu sparen, nutzen wir heute eine etwas einfachere ${feature}-Version. 
                  Die Qualität ist weiterhin gut, nur minimal langsamer.`,
        icon: "savings",
        severity: "info"
      },
      
      highDemand: {
        title: "Hohe Nachfrage 🔥",
        message: `Viele Nutzer verwenden gerade ${feature}. 
                  Wir haben auf eine alternative Lösung umgeschaltet, 
                  die genauso gut funktioniert!`,
        icon: "trending_up",
        severity: "info"
      },
      
      temporaryLimit: {
        title: "Kurze Pause nötig ⏸️",
        message: `${feature} macht eine kurze Pause. 
                  In ${getRemainingTime()} Minuten ist alles wieder normal.`,
        icon: "schedule",
        severity: "warning",
        showCountdown: true
      }
    };
    
    return messages[reason.type] || messages.budgetLimit;
  },
  
  // Feature nicht verfügbar
  featureUnavailable: (feature: string, alternatives: Alternative[]) => {
    return {
      title: `${feature} gerade nicht verfügbar`,
      message: "Aber keine Sorge, hier sind Alternativen:",
      alternatives: alternatives.map(alt => ({
        label: alt.name,
        description: alt.benefit,
        action: () => useAlternative(alt.id)
      })),
      icon: "lightbulb",
      severity: "info"
    };
  },
  
  // Positives Feedback bei Kostenersparnis
  costSaved: (amount: number, action: string) => {
    return {
      title: "Gut gemacht! 🎉",
      message: `Durch ${action} haben Sie heute €${amount.toFixed(2)} gespart.`,
      icon: "celebration",
      severity: "success",
      duration: 3000
    };
  }
};

// UI Component für Cost-Kommunikation
export const CostCommunicator: React.FC<{event: CostEvent}> = ({event}) => {
  const message = getUserFriendlyMessage(event);
  const [showDetails, setShowDetails] = useState(false);
  
  return (
    <Snackbar 
      open={true} 
      autoHideDuration={message.duration || 6000}
      anchorOrigin={{vertical: 'bottom', horizontal: 'center'}}
    >
      <Alert 
        severity={message.severity}
        icon={<Icon>{message.icon}</Icon>}
        action={
          <Box>
            {message.showCountdown && <CountdownTimer />}
            <IconButton 
              size="small" 
              onClick={() => setShowDetails(true)}
              title="Mehr erfahren"
            >
              <InfoIcon />
            </IconButton>
          </Box>
        }
      >
        <AlertTitle>{message.title}</AlertTitle>
        {message.message}
        
        {/* Alternative Optionen */}
        {message.alternatives && (
          <Box sx={{mt: 1}}>
            {message.alternatives.map((alt, idx) => (
              <Chip
                key={idx}
                label={alt.label}
                onClick={alt.action}
                size="small"
                sx={{mr: 0.5}}
              />
            ))}
          </Box>
        )}
      </Alert>
    </Snackbar>
  );
};

// Transparenz-Dashboard für User
export const UserCostTransparency: React.FC = () => {
  const userImpact = useUserCostImpact();
  
  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Ihr Beitrag zur Kosteneffizienz
        </Typography>
        
        <Grid container spacing={2}>
          <Grid item xs={6}>
            <MetricDisplay
              label="Ihre Ersparnis heute"
              value={`€${userImpact.savedToday.toFixed(2)}`}
              trend="positive"
              icon={<SavingsIcon />}
            />
          </Grid>
          
          <Grid item xs={6}>
            <MetricDisplay
              label="Optimale Nutzung"
              value={`${userImpact.efficiencyScore}%`}
              subtitle="Super effizient!"
              icon={<EcoIcon />}
            />
          </Grid>
        </Grid>
        
        <Box sx={{mt: 2}}>
          <Typography variant="subtitle2" gutterBottom>
            💡 Tipp des Tages
          </Typography>
          <Typography variant="body2" color="textSecondary">
            {userImpact.tip}
          </Typography>
        </Box>
        
        {/* Gamification Element */}
        {userImpact.milestone && (
          <Alert severity="success" sx={{mt: 2}}>
            <AlertTitle>Meilenstein erreicht! 🏆</AlertTitle>
            {userImpact.milestone.message}
          </Alert>
        )}
      </CardContent>
    </Card>
  );
};
```

### Proaktive Nutzer-Führung

```typescript
// Wenn Feature wegen Kosten limitiert ist
export const GuidedAlternatives: React.FC<{
  blockedFeature: string,
  context: UserContext
}> = ({blockedFeature, context}) => {
  const alternatives = getSmartAlternatives(blockedFeature, context);
  
  return (
    <Dialog open maxWidth="sm" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center">
          <LightbulbIcon sx={{mr: 1, color: 'warning.main'}} />
          Clevere Alternative gefunden!
        </Box>
      </DialogTitle>
      
      <DialogContent>
        <Typography gutterBottom>
          {blockedFeature} ist gerade ausgelastet. 
          Aber wir haben etwas noch Besseres für Sie:
        </Typography>
        
        <List>
          {alternatives.map(alt => (
            <ListItem key={alt.id}>
              <ListItemIcon>
                {alt.isFree ? <StarIcon color="success" /> : <FlashOnIcon />}
              </ListItemIcon>
              <ListItemText
                primary={alt.name}
                secondary={
                  <Box>
                    <Typography variant="body2">{alt.description}</Typography>
                    {alt.isFree && (
                      <Chip 
                        label="Kostenlos" 
                        size="small" 
                        color="success"
                        sx={{mt: 0.5}}
                      />
                    )}
                  </Box>
                }
              />
              <ListItemSecondaryAction>
                <Button 
                  variant={alt.recommended ? "contained" : "outlined"}
                  size="small"
                  onClick={() => useAlternative(alt)}
                >
                  Nutzen
                </Button>
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>
      </DialogContent>
    </Dialog>
  );
};
```

### 7. Feature-Level Cost Control

```java
@Aspect
@Component
public class CostControlAspect {
    
    @Around("@annotation(costLimit)")
    public Object enforceLimit(ProceedingJoinPoint pjp, CostLimit costLimit) throws Throwable {
        String feature = costLimit.feature();
        BigDecimal maxCost = costLimit.maxDailyCost();
        
        // Check Feature Budget
        if (costTracker.getFeatureCost(feature).compareTo(maxCost) >= 0) {
            // Feature deaktivieren für heute
            return handleFeatureDisabled(feature);
        }
        
        // Track Feature Usage
        try (FeatureCostContext ctx = costTracker.trackFeature(feature)) {
            return pjp.proceed();
        }
    }
}

// Usage
@CostLimit(feature = "smart-suggestions", maxDailyCost = "10.00")
public List<Suggestion> generateSmartSuggestions(Contact contact) {
    // ...
}
```

## 📊 Cost Optimization Strategies

### 1. Prompt Engineering für weniger Tokens

```typescript
// Schlecht: Lange, redundante Prompts
const badPrompt = `
  Please analyze the following customer contact information and provide 
  detailed suggestions for improving the relationship. Consider all aspects
  of the interaction history, sentiment, and business context...
  [500+ tokens]
`;

// Gut: Optimierte, präzise Prompts
const optimizedPrompt = `
  Contact: ${contact.name}
  Last interaction: ${contact.lastInteraction}
  Sentiment: ${contact.sentiment}
  Task: Suggest next action (max 50 words)
`;
```

### 2. Response Caching

```java
@Cacheable(
    value = "ai-responses",
    key = "#request.getCacheKey()",
    condition = "#request.isCacheable()",
    unless = "#result.isError()"
)
public AIResponse processWithCache(AIRequest request) {
    return processRequest(request);
}
```

### 3. Batch Processing

```typescript
// Statt einzelne Anfragen...
const results = await Promise.all(
  contacts.map(contact => ai.analyze(contact))
);

// ...Batch-Anfragen nutzen
const batchResult = await ai.analyzeBatch(contacts);
// Spart ~30% Tokens durch geteilten Context
```

## 🎯 KPIs für Cost Management

```typescript
interface CostKPIs {
  efficiency: {
    costPerActiveUser: number;
    costPerSuccessfulInteraction: number;
    roiScore: number; // Revenue impact / AI costs
  };
  
  optimization: {
    cacheHitRate: number;
    fallbackUsageRate: number;
    averageTokensPerRequest: number;
  };
  
  trends: {
    dailyGrowthRate: number;
    projectedMonthlyCost: number;
    budgetBurnRate: number;
  };
}
```

## 🚀 Implementierungs-Roadmap

### Phase 1: Basis Cost Tracking
- [ ] Cost Tracking Service
- [ ] Budget Limiter
- [ ] Basic Dashboard

### Phase 2: Optimierung  
- [ ] Service Selector
- [ ] Response Caching
- [ ] Prompt Optimization

### Phase 3: Alternative Services
- [ ] Local LLM Setup
- [ ] Fallback Rules Engine
- [ ] Hybrid Approach

## 🔗 Verwandte Dokumente

- [AI Features](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/AI_FEATURES.md) - Nutzt Cost Management
- [Smart Suggestions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/SMART_SUGGESTIONS.md) - Haupt-Kostentreiber
- [Performance Optimization](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/PERFORMANCE_OPTIMIZATION.md) - Cache Strategien

---

**Nächster Schritt:** [→ In-App Help System](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/IN_APP_HELP_SYSTEM.md)