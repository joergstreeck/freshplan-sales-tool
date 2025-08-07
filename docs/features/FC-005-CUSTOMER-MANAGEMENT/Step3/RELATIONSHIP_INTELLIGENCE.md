# 🌡️ Relationship Intelligence - Contact Warmth System

**Phase:** 2 - Intelligence Features  
**Tag:** 1 der Woche 2  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MOBILE_CONTACT_ACTIONS.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Voraussetzungen:**
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/BACKEND_INTELLIGENCE.md` ✅
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FRONTEND_FOUNDATION.md` ✅

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Pfade für Copy & Paste:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Intelligence Services erstellen
mkdir -p backend/src/main/java/de/freshplan/intelligence
touch backend/src/main/java/de/freshplan/intelligence/RelationshipWarmthService.java
touch backend/src/main/java/de/freshplan/intelligence/WarmthCalculator.java
touch backend/src/main/java/de/freshplan/intelligence/InteractionAnalyzer.java
touch backend/src/main/java/de/freshplan/intelligence/SuggestionEngine.java

# 2. Frontend Warmth Components erstellen
mkdir -p frontend/src/features/customers/components/intelligence
touch frontend/src/features/customers/components/intelligence/WarmthScoreCalculator.ts
touch frontend/src/features/customers/components/intelligence/InteractionAnalyzer.ts
touch frontend/src/features/customers/components/intelligence/SuggestionEngine.tsx

# 3. Migration für ContactInteraction Entity
touch backend/src/main/resources/db/migration/V201__create_contact_interactions_table.sql

# 4. Tests vorbereiten
mkdir -p backend/src/test/java/de/freshplan/intelligence
touch backend/src/test/java/de/freshplan/intelligence/RelationshipWarmthServiceTest.java
```

## 📦 IMPLEMENTATION REQUIREMENTS

### Backend Dependencies (MÜSSEN existieren!):

| Komponente | Pfad | Status | Implementiert in |
|------------|------|--------|------------------|
| CustomerContact Entity | `backend/src/main/java/de/freshplan/domain/customer/entity/CustomerContact.java` | ✅ Existiert | Backend |
| ContactInteraction Entity | `backend/src/main/java/de/freshplan/domain/interaction/entity/ContactInteraction.java` | ❌ Erstellen | V201 Migration |
| ContactInteractionRepository | `backend/src/main/java/de/freshplan/domain/interaction/repository/ContactInteractionRepository.java` | ❌ Erstellen | Diese Planung |

### Frontend Dependencies:

| Komponente | Pfad | Status |
|------------|------|--------|
| ContactStore | `frontend/src/features/customers/stores/contactStore.ts` | ✅ Geplant |
| ContactIntelligence Types | `frontend/src/features/customers/types/contact.types.ts` | ✅ Geplant |
| WarmthIndicator Component | `frontend/src/features/customers/components/contacts/WarmthIndicator.tsx` | ✅ Dokumentiert |

## 🎯 Vision: Proaktive Beziehungspflege durch Intelligenz

Das **Relationship Warmth System** verwandelt passive Kontaktdaten in **proaktive Vertriebsintelligenz**:

> "Dieser Kontakt kühlt ab - Zeit für einen persönlichen Anruf!"  
> "Geburtstag in 3 Tagen - perfekter Anlass für Kontaktaufnahme!"  
> "10 positive Interaktionen - Zeit für Upselling!"

### 💬 Team-Feedback:
> "Genial! Echter Vertriebs-Vorsprung. Customer Intelligence als Frühwarnsystem. KI-gestützte Vorschläge möglich."

## 🏗️ ARCHITEKTUR: 3-Schichten Intelligence System

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend Layer                        │
│  WarmthIndicator │ SuggestionPanel │ TrendChart         │
└────────────────────┬────────────────────────────────────┘
                     │ API Calls
┌────────────────────▼────────────────────────────────────┐
│                 Intelligence Service Layer               │
│  WarmthCalculator │ InteractionAnalyzer │ SuggestionEngine│
└────────────────────┬────────────────────────────────────┘
                     │ Database
┌────────────────────▼────────────────────────────────────┐
│                    Data Layer                            │
│  ContactInteractions │ CustomerContacts │ Opportunities │
└─────────────────────────────────────────────────────────┘
```

## 💾 DATABASE: Contact Interaction Entity (V201 Migration)

**Datei:** `backend/src/main/resources/db/migration/V201__create_contact_interactions_table.sql`

