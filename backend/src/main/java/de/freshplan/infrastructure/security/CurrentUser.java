package de.freshplan.infrastructure.security;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * CDI qualifier to inject the currently authenticated user. This provides a clean abstraction over
 * the security context.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Qualifier @Documented
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
public @interface CurrentUser {}
