package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.command.ContactInteractionCommandService;
import de.freshplan.domain.customer.service.query.ContactInteractionQueryService;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.dto.DataQualityMetricsDTO;
import de.freshplan.domain.customer.service.dto.WarmthScoreDTO;
import de.freshplan.domain.customer.service.mapper.ContactInteractionMapper;
import io.quarkus.panache.common.Page;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ContactInteractionService with CQRS pattern enabled.
 * Tests the complete flow through the facade to command and query services.
 */
@QuarkusTest
@TestProfile(ContactInteractionServiceCQRSTestProfile.class)
class ContactInteractionServiceCQRSIntegrationTest {

  @Inject
  ContactInteractionService facadeService;

  @InjectMock
  ContactInteractionRepository interactionRepository;

  @InjectMock
  ContactRepository contactRepository;

  @InjectMock
  ContactInteractionMapper mapper;

  private UUID contactId;
  private CustomerContact testContact;
  private ContactInteraction testInteraction;
  private ContactInteractionDTO testDTO;

  @BeforeEach
  void setUp() {
    contactId = UUID.randomUUID();
    
    // Setup test contact
    testContact = new CustomerContact();
    testContact.setId(contactId);
    testContact.setFirstName("John");
    testContact.setLastName("Doe");
    testContact.setEmail("john.doe@example.com");
    
    // Setup test interaction
    testInteraction = new ContactInteraction();
    testInteraction.setId(UUID.randomUUID());
    testInteraction.setContact(testContact);
    testInteraction.setType(InteractionType.EMAIL);
    testInteraction.setTimestamp(LocalDateTime.now());
    testInteraction.setSummary("Test interaction");
    testInteraction.setFullContent("This is a test interaction content");
    testInteraction.setSentimentScore(0.5);
    testInteraction.setEngagementScore(75);
    
    // Setup test DTO
    testDTO = ContactInteractionDTO.builder()
        .contactId(contactId)
        .type(InteractionType.EMAIL)
        .timestamp(LocalDateTime.now())
        .summary("Test interaction")
        .fullContent("This is a test interaction content")
        .initiatedBy("SALES")
        .createdBy("testuser")
        .build();
    
    // Reset mocks
    reset(interactionRepository, contactRepository, mapper);
  }

  // ========== COMMAND FLOW TESTS ==========

  @Test
  void createInteraction_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(mapper.toEntity(testDTO)).thenReturn(testInteraction);
    when(mapper.toDTO(testInteraction)).thenReturn(testDTO);
    when(interactionRepository.findLastUpdateDate(testContact)).thenReturn(LocalDateTime.now());
    
