# 📊 Contact Timeline - Interaktions-Historie visualisiert

**Phase:** 2 - Intelligence Features  
**Tag:** 2 der Woche 2  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/RELATIONSHIP_INTELLIGENCE.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_SUGGESTIONS.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Voraussetzungen:**
- `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/RELATIONSHIP_INTELLIGENCE.md` ✅
- V201 Migration (ContactInteraction Table) ✅

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Pfade für Copy & Paste:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Timeline Components erstellen
mkdir -p frontend/src/features/customers/components/timeline
touch frontend/src/features/customers/components/timeline/ContactTimeline.tsx
touch frontend/src/features/customers/components/timeline/TimelineEvent.tsx
touch frontend/src/features/customers/components/timeline/TimelineFilter.tsx
touch frontend/src/features/customers/components/timeline/TimelineAnalytics.tsx

# 2. Timeline Services erstellen
mkdir -p frontend/src/features/customers/services/timeline
touch frontend/src/features/customers/services/timeline/TimelineService.ts
touch frontend/src/features/customers/services/timeline/TimelineAggregator.ts
touch frontend/src/features/customers/services/timeline/EventFormatter.ts

# 3. Timeline Types definieren
touch frontend/src/features/customers/types/timeline.types.ts

# 4. Backend Timeline Resource
mkdir -p backend/src/main/java/de/freshplan/api/timeline
touch backend/src/main/java/de/freshplan/api/timeline/ContactTimelineResource.java
touch backend/src/main/java/de/freshplan/api/timeline/TimelineService.java

# 5. Tests vorbereiten
mkdir -p frontend/src/features/customers/components/timeline/__tests__
touch frontend/src/features/customers/components/timeline/__tests__/ContactTimeline.test.tsx
```

## 📦 IMPLEMENTATION REQUIREMENTS

### Backend Dependencies (MÜSSEN existieren!):

| Komponente | Pfad | Status | Benötigt für |
|------------|------|--------|--------------|
| ContactInteraction Entity | `backend/.../ContactInteraction.java` | ✅ V201 | Timeline Events |
| ContactInteractionRepository | `backend/.../ContactInteractionRepository.java` | ✅ Geplant | Event Queries |
| RelationshipWarmthService | `backend/.../RelationshipWarmthService.java` | ✅ Geplant | Warmth Changes |

### Frontend Dependencies:

| Komponente | Pfad | Status |
|------------|------|--------|
| ContactIntelligence Types | `frontend/.../types/contact.types.ts` | ✅ Geplant |
| ContactStore | `frontend/.../stores/contactStore.ts` | ✅ Geplant |
| MUI Timeline Components | `@mui/lab` | ❌ Installieren |

```bash
# MUI Lab installieren für Timeline Components
npm install @mui/lab
```

## 🎯 Vision: Jede Interaktion erzählt eine Geschichte

**Contact Timeline** macht die **komplette Beziehungsgeschichte** sichtbar:

> "Von der ersten E-Mail bis zum erfolgreichen Abschluss - alles auf einen Blick"  
> "Warmth-Veränderungen live verfolgen"  
> "Muster erkennen, Chancen nutzen"

### 💬 Team-Feedback:
> "Timeline-View essentiell für Vertrieb. Zeigt Beziehungsverlauf. Muster werden sichtbar."

## 🏗️ ARCHITEKTUR: Event-Driven Timeline System

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer                              │
│  ContactTimeline │ TimelineFilter │ TimelineAnalytics   │
└────────────────────┬────────────────────────────────────┘
                     │ 
┌────────────────────▼────────────────────────────────────┐
│                Service Layer                             │
│  TimelineService │ EventAggregator │ EventFormatter     │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                  Data Sources                            │
│  ContactInteractions │ SystemEvents │ BusinessEvents    │
└─────────────────────────────────────────────────────────┘
```

## 📝 TIMELINE TYPES (Frontend Foundation)

**Datei:** `frontend/src/features/customers/types/timeline.types.ts`

