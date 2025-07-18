# 🔧 FC-035 SOCIAL SELLING - BACKEND

**Fokus:** Quarkus Services & APIs

---

## 🏗️ DOMAIN MODEL

### Entities
```java
@Entity
@Table(name = "social_profiles")
public class SocialProfile extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    public Customer customer;
    
    @Enumerated(EnumType.STRING)
    public Platform platform;
    
    public String profileUrl;
    public String profileName;
    public LocalDateTime lastSynced;
    
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    public List<SocialActivity> activities;
}

@Entity
@Table(name = "social_activities")
public class SocialActivity extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;
    
    @ManyToOne
    @JoinColumn(name = "profile_id")
    public SocialProfile profile;
    
    public String postUrl;
    public String content;
    public LocalDateTime postedAt;
    public Integer likes;
    public Integer comments;
    public Integer shares;
}

@Entity
@Table(name = "engagement_opportunities")
public class EngagementOpportunity extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    public Customer customer;
    
    @Enumerated(EnumType.STRING)
    public TriggerType triggerType;
    
    public String triggerDetails;
    public String suggestedAction;
    public LocalDateTime detectedAt;
    public Boolean acted;
}
```

---

## 🔌 REST ENDPOINTS

### SocialProfileResource
```java
@Path("/api/social-profiles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SocialProfileResource {
    
    @Inject
    SocialProfileService profileService;
    
    @GET
    @Path("/customer/{customerId}")
    public List<SocialProfileDTO> getCustomerProfiles(@PathParam("customerId") UUID customerId) {
        return profileService.getProfilesByCustomer(customerId);
    }
    
    @POST
    @Path("/sync/{profileId}")
    public Response syncProfile(@PathParam("profileId") UUID profileId) {
        profileService.syncProfile(profileId);
        return Response.accepted().build();
    }
    
    @GET
    @Path("/{profileId}/activities")
    public List<ActivityDTO> getRecentActivities(
            @PathParam("profileId") UUID profileId,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        return profileService.getRecentActivities(profileId, limit);
    }
}
```

### EngagementResource
```java
@Path("/api/engagements")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EngagementResource {
    
    @Inject
    EngagementService engagementService;
    
    @GET
    @Path("/opportunities")
    public List<OpportunityDTO> getOpportunities(
            @QueryParam("status") @DefaultValue("pending") String status) {
        return engagementService.getOpportunities(status);
    }
    
    @POST
    @Path("/action")
    public Response executeAction(EngagementActionRequest request) {
        engagementService.executeAction(request);
        return Response.ok().build();
    }
    
    @GET
    @Path("/analytics")
    public EngagementAnalytics getAnalytics(
            @QueryParam("days") @DefaultValue("30") int days) {
        return engagementService.calculateAnalytics(days);
    }
}
```

---

## 🔄 SERVICES

### LinkedInService
```java
@ApplicationScoped
public class LinkedInService {
    
    @ConfigProperty(name = "linkedin.api.key")
    String apiKey;
    
    @ConfigProperty(name = "linkedin.api.url")
    String apiUrl;
    
    @Inject
    @RestClient
    LinkedInClient linkedInClient;
    
    public CompletionStage<List<Post>> getCompanyFeed(String companyId) {
        return linkedInClient.getCompanyPosts(companyId, apiKey)
            .thenApply(this::mapToInternalModel);
    }
    
    public CompletionStage<ProfileInfo> getProfileInfo(String profileId) {
        return linkedInClient.getProfile(profileId, apiKey);
    }
    
    @Scheduled(every = "1h")
    void syncAllProfiles() {
        Log.info("Starting social profile sync");
        
        SocialProfile.find("platform", Platform.LINKEDIN)
            .stream()
            .forEach(profile -> {
                try {
                    syncProfile(profile);
                } catch (Exception e) {
                    Log.error("Failed to sync profile " + profile.id, e);
                }
            });
    }
    
    private void syncProfile(SocialProfile profile) {
        getProfileInfo(profile.externalId)
            .thenAccept(info -> {
                profile.profileName = info.getName();
                profile.lastSynced = LocalDateTime.now();
                profile.persist();
            });
    }
}
```

