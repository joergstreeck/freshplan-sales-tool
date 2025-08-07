# 🔄 Data Strategy Intelligence - Kaltstart-Problem lösen

**Phase:** 0 - Critical Foundation  
**Priorität:** 🔴 KRITISCH - Ohne diese scheitern Intelligence Features  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/OFFLINE_CONFLICT_RESOLUTION.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Blockiert:**
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/RELATIONSHIP_INTELLIGENCE.md`
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md`
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_SUGGESTIONS.md`

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Kaltstart-Defaults implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Default-Konstanten erstellen
mkdir -p backend/src/main/java/de/freshplan/intelligence/defaults
touch backend/src/main/java/de/freshplan/intelligence/defaults/ColdStartDefaults.java
touch backend/src/main/java/de/freshplan/intelligence/defaults/DataFreshnessCalculator.java

# 2. Frontend Data Freshness Service
mkdir -p frontend/src/features/customers/services/freshness
touch frontend/src/features/customers/services/freshness/DataFreshnessService.ts
touch frontend/src/features/customers/services/freshness/ColdStartStrategy.ts

# 3. Migration für Default-Werte (nächste freie Nummer prüfen!)
ls -la backend/src/main/resources/db/migration/ | tail -5
# Erstelle V[NEXT]__add_default_warmth_values.sql

# 4. Tests vorbereiten
touch backend/src/test/java/de/freshplan/intelligence/defaults/ColdStartDefaultsTest.java
touch frontend/src/features/customers/services/freshness/__tests__/DataFreshnessService.test.ts
```

## 🎯 Das Problem: Keine Historie = Keine Intelligenz

**Ohne Daten funktionieren nicht:**
- 🌡️ Warmth Score (braucht Interaktions-Historie)
- 📊 Timeline (braucht vergangene Events)
- 💡 Smart Suggestions (braucht Muster)
- 📈 Analytics (braucht Datenbasis)

## 💡 DIE LÖSUNG: 4-Stufige Kaltstart-Strategie

### 1. Data Freshness Classification

**Datei:** `frontend/src/features/customers/types/freshness.types.ts`

```typescript
// CLAUDE: Basis-Types für Data Freshness
// Pfad: frontend/src/features/customers/types/freshness.types.ts

export enum DataFreshness {
  FRESH = "fresh",              // < 7 Tage - Grün (#4CAF50)
  AGING = "aging",              // 7-30 Tage - Gelb (#FFC107)
  STALE = "stale",              // 30-90 Tage - Orange (#FF9800)
  CRITICAL = "critical",        // > 90 Tage - Rot (#F44336)
  NO_CONTACT = "no_contact",   // Nie kontaktiert - Grau (#9E9E9E)
  NEW = "new"                   // < 24h alt - Blau (#2196F3)
}

export interface DataFreshnessMetrics {
  level: DataFreshness;
  daysSinceLastUpdate: number;
  daysSinceLastContact: number;
  completenessScore: number;      // 0-100%
  qualityScore: number;           // 0-100%
  recommendations: string[];
  nextActionDate?: Date;
}

export interface ColdStartDefaults {
  warmthScore: 50;               // Neutral start
  expectedInteractionDays: 30;   // Erwartung: Kontakt alle 30 Tage
  initialSuggestions: string[];  // Onboarding-Vorschläge
  defaultPriority: 'medium';
  defaultTags: ['neu', 'unqualifiziert'];
}
```

### 2. Backend ColdStart Defaults

**Datei:** `backend/src/main/java/de/freshplan/intelligence/defaults/ColdStartDefaults.java`

