package de.freshplan.domain.permission.interceptor;

import de.freshplan.domain.permission.annotation.PermissionRequired;
import de.freshplan.domain.permission.service.PermissionService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor that automatically checks permissions for methods annotated with @PermissionRequired.
 */
@Interceptor
@PermissionRequired("")
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 200)
public class PermissionInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(PermissionInterceptor.class);

  @Inject PermissionService permissionService;

  @AroundInvoke
  public Object checkPermission(InvocationContext context) throws Exception {
    PermissionRequired annotation = context.getMethod().getAnnotation(PermissionRequired.class);
    
    // Check for class-level annotation if method-level annotation is not present
    if (annotation == null) {
      annotation = context.getTarget().getClass().getAnnotation(PermissionRequired.class);
    }

    if (annotation != null) {
      String requiredPermission = annotation.value();
      String customMessage = annotation.message();

      logger.debug(
          "Checking permission '{}' for method '{}.{}'",
          requiredPermission,
          context.getMethod().getDeclaringClass().getSimpleName(),
          context.getMethod().getName());

      if (!permissionService.hasPermission(requiredPermission)) {
        String message =
            customMessage.isEmpty() ? "Missing permission: " + requiredPermission : customMessage;

        logger.warn(
            "Permission denied: {} for method {}.{}",
            requiredPermission,
            context.getMethod().getDeclaringClass().getSimpleName(),
            context.getMethod().getName());

        throw new ForbiddenException(message);
      }
    }

    return context.proceed();
  }
}
