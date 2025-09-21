# 📱 Social Media Integration - LinkedIn & XING Intelligence

**Phase:** 3 - Premium Features  
**Priorität:** 🟢 MITTEL - Wertvolle Insights  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/ORG_CHART_VISUALIZATION.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AI_DATA_ENRICHMENT.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- Vertrieb (Gesprächs-Aufhänger)
- Research (Hintergrund-Info)
- Networking (Gemeinsame Kontakte)

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Social Media Integration implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Social Media Services
mkdir -p backend/src/main/java/de/freshplan/social
touch backend/src/main/java/de/freshplan/social/service/LinkedInService.java
touch backend/src/main/java/de/freshplan/social/service/XingService.java
touch backend/src/main/java/de/freshplan/social/service/SocialEnrichmentService.java
touch backend/src/main/java/de/freshplan/social/entity/SocialProfile.java

# 2. Frontend Social Components
mkdir -p frontend/src/features/customers/components/social
touch frontend/src/features/customers/components/social/SocialProfileCard.tsx
touch frontend/src/features/customers/components/social/LinkedInInsights.tsx
touch frontend/src/features/customers/components/social/CommonConnections.tsx
touch frontend/src/features/customers/components/social/ActivityFeed.tsx

# 3. API Configuration
touch backend/src/main/resources/social-api-config.properties

# 4. Migration (nächste freie Nummer prüfen!)
ls -la backend/src/main/resources/db/migration/ | tail -5
# Erstelle V[NEXT]__create_social_profiles_table.sql

# 5. Tests
mkdir -p backend/src/test/java/de/freshplan/social
touch backend/src/test/java/de/freshplan/social/SocialEnrichmentServiceTest.java
```

## 🎯 Das Problem: Blinde Kontaktaufnahme

**Verpasste Chancen:**
- 🤝 "Wir kennen beide Max Müller!" - Nicht gewusst
- 📚 "Ich sehe Sie waren bei SAP" - Keine Ahnung
- 🎯 "Gratulation zum neuen Job!" - Verpasst
- 💡 "Ihr Artikel war interessant" - Nicht gesehen

## 💡 DIE LÖSUNG: Intelligente Social Media Integration

### 1. Social Profile Entity

**Datei:** `backend/src/main/java/de/freshplan/social/entity/SocialProfile.java`

```java
// CLAUDE: Social Media Profile Aggregation
// Pfad: backend/src/main/java/de/freshplan/social/entity/SocialProfile.java

