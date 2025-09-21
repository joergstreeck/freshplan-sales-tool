# 🎨 Component Library - FreshPlan Design System

**Erstellt:** 2025-09-17
**Status:** ✅ Konsolidiert aus bestehendem System
**Tech Stack:** Material-UI v7.2.0 + React 18 + TypeScript
**Basis:** Analysis of existing codebase, not assumptions

## 📋 Current State Analysis

### **KORREKTUR ALTER ADRS:**
❌ **ADR-004 (veraltet):** Entscheidung war Tailwind CSS
✅ **AKTUELLER STAND:** Material-UI v7.2.0 ist implementiert und produktiv

**Beweise aus Codebase:**
- `package.json`: "@mui/material": "^7.2.0"
- Verwendung in allen Components: `import { Box, Typography } from '@mui/material'`
- Theme-Integration aktiv: `styled(Box)(({ theme }) => ({ ... }))`

## 🏗️ Architecture Overview

### **Current Design System Stack:**
```yaml
Core Framework: Material-UI v7.2.0
Theme Engine: MUI Emotion-based theming
Styling: styled() + sx prop pattern
Layout System: SmartLayout + MainLayoutV3
Icons: @mui/icons-material v7.2.0
Colors: Custom Palette (FreshPlan Corporate Colors)
Typography: Antonio Bold + Poppins (Freshfoodz CI compliant)
```

### **Layout Evolution:**
```
MainLayoutV1 → MainLayoutV2 → MainLayoutV3 + SmartLayout
```

**Current Best Practice:** MainLayoutV3 mit SmartLayout für automatische Content-Breiten

## 🎨 Theme Configuration

### **Corporate Colors (Freshfoodz CI Compliant):**
```typescript
// Primary Colors (MUST USE - Freshfoodz CI #94)
const freshplanTheme = {
  palette: {
    primary: {
      main: '#004F7B',        // FreshPlan Blue
      contrastText: '#FFFFFF'
    },
    success: {
      main: '#94C456',        // FreshPlan Green
      contrastText: '#000000'
    },
    background: {
      default: '#FAFAFA',     // Light Gray Background
      paper: '#FFFFFF'        // White Cards/Papers
    },
    text: {
      primary: 'rgba(0, 0, 0, 0.87)',
      secondary: 'rgba(0, 0, 0, 0.6)'
    }
  }
};
```

### **Typography System:**
```typescript
typography: {
  fontFamily: 'Poppins, sans-serif',     // Body text
  h1: { fontFamily: 'Antonio Bold' },    // Headers
  h2: { fontFamily: 'Antonio Bold' },
  h3: { fontFamily: 'Antonio Bold' },
  h4: { fontFamily: 'Antonio Bold' },
}
```

## 🧩 Component Patterns

### **1. Styled Components Pattern**
```typescript
// ✅ CORRECT - Use theme variables
const SectionContainer = styled(Box)(({ theme }) => ({
  marginBottom: theme.spacing(2),
  backgroundColor: theme.palette.background.default,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: theme.shape.borderRadius,
}));

// ❌ WRONG - Hardcoded values
const BadContainer = styled(Box)({
  marginBottom: '16px',
  backgroundColor: '#FAFAFA',
  border: '1px solid #E0E0E0',
});
```

### **2. sx Prop Pattern**
```typescript
// ✅ CORRECT - Responsive with theme
<Box sx={{
  backgroundColor: theme.palette.background.paper,
  padding: { xs: 1, md: 2 },
  color: theme.palette.text.primary,
}}>
  Content
</Box>

// ❌ WRONG - Hardcoded colors
<Box sx={{
  backgroundColor: '#FFFFFF',
  padding: '16px',
  color: '#000000',
}}>
  Content
</Box>
```

