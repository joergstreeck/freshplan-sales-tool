# 📆 Tag 4: Timeline Implementation

**Datum:** Donnerstag, 22. August 2025  
**Fokus:** Event Timeline UI  
**Ziel:** Chronologische Darstellung aller Aktivitäten  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 3: Analytics](./DAY3_ANALYTICS.md)  
**↑ Woche 3 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 5: AI-Ready](./DAY5_AI_READY.md)  
**📘 Spec:** [Timeline Specification](./specs/TIMELINE_SPEC.md)  

## 🎯 Tagesziel

- Backend: Timeline Projection Service
- Frontend: Timeline Component mit Filter
- UI: Event Icons & Kategorien
- Performance: Pagination & Lazy Loading

## 📝 Timeline Konzept

```
Zeit
 │
 ├── 📧 E-Mail gesendet
 │   └── "Angebot Cook&Fresh"
 │
 ├── 📞 Anruf getätigt  
 │   └── "Positives Feedback"
 │
 ├── 📅 Meeting geplant
 │   └── "Vor-Ort Termin"
 │
 ├── 🗭 Bestellung eingegangen
 │   └── "Erste Testbestellung"
 ↓
```

## 💻 Backend Implementation

### 1. Timeline Projection Service

```java
// TimelineProjection.java
@ApplicationScoped
public class TimelineProjection {
    
    @Inject
    EntityManager em;
    
    @EventHandler
    @Transactional
    public void on(BaseEvent event) {
        // Create timeline entry for every event
        TimelineEntry entry = TimelineEntry.builder()
            .entryId(UUID.randomUUID())
            .contactId(event.getAggregateId())
            .timestamp(event.getCreatedAt())
            .eventType(mapToTimelineType(event))
            .title(generateTitle(event))
            .description(generateDescription(event))
            .icon(getIconForEvent(event))
            .userId(event.getMetadata().getUserId())
            .metadata(extractRelevantMetadata(event))
            .build();
        
        em.persist(entry);
    }
    
    private TimelineEventType mapToTimelineType(BaseEvent event) {
        return switch (event.getClass().getSimpleName()) {
            case "ContactCreatedEvent" -> TimelineEventType.CONTACT_CREATED;
            case "EmailSentEvent" -> TimelineEventType.EMAIL_SENT;
            case "CallLoggedEvent" -> TimelineEventType.CALL_LOGGED;
            case "MeetingScheduledEvent" -> TimelineEventType.MEETING_SCHEDULED;
            case "OrderPlacedEvent" -> TimelineEventType.ORDER_PLACED;
            case "RelationshipDataUpdatedEvent" -> TimelineEventType.INFO_UPDATED;
            default -> TimelineEventType.OTHER;
        };
    }
    
    public TimelineResponse getTimeline(UUID contactId, TimelineFilter filter) {
        String query = "SELECT t FROM TimelineEntry t WHERE t.contactId = :contactId";
        
        if (filter.getStartDate() != null) {
            query += " AND t.timestamp >= :startDate";
        }
        if (filter.getEventTypes() != null && !filter.getEventTypes().isEmpty()) {
            query += " AND t.eventType IN :types";
        }
        
        query += " ORDER BY t.timestamp DESC";
        
        TypedQuery<TimelineEntry> typedQuery = em.createQuery(query, TimelineEntry.class)
            .setParameter("contactId", contactId);
        
        if (filter.getStartDate() != null) {
            typedQuery.setParameter("startDate", filter.getStartDate());
        }
        if (filter.getEventTypes() != null && !filter.getEventTypes().isEmpty()) {
            typedQuery.setParameter("types", filter.getEventTypes());
        }
        
        List<TimelineEntry> entries = typedQuery
            .setFirstResult(filter.getOffset())
            .setMaxResults(filter.getLimit())
            .getResultList();
        
        return TimelineResponse.builder()
            .entries(entries)
            .hasMore(entries.size() == filter.getLimit())
            .build();
    }
}
```

**Vollständiger Code:** [backend/TimelineProjection.java](./code/backend/TimelineProjection.java)

### 2. Event Title & Description Generator

