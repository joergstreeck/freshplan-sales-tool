package de.freshplan.infrastructure.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.*;
import org.jboss.logging.Logger;

/**
 * Security audit interceptor that logs all secured API calls. This is useful for compliance and
 * debugging.
 */
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
@SecurityAudit
public class SecurityAuditInterceptor {

  private static final Logger LOG = Logger.getLogger(SecurityAuditInterceptor.class);

  @Inject SecurityContextProvider securityContext;

  @AroundInvoke
  public Object auditSecuredMethod(InvocationContext context) throws Exception {
    // Only audit if user is authenticated
    if (securityContext.isAuthenticated()) {
      String username = securityContext.getUsername();
      String method = context.getMethod().getName();
      String className = context.getTarget().getClass().getSimpleName();

      // Extract HTTP method if available
      String httpMethod = extractHttpMethod(context);

      LOG.infof(
          "Security Audit - User: %s, Method: %s, Class: %s, HTTP: %s",
          username, method, className, httpMethod);
    }

    try {
      // Proceed with the actual method invocation
      return context.proceed();
    } catch (Exception e) {
      // Log security-related exceptions
      if (securityContext.isAuthenticated()) {
        LOG.errorf(
            "Security Audit - Failed call by user %s: %s",
            securityContext.getUsername(), e.getMessage());
      }
      throw e;
    }
  }

  private String extractHttpMethod(InvocationContext context) {
    // Check for JAX-RS annotations
    if (context.getMethod().isAnnotationPresent(GET.class)) return "GET";
    if (context.getMethod().isAnnotationPresent(POST.class)) return "POST";
    if (context.getMethod().isAnnotationPresent(PUT.class)) return "PUT";
    if (context.getMethod().isAnnotationPresent(DELETE.class)) return "DELETE";
    if (context.getMethod().isAnnotationPresent(PATCH.class)) return "PATCH";

    return "UNKNOWN";
  }
}
