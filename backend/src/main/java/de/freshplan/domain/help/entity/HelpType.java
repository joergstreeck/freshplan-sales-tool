package de.freshplan.domain.help.entity;

/** Arten von Hilfe-Inhalten im System */
public enum HelpType {

  /** Kleine Tooltips mit kurzen Erklärungen Erscheinen beim Hover über Help-Icons */
  TOOLTIP,

  /** Interaktive Feature-Tours Führen User durch neue Features */
  TOUR,

  /** Häufig gestellte Fragen Mit Frage-Antwort Paaren */
  FAQ,

  /** Video-Tutorials Links zu externen oder eingebetteten Videos */
  VIDEO,

  /** Schritt-für-Schritt Anleitungen Mit detaillierten Beschreibungen */
  TUTORIAL,

  /** Proaktive Hilfe bei erkannten Problemen Wird automatisch ausgelöst */
  PROACTIVE,

  /** Erste-Schritte Hilfe für neue Features Beim ersten Verwenden eines Features */
  FIRST_TIME,

  /** Korrektive Hinweise bei Fehlbedienung Nach erkannten Fehlern */
  CORRECTION
}
