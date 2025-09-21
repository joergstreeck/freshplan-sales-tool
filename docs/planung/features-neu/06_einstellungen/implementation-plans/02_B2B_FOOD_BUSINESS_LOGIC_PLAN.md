# 🏢 B2B-Food Business Logic Implementation Plan

**📊 Plan Status:** 🟢 Production-Ready
**🎯 Owner:** Business Logic Team + Domain Experts Team
**⏱️ Timeline:** Woche 2-3 (4-6h Implementation)
**🔧 Effort:** S (Small - Business-Rules + Multi-Contact + Territory-Logic)

## 🎯 Executive Summary (für Claude)

**Mission:** B2B-Food-spezifische Business-Logic für Multi-Contact-Rollen und Territory-Management mit Seasonal-Business-Rules

**Problem:** FreshFoodz Cook&Fresh® B2B-Food-Vertrieb benötigt komplexe Multi-Contact-Workflows (CHEF/BUYER), Territory-spezifische Rules (Deutschland/Schweiz) und Seasonal-Business-Logic

**Solution:** Business-Rules-Engine mit Multi-Contact-Rollen + Territory-Management + Seasonal-Windows + SLA-Integration + Auto-Approval-Workflows

**Timeline:** 4-6h von Business-Rules-Definition bis Production-Integration mit Settings Core Engine

**Impact:** FreshFoodz-spezifische B2B-Food-Workflows + Gastronomiebetrieb-Automatisierung + Territory-optimierte SLAs

## 📋 Context & Dependencies

### Current State:
- ✅ **Settings Core Engine:** 7-Level Scope-Hierarchie operational (FROM PLAN 01)
- ✅ **Territory-Framework:** Deutschland/Schweiz Infrastructure ready
- ✅ **Contact-Management:** Multi-Contact-Rollen (CHEF/BUYER) definiert
- ✅ **Seasonal-Calendar:** B2B-Food-Seasonal-Windows spezifiziert

### Target State:
- 🎯 **Multi-Contact-Rollen:** CHEF (Menu-Planung) + BUYER (Einkauf + Budgets) spezifische Settings
- 🎯 **Territory-Management:** Deutschland (EUR + 19% MwSt) + Schweiz (CHF + 7.7% MwSt) Rules
- 🎯 **Seasonal-Business-Rules:** Spargel-Saison + Oktoberfest + Weihnachts-Catering Windows
- 🎯 **SLA-Integration:** T+3 Samples (DE) + T+2 Samples (CH) + Auto-Approval-Thresholds
- 🎯 **Business-Validation:** Domain-spezifische Rules + Compliance-Checks

### Dependencies:
- **Settings Core Engine:** Scope-Hierarchie + Merge-Engine (FROM PLAN 01)
- **Territory-Infrastructure:** Deutschland/Schweiz Configuration (READY)
- **Contact-Management-System:** Multi-Contact-Rollen (READY)
- **Calendar-System:** Seasonal-Windows + Business-Calendar (READY)

## 🛠️ Implementation Phases (3 Phasen = 4-6h Gesamt)

### Phase 1: Multi-Contact-Rollen Business-Logic (2h)

**Goal:** CHEF/BUYER-spezifische Settings-Categories und Role-basierte Business-Rules

**Actions:**
1. **Contact-Role Business-Rules:**
   ```java
   @Service
   public class ContactRoleBusinessService {
       public SettingsCategory getContactRoleSettings(ContactRole role, Territory territory) {
           return switch (role) {
               case CHEF -> SettingsCategory.builder()
                   .categories(List.of("menu_planning", "quality_standards", "seasonal_preferences"))
                   .notifications(List.of("new_products", "quality_updates", "seasonal_starts"))
                   .permissions(List.of("view_nutritional_data", "request_samples"))
                   .build();

               case BUYER -> SettingsCategory.builder()
                   .categories(List.of("pricing_alerts", "budget_limits", "auto_approval"))
                   .notifications(List.of("price_changes", "delivery_dates", "sla_violations"))
                   .permissions(List.of("view_pricing", "approve_orders", "manage_suppliers"))
                   .build();
           };
       }
   }
   ```

