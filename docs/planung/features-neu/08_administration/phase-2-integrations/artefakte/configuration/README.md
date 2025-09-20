# Configuration Files

**Zweck:** Environment und Configuration Templates für Phase 2 Deployment

## Environment Configuration

### `env.example`
**Purpose:** Template für AI Provider und External Service Configuration

```bash
# AI Provider Configuration
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=ant-...
OPENAI_MODEL_SMALL=gpt-4o-mini
OPENAI_MODEL_LARGE=gpt-4o
ANTHROPIC_MODEL_SMALL=claude-3-haiku
ANTHROPIC_MODEL_LARGE=claude-3-opus

# External Service Integration
XENTRAL_API_URL=https://api.xentral.biz
XENTRAL_API_KEY=xentral_...
ALLIANZ_CREDIT_API_URL=https://api.allianz-trade.com
ALLIANZ_CREDIT_API_KEY=allianz_...
ALL_INKL_EMAIL_HOST=mail.all-inkl.com
ALL_INKL_EMAIL_USER=freshplan@domain.de
ALL_INKL_EMAIL_PASSWORD=...

# Lead Protection Configuration
LEAD_PROTECTION_BASE_MONTHS=6
LEAD_PROTECTION_PROGRESS_DAYS=60
LEAD_PROTECTION_GRACE_DAYS=10

# AI Budget Configuration
AI_BUDGET_MONTHLY_CAP_EUR=1000
AI_CONFIDENCE_THRESHOLD=0.7
AI_CACHE_TTL_HOURS=1

# Performance Configuration
CREDIT_CHECK_TIMEOUT_MS=5000
CREDIT_CHECK_CIRCUIT_BREAKER_THRESHOLD=10
SAMPLE_FEEDBACK_RATE_LIMIT_PER_HOUR=100
```

## Deployment Configuration

### Development Environment
```bash
# Copy template and configure
cp env.example .env.development

# Required for Development
OPENAI_API_KEY=sk-test-...        # Development API key
ANTHROPIC_API_KEY=ant-dev-...     # Development API key
AI_BUDGET_MONTHLY_CAP_EUR=100     # Lower limit für Development

# Optional for Development
XENTRAL_API_URL=https://sandbox.xentral.biz
ALLIANZ_CREDIT_API_URL=https://sandbox.allianz-trade.com
```

### Staging Environment
```bash
# Production-like configuration
cp env.example .env.staging

# Staging-specific overrides
AI_BUDGET_MONTHLY_CAP_EUR=500     # Medium budget
LEAD_PROTECTION_BASE_MONTHS=3     # Shorter für Testing
CREDIT_CHECK_TIMEOUT_MS=3000      # Faster timeout

# Real API endpoints with test credentials
XENTRAL_API_URL=https://staging.xentral.biz
ALLIANZ_CREDIT_API_URL=https://staging.allianz-trade.com
```

### Production Environment
```bash
# Full production configuration
cp env.example .env.production

# Production values
AI_BUDGET_MONTHLY_CAP_EUR=1000
LEAD_PROTECTION_BASE_MONTHS=6
LEAD_PROTECTION_PROGRESS_DAYS=60
LEAD_PROTECTION_GRACE_DAYS=10

# Production API endpoints
XENTRAL_API_URL=https://api.xentral.biz
ALLIANZ_CREDIT_API_URL=https://api.allianz-trade.com
```

## Settings Registry Integration

### Lead Protection Settings
```sql
-- Global Settings (Contract-based, non-overridable)
INSERT INTO settings_registry (key, type, scope, default_value, description) VALUES
('lead.protection.baseMonths', 'scalar', '["global"]', '6', 'Base protection period per Handelsvertretervertrag'),
('lead.protection.progressDays', 'scalar', '["global"]', '60', 'Activity requirement period'),
('lead.protection.graceDays', 'scalar', '["global"]', '10', 'Grace period before expiry');

-- Org-specific Settings (Business Configuration)
INSERT INTO settings_registry (key, type, scope, default_value, description) VALUES
('ai.budget.monthly.cap', 'scalar', '["org"]', '1000', 'Monthly AI budget cap in EUR'),
('lead.protection.reminder.channels', 'list', '["org"]', '["EMAIL","SLACK"]', 'Reminder notification channels'),
('credit.cache.ttl.hours', 'scalar', '["org"]', '24', 'Credit check cache TTL');

-- User-specific Settings (Personal Preference)
INSERT INTO settings_registry (key, type, scope, default_value, description) VALUES
('ai.routing.confidence.threshold', 'scalar', '["user"]', '0.7', 'Confidence threshold für Small→Large routing'),
('sample.feedback.auto.reminder', 'scalar', '["user"]', 'true', 'Automatic feedback reminders');
```