```sql
-- =====================================================
-- V201: Contact Interaction Tracking für Intelligence
-- =====================================================
-- Date: 2025-08-08
-- Author: FreshPlan Team
-- Purpose: Enable relationship warmth calculation
-- =====================================================

CREATE TABLE contact_interactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Foreign Keys
    contact_id UUID NOT NULL REFERENCES customer_contacts(id) ON DELETE CASCADE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    opportunity_id UUID REFERENCES opportunities(id),
    
    -- Interaction Details
    interaction_type VARCHAR(50) NOT NULL, -- EMAIL, CALL, MEETING, NOTE, TASK, VIEW
    interaction_subtype VARCHAR(50), -- INCOMING, OUTGOING, SCHEDULED, COMPLETED
    direction VARCHAR(20), -- INCOMING, OUTGOING, INTERNAL
    channel VARCHAR(50), -- EMAIL, PHONE, WHATSAPP, VIDEO, IN_PERSON, SYSTEM
    
    -- Content & Context
    subject VARCHAR(500),
    notes TEXT,
    duration_minutes INTEGER,
    
    -- Sentiment & Quality
    sentiment VARCHAR(20), -- POSITIVE, NEUTRAL, NEGATIVE, UNKNOWN
    response_received BOOLEAN DEFAULT false,
    response_time_hours INTEGER,
    quality_score INTEGER CHECK (quality_score >= 0 AND quality_score <= 100),
    
    -- Metadata
    interaction_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scheduled_date TIMESTAMP WITH TIME ZONE,
    completed_date TIMESTAMP WITH TIME ZONE,
    
    -- Tracking
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by UUID REFERENCES users(id),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by UUID REFERENCES users(id),
    
    -- Soft Delete
    deleted_at TIMESTAMP WITH TIME ZONE,
    deleted_by UUID REFERENCES users(id),
    
    -- Constraints
    CONSTRAINT interaction_type_check CHECK (
        interaction_type IN ('EMAIL', 'CALL', 'MEETING', 'NOTE', 'TASK', 'VIEW', 'WHATSAPP', 'SMS', 'SOCIAL')
    ),
    CONSTRAINT direction_check CHECK (
        direction IN ('INCOMING', 'OUTGOING', 'INTERNAL', 'SYSTEM')
    ),
    CONSTRAINT sentiment_check CHECK (
        sentiment IN ('POSITIVE', 'NEUTRAL', 'NEGATIVE', 'MIXED', 'UNKNOWN')
    )
);

-- Indexes für Performance
CREATE INDEX idx_contact_interactions_contact_id ON contact_interactions(contact_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_contact_interactions_customer_id ON contact_interactions(customer_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_contact_interactions_date ON contact_interactions(interaction_date DESC) WHERE deleted_at IS NULL;
CREATE INDEX idx_contact_interactions_type ON contact_interactions(interaction_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_contact_warmth_calc ON contact_interactions(contact_id, interaction_date DESC) 
    WHERE deleted_at IS NULL AND interaction_type != 'VIEW';

-- Trigger für updated_at
CREATE TRIGGER update_contact_interactions_updated_at
    BEFORE UPDATE ON contact_interactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Grant Permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON contact_interactions TO freshplan_app;
GRANT SELECT ON contact_interactions TO freshplan_readonly;

-- Sample Test Data (nur für Development)
-- INSERT INTO contact_interactions (contact_id, customer_id, user_id, interaction_type, direction, sentiment)
-- SELECT 
--     cc.id, cc.customer_id, u.id, 'CALL', 'OUTGOING', 'POSITIVE'
-- FROM customer_contacts cc
-- CROSS JOIN users u
-- WHERE u.username = 'admin@freshplan.de'
-- LIMIT 5;
```

## 🧮 WARMTH CALCULATION ALGORITHM

### 1. RelationshipWarmthService.java (BACKEND CORE)

**Datei:** `backend/src/main/java/de/freshplan/intelligence/RelationshipWarmthService.java`  
**Größe:** ~400 Zeilen  
**Verantwortung:** Zentrale Warmth-Berechnung und Orchestrierung

