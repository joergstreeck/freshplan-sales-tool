# Fix: Kettenkunden-Status bleibt beim Tab-Wechsel erhalten

**Datum:** 2025-06-29
**Typ:** FIX
**Status:** Erfolgreich behoben

## 🐛 Problem

Der Kettenkunden-Status (chainCustomer) wurde beim Verlassen des Kundendaten-Tabs immer auf "nein" zurückgesetzt.

## 🔍 Ursache

Die CustomerForm verwaltete ihren State lokal mit useState. Bei jedem Tab-Wechsel wurde die Komponente neu gemountet und der State ging verloren.

## ✅ Lösung

Den kompletten CustomerForm State in die übergeordnete LegacyApp-Komponente verschoben:

1. **CustomerFormData Interface exportiert** aus CustomerForm.tsx
2. **State-Management in LegacyApp** - customerFormData State mit allen Feldern
3. **Props-basierte CustomerForm** - empfängt formData und onFormDataChange
4. **Konsistente State-Verwaltung** - alle Änderungen gehen über onFormDataChange

## 📝 Geänderte Dateien

### `/frontend/src/components/original/LegacyApp.tsx`
- CustomerFormData State hinzugefügt
- Import des CustomerFormData Types
- Props für CustomerForm und abhängige Komponenten angepasst
- handleClearForm implementiert

### `/frontend/src/components/original/CustomerForm.tsx`
- Interface CustomerFormData exportiert
- Von lokalem State zu Props umgestellt
- handleInputChange und handleVolumeChange angepasst
- Alle State-Updates über onFormDataChange

## 🧪 Technische Details

### Vorher (State geht verloren):
```tsx
// CustomerForm.tsx
const [formData, setFormData] = useState<CustomerFormData>({...});
```

### Nachher (State bleibt erhalten):
```tsx
// LegacyApp.tsx
const [customerFormData, setCustomerFormData] = useState<CustomerFormData>({...});

// CustomerForm.tsx
export function CustomerForm({ formData, onFormDataChange }: CustomerFormProps) {
  // Nutzt Props statt lokalem State
}
```

## ✨ Ergebnis

- ✅ Kettenkunden-Status bleibt beim Tab-Wechsel erhalten
- ✅ Alle Formulardaten persistieren zwischen Tab-Wechseln
- ✅ Dynamische Tab-Anzeige funktioniert korrekt
- ✅ TypeScript-typsicher ohne Fehler

## 💡 Lessons Learned

Bei Tab-basierten Interfaces sollte der State in der übergeordneten Komponente verwaltet werden, wenn die Daten zwischen Tab-Wechseln erhalten bleiben sollen.