# 🔗 Cross-Service Integration Implementation Plan

**📊 Plan Status:** 🟢 Production-Ready
**🎯 Owner:** Integration Team + Cross-Module Excellence Team
**⏱️ Timeline:** Woche 5-6 (3-4h Implementation)
**🔧 Effort:** S (Small - Event-Routing + API-Integration + Health-Monitoring)

## 🎯 Executive Summary (für Claude)

**Mission:** Seamless Integration des Settings-Moduls mit allen 8 FreshPlan-Modulen via Event-Routing und API-Standards

**Problem:** Settings-Module muss real-time mit Cockpit, Kundenmanagement, Neukundengewinnung, Auswertungen und Communication integriert werden

**Solution:** Event-Bus-Integration + Cross-Module-APIs + Health-Monitoring + End-to-End-Testing für alle 8 Module

**Timeline:** 3-4h von Event-Routing bis Production-Integration mit Health-Monitoring

**Impact:** Real-time Settings-Updates zu allen Modulen + Cross-Module-Health + Seamless User-Experience

## 📋 Context & Dependencies

### Current State:
- ✅ **Settings Core Engine:** Event-System operational (FROM PLAN 01)
- ✅ **B2B-Food Business Logic:** Business-Events verfügbar (FROM PLAN 02)
- ✅ **Frontend UX-Excellence:** User-Interaction-Events ready (FROM PLAN 03)
- ✅ **Monitoring Operations:** Health-Checks + SLO-Monitoring active (FROM PLAN 04)

### Target State:
- 🎯 **Event-Bus-Integration:** Real-time Settings-Events zu allen 8 Modulen
- 🎯 **Cross-Module-APIs:** Unified Settings-Access für externe Module
- 🎯 **Health-Integration:** Settings-Health in Module-Dashboards integriert
- 🎯 **End-to-End-Flows:** Complete User-Journeys über Module-Grenzen hinweg
- 🎯 **Event-Schema-Registry:** Versionierte Event-Contracts für alle Module

### Dependencies:
- **Event-System:** PostgreSQL LISTEN/NOTIFY → Event-Bus ready
- **Module APIs:** Alle 8 Module haben Integration-Endpoints
- **Health-Monitoring:** Cross-Module-Health-Checks operational
- **Schema-Registry:** Event-Schemas für Module 01-08 available

## 🛠️ Implementation Phases (3 Phasen = 3-4h Gesamt)

### Phase 1: Event-Bus-Integration + Cross-Module-Events (1-2h)

**Goal:** Real-time Settings-Events zu allen 8 FreshPlan-Modulen mit Event-Schema-Registry

**Actions:**
1. **Settings-Event-Publisher:**
   ```java
   @Component
   public class SettingsEventPublisher {
       private final EventBus eventBus;
       private final SchemaRegistry schemaRegistry;

       @EventListener
       public void onSettingChanged(SettingChangedEvent event) {
           CrossModuleSettingsEvent crossModuleEvent = CrossModuleSettingsEvent.builder()
               .settingKey(event.getKey())
               .scopeType(event.getScopeType())
               .scopeId(event.getScopeId())
               .newValue(event.getNewValue())
               .territory(event.getTerritory())
               .contactRole(event.getContactRole())
               .timestamp(Instant.now())
               .eventVersion("1.0")
               .build();

           // Schema-Validation vor Event-Publishing
           schemaRegistry.validate("settings.changed.v1", crossModuleEvent);

           // Event zu allen interessierten Modulen
           eventBus.publish("settings.changed", crossModuleEvent);
       }

       public void publishUserSettingsSync(String userId, Map<String, Object> settings) {
           UserSettingsSyncEvent event = UserSettingsSyncEvent.builder()
               .userId(userId)
               .settings(settings)
               .syncTimestamp(Instant.now())
               .build();

           eventBus.publish("user.settings.sync", event);
       }
   }
   ```

