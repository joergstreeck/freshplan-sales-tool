# 🧠 Backend Intelligence - Smart Contact Infrastructure

**Phase:** 1 - Foundation  
**Tag:** 1 der Woche 1  
**Status:** ⏳ IN ARBEIT  
**Letzte Aktualisierung:** 07.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

### Hierarchie
**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FRONTEND_FOUNDATION.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  

### Verwandte Dokumente
- **Contact Timeline:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md`
- **Smart Suggestions:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_SUGGESTIONS.md`
- **Backend Contact:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/BACKEND_CONTACT.md`

## 🎯 Vision: Intelligente Backend-Services

Erweitern des bestehenden CRUD-Backends um **intelligente Features** für proaktives Contact Management:

> "Das Backend soll nicht nur Daten speichern, sondern Insights generieren!"

### 💬 Team-Feedback zu Soft-Delete:
> "Praxisrelevant & DSGVO-compliant. Gold wert für KPIs und Geschäftsanalysen. Perfekt für Revision und Recovery."

## 🏗️ Enhanced Backend Architecture

### ✅ BEREITS IMPLEMENTIERT

#### CustomerContact Entity (VOLLSTÄNDIG)
Pfad: `backend/src/main/java/de/freshplan/domain/customer/entity/CustomerContact.java`
```java
// Vorhanden:
- Basis-Felder (firstName, lastName, email, phone)
- lastContactDate für Warmth-Berechnung ✓
- Soft-Delete Support ✓
- Audit Fields (created/updated) ✓
- Rollen-System mit ContactRole ✓
- Hierarchie (reportsTo/directReports) ✓
```

#### ContactRole Entity (VOLLSTÄNDIG)
Pfad: `backend/src/main/java/de/freshplan/domain/customer/entity/ContactRole.java`
```java
// Rollen-Verwaltung für Kontakte
```

### ⏳ IN ARBEIT - ContactInteraction Entity

Pfad: `backend/src/main/java/de/freshplan/domain/intelligence/entity/ContactInteraction.java`
```java
// TODO: Neue Entity erstellen für Interaktions-Historie
ContactInteraction Entity    // Speichert jede Interaktion
```

### 📋 ZU IMPLEMENTIEREN - Intelligence Services

```java
RelationshipWarmthService    // Beziehungstemperatur berechnen
InteractionTrackingService   // Kontakt-Historie verwalten
SmartSuggestionService      // Handlungsempfehlungen generieren
ContactAnalyticsService     // Insights & Patterns erkennen
NotificationService         // Proaktive Benachrichtigungen
```

## 🆕 NEUE KOMPONENTE: ContactInteraction Entity

### Warum brauchen wir das?
- **Timeline-Feature:** Jede Interaktion muss gespeichert werden
- **Warmth-Berechnung:** Basiert auf Frequenz und Recency von Interaktionen
- **Smart Suggestions:** Analysiert Patterns in der Historie

### Implementation - ContactInteraction.java

**Pfad:** `backend/src/main/java/de/freshplan/domain/intelligence/entity/ContactInteraction.java`

```java
package de.freshplan.domain.intelligence.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

