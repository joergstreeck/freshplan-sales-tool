# FC-034 CLAUDE_TECH: Instant Insights - KI Sales Intelligence

**CLAUDE TECH** | **Original:** 870 Zeilen → **Optimiert:** 420 Zeilen (52% Reduktion!)  
**Feature-Typ:** 🔀 FULLSTACK | **Priorität:** MITTEL | **Geschätzter Aufwand:** 4 Tage

## ⚡ QUICK-LOAD (30 Sekunden bis produktiv!)

**KI-gestützte Sofort-Einblicke für intelligente Verkaufsentscheidungen direkt im Kontext**

### 🎯 Das macht es:
- **OpenAI GPT-4 Integration**: Automatische Verkaufschancen-Analyse aus Kundendaten
- **Real-time Insights**: Live-Einblicke bei Kundenaktivitäten + proaktive Background-Analyse
- **Context-Aware Display**: Intelligente Insights-Badges in Customer Cards + Opportunity Views
- **Actionable Recommendations**: Konkrete Handlungsempfehlungen mit Confidence-Score

### 🚀 ROI:
- **30% schnellere Entscheidungen** durch KI-Insights direkt im Workflow
- **20% mehr Opportunities** durch proaktive Verkaufschancen-Erkennung
- **40% Zeitersparnis** bei manueller Datenanalyse - KI macht das automatisch
- **Break-even nach 3 Wochen** durch bessere Verkaufsqualität

### 🏗️ Core Components:
```
OpenAI Service → GPT-4 API → Insight Generator → Cache Layer → Real-time UI
Background Scheduler → Proactive Analysis → WebSocket Updates → Toast Notifications
InsightsBadge → Contextual Display → InsightsPanel → Actionable Buttons
```

---

## 📋 COPY-PASTE READY RECIPES

### 🔧 Backend Starter Kit

#### 1. OpenAI Service (Core Engine):
```java
@ApplicationScoped
public class OpenAIService {
    
    @ConfigProperty(name = "openai.api.key")
    String openaiApiKey;
    
    @ConfigProperty(name = "openai.model", defaultValue = "gpt-4-turbo")
    String model;
    
    private final OkHttpClient client = new OkHttpClient();
    
    public CompletionStage<String> generateInsight(InsightRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JSONObject requestBody = new JSONObject()
                    .put("model", model)
                    .put("messages", buildMessages(request))
                    .put("max_tokens", 200)
                    .put("temperature", 0.3);
                
                Request httpRequest = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + openaiApiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(
                        requestBody.toString(),
                        MediaType.get("application/json")
                    ))
                    .build();
                
                try (Response response = client.newCall(httpRequest).execute()) {
                    if (!response.isSuccessful()) {
                        throw new InsightGenerationException("OpenAI API error: " + response.code());
                    }
                    
                    JSONObject responseBody = new JSONObject(response.body().string());
                    return responseBody
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
                }
            } catch (Exception e) {
                Log.error("Failed to generate insight", e);
                throw new InsightGenerationException("Failed to generate insight", e);
            }
        });
    }
    
    private JSONArray buildMessages(InsightRequest request) {
        return new JSONArray()
            .put(new JSONObject()
                .put("role", "system")
                .put("content", buildSystemPrompt(request.getContext()))
            )
            .put(new JSONObject()
                .put("role", "user")
                .put("content", request.getPrompt())
            );
    }
    
    private String buildSystemPrompt(InsightContext context) {
        return String.format("""
            Du bist ein erfahrener Sales-Analyst für ein B2B-Catering-Unternehmen.
            Analysiere die gegebenen Daten und gib konkrete, actionable Insights.
            
            Kontext: %s
            Fokus: Verkaufschancen, Risiken, und nächste Schritte
            Format: Kurz, präzise, deutsch
            Stil: Professionell, aber verständlich
            """, context.toString());
    }
}
```