```java
// CLAUDE: Dies ist der Kern des Intelligence Systems
// Pfad: backend/src/main/java/de/freshplan/intelligence/RelationshipWarmthService.java

package de.freshplan.intelligence;

import de.freshplan.domain.interaction.entity.ContactInteraction;
import de.freshplan.domain.interaction.repository.ContactInteractionRepository;
import de.freshplan.intelligence.dto.*;
import io.quarkus.cache.CacheResult;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RelationshipWarmthService {
    
    private static final Logger log = LoggerFactory.getLogger(RelationshipWarmthService.class);
    
    @Inject
    ContactInteractionRepository interactionRepository;
    
    @Inject
    WarmthCalculator calculator;
    
    @Inject
    SuggestionEngine suggestionEngine;
    
    /**
     * Hauptmethode zur Warmth-Berechnung
     * Gewichtung: Recency 40%, Frequency 30%, Quality 20%, Value 10%
     */
    @CacheResult(cacheName = "warmth-cache")
    public RelationshipWarmth calculateWarmth(UUID contactId) {
        log.debug("Calculating warmth for contact: {}", contactId);
        
        // 1. Sammle Metriken
        WarmthMetrics metrics = collectMetrics(contactId);
        
        // 2. Berechne Score (0-100)
        WarmthScore score = calculator.calculateScore(metrics);
        
        // 3. Analysiere Trend
        WarmthTrend trend = analyzeTrend(contactId);
        
        // 4. Generiere Vorschläge
        List<ActionSuggestion> suggestions = suggestionEngine.generateSuggestions(
            contactId, score, metrics, trend
        );
        
        // 5. Baue Response
        return RelationshipWarmth.builder()
            .contactId(contactId)
            .warmthScore(score.getValue())
            .temperature(score.getTemperature())
            .freshnessLevel(determineFreshnessLevel(metrics))
            .trend(trend)
            .lastInteraction(metrics.getLastInteractionDate())
            .interactionCount(metrics.getTotalInteractions())
            .averageResponseTime(metrics.getAverageResponseTime())
            .suggestions(suggestions)
            .calculatedAt(Instant.now())
            .nextReviewDate(calculateNextReviewDate(score))
            .build();
    }
    
    /**
     * Sammelt alle relevanten Metriken für einen Kontakt
     */
    private WarmthMetrics collectMetrics(UUID contactId) {
        // Letzte 90 Tage betrachten
        LocalDateTime startDate = LocalDateTime.now().minusDays(90);
        
        List<ContactInteraction> interactions = interactionRepository
            .findByContactIdAndDateRange(contactId, startDate, LocalDateTime.now());
        
        if (interactions.isEmpty()) {
            return WarmthMetrics.emptyColdStart(); // ColdStart für neue Kontakte
        }
        
        // Berechne Basis-Metriken
        LocalDateTime lastInteraction = interactions.stream()
            .map(ContactInteraction::getInteractionDate)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        long daysSinceContact = lastInteraction != null ? 
            ChronoUnit.DAYS.between(lastInteraction, LocalDateTime.now()) : 999;
        
        // Interaktions-Frequenz (pro Monat)
        double monthlyRate = interactions.size() / 3.0; // 3 Monate Betrachtung
        
        // Response-Rate berechnen
        long outgoingCount = interactions.stream()
            .filter(i -> "OUTGOING".equals(i.getDirection()))
            .count();
        long responsesReceived = interactions.stream()
            .filter(i -> Boolean.TRUE.equals(i.getResponseReceived()))
            .count();
        double responseRate = outgoingCount > 0 ? 
            (double) responsesReceived / outgoingCount : 0.5;
        
        // Sentiment-Analyse
        Map<String, Long> sentimentCounts = interactions.stream()
            .collect(Collectors.groupingBy(
                ContactInteraction::getSentiment,
                Collectors.counting()
            ));
        
        double sentimentScore = calculateSentimentScore(sentimentCounts);
        
        // Interaktions-Qualität
        double avgQuality = interactions.stream()
            .filter(i -> i.getQualityScore() != null)
            .mapToInt(ContactInteraction::getQualityScore)
            .average()
            .orElse(50.0);
        
        return WarmthMetrics.builder()
            .contactId(contactId)
            .daysSinceLastContact(daysSinceContact)
            .lastInteractionDate(lastInteraction)
            .totalInteractions(interactions.size())
            .monthlyInteractionRate(monthlyRate)
            .responseRate(responseRate)
            .sentimentScore(sentimentScore)
            .averageQualityScore(avgQuality)
            .recentInteractionTypes(getRecentInteractionTypes(interactions))
            .averageResponseTime(calculateAvgResponseTime(interactions))
            .build();
    }
    
    /**
     * Analysiert Warmth-Trend über Zeit
     */
    private WarmthTrend analyzeTrend(UUID contactId) {
        // Vergleiche aktuelle Periode mit vorheriger
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        LocalDateTime sixtyDaysAgo = now.minusDays(60);
        
        int recentCount = interactionRepository.countByContactIdAndDateRange(
            contactId, thirtyDaysAgo, now
        );
        int previousCount = interactionRepository.countByContactIdAndDateRange(
            contactId, sixtyDaysAgo, thirtyDaysAgo
        );
        
        if (recentCount > previousCount * 1.2) {
            return WarmthTrend.IMPROVING;
        } else if (recentCount < previousCount * 0.8) {
            return WarmthTrend.DECLINING;
        } else {
            return WarmthTrend.STABLE;
        }
    }
    
    /**
     * Bestimmt Freshness-Level basierend auf letztem Kontakt
     */
    private DataFreshness determineFreshnessLevel(WarmthMetrics metrics) {
        long daysSince = metrics.getDaysSinceLastContact();
        
        if (daysSince <= 7) return DataFreshness.FRESH;
        if (daysSince <= 30) return DataFreshness.AGING;
        if (daysSince <= 60) return DataFreshness.STALE;
        return DataFreshness.CRITICAL;
    }
    
    /**
     * Berechnet Sentiment-Score aus Verteilung
     */
    private double calculateSentimentScore(Map<String, Long> sentimentCounts) {
        long total = sentimentCounts.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) return 50.0;
        
        long positive = sentimentCounts.getOrDefault("POSITIVE", 0L);
        long negative = sentimentCounts.getOrDefault("NEGATIVE", 0L);
        long neutral = sentimentCounts.getOrDefault("NEUTRAL", 0L);
        
        // Gewichtete Berechnung
        double score = (positive * 100.0 + neutral * 50.0 + negative * 0.0) / total;
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Ermittelt die letzten Interaktionstypen
     */
    private List<String> getRecentInteractionTypes(List<ContactInteraction> interactions) {
        return interactions.stream()
            .sorted((a, b) -> b.getInteractionDate().compareTo(a.getInteractionDate()))
            .limit(5)
            .map(ContactInteraction::getInteractionType)
            .collect(Collectors.toList());
    }
    
    /**
     * Berechnet durchschnittliche Response-Zeit in Stunden
     */
    private Double calculateAvgResponseTime(List<ContactInteraction> interactions) {
        List<Integer> responseTimes = interactions.stream()
            .filter(i -> i.getResponseTimeHours() != null)
            .map(ContactInteraction::getResponseTimeHours)
            .collect(Collectors.toList());
        
        if (responseTimes.isEmpty()) return null;
        
        return responseTimes.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }
    
    /**
     * Bestimmt nächstes Review-Datum basierend auf Score
     */
    private LocalDate calculateNextReviewDate(WarmthScore score) {
        // Heißere Kontakte seltener reviewen
        int daysUntilReview = switch (score.getTemperature()) {
            case HOT -> 30;
            case WARM -> 14;
            case COOLING -> 7;
            case COLD -> 3;
        };
        
        return LocalDate.now().plusDays(daysUntilReview);
    }
    
    /**
     * Scheduled Job: Warmth-Scores nachts aktualisieren
     */
    @Scheduled(cron = "0 0 2 * * ?") // Täglich um 2 Uhr nachts
    @Transactional
    public void updateAllWarmthScores() {
        log.info("Starting nightly warmth score update");
        
        List<UUID> allContactIds = interactionRepository.findAllActiveContactIds();
        int updated = 0;
        
        for (UUID contactId : allContactIds) {
            try {
                calculateWarmth(contactId); // Wird gecached
                updated++;
            } catch (Exception e) {
                log.error("Failed to update warmth for contact: {}", contactId, e);
            }
        }
        
        log.info("Completed warmth update for {} contacts", updated);
    }
}
```

