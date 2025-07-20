# 📊 FC-015 DEAL LOSS ANALYSIS (KOMPAKT)

**Feature Code:** FC-015  
**Feature-Typ:** 🔀 FULLSTACK  
**Geschätzter Aufwand:** 4-5 Tage  
**Priorität:** MEDIUM - Lernen aus Niederlagen  
**ROI:** Win-Rate Verbesserung durch systematisches Lernen  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Deals verloren - niemand weiß warum, gleiche Fehler wiederholen sich  
**Lösung:** Strukturierte Verlustanalyse mit Pflichtfeldern + Quarterly Reports  
**Impact:** Win-Rate +15% durch Mustererkennung und Prozessverbesserung  

---

## 🧠 LOSS ANALYSIS WORKFLOW

```
OPPORTUNITY → CLOSED LOST
         ↓
[❌ Deal verloren - Analyse erforderlich]
         ↓
    PFLICHTFELDER:
    • Verlustgrund (Dropdown)
    • Gewinner (Competitor)
    • Verlustbetrag
    • Lessons Learned
         ↓
[💾 Speichern & Analysieren]
         ↓
    QUARTERLY REPORT:
    • Top Verlustgründe
    • Competitor Win-Rate
    • Trends & Muster
    • Action Items
```

---

## 📋 FEATURES IM DETAIL

### 1. Strukturierte Verlustgründe

```typescript
enum LossReason {
  PRICE = 'Preis zu hoch',
  COMPETITOR = 'Mitbewerber gewonnen',
  NO_BUDGET = 'Kein Budget',
  NO_NEED = 'Kein Bedarf mehr',
  TIMING = 'Falsches Timing',
  RELATIONSHIP = 'Fehlende Beziehung',
  PRODUCT_FIT = 'Produkt passt nicht',
  TECHNICAL = 'Technische Gründe',
  PROCESS = 'Interner Prozess Kunde',
  OTHER = 'Sonstiges'
}

interface DealLossAnalysis {
  opportunityId: string;
  lossReason: LossReason;
  lossReasonDetails?: string;
  competitor?: string;
  competitorPrice?: number;
  ourPrice: number;
  lessonsLearned: string;
  preventable: boolean;
  analysisDate: Date;
  analyzedBy: string;
}
```

### 2. Loss Analysis Modal

