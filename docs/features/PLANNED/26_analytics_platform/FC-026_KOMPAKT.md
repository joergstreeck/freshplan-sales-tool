# 📊 FC-026 ANALYTICS & USAGE TRACKING (KOMPAKT)

**Erstellt:** 18.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🔀 FULLSTACK  
**Priorität:** MEDIUM - Insights  
**Geschätzt:** 2 Tage  

---

## 🧠 WAS WIR BAUEN

**Problem:** Blind für User-Verhalten  
**Lösung:** Analytics von Anfang an  
**Value:** Datengetriebene Entscheidungen  

> **Business Case:** Verstehen was funktioniert = besseres Produkt

### 🎯 Analytics Stack:
- **PostHog/Mixpanel:** Product Analytics
- **Custom Metrics:** Business KPIs
- **Feature Flags:** A/B Testing ready
- **Performance:** Core Web Vitals

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **PostHog Setup:**
```typescript
// main.tsx
import posthog from 'posthog-js'

posthog.init('YOUR_API_KEY', {
  api_host: 'https://eu.posthog.com',
  person_profiles: 'identified_only',
  capture_pageview: false // Manual control
})
```

### 2. **Analytics Hook:**
```typescript
export const useAnalytics = () => {
  const track = (event: string, properties?: any) => {
    posthog.capture(event, {
      ...properties,
      timestamp: new Date().toISOString()
    })
  }
  
  const identify = (userId: string, traits?: any) => {
    posthog.identify(userId, traits)
  }
  
  return { track, identify }
}
```

### 3. **Event Tracking:**
```typescript
// Track key actions
track('opportunity_created', {
  stage: 'lead',
  value: opportunity.value
})

track('feature_used', {
  feature: 'command_palette',
  shortcut: 'cmd+k'
})
```

---

## 📈 KEY METRICS

### Business Metrics:
```typescript
// Backend aggregation
@Scheduled(every = "1h")
void calculateMetrics() {
  metrics.gauge("active_users_daily", 
    getActiveUsers(DAILY));
  
  metrics.gauge("opportunities_created",
    getOpportunitiesCreated(TODAY));
    
  metrics.gauge("conversion_rate",
    getConversionRate(WEEK));
}
```

### Feature Adoption:
```typescript
interface FeatureUsage {
  feature: string;
  dailyActiveUsers: number;
  totalEvents: number;
  adoptionRate: number; // % of all users
  retention7d: number;   // Still using after 7d
}

// Track everything
const TRACKED_FEATURES = [
  'command_palette',
  'quick_create',
  'mobile_app',
  'bulk_actions',
  'smart_search'
];
```

---

## 🎨 ANALYTICS DASHBOARD

```
┌────────────────────────────────────────┐
│ 📊 Usage Analytics                     │
├────────────────────────────────────────┤
│                                        │
│ Daily Active Users     Feature Usage   │
│ ┌──────────────┐      ┌─────────────┐ │
│ │    📈 156    │      │ Quick Create │ │
│ │    +12%      │      │ ████████ 89% │ │
│ └──────────────┘      │ Cmd Palette  │ │
│                       │ ██████ 67%   │ │
│ Top Features Today    │ Smart Search │ │
│ 1. Quick Create (234) │ ████ 45%     │ │
│ 2. Pipeline (189)     └─────────────┘ │
│ 3. Mobile (156)                        │
│                                        │
│ Performance           Errors (24h)     │
│ • API p95: 145ms ✅   • 12 total      │
│ • LCP: 2.1s ✅       • 0 critical    │
│ • Bundle: 198KB ✅    • 3 warnings    │
└────────────────────────────────────────┘
```

---

## 🔧 IMPLEMENTATION

### Frontend Tracking:
```typescript
// Auto-track route changes
router.beforeEach((to, from) => {
  track('page_view', {
    path: to.path,
    referrer: from.path
  })
})

// Component usage
<Button 
  onClick={() => {
    track('button_clicked', {
      button: 'create_opportunity',
      location: 'header'
    })
    createOpportunity()
  }}
>
```

### Backend Metrics:
```java
@ApplicationScoped
public class MetricsService {
    
    @Inject
    MeterRegistry registry;
    
    public void trackApiCall(String endpoint) {
        registry.counter("api.calls",
            "endpoint", endpoint
        ).increment();
    }
    
    @Timed("api.response.time")
    public Response handleRequest() {
        // Automatic timing
    }
}
```

### Feature Flags:
```typescript
// Progressive rollout
if (posthog.isFeatureEnabled('new_dashboard')) {
  return <NewDashboard />
} else {
  return <OldDashboard />
}

// A/B Testing
const variant = posthog.getFeatureFlag('cta_text')
<Button>{variant || 'Default Text'}</Button>
```

---

## 📊 CUSTOM EVENTS

### Sales Events:
```typescript
// Opportunity lifecycle
'opportunity_created'
'opportunity_stage_changed'
'opportunity_won'
'opportunity_lost'

// User journey
'onboarding_started'
'onboarding_completed'
'first_customer_created'
'first_opportunity_won'

// Feature discovery
'feature_discovered'
'feature_first_use'
'feature_regular_use' // 5+ times
```

---

## 🚨 PRIVACY

### DSGVO-konform:
```typescript
// User consent required
if (hasAnalyticsConsent()) {
  posthog.opt_in_capturing()
} else {
  posthog.opt_out_capturing()
}

// Anonymize IPs
posthog.set_config({
  ip: false,
  mask_all_text: true
})
```

---

## 📞 NÄCHSTE SCHRITTE

1. **PostHog Account** + Setup
2. **Analytics Hook** implementieren
3. **Key Events** definieren
4. **Dashboard** bauen
5. **Feature Flags** Setup
6. **Privacy Banner** DSGVO

**WICHTIG:** Keine PII in Events!