=== TESTDATABUILDER MIGRATION ANALYSIS REPORT ===

Date: Mo 18 Aug 2025 16:54:22 CEST

## Summary
- Total test files: 154
- Tests using builders: 33
- Tests with isTestData(true): 31

## Available Builders
- ContactBuilder
- ContactInteractionBuilder
- CustomerBuilder
- OpportunityBuilder
- TimelineEventBuilder
- UserBuilder

## Migration Status

### ✅ Fully Migrated (Using Builders)
- ContactEventCaptureCQRSIntegrationTest.java
- ContactInteractionQueryServiceTest.java
- ContactInteractionResourceIT.java
- ContactInteractionServiceIT.java
- ContactPerformanceTest.java
- ContactQueryServiceTest.java
- ContactRepositoryTest.java
- ContactsCountConsistencyTest.java
- CustomerContactTest.java
- CustomerMapperTest.java
- CustomerQueryServiceIntegrationTest.java
- CustomerRepositoryTest.java
- CustomerSearchBasicTest.java
- CustomerSearchFilterTest.java
- CustomerSearchPaginationTest.java
- CustomerSearchSmartSortTest.java
- CustomerSearchSortTest.java
- CustomerTimelineRepositoryPerformanceTest.java
- CustomerTimelineResourceIT.java
- CustomerTimelineServiceTest.java

### ⚠️ Partially Migrated / Need Review
- ContactInteractionCommandServiceTest (still uses new ContactInteraction())
- ContactInteractionServiceCQRSIntegrationTest (still uses new ContactInteraction())

### ❌ Not Using Builders (Direct Entity Creation)
- ContactInteractionCommandServiceTest.java
- ContactInteractionServiceCQRSIntegrationTest.java
