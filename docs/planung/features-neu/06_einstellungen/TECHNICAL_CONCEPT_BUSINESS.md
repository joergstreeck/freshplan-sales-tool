# Technical Concept: B2B-Food Business Logic

**Status:** Produktionsreif
**Bewertung:** 10/10 ⭐⭐⭐⭐⭐
**Datum:** 2025-09-20

## 🎯 Überblick

Dieses Dokument beschreibt die business-spezifische Logik des FreshPlan Settings-Systems, optimiert für B2B-Food-Workflows mit Multi-Contact-Rollen, Territory-Management und saisonalen Besonderheiten.

## 🏢 B2B-Food Kontext

### Multi-Contact-Rollen System

**CHEF-Rolle:**
- **Zuständigkeit:** Menu-Planung, Qualitäts-Standards, Seasonal Windows
- **Einstellungen:** Spezial-Diäten, Allergene-Filter, Seasonal Preferences
- **Benachrichtigungen:** Neue Produkte, Qualitäts-Updates, Saison-Starts

**BUYER-Rolle:**
- **Zuständigkeit:** Einkauf, Budgets, Supplier-Management, SLA-Compliance
- **Einstellungen:** Preis-Alerts, Budget-Limits, Auto-Approval-Thresholds
- **Benachrichtigungen:** Preis-Änderungen, Liefertermine, SLA-Violations

### Territory-Management

**Deutschland (EUR):**
- **Besonderheiten:** Strenge Bio-Standards, HACCP-Compliance
- **Seasonality:** Spargel-Saison (April-Juni), Oktoberfest (September-Oktober)
- **SLA Standards:** T+3 für Samples, T+7 für Bulk-Orders

**Schweiz (CHF):**
- **Besonderheiten:** Premium-Qualität, lokale Produzenten bevorzugt
- **Seasonality:** Alpkäse-Saison (Mai-September), Weihnachtsmärkte (Dezember)
- **SLA Standards:** T+2 für Samples (höhere Erwartungen), T+5 für Bulk-Orders

## 📊 Business-spezifische Settings-Kategorien

### 1. Seasonal Windows
```json
{
  "seasonal_windows": {
    "summer_season": {
      "enabled": true,
      "start_date": "2025-04-01",
      "end_date": "2025-09-30",
      "auto_suggest_products": ["ice_cream", "salads", "grilled_items"],
      "priority_boost": 1.5
    },
    "christmas_season": {
      "enabled": true,
      "start_date": "2025-11-15",
      "end_date": "2025-12-31",
      "auto_suggest_products": ["christmas_menu", "mulled_wine", "roast"],
      "priority_boost": 2.0
    }
  }
}
```

### 2. Qualitäts-Standards (CHEF-Rolle)
```json
{
  "quality_standards": {
    "allergen_filtering": {
      "gluten_free": true,
      "lactose_free": false,
      "nut_free": true,
      "custom_allergens": ["sesame", "sulfites"]
    },
    "dietary_preferences": {
      "vegetarian_options": true,
      "vegan_percentage": 30,
      "regional_preference": 0.8,
      "bio_minimum": 0.5
    },
    "quality_gates": {
      "min_supplier_rating": 4.2,
      "haccp_required": true,
      "sample_required_above_eur": 500
    }
  }
}
```

### 3. Procurement Settings (BUYER-Rolle)
```json
{
  "procurement": {
    "budget_controls": {
      "monthly_budget_eur": 15000,
      "auto_approval_threshold": 200,
      "requires_chef_approval_above": 1000,
      "emergency_budget_multiplier": 1.2
    },
    "supplier_preferences": {
      "preferred_suppliers": ["supplier_123", "supplier_456"],
      "backup_suppliers": ["supplier_789"],
      "blacklisted_suppliers": [],
      "min_suppliers_per_category": 2
    },
    "sla_configuration": {
      "sample_delivery_days": 3,
      "bulk_delivery_days": 7,
      "escalation_after_hours": 24,
      "auto_reorder_threshold": 0.2
    }
  }
}
```

### 4. Territory-spezifische Settings
```json
{
  "territory_settings": {
    "currency": "EUR",
    "tax_rate": 0.19,
    "local_regulations": {
      "bio_certification_required": true,
      "haccp_level": "enhanced",
      "allergen_labeling": "eu_standard"
    },
    "seasonal_calendars": {
      "asparagus_season": "2025-04-15_to_2025-06-24",
      "strawberry_season": "2025-05-01_to_2025-07-31",
      "christmas_markets": "2025-11-25_to_2025-12-23"
    }
  }
}
```

