package de.freshplan.modules.leads.service;

import de.freshplan.modules.leads.domain.ActivityType;
import de.freshplan.modules.leads.domain.Lead;
import de.freshplan.modules.leads.domain.LeadActivity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.regex.Pattern;
import org.jboss.logging.Logger;

/**
 * Service for automatic email activity detection and lead matching. Sprint 2.1: Foundation for
 * Phase 2 email integration with ML-based classification.
 *
 * <p>Based on implementation-plans/02_EMAIL_INTEGRATION_PLAN.md
 */
@ApplicationScoped
public class EmailActivityDetectionService {

  private static final Logger LOG = Logger.getLogger(EmailActivityDetectionService.class);

  @Inject LeadService leadService;

  // FreshFoodz-specific keywords for activity detection
  private static final Map<ActivityType, List<Pattern>> ACTIVITY_PATTERNS = initPatterns();

  private static Map<ActivityType, List<Pattern>> initPatterns() {
    Map<ActivityType, List<Pattern>> patterns = new HashMap<>();

    // Sample-related patterns
    patterns.put(
        ActivityType.SAMPLE_SENT,
        List.of(
            Pattern.compile("(?i)\\b(muster|sample|probe|testen)\\b"),
            Pattern.compile("(?i)\\b(cook.*fresh|produktprobe)\\b"),
            Pattern.compile("(?i)\\b(gratis.*lieferung|kostenlose.*probe)\\b")));

    // Meeting patterns
    patterns.put(
        ActivityType.MEETING,
        List.of(
            Pattern.compile("(?i)\\b(termin|meeting|besprechung|treffen)\\b"),
            Pattern.compile("(?i)\\b(vor.*ort|besuch|präsentation)\\b"),
            Pattern.compile("(?i)\\b(kalender|zeitfenster|verfügbar)\\b")));

    // Order patterns
    patterns.put(
        ActivityType.ORDER,
        List.of(
            Pattern.compile("(?i)\\b(bestellung|order|auftrag)\\b"),
            Pattern.compile("(?i)\\b(lieferung|versand|zustellung)\\b"),
            Pattern.compile("(?i)\\b(rechnung|zahlung|überweisung)\\b")));

    // Call patterns
    patterns.put(
        ActivityType.CALL,
        List.of(
            Pattern.compile("(?i)\\b(anruf|telefonat|rückruf)\\b"),
            Pattern.compile("(?i)\\b(telefon|handy|mobil)\\b"),
            Pattern.compile("(?i)\\b(erreichen.*telefonisch)\\b")));

    return patterns;
  }

  /**
   * Analyzes email content to detect activity type.
   *
   * @param emailContent the email body content
   * @param subject the email subject
   * @return detected activity type with confidence score
   */
  public EmailActivityResult analyzeEmailContent(String emailContent, String subject) {
    LOG.debugf("Analyzing email content: subject='%s'", subject);

    // Combine subject and content for analysis
    String fullContent = subject + " " + emailContent;

    // Check each activity type pattern
    Map<ActivityType, Double> scores = new HashMap<>();
    for (Map.Entry<ActivityType, List<Pattern>> entry : ACTIVITY_PATTERNS.entrySet()) {
      double score = calculatePatternScore(fullContent, entry.getValue());
      if (score > 0) {
        scores.put(entry.getKey(), score);
      }
    }

    // Find best match
    if (scores.isEmpty()) {
      LOG.debug("No activity pattern matched - defaulting to EMAIL");
      return new EmailActivityResult(ActivityType.EMAIL, 0.5);
    }

    Map.Entry<ActivityType, Double> bestMatch =
        scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(Map.entry(ActivityType.EMAIL, 0.5));

    LOG.infof(
        "Detected activity type: %s with confidence %.2f",
        bestMatch.getKey(), bestMatch.getValue());

    return new EmailActivityResult(bestMatch.getKey(), bestMatch.getValue());
  }

