/**
 * EventBus - Type-safe event management system
 * Singleton pattern with TypeScript generics for type safety
 */

import type { AppEvent } from '../types';

export type EventHandler<T = any> = (data: T) => void | Promise<void>;
export type UnsubscribeFn = () => void;

interface EventContext {
  callback: EventHandler;
  context?: any;
  once?: boolean;
}

class EventBus {
  private static instance: EventBus;
  private events: Map<string, EventContext[]>;
  private wildcardHandlers: Map<string, EventContext[]>;
  private debug: boolean;
  private eventHistory: AppEvent[];
  private maxHistory: number;

  private constructor() {
    this.events = new Map();
    this.wildcardHandlers = new Map();
    this.debug = false;
    this.eventHistory = [];
    this.maxHistory = 100;
  }

  /**
   * Get singleton instance
   */
  static getInstance(): EventBus {
    if (!EventBus.instance) {
      EventBus.instance = new EventBus();
    }
    return EventBus.instance;
  }

  /**
   * Subscribe to an event
   */
  on<T = any>(
    event: string, 
    callback: EventHandler<T>, 
    context?: any
  ): UnsubscribeFn {
    if (this.debug) {
      console.log(`[EventBus] Subscribing to: ${event}`);
    }

    const isWildcard = event.includes('*');
    const map = isWildcard ? this.wildcardHandlers : this.events;
    
    if (!map.has(event)) {
      map.set(event, []);
    }
    
    const handler: EventContext = { callback, context };
    map.get(event)!.push(handler);
    
    // Return unsubscribe function
    return () => this.off(event, callback);
  }

  /**
   * Subscribe to an event once
   */
  once<T = any>(
    event: string, 
    callback: EventHandler<T>, 
    context?: any
  ): UnsubscribeFn {
    const onceWrapper: EventHandler<T> = (data) => {
      callback.call(context, data);
      this.off(event, onceWrapper);
    };

    // Mark as once handler
    (onceWrapper as any).__once = true;
    (onceWrapper as any).__original = callback;

    return this.on(event, onceWrapper, context);
  }

  /**
   * Emit an event
   */
  emit<T = any>(event: string, data?: T): void {
    const appEvent: AppEvent<T> = {
      type: event,
      payload: data as T,
      timestamp: Date.now(),
      source: this.getEventSource()
    };

    // Add to history
    this.addToHistory(appEvent);

    if (this.debug) {
      console.log(`[EventBus] Emitting: ${event}`, data);
    }
    
    // Call exact match handlers
    const handlers = this.events.get(event) || [];
    this.callHandlers(handlers, data);
    
    // Call wildcard handlers
    this.wildcardHandlers.forEach((wildcardHandlers, pattern) => {
      if (this.matchesPattern(event, pattern)) {
        this.callHandlers(wildcardHandlers, data);
      }
    });
  }

  /**
   * Emit an event asynchronously
   */
  async emitAsync<T = any>(event: string, data?: T): Promise<void> {
    const appEvent: AppEvent<T> = {
      type: event,
      payload: data as T,
      timestamp: Date.now(),
      source: this.getEventSource()
    };

    // Add to history
    this.addToHistory(appEvent);

    if (this.debug) {
      console.log(`[EventBus] Emitting async: ${event}`, data);
    }
    
    // Collect all handlers
    const allHandlers: EventContext[] = [];
    
    // Exact match handlers
    const handlers = this.events.get(event) || [];
    allHandlers.push(...handlers);
    
    // Wildcard handlers
    this.wildcardHandlers.forEach((wildcardHandlers, pattern) => {
      if (this.matchesPattern(event, pattern)) {
        allHandlers.push(...wildcardHandlers);
      }
    });

    // Call all handlers asynchronously
    await Promise.all(
      allHandlers.map(handler => this.callHandlerAsync(handler, data))
    );
  }

