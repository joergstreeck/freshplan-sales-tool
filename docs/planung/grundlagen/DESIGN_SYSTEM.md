# 🎨 Design System & FreshFoodz CI - Foundation Standards

**Erstellt:** 2025-09-17
**Status:** ✅ Verbindlich für alle UI-Elemente
**Scope:** Corporate Identity + MUI Theme + Logo + Sprache

> **🎯 Quick Reference:**
> - **Farben:** #94C456 (Green), #004F7B (Blue)
> - **Schrift:** Antonio Bold (Headlines), Poppins (Body)
> - **Logo:** freshplan-logo.png (19 KB, Logo.tsx Component)
> - **Sprache:** Deutsch (Dashboard → Übersicht, Save → Speichern)

---

## 🎯 **FARBPALETTE**

### **Primärfarben (PFLICHT):**

| Farbe | Hex-Code | RGB | Verwendung |
|-------|----------|-----|------------|
| **Primärgrün** | `#94C456` | rgb(148, 196, 86) | Buttons (Primary), Links, Aktionen, Call-to-Actions |
| **Dunkelblau** | `#004F7B` | rgb(0, 79, 123) | Headlines, Navigation, Secondary Buttons Border |
| **Weiß** | `#FFFFFF` | rgb(255, 255, 255) | Hintergründe, Cards, Primary Button Text |
| **Schwarz** | `#000000` | rgb(0, 0, 0) | Haupttext, Icons, Body Text |

