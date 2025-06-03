/**
 * DOM Ready Observer for dynamic module initialization
 * Ensures modules only initialize when their target elements exist
 */

import { DebugLogger } from './debug';

type ReadyCallback = () => void;

/**
 * Observes the DOM and fires EXACTLY ONCE when a matching element appears.
 * Uses WeakSet to prevent duplicate initialization.
 */
export function observeCustomerFormReady(cb: ReadyCallback): void {
  const targetSelector = '#customerForm, .customer-form, [data-module="customer"]';
  const seen = new WeakSet<Element>();

  let observer: MutationObserver;
  let timeoutId: NodeJS.Timeout;

  function invokeOnce(el: Element): void {
    if (seen.has(el)) {
      DebugLogger.log('DOMObserver', 'Element already processed, skipping', { element: el });
      return;
    }
    
    seen.add(el);
    DebugLogger.log('DOMObserver', '✓ Customer form detected, initializing module...', {
      element: el.tagName,
      id: el.id,
      classes: el.className
    });
    
    // Clear timeout
    if (timeoutId) clearTimeout(timeoutId);
    
    // Give DOM a chance to fully render
    requestAnimationFrame(() => {
      cb();
      if (observer) observer.disconnect();
      DebugLogger.log('DOMObserver', 'Observer disconnected after successful init');
    });
  }

  // Check if element already exists
  const initial = document.querySelector(targetSelector);
  if (initial) {
    DebugLogger.log('DOMObserver', 'Element already in DOM, initializing immediately');
    invokeOnce(initial);
    return;
  }

  DebugLogger.log('DOMObserver', 'Starting observation for: ' + targetSelector);

  observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      // Check added nodes
      for (const node of mutation.addedNodes) {
        if (node instanceof HTMLElement) {
          // Check the node itself
          if (node.matches?.(targetSelector)) {
            invokeOnce(node);
            return;
          }
          
          // Check children
          const child = node.querySelector(targetSelector);
          if (child) {
            invokeOnce(child);
            return;
          }
        }
      }
    }
  });

  // Observe with performance in mind
  observer.observe(document.body, { 
    childList: true, 
    subtree: true,
    // Optimize by not watching attributes
    attributes: false,
    characterData: false
  });
  
  // Timeout fallback
  timeoutId = setTimeout(() => {
    DebugLogger.warn('DOMObserver', 'Timeout: Customer form not found after 10s');
    observer.disconnect();
  }, 10000);
}

/**
 * Generic observer for any module
 */
export function observeModuleReady(
  moduleSelector: string, 
  moduleName: string,
  cb: ReadyCallback
): void {
  const seen = new WeakSet<Element>();

  function invokeOnce(el: Element): void {
    if (seen.has(el)) return;
    
    seen.add(el);
    DebugLogger.log('DOMObserver', `✓ ${moduleName} element detected`, { element: el });
    
    requestAnimationFrame(() => {
      cb();
      observer.disconnect();
    });
  }

  const initial = document.querySelector(moduleSelector);
  if (initial) {
    invokeOnce(initial);
    return;
  }

  const observer = new MutationObserver((mutations) => {
    for (const mutation of mutations) {
      for (const node of mutation.addedNodes) {
        if (node instanceof HTMLElement) {
          if (node.matches?.(moduleSelector)) {
            invokeOnce(node);
            return;
          }
          
          const child = node.querySelector(moduleSelector);
          if (child) {
            invokeOnce(child);
            return;
          }
        }
      }
    }
  });

  observer.observe(document.body, { 
    childList: true, 
    subtree: true,
    attributes: false,
    characterData: false
  });
}