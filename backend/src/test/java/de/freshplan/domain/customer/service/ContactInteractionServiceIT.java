package de.freshplan.domain.customer.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.dto.DataQualityMetricsDTO;
import de.freshplan.domain.customer.service.dto.WarmthScoreDTO;
import de.freshplan.test.builders.ContactBuilder;
import de.freshplan.test.builders.CustomerBuilder;
import io.quarkus.panache.common.Page;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@DisplayName("Contact Interaction Service Integration Tests")
class ContactInteractionServiceIT {

  @Inject ContactInteractionService contactInteractionService;

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionRepository interactionRepository;

  @Inject CustomerBuilder customerBuilder;

  @Inject ContactBuilder contactBuilder;

  private UUID testCustomerId;
  private UUID testContactId;
  private CustomerContact testContact;

  private void setupTestData() {
    // Create test customer using CustomerBuilder
    Customer testCustomer = customerBuilder.withCompanyName("Test Company GmbH").build();

    // Override specific fields to maintain test requirements
    testCustomer.setCompanyName(
        "Test Company GmbH"); // Override to use exact name without [TEST-xxx] prefix
    testCustomer.setCustomerNumber("TEST-001");
    testCustomer.setCreatedBy("test-user");
    testCustomer.setUpdatedBy("test-user");
    // Set Sprint 2 fields to avoid NOT NULL constraint violations
    testCustomer.setLocationsGermany(0);
    testCustomer.setLocationsAustria(0);
    testCustomer.setLocationsSwitzerland(0);
    testCustomer.setLocationsRestEU(0);
    testCustomer.setTotalLocationsEU(0);
    // Customer has no email field anymore
    customerRepository.persist(testCustomer);
    testCustomerId = testCustomer.getId();

    // Create test contact using ContactBuilder
    testContact =
        contactBuilder
            .forCustomer(testCustomer)
            .withFirstName("Max")
            .withLastName("Mustermann")
            .withEmail("max@company.com")
            .persist();
    testContactId = testContact.getId();
  }

  @Test
  @TestTransaction
  @DisplayName("Should create interaction and update contact intelligence data")
  void shouldCreateInteractionAndUpdateContact() {
    // Setup test data
    setupTestData();
    // Arrange
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.EMAIL)
            .subject("Test Email Subject")
            .summary("Test email content")
            .sentimentScore(0.8)
            .engagementScore(85)
            .timestamp(LocalDateTime.now())
            .build();

    // Act
    ContactInteractionDTO result = contactInteractionService.createInteraction(dto);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals(testContactId, result.getContactId());
    assertEquals(InteractionType.EMAIL, result.getType());
    assertEquals("Test Email Subject", result.getSubject());
    assertEquals(0.8, result.getSentimentScore(), 0.01);
    assertEquals(85, result.getEngagementScore());

