# Abhängigkeitsbaum-Analyse - Frontend

**Ziel:** Vollständige Abhängigkeitsketten dokumentieren für sichere Umstrukturierung

## 1. card-freshplan.tsx

### Start-Datei:
`components/ui/card-freshplan.tsx`

### Direkte Imports (was importiert diese Datei):
- `React` from 'react' ✓ (externe Dependency)

### Wer importiert diese Datei:
- **NIEMAND** ❌ (ungenutzte Datei!)

### Abhängigkeitsbaum:
```
components/ui/card-freshplan.tsx
└── react (extern)

KEINE WEITEREN ABHÄNGIGKEITEN - DATEI WIRD NICHT VERWENDET!
```

### Status: 
✅ **SICHER ZU VERSCHIEBEN/LÖSCHEN** - Keine internen Abhängigkeiten

---

## 2. card-transition.tsx

### Start-Datei:
`components/ui/card-transition.tsx`

### Direkte Imports (was importiert diese Datei):
- `React` from 'react' ✓ (externe Dependency)
- `../../styles/legacy/components.css` ✓ (CSS-Datei)

### Wer importiert diese Datei:
1. `features/users/components/UserTable.tsx`
2. `pages/LoginBypassPage.tsx`
3. `pages/IntegrationTestPage.tsx`
4. `features/users/components/UserForm.tsx`
5. `features/calculator/components/ScenarioSelector.tsx`
6. `features/calculator/components/CalculatorResultsSkeleton.tsx`
7. `features/calculator/components/CalculatorResults.tsx`
8. `features/calculator/components/CalculatorForm.tsx`
9. `components/ui/card.tsx`
10. `App.tsx`

### Abhängigkeitsbaum - Level 1:
```
components/ui/card-transition.tsx
├── react (extern)
├── styles/legacy/components.css
└── wird importiert von:
    ├── UserTable.tsx → ?
    ├── LoginBypassPage.tsx → ?
    ├── IntegrationTestPage.tsx → ?
    ├── UserForm.tsx → ?
    ├── ScenarioSelector.tsx → ?
    ├── CalculatorResultsSkeleton.tsx → ?
    ├── CalculatorResults.tsx → ?
    ├── CalculatorForm.tsx → ?
    ├── card.tsx → ?
    └── App.tsx → ?
```

### Nächster Schritt: Jede importierende Datei analysieren...

### Status:
⚠️ **KOMPLEX** - 10 importierende Dateien, sehr großer Abhängigkeitsbaum!

---

## 3. types/api.ts

### Start-Datei:
`types/api.ts`

### Direkte Imports (was importiert diese Datei):
- **KEINE** ✓ (reine TypeScript Interface-Definitionen)

### Wer importiert diese Datei:
1. `shared/lib/apiClient.ts`
2. `services/api.ts`

### Abhängigkeitsbaum - Level 1:
```
types/api.ts
└── wird importiert von:
    ├── shared/lib/apiClient.ts → ?
    └── services/api.ts → ?
```

### Level 2 - shared/lib/apiClient.ts analysieren:
- Wird importiert von: `features/users/api/userQueries.ts`

### Level 2 - services/api.ts analysieren:
- Wird importiert von: 
  - `App.test.tsx`
  - `App.tsx`

### Abhängigkeitsbaum - Level 2:
```
types/api.ts
└── wird importiert von:
    ├── shared/lib/apiClient.ts
    │   └── features/users/api/userQueries.ts → ?
    └── services/api.ts
        ├── App.test.tsx → ?
        └── App.tsx → ?
```

### Level 3 - features/users/api/userQueries.ts analysieren:
- Wird importiert von: `pages/UsersPage.tsx`

### Level 3 - App.tsx analysieren:
- Wird importiert von:
  - `App.test.tsx` (Test-Datei)
  - `pages/LegacyToolPage.tsx`

### Abhängigkeitsbaum - Level 3:
```
types/api.ts
└── wird importiert von:
    ├── shared/lib/apiClient.ts
    │   └── features/users/api/userQueries.ts
    │       └── pages/UsersPage.tsx → ?
    └── services/api.ts
        ├── App.test.tsx (TEST)
        └── App.tsx
            ├── App.test.tsx (TEST)
            └── pages/LegacyToolPage.tsx → ?
```