2. **Business-Settings Registry:**
   ```json
   {
     "contact_role_settings": {
       "chef_menu_planning": {
         "schema": {
           "type": "object",
           "properties": {
             "dietary_restrictions": {"type": "array", "items": {"enum": ["vegan", "halal", "gluten_free"]}},
             "seasonal_preference_weight": {"type": "number", "minimum": 0.1, "maximum": 2.0},
             "quality_threshold": {"type": "string", "enum": ["BASIC", "PREMIUM", "LUXURY"]}
           }
         },
         "business_rules": {
           "seasonal_preference_weight": "Must be higher for premium territories (CH: >1.2, DE: >1.0)"
         }
       },
       "buyer_procurement": {
         "schema": {
           "type": "object",
           "properties": {
             "auto_approval_threshold": {"type": "number", "minimum": 0, "maximum": 10000},
             "budget_alert_percentage": {"type": "number", "minimum": 50, "maximum": 95},
             "preferred_payment_terms": {"type": "string", "enum": ["NET30", "NET14", "PREPAID"]}
           }
         }
       }
     }
   }
   ```

3. **Role-Validation Service:**
   ```java
   @Service
   public class ContactRoleValidationService {
       public ValidationResult validateRoleSettings(ContactRole role, String key, JsonNode value, Territory territory) {
           BusinessRule rule = businessRulesRegistry.getRule(role, key);

           return switch (key) {
               case "auto_approval_threshold" -> validateAutoApprovalThreshold(value, role, territory);
               case "seasonal_preference_weight" -> validateSeasonalWeight(value, territory);
               case "quality_threshold" -> validateQualityThreshold(value, role);
               default -> ValidationResult.valid();
           };
       }
   }
   ```

**Success Criteria:** CHEF/BUYER Role-Settings operational + Business-Validation active + Role-specific UI-Categories

### Phase 2: Territory-Management + Regional-Rules (1-2h)

**Goal:** Deutschland/Schweiz-spezifische Business-Rules und Territory-optimierte SLA-Standards

**Actions:**
1. **Territory-Business-Rules:**
   ```java
   @Service
   public class TerritoryBusinessService {
       public TerritoryConfig getTerritoryConfiguration(Territory territory) {
           return switch (territory) {
               case GERMANY -> TerritoryConfig.builder()
                   .currency("EUR")
                   .languagePreference("de_DE")
                   .seasonalHighlights(List.of("ASPARAGUS_SEASON", "OKTOBERFEST", "CHRISTMAS_MARKETS"))
                   .slaStandards(Map.of("SAMPLE_DELIVERY", "T+3", "BULK_ORDER", "T+7"))
                   .taxRate(0.19)
                   .build();

               case SWITZERLAND -> TerritoryConfig.builder()
                   .currency("CHF")
                   .languagePreference("de_CH")
                   .seasonalHighlights(List.of("ALPINE_CHEESE_SEASON", "CHRISTMAS_MARKETS"))
                   .slaStandards(Map.of("SAMPLE_DELIVERY", "T+2", "BULK_ORDER", "T+5"))
                   .taxRate(0.077)
                   .build();
           };
       }
   }
   ```

2. **Territory-Specific Settings-Categories:**
   ```json
   {
     "territory_settings": {
       "germany_preferences": {
         "schema": {
           "type": "object",
           "properties": {
             "currency": {"type": "string", "default": "EUR"},
             "tax_rate": {"type": "number", "default": 0.19},
             "language_preference": {"type": "string", "default": "de_DE"}
           }
         }
       },
       "switzerland_preferences": {
         "schema": {
           "type": "object",
           "properties": {
             "currency": {"type": "string", "default": "CHF"},
             "tax_rate": {"type": "number", "default": 0.077},
             "language_preference": {"type": "string", "default": "de_CH"}
           }
         }
       }
     }
   }
   ```

