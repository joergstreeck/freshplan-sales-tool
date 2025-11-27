package de.freshplan.infrastructure.time;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Clock;
import java.time.ZoneOffset;

/**
 * Clock provider for dependency injection.
 *
 * <p>Provides a Clock instance that can be injected for timezone-safe operations. Uses UTC timezone
 * to avoid timezone-related bugs in business logic.
 */
@ApplicationScoped
public class ClockProvider {

  /**
   * Produces a Clock instance using UTC timezone.
   *
   * <p>Note: Uses default @Dependent scope instead of @ApplicationScoped because Clock is a final
   * class and cannot be proxied by CDI. This prevents ArcUndeclaredThrowable errors.
   *
   * @return Clock instance set to UTC
   */
  @Produces
  public Clock clock() {
    return Clock.system(ZoneOffset.UTC);
  }
}