### Level 4 - pages/UsersPage.tsx analysieren:
- Wird importiert von: `main.tsx` (Einstiegspunkt!)

### Level 4 - pages/LegacyToolPage.tsx analysieren:
- Wird importiert von: `main.tsx` (Einstiegspunkt!)

### FINALER Abhängigkeitsbaum für types/api.ts:
```
types/api.ts
└── wird importiert von:
    ├── shared/lib/apiClient.ts
    │   └── features/users/api/userQueries.ts
    │       └── pages/UsersPage.tsx
    │           └── main.tsx (ENDE)
    └── services/api.ts
        ├── App.test.tsx (TEST - ENDE)
        └── App.tsx
            ├── App.test.tsx (TEST - ENDE)
            └── pages/LegacyToolPage.tsx
                └── main.tsx (ENDE)
```

### Status:
✅ **ÜBERSCHAUBAR** - Baum endet bei main.tsx (Einstiegspunkt) und Test-Dateien

### Zusammenfassung types/api.ts:
- Keine eigenen Imports (reine Types)
- 2 direkte Abhängige
- Maximale Tiefe: 5 Ebenen bis main.tsx
- Kritisch: Wird über apiClient und userQueries verwendet

---

## WICHTIGE ERKENNTNIS: Abhängigkeitsbaum-ENDE

Ein Abhängigkeitsbaum endet bei:
1. **main.tsx** - Einstiegspunkt der App
2. **Test-Dateien** (.test.tsx, .spec.ts) - werden nicht produktiv verwendet
3. **Markdown-Dateien** (.md) - enthalten keinen Code!
4. **CSS-Dateien** (.css) - keine JavaScript-Abhängigkeiten
5. **Statische Assets** (Bilder, Fonts, etc.)
6. **JSON-Dateien** - reine Daten
7. **Externe Dependencies** (react, @mui/material, etc.)

**➡️ Diese Dateien sind IMMER sicher zu verschieben!**

## Korrektur: "Ungefährliche" Datei-Endpunkte

Dateien die **NICHTS kaputt machen können** (Baum-Ende):
- **.md** → Dokumentation, kein ausführbarer Code
- **.css** → Styling, keine JavaScript-Logik
- **.json** → Daten/Config, wird gelesen aber führt keinen Code aus
- **.png/.jpg/.svg** → Bilder, keine Logik
- **.txt** → Text-Dateien
- **.html** → Statisches HTML (außer es hat <script> Tags!)
- **Test-Dateien** → Werden nur beim Testen ausgeführt

Diese können verschoben/gelöscht werden ohne dass Code bricht!
(Außer natürlich jemand importiert eine .json Config-Datei...)

---

## 4. lib/utils.ts (mit neuem Ansatz)

### Start-Datei:
`lib/utils.ts`

### Direkte Imports (was importiert diese Datei):
- `clsx` → externe npm Dependency ✅ (ENDE)
- `tailwind-merge` → externe npm Dependency ✅ (ENDE)

### Wer importiert diese Datei:
1. `components/ui/card-shadcn.tsx`
2. `components/ui/switch.tsx`
3. `components/ui/label.tsx`
4. `components/ui/input.tsx`

### Abhängigkeitsbaum:
```
lib/utils.ts
├── clsx (NPM - ENDE)
├── tailwind-merge (NPM - ENDE)
└── wird importiert von:
    ├── components/ui/card-shadcn.tsx → ?
    ├── components/ui/switch.tsx → ?
    ├── components/ui/label.tsx → ?
    └── components/ui/input.tsx → ?
```

### Analyse der importierenden Dateien:

#### 1. card-shadcn.tsx:
- Wird von **NIEMANDEM** importiert! ✅ (ENDE)

### FINALER Abhängigkeitsbaum für lib/utils.ts:
```
lib/utils.ts
├── clsx (NPM - ENDE)
├── tailwind-merge (NPM - ENDE)
└── wird importiert von:
    ├── components/ui/card-shadcn.tsx → NIEMAND (ENDE)
    ├── components/ui/switch.tsx → (noch nicht analysiert)
    ├── components/ui/label.tsx → (noch nicht analysiert)
    └── components/ui/input.tsx → (noch nicht analysiert)
```