  /**
   * Matches email sender to existing leads.
   *
   * @param senderEmail the sender's email address
   * @param senderName the sender's display name
   * @param companyDomain the domain extracted from email
   * @return list of potential lead matches with confidence scores
   */
  public List<LeadMatch> findMatchingLeads(
      String senderEmail, String senderName, String companyDomain) {
    List<LeadMatch> matches = new ArrayList<>();
    Set<Long> matchedLeadIds = new HashSet<>();  // For O(1) duplicate checking

    // Normalize email for matching
    String normalizedEmail = Lead.normalizeEmail(senderEmail);

    // Strategy 1: Exact email match using normalized field (more efficient)
    if (normalizedEmail != null) {
      List<Lead> exactEmailMatches = Lead.find("emailNormalized", normalizedEmail).list();
      exactEmailMatches.forEach(lead -> {
        matches.add(new LeadMatch(lead, 1.0, "email"));
        matchedLeadIds.add(lead.id);
      });
    }

    // Strategy 2: Domain match using normalized field (more efficient)
    if (companyDomain != null && !companyDomain.isEmpty()) {
      String normalizedDomain = companyDomain.toLowerCase();
      List<Lead> domainMatches = Lead.find("emailNormalized like ?1", "%@" + normalizedDomain).list();
      domainMatches.forEach(
          lead -> {
            if (!matchedLeadIds.contains(lead.id)) {
              matches.add(new LeadMatch(lead, 0.8, "domain"));
              matchedLeadIds.add(lead.id);
            }
          });
    }

    // Strategy 3: Contact name match
    if (senderName != null && !senderName.isEmpty()) {
      List<Lead> nameMatches = Lead.find("contactPerson like ?1", "%" + senderName + "%").list();
      nameMatches.forEach(
          lead -> {
            if (!matchedLeadIds.contains(lead.id)) {
              matches.add(new LeadMatch(lead, 0.6, "name"));
              matchedLeadIds.add(lead.id);
            }
          });
    }

    // Sort by confidence score
    matches.sort((a, b) -> Double.compare(b.confidence, a.confidence));

    LOG.infof("Found %d potential lead matches for email %s", matches.size(), senderEmail);
    return matches;
  }

  /**
   * Creates activity for lead based on email detection.
   *
   * @param lead the lead to update
   * @param activityType detected activity type
   * @param emailContent the email content for description
   * @param confidence the detection confidence
   * @return created lead activity
   */
  @Transactional
  public LeadActivity createEmailActivity(
      Lead lead, ActivityType activityType, String emailContent, double confidence) {
    LeadActivity activity = new LeadActivity();
    activity.lead = lead;
    activity.userId = "EMAIL_SYSTEM";
    activity.activityType = activityType;
    activity.description = truncateContent(emailContent, 500);

    // Add confidence metadata
    activity.metadata.put("source", "email");
    activity.metadata.put("confidence", confidence);
    activity.metadata.put("auto_detected", true);

    activity.persist();

    // Update lead's last activity if meaningful
    if (activityType.isMeaningfulContact()) {
      lead.lastActivityAt = activity.activityDate;
      leadService.reactivateLead(lead);
    }

    LOG.infof(
        "Created email activity for lead %s: type=%s, confidence=%.2f",
        lead.id, activityType, confidence);

    return activity;
  }

  private double calculatePatternScore(String content, List<Pattern> patterns) {
    int matches = 0;
    for (Pattern pattern : patterns) {
      if (pattern.matcher(content).find()) {
        matches++;
      }
    }
    return matches > 0 ? (double) matches / patterns.size() : 0;
  }

  private String truncateContent(String content, int maxLength) {
    if (content == null) return "";
    return content.length() > maxLength ? content.substring(0, maxLength) + "..." : content;
  }

  /** Result of email activity detection */
  public static class EmailActivityResult {
    public final ActivityType activityType;
    public final double confidence;

    public EmailActivityResult(ActivityType activityType, double confidence) {
      this.activityType = activityType;
      this.confidence = confidence;
    }
  }

  /** Lead match result with confidence score */
  public static class LeadMatch {
    public final Lead lead;
    public final double confidence;
    public final String matchStrategy;

    public LeadMatch(Lead lead, double confidence, String matchStrategy) {
      this.lead = lead;
      this.confidence = confidence;
      this.matchStrategy = matchStrategy;
    }
  }
}