package de.freshplan.domain.customer.service;

import static org.junit.jupiter.api.Assertions.*;

import de.freshplan.domain.customer.entity.Contact;
import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.dto.DataQualityMetricsDTO;
import de.freshplan.domain.customer.service.dto.WarmthScoreDTO;
import io.quarkus.panache.common.Page;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
@DisplayName("Contact Interaction Service Integration Tests")
class ContactInteractionServiceIT {

  @Inject ContactInteractionService contactInteractionService;

  @Inject CustomerRepository customerRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionRepository interactionRepository;

  private UUID testCustomerId;
  private UUID testContactId;
  private Contact testContact;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean slate for each test
    interactionRepository.deleteAll();
    contactRepository.deleteAll();
    customerRepository.deleteAll();

    // Create test customer
    Customer testCustomer = new Customer();
    testCustomer.setCompanyName("Test Company GmbH");
    // Customer has no email field anymore
    customerRepository.persist(testCustomer);
    testCustomerId = testCustomer.getId();

    // Create test contact
    testContact = new Contact();
    testContact.setFirstName("Max");
    testContact.setLastName("Mustermann");
    testContact.setEmail("max@company.com");
    testContact.setCustomer(testCustomer);
    contactRepository.persist(testContact);
    testContactId = testContact.getId();
  }

  @Test
  @DisplayName("Should create interaction and update contact intelligence data")
  @Transactional
  void shouldCreateInteractionAndUpdateContact() {
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
    assertEquals(testContactId.toString(), result.getContactId());
    assertEquals(InteractionType.EMAIL, result.getType());
    assertEquals("Test Email Subject", result.getSubject());
    assertEquals(0.8, result.getSentimentScore(), 0.01);
    assertEquals(85, result.getEngagementScore());

    // Verify contact was updated
    Contact updatedContact = contactRepository.findById(testContactId);
    assertEquals(1, updatedContact.getInteractionCount());
    assertNotNull(updatedContact.getLastInteractionDate());
  }

  @Test
  @DisplayName("Should calculate warmth score with multiple interaction factors")
  @Transactional
  void shouldCalculateWarmthScoreWithMultipleFactors() {
    // Arrange - Create diverse interactions
    createInteraction(InteractionType.EMAIL, 0.9, 90, -5); // Recent, positive
    createInteraction(InteractionType.EMAIL, 0.8, 85, -10); // Recent response
    createInteraction(InteractionType.CALL, 0.7, 80, -15); // Call
    createInteraction(InteractionType.MEETING, 0.9, 95, -20); // Successful meeting

    // Act
    WarmthScoreDTO result = contactInteractionService.calculateWarmthScore(testContactId);

    // Assert
    assertNotNull(result);
    assertEquals(testContactId.toString(), result.getContactId());
    assertTrue(result.getWarmthScore() >= 0 && result.getWarmthScore() <= 100);
    assertTrue(result.getConfidence() >= 0 && result.getConfidence() <= 100);

    // Check individual factors - not available in current DTO
    // Factors have been removed from WarmthScoreDTO

    assertNotNull(result.getTrend());
    assertNotNull(result.getRecommendation());
    assertNotNull(result.getLastCalculated());
  }

  @Test
  @DisplayName("Should handle low data scenario with appropriate confidence")
  @Transactional
  void shouldHandleLowDataScenario() {
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
  @DisplayName("Should calculate data quality metrics accurately")
  void shouldCalculateDataQualityMetricsAccurately() {
    // Arrange - Create additional contacts and interactions
    Contact contact2 = createAdditionalContact("Anna", "Schmidt");
    Contact contact3 = createAdditionalContact("Peter", "Müller");

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
  @DisplayName("Should track data freshness categories correctly")
  @Transactional
  void shouldTrackDataFreshnessCorrectly() {
    // Arrange - Create interactions with different ages
    createInteraction(InteractionType.EMAIL, 0.8, 85, -30); // Fresh (< 90 days)

    Contact agingContact = createAdditionalContact("Old", "Contact");
    createInteractionForContact(
        agingContact.getId(), InteractionType.NOTE, 0.5, 60, -120); // Aging (90-180 days)

    Contact staleContact = createAdditionalContact("Stale", "Contact");
    createInteractionForContact(
        staleContact.getId(), InteractionType.CALL, 0.3, 40, -200); // Stale (180-365 days)

    Contact criticalContact = createAdditionalContact("Critical", "Contact");
    createInteractionForContact(
        criticalContact.getId(),
        InteractionType.EMAIL,
        0.2,
        30,
        -400); // Critical (> 365 days)

    // Act
    DataQualityMetricsDTO result = contactInteractionService.getDataQualityMetrics();

    // Assert
    assertEquals(4, result.getTotalContacts());
    assertEquals(4, result.getContactsWithInteractions());

    // Check that all contacts are properly categorized
    long totalCategorized =
        result.getFreshContacts()
            + result.getAgingContacts()
            + result.getStaleContacts()
            + result.getCriticalContacts();
    assertEquals(4, totalCategorized);

    // At least one contact should be in each category based on our test data setup
    assertTrue(result.getFreshContacts() >= 1);
    assertTrue(result.getAgingContacts() >= 1);
    assertTrue(result.getStaleContacts() >= 1);
    assertTrue(result.getCriticalContacts() >= 1);
  }

  @Test
  @DisplayName("Should get interactions for contact chronologically")
  @Transactional
  void shouldGetInteractionsChronologically() {
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
    result.forEach(
        interaction -> assertEquals(testContactId.toString(), interaction.getContactId()));
  }

  @Test
  @DisplayName("Should record different interaction types correctly")
  @Transactional
  void shouldRecordDifferentInteractionTypes() {
    // Test note recording
    ContactInteractionDTO note =
        contactInteractionService.recordNote(testContactId, "Test note content", "test-user");
    assertEquals(InteractionType.NOTE, note.getType());
    assertEquals("Test note content", note.getSummary());

    // Test email recording - use createInteraction instead
    ContactInteractionDTO emailDto = ContactInteractionDTO.builder()
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
    ContactInteractionDTO callDto = ContactInteractionDTO.builder()
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
    ContactInteractionDTO meetingDto = ContactInteractionDTO.builder()
        .contactId(testContactId)
        .type(InteractionType.MEETING)
        .summary("Productive meeting")
        .timestamp(LocalDateTime.now())
        .build();
    ContactInteractionDTO meeting = contactInteractionService.createInteraction(meetingDto);
    assertEquals(InteractionType.MEETING, meeting.getType());
    assertEquals("Productive meeting", meeting.getSummary());
  }

  // Helper methods
  @Transactional
  private void createInteraction(
      InteractionType type, double sentiment, int engagement, int daysAgo) {
    createInteractionForContact(testContactId, type, sentiment, engagement, daysAgo);
  }

  @Transactional
  private void createInteractionForContact(
      UUID contactId, InteractionType type, double sentiment, int engagement, int daysAgo) {
    Contact contact = contactRepository.findById(contactId);
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

  @Transactional
  private void createInteractionAtTime(
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

  @Transactional
  protected Contact createAdditionalContact(String firstName, String lastName) {
    Customer customer = customerRepository.findById(testCustomerId);
    Contact contact = new Contact();
    contact.setFirstName(firstName);
    contact.setLastName(lastName);
    contact.setEmail(firstName.toLowerCase() + "@company.com");
    contact.setCustomer(customer);
    contactRepository.persist(contact);
    return contact;
  }
}
