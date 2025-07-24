# FC-016: Deal Velocity Tracking

**Feature:** FC-016 KPI-Tracking & Reporting  
**Bereich:** Deal Velocity und Pipeline-Performance  
**Datum:** 2025-07-24  

## 🏃 Deal Velocity Metriken

### Definition
Deal Velocity misst wie schnell Opportunities durch die Pipeline bewegen und identifiziert Bottlenecks.

### Kern-Metriken

```java
public class DealVelocityMetrics {
    // Zeit pro Stage
    Map<String, Duration> avgTimePerStage;
    
    // Conversion Rates
    Map<String, Double> stageConversionRates;
    
    // Velocity Score
    Double overallVelocityScore;
    
    // Bottlenecks
    List<Bottleneck> identifiedBottlenecks;
}

public class Bottleneck {
    String fromStage;
    String toStage;
    Duration avgTime;
    Double dropOffRate;
    List<CommonBlocker> blockers;
}
```

## 📊 Stage-Zeit-Analyse

### Implementierung

```java
@ApplicationScoped
public class StageTimeAnalyzer {
    
    public StageTimeAnalysis analyzeStageTransitions(
        DateRange period,
        FilterCriteria filters
    ) {
        // 1. Alle Transitionen im Zeitraum
        var transitions = transitionRepo.findInPeriod(
            period.getFrom(), 
            period.getTo()
        );
        
        // 2. Nach Stage-Paaren gruppieren
        var byStageTransition = transitions.stream()
            .collect(Collectors.groupingBy(
                t -> StageTransitionKey.of(t.getFromStage(), t.getToStage())
            ));
        
        // 3. Metriken pro Übergang berechnen
        var stageMetrics = byStageTransition.entrySet().stream()
            .map(entry -> calculateStageMetrics(
                entry.getKey(), 
                entry.getValue()
            ))
            .collect(Collectors.toList());
        
        // 4. Bottlenecks identifizieren
        var bottlenecks = identifyBottlenecks(stageMetrics);
        
        return StageTimeAnalysis.builder()
            .stageMetrics(stageMetrics)
            .bottlenecks(bottlenecks)
            .overallVelocity(calculateOverallVelocity(transitions))
            .recommendations(generateRecommendations(bottlenecks))
            .build();
    }
    
    private StageMetric calculateStageMetrics(
        StageTransitionKey key,
        List<DealTransition> transitions
    ) {
        // Zeiten berechnen
        var times = transitions.stream()
            .map(DealTransition::getDuration)
            .collect(Collectors.toList());
        
        var avgTime = calculateAverage(times);
        var medianTime = calculateMedian(times);
        var p90Time = calculatePercentile(times, 90);
        
        // Erfolgsrate
        var successRate = transitions.stream()
            .filter(DealTransition::isSuccessful)
            .count() / (double) transitions.size();
        
        return StageMetric.builder()
            .fromStage(key.getFrom())
            .toStage(key.getTo())
            .transitionCount(transitions.size())
            .avgDuration(avgTime)
            .medianDuration(medianTime)
            .p90Duration(p90Time)
            .successRate(successRate)
            .build();
    }
}
```

## 🎯 Velocity Score Berechnung

### Formel
```
Velocity Score = (Anzahl Deals × Durchschnittlicher Deal-Wert × Win Rate) / Durchschnittliche Zykluszeit
```

### Implementierung

```java
@ApplicationScoped
public class VelocityScoreCalculator {
    
    public VelocityScore calculateVelocityScore(
        List<Opportunity> opportunities,
        LocalDate from,
        LocalDate to
    ) {
        // Nur abgeschlossene Deals
        var closedDeals = opportunities.stream()
            .filter(o -> o.getClosedAt() != null)
            .filter(o -> isInPeriod(o.getClosedAt(), from, to))
            .collect(Collectors.toList());
        
        if (closedDeals.isEmpty()) {
            return VelocityScore.zero();
        }
        
        // Komponenten berechnen
        var dealCount = closedDeals.size();
        var avgDealValue = calculateAvgValue(closedDeals);
        var winRate = calculateWinRate(closedDeals);
        var avgCycleTime = calculateAvgCycleTime(closedDeals);
        
        // Score berechnen
        var score = (dealCount * avgDealValue.doubleValue() * winRate) 
                    / avgCycleTime.toDays();
        
        // Historischer Vergleich
        var lastPeriodScore = calculateLastPeriodScore(from, to);
        var trend = score > lastPeriodScore ? Trend.IMPROVING : Trend.DECLINING;
        
        return VelocityScore.builder()
            .score(score)
            .components(VelocityComponents.builder()
                .dealCount(dealCount)
                .avgDealValue(avgDealValue)
                .winRate(winRate)
                .avgCycleTime(avgCycleTime)
                .build())
            .trend(trend)
            .percentageChange(calculatePercentageChange(score, lastPeriodScore))
            .build();
    }
}
```

