# 📋 Event-Schemas Integration Plan

**📊 Plan Status:** 🟢 Production-Ready (External AI Validated 9.9/10)
**🎯 Owner:** Integration Team + Domain-Experts
**⏱️ Timeline:** Tag 3-4 (4-5 Stunden aktive Implementation)
**🔧 Effort:** M (Medium - JSON-Schema-Integration mit CI-Pipeline + Cross-Module-Standards)

## 🎯 Executive Summary (für Claude)

**Mission:** CloudEvents 1.0 + B2B-Food-Domain-Events production-ready integrieren mit JSON-Schema-Validation, CI-Pipeline, und Cross-Module-Standards

**Problem:** FreshPlan 8-Module-Ecosystem produziert Events ohne standardisierte Schemas - führt zu Runtime-Errors, Breaking-Changes, und schwierige Cross-Module-Integration

**Solution:** CloudEvents 1.0 Envelope + 6 B2B-Food-Domain-Event-Schemas (sample.status.changed, credit.checked, trial.phase.*, product.feedback.recorded) mit CI-Schema-Validation + Breaking-Change-Protection

**Timeline:** 4-5 Stunden von Schema-Validation bis Production-Event-Publishing mit kompletter CI-Integration

**Impact:** Alle 8 Module können sofort standardisierte Events produzieren/konsumieren + Foundation für Event-Bus-Migration vorbereitet

## 📋 Context & Dependencies

### Current State:
- ✅ **Event-Schemas:** 6 JSON-Schema-Files (External AI erstellt + CloudEvents 1.0 compliant)
- ✅ **PostgreSQL LISTEN/NOTIFY:** Funktional für Event-Distribution
- ✅ **EVENT_CATALOG.md:** Basis-Struktur für Domain-Events vorhanden
- ✅ **CI-Pipeline:** Maven/GitHub-Actions ready für Schema-Validation-Integration
- ✅ **Domain-Expertise:** B2B-Food-Events von External AI analysiert + validiert

### Target State:
- 🎯 **Schema-Validation:** CI-Pipeline validiert alle Events gegen JSON-Schemas
- 🎯 **CloudEvents-Compliance:** Alle Events folgen CloudEvents 1.0 Standard + FreshPlan-Extensions
- 🎯 **Breaking-Change-Protection:** CI blockt Breaking-Schema-Changes + enforced Additive-Evolution
- 🎯 **Cross-Module-Standards:** Alle 8 Module nutzen identical Event-Structures
- 🎯 **Event-Publishing:** Production-ready Event-Publishers mit Schema-Validation

### Dependencies:
- **JSON-Schema-Files:** 6 Event-Schemas + CloudEvents-Envelope (READY)
- **CI-Pipeline:** GitHub-Actions + Maven für Schema-Validation (READY)
- **EVENT_CATALOG.md:** Event-Documentation für Cross-Module-Usage (READY)
- **PostgreSQL:** Event-Publishing-Tables + LISTEN/NOTIFY (READY)
- **Maven Dependencies:** JSON-Schema-Validator + CloudEvents-SDK (READY)

## 🛠️ Implementation Phases (3 Phasen = 4-5 Stunden Gesamt)

### Phase 1: JSON-Schema-Validation + CI-Integration (2 Stunden)

**Goal:** CI-Pipeline validiert alle Events gegen JSON-Schemas + blockt Breaking-Changes

**Actions:**
1. **JSON-Schema-Files Organization + Validation:**
   ```bash
   cd ../artefakte/schemas/

   # Schema-Validation gegen Sample-Events
   npm install -g ajv-cli
   ajv validate -s cloudevents-event-envelope.v1.json \
       -d sample-events/sample.status.changed.example.json

   ajv validate -s credit.checked.v1.json \
       -d sample-events/credit.checked.example.json
   ```
   - **Schema-Structure:** CloudEvents 1.0 Envelope + FreshPlan-Domain-Extensions
   - **Validation-Tests:** Alle 6 Event-Types gegen ihre Schemas validiert
   - **Error-Scenarios:** Invalid Events → clear Validation-Error-Messages
   - **Success Criteria:** Alle JSON-Schemas syntaktisch korrekt + Validation funktional

2. **Maven Schema-Validation Integration:**
   ```xml
   <!-- pom.xml - JSON-Schema-Validation Plugin -->
   <plugin>
     <groupId>com.github.everit-org.json-schema</groupId>
     <artifactId>json-schema-maven-plugin</artifactId>
     <version>1.14.1</version>
     <executions>
       <execution>
         <phase>validate</phase>
         <goals>
           <goal>generate</goal>
         </goals>
         <configuration>
           <sourceDirectory>src/main/resources/schemas</sourceDirectory>
           <targetDirectory>target/generated-sources/json-schema</targetDirectory>
         </configuration>
       </execution>
     </executions>
   </plugin>
   ```
   - **Build-Integration:** Schema-Validation als Maven-Phase
   - **Code-Generation:** Java-Classes aus JSON-Schemas generiert
   - **Error-Handling:** Build-Failure bei Invalid-Schemas
   - **Success Criteria:** mvn validate mit Schema-Validation erfolgreich