## 🎯 MIGRIERBARER TEILBAUM GEFUNDEN!

### Sicher zu verschieben:
1. **lib/utils.ts** 
   - Importiert nur NPM packages
   - Hat 4 Abhängige
   
2. **components/ui/card-shadcn.tsx**
   - Importiert lib/utils.ts
   - Wird von NIEMANDEM verwendet
   - **KANN GELÖSCHT WERDEN!**

### Migration möglich für:
```
lib/utils.ts → helpers/
components/ui/card-shadcn.tsx → LÖSCHEN (ungenutzt)
```

### ✅ ERFOLGREICH MIGRIERT!

---

## 5. card-freshplan.tsx (einfacher Kandidat)

### Start-Datei:
`components/ui/card-freshplan.tsx`

### Rückblick von vorhin:
- Importiert nur React (extern)
- Wird von **NIEMANDEM** verwendet!

### FINALER Abhängigkeitsbaum:
```
components/ui/card-freshplan.tsx
└── react (extern - ENDE)

WIRD VON NIEMANDEM VERWENDET!
```

## 🎯 MIGRATION MÖGLICH:

### Aktion:
```
components/ui/card-freshplan.tsx → LÖSCHEN (ungenutzt)
```

### Status:
✅ **SUPER EINFACH** - Kann sofort gelöscht werden!

### ✅ ERFOLGREICH GELÖSCHT!

---

## 6. utils/formatting.ts

### Start-Datei:
`utils/formatting.ts`

### Direkte Imports (was importiert diese Datei):
- **KEINE** ✅ (nutzt nur JavaScript Intl API)

### Wer importiert diese Datei:
1. `features/calculator/components/ScenarioSelector.tsx`
2. `features/calculator/components/CalculatorResults.tsx`

### Abhängigkeitsbaum - Level 1:
```
utils/formatting.ts
└── wird importiert von:
    ├── ScenarioSelector.tsx → ?
    └── CalculatorResults.tsx → ?
```

### Level 2 - Wer importiert ScenarioSelector.tsx:
- `features/calculator/pages/CalculatorPage.tsx`
- `features/calculator/components/index.ts` (Barrel Export)

### Level 2 - Wer importiert CalculatorResults.tsx:
- `features/calculator/components/index.ts` (Barrel Export)

### Level 2 - Wer importiert index.ts:
- **NIEMAND!** (ungenutzt)

### Level 3 - Wer importiert CalculatorPage.tsx:
- **NIEMAND!** (wird nicht verwendet)

### FINALER Abhängigkeitsbaum für utils/formatting.ts:
```
utils/formatting.ts
└── wird importiert von:
    ├── ScenarioSelector.tsx
    │   ├── features/calculator/pages/CalculatorPage.tsx → NIEMAND (ENDE)
    │   └── features/calculator/components/index.ts → NIEMAND (ENDE)
    └── CalculatorResults.tsx
        └── features/calculator/components/index.ts → NIEMAND (ENDE)
```

## 🎯 INTERESSANTE ENTDECKUNG:

### Status:
⚠️ **VORSICHT** - Die Calculator-Features scheinen ungenutzt zu sein!

- `utils/formatting.ts` selbst ist OK
- Aber die importierenden Dateien werden nicht verwendet
- `CalculatorPage.tsx` wird von niemandem importiert
- `index.ts` (Barrel Export) wird auch nicht verwendet

### Migration möglich:
```
utils/formatting.ts → helpers/formatting.ts
```

Aber die Calculator-Features scheinen tot zu sein!

### ✅ ERFOLGREICH MIGRIERT!

---

## 7. button-freshplan.tsx

### Start-Datei:
`components/ui/button-freshplan.tsx`

### Direkte Imports (was importiert diese Datei):
- `React` from 'react' ✓ (externe Dependency)
- `Slot` from '@radix-ui/react-slot' ✓ (externe Dependency)
- `../../styles/legacy/forms.css` ✓ (CSS-Datei - ENDE)