2. **Cross-Module-Event-Schemas:**
   ```json
   {
     "settings.changed.v1": {
       "type": "object",
       "properties": {
         "settingKey": {"type": "string"},
         "scopeType": {"enum": ["GLOBAL", "TENANT", "TERRITORY", "ACCOUNT", "CONTACT_ROLE", "CONTACT", "USER"]},
         "scopeId": {"type": "string"},
         "newValue": {"type": "object"},
         "territory": {"type": "string"},
         "contactRole": {"enum": ["CHEF", "BUYER", "ADMIN"]},
         "timestamp": {"type": "string", "format": "date-time"},
         "eventVersion": {"type": "string"}
       },
       "required": ["settingKey", "scopeType", "scopeId", "newValue", "timestamp"]
     },
     "user.settings.sync.v1": {
       "type": "object",
       "properties": {
         "userId": {"type": "string"},
         "settings": {"type": "object"},
         "syncTimestamp": {"type": "string", "format": "date-time"}
       }
     }
   }
   ```

3. **Module-Integration-Events:**
   ```java
   // Cockpit-Integration (Module 01)
   @EventListener
   public void onCockpitSettingsRequest(CockpitSettingsRequestEvent event) {
       List<DashboardSettings> settings = settingsService.getDashboardSettings(
           event.getUserId(), event.getTerritory()
       );
       eventBus.publish("cockpit.settings.response", settings);
   }

   // Neukundengewinnung-Integration (Module 02)
   @EventListener
   public void onLeadSettingsUpdate(LeadSettingsUpdateEvent event) {
       UserLeadSettings userSettings = settingsService.getUserLeadSettings(
           event.getContactId(), event.getTerritory()
       );
       eventBus.publish("lead.settings.updated", userSettings);
   }

   // Communication-Integration (Module 05)
   @EventListener
   public void onNotificationPreferencesChange(NotificationSettingsEvent event) {
       NotificationPreferences prefs = settingsService.getNotificationPreferences(
           event.getUserId()
       );
       eventBus.publish("notification.preferences.updated", prefs);
   }
   ```

**Success Criteria:** Event-Bus-Integration operational + Cross-Module-Events zu allen 8 Modulen aktiv

### Phase 2: Cross-Module-APIs + Health-Integration (1h)

**Goal:** Unified Settings-APIs für externe Module mit Health-Monitoring Integration

**Actions:**
1. **Cross-Module-Settings-API:**
   ```java
   @RestController
   @RequestMapping("/api/cross-module/settings")
   public class CrossModuleSettingsController {

       @GetMapping("/cockpit/{userId}")
       public ResponseEntity<CockpitSettings> getCockpitSettings(
           @PathVariable String userId,
           @RequestParam String territory
       ) {
           CockpitSettings settings = settingsService.getCockpitSettings(userId, territory);
           return ResponseEntity.ok()
               .eTag(settings.getEtag())
               .cacheControl(CacheControl.maxAge(Duration.ofMinutes(5)))
               .body(settings);
       }

       @GetMapping("/lead-management/{contactId}")
       public ResponseEntity<UserLeadSettings> getLeadSettings(
           @PathVariable String contactId,
           @RequestParam String territory
       ) {
           UserLeadSettings settings = settingsService.getUserLeadSettings(contactId, territory);
           return ResponseEntity.ok(settings);
       }

       @PostMapping("/notification-preferences")
       public ResponseEntity<NotificationPreferences> updateNotificationPreferences(
           @RequestBody @Valid NotificationPreferencesRequest request
       ) {
           NotificationPreferences preferences = settingsService.updateNotificationPreferences(
               request.getUserId(), request.getPreferences()
           );
           return ResponseEntity.ok(preferences);
       }

       @GetMapping("/bulk/{moduleId}")
       public ResponseEntity<BulkSettingsResponse> getBulkSettings(
           @PathVariable String moduleId,
           @RequestParam List<String> settingKeys,
           @RequestParam String scopeContext
       ) {
           BulkSettingsResponse response = settingsService.getBulkSettings(
               moduleId, settingKeys, ScopeContext.parse(scopeContext)
           );
           return ResponseEntity.ok(response);
       }
   }
   ```

