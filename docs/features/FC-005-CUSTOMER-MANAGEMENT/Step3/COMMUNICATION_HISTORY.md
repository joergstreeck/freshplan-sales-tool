# 📞 Communication History - Lückenlose Interaktions-Historie

**Phase:** 1 - Core Functionality  
**Priorität:** 🔴 KRITISCH - Ohne Historie = Blindflug  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/DUPLICATE_DETECTION.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/ORG_CHART_VISUALIZATION.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- Vertrieb (Gespräche nachvollziehen)
- Support (Historie einsehen)
- Management (Aktivität tracken)

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Communication History implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Communication Entities
mkdir -p backend/src/main/java/de/freshplan/communication
touch backend/src/main/java/de/freshplan/communication/entity/CommunicationEvent.java
touch backend/src/main/java/de/freshplan/communication/service/CommunicationHistoryService.java
touch backend/src/main/java/de/freshplan/communication/resource/CommunicationHistoryResource.java

# 2. Frontend Communication Components
mkdir -p frontend/src/features/customers/components/communication
touch frontend/src/features/customers/components/communication/CommunicationTimeline.tsx
touch frontend/src/features/customers/components/communication/CommunicationEntry.tsx
touch frontend/src/features/customers/components/communication/QuickLogDialog.tsx
touch frontend/src/features/customers/components/communication/CommunicationStats.tsx

# 3. Communication Store
touch frontend/src/features/customers/stores/communicationStore.ts

# 4. Migration (nächste freie Nummer prüfen!)
ls -la backend/src/main/resources/db/migration/ | tail -5
# Erstelle V[NEXT]__create_communication_history_tables.sql

# 5. Tests
mkdir -p backend/src/test/java/de/freshplan/communication
touch backend/src/test/java/de/freshplan/communication/CommunicationHistoryServiceTest.java
```

## 🎯 Das Problem: Vertrieb ohne Gedächtnis

**Typische Katastrophen:**
- 📞 "Hatten wir nicht letzte Woche telefoniert?" - Keine Ahnung!
- 📧 "Was hatten Sie mir per Email geschickt?" - Suche beginnt...
- 🤝 "Beim letzten Treffen sagten Sie..." - War das wirklich so?
- 📊 "Wie oft hatten wir Kontakt?" - Excel-Chaos durchsuchen

## 💡 DIE LÖSUNG: Automatische Communication History

### 1. Communication Event Entity

**Datei:** `backend/src/main/java/de/freshplan/communication/entity/CommunicationEvent.java`

```java
// CLAUDE: Umfassende Communication History mit allen Kanälen
// Pfad: backend/src/main/java/de/freshplan/communication/entity/CommunicationEvent.java

package de.freshplan.communication.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "communication_events")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "event_type", discriminatorType = DiscriminatorType.STRING)
public abstract class CommunicationEvent extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    public Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    public CustomerContact contact;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    public User user; // FreshPlan User der kommuniziert hat
    
    @Column(name = "occurred_at", nullable = false)
    public LocalDateTime occurredAt;
    
    @Column(name = "duration_minutes")
    public Integer durationMinutes;
    
    @Column(name = "subject", length = 500)
    public String subject;
    
    @Column(name = "summary", columnDefinition = "TEXT")
    public String summary;
    
    @Column(name = "outcome")
    @Enumerated(EnumType.STRING)
    public CommunicationOutcome outcome;
    
    @Column(name = "sentiment")
    @Enumerated(EnumType.STRING)
    public CommunicationSentiment sentiment;
    
    @ElementCollection
    @CollectionTable(name = "communication_tags")
    public Set<String> tags = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "communication_attachments")
    public List<String> attachmentUrls = new ArrayList<>();
    
    @Column(name = "follow_up_required")
    public Boolean followUpRequired = false;
    
    @Column(name = "follow_up_date")
    public LocalDateTime followUpDate;
    
    @Column(name = "follow_up_notes", columnDefinition = "TEXT")
    public String followUpNotes;
    
    // Metadata as JSON for flexibility
    @Column(name = "metadata", columnDefinition = "jsonb")
    @Convert(converter = JsonNodeConverter.class)
    public JsonNode metadata;
    
    // Integration tracking
    @Column(name = "external_id")
    public String externalId; // ID from external system (CRM, Email, etc.)
    
    @Column(name = "external_source")
    public String externalSource; // "outlook", "gmail", "manual", "api"
    
    // Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @Column(name = "created_by", nullable = false, updatable = false)
    public UUID createdBy;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Abstract method for channel-specific details
    public abstract CommunicationChannel getChannel();
    public abstract String getChannelDetails();
}