package de.freshplan.social.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "social_profiles")
public class SocialProfile extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", unique = true)
    public CustomerContact contact;
    
    // LinkedIn Data
    @Column(name = "linkedin_url")
    public String linkedInUrl;
    
    @Column(name = "linkedin_id")
    public String linkedInId;
    
    @Column(name = "linkedin_headline", length = 500)
    public String linkedInHeadline;
    
    @Column(name = "linkedin_summary", columnDefinition = "TEXT")
    public String linkedInSummary;
    
    @Column(name = "linkedin_connections")
    public Integer linkedInConnections;
    
    @Column(name = "linkedin_profile_image")
    public String linkedInProfileImage;
    
    // XING Data
    @Column(name = "xing_url")
    public String xingUrl;
    
    @Column(name = "xing_id")
    public String xingId;
    
    @Column(name = "xing_wants", columnDefinition = "TEXT")
    public String xingWants; // Was die Person sucht
    
    @Column(name = "xing_haves", columnDefinition = "TEXT")
    public String xingHaves; // Was die Person anbietet
    
    @Column(name = "xing_interests", columnDefinition = "TEXT")
    public String xingInterests;
    
    // Common Fields
    @ElementCollection
    @CollectionTable(name = "social_skills")
    public Set<String> skills = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "social_languages")
    public Set<String> languages = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "social_certifications")
    public Set<String> certifications = new HashSet<>();
    
    // Employment History
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    public List<Employment> employmentHistory = new ArrayList<>();
    
    // Education
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    public List<Education> educationHistory = new ArrayList<>();
    
    // Recent Activity
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    @OrderBy("activityDate DESC")
    public List<SocialActivity> recentActivities = new ArrayList<>();
    
    // Common Connections (als JSON für Flexibilität)
    @Column(name = "common_connections", columnDefinition = "jsonb")
    @Convert(converter = JsonNodeConverter.class)
    public JsonNode commonConnections;
    
    // Enrichment Metadata
    @Column(name = "last_enriched_at")
    public LocalDateTime lastEnrichedAt;
    
    @Column(name = "enrichment_source")
    public String enrichmentSource; // "manual", "api", "scraping"
    
    @Column(name = "enrichment_quality")
    @Enumerated(EnumType.STRING)
    public EnrichmentQuality enrichmentQuality;
    
    @Column(name = "profile_completeness")
    public Integer profileCompleteness; // 0-100%
    
    // Conversation Starters
    @ElementCollection
    @CollectionTable(name = "conversation_starters")
    public List<String> conversationStarters = new ArrayList<>();
    
    // Audit
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateCompleteness();
        generateConversationStarters();
    }
    
    /**
     * Calculate profile completeness score
     */
    private void calculateCompleteness() {
        int score = 0;
        int fields = 0;
        
        if (linkedInUrl != null) { score += 10; fields++; }
        if (linkedInHeadline != null) { score += 5; fields++; }
        if (linkedInSummary != null) { score += 10; fields++; }
        if (xingUrl != null) { score += 10; fields++; }
        if (!skills.isEmpty()) { score += 10; fields++; }
        if (!employmentHistory.isEmpty()) { score += 15; fields++; }
        if (!educationHistory.isEmpty()) { score += 10; fields++; }
        if (!recentActivities.isEmpty()) { score += 15; fields++; }
        if (commonConnections != null) { score += 15; fields++; }
        
        this.profileCompleteness = Math.min(score, 100);
    }
    
    /**
     * Generate conversation starters based on profile data
     */
    private void generateConversationStarters() {
        conversationStarters.clear();
        
        // Recent job change
        if (!employmentHistory.isEmpty() && employmentHistory.get(0).startDate != null) {
            LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
            if (employmentHistory.get(0).startDate.isAfter(sixMonthsAgo)) {
                conversationStarters.add(
                    "Gratulation zur neuen Position bei " + employmentHistory.get(0).company + "!"
                );
            }
        }
        
        // Shared connections
        if (commonConnections != null && commonConnections.has("count")) {
            int count = commonConnections.get("count").asInt();
            if (count > 0) {
                conversationStarters.add(
                    "Wir haben " + count + " gemeinsame Kontakte auf LinkedIn"
                );
            }
        }
        
        // Recent activity
        if (!recentActivities.isEmpty()) {
            SocialActivity latest = recentActivities.get(0);
            if (latest.type == ActivityType.POST) {
                conversationStarters.add(
                    "Ihr Beitrag über '" + latest.title + "' war sehr interessant"
                );
            }
        }
        
        // Shared education
        if (!educationHistory.isEmpty() && contact.customer != null) {
            // Check if any team member went to same school
            // This would need additional logic to compare with team profiles
        }
    }
}

@Entity
@Table(name = "social_employment")
public class Employment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    public String company;
    public String position;
    public String description;
    public LocalDate startDate;
    public LocalDate endDate;
    public Boolean isCurrent = false;
    public String location;
}

@Entity
@Table(name = "social_education")
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    public String institution;
    public String degree;
    public String fieldOfStudy;
    public LocalDate startDate;
    public LocalDate endDate;
    public String grade;
}

@Entity
@Table(name = "social_activities")
public class SocialActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;
    
    @Enumerated(EnumType.STRING)
    public ActivityType type;
    
    public String title;
    public String description;
    public String url;
    public LocalDateTime activityDate;
    public Integer engagementCount; // Likes, Comments, etc.
    
    @Enumerated(EnumType.STRING)
    public Platform platform;
}

public enum ActivityType {
    POST, ARTICLE, SHARE, COMMENT, LIKE, JOB_CHANGE, ACHIEVEMENT
}

public enum Platform {
    LINKEDIN, XING, TWITTER, FACEBOOK
}

public enum EnrichmentQuality {
    HIGH, MEDIUM, LOW, UNVERIFIED
}
```

### 2. Social Enrichment Service

**Datei:** `backend/src/main/java/de/freshplan/social/service/SocialEnrichmentService.java`

```java
// CLAUDE: Service für Social Media Enrichment mit API Limits
// Pfad: backend/src/main/java/de/freshplan/social/service/SocialEnrichmentService.java

