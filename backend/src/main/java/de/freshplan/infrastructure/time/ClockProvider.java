package de.freshplan.infrastructure.time;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.time.Clock;
import java.time.ZoneOffset;

/**
 * Clock provider for dependency injection.
 * 
 * Provides a Clock instance that can be injected for timezone-safe operations.
 * Uses UTC timezone to avoid timezone-related bugs in business logic.
 */
@ApplicationScoped
public class ClockProvider {

    /**
     * Produces a Clock instance using UTC timezone.
     * 
     * @return Clock instance set to UTC
     */
    @Produces
    @ApplicationScoped
    public Clock clock() {
        return Clock.system(ZoneOffset.UTC);
    }
}