2. **Health-Integration mit Cross-Module-Monitoring:**
   ```java
   @Component
   public class CrossModuleHealthIntegration {

       @EventListener
       public void onModuleHealthCheck(ModuleHealthCheckEvent event) {
           SettingsHealthStatus status = SettingsHealthStatus.builder()
               .moduleId("settings")
               .status(getSettingsHealthStatus())
               .dependencies(List.of(
                   HealthDependency.of("database", databaseHealth()),
                   HealthDependency.of("cache", cacheHealth()),
                   HealthDependency.of("event-bus", eventBusHealth())
               ))
               .metrics(Map.of(
                   "avg_response_time", metricsService.getAvgResponseTime(),
                   "cache_hit_rate", settingsCache.getHitRate(),
                   "active_settings_count", settingsRepository.countActiveSettings()
               ))
               .build();

           eventBus.publish("module.health.response", status);
       }

       @Scheduled(fixedRate = 30000) // Every 30 seconds
       public void publishSettingsHealth() {
           SettingsHealthBroadcast broadcast = SettingsHealthBroadcast.builder()
               .timestamp(Instant.now())
               .sloCompliance(sloMonitor.getCurrentCompliance())
               .errorRate(metricsService.getErrorRate())
               .throughput(metricsService.getCurrentThroughput())
               .build();

           eventBus.publish("settings.health.broadcast", broadcast);
       }
   }
   ```

3. **Cross-Module-Error-Handling:**
   ```java
   @Component
   public class CrossModuleErrorHandler {

       @EventListener
       public void onCrossModuleError(CrossModuleErrorEvent event) {
           String errorContext = String.format(
               "Module %s failed to access settings %s: %s",
               event.getSourceModule(),
               event.getSettingKey(),
               event.getErrorMessage()
           );

           logger.error("Cross-module settings error: {}", errorContext);

           // Fallback-Settings bereitstellen
           if (event.isCriticalSetting()) {
               Object fallbackValue = settingsService.getFallbackValue(event.getSettingKey());
               eventBus.publish("settings.fallback.provided",
                   FallbackSettingEvent.of(event.getSettingKey(), fallbackValue));
           }

           // Alert für Operations-Team
           alertService.sendAlert(
               AlertLevel.WARNING,
               "Cross-Module Settings Error",
               errorContext
           );
       }
   }
   ```

**Success Criteria:** Cross-Module-APIs operational + Health-Integration active + Error-Handling functional

### Phase 3: End-to-End-Testing + Production-Integration (1h)

**Goal:** Complete End-to-End-Testing für alle 8 Module mit Production-Integration-Validation

**Actions:**
1. **End-to-End-Integration-Tests:**
   ```java
   @SpringBootTest
   @TestMethodOrder(OrderAnnotation.class)
   class CrossModuleIntegrationTest {

       @Test
       @Order(1)
       void testCockpitSettingsIntegration() {
           // User öffnet Cockpit → Settings werden geladen
           String userId = "test-user-123";
           String territory = "GERMANY";

           CockpitSettings settings = settingsService.getCockpitSettings(userId, territory);

           assertThat(settings).isNotNull();
           assertThat(settings.getDashboardLayout()).isNotNull();
           assertThat(settings.getTerritory()).isEqualTo(territory);
       }

       @Test
       @Order(2)
       void testLeadManagementSettingsFlow() {
           // Lead-Management benötigt UserLeadSettings
           String contactId = "contact-456";
           String territory = "SWITZERLAND";

           UserLeadSettings leadSettings = settingsService.getUserLeadSettings(contactId, territory);

           assertThat(leadSettings).isNotNull();
           assertThat(leadSettings.getProvisionPercentage()).isGreaterThan(0);
           assertThat(leadSettings.getTerritory()).isEqualTo(territory);
       }

       @Test
       @Order(3)
       void testSettingsEventPropagation() {
           // Settings-Änderung → Event zu allen Modulen
           String settingKey = "notification.email.frequency";
           String newValue = "DAILY";

           settingsService.updateSetting(settingKey, newValue, testScopeContext);

           // Verify Event wurde zu allen Modulen gesendet
           await().atMost(Duration.ofSeconds(5))
                   .until(() -> eventCaptor.getEvents().size() >= 3);

           List<CrossModuleSettingsEvent> events = eventCaptor.getEvents();
           assertThat(events).hasSize(3);
           assertThat(events).allMatch(e -> e.getSettingKey().equals(settingKey));
       }
   }
   ```

