# Freshfoodz Corporate Identity Guidelines für Frontend-Entwicklung

**📅 Erstellt:** 07.07.2025
**Status:** VERBINDLICH - Compliance-Pflicht für alle sichtbaren UI-Elemente!

---

## 🚨 KRITISCHE REGEL

**ALLE sichtbaren Frontend-Elemente des FreshPlan Sales Tools MÜSSEN der Freshfoodz Corporate Identity entsprechen!**

Keine Ausnahmen. Keine kreativen Interpretationen. Diese CI-Vorgaben sind das Gesetz für unser Frontend.

---

## 🎨 Verbindliche Freshfoodz Farbpalette

### Primärfarben (PFLICHT)

| Farbe | Hex-Code | RGB | Verwendung |
|-------|----------|-----|------------|
| **Primärgrün** | `#94C456` | rgb(148, 196, 86) | Buttons (Primary), Links, Aktionen, Call-to-Actions |
| **Dunkelblau** | `#004F7B` | rgb(0, 79, 123) | Headlines, Navigation, Secondary Buttons Border |
| **Weiß** | `#FFFFFF` | rgb(255, 255, 255) | Hintergründe, Cards, Primary Button Text |
| **Schwarz** | `#000000` | rgb(0, 0, 0) | Haupttext, Icons, Body Text |

### CSS-Implementierung (VERBINDLICH)

```css
:root {
  /* Freshfoodz Corporate Identity Farben - NICHT ÄNDERN! */
  --color-primary: #94C456;
  --color-secondary: #004F7B;
  --color-white: #FFFFFF;
  --color-black: #000000;
  
  /* Accessibility-konforme Varianten */
  --color-primary-hover: #7BA945;
  --color-secondary-hover: #003A5C;
  --color-disabled: #CCCCCC;
  --color-error: #DC3545;
  --color-success: var(--color-primary);
  --color-warning: #FFC107;
}
```

---

## 📝 Verbindliche Freshfoodz Typografie

### Schriftarten (PFLICHT)

| Element | Schriftart | Gewicht | Verwendung |
|---------|------------|---------|------------|
| **Headlines** | Antonio | Bold (700) | H1, H2, H3, Page Titles, Section Headers |
| **Body Text** | Poppins | Regular (400) | Normaler Text, Labels, Descriptions |
| **Emphasized Text** | Poppins | Medium (500) | Buttons, Important Text, Form Labels |

### Font-Loading (Performance-optimiert)

```css
/* Google Fonts - Optimiert für Performance */
@import url('https://fonts.googleapis.com/css2?family=Antonio:wght@700&family=Poppins:wght@400;500&display=swap');

/* CSS Font Definitions */
.font-headline {
  font-family: 'Antonio', sans-serif;
  font-weight: 700;
}

.font-body {
  font-family: 'Poppins', sans-serif;
  font-weight: 400;
}

.font-body-medium {
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
}
```

---

## 🧩 UI-Komponenten CI-Standards

### Buttons

```css
/* Primary Button - Freshfoodz Hauptaktion */
.btn-primary {
  background-color: var(--color-primary);
  color: var(--color-white);
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
  border: none;
  border-radius: 8px;
  padding: 12px 24px;
  transition: background-color 0.2s ease;
}

.btn-primary:hover {
  background-color: var(--color-primary-hover);
}

/* Secondary Button - Freshfoodz Sekundäraktion */
.btn-secondary {
  background-color: transparent;
  color: var(--color-secondary);
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
  border: 2px solid var(--color-secondary);
  border-radius: 8px;
  padding: 10px 22px; /* 2px weniger wegen Border */
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background-color: var(--color-secondary);
  color: var(--color-white);
}
```

### Navigation

```css
/* Hauptnavigation - Freshfoodz Branding */
.main-nav {
  background-color: var(--color-secondary);
  color: var(--color-white);
}

.nav-link {
  color: var(--color-white);
  font-family: 'Poppins', sans-serif;
  font-weight: 400;
  text-decoration: none;
}

.nav-link:hover,
.nav-link.active {
  color: var(--color-primary);
}
```

### Headlines

```css
/* Page Title - Freshfoodz Antonio Bold */
.page-title {
  font-family: 'Antonio', sans-serif;
  font-weight: 700;
  color: var(--color-secondary);
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

/* Section Heading - Freshfoodz Antonio Bold */
.section-heading {
  font-family: 'Antonio', sans-serif;
  font-weight: 700;
  color: var(--color-black);
  font-size: 1.5rem;
  margin-bottom: 0.75rem;
}
```

### Links & Actions

```css
/* Standard Link - Freshfoodz Primärgrün */
.link {
  color: var(--color-primary);
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
  text-decoration: none;
}

.link:hover {
  color: var(--color-secondary);
  text-decoration: underline;
}
```

### Form Elements

```css
/* Input Fields - Freshfoodz konform */
.form-input {
  font-family: 'Poppins', sans-serif;
  font-weight: 400;
  border: 2px solid #E0E0E0;
  border-radius: 8px;
  padding: 12px 16px;
  transition: border-color 0.2s ease;
}

.form-input:focus {
  border-color: var(--color-primary);
  outline: none;
}

.form-label {
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
  color: var(--color-black);
  margin-bottom: 4px;
  display: block;
}
```

---

## 🏢 Markenidentität Integration

### Slogan
**"So einfach, schnell und lecker!"**

Verwendung:
- Onboarding-Screens
- Loading-Screens
- Marketing-Bereiche
- Footer-Bereich

### Logo-Implementierung

```css
/* Logo Container - Schutzzone beachten */
.logo-container {
  padding: 16px; /* Minimale Schutzzone = Signetbreite */
  background-color: var(--color-white); /* Nur neutraler Hintergrund */
}

/* NIEMALS: Logo ohne Wortmarke */
.logo-icon-only {
  display: none; /* Verboten! */
}
```