// Email Communication
@Entity
@DiscriminatorValue("EMAIL")
public class EmailCommunication extends CommunicationEvent {
    
    @Column(name = "from_address")
    public String fromAddress;
    
    @Column(name = "to_addresses", columnDefinition = "TEXT")
    public String toAddresses; // Comma-separated
    
    @Column(name = "cc_addresses", columnDefinition = "TEXT")
    public String ccAddresses;
    
    @Column(name = "message_id")
    public String messageId;
    
    @Column(name = "thread_id")
    public String threadId;
    
    @Column(name = "reply_to_id")
    public String replyToId;
    
    @Column(name = "email_body", columnDefinition = "TEXT")
    public String emailBody;
    
    @Column(name = "is_inbound")
    public Boolean isInbound = false;
    
    @Override
    public CommunicationChannel getChannel() {
        return CommunicationChannel.EMAIL;
    }
    
    @Override
    public String getChannelDetails() {
        return isInbound ? "Eingehende Email" : "Ausgehende Email";
    }
}

// Phone Communication
@Entity
@DiscriminatorValue("PHONE")
public class PhoneCommunication extends CommunicationEvent {
    
    @Column(name = "phone_number")
    public String phoneNumber;
    
    @Column(name = "call_direction")
    @Enumerated(EnumType.STRING)
    public CallDirection direction; // INBOUND, OUTBOUND, MISSED
    
    @Column(name = "recording_url")
    public String recordingUrl;
    
    @Column(name = "transcript", columnDefinition = "TEXT")
    public String transcript;
    
    @Override
    public CommunicationChannel getChannel() {
        return CommunicationChannel.PHONE;
    }
    
    @Override
    public String getChannelDetails() {
        return String.format("%s Anruf (%d Min)", 
            direction == CallDirection.INBOUND ? "Eingehender" : "Ausgehender",
            durationMinutes != null ? durationMinutes : 0
        );
    }
}

// Meeting Communication
@Entity
@DiscriminatorValue("MEETING")
public class MeetingCommunication extends CommunicationEvent {
    
    @Column(name = "meeting_type")
    @Enumerated(EnumType.STRING)
    public MeetingType meetingType; // IN_PERSON, VIDEO, PHONE_CONFERENCE
    
    @Column(name = "location")
    public String location;
    
    @Column(name = "meeting_url")
    public String meetingUrl; // For video meetings
    
    @ElementCollection
    @CollectionTable(name = "meeting_participants")
    public Set<UUID> participantIds = new HashSet<>();
    
    @Column(name = "agenda", columnDefinition = "TEXT")
    public String agenda;
    
    @Column(name = "minutes", columnDefinition = "TEXT")
    public String minutes;
    
    @ElementCollection
    @CollectionTable(name = "meeting_action_items")
    public List<String> actionItems = new ArrayList<>();
    
    @Override
    public CommunicationChannel getChannel() {
        return CommunicationChannel.MEETING;
    }
    
    @Override
    public String getChannelDetails() {
        return meetingType == MeetingType.IN_PERSON ? 
            "Persönliches Treffen" : 
            "Video-Meeting";
    }
}

// Chat/Message Communication
@Entity
@DiscriminatorValue("CHAT")
public class ChatCommunication extends CommunicationEvent {
    
    @Column(name = "platform")
    public String platform; // "whatsapp", "slack", "teams", "sms"
    
    @Column(name = "conversation_id")
    public String conversationId;
    
    @Column(name = "message_text", columnDefinition = "TEXT")
    public String messageText;
    
    @Column(name = "is_group_chat")
    public Boolean isGroupChat = false;
    
    @Override
    public CommunicationChannel getChannel() {
        return CommunicationChannel.CHAT;
    }
    
    @Override
    public String getChannelDetails() {
        return String.format("%s Nachricht", 
            platform != null ? platform.substring(0, 1).toUpperCase() + platform.substring(1) : "Chat"
        );
    }
}

// Enums
public enum CommunicationChannel {
    EMAIL("Email"),
    PHONE("Telefon"),
    MEETING("Meeting"),
    CHAT("Chat/Nachricht"),
    LETTER("Brief"),
    FAX("Fax"),
    SOCIAL("Social Media");
    
    private final String displayName;
    