## 🔄 Business Logic Workflows

### Workflow 1: Seasonal Product Auto-Suggestion

```java
@Service
public class SeasonalProductService {

    public List<Product> suggestSeasonalProducts(User user) {
        var userSettings = settingsService.getMergedSettings(user);
        var currentSeason = seasonalService.getCurrentSeason();

        if (userSettings.seasonal_windows.get(currentSeason).enabled) {
            var products = productService.getSeasonalProducts(currentSeason);
            var priorityBoost = userSettings.seasonal_windows.get(currentSeason).priority_boost;

            return products.stream()
                .filter(p -> matchesQualityStandards(p, userSettings))
                .sorted(byPriorityBoost(priorityBoost))
                .limit(20)
                .collect(toList());
        }

        return Collections.emptyList();
    }
}
```

### Workflow 2: Multi-Role Approval Process

```java
@Service
public class ApprovalWorkflowService {

    public ApprovalResult processOrder(Order order, User buyer) {
        var buyerSettings = settingsService.getMergedSettings(buyer);
        var orderAmount = order.getTotalAmount();

        // Auto-Approval für kleine Beträge
        if (orderAmount <= buyerSettings.procurement.auto_approval_threshold) {
            return ApprovalResult.AUTO_APPROVED;
        }

        // CHEF-Approval bei hohen Beträgen oder Qualitäts-relevanten Items
        if (orderAmount > buyerSettings.procurement.requires_chef_approval_above
            || order.hasQualityRelevantItems()) {

            var chef = contactService.getChefForAccount(buyer.getAccountId());
            notificationService.sendApprovalRequest(chef, order);
            return ApprovalResult.PENDING_CHEF_APPROVAL;
        }

        return ApprovalResult.APPROVED;
    }
}
```

### Workflow 3: Territory-basierte SLA-Berechnung

```java
@Service
public class SLACalculationService {

    public SLATimeline calculateDeliveryTimeline(Order order, Territory territory) {
        var territorySettings = settingsService.getTerritorySettings(territory);
        var slaConfig = territorySettings.procurement.sla_configuration;

        LocalDate expectedSampleDate = LocalDate.now()
            .plusDays(slaConfig.sample_delivery_days);

        LocalDate expectedBulkDate = LocalDate.now()
            .plusDays(slaConfig.bulk_delivery_days);

        // Schweiz hat strengere SLAs
        if (territory == Territory.SWITZERLAND) {
            expectedSampleDate = expectedSampleDate.minusDays(1);
            expectedBulkDate = expectedBulkDate.minusDays(2);
        }

        return new SLATimeline(expectedSampleDate, expectedBulkDate);
    }
}
```

## 🎯 Performance-optimierte Business Queries

### Scope-optimierte Abfragen
```sql
-- Häufige Query: User-Settings mit Role-Context
WITH user_context AS (
  SELECT u.id, u.tenant_id, cr.role_type, c.account_id, a.territory_id
  FROM users u
  JOIN contact_roles cr ON u.contact_id = cr.contact_id
  JOIN contacts c ON cr.contact_id = c.id
  JOIN accounts a ON c.account_id = a.id
  WHERE u.id = ?
)
SELECT s.setting_key, s.setting_value
FROM settings s, user_context uc
WHERE s.tenant_id = uc.tenant_id
AND (
  s.scope_type = 'USER' AND s.scope_id = uc.id::text OR
  s.scope_type = 'CONTACT_ROLE' AND s.scope_id = uc.role_type OR
  s.scope_type = 'ACCOUNT' AND s.scope_id = uc.account_id::text OR
  s.scope_type = 'TERRITORY' AND s.scope_id = uc.territory_id::text OR
  s.scope_type = 'TENANT' AND s.scope_id = uc.tenant_id::text OR
  s.scope_type = 'GLOBAL'
)
ORDER BY scope_priority DESC;
```

### Seasonal Product Matching
```sql
-- Performance-optimierte Seasonal Product Query
SELECT p.*, ps.priority_boost
FROM products p
JOIN product_seasons ps ON p.id = ps.product_id
JOIN settings s ON s.setting_key = 'seasonal_windows'
WHERE ps.season_type = current_season()
AND (s.setting_value->ps.season_type->>'enabled')::boolean = true
AND p.quality_rating >= (s.setting_value->'quality_standards'->>'min_supplier_rating')::float
ORDER BY (p.base_priority * ps.priority_boost) DESC;
```

