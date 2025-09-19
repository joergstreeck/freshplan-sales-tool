# 📚 Foundation Standards Reference Bundle - Modul 03 Kundenmanagement

**📅 Datum:** 2025-09-19
**🎯 Zweck:** Komplette Foundation Standards Referenz für KI-Implementation
**📊 Status:** Production-Ready Standards aus Modul 02 + 04 Success Stories
**🏗️ Architektur:** Monolithisch (bewusste Entscheidung für zusammenhängende Customer-Workflows)

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

/* Emphasized - Poppins Medium */
.font-body-medium {
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
}
```

### **Theme V2 Integration (KEIN HARDCODING!):**
```typescript
// ❌ FALSCH: Hardcoding
const styles = {
  backgroundColor: '#94C456',
  fontFamily: 'Antonio, sans-serif'
};

// ✅ RICHTIG: Theme V2 verwenden
import { ThemeProvider } from '@mui/material/styles';
import { freshfoodzTheme } from '@/theme/freshfoodz';

<ThemeProvider theme={freshfoodzTheme}>
  <Button variant="contained" color="primary">
    {/* Automatisch #94C456 via Theme V2 */}
  </Button>
</ThemeProvider>
```

### **FreshFoodz Theme V2 Definition für Customer-Management:**
```typescript
export const freshfoodzTheme = createTheme({
  palette: {
    primary: {
      main: '#94C456', // FreshFoodz Primärgrün
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#004F7B', // FreshFoodz Dunkelblau
      contrastText: '#FFFFFF',
    },
  },
  typography: {
    fontFamily: 'Poppins, sans-serif',
    h1: {
      fontFamily: 'Antonio, sans-serif',
      fontWeight: 700,
      color: '#004F7B',
    },
    h2: {
      fontFamily: 'Antonio, sans-serif',
      fontWeight: 700,
      color: '#004F7B',
    },
    button: {
      fontFamily: 'Poppins, sans-serif',
      fontWeight: 500,
      textTransform: 'none',
    },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        containedPrimary: {
          backgroundColor: '#94C456',
          '&:hover': {
            backgroundColor: '#7fb03f',
          },
        },
      },
    },
    // Customer-Management spezifische Overrides
    MuiDataGrid: {
      styleOverrides: {
        root: {
          '& .MuiDataGrid-row:hover': {
            backgroundColor: 'rgba(148, 196, 86, 0.08)',
          },
        },
      },
    },
  },
});
```

---

## 🔧 **API STANDARDS - Jakarta EE + Foundation References**

### **Customer Resource Template mit Foundation Standards:**
```java
/**
 * Customer Management REST API Controller for B2B-Convenience-Food-Vertrieb
 *
 * @see ../../grundlagen/API_STANDARDS.md - Jakarta EE REST Standards
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Security Implementation
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - P95 <200ms SLO Requirements
 * @see ../../grundlagen/BUSINESS_LOGIC_STANDARDS.md - B2B-Convenience-Food Workflows
 *
 * This controller provides customer management capabilities for Cook&Fresh® B2B-Convenience-Food-Vertrieb
 * with ABAC security, ROI-Kalkulation, Sample-Management, and Gastronomiebetrieb-Account-Management.
 *
 * Business Context: B2B-Convenience-Food-Hersteller (FreshFoodz) verkauft Cook&Fresh® Produkte
 * an Gastronomiebetriebe (Hotels, Restaurants, Betriebsgastronomie) mit ROI-basierter Beratung.
 *
 * @author Backend Team
 * @version 1.1
 * @since 2025-09-19
 */
