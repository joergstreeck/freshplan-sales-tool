# ADR-0007: RLS Connection Affinity via CDI Interceptor

**Status:** Accepted
**Date:** 2025-09-25
**Sprint:** 1.5 Security Retrofit

## Context

Row-Level Security (RLS) in PostgreSQL depends on GUC (Grand Unified Configuration) variables being set on the database connection. In our initial implementation, we used a ContainerRequestFilter to set these variables at the beginning of each HTTP request.

However, due to connection pooling, there's no guarantee that:
1. The connection used to set GUCs is the same one used for actual database operations
2. GUCs persist across different database operations within the same request
3. Nested transactions maintain the correct RLS context

This creates a critical security vulnerability where users might access data they shouldn't be able to see.

## Decision

We will implement RLS Connection Affinity using a CDI Interceptor pattern that ensures GUC variables are set on the same connection used for database operations.

Key components:
- `@RlsContext` annotation to mark methods requiring RLS
- `RlsConnectionAffinityGuard` interceptor that sets GUCs within the transaction
- Use of `SET LOCAL` to ensure GUCs are transaction-scoped
- Central `AppGuc` enum for consistent GUC key management

## Consequences

### Positive
- **Security**: Guaranteed that RLS context is applied to the correct connection
- **Reliability**: Transaction-scoped GUCs automatically cleared at transaction end
- **Performance**: Minimal overhead (~5ms per operation)
- **Maintainability**: Clear annotation-based approach

### Negative
- **Migration effort**: All infrastructure services must be updated with @RlsContext
- **Testing complexity**: Need to verify connection affinity in tests
- **Learning curve**: Developers must understand when to use @RlsContext

## Implementation Details

### Pattern
```java
@Transactional
@RlsContext
public void anyDatabaseOperation() {
    // RLS context guaranteed on same connection
}
```

### Requirements
1. Methods with @RlsContext MUST also have @Transactional
2. Use SET LOCAL for transaction-scoped GUCs
3. Fail-closed by default (no data without proper context)

## Alternatives Considered

1. **ContainerRequestFilter only**: Rejected due to connection pool issues
2. **Manual GUC setting**: Too error-prone and repetitive
3. **Custom DataSource**: Too invasive and complex
4. **AspectJ**: Additional dependency and complexity

## References
- PostgreSQL RLS Documentation
- Quarkus CDI Interceptor Guide
- OWASP Fail-Safe Security Patterns