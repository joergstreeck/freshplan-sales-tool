# 📝 FC-035 CODE EXAMPLES

## social-profile-card
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

## engagement-alerts
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

// Trigger Examples
const triggers = {
  JOB_CHANGE: {
    icon: <WorkIcon />,
    action: 'Gratulieren',
    template: 'Herzlichen Glückwunsch zur neuen Position!'
  },
  COMPANY_NEWS: {
    icon: <BusinessIcon />,
    action: 'Kommentieren',
    template: 'Tolle Neuigkeiten! Würde gerne mehr erfahren.'
  },
  BIRTHDAY: {
    icon: <CakeIcon />,
    action: 'Glückwünsche',
    template: 'Alles Gute zum Geburtstag!'
  }
};
```

## content-suggestions
```typescript
const ContentSuggestions = () => {
  const suggestions = useContentSuggestions();
  
  return (
    <Grid container spacing={2}>
      {suggestions.map(content => (
        <Grid item xs={12} md={6} key={content.id}>
          <Card>
            <CardMedia
              component="img"
              height="140"
              image={content.image}
              alt={content.title}
            />
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
              <Button size="small" onClick={() => useTemplate(content)}>
                Verwenden
              </Button>
              <Button size="small" onClick={() => customize(content)}>
                Anpassen
              </Button>
            </CardActions>
          </Card>
        </Grid>
      ))}
    </Grid>
  );
};
```

## network-mapping
```typescript
const NetworkMap = ({ customer }) => {
  const connections = useConnectionMap(customer);
  
  return (
    <Box sx={{ height: 400 }}>
      {/* D3.js Network Visualization */}
      <ForceGraph2D
        graphData={connections}
        nodeLabel="name"
        nodeAutoColorBy="group"
        linkDirectionalParticles={2}
        onNodeClick={(node) => showProfile(node)}
      />
      
      {/* Key Insights */}
      <Box sx={{ mt: 2 }}>
        <Alert severity="success">
          💡 3 gemeinsame Kontakte mit Zielkunde detected!
        </Alert>
      </Box>
    </Box>
  );
};

// Find warm intros
const findWarmIntros = (targetCompany: string) => {
  return db.connections
    .where('company')
    .equals(targetCompany)
    .and(conn => conn.degree <= 2) // 2nd degree connections
    .toArray();
};
```

## social-listening
```typescript
const socialListener = {
  // Monitor keywords
  keywords: ['catering', 'lieferant', 'frische', 'regional'],
  
  // Track mentions
  trackMentions: async () => {
    const mentions = await linkedinAPI.searchPosts({
      keywords: socialListener.keywords,
      location: 'Hamburg',
      timeframe: 'past-week'
    });
    
    // Match with customers
    return mentions.filter(mention => 
      isRelevantForBusiness(mention)
    );
  },
  
  // Alert on opportunities
  createAlert: (mention) => {
    if (mention.intent === 'looking-for-supplier') {
      notify({
        title: '🔥 Neue Opportunity',
        body: `${mention.author} sucht Lieferanten!`,
        priority: 'high'
      });
    }
  }
};
```

## quick-engage
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

## smart-comments
```typescript
// KI-generierte Kommentare
const smartComments = {
  generate: async (post: Post) => {
    const context = await analyzePost(post);
    
    return {
      professional: `Interessanter Punkt! In unserer Erfahrung...`,
      friendly: `Super Idee! 👍 Wie sind eure Erfahrungen mit...`,
      questioning: `Spannend! Habt ihr auch ${context.topic} berücksichtigt?`,
      supportive: `Kann ich nur bestätigen! Wir haben ähnliche Erfahrungen gemacht.`
    };
  }
};
```

## linkedin-api
```typescript
@ApplicationScoped
public class LinkedInService {
  
  @ConfigProperty(name = "linkedin.api.key")
  String apiKey;
  
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
}
```

## engagement-tracking
```typescript
// Track ROI of Social Selling
CREATE TABLE social_engagements (
  id UUID PRIMARY KEY,
  customer_id UUID REFERENCES customers(id),
  platform VARCHAR(50),
  action_type VARCHAR(50),
  post_url TEXT,
  engaged_at TIMESTAMP,
  resulted_in_opportunity BOOLEAN,
  opportunity_id UUID REFERENCES opportunities(id)
);

// Analytics
SELECT 
  COUNT(*) as engagements,
  SUM(CASE WHEN resulted_in_opportunity THEN 1 ELSE 0 END) as conversions,
  AVG(opportunity_value) as avg_deal_size
FROM social_engagements
WHERE engaged_at > NOW() - INTERVAL '30 days';
```