/**
 * Erfasst jede Interaktion mit einem Kontakt für Timeline und Intelligence Features.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Entity
@Table(
    name = "contact_interactions",
    indexes = {
        @Index(name = "idx_interaction_contact", columnList = "contact_id"),
        @Index(name = "idx_interaction_timestamp", columnList = "interaction_timestamp"),
        @Index(name = "idx_interaction_type", columnList = "interaction_type")
    }
)
public class ContactInteraction extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    // Referenz zum Kontakt
    @Column(name = "contact_id", nullable = false)
    private UUID contactId;
    
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    
    // Interaktions-Details
    @Column(name = "interaction_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private InteractionType interactionType;
    
    @Column(name = "interaction_timestamp", nullable = false)
    private LocalDateTime interactionTimestamp;
    
    @Column(name = "direction", length = 20)
    @Enumerated(EnumType.STRING)
    private InteractionDirection direction = InteractionDirection.OUTGOING;
    
    // Kontext und Details
    @Column(name = "subject", length = 255)
    private String subject;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "sentiment", length = 20)
    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;
    
    // Metriken für Analytics
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "response_time_hours")
    private Integer responseTimeHours;
    
    // Verknüpfungen
    @Column(name = "opportunity_id")
    private UUID opportunityId;
    
    @Column(name = "order_id")
    private UUID orderId;
    
    // Audit
    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Enums
    public enum InteractionType {
        CALL, EMAIL, MEETING, NOTE, VISIT, CHAT, SOCIAL_MEDIA, DOCUMENT_SENT, SYSTEM_EVENT
    }
    
    public enum InteractionDirection {
        INCOMING, OUTGOING, INTERNAL
    }
    
    public enum Sentiment {
        VERY_POSITIVE, POSITIVE, NEUTRAL, NEGATIVE, VERY_NEGATIVE
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (interactionTimestamp == null) {
            interactionTimestamp = LocalDateTime.now();
        }
    }
    
    // Getters und Setters...
}
```

## 🌡️ Relationship Warmth Service

### Core Service Implementation

**Pfad:** `backend/src/main/java/de/freshplan/domain/intelligence/service/RelationshipWarmthService.java`

```java
package de.freshplan.domain.intelligence.service;

@ApplicationScoped
public class RelationshipWarmthService {
    
    @Inject
    CustomerContactRepository contactRepository; // Angepasst an tatsächliche Entity
    
    @Inject
    InteractionTrackingService interactionService;
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    ContactInteractionRepository interactionRepository; // NEU
    
    /**
     * Calculate relationship warmth for a contact
     */
    public RelationshipWarmth calculateWarmth(UUID contactId) {
        Contact contact = contactRepository.findById(contactId);
        if (contact == null) {
            throw new NotFoundException("Contact not found: " + contactId);
        }
        
        WarmthMetrics metrics = collectMetrics(contact);
        WarmthScore score = calculateScore(metrics);
        List<ActionSuggestion> suggestions = generateSuggestions(score, metrics, contact);
        
        return RelationshipWarmth.builder()
            .contactId(contactId)
            .customerId(contact.getCustomer().getId())
            .temperature(score.getTemperature())
            .score(score.getValue())
            .lastInteraction(metrics.getLastInteraction())
            .interactionFrequency(metrics.getInteractionFrequency())
            .responseRate(metrics.getResponseRate())
            .trendDirection(metrics.getTrendDirection())
            .suggestions(suggestions)
            .calculatedAt(Instant.now())
            .build();
    }
    
    /**
     * Collect metrics from various sources
     */
    private WarmthMetrics collectMetrics(Contact contact) {
        // Get interaction history
        List<ContactInteraction> interactions = interactionService.getInteractions(contact.getId());
        
        // Calculate basic metrics
        Instant lastInteraction = interactions.stream()
            .map(ContactInteraction::getTimestamp)
            .max(Instant::compareTo)
            .orElse(contact.getCreatedAt().toInstant(ZoneOffset.UTC));
            
        long daysSinceLastContact = ChronoUnit.DAYS.between(lastInteraction, Instant.now());
        
        // Calculate interaction frequency (per month)
        long monthsActive = Math.max(1, ChronoUnit.DAYS.between(
            contact.getCreatedAt().toInstant(ZoneOffset.UTC), 
            Instant.now()
        ) / 30);
        double interactionFrequency = (double) interactions.size() / monthsActive;
        
        // Calculate response rate (interactions with responses vs total outgoing)
        long outgoingInteractions = interactions.stream()
            .mapToLong(i -> i.getType() == InteractionType.OUTGOING ? 1 : 0)
            .sum();
        long incomingInteractions = interactions.stream()
            .mapToLong(i -> i.getType() == InteractionType.INCOMING ? 1 : 0)
            .sum();
        double responseRate = outgoingInteractions > 0 ? 
            (double) incomingInteractions / outgoingInteractions : 0.0;
            
        // Calculate trend (last 30 days vs previous 30 days)
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant sixtyDaysAgo = Instant.now().minus(60, ChronoUnit.DAYS);
        
        long recentInteractions = interactions.stream()
            .mapToLong(i -> i.getTimestamp().isAfter(thirtyDaysAgo) ? 1 : 0)
            .sum();
        long previousInteractions = interactions.stream()
            .mapToLong(i -> i.getTimestamp().isAfter(sixtyDaysAgo) && 
                           i.getTimestamp().isBefore(thirtyDaysAgo) ? 1 : 0)
            .sum();
            
        TrendDirection trend = recentInteractions > previousInteractions ? 
            TrendDirection.IMPROVING : 
            recentInteractions < previousInteractions ? 
                TrendDirection.DECLINING : TrendDirection.STABLE;
        
        // Get business context
        double averageOrderValue = getAverageOrderValue(contact.getCustomer().getId());
        boolean isDecisionMaker = "executive".equals(contact.getDecisionLevel()) || 
                                 "manager".equals(contact.getDecisionLevel());
        
        return WarmthMetrics.builder()
            .contactId(contact.getId())
            .lastInteraction(lastInteraction)
            .daysSinceLastContact(daysSinceLastContact)
            .interactionFrequency(interactionFrequency)
            .responseRate(responseRate)
            .trendDirection(trend)
            .isDecisionMaker(isDecisionMaker)
            .averageOrderValue(averageOrderValue)
            .totalInteractions(interactions.size())
            .build();
    }
}
```

## 🔄 V201 Migration für ContactInteraction

**Pfad:** `backend/src/main/resources/db/migration/V201__create_contact_interactions_table.sql`

```sql
-- V201: Create contact_interactions table for Timeline and Intelligence features
CREATE TABLE IF NOT EXISTS contact_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    
    -- Interaction Details
    interaction_type VARCHAR(50) NOT NULL,
    interaction_timestamp TIMESTAMP NOT NULL,
    direction VARCHAR(20) DEFAULT 'OUTGOING',
    
    -- Context
    subject VARCHAR(255),
    notes TEXT,
    sentiment VARCHAR(20),
    
    -- Metrics
    duration_minutes INTEGER,
    response_time_hours INTEGER,
    
    -- Relations
    opportunity_id UUID,
    order_id UUID,
    
    -- Audit
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Keys
    CONSTRAINT fk_interaction_contact 
        FOREIGN KEY (contact_id) 
        REFERENCES customer_contacts(id) ON DELETE CASCADE,
    CONSTRAINT fk_interaction_customer 
        FOREIGN KEY (customer_id) 
        REFERENCES customers(id) ON DELETE CASCADE
);

