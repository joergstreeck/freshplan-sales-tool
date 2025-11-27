package de.freshplan.domain.testdata.service.provider;

/**
 * Statistics about test data in the database
 *
 * <p>Extracted from TestDataService during Sprint 2.1.7.7 Cycle 8-9 fix to break circular
 * dependency between testdata.service, testdata.service.command and testdata.service.query
 * packages. Follows Dependency Inversion Principle (SOLID).
 *
 * @param customerCount Number of test customers
 * @param eventCount Number of test events
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record TestDataStats(long customerCount, long eventCount) {}
