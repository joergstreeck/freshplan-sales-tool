# 📚 Foundation Standards Reference Bundle - Modul 02

**📅 Datum:** 2025-09-19
**🎯 Zweck:** Komplette Foundation Standards Referenz für KI-Implementation
**📊 Status:** Production-Ready Standards aus Modul 04 Success Story

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

### **FreshFoodz Theme V2 Definition:**
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
  },
});
```

---

## 🔧 **API STANDARDS - Jakarta EE + Foundation References**

### **Controller Template mit Foundation Standards:**
```java
/**
 * Lead Management REST API Controller
 *
 * @see ../../grundlagen/API_STANDARDS.md - Jakarta EE REST Standards
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - RBAC Security Implementation
 * @see ../../grundlagen/PERFORMANCE_STANDARDS.md - P95 <200ms SLO Requirements
 *
 * This controller provides lead management capabilities with ABAC security
 * and follows OpenAPI 3.1 specifications for B2B food industry workflows.
 *
 * @author Backend Team
 * @version 1.1
 * @since 2025-09-19
 */
@Path("/api/leads")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class LeadResource {

    /** Lead Service with Foundation Standards compliance */
    @Inject LeadService leadService;

    /** ABAC-secured query service for data access */
    @Inject LeadQuery leadQuery;

    /**
     * Create New Lead with ABAC Territory Validation
     *
     * @param leadRequest Lead creation data with territory assignment
     * @return LeadResponse with generated ID and confirmation
     * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Territory Enforcement
     */
    @POST
    @RolesAllowed({"user","manager","admin"})
    public Response createLead(@Valid LeadCreateRequest leadRequest) {
        // ABAC enforcement + Foundation Standards implementation
        if (!scopeContext.hasAccess("leads", leadRequest.getTerritory())) {
            throw new ForbiddenException("Territory access denied");
        }

        var lead = leadService.createLead(leadRequest);
        return Response.status(Response.Status.CREATED)
                .entity(lead)
                .build();
    }
}
```

### **OpenAPI 3.1 Template:**
```yaml
openapi: 3.1.0
info:
  title: Lead Management API
  version: 1.1.0
  description: Foundation Standards compliant Lead Management API

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    LeadCreateRequest:
      type: object
      required:
        - companyName
        - territory
        - contactEmail
      properties:
        companyName:
          type: string
          maxLength: 200
          pattern: '^[a-zA-Z0-9\s\-\.]+$'
        territory:
          type: string
          enum: [nord, sued, ost, west]
        contactEmail:
          type: string
          format: email

security:
  - bearerAuth: []
```

---

## 🔒 **SECURITY STANDARDS - ABAC Implementation**

### **ABAC Security Pattern:**
```java
/**
 * ABAC-Secured Query Service for Lead Data Access
 *
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - ABAC Security Implementation
 * @see ../../grundlagen/CODING_STANDARDS.md - Named Parameters SQL Injection Prevention
 */
@ApplicationScoped
public class LeadQuery {

    @Inject EntityManager em;
    @Inject ScopeContext scopeContext;

    /**
     * Fetch Leads with ABAC Territory Scoping
     */
    public List<Lead> fetchLeads(String territory, String status) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT l FROM Lead l WHERE 1=1");

        Map<String, Object> params = new HashMap<>();

        // ABAC Territory Scoping (automatic from JWT)
        if (!scopeContext.getTerritories().isEmpty()) {
            sql.append(" AND l.territory = ANY(:territories)");
            params.put("territories", scopeContext.getTerritories().toArray());
        }

        // Additional filters with named parameters
        if (territory != null) {
            sql.append(" AND l.territory = :territory");
            params.put("territory", territory);
        }

        if (status != null) {
            sql.append(" AND l.status = :status");
            params.put("status", status);
        }

        Query query = em.createQuery(sql.toString());
        params.forEach(query::setParameter);

