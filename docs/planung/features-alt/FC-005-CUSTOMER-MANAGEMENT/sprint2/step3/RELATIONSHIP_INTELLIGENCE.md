# 🌡️ Relationship Intelligence - Contact Warmth System

**Phase:** 2 - Intelligence Features  
**Tag:** 1 der Woche 2  
**Status:** 🎯 Ready for Implementation  

## 🧭 Navigation

**← Zurück:** [Step 3 Main Guide](./README.md)  
**→ Nächster:** [Contact Timeline](./CONTACT_TIMELINE.md)  
**↑ Übergeordnet:** [Sprint 2 Master Plan CRUD](../SPRINT2_MASTER_PLAN_CRUD.md)  

## 🎯 Vision: Proaktive Beziehungspflege

Das **Relationship Warmth System** verwandelt passive Kontaktdaten in **proaktive Vertriebsintelligenz**:

> "Dieser Kontakt kühlt ab - Zeit für einen persönlichen Anruf!"

### 💬 Team-Feedback:
> "Genial! Echter Vertriebs-Vorsprung. Customer Intelligence als Frühwarnsystem. KI-gestützte Vorschläge möglich."

## 🌡️ Warmth Indicator System

### Core Algorithm (Backend)

```java
@ApplicationScoped
public class RelationshipWarmthService {
    
    public RelationshipWarmth calculateWarmth(UUID contactId) {
        WarmthMetrics metrics = collectMetrics(contactId);
        WarmthScore score = calculateScore(metrics);
        List<ActionSuggestion> suggestions = generateSuggestions(score, metrics);
        
        return RelationshipWarmth.builder()
            .contactId(contactId)
            .temperature(score.getTemperature())
            .score(score.getValue()) // 0-100
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

### Data Models

```java
// RelationshipWarmth.java
public class RelationshipWarmth {
    private UUID contactId;
    private Temperature temperature; // HOT, WARM, COOLING, COLD
    private double score; // 0-100
    private Instant lastInteraction;
    private double interactionFrequency; // per month
    private double responseRate; // 0.0-1.0
    private List<ActionSuggestion> suggestions;
    private Instant calculatedAt;
}

// ActionSuggestion.java
public class ActionSuggestion {
    private ActionType type; // REACH_OUT, SEND_INFO, SCHEDULE_MEETING
    private String reason;
    private Urgency urgency; // HIGH, MEDIUM, LOW
    private String suggestedAction;
    
    // Smart suggestions examples:
    // "Kontakt kühlt ab - Zeit für persönliche Note"
    // "Geburtstag in 3 Tagen - Glückwunsch senden"
    // "Letzter Kontakt vor 45 Tagen - Check-in Call"
}

// WarmthMetrics.java (collected from various sources)
public class WarmthMetrics {
    private long daysSinceLastContact;
    private double monthlyInteractionRate;
    private double responseRate;
    private double averageOrderValue;
    private int daysUntilBirthday;
    private List<InteractionType> recentInteractionTypes;
}
```

## 🎨 Frontend UI Components

### Warmth Indicator Badge

```typescript
// components/WarmthIndicator.tsx
interface WarmthIndicatorProps {
  warmth: RelationshipWarmth;
  size?: 'small' | 'medium' | 'large';
  showScore?: boolean;
}

