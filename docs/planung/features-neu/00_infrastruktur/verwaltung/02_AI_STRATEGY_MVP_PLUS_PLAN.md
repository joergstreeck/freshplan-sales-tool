# AI Strategy MVP-Plus - Implementation Plan

**📊 Plan Status:** 🟡 Architecture Complete, Ready for Implementation
**🎯 Owner:** Infrastructure Team + AI/ML Team
**⏱️ Timeline:** Q1 2026 (3-4 Wochen nach Settings Registry)
**🔧 Effort:** S (Small-Medium)

## 🎯 Executive Summary (für Claude)

**Mission:** AI Cost Control (€600-1200/month) + Confidence Routing für 2-3x Kostensenkung
**Problem:** AI-Kosten unkontrolliert, keine Smart Routing, Module 07 CAR-Strategy benötigt AI-Infrastructure
**Solution:** MVP-Plus mit Budget Gates + Small-first Routing + Provider Fallbacks
**Timeline:** 3-4 Wochen nach Settings Registry (Q1 2026)
**Impact:** Predictable AI Costs + Quality Assurance + Module 07 CAR-Strategy enabling

**Quick Context:** External AI Strategy (9.2/10) empfiehlt MVP-Plus statt Budget-only für sofortigen 2-3x ROI durch Smart Routing. Settings Registry Integration für ai.* configuration keys.

## 📋 Context & Dependencies

### Current State:
- Keine AI Cost Tracking oder Budget Controls
- Direkte Model-Calls ohne Routing Logic
- Module 07 CAR-Strategy blocked ohne AI Infrastructure
- Keine Provider Fallback oder Reliability Patterns
- AI-Kosten unpredictable und nicht controllierbar

### Target State:
- €600-1200/month Budget Control per Organization
- Small-first Routing (0.7 confidence) → Large fallback
- Provider Abstraction (OpenAI ↔ Anthropic fallback)
- Real-time Cost Tracking + KPI Dashboards
- Module 07 CAR-Strategy AI Infrastructure ready

### Dependencies:
- ✅ [Settings Registry MVP](./01_SETTINGS_REGISTRY_MVP_PLAN.md) - ai.* settings required
- 🔄 [Module 07 CAR-Strategy](../../../07_hilfe_support/) - Primary consumer
- ✅ [External AI Providers](./artefakte/AI_STRATEGY_FINAL.md) - OpenAI + Anthropic accounts
- 📋 [Cost Tracking Database](../../database/) - New tables for ai_costs

## 🛠️ Implementation Phases

### Phase 1: Budget Infrastructure (Week 1)
**Goal:** AI Budget Tracking + Enforcement operational

**Actions:**
- [ ] Create ai_costs table für cost tracking per org/user
- [ ] Implement AIBudgetService mit monthly cap enforcement
- [ ] Setup ai.budget.monthly.cap settings in Registry
- [ ] Basic cost calculation + tracking logic

**Code Changes:**
```java
@ApplicationScoped
public class AIBudgetService {
    public boolean checkBudget(String orgId, BigDecimal requestCost) {
        // Get monthly cap from Settings Registry
        BigDecimal cap = settingsService.getOrDefault("ai.budget.monthly.cap", orgId);
        BigDecimal currentUsage = getCurrentMonthUsage(orgId);
        return currentUsage.add(requestCost).compareTo(cap) <= 0;
    }
}
```

**Success Criteria:**
- Budget enforcement blocks requests bei cap exceeded
- Cost tracking accurate per organization
- Settings integration für budget configuration

### Phase 2: Confidence Routing (Week 2)
**Goal:** Small-first Routing mit Large fallback operational

**Actions:**
- [ ] Implement AIRoutingService mit confidence-based decisions
- [ ] Provider Adapters für OpenAI + Anthropic
- [ ] ai.routing.confidence.threshold settings integration
- [ ] Cache Strategy für AI responses (TTL 8h)

**Code Changes:**
```java
@ApplicationScoped
public class AIRoutingService {
    public AIResponse route(AIRequest request) {
        if (request.requiresLargeModel()) return callLargeModel(request);

        AIResponse small = callSmallModel(request);
        double threshold = getConfidenceThreshold(request.userId());

        if (small.confidence() >= threshold) return small;

        // Fallback to large if budget allows
        if (budgetService.checkBudget(request.orgId(), LARGE_MODEL_COST)) {
            return callLargeModel(request);
        }
        return small; // Return even with low confidence if budget exceeded
    }
}
```