### 2. WarmthCalculator.java (Score-Berechnung)

**Datei:** `backend/src/main/java/de/freshplan/intelligence/WarmthCalculator.java`  
**Größe:** ~200 Zeilen

```java
// CLAUDE: Score-Berechnung mit gewichteten Faktoren
// Pfad: backend/src/main/java/de/freshplan/intelligence/WarmthCalculator.java

package de.freshplan.intelligence;

import de.freshplan.intelligence.dto.*;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarmthCalculator {
    
    // Gewichtungen für Score-Berechnung
    private static final double WEIGHT_RECENCY = 0.40;
    private static final double WEIGHT_FREQUENCY = 0.30;
    private static final double WEIGHT_QUALITY = 0.20;
    private static final double WEIGHT_VALUE = 0.10;
    
    public WarmthScore calculateScore(WarmthMetrics metrics) {
        double score = 0.0;
        
        // 1. RECENCY (40%): Wie lange her ist der letzte Kontakt?
        score += calculateRecencyScore(metrics.getDaysSinceLastContact()) * WEIGHT_RECENCY;
        
        // 2. FREQUENCY (30%): Wie oft interagieren wir?
        score += calculateFrequencyScore(metrics.getMonthlyInteractionRate()) * WEIGHT_FREQUENCY;
        
        // 3. QUALITY (20%): Wie gut sind die Interaktionen?
        score += calculateQualityScore(metrics) * WEIGHT_QUALITY;
        
        // 4. VALUE (10%): Business-Wert des Kontakts
        score += calculateValueScore(metrics) * WEIGHT_VALUE;
        
        // Normalisiere auf 0-100
        score = Math.max(0, Math.min(100, score));
        
        // Bestimme Temperature
        Temperature temp = determineTemperature(score);
        
        return new WarmthScore(score, temp);
    }
    
    private double calculateRecencyScore(long daysSince) {
        if (daysSince <= 3) return 100;
        if (daysSince <= 7) return 85;
        if (daysSince <= 14) return 70;
        if (daysSince <= 30) return 50;
        if (daysSince <= 60) return 25;
        return 0;
    }
    
    private double calculateFrequencyScore(double monthlyRate) {
        if (monthlyRate >= 8) return 100;  // 2x pro Woche
        if (monthlyRate >= 4) return 80;   // 1x pro Woche
        if (monthlyRate >= 2) return 60;   // Alle 2 Wochen
        if (monthlyRate >= 1) return 40;   // 1x pro Monat
        if (monthlyRate >= 0.5) return 20; // Alle 2 Monate
        return 0;
    }
    
    private double calculateQualityScore(WarmthMetrics metrics) {
        double qualityScore = 50.0; // Basis
        
        // Response-Rate Einfluss
        qualityScore += (metrics.getResponseRate() - 0.5) * 30;
        
        // Sentiment Einfluss
        qualityScore += (metrics.getSentimentScore() - 50) * 0.4;
        
        // Quality Score direkt
        if (metrics.getAverageQualityScore() > 0) {
            qualityScore = (qualityScore + metrics.getAverageQualityScore()) / 2;
        }
        
        return Math.max(0, Math.min(100, qualityScore));
    }
    
    private double calculateValueScore(WarmthMetrics metrics) {
        // TODO: Integration mit Opportunity-Daten
        // Vorerst Basis-Score
        return 50.0;
    }
    
    private Temperature determineTemperature(double score) {
        if (score >= 80) return Temperature.HOT;
        if (score >= 60) return Temperature.WARM;
        if (score >= 40) return Temperature.COOLING;
        return Temperature.COLD;
    }
}
```