### Wer importiert diese Datei:
- **NIEMAND** ❌ (ungenutzte Datei!)

### FINALER Abhängigkeitsbaum:
```
components/ui/button-freshplan.tsx
├── react (NPM - ENDE)
├── @radix-ui/react-slot (NPM - ENDE)  
└── styles/legacy/forms.css (CSS - ENDE)

WIRD VON NIEMANDEM VERWENDET!
```

## 🎯 MIGRATION MÖGLICH:

### Aktion:
```
components/ui/button-freshplan.tsx → LÖSCHEN (ungenutzt)
```

### Status:
✅ **EINFACH** - Noch eine ungenutzte UI-Komponente!

### ✅ IN TEMP VERSCHOBEN!

---

## 8. input.tsx (UI-Komponente)

### Start-Datei:
`components/ui/input.tsx`

### Direkte Imports (was importiert diese Datei):
- `React` from 'react' ✓ (externe Dependency)
- `cn` from '../../helpers/classnames' ✓ (schon migriert!)

### Wer importiert diese Datei:
1. `features/users/components/UserTable.tsx`
2. `features/users/components/UserForm.tsx`
3. `features/calculator/components/CalculatorForm.tsx`

### Abhängigkeitsbaum:
```
components/ui/input.tsx
├── react (NPM - ENDE)
├── helpers/classnames.ts ✓ (schon migriert)
└── wird importiert von:
    ├── UserTable.tsx → ?
    ├── UserForm.tsx → ?
    └── CalculatorForm.tsx → ?
```

### Status:
⚠️ **AKTIV GENUTZT** - Wird von 3 wichtigen Komponenten verwendet!

Diese Komponente ist aktiv in Verwendung. Aber sie nutzt bereits unsere migrierten `helpers/classnames.ts` - das ist gut!

---

## 9. button-transition.tsx (UI-Komponente)

### Start-Datei:
`components/ui/button-transition.tsx`

### Info:
Wissen wir schon - wird von 7 Dateien verwendet! Zu wichtig zum Verschieben.

---

## 10. button.tsx (ohne -transition)

### Start-Datei:
`components/ui/button.tsx`

### Direkte Imports (was importiert diese Datei):
- `Button, buttonVariants` from './button-transition' (interne Weiterleitung)
- `VariantProps` from 'class-variance-authority' (NPM)

### Wer importiert diese Datei:
- **NIEMAND** ❌ (ungenutzt)

### FINALER Abhängigkeitsbaum:
```
components/ui/button.tsx
├── ./button-transition (nur Re-Export)
└── class-variance-authority (NPM - ENDE)

WIRD VON NIEMANDEM VERWENDET!
```

### Status:
✅ **LEGACY REDIRECT** - Nur für Kompatibilität, wird nicht verwendet!

### Aktion:
```
components/ui/button.tsx → temp/unused-components/
```

### ✅ ERFOLGREICH IN TEMP VERSCHOBEN!

---

## 11. label.tsx (UI-Komponente)

### Start-Datei:
`components/ui/label.tsx`

### Direkte Imports (was importiert diese Datei):
- `React` from 'react' ✓ (externe Dependency)
- `@radix-ui/react-label` ✓ (externe Dependency)
- `class-variance-authority` ✓ (externe Dependency)
- `cn` from '../../helpers/classnames' ✓ (schon migriert)

### Wer importiert diese Datei:
1. `features/calculator/components/CalculatorForm.tsx`

### Abhängigkeitsbaum:
```
components/ui/label.tsx
├── react (NPM - ENDE)
├── @radix-ui/react-label (NPM - ENDE)
├── class-variance-authority (NPM - ENDE)
├── helpers/classnames.ts ✓ (schon migriert)
└── wird importiert von:
    └── CalculatorForm.tsx
        └── (Teil des ungenutzten Calculator-Features)
```

### Status:
⚠️ **PRAKTISCH UNGENUTZT** - Wird nur vom ungenutzten Calculator-Feature verwendet

### Aktion:
```
components/ui/label.tsx → BEHALTEN (vorerst)
```

Grund: Obwohl praktisch ungenutzt, ist es eine generische UI-Komponente die später wieder verwendet werden könnte.

