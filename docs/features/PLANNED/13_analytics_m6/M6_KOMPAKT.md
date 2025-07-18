# M6: Analytics & Reports - Embedded BI ⚡

**Feature Code:** M6  
**Feature-Typ:** 🎨 FRONTEND  
**Geschätzter Aufwand:** 10-12 Tage  
**Priorität:** MITTEL - Nice-to-have wird Must-have  
**ROI:** Datengetriebene Entscheidungen = +30% Effizienz  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** "Ich brauche Reports!" → Excel-Export → Manuelles Rumfummeln  
**Lösung:** Embedded Analytics mit Drill-Down direkt im CRM  
**Impact:** Von Daten zu Entscheidung in <1 Minute  

---

## 📊 KERN-REPORTS

```
1. SALES FUNNEL
   Leads → Opportunities → Angebote → Aufträge
   
2. PERFORMANCE DASHBOARD
   Aktivitäten / Abschlussquote / Umsatz pro Verkäufer
   
3. CUSTOMER ANALYTICS
   Lifetime Value / Churn Risk / Kaufverhalten
   
4. FORECAST & TRENDS
   Pipeline-Entwicklung / Saisonalität / Predictions
```

---

## 🏃 QUICK-START IMPLEMENTIERUNG

### Analytics Engine
```java
@ApplicationScoped
public class AnalyticsService {
    @Inject
    AnalyticsQueryBuilder queryBuilder;
    
    public SalesFunnelMetrics calculateFunnel(DateRange range) {
        return SalesFunnelMetrics.builder()
            .leads(countByStage(LEAD, range))
            .opportunities(countByStage(OPPORTUNITY, range))
            .offers(countByStage(OFFER, range))
            .deals(countByStage(DEAL, range))
            .conversionRates(calculateConversions())
            .build();
    }
    
    @Cacheable("analytics-30min")
    public Report generateReport(ReportRequest request) {
        var query = queryBuilder.build(request);
        var data = executeQuery(query);
        return transformToReport(data, request.getType());
    }
}
```

### Chart Components
```typescript
// Recharts für interaktive Visualisierungen
import { BarChart, LineChart, PieChart, Treemap } from 'recharts';

export const SalesFunnelChart: React.FC<{ data: FunnelData }> = ({ data }) => {
  const funnelData = [
    { stage: 'Leads', value: data.leads, fill: '#8884d8' },
    { stage: 'Opportunities', value: data.opportunities, fill: '#83a6ed' },
    { stage: 'Angebote', value: data.offers, fill: '#8dd1e1' },
    { stage: 'Aufträge', value: data.deals, fill: '#82ca9d' }
  ];
  
  return (
    <ResponsiveContainer width="100%" height={400}>
      <BarChart data={funnelData} layout="horizontal">
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis type="number" />
        <YAxis dataKey="stage" type="category" />
        <Tooltip />
        <Bar dataKey="value" />
      </BarChart>
    </ResponsiveContainer>
  );
};
```

### Report Builder UI
```typescript
export const ReportBuilder: React.FC = () => {
  const [config, setConfig] = useState<ReportConfig>({
    type: 'sales-funnel',
    dateRange: 'last-30-days',
    groupBy: 'week',
    filters: []
  });
  
  const { data, isLoading } = useReport(config);
  
  return (
    <Box>
      {/* Report Configuration */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} md={3}>
            <ReportTypeSelector 
              value={config.type}
              onChange={(type) => setConfig({...config, type})}
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <DateRangeSelector 
              value={config.dateRange}
              onChange={(range) => setConfig({...config, dateRange: range})}
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <GroupBySelector 
              value={config.groupBy}
              options={['day', 'week', 'month', 'quarter']}
            />
          </Grid>
          <Grid item xs={12} md={3}>
            <FilterBuilder 
              filters={config.filters}
              onChange={(filters) => setConfig({...config, filters})}
            />
          </Grid>
        </Grid>
      </Paper>
      
      {/* Report Display */}
      <Paper sx={{ p: 2 }}>
        {isLoading ? (
          <ReportSkeleton />
        ) : (
          <ReportDisplay 
            type={config.type}
            data={data}
            onDrillDown={handleDrillDown}
          />
        )}
      </Paper>
      
      {/* Export Options */}
      <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
        <Button startIcon={<DownloadIcon />} onClick={() => exportPDF(data)}>
          PDF Export
        </Button>
        <Button startIcon={<TableChartIcon />} onClick={() => exportExcel(data)}>
          Excel Export
        </Button>
        <Button startIcon={<ShareIcon />} onClick={() => shareReport(config)}>
          Report teilen
        </Button>
      </Box>
    </Box>
  );
};
```

---

## 🔗 DEPENDENCIES & INTEGRATION

**Datenquellen:**
- M5 Customer (Stammdaten)
- M4 Opportunities (Pipeline)
- FC-005 Xentral (Umsatzdaten)
- Activities (Verkäufer-Performance)

**Tech Stack:**
- Apache Superset (optional für Power Users)
- Recharts für Visualisierung
- jsPDF für PDF Export
- SheetJS für Excel Export

---

## ⚡ KRITISCHE FEATURES

1. **Drill-Down:** Von Übersicht zu Details in einem Klick
2. **Scheduled Reports:** Automatischer Versand per E-Mail
3. **Custom Dashboards:** Jeder User seine Ansicht
4. **Real-Time Updates:** Live-Daten wo sinnvoll

---

## 📊 SUCCESS METRICS

- **Report Generation:** <3s für Standard-Reports
- **Adoption:** 80% nutzen wöchentlich
- **Insights:** 5+ Aha-Momente pro Monat
- **ROI:** 30% bessere Forecasts

---

## 🚀 NÄCHSTER SCHRITT

1. Report-Anforderungen sammeln (Top 10)
2. Datenmodell für Analytics
3. Recharts Prototyp bauen

**Report Catalog:** [REPORT_CATALOG.md](./REPORT_CATALOG.md)  
**Implementation Guide:** [ANALYTICS_GUIDE.md](./ANALYTICS_GUIDE.md)