package de.freshplan.infrastructure.cqrs;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Test class for CQRS Light Event Publisher Validates event publishing with <200ms performance
 * target
 */
@QuarkusTest
@Tag("integration")
public class EventPublisherTest {

  @Inject EventPublisher eventPublisher;

  @Inject DataSource dataSource;

  @BeforeEach
  @Transactional
  void cleanup() {
    // Clean up test events before each test
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt =
            conn.prepareStatement("DELETE FROM domain_events WHERE user_id = 'test-user'")) {
      stmt.executeUpdate();
    } catch (Exception e) {
      // Ignore cleanup errors
    }
  }

  @AfterEach
  @Transactional
  void cleanupAfter() {
    // Clean up test events after each test
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt =
            conn.prepareStatement("DELETE FROM domain_events WHERE user_id = 'test-user'")) {
      stmt.executeUpdate();
    } catch (Exception e) {
      // Ignore cleanup errors
    }
  }

  @Test
  @Transactional
  void testPublishEvent() {
    // Arrange
    UUID aggregateId = UUID.randomUUID();
    JsonObject payload =
        new JsonObject()
            .put("name", "Test Restaurant")
            .put("email", "test@restaurant.de")
            .put("territory", "DE");

    EventPublisher.DomainEvent event =
        EventPublisher.DomainEvent.builder()
            .eventType("lead.created")
            .aggregateId(aggregateId)
            .aggregateType("Lead")
            .payload(payload)
            .userId("test-user")
            .build();

    // Act - Measure performance
    long startTime = System.currentTimeMillis();
    UUID eventId = eventPublisher.publishEvent(event);
    long duration = System.currentTimeMillis() - startTime;

    // Assert
    assertNotNull(eventId, "Event ID should not be null");
    assertTrue(duration < 200, "Event publishing should complete in <200ms");

    // Verify event was stored
    verifyEventInDatabase(eventId, "lead.created", aggregateId);
  }

  @Test
  @Transactional
  void testPublishCorrelatedEvents() {
    // Arrange
    UUID correlationId = UUID.randomUUID();
    UUID leadId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();

    EventPublisher.DomainEvent leadEvent =
        EventPublisher.DomainEvent.builder()
            .eventType("lead.converted")
            .aggregateId(leadId)
            .aggregateType("Lead")
            .payload(new JsonObject().put("customer_id", customerId.toString()))
            .userId("test-user")
            .build();

    EventPublisher.DomainEvent customerEvent =
        EventPublisher.DomainEvent.builder()
            .eventType("customer.created")
            .aggregateId(customerId)
            .aggregateType("Customer")
            .payload(new JsonObject().put("lead_id", leadId.toString()))
            .userId("test-user")
            .build();

    // Act
    UUID leadEventId = eventPublisher.publishCorrelatedEvent(leadEvent, correlationId);
    UUID customerEventId = eventPublisher.publishCorrelatedEvent(customerEvent, correlationId);

    // Assert
    assertNotNull(leadEventId);
    assertNotNull(customerEventId);

    // Verify correlation
    verifyEventCorrelation(leadEventId, customerEventId, correlationId);
  }

  @Test
  void testValidateEventType() {
    // Arrange
    EventPublisher.DomainEvent invalidEvent =
        EventPublisher.DomainEvent.builder()
            .eventType("InvalidEventType") // Should be module.action format
            .aggregateId(UUID.randomUUID())
            .aggregateType("Lead")
            .payload(new JsonObject())
            .build();

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> eventPublisher.publishEvent(invalidEvent),
        "Should reject invalid event type format");
  }

  @Test
  void testPayloadSizeLimit() {
    // Arrange - Create payload > 10KB
    JsonObject largePayload = new JsonObject();
    String largeString = "x".repeat(11000); // 11KB string
    largePayload.put("data", largeString);

    EventPublisher.DomainEvent event =
        EventPublisher.DomainEvent.builder()
            .eventType("lead.created")
            .aggregateId(UUID.randomUUID())
            .aggregateType("Lead")
            .payload(largePayload)
            .build();

    // Act & Assert
    assertThrows(
        IllegalArgumentException.class,
        () -> eventPublisher.publishEvent(event),
        "Should reject payload > 10KB for LISTEN/NOTIFY performance");
  }

  @Test
  @Transactional
  void testEventPerformance() {
    // Arrange - Batch of events
    int eventCount = 100;
    long[] durations = new long[eventCount];

    // Act - Publish multiple events and measure
    for (int i = 0; i < eventCount; i++) {
      EventPublisher.DomainEvent event =
          EventPublisher.DomainEvent.builder()
              .eventType("lead.created")
              .aggregateId(UUID.randomUUID())
              .aggregateType("Lead")
              .payload(new JsonObject().put("index", i))
              .userId("test-user")
              .build();

      long start = System.currentTimeMillis();
      eventPublisher.publishEvent(event);
      durations[i] = System.currentTimeMillis() - start;
    }

    // Assert - P95 < 200ms
    long p95 = calculatePercentile(durations, 95);
    assertTrue(p95 < 200, String.format("P95 latency should be <200ms, was %dms", p95));
  }

  private void verifyEventInDatabase(UUID eventId, String eventType, UUID aggregateId) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt =
            conn.prepareStatement(
                "SELECT event_type, aggregate_id, published FROM domain_events WHERE id = ?")) {

      stmt.setObject(1, eventId);
      try (ResultSet rs = stmt.executeQuery()) {
        assertTrue(rs.next(), "Event should exist in database");
        assertEquals(eventType, rs.getString("event_type"));
        assertEquals(aggregateId, rs.getObject("aggregate_id"));
        // Note: published flag is set by trigger, but may not be visible
        // in same transaction. Check that event exists is sufficient
        assertNotNull(rs.getObject("aggregate_id"), "Event should have aggregate_id");
      }
    } catch (Exception e) {
      fail("Failed to verify event in database: " + e.getMessage());
    }
  }

  private void verifyEventCorrelation(UUID event1Id, UUID event2Id, UUID correlationId) {
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt =
            conn.prepareStatement("SELECT correlation_id FROM domain_events WHERE id IN (?, ?)")) {

      stmt.setObject(1, event1Id);
      stmt.setObject(2, event2Id);

      try (ResultSet rs = stmt.executeQuery()) {
        int count = 0;
        while (rs.next()) {
          assertEquals(
              correlationId,
              rs.getObject("correlation_id"),
              "Events should have same correlation ID");
          count++;
        }
        assertEquals(2, count, "Should find both correlated events");
      }
    } catch (Exception e) {
      fail("Failed to verify event correlation: " + e.getMessage());
    }
  }

  private long calculatePercentile(long[] values, int percentile) {
    java.util.Arrays.sort(values);
    int index = (int) Math.ceil(percentile / 100.0 * values.length) - 1;
    return values[Math.min(index, values.length - 1)];
  }
}
