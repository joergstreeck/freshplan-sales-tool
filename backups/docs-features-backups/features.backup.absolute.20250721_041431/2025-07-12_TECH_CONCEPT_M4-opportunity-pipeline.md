# 🚀 Technisches Konzept: M4 - Opportunity Pipeline

**Feature Code:** FC-002-M4  
**Titel:** Opportunity Pipeline - Das Herzstück des prozessorientierten Vertriebs  
**Erstellt:** 12.07.2025  
**Status:** 📋 Technisches Konzept  
**Geschätzter Aufwand:** 4.5 Tage  

## 📋 Executive Summary

Die Opportunity Pipeline ist das zentrale Element unserer prozessorientierten Architektur. Sie visualisiert und steuert den gesamten Vertriebsprozess von der Lead-Generierung bis zum Abschluss. Jede Opportunity durchläuft definierte Stages, die kontextabhängige Aktionen und Tools triggern - wie z.B. den Calculator bei "Angebotserstellung".

## 🎯 Geschäftsziele

### Primäre Ziele:
1. **Transparenz:** Vollständige Sichtbarkeit aller Verkaufschancen
2. **Prozess-Steuerung:** Geführte Workflows je nach Pipeline-Stage
3. **Tool-Integration:** Kontextabhängige Verfügbarkeit von Tools (Calculator, E-Mail-Templates)
4. **Team-Kollaboration:** Mehrere Personen können an einer Opportunity arbeiten

### Business Value:
- **Höhere Abschlussquote** durch strukturierten Prozess
- **Kürzere Sales-Cycles** durch geführte Next-Best-Actions
- **Bessere Forecasts** durch transparente Pipeline-Daten
- **Weniger verlorene Leads** durch systematisches Follow-up

## 🏗️ Technische Architektur

### Frontend-Architektur

```typescript
// Komponenten-Struktur
frontend/src/features/opportunity/
├── components/
│   ├── OpportunityPipeline.tsx      // Haupt-Container
│   ├── PipelineStage.tsx            // Einzelne Stage-Spalte
│   ├── OpportunityCard.tsx          // Opportunity-Karte
│   ├── OpportunityDetail.tsx        // Detail-Ansicht (Modal/Drawer)
│   └── StageActions.tsx             // Kontextuelle Aktionen
├── hooks/
│   ├── useOpportunities.ts          // Data fetching & mutations
│   ├── usePipelineFilters.ts       // Filter-Logik
│   └── useDragAndDrop.ts           // Drag & Drop zwischen Stages
├── services/
│   └── opportunityApi.ts            // API-Client
├── types/
│   └── opportunity.types.ts         // TypeScript Definitionen
└── store/
    └── opportunityStore.ts          // Zustand (Zustand/Context)
```

### Pipeline-Stages Definition

```typescript
enum OpportunityStage {
  NEW_LEAD = "new_lead",                    // Neuer Lead
  QUALIFICATION = "qualification",          // Qualifizierung
  NEEDS_ANALYSIS = "needs_analysis",       // Bedarfsanalyse
  PROPOSAL = "proposal",                   // Angebotserstellung ⭐
  NEGOTIATION = "negotiation",             // Verhandlung
  CLOSED_WON = "closed_won",              // Gewonnen
  CLOSED_LOST = "closed_lost"             // Verloren
}

interface StageConfig {
  stage: OpportunityStage;
  label: string;
  color: string;                          // Freshfoodz CI Farben
  allowedActions: ActionType[];           // Verfügbare Aktionen
  requiredFields: string[];               // Pflichtfelder für nächste Stage
  autoTriggers?: TriggerConfig[];         // Automatische Aktionen
}
```

### Backend-Architektur

```java
// Entity-Modell
@Entity
@Table(name = "opportunities")
public class Opportunity {
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityStage stage;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;        // Optional bei Leads
    
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;         // Verkäufer-Zuweisung
    
    @Column(precision = 19, scale = 2)
    private BigDecimal expectedValue;
    
    @Column
    private LocalDate expectedCloseDate;
    
    @Column
    private Integer probability;      // 0-100%
    
    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL)
    private List<OpportunityActivity> activities;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime stageChangedAt;
}
```

### API-Endpoints

```yaml
# Opportunity CRUD
GET    /api/opportunities                 # Liste mit Filtern
GET    /api/opportunities/{id}           # Details
POST   /api/opportunities                # Neue Opportunity
PUT    /api/opportunities/{id}           # Update
DELETE /api/opportunities/{id}           # Löschen (Soft-Delete)

# Pipeline-spezifisch
GET    /api/opportunities/pipeline       # Gruppiert nach Stages
PUT    /api/opportunities/{id}/stage     # Stage ändern
GET    /api/opportunities/{id}/actions   # Verfügbare Aktionen
POST   /api/opportunities/{id}/execute   # Aktion ausführen

# Metriken
GET    /api/opportunities/metrics        # Pipeline-Metriken
GET    /api/opportunities/forecast       # Umsatz-Forecast
```