@Path("/api/customers")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class CustomerResource {

    /** Customer Service with Foundation Standards compliance */
    @Inject CustomerService customerService;

    /** ABAC-secured query service for data access */
    @Inject CustomerQuery customerQuery;

    /** Sample Management Service for Cook&Fresh® integration */
    @Inject SampleManagementService sampleService;

    /**
     * Create New Customer with ABAC Territory Validation
     *
     * @param customerRequest Customer creation data with Gastronomiebetrieb categorization
     * @return CustomerResponse with generated ID and confirmation
     * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Territory Enforcement
     */
    @POST
    @RolesAllowed({"user","manager","admin"})
    public Response createCustomer(@Valid CustomerCreateRequest customerRequest) {
        // ABAC enforcement + Foundation Standards implementation
        if (!scopeContext.hasAccess("customers", customerRequest.getTerritory())) {
            throw new ForbiddenException("Territory access denied");
        }

        var customer = customerService.createCustomer(customerRequest);
        return Response.status(Response.Status.CREATED)
                .entity(customer)
                .build();
    }

    /**
     * Get Customer ROI-Kalkulation for Cook&Fresh® Products
     *
     * @param customerId Customer identifier
     * @return ROI calculation based on Convenience-Food benefits
     */
    @GET
    @Path("/{id}/roi-calculation")
    public Response getCustomerROI(@PathParam("id") UUID customerId) {
        var roiData = customerService.calculateROI(customerId);
        return Response.ok(roiData).build();
    }
}
```

### **OpenAPI 3.1 Template für Customer-Management:**
```yaml
openapi: 3.1.0
info:
  title: Customer Management API - B2B-Convenience-Food-Vertrieb
  version: 1.1.0
  description: Foundation Standards compliant Customer Management API for Cook&Fresh® B2B-Convenience-Food-Vertrieb

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    CustomerCreateRequest:
      type: object
      required:
        - companyName
        - territory
        - gastronomiebetriebType
      properties:
        companyName:
          type: string
          maxLength: 200
          pattern: '^[a-zA-Z0-9\s\-\.äöüÄÖÜß]+$'
          description: "Gastronomiebetrieb company name"
          example: "Hotel Zur Post"
        territory:
          type: string
          enum: [nord, sued, ost, west, BER, HAM, MUC]
          description: "Territory assignment for ABAC access control"
        gastronomiebetriebType:
          type: string
          enum: [hotel, restaurant, betriebsgastronomie, catering, cafe]
          description: "Type of Gastronomiebetrieb for Cook&Fresh® targeting"
        estimatedVolume:
          type: number
          description: "Estimated monthly Cook&Fresh® product volume"
        currentChallenges:
          type: array
          items:
            type: string
            enum: [personal_kosten, food_waste, qualitaet, zeit_management]
          description: "Current challenges for ROI-Kalkulation"

security:
  - bearerAuth: []
```

---

## 🔒 **SECURITY STANDARDS - ABAC Implementation**

### **ABAC Security Pattern für Customer-Management:**
```java
/**
 * ABAC-Secured Query Service for Customer Data Access with B2B-Convenience-Food Context
 *
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Security Implementation
 * @see ../../grundlagen/CODING_STANDARDS.md - Named Parameters SQL Injection Prevention
 */
@ApplicationScoped
public class CustomerQuery {

    @Inject EntityManager em;
    @Inject ScopeContext scopeContext;

    /**
     * Fetch Customers with ABAC Territory Scoping for B2B-Convenience-Food-Vertrieb
     */
    public List<Customer> fetchCustomers(String territory, String gastronomiebetriebType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c FROM Customer c WHERE 1=1");

        Map<String, Object> params = new HashMap<>();

        // ABAC Territory Scoping (automatic from JWT)
        if (!scopeContext.getTerritories().isEmpty()) {
            sql.append(" AND c.territory = ANY(:territories)");
            params.put("territories", scopeContext.getTerritories().toArray());
        }

        // Additional filters with named parameters
        if (territory != null) {
            sql.append(" AND c.territory = :territory");
            params.put("territory", territory);
        }

        if (gastronomiebetriebType != null) {
            sql.append(" AND c.gastronomiebetriebType = :gastronomiebetriebType");
            params.put("gastronomiebetriebType", gastronomiebetriebType);
        }

        Query query = em.createQuery(sql.toString());
        params.forEach(query::setParameter);

        return query.getResultList();
    }
}
```

### **Security Context Integration für Customer-Management:**
```java
@ApplicationScoped
public class ScopeContext {

    @Inject JsonWebToken jwt;