```typescript
const LossAnalysisModal: React.FC<{opportunity}> = ({ opportunity }) => {
  const [analysis, setAnalysis] = useState<DealLossAnalysis>({
    opportunityId: opportunity.id,
    ourPrice: opportunity.value,
    analyzedBy: currentUser.id
  });

  return (
    <Dialog open={true} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center">
          <ErrorOutline color="error" sx={{ mr: 1 }} />
          Deal-Verlust analysieren: {opportunity.title}
        </Box>
      </DialogTitle>
      
      <DialogContent>
        <Grid container spacing={3}>
          {/* Hauptgrund - Pflichtfeld */}
          <Grid item xs={12}>
            <FormControl fullWidth required>
              <InputLabel>Hauptgrund für Verlust</InputLabel>
              <Select
                value={analysis.lossReason}
                onChange={(e) => setAnalysis({...analysis, lossReason: e.target.value})}
              >
                {Object.entries(LossReason).map(([key, label]) => (
                  <MenuItem key={key} value={key}>{label}</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          {/* Dynamische Zusatzfelder basierend auf Grund */}
          {analysis.lossReason === 'COMPETITOR' && (
            <>
              <Grid item xs={6}>
                <Autocomplete
                  options={competitors}
                  renderInput={(params) => 
                    <TextField {...params} label="Gewinner" required />
                  }
                  onChange={(_, value) => 
                    setAnalysis({...analysis, competitor: value})
                  }
                />
              </Grid>
              <Grid item xs={6}>
                <TextField
                  label="Mitbewerber-Preis"
                  type="number"
                  InputProps={{
                    startAdornment: <InputAdornment position="start">€</InputAdornment>
                  }}
                  onChange={(e) => 
                    setAnalysis({...analysis, competitorPrice: Number(e.target.value)})
                  }
                />
              </Grid>
            </>
          )}

          {/* Lessons Learned - Pflichtfeld */}
          <Grid item xs={12}>
            <TextField
              label="Was haben wir gelernt?"
              multiline
              rows={4}
              required
              fullWidth
              placeholder="z.B. Entscheider wurde zu spät eingebunden, Preisverhandlung zu früh..."
              value={analysis.lessonsLearned}
              onChange={(e) => 
                setAnalysis({...analysis, lessonsLearned: e.target.value})
              }
            />
          </Grid>

          {/* War es vermeidbar? */}
          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={analysis.preventable}
                  onChange={(e) => 
                    setAnalysis({...analysis, preventable: e.target.checked})
                  }
                />
              }
              label="War dieser Verlust vermeidbar?"
            />
            {analysis.preventable && (
              <TextField
                label="Wie hätten wir es verhindern können?"
                fullWidth
                multiline
                rows={2}
                sx={{ mt: 2 }}
              />
            )}
          </Grid>
        </Grid>
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onCancel}>Abbrechen</Button>
        <Button 
          variant="contained" 
          onClick={handleSubmit}
          disabled={!isValid}
        >
          Analyse speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

### 3. Quarterly Loss Report Dashboard

```typescript
const LossAnalysisDashboard: React.FC = () => {
  const { data: lossData } = useQuarterlyLossReport();
  
  return (
    <Grid container spacing={3}>
      {/* Top Verlustgründe */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardHeader title="Top 5 Verlustgründe Q3 2025" />
          <CardContent>
            <BarChart
              data={lossData.topReasons}
              xAxis="reason"
              yAxis="count"
              color="#ff4444"
            />
            <List>
              {lossData.topReasons.map(reason => (
                <ListItem key={reason.name}>
                  <ListItemText
                    primary={reason.label}
                    secondary={`${reason.count} Deals (${reason.value}€)`}
                  />
                  <Chip label={`${reason.percentage}%`} />
                </ListItem>
              ))}
            </List>
          </CardContent>
        </Card>
      </Grid>

      {/* Competitor Analysis */}
      <Grid item xs={12} md={6}>
        <Card>
          <CardHeader title="Mitbewerber-Analyse" />
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Win/Loss gegen Competitors
            </Typography>
            {lossData.competitors.map(comp => (
              <Box key={comp.name} mb={2}>
                <Typography variant="subtitle1">{comp.name}</Typography>
                <LinearProgress
                  variant="determinate"
                  value={comp.winRate}
                  sx={{ height: 10, mb: 1 }}
                />
                <Typography variant="caption">
                  Gewonnen: {comp.wins} | Verloren: {comp.losses} 
                  | Win-Rate: {comp.winRate}%
                </Typography>
              </Box>
            ))}
          </CardContent>
        </Card>
      </Grid>

      {/* Trend Analysis */}
      <Grid item xs={12}>
        <Card>
          <CardHeader title="Verlustgrund-Trends (12 Monate)" />
          <CardContent>
            <LineChart
              data={lossData.trends}
              lines={['price', 'competitor', 'timing', 'product']}
              xAxis="month"
              yAxis="percentage"
            />
          </CardContent>
        </Card>
      </Grid>

      {/* Action Items */}
      <Grid item xs={12}>
        <Card>
          <CardHeader 
            title="Empfohlene Maßnahmen"
            avatar={<Lightbulb color="warning" />}
          />
          <CardContent>
            <List>
              {lossData.recommendations.map((rec, idx) => (
                <ListItem key={idx}>
                  <ListItemIcon>
                    <CheckCircle color="primary" />
                  </ListItemIcon>
                  <ListItemText
                    primary={rec.action}
                    secondary={`Basierend auf: ${rec.insight}`}
                  />
                  <Button size="small" variant="outlined">
                    Als Task erstellen
                  </Button>
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

### 4. Backend Analysis Service

```java
@ApplicationScoped
public class LossAnalysisService {
    
    @Inject
    OpportunityRepository opportunityRepo;
    
    @Inject
    LossAnalysisRepository analysisRepo;
    
    public QuarterlyLossReport generateQuarterlyReport(Quarter quarter) {
        List<LossAnalysis> analyses = analysisRepo
            .findByQuarter(quarter);
        
        return QuarterlyLossReport.builder()
            .topReasons(calculateTopReasons(analyses))
            .competitorAnalysis(analyzeCompetitors(analyses))
            .trends(calculateTrends(analyses))
            .recommendations(generateRecommendations(analyses))
            .preventablePercentage(calculatePreventable(analyses))
            .totalLossValue(calculateTotalLoss(analyses))
            .build();
    }
    
    private List<Recommendation> generateRecommendations(List<LossAnalysis> analyses) {
        List<Recommendation> recommendations = new ArrayList<>();
        
        // Preis-Probleme
        long priceIssues = analyses.stream()
            .filter(a -> a.getLossReason() == LossReason.PRICE)
            .count();
        
        if (priceIssues > analyses.size() * 0.3) {
            recommendations.add(new Recommendation(
                "Pricing-Strategie überprüfen",
                "30% der Verluste wegen Preis",
                Priority.HIGH
            ));
        }
        
        // Competitor-Muster
        Map<String, Long> competitorWins = analyses.stream()
            .filter(a -> a.getCompetitor() != null)
            .collect(Collectors.groupingBy(
                LossAnalysis::getCompetitor,
                Collectors.counting()
            ));
        
        competitorWins.entrySet().stream()
            .filter(e -> e.getValue() > 5)
            .forEach(e -> recommendations.add(new Recommendation(
                "Competitive Analysis für " + e.getKey(),
                e.getValue() + " Verluste an diesen Mitbewerber",
                Priority.MEDIUM
            )));
        
        return recommendations;
    }
}
```

### 5. Notifications & Alerts

```typescript
// Alert bei Mustern
const useLossPatternDetection = () => {
  useEffect(() => {
    // Check for patterns every week
    const checkPatterns = async () => {
      const patterns = await detectLossPatterns();
      
      if (patterns.criticalPattern) {
        showNotification({
          title: 'Kritisches Verlustmuster erkannt!',
          message: `${patterns.count} Deals in ${patterns.timeframe} 
                    aus gleichem Grund verloren: ${patterns.reason}`,
          severity: 'warning',
          action: {
            label: 'Analyse anzeigen',
            onClick: () => navigate('/analytics/loss-analysis')
          }
        });
      }
    };
    
    const interval = setInterval(checkPatterns, 7 * 24 * 60 * 60 * 1000);
    return () => clearInterval(interval);
  }, []);
};
```

---

## 🎯 BUSINESS VALUE

- **Win-Rate Verbesserung:** +10-15% durch Lernen
- **Preisoptimierung:** Bessere Positionierung gegen Competitors
- **Prozessverbesserung:** Schwachstellen identifizieren
- **Team-Training:** Gezielte Schulungen basierend auf Daten

---

## 🚀 IMPLEMENTIERUNGS-PHASEN

1. **Phase 1:** Loss Analysis Modal + Pflichtfelder
2. **Phase 2:** Basic Reporting (Top Gründe)
3. **Phase 3:** Competitor Intelligence
4. **Phase 4:** AI-basierte Empfehlungen

---

## 🔗 ABHÄNGIGKEITEN

- **Benötigt:** Opportunity Entity mit Status "Closed Lost"
- **Integration:** M4 Opportunity Pipeline
- **Nice-to-have:** BI Tool für erweiterte Analysen

---

## 📊 SUCCESS METRICS

- **Analysis Rate:** 100% aller Lost Deals analysiert
- **Data Quality:** > 90% mit Lessons Learned
- **Report Usage:** > 80% Management schaut monatlich
- **Win-Rate Impact:** Messbare Verbesserung nach 6 Monaten

---

**Nächster Schritt:** Loss Analysis Entity + Modal in Opportunity Pipeline integrieren

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[📊 M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)** - Opportunity Status "Closed Lost"
- **[🔒 FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)** - Analyse-Berechtigungen
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Loss Event in Timeline

### ⚡ Datenquellen:
- **[📊 M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md)** - Reporting Framework
- **[📥 FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_KOMPAKT.md)** - Competitor-Preise
- **[🛡️ FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_KOMPAKT.md)** - Sales Rep Performance

### 🚀 Ermöglicht folgende Features:
- **[📊 FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_KOMPAKT.md)** - Loss Analysis KPIs
- **[📈 FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_KOMPAKT.md)** - Win/Loss Ratios
- **[🎯 FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_KOMPAKT.md)** - Loss Prevention Insights

### 🎨 UI Integration:
- **[🧭 M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)** - Analytics-Menüpunkt
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Loss Alerts Widget
- **[⚙️ M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)** - Loss Reason Config

### 🔧 Technische Details:
- **[FC-015_IMPLEMENTATION_GUIDE.md](./FC-015_IMPLEMENTATION_GUIDE.md)** - Analysis Engine Details
- **[FC-015_DECISION_LOG.md](./FC-015_DECISION_LOG.md)** - Mandatory vs. Optional Fields
- **[LOSS_TAXONOMY.md](./LOSS_TAXONOMY.md)** - Standardisierte Verlustgründe