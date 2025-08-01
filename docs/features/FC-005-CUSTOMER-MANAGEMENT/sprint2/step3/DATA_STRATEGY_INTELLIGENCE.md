# 📊 Datenstrategie für Intelligenz-Features

**Erstellt:** 01.08.2025  
**Status:** 🆕 Konzept  
**Priorität:** HIGH - Kritisch für alle Intelligence Features  

## 🧭 Navigation

**← Zurück:** [Step 3 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  
**→ Nächstes:** [Offline Conflict Resolution](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/OFFLINE_CONFLICT_RESOLUTION.md)  
**↗ Verbunden:** [Relationship Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/RELATIONSHIP_INTELLIGENCE.md)  

## 🎯 Problem: Kaltstart ohne historische Daten

**Die Herausforderung:**
- Warmth Score braucht Interaktions-Historie
- Timeline benötigt vergangene Events
- Smart Suggestions basieren auf Mustern
- **Ohne Daten = Keine Intelligenz**

## 💡 Lösung: Kaltstart-Strategie mit schnellem Lernen

### 1. Default-Annahmen für neue Kontakte

```typescript
interface ColdStartDefaults {
  warmthScore: 50;           // Neutral starten
  expectedInteractionDays: 30; // Branchenstandard
  suggestedActions: [
    "Willkommens-Email senden",
    "Kennenlern-Termin vereinbaren",
    "Produktportfolio vorstellen"
  ];
}
```

### 2. Bestandsdaten-Migration mit NLP

```java
@Component
public class LegacyDataMigrationService {
    
    @Inject
    NLPService nlpService;
    
    public ContactIntelligence migrateFromNotes(String legacyNotes) {
        // Extract sentiments
        SentimentAnalysis sentiment = nlpService.analyzeSentiment(legacyNotes);
        
        // Extract dates and events
        List<TemporalEvent> events = nlpService.extractTemporalEvents(legacyNotes);
        
        // Extract relationship indicators
        RelationshipIndicators indicators = nlpService.extractRelationshipData(legacyNotes);
        
        return ContactIntelligence.builder()
            .initialWarmth(calculateWarmthFromSentiment(sentiment))
            .historicalEvents(events)
            .relationshipStrength(indicators.getStrength())
            .build();
    }
}
```

### 3. Schnelles Lernen durch explizites Feedback

```typescript
// UI Component für Warmth-Kalibrierung
export const WarmthCalibration: React.FC<{contact: Contact}> = ({contact}) => {
  return (
    <CalibrationCard>
      <Typography variant="subtitle2">
        Wie würden Sie die Beziehung einschätzen?
      </Typography>
      <WarmthSlider
        defaultValue={contact.warmthScore}
        onChangeCommitted={(value) => {
          // Explizites Feedback für schnelleres Lernen
          api.calibrateWarmth(contact.id, value);
        }}
        marks={[
          {value: 0, label: "Kalt 🧊"},
          {value: 50, label: "Neutral 😐"},
          {value: 100, label: "Heiß 🔥"}
        ]}
      />
    </CalibrationCard>
  );
};
```

## 📈 Datensammlung ab Tag 1

### Automatische Event-Erfassung

```java
@Entity
@Table(name = "contact_interactions")
public class ContactInteraction {
    @Id
    private UUID id;
    
    @ManyToOne
    private Contact contact;
    
    @Enumerated(EnumType.STRING)
    private InteractionType type; // EMAIL, CALL, MEETING, NOTE
    
    private LocalDateTime timestamp;
    
    @Column(name = "sentiment_score")
    private Double sentimentScore; // -1.0 bis +1.0
    
    @Column(name = "engagement_score")
    private Integer engagementScore; // 0-100
    
    // Auto-captured metadata
    @Column(name = "response_time_minutes")
    private Integer responseTime;
    
    @Column(name = "word_count")
    private Integer wordCount;
    
    @Column(name = "initiated_by")
    private String initiatedBy; // CUSTOMER, SALES, SYSTEM
}
```

### Progressive Datenqualität

```typescript
interface DataQualityLevels {
  BOOTSTRAP: {
    duration: "0-7 Tage",
    features: ["Basic CRUD", "Manual Notes"],
    accuracy: "Sehr niedrig"
  },
  LEARNING: {
    duration: "7-30 Tage", 
    features: ["Erste Warmth Trends", "Simple Suggestions"],
    accuracy: "Mittel"
  },
  INTELLIGENT: {
    duration: "30+ Tage",
    features: ["Predictive Analytics", "Smart Recommendations"],
    accuracy: "Hoch"
  }
}
```

## 🔄 Migration bestehender Systeme

### 1. CRM-Import Adapter

```java
@Component
public class CRMImportAdapter {
    
    public List<ContactInteraction> importFromSalesforce(String accountId) {
        // Map Salesforce Activities zu unseren Interactions
        return salesforceClient.getActivities(accountId)
            .stream()
            .map(this::mapToInteraction)
            .collect(Collectors.toList());
    }
    
    public List<ContactInteraction> importFromHubspot(String contactId) {
        // Map Hubspot Timeline zu unseren Interactions
        return hubspotClient.getTimeline(contactId)
            .stream()
            .map(this::mapToInteraction)
            .collect(Collectors.toList());
    }
}
```

### 2. Email-Integration für Historie

```typescript
// Outlook/Gmail Integration
export const EmailHistoryImporter = {
  async importLast90Days(contactEmail: string) {
    const emails = await emailClient.search({
      from: contactEmail,
      to: contactEmail,
      dateRange: "last90days"
    });
    
    return emails.map(email => ({
      type: "EMAIL",
      timestamp: email.date,
      sentimentScore: analyzeSentiment(email.content),
      responseTime: calculateResponseTime(email),
      wordCount: email.content.split(" ").length,
      initiatedBy: determineInitiator(email)
    }));
  }
};
```

## 📊 Metriken für Datenqualität

### Dashboard für Data Health

```typescript
interface DataHealthMetrics {
  contact: {
    totalContacts: number;
    contactsWithInteractions: number;
    averageInteractionsPerContact: number;
    dataCompletenessScore: number; // 0-100%
  };
  
  intelligence: {
    contactsWithWarmthScore: number;
    warmthScoreConfidence: number; // 0-100%
    suggestionsAcceptanceRate: number;
    predictionAccuracy: number;
  };
  
  recommendations: {
    showDataCollectionHints: boolean;
    criticalDataGaps: string[];
    improvementSuggestions: string[];
  };
}
```

## 🔄 Kontinuierliche Datenhygiene (NEU)

### Automatische Datenalterung-Erkennung

```typescript
export const DataFreshnessIndicator: React.FC<{contact: Contact}> = ({contact}) => {
  const daysSinceUpdate = getDaysSinceLastUpdate(contact);
  const freshnessLevel = getDataFreshnessLevel(daysSinceUpdate);
  
  if (freshnessLevel === 'fresh') return null;
  
  return (
    <Alert 
      severity={freshnessLevel === 'stale' ? 'warning' : 'error'}
      action={
        <Button size="small" onClick={() => openUpdateWizard(contact)}>
          Jetzt aktualisieren
        </Button>
      }
    >
      <AlertTitle>
        {freshnessLevel === 'stale' 
          ? 'Daten könnten veraltet sein'
          : 'Dringend: Daten sind über ein Jahr alt'}
      </AlertTitle>
      Dieser Kontakt wurde seit {daysSinceUpdate} Tagen nicht aktualisiert. 
      Bitte überprüfen Sie die Daten.
    </Alert>
  );
};

// Freshness Levels
interface DataFreshnessLevels {
  fresh: "< 90 Tage",      // Grün - Alles gut
  aging: "90-180 Tage",    // Gelb - Hinweis anzeigen
  stale: "180-365 Tage",   // Orange - Update empfohlen
  critical: "> 365 Tage"   // Rot - Dringender Handlungsbedarf
}
```

### Proaktive Update-Kampagnen

```java
@Component
public class DataHygieneService {
    
    @Scheduled(cron = "0 0 9 * * MON") // Jeden Montag 9 Uhr
    public void checkDataFreshness() {
        List<Contact> staleContacts = contactRepository
            .findByLastUpdatedBefore(LocalDate.now().minusMonths(6));
            
        // Gruppiere nach verantwortlichem Sales Rep
        Map<User, List<Contact>> contactsByRep = groupByResponsibleUser(staleContacts);
        
        // Sende personalisierte Update-Reminder
        contactsByRep.forEach((user, contacts) -> {
            notificationService.sendDataUpdateReminder(user, contacts);
            
            // Erstelle Data Hygiene Task
            taskService.createTask(
                "Kontaktdaten aktualisieren",
                "Bitte überprüfen Sie " + contacts.size() + " Kontakte",
                user,
                Priority.MEDIUM,
                contacts.stream().map(Contact::getId).collect(Collectors.toList())
            );
        });
    }
    
    // Automatische Validierung bei veralteten Daten
    public DataQualityScore validateDataQuality(Contact contact) {
        int score = 100;
        LocalDate lastUpdate = contact.getUpdatedAt().toLocalDate();
        long daysSinceUpdate = ChronoUnit.DAYS.between(lastUpdate, LocalDate.now());
        
        // Progressive Punktabzüge
        if (daysSinceUpdate > 365) score -= 40;  // Über 1 Jahr
        else if (daysSinceUpdate > 180) score -= 20;  // Über 6 Monate
        else if (daysSinceUpdate > 90) score -= 10;   // Über 3 Monate
        
        // Weitere Qualitätskriterien
        if (contact.getEmail() == null) score -= 15;
        if (contact.getPhone() == null && contact.getMobile() == null) score -= 10;
        if (contact.getPosition() == null) score -= 5;
        
        return new DataQualityScore(score, generateRecommendations(contact, score));
    }
}
```

### Data Hygiene Dashboard

```typescript
export const DataHygieneDashboard: React.FC = () => {
  const metrics = useDataHygieneMetrics();
  
  return (
    <Grid container spacing={3}>
      <Grid item xs={12} md={4}>
        <MetricCard
          title="Datenqualität Gesamt"
          value={`${metrics.overallQuality}%`}
          subtitle="Durchschnittlicher Score"
          trend={metrics.qualityTrend}
          color={getQualityColor(metrics.overallQuality)}
        />
      </Grid>
      
      <Grid item xs={12} md={8}>
        <Card>
          <CardContent>
            <Typography variant="h6">Kontakte nach Aktualität</Typography>
            <DataFreshnessChart 
              fresh={metrics.freshContacts}
              aging={metrics.agingContacts}
              stale={metrics.staleContacts}
              critical={metrics.criticalContacts}
            />
            
            {metrics.criticalContacts > 0 && (
              <Alert severity="error" sx={{mt: 2}}>
                <AlertTitle>Handlungsbedarf</AlertTitle>
                {metrics.criticalContacts} Kontakte wurden über ein Jahr nicht aktualisiert.
                <Button 
                  size="small" 
                  onClick={() => navigateTo('/contacts?filter=needs-update')}
                >
                  Jetzt überprüfen
                </Button>
              </Alert>
            )}
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};
```

## 🚀 Implementierungs-Roadmap

### Phase 1: Basis-Datensammlung (Woche 1)
- [ ] Interaction Entity implementieren
- [ ] Event-Capturing für alle User-Actions
- [ ] Basis-Metriken Dashboard
- [ ] **NEU:** Data Freshness Tracking einbauen

### Phase 2: Import & Migration (Woche 2)  
- [ ] Legacy Notes NLP Parser
- [ ] CRM Import Adapter
- [ ] Email History Integration
- [ ] **NEU:** Initiale Datenqualitäts-Bewertung

### Phase 3: Intelligenz-Aktivierung (Woche 3)
- [ ] Warmth Score Kalibrierung UI
- [ ] Confidence Indicators
- [ ] Progressive Feature Activation
- [ ] **NEU:** Data Hygiene Dashboard
- [ ] **NEU:** Automatische Update-Reminder

## 💡 Best Practices

### 1. Transparenz über Datenqualität

```typescript
// Immer Confidence anzeigen
<WarmthIndicator
  score={contact.warmthScore}
  confidence={contact.warmthConfidence}
  tooltip={
    contact.warmthConfidence < 50 
      ? "Noch zu wenig Daten - Score wird präziser mit mehr Interaktionen"
      : "Basiert auf " + contact.interactionCount + " Interaktionen"
  }
/>
```

### 2. Explizite Datensammlung fördern

```typescript
// Gamification für Datenqualität
<DataQualityBadge
  level={getDataQualityLevel(contact)}
  nextLevel={{
    requirement: "Noch 3 Interaktionen bis 'Gold' Status",
    benefits: ["Präzisere Vorschläge", "Trend-Analysen"]
  }}
/>
```

### 3. Privacy by Design

```java
@EventListener
public void onDataCollection(DataCollectionEvent event) {
    // Audit Log für DSGVO
    auditService.log(
        "DATA_COLLECTED",
        event.getContactId(),
        event.getDataType(),
        event.getPurpose()
    );
    
    // Consent Check
    if (!consentService.hasConsent(event.getContactId(), event.getDataType())) {
        throw new ConsentRequiredException();
    }
}
```

## 🔗 Verwandte Dokumente

- [Relationship Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/RELATIONSHIP_INTELLIGENCE.md) - Nutzt diese Daten
- [Contact Timeline](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/CONTACT_TIMELINE.md) - Visualisiert die Historie
- [DSGVO Consent](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/DSGVO_CONSENT.md) - Rechtliche Grundlage

---

**Nächster Schritt:** [→ Offline Conflict Resolution UX](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/OFFLINE_CONFLICT_RESOLUTION.md)