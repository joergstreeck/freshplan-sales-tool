package de.freshplan.domain.customer.entity;

/**
 * Academic and Professional Titles (German Business Etiquette).
 *
 * <p>Sprint 2.1.7.2 D11.1: Contact Management - Backend/Frontend Parity
 *
 * <p>German business culture requires proper titles in formal communication (letters, emails).
 * These are the most common titles used in German business correspondence.
 */
public enum Title {
  /** Dr. - Doktor (most common academic title) */
  DR("Dr."),

  /** Prof. - Professor */
  PROF("Prof."),

  /** Prof. Dr. - Professor Doktor (combined) */
  PROF_DR("Prof. Dr."),

  /** Dipl.-Ing. - Diplom-Ingenieur */
  DIPL_ING("Dipl.-Ing."),

  /** Dipl.-Kfm. - Diplom-Kaufmann */
  DIPL_KFM("Dipl.-Kfm."),

  /** Dipl.-Betriebswirt - Diplom-Betriebswirt */
  DIPL_BW("Dipl.-Betriebswirt"),

  /** M.Sc. - Master of Science */
  MSC("M.Sc."),

  /** B.Sc. - Bachelor of Science */
  BSC("B.Sc."),

  /** MBA - Master of Business Administration */
  MBA("MBA");

  private final String displayName;

  Title(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
