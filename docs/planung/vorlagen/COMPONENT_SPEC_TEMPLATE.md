# Component Spezifikation: [ComponentName]

**Feature-Code:** FC-XXX
**Component Type:** [Shared / Domain-Specific / Layout / Form]
**Erstellt:** [YYYY-MM-DD]
**Status:** 🟡 Entwurf / 🔵 Entwicklung / 🟠 Review / 🟢 Freigegeben
**Storybook:** [Link zu Storybook Story]

## 📋 Overview

### Purpose
**Beschreibung:** [Was macht diese Komponente?]
**Use Cases:** [In welchen Szenarien wird sie verwendet?]
**Business Value:** [Welchen Business-Wert liefert sie?]

### Component Category
- [ ] **Atomic Component** (Button, Input, Icon)
- [ ] **Molecular Component** (SearchBar, Card, FormField)
- [ ] **Organism Component** (Header, ProductList, OrderForm)
- [ ] **Template Component** (PageLayout, Dashboard)
- [ ] **Page Component** (UserPage, OrderPage)

## 🎨 Design Specification

### Visual Design
- **Design System:** [MUI / Custom / Hybrid]
- **Variants:** [Primary, Secondary, Outlined, etc.]
- **Sizes:** [Small, Medium, Large]
- **States:** [Default, Hover, Active, Disabled, Loading, Error]

### Layout & Spacing
- **Container:** [Flex, Grid, Block]
- **Spacing:** [Margin, Padding using theme.spacing()]
- **Responsive:** [Desktop, Tablet, Mobile behavior]

### Typography
- **Font Family:** [theme.typography.fontFamily]
- **Font Sizes:** [h1, h2, body1, body2, caption]
- **Font Weights:** [light, regular, medium, bold]
- **Line Heights:** [normal, compact, relaxed]

### Colors
```typescript
// Theme colors (NO hardcoded values!)
const colors = {
  primary: theme.palette.primary.main,      // #004F7B
  success: theme.palette.success.main,      // #94C456
  error: theme.palette.error.main,
  warning: theme.palette.warning.main,
  info: theme.palette.info.main,
  text: {
    primary: theme.palette.text.primary,
    secondary: theme.palette.text.secondary,
  },
  background: {
    default: theme.palette.background.default,
    paper: theme.palette.background.paper,
  }
};
```

## 🏗️ Technical Specification

### Component Interface
```typescript
// Props Interface
interface [ComponentName]Props {
  // Required Props
  children?: React.ReactNode;
  className?: string;

  // Variant & Size
  variant?: 'primary' | 'secondary' | 'outlined';
  size?: 'small' | 'medium' | 'large';

  // State Props
  disabled?: boolean;
  loading?: boolean;
  error?: boolean;

  // Event Handlers
  onClick?: (event: React.MouseEvent<HTMLButtonElement>) => void;
  onChange?: (value: string) => void;
  onSubmit?: (data: FormData) => void;

  // Styling Props
  sx?: SxProps<Theme>;

  // Accessibility Props
  'aria-label'?: string;
  'aria-describedby'?: string;
  id?: string;

  // Component-Specific Props
  [specificProp1]?: string;
  [specificProp2]?: number;
}

// Default Props
const defaultProps: Partial<[ComponentName]Props> = {
  variant: 'primary',
  size: 'medium',
  disabled: false,
  loading: false,
  error: false,
};
```

