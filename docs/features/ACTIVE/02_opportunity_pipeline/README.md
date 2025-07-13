# 🚀 M4 - Opportunity Pipeline (Tag 2-5)

**Status:** 📋 Geplant  
**Geschätzter Aufwand:** 4 Tage  
**Priorität:** HOCH - Herzstück des Systems  
**Abhängigkeit:** Security Foundation ✅

## 🎯 Ziel

Prozessorientierte Sales Pipeline mit Kanban-Board, die den kompletten Vertriebsprozess visualisiert und steuert.

## 📋 Implementation Checklist

### Tag 2: Backend Foundation
- [ ] Opportunity Entity erstellen
- [ ] Repository mit Custom Queries
- [ ] Service Layer mit Business Logic
- [ ] REST Endpoints (CRUD + Pipeline-spezifisch)
- [ ] Test Data Generator

### Tag 3-4: Frontend Pipeline
- [ ] OpportunityPipeline.tsx - Hauptcontainer
- [ ] Drag & Drop mit react-beautiful-dnd
- [ ] Stage-Komponenten mit Cards
- [ ] Stage-spezifische Actions
- [ ] Integration mit MainLayoutV2

### Tag 5: Integration & Polish
- [ ] Stage-Validierung implementieren
- [ ] WebSocket für Team-Updates
- [ ] Performance-Optimierung
- [ ] E2E Tests

## 🚨 OFFENE FRAGEN (VOR START KLÄREN!)

### 🔴 Business Rules (KRITISCH):

1. **Stage-Übergänge:**
   ```
   Von NEW_LEAD nach:
   - QUALIFICATION ✅
   - CLOSED_LOST ✅
   - NEEDS_ANALYSIS ❓ (Erlaubt oder nicht?)
   ```
   → **KLÄREN:** Vollständige Übergangsmatrix erstellen!

2. **Automatische Aktionen:**
   - Bei Stage "Angebot" → Calculator öffnen? (Pflicht oder Optional?)
   - Bei "Gewonnen" → Kunde automatisch erstellen?
   - Bei "Verloren" → Grund erfassen? (Pflichtfeld?)

3. **Verkäuferschutz-Integration:**
   - Wer darf Opportunities sehen? (Nur eigene? Team? Alle?)
   - Wer darf Stage ändern? (Nur Owner? Manager auch?)
   - Was passiert bei Verkäufer-Wechsel? (Historie? Provision?)

### 🟡 Technische Fragen:

1. **Performance:**
   - Wie viele Opportunities maximal pro Stage? (100? 500? 1000?)
   - Virtual Scrolling nötig?
   - Pagination oder Lazy Loading?

2. **Drag & Drop Edge Cases:**
   - Was bei gleichzeitigem Drag von 2 Usern?
   - Optimistic UI oder Server-Wait?
   - Undo-Funktion nötig?

3. **Integration Points:**
   - Wie übergibt Pipeline Daten an Calculator?
   - Wo speichern wir Calculator-Ergebnisse?
   - E-Mail-Integration: Automatische Zuordnung wie?

### 🟢 UX/UI Fragen:

1. **Visuelle Darstellung:**
   - Wie viele Stages gleichzeitig sichtbar? (Horizontal Scroll?)
   - Card-Größe: Kompakt oder Detailed View?
   - Farbcodierung nach was? (Stage? Priorität? Alter?)

2. **Aktionen:**
   - Quick Actions direkt auf Card?
   - Oder Detail-Modal für Aktionen?
   - Keyboard Shortcuts? (Tab zwischen Stages?)

## 🏗️ Technische Architektur

### Backend Entity Design:
```java
@Entity
public class Opportunity {
    @Id
    private UUID id;
    
    private String name;
    private OpportunityStage stage;
    
    @ManyToOne
    private Customer customer;  // Optional bei Leads!
    
    @ManyToOne
    private User assignedTo;    // Verkäuferschutz!
    
    private BigDecimal expectedValue;
    private LocalDate expectedCloseDate;
    private Integer probability;  // 0-100%
    
    // FRAGEN:
    // - Brauchen wir Stage-History?
    // - Wie tracken wir Provisionen?
    // - Custom Fields nötig?
}
```

