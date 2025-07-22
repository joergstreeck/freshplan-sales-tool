# 📘 M4 IMPLEMENTATION GUIDE

**Zweck:** Opportunity Pipeline mit Geführter Freiheit  
**Fokus:** Hybride UX zwischen Struktur und Flexibilität  

---

## <a id="guided-freedom"></a>🎯 Geführte Freiheit Konzept

### Die 3 Säulen:

#### 1. Strukturierte Menüführung
```typescript
// Navigation im Hauptmenü
const menuItems = [
  {
    id: 'opportunities',
    label: 'Opportunities',
    icon: <TrendingUpIcon />,
    subItems: [
      { id: 'board', label: 'Pipeline Board' },
      { id: 'list', label: 'Alle Opportunities' },
      { id: 'my-deals', label: 'Meine Deals' },
      { id: 'team-deals', label: 'Team Deals' }
    ]
  }
];
```

#### 2. Kontextuelle Action Buttons
```typescript
// Direkt auf der Opportunity Card
const OpportunityCard = ({ opportunity }) => {
  return (
    <Card>
      <CardContent>
        <Typography>{opportunity.title}</Typography>
        <Typography>{opportunity.value}€</Typography>
      </CardContent>
      <CardActions>
        {/* Primäre Aktion je nach Stage */}
        {opportunity.stage === 'QUALIFIED' && (
          <Button 
            color="primary" 
            startIcon={<DescriptionIcon />}
            onClick={handleCreateProposal}
          >
            Angebot erstellen
          </Button>
        )}
        
        {/* Sekundäre Aktionen */}
        <IconButton onClick={handleEdit}>
          <EditIcon />
        </IconButton>
        <IconButton onClick={handleComment}>
          <CommentIcon />
        </IconButton>
      </CardActions>
    </Card>
  );
};
```

#### 3. Guided Process Flow
```typescript
// System schlägt nächste Schritte vor
const NextStepSuggestion = ({ opportunity }) => {
  const suggestions = {
    LEAD: {
      text: "Qualifizierungsgespräch vereinbaren",
      action: () => openScheduler(),
      icon: <PhoneIcon />
    },
    QUALIFIED: {
      text: "Angebot mit Calculator erstellen",
      action: () => openCalculatorModal(opportunity),
      icon: <CalculateIcon />
    },
    PROPOSAL: {
      text: "Follow-up Email senden",
      action: () => openEmailTemplate(opportunity),
      icon: <EmailIcon />
    }
  };
  
  const suggestion = suggestions[opportunity.stage];
  
  return (
    <Alert severity="info" action={
      <Button color="inherit" size="small" onClick={suggestion.action}>
        {suggestion.text}
      </Button>
    }>
      <AlertTitle>Nächster Schritt</AlertTitle>
      {suggestion.text}
    </Alert>
  );
};
```

---

## <a id="entity-code"></a>💾 Backend Entity

### Opportunity Entity mit Guided Fields
```java
@Entity
@Table(name = "opportunities")
public class Opportunity extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    // Basis-Daten
    @Column(nullable = false)
    public String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;
    
    @Column(nullable = false)
    public BigDecimal value;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public OpportunityStage stage = OpportunityStage.LEAD;
    
    // Guided Process Fields
    @Column(name = "next_action")
    public String nextAction;
    
    @Column(name = "next_action_date")
    public LocalDate nextActionDate;
    
    @Column(name = "stage_entered_at")
    public LocalDateTime stageEnteredAt;
    
    @Column(name = "days_in_stage")
    public Integer daysInStage;
    
    // Team & Ownership
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    public User owner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    public Team team;
    
    // Tracking
    @Column(name = "probability_percent")
    public Integer probabilityPercent;
    
    @Column(name = "expected_close_date")
    public LocalDate expectedCloseDate;
    
    @Column(name = "lost_reason")
    public String lostReason;
    
    @PreUpdate
    public void calculateDaysInStage() {
        if (stageEnteredAt != null) {
            daysInStage = (int) ChronoUnit.DAYS.between(
                stageEnteredAt.toLocalDate(), 
                LocalDate.now()
            );
        }
    }
}
```

