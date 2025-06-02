/**
 * Module - Base class for all feature modules
 * Provides consistent lifecycle, state management, and event handling
 */

import EventBus from './EventBus';
import StateManager, { type StateChangeEvent } from './StateManager';
import DOMHelper from './DOMHelper';
import type { ModuleConfig, Notification } from '../types';

export type ModuleState = Record<string, any>;
export type CleanupFn = () => void | Promise<void>;

export interface ModuleLifecycle {
  setup?(): void | Promise<void>;
  bindEvents?(): void;
  subscribeToState?(): void;
  cleanup(): void | Promise<void>;
}

export default abstract class Module implements ModuleLifecycle {
  protected readonly name: string;
  protected readonly events: typeof EventBus = EventBus;
  protected readonly state: typeof StateManager = StateManager;
  protected readonly dom: typeof DOMHelper = DOMHelper;
  
  private initialized: boolean = false;
  private subscriptions: CleanupFn[] = [];
  private eventHandlers: CleanupFn[] = [];
  private intervals: NodeJS.Timeout[] = [];
  private timeouts: NodeJS.Timeout[] = [];
  private _isDestroyed: boolean = false;

  constructor(name: string, _config?: ModuleConfig) {
    this.name = name;
    
    // Validate module name
    if (!name || typeof name !== 'string') {
      throw new Error('Module name is required');
    }
  }

  /**
   * Initialize module
   */
  async init(): Promise<void> {
    if (this.initialized) {
      console.warn(`Module ${this.name} already initialized`);
      return;
    }

    if (this._isDestroyed) {
      console.warn(`Module ${this.name} has been destroyed and cannot be initialized`);
      return;
    }

    try {
      console.log(`[${this.name}] Initializing...`);
      
      // Call lifecycle methods
      await this.setup?.();
      this.bindEvents?.();
      this.subscribeToState?.();
      
      this.initialized = true;
      this.emit('initialized');
      
      console.log(`[${this.name}] Initialized successfully`);
    } catch (error) {
      console.error(`[${this.name}] Initialization failed:`, error);
      this.handleError(error as Error);
      throw error;
    }
  }

  /**
   * Reinitialize module
   */
  async reinit(): Promise<void> {
    await this.destroy();
    this._isDestroyed = false;
    await this.init();
  }

  /**
   * Destroy module and cleanup
   */
  async destroy(): Promise<void> {
    if (this._isDestroyed) return;

    console.log(`[${this.name}] Destroying...`);
    
    try {
      // Call custom cleanup
      await this.cleanup?.();
      
      // Clear intervals and timeouts
      this.intervals.forEach(clearInterval);
      this.timeouts.forEach(clearTimeout);
      
      // Unsubscribe from events
      this.eventHandlers.forEach(unsubscribe => {
        try {
          unsubscribe();
        } catch (error) {
          console.error(`[${this.name}] Error unsubscribing event:`, error);
        }
      });
      
      // Unsubscribe from state
      this.subscriptions.forEach(unsubscribe => {
        try {
          unsubscribe();
        } catch (error) {
          console.error(`[${this.name}] Error unsubscribing state:`, error);
        }
      });
      
      // Clear arrays
      this.intervals = [];
      this.timeouts = [];
      this.eventHandlers = [];
      this.subscriptions = [];
      
      this.initialized = false;
      this._isDestroyed = true;
      
      this.emit('destroyed');
      console.log(`[${this.name}] Destroyed successfully`);
    } catch (error) {
      console.error(`[${this.name}] Error during destroy:`, error);
    }
  }

  /**
   * Check if module is initialized
   */
  isInitialized(): boolean {
    return this.initialized && !this._isDestroyed;
  }

  // State Management Helpers

  /**
   * Get module state
   */
  protected getModuleState<T = any>(path?: string, defaultValue?: T): T {
    const basePath = `${this.name}${path ? `.${path}` : ''}`;
    return this.state.get<T>(basePath, defaultValue);
  }

  /**
   * Set module state
   */
  protected setModuleState<T = any>(pathOrValue: string | ModuleState, value?: T): void {
    if (typeof pathOrValue === 'string') {
      const fullPath = `${this.name}.${pathOrValue}`;
      this.state.set(fullPath, value);
    } else {
      // Set entire module state
      this.state.set(this.name, pathOrValue);
    }
  }

  /**
   * Update module state (partial update)
   */
  protected updateModuleState(updates: Partial<ModuleState>): void {
    const currentState = this.getModuleState() || {};
    this.setModuleState({ ...currentState, ...updates });
  }

