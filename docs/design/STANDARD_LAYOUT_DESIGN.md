# 📐 Standard Layout Design - FreshPlan 2.0

**Status:** ✅ VERBINDLICH  
**Datum:** 09.07.2025  
**Letzte Änderung:** 09.07.2025 - Header fixed positioning  
**Bestätigt von:** Jörg  

## 🎯 Das ist unser Standard-Design

Alle Seiten im FreshPlan Sales Tool bauen auf diesem Layout auf.

## 📸 Referenz-Screenshot

![Standard Layout](./screenshots/standard-layout-2025-07-09.png)
*Settings-Seite zeigt das Standard-Layout*

## 🏗️ Layout-Struktur

```
┌─────────────────────────────────────────────────────────────┐
│  HEADER (weiß, fixed)                                       │
│  [🟢 Logo] [🔍 Suche...............] [🔔] [👤 User ▼]      │
└─────────────────────────────────────────────────────────────┘
├─ Schatten (0 2px 4px rgba(0,0,0,0.08)) ────────────────────┤

┌─────┬───────────────────────────────────────────────────────┐
│     │  CONTENT AREA (grauer Hintergrund #FAFAFA)           │
│  S  │                                                        │
│  I  │  ┌─────────────────────────────────────────────┐     │
│  D  │  │  Paper Container (weiß)                      │     │
│  E  │  │                                              │     │
│  B  │  │  Hier kommt der Seiteninhalt                │     │
│  A  │  │                                              │     │
│  R  │  └─────────────────────────────────────────────┘     │
│     │                                                        │
└─────┴───────────────────────────────────────────────────────┘
```

## 🎨 Design-Elemente

### Header
- **Hintergrund:** #FFFFFF (weiß)
- **Höhe:** 64px
- **Position:** Fixed am oberen Rand (scrollt nicht mit)
- **Schatten:** `0 2px 4px rgba(0,0,0,0.08)`
- **Border-Bottom:** `2px solid #94C456` (Freshfoodz Grün)
- **Positionierung:** Links neben der Sidebar (nicht darüber)
- **Breite:** Passt sich an Sidebar-Status an (ausgeklappt/eingeklappt)

### Header-Inhalte
1. **Logo (links)**
   - Freshfoodz Logo
   - Höhe: 40px
   - Klickbar → Navigation zur Startseite

2. **Suchleiste (mitte)**
   - Placeholder: "Suche nach Kunden, Aufträgen oder Produkten..."
   - Maximale Breite: 400px
   - Grüner Fokus-Rahmen (#94C456)

3. **Notifications (rechts)**
   - Badge mit Anzahl
   - Icon-Button Style

4. **User-Menu (ganz rechts)**
   - Avatar mit Initialen
   - Dropdown mit Logout

### Sidebar
- **Breite:** 320px (ausgeklappt) / 64px (eingeklappt)
- **Hintergrund:** #FAFAFA
- **Border-Right:** `2px solid #94C456`
- **FreshPlan Logo** in der Sidebar

### Content-Bereich
- **Hintergrund:** #FAFAFA (leicht grau)
- **Padding:** Abhängig vom Inhalt
- **Container:** Paper-Components für Inhalte

## 💻 Technische Umsetzung

### Verwendung in Pages

```typescript
import { MainLayoutV2 } from '@/components/layout/MainLayoutV2';

export function MyPage() {
  return (
    <MainLayoutV2>
      <Typography variant="h4" gutterBottom sx={{ mb: 4 }}>
        Seitentitel
      </Typography>
      
      <Paper sx={{ width: '100%' }}>
        {/* Seiteninhalt */}
      </Paper>
    </MainLayoutV2>
  );
}
```

### Container-Optionen

Für verschiedene Inhaltstypen:

```typescript
// Volle Breite (z.B. für Tabellen)
<Paper sx={{ width: '100%' }}>

// Begrenzte Breite (z.B. für Formulare)
<Paper sx={{ maxWidth: 800, mx: 'auto' }}>

// Mit Padding
<Paper sx={{ p: 3 }}>
```

## ✅ Checkliste für neue Seiten

- [ ] MainLayoutV2 als Wrapper verwenden
- [ ] Seitentitel als Typography h4 mit mb: 4
- [ ] Inhalte in Paper-Container wrappen
- [ ] Freshfoodz CI-Farben verwenden (#94C456, #004F7B)
- [ ] Responsive Design testen
- [ ] Loading States berücksichtigen
- [ ] Error States implementieren

## 🚫 Was NICHT zu tun ist

- ❌ Keine eigenen Header erstellen
- ❌ Keine Custom-Layouts ohne Abstimmung
- ❌ Keine Änderungen an MainLayoutV2 ohne ADR
- ❌ Keine anderen Hintergrundfarben
- ❌ Kein Logo in Pages (nur im Layout)

## 📋 Bereits umgesetzte Seiten

1. **SettingsPage** ✅ - Referenz-Implementierung
2. **UsersPage** ⏳ - Migration ausstehend
3. **CalculatorPage** ⏳ - Migration ausstehend
4. **CockpitPage** ⏳ - Migration ausstehend

---

**Dieses Design ist verbindlich für alle Seiten im FreshPlan Sales Tool.**