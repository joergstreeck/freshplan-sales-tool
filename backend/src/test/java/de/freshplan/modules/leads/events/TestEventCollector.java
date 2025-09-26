package de.freshplan.modules.leads.events;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.interceptor.Interceptor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Test-specific event collector for verifying event processing in integration tests.
 *
 * <p>Collects events after they have been processed by the main listeners, allowing tests to verify
 * that the entire LISTEN/NOTIFY mechanism works correctly.
 *
 * <p>Part of FP-236 Security Integration Testing
 */
@ApplicationScoped
public class TestEventCollector {

  private final BlockingQueue<LeadStatusChangeEvent> receivedStatusChangeEvents =
      new LinkedBlockingQueue<>();
  private final BlockingQueue<LeadEventPublisher.LeadCreatedEvent> receivedCreatedEvents =
      new LinkedBlockingQueue<>();
  private final BlockingQueue<Object> allEvents = new LinkedBlockingQueue<>();

  /**
   * Observes LeadStatusChangeEvent with lower priority than main listeners.
   *
   * @param event The status change event
   */
  void onStatusChangeEvent(
      @Observes @Priority(Interceptor.Priority.APPLICATION - 100) LeadStatusChangeEvent event) {
    receivedStatusChangeEvents.add(event);
    allEvents.add(event);
  }

  /**
   * Polls for a status change event with timeout.
   *
   * @param timeout The timeout value
   * @param unit The timeout unit
   * @return The event or null if timeout
   * @throws InterruptedException if interrupted
   */
  public LeadStatusChangeEvent pollStatusChangeEvent(long timeout, TimeUnit unit)
      throws InterruptedException {
    return receivedStatusChangeEvents.poll(timeout, unit);
  }

  /**
   * Polls for any event with timeout.
   *
   * @param timeout The timeout value
   * @param unit The timeout unit
   * @return The event or null if timeout
   * @throws InterruptedException if interrupted
   */
  public Object pollAnyEvent(long timeout, TimeUnit unit) throws InterruptedException {
    return allEvents.poll(timeout, unit);
  }

  /** Clears all collected events. */
  public void clear() {
    receivedStatusChangeEvents.clear();
    receivedCreatedEvents.clear();
    allEvents.clear();
  }

  /**
   * Returns the count of status change events received.
   *
   * @return The count
   */
  public int getStatusChangeEventCount() {
    return receivedStatusChangeEvents.size();
  }

  /**
   * Returns the count of all events received.
   *
   * @return The count
   */
  public int getTotalEventCount() {
    return allEvents.size();
  }

  /**
   * Checks if a specific event with idempotency key was received.
   *
   * @param idempotencyKey The key to check
   * @return true if received
   */
  public boolean hasReceivedEventWithKey(String idempotencyKey) {
    return receivedStatusChangeEvents.stream()
        .anyMatch(e -> idempotencyKey.equals(e.getIdempotencyKey()));
  }
}
