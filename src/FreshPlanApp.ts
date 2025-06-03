/**
 * FreshPlan Application - TypeScript version
 * Main application controller
 */

import EventBus from './core/EventBus';
import DOMHelper from './core/DOMHelper';
import Module from './core/Module';
import { useStore } from './store';

// Import modules
import TabNavigationModule from './modules/TabNavigationModule';
import CalculatorModule from './modules/CalculatorModule';
import CustomerModule from './modules/CustomerModule';
import SettingsModule from './modules/SettingsModule';
import ProfileModule from './modules/ProfileModule';
import PDFModule from './modules/PDFModule';
import i18nModule from './modules/i18nModule';
import LocationsModule from './modules/LocationsModule';

// Phase 2 modules (optional)
import CustomerModuleV2 from './modules/CustomerModuleV2';
import { observeCustomerFormReady } from './utils/domReadyObserver';

interface ModuleMap {
  tabs: TabNavigationModule;
  calculator: CalculatorModule;
  customer: CustomerModule | CustomerModuleV2;
  settings: SettingsModule;
  profile: ProfileModule;
  pdf: PDFModule;
  i18n: i18nModule;
  locations: LocationsModule;
}

// Global interface for dev tools
declare global {
  interface Window {
    FreshPlanApp: FreshPlanApp;
    FreshPlan: {
      app: FreshPlanApp;
      store: typeof useStore;
      events: typeof EventBus;
      modules: Map<keyof ModuleMap, Module>;
      getModule: <K extends keyof ModuleMap>(name: K) => ModuleMap[K] | null;
      reloadModule: (name: keyof ModuleMap) => Promise<void>;
      setState: (path: string, value: any) => void;
      getState: () => any;
      emit: (event: string, ...args: any[]) => void;
      debug: {
        enableEventLogging: () => void;
        disableEventLogging: () => void;
        showState: () => void;
        clearState: () => void;
      };
    };
  }
}

class FreshPlanApp {
  private modules: Map<keyof ModuleMap, Module> = new Map();
  private initialized = false;
  private events = EventBus;
  private dom = DOMHelper;

  /**
   * Initialize application
   */
  async init(): Promise<void> {
    if (this.initialized) {
      console.warn('App already initialized');
      return;
    }

    console.log('🚀 Initializing FreshPlan App...');

    try {
      // Start MSW in development
      if (import.meta.env.DEV) {
        const { startMocks } = await import('./mocks');
        await startMocks();
        console.log('[FreshPlanApp] Mock Service Worker started');
      }
      
      // Wait for DOM
      await this.waitForDOM();

      // Register modules
      this.registerModules();

      // Initialize modules
      await this.initModules();

      // Setup global handlers
      this.setupGlobalHandlers();

      // Setup dev tools
      this.setupDevTools();

      // Hide loading screen
      this.hideLoadingScreen();
      
      this.initialized = true;
      this.events.emit('app:initialized');
      
      console.log('✅ FreshPlan App initialized successfully!');
      
    } catch (error) {
      console.error('❌ Failed to initialize app:', error);
      this.handleInitError(error as Error);
    }
  }

  /**
   * Wait for DOM to be ready
   */
  private waitForDOM(): Promise<void> {
    return new Promise((resolve) => {
      this.dom.ready(() => resolve());
    });
  }
  
  /**
   * Hide the loading screen and show the app
   */
  private hideLoadingScreen(): void {
    const loadingEl = this.dom.$('#loading') as HTMLElement;
    const appEl = this.dom.$('#app') as HTMLElement;
    
    if (loadingEl) {
      loadingEl.style.display = 'none';
    }
    
    if (appEl) {
      appEl.classList.add('loaded');
    }
  }

  /**
   * Register all modules
   */
  private registerModules(): void {
    // Core modules
    this.registerModule('tabs', new TabNavigationModule());
    this.registerModule('calculator', new CalculatorModule());
    
    // Use Phase 2 CustomerModuleV2 if enabled
    const useV2 = new URLSearchParams(window.location.search).get('phase2') === 'true';
    if (useV2) {
      console.log('🔄 Using CustomerModuleV2 (Phase 2) with DOM Observer');
      console.log('📍 Current URL:', window.location.href);
      console.log('📍 Customer form exists:', !!document.getElementById('customerForm'));
      
      // Register module only when customer form is ready
      observeCustomerFormReady(() => {
        console.log('🎯 DOM Observer callback fired - initializing CustomerModuleV2');
        const customerModule = new CustomerModuleV2();
        this.registerModule('customer', customerModule);
        console.log('📦 CustomerModuleV2 registered');
        
        // Initialize the module after registration
        customerModule.setup().then(() => {
          console.log('🔧 CustomerModuleV2 setup complete, binding events...');
          customerModule.bindEvents();
          customerModule.subscribeToState();
          console.log('✅ CustomerModuleV2 fully initialized');
        }).catch(error => {
          console.error('❌ CustomerModuleV2 initialization failed:', error);
        });
      });
    } else {
      this.registerModule('customer', new CustomerModule());
    }
    
    this.registerModule('settings', new SettingsModule());
    this.registerModule('profile', new ProfileModule());
    this.registerModule('pdf', new PDFModule());
    this.registerModule('i18n', new i18nModule());
    this.registerModule('locations', new LocationsModule());
  }