```java
// CLAUDE: Default-Werte für neue Kontakte ohne Historie
// Pfad: backend/src/main/java/de/freshplan/intelligence/defaults/ColdStartDefaults.java

package de.freshplan.intelligence.defaults;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ColdStartDefaults {
    
    // Warmth Score Defaults
    public static final int DEFAULT_WARMTH_SCORE = 50;          // Neutral
    public static final int NEW_CONTACT_WARMTH_BOOST = 10;      // +10 für neue Kontakte
    public static final int PRIMARY_CONTACT_BOOST = 15;         // +15 für Hauptkontakte
    public static final int DECISION_MAKER_BOOST = 20;          // +20 für Entscheider
    
    // Interaction Expectations
    public static final int EXPECTED_INTERACTION_DAYS = 30;     // Alle 30 Tage
    public static final int CRITICAL_SILENCE_DAYS = 90;         // Kritisch nach 90 Tagen
    public static final int WARNING_SILENCE_DAYS = 60;          // Warnung nach 60 Tagen
    
    // Initial Suggestions für neue Kontakte
    public static final List<String> INITIAL_SUGGESTIONS = List.of(
        "Willkommens-E-Mail senden",
        "LinkedIn-Profil recherchieren",
        "Kennenlern-Termin vereinbaren",
        "Produktportfolio vorstellen",
        "Newsletter-Anmeldung anbieten"
    );
    
    // Suggestions nach Kontakt-Typ
    public Map<String, List<String>> getSuggestionsByRole(String decisionLevel) {
        return Map.of(
            "EXECUTIVE", List.of(
                "Executive Summary vorbereiten",
                "ROI-Kalkulation erstellen",
                "Referenzkunden präsentieren"
            ),
            "MANAGER", List.of(
                "Prozess-Optimierung diskutieren",
                "Team-Demo vereinbaren",
                "Best Practices teilen"
            ),
            "OPERATIONAL", List.of(
                "Technische Details klären",
                "Hands-on Demo zeigen",
                "Support-Optionen erläutern"
            ),
            "INFLUENCER", List.of(
                "Meinungsführer einbinden",
                "Feedback einholen",
                "Als Multiplikator gewinnen"
            )
        );
    }
    
    // Intelligente Warmth-Berechnung für neue Kontakte
    public int calculateInitialWarmth(ContactDTO contact) {
        int warmth = DEFAULT_WARMTH_SCORE;
        
        // Boost basierend auf Rolle
        if (contact.isPrimary()) {
            warmth += PRIMARY_CONTACT_BOOST;
        }
        
        if ("EXECUTIVE".equals(contact.getDecisionLevel())) {
            warmth += DECISION_MAKER_BOOST;
        }
        
        // Boost basierend auf Vollständigkeit
        int completeness = calculateCompleteness(contact);
        if (completeness > 80) {
            warmth += 10;
        } else if (completeness > 60) {
            warmth += 5;
        }
        
        // Boost wenn von Vertrieb erfasst (nicht importiert)
        if (contact.getSource() == ContactSource.MANUAL_ENTRY) {
            warmth += 5;
        }
        
        return Math.min(warmth, 100);
    }
    
    private int calculateCompleteness(ContactDTO contact) {
        int score = 0;
        int fields = 0;
        
        if (contact.getFirstName() != null) { score++; }
        if (contact.getLastName() != null) { score++; }
        if (contact.getEmail() != null) { score++; }
        if (contact.getPhone() != null) { score++; }
        if (contact.getMobile() != null) { score++; }
        if (contact.getPosition() != null) { score++; }
        if (contact.getDecisionLevel() != null) { score++; }
        if (contact.getDepartment() != null) { score++; }
        fields = 8;
        
        return (score * 100) / fields;
    }
}
```

### 3. Data Freshness Service (Frontend)

**Datei:** `frontend/src/features/customers/services/freshness/DataFreshnessService.ts`