```typescript
// CLAUDE: Zentrale Type-Definitionen für Timeline
// Pfad: frontend/src/features/customers/types/timeline.types.ts

export interface TimelineEvent {
  id: string;
  contactId: string;
  customerId: string;
  timestamp: Date;
  type: TimelineEventType;
  category: EventCategory;
  title: string;
  description?: string;
  icon?: string;
  color?: string;
  
  // Metadata
  metadata?: {
    duration?: number;        // Minuten
    channel?: string;         // EMAIL, PHONE, etc.
    direction?: 'incoming' | 'outgoing' | 'internal';
    attachments?: number;
    location?: string;
  };
  
  // Performer
  performer?: {
    id: string;
    name: string;
    avatar?: string;
    role?: string;
  };
  
  // Related Entities
  relatedEntities?: {
    opportunityId?: string;
    orderId?: string;
    documentId?: string;
    taskId?: string;
    noteId?: string;
  };
  
  // Impact & Analytics
  impact?: {
    warmthChange?: number;      // -10 to +10
    dealValue?: number;         // EUR
    sentiment?: 'positive' | 'neutral' | 'negative' | 'mixed';
    urgency?: 'low' | 'medium' | 'high' | 'urgent';
  };
  
  // Display Control
  isExpandable?: boolean;
  isHighlighted?: boolean;
  isPinned?: boolean;
}

export enum TimelineEventType {
  // Communication Events
  CALL_INCOMING = 'call_incoming',
  CALL_OUTGOING = 'call_outgoing',
  CALL_MISSED = 'call_missed',
  EMAIL_SENT = 'email_sent',
  EMAIL_RECEIVED = 'email_received',
  EMAIL_BOUNCED = 'email_bounced',
  MEETING_HELD = 'meeting_held',
  MEETING_SCHEDULED = 'meeting_scheduled',
  MEETING_CANCELLED = 'meeting_cancelled',
  WHATSAPP_SENT = 'whatsapp_sent',
  WHATSAPP_RECEIVED = 'whatsapp_received',
  SMS_SENT = 'sms_sent',
  SMS_RECEIVED = 'sms_received',
  
  // System Events
  CONTACT_CREATED = 'contact_created',
  CONTACT_UPDATED = 'contact_updated',
  CONTACT_MERGED = 'contact_merged',
  DATA_ENRICHED = 'data_enriched',
  WARMTH_CALCULATED = 'warmth_calculated',
  
  // Business Events
  QUOTE_SENT = 'quote_sent',
  QUOTE_ACCEPTED = 'quote_accepted',
  QUOTE_REJECTED = 'quote_rejected',
  ORDER_PLACED = 'order_placed',
  ORDER_DELIVERED = 'order_delivered',
  INVOICE_SENT = 'invoice_sent',
  PAYMENT_RECEIVED = 'payment_received',
  CONTRACT_SIGNED = 'contract_signed',
  CONTRACT_RENEWED = 'contract_renewed',
  
  // Relationship Events
  COMPLAINT_RECEIVED = 'complaint_received',
  COMPLAINT_RESOLVED = 'complaint_resolved',
  FEEDBACK_POSITIVE = 'feedback_positive',
  FEEDBACK_NEGATIVE = 'feedback_negative',
  REFERRAL_GIVEN = 'referral_given',
  
  // Milestones
  BIRTHDAY = 'birthday',
  ANNIVERSARY = 'anniversary',
  PROMOTION = 'promotion',
  ACHIEVEMENT = 'achievement',
  
  // Tasks & Notes
  TASK_CREATED = 'task_created',
  TASK_COMPLETED = 'task_completed',
  NOTE_ADDED = 'note_added',
  REMINDER_SET = 'reminder_set'
}

export enum EventCategory {
  COMMUNICATION = 'communication',
  BUSINESS = 'business',
  SYSTEM = 'system',
  MILESTONE = 'milestone',
  TASK = 'task',
  RELATIONSHIP = 'relationship'
}

export interface TimelineFilter {
  dateRange?: {
    start: Date;
    end: Date;
  };
  eventTypes?: TimelineEventType[];
  categories?: EventCategory[];
  performers?: string[];
  sentiment?: ('positive' | 'neutral' | 'negative')[];
  searchTerm?: string;
  hasImpact?: boolean;
  minWarmthChange?: number;
}

export interface TimelineGrouping {
  by: 'day' | 'week' | 'month' | 'category' | 'performer';
  collapsed?: string[]; // IDs of collapsed groups
}

export interface TimelineAnalytics {
  totalEvents: number;
  periodEvents: number;
  averagePerWeek: number;
  mostActiveDay: string;
  mostActiveHour: number;
  preferredChannel: string;
  responseRate: number;
  averageResponseTime: number; // hours
  sentimentDistribution: {
    positive: number;
    neutral: number;
    negative: number;
  };
  warmthTrend: number; // -100 to +100
  communicationBalance: number; // % incoming
}
```

## 🎨 MAIN TIMELINE COMPONENT

**Datei:** `frontend/src/features/customers/components/timeline/ContactTimeline.tsx`  
**Größe:** ~500 Zeilen  
**Kern-Feature:** Interaktive Timeline mit Filtering und Grouping

