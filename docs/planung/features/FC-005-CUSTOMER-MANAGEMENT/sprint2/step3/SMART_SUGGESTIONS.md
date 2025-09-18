# 🎯 Smart Suggestions - Proaktive Handlungsempfehlungen

**Phase:** 2 - Intelligence Features  
**Tag:** 3 der Woche 2  
**Status:** 🎯 Ready for Implementation  

## 🧭 Navigation

**← Zurück:** [Contact Timeline](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/CONTACT_TIMELINE.md)  
**→ Nächster:** [Location Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/LOCATION_INTELLIGENCE.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: Proaktive Vertriebsunterstützung

**Smart Suggestions** verwandelt Daten in **konkrete Handlungsempfehlungen**:

> "Der KI-Assistent, der mitdenkt und rechtzeitig erinnert"

## 🧠 Suggestion Engine Architecture

### Core Suggestion Types

```typescript
// types/suggestions.types.ts
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

## 🎂 Birthday Reminder System

### Advanced Birthday Widget

```typescript
// components/BirthdayReminderWidget.tsx
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

## 🤖 Suggestion Engine Service

### Backend Integration

```typescript
// services/suggestionEngine.ts
export class SuggestionEngine {
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

## 🎨 Suggestion UI Components

### Suggestion Card

```typescript
// components/SuggestionCard.tsx
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

### Suggestion Dashboard

```typescript
// components/SuggestionDashboard.tsx
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

## 🎯 Success Metrics

### Engagement:
- **Suggestion Acceptance Rate:** > 60%
- **Action Completion Rate:** > 80%
- **Time to Action:** < 24h for urgent

### Business Impact:
- **Relationship Score Improvement:** +15% average
- **Customer Touch Points:** +30% increase
- **Revenue Correlation:** Measurable uplift

---

**Nächster Schritt:** [→ Location Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/LOCATION_INTELLIGENCE.md)

**Smart Suggestions = Proaktiver Vertrieb! 🎯✨**