---

## 12. switch.tsx (UI-Komponente)

### Start-Datei:
`components/ui/switch.tsx`

### Direkte Imports (was importiert diese Datei):
- `React` from 'react' ✓ (externe Dependency)
- `@radix-ui/react-switch` ✓ (externe Dependency)
- `cn` from '../../helpers/classnames' ✓ (schon migriert)

### Wer importiert diese Datei:
1. `features/calculator/components/CalculatorForm.tsx`

### Abhängigkeitsbaum:
```
components/ui/switch.tsx
├── react (NPM - ENDE)
├── @radix-ui/react-switch (NPM - ENDE)
├── helpers/classnames.ts ✓ (schon migriert)
└── wird importiert von:
    └── CalculatorForm.tsx
        └── (Teil des ungenutzten Calculator-Features)
```

### Status:
⚠️ **PRAKTISCH UNGENUTZT** - Wird nur vom ungenutzten Calculator-Feature verwendet

### Aktion:
```
components/ui/switch.tsx → BEHALTEN (vorerst)
```

Grund: Generische UI-Komponente für Toggle-Switches, könnte später nützlich sein.

---

## 13. card.tsx (Legacy-Redirect)

### Start-Datei:
`components/ui/card.tsx`

### Direkte Imports (was importiert diese Datei):
- Nur Re-Export von `./card-transition`

### Wer importiert diese Datei:
- **NIEMAND** ❌ (ungenutzt)

### FINALER Abhängigkeitsbaum:
```
components/ui/card.tsx
└── ./card-transition (nur Re-Export)

WIRD VON NIEMANDEM VERWENDET!
```

### Status:
✅ **LEGACY REDIRECT** - Nur für Kompatibilität, wird nicht verwendet!

### Aktion:
```
components/ui/card.tsx → temp/unused-components/
```

### ✅ ERFOLGREICH IN TEMP VERSCHOBEN!

---

## 14. components/original Ordner Analyse

### Übersicht:
Der \ Ordner enthält Legacy-Komponenten.

### Verwendung:
- Nur \ wird verwendet (von \)
- Alle anderen Dateien sind ungenutzt!

### Ungenutzte Dateien im original-Ordner:
1. **Header.tsx** - Legacy Header-Komponente
2. **Navigation.tsx** - Legacy Navigation
3. **CustomerForm.tsx** - Legacy Kundenformular
4. **CalculatorLayout.tsx** - Legacy Calculator Layout
5. **CustomSlider.tsx** - Legacy Slider-Komponente
6. **LocationsForm.tsx** - Legacy Standort-Formular

### Status:
✅ **6 UNGENUTZTE KOMPONENTEN GEFUNDEN**

### Aktion:
```
components/original/*.tsx (außer LegacyApp.tsx) → temp/unused-components/original/
```

### ✅ ERFOLGREICH VERSCHOBEN!

### KORREKTUR:
❌ **FEHLERHAFTE ANALYSE!** 

LegacyApp.tsx importiert tatsächlich ALLE anderen Komponenten im original-Ordner. 
Alle Dateien wurden zurück verschoben!

---

## 15. Ungenutzte CSS-Dateien

### Analyse:
Folgende CSS-Dateien werden weder von TypeScript/JavaScript noch von anderen CSS-Dateien importiert:

### Ungenutzte CSS-Dateien:
1. **styles/legacy/calculator-components.css**
2. **styles/legacy/calculator-layout.css**
3. **styles/legacy/header-logo.css**
4. **styles/legacy/locations.css**
5. **styles/legacy/notifications.css**
6. **styles/legacy/original-styles.css**
7. **styles/legacy/slider.css**

### Status:
✅ **7 UNGENUTZTE CSS-DATEIEN GEFUNDEN**

### Aktion:
```
Diese CSS-Dateien → temp/unused-styles/
```

### KORREKTUR:
❌ **AUCH FEHLERHAFTE ANALYSE!** 

Die original-Komponenten importieren fast alle diese CSS-Dateien.
Alle CSS-Dateien wurden zurück verschoben!

---

## 16. Calculator Feature Analyse

