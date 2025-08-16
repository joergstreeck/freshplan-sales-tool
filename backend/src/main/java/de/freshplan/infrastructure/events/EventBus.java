package de.freshplan.infrastructure.events;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Event Bus for Domain Events using CDI Events.
 *
 * <p>Provides synchronous and asynchronous event publishing capabilities for decoupling domain
 * operations from their side effects.
 *
 * <p>Uses Quarkus CDI Events under the hood for reliable event delivery. In production, this could
 * be extended to use external message brokers like Kafka or RabbitMQ for cross-service
 * communication.
 */
@ApplicationScoped
public class EventBus {

  private static final Logger LOG = LoggerFactory.getLogger(EventBus.class);

  @Inject Event<Object> eventProducer;

  /**
   * Publishes an event synchronously.
   *
   * <p>All event handlers will be executed in the same transaction as the caller. Use this for
   * critical side effects that must succeed together with the main operation.
   *
   * @param event The domain event to publish
   */
  public void publishSync(Object event) {
    LOG.debug("Publishing sync event: {}", event.getClass().getSimpleName());
    try {
      eventProducer.fire(event);
      LOG.debug("Sync event published successfully: {}", event.getClass().getSimpleName());
    } catch (Exception e) {
      LOG.error("Failed to publish sync event: {}", event.getClass().getSimpleName(), e);
      throw new EventPublishingException(
          "Failed to publish event: " + event.getClass().getSimpleName(), e);
    }
  }

  /**
   * Publishes an event asynchronously.
   *
   * <p>Event handlers will be executed in separate transactions. Use this for non-critical side
   * effects like analytics, notifications, or cache updates that shouldn't block the main
   * operation.
   *
   * @param event The domain event to publish
   */
  public void publishAsync(Object event) {
    LOG.debug("Publishing async event: {}", event.getClass().getSimpleName());
    try {
      eventProducer.fireAsync(event);
      LOG.debug("Async event scheduled successfully: {}", event.getClass().getSimpleName());
    } catch (Exception e) {
      LOG.error("Failed to schedule async event: {}", event.getClass().getSimpleName(), e);
      // Don't throw for async events - log and continue
    }
  }

  /**
   * Publishes an event with a specific qualifier.
   *
   * @param event The domain event to publish
   * @param qualifier The CDI qualifier annotation
   */
  public void publishWithQualifier(Object event, java.lang.annotation.Annotation qualifier) {
    LOG.debug(
        "Publishing qualified event: {} with qualifier: {}",
        event.getClass().getSimpleName(),
        qualifier.annotationType().getSimpleName());
    try {
      eventProducer.select(qualifier).fire(event);
      LOG.debug("Qualified event published successfully");
    } catch (Exception e) {
      LOG.error("Failed to publish qualified event", e);
      throw new EventPublishingException("Failed to publish qualified event", e);
    }
  }

  /** Exception thrown when event publishing fails. */
  public static class EventPublishingException extends RuntimeException {
    public EventPublishingException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