### Component Structure
```typescript
// Main Component
export const [ComponentName] = React.forwardRef<
  HTMLElement,
  [ComponentName]Props
>(({
  children,
  className,
  variant = 'primary',
  size = 'medium',
  disabled = false,
  loading = false,
  error = false,
  onClick,
  sx,
  ...rest
}, ref) => {
  const theme = useTheme();

  // Hooks
  const [internalState, setInternalState] = useState();

  // Handlers
  const handleClick = useCallback((event: React.MouseEvent) => {
    if (disabled || loading) return;
    onClick?.(event);
  }, [disabled, loading, onClick]);

  // Styles
  const styles = useMemo(() => ({
    root: {
      // Base styles using theme
      color: theme.palette.text.primary,
      backgroundColor: theme.palette.background.paper,
      // Variant-specific styles
      ...(variant === 'primary' && {
        backgroundColor: theme.palette.primary.main,
        color: theme.palette.primary.contrastText,
      }),
      // Size-specific styles
      ...(size === 'large' && {
        padding: theme.spacing(2),
        fontSize: theme.typography.h6.fontSize,
      }),
      // State-specific styles
      ...(disabled && {
        opacity: 0.6,
        cursor: 'not-allowed',
      }),
      ...(loading && {
        cursor: 'wait',
      }),
      // Custom sx prop
      ...sx,
    },
  }), [theme, variant, size, disabled, loading, sx]);

  return (
    <ComponentWrapper
      ref={ref}
      className={className}
      sx={styles.root}
      onClick={handleClick}
      disabled={disabled}
      aria-disabled={disabled}
      {...rest}
    >
      {loading && <LoadingSpinner />}
      {children}
    </ComponentWrapper>
  );
});

// Display name for debugging
[ComponentName].displayName = '[ComponentName]';
```

### Dependencies
```typescript
// External Dependencies
import React, { useState, useCallback, useMemo, forwardRef } from 'react';
import {
  Box,
  Button,
  Typography,
  useTheme,
  type SxProps,
  type Theme
} from '@mui/material';

// Internal Dependencies
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { useComponentAnalytics } from '@/hooks/useAnalytics';

// Types
import type { ComponentProps } from '@/types/components';
```

## 🎛️ Component Variants

### Primary Variant
```typescript
<[ComponentName]
  variant="primary"
  size="medium"
>
  Primary Action
</[ComponentName]>
```

### Secondary Variant
```typescript
<[ComponentName]
  variant="secondary"
  size="medium"
>
  Secondary Action
</[ComponentName]>
```

### Custom Styling
```typescript
<[ComponentName]
  sx={{
    backgroundColor: theme.palette.success.main,
    '&:hover': {
      backgroundColor: theme.palette.success.dark,
    }
  }}
>
  Custom Styled
</[ComponentName]>
```

## 🔧 Props Documentation

### Core Props
| Prop | Type | Default | Required | Description |
|------|------|---------|----------|-------------|
| `children` | ReactNode | - | No | Content to render inside component |
| `className` | string | - | No | Additional CSS class names |
| `variant` | 'primary' \| 'secondary' \| 'outlined' | 'primary' | No | Visual variant |
| `size` | 'small' \| 'medium' \| 'large' | 'medium' | No | Component size |
| `disabled` | boolean | false | No | Whether component is disabled |
| `loading` | boolean | false | No | Whether component is in loading state |
| `sx` | SxProps<Theme> | - | No | MUI sx prop for custom styling |

### Event Props
| Prop | Type | Description |
|------|------|-------------|
| `onClick` | (event: MouseEvent) => void | Click event handler |
| `onChange` | (value: string) => void | Change event handler |
| `onFocus` | (event: FocusEvent) => void | Focus event handler |
| `onBlur` | (event: FocusEvent) => void | Blur event handler |

### Accessibility Props
| Prop | Type | Description |
|------|------|-------------|
| `aria-label` | string | Accessible label |
| `aria-describedby` | string | ID of element that describes this component |
| `id` | string | Unique identifier |
| `role` | string | ARIA role |

## 🎨 Styling & Theming

### Theme Integration
```typescript
// Component uses theme values
const useStyles = (theme: Theme) => ({
  root: {
    // Spacing
    padding: theme.spacing(1, 2),
    margin: theme.spacing(1),

    // Colors
    backgroundColor: theme.palette.background.paper,
    color: theme.palette.text.primary,

    // Typography
    fontSize: theme.typography.body1.fontSize,
    fontFamily: theme.typography.fontFamily,

    // Breakpoints
    [theme.breakpoints.down('md')]: {
      padding: theme.spacing(0.5, 1),
    },

    // Transitions
    transition: theme.transitions.create(['background-color', 'transform'], {
      duration: theme.transitions.duration.short,
    }),
  },
});
```

