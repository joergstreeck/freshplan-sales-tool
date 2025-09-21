# Business Logic Governance - Implementation Plan

**📊 Plan Status:** 🟡 Architecture Designed, Implementation Ready
**🎯 Owner:** Business Logic Team + Infrastructure Team
**⏱️ Timeline:** Q2 2026 (3-5 Wochen nach AI Strategy)
**🔧 Effort:** M (Medium)

## 🎯 Executive Summary (für Claude)

**Mission:** Business Logic Governance für neue Rabattlogik ab 01.10.2025 + Policy Store Integration
**Problem:** Business Rules hardcoded, neue Rabattlogik braucht zentrale Governance, Calculator-Migration erforderlich
**Solution:** Settings Registry als Policy Store + separates Business Rules Engine (Modul 09)
**Timeline:** Q2 2026 nach Settings + AI Infrastructure (3-5 Wochen)
**Impact:** Policy-driven Business Logic + zentrale Governance + neue Rabattlogik operational

**Quick Context:** Grundlagen Integration shows Business Logic Standards ab 01.10.2025 require Settings Registry als Policy Store. Clean Architecture: Policy (Governance) vs. Logic (Business Rules Engine).

## 📋 Context & Dependencies

### Current State:
- Business Rules hardcoded in verschiedenen Services
- Calculator-basierte Rabattlogik (deprecated ab 01.10.2025)
- Keine zentrale Policy-Verwaltung
- Neue Rabattlogik (Jahresumsatz-basiert) needs governance
- Business Rules Testing kompliziert durch Code-coupling

### Target State:
- Settings Registry als central Policy Store
- Business Rules Engine als separates Modul 09
- Neue Rabattlogik policy-driven + testable
- Clean separation: Policy (Governance) vs. Logic (Implementation)
- Real-time Business Rules updates via Settings

### Dependencies:
- ✅ [Settings Registry MVP](./01_SETTINGS_REGISTRY_MVP_PLAN.md) - Policy Store required
- ✅ [Business Logic Standards](../../../../grundlagen/BUSINESS_LOGIC_STANDARDS.md) - Neue Rabattlogik defined
- 📋 [Modul 09 Business Rules Engine](./04_BUSINESS_RULES_ENGINE_PLAN.md) - Implementation module
- 🔄 [Calculator Migration](./analyse/01_BUSINESS_LOGIC_GOVERNANCE_ANALYSIS.md) - Legacy removal

## 🛠️ Implementation Phases

### Phase 1: Policy Store Setup (Week 1)
**Goal:** Business Logic Settings in Registry verfügbar

**Actions:**
- [ ] Define business.* settings schema in Registry
- [ ] Implement neue Rabattlogik als Settings (2-10% Staffelung)
- [ ] Setup business.discount.* policy keys
- [ ] Migration strategy für existing business rules

**Code Changes:**
```yaml
# Settings Registry Seeds for Business Logic
business.discount.annual.tiers:
  type: list
  scope: ["global"]
  default: [
    {threshold: 50000, rate: 0.02},
    {threshold: 100000, rate: 0.04},
    {threshold: 250000, rate: 0.06},
    {threshold: 500000, rate: 0.08},
    {threshold: 999999999, rate: 0.10}
  ]

business.discount.welcome.duration:
  type: scalar
  scope: ["global"]
  default: {months: 6}

business.discount.skonto.rate:
  type: scalar
  scope: ["global"]
  default: 0.01
```

**Success Criteria:**
- Business Logic Settings in Registry verfügbar
- JSON Schema validation für business rules
- Policy updates ohne Code deployment möglich

### Phase 2: Business Rules Engine Foundation (Week 2-3)
**Goal:** Modul 09 Business Rules Engine basic structure

**Actions:**
- [ ] Create Modul 09 als separates Business Rules Module
- [ ] Implement PolicyEvaluationService
- [ ] Settings Registry integration für rule fetching
- [ ] Basic rule evaluation patterns

**Code Changes:**
```java
@ApplicationScoped
public class PolicyEvaluationService {
    public DiscountResult evaluateDiscount(CustomerContext ctx) {
        // Fetch current discount tiers from Settings Registry
        List<DiscountTier> tiers = settingsService.getBusinessSetting(
            "business.discount.annual.tiers", ctx.orgId()
        );

        // Evaluate based on annual revenue
        return calculateDiscountForRevenue(ctx.annualRevenue(), tiers);
    }
}
```

**Success Criteria:**
- Modul 09 basic structure operational
- Policy evaluation working mit Settings integration
- Clear separation Policy Store vs. Business Logic