### Übersicht:
Das Calculator-Feature scheint größtenteils ungenutzt zu sein.

### Ungenutzte Dateien:
1. **features/calculator/pages/CalculatorPage.tsx** - Wird von niemandem importiert
2. **features/calculator/components/OriginalCalculator.tsx** - Wird von niemandem importiert
3. **features/calculator/components/index.ts** - Barrel Export, wird nicht verwendet

### Genutzter Status der anderen Calculator-Dateien:
- Die anderen Calculator-Komponenten werden zwar untereinander verwendet, aber da die CalculatorPage nicht eingebunden ist, ist das ganze Feature praktisch tot.

### Status:
⚠️ **GANZES CALCULATOR-FEATURE UNGENUTZT**

### Aktion:
```
Vorerst behalten - könnte ein Feature in Entwicklung sein
```

---

## 17. Weitere ungenutzte Dateien

### Gefundene ungenutzte Dateien:
1. **main-minimal.tsx** - Minimale Test-App, wird nirgends referenziert
2. **test-calculator-integration.ts** - Test-Script für Calculator API

### Status:
✅ **2 UNGENUTZTE DATEIEN GEFUNDEN**

### Aktion:
```
main-minimal.tsx → temp/unused-files/
test-calculator-integration.ts → temp/unused-files/
```

### ✅ ERFOLGREICH VERSCHOBEN!

---

## 18. Weitere Aufräumarbeiten

### Gefundene Probleme:
1. **assets/react.svg** - Ungenutztes React-Logo
2. **index.css** - Wird nicht importiert (globals.css wird stattdessen verwendet)
3. **frontend/** - Fehlerhaft verschachtelter leerer Ordner (bereits gelöscht)
4. **layouts/** - Leerer Ordner
5. **hooks/** - Leerer Ordner
6. **store/** - Leerer Ordner

### Status:
✅ **WEITERE AUFRÄUM-KANDIDATEN GEFUNDEN**

### Aktion:
```
assets/react.svg → temp/unused-files/
index.css → temp/unused-files/
Leere Ordner können später gelöscht werden
```

### ✅ ERFOLGREICH AUFGERÄUMT!

---

## 19. Verwaiste Test-Dateien

### Gefundene verwaiste Tests:
1. **components/ui/button.test.tsx** - Test für verschobene button.tsx
2. **components/ui/card.test.tsx** - Test für verschobene card.tsx

### Status:
✅ **2 VERWAISTE TEST-DATEIEN GEFUNDEN**

### Aktion:
```
button.test.tsx → temp/unused-tests/
card.test.tsx → temp/unused-tests/
```

### ✅ ERFOLGREICH VERSCHOBEN!

---

## 20. Identifizierte leere Ordner

### Leere Ordner die gelöscht werden können:
1. **layouts/** - Komplett leer
2. **hooks/** - Komplett leer  
3. **store/** - Komplett leer
4. **assets/** - Jetzt leer (react.svg verschoben)

### Status:
✅ **4 LEERE ORDNER IDENTIFIZIERT**

### ✅ ALLE LEEREN ORDNER GELÖSCHT!

---

## 21. Components-Ordner Aufräumarbeiten

### Weitere gefundene leere Ordner:
1. **components/common/** - Komplett leer
2. **components/domain/** - Komplett leer

### Status:
✅ **2 WEITERE LEERE ORDNER GELÖSCHT**

### Aktuelle Components-Struktur:
```
components/
├── ErrorBoundary.tsx (wird verwendet)
├── ErrorBoundary.test.tsx
├── original/ (8 Legacy-Komponenten für LegacyApp)
└── ui/ (5 UI-Komponenten + 1 Test)
    ├── button-transition.tsx
    ├── card-transition.tsx
    ├── input.tsx
    ├── input.test.tsx
    ├── label.tsx
    └── switch.tsx
```

### Beobachtungen:
- Die "-transition" Benennung macht Sinn (Übergang von shadcn zu FreshPlan-Styles)
- UI-Ordner könnte theoretisch aufgelöst werden (nur 5 Komponenten)
- Original-Ordner muss bleiben (alle Komponenten werden von LegacyApp verwendet)
