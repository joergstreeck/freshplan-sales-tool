package de.freshplan.modules.leads.security;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * Interceptor binding annotation for RLS context setting. Ensures GUC variables are set on the same
 * connection used by Hibernate.
 *
 * <p>Sprint 2.1: Proper connection affinity for RLS policies.
 */
@Inherited
@InterceptorBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RlsContext {}
