/**
 * DOMHelper - Type-safe DOM manipulation utilities
 * jQuery-like API with modern features and TypeScript support
 */

export type ElementSelector = string | Element | Document | Window | null;
export type EventHandler<T = Event> = (event: T) => void | boolean;

interface EventOptions {
  capture?: boolean;
  once?: boolean;
  passive?: boolean;
  signal?: AbortSignal;
}

interface DOMHelperStatic {
  // Element selection
  $<T extends Element = Element>(selector: ElementSelector): T | null;
  $$<T extends Element = Element>(selector: string): T[];
  
  // Element creation
  create<K extends keyof HTMLElementTagNameMap>(
    tag: K,
    attrs?: Record<string, any>,
    content?: string | Element | Element[]
  ): HTMLElementTagNameMap[K];
  
  // DOM ready
  ready(callback: () => void): void;
  
  // Utilities
  debounce<T extends (...args: any[]) => any>(
    fn: T,
    delay: number
  ): (...args: Parameters<T>) => void;
  
  throttle<T extends (...args: any[]) => any>(
    fn: T,
    delay: number
  ): (...args: Parameters<T>) => void;
}

class DOMHelper {
  private static instance: DOMHelper;
  private eventDelegates: Map<string, Map<string, EventHandler[]>>;

  private constructor() {
    this.eventDelegates = new Map();
  }

  /**
   * Get singleton instance
   */
  static getInstance(): DOMHelper {
    if (!DOMHelper.instance) {
      DOMHelper.instance = new DOMHelper();
    }
    return DOMHelper.instance;
  }

  /**
   * Query single element
   */
  $<T extends Element = Element>(selector: ElementSelector): T | null {
    if (!selector) return null;
    
    if (typeof selector === 'string') {
      return document.querySelector<T>(selector);
    }
    
    if (selector instanceof Element || selector === document || selector === window) {
      return selector as T;
    }
    
    return null;
  }

  /**
   * Query multiple elements
   */
  $$<T extends Element = Element>(selector: string): T[] {
    return Array.from(document.querySelectorAll<T>(selector));
  }

  /**
   * Create element with attributes and content
   */
  create<K extends keyof HTMLElementTagNameMap>(
    tag: K,
    attrs?: Record<string, any>,
    content?: string | Element | Element[]
  ): HTMLElementTagNameMap[K] {
    const element = document.createElement(tag);
    
    // Set attributes
    if (attrs) {
      Object.entries(attrs).forEach(([key, value]) => {
        if (key === 'class') {
          element.className = value;
        } else if (key === 'style' && typeof value === 'object') {
          Object.assign(element.style, value);
        } else if (key.startsWith('data-')) {
          element.setAttribute(key, value);
        } else if (key in element) {
          (element as any)[key] = value;
        } else {
          element.setAttribute(key, value);
        }
      });
    }
    
    // Add content
    if (content) {
      if (typeof content === 'string') {
        element.innerHTML = content;
      } else if (Array.isArray(content)) {
        content.forEach(child => element.appendChild(child));
      } else {
        element.appendChild(content);
      }
    }
    
    return element;
  }

  /**
   * Add event listener with delegation support
   */
  on<K extends keyof HTMLElementEventMap>(
    target: ElementSelector,
    event: K | string,
    selectorOrHandler: string | EventHandler<HTMLElementEventMap[K]>,
    handler?: EventHandler<HTMLElementEventMap[K]>,
    options?: EventOptions
  ): () => void {
    const element = this.$(target);
    if (!element) return () => {};
    
    // Determine if using delegation
    const isDelegated = typeof selectorOrHandler === 'string';
    const delegateSelector = isDelegated ? selectorOrHandler : null;
    const eventHandler = isDelegated ? handler! : selectorOrHandler as EventHandler;
    
    if (isDelegated && delegateSelector) {
      // Event delegation
      const delegatedHandler: EventHandler = (e) => {
        const target = e.target as Element;
        const delegateTarget = target.closest(delegateSelector);
        
        if (delegateTarget && element.contains(delegateTarget)) {
          // Call handler with delegated target as context
          eventHandler.call(delegateTarget, e as any);
        }
      };
      
      // Store for cleanup
      const key = `${event}-${delegateSelector}`;
      if (!this.eventDelegates.has(key)) {
        this.eventDelegates.set(key, new Map());
      }
      this.eventDelegates.get(key)!.set(element as any, [...(this.eventDelegates.get(key)!.get(element as any) || []), delegatedHandler]);
      
      element.addEventListener(event, delegatedHandler, options);
      
      return () => {
        element.removeEventListener(event, delegatedHandler);
        const handlers = this.eventDelegates.get(key)?.get(element as any);
        if (handlers) {
          const index = handlers.indexOf(delegatedHandler);
          if (index > -1) handlers.splice(index, 1);
        }
      };
    } else {
      // Direct event binding
      element.addEventListener(event, eventHandler as EventListener, options);
      return () => element.removeEventListener(event, eventHandler as EventListener);
    }
  }

  /**
   * Remove event listener
   */
  off<K extends keyof HTMLElementEventMap>(
    target: ElementSelector,
    event: K | string,
    handler?: EventHandler<HTMLElementEventMap[K]>
  ): void {
    const element = this.$(target);
    if (!element) return;
    
    if (handler) {
      element.removeEventListener(event, handler as EventListener);
    }
  }

  /**
   * DOM ready handler
   */
  ready(callback: () => void): void {
    if (document.readyState !== 'loading') {
      callback();
    } else {
      document.addEventListener('DOMContentLoaded', callback, { once: true });
    }
  }

  /**
   * Get/set element text content
   */
  text(element: ElementSelector, value?: string): string | undefined {
    const el = this.$(element);
    if (!el) return undefined;
    
    if (value !== undefined) {
      el.textContent = value;
      return value;
    }
    
    return el.textContent || '';
  }

