package de.freshplan.domain.customer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.Contact;
import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.dto.DataQualityMetricsDTO;
import de.freshplan.domain.customer.service.dto.WarmthScoreDTO;
import de.freshplan.domain.customer.service.mapper.ContactInteractionMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
class ContactInteractionServiceTest {

  @Inject ContactInteractionService service;

  @InjectMock ContactInteractionRepository interactionRepository;

  @InjectMock ContactRepository contactRepository;

  @InjectMock ContactInteractionMapper mapper;

  private Contact testContact;
  private ContactInteraction testInteraction;
  private ContactInteractionDTO testInteractionDTO;

  @BeforeEach
  void setUp() {
    // Setup test data
    testContact = new Contact();
    testContact.setId(UUID.randomUUID());
    testContact.setFirstName("Max");
    testContact.setLastName("Mustermann");

    testInteraction =
        ContactInteraction.builder()
            .contact(testContact)
            .type(InteractionType.EMAIL)
            .timestamp(LocalDateTime.now())
            .sentimentScore(0.5)
            .engagementScore(75)
            .initiatedBy("SALES")
            .build();
    testInteraction.setId(UUID.randomUUID());

    testInteractionDTO =
        ContactInteractionDTO.builder()
            .contactId(testContact.getId())
            .type(InteractionType.EMAIL)
            .timestamp(LocalDateTime.now())
            .sentimentScore(0.5)
            .engagementScore(75)
            .initiatedBy("SALES")
            .build();
  }

  @Test
  void createInteraction_shouldCreateAndUpdateContactMetrics() {
    // Given
    when(contactRepository.findById(testContact.getId())).thenReturn(testContact);
    when(mapper.toEntity(any())).thenReturn(testInteraction);
    when(mapper.toDTO(any())).thenReturn(testInteractionDTO);
    when(interactionRepository.findLastUpdateDate(any())).thenReturn(LocalDateTime.now());
    when(interactionRepository.find("contact", testContact))
        .thenReturn(new MockPanacheQuery(Arrays.asList(testInteraction)));

    // When
    ContactInteractionDTO result = service.createInteraction(testInteractionDTO);

    // Then
    assertNotNull(result);
    verify(interactionRepository).persist(any(ContactInteraction.class));
    verify(contactRepository).persist(testContact);
    assertEquals(1, testContact.getInteractionCount());
    assertNotNull(testContact.getLastInteractionDate());
  }

  @Test
  void createInteraction_shouldCalculateWordCount_whenContentProvided() {
    // Given
    testInteractionDTO.setFullContent("This is a test message with eight words");
    testInteraction.setFullContent("This is a test message with eight words");

    when(contactRepository.findById(testContact.getId())).thenReturn(testContact);
    when(mapper.toEntity(any())).thenReturn(testInteraction);
    when(mapper.toDTO(any())).thenReturn(testInteractionDTO);
    when(interactionRepository.findLastUpdateDate(any())).thenReturn(LocalDateTime.now());
    when(interactionRepository.find("contact", testContact))
        .thenReturn(new MockPanacheQuery(Arrays.asList(testInteraction)));

    // When
    service.createInteraction(testInteractionDTO);

    // Then
    ArgumentCaptor<ContactInteraction> captor = ArgumentCaptor.forClass(ContactInteraction.class);
    verify(interactionRepository).persist(captor.capture());
    assertEquals(8, captor.getValue().getWordCount());
  }

