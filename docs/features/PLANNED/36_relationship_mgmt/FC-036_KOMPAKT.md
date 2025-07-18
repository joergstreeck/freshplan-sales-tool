# 🎁 FC-036 BEZIEHUNGSMANAGEMENT (ARCHIVIERT)

**HINWEIS:** Dieses Dokument wurde aufgeteilt in:
- [FC-036_OVERVIEW.md](./FC-036_OVERVIEW.md) - Übersicht & Business Value
- [FC-036_IMPLEMENTATION.md](./FC-036_IMPLEMENTATION.md) - Frontend Components
- [FC-036_API.md](./FC-036_API.md) - Datenmodell & Privacy
- [FC-036_TESTING.md](./FC-036_TESTING.md) - Test-Strategie

---

# 🎁 FC-036 BEZIEHUNGSMANAGEMENT (KOMPAKT)

**Erstellt:** 19.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🎨 FRONTEND  
**Priorität:** MEDIUM - Der persönliche Touch  
**Geschätzt:** 1 Tag  

---

## 🧠 WAS WIR BAUEN

**Problem:** Geschäft ist Beziehung, nicht nur Transaktion  
**Lösung:** Persönliche Details tracken & nutzen  
**Value:** Aus Kunden werden Partner  

> **Business Case:** "Sie erinnern sich an meine Tochter?" → Vertrauen

### 🎯 Beziehungs-Features:
- **Persönliche Events:** Geburtstage, Jubiläen
- **Familie & Hobbies:** Kinder, Sport, Interessen
- **Geschenk-Historie:** Was kam gut an?
- **Gesprächs-Notizen:** Worüber haben wir geredet?

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Personal Details Card:**
```typescript
const PersonalDetailsCard = ({ contact }) => {
  const details = usePersonalDetails(contact.id);
  
  return (
    <Card>
      <CardHeader
        avatar={<FavoriteIcon color="secondary" />}
        title="Persönliche Details"
        subheader="Nur für Sie sichtbar"
      />
      <CardContent>
        {/* Familie */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="subtitle2" gutterBottom>
            👨‍👩‍👧‍👦 Familie
          </Typography>
          <Chip label="Verheiratet" size="small" />
          <Chip label="2 Kinder (Lisa 8, Tom 5)" size="small" sx={{ ml: 1 }} />
          <Chip label="Hund (Bello)" size="small" sx={{ ml: 1 }} />
        </Box>
        
        {/* Hobbies */}
        <Box sx={{ mb: 2 }}>
          <Typography variant="subtitle2" gutterBottom>
            ⚽ Interessen
          </Typography>
          <Chip label="Tennis" icon={<SportsIcon />} size="small" />
          <Chip label="Kochen" icon={<RestaurantIcon />} size="small" sx={{ ml: 1 }} />
          <Chip label="Reisen" icon={<FlightIcon />} size="small" sx={{ ml: 1 }} />
        </Box>
        
        {/* Wichtige Daten */}
        <Box>
          <Typography variant="subtitle2" gutterBottom>
            📅 Wichtige Daten
          </Typography>
          <List dense>
            <ListItem>
              <ListItemIcon><CakeIcon /></ListItemIcon>
              <ListItemText 
                primary="Geburtstag"
                secondary="15. März (in 23 Tagen) 🎂"
              />
            </ListItem>
            <ListItem>
              <ListItemIcon><BusinessIcon /></ListItemIcon>
              <ListItemText 
                primary="Firmenjubiläum"
                secondary="10 Jahre am 1. Juli"
              />
            </ListItem>
          </List>
        </Box>
      </CardContent>
    </Card>
  );
};
```

### 2. **Event Reminder System:**
```typescript
const EventReminders = () => {
  const upcomingEvents = useUpcomingPersonalEvents();
  
  return (
    <Timeline>
      {upcomingEvents.map(event => (
        <TimelineItem key={event.id}>
          <TimelineSeparator>
            <TimelineDot color={getDotColor(event.daysUntil)}>
              {getEventIcon(event.type)}
            </TimelineDot>
          </TimelineSeparator>
          <TimelineContent>
            <Alert 
              severity={event.daysUntil < 7 ? 'warning' : 'info'}
              action={
                <Button size="small" onClick={() => prepareFor(event)}>
                  Vorbereiten
                </Button>
              }
            >
              <AlertTitle>
                {event.contact.name} - {event.type}
              </AlertTitle>
              In {event.daysUntil} Tagen • {event.suggestion}
            </Alert>
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
};

// Smart Suggestions
const eventSuggestions = {
  BIRTHDAY: 'Kleine Aufmerksamkeit vorbereiten',
  ANNIVERSARY: 'Gratulation per LinkedIn',
  CHILD_BIRTHDAY: 'Nach Kind fragen im nächsten Gespräch',
  VACATION_RETURN: 'Nach Urlaub erkundigen'
};
```

### 3. **Conversation History:**
```typescript
const ConversationNotes = ({ customerId }) => {
  const [notes, setNotes] = useState('');
  const history = useConversationHistory(customerId);
  
  return (
    <Box>
      {/* Quick Add */}
      <TextField
        fullWidth
        multiline
        rows={2}
        placeholder="Worüber haben Sie gesprochen? (z.B. Tochter hat Abi bestanden)"
        value={notes}
        onChange={(e) => setNotes(e.target.value)}
        onKeyPress={(e) => {
          if (e.key === 'Enter' && e.ctrlKey) {
            saveConversationNote(customerId, notes);
            setNotes('');
          }
        }}
      />
      
      {/* History */}
      <List sx={{ mt: 2 }}>
        {history.map(note => (
          <ListItem key={note.id}>
            <ListItemText
              primary={note.content}
              secondary={
                <>
                  {formatDate(note.date)} • {note.context}
                  {note.followUp && (
                    <Chip
                      label={`Follow-up: ${note.followUp}`}
                      size="small"
                      color="primary"
                      sx={{ ml: 1 }}
                    />
                  )}
                </>
              }
            />
          </ListItem>
        ))}
      </List>
    </Box>
  );
};
```

