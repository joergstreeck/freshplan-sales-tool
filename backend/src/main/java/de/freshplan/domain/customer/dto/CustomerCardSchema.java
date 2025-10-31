package de.freshplan.domain.customer.dto;

import java.util.List;

/**
 * Customer Card Schema for Server-Driven UI
 *
 * <p>Sprint 2.1.7.2 D11: Server-Driven Customer Cards
 *
 * <p>Represents a complete Customer Card definition.
 *
 * <p>Frontend fetches this schema from `/api/customers/schema` and renders all 7 cards dynamically.
 *
 * <p>Each Card contains multiple Sections, each Section contains multiple Fields.
 *
 * <p>Example Structure:
 *
 * <pre>
 * CustomerCardSchema
 *   cardId: "company-profile"
 *   title: "Unternehmensprofil"
 *   icon: "🏢"
 *   order: 1
 *   sections: [
 *     CardSection(sectionId="basic-info", title="Stammdaten", fields=[...]),
 *     CardSection(sectionId="locations", title="Standorte", fields=[...])
 *   ]
 * </pre>
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
public record CustomerCardSchema(
    /** Card ID (e.g., "company-profile", "business-data", "health-risk") */
    String cardId,

    /** Card title (German, e.g., "Unternehmensprofil", "Geschäftsdaten & Performance") */
    String title,

    /** Card subtitle (optional, e.g., "Stammdaten, Standorte, Klassifikation") */
    String subtitle,

    /** Card icon emoji (e.g., "🏢", "💰", "⚠️") */
    String icon,

    /** Display order (1-7 for the 7 cards) */
    int order,

    /** List of sections in this card */
    List<CardSection> sections,

    /** Should this card be initially collapsed? */
    boolean defaultCollapsed) {

  /** Builder for convenient CustomerCardSchema creation */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String cardId;
    private String title;
    private String subtitle;
    private String icon;
    private int order;
    private List<CardSection> sections = List.of();
    private boolean defaultCollapsed = false;

    public Builder cardId(String cardId) {
      this.cardId = cardId;
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

    public Builder icon(String icon) {
      this.icon = icon;
      return this;
    }

    public Builder order(int order) {
      this.order = order;
      return this;
    }

    public Builder sections(List<CardSection> sections) {
      this.sections = sections;
      return this;
    }

    public Builder defaultCollapsed(boolean defaultCollapsed) {
      this.defaultCollapsed = defaultCollapsed;
      return this;
    }

    public CustomerCardSchema build() {
      return new CustomerCardSchema(
          cardId, title, subtitle, icon, order, sections, defaultCollapsed);
    }
  }
}