-- Indices for Performance
CREATE INDEX idx_interaction_contact ON contact_interactions(contact_id);
CREATE INDEX idx_interaction_timestamp ON contact_interactions(interaction_timestamp);
CREATE INDEX idx_interaction_type ON contact_interactions(interaction_type);
CREATE INDEX idx_interaction_customer ON contact_interactions(customer_id);

-- Insert sample data for testing
INSERT INTO contact_interactions (contact_id, customer_id, interaction_type, interaction_timestamp, direction, subject, sentiment, created_by)
SELECT 
    cc.id,
    cc.customer_id,
    'EMAIL',
    NOW() - INTERVAL '5 days',
    'OUTGOING',
    'Follow-up on proposal',
    'POSITIVE',
    'system'
FROM customer_contacts cc
LIMIT 10;
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### SOFORT zu erledigen:
- [ ] ContactInteraction Entity erstellen (siehe Code oben)
- [ ] V201 Migration ausführen
- [ ] ContactInteractionRepository erstellen
- [ ] InteractionTrackingService implementieren

### Als nächstes:
- [ ] RelationshipWarmthService vervollständigen
- [ ] DataQualityResource auf echte Daten umstellen
- [ ] Tests für neue Services schreiben

## 🧑‍💻 KONKRETE IMPLEMENTIERUNGSSCHRITTE FÜR CLAUDE

### 1. ContactInteraction Entity erstellen
```bash
# Navigiere zum richtigen Verzeichnis
cd backend/src/main/java/de/freshplan/domain/intelligence

# Erstelle entity Unterordner falls nicht vorhanden
mkdir -p entity

# Erstelle ContactInteraction.java mit obigem Code
```

### 2. Repository erstellen
```bash
# Erstelle repository Unterordner
mkdir -p repository

# Erstelle ContactInteractionRepository.java
```

```java
package de.freshplan.domain.intelligence.repository;

import de.freshplan.domain.intelligence.entity.ContactInteraction;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ContactInteractionRepository 
    implements PanacheRepositoryBase<ContactInteraction, UUID> {
    
    public List<ContactInteraction> findByContactId(UUID contactId) {
        return find("contactId", contactId).list();
    }
    
    public List<ContactInteraction> findRecentByContactId(UUID contactId, int days) {
        return find(
            "contactId = ?1 AND interactionTimestamp > CURRENT_TIMESTAMP - INTERVAL '" + days + " days'",
            contactId
        ).list();
    }
}
```

### 3. Migration ausführen
```bash
cd backend
./mvnw flyway:migrate
```

### 4. Service testen
```bash
# Unit Test erstellen und ausführen
./mvnw test -Dtest=ContactInteractionRepositoryTest
```

## ⚠️ WICHTIGE HINWEISE

1. **CustomerContact vs Contact:** Die tatsächliche Entity heißt `CustomerContact`, nicht `Contact`!
2. **Package-Struktur:** Neue Intelligence-Features unter `de.freshplan.domain.intelligence`
3. **Migration-Nummer:** V201 ist die nächste freie Nummer (nicht V121!)
4. **Relationship:** ContactInteraction referenziert CustomerContact, nicht direkt Customer

## 🎯 NÄCHSTE SCHRITTE

Nach erfolgreicher Implementierung:
1. **Frontend Foundation** implementieren
2. **Smart Contact Cards** UI erstellen  
3. **Contact Timeline** Component entwickeln

---