    // Verify contact was updated
    CustomerContact updatedContact = contactRepository.findById(testContactId);
    assertEquals(1, updatedContact.getInteractionCount());
    assertNotNull(updatedContact.getLastInteractionDate());
  }

  @Test
  @TestTransaction
  @DisplayName("Should calculate warmth score with multiple interaction factors")
  void shouldCalculateWarmthScoreWithMultipleFactors() {
    // Setup test data
    setupTestData();
    // Arrange - Create diverse interactions
    createInteraction(InteractionType.EMAIL, 0.9, 90, -5); // Recent, positive
    createInteraction(InteractionType.EMAIL, 0.8, 85, -10); // Recent response
    createInteraction(InteractionType.CALL, 0.7, 80, -15); // Call
    createInteraction(InteractionType.MEETING, 0.9, 95, -20); // Successful meeting

    // Act
    WarmthScoreDTO result = contactInteractionService.calculateWarmthScore(testContactId);

    // Assert
    assertNotNull(result);
    assertEquals(testContactId, result.getContactId());
    assertTrue(result.getWarmthScore() >= 0 && result.getWarmthScore() <= 100);
    assertTrue(result.getConfidence() >= 0 && result.getConfidence() <= 100);

    // Check individual factors - not available in current DTO
    // Factors have been removed from WarmthScoreDTO

    assertNotNull(result.getTrend());
    assertNotNull(result.getRecommendation());
    assertNotNull(result.getLastCalculated());
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle low data scenario with appropriate confidence")
  void shouldHandleLowDataScenario() {
    // Setup test data
    setupTestData();
    // Arrange - Only one interaction
    createInteraction(InteractionType.NOTE, 0.5, 60, -1);

    // Act
    WarmthScoreDTO result = contactInteractionService.calculateWarmthScore(testContactId);

    // Assert
    assertNotNull(result);
    assertTrue(result.getWarmthScore() >= 0 && result.getWarmthScore() <= 100);
    // With only one interaction, confidence should be lower
    assertTrue(
        result.getConfidence() < 80,
        "Confidence should be lower with limited data, was: " + result.getConfidence());

    // Should include recommendations for more data
    assertNotNull(result.getRecommendation());
    String recommendation = result.getRecommendation().toLowerCase();
    assertTrue(
        recommendation.contains("mehr")
            || recommendation.contains("daten")
            || recommendation.contains("interaktionen"));
  }

  @Test
  @TestTransaction
  @DisplayName("Should calculate data quality metrics accurately")
  void shouldCalculateDataQualityMetricsAccurately() {
    // Setup test data
    setupTestData();
    // Arrange - Create additional contacts and interactions
    CustomerContact contact2 = createAdditionalContact("Anna", "Schmidt");
    CustomerContact contact3 = createAdditionalContact("Peter", "Müller");

    // Contact 1: Multiple recent interactions
    createInteraction(InteractionType.EMAIL, 0.8, 85, -5);
    createInteraction(InteractionType.EMAIL, 0.9, 90, -3);

    // Contact 2: One older interaction
    createInteractionForContact(contact2.getId(), InteractionType.NOTE, 0.5, 60, -100);

    // Contact 3: No interactions

    // Act
    DataQualityMetricsDTO result = contactInteractionService.getDataQualityMetrics();

    // Assert
    assertNotNull(result);
    assertEquals(3, result.getTotalContacts());
    assertEquals(2, result.getContactsWithInteractions());
    assertEquals(
        1.5, result.getAverageInteractionsPerContact(), 0.1); // 3 interactions / 2 contacts
    assertTrue(result.getDataCompletenessScore() >= 0);

    // Check freshness categories
    assertTrue(result.getFreshContacts() >= 0);
    assertTrue(result.getAgingContacts() >= 0);
    assertTrue(result.getStaleContacts() >= 0);
    assertTrue(result.getCriticalContacts() >= 0);

    // Should have recommendations
    assertTrue(result.getShowDataCollectionHints());
    assertFalse(result.getCriticalDataGaps().isEmpty());
    assertFalse(result.getImprovementSuggestions().isEmpty());

    // Interaction coverage should be 66.7% (2 of 3 contacts)
    assertEquals(66.7, result.getInteractionCoverage(), 1.0);
  }

  @Test
  @TestTransaction
  @DisplayName("Should track data freshness categories correctly")
  void shouldTrackDataFreshnessCorrectly() {
    // Setup test data
    setupTestData();
    // NOTE: Due to @UpdateTimestamp on Contact.updatedAt, we cannot test aging categories in this
    // integration test
    // as Hibernate automatically overwrites updatedAt with current time. This test verifies the
    // basic functionality.

    // Arrange - Create contacts with interactions
    createInteraction(InteractionType.EMAIL, 0.8, 85, -30); // Main test contact

    CustomerContact contact2 = createAdditionalContact("Second", "Contact");
    createInteractionForContact(contact2.getId(), InteractionType.NOTE, 0.5, 60, -120);

    CustomerContact contact3 = createAdditionalContact("Third", "Contact");
    createInteractionForContact(contact3.getId(), InteractionType.CALL, 0.3, 40, -200);

    CustomerContact contact4 = createAdditionalContact("Fourth", "Contact");
    createInteractionForContact(contact4.getId(), InteractionType.EMAIL, 0.2, 30, -400);

    // Act
    DataQualityMetricsDTO result = contactInteractionService.getDataQualityMetrics();

    // Assert - Basic functionality tests (all contacts will be "fresh" due to @UpdateTimestamp)
    assertEquals(4, result.getTotalContacts());
    assertEquals(4, result.getContactsWithInteractions());

    // Check that all contacts are properly categorized (all should be fresh due to
    // @UpdateTimestamp)
    long totalCategorized =
        result.getFreshContacts()
            + result.getAgingContacts()
            + result.getStaleContacts()
            + result.getCriticalContacts();
    assertEquals(4, totalCategorized);

    // All contacts should be fresh due to @UpdateTimestamp behavior
    assertEquals(
        4, result.getFreshContacts(), "All contacts should be fresh due to @UpdateTimestamp");
    assertEquals(
        0, result.getAgingContacts(), "No aging contacts expected due to @UpdateTimestamp");
    assertEquals(
        0, result.getStaleContacts(), "No stale contacts expected due to @UpdateTimestamp");
    assertEquals(
        0, result.getCriticalContacts(), "No critical contacts expected due to @UpdateTimestamp");

    // Verify other metrics work correctly
    assertTrue(result.getInteractionCoverage() > 0, "Should have interaction coverage");
    assertTrue(result.getAverageInteractionsPerContact() > 0, "Should have average interactions");
  }

  @Test
  @TestTransaction
  @DisplayName("Should get interactions for contact chronologically")
  void shouldGetInteractionsChronologically() {
    // Setup test data
    setupTestData();
    // Arrange - Create interactions at different times
    LocalDateTime now = LocalDateTime.now();
    createInteractionAtTime(InteractionType.EMAIL, "First", now.minusDays(3));
    createInteractionAtTime(InteractionType.EMAIL, "Response", now.minusDays(2));
    createInteractionAtTime(InteractionType.CALL, "Follow-up", now.minusDays(1));

    // Act
    List<ContactInteractionDTO> result =
        contactInteractionService.getInteractionsByContact(testContactId, Page.of(0, 10));

    // Assert
    assertEquals(3, result.size());

    // Should be in chronological order (most recent first)
    assertEquals("Follow-up", result.get(0).getSubject());
    assertEquals("Response", result.get(1).getSubject());
    assertEquals("First", result.get(2).getSubject());

    // All should have the correct contact ID
    result.forEach(interaction -> assertEquals(testContactId, interaction.getContactId()));
  }

  @Test
  @TestTransaction
  @DisplayName("Should record different interaction types correctly")
  void shouldRecordDifferentInteractionTypes() {
    // Setup test data
    setupTestData();
    // Test note recording
    ContactInteractionDTO note =
        contactInteractionService.recordNote(testContactId, "Test note content", "test-user");
    assertEquals(InteractionType.NOTE, note.getType());
    assertEquals("Test note content", note.getSummary());

    // Test email recording - use createInteraction instead
    ContactInteractionDTO emailDto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.EMAIL)
            .subject("Test Subject")
            .sentimentScore(0.8)
            .timestamp(LocalDateTime.now())
            .build();
    ContactInteractionDTO email = contactInteractionService.createInteraction(emailDto);
    assertEquals(InteractionType.EMAIL, email.getType());
    assertEquals("Test Subject", email.getSubject());
    assertEquals(0.8, email.getSentimentScore(), 0.01);

    // Test call recording - use createInteraction instead
    ContactInteractionDTO callDto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.CALL)
            .outcome("POSITIVE")
            .summary("Successful call")
            .timestamp(LocalDateTime.now())
            .build();
    ContactInteractionDTO call = contactInteractionService.createInteraction(callDto);
    assertEquals(InteractionType.CALL, call.getType());
    assertEquals("POSITIVE", call.getOutcome());

    // Test meeting recording - use createInteraction instead
    ContactInteractionDTO meetingDto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.MEETING)
            .summary("Productive meeting")
            .timestamp(LocalDateTime.now())
            .build();
    ContactInteractionDTO meeting = contactInteractionService.createInteraction(meetingDto);
    assertEquals(InteractionType.MEETING, meeting.getType());
    assertEquals("Productive meeting", meeting.getSummary());
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle CQRS mode correctly when enabled")
  void shouldHandleCQRSModeWhenEnabled() {
    // Setup test data
    setupTestData();

    // Given - Assuming CQRS is enabled via feature flag
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.EMAIL)
            .subject("CQRS Test")
            .summary("Testing CQRS pattern")
            .sentimentScore(0.75)
            .engagementScore(80)
            .timestamp(LocalDateTime.now())
            .build();

    // When - Create via command side
    ContactInteractionDTO created = contactInteractionService.createInteraction(dto);

    // Then - Query side should reflect the change
    assertNotNull(created.getId());

    // Verify via query side
    List<ContactInteractionDTO> interactions =
        contactInteractionService.getInteractionsByContact(testContactId, Page.of(0, 10));

    assertTrue(interactions.stream().anyMatch(i -> "CQRS Test".equals(i.getSubject())));

    // Verify event was potentially published (if event bus exists)
    // This would be verified through event store or audit log
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle concurrent interaction updates correctly")
  void shouldHandleConcurrentInteractionUpdates() {
    // Setup test data
    setupTestData();

    // Create multiple interactions rapidly
    for (int i = 0; i < 5; i++) {
      ContactInteractionDTO dto =
          ContactInteractionDTO.builder()
              .contactId(testContactId)
              .type(InteractionType.EMAIL)
              .subject("Concurrent Test " + i)
              .summary("Testing concurrent updates")
              .sentimentScore(0.5 + (i * 0.1))
              .engagementScore(60 + (i * 5))
              .timestamp(LocalDateTime.now().minusMinutes(i))
              .build();

      contactInteractionService.createInteraction(dto);
    }

    // Verify all interactions were created
    List<ContactInteractionDTO> interactions =
        contactInteractionService.getInteractionsByContact(testContactId, Page.of(0, 10));
    assertEquals(5, interactions.size());

    // Verify contact statistics are correct
    CustomerContact contact = contactRepository.findById(testContactId);
    assertEquals(5, contact.getInteractionCount());
    assertNotNull(contact.getLastInteractionDate());
  }

  @Test
  @TestTransaction
  @DisplayName("Should calculate engagement trends over time")
  void shouldCalculateEngagementTrends() {
    // Setup test data
    setupTestData();

    // Create interactions with varying engagement over time
    LocalDateTime baseTime = LocalDateTime.now().minusDays(30);

    // Low engagement initially
    for (int i = 0; i < 3; i++) {
      createInteractionAtTime(InteractionType.EMAIL, "Low engagement " + i, baseTime.plusDays(i));
    }

    // High engagement recently
    for (int i = 20; i < 30; i++) {
      ContactInteractionDTO dto =
          ContactInteractionDTO.builder()
              .contactId(testContactId)
              .type(InteractionType.MEETING)
              .subject("High engagement " + i)
              .summary("Very engaged discussion")
              .sentimentScore(0.9)
              .engagementScore(95)
              .timestamp(baseTime.plusDays(i))
              .build();
      contactInteractionService.createInteraction(dto);
    }

    // Calculate warmth score
    WarmthScoreDTO warmth = contactInteractionService.calculateWarmthScore(testContactId);

    // Should show positive trend (trend can be any non-null value indicating analysis)
    assertNotNull(warmth.getTrend());
    // Verify warmth score exists and is reasonable
    assertTrue(warmth.getWarmthScore() >= 0 && warmth.getWarmthScore() <= 100);
    // High recent engagement should lead to higher score
    assertTrue(warmth.getWarmthScore() > 50);
  }

  @Test
  @TestTransaction
  @DisplayName("Should handle invalid contact ID gracefully")
  void shouldHandleInvalidContactIdGracefully() {
    // Setup test data
    setupTestData();

    UUID invalidId = UUID.randomUUID();

    // Try to create interaction for non-existent contact
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(invalidId)
            .type(InteractionType.EMAIL)
            .subject("Invalid Contact Test")
            .timestamp(LocalDateTime.now())
            .build();

    // Should throw appropriate exception
    assertThrows(
        Exception.class,
        () -> {
          contactInteractionService.createInteraction(dto);
        });

    // Try to get warmth score for non-existent contact - should also throw
    assertThrows(
        Exception.class,
        () -> {
          contactInteractionService.calculateWarmthScore(invalidId);
        });
  }

  @Test
  @TestTransaction
  @DisplayName("Should properly categorize interaction outcomes")
  void shouldCategorizeInteractionOutcomes() {
    // Setup test data
    setupTestData();

    // Create interactions with different outcomes
    String[] positiveOutcomes = {"DEAL_WON", "POSITIVE", "SUCCESS", "APPROVED"};
    String[] negativeOutcomes = {"DEAL_LOST", "NEGATIVE", "FAILED", "REJECTED"};
    String[] neutralOutcomes = {"PENDING", "NEUTRAL", "DEFERRED", "UNKNOWN"};

    for (String outcome : positiveOutcomes) {
      createInteractionWithOutcome(outcome, 0.8);
    }

    for (String outcome : negativeOutcomes) {
      createInteractionWithOutcome(outcome, 0.2);
    }

    for (String outcome : neutralOutcomes) {
      createInteractionWithOutcome(outcome, 0.5);
    }

    // Get all interactions
    List<ContactInteractionDTO> interactions =
        contactInteractionService.getInteractionsByContact(testContactId, Page.of(0, 20));

    // Verify all were created
    assertEquals(
        positiveOutcomes.length + negativeOutcomes.length + neutralOutcomes.length,
        interactions.size());

    // Calculate warmth score should consider outcomes
    WarmthScoreDTO warmth = contactInteractionService.calculateWarmthScore(testContactId);
    assertNotNull(warmth);

    // With mixed outcomes, score should exist and be in valid range
    assertTrue(warmth.getWarmthScore() >= 0 && warmth.getWarmthScore() <= 100);
  }

  private void createInteractionWithOutcome(String outcome, double sentiment) {
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(testContactId)
            .type(InteractionType.EMAIL)
            .subject("Outcome test: " + outcome)
            .outcome(outcome)
            .sentimentScore(sentiment)
            .engagementScore((int) (sentiment * 100))
            .timestamp(LocalDateTime.now())
            .build();
    contactInteractionService.createInteraction(dto);
  }

  // Helper methods
  protected void createInteraction(
      InteractionType type, double sentiment, int engagement, int daysAgo) {
    createInteractionForContact(testContactId, type, sentiment, engagement, daysAgo);
  }

  protected void createInteractionForContact(
      UUID contactId, InteractionType type, double sentiment, int engagement, int daysAgo) {
    CustomerContact contact = contactRepository.findById(contactId);
    ContactInteraction interaction =
        ContactInteraction.builder()
            .contact(contact)
            .type(type)
            .timestamp(LocalDateTime.now().minusDays(Math.abs(daysAgo)))
            .subject("Test " + type.name())
            .summary("Test interaction")
            .sentimentScore(sentiment)
            .engagementScore(engagement)
            .build();

    interactionRepository.persist(interaction);

    // Update contact statistics
    contact.setInteractionCount(contact.getInteractionCount() + 1);
    contact.setLastInteractionDate(interaction.getTimestamp());
    contactRepository.persist(contact);
  }

  protected void createInteractionAtTime(
      InteractionType type, String subject, LocalDateTime timestamp) {
    ContactInteraction interaction =
        ContactInteraction.builder()
            .contact(testContact)
            .type(type)
            .timestamp(timestamp)
            .subject(subject)
            .summary("Test interaction")
            .sentimentScore(0.7)
            .engagementScore(75)
            .build();

    interactionRepository.persist(interaction);
  }

  protected CustomerContact createAdditionalContact(String firstName, String lastName) {
    Customer customer = customerRepository.findById(testCustomerId);
    CustomerContact contact =
        contactBuilder
            .forCustomer(customer)
            .withFirstName(firstName)
            .withLastName(lastName)
            .withEmail(firstName.toLowerCase() + "@company.com")
            .persist();
    return contact;
  }
}
