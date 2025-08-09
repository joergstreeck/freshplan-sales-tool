package de.freshplan.test;

import de.freshplan.domain.audit.entity.AuditEntry;
import de.freshplan.domain.audit.entity.AuditEventType;
import de.freshplan.domain.audit.entity.AuditSource;
import de.freshplan.domain.audit.repository.AuditRepository;
import de.freshplan.domain.audit.service.AuditService;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.user.entity.User;
import de.freshplan.domain.user.repository.UserRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import org.jboss.logging.Logger;

/**
 * Test data initializer for Audit System. Creates realistic audit trail entries for dashboard
 * display. Active only in dev and test profiles.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class AuditTestDataInitializer {

  private static final Logger LOG = Logger.getLogger(AuditTestDataInitializer.class);

  @Inject AuditRepository auditRepository;

  @Inject AuditService auditService;

  @Inject UserRepository userRepository;

  @Inject CustomerRepository customerRepository;

  // Realistic user agents
  private static final List<String> USER_AGENTS =
      Arrays.asList(
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0",
          "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_1) Safari/17.1",
          "Mozilla/5.0 (X11; Linux x86_64) Firefox/121.0",
          "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) Mobile/15E148",
          "FreshPlan Mobile/2.1.0 (iOS 17.1)",
          "FreshPlan API Client/1.0",
          "Postman/10.20.0",
          "curl/8.1.2");

  // Realistic IP addresses
  private static final List<String> IP_ADDRESSES =
      Arrays.asList(
          "192.168.1.100",
          "192.168.1.101",
          "192.168.1.102",
          "10.0.1.50",
          "10.0.1.51",
          "172.16.0.10",
          "85.182.92.145", // External IP
          "217.160.0.99", // External IP
          "::1", // IPv6 localhost
          "2001:db8::1"); // IPv6

  @Transactional
  void onStart(@Observes StartupEvent ev) {
    // Skip initialization if we're running in test mode
    String testProfile = System.getProperty("quarkus.test.profile");
    if ("test".equals(testProfile) || Boolean.getBoolean("quarkus.test")) {
      LOG.debug("Skipping audit test data initialization in test mode");
      return;
    }

    LOG.info("Initializing audit test data...");

    // Check if we already have audit entries
    long existingCount = auditRepository.count();
    if (existingCount > 500) {
      LOG.infof("Already have %d audit entries, skipping initialization", existingCount);
      return;
    }

    LOG.infof("Found %d existing audit entries, adding more test data...", existingCount);

    try {
      // Get existing users and customers for realistic references
      List<User> users = userRepository.listAll();
      List<Customer> customers = customerRepository.listAll();

      if (users.isEmpty() || customers.isEmpty()) {
        LOG.warn("No users or customers found, skipping audit data initialization");
        return;
      }

      // Generate audit entries for the last 30 days
      generateAuditHistory(users, customers, 30);

      LOG.info("Audit test data initialized successfully");

    } catch (Exception e) {
      LOG.error("Failed to initialize audit test data", e);
    }
  }

  private void generateAuditHistory(List<User> users, List<Customer> customers, int days) {
    Random random = ThreadLocalRandom.current();
    Instant now = Instant.now();

    // Track session IDs per user
    Map<String, UUID> userSessions = new HashMap<>();

    // Generate entries for each day
    for (int day = days; day >= 0; day--) {
      Instant dayStart = now.minus(day, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);

      // Vary activity by day of week (more on weekdays)
      int eventsForDay =
          day % 7 == 0 || day % 7 == 6 ? 20 + random.nextInt(30) : 50 + random.nextInt(100);

      for (int i = 0; i < eventsForDay; i++) {
        // Random time within the day (business hours weighted)
        int hour = generateBusinessHour(random);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);

        Instant eventTime =
            dayStart
                .plus(hour, ChronoUnit.HOURS)
                .plus(minute, ChronoUnit.MINUTES)
                .plus(second, ChronoUnit.SECONDS);

        // Skip future events
        if (eventTime.isAfter(now)) {
          continue;
        }

        // Select random user
        User user = users.get(random.nextInt(users.size()));

        // Get or create session for user
        UUID sessionId = userSessions.computeIfAbsent(user.getUsername(), k -> UUID.randomUUID());

        // 10% chance to create new session
        if (random.nextDouble() < 0.1) {
          sessionId = UUID.randomUUID();
          userSessions.put(user.getUsername(), sessionId);
        }

        // Generate realistic event based on probabilities
        AuditEventType eventType = selectRandomEventType(random, user);

        // Create audit entry based on event type
        createAuditEntry(eventType, user, customers, eventTime, sessionId, random);
      }
    }

    // Add some recent critical events for dashboard display
    generateRecentCriticalEvents(users, customers);
  }

  private int generateBusinessHour(Random random) {
    // Weight towards business hours (8-18)
    double rand = random.nextDouble();
    if (rand < 0.7) {
      return 8 + random.nextInt(10); // 8-17 (70% probability)
    } else if (rand < 0.9) {
      return 6 + random.nextInt(2); // 6-7 or 18-19 (20% probability)
    } else {
      return random.nextInt(24); // Any hour (10% probability)
    }
  }

  private AuditEventType selectRandomEventType(Random random, User user) {
    double rand = random.nextDouble();

    // Different probabilities based on user role
    boolean isAdmin = user.getRoles() != null && user.getRoles().contains("admin");
    boolean isSales = user.getRoles() != null && user.getRoles().contains("sales");

    if (rand < 0.25) {
      // Login events (25%)
      return random.nextDouble() < 0.95
          ? AuditEventType.LOGIN_SUCCESS
          : AuditEventType.LOGIN_FAILURE;
    } else if (rand < 0.40) {
      // Opportunity events (15%)
      return AuditEventType.OPPORTUNITY_UPDATED;
    } else if (rand < 0.55) {
      // Customer updates (15%)
      return AuditEventType.CUSTOMER_UPDATED;
    } else if (rand < 0.65) {
      // Activity events (10%)
      return AuditEventType.ACTIVITY_CREATED;
    } else if (rand < 0.75) {
      // Customer creation (10%)
      return AuditEventType.CUSTOMER_CREATED;
    } else if (rand < 0.80) {
      // Data exports (5%)
      return isAdmin || isSales
          ? AuditEventType.DATA_EXPORT_STARTED
          : AuditEventType.OPPORTUNITY_CREATED;
    } else if (rand < 0.85) {
      // Permission changes (5% - admin only)
      return isAdmin ? AuditEventType.PERMISSION_GRANTED : AuditEventType.OPPORTUNITY_STAGE_CHANGED;
    } else if (rand < 0.90) {
      // Calculation events (5%)
      return AuditEventType.CALCULATION_PERFORMED;
    } else if (rand < 0.93) {
      // System maintenance (3% - admin only)
      return isAdmin ? AuditEventType.MAINTENANCE_MODE_ENABLED : AuditEventType.NOTE_ADDED;
    } else if (rand < 0.96) {
      // Permission denied (3%)
      return AuditEventType.PERMISSION_DENIED;
    } else if (rand < 0.98) {
      // Customer deletion (2%)
      return AuditEventType.CUSTOMER_DELETED;
    } else {
      // Configuration changes (2% - admin only)
      return isAdmin ? AuditEventType.CONFIGURATION_CHANGED : AuditEventType.TASK_ASSIGNED;
    }
  }

  private void createAuditEntry(
      AuditEventType eventType,
      User user,
      List<Customer> customers,
      Instant timestamp,
      UUID sessionId,
      Random random) {

    try {
      AuditEntry.Builder builder =
          AuditEntry.builder()
              .timestamp(timestamp)
              .eventType(eventType)
              .userId(UUID.fromString(user.getId().toString()))
              .userName(user.getFirstName() + " " + user.getLastName())
              .userRole(
                  user.getRoles() != null && !user.getRoles().isEmpty()
                      ? user.getRoles().get(0).toUpperCase()
                      : "USER")
              .sessionId(sessionId)
              .ipAddress(IP_ADDRESSES.get(random.nextInt(IP_ADDRESSES.size())))
              .userAgent(USER_AGENTS.get(random.nextInt(USER_AGENTS.size())))
              .source(selectRandomSource(random));

      // Set entity information based on event type
      String eventName = eventType.name();

      if (eventName.startsWith("CUSTOMER_")) {
        if (!customers.isEmpty()) {
          Customer customer = customers.get(random.nextInt(customers.size()));
          builder.entityType("CUSTOMER").entityId(customer.getId());

          if (eventType == AuditEventType.CUSTOMER_UPDATED) {
            // Add some change details
            builder.oldValue("{\"status\":\"LEAD\"}").newValue("{\"status\":\"CUSTOMER\"}");
          }
        }
      } else if (eventName.startsWith("OPPORTUNITY_")) {
        builder.entityType("OPPORTUNITY").entityId(UUID.randomUUID());

        if (eventType == AuditEventType.OPPORTUNITY_STAGE_CHANGED) {
          builder.oldValue("{\"stage\":\"QUALIFICATION\"}").newValue("{\"stage\":\"PROPOSAL\"}");
        } else if (eventType == AuditEventType.OPPORTUNITY_VALUE_CHANGED) {
          builder.oldValue("{\"value\":50000}").newValue("{\"value\":75000}");
        }
      } else if (eventName.startsWith("LOGIN_")
          || eventName.startsWith("LOGOUT")
          || eventName.startsWith("SESSION_")) {
        builder.entityType("USER").entityId(UUID.fromString(user.getId().toString()));
      } else if (eventName.startsWith("DATA_EXPORT_") || eventName.startsWith("DATA_IMPORT_")) {
        builder
            .entityType("DATA_OPERATION")
            .entityId(UUID.randomUUID())
            .newValue("{\"format\":\"CSV\",\"records\":1000}");
      } else if (eventName.startsWith("CALCULATION_")) {
        builder
            .entityType("CALCULATION")
            .entityId(UUID.randomUUID())
            .newValue("{\"products\":15,\"total\":125000}");
      } else if (eventName.startsWith("PERMISSION_") || eventName.startsWith("ROLE_")) {
        builder
            .entityType("PERMISSION")
            .entityId(UUID.randomUUID())
            .newValue("{\"resource\":\"CUSTOMER_DATA\",\"action\":\"EXPORT\"}");
      } else if (eventName.startsWith("ACTIVITY_")
          || eventName.startsWith("TASK_")
          || eventName.startsWith("NOTE_")) {
        builder.entityType("ACTIVITY").entityId(UUID.randomUUID());

        if (eventType == AuditEventType.NOTE_ADDED) {
          builder.newValue("{\"note\":\"Customer interested in premium features\"}");
        }
      } else if (eventName.startsWith("CONFIGURATION_") || eventName.startsWith("MAINTENANCE_")) {
        builder.entityType("SYSTEM").entityId(UUID.randomUUID());

        if (eventType == AuditEventType.CONFIGURATION_CHANGED) {
          builder.oldValue("{\"retention_days\":90}").newValue("{\"retention_days\":180}");
        }
      } else {
        builder.entityType("GENERAL").entityId(UUID.randomUUID());
      }

      // Generate data hash
      String dataHash = generateDataHash(timestamp, eventType, user.getId().toString());
      builder.dataHash(dataHash);

      // Persist the entry
      AuditEntry entry = builder.build();
      auditRepository.persist(entry);

    } catch (Exception e) {
      LOG.warnf("Failed to create audit entry for event type %s: %s", eventType, e.getMessage());
    }
  }

  private AuditSource selectRandomSource(Random random) {
    double rand = random.nextDouble();
    if (rand < 0.6) {
      return AuditSource.UI;
    } else if (rand < 0.8) {
      return AuditSource.API;
    } else if (rand < 0.9) {
      return AuditSource.MOBILE;
    } else if (rand < 0.95) {
      return AuditSource.SYSTEM;
    } else {
      return AuditSource.BATCH;
    }
  }

  private String generateDataHash(Instant timestamp, AuditEventType eventType, String userId) {
    // Simple hash generation for test data
    String input = timestamp.toString() + eventType.toString() + userId;
    return Integer.toHexString(input.hashCode());
  }

  private void generateRecentCriticalEvents(List<User> users, List<Customer> customers) {
    Random random = ThreadLocalRandom.current();
    Instant now = Instant.now();

    // Critical event types
    AuditEventType[] criticalEvents = {
      AuditEventType.LOGIN_FAILURE,
      AuditEventType.PERMISSION_DENIED,
      AuditEventType.DATA_EXPORT_STARTED,
      AuditEventType.CUSTOMER_DELETED,
      AuditEventType.CONFIGURATION_CHANGED,
      AuditEventType.SECURITY_VIOLATION,
      AuditEventType.CRITICAL_ERROR,
      AuditEventType.GDPR_REQUEST
    };

    // Generate 10-20 critical events in the last 24 hours
    int criticalCount = 10 + random.nextInt(11);
    for (int i = 0; i < criticalCount; i++) {
      Instant eventTime = now.minus(random.nextInt(24), ChronoUnit.HOURS);
      User user = users.get(random.nextInt(users.size()));
      AuditEventType eventType = criticalEvents[random.nextInt(criticalEvents.length)];

      createAuditEntry(eventType, user, customers, eventTime, UUID.randomUUID(), random);
    }

    LOG.infof("Generated %d critical events for dashboard display", criticalCount);
  }
}
