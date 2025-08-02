package de.freshplan.domain.customer.service.dto;

/**
 * Enum für die verschiedenen Stufen der Datenfreshness.
 *
 * <p>Basiert auf dem Data Strategy Intelligence Konzept für progressive Datenqualitätsbewertung.
 */
public enum DataFreshnessLevel {

  /** Fresh - weniger als 90 Tage alt. Grün - Alles gut, keine Aktion erforderlich. */
  FRESH("< 90 Tage", "fresh", "success", 90),

  /** Aging - 90-180 Tage alt. Gelb - Hinweis anzeigen, bald aktualisieren. */
  AGING("90-180 Tage", "aging", "warning", 180),

  /** Stale - 180-365 Tage alt. Orange - Update empfohlen. */
  STALE("180-365 Tage", "stale", "warning", 365),

  /** Critical - über 365 Tage alt. Rot - Dringender Handlungsbedarf. */
  CRITICAL("> 365 Tage", "critical", "error", Integer.MAX_VALUE);

  private final String description;
  private final String key;
  private final String severity;
  private final int maxDays;

  DataFreshnessLevel(String description, String key, String severity, int maxDays) {
    this.description = description;
    this.key = key;
    this.severity = severity;
    this.maxDays = maxDays;
  }

  public String getDescription() {
    return description;
  }

  public String getKey() {
    return key;
  }

  public String getSeverity() {
    return severity;
  }

  public int getMaxDays() {
    return maxDays;
  }

  /** Bestimmt das Freshness Level basierend auf Tagen seit letztem Update. */
  public static DataFreshnessLevel fromDays(long daysSinceUpdate) {
    if (daysSinceUpdate < 90) {
      return FRESH;
    } else if (daysSinceUpdate < 180) {
      return AGING;
    } else if (daysSinceUpdate < 365) {
      return STALE;
    } else {
      return CRITICAL;
    }
  }

  /** Gibt die entsprechende Material-UI Farbe zurück. */
  public String getMuiColor() {
    switch (this) {
      case FRESH:
        return "success";
      case AGING:
        return "info";
      case STALE:
        return "warning";
      case CRITICAL:
        return "error";
      default:
        return "default";
    }
  }

  /** Gibt eine benutzerfreundliche deutsche Bezeichnung zurück. */
  public String getDisplayName() {
    switch (this) {
      case FRESH:
        return "Aktuell";
      case AGING:
        return "Bald veraltet";
      case STALE:
        return "Veraltet";
      case CRITICAL:
        return "Kritisch";
      default:
        return "Unbekannt";
    }
  }
}