### Stage-spezifische Aktionen

```typescript
// Beispiel: Aktionen für Stage "Angebotserstellung"
const proposalStageActions: ActionConfig[] = [
  {
    id: "open_calculator",
    label: "Angebot kalkulieren",
    icon: "calculate",
    component: "CalculatorModal",     // Modal statt Route!
    requiredPermissions: ["calculator.use"],
    preConditions: {
      customerExists: true,
      contactPersonExists: true
    }
  },
  {
    id: "send_proposal_email",
    label: "Angebot per E-Mail senden",
    icon: "email",
    component: "EmailComposer",
    template: "proposal_template",
    requiredPermissions: ["email.send"]
  },
  {
    id: "schedule_followup",
    label: "Nachfass-Termin planen",
    icon: "calendar",
    component: "AppointmentScheduler"
  }
];
```

## 💡 Kern-Features

### 1. Drag & Drop zwischen Stages

```typescript
// React DnD Implementation
const OpportunityCard: React.FC<OpportunityCardProps> = ({ opportunity }) => {
  const [{ isDragging }, drag] = useDrag({
    type: 'opportunity',
    item: { id: opportunity.id, currentStage: opportunity.stage },
    collect: (monitor) => ({
      isDragging: monitor.isDragging()
    })
  });

  return (
    <Card 
      ref={drag} 
      sx={{ 
        opacity: isDragging ? 0.5 : 1,
        cursor: 'move',
        bgcolor: 'white',
        borderLeft: `4px solid ${stageColors[opportunity.stage]}`
      }}
    >
      {/* Card Content */}
    </Card>
  );
};
```

### 2. Automatische Validierung beim Stage-Wechsel

```java
@Service
public class OpportunityStageValidator {
    
    public ValidationResult canMoveToStage(
        Opportunity opp, 
        OpportunityStage targetStage
    ) {
        var rules = stageRules.get(targetStage);
        var errors = new ArrayList<String>();
        
        // Beispiel: Angebotserstellung requires Customer
        if (targetStage == PROPOSAL && opp.getCustomer() == null) {
            errors.add("Kunde muss zugeordnet sein");
        }
        
        // Beispiel: Probability muss gesetzt sein
        if (targetStage.ordinal() >= NEEDS_ANALYSIS.ordinal() 
            && opp.getProbability() == null) {
            errors.add("Abschlusswahrscheinlichkeit fehlt");
        }
        
        return ValidationResult.of(errors);
    }
}
```

### 3. Smart Notifications

```typescript
// Beispiel: Erinnerung bei stagnierender Opportunity
const stagnationRules = {
  [OpportunityStage.QUALIFICATION]: 7,    // Tage
  [OpportunityStage.PROPOSAL]: 14,
  [OpportunityStage.NEGOTIATION]: 7
};

// Backend Scheduled Job
@Scheduled(cron = "0 0 9 * * *") // Täglich 9 Uhr
public void checkStagnantOpportunities() {
    opportunityRepository
        .findStagnantOpportunities()
        .forEach(opp -> {
            notificationService.notify(
                opp.getAssignedTo(),
                "Opportunity '" + opp.getName() + "' wartet auf Aktion",
                NotificationType.WARNING
            );
        });
}
```

### 4. Integration mit Calculator (Modal)

```typescript
// Trigger Calculator from Opportunity Context
const handleCalculatorOpen = () => {
  setCalculatorModalOpen(true);
  setCalculatorContext({
    opportunity: currentOpportunity,
    customer: currentOpportunity.customer,
    prefilledData: {
      customerName: currentOpportunity.customer?.name,
      eventDate: currentOpportunity.expectedCloseDate,
      // Weitere Kontextdaten
    }
  });
};

// Calculator Modal erhält Kontext
<CalculatorModal
  open={calculatorModalOpen}
  onClose={handleCalculatorClose}
  context={calculatorContext}
  onComplete={(result) => {
    // Ergebnis zur Opportunity hinzufügen
    updateOpportunity({
      ...currentOpportunity,
      expectedValue: result.totalAmount,
      calculationId: result.id
    });
  }}
/>
```

## 🎨 UI/UX Design

### Pipeline-Ansicht (Kanban-Style)

```
┌─────────────────────────────────────────────────────────────────┐
│ 🔍 Suche...  [Filter ▼] [Nur meine] [Diese Woche] [+ Opportunity]│
├─────────────────────────────────────────────────────────────────┤
│ Neu (3)       Qualif. (5)    Analyse (2)    Angebot (4)    ... │
│ ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐         │
│ │ Hotel    │   │ Kantine │   │ Catering│   │ Event   │         │
│ │ Adler    │   │ Siemens │   │ f. BMW  │   │ Hochzeit│         │
│ │ €15.000  │   │ €45.000 │   │ €8.500  │   │ €12.000 │         │
│ │ 60%      │──>│ 40%     │   │ 70%     │   │ 90% ⚡  │         │
│ │ T.Schmidt│   │ M.Weber │   │ K.Müller│   │ T.Schmidt│        │
│ └─────────┘   └─────────┘   └─────────┘   └─────────┘         │
│      ⬇              ⬇              ⬇              ⬇              │
│ [+ Neu]       [+ Neu]       [+ Neu]       [🧮 Rechner]          │
└─────────────────────────────────────────────────────────────────┘

Legende: ⚡ = Aktion erforderlich, 🧮 = Tool verfügbar
```