### Stage Enum mit Guided Metadata
```java
public enum OpportunityStage {
    LEAD(10, "Erster Kontakt", "Qualifizierungsgespräch vereinbaren"),
    QUALIFIED(25, "Qualifiziert", "Angebot erstellen"),
    PROPOSAL(50, "Angebot", "Nachfassen"),
    NEGOTIATION(75, "Verhandlung", "Entscheidung herbeiführen"),
    CLOSED_WON(100, "Gewonnen", "Onboarding starten"),
    CLOSED_LOST(0, "Verloren", "Analyse durchführen");
    
    private final int probability;
    private final String displayName;
    private final String suggestedAction;
    
    OpportunityStage(int probability, String displayName, String suggestedAction) {
        this.probability = probability;
        this.displayName = displayName;
        this.suggestedAction = suggestedAction;
    }
}
```

---

## <a id="kanban-ui"></a>🎨 Kanban Board UI

### Board mit Guided Elements
```typescript
const OpportunityBoard: React.FC = () => {
  const [showGuidance, setShowGuidance] = useState(true);
  
  return (
    <Box>
      {/* Header mit Toggle für Guided Mode */}
      <Box display="flex" justifyContent="space-between" mb={2}>
        <Typography variant="h4">Opportunity Pipeline</Typography>
        <FormControlLabel
          control={
            <Switch 
              checked={showGuidance} 
              onChange={(e) => setShowGuidance(e.target.checked)}
            />
          }
          label="Geführter Modus"
        />
      </Box>
      
      {/* Quick Actions Bar */}
      <Paper sx={{ p: 2, mb: 2 }}>
        <Stack direction="row" spacing={2}>
          <Button 
            variant="contained" 
            startIcon={<AddIcon />}
            onClick={() => openQuickCreate('opportunity')}
          >
            Neue Opportunity
          </Button>
          <Button 
            variant="outlined"
            startIcon={<FilterListIcon />}
          >
            Filter
          </Button>
          <Button 
            variant="outlined"
            startIcon={<DownloadIcon />}
          >
            Export
          </Button>
        </Stack>
      </Paper>
      
      {/* Kanban Board */}
      <DragDropContext onDragEnd={handleDragEnd}>
        <Box display="flex" gap={2} overflow="auto">
          {stages.map(stage => (
            <StageColumn 
              key={stage.id} 
              stage={stage}
              showGuidance={showGuidance}
            />
          ))}
        </Box>
      </DragDropContext>
      
      {/* Floating Action Button für Mobile */}
      <Fab 
        color="primary" 
        sx={{ position: 'fixed', bottom: 16, right: 16 }}
        onClick={() => openQuickCreate('opportunity')}
      >
        <AddIcon />
      </Fab>
    </Box>
  );
};
```

### Stage Column mit Guidance
```typescript
const StageColumn: React.FC<{stage: Stage, showGuidance: boolean}> = ({ 
  stage, 
  showGuidance 
}) => {
  const opportunities = useOpportunities(stage.id);
  const avgDaysInStage = useAverageTimeInStage(stage.id);
  
  return (
    <Paper sx={{ minWidth: 300, p: 2 }}>
      {/* Stage Header mit Metriken */}
      <Box mb={2}>
        <Typography variant="h6">{stage.name}</Typography>
        <Typography variant="body2" color="text.secondary">
          {opportunities.length} Deals · {formatCurrency(stage.totalValue)}
        </Typography>
        {showGuidance && avgDaysInStage > stage.targetDays && (
          <Alert severity="warning" sx={{ mt: 1 }}>
            Ø {avgDaysInStage} Tage (Ziel: {stage.targetDays})
          </Alert>
        )}
      </Box>
      
      {/* Opportunities */}
      <Droppable droppableId={stage.id}>
        {(provided) => (
          <Box 
            ref={provided.innerRef} 
            {...provided.droppableProps}
            sx={{ minHeight: 100 }}
          >
            {opportunities.map((opp, index) => (
              <Draggable key={opp.id} draggableId={opp.id} index={index}>
                {(provided) => (
                  <Box
                    ref={provided.innerRef}
                    {...provided.draggableProps}
                    {...provided.dragHandleProps}
                    sx={{ mb: 1 }}
                  >
                    <OpportunityCard 
                      opportunity={opp} 
                      showGuidance={showGuidance}
                    />
                  </Box>
                )}
              </Draggable>
            ))}
            {provided.placeholder}
          </Box>
        )}
      </Droppable>
      
      {/* Stage-spezifische Quick Action */}
      {showGuidance && (
        <Button
          fullWidth
          variant="outlined"
          startIcon={stage.quickActionIcon}
          onClick={() => handleStageAction(stage)}
          sx={{ mt: 2 }}
        >
          {stage.quickActionLabel}
        </Button>
      )}
    </Paper>
  );
};
```