3. **SLA-Territory-Integration:**
   ```java
   @Service
   public class TerritorySlAService {
       public SLAConfiguration getTerritorySlA(Territory territory, OrderType orderType) {
           TerritoryConfig config = territoryBusinessService.getTerritoryConfiguration(territory);

           return SLAConfiguration.builder()
               .deliveryWindow(config.getSlaStandards().get(orderType.name()))
               .escalationThreshold(getEscalationThreshold(territory))
               .autoApprovalLimit(getAutoApprovalLimit(territory))
               .qualityCheckRequired(config.getQualityStandards().contains("PREMIUM_QUALITY"))
               .build();
       }
   }
   ```

**Success Criteria:** Territory-Rules operational + SLA-Standards territory-optimiert + Regional-Settings active

### Phase 3: Seasonal-Business-Rules + Auto-Approval-Workflows (1-2h)

**Goal:** Seasonal-Windows für B2B-Food + Auto-Approval-Workflows + Business-Calendar-Integration

**Actions:**
1. **Seasonal-Business-Calendar:**
   ```java
   @Service
   public class SeasonalBusinessService {
       public List<SeasonalWindow> getActiveSeasonalWindows(Territory territory, LocalDate date) {
           return seasonalWindowRepository.findActiveWindows(territory, date).stream()
               .map(this::enhanceWithBusinessRules)
               .collect(toList());
       }

       private SeasonalWindow enhanceWithBusinessRules(SeasonalWindow window) {
           return window.toBuilder()
               .productBoostFactor(calculateSeasonalBoost(window))
               .autoSuggestProducts(getSeasonalProducts(window))
               .marketingPriority(calculateMarketingPriority(window))
               .build();
       }
   }
   ```

2. **Seasonal-Settings Registry:**
   ```json
   {
     "seasonal_windows": {
       "summer_season": {
         "enabled": true,
         "start_date": "2025-04-01",
         "end_date": "2025-09-30",
         "auto_suggest_products": ["ice_cream", "salads", "grilled_items"],
         "priority_boost": 1.5,
         "marketing_campaigns": ["summer_specials", "bbq_season"]
       },
       "christmas_season": {
         "enabled": true,
         "start_date": "2025-11-15",
         "end_date": "2025-12-31",
         "auto_suggest_products": ["premium_meats", "holiday_desserts", "catering_packages"],
         "priority_boost": 2.0,
         "auto_approval_boost": 1.3
       }
     }
   }
   ```

3. **Auto-Approval-Workflow:**
   ```java
   @Service
   public class AutoApprovalWorkflowService {
       public ApprovalDecision processAutoApproval(OrderRequest order, Settings userSettings) {
           BigDecimal threshold = extractAutoApprovalThreshold(userSettings);
           BigDecimal seasonalBoost = calculateSeasonalBoost(order.getTerritory(), order.getDate());
           BigDecimal effectiveThreshold = threshold.multiply(seasonalBoost);

           if (order.getTotalValue().compareTo(effectiveThreshold) <= 0) {
               return ApprovalDecision.autoApproved()
                   .reason("Under auto-approval threshold: " + effectiveThreshold)
                   .seasonalBoost(seasonalBoost)
                   .build();
           }

           return ApprovalDecision.manualReviewRequired()
               .reason("Exceeds threshold, manual review needed")
               .escalateTo(getEscalationContact(order))
               .build();
       }
   }
   ```

**Success Criteria:** Seasonal-Windows active + Auto-Approval-Workflows operational + Business-Calendar-Integration functional

## ✅ Success Metrics

### **Immediate Success (4-6h):**
1. **Multi-Contact-Rollen:** CHEF/BUYER-spezifische Settings-Categories operational
2. **Territory-Management:** Deutschland/Schweiz Rules + SLA-Standards active
3. **Seasonal-Business-Rules:** Seasonal-Windows + Product-Boost-Factors functional
4. **Auto-Approval-Workflows:** Territory + Seasonal-optimierte Approval-Logic operational
5. **Business-Validation:** Domain-spezifische Rules + Compliance-Checks active