## 📈 Pipeline Flow Visualisierung

### Sankey-Diagramm Daten

```typescript
interface PipelineFlowData {
    nodes: StageNode[];
    links: StageLink[];
    summary: {
        totalDealsEntered: number;
        totalDealsWon: number;
        totalDealsLost: number;
        avgTimeToClose: number;
    };
}

interface StageNode {
    id: string;
    label: string;
    dealCount: number;
    totalValue: number;
    avgTimeInStage: number;
}

interface StageLink {
    source: string;
    target: string;
    value: number;  // Anzahl Deals
    avgTime: number; // Durchschnittliche Zeit für Übergang
    dropOffRate: number; // % die hier verloren gehen
}
```

### Frontend-Komponente

```tsx
const PipelineFlowChart: React.FC = () => {
    const { flowData } = usePipelineFlow();
    
    return (
        <Card>
            <CardHeader>
                <Typography variant="h6">Pipeline Flow Analysis</Typography>
                <TimeRangeSelector onChange={handleTimeRangeChange} />
            </CardHeader>
            <CardContent>
                <SankeyDiagram
                    data={flowData}
                    height={400}
                    nodeWidth={15}
                    nodePadding={10}
                    onNodeClick={handleDrillDown}
                    tooltip={<FlowTooltip />}
                />
                
                <Box mt={2}>
                    <Grid container spacing={2}>
                        <Grid item xs={3}>
                            <MetricCard
                                label="Avg. Velocity"
                                value={`${flowData.summary.avgTimeToClose}d`}
                                icon={<SpeedIcon />}
                            />
                        </Grid>
                        <Grid item xs={3}>
                            <MetricCard
                                label="Win Rate"
                                value={`${calculateWinRate(flowData)}%`}
                                icon={<TrophyIcon />}
                            />
                        </Grid>
                        <Grid item xs={3}>
                            <MetricCard
                                label="Biggest Bottleneck"
                                value={findBiggestBottleneck(flowData)}
                                icon={<WarningIcon />}
                            />
                        </Grid>
                        <Grid item xs={3}>
                            <MetricCard
                                label="Pipeline Value"
                                value={formatCurrency(calculateTotalValue(flowData))}
                                icon={<EuroIcon />}
                            />
                        </Grid>
                    </Grid>
                </Box>
            </CardContent>
        </Card>
    );
};
```

## 🚨 Bottleneck Detection

### Algorithmus

```java
@ApplicationScoped
public class BottleneckDetector {
    
    @ConfigProperty(name = "kpi.bottleneck.threshold.days")
    Integer bottleneckThresholdDays = 14;
    
    @ConfigProperty(name = "kpi.bottleneck.dropoff.threshold")
    Double dropOffThreshold = 0.3; // 30%
    
    public List<Bottleneck> detectBottlenecks(
        List<StageMetric> stageMetrics
    ) {
        return stageMetrics.stream()
            .filter(this::isBottleneck)
            .map(this::analyzeBottleneck)
            .sorted(Comparator.comparing(Bottleneck::getSeverity).reversed())
            .collect(Collectors.toList());
    }
    
    private boolean isBottleneck(StageMetric metric) {
        // Zeit-basierter Bottleneck
        boolean timeBottleneck = metric.getAvgDuration().toDays() > bottleneckThresholdDays;
        
        // Drop-off basierter Bottleneck
        boolean dropOffBottleneck = (1 - metric.getSuccessRate()) > dropOffThreshold;
        
        // Velocity-basierter Bottleneck (im Vergleich zu anderen Stages)
        boolean velocityBottleneck = metric.getVelocityScore() < 0.5;
        
        return timeBottleneck || dropOffBottleneck || velocityBottleneck;
    }
    
    private Bottleneck analyzeBottleneck(StageMetric metric) {
        // Ursachen analysieren
        var causes = analyzeCauses(metric);
        
        // Empfehlungen generieren
        var recommendations = generateRecommendations(metric, causes);
        
        // Severity berechnen (1-10)
        var severity = calculateSeverity(metric);
        
        return Bottleneck.builder()
            .fromStage(metric.getFromStage())
            .toStage(metric.getToStage())
            .avgTime(metric.getAvgDuration())
            .dropOffRate(1 - metric.getSuccessRate())
            .causes(causes)
            .recommendations(recommendations)
            .severity(severity)
            .estimatedImpact(calculateImpact(metric))
            .build();
    }
    
    private List<BottleneckCause> analyzeCauses(StageMetric metric) {
        var causes = new ArrayList<BottleneckCause>();
        
        // Analyse der häufigsten Blockierungsgründe
        var activities = activityRepo.findByStageTransition(
            metric.getFromStage(), 
            metric.getToStage()
        );
        
        // Pattern-Erkennung
        if (hasPattern(activities, "waiting_for_approval")) {
            causes.add(new BottleneckCause(
                "Genehmigungsprozess",
                "60% der Deals warten auf interne Genehmigung",
                0.6
            ));
        }
        
        if (hasPattern(activities, "missing_information")) {
            causes.add(new BottleneckCause(
                "Fehlende Informationen",
                "40% benötigen zusätzliche Kundendaten",
                0.4
            ));
        }
        
        return causes;
    }
}
```

