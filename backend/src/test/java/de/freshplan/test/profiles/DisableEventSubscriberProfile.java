package de.freshplan.test.profiles;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Map;

/**
 * Test profile that disables the EventSubscriber for isolated testing.
 * Use this profile for tests that mock the EventSubscriber or need to test without background listeners.
 */
public class DisableEventSubscriberProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
            "cqrs.subscriber.enabled", "false",
            // Also disable RLS interceptor for these tests if needed
            "security.rls.interceptor.enabled", "true"
        );
    }

    @Override
    public String getConfigProfile() {
        return "test-no-subscriber";
    }
}