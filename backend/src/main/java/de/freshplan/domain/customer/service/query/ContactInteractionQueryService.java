package de.freshplan.domain.customer.service.query;

import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.dto.ContactInteractionDTO;
import de.freshplan.domain.customer.service.dto.DataQualityMetricsDTO;
import de.freshplan.domain.customer.service.dto.WarmthScoreDTO;
import de.freshplan.domain.customer.service.mapper.ContactInteractionMapper;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * Query service for contact interactions - handles all read operations.
 * Part of CQRS refactoring to separate queries from commands.
 * 
 * This service is responsible for:
 * - Retrieving interactions for contacts
 * - Calculating warmth scores (without persisting)
 * - Providing data quality metrics
 * - Analyzing interaction patterns
 * 
 * IMPORTANT: This service does NOT have @Transactional as it only reads data!
 */
@ApplicationScoped
public class ContactInteractionQueryService {

  private static final Logger LOG = Logger.getLogger(ContactInteractionQueryService.class);

  // Warmth Score calculation weights (exact copies from original)
  private static final double WARMTH_WEIGHT_FREQUENCY = 0.3;
  private static final double WARMTH_WEIGHT_SENTIMENT = 0.3;
  private static final double WARMTH_WEIGHT_ENGAGEMENT = 0.2;
  private static final double WARMTH_WEIGHT_RESPONSE = 0.2;

  // Time-based constants (in days) - exact copies from original
  private static final int DAYS_RECENT = 30;
  private static final int DAYS_FRESH = 90;
  private static final int DAYS_AGING = 180;
  private static final int DAYS_STALE = 365;

  // Default scores - exact copies from original
  private static final double DEFAULT_SENTIMENT_SCORE = 50.0;
  private static final double DEFAULT_ENGAGEMENT_SCORE = 50.0;
  private static final double DEFAULT_RESPONSE_SCORE = 50.0;

  // Frequency calculation constants - exact copies from original
  private static final int DAYS_PER_WEEK = 7;
  private static final double MAX_FREQUENCY_SCORE = 100.0;

  @Inject ContactInteractionRepository interactionRepository;

  @Inject ContactRepository contactRepository;

  @Inject ContactInteractionMapper mapper;

