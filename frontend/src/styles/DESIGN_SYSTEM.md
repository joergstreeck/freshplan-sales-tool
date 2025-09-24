# FreshPlan Design System

## 📌 Wichtiger Hinweis zur Implementierung

**Alle Typografie-Styles werden zentral über CSS-Klassen verwaltet!**

- Fonts werden automatisch über `typography.css` angewendet
- KEINE Inline-Styles für `fontFamily` verwenden
- Alle Komponenten erben die richtigen Fonts über ihre CSS-Klassen

## 🎨 Corporate Identity & Design-Prinzipien

### Markenidentität

**FreshFoodz** ist ein modernes, frisches Unternehmen im Bereich Lebensmittellieferung. Das Design spiegelt diese Werte wider:

- **Frisch & Modern**: Klare Linien, viel Weißraum
- **Vertrauenswürdig**: Professionelle Typografie, konsistente Farben
- **Benutzerfreundlich**: Intuitive Bedienung, klare Hierarchien

### Logo & Markenelemente

- **Logo**: FreshFoodz Logo (freshfoodzlogo.png)
- **Tagline**: "So einfach, schnell und lecker!"
- **FreshPlan**: Produktname für das B2B Sales Tool

### Farbpalette

#### Primärfarben

- **FreshFoodz Blau**: `#004F7B` (Primäre Markenfarbe)
  - Verwendung: Überschriften, wichtige UI-Elemente, Links
  - RGB: 0, 79, 123
  - Bedeutung: Vertrauen, Professionalität

- **FreshFoodz Grün**: `#94C456` (Sekundäre Markenfarbe)
  - Verwendung: CTAs, Erfolgs-States, Aktions-Buttons
  - RGB: 148, 196, 86
  - Bedeutung: Frische, Wachstum, Positivität

#### Sekundärfarben

- **Dunkelgrün**: `#7FB040` (Hover-States für Grün)
- **Hellgrün**: `#A8D89A` (Akzente)
- **Dunkelgrün Alt**: `#7FB069` (Variante)

#### Neutrale Farben

- **Schwarz**: `#000000` (nur für absoluten Kontrast)
- **Dunkelgrau**: `#333333` (Haupttext)
- **Mittelgrau**: `#666666` (Sekundärtext, Beschreibungen)
- **Hellgrau**: `#E0E0E0` (Borders, Linien)
- **Sehr Hellgrau**: `#F5F5F5` (Hintergründe)
- **Weiß**: `#FFFFFF` (Primäre Hintergrundfarbe)

#### Semantische Farben

- **Erfolg**: `#4CAF50` (Bestätigungen, positive Aktionen)
- **Warnung**: `#FF9800` (Warnungen, wichtige Hinweise)
- **Fehler**: `#F44336` (Fehler, kritische Zustände)
- **Info**: `#2196F3` (Informative Hinweise)

### Typografie

#### Schriftarten

- **Headline-Font**: `Antonio` (Google Fonts)
  - Gewichte: 400, 500, 600, 700
  - Verwendung: Alle Überschriften (h1-h6), Logo-Text
  - Charakter: Modern, kraftvoll, aufmerksamkeitsstark

- **Body-Font**: `Poppins` (Google Fonts)
  - Gewichte: 300, 400, 500, 600, 700
  - Verwendung: Fließtext, Labels, Buttons, UI-Elemente
  - Charakter: Freundlich, gut lesbar, modern

#### Schriftgrößen

```css
--font-size-xs: 0.75rem; /* 12px - Kleine Hinweise */
--font-size-sm: 0.875rem; /* 14px - Labels, Hilfetext */
--font-size-base: 1rem; /* 16px - Standard Text */
--font-size-lg: 1.125rem; /* 18px - Größerer Text */
--font-size-xl: 1.25rem; /* 20px - Kleinere Überschriften */
--font-size-2xl: 1.5rem; /* 24px - Seitentitel */
--font-size-3xl: 2rem; /* 32px - Hauptüberschriften */
--font-size-4xl: 2.5rem; /* 40px - Hero-Titel */
```

### Marken-Anwendung