        return query.getResultList();
    }
}
```

### **Security Context Integration:**
```java
@ApplicationScoped
public class ScopeContext {

    @Inject JsonWebToken jwt;

    public Set<String> getTerritories() {
        return jwt.getClaim("territories");
    }

    public boolean hasAccess(String resource, String territory) {
        return getTerritories().contains(territory);
    }
}
```

---

## 📊 **PERFORMANCE STANDARDS - P95 <200ms**

### **Performance Requirements:**
- **API Response Time:** P95 <200ms für alle Endpoints
- **Database Queries:** <50ms average
- **Frontend Bundle Size:** <200KB initial (gzipped)
- **Memory Usage:** <50MB per request

### **Performance-optimierte Queries:**
```sql
-- Performance-optimized Lead queries with indices
CREATE INDEX idx_leads_territory_status
ON leads(territory, status)
WHERE status IN ('active', 'qualified');

-- Cursor-based pagination for large datasets
SELECT * FROM leads
WHERE territory = :territory
  AND id > :cursor
ORDER BY id ASC
LIMIT :limit;
```

---

## 🧪 **TESTING STANDARDS - 80%+ Coverage**

### **Test-Suite Template:**
```java
/**
 * Integration Tests for Lead REST API Controller
 *
 * @see ../../grundlagen/TESTING_GUIDE.md - Given-When-Then BDD Pattern
 * @see ../../grundlagen/SECURITY_GUIDELINES.md - RBAC Security Testing
 */
@QuarkusTest
@DisplayName("Lead API Integration Tests")
class LeadResourceTest {

    @Test
    @DisplayName("POST /api/leads - Success with ABAC territory validation")
    void createLead_withValidTerritory_shouldCreateLead() {
        // Given: Valid lead data for accessible territory
        var leadRequest = LeadCreateRequest.builder()
            .companyName("Restaurant Alpha")
            .territory("nord")
            .contactEmail("info@restaurant-alpha.de")
            .build();

        // When: POST lead creation
        given()
            .auth().oauth2("valid-jwt-with-nord-territory")
            .contentType(MediaType.APPLICATION_JSON)
            .body(leadRequest)
        .when()
            .post("/api/leads")
        // Then: Lead created successfully
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("companyName", equalTo("Restaurant Alpha"))
            .body("territory", equalTo("nord"));
    }

    @Test
    @DisplayName("POST /api/leads - ABAC territory access denied")
    void createLead_withForbiddenTerritory_shouldReturn403() {
        // Given: Lead data for inaccessible territory
        var leadRequest = LeadCreateRequest.builder()
            .territory("sued") // User only has 'nord' access
            .build();

        // When: POST with forbidden territory
        given()
            .auth().oauth2("valid-jwt-with-nord-territory-only")
            .body(leadRequest)
        .when()
            .post("/api/leads")
        // Then: Access denied
        .then()
            .statusCode(403);
    }
}
```

### **Frontend Testing Template:**
```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { ThemeProvider } from '@mui/material/styles';
import { freshfoodzTheme } from '@/theme/freshfoodz';

/**
 * Lead Form Component Tests with Theme V2 and Accessibility
 */
