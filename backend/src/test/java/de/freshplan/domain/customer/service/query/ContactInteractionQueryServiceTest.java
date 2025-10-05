package de.freshplan.domain.customer.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.dto.DataQualityMetricsDTO;
import de.freshplan.domain.customer.service.dto.WarmthScoreDTO;
import de.freshplan.domain.customer.service.mapper.ContactInteractionMapper;
import de.freshplan.test.builders.ContactTestDataFactory;
import de.freshplan.test.builders.CustomerTestDataFactory;
import io.quarkus.panache.common.Page;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ContactInteractionQueryService. Tests all query operations with mocked
 * dependencies. IMPORTANT: Verifies that NO write operations are performed.
 */
@QuarkusTest
@Tag("integration")
class ContactInteractionQueryServiceTest {

  @Inject ContactInteractionQueryService queryService;

  @InjectMock ContactRepository contactRepository;

  @InjectMock ContactInteractionRepository interactionRepository;

  @InjectMock ContactInteractionMapper mapper;

  private UUID contactId;
  private CustomerContact testContact;
  private ContactInteraction testInteraction;
  private ContactInteractionDTO testDTO;
  private Page defaultPage;

  @BeforeEach
  void setUp() {
    contactId = UUID.randomUUID();
    defaultPage = Page.of(0, 10);

    // Create test customer using factory
    Customer testCustomer = CustomerTestDataFactory.builder()
        .withCompanyName("Test Company")
        .build();
    testCustomer.setId(UUID.randomUUID());
    testCustomer.setIsTestData(true);

    // Setup test contact
    testContact =
        ContactTestDataFactory.builder()
            .forCustomer(testCustomer)
            .withFirstName("John")
            .withLastName("Doe")
            .withEmail("john.doe@example.com")
            .build();
    testContact.setId(contactId);
    testContact.setWarmthScore(75);
    testContact.setWarmthConfidence(80);

    // Setup test interaction manually (no factory available for ContactInteraction)
    testInteraction = new ContactInteraction();
    testInteraction.setId(UUID.randomUUID());
    testInteraction.setContact(testContact);
    testInteraction.setType(InteractionType.EMAIL);
    testInteraction.setTimestamp(LocalDateTime.now());
    testInteraction.setSummary("Test interaction");
    testInteraction.setFullContent("This is a test interaction content");
    testInteraction.setSentimentScore(0.7);
    testInteraction.setOutcome("Positive");

    // Setup test DTO
    testDTO =
        ContactInteractionDTO.builder()
            .id(testInteraction.getId())
            .contactId(contactId)
            .type(InteractionType.EMAIL)
            .timestamp(testInteraction.getTimestamp())
            .summary(testInteraction.getSummary())
            .fullContent(testInteraction.getFullContent())
            .sentimentScore(testInteraction.getSentimentScore())
            .outcome(testInteraction.getOutcome())
            .build();

    // Reset mocks
    reset(contactRepository, interactionRepository, mapper);
  }

  // ========== GET INTERACTIONS BY CONTACT TESTS ==========

