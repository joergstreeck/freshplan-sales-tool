# 🎯 Smart Suggestions - Proaktive Handlungsempfehlungen

**Phase:** 2 - Intelligence Features  
**Tag:** 3 der Woche 2  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/TESTING_INTEGRATION.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Voraussetzungen:**
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/RELATIONSHIP_INTELLIGENCE.md` ✅
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md` ✅

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Pfade für Copy & Paste:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Migration prüfen (WICHTIG: Nächste freie Nummer ermitteln!)
ls -la backend/src/main/resources/db/migration/ | tail -5
# → Verwende die nächste freie V-Nummer für neue Migrationen

# 2. Suggestion Types erstellen
mkdir -p frontend/src/features/customers/types
touch frontend/src/features/customers/types/suggestions.types.ts

# 3. Suggestion Engine erstellen
mkdir -p frontend/src/features/customers/services/suggestions
touch frontend/src/features/customers/services/suggestions/SuggestionEngine.ts
touch frontend/src/features/customers/services/suggestions/SuggestionRules.ts

# 4. UI Components erstellen
mkdir -p frontend/src/features/customers/components/suggestions
touch frontend/src/features/customers/components/suggestions/SuggestionCard.tsx
touch frontend/src/features/customers/components/suggestions/SuggestionDashboard.tsx
touch frontend/src/features/customers/components/suggestions/BirthdayReminderWidget.tsx

# 5. Backend Service vorbereiten
mkdir -p backend/src/main/java/de/freshplan/services/suggestions
touch backend/src/main/java/de/freshplan/services/suggestions/SuggestionService.java
touch backend/src/main/java/de/freshplan/services/suggestions/SuggestionEngine.java

# 6. Tests vorbereiten
mkdir -p frontend/src/features/customers/services/suggestions/__tests__
touch frontend/src/features/customers/services/suggestions/__tests__/SuggestionEngine.test.ts
```

## 📦 IMPLEMENTATION REQUIREMENTS

### Abhängigkeiten (MÜSSEN existieren!):

| Komponente | Pfad | Status | Implementiert in |
|------------|------|--------|------------------|
| RelationshipWarmth Types | `frontend/src/features/customers/types/intelligence.types.ts` | ⚠️ Prüfen | [RELATIONSHIP_INTELLIGENCE.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/RELATIONSHIP_INTELLIGENCE.md) |
| Contact Store | `frontend/src/features/customers/stores/contactStore.ts` | ⚠️ Prüfen | [FRONTEND_FOUNDATION.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/FRONTEND_FOUNDATION.md) |
| Contact Timeline | `frontend/src/features/customers/components/timeline/ContactTimeline.tsx` | ⚠️ Prüfen | [CONTACT_TIMELINE.md](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CONTACT_TIMELINE.md) |
| date-fns | `package.json` | ❌ Installieren | `npm install date-fns` |

### Neue Dateien (werden erstellt):

```
frontend/src/features/customers/
├── types/
│   └── suggestions.types.ts         # Suggestion Typen
├── services/suggestions/
│   ├── SuggestionEngine.ts          # Haupt-Engine
│   ├── SuggestionRules.ts           # Regel-Definitionen
│   └── __tests__/
│       └── SuggestionEngine.test.ts # Tests
└── components/suggestions/
    ├── SuggestionCard.tsx           # Einzelne Suggestion
    ├── SuggestionDashboard.tsx      # Dashboard View
    └── BirthdayReminderWidget.tsx   # Geburtstags-Widget
```  

## 🎯 Vision: Proaktive Vertriebsunterstützung

**Smart Suggestions** verwandelt Daten in **konkrete Handlungsempfehlungen**:

> "Der KI-Assistent, der mitdenkt und rechtzeitig erinnert"

### 💬 Team-Feedback:
> "Automatisierte Vorschläge = weniger verpasste Chancen. Besonders Geburtstage und abkühlende Beziehungen sind Gold wert."

## 💻 VOLLSTÄNDIGE IMPLEMENTIERUNG

### 1. Core Suggestion Types (FOUNDATION)

**Datei:** `frontend/src/features/customers/types/suggestions.types.ts`  
**Größe:** ~100 Zeilen  
**Wichtig:** Basis für alle Suggestion-Features

```typescript
// CLAUDE: Erstelle diese Types ZUERST!
// Pfad: frontend/src/features/customers/types/suggestions.types.ts
export interface SmartSuggestion {
  id: string;
  type: SuggestionType;
  priority: 'urgent' | 'high' | 'medium' | 'low';
  title: string;
  description: string;
  reason: string;
  actionItems: ActionItem[];
  targetDate?: Date;
  relatedContact: Contact;
  confidence: number; // 0-100%
  metadata?: Record<string, any>;
}

