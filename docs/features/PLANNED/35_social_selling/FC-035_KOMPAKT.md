# 🤝 FC-035 SOCIAL SELLING HELPER (ARCHIVIERT)

**HINWEIS:** Dieses Dokument wurde aufgeteilt in:
- [FC-035_OVERVIEW.md](./FC-035_OVERVIEW.md) - Übersicht & Business Value
- [FC-035_IMPLEMENTATION.md](./FC-035_IMPLEMENTATION.md) - Frontend & Backend Code
- [FC-035_API.md](./FC-035_API.md) - API & Integration
- [FC-035_TESTING.md](./FC-035_TESTING.md) - Test-Strategie

---

# 🤝 FC-035 SOCIAL SELLING HELPER (KOMPAKT)

**Erstellt:** 19.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🔀 FULLSTACK  
**Priorität:** MEDIUM - Moderne Kundenbeziehung  
**Geschätzt:** 2 Tage  

---

## 🧠 WAS WIR BAUEN

**Problem:** Kunden sind auf LinkedIn, nicht am Telefon  
**Lösung:** Social Media ins CRM integrieren  
**Value:** Beziehungen aufbauen, nicht cold callen  

> **Business Case:** Warm Leads durch Social Engagement

### 🎯 Core Features:
- **Social Monitoring:** Was posten meine Kunden?
- **Engagement Alerts:** Chef hat befördert → Gratulieren!
- **Content Suggestions:** Was soll ich posten?
- **Connection Mapping:** Wer kennt wen?

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Social Profile Integration:**
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

### 2. **Engagement Opportunities:**
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

### 3. **Content Calendar:**
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

---

## 🔗 CONNECTION INTELLIGENCE

### Network Mapping:
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

### Social Listening:
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

---

## 📱 ENGAGEMENT WORKFLOWS

### Quick Engage:
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

### Smart Comments:
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

---

## 🔧 BACKEND INTEGRATION

### LinkedIn API Wrapper:
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

### Engagement Tracking:
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

---

## 🎯 SUCCESS METRICS

- **Engagement Rate:** 30% der Posts liked/kommentiert
- **Warm Leads:** +40% durch Social
- **Response Rate:** 3x höher als Cold Calls
- **Time to Close:** -20% durch Beziehungsaufbau

**Integration:** Tag 21-22 nach Timeline & Beziehungen!

---

## 🧭 NAVIGATION & VERWEISE

### 📋 Zurück zum Überblick:
- **[📊 Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)** - Vollständige Feature-Roadmap
- **[🗺️ Feature Overview](/docs/features/MASTER/FEATURE_OVERVIEW.md)** - Alle 40 Features im Überblick

### 🔗 Dependencies (Required):
- **[👥 FC-036 Beziehungsmanagement](/docs/features/PLANNED/36_relationship_mgmt/FC-036_KOMPAKT.md)** - Contact Network
- **[📈 FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)** - Social Activities
- **[🔍 FC-034 Instant Insights](/docs/features/PLANNED/34_instant_insights/FC-034_KOMPAKT.md)** - Social Insights

### ⚡ Social Media Integration:
- **[📧 FC-003 Email Integration](/docs/features/PLANNED/06_email_integration/FC-003_KOMPAKT.md)** - Multi-Channel
- **[💬 FC-028 WhatsApp Integration](/docs/features/PLANNED/28_whatsapp_integration/FC-028_KOMPAKT.md)** - Messaging
- **[📁 FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_KOMPAKT.md)** - Post Assets

### 🚀 Ermöglicht folgende Features:
- **[🎯 FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_KOMPAKT.md)** - Social Celebrations
- **[📊 FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_KOMPAKT.md)** - Social ROI
- **[🏆 FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_KOMPAKT.md)** - Social Points

### 🎨 UI Integration:
- **[📊 M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)** - Social Widget
- **[📱 FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_KOMPAKT.md)** - Mobile Social
- **[⚡ M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)** - Quick Post

### 🔧 Technische Details:
- **FC-035_IMPLEMENTATION_GUIDE.md** *(geplant)* - API Integration Guide
- **FC-035_DECISION_LOG.md** *(geplant)* - Platform-Auswahl
- **SOCIAL_PLAYBOOK.md** *(geplant)* - Best Practices Social Selling