#### 2. Insights Service (Business Logic):
```java
@ApplicationScoped
public class InsightsService {
    
    @Inject
    OpenAIService openaiService;
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    OpportunityRepository opportunityRepository;
    
    public CompletionStage<List<Insight>> generateCustomerInsights(UUID customerId) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Collect customer context
            Customer customer = customerRepository.findById(customerId);
            List<Opportunity> opportunities = opportunityRepository.findByCustomerId(customerId);
            CustomerMetrics metrics = calculateCustomerMetrics(customerId);
            
            // 2. Generate specialized insights
            List<CompletableFuture<Insight>> insightFutures = List.of(
                generateSalesOpportunityInsight(customer, opportunities, metrics),
                generateRiskAssessmentInsight(customer, opportunities, metrics),
                generateNextStepsInsight(customer, opportunities, metrics)
            );
            
            return insightFutures.stream()
                .map(CompletableFuture::join)
                .filter(insight -> insight.getConfidence() > 0.6)
                .collect(Collectors.toList());
        });
    }
    
    private CompletableFuture<Insight> generateSalesOpportunityInsight(
            Customer customer, List<Opportunity> opportunities, CustomerMetrics metrics) {
        
        String prompt = String.format("""
            Analysiere diese B2B-Catering-Kundendaten auf Verkaufschancen:
            - Kunde: %s (Branche: %s)
            - Letzte Aufträge: %s
            - Aktuelle Opportunities: %d
            - Durchschnittliches Auftragsvolumen: €%.2f
            
            Finde konkrete Upselling/Cross-Selling Möglichkeiten.
            Antwort in 1-2 Sätzen mit konkreter Handlungsempfehlung.
            """, 
            customer.getCompanyName(),
            customer.getIndustry(),
            getRecentOrdersSummary(customer.getId()),
            opportunities.size(),
            metrics.getAverageOrderValue()
        );
        
        return openaiService.generateInsight(
                InsightRequest.builder()
                    .context(InsightContext.CUSTOMER)
                    .entityId(customer.getId())
                    .prompt(prompt)
                    .build()
            )
            .thenApply(content -> Insight.builder()
                .type(InsightType.SALES_OPPORTUNITY)
                .title("Verkaufschance")
                .content(content)
                .confidence(0.8f)
                .actionable(true)
                .metadata(InsightMetadata.builder()
                    .generatedAt(LocalDateTime.now())
                    .source("AI")
                    .context(InsightContext.CUSTOMER)
                    .entityId(customer.getId())
                    .build())
                .build()
            )
            .toCompletableFuture();
    }
}
```

#### 3. Background Scheduler (Proactive Intelligence):
```java
@ApplicationScoped
public class InsightsScheduler {
    
    @Inject
    InsightsService insightsService;
    
    @Inject
    @Channel("insights-updates")
    Emitter<InsightUpdate> insightEmitter;
    
    // Proactive insights every 15 minutes
    @Scheduled(every = "15m")
    public void generateProactiveInsights() {
        Log.info("Starting proactive insights generation");
        
        // Find customers with recent activity
        List<UUID> activeCustomers = findRecentlyActiveCustomers();
        
        activeCustomers.stream()
            .limit(50) // Prevent API overload
            .forEach(customerId -> {
                insightsService.generateCustomerInsights(customerId)
                    .thenAccept(insights -> {
                        insights.stream()
                            .filter(insight -> insight.getConfidence() > 0.7)
                            .forEach(insight -> {
                                cacheInsight(insight);
                                
                                // Emit real-time update
                                insightEmitter.send(InsightUpdate.builder()
                                    .customerId(customerId)
                                    .insight(insight)
                                    .timestamp(LocalDateTime.now())
                                    .build());
                            });
                    });
            });
    }
    
    // React to customer updates
    public void onCustomerUpdated(@Observes CustomerUpdatedEvent event) {
        Log.info("Customer updated, generating fresh insights: " + event.getCustomerId());
        
        insightsService.generateCustomerInsights(event.getCustomerId())
            .thenAccept(insights -> {
                insights.forEach(this::cacheInsight);
                
                insightEmitter.send(InsightUpdate.builder()
                    .customerId(event.getCustomerId())
                    .insights(insights)
                    .trigger("CUSTOMER_UPDATE")
                    .timestamp(LocalDateTime.now())
                    .build());
            });
    }
}
```

### 🎨 Frontend Starter Kit

#### 4. Context-Aware Insights Hook:
```typescript
export const useContextualInsights = () => {
  const location = useLocation();
  const { customerId, opportunityId } = useParams();
  const { track } = useAnalytics();
  
  const context = useMemo(() => {
    if (location.pathname.includes('/customers/') && customerId) {
      return { type: 'CUSTOMER', entityId: customerId };
    }
    if (location.pathname.includes('/opportunities/') && opportunityId) {
      return { type: 'OPPORTUNITY', entityId: opportunityId };
    }
    if (location.pathname.includes('/cockpit')) {
      return { type: 'DASHBOARD', entityId: 'global' };
    }
    return null;
  }, [location, customerId, opportunityId]);
  
  const { data: insights, isLoading } = useQuery({
    queryKey: ['insights', context?.type, context?.entityId],
    queryFn: () => insightsApi.getInsights(context.type, context.entityId),
    staleTime: 15 * 60 * 1000, // 15 minutes cache
    enabled: !!context?.entityId,
    onSuccess: (data) => {
      track('insights_loaded', {
        context: context?.type,
        entity_id: context?.entityId,
        insights_count: data?.length || 0,
      });
    },
  });
  
  return {
    insights: insights || [],
    isLoading,
    hasInsights: insights && insights.length > 0,
    context
  };
};
```

