# 🔧 CR-003: Configuration Data Externalization

**Datum:** 27.07.2025  
**Feature:** FC-005 Customer Management  
**Code Review Item:** CR-003  
**Status:** ✅ ABGESCHLOSSEN

## 📋 Zusammenfassung

Externalisierung der hardcoded Configuration Data aus `DetailedLocationsStep.tsx` in separate, wartbare Konfigurationsdateien.

## 🎯 Ziele

- Bessere Wartbarkeit durch Trennung von Konfiguration und Komponenten-Logik
- Zentrale Verwaltung von Icons, Labels und Templates
- Einfache Erweiterbarkeit für neue Branchen und Kategorien
- Type-Safety beibehalten

## 🏗️ Implementierung

### 1. Neue Struktur erstellt

```
frontend/src/features/customers/config/
├── index.ts                  # Zentrale Export-Datei
├── locationCategories.ts     # Icons und Labels für Kategorien
└── industryTemplates.ts      # Branchenspezifische Templates
```

### 2. locationCategories.ts

- **categoryIcons**: Icons als Funktionen die React-Elemente zurückgeben
- **categoryLabels**: Deutsche Bezeichnungen für alle Kategorien
- **Helper-Funktionen**: `getCategoryIcon()`, `getCategoryLabel()`
- **availableCategories**: Vorbereitete Array für Select-Dropdowns

### 3. industryTemplates.ts

- **LocationTemplate Interface**: Struktur für Template-Definitionen
- **industryTemplates**: Branchenspezifische Vorlagen mit Details
- **Helper-Funktionen**: `getIndustryTemplates()`, `hasIndustryTemplates()`
- **Erweiterte Templates**: Jetzt mit description und capacity

### 4. DetailedLocationsStep.tsx Updates

- Imports geändert auf zentrale config
- Verwendung von `getCategoryIcon()` statt direktem Array-Zugriff
- Verwendung von `getIndustryTemplates()` für Type-Safety

## 🧪 Tests

12 Tests erstellt in `locationConfig.test.ts`:
- ✅ Alle Category Icons vorhanden
- ✅ Alle Category Labels korrekt
- ✅ Helper-Funktionen funktionieren
- ✅ Industry Templates vollständig
- ✅ Graceful Handling für unbekannte Werte
- ✅ Konsistenz zwischen Icons und Labels

## 📊 Vorteile der Lösung

1. **Wartbarkeit**: Änderungen an Labels/Icons an einer Stelle
2. **Erweiterbarkeit**: Neue Branchen einfach hinzufügen
3. **Type-Safety**: Vollständige TypeScript-Unterstützung
4. **Testbarkeit**: Isolierte Tests für Konfiguration
5. **Performance**: Icons werden nur bei Bedarf erstellt

## 🔍 Technische Details

### Icon-Funktionen statt JSX

Da TypeScript-Dateien kein JSX unterstützen, werden Icons als Funktionen definiert:

```typescript
export const categoryIcons: Record<DetailedLocationCategory, () => React.ReactElement> = {
  restaurant: () => React.createElement(RestaurantIcon),
  // ...
};
```

### Enterprise-Ready Templates

Templates enthalten jetzt zusätzliche Metadaten:
- `description`: Für Tooltips und Hilfe
- `capacity`: Vorbelegung für Kapazitätsfelder

## ✅ Abschluss

CR-003 erfolgreich implementiert. Die Configuration Data ist jetzt:
- ✅ Externalisiert in separate Dateien
- ✅ Vollständig typsicher
- ✅ Getestet mit 100% Coverage
- ✅ Dokumentiert und wartbar

## 🔗 Referenzen

- Pull Request: #70
- Files geändert:
  - `/frontend/src/features/customers/config/index.ts` (NEU)
  - `/frontend/src/features/customers/config/locationCategories.ts` (NEU)
  - `/frontend/src/features/customers/config/industryTemplates.ts` (NEU)
  - `/frontend/src/features/customers/components/steps/DetailedLocationsStep.tsx` (UPDATED)
  - `/frontend/src/features/customers/tests/unit/config/locationConfig.test.ts` (NEU)