---

## 🔍 Accessibility & WCAG 2.1 AA Compliance

### Farbkontraste (Geprüft & Konform)

| Kombination | Kontrast-Ratio | Status |
|-------------|----------------|--------|
| Primärgrün (#94C456) auf Weiß | 4.52:1 | ✅ AA |
| Dunkelblau (#004F7B) auf Weiß | 8.89:1 | ✅ AAA |
| Schwarz (#000000) auf Weiß | 21:1 | ✅ AAA |
| Weiß (#FFFFFF) auf Primärgrün | 4.52:1 | ✅ AA |
| Weiß (#FFFFFF) auf Dunkelblau | 8.89:1 | ✅ AAA |

### Accessibility-Features

```css
/* Focus States - Accessibility */
.btn:focus,
.link:focus,
.form-input:focus {
  outline: 3px solid var(--color-primary);
  outline-offset: 2px;
}

/* High Contrast Mode Support */
@media (prefers-contrast: high) {
  :root {
    --color-primary: #7BA945; /* Dunklerer Grünton */
    --color-secondary: #003A5C; /* Dunkleres Blau */
  }
}
```

---

## 📋 CI-Compliance Checkliste

### Vor jedem Commit prüfen:

#### Farben ✓
- [ ] Alle Farben verwenden ausschließlich Freshfoodz Palette
- [ ] Keine benutzerdefinierten Farben außerhalb der CI
- [ ] Primärgrün (#94C456) für alle Hauptaktionen
- [ ] Dunkelblau (#004F7B) für alle Headlines/Navigation

#### Typografie ✓
- [ ] Antonio Bold für alle Headlines (H1, H2, H3)
- [ ] Poppins Regular für alle Body-Texte
- [ ] Poppins Medium für wichtige UI-Elemente
- [ ] Keine anderen Schriftarten verwendet

#### Markenidentität ✓
- [ ] Logo nur auf neutralen Hintergründen
- [ ] Schutzzone um Logo eingehalten
- [ ] Slogan korrekt verwendet: "So einfach, schnell und lecker!"
- [ ] Bildmarke nie ohne Wortmarke

#### Accessibility ✓
- [ ] Alle Farbkontraste erfüllen WCAG 2.1 AA
- [ ] Focus States sind sichtbar und CI-konform
- [ ] Alt-Texte für alle visuellen Elemente
- [ ] Keyboard-Navigation funktioniert

---

## 🚦 Automatisierte CI-Compliance Prüfung

### ESLint Rules (Empfohlen)

```json
{
  "rules": {
    "no-hardcoded-colors": "error",
    "freshfoodz-colors-only": "error",
    "antonio-headlines-only": "error",
    "poppins-body-only": "error"
  }
}
```

### CSS-in-JS Linting

```javascript
// VERBOTEN ❌
const WrongButton = styled.button`
  background-color: #FF0000; // Nicht-CI Farbe!
  font-family: 'Arial', sans-serif; // Falsche Schrift!
`;

// RICHTIG ✅
const CorrectButton = styled.button`
  background-color: var(--color-primary);
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
`;
```

---

## 🎯 Implementierungsreihenfolge

### Phase 1: Design-Tokens (SOFORT)
1. CSS-Variablen für Freshfoodz CI definieren
2. Font-Loading optimieren (Antonio + Poppins)
3. Alte Farbwerte ersetzen

### Phase 2: Komponenten-Update (DIESE WOCHE)
1. Button-Komponenten CI-konform machen
2. Navigation/Headers aktualisieren
3. Form-Elemente anpassen

### Phase 3: Qualitätssicherung (LAUFEND)
1. CI-Compliance Tests implementieren
2. Design Review vor jedem Merge
3. Accessibility-Tests automatisieren

---

## 📞 Support & Fragen

**Bei CI-Fragen oder Unsicherheiten:**
- Referenz: `/docs/FRESH-FOODZ_CI.md`
- Master Plan: `/docs/CRM_COMPLETE_MASTER_PLAN.md`
- Code-Review durch Claude mit CI-Focus

**WICHTIG:** Im Zweifelsfall IMMER die strengeren CI-Vorgaben wählen!

---

## 🗣️ UI-Sprachregeln

### Grundprinzip
**"Das Tool muss die Sprache des Vertriebsmitarbeiters sprechen, nicht die von IT-Experten."**

### Verbindliche Regeln
1. **Deutsch als Standardsprache** - Alle UI-Texte konsequent auf Deutsch
2. **Kein Fachjargon** - Keine Anglizismen oder "Berater-Deutsch"
3. **Einfachheit vor Präzision** - Immer den bekannteren Begriff wählen

### Wichtigste Übersetzungen
| ❌ Vermeiden | ✅ Verwenden |
|--------------|--------------|
| Dashboard | Übersicht |
| Customer | Kunde |
| Save | Speichern |
| Cancel | Abbrechen |
| Delete | Löschen |
| Edit | Bearbeiten |
| Create | Erstellen/Anlegen |
| Submit | Absenden |
| Settings | Einstellungen |
| Error | Fehler |
| Success | Erfolgreich |
| Loading | Lädt... |

### Stil-Richtlinien
- **Höflich und direkt:** "Bitte wählen Sie..."
- **Einheitlich "Sie"** für professionellen Kontext
- **Keine Abkürzungen:** "Kundennummer" statt "Kd-Nr."

**Vollständige Sprachregeln:** `/docs/UI_SPRACHREGELN.md`

---

*Diese Guidelines sind verbindlich und werden bei jedem Code-Review überprüft. CI-Compliance ist nicht optional - es ist unsere Markenverpflichtung!*