# 🎯 FC-019: Advanced Sales Metrics KOMPAKT

**In 15 Minuten Sales Metrics verstehen und umsetzen!**

## 🎯 Was bauen wir?

**Ein Real-time Sales Analytics Dashboard** das die wichtigsten KPIs visualisiert und Bottlenecks aufdeckt.

**Kernmetriken:**
- 📊 **Sales Velocity**: Wie schnell Deals durch die Pipeline wandern
- 🎯 **Win Rates**: Erfolgsquote pro Stage und gesamt
- 📈 **Deal Size Trends**: Entwicklung der Dealgrößen
- ⏱️ **Sales Cycle Length**: Zeit von Lead zu Close
- 🚨 **Bottleneck Detection**: Wo stocken Deals?

## 💰 Business Value in Zahlen

| Metrik | Ohne Dashboard | Mit Dashboard | Impact |
|--------|----------------|---------------|---------|
| Forecast Genauigkeit | ~60% | >85% | **+25% genauer** |
| Problem-Erkennung | 2-4 Wochen | Real-time | **95% schneller** |
| Sales Velocity | Unbekannt | Optimiert | **20% schneller** |
| Team Performance | Gefühl | Fakten | **Objektiv messbar** |

## 🏗️ Quick Architecture

```typescript
// 1. Metrics Engine berechnet KPIs
interface SalesMetrics {
  velocity: number;        // Tage von Stage 1 → Won
  winRates: WinRateByStage[];
  avgDealSize: number;
  cycleLength: number;
  bottlenecks: Bottleneck[];
}

// 2. Real-time Updates via WebSocket
metricsService.subscribe((metrics) => {
  updateDashboard(metrics);
  checkAlerts(metrics);
});

// 3. Smart Alerts
if (metrics.winRate < threshold) {
  notify("Win Rate dropped below 30%!");
}
```

## 📱 UI in 5 Minuten

**Dashboard-Mockup:**
```
Sales Metrics Dashboard                    [Export] [Settings]

VELOCITY: 23 Tage ↗️+15%    WIN RATE: 38.5% ↘️-5%

Pipeline Health                    Team Performance
┌─────────────────┐               ┌─────────────────┐
│ Lead → Won      │               │ Anna:    45% ⭐ │
│ ████████░░ 75%  │               │ Tom:     38% 📈 │
│ Stuck: 15 deals │               │ Lisa:    31% 📊 │
└─────────────────┘               └─────────────────┘

Deal Trends (€)                   Alerts (3)
📈 47k avg (+12%)                 🚨 15 Deals stuck >30d
                                  ⚠️ Win rate dropping
[Last 30 days ▼]                 💡 Focus on Negotiation
```

## 🚀 Implementierung - Der schnelle Weg

### Backend (2 Tage):
```java
@Path("/api/metrics")
public class MetricsResource {
    
    @GET
    @Path("/velocity")
    public SalesVelocity getVelocity(
        @QueryParam("period") String period
    ) {
        return metricsService.calculateVelocity(period);
    }
    
    @GET
    @Path("/win-rates")
    public WinRates getWinRates() {
        // Real-time aus Pipeline-Daten
        return metricsService.calculateWinRates();
    }
}
```

### Frontend (2 Tage):
```typescript
// React Hook für Live-Metriken
export const useSalesMetrics = () => {
  const { data, mutate } = useSWR('/api/metrics/summary', {
    refreshInterval: 30000 // 30 Sekunden
  });
  
  // WebSocket für Real-time Updates
  useEffect(() => {
    const ws = connectMetricsWebSocket();
    ws.on('update', mutate);
    return () => ws.close();
  }, []);
  
  return data;
};
```

### Quick Win Metriken (1 Tag):
```sql
-- Sales Velocity
SELECT AVG(closed_at - created_at) as velocity
FROM opportunities
WHERE status = 'WON'
AND closed_at > NOW() - INTERVAL '30 days';

-- Win Rate by Stage
SELECT 
  stage,
  COUNT(*) FILTER (WHERE status = 'WON') * 100.0 / 
  COUNT(*) as win_rate
FROM opportunities
GROUP BY stage;
```

## ⚡ Start in 3 Schritten

**Tag 1: Basis-Metriken**
- Sales Velocity berechnen
- Win Rates implementieren
- Erste API Endpoints

**Tag 2-3: Dashboard UI**
- Chart.js Integration
- Real-time Updates
- Responsive Design

**Tag 4-5: Advanced Features**
- Bottleneck Detection
- Smart Alerts
- Export-Funktionen

## 🎯 Erfolgs-Kriterien

✅ **MVP fertig wenn:**
- 4 Kern-Metriken live
- Dashboard lädt < 2 Sekunden
- Daten max. 30 Sekunden alt
- Mobile-optimiert

✅ **Production-ready wenn:**
- Historische Vergleiche möglich
- Alerts konfigurierbar
- Export nach Excel/PDF
- Performance < 500ms

## 🚨 Häufige Fehler vermeiden

❌ **Nicht machen:**
- Zu viele Metriken auf einmal
- Komplexe Berechnungen im Frontend
- Echtzeit wo 30 Sekunden reichen

✅ **Stattdessen:**
- Mit 4-5 Kern-KPIs starten
- Berechnungen im Backend cachen
- Progressive Enhancement

---

**🎯 In 15 Minuten hast du:** Dashboard-Konzept + erste Metrik läuft!

**📚 Details:** Siehe `FC-019_IMPLEMENTATION_GUIDE.md` für vollständige Umsetzung