export const WarmthIndicator: React.FC<WarmthIndicatorProps> = ({
  warmth,
  size = 'medium',
  showScore = false
}) => {
  const getColor = () => {
    switch (warmth.temperature) {
      case 'HOT': return '#FF4444';      // Rot/Hot
      case 'WARM': return '#FF8800';     // Orange
      case 'COOLING': return '#FFBB00'; // Gelb
      case 'COLD': return '#666666';    // Grau
    }
  };
  
  const getIcon = () => {
    switch (warmth.temperature) {
      case 'HOT': return '🔥';
      case 'WARM': return '🌡️';
      case 'COOLING': return '⚠️';
      case 'COLD': return '❄️';
    }
  };
  
  return (
    <Chip
      icon={<span>{getIcon()}</span>}
      label={showScore ? `${warmth.score.toFixed(0)}` : warmth.temperature}
      size={size}
      sx={{
        backgroundColor: getColor(),
        color: 'white',
        fontWeight: 'bold'
      }}
      onClick={() => showWarmthDetails(warmth)}
    />
  );
};
```

### Smart Suggestions Panel

```typescript
// components/SmartSuggestions.tsx
export const SmartSuggestions: React.FC<{suggestions: ActionSuggestion[]}> = ({
  suggestions
}) => {
  if (suggestions.length === 0) return null;
  
  return (
    <Card sx={{ mt: 2, backgroundColor: '#f8f9fa' }}>
      <CardHeader
        avatar={<LightbulbIcon color="primary" />}
        title="💡 Smart Suggestions"
        subheader="Proaktive Handlungsempfehlungen"
      />
      <CardContent>
        {suggestions.map((suggestion, index) => (
          <Alert 
            key={index}
            severity={suggestion.urgency === 'HIGH' ? 'error' : 'info'}
            action={
              <Button size="small" variant="outlined">
                {suggestion.type === 'REACH_OUT' ? 'Kontaktieren' : 'Details'}
              </Button>
            }
            sx={{ mb: 1 }}
          >
            <AlertTitle>{suggestion.reason}</AlertTitle>
            {suggestion.suggestedAction}
          </Alert>
        ))}
      </CardContent>
    </Card>
  );
};
```

## 📊 Backend Integration

### Enhanced Contact Service

```java
@ApplicationScoped
public class ContactService {
    
    @Inject
    RelationshipWarmthService warmthService;
    
    @Inject
    InteractionTrackingService interactionService;
    
    public ContactWithWarmth getContactWithIntelligence(UUID contactId) {
        Contact contact = getContact(contactId);
        RelationshipWarmth warmth = warmthService.calculateWarmth(contactId);
        List<RecentInteraction> timeline = interactionService.getRecentInteractions(contactId);
        
        return ContactWithWarmth.builder()
            .contact(contact)
            .warmth(warmth)
            .recentTimeline(timeline)
            .build();
    }
    
    // Track interaction when contact is accessed/modified
    @EventHandler
    public void onContactAccessed(ContactAccessedEvent event) {
        interactionService.recordInteraction(
            event.getContactId(),
            InteractionType.VIEWED,
            event.getUserId(),
            Instant.now()
        );
    }
}
```

### REST API Extensions

```java
@Path("/api/customers/{customerId}/contacts")
public class ContactResource {
    
    @GET
    @Path("/{contactId}/intelligence")
    @Operation(summary = "Get contact with relationship intelligence")
    public ContactWithWarmth getContactIntelligence(
            @PathParam("customerId") UUID customerId,
            @PathParam("contactId") UUID contactId) {
        return contactService.getContactWithIntelligence(contactId);
    }
    
    @GET
    @Path("/warmth-alerts")
    @Operation(summary = "Get contacts needing attention")
    public List<ContactAlert> getWarmthAlerts(
            @PathParam("customerId") UUID customerId,
            @QueryParam("temperature") String temperature) {
        return warmthService.getContactsNeedingAttention(customerId, temperature);
    }
}
```

## 🧪 Testing Strategy

### Backend Unit Tests

```java
@QuarkusTest
class RelationshipWarmthServiceTest {
    
    @Test
    void shouldCalculateHotTemperature_whenRecentFrequentContact() {
        // Given
        WarmthMetrics metrics = WarmthMetrics.builder()
            .daysSinceLastContact(3)
            .monthlyInteractionRate(8.0)
            .responseRate(0.9)
            .averageOrderValue(15000)
            .build();
        
        // When
        WarmthScore score = warmthService.calculateScore(metrics);
        
        // Then
        assertThat(score.getTemperature()).isEqualTo(Temperature.HOT);
        assertThat(score.getValue()).isGreaterThan(80);
    }
    