    CommunicationChannel(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}

public enum CommunicationOutcome {
    SUCCESSFUL("Erfolgreich"),
    FOLLOW_UP_NEEDED("Follow-up nötig"),
    NO_RESPONSE("Keine Antwort"),
    POSTPONED("Verschoben"),
    CANCELLED("Abgesagt"),
    NOT_INTERESTED("Kein Interesse");
    
    private final String displayName;
    
    CommunicationOutcome(String displayName) {
        this.displayName = displayName;
    }
}

public enum CommunicationSentiment {
    VERY_POSITIVE("Sehr positiv"),
    POSITIVE("Positiv"),
    NEUTRAL("Neutral"),
    NEGATIVE("Negativ"),
    VERY_NEGATIVE("Sehr negativ");
    
    private final String displayName;
    
    CommunicationSentiment(String displayName) {
        this.displayName = displayName;
    }
}

public enum CallDirection {
    INBOUND, OUTBOUND, MISSED
}

public enum MeetingType {
    IN_PERSON, VIDEO, PHONE_CONFERENCE
}
```

### 2. Frontend Communication Timeline

**Datei:** `frontend/src/features/customers/components/communication/CommunicationTimeline.tsx`

```typescript
// CLAUDE: Interaktive Communication Timeline mit Filtering
// Pfad: frontend/src/features/customers/components/communication/CommunicationTimeline.tsx

import React, { useState, useCallback, useMemo } from 'react';
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
  Button,
  Menu,
  MenuItem,
  TextField,
  InputAdornment,
  FormControl,
  InputLabel,
  Select,
  ToggleButton,
  ToggleButtonGroup,
  Collapse,
  Alert,
  Tooltip,
  Badge,
  Fab
} from '@mui/material';
import {
  Email as EmailIcon,
  Phone as PhoneIcon,
  Groups as MeetingIcon,
  Chat as ChatIcon,
  Description as DocumentIcon,
  Add as AddIcon,
  FilterList as FilterIcon,
  Search as SearchIcon,
  ExpandMore as ExpandIcon,
  ExpandLess as CollapseIcon,
  Star as StarIcon,
  Flag as FlagIcon,
  Schedule as ScheduleIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Remove as NeutralIcon
} from '@mui/icons-material';
import { format, formatDistanceToNow, isToday, isYesterday, isThisWeek } from 'date-fns';
import { de } from 'date-fns/locale';
import { CommunicationEvent, CommunicationChannel } from '../../../types/communication.types';
import { QuickLogDialog } from './QuickLogDialog';
import { CommunicationStats } from './CommunicationStats';

interface CommunicationTimelineProps {
  customerId: string;
  contactId?: string;
  onEventClick?: (event: CommunicationEvent) => void;
  onAddEvent?: () => void;
}

