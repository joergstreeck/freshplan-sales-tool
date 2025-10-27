package de.freshplan.domain.customer.entity;

/**
 * Salutation for Contacts (German Business Etiquette).
 *
 * Sprint 2.1.7.2 D11.1: Contact Management - Backend/Frontend Parity
 *
 * German business culture requires proper salutations in professional communication.
 */
public enum Salutation {
  /** Herr (Mr.) */
  HERR("Herr"),

  /** Frau (Ms./Mrs.) */
  FRAU("Frau"),

  /** Divers (Gender-neutral) */
  DIVERS("Divers");

  private final String displayName;

  Salutation(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