**Success Criteria:**
- Small model routing working mit confidence thresholds
- Large model fallback nur bei budget availability
- Provider abstraction allows OpenAI/Anthropic switching

### Phase 3: Advanced Features (Week 3)
**Goal:** Provider Fallback + KPIs operational

**Actions:**
- [ ] Health Probing für provider availability
- [ ] Automatic failover logic (OpenAI ↔ Anthropic)
- [ ] Circuit Breaker patterns für provider issues
- [ ] KPI Metrics: Cost/Lead, Cost/Order, confidence histograms

**Code Changes:**
```java
@ApplicationScoped
public class AIProviderService {
    public AIResponse callWithFallback(AIRequest request) {
        try {
            return primaryProvider.call(request);
        } catch (ProviderException e) {
            log.warn("Primary provider failed, trying fallback", e);
            return fallbackProvider.call(request);
        }
    }
}
```

**Success Criteria:**
- Provider failover working automatisch
- KPI metrics collection operational
- Circuit breaker prevents cascade failures

### Phase 4: Module 07 Integration (Week 4)
**Goal:** CAR-Strategy AI Infrastructure ready

**Actions:**
- [ ] Module 07 AI Integration für CAR-Strategy
- [ ] help.space.* settings für nudge budgets
- [ ] Proactive assistance confidence tuning
- [ ] Production monitoring + alerting

**Success Criteria:**
- Module 07 CAR-Strategy using AI Infrastructure
- Nudge budget management working
- Production monitoring shows cost compliance

## ✅ Success Metrics

### Technical KPIs:
- AI Routing decision p95 <300ms
- Budget tracking accuracy >99%
- Provider failover <500ms recovery time
- Cache hit rate >60% für repeated queries
- Cost calculation accuracy within 1% actual billing

### Business KPIs:
- AI costs within €600-1200/month budget
- 2-3x cost reduction through smart routing
- Module 07 CAR-Strategy enabled + functional
- Cost/Lead + Cost/Order metrics available
- Provider reliability >99.5% (with fallback)

### Quality Gates:
- All unit tests passing >80% coverage
- Integration tests mit real provider APIs
- Cost tracking validation mit mock billing
- Performance tests für routing decisions

## 🔗 Related Documentation

**Foundation:**
- → [AI Strategy SoT](./artefakte/AI_STRATEGY_FINAL.md)
- → [Settings Registry Integration](./01_SETTINGS_REGISTRY_MVP_PLAN.md)
- → [External AI Discussion](./diskussionen/2025-09-20_GOVERNANCE_STRATEGIEDISKUSSION_VORBEREITUNG.md)

**Module Integration:**
- → [Module 07 CAR-Strategy](../../../07_hilfe_support/) - Primary AI consumer
- → [Module 02 Lead Classification](../../../02_neukundengewinnung/) - AI use case
- → [Module 05 Communication](../../../05_kommunikation/) - AI-assisted features

**Cost Management:**
- → [Performance Standards](../../../../grundlagen/PERFORMANCE_STANDARDS.md)
- → [Budget Approval Workflows](../../operations/artefakte/OPERATIONS_RUNBOOK.md)

## 🤖 Claude Handover Section

**Context:** AI Strategy MVP-Plus nach Strategic Discussion mit External AI entwickelt. Nicht nur Budget-Control sondern Smart Routing für sofortigen 2-3x ROI. Settings Registry Integration für configuration management.

**Architecture Decision:** MVP-Plus (Budget + Routing) statt Budget-only weil Smart Routing sofortigen messbaren Value liefert bei geringer zusätzlicher Komplexität. Provider Fallback für Enterprise Reliability.

**Module 07 Dependency:** CAR-Strategy requires AI Infrastructure für proactive assistance. help.space.* settings für nudge budgets. Confidence-based quality assurance für User Experience.

**Cost Control:** €600-1200/month realistic budget based auf Expected Usage. Hard budget caps prevent cost overruns. Real-time tracking für predictable monthly costs.