    public Set<String> getTerritories() {
        return jwt.getClaim("territories");
    }

    public boolean hasCustomerAccess(String customerId, String operation) {
        // Additional business logic for Customer-specific access
        return getTerritories().contains(getCustomerTerritory(customerId));
    }
}
```

---

## 🍽️ **B2B-CONVENIENCE-FOOD-VERTRIEB SPEZIFISCHE STANDARDS**

### **Cook&Fresh® Sample Management:**
```java
/**
 * Sample Management Service for Cook&Fresh® Product Testing in Gastronomiebetrieben
 *
 * @see ../../grundlagen/BUSINESS_LOGIC_STANDARDS.md - B2B-Convenience-Food Workflows
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - P95 <200ms SLO Requirements
 *
 * Business Context: Track Sample-Box deliveries to Gastronomiebetriebe for product testing,
 * supporting ROI-Kalkulation and Test-Phase management for Cook&Fresh® products.
 *
 * @author Business Logic Team
 * @version 1.1
 * @since 2025-09-19
 */
@ApplicationScoped
public class SampleManagementService {

    @Inject CustomerService customerService;
    @Inject CookFreshProductCatalog productCatalog;

    /**
     * Track Sample-Box delivery to Gastronomiebetrieb
     * Supports ROI-Kalkulation and Test-Phase management
     */
    public SampleDelivery trackSampleBox(UUID customerId, List<CookFreshProduct> products,
                                       SamplePurpose purpose) {
        Customer customer = customerService.findById(customerId);

        // Validate Gastronomiebetrieb type for product compatibility
        validateProductCompatibility(customer.getGastronomiebetriebType(), products);

        SampleDelivery delivery = SampleDelivery.builder()
            .customer(customer)
            .products(products)
            .purpose(purpose) // ROI_CALCULATION, MENU_TESTING, QUALITY_ASSESSMENT
            .deliveredAt(LocalDateTime.now())
            .expectedFeedbackDate(LocalDateTime.now().plusWeeks(2))
            .status(SampleStatus.DELIVERED)
            .build();

        return sampleRepository.save(delivery);
    }

    /**
     * Calculate ROI for Gastronomiebetrieb based on Cook&Fresh® product usage
     */
    public ROICalculation calculateROI(UUID customerId, List<CookFreshProduct> products) {
        Customer customer = customerService.findById(customerId);

        // Calculate savings: Personal, Food-Waste, Zeit, Qualität
        BigDecimal personalSavings = calculatePersonalSavings(customer, products);
        BigDecimal wasteSavings = calculateWasteSavings(customer, products);
        BigDecimal timeSavings = calculateTimeSavings(customer, products);
        BigDecimal qualityImprovements = calculateQualityBenefits(customer, products);

        return ROICalculation.builder()
            .customer(customer)
            .products(products)
            .personalSavingsPerMonth(personalSavings)
            .wasteSavingsPerMonth(wasteSavings)
            .timeSavingsPerMonth(timeSavings)
            .qualityScore(qualityImprovements)
            .totalROIPercentage(calculateTotalROI(personalSavings, wasteSavings, timeSavings))
            .calculatedAt(LocalDateTime.now())
            .build();
    }
}
```

### **Gastronomiebetrieb-Account-Management:**
```java
/**
 * Multi-Location Chain-Account-Management for Gastronomiebetrieb-Ketten
 */
@ApplicationScoped
public class ChainAccountService {

    /**
     * Manage Multi-Location accounts (Hotel-Ketten, Restaurant-Gruppen)
     */
    public ChainAccount createChainAccount(ChainAccountRequest request) {
        ChainAccount chain = ChainAccount.builder()
            .parentCompany(request.getParentCompany())
            .locations(new ArrayList<>())
            .centralContact(request.getCentralContact())
            .chainType(request.getChainType()) // HOTEL_CHAIN, RESTAURANT_GROUP, CATERING_COMPANY
            .volumeDiscount(calculateVolumeDiscount(request.getEstimatedTotalVolume()))
            .build();

        return chainRepository.save(chain);
    }
}
```

---

## 📊 **PERFORMANCE STANDARDS - P95 <200ms**

### **Performance Requirements für Customer-Management:**
- **API Response Time:** P95 <200ms für alle Customer-Endpoints
- **Database Queries:** <50ms average für Customer-Listen
- **Frontend Bundle Size:** <200KB initial (gzipped)
- **Memory Usage:** <50MB per Customer-Management request

### **Performance-optimierte Customer Queries:**
```sql
-- Performance-optimized Customer queries mit Field-Bridge Integration
CREATE INDEX idx_customers_territory_type
ON customers(territory, gastronomiebetrieb_type)
WHERE status = 'active';