```typescript
// CLAUDE: Hauptkomponente für Contact Timeline
// Pfad: frontend/src/features/customers/components/timeline/ContactTimeline.tsx

import React, { useState, useMemo, useCallback, useRef, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  InputAdornment,
  IconButton,
  Menu,
  MenuItem,
  Chip,
  Collapse,
  Button,
  Divider,
  Skeleton,
  Alert,
  Badge,
  Tooltip,
  ToggleButtonGroup,
  ToggleButton
} from '@mui/material';
import {
  Timeline,
  TimelineItem,
  TimelineSeparator,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineOppositeContent
} from '@mui/lab';
import {
  Search as SearchIcon,
  FilterList as FilterIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  Event as EventIcon,
  Description as DocumentIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Cake as CakeIcon,
  Warning as WarningIcon,
  CheckCircle as SuccessIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
  ViewDay as DayViewIcon,
  ViewWeek as WeekViewIcon,
  Category as CategoryViewIcon,
  Refresh as RefreshIcon
} from '@mui/icons-material';
import { format, isToday, isYesterday, formatDistanceToNow, startOfDay, endOfDay } from 'date-fns';
import { de } from 'date-fns/locale';
import { TimelineEvent, TimelineEventType, EventCategory, TimelineFilter, TimelineGrouping } from '../../types/timeline.types';
import { TimelineService } from '../../services/timeline/TimelineService';
import { TimelineAnalytics } from './TimelineAnalytics';
import { TimelineEventCard } from './TimelineEventCard';
import { useInfiniteScroll } from '../../hooks/useInfiniteScroll';

interface ContactTimelineProps {
  contactId: string;
  customerId: string;
  height?: number | string;
  showAnalytics?: boolean;
  onEventClick?: (event: TimelineEvent) => void;
  onEventAction?: (event: TimelineEvent, action: string) => void;
}

export const ContactTimeline: React.FC<ContactTimelineProps> = ({
  contactId,
  customerId,
  height = 600,
  showAnalytics = true,
  onEventClick,
  onEventAction
}) => {
  // State Management
  const [events, setEvents] = useState<TimelineEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<TimelineFilter>({});
  const [searchTerm, setSearchTerm] = useState('');
  const [grouping, setGrouping] = useState<TimelineGrouping>({ by: 'day' });
  const [expandedGroups, setExpandedGroups] = useState<Set<string>>(new Set());
  const [expandedEvents, setExpandedEvents] = useState<Set<string>>(new Set());
  const [filterMenuAnchor, setFilterMenuAnchor] = useState<null | HTMLElement>(null);
  
  // Refs
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const timelineService = useRef(new TimelineService());
  
  // Load Timeline Events
  useEffect(() => {
    loadTimelineEvents();
  }, [contactId, filter]);
  
  const loadTimelineEvents = async () => {
    setLoading(true);
    setError(null);
    
    try {
      const timelineEvents = await timelineService.current.getContactTimeline(
        contactId,
        {
          filter,
          limit: 50
        }
      );
      setEvents(timelineEvents);
    } catch (err) {
      setError('Fehler beim Laden der Timeline');
      console.error('Timeline load error:', err);
    } finally {
      setLoading(false);
    }
  };
  
  // Filter & Search Events
  const filteredEvents = useMemo(() => {
    let filtered = [...events];
    
    // Apply search term
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(event =>
        event.title.toLowerCase().includes(term) ||
        event.description?.toLowerCase().includes(term) ||
        event.performer?.name.toLowerCase().includes(term)
      );
    }
    
    // Apply date range
    if (filter.dateRange) {
      filtered = filtered.filter(event => {
        const eventDate = new Date(event.timestamp);
        return eventDate >= filter.dateRange!.start && 
               eventDate <= filter.dateRange!.end;
      });
    }
    
    // Apply type filter
    if (filter.eventTypes?.length) {
      filtered = filtered.filter(event => 
        filter.eventTypes!.includes(event.type)
      );
    }
    
    // Apply category filter
    if (filter.categories?.length) {
      filtered = filtered.filter(event => 
        filter.categories!.includes(event.category)
      );
    }
    
    // Apply sentiment filter
    if (filter.sentiment?.length) {
      filtered = filtered.filter(event => 
        event.impact?.sentiment && 
        filter.sentiment!.includes(event.impact.sentiment)
      );
    }
    
    // Sort by timestamp descending
    return filtered.sort((a, b) => 
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
  }, [events, searchTerm, filter]);
  
  // Group Events
  const groupedEvents = useMemo(() => {
    const groups = new Map<string, TimelineEvent[]>();
    
    filteredEvents.forEach(event => {
      let groupKey: string;
      
      switch (grouping.by) {
        case 'day':
          groupKey = format(new Date(event.timestamp), 'yyyy-MM-dd');
          break;
        case 'week':
          groupKey = format(new Date(event.timestamp), 'yyyy-ww');
          break;
        case 'month':
          groupKey = format(new Date(event.timestamp), 'yyyy-MM');
          break;
        case 'category':
          groupKey = event.category;
          break;
        case 'performer':
          groupKey = event.performer?.name || 'System';
          break;
        default:
          groupKey = 'all';
      }
      
      if (!groups.has(groupKey)) {
        groups.set(groupKey, []);
      }
      groups.get(groupKey)!.push(event);
    });
    
    return Array.from(groups.entries()).map(([key, events]) => ({
      key,
      displayName: getGroupDisplayName(key, grouping.by),
      events: events.sort((a, b) => 
        new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
      ),
      stats: calculateGroupStats(events)
    }));
  }, [filteredEvents, grouping]);
  
  // Group Display Names
  const getGroupDisplayName = (key: string, groupBy: string): string => {
    switch (groupBy) {
      case 'day':
        const date = new Date(key);
        if (isToday(date)) return 'Heute';
        if (isYesterday(date)) return 'Gestern';
        return format(date, 'EEEE, d. MMMM yyyy', { locale: de });
      case 'week':
        return `KW ${key.split('-')[1]}`;
      case 'month':
        return format(new Date(key + '-01'), 'MMMM yyyy', { locale: de });
      case 'category':
        return getCategoryLabel(key as EventCategory);
      default:
        return key;
    }
  };
  
  // Category Labels
  const getCategoryLabel = (category: EventCategory): string => {
    const labels = {
      [EventCategory.COMMUNICATION]: 'Kommunikation',
      [EventCategory.BUSINESS]: 'Geschäft',
      [EventCategory.SYSTEM]: 'System',
      [EventCategory.MILESTONE]: 'Meilensteine',
      [EventCategory.TASK]: 'Aufgaben',
      [EventCategory.RELATIONSHIP]: 'Beziehung'
    };
    return labels[category] || category;
  };
  
  // Calculate Group Statistics
  const calculateGroupStats = (events: TimelineEvent[]) => {
    const warmthChange = events.reduce((sum, e) => 
      sum + (e.impact?.warmthChange || 0), 0
    );
    
    const sentiments = events.filter(e => e.impact?.sentiment);
    const positive = sentiments.filter(e => e.impact?.sentiment === 'positive').length;
    const negative = sentiments.filter(e => e.impact?.sentiment === 'negative').length;
    
    return {
      count: events.length,
      warmthChange,
      positiveRatio: sentiments.length > 0 ? positive / sentiments.length : 0
    };
  };
  
  // Event Icon Mapping
  const getEventIcon = (type: TimelineEventType): React.ReactNode => {
    const iconMap: Record<string, React.ReactNode> = {
      [TimelineEventType.CALL_INCOMING]: <PhoneIcon />,
      [TimelineEventType.CALL_OUTGOING]: <PhoneIcon />,
      [TimelineEventType.EMAIL_SENT]: <EmailIcon />,
      [TimelineEventType.EMAIL_RECEIVED]: <EmailIcon />,
      [TimelineEventType.MEETING_HELD]: <EventIcon />,
      [TimelineEventType.QUOTE_SENT]: <DocumentIcon />,
      [TimelineEventType.ORDER_PLACED]: <TrendingUpIcon />,
      [TimelineEventType.BIRTHDAY]: <CakeIcon />,
      [TimelineEventType.COMPLAINT_RECEIVED]: <WarningIcon />,
      [TimelineEventType.TASK_COMPLETED]: <SuccessIcon />
    };
    
    return iconMap[type] || <InfoIcon />;
  };
  
  // Event Color Mapping
  const getEventColor = (event: TimelineEvent): 'primary' | 'secondary' | 'error' | 'warning' | 'info' | 'success' | 'grey' => {
    // By sentiment
    if (event.impact?.sentiment === 'positive') return 'success';
    if (event.impact?.sentiment === 'negative') return 'error';
    
    // By category
    const categoryColors: Record<EventCategory, any> = {
      [EventCategory.COMMUNICATION]: 'primary',
      [EventCategory.BUSINESS]: 'success',
      [EventCategory.SYSTEM]: 'grey',
      [EventCategory.MILESTONE]: 'secondary',
      [EventCategory.TASK]: 'info',
      [EventCategory.RELATIONSHIP]: 'warning'
    };
    
    return categoryColors[event.category] || 'grey';
  };
  
  // Toggle Handlers
  const toggleGroup = (groupKey: string) => {
    setExpandedGroups(prev => {
      const next = new Set(prev);
      if (next.has(groupKey)) {
        next.delete(groupKey);
      } else {
        next.add(groupKey);
      }
      return next;
    });
  };
  
  const toggleEvent = (eventId: string) => {
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
  
  // Infinite Scroll
  const { loadMore, hasMore } = useInfiniteScroll({
    loadMoreItems: async () => {
      // Load next batch
      const moreEvents = await timelineService.current.getContactTimeline(
        contactId,
        {
          filter,
          offset: events.length,
          limit: 50
        }
      );
      setEvents(prev => [...prev, ...moreEvents]);
    },
    hasMore: events.length % 50 === 0 && events.length > 0,
    scrollContainer: scrollContainerRef.current
  });
  
  // Quick Filters
  const applyQuickFilter = (type: string) => {
    switch (type) {
      case 'calls':
        setFilter({
          eventTypes: [
            TimelineEventType.CALL_INCOMING,
            TimelineEventType.CALL_OUTGOING,
            TimelineEventType.CALL_MISSED
          ]
        });
        break;
      case 'emails':
        setFilter({
          eventTypes: [
            TimelineEventType.EMAIL_SENT,
            TimelineEventType.EMAIL_RECEIVED
          ]
        });
        break;
      case 'meetings':
        setFilter({
          eventTypes: [
            TimelineEventType.MEETING_HELD,
            TimelineEventType.MEETING_SCHEDULED
          ]
        });
        break;
      case 'business':
        setFilter({
          categories: [EventCategory.BUSINESS]
        });
        break;
      case 'positive':
        setFilter({
          sentiment: ['positive']
        });
        break;
      case 'today':
        setFilter({
          dateRange: {
            start: startOfDay(new Date()),
            end: endOfDay(new Date())
          }
        });
        break;
      default:
        setFilter({});
    }
    setFilterMenuAnchor(null);
  };
  
  return (
    <Box>
      {/* Analytics Section */}
      {showAnalytics && !loading && (
        <TimelineAnalytics 
          events={filteredEvents}
          contactId={contactId}
        />
      )}
      
      {/* Controls Bar */}
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Box display="flex" gap={2} alignItems="center" flexWrap="wrap">
            {/* Search */}
            <TextField
              size="small"
              placeholder="Timeline durchsuchen..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              sx={{ flex: 1, minWidth: 200 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                )
              }}
            />
            
            {/* Grouping Toggle */}
            <ToggleButtonGroup
              size="small"
              value={grouping.by}
              exclusive
              onChange={(_, value) => value && setGrouping({ by: value })}
            >
              <ToggleButton value="day">
                <Tooltip title="Nach Tag gruppieren">
                  <DayViewIcon />
                </Tooltip>
              </ToggleButton>
              <ToggleButton value="week">
                <Tooltip title="Nach Woche gruppieren">
                  <WeekViewIcon />
                </Tooltip>
              </ToggleButton>
              <ToggleButton value="category">
                <Tooltip title="Nach Kategorie gruppieren">
                  <CategoryViewIcon />
                </Tooltip>
              </ToggleButton>
            </ToggleButtonGroup>
            
            {/* Filter Menu */}
            <Badge
              badgeContent={Object.keys(filter).length}
              color="primary"
            >
              <IconButton
                onClick={(e) => setFilterMenuAnchor(e.currentTarget)}
                color={Object.keys(filter).length > 0 ? 'primary' : 'default'}
              >
                <FilterIcon />
              </IconButton>
            </Badge>
            
            {/* Refresh */}
            <IconButton onClick={loadTimelineEvents}>
              <RefreshIcon />
            </IconButton>
          </Box>
          
          {/* Active Filters */}
          {Object.keys(filter).length > 0 && (
            <Box display="flex" gap={1} mt={2} flexWrap="wrap">
              {filter.eventTypes?.map(type => (
                <Chip
                  key={type}
                  label={type}
                  size="small"
                  onDelete={() => {
                    setFilter(prev => ({
                      ...prev,
                      eventTypes: prev.eventTypes?.filter(t => t !== type)
                    }));
                  }}
                />
              ))}
              {filter.categories?.map(cat => (
                <Chip
                  key={cat}
                  label={getCategoryLabel(cat)}
                  size="small"
                  onDelete={() => {
                    setFilter(prev => ({
                      ...prev,
                      categories: prev.categories?.filter(c => c !== cat)
                    }));
                  }}
                />
              ))}
              {filter.dateRange && (
                <Chip
                  label={`${format(filter.dateRange.start, 'dd.MM')} - ${format(filter.dateRange.end, 'dd.MM')}`}
                  size="small"
                  onDelete={() => {
                    setFilter(prev => {
                      const { dateRange, ...rest } = prev;
                      return rest;
                    });
                  }}
                />
              )}
              <Chip
                label="Alle Filter löschen"
                size="small"
                variant="outlined"
                onClick={() => setFilter({})}
              />
            </Box>
          )}
        </CardContent>
      </Card>
      
      {/* Filter Menu */}
      <Menu
        anchorEl={filterMenuAnchor}
        open={Boolean(filterMenuAnchor)}
        onClose={() => setFilterMenuAnchor(null)}
      >
        <MenuItem onClick={() => applyQuickFilter('calls')}>
          <PhoneIcon sx={{ mr: 1 }} /> Nur Anrufe
        </MenuItem>
        <MenuItem onClick={() => applyQuickFilter('emails')}>
          <EmailIcon sx={{ mr: 1 }} /> Nur E-Mails
        </MenuItem>
        <MenuItem onClick={() => applyQuickFilter('meetings')}>
          <EventIcon sx={{ mr: 1 }} /> Nur Meetings
        </MenuItem>
        <Divider />
        <MenuItem onClick={() => applyQuickFilter('business')}>
          <TrendingUpIcon sx={{ mr: 1 }} /> Geschäftsereignisse
        </MenuItem>
        <MenuItem onClick={() => applyQuickFilter('positive')}>
          <SuccessIcon sx={{ mr: 1 }} /> Positive Ereignisse
        </MenuItem>
        <Divider />
        <MenuItem onClick={() => applyQuickFilter('today')}>
          Nur heute
        </MenuItem>
        <MenuItem onClick={() => setFilter({})}>
          Filter zurücksetzen
        </MenuItem>
      </Menu>
      
      {/* Timeline Content */}
      <Card>
        <Box 
          ref={scrollContainerRef}
          sx={{ 
            height, 
            overflow: 'auto',
            p: 2
          }}
        >
          {loading ? (
            // Loading Skeleton
            Array.from({ length: 3 }).map((_, i) => (
              <Box key={i} mb={3}>
                <Skeleton variant="text" width={150} height={30} />
                <Skeleton variant="rectangular" height={100} sx={{ mt: 1 }} />
              </Box>
            ))
          ) : error ? (
            // Error State
            <Alert severity="error">
              {error}
              <Button onClick={loadTimelineEvents} sx={{ ml: 2 }}>
                Erneut versuchen
              </Button>
            </Alert>
          ) : filteredEvents.length === 0 ? (
            // Empty State
            <Box textAlign="center" py={4}>
              <Typography variant="h6" color="text.secondary" gutterBottom>
                Keine Ereignisse gefunden
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {searchTerm ? 'Versuchen Sie einen anderen Suchbegriff' : 
                 filter.eventTypes?.length ? 'Erweitern Sie Ihre Filter' :
                 'Noch keine Interaktionen mit diesem Kontakt'}
              </Typography>
            </Box>
          ) : (
            // Timeline Groups
            groupedEvents.map(group => (
              <Box key={group.key} mb={3}>
                {/* Group Header */}
                <Box
                  display="flex"
                  alignItems="center"
                  justifyContent="space-between"
                  sx={{
                    cursor: 'pointer',
                    py: 1,
                    px: 2,
                    bgcolor: 'action.hover',
                    borderRadius: 1,
                    mb: 1,
                    '&:hover': {
                      bgcolor: 'action.selected'
                    }
                  }}
                  onClick={() => toggleGroup(group.key)}
                >
                  <Box display="flex" alignItems="center" gap={1}>
                    <IconButton size="small">
                      {expandedGroups.has(group.key) ? 
                        <ExpandLessIcon /> : <ExpandMoreIcon />
                      }
                    </IconButton>
                    <Typography variant="subtitle1" fontWeight="medium">
                      {group.displayName}
                    </Typography>
                    <Chip 
                      label={group.events.length}
                      size="small"
                      color="primary"
                      variant="outlined"
                    />
                  </Box>
                  
                  <Box display="flex" gap={1} alignItems="center">
                    {/* Group Stats */}
                    {group.stats.warmthChange !== 0 && (
                      <Chip
                        label={group.stats.warmthChange > 0 ? 
                          `+${group.stats.warmthChange}` : 
                          group.stats.warmthChange
                        }
                        size="small"
                        color={group.stats.warmthChange > 0 ? 'success' : 'error'}
                        icon={group.stats.warmthChange > 0 ? 
                          <TrendingUpIcon /> : <TrendingDownIcon />
                        }
                      />
                    )}
                    {group.stats.positiveRatio > 0 && (
                      <Box display="flex" alignItems="center">
                        <Typography variant="caption" color="text.secondary">
                          {Math.round(group.stats.positiveRatio * 100)}% positiv
                        </Typography>
                      </Box>
                    )}
                  </Box>
                </Box>
                
                {/* Group Events */}
                <Collapse in={expandedGroups.has(group.key)}>
                  <Timeline position="right">
                    {group.events.map((event, index) => (
                      <TimelineItem key={event.id}>
                        <TimelineOppositeContent
                          sx={{ m: 'auto 0', maxWidth: 100 }}
                          align="right"
                          variant="body2"
                          color="text.secondary"
                        >
                          {format(new Date(event.timestamp), 'HH:mm')}
                        </TimelineOppositeContent>
                        
                        <TimelineSeparator>
                          <TimelineConnector 
                            sx={{ 
                              visibility: index === 0 ? 'hidden' : 'visible' 
                            }} 
                          />
                          <TimelineDot 
                            color={getEventColor(event)}
                            variant={event.isHighlighted ? 'filled' : 'outlined'}
                          >
                            {getEventIcon(event.type)}
                          </TimelineDot>
                          <TimelineConnector 
                            sx={{ 
                              visibility: index === group.events.length - 1 ? 
                                'hidden' : 'visible' 
                            }} 
                          />
                        </TimelineSeparator>
                        
                        <TimelineContent sx={{ py: '12px', px: 2 }}>
                          <TimelineEventCard
                            event={event}
                            expanded={expandedEvents.has(event.id)}
                            onToggle={() => toggleEvent(event.id)}
                            onClick={() => onEventClick?.(event)}
                            onAction={(action) => onEventAction?.(event, action)}
                          />
                        </TimelineContent>
                      </TimelineItem>
                    ))}
                  </Timeline>
                </Collapse>
              </Box>
            ))
          )}
          
          {/* Load More */}
          {hasMore && !loading && (
            <Box textAlign="center" py={2}>
              <Button onClick={loadMore}>
                Weitere Ereignisse laden
              </Button>
            </Box>
          )}
        </Box>
      </Card>
    </Box>
  );
};
```

