# Frontend-Internationalisierung (i18n) - Detaillierter Implementierungsplan

**Datum:** 2025-06-29
**Typ:** PROPOSAL
**Status:** Zur Umsetzung bereit
**Geschätzter Aufwand:** 1.5-2 Tage

## 🎯 Ziel

Das FreshPlan Sales Tool soll vollständig zweisprachig (Deutsch/Englisch) werden, mit:
- Instant-Sprachumschaltung ohne Reload
- Korrekte Zahlen- und Datumsformatierung
- Übersetzung aller statischen und dynamischen Texte
- Zukunftssichere Architektur für weitere Sprachen

## 🛠️ Technologie-Stack

### Hauptbibliothek: react-i18next
```bash
npm install react-i18next i18next i18next-browser-languagedetector
```

**Warum react-i18next?**
- ✅ De-facto Standard für React
- ✅ Hervorragende TypeScript-Unterstützung  
- ✅ React Hooks Integration
- ✅ Lazy Loading & Code-Splitting
- ✅ Pluralisierung & Interpolation
- ✅ Namespace-Support für Modularität

## 📁 Dateistruktur

```
frontend/src/
├── i18n/
│   ├── index.ts                    # i18n Konfiguration & Setup
│   ├── types.ts                    # TypeScript Definitionen
│   ├── hooks.ts                    # Custom Hooks (useTranslation wrapper)
│   └── locales/
│       ├── de/
│       │   ├── common.json         # Allgemeine UI-Texte
│       │   ├── calculator.json     # Rabattrechner-Texte
│       │   ├── customers.json      # Kundenverwaltung-Texte
│       │   ├── navigation.json     # Navigation & Menüs
│       │   └── errors.json         # Fehlermeldungen
│       └── en/
│           ├── common.json
│           ├── calculator.json
│           ├── customers.json
│           ├── navigation.json
│           └── errors.json
```

## 🔧 Implementierungs-Phasen

### Phase 1: Setup & Infrastruktur (2-3 Stunden)

#### 1.1 Installation & Basis-Konfiguration
```typescript
// src/i18n/index.ts
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// Import der Übersetzungen
import deCommon from './locales/de/common.json';
import enCommon from './locales/en/common.json';
// ... weitere Imports

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    fallbackLng: 'de',
    debug: import.meta.env.DEV,
    interpolation: {
      escapeValue: false
    },
    resources: {
      de: {
        common: deCommon,
        calculator: deCalculator,
        // ...
      },
      en: {
        common: enCommon,
        calculator: enCalculator,
        // ...
      }
    }
  });
```

#### 1.2 TypeScript-Integration
```typescript
// src/i18n/types.ts
import 'react-i18next';
import type common from './locales/de/common.json';
import type calculator from './locales/de/calculator.json';

declare module 'react-i18next' {
  interface CustomTypeOptions {
    defaultNS: 'common';
    resources: {
      common: typeof common;
      calculator: typeof calculator;
      // weitere Namespaces
    };
  }
}
```

#### 1.3 App-Integration
```typescript
// src/main.tsx
import './i18n'; // Import vor App!
import { Suspense } from 'react';

root.render(
  <React.StrictMode>
    <Suspense fallback={<div>Loading...</div>}>
      <App />
    </Suspense>
  </React.StrictMode>
);
```

### Phase 2: Content-Extraktion & Übersetzung (4-5 Stunden)

#### 2.1 Übersetzungs-Dateien erstellen

**common.json (Beispiel):**
```json
{
  "buttons": {
    "save": "Speichern",
    "cancel": "Abbrechen",
    "delete": "Löschen",
    "edit": "Bearbeiten",
    "back": "Zurück",
    "next": "Weiter"
  },
  "labels": {
    "name": "Name",
    "email": "E-Mail",
    "phone": "Telefon",
    "address": "Adresse",
    "date": "Datum"
  },
  "messages": {
    "loading": "Wird geladen...",
    "success": "Erfolgreich gespeichert",
    "error": "Ein Fehler ist aufgetreten"
  }
}
```

**calculator.json (Beispiel):**
```json
{
  "title": "FreshPlan Rabattrechner",
  "sliders": {
    "orderValue": "Bestellwert",
    "leadTime": "Vorlaufzeit",
    "leadTimeDays": "{{days}} Tage"
  },
  "checkboxes": {
    "pickup": "Abholung (Mindestbestellwert: {{minValue}} netto)"
  },
  "results": {
    "baseDiscount": "Basisrabatt",
    "earlyBooking": "Frühbucher",
    "pickupDiscount": "Abholung",
    "totalDiscount": "Gesamtrabatt",
    "savings": "Ersparnis",
    "finalPrice": "Endpreis"
  },
  "scenarios": {
    "hotel": "Hotelkette",
    "clinic": "Klinikgruppe",
    "restaurant": "Restaurant"
  },
  "rules": {
    "maxDiscount": "Maximaler Gesamtrabatt: {{max}}%"
  }
}
```

#### 2.2 Komponenten-Migration

**Vorher:**
```tsx
<h2 className="section-title">FreshPlan Rabattrechner</h2>
<label htmlFor="orderValue">Bestellwert</label>
```

**Nachher:**
```tsx
import { useTranslation } from 'react-i18next';

function CalculatorLayout() {
  const { t } = useTranslation('calculator');
  
  return (
    <>
      <h2 className="section-title">{t('title')}</h2>
      <label htmlFor="orderValue">{t('sliders.orderValue')}</label>
    </>
  );
}
```

