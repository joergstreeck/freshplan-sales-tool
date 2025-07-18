# FC-007: Chef-Dashboard - Führungs-KPIs ⚡

**Feature Code:** FC-007  
**Feature-Typ:** 🎨 FRONTEND  
**Geschätzter Aufwand:** 7-8 Tage  
**Priorität:** HOCH - Führungskräfte brauchen Transparenz  
**ROI:** 50% schnellere Entscheidungen, +20% Team-Performance  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** "Was tun meine Verkäufer? Wie ist die Pipeline?" = viele Klicks  
**Lösung:** Ein Dashboard mit Live-Updates + KI-Insights  
**Impact:** Alle KPIs auf einen Blick, proaktives Coaching  

---

## 📊 KERN-DASHBOARD

```
┌─────────────────────────────────────────────────────┐
│ Pipeline: 2.450.000€    | Forecast: 735.000€       │
│ Quote: 32% ⬆️+5%        | Team-Score: 8.2/10       │
└─────────────────────────────────────────────────────┘

LIVE NOW: Thomas → Anruf mit Kunde XY (vor 2 Min)
          Sarah → Angebot #1234 erstellt (vor 5 Min)
```

**4 Hauptbereiche:**
1. **KPI Overview** - Die wichtigsten Zahlen
2. **Live Monitor** - Was passiert gerade?
3. **Team Matrix** - Wer performt wie?
4. **AI Insights** - Was sollte ich tun?

---

## 🏃 QUICK-START IMPLEMENTIERUNG

### Analytics Engine
```java
@Cacheable(cacheName = "sales-metrics")
public SalesMetrics calculateMetrics(MetricRequest request) {
    return SalesMetrics.builder()
        .pipelineValue(calculatePipelineValue())
        .forecast(calculateWeightedForecast())
        .conversionRate(calculateConversionRate())
        .teamPerformance(calculateTeamMetrics())
        .build();
}
```

### GraphQL API
```graphql
type Query {
  dashboardOverview(dateRange: DateRange!): DashboardData!
  salesRepPerformance(repId: ID!): RepPerformance!
}

type Subscription {
  liveActivities: ActivityStream!
  alertsStream: Alert!
}
```

### React Dashboard
```typescript
export const ChefDashboard: React.FC = () => {
  const { data: overview } = useDashboardOverview();
  const { data: activities } = useLiveActivities();
  
  return (
    <>
      <KPICards metrics={overview?.keyMetrics} />
      <ActivityMonitor activities={activities} />
      <PipelineFunnel data={overview?.pipeline} />
      <TeamPerformanceMatrix performers={overview?.team} />
      <InsightsPanel insights={overview?.aiInsights} />
    </>
  );
};
```

---

## 🔗 DEPENDENCIES & INTEGRATION

**Datenquellen:**
- M4 Activities (Live-Daten)
- M5 Customer (Pipeline)
- FC-005 Xentral (echte Umsätze)

**Performance:**
- Redis für Real-Time Cache
- GraphQL für effiziente Queries
- WebSocket für Live-Updates

**KI-Integration:**
- Pattern Detection
- Anomalie-Erkennung
- Predictive Forecasting

---

## ⚡ KRITISCHE ENTSCHEIDUNGEN

1. **Update-Frequenz:** Real-Time vs. 5-Min Cache?
2. **KI-Provider:** OpenAI vs. eigenes ML-Model?
3. **Mobile-First:** Responsive vs. dedizierte App?

---

## 📊 SUCCESS METRICS

- **Load Time:** <2s für Dashboard
- **Adoption:** >5x täglich pro Manager
- **Forecast Accuracy:** ±10% Abweichung
- **Decision Speed:** 50% schneller

---

## 🚀 NÄCHSTER SCHRITT

1. Read Models für Performance definieren
2. GraphQL Schema entwerfen
3. KPI Card Components bauen

**Detaillierte Implementierung:** [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)  
**UI/UX Mockups:** [DASHBOARD_DESIGN.md](./DASHBOARD_DESIGN.md)