describe('LeadForm Component', () => {
  const renderWithTheme = (component: React.ReactElement) => {
    return render(
      <ThemeProvider theme={freshfoodzTheme}>
        {component}
      </ThemeProvider>
    );
  };

  it('should render with FreshFoodz Theme V2 colors', () => {
    // Given: Lead form with Theme V2
    renderWithTheme(<LeadForm />);

    // When: Form is rendered
    const submitButton = screen.getByRole('button', { name: /lead erstellen/i });

    // Then: Theme V2 applied (primary color #94C456)
    expect(submitButton).toHaveAttribute('color', 'primary');
  });

  it('should handle form submission with validation', async () => {
    // Given: Valid form data
    renderWithTheme(<LeadForm onSubmit={mockSubmit} />);

    // When: Fill and submit form
    fireEvent.change(screen.getByLabelText(/firmenname/i), {
      target: { value: 'Restaurant Beta' }
    });
    fireEvent.click(screen.getByRole('button', { name: /erstellen/i }));

    // Then: Validation and submission
    await waitFor(() => {
      expect(mockSubmit).toHaveBeenCalledWith({
        companyName: 'Restaurant Beta'
      });
    });
  });
});
```

---

## 📐 **CODING STANDARDS - TypeScript + Clean Code**

### **TypeScript Standards:**
```typescript
// ✅ RICHTIG: import type für Types (Vite-kompatibel)
import type { FC } from 'react';
import type { LeadCreateRequest } from '@/types/leads';

// ✅ RICHTIG: Interface mit Foundation Standards
interface LeadFormProps {
  onSubmit: (lead: LeadCreateRequest) => Promise<void>;
  initialData?: Partial<LeadCreateRequest>;
}

// ✅ RICHTIG: Component mit Foundation Standards
export const LeadForm: FC<LeadFormProps> = ({ onSubmit, initialData }) => {
  // Implementation with Theme V2 and Foundation Standards
};
```

### **Clean Code Principles:**
- **Single Responsibility:** Eine Verantwortung pro Klasse/Komponente
- **DRY:** Don't Repeat Yourself - gemeinsame Logik extrahieren
- **SOLID:** Dependency Injection, Interface Segregation
- **Naming:** Sprechende Namen für Variablen und Methoden

---

## 🏗️ **SMARTLAYOUT INTEGRATION**

### **Content-Type Detection:**
```typescript
import { SmartLayout } from '@/components/layout/SmartLayout';

// Automatische Breiten-Erkennung
<SmartLayout>
  <Table>...</Table> {/* → 100% Breite */}
</SmartLayout>

<SmartLayout>
  <form>...</form> {/* → 800px max Breite */}
</SmartLayout>

// Override bei Bedarf
<SmartLayout forceWidth="full">
  <Dashboard>...</Dashboard>
</SmartLayout>
```

---

## 🔗 **UNIVERSAL EXPORT INTEGRATION**

### **Export-Button Template:**
```typescript
import { UniversalExportButton } from '@/components/export';

<UniversalExportButton
  entity="leads"
  queryParams={{ territory: 'nord', status: 'active' }}
  formats={['csv', 'xlsx', 'pdf', 'json', 'html', 'jsonl']}
  buttonLabel="Leads exportieren"
  variant="contained"
  color="primary" // ✅ Theme V2: #94C456 automatisch
/>
```

---

## 🚀 **SUCCESS PATTERN - Modul 04 Referenz**

### **Compliance Scores erreicht:**
- **Design System:** 95% (Theme V2 statt Hardcoding)
- **API Standards:** 98% (JavaDoc + Foundation References)
- **Coding Standards:** 96% (TypeScript + ABAC Standards)
- **Security Guidelines:** 95% (ABAC + Named Parameters)
- **Performance Standards:** 90% (P95 <200ms + Optimization)
- **Testing Standards:** 85% (80%+ Coverage + BDD)

**Overall: 92% Enterprise-Grade Compliance**

### **Implementierte Features:**
- ✅ FreshFoodz Theme V2 ohne Hardcoding
- ✅ ABAC Security mit Territory-Scoping
- ✅ Universal Export in allen Formaten
- ✅ SmartLayout für intelligente Layouts
- ✅ Real-time Updates via WebSocket
- ✅ Enterprise-Grade Testing (85% Coverage)
- ✅ Performance-optimierte APIs (P95 <200ms)

---

**📊 Status:** FOUNDATION STANDARDS COMPLETE REFERENCE
**🎯 Ziel:** Identische 92% Compliance für Modul 02
**📝 Pattern:** Exakt nach Modul 04 Success Story