#### 2.3 Dynamische Inhalte

**Problem: Dynamische Tab-Namen**
```tsx
// Aktuell
const tabOptions = ['Abholung', 'Lieferung', 'Beides'];

// Mit i18n
const tabOptions = [
  t('calculator.delivery.pickup'),
  t('calculator.delivery.delivery'),
  t('calculator.delivery.both')
];
```

**Problem: Formatierte Werte**
```tsx
// Aktuell
<span>{leadTime} Tage</span>

// Mit i18n
<span>{t('sliders.leadTimeDays', { days: leadTime })}</span>
```

### Phase 3: UI-Integration (2-3 Stunden)

#### 3.1 Sprachumschalter-Komponente
```tsx
// src/components/LanguageSwitch.tsx
import { useTranslation } from 'react-i18next';

export function LanguageSwitch() {
  const { i18n } = useTranslation();
  
  const toggleLanguage = () => {
    const newLang = i18n.language === 'de' ? 'en' : 'de';
    i18n.changeLanguage(newLang);
  };
  
  return (
    <button 
      onClick={toggleLanguage}
      className="language-switch"
      aria-label="Sprache wechseln"
    >
      {i18n.language === 'de' ? '🇬🇧 EN' : '🇩🇪 DE'}
    </button>
  );
}
```

#### 3.2 Header-Integration
```tsx
// In Header-Komponente
<div className="header-actions">
  <LanguageSwitch />
  <UserMenu />
</div>
```

#### 3.3 Zahlen- und Datumsformatierung
```tsx
// src/i18n/formatters.ts
export function formatCurrency(value: number, locale: string): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value);
}

export function formatDate(date: Date, locale: string): string {
  return new Intl.DateTimeFormat(locale, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  }).format(date);
}

// Verwendung
const { i18n } = useTranslation();
const formattedPrice = formatCurrency(1500, i18n.language);
// DE: "1.500,00 €"
// EN: "€1,500.00"
```

### Phase 4: Testing & Qualitätssicherung (2 Stunden)

#### 4.1 Unit-Tests
```typescript
// src/i18n/__tests__/translations.test.ts
describe('Translation Keys', () => {
  test('all German keys have English translations', () => {
    const deKeys = getAllKeys(deTranslations);
    const enKeys = getAllKeys(enTranslations);
    
    expect(enKeys).toEqual(deKeys);
  });
  
  test('no missing interpolation variables', () => {
    // Prüft dass {{variables}} in beiden Sprachen gleich sind
  });
});
```

#### 4.2 Visual Regression Tests
- Screenshots in beiden Sprachen
- Prüfung auf Text-Überlauf
- Button-Größen-Anpassung

#### 4.3 Checkliste für Review
- [ ] Alle Texte übersetzt
- [ ] Keine hartcodierten Strings
- [ ] Zahlenformate korrekt
- [ ] Datumsformate korrekt
- [ ] UI-Layout stabil
- [ ] Performance unverändert

## 🎯 Definition of Done

1. **Funktional**
   - [ ] Alle UI-Texte in DE/EN verfügbar
   - [ ] Sprachumschaltung funktioniert instant
   - [ ] Gewählte Sprache wird gespeichert (localStorage)
   - [ ] Browser-Sprache wird erkannt

2. **Technisch**
   - [ ] TypeScript-Typen für alle Keys
   - [ ] Keine Konsolen-Warnungen
   - [ ] Bundle-Size < 10KB Overhead
   - [ ] Code-Splitting funktioniert

3. **Qualität**
   - [ ] 100% Test-Coverage für Formatter
   - [ ] Alle Komponenten-Tests grün
   - [ ] Keine visuellen Regressionen
   - [ ] Code Review bestanden

## 🚨 Besondere Herausforderungen

### 1. Dynamische Tab-Namen
**Problem:** Tab-Namen werden aus Daten generiert
**Lösung:** Mapping-Funktion für bekannte Werte

### 2. Tooltips & Aria-Labels
**Problem:** Oft übersehen bei Übersetzungen
**Lösung:** Systematische Suche nach `title=`, `aria-label=`, `alt=`

### 3. Pluralisierung
**Problem:** "1 Tag" vs "2 Tage"
**Lösung:** i18next Plural-Rules nutzen
```json
{
  "days_one": "{{count}} Tag",
  "days_other": "{{count}} Tage"
}
```

### 4. SEO & Meta-Tags
**Problem:** HTML-Head muss auch übersetzt werden
**Lösung:** react-helmet-async Integration

## 📊 Aufwandsschätzung Final

| Phase | Aufwand | Priorität |
|-------|---------|-----------|
| Setup & Infrastruktur | 2-3h | Hoch |
| Content-Extraktion | 4-5h | Hoch |
| UI-Integration | 2-3h | Mittel |
| Testing | 2h | Mittel |
| **Gesamt** | **10-13h** | - |

## 🚀 Nächste Schritte nach Compact

1. Neue Branch erstellen: `feature/i18n-implementation`
2. Dependencies installieren
3. Basis-Setup implementieren
4. Schrittweise Komponenten migrieren
5. Tests schreiben
6. Review & Merge

---

**Bereit für Compact und anschließende Implementierung!**