  @Test
  void createInteraction_shouldThrowException_whenContactNotFound() {
    // Given
    when(contactRepository.findById(testContact.getId())).thenReturn(null);

    // When/Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          service.createInteraction(testInteractionDTO);
        });
  }

  @Test
  void calculateWarmthScore_shouldReturnDefaultValues_whenNoInteractions() {
    // Given
    when(contactRepository.findById(testContact.getId())).thenReturn(testContact);
    when(interactionRepository.findRecentInteractions(testContact, 90)).thenReturn(Arrays.asList());

    // When
    WarmthScoreDTO result = service.calculateWarmthScore(testContact.getId());

    // Then
    assertNotNull(result);
    assertEquals(50, result.getWarmthScore()); // Default neutral
    assertEquals(0, result.getConfidence()); // No confidence
    assertEquals(0, result.getDataPoints());
  }

  @Test
  void calculateWarmthScore_shouldCalculateBasedOnInteractions() {
    // Given
    ContactInteraction interaction1 = createInteraction(0.8, 90, "CUSTOMER");
    ContactInteraction interaction2 = createInteraction(0.6, 70, "SALES");
    ContactInteraction interaction3 = createInteraction(0.7, 80, "CUSTOMER");

    when(contactRepository.findById(testContact.getId())).thenReturn(testContact);
    when(interactionRepository.findRecentInteractions(testContact, 90))
        .thenReturn(Arrays.asList(interaction1, interaction2, interaction3));
    when(interactionRepository.calculateResponseRate(testContact)).thenReturn(100.0);

    // When
    WarmthScoreDTO result = service.calculateWarmthScore(testContact.getId());

    // Then
    assertNotNull(result);
    assertTrue(result.getWarmthScore() > 50); // Should be warm
    assertEquals(30, result.getConfidence()); // 3 interactions = 30% confidence
    assertEquals(3, result.getDataPoints());
    verify(contactRepository).persist(testContact);
  }

  @Test
  void getDataQualityMetrics_shouldReturnMetrics() {
    // Given
    when(contactRepository.count()).thenReturn(100L);
    when(contactRepository.count("id in (select distinct i.contact.id from ContactInteraction i)"))
        .thenReturn(75L);
    when(interactionRepository.count()).thenReturn(300L);
    when(contactRepository.count("warmthScore is not null")).thenReturn(50L);
    when(contactRepository.count(
            "email is not null and (phone is not null or mobile is not null) and position is not null"))
        .thenReturn(60L);

    // When
    DataQualityMetricsDTO result = service.getDataQualityMetrics();

    // Then
    assertNotNull(result);
    assertEquals(100L, result.getTotalContacts());
    assertEquals(75L, result.getContactsWithInteractions());
    assertEquals(4.0, result.getAverageInteractionsPerContact(), 0.01);
    assertEquals(60.0, result.getDataCompletenessScore(), 0.01);
    assertEquals(50L, result.getContactsWithWarmthScore());
  }

  @Test
  void recordNote_shouldCreateNoteInteraction() {
    // Given
    UUID contactId = testContact.getId();
    String note = "Important customer feedback";
    String userId = "user123";

    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(mapper.toEntity(any())).thenReturn(testInteraction);
    when(mapper.toDTO(any())).thenReturn(testInteractionDTO);
    when(interactionRepository.findLastUpdateDate(any())).thenReturn(LocalDateTime.now());
    when(interactionRepository.find("contact", testContact))
        .thenReturn(new MockPanacheQuery(Arrays.asList(testInteraction)));

    // When
    ContactInteractionDTO result = service.recordNote(contactId, note, userId);

    // Then
    assertNotNull(result);
    ArgumentCaptor<ContactInteractionDTO> captor =
        ArgumentCaptor.forClass(ContactInteractionDTO.class);
    verify(mapper).toEntity(captor.capture());
    ContactInteractionDTO captured = captor.getValue();
    assertEquals(InteractionType.NOTE, captured.getType());
    assertEquals(note, captured.getSummary());
    assertEquals(note, captured.getFullContent());
    assertEquals("SALES", captured.getInitiatedBy());
    assertEquals(userId, captured.getCreatedBy());
  }

  // Helper methods

  private ContactInteraction createInteraction(
      Double sentiment, Integer engagement, String initiatedBy) {
    return ContactInteraction.builder()
        .contact(testContact)
        .type(InteractionType.EMAIL)
        .timestamp(LocalDateTime.now().minusDays(10))
        .sentimentScore(sentiment)
        .engagementScore(engagement)
        .initiatedBy(initiatedBy)
        .build();
  }

  // Mock PanacheQuery implementation for testing
  private static class MockPanacheQuery
      implements io.quarkus.hibernate.orm.panache.PanacheQuery<ContactInteraction> {
    private final java.util.List<ContactInteraction> results;

    MockPanacheQuery(java.util.List<ContactInteraction> results) {
      this.results = results;
    }

    @Override
    public java.util.List<ContactInteraction> list() {
      return results;
    }

    // Implement other required methods with default behavior
    @Override
    public <T> MockPanacheQuery page(io.quarkus.panache.common.Page page) {
      return this;
    }

    @Override
    public <T> MockPanacheQuery page(int pageIndex, int pageSize) {
      return this;
    }

    @Override
    public <T> MockPanacheQuery nextPage() {
      return this;
    }

    @Override
    public <T> MockPanacheQuery previousPage() {
      return this;
    }

    @Override
    public <T> MockPanacheQuery firstPage() {
      return this;
    }

    @Override
    public <T> MockPanacheQuery lastPage() {
      return this;
    }

    @Override
    public boolean hasNextPage() {
      return false;
    }

    @Override
    public boolean hasPreviousPage() {
      return false;
    }

    @Override
    public int pageCount() {
      return 1;
    }

    @Override
    public io.quarkus.panache.common.Page page() {
      return null;
    }

    @Override
    public <T> MockPanacheQuery range(int startIndex, int lastIndex) {
      return this;
    }

    @Override
    public <T> MockPanacheQuery withCollation(String collation) {
      return this;
    }

    @Override
    public <T> MockPanacheQuery withLock(jakarta.persistence.LockModeType lockModeType) {
      return this;
    }

    @Override
    public <T> MockPanacheQuery withHint(String hintName, Object value) {
      return this;
    }

    @Override
    public <T> MockPanacheQuery filter(
        String filterName, io.quarkus.panache.common.Parameters params) {
      return this;
    }

    @Override
    public <T> MockPanacheQuery filter(String filterName, java.util.Map<String, Object> params) {
      return this;
    }

    @Override
    public <T> MockPanacheQuery filter(String filterName) {
      return this;
    }

    @Override
    public <T> MockPanacheQuery project(Class<T> type) {
      return this;
    }

    @Override
    public <T> java.util.stream.Stream<T> stream() {
      return (java.util.stream.Stream<T>) results.stream();
    }

    @Override
    public ContactInteraction firstResult() {
      return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public <T> T firstResultOptional() {
      return (T) java.util.Optional.ofNullable(firstResult());
    }

    @Override
    public ContactInteraction singleResult() {
      return results.size() == 1 ? results.get(0) : null;
    }

    @Override
    public <T> T singleResultOptional() {
      return (T) java.util.Optional.ofNullable(singleResult());
    }

    @Override
    public long count() {
      return results.size();
    }
  }
}
