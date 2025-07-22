# 📝 FC-035 IMPLEMENTATION DETAILS

**Companion zu:** FC-035_OVERVIEW.md  
**Zweck:** Vollständige Frontend & Backend Implementierung  

---

## 🎨 FRONTEND COMPONENTS

### 1. Social Profile Card Component
```typescript
const SocialProfileCard = ({ customer }) => {
  const profiles = useSocialProfiles(customer.id);
  
  return (
    <Card>
      <CardHeader
        title="Social Media Profile"
        action={
          <IconButton onClick={syncProfiles}>
            <SyncIcon />
          </IconButton>
        }
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
          {profiles.xing && (
            <Chip
              icon={<BusinessIcon />}
              label="XING"
              component="a"
              href={profiles.xing.url}
              clickable
            />
          )}
        </Stack>
        
        {/* Recent Activity */}
        <Box sx={{ mt: 2 }}>
          <Typography variant="subtitle2">Letzte Aktivität:</Typography>
          {profiles.recentPosts.map(post => (
            <Alert 
              key={post.id} 
              severity="info" 
              sx={{ mt: 1 }}
              action={
                <Button size="small" onClick={() => engageWith(post)}>
                  Reagieren
                </Button>
              }
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

### 2. Engagement Alerts Component
```typescript
const EngagementAlerts = () => {
  const opportunities = useEngagementOpportunities();
  
  return (
    <List>
      {opportunities.map(opp => (
        <ListItem key={opp.id}>
          <ListItemAvatar>
            <Avatar src={opp.contact.photo}>
              {opp.contact.initials}
            </Avatar>
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

### 3. Network Visualization
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

### 4. Quick Engage Widget
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

## 🔧 BACKEND SERVICES

### 1. LinkedIn Service
```java
@ApplicationScoped
public class LinkedInService {
  
  @ConfigProperty(name = "linkedin.api.key")
  String apiKey;
  
  @Inject
  CustomerService customerService;
  
  public CompletionStage<List<Post>> getCompanyFeed(String companyId) {
    return client
      .target(LINKEDIN_API_URL)
      .path("/companies/{id}/posts")
      .resolveTemplate("id", companyId)
      .request()
      .header("Authorization", "Bearer " + apiKey)
      .rx()
      .get(new GenericType<List<Post>>() {});
  }
  
  @Scheduled(every = "1h")
  void syncSocialData() {
    customerService.getActiveCustomers()
      .forEach(customer -> {
        if (customer.getLinkedInId() != null) {
          updateSocialActivity(customer);
        }
      });
  }
  
  private void updateSocialActivity(Customer customer) {
    getCompanyFeed(customer.getLinkedInId())
      .thenAccept(posts -> {
        posts.forEach(post -> {
          socialActivityRepository.save(
            SocialActivity.from(customer.getId(), post)
          );
        });
      });
  }
}
```

### 2. Engagement Engine
```java
@ApplicationScoped
public class EngagementEngine {
  
  public List<EngagementOpportunity> findOpportunities() {
    List<EngagementOpportunity> opportunities = new ArrayList<>();
    
    // Job Changes
    opportunities.addAll(findJobChanges());
    
    // Company News
    opportunities.addAll(findCompanyNews());
    
    // Birthdays & Anniversaries
    opportunities.addAll(findSpecialDates());
    
    return opportunities
      .stream()
      .sorted(Comparator.comparing(EngagementOpportunity::getPriority))
      .limit(10)
      .collect(Collectors.toList());
  }
  
  private List<EngagementOpportunity> findJobChanges() {
    return socialActivityRepository
      .findRecentByType("JOB_CHANGE")
      .stream()
      .map(activity -> EngagementOpportunity.builder()
        .trigger("Neue Position")
        .action("Gratulieren")
        .template("Herzlichen Glückwunsch zur neuen Position!")
        .priority(Priority.HIGH)
        .build())
      .collect(Collectors.toList());
  }
}
```

---

## 🔗 INTEGRATION HOOKS

### 1. Customer Detail Page
```typescript
// In CustomerDetail.tsx
<Grid container spacing={3}>
  <Grid item xs={12} md={8}>
    <CustomerInfo customer={customer} />
  </Grid>
  <Grid item xs={12} md={4}>
    <SocialProfileCard customer={customer} />  {/* NEU */}
  </Grid>
</Grid>
```

### 2. Dashboard Widget
```typescript
// In Dashboard.tsx
<Grid item xs={12} md={6}>
  <Paper sx={{ p: 2 }}>
    <Typography variant="h6">Social Engagement</Typography>
    <EngagementAlerts />  {/* NEU */}
  </Paper>
</Grid>
```

### 3. Activity Timeline Integration
```typescript
// In ActivityTimeline.tsx
const renderActivity = (activity) => {
  switch(activity.type) {
    case 'SOCIAL_ENGAGEMENT':
      return <SocialEngagementItem activity={activity} />;
    // ... andere Typen
  }
};
```