### Phase 3: Neue Rabattlogik Implementation (Week 3-4)
**Goal:** Jahresumsatz-basierte Rabattlogik operational

**Actions:**
- [ ] Implement neue Rabattlogik (2-10% Staffelung)
- [ ] Welcome-Bonus für Neukunden (6 Monate)
- [ ] Skonto bei Lastschrift (1%)
- [ ] Migration von Calculator-basierter Logic

**Code Changes:**
```java
@ApplicationScoped
public class DiscountCalculationService {
    public CustomerDiscount calculateCustomerDiscount(Customer customer) {
        // Annual revenue-based discount tiers
        BigDecimal annualRevenue = getAnnualRevenue(customer);
        List<DiscountTier> tiers = policyService.getDiscountTiers();

        DiscountRate baseRate = findApplicableTier(annualRevenue, tiers);

        // Welcome bonus for new customers
        if (isNewCustomer(customer)) {
            Duration welcomePeriod = policyService.getWelcomeDuration();
            if (withinWelcomePeriod(customer, welcomePeriod)) {
                baseRate = applyWelcomeBonus(baseRate);
            }
        }

        return new CustomerDiscount(baseRate, calculateSkonto(customer));
    }
}
```

**Success Criteria:**
- Neue Rabattlogik funktional + testable
- Welcome-Bonus + Skonto implementation working
- Calculator-Logic deprecated + migration path defined

### Phase 4: Production Integration (Week 4-5)
**Goal:** Business Logic Governance production-ready

**Actions:**
- [ ] Integration mit bestehenden Order/Invoice Services
- [ ] A/B Testing für neue vs. alte Rabattlogik
- [ ] Business Rules Admin UI für Policy Management
- [ ] Monitoring + KPIs für business rule evaluation

**Success Criteria:**
- Production deployment successful
- Business team kann policies via UI verwalten
- A/B testing shows neue Rabattlogik performance
- Zero business disruption during transition

## ✅ Success Metrics

### Technical KPIs:
- Policy evaluation performance <100ms p95
- Settings Registry integration <50ms für business rules
- Business Rules Engine uptime >99.9%
- A/B testing infrastructure operational
- Admin UI responsive <2s für policy updates

### Business KPIs:
- Neue Rabattlogik ab 01.10.2025 operational
- Business team self-service für policy updates
- Calculator-Logic vollständig deprecated
- Revenue impact neutral oder positive during transition
- Welcome-Bonus + Skonto features functional

### Quality Gates:
- Unit tests >90% coverage für business logic
- Integration tests mit real Settings Registry
- Business rule validation tests comprehensive
- Performance tests für policy evaluation
- Security tests für business data access

## 🔗 Related Documentation

**Foundation:**
- → [Business Logic Standards](../../../../grundlagen/BUSINESS_LOGIC_STANDARDS.md)
- → [Settings Registry Integration](./01_SETTINGS_REGISTRY_MVP_PLAN.md)
- → [Grundlagen Integration](./analyse/03_GRUNDLAGEN_INTEGRATION_GOVERNANCE.md)

**Implementation:**
- → [Business Rules Engine Plan](./04_BUSINESS_RULES_ENGINE_PLAN.md)
- → [Calculator Migration Analysis](./analyse/01_BUSINESS_LOGIC_GOVERNANCE_ANALYSIS.md)
- → [API Standards](../../../../grundlagen/API_STANDARDS.md)

**Business Context:**
- → [Neue Rabattlogik Specification](../../../../grundlagen/BUSINESS_LOGIC_STANDARDS.md#rabattsystem)
- → [Revenue Impact Analysis](../../../financial-planning/)

## 🤖 Claude Handover Section

**Context:** Business Logic Governance nach Strategic Decision für Clean Architecture: Policy Store (Settings Registry) vs. Implementation (Business Rules Engine). Neue Rabattlogik ab 01.10.2025 requires governance infrastructure.

**Architecture Pattern:** Settings Registry als Policy Store enables real-time business rule updates ohne Code deployment. Modul 09 als dedicated Business Rules Engine for clean separation of concerns.

**Business Impact:** Calculator-basierte Rabattlogik deprecated. Neue Jahresumsatz-basierte Staffelung (2-10%) + Welcome-Bonus + Skonto requires policy-driven implementation für business team self-service.

**Timeline Critical:** 01.10.2025 deadline für neue Rabattlogik. Settings Registry MVP must be ready Q4 2025, Business Logic Governance in Q2 2026 für rechtzeitige business rule transition.