#### 5. Insights Badge Component:
```typescript
export const InsightsBadge: React.FC<{
  context: InsightContext;
  entityId: string;
  variant?: 'dot' | 'count' | 'icon';
  onClick?: () => void;
}> = ({ context, entityId, variant = 'count', onClick }) => {
  const { insights, isLoading } = useInsights(context, entityId);
  const { track } = useAnalytics();
  
  if (isLoading || !insights?.length) return null;
  
  const highPriorityInsights = insights.filter(insight => 
    insight.confidence > 0.7 && insight.actionable
  );
  
  const handleClick = () => {
    track('insights_badge_clicked', {
      context,
      entity_id: entityId,
      insights_count: highPriorityInsights.length,
    });
    onClick?.();
  };
  
  return (
    <Badge 
      badgeContent={variant === 'count' ? highPriorityInsights.length : ''}
      color="primary"
      variant={variant === 'dot' ? 'dot' : 'standard'}
      onClick={handleClick}
      sx={{ 
        cursor: 'pointer',
        '& .MuiBadge-badge': {
          backgroundColor: '#94C456', // Freshfoodz Grün
          color: 'white',
          fontFamily: 'Poppins',
          fontWeight: 600,
        }
      }}
    >
      <Tooltip title={`${highPriorityInsights.length} neue Insights verfügbar`}>
        <LightbulbIcon 
          color={highPriorityInsights.length > 0 ? 'primary' : 'disabled'}
          sx={{ 
            fontSize: 20,
            color: highPriorityInsights.length > 0 ? '#94C456' : undefined,
          }}
        />
      </Tooltip>
    </Badge>
  );
};
```

#### 6. Insights Panel Component:
```typescript
export const InsightsPanel: React.FC<{
  insights: Insight[];
  onActionClick?: (insight: Insight) => void;
  onDismiss?: () => void;
}> = ({ insights, onActionClick, onDismiss }) => {
  const [expandedInsight, setExpandedInsight] = useState<string | null>(null);
  const { track } = useAnalytics();
  
  const getInsightIcon = (type: InsightType) => {
    switch (type) {
      case 'SALES_OPPORTUNITY': return <TrendingUpIcon color="success" />;
      case 'RISK_ASSESSMENT': return <WarningIcon color="warning" />;
      case 'NEXT_STEPS': return <NavigateNextIcon color="primary" />;
      default: return <InfoIcon />;
    }
  };
  
  const getConfidenceColor = (confidence: number) => {
    if (confidence >= 0.8) return 'success';
    if (confidence >= 0.6) return 'warning'; 
    return 'error';
  };
  
  const handleActionClick = (insight: Insight) => {
    track('insight_action_clicked', {
      insight_type: insight.type,
      insight_id: insight.id,
      confidence: insight.confidence,
    });
    onActionClick?.(insight);
  };
  
  return (
    <Paper 
      elevation={3}
      sx={{ 
        p: 2, 
        maxWidth: 400,
        bgcolor: 'background.paper',
        border: '2px solid #94C456',
        borderRadius: 2,
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
        <LightbulbIcon sx={{ color: '#94C456', mr: 1 }} />
        <Typography variant="h6" fontFamily="Antonio Bold">
          KI-Insights ({insights.length})
        </Typography>
        <IconButton 
          size="small" 
          onClick={onDismiss}
          sx={{ ml: 'auto' }}
        >
          <CloseIcon />
        </IconButton>
      </Box>
      
      <List sx={{ maxHeight: 400, overflow: 'auto' }}>
        {insights.map((insight) => (
          <ListItem 
            key={insight.id}
            sx={{ 
              border: '1px solid #f0f0f0',
              borderRadius: 1,
              mb: 1,
              flexDirection: 'column',
              alignItems: 'stretch'
            }}
          >
            <ListItemButton 
              onClick={() => setExpandedInsight(
                expandedInsight === insight.id ? null : insight.id
              )}
            >
              <ListItemIcon>
                {getInsightIcon(insight.type)}
              </ListItemIcon>
              <ListItemText
                primary={insight.title}
                secondary={expandedInsight === insight.id ? null : 
                  insight.content.substring(0, 60) + '...'
                }
                primaryTypographyProps={{
                  fontFamily: 'Poppins',
                  fontWeight: 600
                }}
              />
              <Chip 
                size="small"
                label={`${Math.round(insight.confidence * 100)}%`}
                color={getConfidenceColor(insight.confidence)}
                sx={{ ml: 1 }}
              />
            </ListItemButton>
            
            <Collapse in={expandedInsight === insight.id}>
              <Box sx={{ p: 2, pt: 0 }}>
                <Typography variant="body2" sx={{ mb: 2 }}>
                  {insight.content}
                </Typography>
                
                {insight.actionable && (
                  <Button
                    variant="contained"
                    size="small"
                    startIcon={<PlayArrowIcon />}
                    onClick={() => handleActionClick(insight)}
                    sx={{ 
                      bgcolor: '#94C456',
                      fontFamily: 'Poppins',
                      fontWeight: 600,
                      '&:hover': { bgcolor: '#7BA846' }
                    }}
                  >
                    Aktion ausführen
                  </Button>
                )}
              </Box>
            </Collapse>
          </ListItem>
        ))}
      </List>
    </Paper>
  );
};
```

