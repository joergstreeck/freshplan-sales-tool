# 📚 Foundation Standards Reference Bundle - Modul 01 Mein Cockpit

**📅 Datum:** 2025-09-19
**🎯 Zweck:** Komplette Foundation Standards Referenz für KI-Implementation
**📊 Status:** Production-Ready Standards aus Modul 03 + 04 Success Stories
**🏗️ Architektur:** Multi-Channel Sales Dashboard (3-Spalten-SmartLayout für FreshFoodz Genussberater)

---

## 🎨 **DESIGN SYSTEM V2 - FreshFoodz CI Standards**

### **Verbindliche FreshFoodz Farbpalette:**
```css
:root {
  /* FreshFoodz Corporate Identity - NICHT ÄNDERN! */
  --color-primary: #94C456;    /* Primärgrün */
  --color-secondary: #004F7B;  /* Dunkelblau */
  --color-white: #FFFFFF;
  --color-black: #000000;

  /* Accessibility-konforme Varianten */
  --color-primary-hover: #7BA945;
  --color-secondary-hover: #003A5C;

  /* Dashboard-spezifische Farben */
  --color-success: #28a745;
  --color-warning: #ffc107;
  --color-danger: #dc3545;
  --color-info: #17a2b8;
}
```

### **Typography Standards:**
```css
/* Headlines - Antonio Bold (PFLICHT) */
.font-headline {
  font-family: 'Antonio', sans-serif;
  font-weight: 700;
}

/* Body Text - Poppins Regular */
.font-body {
  font-family: 'Poppins', sans-serif;
  font-weight: 400;
}

/* Dashboard-KPIs - Antonio Bold */
.font-kpi {
  font-family: 'Antonio', sans-serif;
  font-weight: 700;
  font-size: 2rem;
}
```

### **Theme V2 MUI Integration (Cockpit-spezifisch):**
```typescript
// theme-v2-cockpit.mui.ts - Foundation Standards Template
import { createTheme } from '@mui/material/styles';

export const cockpitThemeV2 = createTheme({
  palette: {
    primary: { main: 'var(--color-primary)' },      // #94C456
    secondary: { main: 'var(--color-secondary)' },  // #004F7B
    success: { main: 'var(--color-success)' },
    warning: { main: 'var(--color-warning)' },
    error: { main: 'var(--color-danger)' },
    info: { main: 'var(--color-info)' },
  },
  typography: {
    fontFamily: 'Poppins, system-ui, -apple-system, Segoe UI, Roboto, Arial',
    h1: { fontFamily: 'Antonio', fontWeight: 700 },
    h2: { fontFamily: 'Antonio', fontWeight: 700 },
    h3: { fontFamily: 'Antonio', fontWeight: 700 },
  },
  shape: { borderRadius: 8 },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          border: '1px solid #e0e0e0',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 600,
        },
      },
    },
  },
});
```

---

## 🔒 **SECURITY ABAC - Territory-Based Access Control**

### **JWT Claims Structure:**
```typescript
interface JWTClaims {
  sub: string;                    // User ID
  email: string;                  // User Email
  territories: string[];          // ["BER", "MUC", "HAM"] - Territory Access
  roles: string[];               // ["SALES_REP", "TEAM_LEAD", "ADMIN"]
  channelAccess: ChannelType[];  // ["DIRECT", "PARTNER"] - Multi-Channel Access
  scope: string;                 // "cockpit:read cockpit:write"
}

enum ChannelType {
  DIRECT = "DIRECT",              // Direct Sales Channel
  PARTNER = "PARTNER"             // Partner Channel
}
```

### **Security Scope Filter (Template):**
```java
/** SecurityScopeFilter – Multi-Channel Territory-based ABAC Security
 * Foundation References:
 * - Security: /docs/planung/grundlagen/SECURITY_ABAC.md
 * - Multi-Channel: /docs/planung/CRM_SYSTEM_CONTEXT.md
 */
package de.freshplan.cockpit.security;

import de.freshplan.security.ScopeContext;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import java.util.List;

@RequestScoped
public class CockpitSecurityScopeFilter implements ContainerRequestFilter {

    @Inject
    ScopeContext scopeContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        SecurityContext securityContext = requestContext.getSecurityContext();
        if (securityContext.getUserPrincipal() != null) {
            // Extract Territory + Channel access from JWT
            List<String> territories = extractTerritories(securityContext);
            List<ChannelType> channels = extractChannelAccess(securityContext);

            scopeContext.setTerritories(territories);
            scopeContext.setChannelAccess(channels);
        }
    }
}
```

