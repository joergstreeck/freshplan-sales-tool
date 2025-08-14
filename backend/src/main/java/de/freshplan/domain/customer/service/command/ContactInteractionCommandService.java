package de.freshplan.domain.customer.service.command;

import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.mapper.ContactInteractionMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * Command service for contact interactions - handles all write operations.
 * Part of CQRS refactoring to separate commands from queries.
 * 
 * This service is responsible for:
 * - Creating new interactions
 * - Updating contact metrics
 * - Batch importing interactions
 * - Updating warmth scores on contacts
 */
@ApplicationScoped
public class ContactInteractionCommandService {

  private static final Logger LOG = Logger.getLogger(ContactInteractionCommandService.class);

  @Inject ContactInteractionRepository interactionRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionMapper mapper;

  /**
   * Create a new interaction and update contact metrics.
   * This is an exact copy of the original ContactInteractionService.createInteraction method.
   * 
   * @param dto The interaction data transfer object
   * @return The created interaction as DTO
   * @throws IllegalArgumentException if contact is not found
   */
  @Transactional
  public ContactInteractionDTO createInteraction(ContactInteractionDTO dto) {
    LOG.infof("Creating interaction for contact %s", dto.getContactId());

    CustomerContact contact = contactRepository.findById(dto.getContactId());
    if (contact == null) {
      throw new IllegalArgumentException("Contact not found: " + dto.getContactId());
    }

    ContactInteraction interaction = mapper.toEntity(dto);
    interaction.setContact(contact);

    // Auto-capture metadata if not provided
    if (interaction.getWordCount() == null && interaction.getFullContent() != null) {
      interaction.setWordCount(countWords(interaction.getFullContent()));
    }

    // Persist interaction
    interactionRepository.persist(interaction);

    // Update contact metrics
    updateContactMetrics(contact);

    return mapper.toDTO(interaction);
  }

  /**
   * Record a note as an interaction.
   * This is an exact copy of the original ContactInteractionService.recordNote method.
   * 
   * @param contactId The contact ID
   * @param note The note content
   * @param createdBy The user creating the note
   * @return The created interaction as DTO
   */
  @Transactional
  public ContactInteractionDTO recordNote(UUID contactId, String note, String createdBy) {
    ContactInteractionDTO dto =
        ContactInteractionDTO.builder()
            .contactId(contactId)
            .type(InteractionType.NOTE)
            .timestamp(LocalDateTime.now())
            .summary(note)
            .fullContent(note)
            .initiatedBy("SALES")
            .createdBy(createdBy)
            .build();

    return createInteraction(dto);
  }

  /**
   * Batch import interactions for better performance.
   * This is an exact copy of the original ContactInteractionService.batchImportInteractions method.
   * Processes all interactions in a single transaction.
   *
   * @param dtos List of interaction DTOs to import
   * @return Import result with success/failure counts
   */
  @Transactional
  public BatchImportResult batchImportInteractions(List<ContactInteractionDTO> dtos) {
    LOG.infof("Starting batch import of %d interactions", dtos.size());

    int imported = 0;
    int failed = 0;
    List<String> errors = new ArrayList<>();

    // Validate and prepare all entities first
    List<ContactInteraction> toImport = new ArrayList<>();

    for (ContactInteractionDTO dto : dtos) {
      try {
        CustomerContact contact = contactRepository.findById(dto.getContactId());
        if (contact == null) {
          failed++;
          errors.add("Contact not found: " + dto.getContactId());
          continue;
        }

        ContactInteraction interaction = mapper.toEntity(dto);
        interaction.setContact(contact);
        toImport.add(interaction);

      } catch (Exception e) {
        failed++;
        errors.add("Failed to process interaction: " + e.getMessage());
        LOG.warn("Failed to process interaction during batch import", e);
      }
    }

    // Batch persist all valid interactions
    if (!toImport.isEmpty()) {
      try {
        interactionRepository.persist(toImport);
        imported = toImport.size();

        // Update contact last interaction dates in batch
        updateContactsLastInteraction(toImport);

        LOG.infof("Successfully imported %d interactions", imported);
      } catch (Exception e) {
        LOG.error("Failed to persist batch interactions", e);
        throw new RuntimeException("Batch import failed: " + e.getMessage(), e);
      }
    }

    return new BatchImportResult(imported, failed, errors);
  }

  /**
   * Update warmth score and confidence for a contact.
   * This is the WRITE part extracted from the original calculateWarmthScore method.
   * Should be called after the Query service calculates the score.
   * 
   * @param contactId The contact ID
   * @param warmthScore The calculated warmth score (0-100)
   * @param confidence The confidence level (0-100)
   * @throws IllegalArgumentException if contact is not found
   */
  @Transactional
  public void updateWarmthScore(UUID contactId, int warmthScore, int confidence) {
    LOG.debugf("Updating warmth score for contact %s: score=%d, confidence=%d", 
               contactId, warmthScore, confidence);

    CustomerContact contact = contactRepository.findById(contactId);
    if (contact == null) {
      throw new IllegalArgumentException("Contact not found: " + contactId);
    }

    // Update contact with calculated values
    contact.setWarmthScore(warmthScore);
    contact.setWarmthConfidence(confidence);
    contactRepository.persist(contact);

    LOG.infof("Updated warmth score for contact %s", contactId);
  }

  // ========== Private helper methods (exact copies from original) ==========

  /**
   * Update contact metrics after interaction.
   * Exact copy from original ContactInteractionService.
   */
  private void updateContactMetrics(CustomerContact contact) {
    LocalDateTime lastInteraction = interactionRepository.findLastUpdateDate(contact);
    int interactionCount = interactionRepository.find("contact", contact).list().size();

    contact.setLastInteractionDate(lastInteraction);
    contact.setInteractionCount(interactionCount);
    contactRepository.persist(contact);
  }

  /**
   * Count words in text.
   * Exact copy from original ContactInteractionService.
   */
  private int countWords(String text) {
    if (text == null || text.isBlank()) return 0;
    return text.trim().split("\\s+").length;
  }

  /**
   * Update last interaction dates for multiple contacts efficiently.
   * Exact copy from original ContactInteractionService.
   */
  private void updateContactsLastInteraction(List<ContactInteraction> interactions) {
    // Group by contact to find latest interaction per contact
    Map<UUID, LocalDateTime> latestInteractions =
        interactions.stream()
            .collect(
                Collectors.toMap(
                    i -> i.getContact().getId(),
                    ContactInteraction::getTimestamp,
                    (existing, replacement) ->
                        existing.isAfter(replacement) ? existing : replacement));

    // Batch update using custom query
    latestInteractions.forEach(
        (contactId, timestamp) -> {
          contactRepository.update("lastInteractionAt = ?1 where id = ?2", timestamp, contactId);
        });
  }

  /**
   * Result of batch import operation.
   * Exact copy from original ContactInteractionService.
   */
  public static class BatchImportResult {
    public final int imported;
    public final int failed;
    public final List<String> errors;

    public BatchImportResult(int imported, int failed, List<String> errors) {
      this.imported = imported;
      this.failed = failed;
      this.errors = errors;
    }
  }
}