## 🤖 SUGGESTION ENGINE (Intelligente Vorschläge)

**Datei:** `backend/src/main/java/de/freshplan/intelligence/SuggestionEngine.java`

```java
// CLAUDE: Generiert intelligente Handlungsempfehlungen
// Pfad: backend/src/main/java/de/freshplan/intelligence/SuggestionEngine.java

package de.freshplan.intelligence;

import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.CustomerContactRepository;
import de.freshplan.intelligence.dto.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@ApplicationScoped
public class SuggestionEngine {
    
    @Inject
    CustomerContactRepository contactRepository;
    
    public List<ActionSuggestion> generateSuggestions(
        UUID contactId,
        WarmthScore score,
        WarmthMetrics metrics,
        WarmthTrend trend
    ) {
        List<ActionSuggestion> suggestions = new ArrayList<>();
        CustomerContact contact = contactRepository.findById(contactId).orElse(null);
        
        if (contact == null) return suggestions;
        
        // 1. URGENTE AKTIONEN (Geburtstag, kritischer Status)
        addUrgentSuggestions(suggestions, contact, score);
        
        // 2. TREND-BASIERTE VORSCHLÄGE
        addTrendBasedSuggestions(suggestions, trend, metrics);
        
        // 3. TEMPERATUR-BASIERTE VORSCHLÄGE
        addTemperatureBasedSuggestions(suggestions, score);
        
        // 4. OPPORTUNITY-BASIERTE VORSCHLÄGE
        addOpportunityBasedSuggestions(suggestions, contact, score);
        
        // Sortiere nach Priorität
        suggestions.sort(Comparator.comparing(ActionSuggestion::getUrgency));
        
        // Maximal 5 Vorschläge
        return suggestions.stream().limit(5).toList();
    }
    
    private void addUrgentSuggestions(
        List<ActionSuggestion> suggestions,
        CustomerContact contact,
        WarmthScore score
    ) {
        // Geburtstag Check
        if (contact.getBirthday() != null) {
            long daysUntilBirthday = calculateDaysUntilBirthday(contact.getBirthday());
            
            if (daysUntilBirthday == 0) {
                suggestions.add(ActionSuggestion.builder()
                    .type(ActionType.CALL)
                    .urgency(Urgency.URGENT)
                    .reason("🎂 Heute ist der Geburtstag!")
                    .suggestedAction("Persönlich anrufen und gratulieren")
                    .expectedImpact("Sehr positive Beziehungswirkung")
                    .build());
            } else if (daysUntilBirthday > 0 && daysUntilBirthday <= 7) {
                suggestions.add(ActionSuggestion.builder()
                    .type(ActionType.SCHEDULE)
                    .urgency(Urgency.HIGH)
                    .reason(String.format("🎂 Geburtstag in %d Tagen", daysUntilBirthday))
                    .suggestedAction("Erinnerung für Geburtstagsanruf setzen")
                    .expectedImpact("Beziehungspflege")
                    .build());
            }
        }
        
        // Kritischer Status
        if (score.getTemperature() == Temperature.COLD) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.REACH_OUT)
                .urgency(Urgency.HIGH)
                .reason("❄️ Kontakt ist sehr kalt geworden")
                .suggestedAction("Dringend persönlichen Kontakt aufnehmen")
                .expectedImpact("Reaktivierung der Beziehung")
                .build());
        }
    }
    
    private void addTrendBasedSuggestions(
        List<ActionSuggestion> suggestions,
        WarmthTrend trend,
        WarmthMetrics metrics
    ) {
        switch (trend) {
            case IMPROVING -> suggestions.add(ActionSuggestion.builder()
                .type(ActionType.CAPITALIZE)
                .urgency(Urgency.MEDIUM)
                .reason("📈 Beziehung verbessert sich")
                .suggestedAction("Momentum nutzen - Geschäftschance besprechen")
                .expectedImpact("Upselling-Potential")
                .build());
                
            case DECLINING -> suggestions.add(ActionSuggestion.builder()
                .type(ActionType.REACH_OUT)
                .urgency(Urgency.HIGH)
                .reason("📉 Beziehung verschlechtert sich")
                .suggestedAction("Check-in Call vereinbaren")
                .expectedImpact("Beziehung stabilisieren")
                .build());
                
            case STABLE -> {
                if (metrics.getMonthlyInteractionRate() < 2) {
                    suggestions.add(ActionSuggestion.builder()
                        .type(ActionType.ENGAGE)
                        .urgency(Urgency.LOW)
                        .reason("💤 Wenig Aktivität")
                        .suggestedAction("Interessanten Content teilen")
                        .expectedImpact("Engagement erhöhen")
                        .build());
                }
            }
        }
    }
    
    private void addTemperatureBasedSuggestions(
        List<ActionSuggestion> suggestions,
        WarmthScore score
    ) {
        switch (score.getTemperature()) {
            case HOT -> suggestions.add(ActionSuggestion.builder()
                .type(ActionType.CAPITALIZE)
                .urgency(Urgency.MEDIUM)
                .reason("🔥 Beziehung ist sehr warm")
                .suggestedAction("Cross-Selling oder Referral anfragen")
                .expectedImpact("Geschäftswachstum")
                .build());
                
            case COOLING -> suggestions.add(ActionSuggestion.builder()
                .type(ActionType.ENGAGE)
                .urgency(Urgency.MEDIUM)
                .reason("⚠️ Beziehung kühlt ab")
                .suggestedAction("Persönliche Nachricht senden")
                .expectedImpact("Beziehung aufwärmen")
                .build());
        }
    }
    
    private void addOpportunityBasedSuggestions(
        List<ActionSuggestion> suggestions,
        CustomerContact contact,
        WarmthScore score
    ) {
        // Wenn Beziehung warm und keine aktive Opportunity
        if (score.getValue() > 70 && !hasActiveOpportunity(contact)) {
            suggestions.add(ActionSuggestion.builder()
                .type(ActionType.OPPORTUNITY)
                .urgency(Urgency.MEDIUM)
                .reason("💰 Gute Beziehung ohne aktive Opportunity")
                .suggestedAction("Neue Geschäftsmöglichkeiten erkunden")
                .expectedImpact("Neue Verkaufschance")
                .build());
        }
    }
    
    private long calculateDaysUntilBirthday(LocalDate birthday) {
        LocalDate today = LocalDate.now();
        LocalDate nextBirthday = birthday.withYear(today.getYear());
        
        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        
        return ChronoUnit.DAYS.between(today, nextBirthday);
    }
    
    private boolean hasActiveOpportunity(CustomerContact contact) {
        // TODO: Check active opportunities
        return false;
    }
}
```