### **3. SmartLayout Pattern**
```typescript
// ✅ NEW STANDARD - SmartLayout automatically handles content width
import { MainLayoutV3 } from '@/components/layout/MainLayoutV3';

function MyPage() {
  return (
    <MainLayoutV3>
      <Typography variant="h4">Page Title</Typography>
      {/* SmartLayout automatically detects content type and applies optimal width */}
      <DataGrid /> {/* → Full width for tables */}
      <TextField /> {/* → Limited width for forms */}
    </MainLayoutV3>
  );
}

// ❌ OLD PATTERN - Manual container management
function OldPage() {
  return (
    <MainLayoutV2>
      <Container maxWidth="lg">
        <Paper sx={{ p: 2 }}>
          {/* Manual width management */}
        </Paper>
      </Container>
    </MainLayoutV2>
  );
}
```

## 🔧 Component Inventory

### **Layout Components:**
| Component | Status | Use Case | Migration Notes |
|-----------|--------|----------|-----------------|
| `MainLayoutV3` | ✅ Current | All new pages | Includes SmartLayout |
| `SmartLayout` | ✅ Current | Auto content width | Detects content type |
| `MainLayoutV2` | 🟡 Legacy | Backward compatibility | Migrate to V3 |
| `HeaderV2` | ✅ Current | App header | Logo right-aligned |

### **Form Components:**
| Component | Status | Location | Purpose |
|-----------|--------|----------|---------|
| `DynamicFieldRenderer` | ✅ Active | `features/customers/components/fields/` | Dynamic form fields |
| `AdaptiveFormContainer` | ✅ Active | Customer management | Theme-aware forms |
| `FieldGroupContainer` | ✅ Active | Field groupings | Styled form sections |

### **Navigation Components:**
| Component | Status | Performance | Notes |
|-----------|--------|-------------|-------|
| `BreadcrumbNavigation` | ✅ Enterprise | 90% | Theme-compliant colors |
| `NavigationSubMenu` | ✅ Enterprise | 90% | Lazy loading implemented |
| `KeyboardShortcutsHelp` | ✅ Enterprise | 90% | Theme variables used |

## 🎯 Component Standards

