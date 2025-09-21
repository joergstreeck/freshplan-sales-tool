# FC-016: Renewal Metrics Specification

**Feature:** FC-016 KPI-Tracking & Reporting  
**Bereich:** Renewal-spezifische Metriken  
**Datum:** 2025-07-24  

## 📊 Renewal-Quote Berechnung

### Definition
Die Renewal-Quote misst den Prozentsatz erfolgreich verlängerter Verträge im Verhältnis zu allen auslaufenden Verträgen in einem bestimmten Zeitraum.

### Formel
```
Renewal-Quote = (Erfolgreich verlängerte Verträge / Alle auslaufenden Verträge) × 100
```

### Implementierung

```java
@Entity
@Table(name = "renewal_metrics")
public class RenewalMetric {
    @Id
    UUID id;
    
    LocalDate periodStart;
    LocalDate periodEnd;
    
    // Zähler
    Integer totalExpiringContracts;
    Integer successfulRenewals;
    Integer lostRenewals;
    Integer pendingRenewals;
    Integer deferredRenewals;
    
    // Werte
    BigDecimal totalContractValue;
    BigDecimal renewedValue;
    BigDecimal lostValue;
    BigDecimal atRiskValue;
    
    // Berechnete Metriken
    BigDecimal renewalQuote;
    BigDecimal valueRetentionRate;
    BigDecimal averageRenewalTime;
}

@ApplicationScoped
public class RenewalMetricsCalculator {
    
    public RenewalQuoteResult calculateRenewalQuote(
        LocalDate from, 
        LocalDate to, 
        RenewalFilters filters
    ) {
        // 1. Alle Verträge die im Zeitraum auslaufen
        var expiringContracts = contractRepo
            .findExpiringInPeriod(from, to)
            .filter(filters::matches);
        
        // 2. Nach Status gruppieren
        var groupedByStatus = expiringContracts.stream()
            .collect(Collectors.groupingBy(Contract::getRenewalStatus));
        
        // 3. Metriken berechnen
        var successful = groupedByStatus.getOrDefault(RENEWED, List.of());
        var lost = groupedByStatus.getOrDefault(LOST, List.of());
        var pending = groupedByStatus.getOrDefault(PENDING, List.of());
        
        // 4. Quote berechnen
        double renewalQuote = expiringContracts.isEmpty() ? 0 : 
            (double) successful.size() / expiringContracts.size() * 100;
        
        // 5. Value Retention
        var totalValue = sumContractValues(expiringContracts);
        var renewedValue = sumContractValues(successful);
        double valueRetention = totalValue.compareTo(BigDecimal.ZERO) == 0 ? 0 :
            renewedValue.divide(totalValue, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
        
        return RenewalQuoteResult.builder()
            .renewalQuote(renewalQuote)
            .valueRetentionRate(valueRetention)
            .totalContracts(expiringContracts.size())
            .successfulRenewals(successful.size())
            .lostRenewals(lost.size())
            .pendingRenewals(pending.size())
            .build();
    }
}
```

## ⏱️ Durchschnittliche Zeit bis zum Vertragsabschluss

### Definition
Misst die durchschnittliche Anzahl von Tagen zwischen:
1. **Renewal-Start**: 90 Tage vor Vertragsende ODER erstem Kontakt
2. **Renewal-Abschluss**: Unterschrift der neuen Vereinbarung

### Berechnung