package de.freshplan.social.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Transactional
public class SocialEnrichmentService {
    
    @Inject
    LinkedInService linkedInService;
    
    @Inject
    XingService xingService;
    
    @Inject
    SocialProfileRepository profileRepository;
    
    @Inject
    ApiUsageTracker apiUsageTracker;
    
    @ConfigProperty(name = "social.enrichment.auto-enabled", defaultValue = "false")
    boolean autoEnrichmentEnabled;
    
    @ConfigProperty(name = "social.enrichment.daily-limit", defaultValue = "100")
    int dailyEnrichmentLimit;
    
    /**
     * Enrich contact with social media data
     */
    public CompletableFuture<SocialProfile> enrichContact(CustomerContact contact) {
        return CompletableFuture.supplyAsync(() -> {
            // Check API limits
            if (!apiUsageTracker.canMakeRequest("social_enrichment", 1)) {
                throw new ApiLimitExceededException("Daily enrichment limit reached");
            }
            
            // Find or create profile
            SocialProfile profile = profileRepository
                .find("contact", contact)
                .firstResultOptional()
                .orElseGet(() -> {
                    SocialProfile newProfile = new SocialProfile();
                    newProfile.contact = contact;
                    return newProfile;
                });
            
            // Try LinkedIn enrichment
            if (contact.getEmail() != null) {
                try {
                    LinkedInProfile linkedInData = linkedInService.findProfile(
                        contact.getEmail(),
                        contact.getFirstName(),
                        contact.getLastName(),
                        contact.getCompany()
                    );
                    
                    if (linkedInData != null) {
                        mapLinkedInData(profile, linkedInData);
                    }
                } catch (Exception e) {
                    log.warn("LinkedIn enrichment failed for contact {}: {}", 
                        contact.getId(), e.getMessage());
                }
            }
            
            // Try XING enrichment (mainly for DACH region)
            if (isDACHRegion(contact)) {
                try {
                    XingProfile xingData = xingService.findProfile(
                        contact.getEmail(),
                        contact.getFirstName(),
                        contact.getLastName()
                    );
                    
                    if (xingData != null) {
                        mapXingData(profile, xingData);
                    }
                } catch (Exception e) {
                    log.warn("XING enrichment failed for contact {}: {}", 
                        contact.getId(), e.getMessage());
                }
            }
            
            // Find common connections
            if (profile.linkedInUrl != null) {
                findCommonConnections(profile);
            }
            
            // Generate insights
            generateInsights(profile);
            
            // Update metadata
            profile.lastEnrichedAt = LocalDateTime.now();
            profile.enrichmentSource = "api";
            profile.enrichmentQuality = evaluateQuality(profile);
            
            // Save
            profileRepository.persist(profile);
            
            // Track API usage
            apiUsageTracker.recordUsage("social_enrichment", 1);
            
            return profile;
        });
    }
    
    /**
     * Find common connections with team members
     */
    private void findCommonConnections(SocialProfile profile) {
        try {
            // Get team LinkedIn profiles
            List<String> teamLinkedInIds = getTeamLinkedInIds();
            
            if (!teamLinkedInIds.isEmpty() && profile.linkedInId != null) {
                CommonConnectionsResult result = linkedInService.findCommonConnections(
                    profile.linkedInId,
                    teamLinkedInIds
                );
                
                // Store as JSON
                Map<String, Object> connections = new HashMap<>();
                connections.put("count", result.getTotalCount());
                connections.put("topConnections", result.getTopConnections());
                connections.put("lastUpdated", LocalDateTime.now());
                
                profile.commonConnections = objectMapper.valueToTree(connections);
            }
        } catch (Exception e) {
            log.warn("Failed to find common connections: {}", e.getMessage());
        }
    }
    
