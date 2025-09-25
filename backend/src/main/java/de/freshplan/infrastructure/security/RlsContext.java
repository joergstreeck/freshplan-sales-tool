package de.freshplan.infrastructure.security;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks methods that require RLS context to be set on the database connection.
 * This annotation ensures that PostgreSQL GUC variables (app.current_user, app.current_territory, etc.)
 * are set on the same connection used for the database operations within the annotated method.
 *
 * IMPORTANT: Must be used together with @Transactional to ensure connection affinity.
 *
 * @see RlsConnectionAffinityGuard
 */
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RlsContext {
}