2. **Production-Integration-Monitoring:**
   ```java
   @Component
   public class ProductionIntegrationMonitor {

       @EventListener
       public void onProductionDeployment(ProductionDeploymentEvent event) {
           // Validate Settings-Integration mit allen Modulen
           List<ModuleIntegrationCheck> checks = List.of(
               checkCockpitIntegration(),
               checkLeadManagementIntegration(),
               checkCustomerManagementIntegration(),
               checkAnalyticsIntegration(),
               checkCommunicationIntegration()
           );

           boolean allHealthy = checks.stream().allMatch(ModuleIntegrationCheck::isHealthy);

           if (!allHealthy) {
               alertService.sendCriticalAlert(
                   "Settings Cross-Module Integration Failed",
                   "One or more modules failed Settings integration post-deployment"
               );
           }

           IntegrationHealthReport report = IntegrationHealthReport.builder()
               .timestamp(Instant.now())
               .moduleChecks(checks)
               .overallStatus(allHealthy ? "HEALTHY" : "DEGRADED")
               .build();

           eventBus.publish("integration.health.report", report);
       }

       private ModuleIntegrationCheck checkCockpitIntegration() {
           try {
               cockpitService.testSettingsAccess();
               return ModuleIntegrationCheck.healthy("cockpit");
           } catch (Exception e) {
               return ModuleIntegrationCheck.unhealthy("cockpit", e.getMessage());
           }
       }
   }
   ```

3. **Integration-Performance-Testing:**
   ```java
   @Test
   void testCrossModulePerformance() {
       // Load-Test: 100 gleichzeitige Cross-Module-Requests
       int concurrentRequests = 100;
       CountDownLatch latch = new CountDownLatch(concurrentRequests);
       List<CompletableFuture<Long>> futures = new ArrayList<>();

       for (int i = 0; i < concurrentRequests; i++) {
           CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
               long start = System.currentTimeMillis();
               settingsService.getCockpitSettings("user-" + Thread.currentThread().getId(), "GERMANY");
               long end = System.currentTimeMillis();
               latch.countDown();
               return end - start;
           });
           futures.add(future);
       }

       // Alle Requests abwarten
       latch.await(10, TimeUnit.SECONDS);

       // Performance-Validation
       List<Long> responseTimes = futures.stream()
           .map(CompletableFuture::join)
           .collect(toList());

       double avgResponseTime = responseTimes.stream()
           .mapToLong(Long::longValue)
           .average()
           .orElse(0);

       double p95ResponseTime = responseTimes.stream()
           .sorted(Comparator.reverseOrder())
           .skip((int) (responseTimes.size() * 0.05))
           .findFirst()
           .orElse(0L);

       // SLO-Validation: <50ms P95
       assertThat(p95ResponseTime).isLessThan(50);
       assertThat(avgResponseTime).isLessThan(25);
   }
   ```

**Success Criteria:** End-to-End-Tests für alle 8 Module passing + Production-Integration validated

## ✅ Success Metrics

