# 📆 Tag 2: Beziehungsebene Deep Features

**Datum:** Dienstag, 20. August 2025  
**Fokus:** Erweiterte Beziehungsdaten  
**Ziel:** Persönliche Details für bessere Kundenbeziehungen  

## 🧭 Navigation

**← Vorheriger Tag:** [Tag 1: Warmth](./DAY1_WARMTH.md)  
**↑ Woche 3 Übersicht:** [README.md](./README.md)  
**→ Nächster Tag:** [Tag 3: Analytics](./DAY3_ANALYTICS.md)  
**📘 Spec:** [Relationship Specification](./specs/RELATIONSHIP_SPEC.md)  

## 🎯 Tagesziel

- Backend: Relationship Data Service erweitern
- Frontend: Enhanced Relationship Form
- Features: Conversation Starters, Birthday Reminders
- UI: Hobby Icons, Family Info

## 👥 Beziehungsebene Konzept

```
Persönliche Daten:
├── 🎂 Geburtstag → Automatische Erinnerungen
├── 🏌️ Hobbys → Gesprächsthemen
├── 👨‍👩‍👧 Familie → Persönliche Bindung
└── 📝 Notizen → Individuelle Präferenzen
```

## 💻 Backend Implementation

### 1. Enhanced Relationship Service

```java
// RelationshipDataService.java
@ApplicationScoped
public class RelationshipDataService {
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    EventBus eventBus;
    
    // Smart hobby matching for conversation starters
    public List<ConversationStarter> generateConversationStarters(UUID contactId) {
        Contact contact = contactRepository.findById(contactId);
        RelationshipData data = contact.getRelationshipData();
        
        List<ConversationStarter> starters = new ArrayList<>();
        
        // Hobby-based starters
        if (data.getHobbies().contains("golf")) {
            starters.add(ConversationStarter.builder()
                .topic("Golf")
                .opener("Waren Sie in letzter Zeit auf dem Golfplatz?")
                .followUp("Haben Sie von dem neuen Golfplatz in " + contact.getCity() + " gehört?")
                .build());
        }
        
        // Family-based starters
        if (data.getChildren() > 0) {
            starters.add(ConversationStarter.builder()
                .topic("Familie")
                .opener("Wie geht es Ihrer Familie?")
                .followUp(getSeasonalFamilyTopic())
                .build());
        }
        
        // Business-relevant starters
        starters.addAll(generateBusinessStarters(contact));
        
        return starters;
    }
    
    // Intelligent birthday reminder system
    @Scheduled(every = "24h")
    void checkUpcomingBirthdays() {
        LocalDate today = LocalDate.now();
        LocalDate weekFromNow = today.plusDays(7);
        
        List<Contact> upcomingBirthdays = contactRepository
            .findByBirthdayBetween(today, weekFromNow);
        
        for (Contact contact : upcomingBirthdays) {
            int daysUntil = Period.between(today, contact.getBirthday()).getDays();
            
            BirthdayReminderEvent event = BirthdayReminderEvent.builder()
                .contactId(contact.getId())
                .contactName(contact.getFullName())
                .birthday(contact.getBirthday())
                .daysUntil(daysUntil)
                .age(Period.between(contact.getBirthday(), today).getYears())
                .build();
            
            eventBus.publish(event);
        }
    }
}
```

**Vollständiger Code:** [backend/RelationshipDataService.java](./code/backend/RelationshipDataService.java)

### 2. Seasonal Topics Generator

```java
private String getSeasonalFamilyTopic() {
    Month currentMonth = LocalDate.now().getMonth();
    
    return switch (currentMonth) {
        case DECEMBER -> "Haben Sie schon Pläne für die Feiertage?";
        case JULY, AUGUST -> "Wohin geht es in den Sommerurlaub?";
        case SEPTEMBER -> "Wie war der Schulstart für die Kinder?";
        case MARCH, APRIL -> "Freuen Sie sich auch schon auf Ostern?";
        default -> "Gibt es Neuigkeiten aus der Familie?";
    };
}
```

## 🎨 Frontend Implementation

### Enhanced Relationship Form