  /**
   * Get/set element HTML
   */
  html(element: ElementSelector, value?: string): string | undefined {
    const el = this.$(element) as HTMLElement;
    if (!el) return undefined;
    
    if (value !== undefined) {
      el.innerHTML = value;
      return value;
    }
    
    return el.innerHTML;
  }

  /**
   * Get/set input value
   */
  val(element: ElementSelector, value?: string | number | boolean): string | undefined {
    const el = this.$(element) as HTMLInputElement;
    if (!el) return undefined;
    
    if (value !== undefined) {
      el.value = String(value);
      return String(value);
    }
    
    return el.value;
  }

  /**
   * Add CSS class
   */
  addClass(element: ElementSelector, ...classes: string[]): void {
    const el = this.$(element);
    if (el) {
      el.classList.add(...classes);
    }
  }

  /**
   * Remove CSS class
   */
  removeClass(element: ElementSelector, ...classes: string[]): void {
    const el = this.$(element);
    if (el) {
      el.classList.remove(...classes);
    }
  }

  /**
   * Toggle CSS class
   */
  toggleClass(element: ElementSelector, className: string, force?: boolean): boolean {
    const el = this.$(element);
    if (el) {
      return el.classList.toggle(className, force);
    }
    return false;
  }

  /**
   * Check if element has class
   */
  hasClass(element: ElementSelector, className: string): boolean {
    const el = this.$(element);
    return el ? el.classList.contains(className) : false;
  }

  /**
   * Show element
   */
  show(element: ElementSelector): void {
    const el = this.$(element) as HTMLElement;
    if (el) {
      el.style.display = '';
      if (getComputedStyle(el).display === 'none') {
        el.style.display = 'block';
      }
    }
  }

  /**
   * Hide element
   */
  hide(element: ElementSelector): void {
    const el = this.$(element) as HTMLElement;
    if (el) {
      el.style.display = 'none';
    }
  }

  /**
   * Toggle element visibility
   */
  toggle(element: ElementSelector, show?: boolean): void {
    const el = this.$(element) as HTMLElement;
    if (!el) return;
    
    if (show === undefined) {
      show = el.style.display === 'none';
    }
    
    if (show) {
      this.show(el);
    } else {
      this.hide(el);
    }
  }

  /**
   * Get element attribute
   */
  attr(element: ElementSelector, name: string, value?: string): string | null | undefined {
    const el = this.$(element);
    if (!el) return undefined;
    
    if (value !== undefined) {
      el.setAttribute(name, value);
      return value;
    }
    
    return el.getAttribute(name);
  }

  /**
   * Remove element attribute
   */
  removeAttr(element: ElementSelector, name: string): void {
    const el = this.$(element);
    if (el) {
      el.removeAttribute(name);
    }
  }

  /**
   * Debounce function
   */
  debounce<T extends (...args: any[]) => any>(
    fn: T,
    delay: number
  ): (...args: Parameters<T>) => void {
    let timeoutId: NodeJS.Timeout;
    
    return (...args: Parameters<T>) => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => fn(...args), delay);
    };
  }

  /**
   * Throttle function
   */
  throttle<T extends (...args: any[]) => any>(
    fn: T,
    delay: number
  ): (...args: Parameters<T>) => void {
    let lastCall = 0;
    let timeoutId: NodeJS.Timeout | null = null;
    
    return (...args: Parameters<T>) => {
      const now = Date.now();
      const timeSinceLastCall = now - lastCall;
      
      if (timeSinceLastCall >= delay) {
        lastCall = now;
        fn(...args);
      } else if (!timeoutId) {
        timeoutId = setTimeout(() => {
          lastCall = Date.now();
          timeoutId = null;
          fn(...args);
        }, delay - timeSinceLastCall);
      }
    };
  }

  /**
   * Animate element
   */
  animate(
    element: ElementSelector,
    keyframes: Keyframe[] | PropertyIndexedKeyframes,
    options?: number | KeyframeAnimationOptions
  ): Animation | undefined {
    const el = this.$(element);
    if (el) {
      return el.animate(keyframes, options);
    }
    return undefined;
  }

  /**
   * Get element offset
   */
  offset(element: ElementSelector): { top: number; left: number } | undefined {
    const el = this.$(element) as HTMLElement;
    if (!el) return undefined;
    
    const rect = el.getBoundingClientRect();
    return {
      top: rect.top + window.pageYOffset,
      left: rect.left + window.pageXOffset
    };
  }

  /**
   * Get element dimensions
   */
  dimensions(element: ElementSelector): { width: number; height: number } | undefined {
    const el = this.$(element) as HTMLElement;
    if (!el) return undefined;
    
    return {
      width: el.offsetWidth,
      height: el.offsetHeight
    };
  }

  /**
   * Observe element mutations
   */
  observe(
    element: ElementSelector,
    callback: MutationCallback,
    options?: MutationObserverInit
  ): () => void {
    const el = this.$(element);
    if (!el) return () => {};
    
    const observer = new MutationObserver(callback);
    observer.observe(el, options || {
      childList: true,
      subtree: true,
      attributes: true
    });
    
    return () => observer.disconnect();
  }
}

// Create static interface
const instance = DOMHelper.getInstance();

const dom: DOMHelperStatic & DOMHelper = Object.assign(
  // Static methods
  {
    $: instance.$.bind(instance),
    $$: instance.$$.bind(instance),
    create: instance.create.bind(instance),
    ready: instance.ready.bind(instance),
    debounce: instance.debounce.bind(instance),
    throttle: instance.throttle.bind(instance),
  },
  // Instance
  instance
);

export default dom;