### CSS-in-JS with sx Prop
```typescript
// Advanced styling with sx
<[ComponentName]
  sx={{
    // Responsive design
    width: { xs: '100%', md: 'auto' },

    // Pseudo-selectors
    '&:hover': {
      backgroundColor: 'primary.dark',
      transform: 'scale(1.02)',
    },

    // Nested selectors
    '& .component-icon': {
      color: 'success.main',
    },

    // Conditional styling
    ...(isActive && {
      backgroundColor: 'primary.main',
      color: 'primary.contrastText',
    }),
  }}
/>
```

## ♿ Accessibility

### WCAG Compliance
- [ ] **Keyboard Navigation:** Component is fully keyboard accessible
- [ ] **Screen Reader:** Proper ARIA labels and roles
- [ ] **Color Contrast:** Meets WCAG AA standards (4.5:1 ratio)
- [ ] **Focus Management:** Clear focus indicators
- [ ] **Semantic HTML:** Uses appropriate HTML elements

### ARIA Attributes
```typescript
// Required ARIA attributes
<[ComponentName]
  role="button"
  aria-label="Descriptive label"
  aria-describedby="help-text"
  aria-expanded={isExpanded}
  aria-pressed={isPressed}
  tabIndex={0}
/>
```

### Keyboard Support
| Key | Action |
|-----|--------|
| `Enter` | Activate component |
| `Space` | Activate component (buttons) |
| `Tab` | Move to next focusable element |
| `Shift + Tab` | Move to previous focusable element |
| `Escape` | Close/cancel action |

## 🧪 Testing Specification

### Unit Tests
```typescript
describe('[ComponentName]', () => {
  it('renders with default props', () => {
    render(<[ComponentName]>Test Content</[ComponentName]>);
    expect(screen.getByText('Test Content')).toBeInTheDocument();
  });

  it('applies correct variant styles', () => {
    render(<[ComponentName] variant="primary">Primary</[ComponentName]>);
    const component = screen.getByText('Primary');
    expect(component).toHaveStyle({
      backgroundColor: theme.palette.primary.main,
    });
  });

  it('handles click events', () => {
    const handleClick = vi.fn();
    render(<[ComponentName] onClick={handleClick}>Click me</[ComponentName]>);

    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('disables interaction when disabled', () => {
    const handleClick = vi.fn();
    render(
      <[ComponentName] disabled onClick={handleClick}>
        Disabled
      </[ComponentName]>
    );

    fireEvent.click(screen.getByText('Disabled'));
    expect(handleClick).not.toHaveBeenCalled();
  });

  it('shows loading state', () => {
    render(<[ComponentName] loading>Loading</[ComponentName]>);
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });
});
```

### Accessibility Tests
```typescript
describe('[ComponentName] Accessibility', () => {
  it('is accessible by keyboard', () => {
    render(<[ComponentName]>Accessible</[ComponentName]>);
    const component = screen.getByRole('button');

    component.focus();
    expect(component).toHaveFocus();

    fireEvent.keyDown(component, { key: 'Enter' });
    // Assert expected behavior
  });

  it('has proper ARIA attributes', () => {
    render(
      <[ComponentName] aria-label="Custom label">
        Content
      </[ComponentName]>
    );

    expect(screen.getByLabelText('Custom label')).toBeInTheDocument();
  });

  it('meets contrast requirements', async () => {
    const { container } = render(<[ComponentName]>Test</[ComponentName]>);
    const results = await axe(container);
    expect(results).toHaveNoViolations();
  });
});
```

### Visual Regression Tests
```typescript
// Storybook stories for visual testing
export default {
  title: 'Components/[ComponentName]',
  component: [ComponentName],
  parameters: {
    docs: { description: { component: 'Component description' } },
  },
} as Meta;

export const Default: Story = {
  args: {
    children: 'Default Component',
  },
};

export const AllVariants: Story = {
  render: () => (
    <Stack spacing={2}>
      <[ComponentName] variant="primary">Primary</[ComponentName]>
      <[ComponentName] variant="secondary">Secondary</[ComponentName]>
      <[ComponentName] variant="outlined">Outlined</[ComponentName]>
    </Stack>
  ),
};

export const AllSizes: Story = {
  render: () => (
    <Stack direction="row" spacing={2}>
      <[ComponentName] size="small">Small</[ComponentName]>
      <[ComponentName] size="medium">Medium</[ComponentName]>
      <[ComponentName] size="large">Large</[ComponentName]>
    </Stack>
  ),
};

export const States: Story = {
  render: () => (
    <Stack spacing={2}>
      <[ComponentName]>Default</[ComponentName]>
      <[ComponentName] disabled>Disabled</[ComponentName]>
      <[ComponentName] loading>Loading</[ComponentName]>
      <[ComponentName] error>Error</[ComponentName]>
    </Stack>
  ),
};
```