### AI Provider Settings
```sql
-- AI Configuration
INSERT INTO settings_registry (key, type, scope, default_value, schema, description) VALUES
('ai.providers.openai.models', 'object', '["global"]',
 '{"small":"gpt-4o-mini","large":"gpt-4o"}',
 '{"type":"object","properties":{"small":{"type":"string"},"large":{"type":"string"}}}',
 'OpenAI model configuration'),

('ai.providers.anthropic.models', 'object', '["global"]',
 '{"small":"claude-3-haiku","large":"claude-3-opus"}',
 '{"type":"object","properties":{"small":{"type":"string"},"large":{"type":"string"}}}',
 'Anthropic model configuration');
```

## Security Configuration

### API Key Management
```bash
# Docker Secrets (Production)
docker secret create openai_api_key /path/to/openai_key.txt
docker secret create anthropic_api_key /path/to/anthropic_key.txt

# Kubernetes Secrets
kubectl create secret generic ai-providers \
  --from-literal=openai-api-key='sk-...' \
  --from-literal=anthropic-api-key='ant-...'

# AWS Secrets Manager
aws secretsmanager create-secret \
  --name "freshplan/ai-providers" \
  --description "AI Provider API Keys" \
  --secret-string '{"openai":"sk-...","anthropic":"ant-..."}'
```

### Environment Validation
```java
// Configuration validation on startup
@ConfigurationProperties("ai")
@Validated
public class AiConfiguration {

    @NotBlank(message = "OpenAI API key is required")
    private String openaiApiKey;

    @NotBlank(message = "Anthropic API key is required")
    private String anthropicApiKey;

    @DecimalMin(value = "0.1", message = "Confidence threshold must be >= 0.1")
    @DecimalMax(value = "1.0", message = "Confidence threshold must be <= 1.0")
    private Double confidenceThreshold = 0.7;

    @Min(value = 50, message = "Monthly cap must be >= 50 EUR")
    private Integer monthlyCapEur = 1000;
}
```

## Monitoring Configuration

### Business KPI Alerts
```yaml
# Prometheus Alerting Rules
groups:
  - name: freshplan-phase2
    rules:
      - alert: LeadProtectionHighExpiry
        expr: lead_protection_expired_count_daily > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High lead protection expiry rate"

      - alert: AiBudgetExceeded
        expr: ai_cost_eur_monthly > ai_budget_cap_eur
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "AI monthly budget exceeded"

      - alert: CreditCheckSLAViolation
        expr: credit_check_p95_ms > 300
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Credit check SLA violation (>300ms p95)"
```

### Log Configuration
```yaml
# Logback configuration
logging:
  level:
    de.freshplan.ai: DEBUG
    de.freshplan.leadprotection: INFO
    de.freshplan.sample: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/freshplan-phase2.log
    max-size: 100MB
    max-history: 30
```

## Integration Testing Configuration

### Test Data Configuration
```bash
# Test environment overrides
AI_BUDGET_MONTHLY_CAP_EUR=10          # Low budget für Testing
AI_CONFIDENCE_THRESHOLD=0.5           # Lower threshold für Testing
LEAD_PROTECTION_BASE_MONTHS=1         # Shorter für Testing
CREDIT_CHECK_TIMEOUT_MS=1000          # Faster timeout
SAMPLE_FEEDBACK_RATE_LIMIT_PER_HOUR=1000  # Higher limit für Load Tests
```

### Mock Service Configuration
```java
// Test profile configuration
@Profile("test")
@Configuration
public class MockConfiguration {

    @Bean
    @Primary
    public XentralClient mockXentralClient() {
        return Mockito.mock(XentralClient.class);
    }

    @Bean
    @Primary
    public AllianzCreditClient mockAllianzClient() {
        return Mockito.mock(AllianzCreditClient.class);
    }
}
```

## Migration Configuration

### Variable Migration Support
```bash
# Dynamic migration numbering
cp sql-templates/lead_protection_and_rls.sql \
   backend/db/migrations/V$(./scripts/get-next-migration.sh)__lead_protection_and_rls.sql

cp sql-templates/sample_cdm_extension.sql \
   backend/db/migrations/V$(./scripts/get-next-migration.sh)__sample_cdm_extension.sql

cp sql-templates/settings_lead_ai.sql \
   backend/db/migrations/V$(./scripts/get-next-migration.sh)__settings_lead_ai.sql
```

### Flyway Configuration
```properties
# flyway.conf
flyway.url=jdbc:postgresql://localhost:5432/freshplan
flyway.user=freshplan
flyway.password=...
flyway.locations=filesystem:db/migrations
flyway.table=schema_version
flyway.validateOnMigrate=true
flyway.outOfOrder=false
flyway.baselineOnMigrate=true
```

---

**📋 Complete configuration management für secure, scalable Phase 2 deployment!**