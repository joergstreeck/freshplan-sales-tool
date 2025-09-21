# 📊 CSS zu MUI Mapping-Referenz

**Projekt:** FreshPlan Sales Tool  
**Datum:** 10.07.2025  
**Zweck:** Schnelle Referenz für CSS-zu-MUI Migration

## 🎨 Allgemeine Patterns

### Container & Layout
| CSS-Klasse | MUI sx-Props |
|------------|--------------|
| `.my-day-column` | `{ height: '100%', display: 'flex', flexDirection: 'column' }` |
| `.column-header` | `{ p: 2, borderBottom: 1, borderColor: 'divider', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }` |
| `.column-content` | `{ flex: 1, overflow: 'auto', p: 2 }` |
| `.column-loading` | `{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%' }` |

### Typography
| CSS-Klasse | MUI-Komponente |
|------------|----------------|
| `.column-title` | `<Typography variant="h6">` |
| `.section-title` | `<Typography variant="subtitle1" gutterBottom>` |
| `.task-title` | `<Typography variant="body1">` |
| `.task-customer` | `<Typography variant="body2" color="text.secondary">` |
| `.error-message` | `<Typography color="error">` |

### Buttons & Actions
| CSS-Klasse | MUI-Komponente |
|------------|----------------|
| `.btn-icon` | `<IconButton size="small">` |
| `.retry-button` | `<Button variant="outlined" startIcon={<RefreshIcon />}>` |
| `.alert-action-btn` | `<Button size="small" variant="text">` |
| `.triage-action` | `<Button size="small" variant="outlined">` |

### Cards & Sections
| CSS-Klasse | MUI sx-Props |
|------------|--------------|
| `.alerts-section` | `<Box sx={{ mb: 3 }}>` |
| `.alert-item` | `<Card sx={{ mb: 1, p: 1.5 }}>` |
| `.alert-high` | `{ bgcolor: 'error.light', color: 'error.dark' }` |
| `.alert-medium` | `{ bgcolor: 'warning.light', color: 'warning.dark' }` |
| `.task-item` | `<Card sx={{ mb: 1, display: 'flex', alignItems: 'center', p: 1 }}>` |

### Spezifische Komponenten

#### Loading Spinner
```typescript
// ALT
<div className="loading-spinner"></div>

// NEU
<CircularProgress size={40} />
```

#### Error State
```typescript
// ALT
<div className="column-error">
  <div className="error-icon">⚠️</div>
  <h3>Fehler beim Laden</h3>
</div>

// NEU
<Alert severity="error" sx={{ m: 2 }}>
  <AlertTitle>Fehler beim Laden</AlertTitle>
  {error.message}
</Alert>
```

#### Toggle Icon
```typescript
// ALT
<svg className={`toggle-icon ${showTriageInbox ? 'expanded' : ''}`}>

// NEU
<ExpandMoreIcon 
  sx={{ 
    transform: showTriageInbox ? 'rotate(180deg)' : 'rotate(0deg)',
    transition: 'transform 0.2s'
  }} 
/>
```

## 🎨 Farben-Mapping (Freshfoodz CI)

| CSS-Variable | MUI Theme |
|--------------|-----------|
| `#94C456` | `primary.main` |
| `#004F7B` | `secondary.main` |
| `#e8f5e9` | `success.light` |
| `var(--gray-100)` | `grey[100]` |
| `var(--gray-200)` | `grey[200]` |

## 📐 Spacing-Konventionen

| CSS | MUI |
|-----|-----|
| `margin: 1rem` | `m: 2` (1rem = theme.spacing(2)) |
| `padding: 0.5rem` | `p: 1` |
| `margin-bottom: 1rem` | `mb: 2` |
| `gap: 1rem` | `gap: 2` |

## 🔄 Responsive Breakpoints

```typescript
// CSS Media Queries
@media (max-width: 768px) { }

// MUI Breakpoints
sx={{
  display: { xs: 'none', md: 'block' },
  gridTemplateColumns: { 
    xs: '1fr',           // mobile
    md: 'repeat(3, 1fr)' // desktop
  }
}}
```

## ⚡ Performance-Tipps

1. **Statische Styles extrahieren:**
```typescript
const columnStyles = {
  height: '100%',
  display: 'flex',
  flexDirection: 'column'
} as const;

<Box sx={columnStyles}>
```

2. **Bedingte Styles vermeiden:**
```typescript
// Schlecht
sx={{ color: isActive ? 'primary.main' : 'text.secondary' }}

// Besser
sx={isActive ? activeStyles : inactiveStyles}
```

3. **Theme-Funktionen cachen:**
```typescript
const useStyles = () => ({
  root: {
    p: (theme) => theme.spacing(2),
    bgcolor: (theme) => theme.palette.background.paper
  }
});
```

## 🚨 Häufige Fehler

1. **Grid2 Import** → Use Box with CSS Grid
2. **className mixing** → Nur sx-Props verwenden
3. **CSS-Dateien importieren** → Alle Styles inline
4. **px-Werte** → Theme spacing verwenden
5. **Hardcoded Farben** → Theme colors nutzen

---

Diese Referenz während der Migration offen halten für schnelle Lookups!