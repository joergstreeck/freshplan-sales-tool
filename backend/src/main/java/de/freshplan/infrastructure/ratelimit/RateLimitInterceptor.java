package de.freshplan.infrastructure.ratelimit;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interceptor for rate limiting annotated methods.
 * 
 * @author FreshPlan Team
 * @since 1.0.0
 */
@Interceptor
@RateLimited
@Priority(Interceptor.Priority.APPLICATION)
public class RateLimitInterceptor {
    
    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    
    @Inject
    SecurityIdentity securityIdentity;
    
    // Simple in-memory storage (should be replaced with Redis in production)
    private final ConcurrentHashMap<String, RateLimitEntry> rateLimitMap = 
        new ConcurrentHashMap<>();
    
    @AroundInvoke
    public Object checkRateLimit(InvocationContext context) throws Exception {
        RateLimited rateLimited = context.getMethod().getAnnotation(RateLimited.class);
        if (rateLimited == null) {
            rateLimited = context.getTarget().getClass().getAnnotation(RateLimited.class);
        }
        
        final RateLimited finalRateLimited = rateLimited;
        String user = securityIdentity.isAnonymous() ? "anonymous" : 
            securityIdentity.getPrincipal().getName();
        String key = buildKey(user, finalRateLimited.key(), context.getMethod().getName());
        
        RateLimitEntry entry = rateLimitMap.compute(key, (k, existing) -> {
            LocalDateTime now = LocalDateTime.now();
            
            if (existing == null || 
                ChronoUnit.SECONDS.between(existing.windowStart, now) >= 
                    finalRateLimited.windowSeconds()) {
                return new RateLimitEntry(now, new AtomicInteger(1));
            }
            
            existing.requestCount.incrementAndGet();
            return existing;
        });
        
        if (entry.requestCount.get() > finalRateLimited.maxRequests()) {
            log.warn("Rate limit exceeded for user: {} on method: {}", 
                user, context.getMethod().getName());
            throw new RateLimitExceededException(
                "Rate limit exceeded. Please try again later.");
        }
        
        return context.proceed();
    }
    
    private String buildKey(String user, String customKey, String methodName) {
        if (!customKey.isEmpty()) {
            return user + ":" + customKey;
        }
        return user + ":" + methodName;
    }
    
    private static class RateLimitEntry {
        final LocalDateTime windowStart;
        final AtomicInteger requestCount;
        
        RateLimitEntry(LocalDateTime windowStart, AtomicInteger requestCount) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
        }
    }
}