**Nächstes Dokument:** [→ Frontend Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FRONTEND_FOUNDATION.md)
            .interactionFrequency(interactionFrequency)
            .responseRate(Math.min(1.0, responseRate)) // Cap at 1.0
            .trendDirection(trend)
            .averageOrderValue(averageOrderValue)
            .isDecisionMaker(isDecisionMaker)
            .totalInteractions(interactions.size())
            .daysUntilBirthday(calculateDaysUntilBirthday(contact.getBirthday()))
            .hasPersonalData(contact.getBirthday() != null || contact.getHobbies() != null)
            .build();
    }
    
    /**
     * Calculate warmth score using weighted algorithm
     */
    private WarmthScore calculateScore(WarmthMetrics metrics) {
        double score = 100.0; // Start with perfect score
        
        // Recency Factor (40% weight) - Most important
        long days = metrics.getDaysSinceLastContact();
        if (days > 90) score -= 50;       // Very cold
        else if (days > 60) score -= 35;  // Cold  
        else if (days > 30) score -= 20;  // Cooling
        else if (days > 14) score -= 10;  // Warm
        else if (days > 7) score -= 5;    // Hot
        // < 7 days = no deduction (very hot)
        
        // Frequency Factor (25% weight)
        double frequency = metrics.getInteractionFrequency();
        if (frequency < 0.5) score -= 25;      // Very low frequency
        else if (frequency < 1.0) score -= 15; // Low frequency
        else if (frequency < 2.0) score -= 5;  // Moderate frequency
        else if (frequency > 4.0) score += 10; // High frequency bonus
        
        // Response Rate Factor (20% weight)
        double responseRate = metrics.getResponseRate();
        if (responseRate < 0.3) score -= 20;
        else if (responseRate < 0.5) score -= 10;
        else if (responseRate > 0.8) score += 15; // Excellent response bonus
        
        // Trend Factor (10% weight)
        switch (metrics.getTrendDirection()) {
            case IMPROVING -> score += 10;
            case DECLINING -> score -= 15;
            case STABLE -> score += 0; // neutral
        }
        
        // Business Value Factor (5% weight)
        if (metrics.getAverageOrderValue() > 10000) score += 10;
        else if (metrics.getAverageOrderValue() > 5000) score += 5;
        
        if (metrics.isDecisionMaker()) score += 5;
        
        // Ensure score is within bounds
        score = Math.max(0, Math.min(100, score));
        
        // Determine temperature based on score
        Temperature temperature;
        if (score >= 80) temperature = Temperature.HOT;
        else if (score >= 60) temperature = Temperature.WARM;
        else if (score >= 40) temperature = Temperature.COOLING;
        else temperature = Temperature.COLD;
        
        return new WarmthScore(score, temperature);
    }
    
    /**
     * Generate smart action suggestions
     */
    private List<ActionSuggestion> generateSuggestions(
            WarmthScore score, 
            WarmthMetrics metrics, 
            Contact contact) {
        
        List<ActionSuggestion> suggestions = new ArrayList<>();
        
        // Temperature-based suggestions
        if (score.getTemperature() == Temperature.COLD) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.REACH_OUT)
                .reason("Kontakt ist kalt geworden - sofortige Aufmerksamkeit nötig")
                .urgency(Urgency.HIGH)
                .suggestedAction("Persönlicher Anruf mit Beziehungsaufbau im Fokus")
                .estimatedImpact("Relationship Revival")
                .build());
        } else if (score.getTemperature() == Temperature.COOLING) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.REACH_OUT)
                .reason("Kontakt kühlt ab - präventive Maßnahme empfohlen")
                .urgency(Urgency.MEDIUM)
                .suggestedAction("Check-in Call oder informelle E-Mail")
                .estimatedImpact("Prevent Relationship Decline")
                .build());
        }
        
        // Birthday suggestions
        if (metrics.getDaysUntilBirthday() >= 0 && metrics.getDaysUntilBirthday() <= 7) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.PERSONAL_TOUCH)
                .reason("Geburtstag in " + metrics.getDaysUntilBirthday() + " Tag(en)")
                .urgency(Urgency.HIGH)
                .suggestedAction("Persönliche Geburtstagsgratulation")
                .estimatedImpact("Relationship Strengthening")
                .build());
        }
        
        // Low response rate suggestions
        if (metrics.getResponseRate() < 0.3 && metrics.getTotalInteractions() > 5) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.STRATEGY_CHANGE)
                .reason("Niedrige Antwortrate - Kommunikationsstrategie überdenken")
                .urgency(Urgency.MEDIUM)
                .suggestedAction("Kommunikationskanal wechseln oder Ansprache personalisieren")
                .estimatedImpact("Improve Engagement")
                .build());
        }
        
        // High value suggestions
        if (metrics.getAverageOrderValue() > 10000 && score.getValue() < 70) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.PRIORITY_FOCUS)
                .reason("Hochwertiger Kunde mit niedrigem Relationship Score")
                .urgency(Urgency.HIGH)
                .suggestedAction("Dedizierte Account Management Strategie entwicklung")
                .estimatedImpact("Revenue Protection")
                .build());
        }
        
        return suggestions;
    }
    
    /**
     * Batch calculation for dashboard views
     */
    public List<RelationshipWarmth> calculateWarmthForCustomer(UUID customerId) {
        List<Contact> contacts = contactRepository.findByCustomerId(customerId);
        return contacts.stream()
            .map(contact -> calculateWarmth(contact.getId()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get contacts needing attention
     */
    public List<ContactAlert> getContactsNeedingAttention(UUID customerId, String temperatureThreshold) {
        List<RelationshipWarmth> warmthData = calculateWarmthForCustomer(customerId);
        
        return warmthData.stream()
            .filter(warmth -> shouldAlert(warmth, temperatureThreshold))
            .map(this::createContactAlert)
            .sorted(Comparator.comparing(ContactAlert::getUrgencyScore).reversed())
            .collect(Collectors.toList());
    }
    
    private boolean shouldAlert(RelationshipWarmth warmth, String threshold) {
        return switch (threshold.toUpperCase()) {
            case "COLD" -> warmth.getTemperature() == Temperature.COLD;
            case "COOLING" -> warmth.getTemperature() == Temperature.COOLING || 
                             warmth.getTemperature() == Temperature.COLD;
            case "ALL" -> !warmth.getSuggestions().isEmpty();
            default -> false;
        };
    }
    
    private ContactAlert createContactAlert(RelationshipWarmth warmth) {
        Contact contact = contactRepository.findById(warmth.getContactId());
        return ContactAlert.builder()
            .contactId(warmth.getContactId())
            .contactName(contact.getDisplayName())
            .customerId(warmth.getCustomerId())
            .temperature(warmth.getTemperature())
            .score(warmth.getScore())
            .urgencyScore(calculateUrgencyScore(warmth))
            .primarySuggestion(warmth.getSuggestions().isEmpty() ? null : 
                              warmth.getSuggestions().get(0))
            .lastInteraction(warmth.getLastInteraction())
            .build();
    }
    
    private double calculateUrgencyScore(RelationshipWarmth warmth) {
        double urgency = 0.0;
        
        // Temperature contributes to urgency
        urgency += switch (warmth.getTemperature()) {
            case COLD -> 100.0;
            case COOLING -> 75.0;
            case WARM -> 25.0;
            case HOT -> 10.0;
        };
        
        // High-priority suggestions add urgency
        urgency += warmth.getSuggestions().stream()
            .mapToDouble(s -> s.getUrgency() == Urgency.HIGH ? 50.0 : 
                             s.getUrgency() == Urgency.MEDIUM ? 25.0 : 10.0)
            .sum();
            
        return Math.min(200.0, urgency);
    }
}
```

## 🔄 Soft-Delete Implementation

### Team-gewünschte Soft-Delete Features

```java
@Entity
@Table(name = "contacts")
@Audited // Für vollständige Historie
public class Contact extends PanacheEntityBase {
    
    // Soft-Delete Fields
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "deleted_by")
    private String deletedBy;
    
    @Column(name = "deletion_reason")
    private String deletionReason;
    
    // Location Historie für KPIs
    @ElementCollection
    @CollectionTable(
        name = "contact_location_history",
        joinColumns = @JoinColumn(name = "contact_id")
    )
    private List<LocationAssignment> locationHistory = new ArrayList<>();
}

// Repository mit Soft-Delete Support
@ApplicationScoped
public class ContactRepository implements PanacheRepositoryBase<Contact, UUID> {
    
    // Nur aktive Kontakte
    public List<Contact> findActiveByCustomerId(UUID customerId) {
        return find("customer.id = ?1 and isActive = true", customerId).list();
    }
    
    // Für Audit & Recovery
    public List<Contact> findAllIncludingDeleted(UUID customerId) {
        return find("customer.id = ?1", customerId).list();
    }
    
    // Soft Delete mit Grund
    @Transactional
    public void softDelete(UUID contactId, String reason, String deletedBy) {
        Contact contact = findById(contactId);
        if (contact != null) {
            contact.setIsActive(false);
            contact.setDeletedAt(LocalDateTime.now());
            contact.setDeletedBy(deletedBy);
            contact.setDeletionReason(reason);
            persist(contact);
        }
    }
    
    // Recovery für versehentliche Löschungen
    @Transactional
    public void restore(UUID contactId) {
        Contact contact = findById(contactId);
        if (contact != null && !contact.getIsActive()) {
            contact.setIsActive(true);
            contact.setDeletedAt(null);
            contact.setDeletedBy(null);
            contact.setDeletionReason(null);
            persist(contact);
        }
    }
}
```

### Business Value des Soft-Delete
- **KPI-Tracking:** Gelöschte Kontakte bleiben in Analysen sichtbar
- **Audit-Compliance:** Vollständige Historie wer wann was gelöscht hat
- **Recovery:** Versehentliche Löschungen können rückgängig gemacht werden
- **DSGVO-konform:** Anonymisierung statt Löschung möglich

## 📊 Interaction Tracking Service

### Interaction Data Model

```java
@Entity
@Table(name = "contact_interactions")
public class ContactInteraction extends PanacheEntityBase {
    
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false)
    private InteractionType type; // CALL_OUTGOING, CALL_INCOMING, EMAIL_SENT, EMAIL_RECEIVED, etc.
    
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds; // For calls
    
    @Column(name = "subject", length = 500)
    private String subject; // Email subject or call purpose
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "outcome")
    @Enumerated(EnumType.STRING)
    private InteractionOutcome outcome; // SUCCESSFUL, NO_RESPONSE, BUSY, FOLLOW_UP_NEEDED
    
    @Column(name = "created_by", nullable = false)
    private String createdBy; // User who logged the interaction
    
    @CreationTimestamp
    private Instant createdAt;
    
    // Getters, Setters, Builder...
}