  /**
   * Get global state
   */
  protected getState<T = any>(path?: string, defaultValue?: T): T {
    return this.state.get<T>(path, defaultValue);
  }

  /**
   * Set global state
   */
  protected setState<T = any>(path: string, value: T): void {
    this.state.set(path, value);
  }

  /**
   * Subscribe to state changes
   */
  protected subscribe<T = any>(
    path: string,
    callback: (event: StateChangeEvent<T>) => void
  ): void {
    const unsubscribe = this.state.subscribe(path, callback);
    this.subscriptions.push(unsubscribe);
  }

  // Event Helpers

  /**
   * Emit module event
   */
  protected emit<T = any>(event: string, data?: T): void {
    this.events.emit(`module:${this.name}:${event}`, data);
  }

  /**
   * Emit global event
   */
  protected emitGlobal<T = any>(event: string, data?: T): void {
    this.events.emit(event, data);
  }

  /**
   * Listen to events
   */
  protected listen<T = any>(
    event: string,
    callback: (data: T) => void
  ): void {
    const unsubscribe = this.events.on(event, callback);
    this.eventHandlers.push(unsubscribe);
  }

  // DOM Helpers

  /**
   * Add event listener with automatic cleanup
   */
  protected on<K extends keyof HTMLElementEventMap>(
    target: string | Element | Document | Window,
    event: K | string,
    selectorOrHandler: string | ((e: Event) => void),
    handler?: (e: Event) => void,
    options?: AddEventListenerOptions
  ): void {
    const unsubscribe = this.dom.on(target, event, selectorOrHandler, handler, options);
    this.eventHandlers.push(unsubscribe);
  }

  /**
   * Set interval with automatic cleanup
   */
  protected setInterval(callback: () => void, delay: number): NodeJS.Timeout {
    const interval = setInterval(callback, delay);
    this.intervals.push(interval);
    return interval;
  }

  /**
   * Set timeout with automatic cleanup
   */
  protected setTimeout(callback: () => void, delay: number): NodeJS.Timeout {
    const timeout = setTimeout(() => {
      callback();
      // Remove from array after execution
      const index = this.timeouts.indexOf(timeout);
      if (index > -1) {
        this.timeouts.splice(index, 1);
      }
    }, delay);
    
    this.timeouts.push(timeout);
    return timeout;
  }

  // UI Helpers

  /**
   * Show loading state
   */
  protected setLoading(loading: boolean): void {
    this.setState('ui.loading', loading);
    this.emit('loading', loading);
  }

  /**
   * Show success notification
   */
  protected showSuccess(message: string, duration: number = 3000): void {
    this.showNotification('success', message, duration);
  }

  /**
   * Show error notification
   */
  protected showError(message: string, duration: number = 5000): void {
    this.showNotification('error', message, duration);
  }

  /**
   * Show warning notification
   */
  protected showWarning(message: string, duration: number = 4000): void {
    this.showNotification('warning', message, duration);
  }

  /**
   * Show info notification
   */
  protected showInfo(message: string, duration: number = 3000): void {
    this.showNotification('info', message, duration);
  }

  /**
   * Show notification
   */
  private showNotification(
    type: Notification['type'],
    message: string,
    duration: number
  ): void {
    const notification: Notification = {
      id: `${this.name}-${Date.now()}`,
      type,
      message,
      timestamp: Date.now(),
      duration
    };
    
    // Add to state
    const notifications = this.getState<Notification[]>('ui.notifications', []);
    this.setState('ui.notifications', [...notifications, notification]);
    
    // Emit event
    this.emitGlobal('notification:show', notification);
    
    // Auto-remove after duration
    if (duration > 0) {
      this.setTimeout(() => {
        const currentNotifications = this.getState<Notification[]>('ui.notifications', []);
        this.setState(
          'ui.notifications',
          currentNotifications.filter(n => n.id !== notification.id)
        );
      }, duration);
    }
  }

  // Error Handling

  /**
   * Handle module errors
   */
  protected handleError(error: Error): void {
    console.error(`[${this.name}] Error:`, error);
    
    // Update error state
    this.setState('ui.error', {
      message: error.message,
      code: (error as any).code,
      details: error
    });
    
    // Emit error event
    this.emit('error', error);
    this.emitGlobal('module:error', {
      module: this.name,
      error
    });
  }

  // Abstract lifecycle methods (optional implementation)
  abstract setup?(): void | Promise<void>;
  abstract bindEvents?(): void;
  abstract subscribeToState?(): void;
  abstract cleanup(): void | Promise<void>;
}