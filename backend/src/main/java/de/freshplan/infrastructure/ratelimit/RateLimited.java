package de.freshplan.infrastructure.ratelimit;

import de.freshplan.shared.constants.RateLimitConstants;
import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply rate limiting to methods.
 *
 * @author FreshPlan Team
 * @since 1.0.0
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

  /** Maximum number of requests allowed within the time window. */
  @Nonbinding
  int maxRequests() default RateLimitConstants.DEFAULT_MAX_REQUESTS;

  /** Time window in seconds. */
  @Nonbinding
  int windowSeconds() default RateLimitConstants.DEFAULT_WINDOW_SECONDS;

  /** Optional rate limit key (e.g., "pdf-export", "user-list"). */
  @Nonbinding
  String key() default "";
}