public enum InteractionType {
    CALL_OUTGOING, CALL_INCOMING, CALL_MISSED,
    EMAIL_SENT, EMAIL_RECEIVED, EMAIL_OPENED,
    MEETING_SCHEDULED, MEETING_HELD, MEETING_CANCELLED,
    WHATSAPP_SENT, WHATSAPP_RECEIVED,
    DOCUMENT_SHARED, PROPOSAL_SENT,
    CONTACT_VIEWED, CONTACT_UPDATED,
    OTHER
}

public enum InteractionOutcome {
    SUCCESSFUL, NO_RESPONSE, BUSY, 
    FOLLOW_UP_NEEDED, MEETING_SCHEDULED,
    INFORMATION_PROVIDED, PROPOSAL_REQUESTED,
    DECLINED, POSTPONED
}
```

### Interaction Tracking Service

```java
@ApplicationScoped
@Transactional
public class InteractionTrackingService {
    
    @Inject
    EntityManager em;
    
    @Inject
    SecurityIdentity securityIdentity;
    
    /**
     * Record a new interaction
     */
    public ContactInteraction recordInteraction(UUID contactId, 
                                               InteractionType type,
                                               String subject,
                                               String notes,
                                               InteractionOutcome outcome) {
        Contact contact = Contact.findById(contactId);
        if (contact == null) {
            throw new NotFoundException("Contact not found: " + contactId);
        }
        
        ContactInteraction interaction = ContactInteraction.builder()
            .contact(contact)
            .type(type)
            .timestamp(Instant.now())
            .subject(subject)
            .notes(notes)
            .outcome(outcome)
            .createdBy(securityIdentity.getPrincipal().getName())
            .build();
            
        interaction.persist();
        
        // Trigger warmth recalculation (async)
        triggerWarmthUpdate(contactId);
        
        return interaction;
    }
    