export enum SuggestionType {
  // Relationship Management
  BIRTHDAY_REMINDER = 'birthday_reminder',
  COOLING_RELATIONSHIP = 'cooling_relationship',
  MISSING_PRIMARY_CONTACT = 'missing_primary_contact',
  
  // Business Opportunities
  CROSS_SELL_OPPORTUNITY = 'cross_sell_opportunity',
  RENEWAL_APPROACHING = 'renewal_approaching',
  EXPANSION_POTENTIAL = 'expansion_potential',
  
  // Data Quality
  INCOMPLETE_PROFILE = 'incomplete_profile',
  OUTDATED_INFORMATION = 'outdated_information',
  DUPLICATE_CONTACT = 'duplicate_contact',
  
  // Engagement
  FOLLOW_UP_REQUIRED = 'follow_up_required',
  EVENT_INVITATION = 'event_invitation',
  SEASONAL_OUTREACH = 'seasonal_outreach'
}

export interface ActionItem {
  id: string;
  label: string;
  icon?: React.ReactNode;
  action: () => Promise<void>;
  type: 'primary' | 'secondary';
}
```

### 2. Suggestion Engine Service (CORE LOGIC)

**Datei:** `frontend/src/features/customers/services/suggestions/SuggestionEngine.ts`  
**Größe:** ~400 Zeilen  
**Dependencies:** RelationshipWarmth, Contact Types

```typescript
// CLAUDE: Hauptlogik für Suggestion-Generierung
// Pfad: frontend/src/features/customers/services/suggestions/SuggestionEngine.ts

import { SmartSuggestion, SuggestionType, ActionItem } from '../../types/suggestions.types';
import { CustomerContact } from '../../types/contact.types';
import { RelationshipWarmth } from '../../types/intelligence.types';
import { differenceInDays, addDays, startOfDay } from 'date-fns';

export class SuggestionEngine {
  private readonly rules: SuggestionRule[] = [];
  
  constructor() {
    // Registriere alle Regeln
    this.rules = [
      new BirthdayRule(),
      new CoolingRelationshipRule(),
      new IncompleteProfileRule(),
      new CrossSellRule(),
      new SeasonalOutreachRule(),
      new MissingPrimaryContactRule(),
      new RenewalApproachingRule()
    ];
  }
  
  /**
   * Generiert alle Suggestions für einen Kunden
   * Priorisiert nach Urgency und Confidence
   */
  async generateSuggestions(
    customerId: string,
    contacts: CustomerContact[],
    warmthData?: Map<string, RelationshipWarmth>,
    customerData?: any
  ): Promise<SmartSuggestion[]> {
    const suggestions: SmartSuggestion[] = [];
    
    // Führe alle Regeln parallel aus für Performance
    const ruleResults = await Promise.all(
      this.rules.map(rule => 
        rule.evaluate(customerId, contacts, warmthData, customerData)
          .catch(error => {
            console.error(`Rule ${rule.constructor.name} failed:`, error);
            return [];
          })
      )
    );
    
    // Flatten und deduplizieren
    const allSuggestions = ruleResults.flat();
    const uniqueSuggestions = this.deduplicateSuggestions(allSuggestions);
    
    // Sortiere nach Priorität und Confidence
    return this.prioritizeSuggestions(uniqueSuggestions);
  }
  
  private deduplicateSuggestions(suggestions: SmartSuggestion[]): SmartSuggestion[] {
    const seen = new Set<string>();
    return suggestions.filter(s => {
      const key = `${s.type}-${s.relatedContact?.id || 'global'}`;
      if (seen.has(key)) return false;
      seen.add(key);
      return true;
    });
  }
  
  private prioritizeSuggestions(suggestions: SmartSuggestion[]): SmartSuggestion[] {
    const priorityOrder = { urgent: 0, high: 1, medium: 2, low: 3 };
    
    return suggestions.sort((a, b) => {
      // Erst nach Priority
      const priorityDiff = priorityOrder[a.priority] - priorityOrder[b.priority];
      if (priorityDiff !== 0) return priorityDiff;
      
      // Dann nach Confidence
      return (b.confidence || 0) - (a.confidence || 0);
    });
  }
}

// Basis-Interface für alle Regeln
interface SuggestionRule {
  evaluate(
    customerId: string,
    contacts: CustomerContact[],
    warmthData?: Map<string, RelationshipWarmth>,
    customerData?: any
  ): Promise<SmartSuggestion[]>;
}