export const CommunicationTimeline: React.FC<CommunicationTimelineProps> = ({
  customerId,
  contactId,
  onEventClick,
  onAddEvent
}) => {
  const [events, setEvents] = useState<CommunicationEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({
    channels: [] as CommunicationChannel[],
    dateRange: 'all',
    searchTerm: '',
    sentiment: 'all',
    showFollowUp: false
  });
  const [expandedEvents, setExpandedEvents] = useState<Set<string>>(new Set());
  const [quickLogOpen, setQuickLogOpen] = useState(false);
  const [viewMode, setViewMode] = useState<'timeline' | 'list' | 'stats'>('timeline');
  
  // Load events
  useEffect(() => {
    loadCommunicationHistory();
  }, [customerId, contactId]);
  
  const loadCommunicationHistory = async () => {
    setLoading(true);
    try {
      const response = await fetch(
        `/api/communication/history?customerId=${customerId}${contactId ? `&contactId=${contactId}` : ''}`
      );
      const data = await response.json();
      setEvents(data);
    } finally {
      setLoading(false);
    }
  };
  
  // Filter events
  const filteredEvents = useMemo(() => {
    let filtered = [...events];
    
    // Channel filter
    if (filter.channels.length > 0) {
      filtered = filtered.filter(e => filter.channels.includes(e.channel));
    }
    
    // Date range filter
    const now = new Date();
    switch (filter.dateRange) {
      case 'today':
        filtered = filtered.filter(e => isToday(new Date(e.occurredAt)));
        break;
      case 'week':
        filtered = filtered.filter(e => isThisWeek(new Date(e.occurredAt)));
        break;
      case 'month':
        const monthAgo = new Date();
        monthAgo.setMonth(monthAgo.getMonth() - 1);
        filtered = filtered.filter(e => new Date(e.occurredAt) > monthAgo);
        break;
    }
    
    // Search filter
    if (filter.searchTerm) {
      const term = filter.searchTerm.toLowerCase();
      filtered = filtered.filter(e => 
        e.subject?.toLowerCase().includes(term) ||
        e.summary?.toLowerCase().includes(term) ||
        e.tags?.some(t => t.toLowerCase().includes(term))
      );
    }
    
    // Sentiment filter
    if (filter.sentiment !== 'all') {
      filtered = filtered.filter(e => e.sentiment === filter.sentiment);
    }
    
    // Follow-up filter
    if (filter.showFollowUp) {
      filtered = filtered.filter(e => e.followUpRequired);
    }
    
    // Sort by date (newest first)
    return filtered.sort((a, b) => 
      new Date(b.occurredAt).getTime() - new Date(a.occurredAt).getTime()
    );
  }, [events, filter]);
  
  // Group events by date
  const groupedEvents = useMemo(() => {
    const groups = new Map<string, CommunicationEvent[]>();
    
    filteredEvents.forEach(event => {
      const date = new Date(event.occurredAt);
      let groupKey: string;
      
      if (isToday(date)) {
        groupKey = 'Heute';
      } else if (isYesterday(date)) {
        groupKey = 'Gestern';
      } else if (isThisWeek(date)) {
        groupKey = 'Diese Woche';
      } else {
        groupKey = format(date, 'MMMM yyyy', { locale: de });
      }
      
      if (!groups.has(groupKey)) {
        groups.set(groupKey, []);
      }
      groups.get(groupKey)!.push(event);
    });
    
    return groups;
  }, [filteredEvents]);
  
  const getChannelIcon = (channel: CommunicationChannel) => {
    switch (channel) {
      case CommunicationChannel.EMAIL:
        return <EmailIcon />;
      case CommunicationChannel.PHONE:
        return <PhoneIcon />;
      case CommunicationChannel.MEETING:
        return <MeetingIcon />;
      case CommunicationChannel.CHAT:
        return <ChatIcon />;
      default:
        return <DocumentIcon />;
    }
  };
  
  const getSentimentIcon = (sentiment?: CommunicationSentiment) => {
    switch (sentiment) {
      case 'VERY_POSITIVE':
      case 'POSITIVE':
        return <TrendingUpIcon color="success" />;
      case 'NEGATIVE':
      case 'VERY_NEGATIVE':
        return <TrendingDownIcon color="error" />;
      default:
        return <NeutralIcon color="disabled" />;
    }
  };
  
  const getSentimentColor = (sentiment?: CommunicationSentiment) => {
    switch (sentiment) {
      case 'VERY_POSITIVE':
        return 'success';
      case 'POSITIVE':
        return 'success';
      case 'NEGATIVE':
        return 'error';
      case 'VERY_NEGATIVE':
        return 'error';
      default:
        return 'grey';
    }
  };
  
  const toggleEventExpansion = (eventId: string) => {
    const newExpanded = new Set(expandedEvents);
    if (newExpanded.has(eventId)) {
      newExpanded.delete(eventId);
    } else {
      newExpanded.add(eventId);
    }
    setExpandedEvents(newExpanded);
  };
  
  const renderTimelineItem = (event: CommunicationEvent) => {
    const isExpanded = expandedEvents.has(event.id);
    const eventDate = new Date(event.occurredAt);
    
    return (
      <TimelineItem key={event.id}>
        <TimelineOppositeContent sx={{ flex: 0.3 }}>
          <Typography variant="caption" color="text.secondary">
            {format(eventDate, 'HH:mm')}
          </Typography>
          {event.durationMinutes && (
            <Typography variant="caption" display="block" color="text.secondary">
              {event.durationMinutes} Min
            </Typography>
          )}
        </TimelineOppositeContent>
        
        <TimelineSeparator>
          <TimelineDot color={getSentimentColor(event.sentiment)}>
            {getChannelIcon(event.channel)}
          </TimelineDot>
          <TimelineConnector />
        </TimelineSeparator>
        
        <TimelineContent>
          <Card 
            sx={{ 
              cursor: 'pointer',
              '&:hover': { boxShadow: 3 }
            }}
            onClick={() => onEventClick?.(event)}
          >
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="flex-start">
                <Box flex={1}>
                  <Typography variant="h6" component="div">
                    {event.subject || event.getChannelDetails()}
                  </Typography>
                  
                  {event.contact && (
                    <Typography variant="body2" color="text.secondary">
                      mit {event.contact.firstName} {event.contact.lastName}
                    </Typography>
                  )}
                  
                  <Typography 
                    variant="body2" 
                    sx={{ 
                      mt: 1,
                      display: '-webkit-box',
                      WebkitLineClamp: isExpanded ? 'unset' : 2,
                      WebkitBoxOrient: 'vertical',
                      overflow: 'hidden'
                    }}
                  >
                    {event.summary}
                  </Typography>
                  
                  {/* Tags */}
                  {event.tags && event.tags.length > 0 && (
                    <Box mt={1} display="flex" gap={0.5} flexWrap="wrap">
                      {event.tags.map(tag => (
                        <Chip key={tag} label={tag} size="small" />
                      ))}
                    </Box>
                  )}
                  
                  {/* Follow-up indicator */}
                  {event.followUpRequired && (
                    <Alert severity="warning" sx={{ mt: 1 }}>
                      <Typography variant="caption">
                        Follow-up erforderlich bis {format(new Date(event.followUpDate!), 'dd.MM.yyyy')}
                      </Typography>
                    </Alert>
                  )}
                </Box>
                
                <Box display="flex" flexDirection="column" alignItems="flex-end">
                  {getSentimentIcon(event.sentiment)}
                  
                  <IconButton 
                    size="small"
                    onClick={(e) => {
                      e.stopPropagation();
                      toggleEventExpansion(event.id);
                    }}
                  >
                    {isExpanded ? <CollapseIcon /> : <ExpandIcon />}
                  </IconButton>
                </Box>
              </Box>
              
              {/* Expanded details */}
              <Collapse in={isExpanded}>
                <Box mt={2} pt={2} borderTop={1} borderColor="divider">
                  {event.outcome && (
                    <Typography variant="body2">
                      <strong>Ergebnis:</strong> {event.outcome}
                    </Typography>
                  )}
                  
                  {event.actionItems && event.actionItems.length > 0 && (
                    <Box mt={1}>
                      <Typography variant="body2"><strong>Action Items:</strong></Typography>
                      <ul>
                        {event.actionItems.map((item, idx) => (
                          <li key={idx}><Typography variant="body2">{item}</Typography></li>
                        ))}
                      </ul>
                    </Box>
                  )}
                  
                  {event.attachmentUrls && event.attachmentUrls.length > 0 && (
                    <Box mt={1}>
                      <Typography variant="body2">
                        <strong>Anhänge:</strong> {event.attachmentUrls.length} Datei(en)
                      </Typography>
                    </Box>
                  )}
                  
                  <Typography variant="caption" color="text.secondary" display="block" mt={1}>
                    Erfasst von {event.createdBy} • {formatDistanceToNow(eventDate, { locale: de, addSuffix: true })}
                  </Typography>
                </Box>
              </Collapse>
            </CardContent>
          </Card>
        </TimelineContent>
      </TimelineItem>
    );
  };
  
  return (
    <Box>
      {/* Header with View Toggle and Actions */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">
          Kommunikations-Historie
        </Typography>
        
        <Box display="flex" gap={2} alignItems="center">
          <ToggleButtonGroup
            value={viewMode}
            exclusive
            onChange={(_, value) => value && setViewMode(value)}
            size="small"
          >
            <ToggleButton value="timeline">
              <ScheduleIcon />
            </ToggleButton>
            <ToggleButton value="list">
              <FilterIcon />
            </ToggleButton>
            <ToggleButton value="stats">
              <TrendingUpIcon />
            </ToggleButton>
          </ToggleButtonGroup>
          
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setQuickLogOpen(true)}
          >
            Kommunikation erfassen
          </Button>
        </Box>
      </Box>
      
      {/* Filter Bar */}
      <Box mb={3} p={2} bgcolor="background.paper" borderRadius={1}>
        <Grid container spacing={2} alignItems="center">
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              size="small"
              placeholder="Suchen..."
              value={filter.searchTerm}
              onChange={(e) => setFilter({ ...filter, searchTerm: e.target.value })}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                )
              }}
            />
          </Grid>
          
          <Grid item xs={12} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Zeitraum</InputLabel>
              <Select
                value={filter.dateRange}
                onChange={(e) => setFilter({ ...filter, dateRange: e.target.value })}
                label="Zeitraum"
              >
                <MenuItem value="all">Alle</MenuItem>
                <MenuItem value="today">Heute</MenuItem>
                <MenuItem value="week">Diese Woche</MenuItem>
                <MenuItem value="month">Letzter Monat</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} md={2}>
            <FormControl fullWidth size="small">
              <InputLabel>Stimmung</InputLabel>
              <Select
                value={filter.sentiment}
                onChange={(e) => setFilter({ ...filter, sentiment: e.target.value })}
                label="Stimmung"
              >
                <MenuItem value="all">Alle</MenuItem>
                <MenuItem value="VERY_POSITIVE">Sehr positiv</MenuItem>
                <MenuItem value="POSITIVE">Positiv</MenuItem>
                <MenuItem value="NEUTRAL">Neutral</MenuItem>
                <MenuItem value="NEGATIVE">Negativ</MenuItem>
                <MenuItem value="VERY_NEGATIVE">Sehr negativ</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          
          <Grid item xs={12} md={4}>
            <Box display="flex" gap={1}>
              {Object.values(CommunicationChannel).map(channel => (
                <Chip
                  key={channel}
                  label={channel}
                  icon={getChannelIcon(channel)}
                  onClick={() => {
                    const channels = filter.channels.includes(channel)
                      ? filter.channels.filter(c => c !== channel)
                      : [...filter.channels, channel];
                    setFilter({ ...filter, channels });
                  }}
                  color={filter.channels.includes(channel) ? 'primary' : 'default'}
                  variant={filter.channels.includes(channel) ? 'filled' : 'outlined'}
                />
              ))}
            </Box>
          </Grid>
        </Grid>
        
        {/* Follow-up Filter */}
        <Box mt={2}>
          <Chip
            icon={<FlagIcon />}
            label="Nur Follow-ups"
            onClick={() => setFilter({ ...filter, showFollowUp: !filter.showFollowUp })}
            color={filter.showFollowUp ? 'warning' : 'default'}
            variant={filter.showFollowUp ? 'filled' : 'outlined'}
          />
        </Box>
      </Box>
      
      {/* Content based on view mode */}
      {viewMode === 'timeline' && (
        <Timeline position="alternate">
          {Array.from(groupedEvents.entries()).map(([group, events]) => (
            <React.Fragment key={group}>
              <TimelineItem>
                <TimelineContent>
                  <Typography variant="h6" color="primary">
                    {group}
                  </Typography>
                </TimelineContent>
              </TimelineItem>
              {events.map(renderTimelineItem)}
            </React.Fragment>
          ))}
        </Timeline>
      )}
      
      {viewMode === 'stats' && (
        <CommunicationStats events={filteredEvents} />
      )}
      
      {/* Quick Log Dialog */}
      <QuickLogDialog
        open={quickLogOpen}
        onClose={() => setQuickLogOpen(false)}
        customerId={customerId}
        contactId={contactId}
        onSave={() => {
          setQuickLogOpen(false);
          loadCommunicationHistory();
        }}
      />
      
      {/* Floating Action Button for Mobile */}
      <Fab
        color="primary"
        aria-label="add"
        sx={{
          position: 'fixed',
          bottom: 16,
          right: 16,
          display: { xs: 'flex', md: 'none' }
        }}
        onClick={() => setQuickLogOpen(true)}
      >
        <AddIcon />
      </Fab>
    </Box>
  );
};
```

### 3. Communication History Service

**Datei:** `backend/src/main/java/de/freshplan/communication/service/CommunicationHistoryService.java`

```java
// CLAUDE: Service für Communication History mit Analytics
// Pfad: backend/src/main/java/de/freshplan/communication/service/CommunicationHistoryService.java