    /**
     * Generate actionable insights
     */
    private void generateInsights(SocialProfile profile) {
        List<SocialInsight> insights = new ArrayList<>();
        
        // Job change detection
        if (!profile.employmentHistory.isEmpty()) {
            Employment current = profile.employmentHistory.stream()
                .filter(e -> e.isCurrent)
                .findFirst()
                .orElse(null);
            
            if (current != null && current.startDate != null) {
                long monthsSinceStart = ChronoUnit.MONTHS.between(
                    current.startDate, LocalDate.now()
                );
                
                if (monthsSinceStart <= 3) {
                    insights.add(new SocialInsight(
                        InsightType.JOB_CHANGE,
                        "Neue Position seit " + monthsSinceStart + " Monaten",
                        "Perfekter Zeitpunkt für Kontaktaufnahme bei neuer Rolle",
                        InsightPriority.HIGH
                    ));
                }
            }
        }
        
        // Shared interests
        Set<String> sharedSkills = findSharedSkills(profile);
        if (!sharedSkills.isEmpty()) {
            insights.add(new SocialInsight(
                InsightType.SHARED_INTEREST,
                "Gemeinsame Skills: " + String.join(", ", sharedSkills),
                "Nutzen Sie gemeinsame Expertise als Gesprächsaufhänger",
                InsightPriority.MEDIUM
            ));
        }
        
        // Activity patterns
        if (!profile.recentActivities.isEmpty()) {
            Map<DayOfWeek, Long> activityByDay = profile.recentActivities.stream()
                .collect(Collectors.groupingBy(
                    a -> a.activityDate.getDayOfWeek(),
                    Collectors.counting()
                ));
            
            DayOfWeek mostActiveDay = activityByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
            
            if (mostActiveDay != null) {
                insights.add(new SocialInsight(
                    InsightType.BEST_TIME,
                    "Meist aktiv am " + germanDayName(mostActiveDay),
                    "Beste Zeit für Social Media Interaktion",
                    InsightPriority.LOW
                ));
            }
        }
        
        // Store insights
        profile.insights = insights;
    }
    
    /**
     * Evaluate profile quality
     */
    private EnrichmentQuality evaluateQuality(SocialProfile profile) {
        int score = 0;
        
        if (profile.linkedInUrl != null) score += 30;
        if (profile.linkedInHeadline != null) score += 10;
        if (!profile.employmentHistory.isEmpty()) score += 20;
        if (!profile.skills.isEmpty()) score += 10;
        if (profile.commonConnections != null) score += 20;
        if (!profile.recentActivities.isEmpty()) score += 10;
        
        if (score >= 70) return EnrichmentQuality.HIGH;
        if (score >= 40) return EnrichmentQuality.MEDIUM;
        if (score >= 20) return EnrichmentQuality.LOW;
        return EnrichmentQuality.UNVERIFIED;
    }
}
```

### 3. Frontend Social Profile Component

**Datei:** `frontend/src/features/customers/components/social/SocialProfileCard.tsx`

```typescript
// CLAUDE: Social Media Profile Card mit LinkedIn/XING Integration
// Pfad: frontend/src/features/customers/components/social/SocialProfileCard.tsx

import React, { useState, useEffect } from 'react';
import {
  Card,
  CardContent,
  CardActions,
  Box,
  Typography,
  Avatar,
  Chip,
  Button,
  IconButton,
  Link,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  ListItemSecondary,
  Divider,
  Skeleton,
  Alert,
  Tooltip,
  Badge,
  LinearProgress,
  Collapse,
  Menu,
  MenuItem
} from '@mui/material';
import {
  LinkedIn as LinkedInIcon,
  Language as XingIcon,
  Work as WorkIcon,
  School as SchoolIcon,
  EmojiObjects as InsightIcon,
  People as ConnectionsIcon,
  Timeline as ActivityIcon,
  Refresh as RefreshIcon,
  OpenInNew as OpenIcon,
  ContentCopy as CopyIcon,
  Chat as ChatIcon,
  TrendingUp as TrendingIcon,
  Schedule as ScheduleIcon,
  Star as StarIcon
} from '@mui/icons-material';
import { format, formatDistanceToNow } from 'date-fns';
import { de } from 'date-fns/locale';

interface SocialProfileCardProps {
  contactId: string;
  onConversationStarter?: (starter: string) => void;
}

