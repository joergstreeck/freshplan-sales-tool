# Backend Dependencies - Modul 07 Hilfe & Support

**Zweck:** Maven Dependencies für Help & Support Backend-Komponenten
**Status:** Production-Ready
**Letztes Update:** 2025-09-20

## 🚀 Required Maven Dependencies

### Core Quarkus Extensions
```xml
<!-- Already included in project -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-orm-panache</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-resteasy-reactive-jackson</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-security-jpa</artifactId>
</dependency>
```

### Additional Required Dependencies
```xml
<!-- Micrometer for HelpMetrics.java -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>

<!-- JSON Schema Validation (if not already included) -->
<dependency>
    <groupId>com.networknt</groupId>
    <artifactId>json-schema-validator</artifactId>
    <version>1.0.87</version>
</dependency>

<!-- For advanced JSONB operations (if needed) -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jsonb</artifactId>
</dependency>
```

## 🔧 Configuration Properties

### Required application.properties additions:
```properties
# Help & Support Module
help.nudge.enabled=true
help.struggle.detection.enabled=true

# Metrics for Help KPIs
quarkus.micrometer.export.prometheus.enabled=true

# Database connection (if not already configured)
quarkus.datasource.jdbc.max-size=20
```

## 📁 File Integration Guide

### 1. Java Source Files Location:
```
src/main/java/de/freshplan/help/
├── api/
│   ├── HelpResource.java
│   └── GuidedResource.java
├── domain/
│   ├── HelpArticleDTO.java
│   ├── SuggestionDTO.java
│   ├── FollowUpPlanRequest.java
│   ├── FollowUpPlanResponse.java
│   ├── RoiQuickCheckRequest.java
│   └── RoiQuickCheckResponse.java
├── repo/
│   └── HelpRepository.java
├── service/
│   └── HelpService.java
└── metrics/
    └── HelpMetrics.java
```

### 2. Migration Number Setup:
```bash
# WICHTIG: Vor Deployment die korrekte Migrationsnummer ermitteln
./scripts/get-next-migration.sh

# VXXX__help_core.sql in VYYY__help_core.sql umbenennen
# mit der aktuell nächsten freien Nummer
```

### 3. Integration with Existing Security:
```java
// HelpResource.java already includes:
@Inject ScopeContext scope;  // Requires existing ABAC implementation
```

### 4. Settings Service Integration:
```java
// Replace hardcoded values in HelpResource.java:
@Inject SettingsService settingsService;

// Then in suggest() method:
double minConfidence = settingsService.getDouble("help.nudge.confidence.min", 0.7);
int base = settingsService.getInt("help.nudge.budget.session.base", 2);
int perHour = settingsService.getInt("help.nudge.budget.session.perHour", 1);
int max = settingsService.getInt("help.nudge.budget.session.max", 5);
```

## 🔗 External API Integration

### Activities API (for GuidedResource.java):
```java
// Replace mock in HelpService.planFollowUp():
@RestClient ActivityApiClient activityApi;

// Create real activities:
var activities = List.of(
    new Activity(req.accountId, "FOLLOW_UP", now.plusDays(3)),
    new Activity(req.accountId, "FOLLOW_UP", now.plusDays(7))
);
var result = activityApi.createBulk(activities);
```

## ⚡ Performance Considerations

### Database Indexes (already in V226 migration):
```sql
-- Optimized for help suggestion queries
CREATE INDEX ix_help_article_keywords ON help_article USING GIN (keywords);
CREATE INDEX ix_help_article_module ON help_article(module);
CREATE INDEX ix_help_article_persona ON help_article(persona);
```

### Memory Configuration:
```properties
# For in-memory session budgets (HelpService.java)
quarkus.cache.caffeine.expire-after-write=8H
```

## 🧪 Testing Integration

### Required Test Dependencies:
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
```

### Test Configuration:
```properties
# src/test/resources/application.properties
%test.quarkus.datasource.db-kind=h2
%test.quarkus.datasource.jdbc.url=jdbc:h2:mem:test
```

## 🚨 Critical Notes

1. **Migration Number:** VXXX muss vor Deployment über Scripts ermittelt werden
2. **ScopeContext Dependency:** Requires existing ABAC/Security implementation
3. **Settings Service:** CAR parameters need Settings Service from Modul 06
4. **Database Migration:** VXXX must be deployed before service startup
5. **Activities Integration:** Optional - can start with mocks, upgrade later

## 📊 Monitoring Setup

### Prometheus Metrics (automatic with HelpMetrics.java):
- `help_nudges_shown_total`
- `help_nudges_accepted_total`
- `help_nudges_false_positive_total`
- `help_time_to_help_seconds`
- `help_self_serve_total`

### Grafana Dashboard:
Import `../monitoring/help_dashboard.json` for instant KPI visibility.

---

**Ready for copy-paste integration into existing Quarkus project! 🚀**