### **Immediate Success (3-4h):**
1. **Event-Bus-Integration:** Real-time Settings-Events zu allen 8 FreshPlan-Modulen operational
2. **Cross-Module-APIs:** Unified Settings-Access für externe Module functional
3. **Health-Integration:** Settings-Health in alle Module-Dashboards integriert
4. **End-to-End-Flows:** Complete User-Journeys über Module-Grenzen hinweg validated
5. **Production-Integration:** All 8 modules successfully accessing Settings via APIs

### **Business Success (1-2 Wochen):**
1. **Seamless-User-Experience:** Settings-Änderungen propagieren real-time zu allen Modulen
2. **Cross-Module-Insights:** Settings-Usage-Patterns über alle 8 Module verfügbar
3. **Integration-Reliability:** 99.9% Cross-Module-API-Availability achieved
4. **Performance-Excellence:** <50ms Cross-Module-Settings-Access maintained

### **Technical Excellence:**
- **Event-Schema-Compliance:** 100% Schema-validated Events zwischen Modulen
- **API-Performance:** <50ms P95 Cross-Module-API-Calls sustained
- **Health-Monitoring:** Real-time Settings-Health in allen Module-Dashboards
- **Integration-Testing:** End-to-End-Tests für alle 8 Module automated

## 🔗 Related Documentation

### **Foundation Dependencies:**
- [Settings Core Engine Plan](01_SETTINGS_CORE_ENGINE_PLAN.md) - Event-System + Core-APIs
- [B2B-Food Business Logic Plan](02_B2B_FOOD_BUSINESS_LOGIC_PLAN.md) - Business-Events + Territory-Logic
- [Frontend UX-Excellence Plan](03_FRONTEND_UX_EXCELLENCE_PLAN.md) - User-Events + Frontend-Integration
- [Monitoring Operations Plan](04_MONITORING_OPERATIONS_PLAN.md) - Health-Monitoring + SLO-Tracking

### **Cross-Module Integration:**
- [Module 01 Cockpit](../../01_cockpit/) - Dashboard-Settings Integration
- [Module 02 Neukundengewinnung](../../02_neukundengewinnung/) - UserLeadSettings Integration
- [Module 03 Kundenmanagement](../../03_kundenmanagement/) - Customer-Preferences Integration
- [Module 04 Auswertungen](../../04_auswertungen/) - Analytics-Settings Integration
- [Module 05 Kommunikation](../../05_kommunikation/) - Notification-Preferences Integration

### **Technical Standards:**
- [Event-Bus Architecture](../../../grundlagen/EVENT_BUS_ARCHITECTURE.md) - Cross-Module-Event-Patterns
- [API Integration Standards](../../../grundlagen/API_INTEGRATION_STANDARDS.md) - Cross-Module-API-Guidelines

## 🤖 Claude Handover Section

### **Current Priority (für neue Claude):**
```bash
# Phase 1: Event-Bus-Integration + Cross-Module-Events
cd implementation-plans/
→ 05_CROSS_SERVICE_INTEGRATION_PLAN.md (CURRENT)

# Start: Event-Bus-Integration + Schema-Registry
cd ../artefakte/integration/

# Success: Real-time Settings-Events zu allen 8 Modulen operational
# Next: Complete Settings-Module Production-Deployment
```

### **Context für neue Claude:**
- **Cross-Service Integration:** Real-time Integration mit allen 8 FreshPlan-Modulen
- **Timeline:** 3-4h für Complete Cross-Module-Integration + End-to-End-Testing
- **Dependencies:** Alle 4 vorherigen Pläne operational (Settings Core + Business Logic + Frontend + Monitoring)
- **Business-Value:** Seamless Settings-Experience über alle Module + Real-time-Updates

### **Key Success-Factors:**
- **Event-Schema-Registry:** Versionierte Event-Contracts für alle Module
- **Cross-Module-Performance:** <50ms API-Calls zwischen Modulen
- **Health-Integration:** Settings-Health in allen Module-Dashboards sichtbar
- **End-to-End-Validation:** Complete User-Journeys über alle 8 Module getestet

**🚀 Ready für Seamless Cross-Module-Integration des Complete Settings-Systems!**