    /**
     * Auto-record system interactions
     */
    @EventListener
    public void onContactViewed(ContactViewedEvent event) {
        recordInteraction(
            event.getContactId(),
            InteractionType.CONTACT_VIEWED,
            "Contact viewed in system",
            null,
            InteractionOutcome.SUCCESSFUL
        );
    }
    
    @EventListener  
    public void onContactUpdated(ContactUpdatedEvent event) {
        recordInteraction(
            event.getContactId(),
            InteractionType.CONTACT_UPDATED,
            "Contact information updated",
            "Fields: " + String.join(", ", event.getChangedFields()),
            InteractionOutcome.SUCCESSFUL
        );
    }
    
    /**
     * Get interaction timeline
     */
    public List<ContactInteraction> getInteractions(UUID contactId) {
        return em.createQuery(
            "SELECT i FROM ContactInteraction i WHERE i.contact.id = :contactId ORDER BY i.timestamp DESC",
            ContactInteraction.class)
            .setParameter("contactId", contactId)
            .getResultList();
    }
    
    /**
     * Get recent interactions across all contacts
     */
    public List<ContactInteraction> getRecentInteractions(UUID customerId, int limit) {
        return em.createQuery(
            "SELECT i FROM ContactInteraction i " +
            "WHERE i.contact.customer.id = :customerId " +
            "ORDER BY i.timestamp DESC",
            ContactInteraction.class)
            .setParameter("customerId", customerId)
            .setMaxResults(limit)
            .getResultList();
    }
    
    @Async
    private void triggerWarmthUpdate(UUID contactId) {
        // Trigger warmth recalculation in background
        // This could be done via CDI events or message queue
        CDI.current().select(RelationshipWarmthService.class).get()
           .calculateWarmth(contactId);
    }
}
```

## 🔔 Smart Notification Service

```java
@ApplicationScoped
public class ContactNotificationService {
    
    @Inject
    RelationshipWarmthService warmthService;
    
    @Inject
    ContactRepository contactRepository;
    
    /**
     * Check for contacts needing attention and send notifications
     */
    @Scheduled(every = "1h") // Run every hour
    public void checkContactAlerts() {
        List<Customer> customers = Customer.listAll();
        
        for (Customer customer : customers) {
            List<ContactAlert> alerts = warmthService.getContactsNeedingAttention(
                customer.getId(), "COOLING");
                
            if (!alerts.isEmpty()) {
                sendContactAlerts(customer, alerts);
            }
        }
    }
    
    /**
     * Daily birthday reminders
     */
    @Scheduled(cron = "0 9 * * MON-FRI") // 9 AM on weekdays
    public void sendBirthdayReminders() {
        List<Contact> upcomingBirthdays = contactRepository.findUpcomingBirthdays(7);
        
        for (Contact contact : upcomingBirthdays) {
            sendBirthdayReminder(contact);
        }
    }
    
