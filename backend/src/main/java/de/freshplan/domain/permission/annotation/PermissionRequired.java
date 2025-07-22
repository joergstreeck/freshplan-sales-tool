package de.freshplan.domain.permission.annotation;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that require specific permissions.
 *
 * <p>Usage: @PermissionRequired("customers:write") public void createCustomer() { ... }
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
public @interface PermissionRequired {

  /**
   * The permission code required to access this method. Format: "resource:action" (e.g.,
   * "customers:read", "admin:access")
   */
  String value() default "";

  /** Optional message to include in the exception if permission is denied. */
  String message() default "";
}
