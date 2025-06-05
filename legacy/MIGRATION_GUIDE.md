# 🚀 Migration Guide: FreshPlan Sales Tool v1.0 → v2.0

## 📋 Übersicht

Dieses Dokument beschreibt die Migration von der alten monolithischen Struktur zur neuen modularen Architektur.

## 🔄 Wichtigste Änderungen

### 1. **Modularisierung**
- **Alt**: Alle Funktionen global im `window` Objekt
- **Neu**: ES6 Module mit expliziten Imports/Exports

### 2. **State Management**
- **Alt**: Globales `FreshPlan.state` Objekt
- **Neu**: Zentralisiertes State Management mit Pub/Sub Pattern

### 3. **Build System**
- **Alt**: Direkte Script-Tags in HTML
- **Neu**: Vite als Build-Tool mit Hot Module Replacement

### 4. **CSS Architektur**
- **Alt**: Gemischte inline styles und externe CSS
- **Neu**: Modulare CSS mit CI-konformen Variablen

## 📁 Neue Verzeichnisstruktur

```
freshplan-sales-tool/
├── js/
│   ├── main.js              # Neuer Einstiegspunkt (ersetzt app.js)
│   ├── config.js            # Konfiguration
│   ├── state.js             # NEU: State Management
│   ├── translations.js      # Übersetzungen
│   ├── modules/             # NEU: Feature-Module
│   │   ├── calculator.js
│   │   ├── customer.js
│   │   ├── profile.js
│   │   ├── pdf.js
│   │   ├── settings.js
│   │   └── tabs.js          # NEU: Tab-Navigation
│   └── utils/               # NEU: Utility-Funktionen
│       ├── dom.js
│       ├── formatting.js
│       ├── i18n.js
│       ├── storage.js
│       └── validation.js
├── css/
│   ├── main.css            # NEU: Import-Hub
│   ├── variables.css       # Aktualisiert mit CI-Farben
│   ├── layout.css
│   ├── components.css
│   └── responsive.css
└── tests/                  # NEU: Test-Verzeichnis
```

## 🔧 Entwicklungssetup

### 1. Dependencies installieren
```bash
npm install
```

### 2. Entwicklungsserver starten
```bash
npm run dev
```

### 3. Production Build
```bash
npm run build
```

## 📝 Code-Migration

### State-Zugriff

**Alt:**
```javascript
FreshPlan.state.customerData = { name: 'Test' };
const data = FreshPlan.state.customerData;
```

**Neu:**
```javascript
import state from './state.js';

state.set('customerData', { name: 'Test' });
const data = state.get('customerData');
```

### Event Handling

**Alt:**
```javascript
window.updateLanguage = function() { ... }
```

**Neu:**
```javascript
import { updateLanguage } from './utils/i18n.js';

// State-basierte Updates
state.subscribe('currentLang', updateLanguage);
```

### DOM-Manipulation

**Alt:**
```javascript
document.getElementById('element').style.display = 'block';
```

**Neu:**
```javascript
import { $, toggle } from './utils/dom.js';

toggle('#element', true);
```

### Validierung

**Alt:**
```javascript
function validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}
```

**Neu:**
```javascript
import { isValidEmail } from './utils/validation.js';

if (isValidEmail(email)) { ... }
```

## 🎨 CSS-Updates

### CI-konforme Farben

```css
/* Alt */
--primary-green: #7FB069;

/* Neu */
--color-primary: #94C456;      /* Freshfoodz Grün */
--color-primary-dark: #004F7B;  /* Freshfoodz Blau */
```

### Schriften

```css
/* Neu - CI-konform */
--font-heading: 'Antonio', sans-serif;
--font-body: 'Poppins', sans-serif;
```

## ⚡ Performance-Verbesserungen

1. **Code Splitting**: Module werden nur bei Bedarf geladen
2. **Tree Shaking**: Ungenutzter Code wird automatisch entfernt
3. **Minification**: Produktions-Build ist optimiert
4. **Hot Module Replacement**: Schnellere Entwicklung

## 🚨 Breaking Changes

1. **Globale Funktionen**: Nicht mehr über `window` verfügbar
2. **Script-Reihenfolge**: Wird jetzt von Vite verwaltet
3. **localStorage**: Fallback zu Memory Storage implementiert

## 📋 Checkliste für Entwickler

- [ ] Node.js >= 18.0.0 installiert
- [ ] `npm install` ausgeführt
- [ ] Vite Dev-Server läuft (`npm run dev`)
- [ ] ESLint-Integration in IDE aktiviert
- [ ] Prettier-Integration in IDE aktiviert

## 🔍 Debugging

### State-Inspektion
```javascript
// In der Browser-Konsole
import state from './js/state.js';
console.log(state.get());
```

### Event-Monitoring
```javascript
// PubSub Events überwachen
import PubSub from 'pubsub-js';
PubSub.subscribe('state', (msg, data) => {
  console.log('State changed:', data);
});
```

## 📚 Weitere Ressourcen

- [Vite Dokumentation](https://vitejs.dev/)
- [ES6 Module Guide](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Modules)
- [PubSub.js](https://github.com/mroderick/PubSubJS)

## ❓ Häufige Fragen

### Warum Vite statt Webpack?
Vite bietet schnellere Build-Zeiten und bessere Developer Experience durch nativen ES-Module Support.

### Kann ich weiterhin globale Funktionen nutzen?
Für Abwärtskompatibilität werden kritische Funktionen über `window.FreshPlan` exportiert, aber neue Features sollten Module nutzen.

### Wie teste ich meine Änderungen?
```bash
npm test              # Unit Tests
npm run test:e2e     # End-to-End Tests
```

---

Bei Fragen oder Problemen: Issue im GitHub-Repository erstellen!