## 🎨 FRONTEND COMPONENTS

### WarmthScoreCalculator.ts (Frontend Mirror)

**Datei:** `frontend/src/features/customers/components/intelligence/WarmthScoreCalculator.ts`

```typescript
// CLAUDE: Frontend-Version des Warmth Calculators für Offline-Fähigkeit
// Pfad: frontend/src/features/customers/components/intelligence/WarmthScoreCalculator.ts

import { ContactIntelligence, DataFreshness, WarmthTrend } from '../../types/contact.types';
import { ContactInteraction } from '../../types/interaction.types';

export class WarmthScoreCalculator {
  // Spiegelt Backend-Logik für Offline-Berechnung
  
  calculateWarmth(interactions: ContactInteraction[]): ContactIntelligence {
    const metrics = this.collectMetrics(interactions);
    const score = this.calculateScore(metrics);
    const trend = this.analyzeTrend(interactions);
    
    return {
      warmthScore: score,
      temperature: this.getTemperature(score),
      freshnessLevel: this.getFreshnessLevel(metrics.daysSince),
      trendDirection: trend,
      lastInteractionDate: metrics.lastInteraction,
      interactionCount: interactions.length,
      suggestions: [], // Werden vom Backend geladen
      calculatedAt: new Date()
    };
  }
  
  private collectMetrics(interactions: ContactInteraction[]) {
    if (interactions.length === 0) {
      return {
        daysSince: 999,
        monthlyRate: 0,
        responseRate: 0.5,
        sentimentScore: 50,
        lastInteraction: null
      };
    }
    
    // Sortiere nach Datum
    const sorted = [...interactions].sort((a, b) => 
      new Date(b.interactionDate).getTime() - new Date(a.interactionDate).getTime()
    );
    
    const lastInteraction = new Date(sorted[0].interactionDate);
    const daysSince = Math.floor(
      (Date.now() - lastInteraction.getTime()) / (1000 * 60 * 60 * 24)
    );
    
    // Berechne monatliche Rate (letzte 3 Monate)
    const threeMonthsAgo = new Date();
    threeMonthsAgo.setMonth(threeMonthsAgo.getMonth() - 3);
    const recentInteractions = interactions.filter(i => 
      new Date(i.interactionDate) > threeMonthsAgo
    );
    const monthlyRate = recentInteractions.length / 3;
    
    // Response Rate
    const outgoing = interactions.filter(i => i.direction === 'OUTGOING');
    const responses = outgoing.filter(i => i.responseReceived);
    const responseRate = outgoing.length > 0 ? responses.length / outgoing.length : 0.5;
    
    // Sentiment Score
    const sentiments = interactions.map(i => i.sentiment);
    const sentimentScore = this.calculateSentimentScore(sentiments);
    
    return {
      daysSince,
      monthlyRate,
      responseRate,
      sentimentScore,
      lastInteraction
    };
  }
  
  private calculateScore(metrics: any): number {
    let score = 0;
    
    // Recency (40%)
    if (metrics.daysSince <= 3) score += 40;
    else if (metrics.daysSince <= 7) score += 34;
    else if (metrics.daysSince <= 14) score += 28;
    else if (metrics.daysSince <= 30) score += 20;
    else if (metrics.daysSince <= 60) score += 10;
    
    // Frequency (30%)
    if (metrics.monthlyRate >= 8) score += 30;
    else if (metrics.monthlyRate >= 4) score += 24;
    else if (metrics.monthlyRate >= 2) score += 18;
    else if (metrics.monthlyRate >= 1) score += 12;
    else if (metrics.monthlyRate >= 0.5) score += 6;
    
    // Quality (20%)
    score += (metrics.responseRate * 10);
    score += (metrics.sentimentScore / 100 * 10);
    
    // Value (10%) - Placeholder
    score += 5;
    
    return Math.min(100, Math.max(0, score));
  }
  
  private getTemperature(score: number): 'HOT' | 'WARM' | 'COOLING' | 'COLD' {
    if (score >= 80) return 'HOT';
    if (score >= 60) return 'WARM';
    if (score >= 40) return 'COOLING';
    return 'COLD';
  }
  
  private getFreshnessLevel(daysSince: number): DataFreshness {
    if (daysSince <= 7) return 'fresh';
    if (daysSince <= 30) return 'aging';
    if (daysSince <= 60) return 'stale';
    return 'critical';
  }
  
  private analyzeTrend(interactions: ContactInteraction[]): WarmthTrend {
    const now = new Date();
    const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    const sixtyDaysAgo = new Date(now.getTime() - 60 * 24 * 60 * 60 * 1000);
    
    const recent = interactions.filter(i => 
      new Date(i.interactionDate) > thirtyDaysAgo
    ).length;
    
    const previous = interactions.filter(i => {
      const date = new Date(i.interactionDate);
      return date > sixtyDaysAgo && date <= thirtyDaysAgo;
    }).length;
    
    if (recent > previous * 1.2) return 'improving';
    if (recent < previous * 0.8) return 'declining';
    return 'stable';
  }
  
  private calculateSentimentScore(sentiments: string[]): number {
    if (sentiments.length === 0) return 50;
    
    const scores = sentiments.map(s => {
      switch(s) {
        case 'POSITIVE': return 100;
        case 'NEUTRAL': return 50;
        case 'NEGATIVE': return 0;
        default: return 50;
      }
    });
    
    return scores.reduce((a, b) => a + b, 0) / scores.length;
  }
}

// Singleton Export
export const warmthScoreCalculator = new WarmthScoreCalculator();
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE FÜR CLAUDE

### Phase 1: Database & Backend (45 Min)
- [ ] V201 Migration ausführen (ContactInteraction Table)
- [ ] ContactInteraction Entity erstellen (150 Zeilen)
- [ ] ContactInteractionRepository erstellen (100 Zeilen)
- [ ] RelationshipWarmthService implementieren (400 Zeilen)
- [ ] WarmthCalculator implementieren (200 Zeilen)
- [ ] SuggestionEngine implementieren (300 Zeilen)

### Phase 2: Frontend Components (30 Min)
- [ ] WarmthScoreCalculator.ts implementieren (200 Zeilen)
- [ ] InteractionAnalyzer.ts implementieren (150 Zeilen)
- [ ] SuggestionEngine.tsx implementieren (250 Zeilen)
- [ ] Integration in ContactStore

### Phase 3: REST API (20 Min)
- [ ] ContactIntelligenceResource erstellen
- [ ] Endpoints implementieren:
  - GET /api/contacts/{id}/intelligence
  - GET /api/contacts/warmth-alerts
  - POST /api/contacts/{id}/interactions

### Phase 4: Testing (30 Min)
- [ ] RelationshipWarmthServiceTest schreiben
- [ ] WarmthCalculatorTest schreiben
- [ ] Frontend Calculator Tests
- [ ] Integration Tests

### Phase 5: Integration (15 Min)
- [ ] In SmartContactCard einbinden
- [ ] Dashboard-Widget erstellen
- [ ] Scheduled Jobs konfigurieren

## 🔗 INTEGRATION POINTS

### Mit Smart Contact Cards:
```typescript
// In SmartContactCard.tsx
const { warmth } = useContactIntelligence(contact.id);

