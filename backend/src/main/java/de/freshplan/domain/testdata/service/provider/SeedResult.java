package de.freshplan.domain.testdata.service.provider;

/**
 * Result of a test data seeding operation
 *
 * <p>Extracted from TestDataService during Sprint 2.1.7.7 Cycle 8-9 fix to break circular
 * dependency between testdata.service, testdata.service.command and testdata.service.query
 * packages. Follows Dependency Inversion Principle (SOLID).
 *
 * @param customersCreated Number of customers created
 * @param eventsCreated Number of events created
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record SeedResult(int customersCreated, int eventsCreated) {}