    @Test
    void shouldGenerateSuggestion_whenContactCooling() {
        // Given
        WarmthMetrics coolingMetrics = WarmthMetrics.builder()
            .daysSinceLastContact(35)
            .monthlyInteractionRate(0.5)
            .build();
        
        // When
        List<ActionSuggestion> suggestions = warmthService.generateSuggestions(
            new WarmthScore(45, Temperature.COOLING), 
            coolingMetrics
        );
        
        // Then
        assertThat(suggestions).hasSize(1);
        assertThat(suggestions.get(0).getType()).isEqualTo(ActionType.REACH_OUT);
        assertThat(suggestions.get(0).getUrgency()).isEqualTo(Urgency.HIGH);
    }
}
```

### Frontend Integration Tests

```typescript
describe('WarmthIndicator', () => {
  it('should display correct temperature and color', () => {
    const warmth: RelationshipWarmth = {
      temperature: 'HOT',
      score: 85,
      suggestions: []
    };
    
    render(<WarmthIndicator warmth={warmth} showScore />);
    
    expect(screen.getByText('85')).toBeInTheDocument();
    expect(screen.getByText('🔥')).toBeInTheDocument();
  });
  
  it('should show suggestions when contact is cooling', () => {
    const warmth: RelationshipWarmth = {
      temperature: 'COOLING',
      score: 45,
      suggestions: [{
        type: 'REACH_OUT',
        reason: 'Kontakt kühlt ab',
        urgency: 'HIGH'
      }]
    };
    
    render(<SmartSuggestions suggestions={warmth.suggestions} />);
    
    expect(screen.getByText('Kontakt kühlt ab')).toBeInTheDocument();
    expect(screen.getByText('Kontaktieren')).toBeInTheDocument();
  });
});
```

## 📈 Success Metrics

### Technical KPIs:
- **Response Time:** Warmth calculation < 50ms
- **Accuracy:** 85% user agreement with suggestions
- **Coverage:** Warmth data for 95% of active contacts

### Business KPIs:
- **Proactive Actions:** +40% increase in proactive contact
- **Relationship Quality:** Improved average warmth scores
- **Sales Impact:** Measurable revenue correlation with warmth

## 💡 Enhanced Relationship Features

### Hobby Selection UI

```typescript
// components/HobbySelector.tsx
export const HobbySelector: React.FC<{
  selected: string[];
  onChange: (hobbies: string[]) => void;
}> = ({ selected, onChange }) => {
  const hobbyCategories = {
    sport: ['Golf', 'Tennis', 'Segeln', 'Fitness', 'Wandern', 'Radfahren'],
    kultur: ['Kunst', 'Musik', 'Theater', 'Literatur', 'Fotografie'],
    genuss: ['Wein', 'Kochen', 'Reisen', 'Restaurants'],
    technik: ['Gaming', 'Smart Home', 'Autos', 'Gadgets'],
    social: ['Networking', 'Vereine', 'Familie', 'Haustiere']
  };
  
  return (
    <Box>
      {Object.entries(hobbyCategories).map(([category, hobbies]) => (
        <Box key={category} mb={2}>
          <Typography variant="caption" color="text.secondary" gutterBottom>
            {category.charAt(0).toUpperCase() + category.slice(1)}
          </Typography>
          <Box display="flex" gap={0.5} flexWrap="wrap">
            {hobbies.map(hobby => (
              <Chip
                key={hobby}
                label={hobby}
                onClick={() => {
                  if (selected.includes(hobby)) {
                    onChange(selected.filter(h => h !== hobby));
                  } else {
                    onChange([...selected, hobby]);
                  }
                }}
                color={selected.includes(hobby) ? 'primary' : 'default'}
                size="small"
                sx={{ 
                  cursor: 'pointer',
                  transition: 'all 0.2s',
                  '&:hover': {
                    transform: 'scale(1.05)'
                  }
                }}
              />
            ))}
          </Box>
        </Box>
      ))}
    </Box>
  );
};
```

### Personal Notes Best Practices

```typescript
// components/PersonalNotesField.tsx
export const PersonalNotesField: React.FC<{
  value: string;
  onChange: (value: string) => void;
}> = ({ value, onChange }) => {
  const [showSuggestions, setShowSuggestions] = useState(!value);
  
  const noteSuggestions = [
    '🕐 Bevorzugte Anrufzeiten',
    '☕ Kaffee oder Tee Präferenz',
    '🎯 Persönliche Ziele',
    '👨‍👩‍👧‍👦 Familiäre Situation',
    '🏢 Karriere-Ambitionen',
    '⚡ Kommunikationsstil',
    '🎁 Geschenkideen',
    '💬 Gesprächsthemen'
  ];
  
  return (
    <Box>
      <TextField
        label="Persönliche Notizen"
        multiline
        rows={6}
        fullWidth
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="z.B. Bevorzugt Telefonate am Vormittag, Vegetarier, FC Bayern Fan..."
        helperText="Diese Notizen helfen bei der persönlichen Ansprache"
        InputProps={{
          endAdornment: (
            <IconButton
              onClick={() => setShowSuggestions(!showSuggestions)}
              size="small"
            >
              <LightbulbIcon />
            </IconButton>
          )
        }}
      />
      
      <Collapse in={showSuggestions}>
        <Box mt={1} p={1} bgcolor="action.hover" borderRadius={1}>
          <Typography variant="caption" color="text.secondary" gutterBottom>
            Was könnten Sie notieren?
          </Typography>
          <Box display="flex" gap={0.5} flexWrap="wrap" mt={1}>
            {noteSuggestions.map(suggestion => (
              <Chip
                key={suggestion}
                label={suggestion}
                size="small"
                variant="outlined"
                onClick={() => {
                  onChange(value + (value ? '\n' : '') + suggestion + ': ');
                }}
                sx={{ cursor: 'pointer' }}
              />
            ))}
          </Box>
        </Box>
      </Collapse>
    </Box>
  );
};
```

### Birthday Integration

```typescript
// hooks/useBirthdayIntegration.ts
export const useBirthdayIntegration = (contact: Contact) => {
  const { warmth, updateWarmthScore } = useWarmthStore();
  
  useEffect(() => {
    if (contact.birthday) {
      const daysUntilBirthday = getDaysUntilBirthday(contact.birthday);
      
      // Boost warmth score if birthday is approaching
      if (daysUntilBirthday <= 7 && daysUntilBirthday >= 0) {
        updateWarmthScore(contact.id, {
          birthdayBoost: 10,
          reason: 'Geburtstag steht bevor'
        });
      }
    }
  }, [contact.birthday]);
  
  const birthdayActions = useMemo(() => {
    if (!contact.birthday) return [];
    
    const days = getDaysUntilBirthday(contact.birthday);
    if (days > 7 || days < 0) return [];
    
    return [
      {
        type: 'birthday_greeting',
        priority: days === 0 ? 'urgent' : 'high',
        label: days === 0 ? 'Jetzt gratulieren!' : `In ${days} Tagen gratulieren`,
        icon: <CakeIcon />
      }
    ];
  }, [contact.birthday]);
  
  return { birthdayActions };
};
```

## 🔗 Integration Points

### With Contact Cards:
- Warmth badge in card header
- Suggestions in expandable section
- Color-coded contact borders
- Hobby chips display

### With Mobile Actions:
- Priority ordering by warmth
- Quick action suggestions
- Notification system for cooling contacts
- Birthday reminder push notifications

### With Smart Suggestions:
- Birthday reminders integration
- Personal notes analysis for suggestions
- Hobby-based conversation starters

---

**Nächster Schritt:** [→ Contact Timeline Implementation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/CONTACT_TIMELINE.md)

**Implementierung:** Phase 2, Tag 1 - Relationship Intelligence mit persönlicher Note! 🌡️🔥💝