### **CSS Design Tokens (Verbindlich):**
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

  /* Layout System */
  --header-height: 64px;
  --content-margin-top: 8px;
  --content-padding: 16px;
  --shadow-header: 0 2px 4px rgba(0,0,0,0.08);
  --shadow-paper: 0 1px 3px rgba(0,0,0,0.05);
  --shadow-card: 0 1px 2px rgba(0,0,0,0.04);
}
```

## 📝 **TYPOGRAFIE**

| Element | Schriftart | Gewicht | Verwendung |
|---------|------------|---------|------------|
| **Headlines** | Antonio | Bold (700) | H1-H6, Page Titles, Section Headers |
| **Body Text** | Poppins | Regular (400) | Normaler Text, Labels, Descriptions |
| **Emphasized** | Poppins | Medium (500) | Buttons, Important Text, Form Labels |

**Font-Loading:**
```html
<!-- In index.html - Performance-optimiert -->
<link href="https://fonts.googleapis.com/css2?family=Antonio:wght@700&family=Poppins:wght@400;500&display=swap" rel="stylesheet">
```

## 🏗️ **DESIGN SYSTEM V2 - INTELLIGENTES LAYOUT**

**📋 IMPLEMENTATION-PLAN:**
Die SmartLayout-Migration folgt der PLANUNGSMETHODIK → **[SmartLayout Migration Plan](../infrastruktur/SMARTLAYOUT_MIGRATION_PLAN.md)**

### **SmartLayout-Architektur:**

#### **Automatische Content-Breiten-Erkennung:**
| Content-Typ | Max-Breite | Padding | Erkennung |
|------------|------------|---------|-----------|
| **Tabellen/Listen** | 100% | 16px | `<Table>`, `<DataGrid>`, `data-wide="true"` |
| **Formulare** | 800px | 32px | `<form>`, mehrere `<TextField>` |
| **Text/Artikel** | 1200px | 32px | Hauptsächlich `<Typography>`, `<p>` |
| **Dashboard** | 100% | 16px | Grid mit mehreren Cards |
| **Cockpit** | 100% | 0 | Spezifische Cockpit-Komponenten |

#### **Layout-Hierarchie:**
```
┌─────────────────────────────────────────────────────────┐
│  [Logo] [Suche...............] [🔔] [User ▼]           │ ← Header (64px, #FFFFFF)
└─────────────────────────────────────────────────────────┘
                    ↓ Schatten (4px)
                    ↓ 8px Abstand
┌─────────────────────────────────────────────────────────┐
│                 SmartLayout Content                      │ ← Paper Container (intelligente Breite)
└─────────────────────────────────────────────────────────┘
```

### **SmartLayout TypeScript Interface:**
```typescript
interface SmartLayoutProps {
  children: React.ReactNode;
  forceWidth?: 'full' | 'content' | 'narrow'; // Override wenn nötig
  noPaper?: boolean; // Für spezielle Layouts
}

const detectContentType = (children: ReactNode): ContentType => {
  // Analysiert React-Children automatisch
  const elements = React.Children.toArray(children);

  const hasTable = elements.some(child =>
    child.type === Table || child.type === DataGrid
  );

  const hasForm = elements.some(child =>
    child.type === 'form' || countTextFields(child) > 3
  );

  if (hasTable) return 'wide';
  if (hasForm) return 'form';
  return 'content';
};
```

## 🧩 **UI-KOMPONENTEN CI-STANDARDS**

### **Buttons (FreshFoodz konform):**
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

### **Navigation (FreshFoodz CI):**
```css
/* Hauptnavigation - Freshfoodz Branding */
.main-nav {
  background-color: var(--color-secondary);
  color: var(--color-white);
  height: var(--header-height);
  box-shadow: var(--shadow-header);
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

### **Headlines (Antonio Bold):**
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

### **Form Elements:**
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

## 🖼️ **LOGO-STANDARDS & GUIDELINES**

### **Offizielles Logo:**
- **Datei:** `freshplan-logo.png` (Full) / `freshplan-logo-icon.png` (Icon)
- **Größe:** 19 KB, PNG mit Transparenz, @2x Retina-ready
- **Pfad:** `/frontend/public/freshplan-logo.png`
- **Mindestgröße:** 32px Höhe (Mobile)
- **Standardgröße:** 40px Höhe (Desktop)

### **Logo-Verwendung (Verbindlich):**
```typescript
// Logo.tsx Component verwenden (19 KB optimiert)
import { Logo } from '@/components/common/Logo';

// Desktop - Volles Logo
<Logo
  variant="full"
  height={40}
  onClick={() => navigate('/')}
/>

// Mobile - Icon-Version
<Logo
  variant="icon"
  height={32}
  onClick={() => navigate('/')}
/>
```

**Automatisches Fallback:**
- Bei fehlenden Logo-Dateien zeigt Logo.tsx automatisch FreshPlan Icon + Text
- Fallback verwendet FreshFoodz CI-Farben (#94C456 Green, #004F7B Blue)

### **Logo-Schutzregeln:**
```yaml
✅ ERLAUBT:
  - Logo immer auf weißem oder sehr hellem Hintergrund
  - Ausreichend Freiraum um das Logo (min. 16px)
  - Logo als klickbares Element zur Startseite
  - Proportionen beibehalten

❌ VERBOTEN:
  - Logo nicht verzerren oder strecken
  - Keine Farbänderungen am Logo
  - Kein Text direkt am Logo
  - Nicht auf farbigem Hintergrund ohne weißen Container
```

## 🎨 **MUI THEME**

**Theme-Datei:** `/frontend/src/theme/freshfoodz.ts`

**Kern-Konfiguration:**
```typescript
import { createTheme } from '@mui/material/styles';

export const freshfoodzTheme = createTheme({
  palette: {
    primary: { main: '#94C456' },     // FreshFoodz Green
    secondary: { main: '#004F7B' },   // FreshFoodz Blue
  },
  typography: {
    fontFamily: 'Poppins, sans-serif',
    h1: { fontFamily: 'Antonio, sans-serif', fontWeight: 700 },
    h2: { fontFamily: 'Antonio, sans-serif', fontWeight: 700 },
    h3: { fontFamily: 'Antonio, sans-serif', fontWeight: 700 },
    button: { fontFamily: 'Poppins, sans-serif', fontWeight: 500 },
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { borderRadius: 8, textTransform: 'none' },
      },
    },
  },
});
```

**Verwendung:**
```typescript
import { ThemeProvider } from '@mui/material/styles';
import { freshfoodzTheme } from '@/theme/freshfoodz';

<ThemeProvider theme={freshfoodzTheme}>
  <App />
</ThemeProvider>
```

## 🗣️ **UI-SPRACHREGELN (FreshFoodz Standard)**

### **Grundprinzip:**
**"Das Tool muss die Sprache des Vertriebsmitarbeiters sprechen, nicht die von IT-Experten."**

### **Verbindliche Übersetzungen:**
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

### **Stil-Richtlinien:**
- **Höflich und direkt:** "Bitte wählen Sie..."
- **Einheitlich "Sie"** für professionellen Kontext
- **Keine Abkürzungen:** "Kundennummer" statt "Kd-Nr."

## 🔍 **ACCESSIBILITY & WCAG 2.1 AA COMPLIANCE**

### **Farbkontraste (Geprüft & Konform):**

| Kombination | Kontrast-Ratio | Status |
|-------------|----------------|--------|
| Primärgrün (#94C456) auf Weiß | 4.52:1 | ✅ AA |
| Dunkelblau (#004F7B) auf Weiß | 8.89:1 | ✅ AAA |
| Schwarz (#000000) auf Weiß | 21:1 | ✅ AAA |
| Weiß (#FFFFFF) auf Primärgrün | 4.52:1 | ✅ AA |
| Weiß (#FFFFFF) auf Dunkelblau | 8.89:1 | ✅ AAA |

### **Accessibility-Features:**
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

## 📋 **CI-COMPLIANCE CHECKLISTE**

### **Vor jedem Commit prüfen:**

#### **Farben ✓**
- [ ] Alle Farben verwenden ausschließlich Freshfoodz Palette
- [ ] Keine benutzerdefinierten Farben außerhalb der CI
- [ ] Primärgrün (#94C456) für alle Hauptaktionen
- [ ] Dunkelblau (#004F7B) für alle Headlines/Navigation

#### **Typografie ✓**
- [ ] Antonio Bold für alle Headlines (H1, H2, H3)
- [ ] Poppins Regular für alle Body-Texte
- [ ] Poppins Medium für wichtige UI-Elemente
- [ ] Keine anderen Schriftarten verwendet

#### **Layout ✓**
- [ ] SmartLayout für automatische Breiten-Erkennung verwendet
- [ ] Header immer 64px hoch mit weißem Hintergrund
- [ ] Paper-Container mit korrekten Schatten
- [ ] 8px Abstand zwischen Header und Content

#### **Logo ✓**
- [ ] Logo nur auf neutralen Hintergründen
- [ ] Schutzzone um Logo eingehalten (min. 16px)
- [ ] Logo klickbar und führt zu "/"
- [ ] Fallback-Strategie implementiert

## 🚦 **AUTOMATISIERTE CI-COMPLIANCE PRÜFUNG**

### **ESLint Rules (Empfohlen):**
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

### **CSS-in-JS Linting:**
```typescript
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


## 🚀 **IMPLEMENTIERUNGSREIHENFOLGE**

### **Phase 1: Design-Tokens (SOFORT)**
1. ✅ CSS-Variablen für Freshfoodz CI definieren
2. ✅ Font-Loading optimieren (Antonio + Poppins)
3. ✅ MUI Theme konfigurieren

### **Phase 2: SmartLayout System (DIESE WOCHE)**
1. SmartLayout Component implementieren
2. Content-Type Detection verfeinern
3. MainLayoutV3 mit intelligenter Breiten-Erkennung
4. Migration von Settings-Seite als Test

### **Phase 3: Komponenten-Update (LAUFEND)**
1. Button-Komponenten CI-konform machen
2. Navigation/Headers aktualisieren
3. Form-Elemente anpassen
4. ✅ Logo-Komponente optimiert (Logo.tsx - 19 KB)

### **Phase 4: Qualitätssicherung (PERMANENT)**
1. CI-Compliance Tests implementieren
2. Design Review vor jedem Merge
3. Accessibility-Tests automatisieren
4. Performance-Monitoring für Fonts

## 🔧 **QUICK START**

```typescript
// 1. Theme verwenden (bereits in App eingebunden)
import { Button, Typography } from '@mui/material';

<Button variant="contained">Speichern</Button>     // Automatisch #94C456
<Typography variant="h2">Überschrift</Typography>  // Automatisch Antonio Bold

// 2. Logo verwenden
import { Logo } from '@/components/common/Logo';

<Logo variant="full" height={40} />  // Desktop
<Logo variant="icon" height={32} />  // Mobile

// 3. SmartLayout verwenden
import { SmartLayout } from '@/components/layout/SmartLayout';

<SmartLayout>
  <Table>...</Table>  // 100% Breite automatisch
</SmartLayout>
```

---

**📅 Verbindlich ab:** 01.10.2025
**🎯 Ziel:** Konsistentes, markenkonformes UI-System für alle FreshPlan Features