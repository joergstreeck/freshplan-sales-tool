/**
 * Mock implementation of Module base class for testing
 */

// Mock Module implementation for testing

export class MockEventBus {
  private listeners = new Map<string, Set<Function>>();
  
  on(event: string, handler: Function): void {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set());
    }
    this.listeners.get(event)!.add(handler);
  }
  
  off(event: string, handler: Function): void {
    this.listeners.get(event)?.delete(handler);
  }
  
  emit(event: string, ...args: any[]): void {
    this.listeners.get(event)?.forEach(handler => handler(...args));
  }
}

export default class MockModule {
  protected events: any;
  protected dom: any;
  private eventHandlers: Map<EventTarget, Map<string, EventListener>> = new Map();

  constructor() {
    this.events = new MockEventBus() as any;
    this.dom = {
      $: (selector: string) => document.querySelector(selector),
      $$: (selector: string) => Array.from(document.querySelectorAll(selector)),
      ready: (callback: Function) => {
        if (document.readyState === 'loading') {
          document.addEventListener('DOMContentLoaded', () => callback());
        } else {
          callback();
        }
      }
    } as any;
  }

  async setup(): Promise<void> {
    // Override in subclass
  }

  bindEvents(): void {
    // Override in subclass
  }

  subscribeToState(): void {
    // Override in subclass
  }

  protected on(target: EventTarget | null, event: string, handler: EventListener): void {
    if (!target) return;
    
    // Store handler for cleanup
    if (!this.eventHandlers.has(target)) {
      this.eventHandlers.set(target, new Map());
    }
    this.eventHandlers.get(target)!.set(event, handler);
    
    // Add event listener
    target.addEventListener(event, handler);
  }

  protected emit(event: string, data?: any): void {
    this.events.emit(event, data);
  }

  cleanup(): void {
    // Remove all event listeners
    this.eventHandlers.forEach((handlers, target) => {
      handlers.forEach((handler, event) => {
        target.removeEventListener(event, handler);
      });
    });
    this.eventHandlers.clear();
  }
}