  /**
   * Get all interactions for a contact with pagination.
   * This is an exact copy of the original ContactInteractionService.getInteractionsByContact method.
   * 
   * @param contactId The contact ID
   * @param page Pagination parameters
   * @return List of interaction DTOs
   * @throws IllegalArgumentException if contact is not found
   */
  public List<ContactInteractionDTO> getInteractionsByContact(UUID contactId, Page page) {
    CustomerContact contact = contactRepository.findById(contactId);
    if (contact == null) {
      throw new IllegalArgumentException("Contact not found: " + contactId);
    }

    return interactionRepository.findByContactPaginated(contact, page).stream()
        .map(mapper::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Calculate warmth score for a contact WITHOUT persisting it.
   * This is the READ part extracted from the original calculateWarmthScore method.
   * The actual persistence should be done by the Command service.
   * 
   * IMPORTANT CHANGE: This method no longer updates the contact entity!
   * It only calculates and returns the score. The CommandService.updateWarmthScore
   * should be called separately if persistence is needed.
   * 
   * @param contactId The contact ID
   * @return Calculated warmth score data
   * @throws IllegalArgumentException if contact is not found
   */
  public WarmthScoreDTO calculateWarmthScore(UUID contactId) {
    CustomerContact contact = contactRepository.findById(contactId);
    if (contact == null) {
      throw new IllegalArgumentException("Contact not found: " + contactId);
    }

    // Get recent interactions (last 90 days) - exact copy from original
    List<ContactInteraction> recentInteractions =
        interactionRepository.findRecentInteractions(contact, DAYS_FRESH);

    if (recentInteractions.isEmpty()) {
      // Return default cold start values - exact copy from original
      return WarmthScoreDTO.builder()
          .contactId(contactId)
          .warmthScore((int) Math.round(DEFAULT_SENTIMENT_SCORE)) // Neutral
          .confidence(0) // No confidence
          .dataPoints(0)
          .build();
    }

    // Calculate base score from multiple factors - exact copy from original
    double frequencyScore = calculateFrequencyScore(recentInteractions);
    double sentimentScore = calculateSentimentScore(recentInteractions);
    double engagementScore = calculateEngagementScore(recentInteractions);
    double responseScore = calculateResponseScore(contact);

    // Weighted average - exact copy from original
    double warmthScore =
        (frequencyScore * WARMTH_WEIGHT_FREQUENCY)
            + (sentimentScore * WARMTH_WEIGHT_SENTIMENT)
            + (engagementScore * WARMTH_WEIGHT_ENGAGEMENT)
            + (responseScore * WARMTH_WEIGHT_RESPONSE);

    // Calculate confidence based on data points - exact copy from original
    int dataPoints = recentInteractions.size();
    int confidence = Math.min(100, dataPoints * 10); // 10 interactions = 100% confidence

    // REMOVED: The original method updated the contact here
    // This is now handled by CommandService.updateWarmthScore() if needed

    // Determine trend (simplified logic for now) - exact copy from original
    String trend = warmthScore >= 70 ? "INCREASING" : warmthScore >= 40 ? "STABLE" : "DECREASING";

    return WarmthScoreDTO.builder()
        .contactId(contactId)
        .warmthScore((int) Math.round(warmthScore))
        .confidence(confidence)
        .dataPoints(dataPoints)
        .lastCalculated(LocalDateTime.now())
        .trend(trend)
        .build();
  }

  /**
   * Get data quality metrics for intelligence features.
   * This is an exact copy of the original ContactInteractionService.getDataQualityMetrics method.
   * 
   * @return Data quality metrics
   */
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

  // ========== Private helper methods for calculations (exact copies from original) ==========

  /**
   * Calculate frequency score based on interaction patterns.
   * Exact copy from original ContactInteractionService.
   */
  private double calculateFrequencyScore(List<ContactInteraction> interactions) {
    if (interactions.isEmpty()) return 0;

    // Calculate days between first and last interaction
    LocalDateTime first = interactions.get(interactions.size() - 1).getTimestamp();
    LocalDateTime last = interactions.get(0).getTimestamp();
    long daysBetween = ChronoUnit.DAYS.between(first, last) + 1;

    // Interactions per week
    double interactionsPerWeek = (interactions.size() * (double) DAYS_PER_WEEK) / daysBetween;

    // Score based on frequency (1+ per week = 100, 0 = 0)
    return Math.min(MAX_FREQUENCY_SCORE, interactionsPerWeek * MAX_FREQUENCY_SCORE);
  }

  /**
   * Calculate average sentiment score from interactions.
   * Exact copy from original ContactInteractionService.
   */
  private double calculateSentimentScore(List<ContactInteraction> interactions) {
    List<Double> sentiments =
        interactions.stream()
            .map(ContactInteraction::getSentimentScore)
            .filter(s -> s != null)
            .collect(Collectors.toList());

    if (sentiments.isEmpty()) return DEFAULT_SENTIMENT_SCORE; // Neutral if no sentiment data

    double avgSentiment =
        sentiments.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

    // Convert from -1 to 1 range to 0 to 100
    return (avgSentiment + 1) * DEFAULT_SENTIMENT_SCORE;
  }

  /**
   * Calculate engagement score from interactions.
   * Exact copy from original ContactInteractionService.
   */
  private double calculateEngagementScore(List<ContactInteraction> interactions) {
    List<Integer> engagements =
        interactions.stream()
            .map(ContactInteraction::getEngagementScore)
            .filter(e -> e != null)
            .collect(Collectors.toList());

    if (engagements.isEmpty()) return DEFAULT_ENGAGEMENT_SCORE; // Default if no engagement data

    return engagements.stream()
        .mapToInt(Integer::intValue)
        .average()
        .orElse(DEFAULT_ENGAGEMENT_SCORE);
  }

  /**
   * Calculate response score for a contact.
   * Exact copy from original ContactInteractionService.
   */
  private double calculateResponseScore(CustomerContact contact) {
    Double responseRate = interactionRepository.calculateResponseRate(contact);
    return responseRate != null ? responseRate : DEFAULT_RESPONSE_SCORE;
  }

  /**
   * Count fresh contacts (updated within 90 days).
   * Exact copy from original ContactInteractionService.
   */
  private long countFreshContacts() {
    LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(DAYS_FRESH);
    return contactRepository.count("updatedAt >= ?1", ninetyDaysAgo);
  }

  /**
   * Count aging contacts (updated between 90-180 days ago).
   * Exact copy from original ContactInteractionService.
   */
  private long countAgingContacts() {
    LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(DAYS_FRESH);
    LocalDateTime oneEightyDaysAgo = LocalDateTime.now().minusDays(DAYS_AGING);
    return contactRepository.count(
        "updatedAt < ?1 and updatedAt >= ?2", ninetyDaysAgo, oneEightyDaysAgo);
  }

  /**
   * Count stale contacts (updated between 180-365 days ago).
   * Exact copy from original ContactInteractionService.
   */
  private long countStaleContacts() {
    LocalDateTime oneEightyDaysAgo = LocalDateTime.now().minusDays(DAYS_AGING);
    LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(DAYS_STALE);
    return contactRepository.count(
        "updatedAt < ?1 and updatedAt >= ?2", oneEightyDaysAgo, oneYearAgo);
  }

  /**
   * Count critical contacts (not updated for over a year).
   * Exact copy from original ContactInteractionService.
   */
  private long countCriticalContacts() {
    LocalDateTime oneYearAgo = LocalDateTime.now().minusDays(DAYS_STALE);
    return contactRepository.count("updatedAt < ?1", oneYearAgo);
  }

  /**
   * Calculate data completeness percentage.
   * Exact copy from original ContactInteractionService.
   */
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