---

## <a id="stage-rules"></a>📋 Stage Transition Rules

### Validierung bei Stage-Wechsel
```java
@ApplicationScoped
public class OpportunityStageService {
    
    public void moveToStage(Opportunity opp, OpportunityStage newStage) {
        // Validiere erlaubte Übergänge
        validateTransition(opp.stage, newStage);
        
        // Stage-spezifische Validierung
        switch (newStage) {
            case QUALIFIED:
                validateQualification(opp);
                break;
            case PROPOSAL:
                validateProposalReadiness(opp);
                break;
            case CLOSED_WON:
                validateClosingRequirements(opp);
                break;
        }
        
        // Update Opportunity
        opp.stage = newStage;
        opp.stageEnteredAt = LocalDateTime.now();
        opp.probabilityPercent = newStage.getProbability();
        
        // Trigger Guided Actions
        opp.nextAction = newStage.getSuggestedAction();
        opp.nextActionDate = calculateNextActionDate(newStage);
        
        // Event für andere Systeme
        eventBus.publish(new OpportunityStageChanged(opp));
    }
    
    private void validateQualification(Opportunity opp) {
        if (opp.customer.getContactPerson() == null) {
            throw new ValidationException("Ansprechpartner fehlt");
        }
        if (opp.value == null || opp.value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Opportunity-Wert muss definiert sein");
        }
    }
}
```

---

## <a id="team-features"></a>👥 Team Collaboration

### Opportunity Sharing
```typescript
const ShareOpportunityDialog: React.FC = ({ opportunity, open, onClose }) => {
  const [selectedUsers, setSelectedUsers] = useState<string[]>([]);
  const [shareType, setShareType] = useState<'view' | 'edit'>('view');
  const [note, setNote] = useState('');
  
  const handleShare = async () => {
    await api.shareOpportunity(opportunity.id, {
      userIds: selectedUsers,
      permission: shareType,
      note
    });
    
    // Notification an geteilte User
    selectedUsers.forEach(userId => {
      notify(userId, `${currentUser.name} hat eine Opportunity mit dir geteilt`);
    });
    
    onClose();
  };
  
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Opportunity teilen</DialogTitle>
      <DialogContent>
        <Alert severity="info" sx={{ mb: 2 }}>
          Geteilte Opportunities erscheinen im "Team Deals" Bereich
        </Alert>
        
        <UserSelector
          value={selectedUsers}
          onChange={setSelectedUsers}
          multiple
          label="Mit wem teilen?"
        />
        
        <RadioGroup value={shareType} onChange={(e) => setShareType(e.target.value)}>
          <FormControlLabel value="view" control={<Radio />} label="Nur ansehen" />
          <FormControlLabel value="edit" control={<Radio />} label="Bearbeiten erlauben" />
        </RadioGroup>
        
        <TextField
          fullWidth
          multiline
          rows={3}
          label="Notiz (optional)"
          value={note}
          onChange={(e) => setNote(e.target.value)}
          placeholder="Warum teilst du diese Opportunity?"
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button onClick={handleShare} variant="contained">
          Teilen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
```

---

## <a id="forecast-logic"></a>📊 Forecast Calculation

### Weighted Pipeline Value
```java
@ApplicationScoped
public class ForecastService {
    
    public ForecastDTO calculateForecast(List<Opportunity> opportunities) {
        var forecast = new ForecastDTO();
        
        // Gruppiere nach Stage
        var byStage = opportunities.stream()
            .collect(Collectors.groupingBy(Opportunity::getStage));
        
        // Berechne weighted values
        byStage.forEach((stage, opps) -> {
            var stageTotal = opps.stream()
                .map(o -> o.value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            var weightedValue = stageTotal
                .multiply(BigDecimal.valueOf(stage.getProbability()))
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP);
            
            forecast.addStageValue(stage, stageTotal, weightedValue);
        });
        
        // Best/Worst Case
        forecast.bestCase = calculateBestCase(opportunities);
        forecast.worstCase = calculateCommittedOnly(opportunities);
        forecast.mostLikely = forecast.getWeightedTotal();
        
        return forecast;
    }
}
```