#### 7. Real-time Updates Hook:
```typescript
export const useRealtimeInsights = (customerId: string) => {
  const [insights, setInsights] = useState<Insight[]>([]);
  const { track } = useAnalytics();
  
  useEffect(() => {
    if (!customerId) return;
    
    const eventSource = new EventSource(`/api/insights/stream/${customerId}`);
    
    eventSource.onmessage = (event) => {
      const update: InsightUpdate = JSON.parse(event.data);
      
      setInsights(prevInsights => {
        // Merge new insights, avoiding duplicates
        const existingIds = new Set(prevInsights.map(i => i.id));
        const newInsights = update.insights.filter(i => !existingIds.has(i.id));
        
        return [...prevInsights, ...newInsights]
          .sort((a, b) => b.confidence - a.confidence);
      });
      
      // Show notification for high-confidence insights
      update.insights
        .filter(insight => insight.confidence > 0.8 && insight.actionable)
        .forEach(insight => {
          track('insight_notification_shown', {
            insight_type: insight.type,
            confidence: insight.confidence,
            customer_id: customerId,
          });
          
          toast.info(`💡 Neue Verkaufschance: ${insight.title}`, {
            action: {
              label: 'Anzeigen',
              onClick: () => navigateToInsight(insight)
            }
          });
        });
    };
    
    return () => eventSource.close();
  }, [customerId, track]);
  
  return insights;
};
```

---

## 📊 IMPLEMENTIERUNGSPLAN

### Phase 1: OpenAI Foundation (2 Tage)
1. **OpenAI Service**: API Integration + Error Handling + Prompt Engineering
2. **Insights Service**: Business Logic + Context Collection + Caching
3. **Basic Entities**: InsightEntity + InsightRequest + InsightResponse

### Phase 2: Real-time Intelligence (1 Tag)
1. **Background Scheduler**: Proactive Analysis + Customer Event Listeners
2. **WebSocket Updates**: SSE Stream + Real-time Frontend Updates
3. **Notification System**: Toast Messages + Badge Updates

### Phase 3: UI Integration (1 Tag)
1. **Insights Components**: Badge + Panel + Context-aware Display
2. **Customer Card Integration**: Insights Badge in Customer Lists
3. **Action Handlers**: Click-through to Opportunities + Next Steps

---

## 🎯 BUSINESS VALUE

### ROI Metriken:
- **30% schnellere Entscheidungen** durch KI-Insights direkt im Workflow
- **20% mehr Opportunities** durch proaktive Verkaufschancen-Erkennung  
- **40% Zeitersparnis** bei manueller Datenanalyse - KI übernimmt
- **Break-even nach 3 Wochen** durch qualitativ bessere Verkaufsentscheidungen

### UX Benefits:
- **Contextual Intelligence**: Insights erscheinen genau dort wo sie gebraucht werden
- **Proactive Guidance**: System erkennt Verkaufschancen bevor der User sie sieht
- **Confidence Scoring**: User weiß sofort wie vertrauenswürdig ein Insight ist
- **Actionable Recommendations**: Nicht nur Information sondern konkrete Next Steps

---

## 🔗 INTEGRATION POINTS

### Dependencies:
- **FC-026 Analytics Platform**: Datengrundlage für KI-Analyse (Recommended)
- **M3 Sales Cockpit**: Context für Dashboard-Insights (Recommended)

### Enables:
- **FC-031 Smart Templates**: KI-Engine für Template-Generierung
- **FC-027 Magic Moments**: Insight-based Opportunity Detection
- **FC-020 Quick Wins**: Power User Intelligence Features

---

## ⚠️ WICHTIGE ENTSCHEIDUNGEN

1. **OpenAI GPT-4**: State-of-the-art Qualität für B2B-Sales-Kontext
2. **15min Cache TTL**: Balance zwischen Aktualität und API-Kosten
3. **Confidence Threshold 0.6**: Filterung für relevante Insights
4. **Real-time via SSE**: Better UX als Polling, weniger komplex als WebSocket

---

**Status:** Ready for Implementation | **Phase 1:** OpenAI Service + Basic Insights | **Next:** Real-time Intelligence Engine