/**
 * Test Module Helper
 * Creates module instances suitable for testing
 */

import { vi } from 'vitest';
import Module from '../../core/Module';
import DOMHelperMock from '../mocks/DOMHelper.mock';

export class TestModule extends Module {
  constructor(name: string) {
    super(name);
    
    // Override DOM helper with mock
    (this as any).dom = DOMHelperMock;
  }
  
  // Mock implementations
  setup() {
    // Empty setup
  }
  
  bindEvents() {
    // Empty bind events
  }
  
  cleanup() {
    // Empty cleanup
  }
  
  subscribeToState() {
    // Empty implementation for abstract method
  }
}

/**
 * Create a test-friendly module instance
 */
export function createTestModule<T extends Module>(
  ModuleClass: new (...args: any[]) => T,
  ...args: any[]
): T {
  const module = new ModuleClass(...args);
  
  // Override the dom property with mock
  (module as any).dom = DOMHelperMock;
  
  // Mock the on method to avoid DOM issues
  (module as any).on = vi.fn(() => vi.fn());
  
  // Skip DOM-dependent lifecycle methods if they exist
  if (module.bindEvents) {
    const originalBindEvents = module.bindEvents.bind(module);
    module.bindEvents = vi.fn(() => {
      try {
        originalBindEvents();
      } catch (e) {
        // Ignore DOM errors in tests
      }
    });
  }
  
  return module;
}