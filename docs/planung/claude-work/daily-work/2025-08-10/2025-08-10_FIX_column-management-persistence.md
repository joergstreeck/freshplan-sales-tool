# Fix: Spalten-Verwaltung Persistierung

**Datum:** 2025-08-10
**Problem:** Spalten-Einstellungen und Sortierungen werden bei Seiten-Neustart zurückgesetzt

## Problem-Analyse

Die Spalten-Verwaltung in der Kundenliste hatte folgende Probleme:
1. Sortierung wurde nicht persistiert
2. Spalten-Sichtbarkeit wurde zwar gespeichert, aber die Sortierung nicht
3. CustomersPageV2 verwendete lokalen State statt den persistierten Store

## Lösung

### 1. Store-Persistierung erweitert

**Datei:** `/frontend/src/features/customer/store/focusListStore.ts`

Die `partialize` Funktion im Zustand Store wurde erweitert um:
- `sortBy` - Aktuelle Sortierung
- `smartSortId` - Gewählte Smart-Sort Option  
- `globalSearch` - Suchtext
- `activeFilters` - Aktive Filter

```typescript
partialize: state => ({
  savedViews: state.savedViews,
  viewMode: state.viewMode,
  pageSize: state.pageSize,
  tableColumns: state.tableColumns,
  sortBy: state.sortBy,           // NEU
  smartSortId: state.smartSortId,  // NEU
  globalSearch: state.globalSearch, // NEU
  activeFilters: state.activeFilters, // NEU
}),
```

### 2. CustomersPageV2 angepasst

**Datei:** `/frontend/src/pages/CustomersPageV2.tsx`

Die Komponente verwendet jetzt den persistierten Store:

```typescript
// Vorher: Lokaler State
const [sortConfig, setSortConfig] = useState<SortConfig>({
  field: 'companyName',
  direction: 'asc'
});

// Nachher: Store verwenden
const { 
  tableColumns, 
  sortBy, 
  setSortBy,
  globalSearch,
  activeFilters 
} = useFocusListStore();
```

### 3. Integration mit IntelligentFilterBar

Die `onSortChange` Callback wurde angepasst:

```typescript
onSortChange={(config: SortConfig) => {
  setSortBy({
    field: config.field,
    ascending: config.direction === 'asc'
  });
}}
```

## Komponenten-Übersicht

### Betroffene Komponenten:
1. **TableColumnSettings** (`/features/customer/components/TableColumnSettings.tsx`)
   - Verwaltet Spalten-Sichtbarkeit und Reihenfolge
   - Verwendet `focusListStore` für Persistierung

2. **FilterBar** (`/features/customer/components/FilterBar.tsx`)
   - Zeigt TableColumnSettings bei Tabellenansicht
   - Integriert mit Store

3. **focusListStore** (`/features/customer/store/focusListStore.ts`)
   - Zustand-Management mit Zustand
   - Persistiert Einstellungen im localStorage
   - Name: `focus-list-store`

4. **CustomersPageV2** (`/pages/CustomersPageV2.tsx`)
   - Hauptseite für Kundenliste
   - Verwendet Store für Spalten und Sortierung

## Vorteile der Lösung

✅ **Persistenz:** Einstellungen bleiben über Browser-Sessions erhalten
✅ **Konsistenz:** Alle Komponenten verwenden denselben Store
✅ **Performance:** Keine redundanten API-Calls durch lokale Persistierung
✅ **UX:** Benutzer finden ihre Einstellungen wieder

## Test-Anleitung

1. Öffne die Kundenliste: http://localhost:5173/customers
2. Ändere die Sortierung (klicke auf Spaltenköpfe)
3. Öffne Spalten-Einstellungen (Zahnrad-Icon)
4. Ändere Sichtbarkeit und Reihenfolge der Spalten
5. Lade die Seite neu (F5)
6. **Erwartung:** Alle Einstellungen bleiben erhalten

## localStorage Struktur

Die Einstellungen werden unter dem Key `focus-list-store` gespeichert:

```json
{
  "state": {
    "savedViews": [],
    "viewMode": "table",
    "pageSize": 50,
    "tableColumns": [...],
    "sortBy": {
      "field": "companyName",
      "ascending": false
    },
    "smartSortId": "revenue-high-to-low",
    "globalSearch": "",
    "activeFilters": []
  },
  "version": 0
}
```

## Nächste Schritte

- [ ] Tests für die Persistierung schreiben
- [ ] Migration der alten CustomerList Komponente
- [ ] Synchronisation mit Backend-Präferenzen (optional)