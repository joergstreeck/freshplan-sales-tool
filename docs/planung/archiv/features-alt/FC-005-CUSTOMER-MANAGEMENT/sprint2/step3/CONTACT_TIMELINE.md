# 📊 Contact Timeline - Interaktions-Historie

**Phase:** 2 - Intelligence Features  
**Tag:** 2 der Woche 2  
**Status:** 🎯 Ready for Implementation  

## 🧭 Navigation

**← Zurück:** [Relationship Intelligence](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/RELATIONSHIP_INTELLIGENCE.md)  
**→ Nächster:** [Smart Suggestions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/SMART_SUGGESTIONS.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: Jede Interaktion zählt

**Contact Timeline** macht die **Beziehungsgeschichte** sichtbar und nutzbar:

> "Von der ersten E-Mail bis zum erfolgreichen Abschluss - alles auf einen Blick"

## 📊 Timeline Architecture

### Data Model

```typescript
// types/timeline.types.ts
export interface TimelineEvent {
  id: string;
  contactId: string;
  timestamp: Date;
  type: TimelineEventType;
  category: EventCategory;
  title: string;
  description?: string;
  metadata?: Record<string, any>;
  performer?: {
    id: string;
    name: string;
    avatar?: string;
  };
  relatedEntities?: {
    orderId?: string;
    documentId?: string;
    noteId?: string;
  };
  impact?: {
    warmthChange?: number;
    dealValue?: number;
    sentiment?: 'positive' | 'neutral' | 'negative';
  };
}

export enum TimelineEventType {
  // Communication
  CALL_INCOMING = 'call_incoming',
  CALL_OUTGOING = 'call_outgoing',
  EMAIL_SENT = 'email_sent',
  EMAIL_RECEIVED = 'email_received',
  MEETING_HELD = 'meeting_held',
  
  // System Events
  CONTACT_CREATED = 'contact_created',
  CONTACT_UPDATED = 'contact_updated',
  RELATIONSHIP_DATA_ADDED = 'relationship_data_added',
  
  // Business Events
  QUOTE_SENT = 'quote_sent',
  ORDER_PLACED = 'order_placed',
  COMPLAINT_RECEIVED = 'complaint_received',
  
  // Milestones
  BIRTHDAY = 'birthday',
  ANNIVERSARY = 'anniversary',
  PROMOTION = 'promotion'
}

export interface TimelineFilter {
  dateRange?: { start: Date; end: Date };
  eventTypes?: TimelineEventType[];
  categories?: EventCategory[];
  searchTerm?: string;
}
```

## 🎨 Timeline UI Components

### Main Timeline Component

```typescript
// components/ContactTimeline.tsx
import React, { useState, useMemo } from 'react';
import {
  Box,
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineOppositeContent,
  Typography,
  Card,
  CardContent,
  IconButton,
  Chip,
  TextField,
  InputAdornment,
  Menu,
  MenuItem,
  Collapse,
  Button
} from '@mui/material';
import {
  Phone as PhoneIcon,
  Email as EmailIcon,
  Event as EventIcon,
  Description as DocumentIcon,
  TrendingUp as BusinessIcon,
  Cake as CakeIcon,
  Search as SearchIcon,
  FilterList as FilterIcon,
  ExpandMore as ExpandMoreIcon
} from '@mui/icons-material';
import { format, isToday, isYesterday, formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';

export const ContactTimeline: React.FC<{
  contactId: string;
  events: TimelineEvent[];
  onEventClick?: (event: TimelineEvent) => void;
}> = ({ contactId, events, onEventClick }) => {
  const [filter, setFilter] = useState<TimelineFilter>({});
  const [searchTerm, setSearchTerm] = useState('');
  const [expandedEvents, setExpandedEvents] = useState<Set<string>>(new Set());
  const [filterMenuAnchor, setFilterMenuAnchor] = useState<null | HTMLElement>(null);
  
  // Filter and search events
  const filteredEvents = useMemo(() => {
    let filtered = [...events];
    
    // Date range filter
    if (filter.dateRange) {
      filtered = filtered.filter(event => 
        event.timestamp >= filter.dateRange!.start &&
        event.timestamp <= filter.dateRange!.end
      );
    }
    
    // Event type filter
    if (filter.eventTypes?.length) {
      filtered = filtered.filter(event => 
        filter.eventTypes!.includes(event.type)
      );
    }
    
    // Search filter
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(event =>
        event.title.toLowerCase().includes(term) ||
        event.description?.toLowerCase().includes(term)
      );
    }
    
    // Sort by timestamp descending
    return filtered.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
  }, [events, filter, searchTerm]);
  
  // Group events by date
  const groupedEvents = useMemo(() => {
    const groups = new Map<string, TimelineEvent[]>();
    
    filteredEvents.forEach(event => {
      const dateKey = format(event.timestamp, 'yyyy-MM-dd');
      if (!groups.has(dateKey)) {
        groups.set(dateKey, []);
      }
      groups.get(dateKey)!.push(event);
    });
    
    return Array.from(groups.entries()).map(([date, events]) => ({
      date,
      displayDate: getDisplayDate(new Date(date)),
      events: events.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime())
    }));
  }, [filteredEvents]);
  
  const getDisplayDate = (date: Date): string => {
    if (isToday(date)) return 'Heute';
    if (isYesterday(date)) return 'Gestern';
    return format(date, 'EEEE, d. MMMM yyyy', { locale: de });
  };
  
  const getEventIcon = (type: TimelineEventType): React.ReactNode => {
    const iconMap = {
      [TimelineEventType.CALL_INCOMING]: <PhoneIcon />,
      [TimelineEventType.CALL_OUTGOING]: <PhoneIcon />,
      [TimelineEventType.EMAIL_SENT]: <EmailIcon />,
      [TimelineEventType.EMAIL_RECEIVED]: <EmailIcon />,
      [TimelineEventType.MEETING_HELD]: <EventIcon />,
      [TimelineEventType.QUOTE_SENT]: <DocumentIcon />,
      [TimelineEventType.ORDER_PLACED]: <BusinessIcon />,
      [TimelineEventType.BIRTHDAY]: <CakeIcon />
    };
    
    return iconMap[type] || <EventIcon />;
  };
  
  const getEventColor = (event: TimelineEvent): 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success' => {
    if (event.impact?.sentiment === 'positive') return 'success';
    if (event.impact?.sentiment === 'negative') return 'error';
    
    const colorMap = {
      'communication': 'primary',
      'business': 'success',
      'system': 'info',
      'milestone': 'secondary'
    };
    
    return colorMap[event.category] || 'primary';
  };
  
  const toggleEventExpansion = (eventId: string) => {
    setExpandedEvents(prev => {
      const next = new Set(prev);
      if (next.has(eventId)) {
        next.delete(eventId);
      } else {
        next.add(eventId);
      }
      return next;
    });
  };
  
  return (
    <Box>
      {/* Search and Filter Bar */}
      <Box display="flex" gap={2} mb={3}>
        <TextField
          fullWidth
          size="small"
          placeholder="Timeline durchsuchen..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            )
          }}
        />
        
        <IconButton
          onClick={(e) => setFilterMenuAnchor(e.currentTarget)}
          color={Object.keys(filter).length > 0 ? 'primary' : 'default'}
        >
          <FilterIcon />
        </IconButton>
      </Box>
      
      {/* Filter Menu */}
      <Menu
        anchorEl={filterMenuAnchor}
        open={Boolean(filterMenuAnchor)}
        onClose={() => setFilterMenuAnchor(null)}
      >
        <MenuItem onClick={() => {
          setFilter({ eventTypes: [TimelineEventType.CALL_INCOMING, TimelineEventType.CALL_OUTGOING] });
          setFilterMenuAnchor(null);
        }}>
          Nur Anrufe
        </MenuItem>
        <MenuItem onClick={() => {
          setFilter({ eventTypes: [TimelineEventType.EMAIL_SENT, TimelineEventType.EMAIL_RECEIVED] });
          setFilterMenuAnchor(null);
        }}>
          Nur E-Mails
        </MenuItem>
        <MenuItem onClick={() => {
          setFilter({ categories: ['business'] });
          setFilterMenuAnchor(null);
        }}>
          Nur Geschäftsereignisse
        </MenuItem>
        <MenuItem onClick={() => {
          setFilter({});
          setFilterMenuAnchor(null);
        }}>
          Filter zurücksetzen
        </MenuItem>
      </Menu>
      
      {/* Timeline Content */}
      {groupedEvents.length === 0 ? (
        <Box textAlign="center" py={4}>
          <Typography color="text.secondary">
            Keine Ereignisse gefunden
          </Typography>
        </Box>
      ) : (
        groupedEvents.map(({ date, displayDate, events }) => (
          <Box key={date} mb={4}>
            <Typography variant="h6" gutterBottom sx={{ position: 'sticky', top: 0, bgcolor: 'background.paper', py: 1 }}>
              {displayDate}
            </Typography>
            
            <Timeline position="right">
              {events.map((event, index) => (
                <TimelineItem key={event.id}>
                  <TimelineOppositeContent
                    sx={{ m: 'auto 0' }}
                    align="right"
                    variant="body2"
                    color="text.secondary"
                  >
                    {format(event.timestamp, 'HH:mm')}
                  </TimelineOppositeContent>
                  
                  <TimelineSeparator>
                    <TimelineConnector sx={{ visibility: index === 0 ? 'hidden' : 'visible' }} />
                    <TimelineDot 
                      color={getEventColor(event)}
                      variant={event.impact?.warmthChange ? 'filled' : 'outlined'}
                    >
                      {getEventIcon(event.type)}
                    </TimelineDot>
                    <TimelineConnector sx={{ visibility: index === events.length - 1 ? 'hidden' : 'visible' }} />
                  </TimelineSeparator>
                  
                  <TimelineContent sx={{ py: '12px', px: 2 }}>
                    <Card 
                      sx={{ 
                        cursor: 'pointer',
                        transition: 'all 0.2s',
                        '&:hover': {
                          boxShadow: 3,
                          transform: 'translateX(-2px)'
                        }
                      }}
                      onClick={() => onEventClick?.(event)}
                    >
                      <CardContent>
                        <Box display="flex" alignItems="center" justifyContent="space-between" mb={1}>
                          <Typography variant="subtitle1" component="span">
                            {event.title}
                          </Typography>
                          
                          <Box display="flex" gap={0.5} alignItems="center">
                            {event.impact?.warmthChange && (
                              <Chip
                                label={event.impact.warmthChange > 0 ? `+${event.impact.warmthChange}` : event.impact.warmthChange}
                                size="small"
                                color={event.impact.warmthChange > 0 ? 'success' : 'error'}
                              />
                            )}
                            
                            {event.impact?.dealValue && (
                              <Chip
                                label={`${event.impact.dealValue.toLocaleString('de-DE')} €`}
                                size="small"
                                color="primary"
                              />
                            )}
                            
                            <IconButton
                              size="small"
                              onClick={(e) => {
                                e.stopPropagation();
                                toggleEventExpansion(event.id);
                              }}
                            >
                              <ExpandMoreIcon 
                                sx={{
                                  transform: expandedEvents.has(event.id) ? 'rotate(180deg)' : 'rotate(0deg)',
                                  transition: 'transform 0.2s'
                                }}
                              />
                            </IconButton>
                          </Box>
                        </Box>
                        
                        <Typography variant="body2" color="text.secondary">
                          {event.description}
                        </Typography>
                        
                        <Collapse in={expandedEvents.has(event.id)}>
                          <Box mt={2}>
                            {event.metadata && (
                              <Box mb={1}>
                                <Typography variant="caption" color="text.secondary">
                                  Details:
                                </Typography>
                                <Box component="pre" sx={{ 
                                  fontSize: '0.75rem', 
                                  bgcolor: 'action.hover',
                                  p: 1,
                                  borderRadius: 1,
                                  overflow: 'auto'
                                }}>
                                  {JSON.stringify(event.metadata, null, 2)}
                                </Box>
                              </Box>
                            )}
                            
                            {event.performer && (
                              <Typography variant="caption" color="text.secondary">
                                Durchgeführt von: {event.performer.name}
                              </Typography>
                            )}
                          </Box>
                        </Collapse>
                      </CardContent>
                    </Card>
                  </TimelineContent>
                </TimelineItem>
              ))}
            </Timeline>
          </Box>
        ))
      )}
    </Box>
  );
};
```

## 🔄 Timeline Service

### Event Collection & Aggregation

```typescript
// services/timelineService.ts
export class TimelineService {
  async getContactTimeline(
    contactId: string,
    options?: {
      limit?: number;
      offset?: number;
      filter?: TimelineFilter;
    }
  ): Promise<TimelineEvent[]> {
    const events: TimelineEvent[] = [];
    
    // Collect from multiple sources
    const [
      interactions,
      systemEvents,
      businessEvents,
      milestones
    ] = await Promise.all([
      this.getInteractionEvents(contactId),
      this.getSystemEvents(contactId),
      this.getBusinessEvents(contactId),
      this.getMilestoneEvents(contactId)
    ]);
    
    // Merge and sort
    events.push(...interactions, ...systemEvents, ...businessEvents, ...milestones);
    
    // Apply filters
    let filtered = events;
    if (options?.filter) {
      filtered = this.applyFilters(events, options.filter);
    }
    
    // Sort by timestamp
    filtered.sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime());
    
    // Apply pagination
    if (options?.limit) {
      const start = options.offset || 0;
      filtered = filtered.slice(start, start + options.limit);
    }
    
    return filtered;
  }
  
  private async getInteractionEvents(contactId: string): Promise<TimelineEvent[]> {
    const interactions = await interactionApi.getInteractions(contactId);
    
    return interactions.map(interaction => ({
      id: interaction.id,
      contactId,
      timestamp: new Date(interaction.timestamp),
      type: interaction.type as TimelineEventType,
      category: 'communication',
      title: this.getInteractionTitle(interaction),
      description: interaction.notes,
      metadata: {
        duration: interaction.durationSeconds,
        outcome: interaction.outcome
      },
      performer: {
        id: interaction.createdBy,
        name: interaction.createdByName
      },
      impact: this.calculateInteractionImpact(interaction)
    }));
  }
  
  private calculateInteractionImpact(interaction: any) {
    let warmthChange = 0;
    
    // Positive interactions increase warmth
    if (interaction.outcome === 'SUCCESSFUL') {
      warmthChange = interaction.type.includes('CALL') ? 5 : 3;
    }
    
    // Incoming interactions are more valuable
    if (interaction.type.includes('INCOMING')) {
      warmthChange += 2;
    }
    
    return { warmthChange };
  }
}
```

## 📊 Timeline Analytics

### Interaction Patterns

```typescript
// components/TimelineAnalytics.tsx
export const TimelineAnalytics: React.FC<{
  events: TimelineEvent[];
  contactId: string;
}> = ({ events, contactId }) => {
  const analytics = useMemo(() => {
    const now = new Date();
    const thirtyDaysAgo = subDays(now, 30);
    const recentEvents = events.filter(e => e.timestamp >= thirtyDaysAgo);
    
    return {
      totalInteractions: events.length,
      recentInteractions: recentEvents.length,
      averageInteractionsPerWeek: recentEvents.length / 4,
      mostActiveDay: getMostActiveDay(recentEvents),
      preferredChannel: getPreferredChannel(recentEvents),
      responseRate: calculateResponseRate(recentEvents),
      communicationBalance: getCommunicationBalance(recentEvents)
    };
  }, [events]);
  
  return (
    <Card sx={{ mb: 3 }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Kommunikations-Insights
        </Typography>
        
        <Grid container spacing={2}>
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Typography variant="h4" color="primary">
                {analytics.totalInteractions}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Gesamt-Interaktionen
              </Typography>
            </Box>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Typography variant="h4" color="success.main">
                {analytics.averageInteractionsPerWeek.toFixed(1)}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Pro Woche (Ø)
              </Typography>
            </Box>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Typography variant="h4" color="info.main">
                {analytics.responseRate}%
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Antwortrate
              </Typography>
            </Box>
          </Grid>
          
          <Grid item xs={6} sm={3}>
            <Box textAlign="center">
              <Chip
                label={analytics.preferredChannel}
                color="primary"
                size="small"
              />
              <Typography variant="caption" color="text.secondary" display="block">
                Bevorzugter Kanal
              </Typography>
            </Box>
          </Grid>
        </Grid>
        
        {/* Communication Balance */}
        <Box mt={3}>
          <Typography variant="subtitle2" gutterBottom>
            Kommunikations-Balance
          </Typography>
          <LinearProgress
            variant="determinate"
            value={analytics.communicationBalance}
            sx={{
              height: 10,
              borderRadius: 5,
              bgcolor: 'error.light',
              '& .MuiLinearProgress-bar': {
                bgcolor: 'success.main'
              }
            }}
          />
          <Box display="flex" justifyContent="space-between" mt={0.5}>
            <Typography variant="caption">Ausgehend</Typography>
            <Typography variant="caption">
              {analytics.communicationBalance}% Eingehend
            </Typography>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};
```

## 🧪 Testing

```typescript
describe('ContactTimeline', () => {
  it('should group events by date correctly', () => {
    const events = [
      createMockEvent({ timestamp: new Date('2024-01-15 10:00') }),
      createMockEvent({ timestamp: new Date('2024-01-15 14:00') }),
      createMockEvent({ timestamp: new Date('2024-01-14 09:00') })
    ];
    
    render(<ContactTimeline contactId="123" events={events} />);
    
    // Should show two date groups
    expect(screen.getAllByRole('heading', { level: 6 })).toHaveLength(2);
  });
  
  it('should filter events by search term', async () => {
    const events = [
      createMockEvent({ title: 'Anruf mit Kunde' }),
      createMockEvent({ title: 'E-Mail gesendet' })
    ];
    
    render(<ContactTimeline contactId="123" events={events} />);
    
    const searchInput = screen.getByPlaceholderText('Timeline durchsuchen...');
    await userEvent.type(searchInput, 'Anruf');
    
    expect(screen.getByText('Anruf mit Kunde')).toBeInTheDocument();
    expect(screen.queryByText('E-Mail gesendet')).not.toBeInTheDocument();
  });
});
```

## 🎯 Success Metrics

### Performance:
- **Load Time:** < 200ms for 100 events
- **Search Response:** < 50ms
- **Render Performance:** 60fps scrolling

### UX:
- **Information Density:** Optimal balance
- **Navigation:** Quick date jumping
- **Filter Effectiveness:** < 3 clicks to any view

---

**Nächster Schritt:** [→ Smart Suggestions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/SMART_SUGGESTIONS.md)

**Timeline = Geschichte einer Geschäftsbeziehung! 📊✨**