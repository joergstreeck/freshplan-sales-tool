# Phase 3.2.1: Tab Navigation Event Bridge Implementation

## Summary

This phase implements a backward-compatible event bridge for tab navigation, allowing the legacy code to communicate with the new modular system without breaking existing functionality.

## Changes Made

### 1. Created Event Constants (`src/constants/events.ts`)
- Centralized all event names in a single location
- Added type definitions for event payloads
- Exported constants for consistent event naming across the application

### 2. Extended TabNavigationModule (`src/modules/TabNavigationModule.ts`)

#### Added Constants
```typescript
export const TAB_EVENTS = {
  SWITCH: 'tab:switch',
  SWITCHED: 'tab:switched',
  BEFORE_SWITCH: 'tab:beforeSwitch',
  PROGRESS_UPDATE: 'tab:progressUpdate',
  ACCESS_DENIED: 'tab:accessDenied'
}
```

#### Added Backward Compatible Methods
- `showTab(tabId: string)` - Deprecated wrapper for `navigateTo()`
- `getActiveTab()` - Deprecated wrapper for `getCurrentTab()`
- `onTabChange(callback)` - Deprecated event listener helper
- `enableLegacyMode()` - Enables legacy event emissions

#### Enhanced Event System
- Emits both module events and window events for legacy compatibility
- Added `BEFORE_SWITCH` event that can be cancelled
- Added `PROGRESS_UPDATE` event when progress changes
- Added `ACCESS_DENIED` event when tab access is blocked

#### Added Helper Methods
- `mapLegacyTabId()` - Maps old tab IDs to new TabName types
- `getAccessDeniedReason()` - Provides user-friendly reasons for access denial
- `getProgressSteps()` - Returns detailed progress information

### 3. Updated Legacy Script (`src/legacy-script.ts`)

#### Added Event Bridge
- Listens for `TAB_EVENTS.SWITCHED` from modules
- Emits `TAB_EVENTS.SWITCH` when tabs are clicked
- Synchronizes UI state between legacy and module systems

#### Added Legacy Helpers
- `window.switchToTab()` - Programmatic tab switching (deprecated)
- `window.getCurrentTab()` - Get current active tab
- `window.onTabChange()` - Listen for tab changes with unsubscribe

#### Added Cleanup
- `__LEGACY_SCRIPT_CLEANUP` function to clean up global variables
- Marks legacy script as active with `__LEGACY_SCRIPT_ACTIVE` flag

## Usage Examples

### From Legacy Code
```javascript
// Switch to a tab (deprecated)
window.switchToTab('customer');

// Get current tab
const currentTab = window.getCurrentTab();

// Listen for tab changes
const unsubscribe = window.onTabChange((tab) => {
  console.log('Tab changed to:', tab);
});

// Stop listening
unsubscribe();
```

### From Module Code
```typescript
// Navigate to tab (will check access)
tabModule.navigateTo('profile');

// Listen for tab events
tabModule.on(TAB_EVENTS.SWITCHED, ({ tab, previousTab }) => {
  console.log(`Switched from ${previousTab} to ${tab}`);
});

// Check if tab is accessible
if (tabModule.isTabAccessible('offer')) {
  tabModule.navigateTo('offer');
}
```

### Event Flow

1. **Legacy → Module**: 
   - Legacy code clicks tab → Emits `TAB_EVENTS.SWITCH` → Module handles navigation

2. **Module → Legacy**: 
   - Module switches tab → Emits `TAB_EVENTS.SWITCHED` → Legacy updates UI

3. **Access Control**:
   - Module checks access → Emits `TAB_EVENTS.ACCESS_DENIED` if blocked → UI can show error

4. **Progress Updates**:
   - State changes → Module calculates progress → Emits `TAB_EVENTS.PROGRESS_UPDATE`

## Testing the Implementation

1. **Tab Navigation**: All tabs should switch correctly from both legacy clicks and module calls
2. **Access Control**: Profile and Offer tabs should be blocked until requirements are met
3. **Progress Bar**: Should update automatically as data is entered
4. **Event Bridge**: Console warnings should appear for deprecated methods
5. **Backward Compatibility**: All existing functionality should continue to work

## Next Steps (Phase 3.2.2)

1. Migrate calculator tab functionality to CalculatorModule
2. Update legacy calculator functions to use module methods
3. Set up state synchronization between legacy and module
4. Add deprecation timeline for legacy methods