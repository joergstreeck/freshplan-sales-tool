package de.freshplan.audit.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.freshplan.audit.entity.AuditLog;
import de.freshplan.audit.entity.AuditLog.*;
import de.freshplan.audit.service.AuditService;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor für automatisches Audit-Logging von Entity-Änderungen.
 * Trackt automatisch alle CREATE, UPDATE und DELETE Operationen.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Audited
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class AuditInterceptor {

  private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);

  @Inject AuditService auditService;
  
  @Inject ObjectMapper objectMapper;

  // Cache für Entity-Type Mapping
  private static final Map<Class<?>, EntityType> ENTITY_TYPE_CACHE = new HashMap<>();

  static {
    // Vorkonfigurierte Mappings
    ENTITY_TYPE_CACHE.put(
        de.freshplan.domain.customer.entity.Customer.class, EntityType.CUSTOMER);
    ENTITY_TYPE_CACHE.put(
        de.freshplan.domain.customer.entity.CustomerContact.class, EntityType.CONTACT);
    ENTITY_TYPE_CACHE.put(
        de.freshplan.domain.opportunity.entity.Opportunity.class, EntityType.OPPORTUNITY);
    ENTITY_TYPE_CACHE.put(de.freshplan.domain.user.entity.User.class, EntityType.USER);
  }

  @AroundInvoke
  public Object auditMethodInvocation(InvocationContext context) throws Exception {
    Method method = context.getMethod();
    String methodName = method.getName();

    // Bestimme die Audit-Action basierend auf Methodennamen
    AuditAction action = determineAction(methodName);
    if (action == null) {
      // Keine Audit-relevante Methode
      return context.proceed();
    }

    Object result = null;
    Object entity = null;
    Object oldEntity = null;

    try {
      // Bei UPDATE: Hole alten Zustand
      if (action == AuditAction.UPDATE) {
        entity = extractEntity(context);
        if (entity != null) {
          oldEntity = cloneEntity(entity);
        }
      }

      // Führe die eigentliche Operation aus
      result = context.proceed();

      // Nach erfolgreicher Ausführung: Audit loggen
      if (action == AuditAction.CREATE || action == AuditAction.UPDATE) {
        entity = result != null ? result : extractEntity(context);
      } else if (action == AuditAction.DELETE) {
        entity = extractEntity(context);
      }

      if (entity != null) {
        logAuditEvent(entity, oldEntity, action);
      }

    } catch (Exception e) {
      // Bei Fehler: Logge den Versuch trotzdem (mit FAILED_ Präfix)
      if (entity != null) {
        logFailedAuditEvent(entity, action, e);
      }
      throw e; // Re-throw original exception
    }

    return result;
  }

  private AuditAction determineAction(String methodName) {
    String lowerMethod = methodName.toLowerCase();

    if (lowerMethod.startsWith("create") || lowerMethod.startsWith("persist")
        || lowerMethod.startsWith("save") || lowerMethod.startsWith("insert")) {
      return AuditAction.CREATE;
    } else if (lowerMethod.startsWith("update") || lowerMethod.startsWith("merge")
        || lowerMethod.startsWith("modify") || lowerMethod.startsWith("change")) {
      return AuditAction.UPDATE;
    } else if (lowerMethod.startsWith("delete") || lowerMethod.startsWith("remove")) {
      return AuditAction.DELETE;
    } else if (lowerMethod.startsWith("find") || lowerMethod.startsWith("get")
        || lowerMethod.startsWith("load") || lowerMethod.startsWith("read")) {
      // Optional: Auch Lesezugriffe tracken (für DSGVO)
      return shouldAuditReads() ? AuditAction.VIEW : null;
    }

    return null;
  }

  private Object extractEntity(InvocationContext context) {
    Object[] params = context.getParameters();
    if (params == null || params.length == 0) {
      return null;
    }

    // Suche nach Entity-Parameter
    for (Object param : params) {
      if (param != null && isEntity(param)) {
        return param;
      }
    }

    return params[0]; // Fallback: Erster Parameter
  }

  private boolean isEntity(Object obj) {
    // Prüfe ob es eine Panache Entity ist
    return obj instanceof PanacheEntityBase
        || ENTITY_TYPE_CACHE.containsKey(obj.getClass());
  }

  private void logAuditEvent(Object entity, Object oldEntity, AuditAction action) {
    try {
      EntityType entityType = determineEntityType(entity);
      UUID entityId = extractEntityId(entity);
      String entityName = extractEntityName(entity);

      if (entityType != null && entityId != null) {
        switch (action) {
          case CREATE:
            auditService.auditCreate(entityType, entityId, entityName, entity);
            break;
          case UPDATE:
            auditService.auditUpdate(entityType, entityId, entityName, oldEntity, entity);
            break;
          case DELETE:
            auditService.auditDelete(entityType, entityId, entityName, entity, "Via Interceptor");
            break;
          case VIEW:
            auditService.auditDataAccess(entityType, entityId, entityName, action);
            break;
        }
      }
    } catch (Exception e) {
      log.error("Failed to log audit event", e);
      // Audit-Fehler dürfen die Hauptoperation nicht blockieren
    }
  }

  private void logFailedAuditEvent(Object entity, AuditAction action, Exception error) {
    try {
      EntityType entityType = determineEntityType(entity);
      UUID entityId = extractEntityId(entity);
      String entityName = extractEntityName(entity);

      if (entityType != null && entityId != null) {
        String errorMessage = "Operation failed: " + error.getMessage();
        auditService.audit(
            entityType,
            entityId,
            entityName,
            AuditAction.SYSTEM_EVENT,
            null,
            null,
            errorMessage,
            "Failed " + action + " attempt");
      }
    } catch (Exception e) {
      log.error("Failed to log failed audit event", e);
    }
  }

  private EntityType determineEntityType(Object entity) {
    if (entity == null) return null;

    Class<?> entityClass = entity.getClass();
    
    // Check cache first
    EntityType cached = ENTITY_TYPE_CACHE.get(entityClass);
    if (cached != null) {
      return cached;
    }

    // Fallback: Bestimme anhand des Klassennamens
    String className = entityClass.getSimpleName();
    try {
      return EntityType.valueOf(className.toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn("Unknown entity type for class: {}", className);
      return EntityType.SYSTEM;
    }
  }

  private UUID extractEntityId(Object entity) {
    if (entity == null) return null;

    try {
      // Versuche getId() Methode
      Method getIdMethod = entity.getClass().getMethod("getId");
      Object id = getIdMethod.invoke(entity);
      
      if (id instanceof UUID) {
        return (UUID) id;
      } else if (id instanceof String) {
        return UUID.fromString((String) id);
      }
    } catch (Exception e) {
      log.debug("Could not extract entity ID", e);
    }

    return null;
  }

  private String extractEntityName(Object entity) {
    if (entity == null) return null;

    try {
      // Versuche getName() oder ähnliche Methoden
      for (String methodName : new String[]{"getName", "getTitle", "getDisplayName", "toString"}) {
        try {
          Method method = entity.getClass().getMethod(methodName);
          Object name = method.invoke(entity);
          if (name != null) {
            return name.toString();
          }
        } catch (NoSuchMethodException ignored) {
          // Try next method
        }
      }
    } catch (Exception e) {
      log.debug("Could not extract entity name", e);
    }

    return entity.getClass().getSimpleName() + "#" + extractEntityId(entity);
  }

  private Object cloneEntity(Object entity) {
    // Deep copy der Entity mittels Jackson Serialization
    // Dies erfasst den tatsächlichen Zustand VOR der Änderung
    try {
      // Serialisiert und deserialisiert das Objekt für eine echte Deep Copy
      String json = objectMapper.writeValueAsString(entity);
      return objectMapper.readValue(json, entity.getClass());
    } catch (Exception e) {
      log.warn("Could not clone entity for audit. Old values will be missing.", e);
      return null;
    }
  }

  private boolean shouldAuditReads() {
    // Konfigurierbar: Sollen auch Lesezugriffe geloggt werden?
    // Für DSGVO kann dies erforderlich sein
    return false; // Default: Keine Read-Audits
  }
}