```typescript
// CLAUDE: Service für Data Freshness Berechnung und Visualisierung
// Pfad: frontend/src/features/customers/services/freshness/DataFreshnessService.ts

import { DataFreshness, DataFreshnessMetrics } from '../../types/freshness.types';
import { CustomerContact } from '../../types/contact.types';
import { differenceInDays, addDays } from 'date-fns';

export class DataFreshnessService {
  
  /**
   * Berechnet Freshness-Level für einen Kontakt
   */
  calculateFreshness(contact: CustomerContact): DataFreshnessMetrics {
    const now = new Date();
    const lastUpdate = contact.updatedAt ? new Date(contact.updatedAt) : new Date(contact.createdAt);
    const lastContact = contact.lastContactDate ? new Date(contact.lastContactDate) : null;
    
    const daysSinceUpdate = differenceInDays(now, lastUpdate);
    const daysSinceContact = lastContact ? differenceInDays(now, lastContact) : -1;
    
    // Freshness Level bestimmen
    let level: DataFreshness;
    if (daysSinceContact === -1) {
      level = DataFreshness.NO_CONTACT;
    } else if (daysSinceContact < 7) {
      level = DataFreshness.FRESH;
    } else if (daysSinceContact < 30) {
      level = DataFreshness.AGING;
    } else if (daysSinceContact < 90) {
      level = DataFreshness.STALE;
    } else {
      level = DataFreshness.CRITICAL;
    }
    
    // Wenn Kontakt sehr neu ist
    if (differenceInDays(now, new Date(contact.createdAt)) < 1) {
      level = DataFreshness.NEW;
    }
    
    // Completeness Score berechnen
    const completenessScore = this.calculateCompleteness(contact);
    
    // Quality Score berechnen
    const qualityScore = this.calculateQualityScore(contact, daysSinceContact);
    
    // Recommendations generieren
    const recommendations = this.generateRecommendations(level, contact, completenessScore);
    
    // Next Action Date
    const nextActionDate = this.calculateNextActionDate(level, lastContact);
    
    return {
      level,
      daysSinceLastUpdate: daysSinceUpdate,
      daysSinceLastContact: daysSinceContact,
      completenessScore,
      qualityScore,
      recommendations,
      nextActionDate
    };
  }
  
  private calculateCompleteness(contact: CustomerContact): number {
    const requiredFields = [
      'firstName', 'lastName', 'email', 'phone',
      'position', 'decisionLevel', 'department'
    ];
    
    const optionalFields = [
      'mobile', 'linkedIn', 'birthday', 'hobbies',
      'personalNotes', 'communicationPreference'
    ];
    
    let requiredScore = 0;
    let optionalScore = 0;
    
    requiredFields.forEach(field => {
      if (contact[field]) requiredScore++;
    });
    
    optionalFields.forEach(field => {
      if (contact[field]) optionalScore++;
    });
    
    // 70% Gewichtung für Required, 30% für Optional
    const required = (requiredScore / requiredFields.length) * 70;
    const optional = (optionalScore / optionalFields.length) * 30;
    
    return Math.round(required + optional);
  }
  
  private calculateQualityScore(contact: CustomerContact, daysSinceContact: number): number {
    let score = 100;
    
    // Abzug für fehlenden Kontakt
    if (daysSinceContact === -1) {
      score -= 30;
    } else if (daysSinceContact > 90) {
      score -= 40;
    } else if (daysSinceContact > 60) {
      score -= 25;
    } else if (daysSinceContact > 30) {
      score -= 10;
    }
    
    // Abzug für unvollständige Daten
    const completeness = this.calculateCompleteness(contact);
    if (completeness < 50) {
      score -= 20;
    } else if (completeness < 70) {
      score -= 10;
    }
    
    // Bonus für Primary Contact
    if (contact.isPrimary) {
      score += 10;
    }
    
    // Bonus für Decision Maker
    if (contact.decisionLevel === 'EXECUTIVE') {
      score += 15;
    }
    
    return Math.max(0, Math.min(100, score));
  }
  
  private generateRecommendations(
    level: DataFreshness,
    contact: CustomerContact,
    completeness: number
  ): string[] {
    const recommendations: string[] = [];
    
    // Freshness-basierte Empfehlungen
    switch (level) {
      case DataFreshness.NO_CONTACT:
        recommendations.push('🎯 Ersten Kontakt herstellen');
        recommendations.push('📧 Willkommens-E-Mail senden');
        break;
        
      case DataFreshness.NEW:
        recommendations.push('📋 Kontaktdaten vervollständigen');
        recommendations.push('🤝 Kennenlern-Termin vereinbaren');
        break;
        
      case DataFreshness.CRITICAL:
        recommendations.push('🚨 Dringend kontaktieren');
        recommendations.push('🔄 Beziehung reaktivieren');
        break;
        
      case DataFreshness.STALE:
        recommendations.push('📞 Follow-up Call durchführen');
        recommendations.push('📊 Status-Update einholen');
        break;
    }
    
    // Completeness-basierte Empfehlungen
    if (completeness < 50) {
      recommendations.push('📝 Fehlende Kontaktdaten ergänzen');
    }
    
    if (!contact.mobile) {
      recommendations.push('📱 Mobilnummer erfragen');
    }
    
    if (!contact.linkedIn) {
      recommendations.push('💼 LinkedIn-Profil verknüpfen');
    }
    
    if (!contact.birthday) {
      recommendations.push('🎂 Geburtstag erfragen');
    }
    
    return recommendations.slice(0, 5); // Max 5 Empfehlungen
  }
  
  private calculateNextActionDate(level: DataFreshness, lastContact: Date | null): Date {
    const now = new Date();
    
    switch (level) {
      case DataFreshness.NO_CONTACT:
      case DataFreshness.NEW:
        return addDays(now, 1); // Morgen
        
      case DataFreshness.CRITICAL:
        return now; // Heute!
        
      case DataFreshness.STALE:
        return addDays(now, 3); // In 3 Tagen
        
      case DataFreshness.FRESH:
        return addDays(now, 30); // In 30 Tagen
        
      case DataFreshness.AGING:
        return addDays(now, 14); // In 2 Wochen
        
      default:
        return addDays(now, 7);
    }
  }
}

// Singleton Export
export const dataFreshnessService = new DataFreshnessService();
```