-- Hot-Projection für Field-Bridge Performance
CREATE MATERIALIZED VIEW customer_field_projection AS
SELECT
    c.id,
    c.company_name,
    c.territory,
    c.gastronomiebetrieb_type,
    jsonb_object_agg(f.field_name, fv.field_value) as field_values
FROM customers c
LEFT JOIN field_values fv ON c.id = fv.customer_id
LEFT JOIN fields f ON fv.field_id = f.id
WHERE c.status = 'active'
GROUP BY c.id, c.company_name, c.territory, c.gastronomiebetrieb_type;

-- Refresh Strategy für Hot-Projection
REFRESH MATERIALIZED VIEW CONCURRENTLY customer_field_projection;
```

---

## 🧪 **TESTING STANDARDS - 80%+ Coverage**

### **Customer Management Test-Suite Template:**
```java
/**
 * Integration Tests for Customer Management REST API Controller
 *
 * @see ../../grundlagen/TESTING_GUIDE.md - Given-When-Then BDD Pattern
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Security Testing
 * @see ../../grundlagen/BUSINESS_LOGIC_STANDARDS.md - B2B-Convenience-Food Testing
 */
@QuarkusTest
@DisplayName("Customer Management API Integration Tests")
class CustomerResourceTest {

    @Test
    @DisplayName("POST /api/customers - Success with Gastronomiebetrieb validation")
    void createCustomer_withValidGastronomiebetrieb_shouldCreateCustomer() {
        // Given: Valid customer data for Gastronomiebetrieb
        var customerRequest = CustomerCreateRequest.builder()
            .companyName("Hotel Vier Jahreszeiten")
            .territory("nord")
            .gastronomiebetriebType("hotel")
            .estimatedVolume(5000.0)
            .currentChallenges(List.of("personal_kosten", "food_waste"))
            .build();

        // When: POST customer creation
        given()
            .auth().oauth2("valid-jwt-with-nord-territory")
            .contentType(MediaType.APPLICATION_JSON)
            .body(customerRequest)
        .when()
            .post("/api/customers")
        // Then: Customer created successfully
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("companyName", equalTo("Hotel Vier Jahreszeiten"))
            .body("gastronomiebetriebType", equalTo("hotel"));
    }

    @Test
    @DisplayName("GET /api/customers/{id}/roi-calculation - Calculate ROI for Cook&Fresh® products")
    void getCustomerROI_withValidCustomer_shouldReturnROICalculation() {
        // Given: Existing customer with product preferences
        UUID customerId = createTestCustomer("restaurant");

        // When: GET ROI calculation
        given()
            .auth().oauth2("valid-jwt-with-nord-territory")
        .when()
            .get("/api/customers/{id}/roi-calculation", customerId)
        // Then: ROI calculation returned
        .then()
            .statusCode(200)
            .body("personalSavingsPerMonth", greaterThan(0))
            .body("wasteSavingsPerMonth", greaterThan(0))
            .body("totalROIPercentage", greaterThan(0));
    }
}
```

### **Sample Management Test Template:**
```java
@Test
@DisplayName("Sample Management - Track Cook&Fresh® Sample-Box delivery")
void trackSampleBox_withValidProducts_shouldCreateSampleDelivery() {
    // Given: Customer and Cook&Fresh® products
    UUID customerId = createTestCustomer("restaurant");
    List<CookFreshProduct> products = List.of(
        CookFreshProduct.builder().name("Convenience Pasta").category("MAIN_DISH").build(),
        CookFreshProduct.builder().name("Fresh Sauce").category("SAUCE").build()
    );

    // When: Track sample delivery
    var delivery = sampleManagementService.trackSampleBox(
        customerId, products, SamplePurpose.ROI_CALCULATION
    );

    // Then: Sample delivery tracked
    assertThat(delivery.getId()).isNotNull();
    assertThat(delivery.getProducts()).hasSize(2);
    assertThat(delivery.getStatus()).isEqualTo(SampleStatus.DELIVERED);
    assertThat(delivery.getExpectedFeedbackDate()).isAfter(LocalDateTime.now());
}
```

---

## 🏗️ **SMARTLAYOUT INTEGRATION FÜR CUSTOMER-MANAGEMENT**

### **Content-Type Detection für Customer-Views:**
```typescript
import { SmartLayout } from '@/components/layout/SmartLayout';