### **1. Theme Integration Requirements:**
- [ ] **MANDATORY:** Use `theme.palette.*` for ALL colors
- [ ] **MANDATORY:** Use `theme.spacing()` for spacing
- [ ] **MANDATORY:** Use `theme.breakpoints` for responsive design
- [ ] **FORBIDDEN:** Hardcoded colors (#004F7B, #94C456, etc.)
- [ ] **FORBIDDEN:** Hardcoded spacing (16px, 8px, etc.)

### **2. TypeScript Standards:**
```typescript
// Component Props Interface
interface ComponentProps {
  variant?: 'primary' | 'secondary';
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
  sx?: SxProps<Theme>;
}

// Forward Ref Pattern
export const Component = React.forwardRef<HTMLElement, ComponentProps>(
  ({ variant = 'primary', sx, ...props }, ref) => {
    const theme = useTheme();

    return (
      <Box
        ref={ref}
        sx={{
          backgroundColor: theme.palette.primary.main,
          ...sx,
        }}
        {...props}
      />
    );
  }
);
```

### **3. Accessibility Standards:**
```typescript
// Required ARIA attributes
<Component
  role="button"
  aria-label="Descriptive label"
  aria-describedby="help-text"
  tabIndex={0}
/>

// Keyboard navigation support
const handleKeyDown = (event: React.KeyboardEvent) => {
  if (event.key === 'Enter' || event.key === ' ') {
    onClick?.(event);
  }
};
```

## 📊 Performance Standards

### **Current Metrics (From Navigation System):**
- **Performance:** 90% (Lighthouse)
- **Code Quality:** 90% (SonarCloud)
- **Security:** 95% (Security Audit)
- **Bundle Impact:** < 5KB per component

### **Optimization Techniques:**
```typescript
// 1. Memoization for expensive computations
const expensiveStyles = useMemo(() => ({
  complexCalculation: calculateComplexStyle(props),
}), [props.dependency]);

// 2. Lazy loading for heavy components
const HeavyComponent = lazy(() => import('./HeavyComponent'));

// 3. Component memoization
export const OptimizedComponent = React.memo(Component);
```

## 🧪 Testing Standards

### **Testing Requirements:**
- [ ] **Unit Tests:** >80% coverage
- [ ] **Component Tests:** React Testing Library
- [ ] **Visual Tests:** Storybook stories
- [ ] **Accessibility Tests:** axe-core integration

### **Test Patterns:**
```typescript
describe('Component', () => {
  it('uses theme colors correctly', () => {
    const { theme } = render(<Component />);
    const component = screen.getByTestId('component');

    expect(component).toHaveStyle({
      backgroundColor: theme.palette.primary.main,
      color: theme.palette.primary.contrastText,
    });
  });

  it('handles keyboard interaction', () => {
    const handleClick = vi.fn();
    render(<Component onClick={handleClick} />);

    fireEvent.keyDown(screen.getByRole('button'), { key: 'Enter' });
    expect(handleClick).toHaveBeenCalled();
  });
});
```

## 🚀 Migration Roadmap

### **Phase 1: Layout System Consolidation**
- [ ] Migrate all pages from MainLayoutV2 → MainLayoutV3
- [ ] Remove manual Container/Paper patterns
- [ ] Adopt SmartLayout for all content

### **Phase 2: Theme Compliance Audit**
- [ ] Scan codebase for hardcoded colors
- [ ] Replace all hardcoded values with theme variables
- [ ] Add ESLint rules to prevent hardcoded styling

### **Phase 3: Component Standardization**
- [ ] Create Storybook documentation for all components
- [ ] Establish component API standards
- [ ] Add visual regression testing

## 🔍 Quality Assurance

### **Code Review Checklist:**
- [ ] Theme variables used (no hardcoded colors/spacing)
- [ ] TypeScript interfaces defined
- [ ] Accessibility attributes present
- [ ] Responsive design implemented
- [ ] Performance optimizations applied
- [ ] Tests written and passing

### **Automated Checks:**
```yaml
# ESLint rules for component quality
rules:
  - no-hardcoded-colors: error
  - require-theme-spacing: error
  - accessible-component: error
  - typescript-strict: error
```

## 📚 Resources

### **Component Documentation:**
- [MUI v7.2.0 Documentation](https://mui.com/material-ui/)
- [SmartLayout Implementation](../claude-work/daily-work/2025-07-09/2025-07-09_CHANGE_LOG_design-system-v2.md)
- [Theme Configuration](../features/customers/theme/)
- [Component Templates](../vorlagen/COMPONENT_SPEC_TEMPLATE.md)

### **Design Guidelines:**
- [Freshfoodz CI Compliance](../FRESH-FOODZ_CI.md)
- [Accessibility Standards (WCAG 2.1 AA)](https://www.w3.org/WAI/WCAG21/quickref/)
- [Material Design Principles](https://m3.material.io/)

## ⚠️ Critical Warnings

### **NEVER DO:**
❌ Hardcode colors: `color: '#004F7B'`
❌ Hardcode spacing: `margin: '16px'`
❌ Skip accessibility attributes
❌ Ignore responsive design
❌ Create components without TypeScript interfaces

### **ALWAYS DO:**
✅ Use theme variables: `color: theme.palette.primary.main`
✅ Use theme spacing: `margin: theme.spacing(2)`
✅ Include ARIA attributes
✅ Test on mobile breakpoints
✅ Define proper TypeScript interfaces

---

**📋 Documentation basiert auf:** Analyse des aktuellen Codestands (nicht Vermutungen)
**📅 Letzte Aktualisierung:** 2025-09-17
**👨‍💻 Maintainer:** Claude + Development Team

**🎯 Diese Dokumentation spiegelt den TATSÄCHLICHEN Stand wider - nicht veraltete ADRs!**

**💡 Foundation-First Prinzip erfolgreich angewendet: Erst analysiert, dann dokumentiert!**