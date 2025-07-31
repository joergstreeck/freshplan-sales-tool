# 📆 Tag 1: Relationship Warmth Calculator

**Datum:** Montag, 19. August 2025  
**Fokus:** Beziehungstemperatur-Berechnung  
**Ziel:** Warmth Indicator für proaktiven Vertrieb  

## 🧭 Navigation

**↑ Woche 3 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 2: Relationship](./DAY2_RELATIONSHIP.md)  
**📘 Spec:** [Warmth Specification](./specs/WARMTH_SPEC.md)  

## 🎯 Tagesziel

- Backend: Warmth Calculation Service
- Frontend: Warmth Indicator Component
- Metriken: Score-Berechnung implementieren
- UI: Visuelle Temperatur-Anzeige

## 🌡️ Warmth Score Konzept

```
Faktoren (100 Punkte gesamt):
├── Recency (40%): Tage seit letztem Kontakt
├── Frequency (30%): Interaktionen pro Monat
├── Response (20%): Antwortrate auf Kommunikation
└── Value (10%): Geschäftswert

Score → Temperatur:
80-100 = 🔥 HOT
60-79  = ☀️ WARM
40-59  = 🌤️ COOLING
0-39   = ❄️ COLD
```

## 💻 Backend Implementation

### 1. Warmth Calculation Service

```java
// RelationshipWarmthService.java
@ApplicationScoped
public class RelationshipWarmthService {
    
    @Inject
    EventStore eventStore;
    
    @Inject
    CommunicationService communicationService;
    
    public RelationshipWarmth calculateWarmth(UUID contactId) {
        // Collect metrics
        WarmthMetrics metrics = collectMetrics(contactId);
        
        // Calculate score
        WarmthScore score = calculateScore(metrics);
        
        // Generate suggestions
        List<ActionSuggestion> suggestions = generateSuggestions(score, metrics);
        
        return RelationshipWarmth.builder()
            .contactId(contactId)
            .temperature(score.getTemperature())
            .score(score.getValue())
            .lastInteraction(metrics.getLastInteraction())
            .interactionFrequency(metrics.getFrequency())
            .responseRate(metrics.getResponseRate())
            .suggestions(suggestions)
            .calculatedAt(Instant.now())
            .build();
    }
    
    private WarmthScore calculateScore(WarmthMetrics metrics) {
        double score = 100.0;
        
        // Recency factor (40% weight)
        long daysSinceContact = metrics.getDaysSinceLastContact();
        if (daysSinceContact > 60) score -= 40;
        else if (daysSinceContact > 30) score -= 20;
        else if (daysSinceContact > 14) score -= 10;
        
        // Frequency factor (30% weight)
        double monthlyInteractions = metrics.getMonthlyInteractionRate();
        if (monthlyInteractions < 1) score -= 30;
        else if (monthlyInteractions < 2) score -= 15;
        else if (monthlyInteractions > 4) score += 10;
        
        // Response rate factor (20% weight)
        double responseRate = metrics.getResponseRate();
        score += (responseRate - 0.5) * 40; // -20 to +20
        
        // Business value factor (10% weight)
        if (metrics.getAverageOrderValue() > 10000) score += 10;
        else if (metrics.getAverageOrderValue() > 5000) score += 5;
        
        // Determine temperature
        Temperature temp;
        if (score >= 80) temp = Temperature.HOT;
        else if (score >= 60) temp = Temperature.WARM;
        else if (score >= 40) temp = Temperature.COOLING;
        else temp = Temperature.COLD;
        
        return new WarmthScore(score, temp);
    }
}
```

**Vollständiger Code:** [backend/RelationshipWarmthService.java](./code/backend/RelationshipWarmthService.java)

### 2. Action Suggestions

```java
private List<ActionSuggestion> generateSuggestions(WarmthScore score, WarmthMetrics metrics) {
    List<ActionSuggestion> suggestions = new ArrayList<>();
    
    if (score.getTemperature() == Temperature.COOLING) {
        suggestions.add(ActionSuggestion.builder()
            .type(ActionType.REACH_OUT)
            .reason("Kontakt kühlt ab - Zeit für eine persönliche Note")
            .urgency(Urgency.HIGH)
            .suggestedAction("Anruf mit offenem Gespräch über aktuelle Herausforderungen")
            .build());
    }
    
    // Birthday reminder
    if (metrics.getDaysUntilBirthday() <= 7 && metrics.getDaysUntilBirthday() > 0) {
        suggestions.add(ActionSuggestion.builder()
            .type(ActionType.SEND_WISHES)
            .reason("Geburtstag in " + metrics.getDaysUntilBirthday() + " Tagen")
            .urgency(Urgency.MEDIUM)
            .suggestedAction("Persönliche Geburtstagsgrüße vorbereiten")
            .build());
    }
    
    // Cross-sell opportunity
    if (metrics.getProductGaps().size() > 0) {
        suggestions.add(ActionSuggestion.builder()
            .type(ActionType.CROSS_SELL)
            .reason("Kunde nutzt noch nicht: " + String.join(", ", metrics.getProductGaps()))
            .urgency(Urgency.LOW)
            .suggestedAction("Produktvorstellung für fehlende Services")
            .build());
    }
    
    return suggestions;
}
```

## 🎨 Frontend Implementation

### Warmth Indicator Component