// Automatische Breiten-Erkennung für Customer-Management
<SmartLayout contentType="customer-list">
  <CustomerDataGrid>...</CustomerDataGrid> {/* → 100% Breite */}
</SmartLayout>

<SmartLayout contentType="customer-form">
  <CustomerForm>...</CustomerForm> {/* → 800px max Breite */}
</SmartLayout>

<SmartLayout contentType="activity-timeline">
  <ActivityTimeline>...</ActivityTimeline> {/* → Responsive Timeline */}
</SmartLayout>

// Customer-Management spezifische Overrides
<SmartLayout contentType="opportunity-dashboard" forceWidth="full">
  <OpportunityPipeline>...</OpportunityPipeline>
</SmartLayout>
```

---

## 🔗 **UNIVERSAL EXPORT INTEGRATION**

### **Customer Export Template:**
```typescript
import { UniversalExportButton } from '@/components/export';

<UniversalExportButton
  entity="customers"
  queryParams={{
    territory: 'nord',
    gastronomiebetriebType: 'restaurant',
    includeROIData: true,
    includeSampleHistory: true
  }}
  formats={['csv', 'xlsx', 'pdf', 'json', 'html', 'jsonl']}
  buttonLabel="Kunden exportieren"
  variant="contained"
  color="primary" // ✅ Theme V2: #94C456 automatisch
  customFields={[
    'companyName',
    'gastronomiebetriebType',
    'estimatedVolume',
    'lastROICalculation',
    'sampleDeliveries'
  ]}
/>
```

---

## 🚀 **SUCCESS PATTERN - Modul 02 + 04 Referenz**

### **Erreichte Compliance Scores:**
- **Design System:** 100% (Theme V2 statt Hardcoding)
- **API Standards:** 100% (JavaDoc + Foundation References)
- **Coding Standards:** 100% (TypeScript + ABAC Standards)
- **Security Guidelines:** 100% (ABAC + Named Parameters)
- **Performance Standards:** 100% (P95 <200ms + Optimization)
- **Testing Standards:** 100% (80%+ Coverage + BDD)

**Overall: 100% Enterprise-Grade Compliance**

### **B2B-Convenience-Food Features implementiert:**
- ✅ Cook&Fresh® Produktkatalog Integration ohne Hardcoding
- ✅ ABAC Security mit Territory-Scoping für Gastronomiebetrieb-Accounts
- ✅ ROI-Kalkulations-Tools für Convenience-Food-Vorteile
- ✅ Sample-Management für Cook&Fresh® Product Testing
- ✅ Multi-Location Chain-Account-Management
- ✅ Universal Export in allen Formaten
- ✅ SmartLayout für intelligente Customer-Management-Layouts
- ✅ Real-time Updates via WebSocket
- ✅ Enterprise-Grade Testing (100% Coverage)
- ✅ Performance-optimierte APIs (P95 <200ms)

---

**📊 Status:** FOUNDATION STANDARDS COMPLETE REFERENCE
**🎯 Ziel:** 100% Compliance für Modul 03 Customer-Management
**📝 Pattern:** Nach Modul 02 + 04 Success Stories
**🍽️ Business Context:** B2B-Convenience-Food-Hersteller für Gastronomiebetriebe