### **Database RLS Policies (Template):**
```sql
-- Territory-based Row Level Security für Cockpit-Daten
-- Foundation Standards: Keine Fallback-Territories!

-- Multi-Channel KPI Access
ALTER TABLE cockpit_kpis ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_cockpit_kpis_territory ON cockpit_kpis
  USING (territory = ANY(current_setting('app.territories', true)::text[]));

-- Channel-specific Data Access
ALTER TABLE channel_performance ENABLE ROW LEVEL SECURITY;
CREATE POLICY rls_channel_performance ON channel_performance
  USING (
    territory = ANY(current_setting('app.territories', true)::text[]) AND
    channel_type = ANY(current_setting('app.channel_access', true)::text[])
  );
```

---

## 📊 **API STANDARDS - OpenAPI 3.1 + JavaDoc**

### **API Documentation Standards:**
```yaml
# cockpit-dashboard.yaml - Foundation Standards Template
openapi: 3.1.0
info:
  title: FreshFoodz Cockpit Dashboard API
  version: 1.0.0
  description: |
    Multi-Channel B2B Sales Dashboard API für FreshFoodz Genussberater

    Foundation Standards References:
    - API Standards: /docs/planung/grundlagen/API_STANDARDS.md
    - Security ABAC: /docs/planung/grundlagen/SECURITY_ABAC.md
    - Performance SLO: P95 <200ms für alle Dashboard-Endpoints

    Business Context:
    - B2B-Convenience-Food-Hersteller mit Cook&Fresh® Produktlinie
    - Multi-Channel-Vertrieb: Direct Sales + Partner Channel
    - Territory-basierte ABAC Security für Genussberater

paths:
  /api/cockpit/dashboard:
    get:
      summary: Multi-Channel Dashboard KPIs
      description: |
        Liefert Territory-gefilterte Dashboard-KPIs für Genussberater
        Foundation Standards: Territory-Scoping via JWT Claims
      parameters:
        - name: channelFilter
          in: query
          schema:
            type: string
            enum: [all, direct, partner]
          description: Multi-Channel Filter für KPI-Anzeige
      responses:
        '200':
          description: Dashboard-KPIs erfolgreich geladen
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/DashboardKPIs'
        '403':
          $ref: '#/components/responses/ForbiddenTerritoryAccess'
```

### **JavaDoc Foundation Standards:**
```java
/** SalesCockpitResource – Multi-Channel Dashboard API
 * Foundation References:
 * - API: /docs/planung/grundlagen/API_STANDARDS.md
 * - Security: /docs/planung/grundlagen/SECURITY_ABAC.md
 * - Performance: P95 <200ms SLO für alle Dashboard-Endpoints
 *
 * Business Context:
 * - FreshFoodz B2B-Convenience-Food-Vertrieb
 * - Multi-Channel: Direct Sales + Partner Channel
 * - Territory-basierte ABAC Security für Genussberater
 */
@Path("/api/cockpit")
@ApplicationScoped
public class SalesCockpitResource {

    /** Multi-Channel Dashboard KPIs abrufen
     * @param channelFilter Multi-Channel Filter (all/direct/partner)
     * @return Territory-gefilterte Dashboard-KPIs
     * @throws ForbiddenException bei fehlender Territory-Berechtigung
     */
    @GET
    @Path("/dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public DashboardKPIsDTO getDashboardKPIs(@QueryParam("channelFilter") String channelFilter) {
        // Implementation mit Foundation Standards
    }
}
```

---

## 🧪 **TESTING STANDARDS - BDD + Coverage**

