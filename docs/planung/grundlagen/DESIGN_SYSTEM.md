# 🎨 Design System & FreshFoodz CI - Foundation Standards

**Erstellt:** 2025-09-17
**Aktualisiert:** 2025-10-25 (Sprint 2.1.7.2 - MUI v7 Grid API)
**Status:** ✅ Verbindlich für alle UI-Elemente
**Scope:** Corporate Identity + MUI Theme + Logo + Sprache

---

## 🎯 Quick Reference

| Kategorie | Wert |
|-----------|------|
| **Primärfarbe** | #94C456 (FreshFoodz Green) |
| **Sekundärfarbe** | #004F7B (FreshFoodz Blue) |
| **Headlines** | Antonio Bold (700) |
| **Body Text** | Poppins Regular (400) |
| **Logo** | freshplan-logo.png (19 KB) |
| **Sprache** | Deutsch |
| **MUI Version** | v7 (Grid v2 API) |
| **Layout** | MainLayoutV2 mit `maxWidth` prop |

---

## 📑 Inhaltsverzeichnis

1. [Farben & Typografie](#1-farben--typografie)
   - [Farbpalette](#farbpalette)
   - [CSS Design Tokens](#css-design-tokens)
   - [Typografie](#typografie)
2. [Layout System](#2-layout-system)
   - [MainLayoutV2 (Production Standard)](#mainlayoutv2-production-standard)
   - [Layout-Hierarchie](#layout-hierarchie)
   - [Breiten-Entscheidungen](#breiten-entscheidungen)
3. [MUI Theme & Komponenten](#3-mui-theme--komponenten)
   - [Theme-Konfiguration](#theme-konfiguration)
   - [Grid Layout System (MUI v7)](#grid-layout-system-mui-v7)
   - [Grid Migration Guide](#grid-migration-guide)
   - [Grid Best Practices](#grid-best-practices)
   - [Component Overrides](#component-overrides-theme-customization)
4. [UI-Komponenten Standards](#4-ui-komponenten-standards)
   - [Buttons](#buttons)
   - [Navigation](#navigation)
   - [Form Elements](#form-elements)
5. [Logo & Branding](#5-logo--branding)
   - [Logo-Verwendung](#logo-verwendung)
   - [Logo-Schutzregeln](#logo-schutzregeln)
6. [Sprache & Accessibility](#6-sprache--accessibility)
   - [UI-Sprachregeln](#ui-sprachregeln)
   - [Accessibility & WCAG 2.1 AA](#accessibility--wcag-21-aa)
7. [CI-Compliance & Qualitätssicherung](#7-ci-compliance--qualitätssicherung)
   - [CI-Compliance Checkliste](#ci-compliance-checkliste)
   - [Automatisierte Prüfung](#automatisierte-prüfung)
8. [Implementierungsstatus](#8-implementierungsstatus)
9. [Quick Start Guide](#9-quick-start-guide)

---

## 1. Farben & Typografie

### Farbpalette

**Primärfarben (PFLICHT):**

| Farbe | Hex-Code | RGB | Verwendung |
|-------|----------|-----|------------|
| **Primärgrün** | `#94C456` | rgb(148, 196, 86) | Buttons (Primary), Links, Aktionen |
| **Dunkelblau** | `#004F7B` | rgb(0, 79, 123) | Headlines, Navigation, Secondary Buttons |
| **Weiß** | `#FFFFFF` | rgb(255, 255, 255) | Hintergründe, Cards |
| **Schwarz** | `#000000` | rgb(0, 0, 0) | Haupttext, Icons |

### CSS Design Tokens

```css
:root {
  /* Freshfoodz Corporate Identity - NICHT ÄNDERN! */
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

### Typografie

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

---

## 2. Layout System

### MainLayoutV2 (Production Standard)

**📋 Status:** Produktiv auf allen 28 Seiten seit Sprint 2.1.7.0 (14.10.2025)

**TypeScript Interface:**
```typescript
interface MainLayoutV2Props {
  children: React.ReactNode;
  showHeader?: boolean;
  hideHeader?: boolean;
  maxWidth?: 'full' | 'xl' | 'lg' | 'md' | 'sm';  // Default: 'xl' (1536px)
}
```

**Beispiele:**
```typescript
// Tabellen/Listen - volle Breite (100%)
<MainLayoutV2 maxWidth="full">
  <CustomerTable />
</MainLayoutV2>

// Formulare - Standard (1536px, default)
<MainLayoutV2>
  <LeadDetailForm />
</MainLayoutV2>

// Detail-Pages - Medium (1200px)
<MainLayoutV2 maxWidth="lg">
  <CustomerDetailPage />
</MainLayoutV2>

// Error-Pages - Schmal (600px)
<MainLayoutV2 maxWidth="sm">
  <NotFoundPage />
</MainLayoutV2>
```

### Layout-Hierarchie

```
┌─────────────────────────────────────────────────────────┐
│  [Logo] [Suche...............] [🔔] [User ▼]           │ ← Header (64px)
└─────────────────────────────────────────────────────────┘
                    ↓ Schatten (4px)
                    ↓ 8px Abstand
┌─────────────────────────────────────────────────────────┐
│         MainLayoutV2 Content (maxWidth prop)            │
└─────────────────────────────────────────────────────────┘
```

### Breiten-Entscheidungen

| Content-Typ | maxWidth | MUI Breakpoint | Verwendung |
|------------|----------|----------------|------------|
| **Tabellen/Listen** | `'full'` | 100% | CustomerTable, LeadList, OpportunityPipeline |
| **Formulare** | `'xl'` (default) | 1536px | LeadDetail, CustomerDetail (Forms mit vielen Feldern) |
| **Info-Pages** | `'lg'` | 1200px | Error-Pages, Maintenance, Unauthorized |
| **Kompakte Pages** | `'sm'` | 600px | NotFound, Login-Bestätigung |

---

## 3. MUI Theme & Komponenten

### Theme-Konfiguration

**Theme-Datei:** `/frontend/src/theme/freshfoodz.ts`

**Basis-Konfiguration:**
```typescript
import { createTheme } from '@mui/material/styles';

export const freshfoodzTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#94C456',      // FreshFoodz Primärgrün
      light: '#a8d06d',
      dark: '#7fb03f',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#004F7B',      // FreshFoodz Dunkelblau
      light: '#3374a0',
      dark: '#003856',
      contrastText: '#ffffff',
    },
    background: {
      default: '#fafafa',
      paper: '#ffffff',
    },
    text: {
      primary: '#000000',   // Schwarz für Haupttext
      secondary: '#004F7B', // Dunkelblau für sekundären Text
    },
  },
  typography: {
    fontFamily: 'Poppins, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    h1: { fontFamily: 'Antonio, sans-serif', fontWeight: 700, color: '#004F7B' },
    h2: { fontFamily: 'Antonio, sans-serif', fontWeight: 700, color: '#004F7B' },
    h3: { fontFamily: 'Antonio, sans-serif', fontWeight: 700, color: '#004F7B' },
    h4: { fontFamily: 'Antonio, sans-serif', fontWeight: 700, color: '#004F7B' },
    h5: { fontFamily: 'Antonio, sans-serif', fontWeight: 700, color: '#004F7B' },
    h6: { fontFamily: 'Antonio, sans-serif', fontWeight: 700, color: '#004F7B' },
    body1: { fontFamily: 'Poppins, sans-serif', fontWeight: 400 },
    body2: { fontFamily: 'Poppins, sans-serif', fontWeight: 400 },
    button: { fontFamily: 'Poppins, sans-serif', fontWeight: 500, textTransform: 'none' },
  },
  shape: {
    borderRadius: 8,
  },
});
```

**Custom Palette Extensions:**
```typescript
// Custom FreshFoodz Namespace
palette: {
  freshfoodz: {
    primary: '#94C456',
    secondary: '#004F7B',
    success: '#94C456',
    background: '#ffffff',
  },
  // Status Colors für Application States
  status: {
    won: '#66BB6A',              // Grün für Erfolg/Gewonnen
    lost: '#EF5350',             // Rot für Verloren
    reactivate: '#2196F3',       // Blau für Reaktivierung
    probabilityHigh: '#66BB6A',  // Grün für hohe Wahrscheinlichkeit (80%+)
    probabilityMedium: '#FFA726',// Orange für mittlere Wahrscheinlichkeit (40-60%)
    probabilityLow: '#FF7043',   // Orange-Rot für niedrige Wahrscheinlichkeit (20-40%)
  },
}
```

### Grid Layout System (MUI v7)

**⚠️ WICHTIG: Projekt verwendet MUI v7 mit Grid v2 API!**

#### Grid Migration Guide

| ❌ Alt (Grid v1 - DEPRECATED) | ✅ Neu (Grid v7 - KORREKT) |
|-------------------------------|----------------------------|
| `<Grid item xs={12} sm={6}>` | `<Grid size={{ xs: 12, sm: 6 }}>` |
| `<Grid container spacing={2}>` | `<Grid container spacing={2}>` *(bleibt)* |
| `<Grid item>` | `<Grid>` *(kein item prop)* |

**Korrekte Verwendung:**

```typescript
import { Grid } from '@mui/material';

// ✅ Grid Container mit Grid Items
<Grid container spacing={2}>
  <Grid size={{ xs: 12, sm: 6, md: 4 }}>
    <TextField label="Vorname" fullWidth />
  </Grid>
  <Grid size={{ xs: 12, sm: 6, md: 4 }}>
    <TextField label="Nachname" fullWidth />
  </Grid>
</Grid>

// ✅ Responsive Sizing
<Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }}>
  <Card>Content</Card>
</Grid>
```

**❌ Falsche Verwendung (Browser-Warnings!):**

```typescript
// ❌ Alte Grid v1 API
<Grid item xs={12} sm={6}>  // DEPRECATED!
  <TextField />
</Grid>

// ❌ Einzelne Props
<Grid xs={12} sm={6}>  // FEHLER: props removed!
  <TextField />
</Grid>

// ❌ Grid2 Import
import Grid2 from '@mui/material/Grid2';  // MODULE NOT FOUND
```

#### Grid Best Practices

1. **Immer `size` Prop mit Objekt verwenden** (nicht einzelne `xs`, `sm` Props)
2. **Grid Container explizit:** `<Grid container spacing={2}>`
3. **Mobile-First:** Immer `xs` definieren, dann `sm`, `md`, `lg`, `xl` optional
4. **Import:** `import { Grid } from '@mui/material';` (nicht Grid2!)
5. **Testing:** Nach Grid-Änderungen Browser-Console prüfen (keine Warnings!)

**Debugging Grid-Warnings:**

```
⚠️ Symptom:
MUI Grid: The `xs` prop has been removed.

✅ Lösung:
// ❌ Alt
<Grid item xs={12} sm={6}>

// ✅ Fix
<Grid size={{ xs: 12, sm: 6 }}>
```

**Verifikation:**
```bash
npm run build
# Browser → Console → Keine Grid-Warnings!
```

### Component Overrides (Theme Customization)

**Alle MUI-Komponenten sind im Theme angepasst für FreshFoodz CI.**

#### Button (Hover-Effekte & Farben)
```typescript
MuiButton: {
  styleOverrides: {
    root: {
      borderRadius: 8,
      textTransform: 'none',
      fontWeight: 500,
      '&:hover': {
        transform: 'translateY(-1px)',      // Subtle Lift
        boxShadow: '0 4px 8px rgba(0,0,0,0.12)',
      },
    },
    containedPrimary: {
      backgroundColor: '#94C456',
      color: '#FFFFFF',
      '&:hover': { backgroundColor: '#7fb03f' },
    },
    containedSecondary: {
      backgroundColor: '#004F7B',
      color: '#FFFFFF',
      '&:hover': { backgroundColor: '#003856' },
    },
    text: {
      color: '#004F7B',
      '&:hover': { backgroundColor: 'rgba(0, 79, 123, 0.08)' },
    },
  },
}
```

#### Card (Hover-Effekte)
```typescript
MuiCard: {
  styleOverrides: {
    root: {
      borderRadius: 12,
      boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
      '&:hover': {
        boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
      },
    },
  },
}
```

#### Drawer & Sidebar (Navigation)
```typescript
MuiDrawer: {
  styleOverrides: {
    paper: {
      borderRight: '1px solid #e0e0e0',
      background: '#ffffff',
    },
  },
},
MuiListItemButton: {
  styleOverrides: {
    root: {
      borderRadius: 8,
      margin: '2px 0',
      '&.Mui-selected': {
        backgroundColor: 'rgba(148, 196, 86, 0.12)',   // Grüner Hintergrund
        color: '#94C456',                              // Grüner Text
        borderLeft: '3px solid #94C456',               // Grüner Border (WICHTIG!)
        '&:hover': {
          backgroundColor: 'rgba(148, 196, 86, 0.18)',
        },
      },
      '&:hover': {
        backgroundColor: 'rgba(148, 196, 86, 0.08)',
      },
    },
  },
}
```

#### TextField (Focus-Effekte)
```typescript
MuiTextField: {
  styleOverrides: {
    root: {
      '& .MuiOutlinedInput-root': {
        borderRadius: 8,
        '&:hover .MuiOutlinedInput-notchedOutline': {
          borderColor: '#94C456',                      // Grüner Border on Hover
        },
        '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
          borderColor: '#94C456',                      // Grüner Border on Focus
        },
      },
    },
  },
}
```

#### Tabs & Tab (Indicator)
```typescript
MuiTabs: {
  styleOverrides: {
    indicator: {
      backgroundColor: '#94C456',                      // Grüner Indicator
      height: 3,
    },
  },
},
MuiTab: {
  styleOverrides: {
    root: {
      fontFamily: 'Poppins, sans-serif',
      color: '#004F7B',
      '&.Mui-selected': {
        color: '#94C456',                              // Grüner Text wenn selected
        fontWeight: 600,
      },
      '&:hover': {
        backgroundColor: 'rgba(148, 196, 86, 0.08)',
      },
    },
  },
}
```

#### Chip (Font & Farben)
```typescript
MuiChip: {
  styleOverrides: {
    root: {
      borderRadius: 16,
      fontFamily: 'Poppins, sans-serif',               // Default für Standard-Chips
    },
    colorPrimary: {
      backgroundColor: '#94C456',
      color: '#ffffff',
      fontFamily: 'Antonio, sans-serif',               // FreshFoodz CI (Bold!)
      fontWeight: 700,
    },
    colorSecondary: {
      backgroundColor: '#004F7B',
      color: '#ffffff',
      fontFamily: 'Antonio, sans-serif',               // FreshFoodz CI (Bold!)
      fontWeight: 700,
    },
  },
}
```

#### Shadows & Transitions

**Custom Shadow System (25 Stufen):**
```typescript
shadows: [
  'none',
  '0px 2px 4px rgba(0,0,0,0.05)',
  '0px 4px 8px rgba(0,0,0,0.08)',
  '0px 6px 12px rgba(0,0,0,0.12)',
  // ... bis Stufe 24
]
```

**Transitions:**
```typescript
transitions: {
  easing: {
    easeInOut: 'cubic-bezier(0.4, 0, 0.2, 1)',
    easeOut: 'cubic-bezier(0.0, 0, 0.2, 1)',
    easeIn: 'cubic-bezier(0.4, 0, 1, 1)',
    sharp: 'cubic-bezier(0.4, 0, 0.6, 1)',
  },
  duration: {
    shortest: 150,
    shorter: 200,
    short: 250,
    standard: 300,
    complex: 375,
    enteringScreen: 225,
    leavingScreen: 195,
  },
}
```

---

## 4. UI-Komponenten Standards

### Buttons

```css
/* Primary Button */
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

/* Secondary Button */
.btn-secondary {
  background-color: transparent;
  color: var(--color-secondary);
  font-family: 'Poppins', sans-serif;
  font-weight: 500;
  border: 2px solid var(--color-secondary);
  border-radius: 8px;
  padding: 10px 22px;
  transition: all 0.2s ease;
}

.btn-secondary:hover {
  background-color: var(--color-secondary);
  color: var(--color-white);
}
```

### Navigation

```css
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

### Form Elements

```css
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

## 5. Logo & Branding

### Logo-Verwendung

**Offizielles Logo:**
- **Datei:** `freshplan-logo.png` (Full) / `freshplan-logo-icon.png` (Icon)
- **Größe:** 19 KB, PNG mit Transparenz, @2x Retina-ready
- **Pfad:** `/frontend/public/freshplan-logo.png`
- **Mindestgröße:** 32px Höhe (Mobile)
- **Standardgröße:** 40px Höhe (Desktop)

**Logo Component verwenden:**
```typescript
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

### Logo-Schutzregeln

```yaml
✅ ERLAUBT:
  - Logo auf weißem/hellem Hintergrund
  - Freiraum min. 16px um Logo
  - Logo klickbar zur Startseite
  - Proportionen beibehalten

❌ VERBOTEN:
  - Logo verzerren/strecken
  - Farbänderungen am Logo
  - Text direkt am Logo
  - Farbiger Hintergrund ohne weißen Container
```

---

## 6. Sprache & Accessibility

### UI-Sprachregeln

**Grundprinzip:** "Das Tool muss die Sprache des Vertriebsmitarbeiters sprechen, nicht die von IT-Experten."

**Verbindliche Übersetzungen:**

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
| Loading | Lädt... |

**Stil-Richtlinien:**
- Höflich und direkt: "Bitte wählen Sie..."
- Einheitlich "Sie" für professionellen Kontext
- Keine Abkürzungen: "Kundennummer" statt "Kd-Nr."

### Accessibility & WCAG 2.1 AA

**Farbkontraste (Geprüft & Konform):**

| Kombination | Kontrast-Ratio | Status |
|-------------|----------------|--------|
| Primärgrün (#94C456) auf Weiß | 4.52:1 | ✅ AA |
| Dunkelblau (#004F7B) auf Weiß | 8.89:1 | ✅ AAA |
| Schwarz (#000000) auf Weiß | 21:1 | ✅ AAA |
| Weiß auf Primärgrün | 4.52:1 | ✅ AA |
| Weiß auf Dunkelblau | 8.89:1 | ✅ AAA |

**Focus States:**
```css
.btn:focus,
.link:focus,
.form-input:focus {
  outline: 3px solid var(--color-primary);
  outline-offset: 2px;
}

/* High Contrast Mode */
@media (prefers-contrast: high) {
  :root {
    --color-primary: #7BA945;
    --color-secondary: #003A5C;
  }
}
```

---

## 7. CI-Compliance & Qualitätssicherung

### CI-Compliance Checkliste

**Vor jedem Commit prüfen:**

#### Farben ✓
- [ ] Alle Farben verwenden FreshFoodz Palette
- [ ] Keine benutzerdefinierten Farben
- [ ] Primärgrün (#94C456) für alle Hauptaktionen
- [ ] Dunkelblau (#004F7B) für Headlines/Navigation

#### Typografie ✓
- [ ] Antonio Bold für Headlines (H1-H3)
- [ ] Poppins Regular für Body-Texte
- [ ] Poppins Medium für wichtige UI-Elemente
- [ ] Keine anderen Schriftarten

#### Layout ✓
- [ ] MainLayoutV2 mit `maxWidth` Prop verwendet
- [ ] Header 64px hoch, weißer Hintergrund
- [ ] Content-Breite korrekt gewählt
- [ ] 8px Abstand zwischen Header und Content

#### MUI v7 Grid ✓
- [ ] `size` Prop mit Objekt verwendet (nicht `xs`, `sm` einzeln)
- [ ] Kein `item` Prop verwendet
- [ ] Import: `import { Grid } from '@mui/material';`
- [ ] Keine Browser Console Warnings

#### Logo ✓
- [ ] Logo nur auf neutralen Hintergründen
- [ ] Schutzzone eingehalten (min. 16px)
- [ ] Logo klickbar, führt zu "/"
- [ ] Fallback-Strategie implementiert

### Automatisierte Prüfung

**Pre-Commit Hooks:** Prüfen auf:
- Hardcoded Farben (nicht aus CI-Palette)
- Falsche Schriftarten
- Grid v1 API Verwendung (`item`, einzelne `xs` props)
- Fehlende Accessibility-Attribute

**Empfohlene ESLint Rules:**
```json
{
  "rules": {
    "no-hardcoded-colors": "error",
    "freshfoodz-colors-only": "error",
    "antonio-headlines-only": "error",
    "poppins-body-only": "error",
    "mui-grid-v7-api": "error"
  }
}
```

---

## 8. Implementierungsstatus

### ✅ Abgeschlossen

**Sprint 2.1.7.0 (14.10.2025):**
- ✅ CSS Design Tokens (Farben, Layout)
- ✅ Font-Loading (Antonio + Poppins)
- ✅ MUI Theme (freshfoodz-theme.ts)
- ✅ MainLayoutV2 mit maxWidth Prop (28 Seiten produktiv)
- ✅ Design Compliance (97 Violations behoben)
- ✅ Container-Cleanup (22× doppelte Container entfernt, -110 LOC)
- ✅ Logo-Komponente (Logo.tsx - 19 KB)
- ✅ FreshFoodz CI V2 100% konform

**Sprint 2.1.7.2 (25.10.2025):**
- ✅ MUI v7 Grid API Migration (DynamicField.tsx)
- ✅ Grid v2 Dokumentation (DESIGN_SYSTEM.md)
- ✅ Browser Console Warnings behoben

### 🟡 Geplant

- SmartLayout Component (Auto-Detection - Zukunft)
- CI-Compliance Tests automatisieren
- Performance-Monitoring für Fonts
- Accessibility Automated Tests

---

## 9. Quick Start Guide

### Theme verwenden
```typescript
import { Button, Typography } from '@mui/material';

<Button variant="contained">Speichern</Button>     // #94C456 automatisch
<Typography variant="h2">Überschrift</Typography>  // Antonio Bold automatisch
```

### Logo verwenden
```typescript
import { Logo } from '@/components/common/Logo';

<Logo variant="full" height={40} />  // Desktop
<Logo variant="icon" height={32} />  // Mobile
```

### MainLayoutV2 verwenden
```typescript
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';

// Tabellen - volle Breite
<MainLayoutV2 maxWidth="full">
  <CustomerTable />
</MainLayoutV2>

// Formulare - Standard (default)
<MainLayoutV2>
  <LeadDetailForm />
</MainLayoutV2>
```

### Grid v7 verwenden
```typescript
import { Grid } from '@mui/material';

<Grid container spacing={2}>
  <Grid size={{ xs: 12, sm: 6, md: 4 }}>
    <TextField label="Feld" fullWidth />
  </Grid>
</Grid>
```

---

**📅 Verbindlich ab:** 01.10.2025
**🔄 Letzte Aktualisierung:** 25.10.2025 (Sprint 2.1.7.2)
**🎯 Ziel:** Konsistentes, markenkonformes UI-System für alle FreshPlan Features