export const SocialProfileCard: React.FC<SocialProfileCardProps> = ({
  contactId,
  onConversationStarter
}) => {
  const [profile, setProfile] = useState<SocialProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [enriching, setEnriching] = useState(false);
  const [activeTab, setActiveTab] = useState(0);
  const [expandedSections, setExpandedSections] = useState<Set<string>>(new Set());
  
  useEffect(() => {
    loadSocialProfile();
  }, [contactId]);
  
  const loadSocialProfile = async () => {
    setLoading(true);
    try {
      const response = await fetch(`/api/social/profile/${contactId}`);
      if (response.ok) {
        const data = await response.json();
        setProfile(data);
      }
    } finally {
      setLoading(false);
    }
  };
  
  const enrichProfile = async () => {
    setEnriching(true);
    try {
      const response = await fetch(`/api/social/enrich/${contactId}`, {
        method: 'POST'
      });
      if (response.ok) {
        const data = await response.json();
        setProfile(data);
      }
    } finally {
      setEnriching(false);
    }
  };
  
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    // Show toast
  };
  
  const toggleSection = (section: string) => {
    const newExpanded = new Set(expandedSections);
    if (newExpanded.has(section)) {
      newExpanded.delete(section);
    } else {
      newExpanded.add(section);
    }
    setExpandedSections(newExpanded);
  };
  
  if (loading) {
    return (
      <Card>
        <CardContent>
          <Skeleton variant="rectangular" height={200} />
        </CardContent>
      </Card>
    );
  }
  
  if (!profile) {
    return (
      <Card>
        <CardContent>
          <Alert severity="info">
            Keine Social Media Profile gefunden
          </Alert>
          <Button
            variant="contained"
            onClick={enrichProfile}
            disabled={enriching}
            sx={{ mt: 2 }}
          >
            Profile anreichern
          </Button>
        </CardContent>
      </Card>
    );
  }
  
  return (
    <Card>
      <CardContent>
        {/* Header */}
        <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
          <Box display="flex" gap={2}>
            <Avatar
              src={profile.linkedInProfileImage}
              sx={{ width: 64, height: 64 }}
            >
              {profile.contact.firstName?.[0]}{profile.contact.lastName?.[0]}
            </Avatar>
            
            <Box>
              <Typography variant="h6">
                {profile.contact.firstName} {profile.contact.lastName}
              </Typography>
              {profile.linkedInHeadline && (
                <Typography variant="body2" color="text.secondary">
                  {profile.linkedInHeadline}
                </Typography>
              )}
              
              {/* Social Links */}
              <Box display="flex" gap={1} mt={1}>
                {profile.linkedInUrl && (
                  <IconButton
                    size="small"
                    color="primary"
                    component={Link}
                    href={profile.linkedInUrl}
                    target="_blank"
                  >
                    <LinkedInIcon />
                  </IconButton>
                )}
                {profile.xingUrl && (
                  <IconButton
                    size="small"
                    color="primary"
                    component={Link}
                    href={profile.xingUrl}
                    target="_blank"
                  >
                    <XingIcon />
                  </IconButton>
                )}
              </Box>
            </Box>
          </Box>
          
          <Box display="flex" flexDirection="column" alignItems="flex-end" gap={1}>
            {/* Profile Quality */}
            <Box display="flex" alignItems="center" gap={1}>
              <Typography variant="caption" color="text.secondary">
                Profil-Qualität:
              </Typography>
              <LinearProgress
                variant="determinate"
                value={profile.profileCompleteness}
                sx={{ width: 100 }}
                color={
                  profile.profileCompleteness > 70 ? 'success' :
                  profile.profileCompleteness > 40 ? 'warning' : 'error'
                }
              />
              <Typography variant="caption">
                {profile.profileCompleteness}%
              </Typography>
            </Box>
            
            {/* Last Enriched */}
            {profile.lastEnrichedAt && (
              <Typography variant="caption" color="text.secondary">
                Aktualisiert {formatDistanceToNow(new Date(profile.lastEnrichedAt), {
                  locale: de,
                  addSuffix: true
                })}
              </Typography>
            )}
            
            <IconButton
              size="small"
              onClick={enrichProfile}
              disabled={enriching}
            >
              {enriching ? <CircularProgress size={20} /> : <RefreshIcon />}
            </IconButton>
          </Box>
        </Box>
        
        {/* Common Connections Alert */}
        {profile.commonConnections?.count > 0 && (
          <Alert
            severity="success"
            icon={<ConnectionsIcon />}
            sx={{ mb: 2 }}
          >
            {profile.commonConnections.count} gemeinsame Kontakte auf LinkedIn!
            {profile.commonConnections.topConnections?.[0] && (
              <Typography variant="caption" display="block">
                z.B. {profile.commonConnections.topConnections[0].name}
              </Typography>
            )}
          </Alert>
        )}
        
        {/* Conversation Starters */}
        {profile.conversationStarters && profile.conversationStarters.length > 0 && (
          <Box mb={2}>
            <Typography variant="subtitle2" gutterBottom>
              💡 Gesprächsaufhänger
            </Typography>
            <Box display="flex" gap={1} flexWrap="wrap">
              {profile.conversationStarters.map((starter, idx) => (
                <Chip
                  key={idx}
                  label={starter}
                  icon={<ChatIcon />}
                  onClick={() => {
                    copyToClipboard(starter);
                    onConversationStarter?.(starter);
                  }}
                  clickable
                />
              ))}
            </Box>
          </Box>
        )}
        
        {/* Tabs */}
        <Tabs value={activeTab} onChange={(_, v) => setActiveTab(v)}>
          <Tab label="Übersicht" />
          <Tab label="Berufserfahrung" />
          <Tab label="Skills & Bildung" />
          <Tab label="Aktivität" />
          <Tab label="Insights" />
        </Tabs>
        
        <Box sx={{ mt: 2 }}>
          {/* Overview Tab */}
          {activeTab === 0 && (
            <Box>
              {profile.linkedInSummary && (
                <Box mb={2}>
                  <Typography variant="subtitle2" gutterBottom>
                    LinkedIn Zusammenfassung
                  </Typography>
                  <Typography
                    variant="body2"
                    sx={{
                      display: '-webkit-box',
                      WebkitLineClamp: expandedSections.has('summary') ? 'unset' : 3,
                      WebkitBoxOrient: 'vertical',
                      overflow: 'hidden'
                    }}
                  >
                    {profile.linkedInSummary}
                  </Typography>
                  {profile.linkedInSummary.length > 200 && (
                    <Button
                      size="small"
                      onClick={() => toggleSection('summary')}
                    >
                      {expandedSections.has('summary') ? 'Weniger' : 'Mehr'}
                    </Button>
                  )}
                </Box>
              )}
              
              {/* Quick Stats */}
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Kontakte
                    </Typography>
                    <Typography variant="h6">
                      {profile.linkedInConnections || '—'}
                    </Typography>
                  </Box>
                </Grid>
                <Grid item xs={6}>
                  <Box>
                    <Typography variant="caption" color="text.secondary">
                      Skills
                    </Typography>
                    <Typography variant="h6">
                      {profile.skills.length}
                    </Typography>
                  </Box>
                </Grid>
              </Grid>
            </Box>
          )}
          
          {/* Employment Tab */}
          {activeTab === 1 && (
            <List>
              {profile.employmentHistory.map((job, idx) => (
                <React.Fragment key={idx}>
                  <ListItem>
                    <ListItemIcon>
                      <WorkIcon color={job.isCurrent ? 'primary' : 'disabled'} />
                    </ListItemIcon>
                    <ListItemText
                      primary={
                        <Box>
                          <Typography variant="subtitle2">
                            {job.position}
                            {job.isCurrent && (
                              <Chip
                                label="Aktuell"
                                size="small"
                                color="primary"
                                sx={{ ml: 1 }}
                              />
                            )}
                          </Typography>
                          <Typography variant="body2">
                            {job.company}
                          </Typography>
                        </Box>
                      }
                      secondary={
                        <Box>
                          <Typography variant="caption">
                            {job.startDate && format(new Date(job.startDate), 'MMM yyyy')} - 
                            {job.endDate ? format(new Date(job.endDate), 'MMM yyyy') : 'Heute'}
                            {job.location && ` • ${job.location}`}
                          </Typography>
                          {job.description && (
                            <Typography variant="body2" sx={{ mt: 1 }}>
                              {job.description}
                            </Typography>
                          )}
                        </Box>
                      }
                    />
                  </ListItem>
                  {idx < profile.employmentHistory.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          )}
          
          {/* Skills & Education Tab */}
          {activeTab === 2 && (
            <Box>
              {/* Skills */}
              {profile.skills.length > 0 && (
                <Box mb={3}>
                  <Typography variant="subtitle2" gutterBottom>
                    Skills ({profile.skills.length})
                  </Typography>
                  <Box display="flex" gap={1} flexWrap="wrap">
                    {profile.skills.map(skill => (
                      <Chip
                        key={skill}
                        label={skill}
                        size="small"
                        variant="outlined"
                      />
                    ))}
                  </Box>
                </Box>
              )}
              
              {/* Education */}
              {profile.educationHistory.length > 0 && (
                <Box>
                  <Typography variant="subtitle2" gutterBottom>
                    Bildung
                  </Typography>
                  <List>
                    {profile.educationHistory.map((edu, idx) => (
                      <ListItem key={idx}>
                        <ListItemIcon>
                          <SchoolIcon />
                        </ListItemIcon>
                        <ListItemText
                          primary={edu.degree}
                          secondary={
                            <Box>
                              <Typography variant="body2">
                                {edu.institution}
                              </Typography>
                              <Typography variant="caption">
                                {edu.fieldOfStudy} • 
                                {edu.startDate && format(new Date(edu.startDate), 'yyyy')} - 
                                {edu.endDate && format(new Date(edu.endDate), 'yyyy')}
                              </Typography>
                            </Box>
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              )}
            </Box>
          )}
          
          {/* Activity Tab */}
          {activeTab === 3 && (
            <List>
              {profile.recentActivities.slice(0, 5).map((activity, idx) => (
                <ListItem key={idx}>
                  <ListItemIcon>
                    <ActivityIcon />
                  </ListItemIcon>
                  <ListItemText
                    primary={activity.title}
                    secondary={
                      <Box>
                        <Typography variant="caption">
                          {activity.type} • {formatDistanceToNow(new Date(activity.activityDate), {
                            locale: de,
                            addSuffix: true
                          })}
                        </Typography>
                        {activity.engagementCount && (
                          <Typography variant="caption" display="block">
                            {activity.engagementCount} Interaktionen
                          </Typography>
                        )}
                      </Box>
                    }
                  />
                  {activity.url && (
                    <ListItemSecondaryAction>
                      <IconButton
                        edge="end"
                        component={Link}
                        href={activity.url}
                        target="_blank"
                      >
                        <OpenIcon />
                      </IconButton>
                    </ListItemSecondaryAction>
                  )}
                </ListItem>
              ))}
            </List>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Backend Setup (45 Min)
- [ ] Social Profile Entity & Relations
- [ ] LinkedIn API Integration
- [ ] XING API Integration (optional)
- [ ] Migration für social_profiles

### Phase 2: Enrichment Logic (45 Min)
- [ ] SocialEnrichmentService
- [ ] API Rate Limiting
- [ ] Common Connections Finder
- [ ] Conversation Starter Generator

### Phase 3: Frontend Components (60 Min)
- [ ] SocialProfileCard Component
- [ ] LinkedIn Insights Widget
- [ ] Common Connections Display
- [ ] Activity Timeline

### Phase 4: Intelligence Features (30 Min)
- [ ] Job Change Detection
- [ ] Shared Interests Finder
- [ ] Best Contact Time Analysis
- [ ] Profile Quality Scoring

### Phase 5: Privacy & Compliance (30 Min)
- [ ] DSGVO Compliance
- [ ] Opt-in/Opt-out Management
- [ ] Data Retention Policies
- [ ] Audit Logging

## 🔗 INTEGRATION POINTS

1. **Contact Management** - Profile anzeigen
2. **Communication History** - Social Activities
3. **Smart Suggestions** - Basierend auf Insights
4. **Relationship Warmth** - Social Engagement

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **API Rate Limits überschreiten**
   → Lösung: Smart Caching & Batching

2. **Veraltete Daten**
   → Lösung: Regelmäßige Updates mit TTL

3. **Privacy Verletzungen**
   → Lösung: Explizite Opt-in Prozesse

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 210 Minuten  
**Nächstes Dokument:** [→ AI Data Enrichment](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/AI_DATA_ENRICHMENT.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Social Intelligence = Vertriebsvorsprung! 📱✨**