### 4. Migration für Default-Werte

**Datei:** `backend/src/main/resources/db/migration/V[NEXT]__add_default_warmth_values.sql`

```sql
-- CLAUDE: Füge Default-Werte für Kaltstart hinzu
-- WICHTIG: Ersetze [NEXT] mit der nächsten freien Nummer!

-- Default Warmth für bestehende Kontakte ohne Interaktionen
UPDATE customer_contacts 
SET warmth_score = 50
WHERE warmth_score IS NULL 
  AND NOT EXISTS (
    SELECT 1 FROM contact_interactions 
    WHERE contact_id = customer_contacts.id
  );

-- Boost für Primary Contacts
UPDATE customer_contacts 
SET warmth_score = warmth_score + 15
WHERE is_primary = true 
  AND warmth_score IS NOT NULL;

-- Boost für Decision Makers
UPDATE customer_contacts 
SET warmth_score = warmth_score + 20
WHERE decision_level IN ('EXECUTIVE', 'C_LEVEL')
  AND warmth_score IS NOT NULL;

-- Cap bei 100
UPDATE customer_contacts 
SET warmth_score = 100
WHERE warmth_score > 100;

-- Freshness Level als computed column
ALTER TABLE customer_contacts 
ADD COLUMN IF NOT EXISTS freshness_level VARCHAR(20) 
GENERATED ALWAYS AS (
  CASE 
    WHEN last_contact_date IS NULL THEN 'NO_CONTACT'
    WHEN last_contact_date >= CURRENT_DATE - INTERVAL '7 days' THEN 'FRESH'
    WHEN last_contact_date >= CURRENT_DATE - INTERVAL '30 days' THEN 'AGING'
    WHEN last_contact_date >= CURRENT_DATE - INTERVAL '90 days' THEN 'STALE'
    ELSE 'CRITICAL'
  END
) STORED;

-- Index für Performance
CREATE INDEX IF NOT EXISTS idx_contacts_freshness 
ON customer_contacts(freshness_level)
WHERE deleted_at IS NULL;
```

## 📊 Data Hygiene Dashboard

**Datei:** `frontend/src/features/customers/components/intelligence/DataHygieneDashboard.tsx`

