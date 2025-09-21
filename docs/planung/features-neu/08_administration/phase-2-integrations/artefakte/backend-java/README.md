# Backend Java Implementation

**Zweck:** Production-ready AI Provider Integration für Phase 2

## Package Structure

```java
// Ziel-Package Structure
backend/src/main/java/de/freshplan/
├── ai/
│   ├── providers/
│   │   ├── OpenAiClient.java        ← Copy-paste ready
│   │   └── AnthropicClient.java     ← Copy-paste ready
│   ├── AiCache.java                 ← TTL-based caching
│   └── ProviderRateLimiter.java     ← Token bucket rate limiting
```

## Deployment Instructions

```bash
# Copy files to target package
cp OpenAiClient.java ../../../../../../backend/src/main/java/de/freshplan/ai/providers/
cp AnthropicClient.java ../../../../../../backend/src/main/java/de/freshplan/ai/providers/
cp AiCache.java ../../../../../../backend/src/main/java/de/freshplan/ai/
cp ProviderRateLimiter.java ../../../../../../backend/src/main/java/de/freshplan/ai/

# Environment Configuration erforderlich
```

## Enthaltene Implementations

### `OpenAiClient.java` (65 Zeilen)
**Features:**
- HTTP Client mit 10s connect, 30s read timeout
- Model Selection per Use Case (Small/Large routing)
- Structured JSON Response Parsing
- Confidence Score Extraction
- Environment-based Configuration

**Supported Models:**
```java
SMALL_MODELS: gpt-4o-mini (LEAD_CLASSIFY, EMAIL_CATEGORIZE)
LARGE_MODELS: gpt-4o (ROI_RECOMMEND, RISK_ASSESS, etc.)
```

### `AnthropicClient.java` (64 Zeilen)
**Features:**
- Claude Integration mit Messages API
- Model Selection (claude-3-haiku vs claude-3-opus)
- Token Usage Tracking
- Confidence-based Response Analysis
- Error Handling + Retry Logic

**Supported Models:**
```java
SMALL_MODELS: claude-3-haiku (Quick Classifications)
LARGE_MODELS: claude-3-opus (Complex Analysis)
```

### `AiCache.java` (33 Zeilen)
**Features:**
- TTL-based In-Memory Cache
- Thread-safe ConcurrentHashMap
- Automatic Expiry Management
- Idempotent Call Support

**Cache Strategy:**
```java
// Deterministic calls → 1 hour TTL
// User-specific calls → 15 minutes TTL
// Real-time calls → No caching
```

### `ProviderRateLimiter.java` (35 Zeilen)
**Features:**
- Token Bucket Rate Limiting
- Per-Provider Limits
- Graceful Degradation
- Circuit Breaker Integration

## Integration Architecture

### Multi-Provider Routing Logic
```java
public AiResponse call(AiUseCase useCase, AiRequest request) {
    // 1. Budget Gate Check (per Org/User)
    // 2. Cache Lookup (if idempotent)
    // 3. Small Model First (confidence threshold)
    // 4. Large Model Fallback + Audit
    // 5. Response Caching + Metrics
}
```

### Environment Configuration

```bash
# .env Configuration
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=ant-...
OPENAI_MODEL_SMALL=gpt-4o-mini
OPENAI_MODEL_LARGE=gpt-4o
ANTHROPIC_MODEL_SMALL=claude-3-haiku
ANTHROPIC_MODEL_LARGE=claude-3-opus

# Budget Configuration (Settings Registry)
AI_BUDGET_MONTHLY_CAP=1000    # EUR per Org
AI_CONFIDENCE_THRESHOLD=0.7   # Small→Large escalation
AI_CACHE_TTL_HOURS=1         # Deterministic calls
```

## AI Use Cases Implementation

### Small Model Use Cases (70-80% traffic)
- **LEAD_CLASSIFY:** Lead quality scoring
- **EMAIL_CATEGORIZE:** Incoming email routing
- **PRODUCT_MATCH:** Basic product recommendations
- **ACTIVITY_SUMMARIZE:** Meeting notes summarization

### Large Model Use Cases (20-30% traffic)
- **ROI_RECOMMEND:** Complex ROI calculations
- **RISK_ASSESS:** Credit risk analysis
- **SAMPLE_STRATEGY:** Product testing strategy
- **NEGOTIATION_SUPPORT:** Sales negotiation assistance

## Performance Targets

### Normal Operations
- **AI Router (cached):** p95 <200ms
- **Provider Calls (fresh):** p95 <2s
- **Cache Hit Rate:** >60% für deterministic calls
- **Budget Compliance:** <1% variance from caps

### Peak Load (Seasonal)
- **Concurrent Calls:** 100+ simultaneous
- **Rate Limiting:** Graceful queuing
- **Circuit Breaker:** Auto-recovery
- **Cost Protection:** Emergency throttling

## Testing Strategy

### Unit Tests erforderlich:
```java
@Test void confidenceRouting_lowScore_escalatesToLargeModel()
@Test void budgetGate_monthlyCapExceeded_rejectsRequest()
@Test void caching_idempotentCall_returnsFromCache()
@Test void rateLimiter_exceedsLimit_appliesBackpressure()
```

### Integration Tests erforderlich:
- Real Provider API calls (test keys)
- End-to-End Use Case workflows
- Budget tracking + Settings integration
- Error recovery patterns

## Foundation Standards Compliance

✅ **Error Handling:** Structured exceptions + logging
✅ **Configuration:** Environment-based, no hardcoded values
✅ **Metrics:** Cost tracking + performance monitoring
✅ **Security:** API key management + input validation
✅ **Testing:** >90% coverage target

## Business Value

### Cost Efficiency
- **70-80% Small Model Usage:** ~60% cost reduction
- **Budget Gates:** Prevent cost explosions
- **Caching:** Reduce redundant API calls
- **Confidence Routing:** Optimal quality/cost balance

### Operational Excellence
- **Multi-Provider:** No vendor lock-in
- **Circuit Breaker:** Graceful degradation
- **Rate Limiting:** Prevents API throttling
- **Monitoring:** Real-time cost + performance tracking

---

**📋 Ready for copy-paste deployment mit complete AI integration framework!**