```java
private String generateTitle(BaseEvent event) {
    return switch (event) {
        case EmailSentEvent e -> "E-Mail: " + e.getSubject();
        case CallLoggedEvent c -> "Anruf: " + c.getDuration() + " Min";
        case MeetingScheduledEvent m -> "Meeting: " + m.getTitle();
        case OrderPlacedEvent o -> "Bestellung: " + formatCurrency(o.getTotal());
        case ContactCreatedEvent -> "Kontakt angelegt";
        default -> event.getEventType();
    };
}

private String generateDescription(BaseEvent event) {
    return switch (event) {
        case EmailSentEvent e -> "An: " + e.getRecipient() + " - " + e.getPreview();
        case CallLoggedEvent c -> c.getNotes() + " (Ergebnis: " + c.getOutcome() + ")";
        case MeetingScheduledEvent m -> m.getLocation() + " - " + m.getAttendees();
        default -> "";
    };
}
```

## 🎨 Frontend Implementation

### Timeline Component

```typescript
// components/ContactTimeline.tsx
export const ContactTimeline: React.FC<TimelineProps> = ({
  contactId,
  compact = false
}) => {
  const [filter, setFilter] = useState<TimelineFilter>({
    eventTypes: [],
    limit: 20
  });
  
  const { data: timeline, loading, fetchMore } = useTimeline(contactId, filter);
  
  const getEventIcon = (type: TimelineEventType): ReactNode => {
    const icons: Record<TimelineEventType, ReactNode> = {
      CONTACT_CREATED: <PersonAddIcon />,
      EMAIL_SENT: <EmailIcon />,
      CALL_LOGGED: <PhoneIcon />,
      MEETING_SCHEDULED: <EventIcon />,
      ORDER_PLACED: <ShoppingCartIcon />,
      INFO_UPDATED: <EditIcon />,
      NOTE_ADDED: <NoteIcon />
    };
    return icons[type] || <MoreHorizIcon />;
  };
  
  const getEventColor = (type: TimelineEventType): string => {
    const colors: Record<TimelineEventType, string> = {
      CONTACT_CREATED: 'primary',
      EMAIL_SENT: 'info',
      CALL_LOGGED: 'success',
      MEETING_SCHEDULED: 'warning',
      ORDER_PLACED: 'error',
      INFO_UPDATED: 'default',
      NOTE_ADDED: 'default'
    };
    return colors[type] || 'default';
  };
  
  return (
    <Box>
      {/* Filter Bar */}
      {!compact && (
        <Box sx={{ mb: 2, display: 'flex', gap: 2 }}>
          <FormControl size="small">
            <InputLabel>Event-Typ</InputLabel>
            <Select
              multiple
              value={filter.eventTypes}
              onChange={(e) => setFilter({...filter, eventTypes: e.target.value})}
              sx={{ minWidth: 200 }}
            >
              <MenuItem value="EMAIL_SENT">E-Mails</MenuItem>
              <MenuItem value="CALL_LOGGED">Anrufe</MenuItem>
              <MenuItem value="MEETING_SCHEDULED">Meetings</MenuItem>
              <MenuItem value="ORDER_PLACED">Bestellungen</MenuItem>
            </Select>
          </FormControl>
          
          <DateRangePicker
            startDate={filter.startDate}
            endDate={filter.endDate}
            onChange={(dates) => setFilter({...filter, ...dates})}
          />
        </Box>
      )}
      
      {/* Timeline */}
      <Timeline position={compact ? 'right' : 'alternate'}>
        {timeline?.entries.map((entry, index) => (
          <TimelineItem key={entry.id}>
            <TimelineOppositeContent
              sx={{ m: 'auto 0' }}
              align="right"
              variant="body2"
              color="text.secondary"
            >
              {format(new Date(entry.timestamp), 'dd.MM.yyyy HH:mm')}
            </TimelineOppositeContent>
            
            <TimelineSeparator>
              <TimelineConnector sx={{ bgcolor: 'grey.300' }} />
              <TimelineDot color={getEventColor(entry.eventType)}>
                {getEventIcon(entry.eventType)}
              </TimelineDot>
              <TimelineConnector sx={{ bgcolor: 'grey.300' }} />
            </TimelineSeparator>
            
            <TimelineContent sx={{ py: '12px', px: 2 }}>
              <Typography variant="h6" component="span">
                {entry.title}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {entry.description}
              </Typography>
              {entry.metadata?.outcome && (
                <Chip 
                  label={entry.metadata.outcome} 
                  size="small" 
                  sx={{ mt: 1 }}
                  color={entry.metadata.outcome === 'positive' ? 'success' : 'default'}
                />
              )}
            </TimelineContent>
          </TimelineItem>
        ))}
      </Timeline>
      
      {/* Load More */}
      {timeline?.hasMore && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Button 
            onClick={() => fetchMore()} 
            disabled={loading}
          >
            {loading ? <CircularProgress size={20} /> : 'Mehr laden'}
          </Button>
        </Box>
      )}
    </Box>
  );
};
```

