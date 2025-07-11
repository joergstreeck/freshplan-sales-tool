# Standard Layout V2 - Design Guidelines

**Dokument:** STANDARD_LAYOUT_V2.md  
**Erstellt:** 11.07.2025  
**Status:** 🟢 Aktiv  
**Zweck:** Definiert den Standard für das neue MainLayoutV2 und alle darauf basierenden Seiten

## 📋 Checkliste für Referenz-Implementierungen

Diese Checkliste definiert die Mindestanforderungen für jede Seite, die als "Goldene Referenz" gilt:

### Layout & Styling
- [ ] Die Seite nutzt die `MainLayoutV2`-Komponente als Basis
- [ ] Alle alten CSS-Dateien und `className`-Attribute wurden entfernt
- [ ] Das Styling erfolgt ausschließlich über das `freshfoodzTheme` und die `sx`-Prop von MUI
- [ ] Die Seite ist vollständig responsive (Desktop, Tablet, Mobile)

### Content & UX
- [ ] Alle Texte entsprechen unserem `/docs/design/WORDING_GUIDE.md`
- [ ] Keyboard-Navigation ist implementiert (wo sinnvoll)
- [ ] Loading States sind klar definiert
- [ ] Error States bieten hilfreiche Handlungsoptionen

### Code-Qualität
- [ ] TypeScript strict mode ist aktiviert
- [ ] Keine `any` Types (außer absolut unvermeidbar)
- [ ] Props sind mit JSDoc dokumentiert
- [ ] Komponenten sind modular und wiederverwendbar

### Testing
- [ ] Es gibt eine solide Basis an Tests (Unit & Integration), die die Kernfunktionalität abdecken
- [ ] Mindestens 80% Code Coverage für Business Logic
- [ ] Kritische User Flows sind getestet
- [ ] Accessibility Tests sind vorhanden

### Dokumentation
- [ ] Alle relevanten Dokumente (Spoke, Hub, Masterplan) sind nach Abschluss konsistent
- [ ] Code-Kommentare erklären das "Warum", nicht das "Was"
- [ ] README oder Inline-Docs für komplexe Komponenten

### Performance
- [ ] Keine unnötigen Re-Renders
- [ ] Lazy Loading wo sinnvoll
- [ ] Bundle Size im Rahmen (< 50KB für Seiten-spezifischen Code)

## 🏗️ MainLayoutV2 Struktur

```typescript
interface MainLayoutV2Props {
  children: React.ReactNode;
  title?: string; // Für document.title
  maxWidth?: 'sm' | 'md' | 'lg' | 'xl' | false; // Container max-width
}
```

### Standard-Struktur:
```
┌─────────────────────────────────────────────────────┐
│ AppBar (Logo | Title | User Menu)                  │
├─────────────┬───────────────────────────────────────┤
│             │                                       │
│  Sidebar    │         Content Area                  │
│  (Drawer)   │         (Container)                   │
│             │                                       │
└─────────────┴───────────────────────────────────────┘
```

## 🎨 Theme-Verwendung

### Farben (Freshfoodz CI)
```typescript
theme.palette.primary.main    // #94C456 - Hauptgrün
theme.palette.secondary.main  // #004F7B - Dunkelblau
theme.palette.background.default // #FFFFFF
theme.palette.text.primary    // #000000
```

### Spacing
```typescript
theme.spacing(1) // 8px
theme.spacing(2) // 16px
theme.spacing(3) // 24px
// etc.
```

### Breakpoints
```typescript
theme.breakpoints.up('sm')    // >= 600px
theme.breakpoints.up('md')    // >= 960px
theme.breakpoints.up('lg')    // >= 1280px
theme.breakpoints.down('sm')  // < 600px
```

## 📐 Responsive Design Patterns

### Mobile First
```typescript
sx={{
  padding: 2,                    // Mobile default
  [theme.breakpoints.up('md')]: {
    padding: 3                   // Desktop override
  }
}}
```

### Grid Layouts
```typescript
<Grid container spacing={2}>
  <Grid item xs={12} md={6} lg={4}>
    {/* Responsive columns */}
  </Grid>
</Grid>
```

## 🚀 Best Practices

1. **Consistency**: Nutze immer die gleichen Patterns
2. **Accessibility**: ARIA labels, Keyboard support, Focus management
3. **Performance**: Memoize expensive computations, virtualize long lists
4. **Error Handling**: User-friendly error messages, recovery options
5. **Loading States**: Skeleton screens > Spinners

## 🏆 Goldene Referenz-Implementierungen

### SettingsPage (M7) - ✅ Vollständig implementiert (11.07.2025)

**Status:** Dies ist unsere erste vollständige Referenz-Implementierung!

#### Erfüllte Kriterien:
- ✅ Nutzt MainLayoutV2 als Basis
- ✅ Keine CSS-Dateien oder className-Attribute
- ✅ Vollständig MUI-basiert mit sx-Props
- ✅ Responsive Design (Tabs mit scrollable und auto scroll buttons)
- ✅ Deutsche Texte (Route: `/einstellungen`)
- ✅ Umfassende Test-Suite (10 Tests, 100% der User Stories abgedeckt)
- ✅ ARIA-Labels und Keyboard-Navigation
- ✅ Loading und Error States implementiert
- ✅ Modular aufgebaut (UserTableMUI, UserFormMUI)

#### Besonderheiten:
- Tab-basierte Navigation für verschiedene Einstellungsbereiche
- Integration der Benutzerverwaltung
- Placeholder für zukünftige Systemeinstellungen und Sicherheit
- Konsistente Verwendung des freshfoodzTheme

#### Lessons Learned:
1. **Theme-Farben**: Hardcodierte Farben (#94C456) durch theme.palette.primary ersetzt
2. **Route-Konsistenz**: Alle Links müssen auf `/einstellungen` zeigen (nicht `/settings`)
3. **Test-Isolation**: Bei Tests mit MainLayoutV2 auf spezifische Selektoren achten (z.B. heading level)

### Nächste Kandidaten für Referenz-Implementierung:
- [ ] M8 (Rechner) - Backend 95% wiederverwendbar
- [ ] M4 (Neukundengewinnung) - Neubau erforderlich
- [ ] M5 (Kundenmanagement) - Komplexestes Modul

---

*Dieses Dokument wird kontinuierlich erweitert, während wir mehr Module migrieren.*