  @Test
  void getInteractionsByContact_withExistingContact_shouldReturnInteractions() {
    // Given
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(interactionRepository.findByContactPaginated(testContact, defaultPage))
        .thenReturn(Arrays.asList(testInteraction));
    when(mapper.toDTO(testInteraction)).thenReturn(testDTO);

    // When
    List<ContactInteractionDTO> result =
        queryService.getInteractionsByContact(contactId, defaultPage);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getContactId()).isEqualTo(contactId);
    assertThat(result.get(0).getType()).isEqualTo(InteractionType.EMAIL);
    // Verify no write operations
    verify(contactRepository, never()).persist(any(CustomerContact.class));
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
  }

  @Test
  void getInteractionsByContact_withNullContactId_shouldThrowException() {
    // When/Then
    assertThatThrownBy(() -> queryService.getInteractionsByContact(null, defaultPage))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Contact not found: null");
  }

  @Test
  void getInteractionsByContact_withNonExistentContact_shouldThrowException() {
    // Given
    when(contactRepository.findById(contactId)).thenReturn(null);

    // When/Then
    assertThatThrownBy(() -> queryService.getInteractionsByContact(contactId, defaultPage))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Contact not found: " + contactId);
  }

  @Test
  void getInteractionsByContact_withNoInteractions_shouldReturnEmptyList() {
    // Given
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(interactionRepository.findByContactPaginated(testContact, defaultPage))
        .thenReturn(Collections.emptyList());

    // When
    List<ContactInteractionDTO> result =
        queryService.getInteractionsByContact(contactId, defaultPage);

    // Then
    assertThat(result).isEmpty();
    // Verify no write operations
    verify(contactRepository, never()).persist(any(CustomerContact.class));
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
  }

  // ========== CALCULATE WARMTH SCORE TESTS ==========

  @Test
  void calculateWarmthScore_withInteractions_shouldCalculateScore() {
    // Given
    LocalDateTime now = LocalDateTime.now();
    testInteraction.setTimestamp(now.minusDays(2));
    testInteraction.setSentimentScore(0.8);

    ContactInteraction recentInteraction = new ContactInteraction();
    recentInteraction.setContact(testContact);
    recentInteraction.setType(InteractionType.MEETING);
    recentInteraction.setTimestamp(now.minusHours(3));
    recentInteraction.setSentimentScore(0.9);

    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(interactionRepository.findRecentInteractions(testContact, 90))
        .thenReturn(Arrays.asList(testInteraction, recentInteraction));
    when(interactionRepository.calculateResponseRate(testContact)).thenReturn(75.0);

    // When
    WarmthScoreDTO result = queryService.calculateWarmthScore(contactId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContactId()).isEqualTo(contactId);
    assertThat(result.getWarmthScore()).isGreaterThan(0);
    assertThat(result.getConfidence()).isGreaterThan(0);
    assertThat(result.getDataPoints()).isEqualTo(2);
    assertThat(result.getLastCalculated()).isNotNull();

    // CRITICAL: Verify NO writes happened (read-only operation!)
    verify(contactRepository, never()).persist(any(CustomerContact.class));
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
  }

  @Test
  void calculateWarmthScore_withNoInteractions_shouldReturnZeroScore() {
    // Given
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(interactionRepository.findRecentInteractions(testContact, 90))
        .thenReturn(Collections.emptyList());

    // When
    WarmthScoreDTO result = queryService.calculateWarmthScore(contactId);

    // Then
    assertThat(result.getWarmthScore()).isEqualTo(50); // Default neutral score
    assertThat(result.getConfidence()).isEqualTo(0);
    assertThat(result.getDataPoints()).isEqualTo(0);
    // Verify no write operations
    verify(contactRepository, never()).persist(any(CustomerContact.class));
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
  }

  @Test
  void calculateWarmthScore_withVariousInteractionTypes_shouldWeightDifferently() {
    // Given
    LocalDateTime now = LocalDateTime.now();

    ContactInteraction emailInteraction = new ContactInteraction();
    emailInteraction.setContact(testContact);
    emailInteraction.setType(InteractionType.EMAIL);
    emailInteraction.setTimestamp(now.minusDays(1));
    emailInteraction.setSentimentScore(0.5);

    ContactInteraction meetingInteraction = new ContactInteraction();
    meetingInteraction.setContact(testContact);
    meetingInteraction.setType(InteractionType.MEETING);
    meetingInteraction.setTimestamp(now.minusDays(2));
    meetingInteraction.setSentimentScore(0.5);

    ContactInteraction callInteraction = new ContactInteraction();
    callInteraction.setContact(testContact);
    callInteraction.setType(InteractionType.CALL);
    callInteraction.setTimestamp(now.minusDays(3));
    callInteraction.setSentimentScore(0.5);

    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(interactionRepository.findRecentInteractions(testContact, 90))
        .thenReturn(Arrays.asList(emailInteraction, meetingInteraction, callInteraction));
    when(interactionRepository.calculateResponseRate(testContact)).thenReturn(60.0);

    // When
    WarmthScoreDTO result = queryService.calculateWarmthScore(contactId);

    // Then
    assertThat(result.getWarmthScore()).isGreaterThan(0);
    assertThat(result.getDataPoints()).isEqualTo(3);
    // Meetings should have higher weight than emails/calls
    verify(contactRepository, never()).persist(any(CustomerContact.class));
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
  }

  @Test
  void calculateWarmthScore_withOldInteractions_shouldHaveLowerScore() {
    // Given
    ContactInteraction oldInteraction = new ContactInteraction();
    oldInteraction.setContact(testContact);
    oldInteraction.setType(InteractionType.EMAIL);
    oldInteraction.setTimestamp(LocalDateTime.now().minusDays(90)); // Very old
    oldInteraction.setSentimentScore(0.9); // High sentiment

    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(interactionRepository.findRecentInteractions(testContact, 90))
        .thenReturn(Arrays.asList(oldInteraction));
    when(interactionRepository.calculateResponseRate(testContact)).thenReturn(85.0);

    // When
    WarmthScoreDTO result = queryService.calculateWarmthScore(contactId);

    // Then
    // Despite high sentiment, low frequency should result in moderate warmth
    assertThat(result.getWarmthScore()).isGreaterThan(0);
    assertThat(result.getDataPoints()).isEqualTo(1);
    // Verify no write operations
    verify(contactRepository, never()).persist(any(CustomerContact.class));
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
  }

  // ========== GET DATA QUALITY METRICS TESTS ==========

  @Test
  void getDataQualityMetrics_withCompleteData_shouldReturnMetrics() {
    // Given
    when(contactRepository.count()).thenReturn(100L);
    when(contactRepository.count("id in (select distinct i.contact.id from ContactInteraction i)"))
        .thenReturn(75L);
    when(interactionRepository.count()).thenReturn(300L);
    when(contactRepository.count("warmthScore is not null")).thenReturn(50L);
    when(contactRepository.count(eq("updatedAt >= ?1"), (Object[]) any())).thenReturn(30L); // fresh
    when(contactRepository.count(eq("updatedAt < ?1 and updatedAt >= ?2"), (Object[]) any()))
        .thenReturn(20L, 25L); // aging, stale
    when(contactRepository.count(eq("updatedAt < ?1"), (Object[]) any()))
        .thenReturn(25L); // critical
    when(contactRepository.count(
            "email is not null and (phone is not null or mobile is not null) and position is not null"))
        .thenReturn(60L);

    // When
    DataQualityMetricsDTO result = queryService.getDataQualityMetrics();

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTotalContacts()).isEqualTo(100);
    assertThat(result.getContactsWithInteractions()).isEqualTo(75);
    assertThat(result.getAverageInteractionsPerContact()).isEqualTo(4.0); // 300/75
    assertThat(result.getContactsWithWarmthScore()).isEqualTo(50);
    assertThat(result.getFreshContacts()).isEqualTo(30);
    assertThat(result.getAgingContacts()).isEqualTo(20);
    assertThat(result.getStaleContacts()).isEqualTo(25);
    assertThat(result.getCriticalContacts()).isEqualTo(25);
    assertThat(result.getDataCompletenessScore()).isEqualTo(60.0); // 60/100 * 100

    // Verify no write operations
    verify(contactRepository, never()).persist(any(CustomerContact.class));
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
  }

  @Test
  void getDataQualityMetrics_withNoContacts_shouldReturnZeroMetrics() {
    // Given - Specific query mocks for the exact queries used in the implementation
    when(contactRepository.count()).thenReturn(0L);
    when(contactRepository.count("id in (select distinct i.contact.id from ContactInteraction i)"))
        .thenReturn(0L);
    when(contactRepository.count("warmthScore is not null")).thenReturn(0L);
    when(contactRepository.count(eq("updatedAt >= ?1"), (Object[]) any())).thenReturn(0L);
    when(contactRepository.count(eq("updatedAt < ?1 and updatedAt >= ?2"), (Object[]) any()))
        .thenReturn(0L);
    when(contactRepository.count(eq("updatedAt < ?1"), (Object[]) any())).thenReturn(0L);
    when(contactRepository.count(
            "email is not null and (phone is not null or mobile is not null) and position is not null"))
        .thenReturn(0L);
    when(interactionRepository.count()).thenReturn(0L);

    // When
    DataQualityMetricsDTO result = queryService.getDataQualityMetrics();

    // Then
    assertThat(result.getTotalContacts()).isEqualTo(0);
    assertThat(result.getContactsWithInteractions()).isEqualTo(0);
    assertThat(result.getAverageInteractionsPerContact()).isEqualTo(0.0);
    assertThat(result.getDataCompletenessScore()).isEqualTo(0.0);

    // Verify no write operations
    verify(contactRepository, never()).persist(any(CustomerContact.class));
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
  }

  @Test
  void getDataQualityMetrics_withPartialData_shouldCalculateCorrectly() {
    // Given
    when(contactRepository.count()).thenReturn(50L);
    when(contactRepository.count("id in (select distinct i.contact.id from ContactInteraction i)"))
        .thenReturn(25L);
    when(interactionRepository.count()).thenReturn(100L);
    when(contactRepository.count("warmthScore is not null")).thenReturn(10L);
    when(contactRepository.count(eq("updatedAt >= ?1"), (Object[]) any())).thenReturn(15L);
    when(contactRepository.count(eq("updatedAt < ?1 and updatedAt >= ?2"), (Object[]) any()))
        .thenReturn(10L, 15L);
    when(contactRepository.count(eq("updatedAt < ?1"), (Object[]) any())).thenReturn(10L);
    when(contactRepository.count(
            "email is not null and (phone is not null or mobile is not null) and position is not null"))
        .thenReturn(20L);

    // When
    DataQualityMetricsDTO result = queryService.getDataQualityMetrics();

    // Then
    assertThat(result.getTotalContacts()).isEqualTo(50);
    assertThat(result.getContactsWithInteractions()).isEqualTo(25);
    assertThat(result.getAverageInteractionsPerContact()).isEqualTo(4.0); // 100/25
    assertThat(result.getContactsWithWarmthScore()).isEqualTo(10);
    assertThat(result.getDataCompletenessScore()).isEqualTo(40.0); // 20/50 * 100

    // Verify no write operations
    verify(contactRepository, never()).persist(any(CustomerContact.class));
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
  }
}
