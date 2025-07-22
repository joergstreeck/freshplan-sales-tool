# 📊 FC-034 INSTANT INSIGHTS - IMPLEMENTATION

[← Zurück zur Übersicht](/docs/features/ACTIVE/01_security/FC-034_OVERVIEW.md)

---

## 🎨 FRONTEND IMPLEMENTATION

### 1. Insight Card Component

```typescript
// components/InsightCard.tsx
import { Card, CardHeader, CardContent, Box, Chip, Accordion, AccordionSummary, AccordionDetails, List, ListItem, ListItemIcon, ListItemText, IconButton, Typography } from '@mui/material';
import { AutoAwesome as AutoAwesomeIcon, Refresh as RefreshIcon, ExpandMore as ExpandMoreIcon, Chat as ChatIcon } from '@mui/icons-material';

export const InsightCard = ({ insight }) => (
  <Card sx={{ mb: 2, border: '1px solid #1976d2' }}>
    <CardHeader
      avatar={<AutoAwesomeIcon color="primary" />}
      title="KI Insights"
      subheader={`Generiert vor ${insight.age} Minuten`}
      action={
        <IconButton onClick={regenerate}>
          <RefreshIcon />
        </IconButton>
      }
    />
    
    <CardContent>
      {/* Key Insights */}
      <Box sx={{ mb: 2 }}>
        {insight.highlights.map((point, i) => (
          <Chip
            key={i}
            label={point}
            size="small"
            sx={{ m: 0.5 }}
            color={point.includes('⚠️') ? 'warning' : 'default'}
          />
        ))}
      </Box>
      
      {/* Talking Points */}
      <Accordion>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Typography>Gesprächsthemen ({insight.talkingPoints.length})</Typography>
        </AccordionSummary>
        <AccordionDetails>
          <List dense>
            {insight.talkingPoints.map((point, i) => (
              <ListItem key={i}>
                <ListItemIcon><ChatIcon fontSize="small" /></ListItemIcon>
                <ListItemText primary={point} />
              </ListItem>
            ))}
          </List>
        </AccordionDetails>
      </Accordion>
    </CardContent>
  </Card>
);
```

### 2. Insight Engine Hook

```typescript
// hooks/useInsightEngine.ts
import { useQuery, useMutation } from '@tanstack/react-query';
import { insightApi } from '../services/insightApi';

export const useInsightEngine = (customerId: string) => {
  // Pre-Meeting Brief generieren
  const generateBrief = async (meetingDate: Date) => {
    const data = await gatherCustomerData(customerId);
    
    return {
      highlights: [
        `🎯 Letzter Kontakt vor ${daysSince(data.lastContact)} Tagen`,
        `📈 Umsatz ${data.trend > 0 ? 'steigend' : 'fallend'} (${data.trend}%)`,
        `⏰ Beste Zeit: ${data.preferredTime}`,
        `💡 Opportunity: ${data.openOpportunity?.name}`
      ],
      
      talkingPoints: [
        'Neue Bio-Linie erwähnen (passt zum Profil)',
        'Nach Expansion-Plänen fragen',
        'Referenz von Hotel Adler einbringen'
      ],
      
      warnings: [
        '⚠️ Rechnung #1234 überfällig (7 Tage)',
        '⚠️ Konkurrent war letzte Woche da'
      ]
    };
  };

  return {
    preMeetingBrief: useQuery({
      queryKey: ['insights', 'pre-meeting', customerId],
      queryFn: () => generateBrief(new Date()),
      staleTime: 5 * 60 * 1000 // 5 minutes
    }),
    
    regenerate: useMutation({
      mutationFn: () => generateBrief(new Date()),
      onSuccess: () => queryClient.invalidateQueries(['insights'])
    })
  };
};
```

### 3. Floating Insight Button

```typescript
// components/InsightButton.tsx
import { Fab, Badge, Drawer } from '@mui/material';
import { AutoAwesome as AutoAwesomeIcon } from '@mui/icons-material';
import { useState } from 'react';

export const InsightButton = ({ customerId }) => {
  const [open, setOpen] = useState(false);
  const insights = useInsights(customerId);
  
  return (
    <>
      <Fab
        color="secondary"
        sx={{ position: 'fixed', bottom: 80, right: 16 }}
        onClick={() => setOpen(true)}
      >
        <Badge badgeContent={insights.new} color="error">
          <AutoAwesomeIcon />
        </Badge>
      </Fab>
      
      <Drawer anchor="bottom" open={open} onClose={() => setOpen(false)}>
        <InsightPanel insights={insights} />
      </Drawer>
    </>
  );
};
```

### 4. Daily Digest Component