```typescript
// CLAUDE: Dashboard für Data Quality Monitoring
// Pfad: frontend/src/features/customers/components/intelligence/DataHygieneDashboard.tsx

import React from 'react';
import { Card, CardContent, Grid, Typography, LinearProgress, Chip } from '@mui/material';
import { DataFreshness } from '../../types/freshness.types';
import { useContactStore } from '../../stores/contactStore';

export const DataHygieneDashboard: React.FC = () => {
  const { contacts } = useContactStore();
  
  const stats = React.useMemo(() => {
    const freshnessCount = {
      [DataFreshness.FRESH]: 0,
      [DataFreshness.AGING]: 0,
      [DataFreshness.STALE]: 0,
      [DataFreshness.CRITICAL]: 0,
      [DataFreshness.NO_CONTACT]: 0,
      [DataFreshness.NEW]: 0
    };
    
    contacts.forEach(contact => {
      const freshness = dataFreshnessService.calculateFreshness(contact);
      freshnessCount[freshness.level]++;
    });
    
    const total = contacts.length || 1;
    const healthScore = (
      (freshnessCount[DataFreshness.FRESH] * 100) +
      (freshnessCount[DataFreshness.AGING] * 70) +
      (freshnessCount[DataFreshness.NEW] * 80) +
      (freshnessCount[DataFreshness.STALE] * 30)
    ) / total;
    
    return {
      total,
      freshnessCount,
      healthScore: Math.round(healthScore),
      criticalCount: freshnessCount[DataFreshness.CRITICAL],
      needsAction: freshnessCount[DataFreshness.CRITICAL] + freshnessCount[DataFreshness.STALE]
    };
  }, [contacts]);
  
  return (
    <Grid container spacing={3}>
      <Grid item xs={12}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Datenqualität Dashboard
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={12} md={4}>
                <Typography variant="subtitle2" color="text.secondary">
                  Gesundheitsscore
                </Typography>
                <Typography variant="h3">
                  {stats.healthScore}%
                </Typography>
                <LinearProgress 
                  variant="determinate" 
                  value={stats.healthScore}
                  color={stats.healthScore > 70 ? 'success' : 'warning'}
                />
              </Grid>
              
              <Grid item xs={12} md={8}>
                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                  Kontakt-Freshness Verteilung
                </Typography>
                <Grid container spacing={1}>
                  <Grid item>
                    <Chip 
                      label={`Fresh: ${stats.freshnessCount[DataFreshness.FRESH]}`}
                      color="success"
                      size="small"
                    />
                  </Grid>
                  <Grid item>
                    <Chip 
                      label={`Aging: ${stats.freshnessCount[DataFreshness.AGING]}`}
                      style={{ backgroundColor: '#FFC107', color: 'white' }}
                      size="small"
                    />
                  </Grid>
                  <Grid item>
                    <Chip 
                      label={`Stale: ${stats.freshnessCount[DataFreshness.STALE]}`}
                      style={{ backgroundColor: '#FF9800', color: 'white' }}
                      size="small"
                    />
                  </Grid>
                  <Grid item>
                    <Chip 
                      label={`Critical: ${stats.freshnessCount[DataFreshness.CRITICAL]}`}
                      color="error"
                      size="small"
                    />
                  </Grid>
                </Grid>
              </Grid>
            </Grid>
            
            {stats.criticalCount > 0 && (
              <Alert severity="warning" sx={{ mt: 2 }}>
                <AlertTitle>Aktion erforderlich</AlertTitle>
                {stats.criticalCount} Kontakte benötigen dringend Aufmerksamkeit!
              </Alert>
            )}
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  );
};
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Basis (30 Min)
- [ ] ColdStartDefaults.java erstellen
- [ ] freshness.types.ts definieren
- [ ] Migration für Default-Werte

### Phase 2: Services (45 Min)
- [ ] DataFreshnessService.ts implementieren
- [ ] ColdStartStrategy im Backend
- [ ] Default-Suggestions generieren

### Phase 3: UI Integration (30 Min)
- [ ] DataHygieneDashboard komponente
- [ ] Freshness-Indicator in ContactCard
- [ ] Recommendations in UI anzeigen

### Phase 4: Testing (30 Min)
- [ ] Unit Tests für Freshness-Berechnung
- [ ] Integration Tests für Defaults
- [ ] E2E Test für Kaltstart

## 🔗 INTEGRATION POINTS

### Wo wird Data Strategy verwendet?

1. **ContactCard** - Freshness Badge anzeigen
2. **ContactTimeline** - Default-Events bei neuen Kontakten
3. **SmartSuggestions** - Kaltstart-Vorschläge
4. **WarmthCalculator** - Initial-Werte verwenden

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Null-Pointer bei neuen Kontakten**
   → Lösung: Immer Default-Werte verwenden

2. **Leere Timeline frustriert User**
   → Lösung: "Getting Started" Events einfügen

3. **Generische Suggestions**
   → Lösung: Rolle-basierte Vorschläge

## 🚀 NÄCHSTE SCHRITTE

1. **Offline Conflict Resolution** implementieren
2. **Cost Management** einrichten
3. **In-App Help** hinzufügen

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 135 Minuten  
**Nächstes Dokument:** [→ Offline Conflict Resolution](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/OFFLINE_CONFLICT_RESOLUTION.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Data Strategy = Intelligenz vom ersten Tag! 🚀**