# 🚀 Detaillierter Migrationsplan: Sales Cockpit zu MUI

**Datum:** 10.07.2025  
**Feature:** FC-002-M3 (Cockpit-Integration)  
**Ziel:** Migration der CSS-basierten Cockpit-Komponenten zu MUI  
**Geschätzte Dauer:** 2-3 Stunden

## 📋 Ausgangslage

### Was wir haben:
1. **SalesCockpitV2.tsx** - MUI-basiertes Grundgerüst mit Platzhaltern
2. **MyDayColumn.tsx** - Vollständige Funktionalität, aber CSS-basiert
3. **FocusListColumn.tsx** - Bereits teilweise modernisiert (nutzt FC-001)
4. **ActionCenterColumn.tsx** - Basis-Implementierung

### Bewährte Lösungen aus der Dokumentation:
- **Grid2 Problem:** Nicht verfügbar in MUI v7 → Box mit `display: 'grid'` verwenden
- **CSS @import:** Entfernen und Fonts via HTML laden
- **FieldPath Import:** `keyof FormData` statt `FieldPath<FormData>`

## 🎯 Migrations-Strategie

### Phase 1: MyDayColumn Migration (45 Min)

#### 1.1 CSS-Klassen-Mapping
```typescript
// ALT (CSS)
<div className="my-day-column">
<div className="column-header">
<h2 className="column-title">

// NEU (MUI)
<Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
<Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
<Typography variant="h6" sx={{ color: 'primary.main' }}>
```

#### 1.2 Komponenten-Struktur
```typescript
// MyDayColumnMUI.tsx
import { Box, Typography, Card, CardContent, Stack, IconButton, Chip, Button, CircularProgress, Alert } from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import NotificationsIcon from '@mui/icons-material/Notifications';
import TaskIcon from '@mui/icons-material/Task';
import EmailIcon from '@mui/icons-material/Email';
```

#### 1.3 Spezifische Anpassungen
- **Loading State:** CircularProgress mit Box-Wrapper
- **Error State:** Alert-Komponente statt custom div
- **Triage Toggle:** IconButton mit Rotation
- **Task Icons:** MUI Icons statt Emojis
- **Scroll Container:** Box mit `overflow: 'auto'`

### Phase 2: FocusListColumn Anpassung (30 Min)

#### 2.1 Minimale Änderungen (bereits 80% MUI)
```typescript
// ALT
<div className="focus-list-column">

// NEU
<Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
```

#### 2.2 Integration prüfen
- FilterBar von FC-001 ist bereits MUI
- CustomerCard anpassen falls nötig
- View-Toggle bereits modern

### Phase 3: ActionCenterColumn Neubau (45 Min)

#### 3.1 Struktur
```typescript
export function ActionCenterColumnMUI({ selectedCustomerId }: { selectedCustomerId?: string }) {
  if (!selectedCustomerId) {
    return (
      <Box sx={{ 
        height: '100%', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        p: 3 
      }}>
        <Typography color="text.secondary">
          Wählen Sie einen Kunden aus der Fokus-Liste
        </Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* Header */}
      {/* Customer Details */}
      {/* Quick Actions */}
      {/* Activity Timeline */}
    </Box>
  );
}
```

### Phase 4: Integration in SalesCockpitV2 (30 Min)

#### 4.1 Platzhalter ersetzen
```typescript
// Statt Platzhalter-Content:
<MyDayColumn />
<FocusListColumn />
<ActionCenterColumn selectedCustomerId={selectedCustomerId} />
```

#### 4.2 State Management
```typescript
const [selectedCustomerId, setSelectedCustomerId] = useState<string>();
const [showTriageInbox, setShowTriageInbox] = useState(true);
```

#### 4.3 Grid-Layout beibehalten
```typescript
<Box sx={{ 
  display: 'grid', 
  gridTemplateColumns: { 
    xs: '1fr', 
    md: 'repeat(3, 1fr)' 
  },
  gap: 2,
  height: '100%' 
}}>
```

## 🔧 Technische Details

### MUI Theme Integration
```typescript
// Freshfoodz CI Farben nutzen
sx={{ 
  color: 'primary.main',      // #94C456
  bgcolor: 'secondary.main',  // #004F7B
}}
```

### Performance-Optimierungen
1. **Memo verwenden** für schwere Komponenten
2. **Virtual Scrolling** für lange Listen (später)
3. **Lazy Loading** für Timeline (später)

### Type Safety
```typescript
interface MyDayColumnProps {
  data?: DashboardData;
  onRefresh: () => void;
  isLoading?: boolean;
}
```

## ⚠️ Bekannte Fallstricke

1. **Keine Grid2 imports!** - Verwende Box mit CSS Grid
2. **CSS-Dateien nicht importieren** - Alle Styles inline mit sx
3. **Store-Integration** - cockpitStore muss angepasst werden
4. **Mock-Daten** - Schrittweise durch echte Daten ersetzen

## 📈 Erfolgs-Kriterien

- [ ] Alle CSS-Klassen durch MUI sx-Props ersetzt
- [ ] Keine CSS-Imports mehr
- [ ] TypeScript strict mode aktiviert
- [ ] Responsive Design funktioniert
- [ ] Alle Features der Original-Komponenten erhalten
- [ ] Performance nicht verschlechtert

## 🧪 Test-Plan

1. **Unit Tests:** Komponenten-Tests anpassen
2. **Integration:** Mit MainLayoutV2 testen
3. **Responsive:** Mobile/Tablet/Desktop prüfen
4. **Browser:** Chrome, Firefox, Safari
5. **Performance:** Loading-Zeiten messen

## 🚀 Implementierungs-Reihenfolge

1. **MyDayColumn** komplett migrieren → MyDayColumnMUI.tsx
2. **In SalesCockpitV2** einbinden und testen
3. **FocusListColumn** anpassen (minimal)
4. **ActionCenterColumn** neu bauen
5. **Finale Integration** und Cleanup
6. **Tests** anpassen

## 📝 Notizen aus vorherigen Migrationen

- Grid2 funktioniert nicht → immer Box mit CSS Grid
- @import Probleme → Fonts via index.html
- Performance bei vielen MUI-Komponenten → sx-Props cachen
- Responsive Breakpoints → xs/sm/md/lg/xl verwenden

---

**Nächster Schritt:** Mit Phase 1 beginnen - MyDayColumn zu MUI migrieren