package de.freshplan.domain.customer.dto;

import java.util.List;

/**
 * Card Section for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * <p>Represents a section within a Customer Card (e.g., "Stammdaten", "Standorte", "Finanzen").
 *
 * <p>Each Card contains multiple Sections, each Section contains multiple Fields.
 *
 * <p>Example:
 *
 * <pre>
 * Card: "Unternehmensprofil"
 *   Section: "Stammdaten"
 *     - Firmenname (TEXT)
 *     - Handelsname (TEXT)
 *     - Rechtsform (ENUM)
 *   Section: "Standorte"
 *     - Anzahl Standorte EU (NUMBER)
 *     - Expansion geplant (BOOLEAN)
 * </pre>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record CardSection(
    /** Section ID (e.g., "basic-info", "locations", "financials") */
    String sectionId,

    /** Section title (German, e.g., "Stammdaten", "Standorte") */
    String title,

    /** Section subtitle (optional, e.g., "Grundlegende Unternehmensinformationen") */
    String subtitle,

    /** List of fields in this section */
    List<FieldDefinition> fields,

    /** Should this section be collapsible? */
    boolean collapsible,

    /** Is this section initially collapsed? (only if collapsible=true) */
    boolean defaultCollapsed) {

  /** Builder for convenient CardSection creation */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String sectionId;
    private String title;
    private String subtitle;
    private List<FieldDefinition> fields = List.of();
    private boolean collapsible = false;
    private boolean defaultCollapsed = false;

    public Builder sectionId(String sectionId) {
      this.sectionId = sectionId;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder subtitle(String subtitle) {
      this.subtitle = subtitle;
      return this;
    }

    public Builder fields(List<FieldDefinition> fields) {
      this.fields = fields;
      return this;
    }

    public Builder collapsible(boolean collapsible) {
      this.collapsible = collapsible;
      return this;
    }

    public Builder defaultCollapsed(boolean defaultCollapsed) {
      this.defaultCollapsed = defaultCollapsed;
      return this;
    }

    public CardSection build() {
      return new CardSection(sectionId, title, subtitle, fields, collapsible, defaultCollapsed);
    }
  }
}