#### Logo-Verwendung

- Mindestgröße: 120px Breite
- Schutzraum: Mindestens 20px zu anderen Elementen
- Platzierung: Immer oben links im Header
- Begleittext: "FreshPlan" in Antonio Bold

#### Sprache & Ton

- **Professionell aber freundlich**: "Sie" im B2B-Kontext
- **Klar und direkt**: Keine unnötigen Fachbegriffe
- **Hilfreich**: Immer lösungsorientiert
- **Positiv**: Fokus auf Vorteile und Möglichkeiten

## 📐 Layout & Spacing

### Container

```css
.customer-container {
  max-width: 1200px;
  margin: 0 auto;
}

.customer-form {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}
```

### Sections

```css
.form-section {
  margin-bottom: var(--spacing-sm); /* Reduziert von xl auf sm */
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--border-light);
}

.form-section:last-of-type {
  border-bottom: none;
}
```

### Grid System

```css
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 0; /* Kein zusätzlicher Abstand */
}

.form-group {
  display: flex;
  flex-direction: column;
}
```

## 🔤 Typography

### Überschriften

```css
/* Haupt-Seitentitel (h2) */
.section-title {
  font-family: 'Antonio', sans-serif;
  font-size: 1.5rem;
  font-weight: 700;
  color: #004f7b;
  margin-top: 2rem; /* Abstand zum Header */
  margin-bottom: 2rem;
}

/* Abschnitts-Titel (h3) */
.form-section-title {
  font-family: 'Antonio', sans-serif;
  font-size: 1.2rem;
  font-weight: 600;
  color: #004f7b;
  margin-bottom: 1rem;
}

/* Unter-Abschnitte (h4) */
h4 {
  font-family: 'Antonio', sans-serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: #004f7b;
  margin-top: 1.5rem;
  margin-bottom: 1rem;
}
```

**Wichtig**: Alle Typografie wird über CSS-Klassen gesteuert. Keine Inline-Styles verwenden!

### Labels & Text

```css
/* Form Labels */
label {
  font-family: 'Poppins', sans-serif;
  font-size: 0.875rem;
  font-weight: 500;
  color: #333333;
  margin-bottom: 0.5rem;
}

/* Die Font-Families werden automatisch über die typography.css angewendet */

/* Beschreibungstexte */
p {
  font-family: 'Poppins', sans-serif;
  font-size: 0.875rem;
  color: #666666;
  line-height: 1.5;
}

/* Pflichtfeld-Markierung */
label::after {
  content: '*';
  color: #f44336;
  margin-left: 2px;
  /* Nur bei required-Feldern anzeigen */
}
```

## 📝 Form Elements

### Input Fields

```css
input[type='text'],
input[type='number'],
input[type='email'],
input[type='tel'],
select,
textarea {
  padding: 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 1rem;
  font-family: 'Poppins', sans-serif;
  transition: all 0.3s;
  background: white;
}

/* Focus State */
input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: #94c456;
  box-shadow: 0 0 0 3px rgba(148, 196, 86, 0.1);
}

/* Readonly State */
input[readonly] {
  background-color: #f5f5f5;
  cursor: not-allowed;
}
```

### Checkboxes

```css
.checkbox-label {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

input[type='checkbox'] {
  width: 20px;
  height: 20px;
  cursor: pointer;
}

/* Checkbox-Beschreibung */
.checkbox-label + p {
  margin-left: 1.75rem; /* Einrückung unter Checkbox */
  margin-top: 0.5rem;
}
```

### Buttons

```css
.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-family: 'Poppins', sans-serif;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary {
  background: #94c456;
  color: white;
}

.btn-primary:hover {
  background: #7fb040;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(148, 196, 86, 0.3);
}

.btn-secondary {
  background: #f8f9fa;
  color: #333333;
  border: 1px solid #e0e0e0;
}
```

## 🎯 Spezielle Komponenten

### Location Service Groups

```css
.location-service-group {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.location-service-group input {
  width: 80px;
  flex-shrink: 0;
}

.location-service-group span {
  font-family: 'Poppins', sans-serif;
  font-size: 0.813rem;
  color: #666666;
}
```