  /**
   * Register a module
   */
  private registerModule<K extends keyof ModuleMap>(name: K, module: ModuleMap[K]): void {
    this.modules.set(name, module as Module);
  }

  /**
   * Initialize all modules
   */
  private async initModules(): Promise<void> {
    for (const [name, module] of this.modules) {
      try {
        await module.init();
      } catch (error) {
        console.error(`Failed to initialize module ${name}:`, error);
        // Continue with other modules
      }
    }
  }

  /**
   * Setup global event handlers
   */
  private setupGlobalHandlers(): void {
    // Handle errors
    window.addEventListener('error', (event) => {
      console.error('Runtime error:', event.error);
      this.events.emit('app:error', event.error);
    });

    window.addEventListener('unhandledrejection', (event) => {
      console.error('Unhandled promise rejection:', event.reason);
      this.events.emit('app:error', event.reason);
    });

    // Handle online/offline
    window.addEventListener('online', () => {
      this.events.emit('app:online');
    });

    window.addEventListener('offline', () => {
      this.events.emit('app:offline');
    });

    // Handle visibility change
    document.addEventListener('visibilitychange', () => {
      this.events.emit('app:visibility', !document.hidden);
    });

    // Save state before unload
    window.addEventListener('beforeunload', () => {
      // Zustand persists automatically, but we can emit an event
      this.events.emit('app:beforeunload');
    });
  }

  /**
   * Setup development tools
   */
  private setupDevTools(): void {
    if (process.env.NODE_ENV === 'development' || window.location.hostname === 'localhost') {
      // Expose app for debugging
      window.FreshPlanApp = this;
      window.FreshPlan = {
        app: this,
        store: useStore,
        events: this.events,
        modules: this.modules,
        
        // Utility functions
        getModule: <K extends keyof ModuleMap>(name: K) => this.getModule(name),
        reloadModule: (name: keyof ModuleMap) => this.reloadModule(name),
        setState: (_path: string, _value: any) => {
          console.warn('setState is deprecated. Use store actions directly.');
          // For compatibility, we could implement a path-based setter
        },
        getState: () => useStore.getState(),
        emit: (event: string, ...args: any[]) => this.events.emit(event, ...args),
        
        // Debug helpers
        debug: {
          enableEventLogging: () => this.events.setDebug(true),
          disableEventLogging: () => this.events.setDebug(false),
          showState: () => console.log(useStore.getState()),
          clearState: () => {
            if (confirm('Clear all state?')) {
              localStorage.clear();
              window.location.reload();
            }
          }
        }
      };

      console.log('🔧 Dev tools enabled. Access via window.FreshPlan');
    }
  }

  /**
   * Handle initialization error
   */
  private handleInitError(error: Error): void {
    const errorContainer = this.dom.create('div', {
      class: 'init-error',
      style: 'position: fixed; top: 20px; left: 50%; transform: translateX(-50%); ' +
        'background: #f44336; color: white; padding: 20px; border-radius: 4px; ' +
        'z-index: 9999; max-width: 500px;'
    });

    errorContainer.innerHTML = `
      <h3>Initialization Error</h3>
      <p>${error.message}</p>
      <button onclick="location.reload()" style="
        background: white;
        color: #f44336;
        border: none;
        padding: 8px 16px;
        border-radius: 4px;
        cursor: pointer;
        margin-top: 10px;
      ">Reload Page</button>
    `;

    document.body.appendChild(errorContainer);
  }

  /**
   * Get module instance
   */
  getModule<K extends keyof ModuleMap>(name: K): ModuleMap[K] | null {
    return (this.modules.get(name) as ModuleMap[K]) || null;
  }

  /**
   * Reload a module
   */
  async reloadModule(name: keyof ModuleMap): Promise<void> {
    const module = this.modules.get(name);
    if (module) {
      await module.reinit();
    }
  }

  /**
   * Destroy application
   */
  destroy(): void {
    // Destroy all modules
    for (const module of this.modules.values()) {
      module.destroy();
    }
    this.modules.clear();

    // Clear events
    this.events.clear();

    this.initialized = false;
  }

  /**
   * Check if app is initialized
   */
  isInitialized(): boolean {
    return this.initialized;
  }
}

// Create and export singleton instance
const app = new FreshPlanApp();
export default app;