<WarmthIndicator 
  warmthScore={warmth.warmthScore}
  freshnessLevel={warmth.freshnessLevel}
  showDetails={true}
/>
```

### Mit Mobile Actions:
```typescript
// In ActionSuggestionService.ts
const warmth = await warmthScoreCalculator.calculateWarmth(interactions);
const suggestions = generateContextualActions(contact, warmth);
```

### Mit Contact Timeline:
```typescript
// Timeline zeigt Warmth-Verlauf
<WarmthTrendChart 
  contactId={contact.id}
  period="3months"
/>
```

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Keine Interaktionen = Cold Start**
   → Lösung: Default Warmth Score 50 für neue Kontakte

2. **Zu viele Suggestions**
   → Lösung: Maximal 5, nach Priorität sortiert

3. **Performance bei vielen Kontakten**
   → Lösung: Caching + Nightly Batch Updates

4. **Offline-Berechnung ungenau**
   → Lösung: Frontend-Calculator als Approximation, Backend als Source of Truth

## 🚀 NÄCHSTE SCHRITTE NACH IMPLEMENTIERUNG

1. **Contact Timeline** implementieren
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md)

2. **Smart Suggestions** ausarbeiten
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_SUGGESTIONS.md)

3. **Testing Integration** durchführen
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/TESTING_INTEGRATION.md)

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 140 Minuten  
**Kritischer Pfad:** V201 Migration → Backend Services → Frontend Integration  
**Nächstes Dokument:** [→ Contact Timeline](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md)  
**Parent:** [↑ Step3 Übersicht](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md)

**Relationship Intelligence = Proaktive Vertriebsintelligenz! 🌡️🔥🎯**