### Alert Boxes

```css
.alert-box {
  background: rgba(255, 152, 0, 0.1);
  border: 1px solid rgba(255, 152, 0, 0.3);
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 2rem;
}

.alert-box h3 {
  color: #ff9800;
  margin-bottom: 0.5rem;
}
```

## 📱 Responsive Design

### Breakpoints

- Mobile: < 768px
- Tablet: 768px - 1024px
- Desktop: > 1024px

### Mobile Anpassungen

```css
@media (max-width: 768px) {
  .form-row {
    grid-template-columns: 1fr;
  }

  .customer-container {
    padding: 0 1rem;
  }

  .customer-form {
    padding: 1.5rem;
  }
}
```

## 🚀 Verwendung für neue Seiten

### Template für neue Form-Seite:

```tsx
import { useState } from 'react';
import '../../styles/legacy/forms.css';

export function NewPageForm() {
  return (
    <div className="customer-container">
      <h2 className="section-title">Seitentitel</h2>

      <div className="customer-form">
        {/* Abschnitt 1 */}
        <div className="form-section">
          <h3 className="form-section-title">Abschnittstitel</h3>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="fieldId">Feldname*</label>
              <input type="text" id="fieldId" name="fieldId" required />
            </div>
            {/* Weitere Felder... */}
          </div>
        </div>

        {/* Weitere Abschnitte... */}
      </div>
    </div>
  );
}
```

## ✅ Checkliste für neue Seiten

- [ ] Container mit `customer-container` Klasse
- [ ] Hauptüberschrift mit `section-title` Klasse (Antonio, 700)
- [ ] Form-Wrapper mit `customer-form` Klasse
- [ ] Sections mit `form-section` Klasse
- [ ] Section-Titel mit `form-section-title` (Antonio)
- [ ] Form-Rows mit 2-Spalten-Grid
- [ ] Labels mit Poppins-Font
- [ ] Konsistente Abstände eingehalten
- [ ] Mobile Responsive getestet
- [ ] Pflichtfelder mit \* markiert

## 🔧 CSS-Variablen

```css
:root {
  /* Farben */
  --primary-blue: #004f7b;
  --primary-green: #94c456;
  --dark-green: #7fb040;
  --text-dark: #333333;
  --text-light: #666666;
  --border-light: #e0e0e0;
  --bg-light: #f5f5f5;

  /* Spacing */
  --spacing-xs: 0.25rem;
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  --spacing-lg: 1.5rem;
  --spacing-xl: 2rem;

  /* Fonts */
  --font-heading: 'Antonio', sans-serif;
  --font-body: 'Poppins', sans-serif;
}
```

## 🎯 Best Practices für CI-Konformität

### Do's ✅

- Verwende IMMER die definierten Farben aus der Palette
- Nutze Antonio für ALLE Überschriften
- Nutze Poppins für ALLEN Fließtext
- Halte dich an die definierten Abstände
- Verwende semantische Farben für Status-Meldungen

### Don'ts ❌

- Keine eigenen Farben erfinden
- Keine anderen Schriftarten verwenden
- Keine Inline-Styles für Fonts
- Keine zu kleinen Schriftgrößen (< 12px)
- Keine zu geringen Kontraste

## 📊 Barrierefreiheit & Kontraste

### Mindest-Kontraste (WCAG 2.1 AA)

- **Normaler Text**: 4.5:1
- **Großer Text** (>18px): 3:1
- **UI-Komponenten**: 3:1

### Getestete Kombinationen

- ✅ Blau (#004F7B) auf Weiß: 8.59:1
- ✅ Grün (#94C456) auf Weiß: 2.51:1 (nur für große Texte!)
- ✅ Dunkelgrau (#333) auf Weiß: 12.63:1
- ✅ Weiß auf Blau (#004F7B): 8.59:1
- ✅ Weiß auf Grün (#94C456): 2.51:1 (nur für große Texte!)

---

**Wichtig**: Bei jeder neuen Seite diese Guidelines befolgen, um die Konsistenz zu wahren!
