package de.freshplan.infrastructure.time;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import java.time.Clock;
import java.time.ZoneOffset;

/**
 * Clock provider for dependency injection.
 *
 * <p>Provides a Clock instance that can be injected for timezone-safe operations. Uses UTC timezone
 * to avoid timezone-related bugs in business logic.
 *
 * <p>IMPORTANT: This class uses @Dependent scope (not @ApplicationScoped) because:
 *
 * <ul>
 *   <li>Clock is a final JDK class and cannot be proxied by CDI
 *   <li>ArC (Quarkus CDI) throws ArcUndeclaredThrowable when trying to proxy final classes
 *   <li>@Dependent scope creates a new instance for each injection point (no proxy needed)
 *   <li>This has minimal performance impact since Clock.system() is lightweight
 * </ul>
 */
@Dependent
public class ClockProvider {

  /**
   * Produces a Clock instance using UTC timezone.
   *
   * <p>Uses @Dependent scope (inherited from class) to avoid CDI proxy issues with final Clock
   * class.
   *
   * @return Clock instance set to UTC
   */
  @Produces
  @Dependent
  public Clock clock() {
    return Clock.system(ZoneOffset.UTC);
  }
}