### **BDD Test Pattern (Template):**
```java
/** SalesCockpitServiceTest – BDD Tests für Dashboard-Service
 * Foundation Standards: Given-When-Then Pattern, 80%+ Coverage
 * Security Tests: ABAC Territory-Validation
 */
package de.freshplan.cockpit;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class SalesCockpitServiceTest {

    @Inject
    SalesCockpitService cockpitService;

    @Test
    void getDashboardKPIs_withValidTerritories_shouldReturnFilteredKPIs() {
        // Given
        List<String> userTerritories = List.of("BER", "MUC");
        String channelFilter = "direct";

        // When
        DashboardKPIsDTO result = cockpitService.getDashboardKPIs(channelFilter, userTerritories);

        // Then
        assertNotNull(result);
        assertTrue(result.getKpis().stream()
            .allMatch(kpi -> userTerritories.contains(kpi.getTerritory())));
        assertEquals("direct", result.getChannelFilter());
    }

    @Test
    void getDashboardKPIs_withNoTerritories_shouldThrowForbidden() {
        // Given
        List<String> emptyTerritories = Collections.emptyList();
        String channelFilter = "all";

        // When & Then
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> cockpitService.getDashboardKPIs(channelFilter, emptyTerritories)
        );
        assertEquals("No authorized territories for cockpit access", exception.getMessage());
    }
}
```

### **Coverage Configuration:**
```xml
<!-- coverage-config.xml - Foundation Standards 80%+ -->
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <properties>
        <!-- Coverage Thresholds gemäß Foundation Standards -->
        <jacoco.coverage.line.minimum>0.80</jacoco.coverage.line.minimum>
        <jacoco.coverage.branch.minimum>0.75</jacoco.coverage.branch.minimum>
        <jacoco.coverage.method.minimum>0.80</jacoco.coverage.method.minimum>
        <jacoco.coverage.class.minimum>0.85</jacoco.coverage.class.minimum>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>check-coverage</id>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>PACKAGE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>${jacoco.coverage.line.minimum}</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 🗄️ **SQL STANDARDS - PostgreSQL + RLS**

### **Performance-optimierte Cockpit Queries:**
```sql
-- cockpit_kpis_projection.sql - Foundation Standards Template
-- Hot-Projection für Dashboard-Performance (P95 <200ms)

CREATE TABLE cockpit_kpis_hot_projection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    territory VARCHAR(10) NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    -- KPI Felder
    active_customers INTEGER NOT NULL DEFAULT 0,
    new_leads_today INTEGER NOT NULL DEFAULT 0,
    roi_calculations_today INTEGER NOT NULL DEFAULT 0,
    sample_tests_pending INTEGER NOT NULL DEFAULT 0,
    -- Performance Felder
    avg_response_time_ms INTEGER,
    last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),

    -- Foundation Standards: Eindeutige Constraints
    CONSTRAINT uq_cockpit_kpis_territory_channel
        UNIQUE (territory, channel_type)
);

-- Strategic Performance-Indizes
CREATE INDEX idx_cockpit_kpis_territory_channel
    ON cockpit_kpis_hot_projection(territory, channel_type);
CREATE INDEX idx_cockpit_kpis_updated
    ON cockpit_kpis_hot_projection(last_updated_at DESC);

-- RLS Security-Policies
ALTER TABLE cockpit_kpis_hot_projection ENABLE ROW LEVEL SECURITY;

CREATE POLICY rls_cockpit_kpis_read ON cockpit_kpis_hot_projection
    FOR SELECT
    USING (
        territory = ANY(current_setting('app.territories', true)::text[]) AND
        channel_type = ANY(current_setting('app.channel_access', true)::text[])
    );

-- Trigger für Auto-Update der Hot-Projection
CREATE OR REPLACE FUNCTION recompute_cockpit_kpis(target_territory TEXT, target_channel TEXT)
RETURNS VOID AS $$
BEGIN
    INSERT INTO cockpit_kpis_hot_projection (territory, channel_type, active_customers, new_leads_today, roi_calculations_today, sample_tests_pending)
    SELECT
        target_territory,
        target_channel,
        COUNT(DISTINCT c.id) FILTER (WHERE c.status = 'ACTIVE'),
        COUNT(DISTINCT l.id) FILTER (WHERE l.created_date = CURRENT_DATE),
        COUNT(DISTINCT r.id) FILTER (WHERE r.calculated_date = CURRENT_DATE),
        COUNT(DISTINCT s.id) FILTER (WHERE s.status = 'PENDING')
    FROM customers c
    LEFT JOIN leads l ON l.territory = target_territory AND l.channel_type = target_channel
    LEFT JOIN roi_calculations r ON r.territory = target_territory
    LEFT JOIN sample_tests s ON s.territory = target_territory
    WHERE c.territory = target_territory AND c.channel_type = target_channel
    ON CONFLICT (territory, channel_type) DO UPDATE SET
        active_customers = EXCLUDED.active_customers,
        new_leads_today = EXCLUDED.new_leads_today,
        roi_calculations_today = EXCLUDED.roi_calculations_today,
        sample_tests_pending = EXCLUDED.sample_tests_pending,
        last_updated_at = now();