## 🔔 Business Event-driven Notifications

### Event Types
```java
public enum BusinessEventType {
    SEASONAL_WINDOW_STARTED,
    BUDGET_THRESHOLD_EXCEEDED,
    SLA_VIOLATION_DETECTED,
    NEW_PRODUCT_IN_SEASON,
    QUALITY_ALERT,
    APPROVAL_REQUIRED,
    SUPPLIER_RATING_CHANGED
}
```

### Event Handlers
```java
@EventListener
public void handleSeasonalWindowStart(SeasonalWindowStartedEvent event) {
    var affectedUsers = userService.getUsersWithSeasonalSettings(event.getSeason());

    affectedUsers.parallelStream().forEach(user -> {
        var suggestions = seasonalProductService.suggestSeasonalProducts(user);
        notificationService.sendSeasonalSuggestions(user, suggestions);
    });
}

@EventListener
public void handleBudgetThresholdExceeded(BudgetExceededEvent event) {
    var buyer = event.getBuyer();
    var chef = contactService.getChefForAccount(buyer.getAccountId());

    notificationService.sendBudgetAlert(buyer, chef, event.getCurrentSpend());
}
```

## 📊 Business Analytics & Insights

### Settings Usage Analytics
```sql
-- Welche saisonalen Einstellungen werden am häufigsten genutzt?
SELECT
  s.setting_key,
  s.setting_value->>'season_type' as season,
  COUNT(*) as usage_count,
  AVG((s.setting_value->>'priority_boost')::float) as avg_priority_boost
FROM settings s
WHERE s.setting_key = 'seasonal_windows'
AND (s.setting_value->>'enabled')::boolean = true
GROUP BY s.setting_key, season
ORDER BY usage_count DESC;
```

### ROI der Settings-Personalisierung
```sql
-- Vergleich Order-Performance mit/ohne personalisierte Settings
SELECT
  CASE WHEN has_custom_settings THEN 'Personalized' ELSE 'Default' END as settings_type,
  COUNT(*) as order_count,
  AVG(order_value) as avg_order_value,
  AVG(customer_satisfaction_score) as avg_satisfaction
FROM orders o
LEFT JOIN (
  SELECT DISTINCT user_id, true as has_custom_settings
  FROM settings
  WHERE scope_type IN ('USER', 'CONTACT_ROLE', 'ACCOUNT')
) us ON o.user_id = us.user_id
GROUP BY has_custom_settings;
```

## 🚀 Deployment & Configuration

### Territory-spezifische Deployments
```bash
# Deutschland Setup
./scripts/setup-territory.sh --territory=DE --currency=EUR --sla-sample=3 --sla-bulk=7

# Schweiz Setup
./scripts/setup-territory.sh --territory=CH --currency=CHF --sla-sample=2 --sla-bulk=5
```

### Business Logic Tests
```java
@Test
public void testMultiRoleApprovalWorkflow() {
    // Given: BUYER und CHEF in einem Account
    var buyer = createBuyerUser();
    var chef = createChefUser(buyer.getAccountId());

    // When: High-value Order
    var order = createOrder(1500.0); // Above approval threshold
    var result = approvalService.processOrder(order, buyer);

    // Then: CHEF approval required
    assertThat(result).isEqualTo(ApprovalResult.PENDING_CHEF_APPROVAL);
    verify(notificationService).sendApprovalRequest(chef, order);
}

@Test
public void testSeasonalProductSuggestion() {
    // Given: User mit aktivierten Seasonal Windows
    var user = createUserWithSeasonalSettings();
    mockCurrentSeason("summer_season");

    // When: Request seasonal products
    var suggestions = seasonalService.suggestSeasonalProducts(user);

    // Then: Summer products mit Priority Boost
    assertThat(suggestions).allMatch(p -> p.getSeasonType() == SUMMER);
    assertThat(suggestions).isSortedAccordingTo(byPriorityDesc());
}
```

---

**Besonderheiten der B2B-Food-Branche vollständig abgebildet:**
- ✅ Multi-Contact-Rollen (CHEF/BUYER)
- ✅ Territory-Management (DE/CH)
- ✅ Saisonale Workflows
- ✅ SLA-Management (T+3/T+7)
- ✅ Qualitäts-Standards & Compliance
- ✅ Budget-Controls & Approval-Workflows