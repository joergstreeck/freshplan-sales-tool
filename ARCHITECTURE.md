# FreshPlan Sales Tool - Clean Architecture Documentation

## Overview

The FreshPlan Sales Tool v3 uses a clean, modular architecture designed for maintainability, testability, and robustness.

## Core Principles

1. **Single Responsibility**: Each module handles one specific feature
2. **Dependency Injection**: Modules don't directly depend on each other
3. **Event-Driven**: Communication via EventBus, not direct calls
4. **State Management**: Centralized, immutable state with change notifications
5. **Error Resilience**: Each module can fail without crashing the app

## Architecture Layers

```
js/
├── core/                   # Core framework
│   ├── EventBus.js        # Central event management
│   ├── StateManager.js    # State management
│   ├── DOMHelper.js       # DOM utilities
│   └── Module.js          # Base module class
│
├── modules/               # Feature modules
│   ├── TabNavigationModule.js
│   ├── CalculatorModule.js
│   ├── CustomerModule.js
│   └── ...
│
├── utils/                 # Utility functions
│   ├── formatting.js
│   ├── validation.js
│   └── ...
│
├── FreshPlanApp.js       # Main application controller
└── main-clean.js         # Entry point
```

## Core Components

### EventBus
- Singleton pattern for consistent event handling
- Decouples modules from each other
- Supports wildcards and namespacing
- Automatic error handling in event handlers

### StateManager
- Centralized state storage
- Immutable updates with change notifications
- Automatic persistence to localStorage
- Path-based access (e.g., 'calculator.orderValue')
- History tracking with undo capability

### DOMHelper
- jQuery-like API for consistency
- Event delegation for dynamic content
- Null-safe operations
- Built-in debounce/throttle utilities

### Module Base Class
- Consistent lifecycle (init, setup, bindEvents, destroy)
- Automatic cleanup on destroy
- Built-in error handling
- State and event helpers

## Creating a New Module

```javascript
import Module from '../core/Module.js';

export default class MyFeatureModule extends Module {
  constructor() {
    super('myfeature'); // Module name
  }

  async setup() {
    // Initialize module state
    this.setModuleState({
      someValue: 'default'
    });
  }

  bindEvents() {
    // Bind DOM events (automatically cleaned up)
    this.on('#myButton', 'click', () => {
      this.handleClick();
    });

    // Event delegation
    this.on(document, 'click', '.dynamic-button', (e) => {
      this.handleDynamicClick(e);
    });
  }

  subscribeToState() {
    // Subscribe to state changes (automatically cleaned up)
    this.subscribe('myfeature.someValue', ({ newValue }) => {
      this.updateUI(newValue);
    });
  }

  handleClick() {
    // Update state
    this.setModuleState('someValue', 'new value');
    
    // Emit event
    this.emit('clicked');
  }
}
```

## Module Communication

Modules communicate via events, not direct calls:

```javascript
// Module A emits event
this.emit('data:updated', { id: 123, value: 'test' });

// Module B listens
this.events.on('module:moduleA:data:updated', (data) => {
  this.handleDataUpdate(data);
});
```

## State Management

State is organized by module:

```javascript
{
  app: {
    version: '2.0.0',
    language: 'de'
  },
  calculator: {
    orderValue: 15000,
    leadTime: 14,
    calculation: { ... }
  },
  customer: {
    data: { ... }
  }
}
```

## Error Handling

Each layer has error boundaries:

1. **Module Level**: Errors in one module don't affect others
2. **Event Level**: Event handler errors are caught and logged
3. **State Level**: State updates are validated
4. **App Level**: Global error handlers for uncaught errors

## Best Practices

1. **Never modify DOM directly in modules** - Use DOMHelper
2. **Never access other modules directly** - Use events
3. **Never store state locally** - Use StateManager
4. **Always clean up** - Module base class handles most cleanup
5. **Use TypeScript types** (future enhancement)

## Adding Features

1. Create new module extending Module base class
2. Register in FreshPlanApp.registerModules()
3. Module automatically initializes with app
4. Communication via events and state

## Debugging

```javascript
// Enable event logging
FreshPlan.debug.enableEventLogging();

// Show current state
FreshPlan.debug.showState();

// Reload specific module
FreshPlan.reloadModule('calculator');

// Access module directly (dev only)
const calc = FreshPlan.getModule('calculator');
```

## Benefits

1. **Maintainable**: Clear structure, single responsibility
2. **Testable**: Modules can be tested in isolation
3. **Robust**: Failures are isolated
4. **Scalable**: Easy to add new features
5. **Debuggable**: Clear event flow, state inspection

## Migration Notes

When migrating old code:

1. Extract feature logic into a module
2. Replace direct DOM manipulation with DOMHelper
3. Replace global variables with state
4. Replace function calls with events
5. Add proper error handling

This architecture ensures that UI changes won't break functionality, modules remain independent, and the app remains stable even as it grows.