    private void sendContactAlerts(Customer customer, List<ContactAlert> alerts) {
        // Send email/push notification to assigned sales person
        // Implementation depends on notification system
        
        String subject = String.format("🌡️ %d Kontakte bei %s benötigen Aufmerksamkeit", 
                                      alerts.size(), customer.getCompanyName());
        
        StringBuilder body = new StringBuilder();
        body.append("Folgende Kontakte benötigen Ihre Aufmerksamkeit:\n\n");
        
        for (ContactAlert alert : alerts) {
            body.append(String.format("• %s (%s) - %s\n", 
                alert.getContactName(),
                alert.getTemperature(),
                alert.getPrimarySuggestion() != null ? 
                    alert.getPrimarySuggestion().getReason() : "Attention needed"));
        }
        
        // Send via your notification system
        sendNotification(customer.getAssignedSalesperson(), subject, body.toString());
    }
}
```

## 🔗 Enhanced REST API

### New Intelligence Endpoints

```java
@Path("/api/customers/{customerId}/contacts")
public class ContactResource {
    
    @Inject
    RelationshipWarmthService warmthService;
    
    @Inject
    InteractionTrackingService interactionService;
    
    // ... existing CRUD endpoints ...
    
    @GET
    @Path("/{contactId}/warmth")
    @Operation(summary = "Get relationship warmth for contact")
    public RelationshipWarmth getContactWarmth(
            @PathParam("customerId") UUID customerId,
            @PathParam("contactId") UUID contactId) {
        return warmthService.calculateWarmth(contactId);
    }
    
    @GET
    @Path("/warmth-dashboard")
    @Operation(summary = "Get warmth overview for all contacts")
    public List<RelationshipWarmth> getWarmthDashboard(
            @PathParam("customerId") UUID customerId) {
        return warmthService.calculateWarmthForCustomer(customerId);
    }
    
    @GET
    @Path("/alerts")
    @Operation(summary = "Get contacts needing attention")
    public List<ContactAlert> getContactAlerts(
            @PathParam("customerId") UUID customerId,
            @QueryParam("temperature") @DefaultValue("COOLING") String temperature) {
        return warmthService.getContactsNeedingAttention(customerId, temperature);
    }
    
    @POST
    @Path("/{contactId}/interactions")
    @Operation(summary = "Record contact interaction")
    public ContactInteraction recordInteraction(
            @PathParam("customerId") UUID customerId,
            @PathParam("contactId") UUID contactId,
            @Valid RecordInteractionRequest request) {
        return interactionService.recordInteraction(
            contactId,
            request.getType(),
            request.getSubject(),
            request.getNotes(),
            request.getOutcome()
        );
    }
    
    @GET
    @Path("/{contactId}/timeline")
    @Operation(summary = "Get contact interaction timeline")
    public List<ContactInteraction> getContactTimeline(
            @PathParam("customerId") UUID customerId,
            @PathParam("contactId") UUID contactId) {
        return interactionService.getInteractions(contactId);
    }
}
```

## 🧪 Testing Strategy

### Service Unit Tests

```java
@QuarkusTest
class RelationshipWarmthServiceTest {
    
    @Inject
    RelationshipWarmthService warmthService;
    
    @Test
    void shouldCalculateHotTemperature_whenRecentFrequentContact() {
        // Given
        Contact contact = createTestContact();
        mockInteractions(contact.getId(), Arrays.asList(
            createInteraction(InteractionType.CALL_OUTGOING, 2), // 2 days ago
            createInteraction(InteractionType.EMAIL_RECEIVED, 5), // 5 days ago
            createInteraction(InteractionType.MEETING_HELD, 7)    // 7 days ago
        ));
        
        // When
        RelationshipWarmth warmth = warmthService.calculateWarmth(contact.getId());
        
        // Then
        assertThat(warmth.getTemperature()).isEqualTo(Temperature.HOT);
        assertThat(warmth.getScore()).isGreaterThan(80);
        assertThat(warmth.getSuggestions()).isEmpty(); // No action needed for hot contacts
    }
    
    @Test
    void shouldGenerateCoolingAlert_whenContactNeedsAttention() {
        // Given
        Contact contact = createTestContact();
        mockInteractions(contact.getId(), Arrays.asList(
            createInteraction(InteractionType.EMAIL_SENT, 35), // 35 days ago, no response
            createInteraction(InteractionType.CALL_OUTGOING, 45) // 45 days ago
        ));
        
        // When
        RelationshipWarmth warmth = warmthService.calculateWarmth(contact.getId());
        
        // Then
        assertThat(warmth.getTemperature()).isEqualTo(Temperature.COOLING);
        assertThat(warmth.getSuggestions()).hasSize(1);
        assertThat(warmth.getSuggestions().get(0).getType()).isEqualTo(ActionType.REACH_OUT);
        assertThat(warmth.getSuggestions().get(0).getUrgency()).isEqualTo(Urgency.MEDIUM);
    }
}
```

## 📈 Performance Optimization

### Caching Strategy

```java
@ApplicationScoped
public class WarmthCacheService {
    