3. **GitHub-Actions CI-Pipeline für Schema-Evolution:**
   ```yaml
   # .github/workflows/event-schema-validation.yml
   name: Event Schema Validation
   on: [push, pull_request]

   jobs:
     schema-validation:
       runs-on: ubuntu-latest
       steps:
       - uses: actions/checkout@v3
       - name: Validate Event Schemas
         run: |
           cd schemas/
           for schema in *.json; do
             echo "Validating $schema"
             ajv validate -s "$schema" -d "sample-events/${schema%.json}.example.json"
           done

       - name: Breaking Change Detection
         run: |
           git fetch origin main
           ./scripts/detect-breaking-schema-changes.sh origin/main HEAD
   ```
   - **PR-Validation:** Alle Schema-Changes in Pull-Requests validiert
   - **Breaking-Change-Detection:** Schema-Evolution-Rules enforced
   - **Automatic-Blocking:** Breaking-Changes → CI-Failure + PR-Block
   - **Success Criteria:** CI-Pipeline blockiert Breaking-Schema-Changes

**Timeline:** Tag 3 (2 Stunden)
**Rollback-Plan:** Schema-Validation-Bypass-Flag für Emergency-Deployments

### Phase 2: CloudEvents-SDK Integration + Event-Publishing (1-2 Stunden)

**Goal:** Production-ready Event-Publishers mit CloudEvents 1.0 + JSON-Schema-Validation

**Actions:**
1. **CloudEvents-SDK Maven-Integration:**
   ```xml
   <!-- pom.xml - CloudEvents Dependencies -->
   <dependency>
     <groupId>io.cloudevents</groupId>
     <artifactId>cloudevents-core</artifactId>
     <version>2.5.0</version>
   </dependency>
   <dependency>
     <groupId>io.cloudevents</groupId>
     <artifactId>cloudevents-json-jackson</artifactId>
     <version>2.5.0</version>
   </dependency>
   <dependency>
     <groupId>com.networknt</groupId>
     <artifactId>json-schema-validator</artifactId>
     <version>1.0.86</version>
   </dependency>
   ```
   - **CloudEvents-Core:** Event-Building + Serialization-Support
   - **JSON-Jackson:** JSON-Serialization für CloudEvents
   - **Schema-Validator:** Runtime-Schema-Validation bei Event-Publishing
   - **Success Criteria:** Dependencies resolved + CloudEvents-API verfügbar

2. **Event-Publisher Service Implementation:**
   ```java
   @Service
   public class EventPublisher {

       @Autowired
       private JsonSchemaValidator schemaValidator;

       public void publishSampleStatusChanged(SampleStatusChangedEvent event) {
           // 1. Schema-Validation
           ValidationResult result = schemaValidator.validate(
               "sample.status.changed.v1.json",
               event.toJson()
           );
           if (!result.isValid()) {
               throw new InvalidEventException(result.getErrors());
           }

           // 2. CloudEvents-Envelope
           CloudEvent cloudEvent = CloudEventBuilder.v1()
               .withId(UUID.randomUUID().toString())
               .withType("com.freshplan.sample.status.changed")
               .withSource(URI.create("https://api.freshplan.com/samples"))
               .withTime(OffsetDateTime.now())
               .withDataContentType("application/json")
               .withData(event.toJson().getBytes())
               .withExtension("tenantId", event.getTenantId())
               .withExtension("orgId", event.getOrgId())
               .withExtension("correlationId", MDC.get("correlationId"))
               .build();

           // 3. Event-Publishing (PostgreSQL LISTEN/NOTIFY)
           eventBus.publish("sample.status.changed", cloudEvent);
       }
   }
   ```
   - **Schema-Validation:** Runtime-Validation vor Event-Publishing
   - **CloudEvents-Envelope:** Standard-conforme Event-Struktur
   - **FreshPlan-Extensions:** tenantId + orgId + correlationId für Multi-Tenancy
   - **Error-Handling:** Invalid-Events → Exception + Logging
   - **Success Criteria:** Event-Publisher mit Schema-Validation operational

