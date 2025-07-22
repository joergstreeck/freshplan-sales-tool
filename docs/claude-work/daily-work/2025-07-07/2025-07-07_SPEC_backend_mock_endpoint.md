# Technische Spezifikation: Backend Mock-Endpunkte für Sales Cockpit

**Datum:** 2025-01-07  
**Status:** Spezifiziert  
**Kategorie:** SPEC  

## Übersicht

Diese Spezifikation beschreibt die Implementierung von Mock-Endpunkten im Backend 
für die Entwicklung des Sales Cockpit Dashboards. Die Mock-Endpunkte ermöglichen 
eine saubere Frontend-Entwicklung ohne Datenbank-Verschmutzung.

## Architektur-Entscheidung

**Referenz:** [ADR-001](/docs/features/../../adr/ADR-001_backend_mock_endpoint_for_dev.md)

**Kernpunkte:**
- ⭐⭐⭐⭐ Architektur-Qualität (4 Sterne)
- Klare Trennung zwischen Development und Production
- Backend kontrolliert Mock-Daten zentral
- Keine Test-User in der echten Datenbank

## Technische Details

### 1. Package-Struktur

```
backend/
└── src/main/java/de/freshsolutions/sales/
    └── api/
        └── dev/
            ├── MockDataResource.java
            ├── dto/
            │   ├── MockDashboardData.java
            │   ├── MockActivity.java
            │   └── MockCustomer.java
            └── service/
                └── MockDataGenerator.java
```

### 2. Mock-Endpunkte

#### 2.1 Dashboard Mock-Daten

**Endpunkt:** `GET /api/dev/sales-cockpit/dashboard/{userId}`

**Response-Struktur:**
```json
{
  "userId": "mock-user-1",
  "displayName": "Max Mustermann",
  "stats": {
    "openLeads": 12,
    "activitiesThisWeek": 45,
    "opportunitiesInProgress": 8,
    "conversionRate": 0.23
  },
  "todaysTasks": [
    {
      "id": "task-1",
      "title": "Follow-up mit Kunde ABC",
      "dueTime": "10:30",
      "priority": "high",
      "customerId": "cust-123"
    }
  ],
  "alerts": [
    {
      "type": "deadline",
      "message": "Angebot für XYZ läuft heute ab",
      "severity": "warning",
      "link": "/opportunities/opp-456"
    }
  ]
}
```

#### 2.2 Activities Mock-Daten

**Endpunkt:** `GET /api/dev/sales-cockpit/activities/{userId}`

**Query-Parameter:**
- `limit` (optional, default: 20)
- `offset` (optional, default: 0)
- `customerId` (optional): Filtert nach Kunde

**Response-Struktur:**
```json
{
  "activities": [
    {
      "id": "act-1",
      "timestamp": "2025-01-07T09:30:00Z",
      "type": "email",
      "title": "E-Mail an Kunde gesendet",
      "description": "Angebot für Catering-Service versendet",
      "customerId": "cust-123",
      "customerName": "ABC GmbH",
      "userId": "mock-user-1"
    }
  ],
  "totalCount": 156,
  "hasMore": true
}
```

#### 2.3 Customers Mock-Daten

**Endpunkt:** `GET /api/dev/customers`

**Query-Parameter:**
- `search` (optional): Suche nach Name
- `status` (optional): lead|prospect|customer
- `limit` (optional, default: 50)
- `offset` (optional, default: 0)

**Response-Struktur:**
```json
{
  "customers": [
    {
      "id": "cust-123",
      "name": "ABC GmbH",
      "status": "prospect",
      "lastContact": "2025-01-05T14:00:00Z",
      "assignedTo": "mock-user-1",
      "revenue": 45000,
      "tags": ["premium", "catering", "events"]
    }
  ],
  "totalCount": 234,
  "facets": {
    "byStatus": {
      "lead": 45,
      "prospect": 89,
      "customer": 100
    }
  }
}
```

### 3. Implementierungs-Details

#### 3.1 Profile-basierte Aktivierung

```java
@Path("/api/dev")
@Profile("dev | test")
@ApplicationScoped
@Tag(name = "Development Mock Data")
public class MockDataResource {
    
    @Inject
    MockDataGenerator mockDataGenerator;
    
    @GET
    @Path("/sales-cockpit/dashboard/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMockDashboard(@PathParam("userId") String userId) {
        // Deterministisch basierend auf userId
        var dashboard = mockDataGenerator.generateDashboard(userId);
        return Response.ok(dashboard).build();
    }
}
```

#### 3.2 Mock-Daten-Generator