```java
public class RenewalTimeMetrics {
    
    public Duration calculateAverageRenewalTime(List<Contract> renewedContracts) {
        if (renewedContracts.isEmpty()) {
            return Duration.ZERO;
        }
        
        var totalDays = renewedContracts.stream()
            .map(this::calculateRenewalDuration)
            .mapToLong(Duration::toDays)
            .sum();
        
        return Duration.ofDays(totalDays / renewedContracts.size());
    }
    
    private Duration calculateRenewalDuration(Contract contract) {
        LocalDateTime renewalStart = determineRenewalStart(contract);
        LocalDateTime renewalEnd = contract.getRenewalCompletedAt();
        
        return Duration.between(renewalStart, renewalEnd);
    }
    
    private LocalDateTime determineRenewalStart(Contract contract) {
        // Option 1: Explizites Renewal-Start-Datum
        if (contract.getRenewalStartedAt() != null) {
            return contract.getRenewalStartedAt();
        }
        
        // Option 2: Erste Renewal-Aktivität
        var firstActivity = activityRepo
            .findFirstRenewalActivity(contract.getId());
        if (firstActivity.isPresent()) {
            return firstActivity.get().getCreatedAt();
        }
        
        // Option 3: 90 Tage vor Vertragsende
        return contract.getEndDate()
            .minusDays(90)
            .atStartOfDay();
    }
}
```

## 📈 Entwicklung verlagerter Deals im Zeitverlauf

### Definition
Tracking von Opportunities/Renewals die verschoben wurden:
- Original geplantes Abschlussdatum vs. tatsächliches Datum
- Anzahl der Verschiebungen
- Gründe für Verschiebungen

### Datenmodell

```java
@Entity
@Table(name = "deal_deferrals")
public class DealDeferral {
    @Id
    UUID id;
    
    @ManyToOne
    Opportunity opportunity;
    
    LocalDate originalCloseDate;
    LocalDate newCloseDate;
    LocalDateTime deferredAt;
    
    @ManyToOne
    User deferredBy;
    
    @Enumerated(EnumType.STRING)
    DeferralReason reason;
    
    String reasonDetails;
    
    // Für Trend-Analyse
    Integer deferralCount; // Wie oft wurde dieser Deal schon verschoben?
    Integer daysDeferred;
}

public enum DeferralReason {
    BUDGET_CONSTRAINTS("Budget-Einschränkungen"),
    DECISION_MAKER_UNAVAILABLE("Entscheider nicht verfügbar"),
    ADDITIONAL_REQUIREMENTS("Zusätzliche Anforderungen"),
    COMPETITOR_EVALUATION("Wettbewerber-Evaluierung"),
    INTERNAL_APPROVAL_PENDING("Interne Genehmigung ausstehend"),
    PRICE_NEGOTIATION("Preisverhandlung"),
    CONTRACT_REVIEW("Vertragsüberprüfung"),
    OTHER("Sonstiges");
}
```

### Trend-Analysen

```java
@ApplicationScoped
public class DeferralTrendAnalyzer {
    
    public DeferralTrends analyzeDeferralTrends(
        LocalDate from,
        LocalDate to,
        GroupingPeriod period // DAILY, WEEKLY, MONTHLY
    ) {
        var deferrals = deferralRepo.findInPeriod(from, to);
        
        // 1. Gruppierung nach Zeitperiode
        var byPeriod = deferrals.stream()
            .collect(Collectors.groupingBy(
                d -> truncateToPeriod(d.getDeferredAt(), period)
            ));
        
        // 2. Metriken pro Periode
        var periodMetrics = byPeriod.entrySet().stream()
            .map(entry -> PeriodMetric.builder()
                .period(entry.getKey())
                .deferralCount(entry.getValue().size())
                .totalValueDeferred(sumDeferredValues(entry.getValue()))
                .avgDaysDeferred(avgDaysDeferred(entry.getValue()))
                .topReasons(getTopReasons(entry.getValue(), 3))
                .build())
            .sorted(Comparator.comparing(PeriodMetric::getPeriod))
            .collect(Collectors.toList());
        
        // 3. Trend-Berechnung
        var trend = calculateTrend(periodMetrics);
        
        return DeferralTrends.builder()
            .periodMetrics(periodMetrics)
            .overallTrend(trend)
            .mostCommonReason(findMostCommonReason(deferrals))
            .avgDeferralCount(calculateAvgDeferralCount(deferrals))
            .build();
    }
    
    // Vorhersage basierend auf historischen Daten
    public DeferralPrediction predictDeferrals(
        List<Opportunity> openOpportunities
    ) {
        var historicalPatterns = analyzeHistoricalPatterns();
        
        return openOpportunities.stream()
            .map(opp -> {
                var riskScore = calculateDeferralRisk(opp, historicalPatterns);
                return OpportunityRisk.builder()
                    .opportunity(opp)
                    .deferralProbability(riskScore)
                    .likelyReasons(predictReasons(opp, historicalPatterns))
                    .recommendedActions(suggestActions(riskScore))
                    .build();
            })
            .filter(risk -> risk.getDeferralProbability() > 0.3) // 30% Threshold
            .sorted(Comparator.comparing(OpportunityRisk::getDeferralProbability).reversed())
            .collect(Collectors.toList());
    }
}
```

