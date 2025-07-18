# FC-035 IMPLEMENTATION DETAILS

## phase1
### Day 1: Basic Social Integration

```typescript
const SocialProfileCard = ({ customer }) => {
  const profiles = useSocialProfiles(customer.id);
  
  return (
    <Card>
      <CardHeader title="Social Media Profile" />
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
      </CardContent>
    </Card>
  );
};
```

## phase2
### Day 2: Full Feature Set

#### Engagement Alerts
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

#### Smart Comments
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

#### Network Visualization
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
    </Box>
  );
};
```

## Backend Integration

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
}
```

## Database Schema

```sql
CREATE TABLE social_profiles (
  id UUID PRIMARY KEY,
  customer_id UUID REFERENCES customers(id),
  platform VARCHAR(50),
  profile_url TEXT,
  profile_name VARCHAR(255),
  last_synced TIMESTAMP
);

CREATE TABLE social_engagements (
  id UUID PRIMARY KEY,
  customer_id UUID REFERENCES customers(id),
  platform VARCHAR(50),
  action_type VARCHAR(50),
  post_url TEXT,
  engaged_at TIMESTAMP,
  resulted_in_opportunity BOOLEAN
);
```