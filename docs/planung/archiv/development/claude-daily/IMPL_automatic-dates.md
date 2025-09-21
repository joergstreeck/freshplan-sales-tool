# Automatische Datums-Marker - Korrekte Implementierung

**Implementiert am:** 08.06.2025
**Dokument angezeigt:** 08.06.2025

## 🎯 Problem gelöst

Zeigt das aktuelle Datum in Dokumenten an, ohne historische Daten zu überschreiben!

## 🛠️ Implementierte Lösung

### 1. Update-Script: `/scripts/update-current-date.js`

Features:
- Ersetzt NUR spezielle Marker wie `08.06.2025`
- Lässt historische Daten unverändert
- "Letzte Aktualisierung" bleibt erhalten
- Dry-Run und Verbose Modi

### 2. Wrapper-Script: `/scripts/update-current-date.sh`

Einfache Verwendung:
```bash
# AUTO_DATE Marker aktualisieren
./scripts/update-current-date.sh

# Testlauf (zeigt was geändert würde)
./scripts/update-current-date.sh --dry-run

# Mit Details
./scripts/update-current-date.sh --verbose
```

### 3. Unterstützte Datum-Marker

| Marker | Ausgabe | Verwendung |
|--------|---------|------------|
| `08.06.2025` | 08.06.2025 | Standard Datum |
| `2025-06-08` | 2025-06-08 | ISO-Format |
| `Sonntag, 8. Juni 2025` | Samstag, 8. Juni 2025 | Ausführlich |
| `Dokument angezeigt am 08.06.2025` | Automatisch aktualisiert am 08.06.2025 | Update-Vermerk |

### 4. Automatisch erkannte Muster

Das Script erkennt und aktualisiert diese Muster:
- `**Datum:** DD.MM.YYYY`
- `**Letzte Aktualisierung:** DD.MM.YYYY`
- `**Stand:** DD.MM.YYYY`
- `**Erstellt am:** DD.MM.YYYY`
- `*Letzte Änderung:* YYYY-MM-DD`

## 📋 Verwendung in neuen Dokumenten

```markdown
# Mein neues Dokument

**Datum:** 08.06.2025
**Autor:** FreshPlan Team

## Inhalt

...

---
*Letzte Aktualisierung: 08.06.2025*
```

## 🔄 Integration

### Manuell ausführen:
```bash
cd /Users/joergstreeck/freshplan-sales-tool
./scripts/update-dates.sh
```

### Git Pre-Commit Hook:
```bash
#!/bin/sh
# .git/hooks/pre-commit
node scripts/update-doc-dates.js
git add -u
```

### GitHub Actions:
```yaml
- name: Update Documentation Dates
  run: |
    node scripts/update-doc-dates.js
    git add -u
    git commit -m "chore: Update documentation dates" || true
```

## ✅ Bereits aktualisiert

- `docs/KNOWN_ISSUES.md` - verwendet jetzt `08.06.2025`
- `docs/MARKDOWN_DATE_TEMPLATE.md` - Beispiel-Template

## 🎯 Vorteile

1. **Konsistenz**: Immer korrektes Datum
2. **Automatisierung**: Keine manuelle Pflege
3. **Flexibilität**: Verschiedene Formate möglich
4. **Einfachheit**: Ein Befehl aktualisiert alles

## 💡 Best Practices

1. **Neue Dokumente**: Immer mit AUTO_DATE Markern beginnen
2. **Reviews**: `**Stand:** 08.06.2025`
3. **Footer**: `*Letzte Aktualisierung: 08.06.2025*`
4. **Regelmäßig**: Script vor wichtigen Commits ausführen

---
*Letzte Aktualisierung: 08.06.2025*