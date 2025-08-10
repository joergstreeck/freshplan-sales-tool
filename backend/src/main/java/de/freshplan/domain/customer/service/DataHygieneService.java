package de.freshplan.domain.customer.service;

import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.service.dto.DataFreshnessLevel;
// import io.quarkus.scheduler.Scheduled; // TODO: Add scheduler dependency
import de.freshplan.domain.customer.service.dto.DataQualityScore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service für kontinuierliche Datenhygiene und Freshness Tracking.
 *
 * <p>Implementiert automatische Datenalterung-Erkennung, proaktive Update-Kampagnen und Data
 * Quality Scoring basierend auf dem Data Strategy Intelligence Konzept.
 */
@ApplicationScoped
@Transactional
public class DataHygieneService {

  private static final Logger LOG = LoggerFactory.getLogger(DataHygieneService.class);

  // Time thresholds for data freshness (in months/years)
  private static final int MONTHS_STALE_THRESHOLD = 6;
  private static final int YEARS_CRITICAL_THRESHOLD = 1;
  private static final int DAYS_FRESH = 30;
  private static final int DAYS_AGING = 90;
  private static final int DAYS_STALE = 180;

  // Data quality score penalties
  private static final int PENALTY_MISSING_EMAIL = 20;
  private static final int PENALTY_MISSING_PHONE = 10;
  private static final int PENALTY_MISSING_POSITION = 5;
  private static final int PENALTY_MISSING_ADDRESS = 5;
  private static final int PENALTY_NO_INTERACTIONS = 15;
  private static final int PENALTY_FEW_INTERACTIONS = 5;
  private static final int PENALTY_LOW_WARMTH_CONFIDENCE = 10;

  // Thresholds
  private static final int MINIMUM_INTERACTIONS = 3;
  private static final int MINIMUM_WARMTH_CONFIDENCE = 50;

  // Base score
  private static final int BASE_QUALITY_SCORE = 100;

  @Inject ContactRepository contactRepository;

  /**
   * Wöchentliche Überprüfung der Datenfreshness. Läuft jeden Montag um 9:00 Uhr. TODO:
   * Aktiviere @Scheduled(cron = "0 0 9 * * MON") nach Hinzufügung der Scheduler-Dependency
   */
  // @Scheduled(cron = "0 0 9 * * MON")
  public void checkDataFreshness() {
    LOG.info("Starting weekly data freshness check");

    try {
      // Finde veraltete Kontakte (über 6 Monate)
      LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(MONTHS_STALE_THRESHOLD);
      List<CustomerContact> staleContacts = contactRepository.find("updatedAt < ?1", sixMonthsAgo).list();

      LOG.info(
          "Found {} stale contacts (> {} months old)",
          staleContacts.size(),
          MONTHS_STALE_THRESHOLD);

      if (!staleContacts.isEmpty()) {
        processStaleContacts(staleContacts);
      }

      // Finde kritische Kontakte (über 1 Jahr)
      LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(YEARS_CRITICAL_THRESHOLD);
      List<CustomerContact> criticalContacts = contactRepository.find("updatedAt < ?1", oneYearAgo).list();

      LOG.info(
          "Found {} critical contacts (> {} year old)",
          criticalContacts.size(),
          YEARS_CRITICAL_THRESHOLD);

      if (!criticalContacts.isEmpty()) {
        processCriticalContacts(criticalContacts);
      }

    } catch (Exception e) {
      LOG.error("Error during data freshness check", e);
    }
  }

  /**
   * Tägliche Qualitätsbewertung für alle Kontakte. Läuft jeden Tag um 2:00 Uhr nachts. TODO:
   * Aktiviere @Scheduled(cron = "0 0 2 * * *") nach Hinzufügung der Scheduler-Dependency
   */
  // @Scheduled(cron = "0 0 2 * * *")
  public void updateDataQualityScores() {
    LOG.info("Starting daily data quality score update");

    try {
      List<CustomerContact> allContacts = contactRepository.listAll();
      int updated = 0;

      for (CustomerContact contact : allContacts) {
        DataQualityScore qualityScore = calculateDataQualityScore(contact);

        // Update nur wenn sich Score geändert hat
        if (contact.getDataQualityScore() == null
            || !contact.getDataQualityScore().equals(qualityScore.getScore())) {

          contact.setDataQualityScore(qualityScore.getScore());
          contact.setDataQualityRecommendations(
              String.join("; ", qualityScore.getRecommendations()));
          contactRepository.persist(contact);
          updated++;
        }
      }

      LOG.info("Updated data quality scores for {} contacts", updated);

    } catch (Exception e) {
      LOG.error("Error during data quality score update", e);
    }
  }

  /** Berechnet das Freshness Level eines Kontakts basierend auf dem letzten Update. */
  public DataFreshnessLevel getDataFreshnessLevel(CustomerContact contact) {
    if (contact.getUpdatedAt() == null) {
      return DataFreshnessLevel.CRITICAL;
    }

    long daysSinceUpdate =
        ChronoUnit.DAYS.between(contact.getUpdatedAt().toLocalDate(), LocalDate.now());

    if (daysSinceUpdate < 90) {
      return DataFreshnessLevel.FRESH;
    } else if (daysSinceUpdate < 180) {
      return DataFreshnessLevel.AGING;
    } else if (daysSinceUpdate < 365) {
      return DataFreshnessLevel.STALE;
    } else {
      return DataFreshnessLevel.CRITICAL;
    }
  }

