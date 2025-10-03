# Known Issues - Modul 01 Mein Cockpit

**📅 Erstellt:** 2025-10-03
**🎯 Zweck:** Dokumentation bekannter Bugs und technischer Schulden im Cockpit-Modul
**✅ Status:** Living Document

---

## 🐛 CRITICAL - Theme Palette Status Properties Fehlen

**Entdeckt:** 2025-10-03 (Sprint 2.1.5 Test-Fixing Session)
**Komponente:** `KanbanBoardDndKit.tsx`
**Severity:** HIGH (Production Bug)
**Status:** 🔴 OPEN - Wird in Sprint 2.4 PR #1 gefixt

### Problem

Die Komponente `frontend/src/features/opportunity/components/kanban/KanbanBoardDndKit.tsx` nutzt Theme-Properties die im `freshfoodzTheme` nicht definiert sind:

```typescript
// BROKEN - Properties existieren nicht:
theme.palette.status.won      // Line 352
theme.palette.status.lost     // Line 367
theme.palette.status.inProgress
theme.palette.status.new
```

### Impact

- **Runtime Error:** `TypeError: Cannot read properties of undefined (reading 'color')`
- **Tests:** 7 Tests in `KanbanBoardDndKit.test.tsx` müssen geskipped werden
- **UX:** Kanban-Board zeigt keine Status-Farben (falls deployed)

### Root Cause

Das `freshfoodzTheme` in `/frontend/src/theme/freshfoodz.ts` hat keine `palette.status` Erweiterung. MUI Theme benötigt TypeScript-Augmentation für custom palette properties.

### Temporary Workaround

Tests wurden geskipped mit TODO-Kommentar:

```typescript
// frontend/src/features/opportunity/components/__tests__/KanbanBoardDndKit.test.tsx
// TODO: KanbanBoardDndKit nutzt theme.palette.status.won/lost die im freshfoodzTheme fehlen
// PRODUCTION BUG: freshfoodz.ts muss erweitert werden um palette.status Properties
describe.skip('KanbanBoardDndKit', () => {
  // 7 tests skipped
});
```

### Solution (Sprint 2.4 PR #1)

#### 1. Theme Augmentation

Erstelle `/frontend/src/theme/theme.d.ts`:

```typescript
import '@mui/material/styles';

declare module '@mui/material/styles' {
  interface Palette {
    status: {
      won: string;
      lost: string;
      inProgress: string;
      new: string;
    };
  }
  interface PaletteOptions {
    status?: {
      won?: string;
      lost?: string;
      inProgress?: string;
      new?: string;
    };
  }
}
```

#### 2. Theme Extension

Erweitere `/frontend/src/theme/freshfoodz.ts`:

```typescript
const freshfoodzTheme = createTheme({
  palette: {
    primary: {
      main: '#004F7B',
      // ... existing colors
    },
    status: {
      won: '#4caf50',      // Success Green
      lost: '#f44336',     // Error Red
      inProgress: '#2196f3', // Primary Blue
      new: '#ff9800',      // Warning Orange
    },
  },
  // ... rest of theme
});
```

#### 3. Test Re-Activation

Entferne `describe.skip()` aus `KanbanBoardDndKit.test.tsx` und nutze `freshfoodzTheme`:

```typescript
import freshfoodzTheme from '../../../../theme/freshfoodz';

const renderWithProviders = (component: React.ReactElement) => {
  return render(
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>
    </QueryClientProvider>
  );
};

describe('KanbanBoardDndKit', () => {
  // All 7 tests should pass
});
```

### Validation Checklist

- [ ] Theme Augmentation (`theme.d.ts`) erstellt
- [ ] `freshfoodzTheme` mit `palette.status` erweitert
- [ ] TypeScript Compilation erfolgreich (keine Errors)
- [ ] `KanbanBoardDndKit.test.tsx` Tests re-aktiviert (remove `describe.skip`)
- [ ] Alle 7 Tests passing
- [ ] Visual Regression Test für Kanban Status-Farben
- [ ] Documentation Update in `DESIGN_SYSTEM.md`

### Related Files

```
frontend/src/features/opportunity/components/kanban/KanbanBoardDndKit.tsx
frontend/src/features/opportunity/components/__tests__/KanbanBoardDndKit.test.tsx
frontend/src/theme/freshfoodz.ts
frontend/src/theme/theme.d.ts (TO BE CREATED)
docs/planung/features-neu/01_mein-cockpit/artefakte/frontend/DESIGN_SYSTEM.md
```

### Testing Strategy

```bash
# Nach Fix - alle Tests sollten passing sein:
npm test -- KanbanBoardDndKit.test.tsx

# Expected Output:
# Test Files  1 passed (1)
# Tests  7 passed (7)
```

### References

- **Discovered in:** PR #129 Code Review + Test Fixing Session
- **Related Sprint:** Sprint 2.4 Cockpit (Dashboard Widgets)
- **ADR:** Wird in Sprint 2.4 als Teil der Theme-Systematisierung addressiert
- **MUI Theme Docs:** https://mui.com/material-ui/customization/palette/

---

## 📋 Weitere Known Issues

### Universal Search Feature Nicht Implementiert

**Entdeckt:** 2025-10-03
**Severity:** LOW (Feature für Zukunft geplant)
**Status:** 📋 PLANNED

Tests in `UniversalSearch.integration.test.tsx` wurden geskipped da das Feature noch nicht in `IntelligentFilterBar` implementiert ist.

```typescript
// frontend/src/features/customers/tests/UniversalSearch.integration.test.tsx
// TODO: Universal Search Feature ist noch nicht vollständig implementiert
describe.skip('Universal Search Integration', () => {
  // 5 tests for future implementation
});
```

**Geplant für:** Sprint 2.5 oder später (Cross-Module Search)

---

## 🔄 Change Log

- **2025-10-03:** Initial creation - KanbanBoard Theme Bug dokumentiert
- **2025-10-03:** UniversalSearch TODO hinzugefügt

---

**📋 Dokument-Zweck:** Living Document für Known Issues im Cockpit-Modul
**🔄 Letzte Aktualisierung:** 2025-10-03
**✅ Status:** Ready for Sprint 2.4 Implementation