## 🔄 TIMELINE SERVICE (Backend Integration)

**Datei:** `frontend/src/features/customers/services/timeline/TimelineService.ts`

```typescript
// CLAUDE: Service für Timeline-Daten Aggregation
// Pfad: frontend/src/features/customers/services/timeline/TimelineService.ts

import { TimelineEvent, TimelineFilter, TimelineEventType, EventCategory } from '../../types/timeline.types';
import { contactApi } from '../contactApi';
import { opportunityApi } from '../opportunityApi';
import { taskApi } from '../taskApi';

export class TimelineService {
  
  async getContactTimeline(
    contactId: string,
    options?: {
      filter?: TimelineFilter;
      limit?: number;
      offset?: number;
    }
  ): Promise<TimelineEvent[]> {
    // Sammle Events aus verschiedenen Quellen parallel
    const [
      interactions,
      opportunities,
      tasks,
      systemEvents
    ] = await Promise.all([
      this.getInteractionEvents(contactId),
      this.getOpportunityEvents(contactId),
      this.getTaskEvents(contactId),
      this.getSystemEvents(contactId)
    ]);
    
    // Merge alle Events
    let allEvents = [
      ...interactions,
      ...opportunities,
      ...tasks,
      ...systemEvents
    ];
    
    // Apply filters
    if (options?.filter) {
      allEvents = this.applyFilters(allEvents, options.filter);
    }
    
    // Sort by timestamp descending
    allEvents.sort((a, b) => 
      new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
    
    // Apply pagination
    if (options?.limit) {
      const start = options.offset || 0;
      allEvents = allEvents.slice(start, start + options.limit);
    }
    
    return allEvents;
  }
  
  private async getInteractionEvents(contactId: string): Promise<TimelineEvent[]> {
    try {
      const interactions = await contactApi.getContactInteractions(contactId);
      
      return interactions.map(interaction => ({
        id: interaction.id,
        contactId,
        customerId: interaction.customerId,
        timestamp: new Date(interaction.interactionDate),
        type: this.mapInteractionType(interaction.interactionType, interaction.direction),
        category: EventCategory.COMMUNICATION,
        title: this.getInteractionTitle(interaction),
        description: interaction.notes || undefined,
        metadata: {
          duration: interaction.durationMinutes,
          channel: interaction.channel,
          direction: interaction.direction?.toLowerCase() as any
        },
        performer: interaction.userId ? {
          id: interaction.userId,
          name: interaction.userName || 'Unbekannt'
        } : undefined,
        impact: {
          sentiment: interaction.sentiment?.toLowerCase() as any,
          warmthChange: this.calculateWarmthImpact(interaction)
        }
      }));
    } catch (error) {
      console.error('Failed to load interactions:', error);
      return [];
    }
  }
  
  private async getOpportunityEvents(contactId: string): Promise<TimelineEvent[]> {
    try {
      const opportunities = await opportunityApi.getContactOpportunities(contactId);
      const events: TimelineEvent[] = [];
      
      opportunities.forEach(opp => {
        // Opportunity created
        events.push({
          id: `opp-created-${opp.id}`,
          contactId,
          customerId: opp.customerId,
          timestamp: new Date(opp.createdAt),
          type: TimelineEventType.QUOTE_SENT,
          category: EventCategory.BUSINESS,
          title: `Opportunity erstellt: ${opp.name}`,
          description: opp.description,
          relatedEntities: {
            opportunityId: opp.id
          },
          impact: {
            dealValue: opp.value,
            sentiment: 'positive'
          }
        });
        
        // Status changes
        if (opp.statusHistory) {
          opp.statusHistory.forEach(status => {
            events.push({
              id: `opp-status-${opp.id}-${status.timestamp}`,
              contactId,
              customerId: opp.customerId,
              timestamp: new Date(status.timestamp),
              type: this.mapOpportunityStatus(status.status),
              category: EventCategory.BUSINESS,
              title: `Status geändert: ${status.status}`,
              relatedEntities: {
                opportunityId: opp.id
              },
              impact: {
                dealValue: opp.value,
                sentiment: this.getStatusSentiment(status.status)
              }
            });
          });
        }
      });
      
      return events;
    } catch (error) {
      console.error('Failed to load opportunities:', error);
      return [];
    }
  }
  
  private async getTaskEvents(contactId: string): Promise<TimelineEvent[]> {
    try {
      const tasks = await taskApi.getContactTasks(contactId);
      
      return tasks.map(task => ({
        id: `task-${task.id}`,
        contactId,
        customerId: task.customerId,
        timestamp: new Date(task.completedAt || task.dueDate || task.createdAt),
        type: task.completedAt ? 
          TimelineEventType.TASK_COMPLETED : 
          TimelineEventType.TASK_CREATED,
        category: EventCategory.TASK,
        title: task.title,
        description: task.description,
        relatedEntities: {
          taskId: task.id
        },
        performer: task.assignedTo ? {
          id: task.assignedTo,
          name: task.assignedToName || 'Unbekannt'
        } : undefined,
        impact: {
          sentiment: task.completedAt ? 'positive' : 'neutral',
          urgency: task.priority as any
        }
      }));
    } catch (error) {
      console.error('Failed to load tasks:', error);
      return [];
    }
  }
  
  private async getSystemEvents(contactId: string): Promise<TimelineEvent[]> {
    // System events wie Contact Created, Updated, etc.
    try {
      const contact = await contactApi.getContact(contactId);
      const events: TimelineEvent[] = [];
      
      // Contact created
      events.push({
        id: `contact-created-${contact.id}`,
        contactId,
        customerId: contact.customerId,
        timestamp: new Date(contact.createdAt),
        type: TimelineEventType.CONTACT_CREATED,
        category: EventCategory.SYSTEM,
        title: 'Kontakt angelegt',
        performer: contact.createdBy ? {
          id: contact.createdBy,
          name: contact.createdByName || 'System'
        } : undefined
      });
      
      // Data enrichments
      if (contact.enrichmentHistory) {
        contact.enrichmentHistory.forEach(enrichment => {
          events.push({
            id: `enrichment-${enrichment.id}`,
            contactId,
            customerId: contact.customerId,
            timestamp: new Date(enrichment.timestamp),
            type: TimelineEventType.DATA_ENRICHED,
            category: EventCategory.SYSTEM,
            title: 'Daten angereichert',
            description: enrichment.fields.join(', ')
          });
        });
      }
      
      return events;
    } catch (error) {
      console.error('Failed to load system events:', error);
      return [];
    }
  }
  
  private applyFilters(events: TimelineEvent[], filter: TimelineFilter): TimelineEvent[] {
    let filtered = [...events];
    
    if (filter.eventTypes?.length) {
      filtered = filtered.filter(e => filter.eventTypes!.includes(e.type));
    }
    
    if (filter.categories?.length) {
      filtered = filtered.filter(e => filter.categories!.includes(e.category));
    }
    
    if (filter.dateRange) {
      filtered = filtered.filter(e => {
        const date = new Date(e.timestamp);
        return date >= filter.dateRange!.start && date <= filter.dateRange!.end;
      });
    }
    
    if (filter.sentiment?.length) {
      filtered = filtered.filter(e => 
        e.impact?.sentiment && filter.sentiment!.includes(e.impact.sentiment)
      );
    }
    
    if (filter.hasImpact) {
      filtered = filtered.filter(e => 
        e.impact && (e.impact.warmthChange || e.impact.dealValue)
      );
    }
    
    if (filter.minWarmthChange) {
      filtered = filtered.filter(e => 
        (e.impact?.warmthChange || 0) >= filter.minWarmthChange!
      );
    }
    
    return filtered;
  }
  
  private mapInteractionType(type: string, direction?: string): TimelineEventType {
    const typeMap: Record<string, TimelineEventType> = {
      'CALL_INCOMING': TimelineEventType.CALL_INCOMING,
      'CALL_OUTGOING': TimelineEventType.CALL_OUTGOING,
      'EMAIL_INCOMING': TimelineEventType.EMAIL_RECEIVED,
      'EMAIL_OUTGOING': TimelineEventType.EMAIL_SENT,
      'MEETING': TimelineEventType.MEETING_HELD,
      'WHATSAPP_INCOMING': TimelineEventType.WHATSAPP_RECEIVED,
      'WHATSAPP_OUTGOING': TimelineEventType.WHATSAPP_SENT
    };
    
    const key = `${type}_${direction}`.toUpperCase();
    return typeMap[key] || TimelineEventType.NOTE_ADDED;
  }
  
  private getInteractionTitle(interaction: any): string {
    const typeLabels: Record<string, string> = {
      'CALL': 'Telefonat',
      'EMAIL': 'E-Mail',
      'MEETING': 'Meeting',
      'WHATSAPP': 'WhatsApp',
      'SMS': 'SMS',
      'NOTE': 'Notiz'
    };
    
    const directionLabels: Record<string, string> = {
      'INCOMING': 'eingehend',
      'OUTGOING': 'ausgehend'
    };
    
    const type = typeLabels[interaction.interactionType] || interaction.interactionType;
    const direction = directionLabels[interaction.direction] || '';
    
    return `${type} ${direction}`.trim();
  }
  
  private calculateWarmthImpact(interaction: any): number {
    let impact = 0;
    
    // Positive interactions
    if (interaction.sentiment === 'POSITIVE') impact += 3;
    if (interaction.responseReceived) impact += 2;
    if (interaction.direction === 'INCOMING') impact += 2;
    
    // Negative interactions
    if (interaction.sentiment === 'NEGATIVE') impact -= 3;
    
    // Quality bonus
    if (interaction.qualityScore > 80) impact += 2;
    
    return impact;
  }
  
  private mapOpportunityStatus(status: string): TimelineEventType {
    const statusMap: Record<string, TimelineEventType> = {
      'QUOTED': TimelineEventType.QUOTE_SENT,
      'WON': TimelineEventType.ORDER_PLACED,
      'LOST': TimelineEventType.QUOTE_REJECTED
    };
    
    return statusMap[status] || TimelineEventType.NOTE_ADDED;
  }
  
  private getStatusSentiment(status: string): 'positive' | 'neutral' | 'negative' {
    if (status === 'WON') return 'positive';
    if (status === 'LOST') return 'negative';
    return 'neutral';
  }
}
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE FÜR CLAUDE

### Phase 1: Types & Foundation (20 Min)
- [ ] timeline.types.ts erstellen (200 Zeilen)
- [ ] MUI Lab installieren (`npm install @mui/lab`)
- [ ] TimelineService.ts implementieren (300 Zeilen)
- [ ] EventFormatter.ts für Event-Mapping

### Phase 2: Main Component (40 Min)
- [ ] ContactTimeline.tsx implementieren (500 Zeilen)
- [ ] TimelineEventCard.tsx für Event-Darstellung (150 Zeilen)
- [ ] TimelineFilter.tsx für Filter-UI (200 Zeilen)
- [ ] TimelineAnalytics.tsx für Statistiken (250 Zeilen)

### Phase 3: Backend Integration (30 Min)
- [ ] ContactTimelineResource.java erstellen
- [ ] TimelineService.java für Event-Aggregation
- [ ] REST Endpoints:
  - GET /api/contacts/{id}/timeline
  - GET /api/contacts/{id}/timeline/analytics
  - POST /api/contacts/{id}/timeline/filter

### Phase 4: Store Integration (15 Min)
- [ ] Timeline Store mit Zustand
- [ ] Integration in ContactStore
- [ ] Caching-Strategie implementieren

### Phase 5: Testing (20 Min)
- [ ] ContactTimeline.test.tsx
- [ ] TimelineService.test.ts
- [ ] Filter-Tests
- [ ] Performance-Tests

## 🔗 INTEGRATION POINTS

### Mit Relationship Intelligence:
```typescript
// Warmth-Changes in Timeline anzeigen
const warmthChanges = events.filter(e => e.impact?.warmthChange);
<WarmthTrendChart events={warmthChanges} />
```

### Mit Smart Contact Cards:
```typescript
// Mini-Timeline in Contact Card
<ContactCard>
  <MiniTimeline 
    contactId={contact.id}
    limit={5}
    compact={true}
  />