3. **Cross-Module Event-Standards Implementation:**
   ```java
   // Shared Event-Base-Class für alle Module
   @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
   @JsonSubTypes({
       @JsonSubTypes.Type(value = SampleStatusChangedEvent.class, name = "sample.status.changed"),
       @JsonSubTypes.Type(value = CreditCheckedEvent.class, name = "credit.checked"),
       @JsonSubTypes.Type(value = TrialPhaseStartedEvent.class, name = "trial.phase.started")
   })
   public abstract class FreshPlanEvent {
       @JsonProperty(required = true)
       private String eventId;

       @JsonProperty(required = true)
       private OffsetDateTime timestamp;

       @JsonProperty(required = true)
       private String tenantId;

       @JsonProperty(required = true)
       private String orgId;

       @JsonProperty
       private String correlationId;

       // Common validation + serialization methods
   }
   ```
   - **Type-Safe Events:** Java-Classes für alle Event-Types
   - **Common-Base:** Shared Fields für Multi-Tenancy + Tracing
   - **JSON-Serialization:** Jackson-Annotations für consistent Serialization
   - **Cross-Module-Usage:** Identical Event-Structures in allen 8 Modulen
   - **Success Criteria:** Type-safe Event-Publishing für alle Domain-Events

**Timeline:** Tag 3-4 (1-2 Stunden)
**Rollback-Plan:** Event-Publishing-Bypass für Critical-Business-Operations

### Phase 3: EVENT_CATALOG Enhancement + Production-Validation (1 Stunde)

**Goal:** Complete Event-Documentation + Production-Event-Publishing mit Monitoring

**Actions:**
1. **EVENT_CATALOG.md Enhancement mit B2B-Food-Domain-Events:**
   ```markdown
   ## 🎯 B2B-Food-Domain Events (Production-Ready)

   ### sample.status.changed
   - **Purpose:** Sample-Phase-Übergänge für Lead-to-Customer-Journey
   - **Schema:** [sample.status.changed.v1.json](../schemas/sample.status.changed.v1.json)
   - **Publisher:** Sample-Management-Service (Modul 02)
   - **Subscribers:** Cockpit-KPIs (Modul 01), Customer-Timeline (Modul 03)
   - **SLA:** <5s Delivery, at-least-once, 30-day Retention

   ### credit.checked
   - **Purpose:** Credit-Check-Ergebnisse für Risk-Management
   - **Schema:** [credit.checked.v1.json](../schemas/credit.checked.v1.json)
   - **Publisher:** Credit-Service (Modul 03)
   - **Subscribers:** Order-Processing (Modul 02), Risk-Dashboard (Modul 04)
   - **SLA:** <10s Delivery, exactly-once via Idempotency, 90-day Retention
   ```
   - **Complete Event-Documentation:** Alle 6 Event-Types dokumentiert
   - **Schema-Links:** Direct Links zu JSON-Schema-Files
   - **Publisher/Subscriber-Matrix:** Cross-Module-Event-Flow dokumentiert
   - **SLA-Definition:** Delivery-Guarantees + Retention-Policies
   - **Success Criteria:** EVENT_CATALOG.md ready für Cross-Module-Teams

2. **Production-Event-Publishing Testing:**
   ```bash
   # End-to-End Event-Publishing Test
   cd ../backend/

   # Sample-Status-Changed Event
   curl -X POST http://localhost:8080/api/samples/lead123/status \
     -H "Content-Type: application/json" \
     -H "X-Tenant-ID: tenant1" \
     -H "X-Correlation-ID: test-$(date +%s)" \
     -d '{"newStatus": "TRIAL", "reason": "Customer confirmed interest"}'

   # Verify Event in PostgreSQL LISTEN/NOTIFY
   psql -c "LISTEN sample_status_changed;"
   # Should receive CloudEvents-formatted Event

   # Verify Schema-Validation
   tail -f logs/application.log | grep "schema.validation"
   ```
   - **End-to-End-Testing:** Complete Event-Flow von API bis Event-Bus
   - **Schema-Validation-Verification:** Events validiert gegen JSON-Schemas
   - **Multi-Tenancy-Testing:** Events mit korrekten Tenant/Org-Extensions
   - **Performance-Testing:** Event-Publishing <50ms P95
   - **Success Criteria:** Production-Event-Publishing mit Schema-Validation operational

3. **Event-Monitoring + Alerting Setup:**
   ```yaml
   # Prometheus-Metrics für Event-Publishing
   event_published_total{type, tenant, status}: Counter
   event_validation_duration_seconds{type}: Histogram
   event_validation_errors_total{type, error}: Counter
   event_publishing_duration_seconds{type}: Histogram

   # Alert-Rules
   - alert: EventValidationFailure
     expr: rate(event_validation_errors_total[5m]) > 0.1
     for: 2m
     annotations:
       summary: "High event validation failure rate"

   - alert: EventPublishingLatency
     expr: histogram_quantile(0.95, event_publishing_duration_seconds_bucket) > 0.1
     for: 5m
     annotations:
       summary: "Event publishing P95 latency > 100ms"
   ```
   - **Event-Metrics:** Success-Rate, Latency, Validation-Errors
   - **Alerting:** High Error-Rates + Performance-Degradation
   - **Dashboard:** Grafana-Dashboard für Event-Operations
   - **Success Criteria:** Complete Event-Observability für Production-Operations