  /**
   * Remove event listener
   */
  off(event: string, callback?: EventHandler): void {
    if (!callback) {
      // Remove all handlers for this event
      this.events.delete(event);
      this.wildcardHandlers.delete(event);
      return;
    }

    // Remove specific handler
    const removeFromMap = (map: Map<string, EventContext[]>) => {
      const handlers = map.get(event);
      if (handlers) {
        const filtered = handlers.filter(h => {
          // Check for once wrapper
          const original = (h.callback as any).__original;
          return h.callback !== callback && original !== callback;
        });
        
        if (filtered.length > 0) {
          map.set(event, filtered);
        } else {
          map.delete(event);
        }
      }
    };

    removeFromMap(this.events);
    removeFromMap(this.wildcardHandlers);
  }

  /**
   * Remove all event listeners
   */
  clear(): void {
    this.events.clear();
    this.wildcardHandlers.clear();
    this.eventHistory = [];
  }

  /**
   * Get event history
   */
  getHistory(): ReadonlyArray<AppEvent> {
    return [...this.eventHistory];
  }

  /**
   * Enable/disable debug mode
   */
  setDebug(enabled: boolean): void {
    this.debug = enabled;
  }

  /**
   * Get all registered events
   */
  getRegisteredEvents(): string[] {
    return [
      ...Array.from(this.events.keys()),
      ...Array.from(this.wildcardHandlers.keys())
    ];
  }

  /**
   * Check if event has listeners
   */
  hasListeners(event: string): boolean {
    if (this.events.has(event)) {
      return this.events.get(event)!.length > 0;
    }

    // Check wildcard patterns
    for (const pattern of this.wildcardHandlers.keys()) {
      if (this.matchesPattern(event, pattern)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Get listener count for event
   */
  listenerCount(event: string): number {
    let count = 0;

    // Exact match
    if (this.events.has(event)) {
      count += this.events.get(event)!.length;
    }

    // Wildcard matches
    this.wildcardHandlers.forEach((handlers, pattern) => {
      if (this.matchesPattern(event, pattern)) {
        count += handlers.length;
      }
    });

    return count;
  }

  /**
   * Call event handlers with error handling
   */
  private callHandlers<T>(handlers: EventContext[], data: T): void {
    handlers.forEach(handler => {
      try {
        handler.callback.call(handler.context, data);
      } catch (error) {
        console.error(`[EventBus] Error in event handler:`, error);
        this.emit('error', { 
          type: 'handler-error', 
          error, 
          handler: handler.callback.toString() 
        });
      }
    });
  }

  /**
   * Call event handler asynchronously
   */
  private async callHandlerAsync<T>(
    handler: EventContext, 
    data: T
  ): Promise<void> {
    try {
      await handler.callback.call(handler.context, data);
    } catch (error) {
      console.error(`[EventBus] Error in async event handler:`, error);
      this.emit('error', { 
        type: 'async-handler-error', 
        error, 
        handler: handler.callback.toString() 
      });
    }
  }

  /**
   * Check if event matches wildcard pattern
   */
  private matchesPattern(event: string, pattern: string): boolean {
    if (!pattern.includes('*')) {
      return event === pattern;
    }

    const regex = new RegExp(
      '^' + pattern.replace(/\*/g, '.*').replace(/\?/g, '.') + '$'
    );
    return regex.test(event);
  }

  /**
   * Add event to history
   */
  private addToHistory(event: AppEvent): void {
    this.eventHistory.push(event);
    
    // Trim history if too long
    if (this.eventHistory.length > this.maxHistory) {
      this.eventHistory.shift();
    }
  }

  /**
   * Get event source from stack trace
   */
  private getEventSource(): string {
    try {
      const stack = new Error().stack;
      if (stack) {
        const lines = stack.split('\n');
        // Find the first line that's not EventBus
        for (let i = 3; i < lines.length; i++) {
          const line = lines[i];
          if (!line.includes('EventBus')) {
            return line.trim();
          }
        }
      }
    } catch {
      // Ignore stack trace errors
    }
    return 'unknown';
  }
}

// Export singleton instance
export default EventBus.getInstance();