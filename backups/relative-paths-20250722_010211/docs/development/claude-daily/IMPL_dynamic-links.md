# Dynamische Links - Implementierung

**Datum:** 08.06.2025  
**Status:** ✅ Implementiert

## 📋 Was wurde umgesetzt?

### 1. Team Dashboard (HTML) - JavaScript-Lösung

#### Zentrale Link-Registry:
```javascript
const DOCUMENT_LINKS = {
    'development-setup': 'DEVELOPMENT_SETUP.md',
    'api-contract': '../technical/API_CONTRACT.md',
    'design-system': '../../frontend/src/styles/DESIGN_SYSTEM.md',
    // ... weitere Links
};
```

#### Automatische Link-Initialisierung:
- Alle Links mit `data-doc` Attribut werden automatisch aufgelöst
- Beispiel: `<a href="#" data-doc="api-contract">API Docs</a>`
- Bei fehlenden Links: Warnung in Console + roter Link

#### Automatisches Datum:
- Datum wird beim Laden automatisch gesetzt
- Update jede Minute
- Format: DD.MM.YYYY, HH:MM Uhr

### 2. CLAUDE.md - Markdown Reference Links

#### Link-Definitionen am Anfang:
```markdown
[vision]: ./VISION_AND_ROADMAP.md
[api-contract]: ./docs/technical/API_CONTRACT.md
[known-issues]: ./docs/KNOWN_ISSUES.md
[adr-template]: ./docs/adr/ADR_TEMPLATE.md
```

#### Verwendung im Text:
- Alt: `Siehe \`VISION_AND_ROADMAP.md\` für Details`
- Neu: `Siehe [Vision und Roadmap][vision] für Details`

## 🎯 Vorteile

1. **Zentrale Wartung**: Links nur an einer Stelle pflegen
2. **Keine toten Links**: Bei Umstrukturierung nur Registry anpassen
3. **Automatisches Datum**: Immer aktuell, kein manuelles Update
4. **IDE-Support**: Markdown-Links bleiben klickbar
5. **Fehler-Erkennung**: Console-Warnung bei fehlenden Links

## 🔧 Wartung

### Neuen Link hinzufügen:

#### In HTML (Team Dashboard):
```javascript
// In DOCUMENT_LINKS hinzufügen:
'mein-dokument': '../pfad/zu/DOKUMENT.md'

// Im HTML verwenden:
<a href="#" data-doc="mein-dokument">Mein Dokument</a>
```

#### In Markdown:
```markdown
// Am Anfang definieren:
[mein-dokument]: ./pfad/zu/DOKUMENT.md

// Im Text verwenden:
Siehe [Mein Dokument][mein-dokument]
```

## 📊 Aktueller Status

### ✅ Implementiert:
- **TEAM_DASHBOARD.html**: Alle Links auf dynamisches System umgestellt
- **CLAUDE.md**: 4 Links konvertiert zu Reference Links

### 🔄 Noch zu migrieren:
- README.md
- Weitere Markdown-Dokumente bei Bedarf

## 🚀 Nächste Schritte

1. **Validierungs-Script**: Prüft ob alle Links existieren
2. **Build-Integration**: Links bei Build validieren
3. **Weitere Dokumente**: Nach und nach umstellen

## 💡 Tipps

- Bei neuen Dokumenten: Immer Link-Registry aktualisieren
- Konsistente Namensgebung für `data-doc` IDs
- Regelmäßig Console auf Link-Warnungen prüfen

---
*Mit diesem System sind wir für zukünftige Umstrukturierungen gewappnet!*