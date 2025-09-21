# Governance Mini-Modul: Grundlagen-Integration

**📅 Datum:** 2025-09-20
**🎯 Zweck:** Integration der `/grundlagen` Governance-relevanten Dokumente
**📊 Status:** Analyse für Strukturplanung

## 🔍 Relevante Grundlagen-Dokumente

### ✅ **BUSINESS_LOGIC_STANDARDS.md (9KB) - CORE BUSINESS RULES**

#### **Neue Rabattlogik (ab 01.10.2025):**
```yaml
Fundamentaler Systemwechsel:
  ❌ ENTFÄLLT:
    - Calculator-basierte Bestellwert-Rabatte
    - Vorlaufzeit-Rabatte (Frühbucher)
    - Pickup-Rabatt
    - Partnerschaftsvereinbarung

  ✅ NEU:
    - Jahresumsatz-basierte Rabattstufen (2-10%)
    - Rückvergütungssystem als Jahresbonus (1-5%)
    - Welcome-Bonus für Neukunden (6 Monate)
    - Skonto bei Lastschrift (1%)
    - Nur noch AGBs (keine Partnerschaftsvereinbarung)
```

#### **Rabattstufen-System (Settings Registry Relevant):**
```yaml
Jahresumsatz-Basierte Rabatte:
  - bis 50.000€: 2% Sofortrabatt
  - bis 100.000€: 4% Sofortrabatt
  - bis 250.000€: 6% Sofortrabatt
  - bis 500.000€: 8% Sofortrabatt
  - über 500.000€: 10% Sofortrabatt

Settings Keys (benötigt):
  - business.discount.annual.tiers[]
  - business.discount.welcome.duration (6 Monate)
  - business.discount.skonto.rate (1%)
  - business.discount.bonus.rates[] (1-5%)
```

#### **Integration mit Settings Registry:**
- 🔄 **Business Rules Engine:** Separate als Modul 09
- ✅ **Settings Storage:** Rabatt-Parameter in Registry
- ✅ **Governance:** Policy vs. Implementation Trennung

### ✅ **API_STANDARDS.md (25KB) - TECHNICAL GOVERNANCE**

#### **REST API Design Standards:**
```yaml
Resource Naming:
  - Plural nouns: /api/customers, /api/opportunities
  - Consistent patterns: /api/{resource}/{id}/{sub-resource}
  - Query parameters: ?filter, ?sort, ?page, ?size

HTTP Methods:
  - GET: Retrieve data
  - POST: Create new resources
  - PUT: Update entire resource
  - PATCH: Partial updates
  - DELETE: Remove resources

Response Formats:
  - Standard JSON structure
  - Error response patterns
  - Pagination metadata
  - ETag support for caching
```

#### **Settings API Requirements:**
```yaml
Settings Admin API:
  - GET /api/settings/effective (mit ETag)
  - PATCH /api/settings (mit JSON-Schema Validation)
  - Scope-basierte Zugriffsrechte
  - Consistent error responses
```

### ✅ **CODING_STANDARDS.md (10KB) - IMPLEMENTATION GOVERNANCE**

#### **Java/Quarkus Standards:**
```yaml
Code Style:
  - Google Java Style Guide
  - Spotless formatting automation
  - 80-character line length
  - Consistent naming conventions

Architecture Patterns:
  - Clean Architecture principles
  - Domain-driven design
  - Service layer separation
  - Repository pattern with Panache

Annotations:
  - @ApplicationScoped for services
  - @Transactional for data operations
  - @RolesAllowed for security
  - @ConfigProperty for configuration
```

#### **TypeScript Standards:**
```yaml
Import Conventions:
  - import type für Vite compatibility
  - Explicit type imports
  - Consistent module organization
  - Interface over type aliases
```

### ✅ **DEVELOPMENT_WORKFLOW.md (13KB) - PROCESS GOVERNANCE**

#### **Development Process:**
```yaml
Branch Strategy:
  - Feature branches: feature/description
  - Main branch protection
  - Pull request reviews required
  - CI checks mandatory

Code Review:
  - Two-pass review process
  - Automated formatting (Spotless)
  - Manual architecture review
  - Security considerations

Release Process:
  - Semantic versioning
  - Automated deployment
  - Database migrations
  - Rollback procedures
```

## 🎯 Integration Strategy für Governance-Planung

### **Phase 1: Settings Registry für Business Logic**
- Business Rules als Settings-Keys
- API Standards für Settings Admin API
- Coding Standards für Implementation

### **Phase 2: Business Rules Engine (Modul 09)**
- Rabatt-Logik als separates Modul
- Settings Registry als Policy Store
- Clean separation: Policy vs. Logic

### **Phase 3: Development Governance**
- Workflow Standards enforcement
- Code Quality automation
- Review Process optimization

## 📊 Gap-Analysis: Grundlagen vs. Governance Strategy

| Component | Grundlagen | Governance Strategy | Integration |
|-----------|------------|-------------------|-------------|
| Business Rules | Documented standards | Settings Registry storage | ✅ Perfect fit |
| API Design | REST standards | Settings Admin API | ✅ Consistent |
| Code Quality | Style guides | Implementation standards | ✅ Enforced |
| Workflow | Process documented | CI/CD automation | 🔄 Enhancement |
| Security | RBAC patterns | ABAC + Settings scopes | 🔄 Extension |

## 📋 Action Items für Governance Technical Concept

1. **Business Logic Storage:** Rabatt-Parameter in Settings Registry
2. **API Consistency:** Settings API follows API_STANDARDS.md
3. **Code Quality:** Implementation follows CODING_STANDARDS.md
4. **Process Automation:** Workflow governance automation
5. **Separation Strategy:** Business Rules Engine als Modul 09

## 🎯 Settings Registry Keys für Business Logic

```yaml
# Aus BUSINESS_LOGIC_STANDARDS.md abgeleitet:
business.discount.annual.tiers:
  type: list
  scope: global
  default: [
    {threshold: 50000, rate: 0.02},
    {threshold: 100000, rate: 0.04},
    {threshold: 250000, rate: 0.06},
    {threshold: 500000, rate: 0.08},
    {threshold: 999999999, rate: 0.10}
  ]

business.discount.welcome.duration:
  type: scalar
  scope: global
  default: {months: 6}

business.discount.skonto.rate:
  type: scalar
  scope: global
  default: 0.01
```

---

**💡 Erkenntnisse:** Governance braucht Business Logic Integration - Settings Registry als Policy Store optimal