// Beispiel-Regel: Geburtstage
class BirthdayRule implements SuggestionRule {
  async evaluate(
    customerId: string,
    contacts: CustomerContact[]
  ): Promise<SmartSuggestion[]> {
    const today = startOfDay(new Date());
    const suggestions: SmartSuggestion[] = [];
    
    for (const contact of contacts) {
      if (!contact.birthday) continue;
      
      const birthday = new Date(contact.birthday);
      const thisYearBirthday = new Date(
        today.getFullYear(),
        birthday.getMonth(),
        birthday.getDate()
      );
      
      // Wenn Geburtstag schon vorbei, nächstes Jahr prüfen
      if (thisYearBirthday < today) {
        thisYearBirthday.setFullYear(today.getFullYear() + 1);
      }
      
      const daysUntil = differenceInDays(thisYearBirthday, today);
      
      // Nur Geburtstage in den nächsten 7 Tagen
      if (daysUntil >= 0 && daysUntil <= 7) {
        suggestions.push({
          id: `birthday-${contact.id}-${thisYearBirthday.getTime()}`,
          type: SuggestionType.BIRTHDAY_REMINDER,
          priority: daysUntil === 0 ? 'urgent' : daysUntil <= 3 ? 'high' : 'medium',
          title: daysUntil === 0 
            ? `🎂 Heute: ${contact.firstName} ${contact.lastName} hat Geburtstag!`
            : `🎂 ${contact.firstName} ${contact.lastName} hat in ${daysUntil} Tagen Geburtstag`,
          description: this.generateBirthdayDescription(contact, daysUntil, birthday),
          reason: 'Persönliche Glückwünsche stärken die Geschäftsbeziehung',
          actionItems: this.generateBirthdayActions(contact),
          targetDate: thisYearBirthday,
          relatedContact: contact,
          confidence: 100,
          metadata: {
            age: today.getFullYear() - birthday.getFullYear(),
            daysUntil
          }
        });
      }
    }
    
    return suggestions;
  }
  
  private generateBirthdayDescription(contact: CustomerContact, daysUntil: number, birthday: Date): string {
    const age = new Date().getFullYear() - birthday.getFullYear();
    
    if (daysUntil === 0) {
      return `${contact.firstName} wird heute ${age} Jahre alt. Vergessen Sie nicht zu gratulieren!`;
    }
    
    return `${contact.firstName} wird ${age} Jahre alt. Planen Sie jetzt einen persönlichen Glückwunsch.`;
  }
  
