/**
 * FreshPlanApp Mock for Testing
 */

import { vi } from 'vitest';

// Mock modules
const mockModules = new Map();

export const FreshPlanAppMock = {
  init: vi.fn().mockResolvedValue(undefined),
  destroy: vi.fn().mockResolvedValue(undefined),
  isInitialized: vi.fn(() => true),
  getModule: vi.fn((name) => mockModules.get(name)),
  modules: mockModules,
  addModule: vi.fn((name, module) => {
    mockModules.set(name, module);
  }),
  // Mock the error handler to avoid DOM issues
  handleInitError: vi.fn()
};

export default FreshPlanAppMock;