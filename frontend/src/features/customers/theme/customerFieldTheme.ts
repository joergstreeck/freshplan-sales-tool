/**
 * Customer Field Theme - Freshfoodz CI konform
 *
 * Dieses Theme-System steuert die adaptive Darstellung von Kundenfeldern
 * unter Einhaltung der Freshfoodz Corporate Identity und UI-Sprachregeln.
 */

export interface CustomerFieldTheme {
  /** Darstellungsmodus des Formulars */
  darstellung: 'standard' | 'anpassungsfähig';

  /** Freshfoodz Corporate Identity Farben */
  farben: {
    primär: string; // #94C456 - Freshfoodz Grün
    sekundär: string; // #004F7B - Freshfoodz Blau
    weiß: string; // #FFFFFF
    schwarz: string; // #000000
    fehler: string; // #DC3545
    erfolg: string; // #94C456
    warnung: string; // #FFC107
    deaktiviert: string; // #CCCCCC
  };

  /** Freshfoodz Corporate Identity Schriften */
  schrift: {
    überschrift: string; // Antonio Bold
    text: string; // Poppins Regular
    betont: string; // Poppins Medium
  };

  /** Adaptive Feldgrößen-Definitionen */
  feldgrößen: {
    kompakt: {
      minBreite: string;
      maxBreite: string;
      beschreibung: string;
    };
    klein: {
      minBreite: string;
      maxBreite: string;
      beschreibung: string;
    };
    mittel: {
      minBreite: string;
      maxBreite: string;
      beschreibung: string;
    };
    groß: {
      minBreite: string;
      maxBreite: string;
      beschreibung: string;
    };
    voll: {
      minBreite: string;
      maxBreite: string;
      beschreibung: string;
    };
  };

  /** Layout-Einstellungen für adaptives Verhalten */
  layout: {
    spaltenAbstand: string;
    zeilenAbstand: string;
    mindestSpaltenBreite: string;
    umbruchBreite: string; // Breakpoint für Mobilansicht
  };

  /** Animationseinstellungen */
  animation: {
    übergangsDauer: string;
    übergangsFunktion: string;
  };
}

/** Standard-Theme mit Freshfoodz CI */
export const standardTheme: CustomerFieldTheme = {
  darstellung: 'standard',

  farben: {
    primär: '#94C456',
    sekundär: '#004F7B',
    weiß: '#FFFFFF',
    schwarz: '#000000',
    fehler: '#DC3545',
    erfolg: '#94C456',
    warnung: '#FFC107',
    deaktiviert: '#CCCCCC',
  },

  schrift: {
    überschrift: 'Antonio, sans-serif',
    text: 'Poppins, sans-serif',
    betont: 'Poppins, sans-serif',
  },

  feldgrößen: {
    kompakt: {
      minBreite: '60px',
      maxBreite: '100px',
      beschreibung: 'PLZ, Hausnummer',
    },
    klein: {
      minBreite: '100px',
      maxBreite: '160px',
      beschreibung: 'Anrede, Titel, kurze Dropdowns',
    },
    mittel: {
      minBreite: '140px',
      maxBreite: '240px',
      beschreibung: 'Name, Telefon, Ort',
    },
    groß: {
      minBreite: '220px',
      maxBreite: '400px',
      beschreibung: 'E-Mail, Straße, Firma',
    },
    voll: {
      minBreite: '100%',
      maxBreite: '100%',
      beschreibung: 'Notizen, Beschreibung',
    },
  },

  layout: {
    spaltenAbstand: '16px',
    zeilenAbstand: '24px',
    mindestSpaltenBreite: '280px',
    umbruchBreite: '768px',
  },

  animation: {
    übergangsDauer: '0.15s',
    übergangsFunktion: 'ease-out',
  },
};

/** Adaptives Theme mit dynamischer Anpassung */
export const adaptivesTheme: CustomerFieldTheme = {
  ...standardTheme,
  darstellung: 'anpassungsfähig',

  layout: {
    ...standardTheme.layout,
    mindestSpaltenBreite: '240px', // Kleinere Mindestbreite für besseres Umbruchverhalten
    umbruchBreite: '640px', // Früherer Umbruch für Mobile
  },
};

/** Theme-Auswahl basierend auf Einstellung */
export const getTheme = (
  mode: 'standard' | 'anpassungsfähig' = 'anpassungsfähig'
): CustomerFieldTheme => {
  return mode === 'anpassungsfähig' ? adaptivesTheme : standardTheme;
};

/** CSS-Variablen aus Theme generieren */
export const generateCSSVariables = (theme: CustomerFieldTheme): Record<string, string> => {
  return {
    '--kunde-primär': theme.farben.primär,
    '--kunde-sekundär': theme.farben.sekundär,
    '--kunde-weiß': theme.farben.weiß,
    '--kunde-schwarz': theme.farben.schwarz,
    '--kunde-fehler': theme.farben.fehler,
    '--kunde-erfolg': theme.farben.erfolg,
    '--kunde-warnung': theme.farben.warnung,
    '--kunde-deaktiviert': theme.farben.deaktiviert,
    '--kunde-überschrift-schrift': theme.schrift.überschrift,
    '--kunde-text-schrift': theme.schrift.text,
    '--kunde-betont-schrift': theme.schrift.betont,
    '--kunde-spalten-abstand': theme.layout.spaltenAbstand,
    '--kunde-zeilen-abstand': theme.layout.zeilenAbstand,
    '--kunde-übergang': `${theme.animation.übergangsDauer} ${theme.animation.übergangsFunktion}`,
    // Feldgrößen-Variablen
    '--kunde-feld-kompakt-min': theme.feldgrößen.kompakt.minBreite,
    '--kunde-feld-kompakt-max': theme.feldgrößen.kompakt.maxBreite,
    '--kunde-feld-klein-min': theme.feldgrößen.klein.minBreite,
    '--kunde-feld-klein-max': theme.feldgrößen.klein.maxBreite,
    '--kunde-feld-mittel-min': theme.feldgrößen.mittel.minBreite,
    '--kunde-feld-mittel-max': theme.feldgrößen.mittel.maxBreite,
    '--kunde-feld-groß-min': theme.feldgrößen.groß.minBreite,
    '--kunde-feld-groß-max': theme.feldgrößen.groß.maxBreite,
  };
};