**Vollständiger Code:** [frontend/ContactTimeline.tsx](./code/frontend/ContactTimeline.tsx)

### Compact Timeline Variant

```typescript
// components/CompactTimeline.tsx
export const CompactTimeline: React.FC<{contactId: string}> = ({ contactId }) => {
  const { data: timeline } = useTimeline(contactId, { limit: 5 });
  
  return (
    <List dense>
      {timeline?.entries.map(entry => (
        <ListItem key={entry.id}>
          <ListItemIcon>
            {getEventIcon(entry.eventType)}
          </ListItemIcon>
          <ListItemText
            primary={entry.title}
            secondary={formatRelativeTime(entry.timestamp)}
          />
        </ListItem>
      ))}
      <ListItem button component={Link} to={`/contacts/${contactId}/timeline`}>
        <ListItemText primary="Vollständige Timeline anzeigen..." />
      </ListItem>
    </List>
  );
};
```

## 📊 Timeline Filter Hook

```typescript
// hooks/useTimelineFilter.ts
export const useTimelineFilter = () => {
  const [filter, setFilter] = useState<TimelineFilter>({
    eventTypes: [],
    dateRange: 'all',
    limit: 20
  });
  
  const presets = {
    today: () => setFilter(f => ({...f, startDate: startOfDay(new Date())})),
    thisWeek: () => setFilter(f => ({...f, startDate: startOfWeek(new Date())})),
    thisMonth: () => setFilter(f => ({...f, startDate: startOfMonth(new Date())})),
    communications: () => setFilter(f => ({...f, eventTypes: ['EMAIL_SENT', 'CALL_LOGGED']}))
  };
  
  return { filter, setFilter, presets };
};
```

## 🧪 Tests

### Timeline Integration Test

```typescript
describe('Contact Timeline', () => {
  it('should display events in chronological order', async () => {
    const contact = await createTestContactWithHistory();
    
    const { container } = render(<ContactTimeline contactId={contact.id} />);
    
    // Verify order
    const timestamps = container.querySelectorAll('[data-testid="timeline-timestamp"]');
    const times = Array.from(timestamps).map(el => new Date(el.textContent));
    
    for (let i = 1; i < times.length; i++) {
      expect(times[i-1].getTime()).toBeGreaterThanOrEqual(times[i].getTime());
    }
  });
  
  it('should filter by event type', async () => {
    const contact = await createTestContactWithHistory();
    
    const { getByLabelText, queryByText } = render(
      <ContactTimeline contactId={contact.id} />
    );
    
    // Filter to only emails
    fireEvent.click(getByLabelText('Event-Typ'));
    fireEvent.click(getByText('E-Mails'));
    
    // Should only show email events
    await waitFor(() => {
      expect(queryByText(/Anruf:/)).not.toBeInTheDocument();
      expect(queryByText(/E-Mail:/)).toBeInTheDocument();
    });
  });
});
```

## 📝 Checkliste

- [ ] Timeline Projection implementiert
- [ ] Event Mapping definiert
- [ ] Timeline Component erstellt
- [ ] Filter Funktionalität
- [ ] Icon & Color Mapping
- [ ] Pagination implementiert
- [ ] Tests geschrieben

## 🔗 Weiterführende Links

- **Timeline Patterns:** [Event Timeline Best Practices](./guides/TIMELINE_PATTERNS.md)
- **Performance:** [Timeline Optimization](./guides/TIMELINE_OPTIMIZATION.md)
- **Nächster Schritt:** [→ Tag 5: KI-Ready Structure](./DAY5_AI_READY.md)

---

**Status:** 📋 Bereit zur Implementierung