END;
$$ LANGUAGE plpgsql;
```

---

## 🚀 **PERFORMANCE STANDARDS - SLO Definition**

### **Cockpit-spezifische Performance-Budgets:**
```yaml
# Performance-Budget für Mein Cockpit Dashboard
dashboard_performance_budget:
  # API Performance
  api_response_times:
    cockpit_dashboard_kpis:
      target: "P95 < 200ms"
      critical: "P95 < 500ms"
    multi_channel_filter:
      target: "P95 < 100ms"  # Channel-switching muss schnell sein
      critical: "P95 < 300ms"
    roi_calculator:
      target: "P95 < 500ms"  # Synchrone Kalkulationen
      critical: "P95 < 1000ms"

  # Frontend Performance
  page_load_times:
    dashboard_initial_load:
      target: "FCP < 1.5s"
      critical: "FCP < 3s"
    channel_filter_switch:
      target: "< 200ms"
      critical: "< 500ms"
    roi_modal_open:
      target: "< 300ms"
      critical: "< 800ms"

  # Database Performance
  query_performance:
    cockpit_kpis_query:
      target: "< 50ms"
      critical: "< 200ms"
    multi_channel_aggregation:
      target: "< 100ms"
      critical: "< 300ms"
```

---

## 📦 **PACKAGE STRUCTURE - de.freshplan Standards**

### **Backend Package-Organization:**
```java
// Cockpit-spezifische Package-Struktur (monolithisch aber organisiert)
de.freshplan.cockpit/
├── api/
│   ├── SalesCockpitResource.java         // REST Endpoints
│   ├── MultiChannelKPIResource.java      // Multi-Channel APIs
│   └── ROICalculatorResource.java        // ROI-Calculator APIs
├── service/
│   ├── SalesCockpitQueryService.java     // Dashboard-Queries (bereits vorhanden)
│   ├── MultiChannelKPIService.java       // Multi-Channel KPI-Logic
│   ├── ROICalculatorService.java         // ROI-Berechnungen
│   └── CockpitSecurityService.java       // ABAC Territory-Validation
├── model/
│   ├── DashboardKPIsDTO.java            // Dashboard-Data-Transfer
│   ├── MultiChannelFilterDTO.java       // Channel-Filter-Models
│   └── ROICalculationDTO.java           // ROI-Calculator-Models
├── security/
│   ├── CockpitSecurityScopeFilter.java  // Territory+Channel ABAC
│   └── MultiChannelAccessValidator.java // Channel-Access-Logic
└── exception/
    ├── CockpitBusinessException.java    // Business-Logic-Exceptions
    └── TerritoryAccessException.java    // ABAC-Security-Exceptions
```

---

## 🔧 **BUSINESS CONTEXT - FreshFoodz Spezifika**

### **B2B-Convenience-Food Business-Model:**
```typescript
// FreshFoodz Geschäfts-Kontext für Cockpit-Implementation
interface FreshFoodzBusinessContext {
  // Convenience-Food Hersteller
  productLine: "Cook&Fresh®";
  businessModel: "B2B-Convenience-Food-Vertrieb";

  // Multi-Channel Vertrieb
  channels: {
    direct: "Direct Sales zu Gastronomiebetrieben";
    partner: "Partner-Channel über Distributoren";
  };

  // Zielkunden (Gastronomiebetriebe)
  targetCustomers: [
    "HOTEL",                    // Hotel-Restaurants
    "RESTAURANT",               // Standalone-Restaurants
    "BETRIEBSGASTRONOMIE",     // Betriebskantinen
    "CATERING"                 // Catering-Services
  ];

