# Test-Implementierung für Modal Architecture Fix

**Datum:** 27.07.2025  
**Autor:** Claude  
**Feature:** Tests für CustomerOnboardingWizardModal und Event-basierte Navigation

## Zusammenfassung

Erfolgreich Tests für die neue Modal-Implementierung erstellt:

### 1. CustomerOnboardingWizardModal Tests ✅
- **Datei:** `/frontend/src/features/customers/components/wizard/__tests__/CustomerOnboardingWizardModal.test.tsx`
- **Coverage:** 15 Tests, alle grün
- **Getestet:**
  - Desktop Mode (Dialog)
  - Mobile Mode (Drawer)
  - Event-basierte Navigation
  - Modal Open/Close
  - Accessibility Features
  - Focus Management

### 2. CustomersPageV2 Tests ✅
- **Datei:** `/frontend/src/pages/__tests__/CustomersPageV2.test.tsx`
- **Coverage:** 8 Tests, alle grün
- **Getestet:**
  - MainLayoutV2 Integration
  - Event Listener für 'freshplan:new-customer'
  - Modal Behavior
  - Empty State
  - Keyboard Shortcuts

## Test-Ergebnisse

```bash
# CustomerOnboardingWizardModal.test.tsx
✓ 15 tests passed (176ms)

# CustomersPageV2.test.tsx  
✓ 8 tests passed (57ms)

Total: 23 tests passed
```

## Wichtige Test-Cases

### Event-basierte Navigation
```typescript
it('opens wizard on freshplan:new-customer event', async () => {
  render(<CustomersPageV2 />, { wrapper });
  
  // Initially no modal
  expect(screen.queryByTestId('wizard-modal')).not.toBeInTheDocument();
  
  // Dispatch custom event
  window.dispatchEvent(new CustomEvent('freshplan:new-customer'));
  
  // Modal should appear
  await waitFor(() => {
    expect(screen.getByTestId('wizard-modal')).toBeInTheDocument();
  });
});
```

### MainLayoutV2 Integration
```typescript
it('uses MainLayoutV2', () => {
  render(<CustomersPageV2 />, { wrapper });
  expect(screen.getByTestId('main-layout-v2')).toBeInTheDocument();
});
```

### Responsive Design
```typescript
// Desktop: Dialog
mockMatchMedia(false);
expect(screen.getByRole('dialog')).toBeInTheDocument();

// Mobile: Drawer  
mockMatchMedia(true);
expect(screen.getByRole('presentation')).toBeInTheDocument();
```

## Lessons Learned

1. **MUI IconButton Testing:** Keine expliziten aria-labels, nutze `getByTestId('CloseIcon').closest('button')`
2. **Mock Navigation:** Global mocken mit `vi.mock('react-router-dom')`
3. **Component Mocks:** Alle dependencies mocken für isolierte Tests
4. **Act Warnings:** Nicht kritisch, können mit `act()` wrapper gelöst werden

## Nächste Schritte

- ✅ TODO-15: Tests für Modal-Implementierung ABGESCHLOSSEN
- ⏭️ TODO-16: Andere Seiten mit MainLayoutV2 prüfen