## 📊 Dashboard-Visualisierungen

### 1. Renewal Quote Widget
```typescript
interface RenewalQuoteWidget {
    currentQuote: number;          // z.B. 87.5%
    target: number;                // z.B. 85%
    trend: 'up' | 'down' | 'stable';
    periodComparison: {
        lastMonth: number;
        lastQuarter: number;
        lastYear: number;
    };
    sparklineData: number[];       // Letzte 12 Monate
}
```

### 2. Time-to-Close Chart
```typescript
interface TimeToCloseChart {
    type: 'bar' | 'line';
    data: {
        labels: string[];          // Monate/Wochen
        avgDays: number[];         // Durchschnittliche Tage
        median: number[];          // Median (robuster)
        target: number;            // Ziel-Linie
    };
    breakdown: {
        byStage: StageTimeBreakdown[];
        byTeam: TeamTimeBreakdown[];
    };
}
```

### 3. Deferral Heatmap
```typescript
interface DeferralHeatmap {
    // Kalender-Ansicht mit Farbcodierung
    data: {
        date: string;
        deferralsCount: number;
        totalValue: number;
        intensity: 0-1;           // Für Farbintensität
    }[];
    // Drill-down Möglichkeit
    onCellClick: (date: string) => DeferralDetails[];
}
```

## 🎯 KPI-Ziele und Alerts

### Automatische Benachrichtigungen

```java
@ApplicationScoped
public class RenewalAlertService {
    
    @ConfigProperty(name = "kpi.renewal.quote.target")
    Double renewalQuoteTarget = 85.0;
    
    @ConfigProperty(name = "kpi.renewal.time.target.days")
    Integer targetRenewalDays = 30;
    
    @Scheduled(every = "1h")
    void checkKpiThresholds() {
        var currentMetrics = metricsCalculator.getCurrentMetrics();
        
        // Renewal Quote unter Ziel?
        if (currentMetrics.getRenewalQuote() < renewalQuoteTarget - 5) {
            alertService.createAlert(
                AlertType.KPI_BELOW_TARGET,
                "Renewal-Quote bei " + currentMetrics.getRenewalQuote() + "%",
                AlertSeverity.HIGH,
                getResponsibleManagers()
            );
        }
        
        // Zu viele Deferrals?
        if (currentMetrics.getDeferralRate() > 20) {
            alertService.createAlert(
                AlertType.HIGH_DEFERRAL_RATE,
                "20% der Deals wurden verschoben",
                AlertSeverity.MEDIUM,
                getSalesManagers()
            );
        }
    }
}
```

## 📈 Export-Formate

### PDF Report Template
- Executive Summary (1 Seite)
- Renewal Performance Details
- Team-Vergleiche
- Trend-Analysen mit Charts
- Handlungsempfehlungen

### Excel Export
- Raw Data Sheet
- Pivot-fähige Struktur
- Vordefinierte Pivot Tables
- Conditional Formatting für KPIs

### CSV für BI-Tools
- Standardisiertes Format
- UTF-8 Encoding
- Semikolon-getrennt für Excel-DE
- ISO-8601 Datumsformat