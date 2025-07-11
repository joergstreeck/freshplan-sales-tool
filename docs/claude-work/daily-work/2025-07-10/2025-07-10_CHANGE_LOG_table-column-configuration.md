# 📝 CHANGE LOG: Tabellen-Spalten-Konfiguration

**Datum:** 10.07.2025  
**Feature:** FC-002-M3 Cockpit - Fokus-Liste  
**Komponente:** FocusListColumnMUI / TableColumnSettings  
**Art der Änderung:** Neue Funktion - Konfigurierbare Tabellenspalten  

## 🎯 Anforderung

Benutzer möchten in der Tabellenansicht der Fokus-Liste (Spalte 2) die Spaltenreihenfolge und -sichtbarkeit anpassen können.

## ✅ Implementierung

### 1. Store-Erweiterung (focusListStore.ts)

**Neue Interfaces:**
```typescript
export interface TableColumn {
  id: string;
  label: string;
  field: string;
  visible: boolean;
  order: number;
  align?: 'left' | 'center' | 'right';
  minWidth?: number;
}
```

**Neue State-Properties:**
- `tableColumns: TableColumn[]` - Alle verfügbaren Spalten
- `visibleTableColumns: TableColumn[]` - Computed: nur sichtbare Spalten

**Neue Actions:**
- `toggleColumnVisibility(columnId)` - Spalte ein/ausblenden
- `setColumnOrder(columnIds)` - Spaltenreihenfolge ändern
- `resetTableColumns()` - Auf Standard zurücksetzen

### 2. Neue Komponente: TableColumnSettings.tsx

**Features:**
- Zahnrad-Icon öffnet Konfigurationsmenü
- Checkbox-Liste aller verfügbaren Spalten
- Mindestens 2 Spalten müssen sichtbar bleiben
- Reset-Button für Standardeinstellungen
- Einstellungen werden in localStorage gespeichert

### 3. Integration in FocusListColumnMUI.tsx

**Dynamische Spalten-Darstellung:**
- Tabellen-Header basiert auf `visibleTableColumns`
- `renderCellContent()` Funktion für spaltenspezifische Darstellung
- Unterstützt alle Spaltentypen:
  - Kunde (mit Kundennummer)
  - Status (mit Chip)
  - Risiko (mit Farbindikator)
  - Branche
  - Jahresumsatz (formatiert)
  - Letzter Kontakt (Datum)
  - Betreuer
  - Aktionen

### 4. UI-Integration

- TableColumnSettings nur in Tabellenansicht sichtbar
- Positioniert neben View-Mode-Toggle
- Nahtlose Integration in FilterBar

## 📊 Verfügbare Spalten

| ID | Label | Standard | Ausrichtung |
|---|---|---|---|
| companyName | Kunde | ✅ Sichtbar | Links |
| customerNumber | Kundennummer | ❌ Versteckt | Links |
| status | Status | ✅ Sichtbar | Links |
| riskScore | Risiko | ✅ Sichtbar | Zentriert |
| industry | Branche | ✅ Sichtbar | Links |
| expectedAnnualVolume | Jahresumsatz | ❌ Versteckt | Rechts |
| lastContactDate | Letzter Kontakt | ❌ Versteckt | Links |
| assignedTo | Betreuer | ❌ Versteckt | Links |
| actions | Aktionen | ✅ Sichtbar | Rechts |

## 🚀 Vorteile

1. **Personalisierung**: Jeder Nutzer kann die Tabelle nach seinen Bedürfnissen anpassen
2. **Persistenz**: Einstellungen bleiben über Sessions erhalten
3. **Performance**: Nur sichtbare Spalten werden gerendert
4. **Flexibilität**: Einfaches Ein-/Ausblenden ohne Code-Änderungen
5. **Best Practice**: Folgt etablierten UI-Patterns

## 🧪 Test-Szenarien

- [x] Spalten ein-/ausblenden funktioniert
- [x] Mindestens 2 Spalten bleiben sichtbar
- [x] Reset setzt auf Standard zurück
- [x] Einstellungen werden gespeichert
- [x] Alle Spaltentypen werden korrekt dargestellt
- [x] Keine Performance-Einbußen

## 🆕 Update: Drag & Drop für Spaltenreihenfolge

### Implementierung (10.07.2025, 19:45)

**Neue Funktionalität:**
- Spalten können per Drag & Drop neu angeordnet werden
- Verwendung von `@dnd-kit/sortable` für moderne DnD-Funktionalität
- Barrierefreie Tastatur-Navigation unterstützt
- Visuelles Feedback beim Ziehen (Opacity-Änderung)
- Aktionen-Spalte bleibt immer am Ende (nicht verschiebbar)

**UI-Verbesserungen:**
- Drag-Handle Icon (≡) für bessere Erkennbarkeit
- Hover-Effekt auf Drag-Handle
- Cursor ändert sich zu "grab" beim Hovern
- Smooth Animations beim Verschieben

**Technische Details:**
- `SortableColumnItem` Komponente für jede Spalte
- `DndContext` mit Sensoren für Maus und Tastatur
- `arrayMove` für effiziente Array-Manipulation
- Automatische Speicherung der neuen Reihenfolge

## 🧪 Erweiterte Test-Szenarien

- [x] Spalten per Drag & Drop verschieben
- [x] Reihenfolge wird gespeichert
- [x] Tastatur-Navigation funktioniert
- [x] Aktionen-Spalte bleibt am Ende
- [x] Reset setzt auch Reihenfolge zurück

## 📋 Verbleibende Punkte

- Spaltenbreiten-Anpassung (wird von MUI Table unterstützt)
- Export der Tabellenansicht
- Gruppierung von Spalten

---
**Status:** ✅ Vollständig implementiert mit Drag & Drop