### Opportunity-Detail (Modal/Drawer)

```
┌─ Opportunity: Catering für BMW Sommerfest ─────────────────────┐
│ Status: Angebotserstellung     [In Verhandlung ▼]              │
├─────────────────────────────────────────────────────────────────┤
│ 📊 Key Facts              │ 🎯 Nächste Schritte                │
│ • Wert: €8.500           │ □ Angebot kalkulieren [🧮]         │
│ • Wahrsch.: 70%          │ □ Angebot versenden                │
│ • Abschluss: 15.07.2025  │ □ Nachfass-Termin                  │
│ • Zuständig: K. Müller   │                                    │
├──────────────────────────┴─────────────────────────────────────┤
│ 📅 Timeline                                                     │
│ • 01.07. - Erstgespräch mit Einkauf (positiv)                 │
│ • 03.07. - Bedarfsanalyse vor Ort                             │
│ • 05.07. - Email: Budget €8-10k bestätigt                     │
│ • Heute  - [Angebot erstellen]                                │
└─────────────────────────────────────────────────────────────────┘
```

## 🔄 Datenfluss

### Opportunity-Lifecycle

```mermaid
graph LR
    A[Lead eingeht] --> B{Qualifiziert?}
    B -->|Ja| C[Opportunity erstellt]
    B -->|Nein| D[Archiviert]
    C --> E[Bedarfsanalyse]
    E --> F[Angebot]
    F --> G{Angenommen?}
    G -->|Ja| H[Gewonnen → Kunde]
    G -->|Nein| I[Nachverhandlung]
    I --> F
    G -->|Endgültig Nein| J[Verloren]
```

### Integration mit anderen Modulen

1. **Customer Management (M5):**
   - Opportunity kann zu Kunde konvertiert werden
   - Bestehende Kunden können neue Opportunities haben

2. **Calculator (M8):**
   - Wird als Modal aus Opportunity heraus geöffnet
   - Calculation-Ergebnis wird mit Opportunity verknüpft

3. **E-Mail Integration (FC-003):**
   - E-Mails werden automatisch zur Opportunity zugeordnet
   - Templates basierend auf Stage

4. **Cockpit (M3):**
   - "Meine Opportunities" Widget
   - Stage-Änderungen im Activity Feed

## 🚧 Implementierungs-Roadmap

### Sprint 1: Backend-Grundlagen (1.5 Tage)
- [ ] Opportunity Entity & Repository
- [ ] REST API Endpoints
- [ ] Stage-Validierungs-Logik
- [ ] Test-Daten Generator

### Sprint 2: Pipeline UI (2 Tage)
- [ ] Kanban-Board Komponente
- [ ] Drag & Drop Funktionalität
- [ ] Opportunity Cards
- [ ] Stage-spezifische Aktionen

### Sprint 3: Integration & Polish (1 Tag)
- [ ] Calculator-Modal Integration
- [ ] E-Mail Template Verknüpfung
- [ ] Notifications & Alerts
- [ ] Performance Optimierung

## ⚡ Performance-Überlegungen

### Optimierungen:
1. **Lazy Loading:** Nur sichtbare Stages laden Details
2. **Optimistic Updates:** UI sofort aktualisieren, dann Server-Sync
3. **Websocket Updates:** Echtzeit-Updates bei Team-Arbeit
4. **Caching:** Opportunity-Liste im Frontend cachen

### Skalierung:
- Pagination bei mehr als 50 Opportunities pro Stage
- Virtual Scrolling für lange Listen
- Server-seitige Filterung und Sortierung

## ✅ Erfolgs-Metriken

1. **Technische Metriken:**
   - Page Load < 1s
   - Drag & Drop Response < 100ms
   - API Response < 200ms

2. **Business Metriken:**
   - Durchschnittliche Zeit pro Stage
   - Conversion Rate zwischen Stages
   - Win/Loss Ratio
   - Pipeline Value Trends

## 🎯 Definition of Done

- [ ] Alle API-Endpoints implementiert und getestet
- [ ] Frontend responsive auf allen Geräten
- [ ] Drag & Drop funktioniert flüssig
- [ ] Calculator-Integration getestet
- [ ] Unit Test Coverage > 80%
- [ ] E2E Tests für kritische Flows
- [ ] Performance-Ziele erreicht
- [ ] Dokumentation aktualisiert

---

**Nächster Schritt:** Review mit Product Owner und Start der Implementierung