package de.freshplan.communication.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class CommunicationHistoryService {
    
    @Inject
    CommunicationEventRepository repository;
    
    @Inject
    ContactInteractionService interactionService;
    
    @Inject
    RelationshipWarmthService warmthService;
    
    @Inject
    NotificationService notificationService;
    
    /**
     * Log a new communication event
     */
    public CommunicationEvent logCommunication(CommunicationDTO dto, UUID userId) {
        CommunicationEvent event = createEventByChannel(dto);
        
        event.customer = Customer.findById(dto.customerId);
        if (dto.contactId != null) {
            event.contact = CustomerContact.findById(dto.contactId);
        }
        
        event.user = User.findById(userId);
        event.occurredAt = dto.occurredAt != null ? dto.occurredAt : LocalDateTime.now();
        event.subject = dto.subject;
        event.summary = dto.summary;
        event.outcome = dto.outcome;
        event.sentiment = dto.sentiment;
        event.tags = new HashSet<>(dto.tags);
        event.durationMinutes = dto.durationMinutes;
        
        // Follow-up handling
        if (dto.followUpRequired) {
            event.followUpRequired = true;
            event.followUpDate = dto.followUpDate;
            event.followUpNotes = dto.followUpNotes;
            
            // Schedule reminder
            scheduleFollowUpReminder(event);
        }
        
        // Persist
        repository.persist(event);
        
        // Update related systems
        updateRelatedSystems(event);
        
        return event;
    }
    
    /**
     * Get communication history with filtering
     */
    public List<CommunicationEvent> getCommunicationHistory(
        UUID customerId,
        UUID contactId,
        CommunicationFilter filter
    ) {
        var query = repository.find("customer.id = ?1", customerId);
        
        if (contactId != null) {
            query = query.and("contact.id", contactId);
        }
        
        if (filter.channels != null && !filter.channels.isEmpty()) {
            query = query.and("type IN ?1", filter.channels);
        }
        
        if (filter.fromDate != null) {
            query = query.and("occurredAt >= ?1", filter.fromDate);
        }
        
        if (filter.toDate != null) {
            query = query.and("occurredAt <= ?1", filter.toDate);
        }
        
        if (filter.sentiment != null) {
            query = query.and("sentiment = ?1", filter.sentiment);
        }
        
        if (filter.followUpOnly) {
            query = query.and("followUpRequired = true");
        }
        
        return query
            .orderBy("occurredAt DESC")
            .page(filter.page, filter.size)
            .list();
    }
    
    /**
     * Communication Analytics
     */
    public CommunicationAnalytics getAnalytics(UUID customerId, LocalDateTime from, LocalDateTime to) {
        List<CommunicationEvent> events = repository.find(
            "customer.id = ?1 AND occurredAt BETWEEN ?2 AND ?3",
            customerId, from, to
        ).list();
        
        CommunicationAnalytics analytics = new CommunicationAnalytics();
        
        // Total communications
        analytics.totalCount = events.size();
        
        // By channel
        analytics.byChannel = events.stream()
            .collect(Collectors.groupingBy(
                CommunicationEvent::getChannel,
                Collectors.counting()
            ));
        
        // By sentiment
        analytics.bySentiment = events.stream()
            .filter(e -> e.sentiment != null)
            .collect(Collectors.groupingBy(
                e -> e.sentiment,
                Collectors.counting()
            ));
        
        // Average response time
        analytics.avgResponseTime = calculateAverageResponseTime(events);
        
        // Communication frequency
        analytics.frequency = calculateFrequency(events);
        
        // Top communicators
        analytics.topCommunicators = events.stream()
            .filter(e -> e.contact != null)
            .collect(Collectors.groupingBy(
                e -> e.contact,
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<CustomerContact, Long>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        
        // Sentiment trend
        analytics.sentimentTrend = calculateSentimentTrend(events);
        
        // Open follow-ups
        analytics.openFollowUps = repository.count(
            "customer.id = ?1 AND followUpRequired = true AND followUpDate >= ?2",
            customerId, LocalDateTime.now()
        );
        
        return analytics;
    }
    
    /**
     * Smart Communication Suggestions
     */
    public List<CommunicationSuggestion> generateSuggestions(UUID customerId) {
        List<CommunicationSuggestion> suggestions = new ArrayList<>();
        
        Customer customer = Customer.findById(customerId);
        List<CustomerContact> contacts = CustomerContact.find("customer", customer).list();
        
        for (CustomerContact contact : contacts) {
            // Check last communication
            Optional<CommunicationEvent> lastComm = repository.find(
                "contact = ?1 ORDER BY occurredAt DESC",
                contact
            ).firstResultOptional();
            
            if (lastComm.isEmpty()) {
                // Never contacted
                suggestions.add(new CommunicationSuggestion(
                    contact,
                    "Erste Kontaktaufnahme",
                    "Noch nie kontaktiert - Zeit für eine Vorstellung!",
                    CommunicationChannel.EMAIL,
                    SuggestionPriority.HIGH
                ));
            } else {
                LocalDateTime lastDate = lastComm.get().occurredAt;
                long daysSince = ChronoUnit.DAYS.between(lastDate, LocalDateTime.now());
                
                // Regular check-in suggestion
                if (daysSince > 30 && contact.isPrimary) {
                    suggestions.add(new CommunicationSuggestion(
                        contact,
                        "Regelmäßiger Check-in",
                        String.format("Letzter Kontakt vor %d Tagen", daysSince),
                        CommunicationChannel.PHONE,
                        SuggestionPriority.MEDIUM
                    ));
                }
                
                // Birthday reminder
                if (contact.birthday != null) {
                    LocalDate today = LocalDate.now();
                    LocalDate birthday = contact.birthday.withYear(today.getYear());
                    long daysUntilBirthday = ChronoUnit.DAYS.between(today, birthday);
                    
                    if (daysUntilBirthday >= 0 && daysUntilBirthday <= 7) {
                        suggestions.add(new CommunicationSuggestion(
                            contact,
                            "Geburtstag in " + daysUntilBirthday + " Tagen",
                            "Geburtstagsgrüße vorbereiten",
                            CommunicationChannel.EMAIL,
                            SuggestionPriority.HIGH
                        ));
                    }
                }
                
                // Follow-up on negative sentiment
                if (lastComm.get().sentiment == CommunicationSentiment.NEGATIVE ||
                    lastComm.get().sentiment == CommunicationSentiment.VERY_NEGATIVE) {
                    suggestions.add(new CommunicationSuggestion(
                        contact,
                        "Follow-up nach negativem Gespräch",
                        "Letztes Gespräch war negativ - Beziehung pflegen!",
                        CommunicationChannel.MEETING,
                        SuggestionPriority.CRITICAL
                    ));
                }
            }
        }
        
        // Sort by priority
        return suggestions.stream()
            .sorted(Comparator.comparing(CommunicationSuggestion::getPriority))
            .collect(Collectors.toList());
    }
    
    /**
     * Update related systems after communication
     */
    private void updateRelatedSystems(CommunicationEvent event) {
        // Update contact's last contact date
        if (event.contact != null) {
            event.contact.lastContactDate = event.occurredAt;
            event.contact.persist();
        }
        
        // Track interaction for warmth calculation
        interactionService.trackInteraction(
            event.customer.id,
            event.contact != null ? event.contact.id : null,
            "COMMUNICATION",
            Map.of(
                "channel", event.getChannel().name(),
                "sentiment", event.sentiment != null ? event.sentiment.name() : "NEUTRAL"
            )
        );
        
        // Update relationship warmth
        if (event.contact != null) {
            warmthService.recalculateWarmth(event.contact.id);
        }
        
        // Send notifications for critical events
        if (event.sentiment == CommunicationSentiment.VERY_NEGATIVE) {
            notificationService.notifyManager(
                "Sehr negatives Gespräch",
                String.format("Kritisches Gespräch mit %s - %s",
                    event.customer.name,
                    event.summary
                )
            );
        }
    }
    
    /**
     * Schedule follow-up reminder
     */
    private void scheduleFollowUpReminder(CommunicationEvent event) {
        // Create a scheduled task for follow-up
        schedulerService.schedule(
            event.followUpDate.minusDays(1), // Remind 1 day before
            () -> {
                notificationService.sendFollowUpReminder(
                    event.user.id,
                    event.customer,
                    event.contact,
                    event.followUpNotes
                );
            }
        );
    }
}
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Backend Foundation (45 Min)
- [ ] CommunicationEvent Entity + Inheritance
- [ ] Email/Phone/Meeting/Chat Subclasses
- [ ] Migration für communication_events Tabelle
- [ ] CommunicationHistoryService

### Phase 2: Frontend Timeline (60 Min)
- [ ] CommunicationTimeline Component
- [ ] Timeline mit Grouping
- [ ] Filter & Search
- [ ] Sentiment Indicators

### Phase 3: Quick Logging (45 Min)
- [ ] QuickLogDialog für schnelle Erfassung
- [ ] Voice-to-Text Support (optional)
- [ ] Template System
- [ ] Bulk Logging

### Phase 4: Analytics & Insights (30 Min)
- [ ] CommunicationStats Dashboard
- [ ] Frequency Analysis
- [ ] Sentiment Trends
- [ ] Top Communicators

### Phase 5: Integration (30 Min)
- [ ] Email Integration (Outlook/Gmail)
- [ ] Calendar Sync
- [ ] Phone System Integration
- [ ] Export Funktionen

## 🔗 INTEGRATION POINTS

1. **Contact Timeline** - Events anzeigen
2. **Relationship Warmth** - Kommunikation tracken
3. **Smart Suggestions** - Basierend auf Historie
4. **Dashboard** - Kommunikations-KPIs

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Zu viele Details erfassen**
   → Lösung: Quick Log mit Vorlagen

2. **Manuelle Eingabe vergessen**
   → Lösung: Automatische Erfassung wo möglich

3. **Historie wird unübersichtlich**
   → Lösung: Smart Filtering & Grouping

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 210 Minuten  
**Nächstes Dokument:** [→ Org-Chart Visualization](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/ORG_CHART_VISUALIZATION.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Keine Kommunikation = Keine Beziehung! 📞✨**