    @CacheResult(cacheName = "contact-warmth")
    public RelationshipWarmth getCachedWarmth(UUID contactId) {
        return warmthService.calculateWarmth(contactId);
    }
    
    @CacheInvalidate(cacheName = "contact-warmth") 
    public void invalidateWarmthCache(UUID contactId) {
        // Called when contact or interactions are updated
    }
    
    // Background refresh every 6 hours
    @Scheduled(every = "6h")
    @CacheInvalidateAll(cacheName = "contact-warmth")
    public void refreshAllWarmthCache() {
        // Force recalculation of all warmth data
    }
}
```

## 🔍 Hibernate Envers für Audit Trail

### Automatische Historie mit Envers

```java
// pom.xml - Envers Dependency hinzufügen
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-envers</artifactId>
</dependency>

// Contact Entity mit vollständiger Audit-Historie
@Entity
@Table(name = "contacts")
@Audited // Vollständige Historie automatisch!
@AuditTable("contacts_audit")
public class Contact extends PanacheEntityBase {
    // Alle Änderungen werden automatisch in contacts_audit gespeichert
}

// Audit-Abfragen
@ApplicationScoped
public class ContactAuditService {
    
    @Inject
    EntityManager em;
    
    /**
     * Hole komplette Historie eines Kontakts
     */
    public List<ContactRevision> getContactHistory(UUID contactId) {
        AuditReader reader = AuditReaderFactory.get(em);
        
        List<Number> revisions = reader.getRevisions(Contact.class, contactId);
        List<ContactRevision> history = new ArrayList<>();
        
        for (Number revision : revisions) {
            Contact contact = reader.find(Contact.class, contactId, revision);
            RevisionEntity revEntity = reader.findRevision(
                DefaultRevisionEntity.class, revision
            );
            
            history.add(ContactRevision.builder()
                .revisionNumber(revision.intValue())
                .revisionDate(new Date(revEntity.getTimestamp()))
                .contact(contact)
                .build());
        }
        
        return history;
    }
    
    /**
     * Wer hat wann was geändert?
     */
    public List<AuditChange> getChanges(UUID contactId, int fromRevision, int toRevision) {
        AuditReader reader = AuditReaderFactory.get(em);
        
        Contact oldVersion = reader.find(Contact.class, contactId, fromRevision);
        Contact newVersion = reader.find(Contact.class, contactId, toRevision);
        
        // Vergleiche und finde Änderungen
        return compareVersions(oldVersion, newVersion);
    }
}
```

**Vorteil:** Vollständige Audit-Historie ohne manuellen Code!

## 🏢 Multi-Tenancy Vorbereitung

### Tenant-Aware Entities

```java
// Base Entity mit Tenant Support
@MappedSuperclass
public abstract class TenantAwareEntity extends PanacheEntityBase {
    
    @Column(name = "tenant_id")
    private String tenantId;
    
    @PrePersist
    @PreUpdate
    public void prePersist() {
        // Automatisch Tenant aus Security Context setzen
        if (tenantId == null) {
            tenantId = TenantContext.getCurrentTenantId();
        }
    }
}

// Contact Entity mit Tenant Support
@Entity
@Table(name = "contacts")
public class Contact extends TenantAwareEntity {
    // Alle Kontakte sind automatisch tenant-isolated
}

// Repository mit automatischem Tenant-Filter
@ApplicationScoped
public class ContactRepository implements PanacheRepositoryBase<Contact, UUID> {
    
    @Override
    public PanacheQuery<Contact> find(String query, Object... params) {
        // Automatisch tenant_id Filter hinzufügen
        String tenantFilter = "tenant_id = '" + TenantContext.getCurrentTenantId() + "'";
        
        if (query == null || query.isEmpty()) {
            return find(tenantFilter);
        } else {
            return find(tenantFilter + " and (" + query + ")", params);
        }
    }
}
```

### Tenant Context für spätere Mandantenfähigkeit

```java
// TenantContext.java - Vorbereitung für Multi-Tenancy
@ApplicationScoped
public class TenantContext {
    
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    
    // Für jetzt: Single Tenant
    private static final String DEFAULT_TENANT = "freshplan-default";
    
    public static String getCurrentTenantId() {
        String tenantId = currentTenant.get();
        return tenantId != null ? tenantId : DEFAULT_TENANT;
    }
    
    public static void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }
    
    public static void clear() {
        currentTenant.remove();
    }
}

// Für spätere Aktivierung: Tenant Resolution Filter
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
public class TenantResolutionFilter implements ContainerRequestFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Später: Tenant aus JWT Token oder Header extrahieren
        // String tenantId = extractTenantFromRequest(requestContext);
        // TenantContext.setCurrentTenant(tenantId);
    }
}
```

**Vorteil:** Wenn Multi-Tenancy benötigt wird, ist die Grundlage bereits vorhanden!

---

**Nächster Schritt:** [→ Frontend Foundation](./FRONTEND_FOUNDATION.md)

**Backend Intelligence = Fundament für Smart Contact Management! 🧠✨**