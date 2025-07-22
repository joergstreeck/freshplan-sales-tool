# 🔌 FC-035 API & INTEGRATION

**Companion zu:** FC-035_OVERVIEW.md  
**Zweck:** API Details, Datenmodell & externe Integrationen  

---

## 📊 DATENMODELL

### Social Profile Entity
```sql
CREATE TABLE social_profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID REFERENCES customers(id) ON DELETE CASCADE,
  platform VARCHAR(50) NOT NULL, -- 'linkedin', 'xing'
  profile_url TEXT NOT NULL,
  profile_id VARCHAR(100), -- Platform-specific ID
  profile_data JSONB, -- Cached profile info
  last_sync TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(customer_id, platform)
);

CREATE INDEX idx_social_profiles_customer ON social_profiles(customer_id);
CREATE INDEX idx_social_profiles_platform ON social_profiles(platform);
```

### Social Activities
```sql
CREATE TABLE social_activities (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID REFERENCES customers(id),
  contact_id UUID REFERENCES contacts(id),
  platform VARCHAR(50),
  activity_type VARCHAR(50), -- 'post', 'job_change', 'company_news'
  activity_data JSONB,
  post_url TEXT,
  occurred_at TIMESTAMP,
  synced_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_social_activities_customer ON social_activities(customer_id);
CREATE INDEX idx_social_activities_type ON social_activities(activity_type);
```

### Engagement Tracking
```sql
CREATE TABLE social_engagements (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  activity_id UUID REFERENCES social_activities(id),
  user_id UUID REFERENCES users(id),
  action_type VARCHAR(50), -- 'like', 'comment', 'share'
  comment_text TEXT,
  engaged_at TIMESTAMP DEFAULT NOW(),
  resulted_in_opportunity BOOLEAN DEFAULT FALSE,
  opportunity_id UUID REFERENCES opportunities(id)
);
```

---

## 🔌 REST API ENDPOINTS

### Social Profiles
```typescript
// GET /api/customers/{id}/social-profiles
interface SocialProfilesResponse {
  profiles: SocialProfile[];
  lastSync: Date;
}

// POST /api/customers/{id}/social-profiles
interface AddSocialProfileRequest {
  platform: 'linkedin' | 'xing';
  profileUrl: string;
}

// POST /api/customers/{id}/social-profiles/sync
// Trigger manual sync of all profiles
```

### Engagement Opportunities
```typescript
// GET /api/engagement-opportunities
interface EngagementOpportunity {
  id: string;
  customerId: string;
  contactId?: string;
  trigger: string;
  triggerType: 'job_change' | 'company_news' | 'birthday';
  suggestedAction: string;
  template: string;
  priority: 'high' | 'medium' | 'low';
  expiresAt: Date;
}

// POST /api/engagement-opportunities/{id}/engage
interface EngageRequest {
  action: 'like' | 'comment' | 'message';
  text?: string;
}
```

### Social Analytics
```typescript
// GET /api/social-analytics/summary
interface SocialAnalytics {
  totalEngagements: number;
  conversionRate: number;
  warmLeads: number;
  topEngagedCustomers: CustomerEngagement[];
}
```

---

## 🔗 EXTERNE INTEGRATIONEN

### LinkedIn API Integration
```typescript
// Configuration
const LINKEDIN_CONFIG = {
  clientId: process.env.LINKEDIN_CLIENT_ID,
  clientSecret: process.env.LINKEDIN_CLIENT_SECRET,
  redirectUri: process.env.LINKEDIN_REDIRECT_URI,
  scopes: ['r_organization_social', 'r_1st_connections_size']
};

// API Wrapper
class LinkedInAPI {
  async getCompanyUpdates(companyId: string) {
    const response = await fetch(
      `https://api.linkedin.com/v2/organizations/${companyId}/updates`,
      {
        headers: {
          'Authorization': `Bearer ${this.accessToken}`,
          'X-Restli-Protocol-Version': '2.0.0'
        }
      }
    );
    return response.json();
  }
  
  async searchPosts(keywords: string[]) {
    // LinkedIn Search API
    const query = encodeURIComponent(keywords.join(' OR '));
    return this.get(`/v2/search/posts?keywords=${query}`);
  }
}
```

### XING API Integration
```java
@ApplicationScoped
public class XingService {
  
  @ConfigProperty(name = "xing.consumer.key")
  String consumerKey;
  
  @ConfigProperty(name = "xing.consumer.secret")
  String consumerSecret;
  
  public CompletionStage<XingProfile> getProfile(String profileId) {
    return client
      .target("https://api.xing.com/v1")
      .path("/users/{id}")
      .resolveTemplate("id", profileId)
      .request()
      .header("Authorization", getOAuthHeader())
      .rx()
      .get(XingProfile.class);
  }
}
```

---

## 🤖 AI/ML INTEGRATION

### Smart Comment Generation
```typescript
// OpenAI Integration für Kommentare
const generateSmartComment = async (post: Post) => {
  const prompt = `
    Generate a professional, friendly comment for this LinkedIn post:
    "${post.content}"
    
    Context: We are a B2B fresh food supplier.
    Tone: Professional but warm
    Length: 1-2 sentences
  `;
  
  const response = await openai.createCompletion({
    model: "gpt-3.5-turbo",
    prompt,
    max_tokens: 100,
    temperature: 0.7
  });
  
  return response.choices[0].text;
};
```

### Connection Intelligence
```typescript
// Graph Analysis für Warm Intros
const findWarmIntros = async (targetCompanyId: string) => {
  const query = `
    MATCH (me:User)-[:CONNECTED_TO]-(contact:Contact)
          -[:WORKS_AT]-(company:Company {id: $targetId})
    RETURN contact, COUNT(*) as mutualConnections
    ORDER BY mutualConnections DESC
    LIMIT 5
  `;
  
  return neo4j.run(query, { targetId: targetCompanyId });
};
```

---

## 🔐 SECURITY & PERMISSIONS

### OAuth2 Flow
```typescript
// LinkedIn OAuth2
router.get('/auth/linkedin', (req, res) => {
  const authUrl = `https://www.linkedin.com/oauth/v2/authorization?` +
    `response_type=code&` +
    `client_id=${LINKEDIN_CONFIG.clientId}&` +
    `redirect_uri=${LINKEDIN_CONFIG.redirectUri}&` +
    `scope=${LINKEDIN_CONFIG.scopes.join(' ')}`;
  
  res.redirect(authUrl);
});

// Callback Handler
router.get('/auth/linkedin/callback', async (req, res) => {
  const { code } = req.query;
  const tokens = await exchangeCodeForTokens(code);
  // Store tokens securely
});
```

### Data Privacy
- Social data cached max. 24h
- Explicit user consent required
- GDPR-compliant data handling
- Right to disconnect social profiles