  private generateBirthdayActions(contact: CustomerContact): ActionItem[] {
    const actions: ActionItem[] = [];
    
    if (contact.phone || contact.mobile) {
      actions.push({
        id: 'call-birthday',
        label: 'Anrufen & gratulieren',
        type: 'primary',
        action: async () => {
          window.location.href = `tel:${contact.mobile || contact.phone}`;
        }
      });
    }
    
    if (contact.email) {
      actions.push({
        id: 'email-birthday',
        label: 'Glückwunsch-E-Mail',
        type: contact.phone ? 'secondary' : 'primary',
        action: async () => {
          const subject = `Herzlichen Glückwunsch zum Geburtstag! 🎂`;
          const body = this.generateBirthdayEmail(contact);
          window.location.href = `mailto:${contact.email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
        }
      });
    }
    
    return actions;
  }
  
  private generateBirthdayEmail(contact: CustomerContact): string {
    return `Liebe(r) ${contact.firstName} ${contact.lastName},

herzlichen Glückwunsch zum Geburtstag!

Ich wünsche Ihnen alles Gute, Gesundheit und Erfolg für das neue Lebensjahr.

Mit den besten Grüßen
[Ihr Name]
FreshPlan Team`;
  }
}

// Singleton Export
export const suggestionEngine = new SuggestionEngine();
```

### 3. Advanced Birthday Widget (UI COMPONENT)

**Datei:** `frontend/src/features/customers/components/suggestions/BirthdayReminderWidget.tsx`  
**Größe:** ~350 Zeilen  
**Wichtig:** Zentrale UI für Geburtstags-Erinnerungen

```typescript
// CLAUDE: Birthday Widget mit erweiterten Features
// Pfad: frontend/src/features/customers/components/suggestions/BirthdayReminderWidget.tsx
import React, { useMemo } from 'react';
import {
  Card,
  CardContent,
  CardHeader,
  Typography,
  Box,
  Avatar,
  Chip,
  Button,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Collapse,
  Alert,
  AlertTitle
} from '@mui/material';
import {
  Cake as CakeIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  WhatsApp as WhatsAppIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon
} from '@mui/icons-material';
import { differenceInDays, format, addDays } from 'date-fns';
import { de } from 'date-fns/locale';

interface BirthdayContact extends Contact {
  daysUntilBirthday: number;
  age?: number;
  lastBirthdayGreeting?: Date;
}

export const BirthdayReminderWidget: React.FC<{
  contacts: Contact[];
  onAction: (contact: Contact, action: string) => void;
}> = ({ contacts, onAction }) => {
  const [expanded, setExpanded] = useState(true);
  const [timeRange, setTimeRange] = useState<7 | 30>(7);
  
  const birthdayContacts = useMemo((): BirthdayContact[] => {
    const today = new Date();
    const currentYear = today.getFullYear();
    
    return contacts
      .filter(contact => contact.birthday)
      .map(contact => {
        const birthday = new Date(contact.birthday!);
        const thisYearBirthday = new Date(
          currentYear,
          birthday.getMonth(),
          birthday.getDate()
        );
        
        // If birthday already passed this year, check next year
        if (thisYearBirthday < today) {
          thisYearBirthday.setFullYear(currentYear + 1);
        }
        
        const daysUntilBirthday = differenceInDays(thisYearBirthday, today);
        const age = currentYear - birthday.getFullYear();
        
        return {
          ...contact,
          daysUntilBirthday,
          age: age > 0 ? age : undefined
        };
      })
      .filter(contact => 
        contact.daysUntilBirthday >= 0 && 
        contact.daysUntilBirthday <= timeRange
      )
      .sort((a, b) => a.daysUntilBirthday - b.daysUntilBirthday);
  }, [contacts, timeRange]);
  
  const getUrgencyColor = (days: number): string => {
    if (days === 0) return 'error';
    if (days <= 3) return 'warning';
    if (days <= 7) return 'info';
    return 'default';
  };
  
  const getGreetingTemplate = (contact: BirthdayContact): string => {
    const templates = [
      `Herzlichen Glückwunsch zum ${contact.age || ''}. Geburtstag, ${contact.firstName}! 🎉`,
      `Alles Gute zum Geburtstag, ${contact.firstName}! Ich hoffe, Sie haben einen wunderbaren Tag.`,
      `Happy Birthday, ${contact.firstName}! Wir wünschen Ihnen alles Beste für das neue Lebensjahr.`
    ];
    
    // Add personal touch if hobbies are known
    if (contact.hobbies?.includes('wine')) {
      templates.push(`Zum Geburtstag wünsche ich Ihnen nur das Beste, ${contact.firstName}! Möge das neue Jahr so gut werden wie ein edler Tropfen. 🍷`);
    }
    
    return templates[Math.floor(Math.random() * templates.length)];
  };
  
  if (birthdayContacts.length === 0) {
    return null;
  }
  
  return (
    <Card sx={{ mb: 3, bgcolor: 'background.paper' }}>
      <CardHeader
        avatar={
          <Avatar sx={{ bgcolor: 'secondary.main' }}>
            <CakeIcon />
          </Avatar>
        }
        title={
          <Box display="flex" alignItems="center" gap={1}>
            <Typography variant="h6">
              Geburtstage
            </Typography>
            <Chip 
              label={birthdayContacts.length} 
              size="small" 
              color="secondary"
            />
          </Box>
        }
        subheader={`Nächste ${timeRange} Tage`}
        action={
          <Box display="flex" alignItems="center" gap={1}>
            <ToggleButtonGroup
              value={timeRange}
              exclusive
              onChange={(_, value) => value && setTimeRange(value)}
              size="small"
            >
              <ToggleButton value={7}>7 Tage</ToggleButton>
              <ToggleButton value={30}>30 Tage</ToggleButton>
            </ToggleButtonGroup>
            <IconButton onClick={() => setExpanded(!expanded)}>
              {expanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
            </IconButton>
          </Box>
        }
      />
      
      <Collapse in={expanded}>
        <CardContent sx={{ pt: 0 }}>
          {birthdayContacts.some(c => c.daysUntilBirthday === 0) && (
            <Alert severity="error" sx={{ mb: 2 }}>
              <AlertTitle>Heute ist ein Geburtstag!</AlertTitle>
              Vergessen Sie nicht zu gratulieren!
            </Alert>
          )}
          
          <List>
            {birthdayContacts.map((contact, index) => (
              <React.Fragment key={contact.id}>
                <ListItem
                  sx={{
                    bgcolor: contact.daysUntilBirthday === 0 ? 'action.selected' : 'transparent',
                    borderRadius: 1,
                    mb: 1
                  }}
                >
                  <ListItemAvatar>
                    <Avatar sx={{ bgcolor: getUrgencyColor(contact.daysUntilBirthday) }}>
                      {contact.firstName[0]}{contact.lastName[0]}
                    </Avatar>
                  </ListItemAvatar>
                  
                  <ListItemText
                    primary={
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography variant="subtitle1" fontWeight="bold">
                          {contact.firstName} {contact.lastName}
                        </Typography>
                        {contact.age && (
                          <Chip 
                            label={`wird ${contact.age}`} 
                            size="small" 
                            variant="outlined"
                          />
                        )}
                      </Box>
                    }
                    secondary={
                      <Box>
                        <Typography variant="body2" color="text.secondary">
                          {contact.daysUntilBirthday === 0 
                            ? '🎉 Heute!'
                            : contact.daysUntilBirthday === 1
                            ? 'Morgen'
                            : `In ${contact.daysUntilBirthday} Tagen`}
                          {' • '}
                          {format(
                            addDays(new Date(), contact.daysUntilBirthday), 
                            'EEEE, d. MMMM', 
                            { locale: de }
                          )}
                        </Typography>
                        {contact.lastBirthdayGreeting && (
                          <Typography variant="caption" color="text.secondary">
                            Letzter Glückwunsch: {format(contact.lastBirthdayGreeting, 'yyyy')}
                          </Typography>
                        )}
                      </Box>
                    }
                  />
                  
                  <ListItemSecondaryAction>
                    <Box display="flex" gap={1}>
                      <Tooltip title="Anrufen">
                        <IconButton
                          color="primary"
                          onClick={() => onAction(contact, 'call')}
                          disabled={!contact.phone && !contact.mobile}
                        >
                          <PhoneIcon />
                        </IconButton>
                      </Tooltip>
                      
                      <Tooltip title="E-Mail senden">
                        <IconButton
                          color="primary"
                          onClick={() => {
                            const template = getGreetingTemplate(contact);
                            onAction(contact, `email:${encodeURIComponent(template)}`);
                          }}
                          disabled={!contact.email}
                        >
                          <EmailIcon />
                        </IconButton>
                      </Tooltip>
                      
                      <Tooltip title="WhatsApp">
                        <IconButton
                          color="primary"
                          onClick={() => onAction(contact, 'whatsapp')}
                          disabled={!contact.mobile}
                        >
                          <WhatsAppIcon />
                        </IconButton>
                      </Tooltip>
                    </Box>
                  </ListItemSecondaryAction>
                </ListItem>
                
                {index < birthdayContacts.length - 1 && <Divider />}
              </React.Fragment>
            ))}
          </List>
        </CardContent>
      </Collapse>
    </Card>
  );
};
```

### 4. Suggestion Rules Implementation (RULE ENGINE)

**Datei:** `frontend/src/features/customers/services/suggestions/SuggestionRules.ts`  
**Größe:** ~500 Zeilen  
**Wichtig:** Alle Suggestion-Regeln implementiert

```typescript
// CLAUDE: Alle Suggestion-Regeln
// Pfad: frontend/src/features/customers/services/suggestions/SuggestionRules.ts

import { SuggestionRule, SmartSuggestion, SuggestionType } from '../../types/suggestions.types';
import { CustomerContact } from '../../types/contact.types';
import { RelationshipWarmth } from '../../types/intelligence.types';

export class CoolingRelationshipRule implements SuggestionRule {
  private readonly rules: SuggestionRule[] = [
    new BirthdayRule(),
    new CoolingRelationshipRule(),
    new IncompleteProfileRule(),
    new CrossSellRule(),
    new SeasonalOutreachRule()
  ];
  
  async generateSuggestions(
    customerId: string,
    contacts: Contact[],
    warmthData?: Map<string, RelationshipWarmth>
  ): Promise<SmartSuggestion[]> {
    const suggestions: SmartSuggestion[] = [];
    
    // Run all rules in parallel
    const ruleResults = await Promise.all(
      this.rules.map(rule => 
        rule.evaluate(customerId, contacts, warmthData)
      )
    );
    
    // Flatten and sort by priority
    return ruleResults
      .flat()
      .filter(Boolean)
      .sort((a, b) => {
        const priorityOrder = { urgent: 0, high: 1, medium: 2, low: 3 };
        return priorityOrder[a.priority] - priorityOrder[b.priority];
      });
  }
}

// Example Rule: Cooling Relationship
class CoolingRelationshipRule implements SuggestionRule {
  async evaluate(
    customerId: string,
    contacts: Contact[],
    warmthData?: Map<string, RelationshipWarmth>
  ): Promise<SmartSuggestion[]> {
    if (!warmthData) return [];
    
    const suggestions: SmartSuggestion[] = [];
    
    for (const contact of contacts) {
      const warmth = warmthData.get(contact.id);
      if (!warmth) continue;
      
      if (warmth.temperature === 'COOLING' || warmth.temperature === 'COLD') {
        suggestions.push({
          id: `cooling-${contact.id}`,
          type: SuggestionType.COOLING_RELATIONSHIP,
          priority: warmth.temperature === 'COLD' ? 'urgent' : 'high',
          title: `Beziehung zu ${contact.firstName} ${contact.lastName} kühlt ab`,
          description: `Die Geschäftsbeziehung zeigt Anzeichen der Abkühlung. Letzter Kontakt vor ${warmth.daysSinceLastContact} Tagen.`,
          reason: this.analyzeReason(warmth),
          actionItems: this.generateActions(contact, warmth),
          relatedContact: contact,
          confidence: 85,
          metadata: {
            warmthScore: warmth.score,
            lastInteraction: warmth.lastInteraction
          }
        });
      }
    }
    
    return suggestions;
  }
  
  private analyzeReason(warmth: RelationshipWarmth): string {
    const reasons = [];
    
    if (warmth.daysSinceLastContact > 60) {
      reasons.push('Sehr lange kein Kontakt');
    }
    
    if (warmth.responseRate < 0.3) {
      reasons.push('Niedrige Antwortrate auf Kommunikation');
    }
    
    if (warmth.trendDirection === 'DECLINING') {
      reasons.push('Abnehmende Interaktionsfrequenz');
    }
    
    return reasons.join('. ');
  }
  
  private generateActions(contact: Contact, warmth: RelationshipWarmth): ActionItem[] {
    const actions: ActionItem[] = [];
    
    // Personal call for cold relationships
    if (warmth.temperature === 'COLD') {
      actions.push({
        id: 'personal-call',
        label: 'Persönlicher Anruf',
        icon: <PhoneIcon />,
        action: async () => {
          await contactApi.initiateCall(contact.id);
          await interactionService.recordInteraction(contact.id, 'CALL_OUTGOING');
        },
        type: 'primary'
      });
    }
    
    // Check-in email
    actions.push({
      id: 'check-in-email',
      label: 'Check-in E-Mail senden',
      icon: <EmailIcon />,
      action: async () => {
        const template = this.getCheckInEmailTemplate(contact);
        await contactApi.sendEmail(contact.id, template);
      },
      type: 'secondary'
    });
    
    // Schedule meeting
    if (contact.isPrimary) {
      actions.push({
        id: 'schedule-meeting',
        label: 'Meeting vereinbaren',
        icon: <CalendarIcon />,
        action: async () => {
          await calendarService.proposeeMeeting(contact.id);
        },
        type: 'secondary'
      });
    }
    
    return actions;
  }
}
```

### 5. Suggestion Card Component (UI)

**Datei:** `frontend/src/features/customers/components/suggestions/SuggestionCard.tsx`  
**Größe:** ~250 Zeilen  
**Dependencies:** MUI, Suggestion Types

```typescript
// CLAUDE: Einzelne Suggestion Card
// Pfad: frontend/src/features/customers/components/suggestions/SuggestionCard.tsx
export const SuggestionCard: React.FC<{
  suggestion: SmartSuggestion;
  onAction: (action: ActionItem) => void;
  onDismiss: (suggestionId: string) => void;
}> = ({ suggestion, onAction, onDismiss }) => {
  const [expanded, setExpanded] = useState(false);
  const [processing, setProcessing] = useState<string | null>(null);
  
  const getPriorityColor = () => {
    const colors = {
      urgent: 'error',
      high: 'warning',
      medium: 'info',
      low: 'default'
    };
    return colors[suggestion.priority];
  };
  
  const getIcon = () => {
    const icons = {
      [SuggestionType.BIRTHDAY_REMINDER]: <CakeIcon />,
      [SuggestionType.COOLING_RELATIONSHIP]: <ThermostatIcon />,
      [SuggestionType.INCOMPLETE_PROFILE]: <AssignmentIcon />,
      [SuggestionType.CROSS_SELL_OPPORTUNITY]: <TrendingUpIcon />
    };
    return icons[suggestion.type] || <LightbulbIcon />;
  };
  
  const handleAction = async (action: ActionItem) => {
    setProcessing(action.id);
    try {
      await action.action();
      onAction(action);
    } finally {
      setProcessing(null);
    }
  };
  
  return (
    <Card sx={{ mb: 2 }}>
      <CardContent>
        <Box display="flex" alignItems="flex-start" gap={2}>
          <Avatar sx={{ bgcolor: `${getPriorityColor()}.main` }}>
            {getIcon()}
          </Avatar>
          
          <Box flex={1}>
            <Box display="flex" alignItems="center" gap={1} mb={1}>
              <Typography variant="h6">
                {suggestion.title}
              </Typography>
              <Chip
                label={suggestion.priority}
                size="small"
                color={getPriorityColor() as any}
              />
              {suggestion.confidence && (
                <Chip
                  label={`${suggestion.confidence}% sicher`}
                  size="small"
                  variant="outlined"
                />
              )}
            </Box>
            
            <Typography variant="body2" color="text.secondary" paragraph>
              {suggestion.description}
            </Typography>
            
            {suggestion.reason && (
              <Box
                sx={{
                  p: 1,
                  bgcolor: 'action.hover',
                  borderRadius: 1,
                  mb: 2
                }}
              >
                <Typography variant="caption" color="text.secondary">
                  <strong>Grund:</strong> {suggestion.reason}
                </Typography>
              </Box>
            )}
            
            {/* Related Contact Preview */}
            {suggestion.relatedContact && (
              <Box
                display="flex"
                alignItems="center"
                gap={1}
                mb={2}
                sx={{
                  p: 1,
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: 1
                }}
              >
                <Avatar sx={{ width: 32, height: 32 }}>
                  {suggestion.relatedContact.firstName[0]}
                  {suggestion.relatedContact.lastName[0]}
                </Avatar>
                <Box>
                  <Typography variant="subtitle2">
                    {suggestion.relatedContact.firstName} {suggestion.relatedContact.lastName}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {suggestion.relatedContact.position}
                  </Typography>
                </Box>
              </Box>
            )}
            
            {/* Action Buttons */}
            <Box display="flex" gap={1} flexWrap="wrap">
              {suggestion.actionItems.map(action => (
                <LoadingButton
                  key={action.id}
                  variant={action.type === 'primary' ? 'contained' : 'outlined'}
                  startIcon={action.icon}
                  onClick={() => handleAction(action)}
                  loading={processing === action.id}
                  size="small"
                >
                  {action.label}
                </LoadingButton>
              ))}
              
              <Button
                size="small"
                onClick={() => onDismiss(suggestion.id)}
                sx={{ ml: 'auto' }}
              >
                Später
              </Button>
            </Box>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### 6. Suggestion Dashboard (HAUPTKOMPONENTE)

**Datei:** `frontend/src/features/customers/components/suggestions/SuggestionDashboard.tsx`  
**Größe:** ~300 Zeilen  
**Wichtig:** Zentrale Dashboard-View für alle Suggestions

```typescript
// CLAUDE: Dashboard für alle Suggestions
// Pfad: frontend/src/features/customers/components/suggestions/SuggestionDashboard.tsx
export const SuggestionDashboard: React.FC<{
  customerId: string;
}> = ({ customerId }) => {
  const { contacts } = useContactStore();
  const { warmthData } = useWarmthStore();
  const [suggestions, setSuggestions] = useState<SmartSuggestion[]>([]);
  const [filter, setFilter] = useState<SuggestionType | 'all'>('all');
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    loadSuggestions();
  }, [contacts, warmthData]);
  
  const loadSuggestions = async () => {
    setLoading(true);
    try {
      const engine = new SuggestionEngine();
      const results = await engine.generateSuggestions(
        customerId,
        contacts,
        warmthData
      );
      setSuggestions(results);
    } finally {
      setLoading(false);
    }
  };
  
  const filteredSuggestions = useMemo(() => {
    if (filter === 'all') return suggestions;
    return suggestions.filter(s => s.type === filter);
  }, [suggestions, filter]);
  
  const suggestionStats = useMemo(() => {
    return {
      total: suggestions.length,
      urgent: suggestions.filter(s => s.priority === 'urgent').length,
      byType: suggestions.reduce((acc, s) => {
        acc[s.type] = (acc[s.type] || 0) + 1;
        return acc;
      }, {} as Record<SuggestionType, number>)
    };
  }, [suggestions]);
  
  if (loading) {
    return <SuggestionSkeleton />;
  }
  
  return (
    <Box>
      {/* Header with Stats */}
      <Box mb={3}>
        <Typography variant="h5" gutterBottom>
          Smart Suggestions
        </Typography>
        <Box display="flex" gap={2} flexWrap="wrap">
          <Chip
            label={`${suggestionStats.total} Total`}
            color="primary"
            variant={filter === 'all' ? 'filled' : 'outlined'}
            onClick={() => setFilter('all')}
          />
          {suggestionStats.urgent > 0 && (
            <Chip
              label={`${suggestionStats.urgent} Dringend`}
              color="error"
              icon={<PriorityHighIcon />}
            />
          )}
        </Box>
      </Box>
      
      {/* Birthday Widget */}
      <BirthdayReminderWidget
        contacts={contacts}
        onAction={(contact, action) => {
          // Handle birthday actions
        }}
      />
      
      {/* Suggestion Cards */}
      {filteredSuggestions.length === 0 ? (
        <EmptyState
          icon={<CheckCircleIcon sx={{ fontSize: 64, color: 'success.main' }} />}
          title="Alles im grünen Bereich!"
          description="Aktuell gibt es keine dringenden Handlungsempfehlungen."
        />
      ) : (
        <Box>
          {filteredSuggestions.map(suggestion => (
            <SuggestionCard
              key={suggestion.id}
              suggestion={suggestion}
              onAction={(action) => {
                // Track action execution
                analyticsService.track('suggestion_action_taken', {
                  suggestionType: suggestion.type,
                  actionId: action.id
                });
              }}
              onDismiss={(id) => {
                setSuggestions(prev => prev.filter(s => s.id !== id));
              }}
            />
          ))}
        </Box>
      )}
    </Box>
  );
};
```

## 🗄️ Backend Service (OPTIONAL - für Persistierung)

**Datei:** `backend/src/main/java/de/freshplan/services/suggestions/SuggestionService.java`  
**Migration:** Prüfe nächste freie V-Nummer mit `ls -la backend/src/main/resources/db/migration/`

```java
// CLAUDE: Backend Service für Suggestion-Persistierung
// Pfad: backend/src/main/java/de/freshplan/services/suggestions/SuggestionService.java

package de.freshplan.services.suggestions;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
@Transactional
public class SuggestionService {
    
    // Suggestion-History für Analytics
    public void recordSuggestionAction(
        String suggestionId,
        String actionType,
        String userId,
        String outcome
    ) {
        // TODO: In DB persistieren für Analytics
        // Benötigt Migration für suggestion_history Tabelle
    }
    
    // Dismissed Suggestions tracken
    public void dismissSuggestion(
        String suggestionId,
        String userId,
        String reason
    ) {
        // TODO: Track dismissals
    }
}
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE FÜR CLAUDE

### Phase 1: Foundation (20 Min)
- [ ] Suggestion Types erstellen (suggestions.types.ts)
- [ ] date-fns installieren (`npm install date-fns`)
- [ ] Ordnerstruktur anlegen

### Phase 2: Core Engine (40 Min)
- [ ] SuggestionEngine.ts implementieren (400 Zeilen)
- [ ] SuggestionRules.ts implementieren (500 Zeilen)
- [ ] Basis-Regeln: Birthday, Cooling, Incomplete

### Phase 3: UI Components (30 Min)
- [ ] BirthdayReminderWidget implementieren (350 Zeilen)
- [ ] SuggestionCard implementieren (250 Zeilen)
- [ ] SuggestionDashboard implementieren (300 Zeilen)

### Phase 4: Integration (20 Min)
- [ ] In CustomerDetailPage einbinden
- [ ] In Dashboard-Overview integrieren
- [ ] Navigation verlinken

### Phase 5: Testing (20 Min)
- [ ] Unit Tests für Engine
- [ ] Component Tests für UI
- [ ] Integration Tests

## 🔗 INTEGRATION POINTS

### Wo wird Smart Suggestions verwendet?

1. **CustomerDetailPage.tsx** (Suggestions für einzelnen Kunden)
   ```typescript
   // Pfad: frontend/src/pages/CustomerDetailPage.tsx
   import { SuggestionDashboard } from '@/features/customers/components/suggestions/SuggestionDashboard';
   ```

2. **DashboardPage.tsx** (Globale Suggestions)
   ```typescript
   // Pfad: frontend/src/pages/DashboardPage.tsx
   import { BirthdayReminderWidget } from '@/features/customers/components/suggestions/BirthdayReminderWidget';
   ```

3. **ContactListView.tsx** (Inline Suggestions)
   ```typescript
   // Pfad: frontend/src/features/customers/components/ContactListView.tsx
   import { SuggestionCard } from '@/features/customers/components/suggestions/SuggestionCard';
   ```

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Zu viele Suggestions gleichzeitig**
   → Lösung: Max. 5 Suggestions pro View, Priorisierung beachten

2. **Performance bei vielen Kontakten**
   → Lösung: Regeln parallel ausführen, Caching implementieren

3. **Duplicate Suggestions**
   → Lösung: Deduplication-Logik in Engine

4. **Zeitzone-Probleme bei Geburtstagen**
   → Lösung: Immer startOfDay() verwenden

## 🧪 Testing

```typescript
// __tests__/SuggestionEngine.test.ts
describe('SuggestionEngine', () => {
  it('should generate birthday reminders', async () => {
    const contacts = [
      createMockContact({
        birthday: addDays(new Date(), 3).toISOString()
      })
    ];
    
    const engine = new SuggestionEngine();
    const suggestions = await engine.generateSuggestions('customer-1', contacts);
    
    expect(suggestions).toContainEqual(
      expect.objectContaining({
        type: SuggestionType.BIRTHDAY_REMINDER,
        priority: 'high'
      })
    );
  });
  
  it('should prioritize urgent suggestions', async () => {
    const suggestions = [
      { priority: 'medium' },
      { priority: 'urgent' },
      { priority: 'low' }
    ];
    
    const sorted = suggestions.sort(priorityComparator);
    expect(sorted[0].priority).toBe('urgent');
  });
});
```

## 🎯 Success Metrics & KPIs

### Engagement:
- **Suggestion Acceptance Rate:** > 60%
- **Action Completion Rate:** > 80%
- **Time to Action:** < 24h for urgent

### Business Impact:
- **Relationship Score Improvement:** +15% average
- **Customer Touch Points:** +30% increase
- **Revenue Correlation:** Measurable uplift

## 🚀 NÄCHSTE SCHRITTE NACH IMPLEMENTIERUNG

1. **Testing Integration** durchführen
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/TESTING_INTEGRATION.md)

2. **Integration Summary** aktualisieren
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md)

3. **Dashboard Integration** planen
   → In Sprint 3 vorgesehen

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 130 Minuten  
**Nächstes Dokument:** [→ Testing Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/TESTING_INTEGRATION.md)  
**Parent:** [↑ Step3 Übersicht](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md)

**Smart Suggestions = Proaktiver Vertrieb! 🎯✨**