### **Business Success (1-2 Wochen):**
1. **Gastronomiebetrieb-Workflows:** CHEF (Menu-Planung) + BUYER (Einkauf) optimiert
2. **Territory-Configuration:** Deutschland (EUR + 19% MwSt) + Schweiz (CHF + 7.7% MwSt) differentiated
3. **Seasonal-Revenue:** Spargel + Oktoberfest + Weihnachts-Catering automatisch optimiert
4. **SLA-Compliance:** T+3 (DE) + T+2 (CH) Territory-optimierte Delivery-Standards

### **Technical Excellence:**
- **Business-Rules-Performance:** Contact-Role + Territory-Rules <5ms Resolution
- **Seasonal-Accuracy:** Real-time Seasonal-Window-Detection + Product-Boost-Calculation
- **Auto-Approval-Efficiency:** >80% Orders auto-approved within Territory-Limits
- **Compliance-Coverage:** 100% Business-Rules + Territory-Standards validation

## 🔗 Related Documentation

### **Integration Foundation:**
- [Settings Core Engine Plan](01_SETTINGS_CORE_ENGINE_PLAN.md) - Scope-Hierarchie + Merge-Engine Base
- [Frontend UX-Excellence Plan](03_FRONTEND_UX_EXCELLENCE_PLAN.md) - Role-based UI + Territory-UX
- [Cross-Service Integration Plan](05_CROSS_SERVICE_INTEGRATION_PLAN.md) - Business-Events + Workflows

### **B2B-Food Business Artifacts:**
- [Business Rules Registry](../artefakte/database/business_rules_registry.json) - Complete Rules Definition
- [Territory Configuration](../artefakte/backend/TerritoryBusinessService.java) - Deutschland/Schweiz Logic
- [Seasonal Calendar](../artefakte/backend/SeasonalBusinessService.java) - Business-Calendar Integration

### **Cross-Module Integration:**
- [Contact Management](../../03_kundenmanagement/README.md) - Multi-Contact-Rollen Integration
- [Communication Module](../../05_kommunikation/README.md) - SLA-Notification + Follow-up-Integration

## 🤖 Claude Handover Section

### **Current Priority (für neue Claude):**
```bash
# Phase 1: Multi-Contact-Rollen Business-Logic
cd implementation-plans/
→ 02_B2B_FOOD_BUSINESS_LOGIC_PLAN.md (CURRENT)

# Start: Contact-Role Business-Rules + Territory-Management
cd ../artefakte/backend/

# Success: CHEF/BUYER-Workflows + Deutschland/Schweiz Rules operational
# Next: Frontend UX-Excellence + Role-based UI Components
```

### **Context für neue Claude:**
- **B2B-Food Business Logic:** Multi-Contact-Rollen + Territory-Management + Seasonal-Rules
- **Timeline:** 4-6h Business-Rules-Engine mit FreshFoodz-spezifischen Workflows
- **Dependencies:** Settings Core Engine operational + Territory-Infrastructure ready
- **Business-Value:** Gastronomiebetrieb-optimierte Workflows + Territory-differentiated SLAs

### **Key Success-Factors:**
- **Role-Differentiation:** CHEF (Menu-Planung) vs. BUYER (Einkauf) klar abgegrenzt
- **Territory-Configuration:** Deutschland (EUR + 19% MwSt) vs. Schweiz (CHF + 7.7% MwSt)
- **Seasonal-Intelligence:** Spargel + Oktoberfest + Weihnachts-Catering Windows
- **Auto-Approval-Logic:** Territory + Seasonal-optimierte Approval-Workflows

**🚀 Ready für FreshFoodz-spezifische B2B-Food Business-Logic Implementation!**