## 📊 Performance

### Performance Considerations
- [ ] **Memoization:** Use `React.memo` for pure components
- [ ] **Callback Optimization:** Use `useCallback` for event handlers
- [ ] **Style Optimization:** Use `useMemo` for complex style calculations
- [ ] **Bundle Size:** Component adds < 5KB to bundle
- [ ] **Render Performance:** No unnecessary re-renders

### Optimization Techniques
```typescript
// Memoized component
export const [ComponentName] = React.memo(React.forwardRef<
  HTMLElement,
  [ComponentName]Props
>(({ ...props }, ref) => {
  // Component implementation
}));

// Optimized event handlers
const handleClick = useCallback((event: React.MouseEvent) => {
  // Handler logic
}, [dependency1, dependency2]);

// Memoized styles
const styles = useMemo(() => ({
  root: {
    // Style calculations
  },
}), [theme, variant, size]);
```

## 📚 Usage Examples

### Basic Usage
```typescript
import { [ComponentName] } from '@/components/[category]/[ComponentName]';

function ExampleComponent() {
  return (
    <[ComponentName]
      variant="primary"
      size="medium"
      onClick={() => console.log('Clicked!')}
    >
      Click me
    </[ComponentName]>
  );
}
```

### Advanced Usage
```typescript
function AdvancedExample() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const handleAction = async () => {
    setLoading(true);
    setError(false);

    try {
      await performAction();
    } catch (err) {
      setError(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <[ComponentName]
      variant="primary"
      size="large"
      loading={loading}
      error={error}
      onClick={handleAction}
      sx={{
        minWidth: 200,
        '&:hover': {
          transform: 'scale(1.05)',
        },
      }}
    >
      {error ? 'Retry' : 'Submit'}
    </[ComponentName]>
  );
}
```

### Integration with Forms
```typescript
function FormExample() {
  const { register, handleSubmit, formState: { isSubmitting } } = useForm();

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <[ComponentName]
        type="submit"
        loading={isSubmitting}
        disabled={isSubmitting}
        variant="primary"
        size="large"
      >
        {isSubmitting ? 'Submitting...' : 'Submit Form'}
      </[ComponentName]>
    </form>
  );
}
```

## 🔗 Related Components

### Component Composition
- **Parent Components:** [List parent components that use this]
- **Child Components:** [List child components this uses]
- **Similar Components:** [List similar/alternative components]

### Design System Integration
- **Design Tokens:** Uses theme.palette, theme.spacing, theme.typography
- **Component Library:** Part of the FreshPlan Design System
- **Storybook:** Available in component library documentation

## 📝 Migration Notes

### Breaking Changes
**v2.0 → v3.0:**
- `color` prop renamed to `variant`
- `onClick` handler signature changed
- Default size changed from `small` to `medium`

### Deprecation Warnings
- `legacy` variant will be removed in v4.0
- `oldProp` prop is deprecated, use `newProp` instead

## 📋 Checklist

### Implementation
- [ ] Component interface defined
- [ ] TypeScript types complete
- [ ] Theme integration implemented
- [ ] Accessibility attributes added
- [ ] Event handlers implemented
- [ ] Error boundaries added

### Testing
- [ ] Unit tests written (>90% coverage)
- [ ] Accessibility tests added
- [ ] Visual regression tests created
- [ ] Performance tests added
- [ ] Storybook stories complete

### Documentation
- [ ] Props documented
- [ ] Examples provided
- [ ] Accessibility guide written
- [ ] Migration guide created
- [ ] Storybook docs complete

---

**📋 Template verwendet:** COMPONENT_SPEC_TEMPLATE.md v1.0
**📅 Letzte Aktualisierung:** [YYYY-MM-DD]
**👨‍💻 Component Owner:** [Team/Person]

**🎯 Diese Spezifikation ist der Standard für alle UI-Komponenten im FreshPlan Design System**