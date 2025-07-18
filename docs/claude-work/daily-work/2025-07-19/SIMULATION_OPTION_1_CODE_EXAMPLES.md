# 📝 FC-035 CODE EXAMPLES

**Companion zu:** FC-035_KOMPAKT.md  
**Zweck:** Vollständige Code-Implementierungen  

---

## 🎨 FRONTEND COMPONENTS

### SocialProfileCard Component:
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

### EngagementAlerts Component:
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

### Network Visualization:
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

---

## 🔧 BACKEND IMPLEMENTATION

### LinkedIn Service:
```java
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

### Database Schema:
```sql
CREATE TABLE social_profiles (
  id UUID PRIMARY KEY,
  customer_id UUID REFERENCES customers(id),
  platform VARCHAR(50),
  profile_url TEXT,
  profile_data JSONB,
  last_sync TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW()
);

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
```

### Smart Engagement Engine:
```typescript
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

---

**Zeilen: ~220** (Alle Code-Beispiele komplett!)