    // Mock the PanacheQuery for interaction count
    @SuppressWarnings("unchecked")
    io.quarkus.hibernate.orm.panache.PanacheQuery<ContactInteraction> mockQuery = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
    when(interactionRepository.find("contact", testContact)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(Arrays.asList(testInteraction));
    
    // When
    ContactInteractionDTO result = facadeService.createInteraction(testDTO);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContactId()).isEqualTo(contactId);
    verify(interactionRepository).persist(testInteraction);
    verify(contactRepository, atLeastOnce()).persist((CustomerContact) testContact); // Metrics update
  }

  @Test
  void recordNote_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    String noteContent = "This is a test note";
    String createdBy = "testuser";
    
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(mapper.toEntity(any())).thenReturn(testInteraction);
    when(mapper.toDTO(any())).thenReturn(testDTO);
    when(interactionRepository.findLastUpdateDate(testContact)).thenReturn(LocalDateTime.now());
    
    // Mock the PanacheQuery for interaction count
    @SuppressWarnings("unchecked")
    io.quarkus.hibernate.orm.panache.PanacheQuery<ContactInteraction> mockQuery = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
    when(interactionRepository.find("contact", testContact)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(Arrays.asList(testInteraction));
    
    // When
    ContactInteractionDTO result = facadeService.recordNote(contactId, noteContent, createdBy);
    
    // Then
    assertThat(result).isNotNull();
    verify(interactionRepository).persist(any(ContactInteraction.class));
  }

  @Test
  void batchImportInteractions_withCQRSEnabled_shouldDelegateToCommandService() {
    // Given
    List<ContactInteractionDTO> dtos = Arrays.asList(testDTO);
    
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(mapper.toEntity(testDTO)).thenReturn(testInteraction);
    
    // When
    ContactInteractionService.BatchImportResult result = 
        facadeService.batchImportInteractions(dtos);
    
    // Then
    assertThat(result.imported).isEqualTo(1);
    assertThat(result.failed).isEqualTo(0);
    verify(interactionRepository).persist(any(List.class));
  }

  // ========== QUERY FLOW TESTS ==========

  @Test
  void getInteractionsByContact_withCQRSEnabled_shouldDelegateToQueryService() {
    // Given
    Page page = Page.of(0, 10);
    List<ContactInteraction> interactions = Arrays.asList(testInteraction);
    
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(interactionRepository.findByContactPaginated(testContact, page)).thenReturn(interactions);
    when(mapper.toDTO(testInteraction)).thenReturn(testDTO);
    
    // When
    List<ContactInteractionDTO> result = facadeService.getInteractionsByContact(contactId, page);
    
    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getContactId()).isEqualTo(contactId);
    verifyNoWriteOperationsForQuery();
  }

  @Test
  void getDataQualityMetrics_withCQRSEnabled_shouldDelegateToQueryService() {
    // Given
    when(contactRepository.count()).thenReturn(100L);
    when(contactRepository.count("id in (select distinct i.contact.id from ContactInteraction i)"))
        .thenReturn(75L);
    when(interactionRepository.count()).thenReturn(300L);
    when(contactRepository.count("warmthScore is not null")).thenReturn(50L);
    when(contactRepository.count(eq("updatedAt >= ?1"), (Object[]) any())).thenReturn(25L);
    when(contactRepository.count(eq("updatedAt < ?1 and updatedAt >= ?2"), (Object[]) any())).thenReturn(20L);
    when(contactRepository.count(eq("updatedAt < ?1"), (Object[]) any())).thenReturn(25L);
    when(contactRepository.count(
        "email is not null and (phone is not null or mobile is not null) and position is not null"))
        .thenReturn(60L);
    
    // When
    DataQualityMetricsDTO result = facadeService.getDataQualityMetrics();
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getTotalContacts()).isEqualTo(100);
    verifyNoWriteOperationsForQuery();
  }

  // ========== MIXED OPERATION TEST (calculateWarmthScore) ==========

  @Test
  void calculateWarmthScore_withCQRSEnabled_shouldSplitBetweenQueryAndCommand() {
    // Given
    List<ContactInteraction> interactions = Arrays.asList(testInteraction);
    
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(interactionRepository.findRecentInteractions(testContact, 90)).thenReturn(interactions);
    when(interactionRepository.calculateResponseRate(testContact)).thenReturn(75.0);
    
    // When
    WarmthScoreDTO result = facadeService.calculateWarmthScore(contactId);
    
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getContactId()).isEqualTo(contactId);
    
    // Verify that the contact was updated (command part)
    verify(contactRepository, times(2)).findById(contactId); // Once for query, once for command
    verify(contactRepository).persist((CustomerContact) testContact); // Update happened
    
    // The warmth score should be set on the contact
    assertThat(testContact.getWarmthScore()).isNotNull();
    assertThat(testContact.getWarmthConfidence()).isNotNull();
  }

  // ========== COMPLETE LIFECYCLE TEST ==========

  @Test
  void completeInteractionLifecycle_withCQRSEnabled_shouldWorkEndToEnd() {
    // This test simulates a complete interaction lifecycle through CQRS
    
    // 1. Create an interaction
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(mapper.toEntity(any())).thenReturn(testInteraction);
    when(mapper.toDTO(any())).thenReturn(testDTO);
    when(interactionRepository.findLastUpdateDate(testContact)).thenReturn(LocalDateTime.now());
    
    // Mock the PanacheQuery for interaction count
    @SuppressWarnings("unchecked")
    io.quarkus.hibernate.orm.panache.PanacheQuery<ContactInteraction> mockQuery = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
    when(interactionRepository.find("contact", testContact)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(Arrays.asList(testInteraction));
    
    ContactInteractionDTO createdInteraction = facadeService.createInteraction(testDTO);
    assertThat(createdInteraction).isNotNull();
    verify(interactionRepository).persist(any(ContactInteraction.class));
    
    // 2. Query interactions
    Page page = Page.of(0, 10);
    when(interactionRepository.findByContactPaginated(testContact, page))
        .thenReturn(Arrays.asList(testInteraction));
    
    List<ContactInteractionDTO> interactions = facadeService.getInteractionsByContact(contactId, page);
    assertThat(interactions).hasSize(1);
    
    // 3. Calculate warmth score (mixed operation)
    when(interactionRepository.findRecentInteractions(testContact, 90))
        .thenReturn(Arrays.asList(testInteraction));
    when(interactionRepository.calculateResponseRate(testContact)).thenReturn(80.0);
    
    WarmthScoreDTO warmthScore = facadeService.calculateWarmthScore(contactId);
    assertThat(warmthScore).isNotNull();
    assertThat(warmthScore.getDataPoints()).isEqualTo(1);
    
    // 4. Check data quality metrics
    when(contactRepository.count()).thenReturn(1L);
    when(contactRepository.count("id in (select distinct i.contact.id from ContactInteraction i)"))
        .thenReturn(1L);
    when(interactionRepository.count()).thenReturn(1L);
    
    DataQualityMetricsDTO metrics = facadeService.getDataQualityMetrics();
    assertThat(metrics.getTotalContacts()).isEqualTo(1);
    assertThat(metrics.getContactsWithInteractions()).isEqualTo(1);
    assertThat(metrics.getAverageInteractionsPerContact()).isEqualTo(1.0);
  }

  @Test
  void recordNote_thenCalculateWarmth_shouldUpdateMetricsCorrectly() {
    // Given
    when(contactRepository.findById(contactId)).thenReturn(testContact);
    when(mapper.toEntity(any())).thenReturn(testInteraction);
    when(mapper.toDTO(any())).thenReturn(testDTO);
    when(interactionRepository.findLastUpdateDate(testContact)).thenReturn(LocalDateTime.now());
    
    // Mock the PanacheQuery for interaction count
    @SuppressWarnings("unchecked")
    io.quarkus.hibernate.orm.panache.PanacheQuery<ContactInteraction> mockQuery = mock(io.quarkus.hibernate.orm.panache.PanacheQuery.class);
    when(interactionRepository.find("contact", testContact)).thenReturn(mockQuery);
    when(mockQuery.list()).thenReturn(Arrays.asList(testInteraction));
    
    // When - Record a note
    ContactInteractionDTO note = facadeService.recordNote(contactId, "Important note", "user");
    assertThat(note).isNotNull();
    
    // And calculate warmth score
    when(interactionRepository.findRecentInteractions(testContact, 90))
        .thenReturn(Arrays.asList(testInteraction));
    when(interactionRepository.calculateResponseRate(testContact)).thenReturn(60.0);
    
    WarmthScoreDTO warmthScore = facadeService.calculateWarmthScore(contactId);
    
    // Then
    assertThat(warmthScore).isNotNull();
    verify(contactRepository, atLeast(2)).persist((CustomerContact) testContact); // Updated for note and warmth
  }

  // ========== HELPER METHOD ==========

  private void verifyNoWriteOperationsForQuery() {
    // For pure query operations, no persist should be called
    verify(interactionRepository, never()).persist(any(ContactInteraction.class));
    verify(interactionRepository, never()).persist(any(List.class));
  }
}