```typescript
// components/RelationshipDataForm.tsx
export const RelationshipDataForm: React.FC<RelationshipDataFormProps> = ({
  contact,
  onSave
}) => {
  const [data, setData] = useState<RelationshipData>(
    contact.relationshipData || getEmptyRelationshipData()
  );
  
  const hobbyOptions = [
    { value: 'golf', label: 'Golf', icon: '⛳' },
    { value: 'tennis', label: 'Tennis', icon: '🎾' },
    { value: 'sailing', label: 'Segeln', icon: '⛵' },
    { value: 'wine', label: 'Wein', icon: '🍷' },
    { value: 'cooking', label: 'Kochen', icon: '👨‍🍳' },
    { value: 'travel', label: 'Reisen', icon: '✈️' },
    { value: 'art', label: 'Kunst', icon: '🎨' },
    { value: 'music', label: 'Musik', icon: '🎵' }
  ];
  
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        Beziehungsebene pflegen
      </Typography>
      
      <Grid container spacing={3}>
        {/* Geburtstag mit Erinnerung */}
        <Grid item xs={12} md={6}>
          <DatePicker
            label="Geburtstag"
            value={data.birthday}
            onChange={(date) => setData({...data, birthday: date})}
            renderInput={(params) => (
              <TextField 
                {...params} 
                fullWidth
                helperText="Automatische Erinnerung 7 Tage vorher"
              />
            )}
          />
        </Grid>
        
        {/* Hobbys mit Icons */}
        <Grid item xs={12}>
          <Autocomplete
            multiple
            options={hobbyOptions}
            value={data.hobbies || []}
            onChange={(_, newValue) => setData({...data, hobbies: newValue})}
            renderOption={(props, option) => (
              <Box component="li" {...props}>
                <span style={{ marginRight: 8 }}>{option.icon}</span>
                {option.label}
              </Box>
            )}
            renderInput={(params) => (
              <TextField
                {...params}
                label="Hobbys & Interessen"
                placeholder="Für Gesprächsthemen"
              />
            )}
          />
        </Grid>
        
        {/* Familienstand */}
        <Grid item xs={12} md={6}>
          <FormControl fullWidth>
            <InputLabel>Familienstand</InputLabel>
            <Select
              value={data.maritalStatus || ''}
              onChange={(e) => setData({...data, maritalStatus: e.target.value})}
            >
              <MenuItem value="single">Ledig</MenuItem>
              <MenuItem value="married">Verheiratet</MenuItem>
              <MenuItem value="divorced">Geschieden</MenuItem>
              <MenuItem value="widowed">Verwitwet</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        
        {/* Kinder */}
        <Grid item xs={12} md={6}>
          <TextField
            type="number"
            label="Anzahl Kinder"
            value={data.children || 0}
            onChange={(e) => setData({...data, children: parseInt(e.target.value)})}
            InputProps={{ inputProps: { min: 0, max: 10 } }}
            fullWidth
          />
        </Grid>
        
        {/* Persönliche Notizen mit KI-Vorschlägen */}
        <Grid item xs={12}>
          <TextField
            multiline
            rows={4}
            label="Persönliche Notizen"
            value={data.personalNotes || ''}
            onChange={(e) => setData({...data, personalNotes: e.target.value})}
            placeholder="z.B. Mag keinen Smalltalk, bevorzugt direkte Kommunikation..."
            fullWidth
            helperText="Diese Notizen helfen bei der Vorbereitung von Gesprächen"
          />
        </Grid>
      </Grid>
    </Box>
  );
};
```

**Vollständiger Code:** [frontend/RelationshipDataForm.tsx](./code/frontend/RelationshipDataForm.tsx)

### Conversation Starter Widget

```typescript
// components/ConversationStarterWidget.tsx
export const ConversationStarterWidget: React.FC<{contactId: string}> = ({ contactId }) => {
  const { starters, loading } = useConversationStarters(contactId);
  
  if (loading || !starters?.length) return null;
  
  return (
    <Card sx={{ mb: 2 }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          💬 Gesprächsthemen
        </Typography>
        
        <List dense>
          {starters.map((starter, idx) => (
            <ListItem key={idx}>
              <ListItemIcon>
                {getTopicIcon(starter.topic)}
              </ListItemIcon>
              <ListItemText
                primary={starter.opener}
                secondary={starter.followUp}
              />
              <ListItemSecondaryAction>
                <IconButton size="small" onClick={() => copyToClipboard(starter.opener)}>
                  <ContentCopyIcon />
                </IconButton>
              </ListItemSecondaryAction>
            </ListItem>
          ))}
        </List>
      </CardContent>
    </Card>
  );
};
```

## 🎂 Birthday Reminder UI

```typescript
// components/BirthdayReminder.tsx
export const BirthdayReminder: React.FC = () => {
  const { reminders } = useBirthdayReminders();
  
  if (!reminders?.length) return null;
  
  return (
    <Alert severity="info" sx={{ mb: 2 }}>
      <AlertTitle>🎂 Anstehende Geburtstage</AlertTitle>
      {reminders.map(reminder => (
        <Box key={reminder.contactId} sx={{ mt: 1 }}>
          <Typography variant="body2">
            <strong>{reminder.contactName}</strong> wird {reminder.age}
          </Typography>
          <Typography variant="caption">
            In {reminder.daysUntil} {reminder.daysUntil === 1 ? 'Tag' : 'Tagen'}
            {' • '}
            <Link onClick={() => prepareGreeting(reminder)}>
              Glückwunsch vorbereiten
            </Link>
          </Typography>
        </Box>
      ))}
    </Alert>
  );
};
```

## 🧪 Tests

### Relationship Features Test

```typescript
describe('Relationship Features', () => {
  it('should generate conversation starters based on hobbies', async () => {
    const contact = await createTestContact({
      relationshipData: {
        hobbies: ['golf', 'wine']
      }
    });
    
    const starters = await relationshipApi.getConversationStarters(contact.id);
    
    expect(starters).toContainEqual(
      expect.objectContaining({
        topic: 'Golf',
        opener: expect.stringContaining('Golfplatz')
      })
    );
  });
  
  it('should show birthday reminders', async () => {
    const contact = await createTestContact({
      relationshipData: {
        birthday: addDays(new Date(), 5)
      }
    });
    
    const { getByText } = render(<BirthdayReminder />);
    
    await waitFor(() => {
      expect(getByText(/In 5 Tagen/)).toBeInTheDocument();
    });
  });
});
```

## 📝 Checkliste

- [ ] RelationshipDataService erweitert
- [ ] Conversation Starters implementiert
- [ ] Birthday Reminder System
- [ ] Enhanced Form mit Icons
- [ ] Seasonal Topics generiert
- [ ] Family Info Felder
- [ ] Tests geschrieben

## 🔗 Weiterführende Links

- **Hobby Mapping:** [Hobby to Business Guide](./guides/HOBBY_BUSINESS_MAPPING.md)
- **Birthday Best Practices:** [Birthday Marketing](./guides/BIRTHDAY_MARKETING.md)
- **Nächster Schritt:** [→ Tag 3: Analytics Events](./DAY3_ANALYTICS.md)

---

**Status:** 📋 Bereit zur Implementierung