### EngagementService
```java
@ApplicationScoped
@Transactional
public class EngagementService {
    
    @Inject
    EventBus eventBus;
    
    public List<OpportunityDTO> getOpportunities(String status) {
        return EngagementOpportunity
            .find("acted", status.equals("completed"))
            .list()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public void detectOpportunities() {
        // Job change detection
        detectJobChanges();
        
        // Company news
        detectCompanyNews();
        
        // Birthdays
        detectBirthdays();
    }
    
    private void detectJobChanges() {
        // Query LinkedIn for job changes
        List<JobChange> changes = linkedInService.detectJobChanges();
        
        changes.forEach(change -> {
            var opportunity = new EngagementOpportunity();
            opportunity.customer = change.getCustomer();
            opportunity.triggerType = TriggerType.JOB_CHANGE;
            opportunity.triggerDetails = change.getNewPosition();
            opportunity.suggestedAction = "Gratulieren";
            opportunity.detectedAt = LocalDateTime.now();
            opportunity.acted = false;
            opportunity.persist();
            
            // Emit event
            eventBus.publish(new OpportunityDetectedEvent(opportunity));
        });
    }
    
    public EngagementAnalytics calculateAnalytics(int days) {
        var cutoff = LocalDateTime.now().minusDays(days);
        
        var analytics = new EngagementAnalytics();
        analytics.totalEngagements = EngagementAction.count("createdAt > ?1", cutoff);
        analytics.conversionRate = calculateConversionRate(cutoff);
        analytics.topEngagers = findTopEngagers(cutoff);
        
        return analytics;
    }
}
```

---

## 📊 ANALYTICS & TRACKING

### Database Schema
```sql
-- Social engagement tracking
CREATE TABLE social_engagements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID REFERENCES customers(id),
    platform VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    post_url TEXT,
    engaged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resulted_in_opportunity BOOLEAN DEFAULT FALSE,
    opportunity_id UUID REFERENCES opportunities(id),
    notes TEXT
);

-- Analytics views
CREATE VIEW engagement_analytics AS
SELECT 
    DATE_TRUNC('week', engaged_at) as week,
    platform,
    COUNT(*) as engagements,
    SUM(CASE WHEN resulted_in_opportunity THEN 1 ELSE 0 END) as conversions,
    AVG(CASE WHEN resulted_in_opportunity THEN 1 ELSE 0 END)::NUMERIC(5,2) as conversion_rate
FROM social_engagements
GROUP BY DATE_TRUNC('week', engaged_at), platform
ORDER BY week DESC;
```

### Event Processing
```java
@ApplicationScoped
public class EngagementEventProcessor {
    
    @Inject
    EventBus eventBus;
    
    void onEngagementAction(@Observes EngagementActionEvent event) {
        // Track in database
        var tracking = new EngagementTracking();
        tracking.customerId = event.getCustomerId();
        tracking.platform = event.getPlatform();
        tracking.actionType = event.getActionType();
        tracking.engagedAt = LocalDateTime.now();
        tracking.persist();
        
        // Update opportunity if exists
        if (event.getOpportunityId() != null) {
            var opportunity = EngagementOpportunity.findById(event.getOpportunityId());
            opportunity.acted = true;
            opportunity.persist();
        }
        
        // Emit analytics event
        eventBus.publish(new AnalyticsEvent("engagement", tracking));
    }
}
```

---

## 🔒 SECURITY

```java
@ApplicationScoped
public class SocialSecurityService {
    
    @ConfigProperty(name = "social.encryption.key")
    String encryptionKey;
    
    public String encryptToken(String token) {
        // Encrypt social media tokens before storage
        return AES.encrypt(token, encryptionKey);
    }
    
    public String decryptToken(String encrypted) {
        return AES.decrypt(encrypted, encryptionKey);
    }
    
    @RolesAllowed({"admin", "manager"})
    public void validateAccess(UUID customerId) {
        // Additional access control for social features
    }
}
```

---

**Frontend-Dokument:** [FC-035-FRONTEND.md](./option4_frontend.md)