### Stage Definition:
```java
public enum OpportunityStage {
    NEW_LEAD("Neuer Lead", 0),
    QUALIFICATION("Qualifizierung", 1),
    NEEDS_ANALYSIS("Bedarfsanalyse", 2),
    PROPOSAL("Angebotserstellung", 3),  // ← Calculator Trigger!
    NEGOTIATION("Verhandlung", 4),
    CLOSED_WON("Gewonnen", 5),
    CLOSED_LOST("Verloren", 6);
    
    // FRAGE: Weitere Stages nötig?
    // - ON_HOLD (Pausiert)?
    // - RENEWAL (Verlängerung)?
}
```

## 🧪 Test-Szenarien

```gherkin
Feature: Opportunity Pipeline Management

Scenario: Verkäufer verschiebt Opportunity zu Angebot
  Given eine Opportunity "Hotel Adler" in Stage "Bedarfsanalyse"
  And ich bin der zugewiesene Verkäufer
  When ich die Opportunity nach "Angebotserstellung" ziehe
  Then sollte der Calculator Modal sich öffnen
  And die Stage sollte aktualisiert werden
  And eine Aktivität sollte geloggt werden
  
# FRAGE: Was wenn Calculator abgebrochen wird?
# - Stage-Wechsel rückgängig?
# - Oder Stage bleibt, Calculator optional?
```

## 📍 Code Locations

### Backend:
```bash
# Neue Struktur anlegen:
mkdir -p backend/src/main/java/de/freshplan/domain/opportunity/{entity,repository,service,service/dto}

# Entity
vim backend/src/main/java/de/freshplan/domain/opportunity/entity/Opportunity.java

# API
vim backend/src/main/java/de/freshplan/api/resources/OpportunityResource.java
```

### Frontend:
```bash
# Feature-Struktur:
mkdir -p frontend/src/features/opportunity/{components,hooks,services,types}

# Hauptkomponente:
vim frontend/src/features/opportunity/components/OpportunityPipeline.tsx
```

## 🔗 Abhängige Dokumente

- [Business Rules](./business_rules.md) - MUSS ERSTELLT WERDEN!
- [Stage Transition Matrix](./stage_transitions.md) - MUSS ERSTELLT WERDEN!
- [Calculator Integration](../03_calculator_modal/integration_flow.md)
- [Security Concept](./security_concept.md)

## ⚠️ Kritische Entscheidungen

1. **State Management:**
   - React Query für Server State?
   - Oder Redux/Zustand für komplexe Updates?
   - Optimistic Updates: Ja/Nein?

2. **Real-time Updates:**
   - WebSockets von Anfang an?
   - Oder Polling für MVP?
   - Conflict Resolution Strategy?

3. **Mobile Strategy:**
   - Drag & Drop auf Mobile?
   - Alternative: Action Buttons?
   - Responsive vs. Adaptive Design?

## ✅ Definition of Done

### Funktionale Anforderungen
- [ ] Pipeline zeigt alle Opportunities nach Stage
- [ ] Drag & Drop funktioniert zwischen Stages
- [ ] Stage-Validierung gemäß Business Rules
- [ ] Calculator öffnet bei "Angebot"
- [ ] Team sieht Updates in Echtzeit
- [ ] Performance < 1s bei 500 Opportunities

### 🏆 Quality Gates (PFLICHT!)
- [ ] **Frontend:** MUSS MainLayoutV2 verwenden
- [ ] **Tests:** Unit > 80%, Integration, E2E, Browser (Chrome/Firefox/Safari)
- [ ] **Formatierung:** `npm run format:check` grün
- [ ] **Two-Pass Review:** Pass 1 (Hygiene) + Pass 2 (Strategie) ✓
- [ ] **Security:** Keine Vulnerabilities in `npm audit`
- [ ] **Performance:** Bundle < 200KB, Lighthouse > 90
- [ ] **Dokumentation:** README, API-Docs, Change Log aktuell

### 🔄 Git-Workflow
- [ ] Commits alle 2-4 Stunden
- [ ] WIP-Commit vor Feierabend
- [ ] PR erstellt nach Review
- [ ] GitHub Push nach PR-Approval

## 🚀 Nächster Schritt

Nach Completion → [M8 Calculator Modal Integration](../03_calculator_modal/README.md)