---

## 💝 GIFT & GESTURE TRACKING

### Gift History:
```typescript
const GiftTracker = ({ contact }) => {
  const history = useGiftHistory(contact.id);
  
  return (
    <Table size="small">
      <TableHead>
        <TableRow>
          <TableCell>Datum</TableCell>
          <TableCell>Anlass</TableCell>
          <TableCell>Geschenk</TableCell>
          <TableCell>Reaktion</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {history.map(gift => (
          <TableRow key={gift.id}>
            <TableCell>{formatDate(gift.date)}</TableCell>
            <TableCell>{gift.occasion}</TableCell>
            <TableCell>{gift.description}</TableCell>
            <TableCell>
              <Rating value={gift.reaction} size="small" readOnly />
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
};

// Smart Gift Suggestions
const suggestGift = (contact: Contact) => {
  const suggestions = [];
  
  // Based on interests
  if (contact.interests.includes('wine')) {
    suggestions.push({
      item: 'Weinflasche',
      reason: 'Weinliebhaber',
      budget: '30-50€'
    });
  }
  
  // Based on family
  if (contact.hasKids) {
    suggestions.push({
      item: 'Familien-Gutschein',
      reason: 'Hat Kinder',
      budget: '50-100€'
    });
  }
  
  return suggestions;
};
```

---

## 🧠 RELATIONSHIP INTELLIGENCE

### Relationship Score:
```typescript
const RelationshipScore = ({ customerId }) => {
  const score = useRelationshipScore(customerId);
  
  return (
    <Box sx={{ textAlign: 'center', p: 2 }}>
      <CircularProgress
        variant="determinate"
        value={score.value}
        size={80}
        thickness={4}
        sx={{
          color: getScoreColor(score.value),
          '& .MuiCircularProgress-circle': {
            strokeLinecap: 'round',
          },
        }}
      />
      <Typography variant="h6" sx={{ mt: 1 }}>
        {score.value}% Beziehungsstärke
      </Typography>
      <Typography variant="caption" color="text.secondary">
        {score.trend > 0 ? '📈' : '📉'} {Math.abs(score.trend)}% vs. letzten Monat
      </Typography>
      
      {/* Improvement Tips */}
      <List dense sx={{ mt: 2 }}>
        {score.improvements.map((tip, i) => (
          <ListItem key={i}>
            <ListItemIcon><TipsIcon /></ListItemIcon>
            <ListItemText primary={tip} />
          </ListItem>
        ))}
      </List>
    </Box>
  );
};

// Calculate Score
const calculateRelationshipScore = (customer: Customer) => {
  const factors = {
    personalDetailsKnown: countPersonalDetails(customer) * 5,
    recentInteractions: getInteractionScore(customer),
    eventEngagement: getEventEngagementScore(customer),
    responseTime: getResponseScore(customer),
    giftHistory: customer.gifts.length * 10
  };
  
  return Math.min(100, Object.values(factors).reduce((a, b) => a + b, 0));
};
```

---

## 🔔 SMART REMINDERS

### Auto-Reminders:
```typescript
const relationshipReminders = {
  // Check-in Reminder
  noContactReminder: (customer) => {
    if (daysSinceLastContact(customer) > 60) {
      createReminder({
        type: 'CHECK_IN',
        message: `Lange nichts von ${customer.name} gehört`,
        suggestion: 'Kurzer Anruf oder WhatsApp'
      });
    }
  },
  
  // Special Occasions
  birthdayReminder: (contact) => {
    const daysUntil = daysUntilBirthday(contact);
    if (daysUntil === 7) {
      createReminder({
        type: 'BIRTHDAY_PREP',
        message: `${contact.name} hat in einer Woche Geburtstag`,
        suggestion: 'Karte besorgen oder Kalendereintrag'
      });
    }
  },
  
  // Follow-up on Personal Events
  personalFollowUp: (note) => {
    if (note.content.includes('Krankenhaus') || note.content.includes('Operation')) {
      scheduleReminder({
        delay: '2 weeks',
        message: 'Nach Gesundheit erkundigen',
        context: note
      });
    }
  }
};
```

---

## 🔧 DATA PRIVACY

### Sensitive Data Handling:
```typescript
// Verschlüsselte Speicherung
const encryptPersonalData = (data: PersonalDetails) => {
  return {
    ...data,
    familyDetails: encrypt(data.familyDetails, userKey),
    personalNotes: encrypt(data.personalNotes, userKey)
  };
};

// Zugriffskontrolle
@Entity
@Table(name = "personal_details")
public class PersonalDetails {
  @Id
  private UUID id;
  
  @ManyToOne
  private Contact contact;
  
  @ManyToOne
  private User owner; // Nur Owner kann sehen
  
  @Column(columnDefinition = "TEXT")
  @Encrypted // Custom annotation
  private String details;
  
  @Column
  private boolean sharedWithTeam = false;
}
```

---

## 🎯 SUCCESS METRICS

- **Datenqualität:** 70% haben persönliche Details
- **Event-Engagement:** 90% Geburtstage gratuliert
- **Beziehungs-Score:** Avg. +15% nach 6 Monaten
- **Kundenbindung:** +25% durch persönliche Touch

**Integration:** Tag 18-20 mit Timeline für volle Historie!