## 📊 Cohort Analysis

### Opportunity Cohorts nach Startmonat

```java
@ApplicationScoped
public class CohortAnalyzer {
    
    public CohortAnalysis analyzeCohorts(
        LocalDate from,
        LocalDate to,
        CohortType type // START_MONTH, DEAL_SIZE, SOURCE, etc.
    ) {
        var opportunities = opportunityRepo.findCreatedBetween(from, to);
        
        // Nach Cohort gruppieren
        var cohorts = opportunities.stream()
            .collect(Collectors.groupingBy(
                opp -> determineCohort(opp, type)
            ));
        
        // Metriken pro Cohort
        var cohortMetrics = cohorts.entrySet().stream()
            .map(entry -> CohortMetric.builder()
                .cohortName(entry.getKey())
                .startSize(entry.getValue().size())
                .currentStageDistribution(getStageDistribution(entry.getValue()))
                .conversionFunnel(buildConversionFunnel(entry.getValue()))
                .avgVelocity(calculateAvgVelocity(entry.getValue()))
                .valueMetrics(calculateValueMetrics(entry.getValue()))
                .build())
            .collect(Collectors.toList());
        
        return CohortAnalysis.builder()
            .cohortType(type)
            .cohorts(cohortMetrics)
            .insights(generateCohortInsights(cohortMetrics))
            .build();
    }
}
```

## 🎯 Performance Targets

### Target Setting und Monitoring

```java
@Entity
@Table(name = "velocity_targets")
public class VelocityTarget {
    @Id
    UUID id;
    
    String stagePair; // z.B. "QUALIFIED->PROPOSAL"
    Duration targetDuration;
    Double targetConversionRate;
    
    @ManyToOne
    Team team; // Team-spezifische Targets
    
    LocalDate effectiveFrom;
    LocalDate effectiveTo;
    
    // Eskalation
    @ElementCollection
    List<String> alertRecipients;
    AlertThreshold warningThreshold; // z.B. 20% über Target
    AlertThreshold criticalThreshold; // z.B. 50% über Target
}

@Scheduled(every = "4h")
public void monitorVelocityTargets() {
    var activeTargets = targetRepo.findActive();
    var currentMetrics = velocityAnalyzer.getCurrentMetrics();
    
    activeTargets.forEach(target -> {
        var actual = currentMetrics.getForStagePair(target.getStagePair());
        var variance = calculateVariance(actual, target);
        
        if (variance > target.getCriticalThreshold()) {
            alertService.sendCriticalAlert(
                target.getAlertRecipients(),
                buildAlertMessage(target, actual, variance)
            );
        } else if (variance > target.getWarningThreshold()) {
            alertService.sendWarning(
                target.getAlertRecipients(),
                buildWarningMessage(target, actual, variance)
            );
        }
    });
}
```

## 📈 Trend Prediction

### ML-basierte Vorhersage

```java
@ApplicationScoped
public class VelocityPredictor {
    
    @Inject
    MLModelService mlService;
    
    public VelocityPrediction predictNextPeriod(
        List<HistoricalVelocityData> historicalData,
        PredictionPeriod period
    ) {
        // Feature Engineering
        var features = extractFeatures(historicalData);
        
        // Saisonalität berücksichtigen
        features.addSasonalityFeatures(period);
        
        // Externe Faktoren
        features.addExternalFactors(
            getHolidayCalendar(),
            getEconomicIndicators()
        );
        
        // Prediction
        var prediction = mlService.predict(
            "velocity_prediction_model",
            features
        );
        
        return VelocityPrediction.builder()
            .period(period)
            .predictedVelocityScore(prediction.getScore())
            .confidenceInterval(prediction.getConfidenceInterval())
            .keyDrivers(prediction.getFeatureImportance())
            .recommendations(generateRecommendations(prediction))
            .build();
    }
}
```