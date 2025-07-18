# 🎨 FC-035 SOCIAL SELLING - FRONTEND

**Fokus:** React Components & UI/UX

---

## 📱 COMPONENT LIBRARY

### 1. SocialProfileCard
```typescript
const SocialProfileCard = ({ customer }) => {
  const profiles = useSocialProfiles(customer.id);
  
  return (
    <Card>
      <CardHeader
        title="Social Media Profile"
        action={<IconButton onClick={syncProfiles}><SyncIcon /></IconButton>}
      />
      <CardContent>
        <Stack direction="row" spacing={2}>
          {profiles.linkedin && (
            <Chip
              icon={<LinkedInIcon />}
              label={profiles.linkedin.name}
              component="a"
              href={profiles.linkedin.url}
              clickable
            />
          )}
        </Stack>
        <Box sx={{ mt: 2 }}>
          <Typography variant="subtitle2">Letzte Aktivität:</Typography>
          {profiles.recentPosts.map(post => (
            <Alert key={post.id} severity="info" sx={{ mt: 1 }}
              action={<Button size="small" onClick={() => engageWith(post)}>Reagieren</Button>}
            >
              {post.summary}
            </Alert>
          ))}
        </Box>
      </CardContent>
    </Card>
  );
};
```

### 2. EngagementAlerts
```typescript
const EngagementAlerts = () => {
  const opportunities = useEngagementOpportunities();
  
  return (
    <List>
      {opportunities.map(opp => (
        <ListItem key={opp.id}>
          <ListItemAvatar>
            <Avatar src={opp.contact.photo}>{opp.contact.initials}</Avatar>
          </ListItemAvatar>
          <ListItemText
            primary={opp.trigger}
            secondary={`${opp.contact.name} - ${opp.company}`}
          />
          <ListItemSecondaryAction>
            <Button
              startIcon={getActionIcon(opp.suggestedAction)}
              onClick={() => executeAction(opp)}
            >
              {opp.suggestedAction}
            </Button>
          </ListItemSecondaryAction>
        </ListItem>
      ))}
    </List>
  );
};
```

### 3. ContentSuggestions
```typescript
const ContentSuggestions = () => {
  const suggestions = useContentSuggestions();
  
  return (
    <Grid container spacing={2}>
      {suggestions.map(content => (
        <Grid item xs={12} md={6} key={content.id}>
          <Card>
            <CardMedia component="img" height="140" image={content.image} />
            <CardContent>
              <Typography variant="h6">{content.title}</Typography>
              <Typography variant="body2" color="text.secondary">
                {content.hook}
              </Typography>
              <Box sx={{ mt: 2 }}>
                <Chip label={content.topic} size="small" />
                <Chip label={`${content.engagement}% Engagement`} size="small" sx={{ ml: 1 }} />
              </Box>
            </CardContent>
            <CardActions>
              <Button size="small" onClick={() => useTemplate(content)}>Verwenden</Button>
              <Button size="small" onClick={() => customize(content)}>Anpassen</Button>
            </CardActions>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};
```

### 4. NetworkVisualization
```typescript
const NetworkMap = ({ customer }) => {
  const connections = useConnectionMap(customer);
  
  return (
    <Box sx={{ height: 400 }}>
      <ForceGraph2D
        graphData={connections}
        nodeLabel="name"
        nodeAutoColorBy="group"
        linkDirectionalParticles={2}
        onNodeClick={(node) => showProfile(node)}
      />
      <Box sx={{ mt: 2 }}>
        <Alert severity="success">
          💡 3 gemeinsame Kontakte mit Zielkunde detected!
        </Alert>
      </Box>
    </Box>
  );
};
```

### 5. QuickEngage
```typescript
const QuickEngage = ({ post }) => {
  const [showOptions, setShowOptions] = useState(false);
  
  const quickActions = [
    { icon: '👍', action: 'like' },
    { icon: '❤️', action: 'love' },
    { icon: '🎉', action: 'celebrate' },
    { icon: '💡', action: 'insightful' }
  ];
  
  return (
    <SpeedDial
      ariaLabel="Quick Engage"
      icon={<ThumbUpIcon />}
      onOpen={() => setShowOptions(true)}
      onClose={() => setShowOptions(false)}
      open={showOptions}
    >
      {quickActions.map(action => (
        <SpeedDialAction
          key={action.action}
          icon={<span>{action.icon}</span>}
          tooltipTitle={action.action}
          onClick={() => {
            engageWithPost(post.id, action.action);
            trackEngagement(post, action);
          }}
        />
      ))}
    </SpeedDial>
  );
};
```

---

## 🎨 UI/UX PATTERNS

### Material-UI Theme Extensions
```typescript
const socialTheme = {
  components: {
    MuiChip: {
      styleOverrides: {
        root: {
          '&.social-linkedin': {
            backgroundColor: '#0077B5',
            color: 'white'
          },
          '&.social-xing': {
            backgroundColor: '#006567',
            color: 'white'
          }
        }
      }
    }
  }
};
```

### Custom Hooks
```typescript
const useSocialProfiles = (customerId: string) => {
  return useQuery({
    queryKey: ['social-profiles', customerId],
    queryFn: () => api.getSocialProfiles(customerId),
    staleTime: 5 * 60 * 1000 // 5 minutes
  });
};

const useEngagementOpportunities = () => {
  return useQuery({
    queryKey: ['engagement-opportunities'],
    queryFn: () => api.getEngagementOpportunities(),
    refetchInterval: 60 * 1000 // 1 minute
  });
};
```

---

## 📊 STATE MANAGEMENT

```typescript
// Zustand Store
const useSocialStore = create((set) => ({
  activeEngagements: [],
  pendingActions: [],
  
  addEngagement: (engagement) => set((state) => ({
    activeEngagements: [...state.activeEngagements, engagement]
  })),
  
  completeAction: (actionId) => set((state) => ({
    pendingActions: state.pendingActions.filter(a => a.id !== actionId)
  }))
}));
```

---

**Backend-Dokument:** [FC-035-BACKEND.md](./option4_backend.md)