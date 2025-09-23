package de.freshplan.infrastructure.cqrs;

import io.quarkus.runtime.Startup;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

/**
 * CQRS Light Event Publisher using PostgreSQL LISTEN/NOTIFY
 * Performance Target: <200ms P95 for event publishing
 * Scale: Optimized for 5-50 internal users
 */
@ApplicationScoped
@Startup
public class EventPublisher {

    private static final Logger LOG = Logger.getLogger(EventPublisher.class);

    @Inject
    DataSource dataSource;

    @ConfigProperty(name = "cqrs.events.enabled", defaultValue = "true")
    boolean eventsEnabled;

    @ConfigProperty(name = "cqrs.events.max-payload-size", defaultValue = "7900")
    int maxPayloadSize;

    /**
     * Publishes a domain event to the event store
     * Events are automatically propagated via LISTEN/NOTIFY trigger
     */
    @Transactional
    public UUID publishEvent(DomainEvent event) {
        if (!eventsEnabled) {
            LOG.debug("CQRS events disabled, skipping event: " + event.getEventType());
            return null;
        }

        validateEvent(event);

        String sql = """
            INSERT INTO domain_events (
                event_type, aggregate_id, aggregate_type,
                payload, user_id, correlation_id, causation_id
            ) VALUES (?, ?, ?, ?::jsonb, ?, ?, ?)
            RETURNING id
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, event.getEventType());
            stmt.setObject(2, event.getAggregateId());
            stmt.setString(3, event.getAggregateType());
            stmt.setString(4, event.getPayload().encode());
            stmt.setString(5, event.getUserId());
            stmt.setObject(6, event.getCorrelationId());
            stmt.setObject(7, event.getCausationId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID eventId = (UUID) rs.getObject("id");
                    LOG.infof("Event published: %s [%s] for aggregate %s",
                        event.getEventType(), eventId, event.getAggregateId());
                    return eventId;
                } else {
                    throw new EventPublishException(
                        "Failed to retrieve event ID after publishing: " + event.getEventType(), null);
                }
            }
        } catch (EventPublishException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Failed to publish event: " + event.getEventType(), e);
            throw new EventPublishException("Failed to publish event: " + event.getEventType(), e);
        }
    }

    /**
     * Publishes an event with correlation to track related events
     */
    public UUID publishCorrelatedEvent(DomainEvent event, UUID correlationId) {
        event.setCorrelationId(correlationId != null ? correlationId : UUID.randomUUID());
        return publishEvent(event);
    }

    /**
     * Validates event before publishing
     */
    private void validateEvent(DomainEvent event) {
        if (event.getEventType() == null || event.getEventType().isEmpty()) {
            throw new IllegalArgumentException("Event type is required");
        }

        if (!event.getEventType().matches("^[a-z]+\\.[a-z]+$")) {
            throw new IllegalArgumentException(
                "Invalid event type format. Expected: module.action (e.g., lead.created)");
        }

        if (event.getAggregateId() == null) {
            throw new IllegalArgumentException("Aggregate ID is required");
        }

        if (event.getPayload() == null) {
            throw new IllegalArgumentException("Event payload is required");
        }

        // Check payload size (7900 bytes limit for PostgreSQL NOTIFY - max is 8KB)
        int payloadSize = event.getPayload().encode().getBytes().length;
        if (payloadSize > maxPayloadSize) {
            throw new IllegalArgumentException(
                String.format("Payload size %d exceeds maximum %d bytes (PostgreSQL NOTIFY limit)",
                    payloadSize, maxPayloadSize));
        }
    }

    /**
     * Domain Event model
     */
    public static class DomainEvent {
        private String eventType;
        private UUID aggregateId;
        private String aggregateType;
        private JsonObject payload;
        private String userId;
        private UUID correlationId;
        private UUID causationId;

        // Builder pattern for convenient event creation
        public static DomainEventBuilder builder() {
            return new DomainEventBuilder();
        }

        // Getters and Setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public UUID getAggregateId() { return aggregateId; }
        public void setAggregateId(UUID aggregateId) { this.aggregateId = aggregateId; }

        public String getAggregateType() { return aggregateType; }
        public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

        public JsonObject getPayload() { return payload; }
        public void setPayload(JsonObject payload) { this.payload = payload; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public UUID getCorrelationId() { return correlationId; }
        public void setCorrelationId(UUID correlationId) { this.correlationId = correlationId; }

        public UUID getCausationId() { return causationId; }
        public void setCausationId(UUID causationId) { this.causationId = causationId; }
    }

    /**
     * Builder for DomainEvent
     */
    public static class DomainEventBuilder {
        private final DomainEvent event = new DomainEvent();

        public DomainEventBuilder eventType(String eventType) {
            event.setEventType(eventType);
            return this;
        }

        public DomainEventBuilder aggregateId(UUID aggregateId) {
            event.setAggregateId(aggregateId);
            return this;
        }

        public DomainEventBuilder aggregateType(String aggregateType) {
            event.setAggregateType(aggregateType);
            return this;
        }

        public DomainEventBuilder payload(JsonObject payload) {
            event.setPayload(payload);
            return this;
        }

        public DomainEventBuilder userId(String userId) {
            event.setUserId(userId);
            return this;
        }

        public DomainEventBuilder correlationId(UUID correlationId) {
            event.setCorrelationId(correlationId);
            return this;
        }

        public DomainEventBuilder causationId(UUID causationId) {
            event.setCausationId(causationId);
            return this;
        }

        public DomainEvent build() {
            return event;
        }
    }

    /**
     * Custom exception for event publishing failures
     */
    public static class EventPublishException extends RuntimeException {
        public EventPublishException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}