```typescript
// components/RelationshipWarmthIndicator.tsx
export const RelationshipWarmthIndicator: React.FC<WarmthIndicatorProps> = ({
  contact,
  showDetails = false,
  onActionClick
}) => {
  const { warmth, loading } = useRelationshipWarmth(contact.id);
  
  const getTemperatureColor = (temp: Temperature): string => {
    const colors = {
      hot: '#ff6b6b',      // Rot
      warm: '#ffd93d',     // Gelb
      cooling: '#6bcf7f',  // Grün
      cold: '#4dabf7'      // Blau
    };
    return colors[temp] || '#868e96';
  };
  
  const getTemperatureIcon = (temp: Temperature) => {
    const icons = {
      hot: '🔥',
      warm: '☀️',
      cooling: '🌤️',
      cold: '❄️'
    };
    return icons[temp] || '❓';
  };
  
  if (loading) return <Skeleton variant="circular" width={40} height={40} />;
  if (!warmth) return null;
  
  return (
    <Box>
      <Tooltip
        title={
          <Box>
            <Typography variant="body2">
              Beziehungstemperatur: {warmth.score.toFixed(0)}/100
            </Typography>
            <Typography variant="caption">
              Letzter Kontakt: vor {warmth.daysSinceContact} Tagen
            </Typography>
          </Box>
        }
      >
        <Box
          sx={{
            display: 'inline-flex',
            alignItems: 'center',
            backgroundColor: getTemperatureColor(warmth.temperature),
            borderRadius: '20px',
            px: 2,
            py: 0.5,
            cursor: showDetails ? 'pointer' : 'default'
          }}
          onClick={() => showDetails && onActionClick?.(warmth)}
        >
          <Typography variant="h6" sx={{ mr: 1 }}>
            {getTemperatureIcon(warmth.temperature)}
          </Typography>
          <Typography variant="body2" fontWeight="bold">
            {warmth.temperature}
          </Typography>
        </Box>
      </Tooltip>
      
      {showDetails && warmth.suggestions.length > 0 && (
        <Alert 
          severity={warmth.temperature === 'cold' ? 'warning' : 'info'}
          sx={{ mt: 1 }}
        >
          <AlertTitle>Empfohlene Aktionen</AlertTitle>
          {warmth.suggestions.map((suggestion, idx) => (
            <Box key={idx} sx={{ mb: 1 }}>
              <Typography variant="body2">
                • {suggestion.suggestedAction}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {suggestion.reason}
              </Typography>
            </Box>
          ))}
        </Alert>
      )}
    </Box>
  );
};
```

**Vollständiger Code:** [frontend/RelationshipWarmthIndicator.tsx](./code/frontend/RelationshipWarmthIndicator.tsx)

## 📊 Metriken Collection

### Backend Metrics Service

```java
// WarmthMetricsCollector.java
private WarmthMetrics collectMetrics(UUID contactId) {
    // Last interaction
    Optional<Instant> lastInteraction = eventStore
        .getEvents(contactId)
        .stream()
        .filter(e -> e instanceof CommunicationEvent)
        .map(BaseEvent::getCreatedAt)
        .max(Comparator.naturalOrder());
    
    // Interaction frequency
    long interactionsLastMonth = eventStore
        .getEventsBetween(contactId, 
            Instant.now().minus(30, ChronoUnit.DAYS),
            Instant.now())
        .stream()
        .filter(e -> e instanceof CommunicationEvent)
        .count();
    
    // Response rate
    double responseRate = calculateResponseRate(contactId);
    
    // Business metrics
    BigDecimal avgOrderValue = orderService
        .getAverageOrderValue(contactId);
    
    return WarmthMetrics.builder()
        .lastInteraction(lastInteraction.orElse(null))
        .monthlyInteractionRate(interactionsLastMonth)
        .responseRate(responseRate)
        .averageOrderValue(avgOrderValue)
        .build();
}
```

## 🧪 Tests

### Warmth Calculation Test

```java
@Test
void shouldCalculateWarmthCorrectly() {
    // Given - Contact with specific interaction pattern
    UUID contactId = createTestContact();
    addInteraction(contactId, -5); // 5 days ago
    addInteraction(contactId, -10);
    addInteraction(contactId, -20);
    
    // When
    RelationshipWarmth warmth = warmthService.calculateWarmth(contactId);
    
    // Then
    assertThat(warmth.getTemperature()).isEqualTo(Temperature.WARM);
    assertThat(warmth.getScore()).isBetween(60.0, 79.0);
    assertThat(warmth.getSuggestions()).isNotEmpty();
}
```

**Alle Tests:** [tests/warmth/](./code/tests/warmth/)

## 📝 Checkliste

- [ ] WarmthService implementiert
- [ ] Score-Berechnung verifiziert
- [ ] Suggestions generiert
- [ ] Frontend Indicator erstellt
- [ ] Farben & Icons korrekt
- [ ] Tooltip informativ
- [ ] Tests geschrieben

## 🔗 Weiterführende Links

- **Metriken Details:** [Warmth Metrics Guide](./guides/WARMTH_METRICS.md)
- **UI Patterns:** [Temperature Indicators](./guides/TEMPERATURE_UI.md)
- **Nächster Schritt:** [→ Tag 2: Beziehungsebene](./DAY2_RELATIONSHIP.md)

---

**Status:** 📋 Bereit zur Implementierung