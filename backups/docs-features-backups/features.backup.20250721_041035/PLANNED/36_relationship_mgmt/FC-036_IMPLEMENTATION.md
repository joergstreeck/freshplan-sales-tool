# 📝 FC-036 IMPLEMENTATION GUIDE

**Companion zu:** FC-036_OVERVIEW.md  
**Zweck:** Vollständige Frontend-Implementierung  

---

## 🎨 CORE COMPONENTS

### 1. Personal Details Card
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
          <Chip label="HSV Fan" size="small" color="primary" />
          <Chip label="Golf" size="small" sx={{ ml: 1 }} />
          <Chip label="Kochen" size="small" sx={{ ml: 1 }} />
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
                secondary="15. März (in 2 Wochen!)" 
              />
            </ListItem>
          </List>
        </Box>
      </CardContent>
      <CardActions>
        <Button size="small" onClick={() => editDetails(contact)}>
          Bearbeiten
        </Button>
        <Button size="small" onClick={() => addReminder(contact)}>
          Erinnerung
        </Button>
      </CardActions>
    </Card>
  );
};
```

### 2. Gift History Tracker
```typescript
const GiftHistory = ({ contact }) => {
  const [gifts, setGifts] = useState(contact.giftHistory || []);
  
  return (
    <Timeline>
      {gifts.map((gift, index) => (
        <TimelineItem key={gift.id}>
          <TimelineOppositeContent>
            <Typography variant="body2" color="textSecondary">
              {formatDate(gift.date)}
            </Typography>
          </TimelineOppositeContent>
          <TimelineSeparator>
            <TimelineDot color={gift.success ? 'success' : 'grey'}>
              <CardGiftcardIcon />
            </TimelineDot>
            {index < gifts.length - 1 && <TimelineConnector />}
          </TimelineSeparator>
          <TimelineContent>
            <Paper elevation={3} sx={{ p: 2 }}>
              <Typography variant="h6">{gift.item}</Typography>
              <Typography>{gift.occasion}</Typography>
              {gift.reaction && (
                <Alert severity={gift.success ? 'success' : 'info'} sx={{ mt: 1 }}>
                  Reaktion: {gift.reaction}
                </Alert>
              )}
            </Paper>
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
};
```

### 3. Relationship Score Visualization
```typescript
const RelationshipScore = ({ contact }) => {
  const score = calculateRelationshipScore(contact);
  
  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="h6" gutterBottom>
        Beziehungsstärke
      </Typography>
      
      <Box sx={{ position: 'relative', display: 'inline-flex' }}>
        <CircularProgress
          variant="determinate"
          value={score.percentage}
          size={120}
          thickness={4}
          sx={{
            color: score.percentage > 70 ? 'success.main' : 
                   score.percentage > 40 ? 'warning.main' : 'error.main'
          }}
        />
        <Box sx={{
          top: 0,
          left: 0,
          bottom: 0,
          right: 0,
          position: 'absolute',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}>
          <Typography variant="h4" component="div" color="text.secondary">
            {score.level}
          </Typography>
        </Box>
      </Box>
      
      <List dense sx={{ mt: 2 }}>
        <ListItem>
          <ListItemIcon><AccessTimeIcon /></ListItemIcon>
          <ListItemText 
            primary="Letzter Kontakt" 
            secondary={`vor ${score.daysSinceContact} Tagen`}
          />
        </ListItem>
        <ListItem>
          <ListItemIcon><EmojiEventsIcon /></ListItemIcon>
          <ListItemText 
            primary="Gemeinsame Events" 
            secondary={`${score.sharedEvents} Veranstaltungen`}
          />
        </ListItem>
      </List>
    </Box>
  );
};
```

### 4. Smart Reminder System
```typescript
const SmartReminders = ({ contact }) => {
  const reminders = useSmartReminders(contact);
  
  return (
    <List>
      {reminders.map(reminder => (
        <ListItem key={reminder.id}>
          <ListItemAvatar>
            <Avatar sx={{ bgcolor: reminder.color }}>
              {reminder.icon}
            </Avatar>
          </ListItemAvatar>
          <ListItemText
            primary={reminder.title}
            secondary={
              <>
                <Typography component="span" variant="body2">
                  {reminder.suggestion}
                </Typography>
                <br />
                <Typography component="span" variant="caption" color="text.secondary">
                  {reminder.timing}
                </Typography>
              </>
            }
          />
          <ListItemSecondaryAction>
            <IconButton onClick={() => scheduleAction(reminder)}>
              <AlarmAddIcon />
            </IconButton>
          </ListItemSecondaryAction>
        </ListItem>
      ))}
    </List>
  );
};

// Reminder Logic
const generateReminders = (contact: Contact) => {
  const reminders = [];
  
  // Birthday reminder
  if (contact.birthday) {
    const daysUntil = getDaysUntilBirthday(contact.birthday);
    if (daysUntil <= 14 && daysUntil > 0) {
      reminders.push({
        type: 'birthday',
        icon: <CakeIcon />,
        color: 'secondary.main',
        title: `Geburtstag in ${daysUntil} Tagen`,
        suggestion: 'Persönliche Karte vorbereiten',
        timing: formatDate(contact.birthday)
      });
    }
  }
  
  // Follow-up reminder
  if (contact.lastMeeting) {
    const daysSince = getDaysSince(contact.lastMeeting);
    if (daysSince > 60) {
      reminders.push({
        type: 'followup',
        icon: <PhoneIcon />,
        color: 'warning.main',
        title: 'Zeit für einen Anruf',
        suggestion: `Letztes Treffen vor ${daysSince} Tagen`,
        timing: 'Diese Woche'
      });
    }
  }
  
  return reminders;
};
```

---

## 🔧 CUSTOM HOOKS

### usePersonalDetails Hook
```typescript
const usePersonalDetails = (contactId: string) => {
  const [details, setDetails] = useState<PersonalDetails | null>(null);
  
  useEffect(() => {
    // Load from IndexedDB for privacy
    db.personalDetails
      .where('contactId')
      .equals(contactId)
      .first()
      .then(setDetails);
  }, [contactId]);
  
  const updateDetails = useCallback(async (updates: Partial<PersonalDetails>) => {
    const updated = { ...details, ...updates };
    await db.personalDetails.put(updated);
    setDetails(updated);
  }, [details]);
  
  return { details, updateDetails };
};
```

### Conversation Notes Component
```typescript
const ConversationNotes = ({ contact }) => {
  const [notes, setNotes] = useState('');
  const [savedNotes, setSavedNotes] = useState<Note[]>([]);
  
  return (
    <Box>
      <TextField
        fullWidth
        multiline
        rows={3}
        placeholder="Notizen zum letzten Gespräch..."
        value={notes}
        onChange={(e) => setNotes(e.target.value)}
        InputProps={{
          endAdornment: (
            <InputAdornment position="end">
              <IconButton onClick={saveNote}>
                <SaveIcon />
              </IconButton>
            </InputAdornment>
          )
        }}
      />
      
      <List sx={{ mt: 2 }}>
        {savedNotes.map(note => (
          <ListItem key={note.id}>
            <ListItemText
              primary={note.content}
              secondary={formatDate(note.date)}
            />
            {note.tags && (
              <Box>
                {note.tags.map(tag => (
                  <Chip key={tag} label={tag} size="small" sx={{ mr: 0.5 }} />
                ))}
              </Box>
            )}
          </ListItem>
        ))}
      </List>
    </Box>
  );
};
```

---

## 🎯 INTEGRATION POINTS

### 1. Customer Detail Page
```typescript
// Enhanced customer detail with relationship tab
<Tabs value={tabValue} onChange={handleTabChange}>
  <Tab label="Stammdaten" />
  <Tab label="Kontakte" />
  <Tab label="Beziehung" icon={<FavoriteIcon />} /> {/* NEU */}
</Tabs>

<TabPanel value={tabValue} index={2}>
  <Grid container spacing={3}>
    <Grid item xs={12} md={6}>
      <PersonalDetailsCard contact={contact} />
    </Grid>
    <Grid item xs={12} md={6}>
      <RelationshipScore contact={contact} />
    </Grid>
    <Grid item xs={12}>
      <GiftHistory contact={contact} />
    </Grid>
  </Grid>
</TabPanel>
```

### 2. Dashboard Integration
```typescript
// Relationship reminders on dashboard
<Card>
  <CardHeader title="Beziehungspflege" />
  <CardContent>
    <SmartReminders contacts={upcomingReminders} />
  </CardContent>
</Card>
```