</ContactCard>
```

### Mit Mobile Actions:
```typescript
// Timeline-Events als Trigger für Actions
const lastInteraction = timeline[0];
if (daysSince(lastInteraction) > 30) {
  suggestAction('REACH_OUT');
}
```

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Performance bei vielen Events**
   → Lösung: Virtualisierung + Pagination

2. **Gruppierung verliert Kontext**
   → Lösung: Expandable Groups mit Stats

3. **Filter zu komplex**
   → Lösung: Quick Filters für häufige Cases

4. **Timeline zu lang**
   → Lösung: Infinite Scroll + Load More

## 🚀 NÄCHSTE SCHRITTE NACH IMPLEMENTIERUNG

1. **Smart Suggestions** ausarbeiten
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_SUGGESTIONS.md)

2. **Testing Integration** durchführen
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/TESTING_INTEGRATION.md)

3. **Integration Summary** aktualisieren
   → [Dokument öffnen](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/INTEGRATION_SUMMARY.md)

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 125 Minuten  
**Kritischer Pfad:** Types → TimelineService → ContactTimeline → Backend Integration  
**Nächstes Dokument:** [→ Smart Suggestions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_SUGGESTIONS.md)  
**Parent:** [↑ Step3 Übersicht](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md)

**Contact Timeline = Die Geschichte jeder Geschäftsbeziehung! 📊📈🎯**