```typescript
// components/DailyInsightDigest.tsx
import { Card, CardHeader, CardContent, Timeline, TimelineItem, TimelineSeparator, TimelineDot, TimelineContent, Typography, Button } from '@mui/material';

export const DailyInsightDigest = () => {
  const insights = useDailyInsights();
  
  return (
    <Card>
      <CardHeader
        title="Ihre täglichen Insights"
        subheader={formatDate(new Date())}
      />
      <CardContent>
        <Timeline>
          {insights.map(insight => (
            <TimelineItem key={insight.id}>
              <TimelineSeparator>
                <TimelineDot color={insight.priority}>
                  {insight.icon}
                </TimelineDot>
              </TimelineSeparator>
              <TimelineContent>
                <Typography variant="h6">{insight.title}</Typography>
                <Typography>{insight.description}</Typography>
                <Button size="small" onClick={() => showDetails(insight)}>
                  Details
                </Button>
              </TimelineContent>
            </TimelineItem>
          ))}
        </Timeline>
      </CardContent>
    </Card>
  );
};
```

---

## 🔧 BACKEND IMPLEMENTATION

### Insight Generator Service

```java
// services/InsightGeneratorService.java
@ApplicationScoped
public class InsightGeneratorService {
    
    @Inject
    CustomerService customerService;
    
    @Inject
    OrderAnalysisService orderAnalysis;
    
    @Inject
    NotificationService notificationService;
    
    @Scheduled(every="15m")
    void generateInsights() {
        // 1. Gather fresh data
        List<Customer> activeCustomers = customerService.getActive();
        
        // 2. Run analysis in parallel
        activeCustomers.parallelStream()
            .forEach(customer -> {
                InsightData data = gatherData(customer);
                List<Insight> insights = analyzeData(data);
                insightService.save(insights);
            });
        
        // 3. Push high priority insights
        pushNotifications(getHighPriorityInsights());
    }
    
    private List<Insight> analyzeData(InsightData data) {
        List<Insight> insights = new ArrayList<>();
        
        // Behavior patterns
        if (data.hasUnusualPattern()) {
            insights.add(createPatternInsight(data));
        }
        
        // Risk detection
        if (data.getChurnRisk() > 0.7) {
            insights.add(createChurnAlert(data));
        }
        
        // Opportunities
        if (data.hasUpsellPotential()) {
            insights.add(createUpsellInsight(data));
        }
        
        return insights;
    }
}
```

### Behavior Analyzer

```java
// services/BehaviorAnalyzer.java
@ApplicationScoped
public class BehaviorAnalyzer {
    
    public CustomerBehavior analyze(Customer customer) {
        List<Order> orders = orderRepository.findByCustomer(customer);
        
        return CustomerBehavior.builder()
            .frequency(calculateOrderFrequency(orders))
            .seasonality(detectSeasonalPatterns(orders))
            .preferences(extractProductPreferences(orders))
            .averageOrderValue(calculateAOV(orders))
            .trend(calculateTrend(orders))
            .build();
    }
    
    public CustomerPrediction predict(Customer customer) {
        CustomerBehavior behavior = analyze(customer);
        
        return CustomerPrediction.builder()
            .nextOrderDate(predictNextOrder(behavior))
            .churnRisk(calculateChurnProbability(behavior))
            .upsellPotential(identifyUpsellOpportunities(behavior))
            .lifetimeValue(estimateLTV(behavior))
            .build();
    }
    
    private double calculateChurnProbability(CustomerBehavior behavior) {
        // Simple logistic regression model
        double score = 0.0;
        score += behavior.getDaysSinceLastOrder() * 0.01;
        score -= behavior.getOrderFrequency() * 0.1;
        score += behavior.getTrendDirection() < 0 ? 0.2 : -0.1;
        
        return Math.max(0, Math.min(1, score));
    }
}
```

### Alert Engine

```java
// services/AlertEngine.java
@ApplicationScoped
public class AlertEngine {
    
    @Inject
    Event<InsightNotification> insightEvent;
    
    public void checkTriggers() {
        // Pre-visit alerts
        appointmentService.getUpcoming(30) // next 30 minutes
            .forEach(appointment -> {
                generateAndPushBrief(appointment.getCustomerId());
            });
        
        // Stale opportunities
        opportunityService.getStale(14) // no activity for 14 days
            .forEach(opp -> {
                suggestNextAction(opp);
            });
        
        // At-risk customers
        customerService.getAtRisk()
            .forEach(customer -> {
                generateRetentionStrategy(customer);
            });
    }
    
    private void pushInsight(String userId, Insight insight) {
        InsightNotification notification = InsightNotification.builder()
            .title("💡 Neuer Insight")
            .body(insight.getSummary())
            .userId(userId)
            .customerId(insight.getCustomerId())
            .priority(insight.getPriority())
            .build();
        
        insightEvent.fire(notification);
    }
}
```

[→ Weiter zur API & ML Integration](/docs/features/ACTIVE/01_security/FC-034_API.md)