```java
@ApplicationScoped
public class MockDataGenerator {
    
    private static final Map<String, UserScenario> SCENARIOS = Map.of(
        "mock-user-1", UserScenario.SUCCESSFUL_SALES,
        "mock-user-2", UserScenario.NEW_EMPLOYEE,
        "mock-user-3", UserScenario.TEAM_MANAGER,
        "mock-user-error", UserScenario.ERROR_SCENARIO
    );
    
    public MockDashboardData generateDashboard(String userId) {
        var scenario = SCENARIOS.getOrDefault(userId, UserScenario.DEFAULT);
        return buildDashboardForScenario(scenario, userId);
    }
}
```

#### 3.3 Szenario-basierte Daten

**Szenarien:**

1. **mock-user-1** (Erfolgreicher Vertriebsmitarbeiter):
   - Viele offene Leads
   - Hohe Aktivitätsrate
   - Gute Conversion Rate
   - Mix aus verschiedenen Kundentypen

2. **mock-user-2** (Neuer Mitarbeiter):
   - Wenige Leads
   - Hauptsächlich Onboarding-Tasks
   - Niedrige Aktivitätsrate
   - Fokus auf Training

3. **mock-user-3** (Team Manager):
   - Team-Übersicht
   - Aggregierte Statistiken
   - Management-Tasks
   - Team-Performance-Alerts

4. **mock-user-error** (Error-Szenario):
   - Provoziert verschiedene Fehlerzustände
   - Für Error-Handling-Tests

### 4. Sicherheits-Aspekte

#### 4.1 Production-Schutz

```java
@ServerRequestFilter
public class DevEndpointProtection {
    
    @ConfigProperty(name = "quarkus.profile")
    String profile;
    
    public void filter(ContainerRequestContext context) {
        if (context.getUriInfo().getPath().startsWith("/api/dev/")) {
            if (!"dev".equals(profile) && !"test".equals(profile)) {
                context.abortWith(Response.status(404).build());
            }
        }
    }
}
```

#### 4.2 Rate Limiting

```java
@RateLimited(value = 100, window = 1, windowUnit = ChronoUnit.MINUTES)
```

### 5. Testing

#### 5.1 Unit Tests

```java
@Test
void testMockDashboardGeneration() {
    var generator = new MockDataGenerator();
    var dashboard = generator.generateDashboard("mock-user-1");
    
    assertThat(dashboard).isNotNull();
    assertThat(dashboard.getStats().getOpenLeads()).isGreaterThan(0);
    assertThat(dashboard.getTodaysTasks()).isNotEmpty();
}
```

#### 5.2 Integration Tests

```java
@QuarkusTest
@TestProfile(DevProfile.class)
class MockDataResourceTest {
    
    @Test
    void testDashboardEndpoint() {
        given()
            .when().get("/api/dev/sales-cockpit/dashboard/mock-user-1")
            .then()
            .statusCode(200)
            .body("userId", is("mock-user-1"))
            .body("stats.openLeads", greaterThan(0));
    }
}
```

### 6. Frontend-Integration

```typescript
// services/mockApi.ts
export const mockApi = {
  getDashboard: (userId: string) => 
    api.get(`/api/dev/sales-cockpit/dashboard/${userId}`),
    
  getActivities: (userId: string, params?: ActivityParams) =>
    api.get(`/api/dev/sales-cockpit/activities/${userId}`, { params }),
    
  getCustomers: (params?: CustomerParams) =>
    api.get('/api/dev/customers', { params })
};

// In Development:
const dashboardData = import.meta.env.DEV 
  ? await mockApi.getDashboard('mock-user-1')
  : await api.getDashboard(currentUser.id);
```

### 7. Dokumentation

#### 7.1 OpenAPI Annotation

```java
@Operation(
    summary = "Get mock dashboard data",
    description = "Returns mock dashboard data for development. Only available in dev/test profiles.",
    tags = {"Development"}
)
@APIResponse(
    responseCode = "200",
    description = "Mock dashboard data",
    content = @Content(schema = @Schema(implementation = MockDashboardData.class))
)
```

#### 7.2 Entwickler-Dokumentation

Siehe `/docs/guides/USING_MOCK_ENDPOINTS.md` für:
- Verfügbare Mock-User und ihre Szenarien
- Beispiel-Requests und Responses
- Integration in Frontend-Development
- Troubleshooting

## Vorteile dieser Lösung

1. **Saubere Architektur**: Keine Vermischung von Test- und Produktionsdaten
2. **Zentrale Kontrolle**: Backend definiert die Mock-Daten-Struktur
3. **Versionierbar**: Mock-Daten sind im Git und können mit API evolvieren
4. **Performance**: Keine DB-Queries für Mock-Daten
5. **Deterministisch**: Immer gleiche Daten für konsistente Tests
6. **Sicherheit**: Automatisch in Production deaktiviert

## Nächste Schritte

1. Implementierung der `MockDataResource` Klasse
2. Erstellung der DTOs für Mock-Daten
3. Implementierung des `MockDataGenerator`
4. Unit- und Integration-Tests
5. Frontend-Integration und Testing
6. Dokumentation für Entwickler