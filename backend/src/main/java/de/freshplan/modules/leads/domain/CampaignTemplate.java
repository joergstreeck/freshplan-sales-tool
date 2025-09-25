package de.freshplan.modules.leads.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Campaign template entity for future campaign management. Sprint 2.1: Foundation for Phase 3
 * campaign implementation.
 *
 * <p>Based on kampagnen/technical-concept.md - prepared for A/B testing and personalization.
 */
@Entity
@Table(name = "campaign_templates")
public class CampaignTemplate extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NotNull @Size(max = 255)
  @Column(nullable = false)
  public String name;

  @Size(max = 1000)
  public String description;

  /** Email subject with variable support (e.g., "{{lead.company}} - Special Offer") */
  @NotNull @Size(max = 255)
  @Column(nullable = false)
  public String subject;

  /** HTML email content with FreshFoodz CI compliance */
  @Column(name = "html_content", columnDefinition = "TEXT")
  public String htmlContent;

  /** Plain text fallback */
  @Column(name = "text_content", columnDefinition = "TEXT")
  public String textContent;

  /** Template variables for personalization */
  @Column(columnDefinition = "jsonb")
  @Convert(converter = JsonObjectConverter.class)
  public JsonObject variables = new JsonObject();

  /** Template type for different campaign purposes */
  @Enumerated(EnumType.STRING)
  @Column(name = "template_type", length = 50)
  public TemplateType templateType = TemplateType.STANDARD;

  /** FreshFoodz CI color scheme */
  @Column(name = "primary_color")
  public String primaryColor = "#94C456"; // FreshFoodz Green

  @Column(name = "secondary_color")
  public String secondaryColor = "#004F7B"; // FreshFoodz Blue

  /** Template status */
  @Column(nullable = false)
  public boolean active = true;

  /** Usage statistics */
  @Column(name = "times_used")
  public int timesUsed = 0;

  @Column(name = "last_used_at")
  public LocalDateTime lastUsedAt;

  /** Metadata */
  @Column(name = "created_at", nullable = false)
  public LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  public LocalDateTime updatedAt;

  @Size(max = 50)
  @Column(name = "created_by")
  public String createdBy;

  /** Template types aligned with B2B Food business */
  public enum TemplateType {
    STANDARD, // General purpose
    SAMPLE_REQUEST, // Cook&Fresh sample campaigns
    FOLLOW_UP, // T+3/T+7 automated follow-ups
    SEASONAL, // Spargel, Oktoberfest, Christmas
    NEWSLETTER, // Regular updates
    WELCOME, // New lead welcome series
    REACTIVATION // Dormant lead reactivation
  }

  /**
   * Get personalized content by replacing variables.
   *
   * @param data variable data for replacement
   * @return personalized HTML content
   */
  public String getPersonalizedContent(Map<String, String> data) {
    String content = htmlContent;
    for (Map.Entry<String, String> entry : data.entrySet()) {
      String placeholder = "{{" + entry.getKey() + "}}";
      content = content.replace(placeholder, entry.getValue());
    }
    return content;
  }

  /**
   * Get personalized subject.
   *
   * @param data variable data for replacement
   * @return personalized subject
   */
  public String getPersonalizedSubject(Map<String, String> data) {
    String subjectLine = subject;
    for (Map.Entry<String, String> entry : data.entrySet()) {
      String placeholder = "{{" + entry.getKey() + "}}";
      subjectLine = subjectLine.replace(placeholder, entry.getValue());
    }
    return subjectLine;
  }

  /** Extract variables from template content. */
  public Map<String, String> extractVariables() {
    Map<String, String> vars = new HashMap<>();
    // Simple regex to find {{variable}} patterns
    String pattern = "\\{\\{([^}]+)\\}\\}";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);

    // Check in subject
    java.util.regex.Matcher m = p.matcher(subject);
    while (m.find()) {
      vars.put(m.group(1), "");
    }

    // Check in HTML content
    if (htmlContent != null) {
      m = p.matcher(htmlContent);
      while (m.find()) {
        vars.put(m.group(1), "");
      }
    }

    return vars;
  }

  // Static finder methods
  public static CampaignTemplate findActiveByType(TemplateType type) {
    return find("templateType = ?1 and active = true", type).firstResult();
  }

  public static List<CampaignTemplate> findAllActive() {
    return list("active", true);
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}