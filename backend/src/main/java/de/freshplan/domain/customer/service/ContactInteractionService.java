package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.Contact;
import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.ContactInteraction.InteractionType;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.dto.DataQualityMetricsDTO;
import de.freshplan.domain.customer.service.dto.WarmthScoreDTO;
import de.freshplan.domain.customer.service.mapper.ContactInteractionMapper;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * Service for managing contact interactions and calculating intelligence metrics. Core component of
 * the Data Strategy Intelligence feature.
 */
@ApplicationScoped
@Transactional
public class ContactInteractionService {

  private static final Logger LOG = Logger.getLogger(ContactInteractionService.class);

  @Inject ContactInteractionRepository interactionRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionMapper mapper;

  /** Create a new interaction and update contact metrics */
  public ContactInteractionDTO createInteraction(ContactInteractionDTO dto) {
    LOG.infof("Creating interaction for contact %s", dto.getContactId());

    Contact contact = contactRepository.findById(dto.getContactId());
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

  /** Get all interactions for a contact */
  public List<ContactInteractionDTO> getInteractionsByContact(UUID contactId, Page page) {
    Contact contact = contactRepository.findById(contactId);
    if (contact == null) {
      throw new IllegalArgumentException("Contact not found: " + contactId);
    }

    return interactionRepository.findByContactPaginated(contact, page).stream()
        .map(mapper::toDTO)
        .collect(Collectors.toList());
  }

  /** Calculate warmth score for a contact */
  public WarmthScoreDTO calculateWarmthScore(UUID contactId) {
    Contact contact = contactRepository.findById(contactId);
    if (contact == null) {
      throw new IllegalArgumentException("Contact not found: " + contactId);
    }

    // Get recent interactions (last 90 days)
    List<ContactInteraction> recentInteractions =
        interactionRepository.findRecentInteractions(contact, 90);

    if (recentInteractions.isEmpty()) {
      // Return default cold start values
      return WarmthScoreDTO.builder()
          .contactId(contactId)
          .warmthScore(50) // Neutral
          .confidence(0) // No confidence
          .dataPoints(0)
          .build();
    }

    // Calculate base score from multiple factors
    double frequencyScore = calculateFrequencyScore(recentInteractions);
    double sentimentScore = calculateSentimentScore(recentInteractions);
    double engagementScore = calculateEngagementScore(recentInteractions);
    double responseScore = calculateResponseScore(contact);

    // Weighted average
    double warmthScore =
        (frequencyScore * 0.3)
            + (sentimentScore * 0.3)
            + (engagementScore * 0.2)
            + (responseScore * 0.2);

    // Calculate confidence based on data points
    int dataPoints = recentInteractions.size();
    int confidence = Math.min(100, dataPoints * 10); // 10 interactions = 100% confidence

    // Update contact with calculated values
    contact.setWarmthScore((int) Math.round(warmthScore));
    contact.setWarmthConfidence(confidence);
    contactRepository.persist(contact);

    return WarmthScoreDTO.builder()
        .contactId(contactId)
        .warmthScore((int) Math.round(warmthScore))
        .confidence(confidence)
        .dataPoints(dataPoints)
        .lastCalculated(LocalDateTime.now())
        .build();
  }

  /** Get data quality metrics for intelligence features */
  public DataQualityMetricsDTO getDataQualityMetrics() {
    long totalContacts = contactRepository.count();
    long contactsWithInteractions =
        contactRepository.count("id in (select distinct i.contact.id from ContactInteraction i)");

    double averageInteractions = 0;
    if (contactsWithInteractions > 0) {
      long totalInteractions = interactionRepository.count();
      averageInteractions = (double) totalInteractions / contactsWithInteractions;
    }

    long contactsWithWarmthScore = contactRepository.count("warmthScore is not null");

    // Calculate data freshness categories based on updatedAt (when contact data was last modified)
    long freshContacts = countFreshContacts();
    long agingContacts = countAgingContacts();
    long staleContacts = countStaleContacts();
    long criticalContacts = countCriticalContacts();

    double dataCompletenessScore = calculateDataCompleteness();

    return DataQualityMetricsDTO.builder()
        .totalContacts(totalContacts)
        .contactsWithInteractions(contactsWithInteractions)
        .averageInteractionsPerContact(averageInteractions)
        .dataCompletenessScore(dataCompletenessScore)
        .contactsWithWarmthScore(contactsWithWarmthScore)
        .freshContacts(freshContacts)
        .agingContacts(agingContacts)
        .staleContacts(staleContacts)
        .criticalContacts(criticalContacts)
        .build();
  }

  /** Record a note as an interaction */
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

  // Private helper methods

  private void updateContactMetrics(Contact contact) {
    LocalDateTime lastInteraction = interactionRepository.findLastUpdateDate(contact);
    int interactionCount = interactionRepository.find("contact", contact).list().size();

    contact.setLastInteractionDate(lastInteraction);
    contact.setInteractionCount(interactionCount);
    contactRepository.persist(contact);
  }

  private int countWords(String text) {
    if (text == null || text.isBlank()) return 0;
    return text.trim().split("\\s+").length;
  }

  private double calculateFrequencyScore(List<ContactInteraction> interactions) {
    if (interactions.isEmpty()) return 0;

    // Calculate days between first and last interaction
    LocalDateTime first = interactions.get(interactions.size() - 1).getTimestamp();
    LocalDateTime last = interactions.get(0).getTimestamp();
    long daysBetween = ChronoUnit.DAYS.between(first, last) + 1;

    // Interactions per week
    double interactionsPerWeek = (interactions.size() * 7.0) / daysBetween;

    // Score based on frequency (1+ per week = 100, 0 = 0)
    return Math.min(100, interactionsPerWeek * 100);
  }

  private double calculateSentimentScore(List<ContactInteraction> interactions) {
    List<Double> sentiments =
        interactions.stream()
            .map(ContactInteraction::getSentimentScore)
            .filter(s -> s != null)
            .collect(Collectors.toList());

    if (sentiments.isEmpty()) return 50; // Neutral if no sentiment data

    double avgSentiment =
        sentiments.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

    // Convert from -1 to 1 range to 0 to 100
    return (avgSentiment + 1) * 50;
  }

  private double calculateEngagementScore(List<ContactInteraction> interactions) {
    List<Integer> engagements =
        interactions.stream()
            .map(ContactInteraction::getEngagementScore)
            .filter(e -> e != null)
            .collect(Collectors.toList());

    if (engagements.isEmpty()) return 50; // Default if no engagement data

    return engagements.stream().mapToInt(Integer::intValue).average().orElse(50.0);
  }

  private double calculateResponseScore(Contact contact) {
    Double responseRate = interactionRepository.calculateResponseRate(contact);
    return responseRate != null ? responseRate : 50.0;
  }

  private long countFreshContacts() {
    LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
    return contactRepository.count("updatedAt >= ?1", ninetyDaysAgo);
  }

  private long countAgingContacts() {
    LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);
    LocalDateTime oneEightyDaysAgo = LocalDateTime.now().minusDays(180);
    return contactRepository.count(
        "updatedAt < ?1 and updatedAt >= ?2", ninetyDaysAgo, oneEightyDaysAgo);
  }

  private long countStaleContacts() {
    LocalDateTime oneEightyDaysAgo = LocalDateTime.now().minusDays(180);
    LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);
    return contactRepository.count(
        "updatedAt < ?1 and updatedAt >= ?2", oneEightyDaysAgo, oneYearAgo);
  }

  private long countCriticalContacts() {
    LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(365);
    return contactRepository.count("updatedAt < ?1", oneYearAgo);
  }

  private double calculateDataCompleteness() {
    // Simple completeness calculation - can be enhanced
    long totalContacts = contactRepository.count();
    if (totalContacts == 0) return 0;

    long completeContacts =
        contactRepository.count(
            "email is not null and (phone is not null or mobile is not null) "
                + "and position is not null");

    return (completeContacts * 100.0) / totalContacts;
  }
}