**Timeline:** Tag 4 (1 Stunde)
**Rollback-Plan:** Event-Monitoring-Disable bei Performance-Impact

## ✅ Success Metrics

### **Immediate Success (Phase 1-3 = 4-5 Stunden):**
1. **Schema-Validation:** CI-Pipeline blockiert Breaking-Schema-Changes
2. **CloudEvents-Integration:** Alle Events folgen CloudEvents 1.0 Standard
3. **Event-Publishing:** Production-ready Publishers mit Runtime-Schema-Validation
4. **Cross-Module-Standards:** Alle 8 Module nutzen identical Event-Structures
5. **EVENT_CATALOG:** Complete Documentation für Cross-Module-Event-Integration

### **Operational Success (1-2 Wochen):**
1. **Schema-Evolution:** Zero Breaking-Changes in Production-Events
2. **Event-Reliability:** >99.9% Event-Delivery-Success-Rate
3. **Performance:** <50ms P95 für Event-Publishing + Schema-Validation
4. **Developer-Experience:** Teams können sofort standardisierte Events nutzen
5. **Monitoring:** Complete Event-Observability mit Prometheus + Grafana

### **External Validation:**
- **Event-Schemas:** 9.9/10 (External AI) - CloudEvents 1.0 + B2B-Food-Domain perfect
- **Schema-Evolution:** CI-Pipeline-Integration mit Breaking-Change-Protection
- **Cross-Module-Standards:** Foundation für Event-Bus-Migration vorbereitet

## 🔗 Related Documentation

### **Schema Implementation:**
- [CloudEvents Envelope](../artefakte/schemas/cloudevents-event-envelope.v1.json) - Standard Event-Structure
- [Sample Status Schema](../artefakte/schemas/sample.status.changed.v1.json) - B2B-Food-Domain Event
- [Credit Check Schema](../artefakte/schemas/credit.checked.v1.json) - Risk-Management Event

### **Event Standards:**
- [EVENT_CATALOG Enhancement](../artefakte/docs/EVENT_CATALOG.md) - Complete Event-Documentation
- [Integration Charter](../artefakte/docs/INTEGRATION_CHARTER.md) - Event-Standards für alle Module
- [API Standards](../../../grundlagen/API_STANDARDS.md) - Headers + Correlation-ID

### **Foundation Integration:**
- [Foundation Integration Guide](../artefakte/docs/FOUNDATION_INTEGRATION_GUIDE.md) - Cross-Module-Event-Usage
- [Event-Bus Migration](../artefakte/docs/EVENT_BUS_MIGRATION_ROADMAP.md) - LISTEN/NOTIFY → Event-Bus
- [Operations Runbook](../../betrieb/README.md) - Event-Operations + Monitoring

## 🤖 Claude Handover Section

### **Current Priority (für neue Claude):**
```bash
# Phase 1: Schema-Validation
cd integration/implementation-plans/
→ 03_EVENT_SCHEMAS_INTEGRATION_PLAN.md (CURRENT)

# Start Here:
cd ../artefakte/schemas/
ajv validate -s cloudevents-event-envelope.v1.json \
    -d sample-events/sample.status.changed.example.json

# Success Criteria: Schema-Validation erfolgreich + CI-Integration ready
# Next: CloudEvents-SDK + Event-Publishing
```

### **Context für neue Claude:**
- **Event-Schemas:** Atomarer Implementation-Plan für CloudEvents 1.0 + JSON-Schema-Integration
- **Timeline:** 4-5 Stunden von Schema-Validation bis Production-Event-Publishing
- **External AI Validation:** 9.9/10 CloudEvents + B2B-Food-Domain-Events perfect
- **Dependencies:** JSON-Schemas ready + CI-Pipeline + CloudEvents-SDK

### **Key Success-Factors:**
- **Schema-Validation:** CI-Pipeline blockiert Breaking-Changes für Production-Safety
- **CloudEvents-Compliance:** Standard-conforme Events für Cross-Module-Integration
- **Runtime-Validation:** Event-Publishing mit Schema-Validation für Data-Quality
- **Cross-Module-Standards:** Identical Event-Structures in allen 8 Modulen

**🚀 Ready für CloudEvents 1.0 + B2B-Food-Domain Event-Schemas Production-Integration!**