# Dark Mode & Themes - Personalisierte UI ⚡

**Feature Code:** V-UX-001  
**Feature-Typ:** 🎨 FRONTEND  
**Geschätzter Aufwand:** 8-10 Tage  
**Priorität:** VISION - User Experience  
**ROI:** Höhere User Satisfaction, weniger Augenbelastung  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** "Abends brennen mir die Augen" + "Sieht aus wie 2010"  
**Lösung:** Dark Mode + Custom Themes + Auto-Switch  
**Impact:** Moderne UI die sich anpasst  

---

## 🎨 THEME SYSTEM

```
1. PRESETS
   Light | Dark | Auto (Systemzeit)
   
2. CUSTOM THEMES
   Freshfoodz Green | Ocean Blue | Sunset Orange
   
3. ACCESSIBILITY
   High Contrast | Larger Text | Reduced Motion
   
4. BRAND THEMES
   Kunde kann eigene Farben definieren
```

---

## 🏃 IMPLEMENTATION KONZEPT

### Theme Engine
```typescript
// Theme System mit CSS Variables
export interface Theme {
  name: string;
  mode: 'light' | 'dark';
  colors: {
    primary: string;
    secondary: string;
    background: {
      default: string;
      paper: string;
      elevated: string;
    };
    text: {
      primary: string;
      secondary: string;
      disabled: string;
    };
    action: {
      active: string;
      hover: string;
      selected: string;
      disabled: string;
    };
    status: {
      error: string;
      warning: string;
      info: string;
      success: string;
    };
  };
  shadows: string[];
  transitions: {
    duration: {
      short: number;
      standard: number;
      complex: number;
    };
  };
}

// Theme Provider
export const ThemeProvider: React.FC = ({ children }) => {
  const [theme, setTheme] = useState<Theme>(getStoredTheme());
  const [mode, setMode] = useState<'light' | 'dark' | 'auto'>('auto');
  
  // Auto-Switch based on time
  useEffect(() => {
    if (mode === 'auto') {
      const hour = new Date().getHours();
      const isDark = hour < 6 || hour > 20;
      setTheme(isDark ? themes.dark : themes.light);
    }
  }, [mode]);
  
  // Apply CSS Variables
  useEffect(() => {
    const root = document.documentElement;
    Object.entries(theme.colors).forEach(([key, value]) => {
      if (typeof value === 'object') {
        Object.entries(value).forEach(([subKey, subValue]) => {
          root.style.setProperty(`--color-${key}-${subKey}`, subValue);
        });
      } else {
        root.style.setProperty(`--color-${key}`, value);
      }
    });
  }, [theme]);
  
  return (
    <ThemeContext.Provider value={{ theme, setTheme, mode, setMode }}>
      <MuiThemeProvider theme={createMuiTheme(theme)}>
        {children}
      </MuiThemeProvider>
    </ThemeContext.Provider>
  );
};
```

### Component Theming
```scss
// Themeable Component Styles
.card {
  background-color: var(--color-background-paper);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-1);
  transition: all var(--transition-standard);
  
  &:hover {
    background-color: var(--color-background-elevated);
    box-shadow: var(--shadow-2);
  }
  
  &.dark-mode {
    border: 1px solid var(--color-divider);
  }
}

// Utility Classes
.text-primary { color: var(--color-text-primary); }
.text-secondary { color: var(--color-text-secondary); }
.bg-default { background-color: var(--color-background-default); }
.bg-paper { background-color: var(--color-background-paper); }
```

### Theme Customizer
```typescript
export const ThemeCustomizer: React.FC = () => {
  const { theme, setTheme } = useTheme();
  const [customTheme, setCustomTheme] = useState(theme);
  
  return (
    <Drawer anchor="right" open={open} onClose={onClose}>
      <Box sx={{ width: 320, p: 3 }}>
        <Typography variant="h6">Theme anpassen</Typography>
        
        {/* Mode Selector */}
        <ToggleButtonGroup value={mode} exclusive onChange={handleModeChange}>
          <ToggleButton value="light">
            <LightModeIcon />
          </ToggleButton>
          <ToggleButton value="dark">
            <DarkModeIcon />
          </ToggleButton>
          <ToggleButton value="auto">
            <AutoModeIcon />
          </ToggleButton>
        </ToggleButtonGroup>
        
        {/* Color Pickers */}
        <Box sx={{ mt: 3 }}>
          <Typography variant="subtitle2">Primärfarbe</Typography>
          <ChromePicker
            color={customTheme.colors.primary}
            onChange={(color) => updateThemeColor('primary', color.hex)}
          />
        </Box>
        
        {/* Preview */}
        <Box sx={{ mt: 3, p: 2, borderRadius: 1, ...previewStyles }}>
          <Typography variant="subtitle2">Vorschau</Typography>
          <Button variant="contained">Primär Button</Button>
          <Button variant="outlined">Outlined Button</Button>
        </Box>
        
        {/* Actions */}
        <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
          <Button onClick={resetTheme}>Zurücksetzen</Button>
          <Button variant="contained" onClick={saveTheme}>
            Speichern
          </Button>
        </Box>
      </Box>
    </Drawer>
  );
};
```

---

## 🔗 TECHNICAL CONSIDERATIONS

**Performance:**
- CSS Variables für instant switching
- No Flash of Unstyled Content
- Lazy load theme assets

**Accessibility:**
- WCAG AAA contrast ratios
- Reduced motion option
- Focus indicators anpassen

**Storage:**
- LocalStorage für User Preference
- Backend sync für Cross-Device

---

## ⚡ PREMIUM FEATURES

1. **Team Themes:** Firma definiert Brand Colors
2. **Schedule Themes:** Andere Themes zu anderen Zeiten
3. **Activity Themes:** Fokus-Modus = Minimal Theme
4. **Holiday Themes:** Weihnachten, Ostern, etc.

---

## 📊 SUCCESS METRICS

- **Adoption:** 60% nutzen Dark Mode
- **Retention:** +15% durch bessere UX
- **Accessibility:** 100% WCAG konform
- **Performance:** <50ms Theme Switch

---

## 🚀 ROLLOUT PLAN

**Phase 1:** Dark/Light Mode Toggle  
**Phase 2:** Auto-Switch + Preferences  
**Phase 3:** Custom Color Themes  
**Phase 4:** Full Customization  

---

**Design System:** [THEME_DESIGN_SYSTEM.md](./THEME_DESIGN_SYSTEM.md)  
**Migration Guide:** [THEME_MIGRATION.md](./THEME_MIGRATION.md)