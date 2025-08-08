package de.freshplan.domain.cost.entity;

/** Status eines Cost Transactions während des Lebenszyklus */
public enum TransactionStatus {
  STARTED, // Transaktion begonnen
  COMPLETED, // Erfolgreich abgeschlossen
  FAILED, // Fehlgeschlagen
  CANCELLED // Abgebrochen (z.B. durch Budget-Limit)
}
