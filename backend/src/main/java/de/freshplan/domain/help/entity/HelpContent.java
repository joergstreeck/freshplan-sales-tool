package de.freshplan.domain.help.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Help Content Entity für kontextuelle Hilfe-Inhalte
 *
 * <p>Diese Entität speichert alle Arten von Hilfe-Inhalten: - Tooltips und Erklärungen - Feature
 * Tours und Tutorials - FAQ Inhalte - Video-Links und Schritt-für-Schritt Anleitungen
 */
@Entity
@Table(name = "help_contents")
public class HelpContent extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  public UUID id;

  @Column(nullable = false, length = 100)
  @NotBlank
  public String feature; // "warmth-score", "contact-timeline", "cost-management"

  @Column(name = "help_type", nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull public HelpType helpType; // TOOLTIP, TOUR, FAQ, VIDEO, TUTORIAL

  @Column(nullable = false, length = 200)
  @NotBlank
  public String title;

  @Column(name = "short_content", columnDefinition = "TEXT")
  public String shortContent; // Kurze Tooltip-Texte

  @Column(name = "medium_content", columnDefinition = "TEXT")
  public String mediumContent; // Mittellange Erklärungen

  @Column(name = "detailed_content", columnDefinition = "TEXT")
  public String detailedContent; // Ausführliche Anleitungen

  @Column(name = "video_url")
  public String videoUrl; // Optional: Link zu Video-Tutorial

  @Column(name = "target_user_level", nullable = false)
  @Enumerated(EnumType.STRING)
  public UserLevel targetUserLevel; // BEGINNER, INTERMEDIATE, EXPERT

  @ElementCollection
  @CollectionTable(name = "help_content_roles", joinColumns = @JoinColumn(name = "help_content_id"))
  @Column(name = "role")
  public List<String> targetRoles; // ["admin", "sales", "manager"]

  @Column(name = "trigger_conditions", columnDefinition = "TEXT")
  public String triggerConditions; // JSON mit Trigger-Bedingungen

  @Column(name = "interaction_data", columnDefinition = "TEXT")
  public String interactionData; // JSON mit Tour-Steps, FAQ-Items, etc.

  // Analytics Felder
  @Column(name = "view_count", nullable = false)
  public Long viewCount = 0L;

  @Column(name = "helpful_count", nullable = false)
  public Long helpfulCount = 0L;

  @Column(name = "not_helpful_count", nullable = false)
  public Long notHelpfulCount = 0L;

  @Column(name = "avg_time_spent")
  public Integer avgTimeSpent; // Sekunden

  // Status und Metadaten
  @Column(name = "is_active", nullable = false)
  public Boolean isActive = true;

  @Column(name = "priority", nullable = false)
  public Integer priority = 10; // 1 = highest, 100 = lowest

  @Column(name = "created_by", length = 100)
  public String createdBy;

  @Column(name = "updated_by", length = 100)
  public String updatedBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  public LocalDateTime updatedAt;

  // Business Methods

  /** Berechnet die Hilfreichkeits-Rate */
  public double getHelpfulnessRate() {
    long total = helpfulCount + notHelpfulCount;
    if (total == 0) return 0.0;
    return (double) helpfulCount / total * 100.0;
  }

  /** Prüft ob der Content für den User-Level geeignet ist */
  public boolean isApplicableForUser(String userLevel, List<String> userRoles) {
    // User Level Check
    UserLevel level = UserLevel.valueOf(userLevel.toUpperCase());
    if (!isLevelApplicable(level)) {
      return false;
    }

    // Role Check (mindestens eine Rolle muss übereinstimmen)
    if (targetRoles != null && !targetRoles.isEmpty()) {
      return userRoles.stream().anyMatch(targetRoles::contains);
    }

    return true;
  }

  private boolean isLevelApplicable(UserLevel userLevel) {
    return switch (targetUserLevel) {
      case BEGINNER -> true; // Beginner content ist für alle sichtbar
      case INTERMEDIATE -> userLevel != UserLevel.BEGINNER;
      case EXPERT -> userLevel == UserLevel.EXPERT;
    };
  }

  /** Erhöht den View Counter */
  public void incrementViewCount() {
    this.viewCount++;
  }

  /** Registriert Feedback */
  public void recordFeedback(boolean helpful, Integer timeSpent) {
    if (helpful) {
      this.helpfulCount++;
    } else {
      this.notHelpfulCount++;
    }

    // Update average time spent
    if (timeSpent != null && timeSpent > 0) {
      if (this.avgTimeSpent == null) {
        this.avgTimeSpent = timeSpent;
      } else {
        // Simple moving average
        this.avgTimeSpent = (this.avgTimeSpent + timeSpent) / 2;
      }
    }
  }

  // Named Queries for common operations
  public static List<HelpContent> findByFeature(String feature) {
    return list("feature = ?1 AND isActive = true ORDER BY priority ASC", feature);
  }

  public static List<HelpContent> findByFeatureAndType(String feature, HelpType type) {
    return list(
        "feature = ?1 AND helpType = ?2 AND isActive = true ORDER BY priority ASC", feature, type);
  }

  public static List<HelpContent> findForUser(
      String feature, String userLevel, List<String> userRoles) {
    List<HelpContent> results =
        find(
                "feature = ?1 AND isActive = true AND "
                    + "(targetUserLevel = 'BEGINNER' OR "
                    + "(targetUserLevel = 'INTERMEDIATE' AND ?2 != 'BEGINNER') OR "
                    + "(targetUserLevel = 'EXPERT' AND ?2 = 'EXPERT')) "
                    + "ORDER BY priority ASC",
                feature,
                userLevel.toUpperCase())
            .list();

    return results.stream()
        .filter(
            content -> {
              return content.targetRoles == null
                  || content.targetRoles.isEmpty()
                  || userRoles.stream().anyMatch(content.targetRoles::contains);
            })
        .toList();
  }

  public static List<HelpContent> findMostViewed(int limit) {
    return find("isActive = true ORDER BY viewCount DESC").page(0, limit).list();
  }

  public static List<HelpContent> findMostHelpful(int limit) {
    return find("isActive = true AND (helpfulCount + notHelpfulCount) > 0 "
            + "ORDER BY (helpfulCount::float / (helpfulCount + notHelpfulCount)) DESC")
        .page(0, limit)
        .list();
  }
}