  // ROI-Value-Proposition
  roiFactors: {
    laborSavings: "Personalkosten-Ersparnis durch vorgefertigte Produkte";
    wasteSavings: "Weniger Food-Waste durch portionierte Convenience-Food";
    qualityConsistency: "Gleichbleibende Qualität vs. Eigenproduktion";
    kitchenEfficiency: "Optimierte Küchenabläufe und Lagerhaltung";
  };
}
```

### **3-Spalten-Dashboard-Konzept:**
```typescript
// Cockpit-Layout für FreshFoodz Genussberater
interface CockpitLayoutStructure {
  // Spalte 1: Genussberater-Tag
  leftColumn: {
    title: "Mein Tag";
    widgets: [
      "Sample-Tests heute",           // Cook&Fresh® Produkttests
      "ROI-Termine heute",           // Beratungstermine
      "Neue Leads aus Lead-Erfassung", // Integration zu Modul 02
      "FreshFoodz-KPIs"              // B2B-Food-spezifische Metriken
    ];
  };

  // Spalte 2: Multi-Channel-Pipeline
  centerColumn: {
    title: "Account-Pipeline";
    features: [
      "Channel-Filter (Direct/Partner)", // Multi-Channel-Koordination
      "Territory-basierte Account-Liste", // ABAC-gefiltert
      "Business-Type-Filter",            // HOTEL/RESTAURANT/etc.
      "Channel-Conflict-Alerts"          // Overlap-Detection
    ];
  };

  // Spalte 3: Account-Intelligence
  rightColumn: {
    title: "Account-Tools";
    tools: [
      "ROI-Calculator-Modal",           // Convenience-Food ROI-Kalkulation
      "Channel-Assignment-Info",        // Direct vs. Partner Info
      "Cook&Fresh®-Affinity-Score",    // Produkt-Fit-Score
      "Quick-Actions (Sample, Angebot)" // Workflow-Integration
    ];
  };
}
```

---

## 📋 **SUCCESS PATTERNS - Von Modul 03 übernehmen**

### **Was bei Modul 03 perfekt funktioniert hat:**

1. **Foundation Standards References:**
   - Explizite Links zu `/docs/planung/grundlagen/` in allen Artefakten
   - JavaDoc Foundation-Comments in Backend-Services
   - Theme V2 durchgängige Integration

2. **Security ABAC Implementation:**
   - Territory-Scoping ohne Fallback-Lücken
   - JWT Claims → RLS Database-Policies
   - BDD-Tests für Security-Validation

3. **Enterprise-Grade Code-Quality:**
   - 80%+ Test-Coverage mit Coverage-Gates
   - RFC7807 Error-Handling durchgängig
   - Performance-optimierte Queries mit Strategic Indices

4. **Production-Ready Artefakte-Set:**
   - 39 Copy-Paste-Ready Deliverables
   - 8 organisierte Kategorien (API/Backend/Frontend/SQL/Testing/CI/Docs)
   - Vollständige Deployment & Operations-Guides

### **Für Modul 01 zu adaptieren:**
- **Multi-Channel-Spezifika:** Channel-Type-Security zusätzlich zu Territory
- **Dashboard-Performance:** Real-time KPI-Updates und Hot-Projections
- **ROI-Calculator:** Complex Business-Logic für Convenience-Food-Kalkulation
- **SmartLayout-Integration:** Pilot für V3-Migration

---

## 🎯 **FINAL CHECKLIST - Foundation Standards Vollständigkeit**

### **Alle Standards vollständig abgedeckt:**

- ✅ **Design System V2:** FreshFoodz CI + Theme V2 + CSS-Tokens
- ✅ **Security ABAC:** Territory + Multi-Channel JWT Claims → RLS
- ✅ **API Standards:** OpenAPI 3.1 + JavaDoc Foundation References
- ✅ **Testing Standards:** BDD Given-When-Then + 80%+ Coverage
- ✅ **Package Structure:** de.freshplan.cockpit.* durchgängig
- ✅ **Performance Standards:** P95 <200ms SLO + Performance-Budget
- ✅ **SQL Standards:** RLS Policies + Strategic Performance-Indizes
- ✅ **Business Context:** B2B-Convenience-Food + Multi-Channel

### **Cockpit-spezifische Extensions:**
- ✅ **Multi-Channel-Security:** Direct/Partner Channel-Access-Control
- ✅ **Dashboard-Performance:** Hot-Projections für Real-time KPIs
- ✅ **ROI-Calculator:** Foundation für Convenience-Food-Kalkulationen
- ✅ **SmartLayout-Pilot:** Content-Type-Detection für V3-Migration

---

**🚀 Dieses Reference Bundle enthält alle Foundation Standards für die erfolgreiche KI-Implementation von Modul 01 Cockpit auf Enterprise-Grade Level - analog zum Modul 03 Success!**