  /** Berechnet einen umfassenden Data Quality Score (0-100). */
  public DataQualityScore calculateDataQualityScore(CustomerContact contact) {
    int score = 100;
    List<String> recommendations = new ArrayList<>();

    // Freshness Bewertung
    DataFreshnessLevel freshnessLevel = getDataFreshnessLevel(contact);
    switch (freshnessLevel) {
      case CRITICAL:
        score -= 40;
        recommendations.add("Kontakt ist über 1 Jahr alt - dringend aktualisieren");
        break;
      case STALE:
        score -= 20;
        recommendations.add("Kontakt ist über 6 Monate alt - Update empfohlen");
        break;
      case AGING:
        score -= 10;
        recommendations.add("Kontakt sollte bald aktualisiert werden");
        break;
    }

    // Vollständigkeit der Basisdaten
    if (contact.getEmail() == null || contact.getEmail().trim().isEmpty()) {
      score -= 15;
      recommendations.add("E-Mail-Adresse fehlt");
    }

    if ((contact.getPhone() == null || contact.getPhone().trim().isEmpty())
        && (contact.getMobile() == null || contact.getMobile().trim().isEmpty())) {
      score -= 10;
      recommendations.add("Telefonnummer fehlt");
    }

    if (contact.getPosition() == null || contact.getPosition().trim().isEmpty()) {
      score -= 5;
      recommendations.add("Position/Funktion fehlt");
    }

    // Department field doesn't exist in Contact entity yet
    // TODO: Add department field or use existing fields
    // if (contact.getDepartment() == null || contact.getDepartment().trim().isEmpty()) {
    //     score -= 5;
    //     recommendations.add("Abteilung fehlt");
    // }

    // Interaktionsdaten
    if (contact.getInteractionCount() == null || contact.getInteractionCount() == 0) {
      score -= 15;
      recommendations.add("Keine Interaktionen erfasst");
    } else if (contact.getInteractionCount() < 3) {
      score -= 5;
      recommendations.add("Wenige Interaktionen - mehr Kontaktpunkte dokumentieren");
    }

    // Warmth Score Verfügbarkeit
    if (contact.getWarmthScore() == null
        || contact.getWarmthConfidence() == null
        || contact.getWarmthConfidence() < 50) {
      score -= 10;
      recommendations.add("Warmth Score benötigt mehr Daten für höhere Genauigkeit");
    }

    // Stelle sicher, dass Score nicht negativ wird
    score = Math.max(0, score);

    if (recommendations.isEmpty()) {
      recommendations.add("Alle Daten aktuell und vollständig");
    }

    return DataQualityScore.builder()
        .score(score)
        .recommendations(recommendations)
        .freshnessLevel(freshnessLevel)
        .lastCalculated(LocalDateTime.now())
        .build();
  }

  /** Verarbeitung von veralteten Kontakten (6+ Monate). */
  private void processStaleContacts(List<CustomerContact> staleContacts) {
    // Gruppiere nach verantwortlichem User (später implementieren)
    // Für jetzt: Log und markiere für Update

    for (CustomerContact contact : staleContacts) {
      LOG.debug(
          "Stale contact found: {} (last updated: {})",
          contact.getFirstName() + " " + contact.getLastName(),
          contact.getUpdatedAt());

      // Update Quality Score
      DataQualityScore qualityScore = calculateDataQualityScore(contact);
      contact.setDataQualityScore(qualityScore.getScore());
      contact.setDataQualityRecommendations(String.join("; ", qualityScore.getRecommendations()));
    }

    contactRepository.persist(staleContacts);
    LOG.info("Processed {} stale contacts", staleContacts.size());
  }

  /** Verarbeitung von kritischen Kontakten (1+ Jahr). */
  private void processCriticalContacts(List<CustomerContact> criticalContacts) {
    for (CustomerContact contact : criticalContacts) {
      LOG.warn(
          "Critical contact found: {} (last updated: {})",
          contact.getFirstName() + " " + contact.getLastName(),
          contact.getUpdatedAt());

      // Markiere als kritisch
      DataQualityScore qualityScore = calculateDataQualityScore(contact);
      contact.setDataQualityScore(qualityScore.getScore());
      contact.setDataQualityRecommendations(String.join("; ", qualityScore.getRecommendations()));

      // TODO: Create high-priority task
      // TODO: Send notification to responsible user
    }

    contactRepository.persist(criticalContacts);
    LOG.info("Processed {} critical contacts", criticalContacts.size());
  }

  /** Gibt Statistiken zur Datenfreshness zurück. */
  public Map<DataFreshnessLevel, Long> getDataFreshnessStatistics() {
    List<CustomerContact> allContacts = contactRepository.listAll();

    return allContacts.stream()
        .collect(Collectors.groupingBy(this::getDataFreshnessLevel, Collectors.counting()));
  }

  /** Findet alle Kontakte mit einem bestimmten Freshness Level. */
  public List<CustomerContact> findContactsByFreshnessLevel(DataFreshnessLevel level) {
    List<CustomerContact> allContacts = contactRepository.listAll();

    return allContacts.stream()
        .filter(contact -> getDataFreshnessLevel(contact) == level)
        .collect(Collectors.toList());
  }
}
